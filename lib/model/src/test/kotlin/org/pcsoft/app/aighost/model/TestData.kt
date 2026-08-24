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
import org.pcsoft.app.aighost.model.project.design.AuthorDesign
import org.pcsoft.app.aighost.model.project.design.ChapterDesign
import org.pcsoft.app.aighost.model.project.design.CopyrightDesign
import org.pcsoft.app.aighost.model.project.design.Design
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

    /** A design whose parts all differ, so a swapped property is caught by a round trip test. */
    fun design(): Design = Design(
        authorDesign = authorDesign(),
        copyrightDesign = copyrightDesign(),
        titleDesign = titleDesign(),
        chapterDesign = chapterDesign(),
        textDesign = textDesign(),
        startWithEmptyPage = true,
        endWithEmptyPage = false
    )

    /** A prolog with an appendix line and text, so a dropped property shows up in a round trip. */
    fun prolog(): Prolog = Prolog(
        title = "Before It All",
        titleAppendix = listOf("A word up front"),
        paragraph = listOf("Long before the story started.")
    )

    /** An epilog with an appendix line and text, so a dropped property shows up in a round trip. */
    fun epilog(): Epilog = Epilog(
        title = "After It All",
        titleAppendix = listOf("A last word"),
        paragraph = listOf("And that was that.")
    )

    /** A blurb with two paragraphs, so a lost order shows up in a round trip. */
    fun blurb(): Blurb = Blurb(
        paragraph = listOf("A gripping tale of two chapters.", "You will not put it down.")
    )

    /** A book with prolog, epilog, blurb and two chapters, the second one still without text. */
    fun book(): Book = Book(
        title = "My Novel",
        titleAppendix = listOf("A Story in Two Parts"),
        prolog = prolog(),
        chapters = listOf(
            Chapter("first", "The First Part", listOf("How it started"), listOf("Once upon a time.", "And then.")),
            Chapter("second", "The Second Part")
        ),
        epilog = epilog(),
        blurb = blurb()
    )

    /** A complete project, the root object a stored document holds. */
    fun project(): Project = Project(
        mapOf(
            Project.PART_META to meta(),
            Project.PART_DESIGN to design(),
            Project.PART_BOOK to book()
        )
    )
}
