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
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import org.pcsoft.app.aighost.model.project.design.AuthorDesign
import org.pcsoft.app.aighost.model.project.design.ChapterDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.PageFormat
import org.pcsoft.app.aighost.model.project.design.TextDesign
import org.pcsoft.app.aighost.model.project.design.TitleDesign
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

    /** A style around [font], aligned to the given [alignment]. */
    fun style(name: String = "Serif", size: Int = 12, alignment: Alignment = Alignment.LEFT): StyleData =
        StyleData(font = font(name, size), alignment = alignment)

    /** A prompt pair whose two texts differ, so a swapped one shows up in a round trip. */
    fun prompt(content: String, style: String = "Warm and calm."): AIPrompt =
        AIPrompt(contentPrompt = content, stylePrompt = style)

    /** Meta data with all three texts filled, so a dropped one shows up in a round trip. */
    fun meta(): Meta = Meta(
        name = "My Novel",
        author = "Jane Doe",
        copyright = "(c) 2026 Jane Doe"
    )

    /** The design of the author name, styled differently from every other design part. */
    fun authorDesign(): AuthorDesign = AuthorDesign(style = style("Sans", 16, Alignment.CENTER))

    /** The design of the copyright page, printed on a page of its own. */
    fun copyrightDesign(): CopyrightDesign = CopyrightDesign(style = style("Serif", 8), show = true)

    /** The design of the title page, styled differently from every other design part. */
    fun titleDesign(): TitleDesign = TitleDesign(style = style("Sans", 28, Alignment.CENTER))

    /** The design of a chapter, whose heading and appendix carry styles of their own. */
    fun chapterDesign(): ChapterDesign = ChapterDesign(
        titleStyle = style("Sans", 20),
        titleAppendixStyle = style("Sans", 14)
    )

    /** The design of the body text, styled differently from every other design part. */
    fun textDesign(): TextDesign = TextDesign(style = style("Serif", 11, Alignment.BLOCK))

    /** A page format whose measures all differ, so a swapped margin is caught by a round trip test. */
    fun pageFormat(): PageFormat = PageFormat(
        width = 400.0,
        height = 600.0,
        innerMargin = 25.0,
        outerMargin = 18.0,
        topMargin = 12.0,
        bottomMargin = 22.0
    )

    /** A design whose parts all differ, so a swapped property is caught by a round trip test. */
    fun design(): Design = Design(
        pageFormat = pageFormat(),
        authorLineSpacing = 1.1,
        copyrightLineSpacing = 1.0,
        titleLineSpacing = 1.5,
        chapterLineSpacing = 1.3,
        textLineSpacing = 1.4,
        authorDesign = authorDesign(),
        copyrightDesign = copyrightDesign(),
        titleDesign = titleDesign(),
        chapterDesign = chapterDesign(),
        textDesign = textDesign(),
        startWithEmptyPage = true,
        endWithEmptyPage = false
    )

    /** The prompts of the whole manuscript, different from those of every single part. */
    fun bookPrompts(): AIPrompt = prompt("Tell a story in two parts.", "Warm and calm.")

    /** A prolog with an appendix line, prompts and text, so a dropped property shows up. */
    fun prolog(): Prolog = Prolog(
        title = "Before It All",
        titleAppendix = listOf("A word up front"),
        prompts = prompt("Tell what happened before the story.", "Quiet and slow."),
        paragraph = listOf("Long before the story started.")
    )

    /** An epilog with an appendix line, prompts and text, so a dropped property shows up. */
    fun epilog(): Epilog = Epilog(
        title = "After It All",
        titleAppendix = listOf("A last word"),
        prompts = prompt("Tell how everybody went on.", "Quiet and slow."),
        paragraph = listOf("And that was that.")
    )

    /** A blurb with a prompt and two paragraphs, so a lost order shows up in a round trip. */
    fun blurb(): Blurb = Blurb(
        prompt = "Advertise a tale of two chapters.",
        paragraph = listOf("A gripping tale of two chapters.", "You will not put it down.")
    )

    /** A book with prolog, epilog, blurb and two chapters, the second one still without text. */
    fun book(): Book = Book(
        title = "My Novel",
        titleAppendix = listOf("A Story in Two Parts"),
        prompts = bookPrompts(),
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
