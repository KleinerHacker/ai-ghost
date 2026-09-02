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

package org.pcsoft.app.aighost.app.ui.component

import de.saxsys.mvvmfx.MvvmFX
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.ui.component.base.AiPromptArea
import org.pcsoft.app.aighost.app.ui.component.base.AiTextField
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import org.pcsoft.app.aighost.model.project.design.ChapterPageDesign
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.TitlePageDesign
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [Inspector].
 *
 * The inspector is bound to a project of its own in every test, and both directions are proven on the
 * real controls the user works with - what is typed has to stand in the model object afterwards, and
 * what the model object carries has to stand in those controls.
 */
class InspectorTest : ApplicationTest() {

    private lateinit var inspector: Inspector

    private val selection = SimpleObjectProperty<ProjectListItem?>(null)

    private val bookSection: TitledPane
        get() = inspector.lookup("#pnlBookSection") as TitledPane

    private val chapterSection: TitledPane
        get() = inspector.lookup("#pnlChapterSection") as TitledPane

    private val bookTitleInput: TextField
        get() = (inspector.lookup("#txtBookTitle") as AiTextField).lookup(".text-field") as TextField

    private val bookAuthorInput: TextField
        get() = inspector.lookup("#txtBookAuthor") as TextField

    private val bookCopyrightInput: TextField
        get() = inspector.lookup("#txtBookCopyright") as TextField

    private val bookContentPromptInput: TextArea
        get() = (inspector.lookup("#txaBookContentPrompt") as AiPromptArea).lookup(".text-area") as TextArea

    private val bookStylePromptInput: TextArea
        get() = (inspector.lookup("#txaBookStylePrompt") as AiPromptArea).lookup(".text-area") as TextArea

    private val chapterNameInput: TextField
        get() = (inspector.lookup("#txtChapterName") as AiTextField).lookup(".text-field") as TextField

    private val chapterContentPromptInput: TextArea
        get() = (inspector.lookup("#txaChapterContentPrompt") as AiPromptArea).lookup(".text-area") as TextArea

    private val blurbPromptInput: TextArea
        get() = (inspector.lookup("#txaBlurbPrompt") as AiPromptArea).lookup(".text-area") as TextArea

    private val bookEmptyBox
        get() = inspector.lookup("#boxBookEmpty")

    private val bookFieldsBox
        get() = inspector.lookup("#boxBookFields")

    private val chapterEmptyBox
        get() = inspector.lookup("#boxChapterEmpty")

    private val chapterFieldsBox
        get() = inspector.lookup("#boxChapterFields")

    private val blurbFieldsBox
        get() = inspector.lookup("#boxBlurbFields")

    private val designSection: TitledPane
        get() = inspector.lookup("#pnlDesignSection") as TitledPane

    private val designEmptyBox
        get() = inspector.lookup("#boxDesignEmpty")

    private val designFieldsBox
        get() = inspector.lookup("#boxDesignFields")

    private val titleStyleEditor: StyleDataEditor
        get() = inspector.lookup("#titleStyleEditor") as StyleDataEditor

    private val chapterTitleStyleEditor: StyleDataEditor
        get() = inspector.lookup("#chapterTitleStyleEditor") as StyleDataEditor

    private val chapterTitleAppendixStyleEditor: StyleDataEditor
        get() = inspector.lookup("#chapterTitleAppendixStyleEditor") as StyleDataEditor

    private val bodyTextStyleEditor: StyleDataEditor
        get() = inspector.lookup("#bodyTextStyleEditor") as StyleDataEditor

