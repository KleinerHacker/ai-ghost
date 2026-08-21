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
        assertEquals("Öffnen...", german.getProperty("menu.file.open"))
        assertEquals("Veröffentlichen", german.getProperty("menu.publish"))
    }

    /**
     * Use case: the bundle configured in [Messages] resolves, so the application finds its texts.
     */
    @Test
    fun configuredBundleResolves() {
        assertEquals("AI Ghost", Messages["window.main.title"])
    }
}
