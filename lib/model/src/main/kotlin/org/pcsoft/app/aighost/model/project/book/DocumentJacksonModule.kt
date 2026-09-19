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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlinx.serialization.SerializationException
import org.pcsoft.framework.simplay.engine.model.Document

/**
 * Writes a [Document] as a JSON object nested directly into whatever Jackson document it is a part
 * of, instead of as a quoted string.
 *
 * simPlay's raw model is `kotlinx.serialization`-only and carries no Jackson bridge of its own, so
 * this class turns [DocumentCodec]'s JSON string into a raw value Jackson copies straight into its
 * own output.
 */
internal class DocumentSerializer : JsonSerializer<Document>() {
    override fun serialize(value: Document, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeRawValue(DocumentCodec.encode(value))
    }
}

/**
 * Reads a [Document] back from the JSON object [DocumentSerializer] nested into the surrounding
 * document.
 *
 * The field is read as a tree first and handed to [DocumentCodec] as text, since `kotlinx.serialization`
 * has no direct reader for a Jackson [TreeNode]. A payload that is not a valid [Document] is reported
 * as a [JsonMappingException], the same way every other unreadable field is, so callers keep catching
 * `JacksonException` alone.
 */
internal class DocumentDeserializer : JsonDeserializer<Document>() {
    override fun deserialize(p: com.fasterxml.jackson.core.JsonParser, ctxt: DeserializationContext): Document {
        val node: TreeNode = p.readValueAsTree()
        return try {
            DocumentCodec.decode(node.toString())
        } catch (e: SerializationException) {
            throw JsonMappingException.from(p, "Could not read document", e)
        }
    }
}

/** The Jackson module bridging [Document] between simPlay's `kotlinx.serialization` model and Jackson. */
internal val documentJacksonModule: SimpleModule = SimpleModule()
    .addSerializer(Document::class.java, DocumentSerializer())
    .addDeserializer(Document::class.java, DocumentDeserializer())
