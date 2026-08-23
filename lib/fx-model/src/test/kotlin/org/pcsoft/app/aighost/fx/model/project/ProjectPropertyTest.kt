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

package org.pcsoft.app.aighost.fx.model.project

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Blurb
import org.pcsoft.app.aighost.model.project.Book
import org.pcsoft.app.aighost.model.project.BookPart
import org.pcsoft.app.aighost.model.project.Chapter
import org.pcsoft.app.aighost.model.project.Epilog
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.Prolog

/**
 * Developer tests for [ProjectProperty].
 *
 * The property holds the project the user is working on and offers every field of that object - and
 * every field of the objects nested in it, down to the font of a single style - as a property of its
 * own. Every test checks the whole object tree through its root, the way the user interface uses it: a
 * binding hangs on each level - the project itself, the settings and the book below it, the styles, the
 * prolog, the epilog, the blurb and every single field - and the tests assert that a change reaches
 * every binding that has to know about it, upwards to the root as well as downwards into the fields of
 * an exchanged object.
 */
class ProjectPropertyTest {

    private lateinit var project: Project
    private lateinit var property: ProjectProperty
    private lateinit var settingsProperty: SettingsProperty
    private lateinit var bookProperty: BookProperty

    /** Binding on the whole project, standing for a view bound to the root of the object tree. */
    private lateinit var rootView: StringProperty
    private var rootViewChanges = 0

    /** Binding on the name of the project. */
    private lateinit var nameView: StringProperty
    private var nameViewChanges = 0

    /** Binding on the author of the manuscript. */
    private lateinit var authorView: StringProperty
    private var authorViewChanges = 0

    /** Binding on the copyright notice. */
    private lateinit var copyrightView: StringProperty
    private var copyrightViewChanges = 0

    /** Binding on the settings, standing for a view bound to that nested object. */
    private lateinit var settingsView: StringProperty
    private var settingsViewChanges = 0

    /** Binding on the family name of the text style, the deepest field of the whole tree. */
    private lateinit var textFontNameView: StringProperty
    private var textFontNameViewChanges = 0

    /** Binding on the flag of the separate copyright page. */
    private lateinit var copyrightPageView: StringProperty
    private var copyrightPageViewChanges = 0

    /** Binding on the book, standing for a view bound to that nested object. */
    private lateinit var bookView: StringProperty
    private var bookViewChanges = 0

    /** Binding on the main title of the book. */
    private lateinit var bookTitleView: StringProperty
    private var bookTitleViewChanges = 0

    /** Binding on the chapters of the book. */
    private lateinit var chaptersView: StringProperty
    private var chaptersViewChanges = 0

    /** Binding on the prolog, an object the book carries only after the user created it. */
    private lateinit var prologView: StringProperty
    private var prologViewChanges = 0

    /** Binding on the heading nested in the prolog. */
    private lateinit var prologTitleView: StringProperty
    private var prologTitleViewChanges = 0

    /** Binding on the epilog, an object the book carries only after the user created it. */
    private lateinit var epilogView: StringProperty
    private var epilogViewChanges = 0

    /** Binding on the heading nested in the epilog. */
    private lateinit var epilogTitleView: StringProperty
    private var epilogTitleViewChanges = 0

    /** Binding on the blurb, an object the book carries only after the user created it. */
    private lateinit var blurbView: StringProperty
    private var blurbViewChanges = 0

    /** Binding on the paragraphs nested in the blurb. */
    private lateinit var blurbParagraphView: StringProperty
    private var blurbParagraphViewChanges = 0

