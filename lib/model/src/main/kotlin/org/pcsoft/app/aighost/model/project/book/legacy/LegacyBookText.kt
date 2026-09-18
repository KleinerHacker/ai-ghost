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

package org.pcsoft.app.aighost.model.project.book.legacy

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Read-only mirror of the fields [org.pcsoft.app.aighost.model.project.book.BookPart] carried before
 * IP-36 - `title`, `titleAppendix` and `paragraph` - kept under their old JSON names.
 *
 * [org.pcsoft.app.aighost.model.project.book.Book], [org.pcsoft.app.aighost.model.project.book.Chapter],
 * [org.pcsoft.app.aighost.model.project.book.Prolog] and
 * [org.pcsoft.app.aighost.model.project.book.Epilog] carry `@JsonIgnoreProperties(ignoreUnknown = true)`,
 * so an old project already opens without failing: Jackson silently drops these fields when it reads
 * one of those classes directly. That is enough to satisfy IP-36 (a Teil A project must still open),
 * but it also means the old text is thrown away unread.
 *
 * This class is NOT used by any production code path yet. It exists so that IP-37's migration can
 * parse the very same JSON entry a second time - once into the production class (which drops the old
 * fields) and once into this class (which keeps them) - and use the values read here to seed the
 * `Document` [org.pcsoft.app.aighost.model.project.book.Book.document] is built with for an old
 * project. Building that migration is explicitly out of scope for IP-36 (Teil A); only the read-only
 * mirror is prepared here.
 *
 * @property title Heading of the part, empty when the field was not present.
 * @property titleAppendix Further heading lines shown below the title, empty when the field was not present.
 * @property paragraph Paragraphs of the part in their order, empty when the field was not present.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LegacyBookPartText(
    val title: String = "",
    val titleAppendix: List<String> = emptyList(),
    val paragraph: List<String> = emptyList()
)

/**
 * Read-only mirror of the fields [org.pcsoft.app.aighost.model.project.book.Book] carried before
 * IP-36 - `title` and `titleAppendix` - kept under their old JSON names.
 *
 * See [LegacyBookPartText] for why this class exists and how IP-37 is expected to use it.
 *
 * @property title Main title of the book, empty when the field was not present.
 * @property titleAppendix Further title lines shown below the main title, empty when the field was not present.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LegacyBookText(
    val title: String = "",
    val titleAppendix: List<String> = emptyList()
)

/**
 * Read-only mirror of the fields [org.pcsoft.app.aighost.model.project.book.Copyright] carried before
 * IP-36 - `copyright` and `copyrightAppendix` - kept under their old JSON names.
 *
 * See [LegacyBookPartText] for why this class exists and how IP-37 is expected to use it.
 *
 * @property copyright The copyright notice, empty when the field was not present.
 * @property copyrightAppendix Further lines printed below the copyright notice, empty when the field was not present.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LegacyCopyrightText(
    val copyright: String = "",
    val copyrightAppendix: List<String> = emptyList()
)
