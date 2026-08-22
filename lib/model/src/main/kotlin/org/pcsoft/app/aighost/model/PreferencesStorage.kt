package org.pcsoft.app.aighost.model

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.pcsoft.app.aighost.model.pref.Preferences
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Reads and writes the [Preferences] of the current user as a JSON file.
 *
 * The file lives in the user's home directory and is written with indentation, so it can be edited
 * by hand. Neither operation throws for an expected failure: both return an [Either] carrying an
 * [Error] on the left, so a caller can start with defaults when nothing is stored yet.
 *
 * Beside the plain [load] and [save] the storage keeps the preferences it last read or wrote. That
 * state is reached through [current] and changed through [update], which writes the new preferences
 * and notifies every registered [Listener] afterwards. Parts of the application that have to follow
 * a setting register a listener instead of polling the file.
 *
 * All functions take the file as an optional parameter, defaulting to [defaultFile].
 */
object PreferencesStorage {

    private const val FILE_NAME = ".ai-ghost/preferences.json"

    /** The file the preferences are read from and written to unless another one is passed in. */
    val defaultFile: File = File(System.getProperty("user.home"), FILE_NAME)

    private val mapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .build()

    private val listeners = CopyOnWriteArrayList<Listener>()

    private var cachedFile: File? = null
    private var cached: Preferences? = null

    /** The preferences currently in effect, read from [defaultFile] on first access. */
    val current: Preferences get() = current(defaultFile)

    /**
     * Returns the preferences currently in effect for [file].
     *
     * The file is read on the first access and kept afterwards, so repeated reads do not touch the
     * disk again. A file that is missing or cannot be read yields the defaults, because a failure to
     * read is not a reason to leave the application without settings.
     */
    fun current(file: File = defaultFile): Preferences {
        val known = cached
        if (known != null && cachedFile == file) {
            return known
        }

        return load(file).getOrElse { Preferences() }.also {
            cached = it
            cachedFile = file
        }
    }

    /**
     * Applies [transform] to the preferences currently in effect and stores the result.
     *
     * Returns the preferences that are in effect afterwards. Every registered [Listener] is notified
     * once the new preferences are written, and only when [transform] actually changed something -
     * a transform that returns equal preferences neither writes nor notifies. If writing fails, the
     * kept state stays untouched and no listener is called.
     */
    fun update(file: File = defaultFile, transform: (Preferences) -> Preferences): Either<Error, Preferences> {
        val old = current(file)
        val new = transform(old)

        if (new == old) {
            return old.right()
        }

        return save(new, file).map {
            cached = new
            cachedFile = file
            listeners.forEach { it.onChanged(old, new) }
            new
        }
    }

    /**
     * Registers [listener] to be notified about every change made through [update].
     *
     * A listener is called on the thread that called [update], after the preferences were written.
     * Registering the same listener twice makes it be called twice.
     */
    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    /** Removes [listener] again; a listener that is not registered is ignored. */
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    /**
     * Reads the preferences from [file].
     *
     * Returns [Error.NotFound] when nothing is stored yet, [Error.NotAFile] when the path exists but
     * is not a regular file, [Error.Malformed] when the content is not the expected JSON document,
     * and [Error.Unreadable] when the file cannot be read at all.
     */
    fun load(file: File = defaultFile): Either<Error, Preferences> {
        if (!file.exists())
            return Error.NotFound(file).left()
        if (!file.isFile)
            return Error.NotAFile(file).left()

        return try {
            mapper.readValue(file, Preferences::class.java).right()
        } catch (e: JacksonException) {
            Error.Malformed(file, e).left()
        } catch (e: IOException) {
            Error.Unreadable(file, e).left()
        }
    }

    /**
     * Writes [preferences] to [file], creating the parent directories if they do not exist yet.
     *
     * The document is written to a temporary file next to the target and moved into place
     * afterwards, so a crash during the write leaves the previous preferences intact instead of a
     * half written file.
     *
     * Returns [Error.NotAFile] when the path exists but is not a regular file, so an existing
     * directory is never replaced, and [Error.Unreadable] when the file cannot be written. Writing
     * directly does not notify any [Listener]; use [update] for a change the application follows.
     */
    fun save(preferences: Preferences, file: File = defaultFile): Either<Error, Unit> {
        if (file.exists() && !file.isFile)
            return Error.NotAFile(file).left()

        return try {
            file.parentFile?.mkdirs()

            val temporary = File.createTempFile(file.name, ".tmp", file.parentFile)
            try {
                mapper.writeValue(temporary, preferences)
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

    /** Notified whenever [update] changed the preferences. */
    fun interface Listener {

        /**
         * Called after the preferences were changed and written.
         *
         * @param old The preferences that were in effect before the change.
         * @param new The preferences that are in effect now.
         */
        fun onChanged(old: Preferences, new: Preferences)
    }

    /**
     * Reason why reading or writing the preferences failed.
     *
     * The storage never throws for these cases, it returns them as the left side of an [Either], so
     * a caller has to decide what to do: [NotFound] is the normal state of a fresh installation and
     * is usually answered with defaults, while [NotAFile], [Unreadable] and [Malformed] point at
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
