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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.pcsoft.framework.simplay.engine.geometry.Margins
import org.pcsoft.framework.simplay.engine.geometry.Size
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.FlowPage
import org.pcsoft.framework.simplay.engine.model.Font
import org.pcsoft.framework.simplay.engine.model.PageLayout
import org.pcsoft.framework.simplay.engine.model.PageNumbering
import org.pcsoft.framework.simplay.engine.model.TextBlock
import org.pcsoft.framework.simplay.engine.model.TextStyle

/**
 * Developer tests for [DocumentCodec].
 */
class DocumentCodecTest {

    /**
     * Use case: a freshly created project has no manuscript yet, so the empty document it carries
     * still round-trips through the codec instead of failing on the edge case of no pages at all.
     */
    @Test
    fun roundTripsAnEmptyDocument() {
        val document = Document()

        assertEquals(document, DocumentCodec.decode(DocumentCodec.encode(document)))
    }

    /**
     * Use case: the user writes several pages of manuscript, so a document with multiple pages and text
     * blocks round-trips without losing a page, a block or the page numbering settings.
     */
    @Test
    fun roundTripsADocumentWithMultiplePagesAndBlocks() {
        val layout = PageLayout(size = Size(420.0, 595.0), margins = Margins(40.0, 40.0, 50.0, 50.0))
        val style = TextStyle(Font("Serif", 12.0))
        val document = Document(
            pages = listOf(
                FlowPage(layout = layout, blocks = listOf(TextBlock.of("Once upon a time.", style))),
                FlowPage(layout = layout, blocks = listOf(TextBlock.of("The story continued.", style)))
            ),
            numbering = PageNumbering.OFF.copy(startNumber = 3)
        )

        assertEquals(document, DocumentCodec.decode(DocumentCodec.encode(document)))
    }

    /**
     * Use case: a project archive was damaged and no longer carries valid document JSON, so decoding it
     * fails loudly instead of handing out a document nobody can trust.
     */
    @Test
    fun failsToDecodeAPayloadThatIsNotValidJson() {
        assertThrows(SerializationException::class.java) { DocumentCodec.decode("not a document") }
    }

    /**
     * Use case: the archive holds well formed JSON that is not a document at all - for instance an
     * empty object missing every field a document could fall back to - so decoding it still fails
     * loudly instead of guessing a document out of unrelated content.
     */
    @Test
    fun failsToDecodeJsonThatIsNotADocument() {
        assertThrows(SerializationException::class.java) { DocumentCodec.decode("""{"foo":"bar"}""") }
    }
}
