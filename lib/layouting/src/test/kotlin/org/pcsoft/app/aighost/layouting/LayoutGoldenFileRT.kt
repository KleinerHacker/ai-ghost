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

package org.pcsoft.app.aighost.layouting

import org.junit.jupiter.api.Test

/**
 * Regression tests (IP-06) pinning the page structure [LayoutEngine] computes for a handful of
 * example projects against checked in golden files (see [GoldenFileSupport]).
 *
 * Every example is broken with [GreedyLineBreaker] against [FixedTextMetrics], so the numbers are
 * reproducible without a real font on the machine running the test - the same reasoning
 * [LayoutEngineTest] already follows. A page is serialised as one line of plain numbers
 * (`position`, `number`, `active`, `lines`, `leftMargin`, `rightMargin`); a change to any of them,
 * intentional or not, shows up as a line-level diff of the golden file instead of as a green test.
 */
class LayoutGoldenFileRT {

    private val metrics = FixedTextMetrics()
    private val breaker = GreedyLineBreaker(metrics)

    private fun geometry(
        width: Double = 300.0,
        height: Double = 400.0,
        innerMargin: Double = 40.0,
        outerMargin: Double = 20.0,
        topMargin: Double = 30.0,
        bottomMargin: Double = 30.0,
        mirroredMargins: Boolean = false
    ) = PageGeometry(width, height, innerMargin, outerMargin, topMargin, bottomMargin, mirroredMargins)

    private fun columnWidth(geometry: PageGeometry) =
        geometry.width - geometry.innerMargin - geometry.outerMargin

    private fun laidOut(text: String, style: TextStyle, geometry: PageGeometry): LaidOutText =
        breaker.breakText(listOf(TextBlock(text, style)), columnWidth(geometry))

    private fun serialize(layout: DocumentLayout): List<String> =
        layout.pages.map { page ->
            "position=${page.position} number=${page.pageNumber ?: "-"} active=${page.active} " +
                "lines=${page.lines.size} leftMargin=${page.leftMargin} rightMargin=${page.rightMargin}"
        }

    private fun verify(name: String, layout: DocumentLayout) =
        GoldenFileSupport.verify(name, serialize(layout))

    /** A short paragraph, well within one page, is pinned as a single-page document. */
    @Test
    fun aShortPartProducesOnePage() {
        val geometry = geometry()
        val style = TextStyle(family = "Test Family", size = 12.0)
        val text = "A short chapter opens with a single paragraph that never leaves its first page."
        val layout = LayoutEngine.layout(laidOut(text, style, geometry), geometry)

        verify("short-part", layout)
    }

    /** A paragraph repeated many times over is pinned as the multi-page document it produces. */
    @Test
    fun aLongPartIsDistributedOverManyPages() {
        val geometry = geometry()
        val style = TextStyle(family = "Test Family", size = 12.0)
        val paragraph = "This paragraph repeats itself many times so the part grows past a single page. "
        val text = paragraph.repeat(60)
        val layout = LayoutEngine.layout(laidOut(text, style, geometry), geometry)

        verify("long-part", layout)
    }

    /** A justified block ("Blocksatz") is pinned the same way as any other alignment. */
    @Test
    fun aJustifiedBlockProducesTheSamePageStructureAsGoldenFile() {
        val geometry = geometry()
        val style = TextStyle(family = "Test Family", size = 12.0, alignment = TextAlignment.JUSTIFY)
        val paragraph = "Justified text stretches every line but the last to the full column width. "
        val text = paragraph.repeat(20)
        val layout = LayoutEngine.layout(laidOut(text, style, geometry), geometry)

        verify("justified-block", layout)
    }

    /** The same text laid out against several page designs is pinned as one golden file per design. */
    @Test
    fun multipleDesignsProduceTheirOwnPageStructure() {
        val style = TextStyle(family = "Test Family", size = 12.0)
        val text = "A design describes the page size and the margins the same text is set against. ".repeat(15)

        val designs = mapOf(
            "compact" to geometry(width = 250.0, height = 350.0, innerMargin = 20.0, outerMargin = 15.0),
            "wide" to geometry(width = 450.0, height = 400.0, innerMargin = 40.0, outerMargin = 40.0)
        )

        val lines = designs.flatMap { (name, geometry) ->
            val layout = LayoutEngine.layout(laidOut(text, style, geometry), geometry)
            listOf("# design=$name") + serialize(layout)
        }

        GoldenFileSupport.verify("multiple-designs", lines)
    }

    /** Mirrored margins alternate the inner margin between a recto and a verso page across a book. */
    @Test
    fun mirroredMarginsAlternateAcrossOddAndEvenPages() {
        val geometry = geometry(mirroredMargins = true)
        val style = TextStyle(family = "Test Family", size = 12.0)
        val parts = (1..4).map { chapter ->
            val text = "Chapter $chapter opens on its own page and stays within it. "
            LayoutEngine.PartInput(laidOut(text, style, geometry))
        }
        val layout = LayoutEngine.layoutBook(parts, geometry)

        verify("odd-even-pages", layout)
    }

    /** A book with its prolog switched off leaves the prolog's pages unnumbered. */
    @Test
    fun aBookWithThePrologSwitchedOffLeavesItsPagesUnnumbered() {
        val geometry = geometry()
        val style = TextStyle(family = "Test Family", size = 12.0)
        val prolog = LayoutEngine.PartInput(
            laidOut("The prolog sets the scene before the first chapter begins.", style, geometry),
            active = false,
            numbered = false
        )
        val chapter = LayoutEngine.PartInput(
            laidOut("The first chapter follows the prolog and always carries a page number.", style, geometry)
        )
        val layout = LayoutEngine.layoutBook(listOf(prolog, chapter), geometry)

        verify("prolog-off", layout)
    }

    /** A book with its prolog switched on numbers the prolog's pages like any other part. */
    @Test
    fun aBookWithThePrologSwitchedOnCountsItsPagesAsNumbered() {
        val geometry = geometry()
        val style = TextStyle(family = "Test Family", size = 12.0)
        val prolog = LayoutEngine.PartInput(
            laidOut("The prolog sets the scene before the first chapter begins.", style, geometry),
            active = true,
            numbered = true
        )
        val chapter = LayoutEngine.PartInput(
            laidOut("The first chapter follows the prolog and always carries a page number.", style, geometry)
        )
        val layout = LayoutEngine.layoutBook(listOf(prolog, chapter), geometry)

        verify("prolog-on", layout)
    }
}