    @Suppress("UNCHECKED_CAST")
    private fun StyleDataEditor.familyText(): String =
        (lookup("#cmbFamily") as ComboBox<String>).editor.text

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        inspector = Inspector()
        inspector.bindSelection(selection)
        stage.scene = Scene(inspector, 400.0, 800.0)
        stage.show()
    }

    private fun projectOf(book: Book, author: String = "Jane Doe"): ProjectProperty =
        ProjectProperty(Project(meta = Meta(author = author), book = book))

    private fun styleOf(family: String): StyleData = StyleData(font = FontData(name = family, size = 12))

    private fun projectWithDesignOf(
        titleStyle: StyleData,
        chapterTitleStyle: StyleData,
        chapterTitleAppendixStyle: StyleData,
        bodyTextStyle: StyleData
    ): ProjectProperty =
        ProjectProperty(
            Project(
                design = Design(
                    titlePage = TitlePageDesign(titleStyle = titleStyle),
                    chapterPage = ChapterPageDesign(
                        titleStyle = chapterTitleStyle,
                        titleAppendixStyle = chapterTitleAppendixStyle,
                        textStyle = bodyTextStyle
                    )
                )
            )
        )

    private fun show(book: Book): ProjectProperty {
        val property = projectOf(book)
        interact { inspector.bindProject(property) }
        WaitForAsyncUtils.waitForFxEvents()

        return property
    }

    private fun select(item: ProjectListItem?) {
        interact { selection.value = item }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * Use case: no project is open, so the "Book" section shows its empty state and the fields are
     * hidden.
     */
    @Test
    fun withoutProjectTheBookSectionIsEmpty() {
        assertTrue(bookEmptyBox.isVisible, "the empty state of the book section is missing")
        assertFalse(bookFieldsBox.isVisible, "the fields are shown without a project")
    }

    /**
     * Use case: a project is bound, so the "Book" section shows its fields filled with the manuscript
     * and hides the empty state.
     */
    @Test
    fun boundProjectFillsTheBookSection() {
        show(
            Book(
                title = "The Silent House",
                titleAppendix = listOf("A ghost story"),
                prompts = AIPrompt(contentPrompt = "A house nobody lives in", stylePrompt = "Dark and quiet")
            )
        )

        assertFalse(bookEmptyBox.isVisible, "the empty state is shown although a project is bound")
        assertTrue(bookFieldsBox.isVisible, "the fields are hidden although a project is bound")
        assertEquals("The Silent House", bookTitleInput.text)
        assertEquals("Jane Doe", bookAuthorInput.text)
        assertEquals("A house nobody lives in", bookContentPromptInput.text)
        assertEquals("Dark and quiet", bookStylePromptInput.text)
    }

    /**
     * Use case: the user types into every field of the "Book" section, so the project bound to it
     * carries the written texts - the inspector keeps no copy of its own.
     */
    @Test
    fun everyBookFieldWritesThroughToTheModel() {
        val book = Book(title = "")
        val property = show(book)

        clickOn(bookTitleInput).write("The Silent House")
        interact {
            bookAuthorInput.text = "John Smith"
            bookCopyrightInput.text = "(c) 2026"
            bookContentPromptInput.text = "A house nobody lives in"
            bookStylePromptInput.text = "Dark and quiet"
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("The Silent House", book.title)
        assertEquals("John Smith", property.metaProperty.author)
        assertEquals("(c) 2026", book.copyright.copyright)
        assertEquals("A house nobody lives in", book.prompts.contentPrompt)
        assertEquals("Dark and quiet", book.prompts.stylePrompt)
    }

    /**
     * Use case: the manuscript is written through its property model, so every control of the "Book"
     * section shows the new values.
     */
    @Test
    fun bookFieldsFollowTheProperty() {
        val property = show(Book())

        interact {
            property.bookProperty.title = "The Silent House"
            property.bookProperty.promptsProperty.contentPrompt = "A house nobody lives in"
            property.metaProperty.author = "John Smith"
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("The Silent House", bookTitleInput.text)
        assertEquals("A house nobody lives in", bookContentPromptInput.text)
        assertEquals("John Smith", bookAuthorInput.text)
    }

    /**
     * Use case: no node of the project tree is picked, so the "Chapter" section shows its empty state.
     */
    @Test
    fun withoutSelectionTheChapterSectionIsEmpty() {
        assertTrue(chapterEmptyBox.isVisible, "the empty state of the chapter section is missing")
        assertFalse(chapterFieldsBox.isVisible, "the fields are shown without a selection")
    }

    /**
     * Use case: a chapter is picked in the project tree, so the "Chapter" section shows its name and
     * prompts and hides the empty state.
     */
    @Test
    fun pickedChapterFillsTheChapterSection() {
        val chapter = Chapter(name = "Chapter one", title = "The arrival")
        select(ProjectListItem.ChapterItem(chapter))

        assertFalse(chapterEmptyBox.isVisible, "the empty state is shown although a chapter is picked")
        assertTrue(chapterFieldsBox.isVisible, "the fields are hidden although a chapter is picked")
        assertEquals("Chapter one", chapterNameInput.text)
    }

    /**
     * Use case: the user renames the picked chapter through the "Chapter" section, so the text lands on
     * the very chapter object the project tree still holds.
     */
    @Test
    fun chapterFieldsWriteThroughToTheChapter() {
        val chapter = Chapter(name = "Chapter one", title = "The arrival")
        select(ProjectListItem.ChapterItem(chapter))

        clickOn(chapterNameInput).write(" and two")
        interact { chapterContentPromptInput.text = "Tell how it began" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("Chapter one and two", chapter.name)
        assertEquals("Tell how it began", chapter.prompts.contentPrompt)
    }

    /**
     * Use case: the blurb is picked and already created, so the "Chapter" section shows its prompt
     * instead of a chapter's fields.
     */
    @Test
    fun pickedBlurbFillsTheChapterSectionWithItsPrompt() {
        val blurb = Blurb(prompt = "A gripping tale")
        val book = Book(blurb = blurb)
        show(book)

        select(ProjectListItem.BlurbItem(blurb))

        assertFalse(chapterEmptyBox.isVisible, "the empty state is shown although the blurb is picked")
        assertTrue(blurbFieldsBox.isVisible, "the blurb fields are hidden although the blurb is picked")
        assertEquals("A gripping tale", blurbPromptInput.text)

        interact { blurbPromptInput.text = "A haunting tale" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("A haunting tale", book.blurb.prompt)
    }

    /**
     * Use case: the root of the project tree is picked, so the "Chapter" section shows its empty
     * state, matching neither a chapter nor a blurb.
     */
    @Test
    fun rootSelectionLeavesTheChapterSectionEmpty() {
        select(ProjectListItem.Root)

        assertTrue(chapterEmptyBox.isVisible, "the empty state is missing for the root selection")
    }

    /**
     * Use case: the user collapses the "Book" section, so it no longer shows its fields, and expanding
     * it again shows them once more.
     */
    @Test
    fun bookSectionCanBeCollapsedAndExpanded() {
        show(Book(title = "The Silent House"))
        assertTrue(bookSection.isExpanded, "the section starts collapsed")

        interact { bookSection.isExpanded = false }
        WaitForAsyncUtils.waitForFxEvents()
        assertFalse(bookSection.isExpanded)

        interact { bookSection.isExpanded = true }
        WaitForAsyncUtils.waitForFxEvents()
        assertTrue(bookSection.isExpanded)
        assertEquals("The Silent House", bookTitleInput.text)
    }

    /**
     * Use case: the user collapses the "Chapter" section, so its collapse state is independent from
     * the "Book" section next to it.
     */
    @Test
    fun chapterSectionCollapsesIndependentlyOfTheBookSection() {
        interact { chapterSection.isExpanded = false }
        WaitForAsyncUtils.waitForFxEvents()

        assertFalse(chapterSection.isExpanded)
        assertTrue(bookSection.isExpanded, "collapsing one section collapsed the other as well")
    }

    /**
     * Use case: no project is open, so the "Design" section shows its empty state and the fields are
     * hidden.
     */
    @Test
    fun withoutProjectTheDesignSectionIsEmpty() {
        assertTrue(designEmptyBox.isVisible, "the empty state of the design section is missing")
        assertFalse(designFieldsBox.isVisible, "the fields are shown without a project")
    }

    /**
     * Use case: a project is bound, so the "Design" section shows its four style editors filled with
     * the styles of the bound design and hides the empty state - independent of what is picked in the
     * project tree, unlike the "Chapter" section next to it.
     */
    @Test
    fun boundProjectFillsTheDesignSection() {
        val property = projectWithDesignOf(
            styleOf("Georgia"),
            styleOf("Cambria"),
            styleOf("Verdana"),
            styleOf("Tahoma")
        )
        interact { inspector.bindProject(property) }
        WaitForAsyncUtils.waitForFxEvents()

        assertFalse(designEmptyBox.isVisible, "the empty state is shown although a project is bound")
        assertTrue(designFieldsBox.isVisible, "the fields are hidden although a project is bound")
        assertEquals("Georgia", titleStyleEditor.familyText())
        assertEquals("Cambria", chapterTitleStyleEditor.familyText())
        assertEquals("Verdana", chapterTitleAppendixStyleEditor.familyText())
        assertEquals("Tahoma", bodyTextStyleEditor.familyText())
    }

    /**
     * Use case: the design is written through its property model, so every embedded style editor of
     * the "Design" section shows the new styles.
     */
    @Test
    fun designFieldsFollowTheProperty() {
        val property =
            projectWithDesignOf(styleOf("Georgia"), styleOf("Cambria"), styleOf("Verdana"), styleOf("Tahoma"))
        interact { inspector.bindProject(property) }
        WaitForAsyncUtils.waitForFxEvents()

        interact {
            property.designProperty.titlePageProperty.titleStyleProperty.fontProperty.name = "Palatino"
            property.designProperty.chapterPageProperty.textStyleProperty.fontProperty.name = "Consolas"
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("Palatino", titleStyleEditor.familyText())
        assertEquals("Consolas", bodyTextStyleEditor.familyText())
    }

    /**
     * Use case: the user changes the font family in a style editor of the "Design" section, so it
     * lands on the very design object the project carries - the section keeps no copy of its own.
     */
    @Test
    fun designFieldsWriteThroughToTheModel() {
        val property =
            projectWithDesignOf(styleOf("Georgia"), styleOf("Cambria"), styleOf("Verdana"), styleOf("Tahoma"))
        interact { inspector.bindProject(property) }
        WaitForAsyncUtils.waitForFxEvents()

        interact { property.designProperty.chapterPageProperty.titleStyleProperty.fontProperty.name = "Rockwell" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("Rockwell", chapterTitleStyleEditor.familyText())
    }

    /**
     * Use case: the project is closed while the inspector stands, so the "Design" section falls back
     * to its empty state and no longer crashes on the release of its style editors.
     */
    @Test
    fun closedProjectEmptiesTheDesignSection() {
        val property =
            projectWithDesignOf(styleOf("Georgia"), styleOf("Cambria"), styleOf("Verdana"), styleOf("Tahoma"))
        interact { inspector.bindProject(property) }
        WaitForAsyncUtils.waitForFxEvents()

        interact { inspector.bindProject(null) }
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(designEmptyBox.isVisible, "the empty state is missing after the project was closed")
        assertFalse(designFieldsBox.isVisible, "the fields are shown after the project was closed")
    }

    /**
     * Use case: a second project is bound after the first, so every style editor of the "Design"
     * section follows the new design and writing into it no longer reaches the design left behind.
     */
    @Test
    fun rebindingShowsTheNewDesignAndLeavesTheOldUntouched() {
        val first =
            projectWithDesignOf(styleOf("Georgia"), styleOf("Cambria"), styleOf("Verdana"), styleOf("Tahoma"))
        interact { inspector.bindProject(first) }
        WaitForAsyncUtils.waitForFxEvents()

        val second =
            projectWithDesignOf(styleOf("Calibri"), styleOf("Consolas"), styleOf("Rockwell"), styleOf("Baskerville"))
        interact { inspector.bindProject(second) }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("Calibri", titleStyleEditor.familyText())

        interact { second.designProperty.titlePageProperty.titleStyleProperty.fontProperty.name = "Optima" }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals("Optima", titleStyleEditor.familyText())
        assertEquals("Georgia", first.designProperty.titlePageProperty.titleStyleProperty.get()!!.font.name)
    }

    /**
     * Use case: the user collapses the "Design" section, so it no longer shows its fields, and
     * expanding it again shows them once more, independent of the other two sections.
     */
    @Test
    fun designSectionCanBeCollapsedAndExpanded() {
        val property = projectWithDesignOf(
            styleOf("Georgia"),
            styleOf("Cambria"),
            styleOf("Verdana"),
            styleOf("Tahoma")
        )
        interact { inspector.bindProject(property) }
        WaitForAsyncUtils.waitForFxEvents()
        assertTrue(designSection.isExpanded, "the section starts collapsed")

        interact { designSection.isExpanded = false }
        WaitForAsyncUtils.waitForFxEvents()
        assertFalse(designSection.isExpanded)

        interact { designSection.isExpanded = true }
        WaitForAsyncUtils.waitForFxEvents()
        assertTrue(designSection.isExpanded)
        assertEquals("Georgia", titleStyleEditor.familyText())
    }
}
