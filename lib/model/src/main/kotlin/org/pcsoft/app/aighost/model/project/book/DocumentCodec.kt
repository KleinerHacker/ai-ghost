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

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.pcsoft.framework.simplay.engine.model.Document

/**
 * Turns a simPlay [Document] into the single JSON string [Book.documentPayload] hands to Jackson, and
 * back.
 *
 * simPlay's raw model is `kotlinx.serialization`-only and carries no Jackson bridge of its own, so
 * this object is the one place that builds the bridge: Jackson never sees a [Document], only the
 * string this object produces from it. [decode] throws [SerializationException] for a payload that is
 * not a valid document, which [Book] lets pass so it surfaces to a caller reading the book through
 * Jackson.
 */
internal object DocumentCodec {

    /**
     * The `kotlinx.serialization` configuration every [Document] is encoded with and read back
     * through.
     *
     * Not pretty printed: the string is embedded as one value of the Jackson-indented `book.json`
     * already, so a second layer of indentation would only add noise.
     */
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = false
    }

    /** Encodes [document] as a JSON string. */
    fun encode(document: Document): String = json.encodeToString(Document.serializer(), document)

    /**
     * Decodes [payload] back into a [Document].
     *
     * @throws SerializationException [payload] is not a valid, encoded [Document].
     */
    fun decode(payload: String): Document = json.decodeFromString(Document.serializer(), payload)
}
