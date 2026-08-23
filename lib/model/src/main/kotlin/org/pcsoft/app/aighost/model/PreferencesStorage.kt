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
 * Keeps the [Preferences] of the current user and backs them by a JSON file.
 *
 * The preferences are a field of the storage: [current] hands out the one instance the application
 * works on, read from [defaultFile] on the first access. A file changed from outside while the
 * application runs takes effect on the next start or on an explicit [load].
 *
 * Settings are changed on that instance, which is a plain mutable value object and reports nothing,
 * so whoever needs a setting reads it when it is needed. A change is only written when [save] is
 * called, so the file follows the preferences instead of the preferences following the file.
 *
 * The file lives in the user's home directory and is written with indentation, so it can be edited
 * by hand. Nothing throws for an expected failure: [load] and [save] return an [Either] carrying an
 * [Error] on the left, so a caller can tell the user about a file that cannot be read or written
 * without having to handle the preferences itself - a failure to read leaves the defaults in effect.
 */
object PreferencesStorage {

    private const val FILE_NAME = ".ai-ghost/preferences.json"

    /**
     * The file the preferences are read from and written to.
     *
     * Points into the user's home directory and can only be redirected from within this module, so
     * the application always works on the preferences of the current user. Redirecting it drops the
     * values read so far, so the next access reads the new file into [current].
     */
    var defaultFile: File = File(System.getProperty("user.home"), FILE_NAME)
        internal set(value) {
            field = value
            loaded = false
        }

    private val preferences = Preferences()

    private var loaded = false

    /**
     * The preferences the application works on.
     *
     * Always the same instance, so whoever holds it keeps working on the values in effect across a
     * [load]. [defaultFile] is read on the first access and the values are kept, so neither a
     * repeated access nor a file changed from outside touches the disk again. A file that is missing
     * or cannot be read leaves the defaults in place, because a failure to read is not a reason to
     * leave the application without settings.
     */
    val current: Preferences
        get() {
            if (!loaded) {
                load()
            }

            return preferences
        }

    /**
     * Reads [defaultFile] again and writes its content into [current].
     *
     * The instance is kept, so everybody working on it sees the values the file brought. A failure
     * puts the defaults in place, so the application is never without settings.
     *
     * Returns [Error.NotFound] when nothing is stored yet, [Error.NotAFile] when the path exists but
     * is not a regular file, [Error.Malformed] when the content is not the expected JSON document,
     * and [Error.Unreadable] when the file cannot be read at all.
     */
    fun load(): Either<Error, Unit> {
        loaded = true

        return read()
            .onRight { apply(it) }
            .onLeft { apply(Preferences()) }
            .map { }
    }

    /**
     * Writes the preferences of [current] to [defaultFile], creating the parent directories if they
     * do not exist yet.
     *
     * The document is written to a temporary file next to the target and moved into place
     * afterwards, so a crash during the write leaves the previous preferences intact instead of a
     * half written file.
     *
     * Returns [Error.NotAFile] when the path exists but is not a regular file, so an existing
     * directory is never replaced, and [Error.Unreadable] when the file cannot be written.
     */
    fun save(): Either<Error, Unit> = write(current)

    /** Takes the values of [read] over into the instance the application works on. */
    private fun apply(read: Preferences) {
        preferences.recentOpened = read.recentOpened
        preferences.themeMode = read.themeMode
    }

    /** Reads [defaultFile] into preferences of its own, without touching [current]. */
    private fun read(): Either<Error, Preferences> {
        val file = defaultFile

        if (!file.exists())
            return Error.NotFound(file).left()
        if (!file.isFile)
            return Error.NotAFile(file).left()

        return try {
            StorageMapper.mapper.readValue(file, Preferences::class.java).right()
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
                StorageMapper.mapper.writeValue(temporary, preferences)
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
     * is usually answered with the defaults that stay in effect, while [NotAFile], [Unreadable] and
     * [Malformed] point at something the user should be told about.
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
