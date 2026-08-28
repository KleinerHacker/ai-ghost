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
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.BookPart
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.common.AIPrompt

/**
 * Developer tests for [BookProperty].
 *
 * The property wraps the manuscript of a project and offers every field of that object - and every
 * field of the objects nested in it - as a property of its own. Every test checks the object tree the
 * way the user interface uses it: a binding hangs on each property of the tree - the book itself, the
 * prolog, the epilog and the blurb below it and every single field - and the tests assert that a change
 * reaches every binding that has to know about it, upwards to the parent the property reports to as
 * well as downwards into the fields of an exchanged object.
 */
class BookPropertyTest {

    /** Stands for the project carrying the book, the object a parent property writes into. */
    private class Holder(var book: Book?)

    private lateinit var holder: Holder
    private lateinit var property: BookProperty

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    /** Binding on the whole book, standing for a view bound to the root of this object tree. */
    private lateinit var rootView: StringProperty
    private var rootViewChanges = 0

    /** Binding on the main title of the book. */
    private lateinit var titleView: StringProperty
    private var titleViewChanges = 0

    /** Binding on the further title lines. */
    private lateinit var titleAppendixView: StringProperty
    private var titleAppendixViewChanges = 0

    /** Binding on the prompts of the whole manuscript. */
    private lateinit var promptsView: StringProperty
    private var promptsViewChanges = 0

    /** Binding on the prolog, standing for a view bound to that nested object. */
    private lateinit var prologView: StringProperty
    private var prologViewChanges = 0

    /** Binding on the heading nested in the prolog. */
    private lateinit var prologTitleView: StringProperty
    private var prologTitleViewChanges = 0

    /** Binding on the paragraphs nested in the prolog. */
    private lateinit var prologParagraphView: StringProperty
    private var prologParagraphViewChanges = 0

    /** Binding on the prompts nested in the prolog. */
    private lateinit var prologPromptsView: StringProperty
    private var prologPromptsViewChanges = 0

    /** Binding on the chapters of the book. */
    private lateinit var chaptersView: StringProperty
    private var chaptersViewChanges = 0

    /** Binding on the epilog, standing for a view bound to that nested object. */
    private lateinit var epilogView: StringProperty
    private var epilogViewChanges = 0

    /** Binding on the heading nested in the epilog. */
    private lateinit var epilogTitleView: StringProperty
    private var epilogTitleViewChanges = 0

    /** Binding on the blurb, standing for a view bound to that nested object. */
    private lateinit var blurbView: StringProperty
    private var blurbViewChanges = 0

