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

package org.pcsoft.app.aighost.model.pref

/**
 * Which of the two modes the writing surface's single sheet is switched to.
 *
 * The value is stored inside the [Preferences] and is written to JSON by its constant name, so a
 * stored preferences file stays readable when new constants are added. `app/ui` maps this onto
 * `org.pcsoft.framework.simplay.fx.PaperSheetMode` - `WRITING` onto `EDITABLE`, `PREVIEW` onto
 * `SELECTABLE` - since that JavaFX type has no place in this toolkit-free module.
 */
enum class WritingMode {

    /** The sheet may be typed into. */
    WRITING,

    /** The sheet only shows the book as it will print; its text is selectable but not editable. */
    PREVIEW
}
