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
 * Reads and writes a project as an archive of its parts.
 *
 * The storage holds nothing: [load] hands the project of a file to its caller and [save] writes the
 * project it is given to the file it is given. Which project is open, and which file it belongs to,
 * is a question of the application - in this application the main window answers it.
 *
 * A part of the document this application cannot read is kept as the text it was stored as and is
 * written back unchanged on the next save, so opening a project never loses what it does not know.
 *
 * Neither operation throws for an expected failure: everything that can go wrong is returned as an
 * [Error] on the left side of an [Either], so the caller decides what the user is told.
 */
object ProjectStorage {

    /**
     * Reads the project from [file] and hands it out.
     *
     * A part the document carries but no class is named for is kept as text. A document missing one
     * of the three standard parts is not read with defaults in their place - it is corrupt.
     *
     * A document that lost only a part beyond the standard ones is not lost itself: it is answered
     * with [Error.Incomplete], which carries the project that could be read and the identifiers of
     * what it lost, so the caller can tell the user what a rescue costs and take the project over
     * afterwards. Nothing of that happens behind the user's back - [load] itself hands out only a
     * complete document.
     *
     * Returns [Error.NotFound] when the file does not exist, [Error.NotAFile] when the path exists but
     * is not a regular file, [Error.Corrupt] when the file was read but does not hold every standard
     * part, [Error.Incomplete] when only parts beyond the standard ones got lost, [Error.Malformed]
     * when the content is not the expected document, and [Error.Unreadable] when the file cannot be
     * read at all.
     *
     * @param file The file to read the project from.
     */
    fun load(file: File): Either<Error, Project> {
        if (!file.exists())
            return Error.NotFound(file).left()
        if (!file.isFile)
            return Error.NotAFile(file).left()

        val result = try {
            StorageIo.loadFromZip(file, Meta::class, Design::class, Book::class)
                .fold({ return errorOf(it, file).left() }, { it })
        } catch (e: JacksonException) {
            return Error.Malformed(file, e).left()
        } catch (e: IOException) {
            return Error.Unreadable(file, e).left()
        }

        if (result.lostParts.isNotEmpty())
            return Error.Incomplete(file, result.lostParts, result.project).left()

        return result.project.right()
    }

    /**
     * Writes [project] to [file], creating the parent directories if they do not exist yet.
     *
     * Every part of the project is written, the three standard ones and everything stored beside
     * them, so a part this application cannot read still survives the save.
     *
     * The document is written to a temporary file next to the target and moved into place afterwards,
     * so a crash during the write leaves the previous document intact instead of a half written file.
     *
     * Returns [Error.NotAFile] when the path exists but is not a regular file, so an existing
     * directory is never replaced, and [Error.Unreadable] when the file cannot be written.
     *
     * @param project The project to store.
     * @param file The file to store it in.
     */
    fun save(project: Project, file: File): Either<Error, Unit> {
        if (file.exists() && !file.isFile)
            return Error.NotAFile(file).left()

        return try {
            file.parentFile?.mkdirs()

            val temporary = File.createTempFile(file.name, ".tmp", file.parentFile)
            try {
                StorageIo.saveToZip(temporary, project)
                Files.move(
                    temporary.toPath(),
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            } finally {
                temporary.delete()
            }

            Unit.right()
        } catch (e: IOException) {
            Error.Unreadable(file, e).left()
        }
    }

    /**
     * The failure this storage reports for a failure of the archive.
     *
     * @param error The failure the archive reported.
     * @param file The file that was read.
     */
    private fun errorOf(error: StorageIo.Error, file: File): Error = when (error) {
        is StorageIo.Error.Corrupt -> Error.Corrupt(file, error.missing)
    }

    /**
     * Reason why reading or storing a project failed.
     *
     * The storage never throws for these cases, it returns them as the left side of an [Either], so a
     * caller has to decide what to do: [NotFound] is answered by asking the user for another path,
     * [Incomplete] by asking whether the project may be opened without what it lost, while
     * [NotAFile], [Unreadable], [Corrupt] and [Malformed] point at something the user should be told
     * about.
     *
     * @property file The file the failing operation worked on.
     */
    sealed interface Error {

        val file: File

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

        /**
         * The file was read without trouble, but it does not hold every standard part a project is
         * made of, so the project is corrupt.
         *
         * @property file The file that was read.
         * @property missing The identifiers of the standard parts the file does not hold.
         */
        data class Corrupt(override val file: File, val missing: Set<String>) : Error

        /**
         * The file holds every standard part, but a part beyond them got lost: its entry is gone or
         * its content could not be read.
         *
         * The project can still be worked with, which is why [recovered] carries it: opening it is a
         * decision of the user, because the lost parts are written out of the document on the next
         * save.
         *
         * @property file The file that was read.
         * @property lostParts The identifiers of the parts that got lost.
         * @property recovered The project that could be read, without the lost parts.
         */
        data class Incomplete(
            override val file: File,
            val lostParts: Set<String>,
            val recovered: Project
        ) : Error
    }
}
