package org.pcsoft.app.aighost.model.common

/**
 * Horizontal placement of a piece of text on the page.
 *
 * The value is part of a [StyleData] and is written to JSON by its constant name, so a stored
 * document stays readable when new constants are added.
 */
enum class Alignment {

    /** Text starts at the left margin, the right edge stays ragged. */
    LEFT,

    /** Text is centred between the margins. */
    CENTER,

    /** Text ends at the right margin, the left edge stays ragged. */
    RIGHT,

    /** Text is stretched so both margins are flush. */
    BLOCK
}
