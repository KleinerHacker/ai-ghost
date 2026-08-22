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
import org.pcsoft.app.aighost.model.project.Book
import org.pcsoft.app.aighost.model.project.Chapter
import org.pcsoft.app.aighost.model.project.Project

/**
 * Fully populated model instances shared by the tests of this module.
 *
 * [Project.Settings] carries nine mandatory properties, so building one inline would bury the actual
 * assertion of every test that needs a project. The fixtures here are created fresh on each access,
 * so a test may copy and modify them without affecting the next one.
 */
object TestData {

    /** A font that differs from every other fixture font, so a mix up shows up in an assertion. */
    fun font(name: String = "Serif", size: Int = 12): FontData =
        FontData(name = name, size = size, bold = false, italic = false)

    /** A style around [font], aligned to the given [alignment]. */
    fun style(name: String = "Serif", size: Int = 12, alignment: Alignment = Alignment.LEFT): StyleData =
        StyleData(font = font(name, size), alignment = alignment)

    /** Settings whose styles all differ, so a swapped property is caught by a round trip test. */
    fun settings(): Project.Settings = Project.Settings(
        copyrightFont = style("Serif", 8),
        titleFont = style("Sans", 28, Alignment.CENTER),
        titleAppendixFont = style("Sans", 18, Alignment.CENTER),
        chapterFont = style("Sans", 20),
        chapterAppendixFont = style("Sans", 14),
        textFont = style("Serif", 11, Alignment.BLOCK),
        copyrightPage = true,
        startWithEmptyPage = true,
        endWithEmptyPage = false
    )

    /** A book with two chapters, the second one still without text. */
    fun book(): Book = Book(
        title = "My Novel",
        titleAppendix = listOf("A Story in Two Parts"),
        chapters = listOf(
            Chapter("Prologue", listOf("How it started"), listOf("Once upon a time.", "And then.")),
            Chapter("Epilogue")
        )
    )

    /** A complete project, the root object a JSON document holds. */
    fun project(): Project = Project(
        name = "My Novel",
        author = "Jane Doe",
        copyright = "(c) 2026 Jane Doe",
        settings = settings(),
        book = book()
    )
}
