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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPart
import org.pcsoft.app.aighost.plugin.api.model.project.ProjectPartInfo
import org.pcsoft.framework.simplay.engine.model.Document

/**
 * Defines the current version of the project structure or model.
 *
 * This constant is used to track changes in the data model to ensure compatibility
 * when handling serialization, deserialization, or migrations in related components.
 *
 * Raised from 1 to 2 with [Book.document] (IP-37): a document stored under version 1 carries no
 * `document` field at all. Reading such a document is not this class's concern - it is not migrated
 * here, so [documentPayload] falls back to an empty [Document] the same way a freshly created book
 * does.
 */
private const val VERSION = 2

/**
 * The manuscript of a [org.pcsoft.app.aighost.model.project.Project]: its prompts and all parts that
 * make it up.
 *
 * Every project holds a book, even an empty one that has not been written yet. The chapter order is
 * part of the data: the list is stored and read back in exactly the order the user arranged it in.
 * [prolog], [epilog] and [blurb] are always there as well; each of them carries a switch of its own
 * that tells whether it belongs to the book.
 *
 * The main title and its further lines that used to sit on this class moved to [document] (IP-36,
 * IP-37): the flowing manuscript text is no longer modelled as fields of this class or of its parts at
 * all, it lives exclusively in the simPlay [Document] this book carries.
 *
 * @property version Version of the project metadata structure.
 * @property prompts Prompts for the book, empty by default.
 * @property copyright Copyright page of the book, included by default.
 * @property prolog Prolog printed before the first chapter, not included by default.
 * @property chapters Chapters of the book in their user defined order, empty by default.
 * @property epilog Epilog printed after the last chapter, not included by default.
 * @property blurb Advertising text printed on the cover, empty and not included by default.
 * @property documentPayload The manuscript's simPlay [Document], encoded as JSON by [DocumentCodec] -
 *   the field Jackson actually reads and writes, an empty encoded [Document] by default. Use
 *   [document] instead of this field; it exists so Jackson - which cannot read simPlay's own
 *   `kotlinx.serialization` types - sees only a plain string.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ProjectPartInfo(identifier = Project.PART_BOOK)
data class Book(
    override val version: Int = VERSION,

    var prompts: AIPrompt = AIPrompt(),

    var copyright: Copyright = Copyright(),
    var prolog: Prolog = Prolog(),
    var chapters: List<Chapter> = emptyList(),
    var epilog: Epilog = Epilog(),
    var blurb: Blurb = Blurb(),

    @JsonProperty("document")
    var documentPayload: String = DocumentCodec.encode(Document())
) : ProjectPart {

    init {
        // Forces the payload to decode once, right when Jackson builds this object - not lazily, the
        // first time some caller happens to read `document`. `SerializationException` is wrapped into
        // a `JacksonException` by Jackson's own constructor invocation, which `StorageIo.loadFromZip`
        // already catches per part: a corrupt `document` entry makes the whole book unparsable and is
        // therefore reported as `ProjectStorage.Error.Corrupt`, exactly like any other unreadable
        // standard part.
        document
    }

    /**
     * The manuscript's flowing text as a simPlay [Document].
     *
     * Never `null`: a project without a manuscript yet - a freshly created one, or one stored before
     * IP-37 introduced this field - carries an empty [Document] the same way it carries an empty
     * [chapters] list, so no consumer of this class has to handle a missing manuscript as a case of
     * its own. Whether a book was already migrated to carry real content is a question [version]
     * answers, not the nullability of this property.
     *
     * Reads and writes go through [DocumentCodec] against [documentPayload], the field Jackson
     * actually persists.
     */
    @get:JsonIgnore
    var document: Document
        get() = DocumentCodec.decode(documentPayload)
        set(value) {
            documentPayload = DocumentCodec.encode(value)
        }
}
