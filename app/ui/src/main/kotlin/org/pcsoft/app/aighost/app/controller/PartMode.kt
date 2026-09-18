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

package org.pcsoft.app.aighost.app.controller

/**
 * Which kind of book part the writing sheet shows.
 *
 * Resolved from the picked project tree node by [BookPartEditorController.resolve] and consumed by
 * the view model of the editor to drive its empty state and its read-only flag.
 */
enum class PartMode {

    /** Nothing writable is picked, so the sheet shows its empty state. */
    NONE,

    /** The title page is shown read only. */
    TITLE_PAGE,

    /** The copyright page is shown read only. */
    COPYRIGHT_PAGE,

    /** A prolog, a chapter or an epilog is edited through one flow. */
    BOOK_PART,

    /** The blurb is edited, a flow without a heading. */
    BLURB
}
