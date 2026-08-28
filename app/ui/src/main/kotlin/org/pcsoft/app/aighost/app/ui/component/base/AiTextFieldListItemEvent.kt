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
 * Request an [AiTextFieldListItem] publishes.
 *
 * The item carries two buttons and therefore reports two different things, which a plain
 * `ActionEvent` could not tell apart: its type says whether the AI was asked for a text
 * ([CREATE_AI_TEXT]) or whether the entry is to be removed ([DELETE]).
 *
 * @param source the item the request came from
 * @param target the item the request is aimed at
 * @param eventType what is being asked for
 */
class AiTextFieldListItemEvent(
    source: Any,
    target: EventTarget,
    eventType: EventType<AiTextFieldListItemEvent>
) : Event(source, target, eventType) {

    companion object {
        /** Any request of an item, no matter which of the two buttons it came from. */
        val ANY: EventType<AiTextFieldListItemEvent> =
            EventType(Event.ANY, "AI_TEXT_FIELD_LIST_ITEM")

        /** Request of the wand, asking the AI for a text of the entry. */
        val CREATE_AI_TEXT: EventType<AiTextFieldListItemEvent> =
            EventType(ANY, "AI_TEXT_FIELD_LIST_ITEM_CREATE")

        /** Request of the cross, asking for the entry to be removed. */
        val DELETE: EventType<AiTextFieldListItemEvent> =
            EventType(ANY, "AI_TEXT_FIELD_LIST_ITEM_DELETE")
    }
}
