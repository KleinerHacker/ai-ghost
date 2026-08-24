/*
 * Copyright (c) KleinerHacker alias Pfeiffer C Soft 2026.
 * This work is licensed under the Apache License, Version 2.0.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations.
 */

package org.pcsoft.app.aighost.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.core.JacksonException
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.meta.Meta
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Holds the project the user is working on and reads and writes it as an archive of its parts.
 *
 * Exactly one project is open at a time: [current] is the project in effect, [currentFile] the file
 * it belongs to. A project that was never saved has no file yet, so [currentFile] is `null` until
 * [save] wrote it somewhere. The storage starts with a fresh [Project], so the application always
 * has something to show.
 *
 * The project is a field of the storage and always the same instance: [new] and [load] write into it
 * instead of replacing it, so whoever holds the project keeps working on the open one. The project
 * is a plain mutable value object and reports nothing, so a reader takes the values when it needs
 * them.
 *
 * Neither operation throws for an expected failure: everything that can go wrong is returned as an
 * [Error] on the left side of an [Either], so the caller decides what the user is told.
 */
object ProjectStorage {

    /** The project currently open, a fresh one until another is loaded. */
    var current: Project = createEmptyProject()
        private set

    /** The file [current] was read from or written to, `null` while it was never saved. */
    var currentFile: File? = null
        private set

    /**
     * Indicates whether the current file has already been saved.
     *
     * This property returns `true` if there is an existing file associated with the current state,
     * meaning the current state has already been saved to persistent storage. It returns `false`
     * if no file is associated, implying the current state is unsaved or new.
     */
    val alreadySaved: Boolean
        get() = currentFile != null

    /**
     * Closes the open project and starts a fresh one.
     *
     * The project keeps its identity and is set back to the defaults. The fresh project has no file,
     * so the next [save] needs an explicit one.
     */
    fun new() {
        current = createEmptyProject()
        currentFile = null
    }

    /**
     * Reads the project from [file] and opens it.
     *
     * On success the values of the document are written into [current] and [currentFile] points at
     * [file]. A failure leaves the open project untouched, so a broken file never closes what the
     * user is working on.
     *
     * Returns [Error.NotFound] when the file does not exist, [Error.NotAFile] when the path exists but
     * is not a regular file, [Error.Malformed] when the content is not the expected document, and
     * [Error.Unreadable] when the file cannot be read at all.
     */
    fun load(file: File): Either<Error, Unit> {
        if (!file.exists())
            return Error.NotFound(file).left()
        if (!file.isFile)
            return Error.NotAFile(file).left()

        val project = try {
            val parts = StorageIO.loadFromZip(file, Meta::class, Design::class, Book::class)
            // A file that is not an archive at all carries no entry, so it would open as a project
            // without a single part. That is not a project document, it is a broken file.
            if (parts.isEmpty())
                return Error.Malformed(file, IOException("The file holds no project part")).left()

            Project(parts)
        } catch (e: JacksonException) {
            return Error.Malformed(file, e).left()
        } catch (e: IOException) {
            return Error.Unreadable(file, e).left()
        }

        current = project
        currentFile = file

        return Unit.right()
    }

    /**
     * Writes the open project to [file], creating the parent directories if they do not exist yet.
     *
     * The document is written to a temporary file next to the target and moved into place afterwards,
     * so a crash during the write leaves the previous document intact instead of a half written file.
     * A successful write makes [file] the file of the open project, which turns the first save of a
     * fresh project into a "save as".
     *
     * Returns [Error.NoFile] when neither [file] nor [currentFile] names a target, [Error.NotAFile]
     * when the path exists but is not a regular file, so an existing directory is never replaced, and
     * [Error.Unreadable] when the file cannot be written.
     */
    fun save(file: File? = currentFile): Either<Error, Unit> {
        val target = file ?: return Error.NoFile.left()

        if (target.exists() && !target.isFile)
            return Error.NotAFile(target).left()

        return try {
            target.parentFile?.mkdirs()

            val temporary = File.createTempFile(target.name, ".tmp", target.parentFile)
            try {
                StorageIO.saveToZip(temporary, *current.parts.values.toTypedArray())
                Files.move(
                    temporary.toPath(),
                    target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            } finally {
                temporary.delete()
            }

            currentFile = target
            Unit.right()
        } catch (e: IOException) {
            Error.Unreadable(target, e).left()
        }
    }

    /**
     * Creates and returns a new instance of an empty project.
     *
     * The created project contains default initializations for its three main parts:
     * metadata, design settings, and manuscript content.
     *
     * @return A new instance of the `Project` class, initialized with default metadata, design, and book content.
     */
    private fun createEmptyProject(): Project = Project()

    /**
     * Reason why opening or storing a project failed.
     *
     * The storage never throws for these cases, it returns them as the left side of an [Either], so a
     * caller has to decide what to do: [NoFile] and [NotFound] are answered by asking the user for a
     * path, while [NotAFile], [Unreadable] and [Malformed] point at something the user should be told
     * about.
     *
     * @property file The file the failing operation worked on, `null` when there was none.
     */
    sealed interface Error {

        val file: File?

        /**
         * No file was named for the operation and no project file is known yet.
         *
         * This is the answer to saving a project that was never stored, so the application asks the
         * user where it should go.
         */
        data object NoFile : Error {
            override val file: File? = null
        }

        /**
         * No file is stored at the given path.
         *
         * @property file The file that was looked for.
         */
        data class NotFound(override val file: File) : Error

        /**
         * The path exists but is not a regular file, for example a directory.
         *
         * @property file The path that was determined to not be a regular file.
         */
        data class NotAFile(override val file: File) : Error

        /**
         * The file exists but could not be read from or written to, for example because of missing
         * permissions or a full disk.
         *
         * @property file The file the operation worked on.
         * @property cause The underlying I/O failure.
         */
        data class Unreadable(override val file: File, val cause: Throwable) : Error

        /**
         * The file was read, but its content is not the project document that was expected.
         *
         * @property file The file that was read.
         * @property cause The underlying parse failure.
         */
        data class Malformed(override val file: File, val cause: Throwable) : Error
    }
}