    @BeforeEach
    fun setUp() {
        project = newProject()
        property = ProjectProperty()
        property.value = project
        settingsProperty = property.settingsProperty as SettingsProperty
        bookProperty = property.bookProperty as BookProperty

        rootView = SimpleStringProperty()
        val rootBinding = Bindings.createStringBinding({ projectState(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        rootBinding.addListener { _, _, _ -> rootViewChanges++ }
        rootView.bind(rootBinding)

        nameView = SimpleStringProperty()
        val nameBinding = Bindings.createStringBinding(
            { property.nameProperty.get() ?: MISSING },
            property.nameProperty
        )
        nameBinding.addListener { _, _, _ -> nameViewChanges++ }
        nameView.bind(nameBinding)

        authorView = SimpleStringProperty()
        val authorBinding = Bindings.createStringBinding(
            { property.authorProperty.get() ?: MISSING },
            property.authorProperty
        )
        authorBinding.addListener { _, _, _ -> authorViewChanges++ }
        authorView.bind(authorBinding)

        copyrightView = SimpleStringProperty()
        val copyrightBinding = Bindings.createStringBinding(
            { property.copyrightProperty.get() ?: MISSING },
            property.copyrightProperty
        )
        copyrightBinding.addListener { _, _, _ -> copyrightViewChanges++ }
        copyrightView.bind(copyrightBinding)

        settingsView = SimpleStringProperty()
        val settingsBinding = Bindings.createStringBinding(
            { settingsState(settingsProperty.value) },
            settingsProperty
        )
        settingsBinding.addListener { _, _, _ -> settingsViewChanges++ }
        settingsView.bind(settingsBinding)

        textFontNameView = SimpleStringProperty()
        val textFontNameBinding = Bindings.createStringBinding(
            { settingsProperty.textFontProperty.fontProperty.nameProperty.get() ?: MISSING },
            settingsProperty.textFontProperty.fontProperty.nameProperty
        )
        textFontNameBinding.addListener { _, _, _ -> textFontNameViewChanges++ }
        textFontNameView.bind(textFontNameBinding)

        copyrightPageView = SimpleStringProperty()
        val copyrightPageBinding = settingsProperty.copyrightPageProperty.asString()
        copyrightPageBinding.addListener { _, _, _ -> copyrightPageViewChanges++ }
        copyrightPageView.bind(copyrightPageBinding)

        bookView = SimpleStringProperty()
        val bookBinding = Bindings.createStringBinding({ bookState(bookProperty.value) }, bookProperty)
        bookBinding.addListener { _, _, _ -> bookViewChanges++ }
        bookView.bind(bookBinding)

        bookTitleView = SimpleStringProperty()
        val bookTitleBinding = Bindings.createStringBinding(
            { bookProperty.titleProperty.get() ?: MISSING },
            bookProperty.titleProperty
        )
        bookTitleBinding.addListener { _, _, _ -> bookTitleViewChanges++ }
        bookTitleView.bind(bookTitleBinding)

        chaptersView = SimpleStringProperty()
        val chaptersBinding = Bindings.createStringBinding(
            { bookProperty.chaptersProperty.joinToString(";") { it.name } },
            bookProperty.chaptersProperty
        )
        chaptersBinding.addListener { _, _, _ -> chaptersViewChanges++ }
        chaptersView.bind(chaptersBinding)

        prologView = SimpleStringProperty()
        val prologBinding = Bindings.createStringBinding(
            { partState(bookProperty.prologProperty.value) },
            bookProperty.prologProperty
        )
        prologBinding.addListener { _, _, _ -> prologViewChanges++ }
        prologView.bind(prologBinding)

        prologTitleView = SimpleStringProperty()
        val prologTitleBinding = Bindings.createStringBinding(
            { bookProperty.prologProperty.titleProperty.get() ?: MISSING },
            bookProperty.prologProperty.titleProperty
        )
        prologTitleBinding.addListener { _, _, _ -> prologTitleViewChanges++ }
        prologTitleView.bind(prologTitleBinding)

        epilogView = SimpleStringProperty()
        val epilogBinding = Bindings.createStringBinding(
            { partState(bookProperty.epilogProperty.value) },
            bookProperty.epilogProperty
        )
        epilogBinding.addListener { _, _, _ -> epilogViewChanges++ }
        epilogView.bind(epilogBinding)

        epilogTitleView = SimpleStringProperty()
        val epilogTitleBinding = Bindings.createStringBinding(
            { bookProperty.epilogProperty.titleProperty.get() ?: MISSING },
            bookProperty.epilogProperty.titleProperty
        )
        epilogTitleBinding.addListener { _, _, _ -> epilogTitleViewChanges++ }
        epilogTitleView.bind(epilogTitleBinding)

        blurbView = SimpleStringProperty()
        val blurbBinding = Bindings.createStringBinding(
            { blurbState(bookProperty.blurbProperty.value) },
            bookProperty.blurbProperty
        )
        blurbBinding.addListener { _, _, _ -> blurbViewChanges++ }
        blurbView.bind(blurbBinding)

        blurbParagraphView = SimpleStringProperty()
        val blurbParagraphBinding = Bindings.createStringBinding(
            { bookProperty.blurbProperty.paragraphProperty.joinToString(";") },
            bookProperty.blurbProperty.paragraphProperty
        )
        blurbParagraphBinding.addListener { _, _, _ -> blurbParagraphViewChanges++ }
        blurbParagraphView.bind(blurbParagraphBinding)

        resetCounters()
    }

    private fun resetCounters() {
        rootViewChanges = 0
        nameViewChanges = 0
        authorViewChanges = 0
        copyrightViewChanges = 0
        settingsViewChanges = 0
        textFontNameViewChanges = 0
        copyrightPageViewChanges = 0
        bookViewChanges = 0
        bookTitleViewChanges = 0
        chaptersViewChanges = 0
        prologViewChanges = 0
        prologTitleViewChanges = 0
        epilogViewChanges = 0
        epilogTitleViewChanges = 0
        blurbViewChanges = 0
        blurbParagraphViewChanges = 0
    }

    /** The project every test starts from, built fresh so no test sees the objects of another. */
    private fun newProject(): Project = Project(
        name = "The long journey project",
        author = "Jane Doe",
        copyright = "(c) 2026 Jane Doe",
        settings = Project.Settings(
            authorFont = StyleData(FontData("Author Serif", 10, bold = false, italic = true), Alignment.RIGHT),
            copyrightFont = StyleData(FontData("Copyright Serif", 8, bold = false, italic = false), Alignment.LEFT),
            titleFont = StyleData(FontData("Title Serif", 28, bold = true, italic = false), Alignment.CENTER),
            titleAppendixFont = StyleData(
                FontData("Title Appendix Serif", 18, bold = false, italic = true),
                Alignment.CENTER
            ),
            chapterFont = StyleData(FontData("Chapter Serif", 20, bold = true, italic = false), Alignment.LEFT),
            chapterAppendixFont = StyleData(
                FontData("Chapter Appendix Serif", 14, bold = false, italic = true),
                Alignment.LEFT
            ),
            textFont = StyleData(FontData("Text Serif", 12, bold = false, italic = false), Alignment.BLOCK),
            copyrightPage = false,
            startWithEmptyPage = true,
            endWithEmptyPage = true
        ),
        book = Book(
            title = "The long journey",
            titleAppendix = listOf("A novel"),
            prolog = null,
            chapters = listOf(
                Chapter(name = "Chapter one", title = "The arrival", paragraph = listOf("The train was late."))
            ),
            epilog = null,
            blurb = null
        )
    )

    /** Text form of the whole project, used as the value of the binding on the root. */
    private fun projectState(project: Project?): String =
        "${project?.name ?: MISSING}|${project?.author ?: MISSING}|${project?.copyright ?: MISSING}|" +
                "${settingsState(project?.settings)}|${bookState(project?.book)}"

    /** Text form of the settings, used as the value of the binding on that nested object. */
    private fun settingsState(settings: Project.Settings?): String = listOf(
        styleState(settings?.authorFont),
        styleState(settings?.copyrightFont),
        styleState(settings?.titleFont),
        styleState(settings?.titleAppendixFont),
        styleState(settings?.chapterFont),
        styleState(settings?.chapterAppendixFont),
        styleState(settings?.textFont),
        (settings?.copyrightPage ?: false).toString(),
        (settings?.startWithEmptyPage ?: false).toString(),
        (settings?.endWithEmptyPage ?: false).toString()
    ).joinToString("|")

    /** Text form of a single style, short enough to be part of the state of the whole project. */
    private fun styleState(style: StyleData?): String =
        "${style?.font?.name ?: MISSING}/${style?.font?.size ?: 0}/${style?.font?.bold ?: false}/" +
                "${style?.font?.italic ?: false}/${style?.alignment ?: MISSING}"

    /** Text form of the manuscript, used as the value of the binding on that nested object. */
    private fun bookState(book: Book?): String =
        "${book?.title ?: MISSING}|${book?.titleAppendix.orEmpty().joinToString(";")}|" +
                "${partState(book?.prolog)}|${book?.chapters.orEmpty().joinToString(";") { it.name }}|" +
                "${partState(book?.epilog)}|${blurbState(book?.blurb)}"

    /** Text form of a written part of the book, used as the value of the binding on that object. */
    private fun partState(part: BookPart?): String =
        "${part?.title ?: MISSING}|${part?.titleAppendix.orEmpty().joinToString(";")}|" +
                part?.paragraph.orEmpty().joinToString(";")

    /** Text form of the blurb, used as the value of the binding on that object. */
    private fun blurbState(blurb: Blurb?): String = blurb?.paragraph.orEmpty().joinToString(";")

    /**
     * Asserts that every binding of the whole object tree delivers exactly what the model object
     * carries right now, so no view keeps the value of a previous object or of a previous field value.
     */
    private fun assertTreeShowsModel() {
        val current = property.value
        val settings = current?.settings
        val book = current?.book

        assertEquals(projectState(current), rootView.get()) {
            "the binding on the project delivers an outdated state"
        }
        assertEquals(current?.name ?: MISSING, nameView.get()) {
            "the binding on the name delivers an outdated value"
        }
        assertEquals(current?.author ?: MISSING, authorView.get()) {
            "the binding on the author delivers an outdated value"
        }
        assertEquals(current?.copyright ?: MISSING, copyrightView.get()) {
            "the binding on the copyright notice delivers an outdated value"
        }
        assertEquals(settingsState(settings), settingsView.get()) {
            "the binding on the settings delivers an outdated state"
        }
        assertEquals(settings?.textFont?.font?.name ?: MISSING, textFontNameView.get()) {
            "the binding on the family name of the text style delivers an outdated value"
        }
        assertEquals((settings?.copyrightPage ?: false).toString(), copyrightPageView.get()) {
            "the binding on the copyright page delivers an outdated value"
        }
        assertEquals(bookState(book), bookView.get()) {
            "the binding on the book delivers an outdated state"
        }
        assertEquals(book?.title ?: MISSING, bookTitleView.get()) {
            "the binding on the main title delivers an outdated value"
        }
        assertEquals(book?.chapters.orEmpty().joinToString(";") { it.name }, chaptersView.get()) {
            "the binding on the chapters delivers outdated chapters"
        }
        assertEquals(partState(book?.prolog), prologView.get()) {
            "the binding on the prolog delivers an outdated state"
        }
        assertEquals(book?.prolog?.title ?: MISSING, prologTitleView.get()) {
            "the binding on the heading of the prolog delivers an outdated value"
        }
        assertEquals(partState(book?.epilog), epilogView.get()) {
            "the binding on the epilog delivers an outdated state"
        }
        assertEquals(book?.epilog?.title ?: MISSING, epilogTitleView.get()) {
            "the binding on the heading of the epilog delivers an outdated value"
        }
        assertEquals(blurbState(book?.blurb), blurbView.get()) {
            "the binding on the blurb delivers an outdated state"
        }
        assertEquals(book?.blurb?.paragraph.orEmpty().joinToString(";"), blurbParagraphView.get()) {
            "the binding on the paragraphs of the blurb delivers outdated paragraphs"
        }
    }

    /**
     * Use case: a project file is read before the user interface is built, so every binding of the whole
     * object tree delivers the state that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShowsModel()
        assertEquals("The long journey project", nameView.get())
        assertEquals("Text Serif", textFontNameView.get())
        assertEquals("Chapter one", chaptersView.get())
    }

    /**
     * Use case: the user renames the project, so the name lands in the model object and both the binding
     * on that field and the binding on the project show it.
     */
    @Test
    fun writesNameToModelAndNotifiesTree() {
        property.name = "The way back project"

        assertEquals("The way back project", project.name)
        assertTreeShowsModel()
        assertTrue(nameViewChanges > 0) { "the binding on the name was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the name is bound to the text field of the project dialog, so every text that field
     * produces reaches the model object and both bindings above it show it.
     */
    @Test
    fun writesBoundNameToModelAndNotifiesTree() {
        val source = SimpleStringProperty("Draft project")
        property.nameProperty.bind(source)

        source.set("The way back project")

        assertEquals("The way back project", project.name)
        assertTreeShowsModel()
        assertTrue(nameViewChanges > 0) { "the binding on the name was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the user types the author of the manuscript, so the text lands in the model object and
     * both the binding on that field and the binding on the project show it.
     */
    @Test
    fun writesAuthorToModelAndNotifiesTree() {
        property.author = "John Roe"

        assertEquals("John Roe", project.author)
        assertTreeShowsModel()
        assertTrue(authorViewChanges > 0) { "the binding on the author was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the author is bound to the text field of the project dialog, so every text that field
     * produces reaches the model object and both bindings above it show it.
     */
    @Test
    fun writesBoundAuthorToModelAndNotifiesTree() {
        val source = SimpleStringProperty("Jane Doe")
        property.authorProperty.bind(source)

        source.set("John Roe")

        assertEquals("John Roe", project.author)
        assertTreeShowsModel()
        assertTrue(authorViewChanges > 0) { "the binding on the author was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the user types the copyright notice, so the text lands in the model object and both the
     * binding on that field and the binding on the project show it.
     */
    @Test
    fun writesCopyrightToModelAndNotifiesTree() {
        property.copyright = "(c) 2026 John Roe"

        assertEquals("(c) 2026 John Roe", project.copyright)
        assertTreeShowsModel()
        assertTrue(copyrightViewChanges > 0) { "the binding on the copyright notice was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the copyright notice is bound to the text field of the project dialog, so every text
     * that field produces reaches the model object and both bindings above it show it.
     */
    @Test
    fun writesBoundCopyrightToModelAndNotifiesTree() {
        val source = SimpleStringProperty("(c) 2026 Jane Doe")
        property.copyrightProperty.bind(source)

        source.set("(c) 2026 John Roe")

        assertEquals("(c) 2026 John Roe", project.copyright)
        assertTreeShowsModel()
        assertTrue(copyrightViewChanges > 0) { "the binding on the copyright notice was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the user asks for a separate copyright page in the settings dialog, so the flag lands in
     * the nested model object and the bindings on that field, on the settings and on the project show
     * it.
     */
    @Test
    fun writesNestedCopyrightPageToModelAndNotifiesTree() {
        settingsProperty.copyrightPage = true

        assertEquals(true, project.settings.copyrightPage)
        assertTreeShowsModel()
        assertTrue(copyrightPageViewChanges > 0) { "the binding on the copyright page was not re-evaluated" }
        assertTrue(settingsViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the user picks another type face for the chapter text, so the family name lands in the
     * font of the text style - the deepest object of the whole tree - and every binding between that
     * field and the project shows it.
     */
    @Test
    fun writesNestedTextFontNameToModelAndNotifiesTree() {
        settingsProperty.textFontProperty.fontProperty.name = "Modern Text"

        assertEquals("Modern Text", project.settings.textFont.font.name)
        assertTreeShowsModel()
        assertTrue(textFontNameViewChanges > 0) {
            "the binding on the family name of the text style was not re-evaluated"
        }
        assertTrue(settingsViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the family name of the text style is bound to the choice box of the font dialog, so
     * every name that choice box produces reaches the deepest object of the tree and every binding
     * between that field and the project shows it.
     */
    @Test
    fun writesBoundNestedTextFontNameToModelAndNotifiesTree() {
        val source = SimpleStringProperty("Text Serif")
        settingsProperty.textFontProperty.fontProperty.nameProperty.bind(source)

        source.set("Modern Text")

        assertEquals("Modern Text", project.settings.textFont.font.name)
        assertTreeShowsModel()
        assertTrue(textFontNameViewChanges > 0) {
            "the binding on the family name of the text style was not re-evaluated"
        }
        assertTrue(settingsViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the whole settings object is replaced, so every property below it belongs to another
     * object afterwards and every binding of the object tree shows the values of that object instead of
     * the previous ones.
     */
    @Test
    fun writesSettingsToModelAndNotifiesTree() {
        val settings = Project.Settings(
            textFont = StyleData(FontData("Modern Text", 13, bold = true, italic = true), Alignment.LEFT),
            copyrightPage = true
        )

        property.settings = settings

        assertEquals(settings, project.settings)
        assertTreeShowsModel()
        assertTrue(textFontNameViewChanges > 0) {
            "the binding on the family name of the text style was not re-evaluated"
        }
        assertTrue(copyrightPageViewChanges > 0) { "the binding on the copyright page was not re-evaluated" }
        assertTrue(settingsViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the user renames the book, so the title lands in the nested model object and the
     * bindings on that field, on the book and on the project show it.
     */
    @Test
    fun writesNestedBookTitleToModelAndNotifiesTree() {
        bookProperty.title = "The way back"

        assertEquals("The way back", project.book.title)
        assertTreeShowsModel()
        assertTrue(bookTitleViewChanges > 0) { "the binding on the main title was not re-evaluated" }
        assertTrue(bookViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the user adds a chapter in the project tree, so the content change alone reaches the
     * nested model object and the bindings on that field, on the book and on the project show it.
     */
    @Test
    fun writesNestedChapterAddedToModelAndNotifiesTree() {
        bookProperty.chaptersProperty.add(Chapter(name = "Chapter two", title = "The departure"))

        assertEquals(listOf("Chapter one", "Chapter two"), project.book.chapters.map { it.name })
        assertTreeShowsModel()
        assertTrue(chaptersViewChanges > 0) { "the binding on the chapters was not re-evaluated" }
        assertTrue(bookViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the chapters are bound to the project tree, so every list that tree produces reaches the
     * nested model object and every binding above it shows it.
     */
    @Test
    fun writesBoundNestedChaptersToModelAndNotifiesTree() {
        val source = SimpleObjectProperty(
            FXCollections.observableArrayList(Chapter(name = "Chapter one", title = "The arrival"))
        )
        bookProperty.chaptersProperty.bind(source)

        source.set(FXCollections.observableArrayList(Chapter(name = "Chapter two", title = "The departure")))

        assertEquals(listOf("Chapter two"), project.book.chapters.map { it.name })
        assertTreeShowsModel()
        assertTrue(chaptersViewChanges > 0) { "the binding on the chapters was not re-evaluated" }
        assertTrue(bookViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the whole manuscript is replaced, so every property below it belongs to another object
     * afterwards and every binding of the object tree shows the values of that object instead of the
     * previous ones.
     */
    @Test
    fun writesBookToModelAndNotifiesTree() {
        val book = Book(
            title = "The way back",
            titleAppendix = listOf("In three parts"),
            prolog = Prolog(title = "After the storm", paragraph = listOf("Then the wind came.")),
            chapters = listOf(Chapter(name = "Chapter two", title = "The departure")),
            epilog = Epilog(title = "The years after"),
            blurb = Blurb(paragraph = listOf("For everyone who ever left home."))
        )

        property.book = book

        assertEquals(book, project.book)
        assertTreeShowsModel()
        assertTrue(bookTitleViewChanges > 0) { "the binding on the main title was not re-evaluated" }
        assertTrue(chaptersViewChanges > 0) { "the binding on the chapters was not re-evaluated" }
        assertTrue(prologTitleViewChanges > 0) { "the binding on the heading of the prolog was not re-evaluated" }
        assertTrue(epilogTitleViewChanges > 0) { "the binding on the heading of the epilog was not re-evaluated" }
        assertTrue(blurbParagraphViewChanges > 0) {
            "the binding on the paragraphs of the blurb was not re-evaluated"
        }
        assertTrue(bookViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the book of a fresh project carries no prolog, so the properties below the prolog answer
     * with neutral values; as soon as the user creates one they take over its values and every binding
     * up to the project reports the change.
     */
    @Test
    fun createsPrologAndNotifiesTree() {
        assertNull(project.book.prolog)
        assertEquals(MISSING, prologTitleView.get())

        val prolog = Prolog(
            title = "Before the storm",
            titleAppendix = listOf("A short note"),
            paragraph = listOf("The night was calm.")
        )
        bookProperty.prolog = prolog

        assertEquals(prolog, project.book.prolog)
        assertTreeShowsModel()
        assertTrue(prologTitleViewChanges > 0) { "the binding on the heading of the prolog was not re-evaluated" }
        assertTrue(prologViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(bookViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the user writes into the prolog created a moment ago, so the text lands in the nested
     * model object and every binding between that field and the project shows it.
     */
    @Test
    fun writesNestedPrologTitleToModelAndNotifiesTree() {
        bookProperty.prolog = Prolog(title = "Before the storm")
        resetCounters()

        bookProperty.prologProperty.title = "After the storm"

        assertEquals("After the storm", project.book.prolog?.title)
        assertTreeShowsModel()
        assertTrue(prologTitleViewChanges > 0) { "the binding on the heading of the prolog was not re-evaluated" }
        assertTrue(prologViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(bookViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the book of a fresh project carries no epilog, so the properties below the epilog answer
     * with neutral values; as soon as the user creates one they take over its values and every binding
     * up to the project reports the change.
     */
    @Test
    fun createsEpilogAndNotifiesTree() {
        assertNull(project.book.epilog)
        assertEquals(MISSING, epilogTitleView.get())

        val epilog = Epilog(title = "What remains", paragraph = listOf("The house stood empty."))
        bookProperty.epilog = epilog

        assertEquals(epilog, project.book.epilog)
        assertTreeShowsModel()
        assertTrue(epilogTitleViewChanges > 0) { "the binding on the heading of the epilog was not re-evaluated" }
        assertTrue(epilogViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(bookViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the book of a fresh project carries no blurb, so the property below the blurb answers
     * with no paragraphs; as soon as the user creates one it takes over its text and every binding up to
     * the project reports the change.
     */
    @Test
    fun createsBlurbAndNotifiesTree() {
        assertNull(project.book.blurb)
        assertEquals("", blurbParagraphView.get())

        val blurb = Blurb(paragraph = listOf("A story about a long journey."))
        bookProperty.blurb = blurb

        assertEquals(blurb, project.book.blurb)
        assertTreeShowsModel()
        assertTrue(blurbParagraphViewChanges > 0) {
            "the binding on the paragraphs of the blurb was not re-evaluated"
        }
        assertTrue(blurbViewChanges > 0) { "the binding on the blurb was not re-evaluated" }
        assertTrue(bookViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: a field somewhere in the object tree is changed by application code past the properties,
     * so every field property delivers the current value instead of a cached copy.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        project.name = "The way back project"
        project.author = "John Roe"
        project.copyright = "(c) 2026 John Roe"
        project.settings.copyrightPage = true
        project.settings.textFont.font.name = "Modern Text"
        project.book.title = "The way back"
        project.book.chapters = listOf(Chapter(name = "Chapter two", title = "The departure"))
        project.book.blurb = Blurb(paragraph = listOf("For everyone who ever left home."))

        assertEquals("The way back project", property.name)
        assertEquals("John Roe", property.author)
        assertEquals("(c) 2026 John Roe", property.copyright)
        assertTrue(settingsProperty.copyrightPage)
        assertEquals("Modern Text", settingsProperty.textFontProperty.fontProperty.name)
        assertEquals("The way back", bookProperty.title)
        assertEquals(listOf("Chapter two"), bookProperty.chapters.map { it.name })
        assertEquals(
            listOf("For everyone who ever left home."),
            bookProperty.blurbProperty.paragraph
        )
    }

    /**
     * Use case: another project file is loaded, so the whole project object behind the property is
     * exchanged and every binding of the object tree - down to the fields of the objects nested in it -
     * shows the values of the new object instead of the previous ones.
     */
    @Test
    fun writesReplacedProjectToModelAndNotifiesWholeTree() {
        property.value = Project(
            name = "The way back project",
            author = "John Roe",
            copyright = "(c) 2026 John Roe",
            settings = Project.Settings(
                textFont = StyleData(FontData("Modern Text", 13, bold = true, italic = true), Alignment.LEFT),
                copyrightPage = true
            ),
            book = Book(
                title = "The way back",
                titleAppendix = listOf("In three parts"),
                prolog = Prolog(title = "After the storm", paragraph = listOf("Then the wind came.")),
                chapters = listOf(Chapter(name = "Chapter two", title = "The departure")),
                epilog = Epilog(title = "The years after"),
                blurb = Blurb(paragraph = listOf("For everyone who ever left home."))
            )
        )

        assertTreeShowsModel()
        assertTrue(nameViewChanges > 0) { "the binding on the name was not re-evaluated" }
        assertTrue(authorViewChanges > 0) { "the binding on the author was not re-evaluated" }
        assertTrue(copyrightViewChanges > 0) { "the binding on the copyright notice was not re-evaluated" }
        assertTrue(textFontNameViewChanges > 0) {
            "the binding on the family name of the text style was not re-evaluated"
        }
        assertTrue(copyrightPageViewChanges > 0) { "the binding on the copyright page was not re-evaluated" }
        assertTrue(settingsViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        assertTrue(bookTitleViewChanges > 0) { "the binding on the main title was not re-evaluated" }
        assertTrue(chaptersViewChanges > 0) { "the binding on the chapters was not re-evaluated" }
        assertTrue(prologTitleViewChanges > 0) { "the binding on the heading of the prolog was not re-evaluated" }
        assertTrue(prologViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(epilogTitleViewChanges > 0) { "the binding on the heading of the epilog was not re-evaluated" }
        assertTrue(epilogViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(blurbParagraphViewChanges > 0) {
            "the binding on the paragraphs of the blurb was not re-evaluated"
        }
        assertTrue(blurbViewChanges > 0) { "the binding on the blurb was not re-evaluated" }
        assertTrue(bookViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the project was not re-evaluated" }
    }

    /**
     * Use case: the project is exchanged for an object carrying the same values - the very same file was
     * loaded again - so nothing the user interface shows changes and no field property of the whole tree
     * reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedProjectCarriesTheSameValues() {
        property.value = newProject()

        assertTreeShowsModel()
        assertEquals(0, nameViewChanges) { "the name was reported as changed although it did not change" }
        assertEquals(0, authorViewChanges) { "the author was reported as changed although it did not change" }
        assertEquals(0, copyrightViewChanges) {
            "the copyright notice was reported as changed although it did not change"
        }
        assertEquals(0, textFontNameViewChanges) {
            "the family name of the text style was reported as changed although it did not change"
        }
        assertEquals(0, copyrightPageViewChanges) {
            "the copyright page was reported as changed although it did not change"
        }
        assertEquals(0, settingsViewChanges) { "the settings were reported as changed although they did not change" }
        assertEquals(0, bookTitleViewChanges) {
            "the main title was reported as changed although it did not change"
        }
        assertEquals(0, chaptersViewChanges) { "the chapters were reported as changed although they did not change" }
        assertEquals(0, prologViewChanges) { "the prolog was reported as changed although it did not change" }
        assertEquals(0, epilogViewChanges) { "the epilog was reported as changed although it did not change" }
        assertEquals(0, blurbViewChanges) { "the blurb was reported as changed although it did not change" }
        assertEquals(0, bookViewChanges) { "the book was reported as changed although it did not change" }
    }

    private companion object {
        /** Stands for a value the model object does not carry at all. */
        const val MISSING = "-"
    }
}
