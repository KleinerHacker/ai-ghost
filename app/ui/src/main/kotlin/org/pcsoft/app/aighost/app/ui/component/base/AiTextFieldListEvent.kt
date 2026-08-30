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

package org.pcsoft.app.aighost.app.ui.component.base

import javafx.event.Event
import javafx.event.EventTarget
import javafx.event.EventType

/**
 * Request an [AiTextFieldList] publishes.
 *
 * The list reports three different things, which a plain `ActionEvent` could not tell apart: its
 * type says whether another entry is wanted ([ADD_ENTRY]), whether an entry is to be removed
 * ([DELETE_ENTRY]) or whether the AI was asked for the text of an entry ([CREATE_AI_TEXT]).
 *
 * Every request names the position it is about, because whoever owns the entries works on that
 * position: [index] is the entry the request came from, and the position the new entry would take
 * for [ADD_ENTRY].
 *
 * @param source the list the request came from
 * @param target the list the request is aimed at
 * @param eventType what is being asked for
 * @param index position the request is about
 */
class AiTextFieldListEvent(
    source: Any,
    target: EventTarget,
    eventType: EventType<AiTextFieldListEvent>,
    val index: Int
) : Event(source, target, eventType) {

    companion object {
        /** Any request of a list, no matter which of its buttons it came from. */
        val ANY: EventType<AiTextFieldListEvent> =
            EventType(Event.ANY, "AI_TEXT_FIELD_LIST")

        /** Request of the plus, asking for another entry at the end of the list. */
        val ADD_ENTRY: EventType<AiTextFieldListEvent> =
            EventType(ANY, "AI_TEXT_FIELD_LIST_ADD")

        /** Request of the bin of an entry, asking for that entry to be removed. */
        val DELETE_ENTRY: EventType<AiTextFieldListEvent> =
            EventType(ANY, "AI_TEXT_FIELD_LIST_DELETE")

        /** Request of the wand of an entry, asking the AI for the text of that entry. */
        val CREATE_AI_TEXT: EventType<AiTextFieldListEvent> =
            EventType(ANY, "AI_TEXT_FIELD_LIST_CREATE")
    }
}
