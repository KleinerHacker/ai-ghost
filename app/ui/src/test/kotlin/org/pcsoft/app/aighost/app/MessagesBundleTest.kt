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

package org.pcsoft.app.aighost.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Properties

/**
 * Developer tests for the message bundles backing the I18N of the UI.
 *
 * The bundles are read straight from the classpath instead of through [java.util.ResourceBundle], so
 * a locale never falls back to another file and each translation is checked on its own.
 */
class MessagesBundleTest {

    private fun properties(fileName: String): Properties {
        val stream = requireNotNull(javaClass.getResourceAsStream("/messages/$fileName")) {
            "bundle not found: $fileName"
        }
        return Properties().apply { stream.reader(Charsets.UTF_8).use { load(it) } }
    }

    private val base get() = properties("bundle.properties")
    private val german get() = properties("bundle_de.properties")

    /**
     * Use case: the German translation carries exactly the keys of the English base bundle, so no
     * UI text falls back to English and no unused key survives a rename.
     */
    @Test
    fun germanTranslationMirrorsTheBaseBundle() {
        assertEquals(
            base.stringPropertyNames().sorted(),
            german.stringPropertyNames().sorted(),
            "German bundle and base bundle must carry the same keys"
        )
    }

    /**
     * Use case: no bundle entry is empty, so every menu entry shows a readable text.
     */
    @Test
    fun noTranslationIsEmpty() {
        listOf("bundle.properties" to base, "bundle_de.properties" to german).forEach { (name, bundle) ->
            bundle.stringPropertyNames().forEach { key ->
                assertTrue(bundle.getProperty(key).isNotBlank(), "key $key is empty in $name")
            }
        }
    }

    /**
     * Use case: the bundles are stored as UTF-8, so German umlauts arrive intact in the UI.
     */
    @Test
    fun germanTranslationKeepsUmlauts() {
        assertEquals("Öffnen...", german.getProperty("window.main.menu.file.open"))
        assertEquals("Veröffentlichen", german.getProperty("window.main.menu.publish"))
    }
}
