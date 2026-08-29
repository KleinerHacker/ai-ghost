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
import org.pcsoft.app.aighost.model.pref.Preferences
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Reads and writes the [Preferences] of the current user as a JSON file.
 *
 * The storage holds nothing: [load] hands the preferences of the file to its caller and [save] writes
 * the preferences it is given. Whoever works with the preferences keeps them - in this application
 * that is the user interface, which offers them as a property model of `ai-ghost-fx-model`. A file
 * changed from outside while the application runs therefore takes effect on an explicit [load].
 *
 * The file lives in the user's home directory and is written with indentation, so it can be edited
 * by hand. Nothing throws for an expected failure: [load] and [save] return an [Either] carrying an
 * [Error] on the left, so a caller can tell the user about a file that cannot be read or written -
 * and decide for itself whether the defaults take the place of what could not be read.
 */
object PreferencesStorage {

    private const val FILE_NAME = ".ai-ghost/preferences.yml"

    /**
     * The file the preferences are read from and written to.
     *
     * Points into the user's home directory and can only be redirected from within this module, so
     * the application always works on the preferences of the current user.
     */
    var defaultFile: File = File(System.getProperty("user.home"), FILE_NAME)
        internal set

    /**
     * Reads [defaultFile] and hands out the preferences it holds.
     *
     * Nothing is kept here, so a failure leaves the caller with whatever it worked on before. What a
     * failure means is its decision - the defaults of [Preferences] are one answer to it.
     *
     * Returns [Error.NotFound] when nothing is stored yet, [Error.NotAFile] when the path exists but
     * is not a regular file, [Error.Malformed] when the content is not the expected JSON document,
     * and [Error.Unreadable] when the file cannot be read at all.
     */
    fun load(): Either<Error, Preferences> = read()

    /**
     * Writes [preferences] to [defaultFile], creating the parent directories if they do not exist yet.
     *
     * The document is written to a temporary file next to the target and moved into place
     * afterwards, so a crash during the write leaves the previous preferences intact instead of a
     * half written file.
     *
     * Returns [Error.NotAFile] when the path exists but is not a regular file, so an existing
     * directory is never replaced, and [Error.Unreadable] when the file cannot be written.
     *
     * @param preferences The preferences to store.
     */
    fun save(preferences: Preferences): Either<Error, Unit> = write(preferences)

    /** Reads [defaultFile] into preferences of its own. */
    private fun read(): Either<Error, Preferences> {
        val file = defaultFile

        if (!file.exists())
            return Error.NotFound(file).left()
        if (!file.isFile)
            return Error.NotAFile(file).left()

        return try {
            StorageIo.yamlMapper.readValue(file, Preferences::class.java).right()
        } catch (e: JacksonException) {
            Error.Malformed(file, e).left()
        } catch (e: IOException) {
            Error.Unreadable(file, e).left()
        }
    }

    /** Writes [preferences] to [defaultFile]. */
    private fun write(preferences: Preferences): Either<Error, Unit> {
        val file = defaultFile

        if (file.exists() && !file.isFile)
            return Error.NotAFile(file).left()

        return try {
            file.parentFile?.mkdirs()

            val temporary = File.createTempFile(file.name, ".tmp", file.parentFile)
            try {
                StorageIo.jsonMapper.writeValue(temporary, preferences)
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
     * Reason why reading or writing the preferences failed.
     *
     * The storage never throws for these cases, it returns them as the left side of an [Either], so
     * a caller has to decide what to do: [NotFound] is the normal state of a fresh installation and
     * is usually answered with the defaults, while [NotAFile], [Unreadable] and [Malformed] point at
     * something the user should be told about.
     *
     * @property file The file the failing operation worked on.
     */
    sealed interface Error {

        val file: File

        /**
         * No file is stored at the given path yet.
         *
         * @property file The file that was looked for.
         */
        data class NotFound(override val file: File) : Error

        /**
         * Represents a failure scenario where the filesystem object being operated on is not a regular file.
         *
         * This error occurs when a path points to something other than a regular file, such as a directory or
         * another type of filesystem object.
         *
         * @property file The file path that was determined to not be a regular file.
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
         * The file was read, but its content is not the JSON document that was expected.
         *
         * @property file The file that was read.
         * @property cause The underlying parse failure.
         */
        data class Malformed(override val file: File, val cause: Throwable) : Error
    }
}
