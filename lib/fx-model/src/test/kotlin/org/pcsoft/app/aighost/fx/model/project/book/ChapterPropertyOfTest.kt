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

package org.pcsoft.app.aighost.fx.model.project.book

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.project.book.Chapter

/**
 * Developer tests for [ChapterProperty.of].
 *
 * A chapter is a plain entry of [BookProperty.chaptersProperty] and therefore never carries a property
 * model of its own; [ChapterProperty.of] is the way a caller that picked one out of that list reaches
 * its fields as properties, so these tests prove that the built property is already bound to the given
 * chapter and that a write through it lands on that very object.
 */
class ChapterPropertyOfTest {

    /**
     * Use case: a chapter is picked out of the project tree, so the property built for it already
     * carries that chapter without any further step.
     */
    @Test
    fun buildsAPropertyAlreadyBoundToTheChapter() {
        val chapter = Chapter(name = "Chapter one", title = "The arrival")

        val property = ChapterProperty.of(chapter)

        assertEquals(chapter, property.value)
        assertEquals("Chapter one", property.name)
        assertEquals("The arrival", property.title)
    }

    /**
     * Use case: the name of the picked chapter is rewritten through the built property, so the text
     * lands on the chapter object the project tree still holds - the same instance, not a copy of it.
     */
    @Test
    fun writesThroughToTheWrappedChapter() {
        val chapter = Chapter(name = "Chapter one", title = "The arrival")

        val property = ChapterProperty.of(chapter)
        property.name = "Chapter two"

        assertEquals("Chapter two", chapter.name)
    }

    /**
     * Use case: two chapters are picked one after another, so each call to [ChapterProperty.of] builds
     * a property of its own instead of two callers sharing the same one.
     */
    @Test
    fun buildsAnIndependentPropertyPerCall() {
        val first = Chapter(name = "Chapter one", title = "The arrival")
        val second = Chapter(name = "Chapter two", title = "The departure")

        val firstProperty = ChapterProperty.of(first)
        val secondProperty = ChapterProperty.of(second)

        assertEquals("Chapter one", firstProperty.name)
        assertEquals("Chapter two", secondProperty.name)
    }
}
