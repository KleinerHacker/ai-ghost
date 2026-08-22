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
