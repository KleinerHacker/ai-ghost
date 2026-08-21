package org.pcsoft.app.aighost.app

import java.util.ResourceBundle

/**
 * Access to the translated texts of the user interface.
 *
 * The bundle follows the default locale of the system; English is the base translation, German is
 * shipped alongside it.
 */
object Messages {

    /** Base name of the resource bundle holding every UI text. */
    const val BUNDLE_NAME: String = "messages.bundle"

    /** The bundle for the current default locale. */
    val bundle: ResourceBundle by lazy { ResourceBundle.getBundle(BUNDLE_NAME) }

    /**
     * Reads a translated text.
     *
     * @param key key of the text inside the bundle
     * @return the translated text
     */
    operator fun get(key: String): String = bundle.getString(key)
}