    /** Binding on the paragraphs nested in the blurb. */
    private lateinit var blurbParagraphView: StringProperty
    private var blurbParagraphViewChanges = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newBook())
        parentEvents = 0
        property = BookProperty(
            { holder.book = it },
            { holder.book },
            { parentEvents++ }
        )
        // A parent property aligns a nested one with the model object as soon as that object arrives,
        // so the same alignment happens here before the views are built.
        property.refresh()

        rootView = SimpleStringProperty()
        val rootBinding = Bindings.createStringBinding({ bookState(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        rootBinding.addListener { _, _, _ -> rootViewChanges++ }
        rootView.bind(rootBinding)

        titleView = SimpleStringProperty()
        val titleBinding = Bindings.createStringBinding(
            { property.titleProperty.get() ?: MISSING },
            property.titleProperty
        )
        titleBinding.addListener { _, _, _ -> titleViewChanges++ }
        titleView.bind(titleBinding)

        titleAppendixView = SimpleStringProperty()
        val titleAppendixBinding = Bindings.createStringBinding(
            { property.titleAppendixProperty.joinToString(";") },
            property.titleAppendixProperty
        )
        titleAppendixBinding.addListener { _, _, _ -> titleAppendixViewChanges++ }
        titleAppendixView.bind(titleAppendixBinding)

        promptsView = SimpleStringProperty()
        val promptsBinding = Bindings.createStringBinding(
            { promptText(property.promptsProperty.get()) },
            property.promptsProperty
        )
        promptsBinding.addListener { _, _, _ -> promptsViewChanges++ }
        promptsView.bind(promptsBinding)

        prologView = SimpleStringProperty()
        val prologBinding = Bindings.createStringBinding(
            { partState(property.prologProperty.value) },
            property.prologProperty
        )
        prologBinding.addListener { _, _, _ -> prologViewChanges++ }
        prologView.bind(prologBinding)

        prologTitleView = SimpleStringProperty()
        val prologTitleBinding = Bindings.createStringBinding(
            { property.prologProperty.titleProperty.get() ?: MISSING },
            property.prologProperty.titleProperty
        )
        prologTitleBinding.addListener { _, _, _ -> prologTitleViewChanges++ }
        prologTitleView.bind(prologTitleBinding)

        prologParagraphView = SimpleStringProperty()
        val prologParagraphBinding = Bindings.createStringBinding(
            { property.prologProperty.paragraphProperty.joinToString(";") },
            property.prologProperty.paragraphProperty
        )
        prologParagraphBinding.addListener { _, _, _ -> prologParagraphViewChanges++ }
        prologParagraphView.bind(prologParagraphBinding)

        prologPromptsView = SimpleStringProperty()
        val prologPromptsBinding = Bindings.createStringBinding(
            { promptText(property.prologProperty.promptsProperty.get()) },
            property.prologProperty.promptsProperty
        )
        prologPromptsBinding.addListener { _, _, _ -> prologPromptsViewChanges++ }
        prologPromptsView.bind(prologPromptsBinding)

        chaptersView = SimpleStringProperty()
        val chaptersBinding = Bindings.createStringBinding(
            { property.chaptersProperty.joinToString(";") { it.name } },
            property.chaptersProperty
        )
        chaptersBinding.addListener { _, _, _ -> chaptersViewChanges++ }
        chaptersView.bind(chaptersBinding)

        epilogView = SimpleStringProperty()
        val epilogBinding = Bindings.createStringBinding(
            { partState(property.epilogProperty.value) },
            property.epilogProperty
        )
        epilogBinding.addListener { _, _, _ -> epilogViewChanges++ }
        epilogView.bind(epilogBinding)

        epilogTitleView = SimpleStringProperty()
        val epilogTitleBinding = Bindings.createStringBinding(
            { property.epilogProperty.titleProperty.get() ?: MISSING },
            property.epilogProperty.titleProperty
        )
        epilogTitleBinding.addListener { _, _, _ -> epilogTitleViewChanges++ }
        epilogTitleView.bind(epilogTitleBinding)

        blurbView = SimpleStringProperty()
        val blurbBinding = Bindings.createStringBinding(
            { blurbState(property.blurbProperty.value) },
            property.blurbProperty
        )
        blurbBinding.addListener { _, _, _ -> blurbViewChanges++ }
        blurbView.bind(blurbBinding)

        blurbParagraphView = SimpleStringProperty()
        val blurbParagraphBinding = Bindings.createStringBinding(
            { property.blurbProperty.paragraphProperty.joinToString(";") },
            property.blurbProperty.paragraphProperty
        )
        blurbParagraphBinding.addListener { _, _, _ -> blurbParagraphViewChanges++ }
        blurbParagraphView.bind(blurbParagraphBinding)

        resetCounters()
    }

    private fun resetCounters() {
        parentEvents = 0
        rootViewChanges = 0
        titleViewChanges = 0
        titleAppendixViewChanges = 0
        promptsViewChanges = 0
        prologViewChanges = 0
        prologTitleViewChanges = 0
        prologParagraphViewChanges = 0
        prologPromptsViewChanges = 0
        chaptersViewChanges = 0
        epilogViewChanges = 0
        epilogTitleViewChanges = 0
        blurbViewChanges = 0
        blurbParagraphViewChanges = 0
    }

    /** The manuscript every test starts from, built fresh so no test sees the objects of another. */
    private fun newBook(): Book = Book(
        title = "The long journey",
        titleAppendix = listOf("A novel"),
        prompts = INITIAL_PROMPTS,
        prolog = Prolog(
            title = "Before the storm",
            titleAppendix = listOf("A short note"),
            prompts = INITIAL_PROLOG_PROMPTS,
            paragraph = listOf("The night was calm.")
        ),
        chapters = listOf(
            Chapter(name = "Chapter one", title = "The arrival", paragraph = listOf("The train was late."))
        ),
        epilog = Epilog(
            title = "What remains",
            titleAppendix = listOf("A last word"),
            paragraph = listOf("The house stood empty.")
        ),
        blurb = Blurb(paragraph = listOf("A story about a long journey."))
    )

    /** Text form of a prompt pair, used as the value of a binding on prompts. */
    private fun promptText(prompts: AIPrompt?): String =
        "${prompts?.contentPrompt ?: MISSING}/${prompts?.stylePrompt ?: MISSING}"

    /** Text form of the whole book, used as the value of the binding on the root. */
    private fun bookState(book: Book?): String =
        "${book?.title ?: MISSING}|${book?.titleAppendix.orEmpty().joinToString(";")}|" +
                "${promptText(book?.prompts)}|" +
                "${partState(book?.prolog)}|${book?.chapters.orEmpty().joinToString(";") { it.name }}|" +
                "${partState(book?.epilog)}|${blurbState(book?.blurb)}"

    /** Text form of a written part of the book, used as the value of the binding on that object. */
    private fun partState(part: BookPart?): String =
        "${part?.title ?: MISSING}|${part?.titleAppendix.orEmpty().joinToString(";")}|" +
                "${promptText(part?.prompts)}|" +
                part?.paragraph.orEmpty().joinToString(";")

    /** Text form of the blurb, used as the value of the binding on that object. */
    private fun blurbState(blurb: Blurb?): String =
        "${blurb?.prompt ?: MISSING}|" + blurb?.paragraph.orEmpty().joinToString(";")

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous object or of a previous field value.
     */
    private fun assertTreeShows(
        title: String?,
        titleAppendix: List<String>,
        prolog: Prolog?,
        chapters: List<Chapter>,
        epilog: Epilog?,
        blurb: Blurb?,
        prompts: AIPrompt? = INITIAL_PROMPTS
    ) {
        val titleAppendixText = titleAppendix.joinToString(";")
        val chaptersText = chapters.joinToString(";") { it.name }

        assertEquals(
            "${title ?: MISSING}|$titleAppendixText|${promptText(prompts)}|" +
                    "${partState(prolog)}|$chaptersText|" +
                    "${partState(epilog)}|${blurbState(blurb)}",
            rootView.get()
        ) { "the binding on the book delivers an outdated state" }
        assertEquals(title ?: MISSING, titleView.get()) {
            "the binding on the main title delivers an outdated value"
        }
        assertEquals(titleAppendixText, titleAppendixView.get()) {
            "the binding on the further title lines delivers outdated lines"
        }
        assertEquals(promptText(prompts), promptsView.get()) {
            "the binding on the prompts of the book delivers outdated prompts"
        }
        assertEquals(promptText(prolog?.prompts), prologPromptsView.get()) {
            "the binding on the prompts of the prolog delivers outdated prompts"
        }
        assertEquals(partState(prolog), prologView.get()) {
            "the binding on the prolog delivers an outdated state"
        }
        assertEquals(prolog?.title ?: MISSING, prologTitleView.get()) {
            "the binding on the heading of the prolog delivers an outdated value"
        }
        assertEquals(prolog?.paragraph.orEmpty().joinToString(";"), prologParagraphView.get()) {
            "the binding on the paragraphs of the prolog delivers outdated paragraphs"
        }
        assertEquals(chaptersText, chaptersView.get()) {
            "the binding on the chapters delivers outdated chapters"
        }
        assertEquals(partState(epilog), epilogView.get()) {
            "the binding on the epilog delivers an outdated state"
        }
        assertEquals(epilog?.title ?: MISSING, epilogTitleView.get()) {
            "the binding on the heading of the epilog delivers an outdated value"
        }
        assertEquals(blurbState(blurb), blurbView.get()) {
            "the binding on the blurb delivers an outdated state"
        }
        assertEquals(blurb?.paragraph.orEmpty().joinToString(";"), blurbParagraphView.get()) {
            "the binding on the paragraphs of the blurb delivers outdated paragraphs"
        }
    }

    /** Asserts that every binding of the object tree shows the manuscript the tests start from. */
    private fun assertTreeShowsInitialBook() {
        val book = newBook()
        assertTreeShows(
            book.title,
            book.titleAppendix,
            book.prolog,
            book.chapters,
            book.epilog,
            book.blurb,
            book.prompts
        )
    }

    /**
     * Use case: the project is read from its file before the user interface is built, so every binding
     * of the object tree delivers the manuscript that already sits in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShowsInitialBook()
    }

    /**
     * Use case: the user renames the book, so the title lands in the model object and both the binding
     * on that field and the binding on the book show it.
     */
    @Test
    fun writesTitleToModelAndNotifiesTree() {
        property.title = "The way back"

        assertEquals("The way back", holder.book?.title)
        assertEquals("The way back", titleView.get())
        assertTrue(titleViewChanges > 0) { "the binding on the main title was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the title is bound to the text field of the book dialog, so every text that field
     * produces reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundTitleToModelAndNotifiesTree() {
        val source = SimpleStringProperty("Draft title")
        property.titleProperty.bind(source)

        source.set("The way back")

        assertEquals("The way back", holder.book?.title)
        assertEquals("The way back", titleView.get())
        assertTrue(titleViewChanges > 0) { "the binding on the main title was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user adds a further title line below the main title, so the content change alone
     * reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesTitleAppendixEntryAddedToModelAndNotifiesTree() {
        property.titleAppendixProperty.add("In three parts")

        assertEquals(listOf("A novel", "In three parts"), holder.book?.titleAppendix)
        assertEquals("A novel;In three parts", titleAppendixView.get())
        assertTrue(titleAppendixViewChanges > 0) { "the binding on the further title lines was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the further title lines are filled from a binding, so every list that binding produces
     * reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundTitleAppendixToModelAndNotifiesTree() {
        val source = SimpleObjectProperty(FXCollections.observableArrayList("A first note"))
        property.titleAppendixProperty.bind(source)

        source.set(FXCollections.observableArrayList("In three parts"))

        assertEquals(listOf("In three parts"), holder.book?.titleAppendix)
        assertEquals("In three parts", titleAppendixView.get())
        assertTrue(titleAppendixViewChanges > 0) { "the binding on the further title lines was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user writes a heading into the prolog, so the text lands in the nested model object
     * and the bindings on that field, on the prolog and on the book show it.
     */
    @Test
    fun writesNestedPrologTitleToModelAndNotifiesTree() {
        property.prologProperty.title = "After the storm"

        assertEquals("After the storm", holder.book?.prolog?.title)
        assertEquals("After the storm", prologTitleView.get())
        assertTrue(prologTitleViewChanges > 0) { "the binding on the heading of the prolog was not re-evaluated" }
        assertTrue(prologViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the paragraphs of the prolog are filled from a binding - the text editor hands over its
     * content - so every list that binding produces reaches the nested model object and the bindings on
     * that field, on the prolog and on the book show it.
     */
    @Test
    fun writesBoundNestedPrologParagraphToModelAndNotifiesTree() {
        val source = SimpleObjectProperty(FXCollections.observableArrayList("A first line."))
        property.prologProperty.paragraphProperty.bind(source)

        source.set(FXCollections.observableArrayList("Then the wind came."))

        assertEquals(listOf("Then the wind came."), holder.book?.prolog?.paragraph)
        assertEquals("Then the wind came.", prologParagraphView.get())
        assertTrue(prologParagraphViewChanges > 0) {
            "the binding on the paragraphs of the prolog was not re-evaluated"
        }
        assertTrue(prologViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the whole prolog is replaced, so the field properties below it belong to another object
     * afterwards and every binding of the object tree shows the values of that object instead of the
     * previous ones.
     */
    @Test
    fun writesPrologToModelAndNotifiesTree() {
        val prolog = Prolog(
            title = "After the storm",
            titleAppendix = listOf("Written in winter"),
            paragraph = listOf("Then the wind came.")
        )

        property.prolog = prolog

        assertEquals(prolog, holder.book?.prolog)
        assertEquals(partState(prolog), prologView.get())
        assertEquals("After the storm", prologTitleView.get())
        assertTrue(prologTitleViewChanges > 0) { "the binding on the heading of the prolog was not re-evaluated" }
        assertTrue(prologParagraphViewChanges > 0) {
            "the binding on the paragraphs of the prolog was not re-evaluated"
        }
        assertTrue(prologViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user deletes the prolog and creates a new one later on, so the properties below the
     * prolog first answer with neutral values and take over the values of the new object afterwards -
     * every binding of the object tree follows both steps.
     */
    @Test
    fun createsPrologAndNotifiesTree() {
        property.prolog = null

        assertNull(holder.book?.prolog)
        assertEquals(partState(null), prologView.get())
        assertEquals(MISSING, prologTitleView.get())
        assertEquals("", prologParagraphView.get())

        resetCounters()
        val prolog = Prolog(title = "After the storm", paragraph = listOf("Then the wind came."))
        property.prolog = prolog

        assertEquals(prolog, holder.book?.prolog)
        assertEquals("After the storm", prologTitleView.get())
        assertEquals("Then the wind came.", prologParagraphView.get())
        assertTrue(prologTitleViewChanges > 0) { "the binding on the heading of the prolog was not re-evaluated" }
        assertTrue(prologParagraphViewChanges > 0) {
            "the binding on the paragraphs of the prolog was not re-evaluated"
        }
        assertTrue(prologViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user describes what the whole manuscript is about, so the single prompt field
     * lands in the model object and every binding above it shows it.
     */
    @Test
    fun writesContentPromptToModelAndNotifiesTree() {
        property.promptsProperty.contentPromptProperty.set("Tell a story of a way back.")

        assertEquals("Tell a story of a way back.", holder.book?.prompts?.contentPrompt)
        assertEquals(
            promptText(AIPrompt("Tell a story of a way back.", INITIAL_PROMPTS.stylePrompt)),
            promptsView.get()
        )
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts of the book was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user describes what the prolog is about, so the prompt of that nested object is
     * written through the tree and every binding above it - up to the book - shows it.
     */
    @Test
    fun writesPrologContentPromptToModelAndNotifiesTree() {
        property.prologProperty.promptsProperty.contentPromptProperty.set("Tell what nobody saw coming.")

        assertEquals("Tell what nobody saw coming.", holder.book?.prolog?.prompts?.contentPrompt)
        assertEquals(
            promptText(AIPrompt("Tell what nobody saw coming.", INITIAL_PROLOG_PROMPTS.stylePrompt)),
            prologPromptsView.get()
        )
        assertTrue(prologPromptsViewChanges > 0) {
            "the binding on the prompts of the prolog was not re-evaluated"
        }
        assertTrue(prologViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user adds a chapter to the manuscript, so the content change alone reaches the model
     * object and every binding above it shows it.
     */
    @Test
    fun writesChapterAddedToModelAndNotifiesTree() {
        property.chaptersProperty.add(Chapter(name = "Chapter two", title = "The departure"))

        assertEquals(listOf("Chapter one", "Chapter two"), holder.book?.chapters?.map { it.name })
        assertEquals("Chapter one;Chapter two", chaptersView.get())
        assertTrue(chaptersViewChanges > 0) { "the binding on the chapters was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the chapters are filled from a binding - the project tree hands over its order - so
     * every list that binding produces reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundChaptersToModelAndNotifiesTree() {
        val source = SimpleObjectProperty(
            FXCollections.observableArrayList(Chapter(name = "Chapter one", title = "The arrival"))
        )
        property.chaptersProperty.bind(source)

        source.set(FXCollections.observableArrayList(Chapter(name = "Chapter two", title = "The departure")))

        assertEquals(listOf("Chapter two"), holder.book?.chapters?.map { it.name })
        assertEquals("Chapter two", chaptersView.get())
        assertTrue(chaptersViewChanges > 0) { "the binding on the chapters was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user writes a heading into the epilog, so the text lands in the nested model object
     * and the bindings on that field, on the epilog and on the book show it.
     */
    @Test
    fun writesNestedEpilogTitleToModelAndNotifiesTree() {
        val source = SimpleStringProperty("Draft heading")
        property.epilogProperty.titleProperty.bind(source)

        source.set("The years after")

        assertEquals("The years after", holder.book?.epilog?.title)
        assertEquals("The years after", epilogTitleView.get())
        assertTrue(epilogTitleViewChanges > 0) { "the binding on the heading of the epilog was not re-evaluated" }
        assertTrue(epilogViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the whole epilog is replaced, so the field properties below it belong to another object
     * afterwards and every binding of the object tree shows the values of that object instead of the
     * previous ones.
     */
    @Test
    fun writesEpilogToModelAndNotifiesTree() {
        val epilog = Epilog(title = "The years after", paragraph = listOf("Nobody came back."))

        property.epilog = epilog

        assertEquals(epilog, holder.book?.epilog)
        assertEquals(partState(epilog), epilogView.get())
        assertEquals("The years after", epilogTitleView.get())
        assertTrue(epilogTitleViewChanges > 0) { "the binding on the heading of the epilog was not re-evaluated" }
        assertTrue(epilogViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user writes a further paragraph into the blurb, so the content change alone reaches
     * the nested model object and the bindings on that field, on the blurb and on the book show it.
     */
    @Test
    fun writesNestedBlurbParagraphToModelAndNotifiesTree() {
        property.blurbProperty.paragraphProperty.add("For everyone who ever left home.")

        assertEquals(
            listOf("A story about a long journey.", "For everyone who ever left home."),
            holder.book?.blurb?.paragraph
        )
        assertEquals(
            "A story about a long journey.;For everyone who ever left home.",
            blurbParagraphView.get()
        )
        assertTrue(blurbParagraphViewChanges > 0) {
            "the binding on the paragraphs of the blurb was not re-evaluated"
        }
        assertTrue(blurbViewChanges > 0) { "the binding on the blurb was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the whole blurb is replaced, so the field property below it belongs to another object
     * afterwards and every binding of the object tree shows the paragraphs of that object instead of
     * the previous ones.
     */
    @Test
    fun writesBlurbToModelAndNotifiesTree() {
        val blurb = Blurb(paragraph = listOf("For everyone who ever left home."))

        property.blurb = blurb

        assertEquals(blurb, holder.book?.blurb)
        assertEquals(blurbState(blurb), blurbView.get())
        assertEquals("For everyone who ever left home.", blurbParagraphView.get())
        assertTrue(blurbParagraphViewChanges > 0) {
            "the binding on the paragraphs of the blurb was not re-evaluated"
        }
        assertTrue(blurbViewChanges > 0) { "the binding on the blurb was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: a field of the book or of an object nested in it is changed by application code past
     * the property, so every field property delivers the current value instead of a cached copy.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        holder.book?.title = "The way back"
        holder.book?.titleAppendix = listOf("In three parts")
        holder.book?.prompts = AIPrompt("Tell a story of a way back.", "Dry and short.")
        holder.book?.prolog?.title = "After the storm"
        holder.book?.chapters = listOf(Chapter(name = "Chapter two", title = "The departure"))
        holder.book?.epilog?.title = "The years after"
        holder.book?.blurb?.paragraph = listOf("For everyone who ever left home.")

        assertEquals("The way back", property.title)
        assertEquals(listOf("In three parts"), property.titleAppendix)
        assertEquals(AIPrompt("Tell a story of a way back.", "Dry and short."), property.prompts)
        assertEquals("After the storm", property.prologProperty.title)
        assertEquals(listOf("Chapter two"), property.chapters.map { it.name })
        assertEquals("The years after", property.epilogProperty.title)
        assertEquals(listOf("For everyone who ever left home."), property.blurbProperty.paragraph)
    }

    /**
     * Use case: the whole manuscript is replaced - another project file was loaded - so every property
     * of the object tree belongs to another object afterwards and every binding shows the values of
     * that object instead of the previous ones.
     */
    @Test
    fun writesReplacedBookToModelAndNotifiesWholeTree() {
        val prolog = Prolog(title = "After the storm", paragraph = listOf("Then the wind came."))
        val epilog = Epilog(title = "The years after", paragraph = listOf("Nobody came back."))
        val blurb = Blurb(paragraph = listOf("For everyone who ever left home."))
        val chapters = listOf(Chapter(name = "Chapter two", title = "The departure"))

        property.value = Book(
            title = "The way back",
            titleAppendix = listOf("In three parts"),
            prolog = prolog,
            chapters = chapters,
            epilog = epilog,
            blurb = blurb
        )

        assertTreeShows(
            "The way back",
            listOf("In three parts"),
            prolog,
            chapters,
            epilog,
            blurb,
            AIPrompt()
        )
        assertTrue(titleViewChanges > 0) { "the binding on the main title was not re-evaluated" }
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts of the book was not re-evaluated" }
        assertTrue(prologPromptsViewChanges > 0) {
            "the binding on the prompts of the prolog was not re-evaluated"
        }
        assertTrue(titleAppendixViewChanges > 0) { "the binding on the further title lines was not re-evaluated" }
        assertTrue(prologTitleViewChanges > 0) { "the binding on the heading of the prolog was not re-evaluated" }
        assertTrue(prologParagraphViewChanges > 0) {
            "the binding on the paragraphs of the prolog was not re-evaluated"
        }
        assertTrue(prologViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(chaptersViewChanges > 0) { "the binding on the chapters was not re-evaluated" }
        assertTrue(epilogTitleViewChanges > 0) { "the binding on the heading of the epilog was not re-evaluated" }
        assertTrue(epilogViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(blurbParagraphViewChanges > 0) {
            "the binding on the paragraphs of the blurb was not re-evaluated"
        }
        assertTrue(blurbViewChanges > 0) { "the binding on the blurb was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the manuscript is exchanged for an object carrying the same values, so nothing the user
     * interface shows changes and no field property reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedBookCarriesTheSameValues() {
        property.value = newBook()

        assertTreeShowsInitialBook()
        assertEquals(0, titleViewChanges) { "the main title was reported as changed although it did not change" }
        assertEquals(0, titleAppendixViewChanges) {
            "the further title lines were reported as changed although they did not change"
        }
        assertEquals(0, promptsViewChanges) {
            "the prompts of the book were reported as changed although they did not change"
        }
        assertEquals(0, prologPromptsViewChanges) {
            "the prompts of the prolog were reported as changed although they did not change"
        }
        assertEquals(0, prologTitleViewChanges) {
            "the heading of the prolog was reported as changed although it did not change"
        }
        assertEquals(0, prologParagraphViewChanges) {
            "the paragraphs of the prolog were reported as changed although they did not change"
        }
        assertEquals(0, prologViewChanges) { "the prolog was reported as changed although it did not change" }
        assertEquals(0, chaptersViewChanges) { "the chapters were reported as changed although they did not change" }
        assertEquals(0, epilogTitleViewChanges) {
            "the heading of the epilog was reported as changed although it did not change"
        }
        assertEquals(0, epilogViewChanges) { "the epilog was reported as changed although it did not change" }
        assertEquals(0, blurbParagraphViewChanges) {
            "the paragraphs of the blurb were reported as changed although they did not change"
        }
        assertEquals(0, blurbViewChanges) { "the blurb was reported as changed although it did not change" }
    }

    /**
     * Use case: no project is open at all, so the property carries no manuscript and every field
     * property - down to the fields of the objects nested in it - answers with a neutral value, which
     * lets the whole user interface be built before a project is loaded.
     */
    @Test
    fun readsNeutralValuesWhenBookIsAbsent() {
        property.value = null

        assertNull(property.title)
        assertEquals(emptyList<String>(), property.titleAppendix)
        assertNull(property.prompts)
        assertNull(property.prolog)
        assertNull(property.prologProperty.title)
        assertEquals(emptyList<Chapter>(), property.chapters)
        assertNull(property.epilog)
        assertNull(property.epilogProperty.title)
        assertNull(property.blurb)
        assertEquals(emptyList<String>(), property.blurbProperty.paragraph)
        assertTreeShows(null, emptyList(), null, emptyList(), null, null, null)
    }

    /**
     * Use case: the user interface writes into the property while no manuscript sits behind it, so the
     * values are dropped instead of creating a book nobody asked for.
     */
    @Test
    fun dropsWritesWhenBookIsAbsent() {
        property.value = null

        property.title = "The way back"
        property.titleAppendix = listOf("In three parts")
        property.prompts = AIPrompt("Tell a story of a way back.", "Dry and short.")
        property.prolog = Prolog(title = "After the storm")
        property.chapters = listOf(Chapter(name = "Chapter two", title = "The departure"))
        property.epilog = Epilog(title = "The years after")
        property.blurb = Blurb(paragraph = listOf("For everyone who ever left home."))

        assertNull(holder.book)
    }

    private companion object {
        /** Stands for a value the model object does not carry at all. */
        const val MISSING = "-"

        /** The prompts of the manuscript every test starts from. */
        val INITIAL_PROMPTS: AIPrompt
            get() = AIPrompt("Tell a story in two parts.", "Warm and calm.")

        /** The prompts of the prolog every test starts from, different from those of the book. */
        val INITIAL_PROLOG_PROMPTS: AIPrompt
            get() = AIPrompt("Tell what happened before the story.", "Quiet and slow.")
    }
}
