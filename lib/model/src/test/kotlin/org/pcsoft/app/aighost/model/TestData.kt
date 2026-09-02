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

package org.pcsoft.app.aighost.model

import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Copyright
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import org.pcsoft.app.aighost.model.project.design.BlurbPageDesign
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightPageDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.EpilogPageDesign
import org.pcsoft.app.aighost.model.project.design.PageFormat
import org.pcsoft.app.aighost.model.project.design.PrologPageDesign
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign
import org.pcsoft.app.aighost.model.project.meta.Meta

/**
 * Fully populated model instances shared by the tests of this module.
 *
 * A project is made of several parts, each of them carrying nested styles, so building one inline
 * would bury the actual assertion of every test that needs a project. The fixtures here are created
 * fresh on each access, so a test may copy and modify them without affecting the next one.
 */
object TestData {

    /** A font that differs from every other fixture font, so a mix up shows up in an assertion. */
    fun font(name: String = "Serif", size: Int = 12): FontData =
        FontData(name = name, size = size, bold = false, italic = false)

    /** A style around [font], aligned to the given [alignment] and set with the given [lineSpacing]. */
    fun style(
        name: String = "Serif",
        size: Int = 12,
        alignment: Alignment = Alignment.LEFT,
        lineSpacing: Double = 1.2
    ): StyleData =
        StyleData(font = font(name, size), textLineSpacing = lineSpacing, alignment = alignment)

    /** A prompt pair whose two texts differ, so a swapped one shows up in a round trip. */
    fun prompt(content: String, style: String = "Warm and calm."): AIPrompt =
        AIPrompt(contentPrompt = content, stylePrompt = style)

    /** Meta data with name and author filled, so a dropped one shows up in a round trip. */
    fun meta(): Meta = Meta(
        name = "My Novel",
        author = "Jane Doe"
    )

    /** The copyright page of the book, with an appendix line and switched on. */
    fun copyright(): Copyright = Copyright(
        copyright = "(c) 2026 Jane Doe",
        copyrightAppendix = listOf("All rights reserved."),
        included = true
    )

    /** The design of the title page, whose three texts carry styles of their own. */
    fun titlePageDesign(): TitlePageDesign = TitlePageDesign(
        titleStyle = style("Sans", 28, Alignment.CENTER, 1.5),
        titleAppendixStyle = style("Sans", 18, Alignment.CENTER, 1.15),
        showAuthor = true,
        authorStyle = style("Sans", 16, Alignment.CENTER, 1.1)
    )

    /** The design of the copyright page, whose three texts carry styles of their own. */
    fun copyrightPageDesign(): CopyrightPageDesign = CopyrightPageDesign(
        copyrightStyle = style("Serif", 8, Alignment.LEFT, 1.0),
        copyrightAppendixStyle = style("Serif", 7, Alignment.LEFT, 1.05),
        showAuthor = true,
        authorStyle = style("Serif", 9, Alignment.LEFT, 1.1)
    )

    /** The design of the prolog page, whose heading, appendix and text carry styles of their own. */
    fun prologPageDesign(): PrologPageDesign = PrologPageDesign(
        titleStyle = style("Sans", 22, Alignment.LEFT, 1.25),
        titleAppendixStyle = style("Sans", 13, Alignment.LEFT, 1.2),
        textStyle = style("Serif", 11, Alignment.BLOCK, 1.35)
    )

    /** The design of the epilog page, whose heading, appendix and text carry styles of their own. */
    fun epilogPageDesign(): EpilogPageDesign = EpilogPageDesign(
        titleStyle = style("Sans", 21, Alignment.LEFT, 1.24),
        titleAppendixStyle = style("Sans", 12, Alignment.LEFT, 1.18),
        textStyle = style("Serif", 11, Alignment.BLOCK, 1.36)
    )

    /** The design of a chapter page, with its own styles and the heading pushed onto its own page. */
    fun chapterPageDesign(): ChapterPageDesign = ChapterPageDesign(
        titleStyle = style("Sans", 20, Alignment.LEFT, 1.3),
        titleAppendixStyle = style("Sans", 14, Alignment.LEFT, 1.22),
        textStyle = style("Serif", 11, Alignment.BLOCK, 1.4),
        titleOnSeparatePage = true
    )

    /** The design of the blurb page, styled differently from every other design part. */
    fun blurbPageDesign(): BlurbPageDesign = BlurbPageDesign(
        textStyle = style("Serif", 10, Alignment.BLOCK, 1.45)
    )

    /** A page format whose measures all differ, so a swapped margin is caught by a round trip test. */
    fun pageFormat(): PageFormat = PageFormat(
        width = 400.0,
        height = 600.0,
        innerMargin = 25.0,
        outerMargin = 18.0,
        topMargin = 12.0,
        bottomMargin = 22.0,
        mirroredMargins = true
    )

    /** A design whose parts all differ, so a swapped property is caught by a round trip test. */
    fun design(): Design = Design(
        pageFormat = pageFormat(),
        titlePage = titlePageDesign(),
        copyrightPage = copyrightPageDesign(),
        prologPage = prologPageDesign(),
        blurbPage = blurbPageDesign(),
        chapterPage = chapterPageDesign(),
        epilogPage = epilogPageDesign(),
        startWithEmptyPage = true,
        endWithEmptyPage = false
    )

    /** The prompts of the whole manuscript, different from those of every single part. */
    fun bookPrompts(): AIPrompt = prompt("Tell a story in two parts.", "Warm and calm.")

    /**
     * A prolog with an appendix line, prompts and text, belonging to the book, so a dropped property
     * shows up.
     */
    fun prolog(): Prolog = Prolog(
        title = "Before It All",
        titleAppendix = listOf("A word up front"),
        prompts = prompt("Tell what happened before the story.", "Quiet and slow."),
        paragraph = listOf("Long before the story started."),
        included = true
    )

    /**
     * An epilog with an appendix line, prompts and text, belonging to the book, so a dropped property
     * shows up.
     */
    fun epilog(): Epilog = Epilog(
        title = "After It All",
        titleAppendix = listOf("A last word"),
        prompts = prompt("Tell how everybody went on.", "Quiet and slow."),
        paragraph = listOf("And that was that."),
        included = true
    )

    /**
     * A blurb with a prompt and two paragraphs, belonging to the book, so a lost order shows up in a
     * round trip.
     */
    fun blurb(): Blurb = Blurb(
        prompt = "Advertise a tale of two chapters.",
        paragraph = listOf("A gripping tale of two chapters.", "You will not put it down."),
        included = true
    )

    /** A book with copyright, prolog, epilog, blurb and two chapters, the second one still without text. */
    fun book(): Book = Book(
        title = "My Novel",
        titleAppendix = listOf("A Story in Two Parts"),
        prompts = bookPrompts(),
        copyright = copyright(),
        prolog = prolog(),
        chapters = listOf(
            Chapter(
                name = "first",
                title = "The First Part",
                titleAppendix = listOf("How it started"),
                prompts = prompt("Tell how the journey started.", "Lively and warm."),
                paragraph = listOf("Once upon a time.", "And then.")
            ),
            Chapter(name = "second", title = "The Second Part")
        ),
        epilog = epilog(),
        blurb = blurb()
    )

    /** A complete project, the root object a stored document holds. */
    fun project(): Project = Project(
        meta = meta(),
        design = design(),
        book = book()
    )
}
