package org.pcsoft.app.aighost.model.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Font a piece of text is rendered with, stored independently of any UI toolkit.
 *
 * @property name Family name of the font, as the rendering side resolves it.
 * @property size Font size in points.
 * @property bold Whether the text is drawn in a bold weight.
 * @property italic Whether the text is drawn slanted.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class FontData(
    val name: String,
    val size: Int,
    val bold: Boolean,
    val italic: Boolean
)
