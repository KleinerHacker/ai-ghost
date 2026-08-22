package org.pcsoft.app.aighost.model.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Appearance of a single piece of text: how it is drawn and how it is placed.
 *
 * @property font Font the text is rendered with.
 * @property alignment Horizontal placement of the text, [Alignment.LEFT] by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class StyleData(
    val font: FontData,
    val alignment: Alignment = Alignment.LEFT
)
