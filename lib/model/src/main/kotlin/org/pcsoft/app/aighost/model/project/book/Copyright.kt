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

package org.pcsoft.app.aighost.model.project.book

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * The copyright page of a [org.pcsoft.app.aighost.model.project.Project]'s book.
 *
 * Whether the page belongs to the book is a switch of its own. The copyright notice and the further
 * lines that used to sit on this class moved to the simPlay `Document` of
 * [org.pcsoft.app.aighost.model.project.book.Book] (IP-36/37/38); the copyright page contributes to
 * that document through its anchor instead of carrying text fields of its own.
 *
 * @property included Whether the copyright page is printed in the book, true by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Copyright(
    var included: Boolean = true
)
