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

package org.pcsoft.app.aighost.app.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.PreferencesStorage
import java.io.File
import java.io.IOException
import java.util.Properties

/**
 * Developer tests for [PreferencesLoader].
 *
 * The dialogs of the loader need a running UI, so only the decision which reaction a read failure
 * deserves and the texts these dialogs are built from are checked here.
 */
class PreferencesLoaderTest {

    private val file = File("preferences.json")

    /**
     * Use case: the preferences file does not exist yet, which is the normal first start of the
     * application - the defaults are taken without bothering the user.
     */
    @Test
    fun missingFileResetsWithoutAsking() {
        assertEquals(
            PreferencesLoader.Reaction.RESET,
            PreferencesLoader.reactionFor(PreferencesStorage.Error.NotFound(file))
        )
    }

    /**
     * Use case: the preferences file holds content the application cannot understand - the user
     * decides whether the settings are thrown away, because they may be recoverable by hand.
     */
    @Test
    fun malformedFileAsksBeforeReset() {
        assertEquals(
            PreferencesLoader.Reaction.ASK_FOR_RESET,
            PreferencesLoader.reactionFor(PreferencesStorage.Error.Malformed(file, IOException("broken")))
        )
    }

    /**
     * Use case: the preferences file cannot be read, for example because of its permissions - the
     * user decides whether the settings are replaced by the defaults.
     */
    @Test
    fun unreadableFileAsksBeforeReset() {
        assertEquals(
            PreferencesLoader.Reaction.ASK_FOR_RESET,
            PreferencesLoader.reactionFor(PreferencesStorage.Error.Unreadable(file, IOException("denied")))
        )
    }

    /**
     * Use case: a directory sits where the preferences file is expected - the application must not
     * remove it on its own and gives up instead.
     */
    @Test
    fun directoryInsteadOfFileFails() {
        assertEquals(
            PreferencesLoader.Reaction.FAIL,
            PreferencesLoader.reactionFor(PreferencesStorage.Error.NotAFile(file))
        )
    }

    /**
     * Use case: the failure that stops the application names its own reason, while every other
     * failure falls back to a generic description.
     */
    @Test
    fun reasonKeyDescribesTheFailure() {
        assertEquals(
            "preferences.error.notAFile",
            PreferencesLoader.reasonKeyOf(PreferencesStorage.Error.NotAFile(file))
        )
        assertEquals(
            "preferences.error.unknown",
            PreferencesLoader.reasonKeyOf(PreferencesStorage.Error.NotFound(file))
        )
    }

    /**
     * Use case: every text the dialogs of the loader are built from exists in the base bundle and
     * in the German translation, so no dialog shows a raw key.
     */
    @Test
    fun everyDialogTextIsTranslated() {
        val keys = listOf(
            "button.ok",
            "button.cancel",
            "preferences.reset.title",
            "preferences.reset.header",
            "preferences.reset.content",
            "preferences.reset.button",
            "preferences.error.title",
            "preferences.error.header",
            "preferences.error.notAFile",
            "preferences.error.unknown"
        )

        listOf("bundle.properties", "bundle_de.properties").forEach { fileName ->
            val bundle = properties(fileName)
            keys.forEach { key ->
                assertTrue(
                    bundle.getProperty(key)?.isNotBlank() == true,
                    "key $key is missing or empty in $fileName"
                )
            }
        }
    }

    private fun properties(fileName: String): Properties {
        val stream = requireNotNull(javaClass.getResourceAsStream("/messages/$fileName")) {
            "bundle not found: $fileName"
        }
        return Properties().apply { stream.reader(Charsets.UTF_8).use { load(it) } }
    }
}
