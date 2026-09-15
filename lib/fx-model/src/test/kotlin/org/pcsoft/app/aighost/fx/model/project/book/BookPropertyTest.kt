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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.project.book.Blurb
import org.pcsoft.app.aighost.model.project.book.Book
import org.pcsoft.app.aighost.model.project.book.Chapter
import org.pcsoft.app.aighost.model.project.book.Copyright
import org.pcsoft.app.aighost.model.project.book.Epilog
import org.pcsoft.app.aighost.model.project.book.Prolog
import org.pcsoft.app.aighost.model.project.common.AIPrompt
import org.pcsoft.framework.simplay.engine.model.Document
import org.pcsoft.framework.simplay.engine.model.PageNumbering

/**
 * Developer tests for [BookProperty].
 *
 * The property wraps the manuscript of a project and offers every field of that object - and every
 * field of the objects nested in it - as a property of its own. Every test checks the object tree the
 * way the user interface uses it: a binding hangs on each property of the tree - the book itself, the
 * copyright page, the prolog, the epilog and the blurb below it and every single field - and the tests
 * assert that a change reaches every binding that has to know about it, upwards to the parent the
 * property reports to as well as downwards into the fields of an exchanged object.
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

    /** Binding on the prompts of the whole manuscript. */
    private lateinit var promptsView: StringProperty
    private var promptsViewChanges = 0

    /** Binding on the copyright page, standing for a view bound to that nested object. */
    private lateinit var copyrightView: StringProperty
    private var copyrightViewChanges = 0

    /** Binding on the switch nested in the copyright page. */
    private lateinit var copyrightIncludedView: StringProperty
    private var copyrightIncludedViewChanges = 0

    /** Binding on the prolog, standing for a view bound to that nested object. */
    private lateinit var prologView: StringProperty
    private var prologViewChanges = 0

    /** Binding on the prompts nested in the prolog. */
    private lateinit var prologPromptsView: StringProperty
    private var prologPromptsViewChanges = 0

    /** Binding on the switch nested in the prolog. */
    private lateinit var prologIncludedView: StringProperty
    private var prologIncludedViewChanges = 0

    /** Binding on the chapters of the book. */
    private lateinit var chaptersView: StringProperty
    private var chaptersViewChanges = 0

    /** Binding on the epilog, standing for a view bound to that nested object. */
    private lateinit var epilogView: StringProperty
    private var epilogViewChanges = 0

    /** Binding on the switch nested in the epilog. */
    private lateinit var epilogIncludedView: StringProperty
    private var epilogIncludedViewChanges = 0

    /** Binding on the blurb, standing for a view bound to that nested object. */
    private lateinit var blurbView: StringProperty
    private var blurbViewChanges = 0

    /** Binding on the paragraphs nested in the blurb. */
    private lateinit var blurbParagraphView: StringProperty
    private var blurbParagraphViewChanges = 0

    /** Binding on the switch nested in the blurb. */
    private lateinit var blurbIncludedView: StringProperty
    private var blurbIncludedViewChanges = 0

    /** Binding on the manuscript's document, a plain reference field with no nested model of its own. */
    private lateinit var documentView: StringProperty
    private var documentViewChanges = 0

    @BeforeEach
    fun setUp() {
        holder = Holder(newBook())
        parentEvents = 0
        property = BookProperty()
        // A parent property reports a change of a nested one as its own and writes an exchanged object
        // back into the one carrying it, which is what these two listeners stand for.
        property.addListener { _ -> parentEvents++ }
        property.addListener { _, _, newValue -> holder.book = newValue }
        // A parent property hands the nested object to this property as soon as that object arrives.
        property.set(holder.book)

        rootView = SimpleStringProperty()
        val rootBinding = Bindings.createStringBinding({ bookState(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        rootBinding.addListener { _, _, _ -> rootViewChanges++ }
        rootView.bind(rootBinding)

        promptsView = SimpleStringProperty()
        val promptsBinding = Bindings.createStringBinding(
            { promptText(property.promptsProperty.get()) },
            property.promptsProperty
        )
        promptsBinding.addListener { _, _, _ -> promptsViewChanges++ }
        promptsView.bind(promptsBinding)

        copyrightView = SimpleStringProperty()
        val copyrightBinding = Bindings.createStringBinding(
            { copyrightState(property.copyrightProperty.value) },
            property.copyrightProperty
        )
        copyrightBinding.addListener { _, _, _ -> copyrightViewChanges++ }
        copyrightView.bind(copyrightBinding)

        copyrightIncludedView = SimpleStringProperty()
        val copyrightIncludedBinding = Bindings.createStringBinding(
            { property.copyrightProperty.includedProperty.get().toString() },
            property.copyrightProperty.includedProperty
        )
        copyrightIncludedBinding.addListener { _, _, _ -> copyrightIncludedViewChanges++ }
        copyrightIncludedView.bind(copyrightIncludedBinding)

        prologView = SimpleStringProperty()
        val prologBinding = Bindings.createStringBinding(
            { prologState(property.prologProperty.value) },
            property.prologProperty
        )
        prologBinding.addListener { _, _, _ -> prologViewChanges++ }
        prologView.bind(prologBinding)

        prologPromptsView = SimpleStringProperty()
        val prologPromptsBinding = Bindings.createStringBinding(
            { promptText(property.prologProperty.promptsProperty.get()) },
            property.prologProperty.promptsProperty
        )
        prologPromptsBinding.addListener { _, _, _ -> prologPromptsViewChanges++ }
        prologPromptsView.bind(prologPromptsBinding)

        prologIncludedView = SimpleStringProperty()
        val prologIncludedBinding = Bindings.createStringBinding(
            { property.prologProperty.includedProperty.get().toString() },
            property.prologProperty.includedProperty
        )
        prologIncludedBinding.addListener { _, _, _ -> prologIncludedViewChanges++ }
        prologIncludedView.bind(prologIncludedBinding)

        chaptersView = SimpleStringProperty()
        val chaptersBinding = Bindings.createStringBinding(
            { property.chaptersProperty.joinToString(";") { it.name } },
            property.chaptersProperty
        )
        chaptersBinding.addListener { _, _, _ -> chaptersViewChanges++ }
        chaptersView.bind(chaptersBinding)

        epilogView = SimpleStringProperty()
        val epilogBinding = Bindings.createStringBinding(
            { epilogState(property.epilogProperty.value) },
            property.epilogProperty
        )
        epilogBinding.addListener { _, _, _ -> epilogViewChanges++ }
        epilogView.bind(epilogBinding)

        epilogIncludedView = SimpleStringProperty()
        val epilogIncludedBinding = Bindings.createStringBinding(
            { property.epilogProperty.includedProperty.get().toString() },
            property.epilogProperty.includedProperty
        )
        epilogIncludedBinding.addListener { _, _, _ -> epilogIncludedViewChanges++ }
        epilogIncludedView.bind(epilogIncludedBinding)

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

        blurbIncludedView = SimpleStringProperty()
        val blurbIncludedBinding = Bindings.createStringBinding(
            { property.blurbProperty.includedProperty.get().toString() },
            property.blurbProperty.includedProperty
        )
        blurbIncludedBinding.addListener { _, _, _ -> blurbIncludedViewChanges++ }
        blurbIncludedView.bind(blurbIncludedBinding)

        documentView = SimpleStringProperty()
        val documentBinding = Bindings.createStringBinding(
            { documentState(property.documentProperty.get()) },
            property.documentProperty
        )
        documentBinding.addListener { _, _, _ -> documentViewChanges++ }
        documentView.bind(documentBinding)

        resetCounters()
    }

    private fun resetCounters() {
        parentEvents = 0
        rootViewChanges = 0
        promptsViewChanges = 0
        copyrightViewChanges = 0
        copyrightIncludedViewChanges = 0
        prologViewChanges = 0
        prologPromptsViewChanges = 0
        prologIncludedViewChanges = 0
        chaptersViewChanges = 0
        epilogViewChanges = 0
        epilogIncludedViewChanges = 0
        blurbViewChanges = 0
        blurbParagraphViewChanges = 0
        blurbIncludedViewChanges = 0
        documentViewChanges = 0
    }

    /** The manuscript every test starts from, built fresh so no test sees the objects of another. */
    private fun newBook(): Book = Book(
        prompts = INITIAL_PROMPTS,
        copyright = Copyright(included = true),
        prolog = Prolog(prompts = INITIAL_PROLOG_PROMPTS, included = true),
        chapters = listOf(Chapter(name = "Chapter one")),
        epilog = Epilog(),
        blurb = Blurb(paragraph = listOf("A story about a long journey."), included = true)
    ).apply { document = INITIAL_DOCUMENT }

    /** The book of the current test, which every test works on through the property. */
    private fun book(): Book = holder.book!!

    /** Text form of a prompt pair, used as the value of a binding on prompts. */
    private fun promptText(prompts: AIPrompt?): String =
        "${prompts?.contentPrompt ?: MISSING}/${prompts?.stylePrompt ?: MISSING}"

    /** Text form of the whole book, used as the value of the binding on the root. */
    private fun bookState(book: Book?): String =
        "${promptText(book?.prompts)}|${copyrightState(book?.copyright)}|" +
                "${prologState(book?.prolog)}|${book?.chapters.orEmpty().joinToString(";") { it.name }}|" +
                "${epilogState(book?.epilog)}|${blurbState(book?.blurb)}|${documentState(book?.document)}"

    /** Text form of the manuscript's document, used as the value of the binding on that field. */
    private fun documentState(document: Document?): String =
        document?.numbering?.startNumber?.toString() ?: MISSING

    /** Text form of the copyright page, used as the value of the binding on that object. */
    private fun copyrightState(copyright: Copyright?): String = "${copyright?.included ?: false}"

    /** Text form of the prolog, used as the value of the binding on that object. */
    private fun prologState(prolog: Prolog?): String =
        "${promptText(prolog?.prompts)}|${prolog?.included ?: false}"

    /** Text form of the epilog, used as the value of the binding on that object. */
    private fun epilogState(epilog: Epilog?): String =
        "${promptText(epilog?.prompts)}|${epilog?.included ?: false}"

    /** Text form of the blurb, used as the value of the binding on that object. */
    private fun blurbState(blurb: Blurb?): String =
        "${blurb?.prompt ?: MISSING}|${blurb?.paragraph.orEmpty().joinToString(";")}|" +
                "${blurb?.included ?: false}"

    /**
     * Asserts that every binding of the object tree delivers the given state, so no view keeps the
     * value of a previous object or of a previous field value.
     */
    private fun assertTreeShows(
        copyright: Copyright?,
        prolog: Prolog?,
        chapters: List<Chapter>,
        epilog: Epilog?,
        blurb: Blurb?,
        prompts: AIPrompt? = INITIAL_PROMPTS,
        document: Document? = INITIAL_DOCUMENT
    ) {
        val chaptersText = chapters.joinToString(";") { it.name }

        assertEquals(
            "${promptText(prompts)}|${copyrightState(copyright)}|" +
                    "${prologState(prolog)}|$chaptersText|" +
                    "${epilogState(epilog)}|${blurbState(blurb)}|${documentState(document)}",
            rootView.get()
        ) { "the binding on the book delivers an outdated state" }
        assertEquals(documentState(document), documentView.get()) {
            "the binding on the document delivers an outdated state"
        }
        assertEquals(promptText(prompts), promptsView.get()) {
            "the binding on the prompts of the book delivers outdated prompts"
        }
        assertEquals(copyrightState(copyright), copyrightView.get()) {
            "the binding on the copyright page delivers an outdated state"
        }
        assertEquals((copyright?.included ?: false).toString(), copyrightIncludedView.get()) {
            "the binding on the switch of the copyright page delivers an outdated value"
        }
        assertEquals(promptText(prolog?.prompts), prologPromptsView.get()) {
            "the binding on the prompts of the prolog delivers outdated prompts"
        }
        assertEquals(prologState(prolog), prologView.get()) {
            "the binding on the prolog delivers an outdated state"
        }
        assertEquals((prolog?.included ?: false).toString(), prologIncludedView.get()) {
            "the binding on the switch of the prolog delivers an outdated value"
        }
        assertEquals(chaptersText, chaptersView.get()) {
            "the binding on the chapters delivers outdated chapters"
        }
        assertEquals(epilogState(epilog), epilogView.get()) {
            "the binding on the epilog delivers an outdated state"
        }
        assertEquals((epilog?.included ?: false).toString(), epilogIncludedView.get()) {
            "the binding on the switch of the epilog delivers an outdated value"
        }
        assertEquals(blurbState(blurb), blurbView.get()) {
            "the binding on the blurb delivers an outdated state"
        }
        assertEquals(blurb?.paragraph.orEmpty().joinToString(";"), blurbParagraphView.get()) {
            "the binding on the paragraphs of the blurb delivers outdated paragraphs"
        }
        assertEquals((blurb?.included ?: false).toString(), blurbIncludedView.get()) {
            "the binding on the switch of the blurb delivers an outdated value"
        }
    }

    /** Asserts that every binding of the object tree shows the manuscript the tests start from. */
    private fun assertTreeShowsInitialBook() {
        val book = newBook()
        assertTreeShows(book.copyright, book.prolog, book.chapters, book.epilog, book.blurb, book.prompts, book.document)
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
     * Use case: the user types another copyright switch, so it lands in the nested model object and
     * the bindings on that field, on the copyright page and on the book show it.
     */
    @Test
    fun writesNestedCopyrightIncludedToModelAndNotifiesTree() {
        property.copyrightProperty.included = false

        assertFalse(book().copyright.included)
        assertEquals("false", copyrightIncludedView.get())
        assertTrue(copyrightIncludedViewChanges > 0) {
            "the binding on the switch of the copyright page was not re-evaluated"
        }
        assertTrue(copyrightViewChanges > 0) { "the binding on the copyright page was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the whole copyright page is replaced, so the field property below it belongs to
     * another object afterwards and every binding of the object tree shows the values of that object
     * instead of the previous ones.
     */
    @Test
    fun writesCopyrightToModelAndNotifiesTree() {
        val copyright = Copyright(included = false)

        property.copyright = copyright

        assertEquals(copyright, book().copyright)
        assertEquals(copyrightState(copyright), copyrightView.get())
        assertEquals("false", copyrightIncludedView.get())
        assertTrue(copyrightIncludedViewChanges > 0) {
            "the binding on the switch of the copyright page was not re-evaluated"
        }
        assertTrue(copyrightViewChanges > 0) { "the binding on the copyright page was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user takes the prolog out of the book, so the switch lands in the nested model
     * object and the bindings on that field, on the prolog and on the book show it.
     */
    @Test
    fun writesNestedPrologIncludedToModelAndNotifiesTree() {
        property.prologProperty.included = false

        assertFalse(book().prolog.included)
        assertEquals("false", prologIncludedView.get())
        assertTrue(prologIncludedViewChanges > 0) { "the binding on the switch of the prolog was not re-evaluated" }
        assertTrue(prologViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user puts the epilog into the book, so the switch lands in the nested model object
     * and the bindings on that field, on the epilog and on the book show it.
     */
    @Test
    fun writesNestedEpilogIncludedToModelAndNotifiesTree() {
        property.epilogProperty.included = true

        assertTrue(book().epilog.included)
        assertEquals("true", epilogIncludedView.get())
        assertTrue(epilogIncludedViewChanges > 0) { "the binding on the switch of the epilog was not re-evaluated" }
        assertTrue(epilogViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user takes the blurb off the cover, so the switch lands in the nested model object
     * and the bindings on that field, on the blurb and on the book show it.
     */
    @Test
    fun writesNestedBlurbIncludedToModelAndNotifiesTree() {
        property.blurbProperty.included = false

        assertFalse(book().blurb.included)
        assertEquals("false", blurbIncludedView.get())
        assertTrue(blurbIncludedViewChanges > 0) { "the binding on the switch of the blurb was not re-evaluated" }
        assertTrue(blurbViewChanges > 0) { "the binding on the blurb was not re-evaluated" }
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
        val prolog = Prolog(prompts = AIPrompt("Tell what happened.", "Slow."))

        property.prolog = prolog

        assertEquals(prolog, book().prolog)
        assertEquals(prologState(prolog), prologView.get())
        assertEquals("false", prologIncludedView.get())
        assertTrue(prologPromptsViewChanges > 0) {
            "the binding on the prompts of the prolog was not re-evaluated"
        }
        assertTrue(prologIncludedViewChanges > 0) { "the binding on the switch of the prolog was not re-evaluated" }
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

        assertEquals("Tell a story of a way back.", book().prompts.contentPrompt)
        assertEquals(
            promptText(AIPrompt("Tell a story of a way back.", INITIAL_PROMPTS.stylePrompt)),
            promptsView.get()
        )
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts of the book was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: a user interface reaches the two prompts of the manuscript through the property model
     * the book hands out for them, so writing through that model lands in the model object just the
     * same and is answered by it afterwards.
     */
    @Test
    fun writesBothPromptsThroughTheNestedPropertyToModelAndNotifiesTree() {
        property.promptsProperty.contentPromptProperty.set("Tell a story of a way back.")
        property.promptsProperty.stylePromptProperty.set("Write it plainly.")

        assertEquals("Tell a story of a way back.", book().prompts.contentPrompt)
        assertEquals("Write it plainly.", book().prompts.stylePrompt)
        assertEquals("Tell a story of a way back.", property.promptsProperty.contentPrompt)
        assertEquals("Write it plainly.", property.promptsProperty.stylePrompt)
        assertEquals(
            promptText(AIPrompt("Tell a story of a way back.", "Write it plainly.")),
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

        assertEquals("Tell what nobody saw coming.", book().prolog.prompts.contentPrompt)
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
        property.chaptersProperty.add(Chapter(name = "Chapter two"))

        assertEquals(listOf("Chapter one", "Chapter two"), book().chapters.map { it.name })
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
            FXCollections.observableArrayList(Chapter(name = "Chapter one"))
        )
        property.chaptersProperty.bind(source)

        source.set(FXCollections.observableArrayList(Chapter(name = "Chapter two")))

        assertEquals(listOf("Chapter two"), book().chapters.map { it.name })
        assertEquals("Chapter two", chaptersView.get())
        assertTrue(chaptersViewChanges > 0) { "the binding on the chapters was not re-evaluated" }
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
        val epilog = Epilog(included = true)

        property.epilog = epilog

        assertEquals(epilog, book().epilog)
        assertEquals(epilogState(epilog), epilogView.get())
        assertEquals("true", epilogIncludedView.get())
        assertTrue(epilogIncludedViewChanges > 0) { "the binding on the switch of the epilog was not re-evaluated" }
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
            book().blurb.paragraph
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
     * Use case: the whole blurb is replaced, so the field properties below it belong to another object
     * afterwards and every binding of the object tree shows the values of that object instead of the
     * previous ones.
     */
    @Test
    fun writesBlurbToModelAndNotifiesTree() {
        val blurb = Blurb(paragraph = listOf("For everyone who ever left home."))

        property.blurb = blurb

        assertEquals(blurb, book().blurb)
        assertEquals(blurbState(blurb), blurbView.get())
        assertEquals("For everyone who ever left home.", blurbParagraphView.get())
        assertEquals("false", blurbIncludedView.get())
        assertTrue(blurbParagraphViewChanges > 0) {
            "the binding on the paragraphs of the blurb was not re-evaluated"
        }
        assertTrue(blurbIncludedViewChanges > 0) { "the binding on the switch of the blurb was not re-evaluated" }
        assertTrue(blurbViewChanges > 0) { "the binding on the blurb was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: a field of the book or of an object nested in it is changed by application code past
     * the property, so the property is told to read the book again and every field property delivers
     * the current value afterwards - down into copyright page, prolog, epilog and blurb, which nothing
     * else would reach because those objects were not exchanged.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        val book = book()
        book.prompts = AIPrompt("Tell a story of a way back.", "Dry and short.")
        book.copyright.included = false
        book.prolog.included = false
        book.chapters = listOf(Chapter(name = "Chapter two"))
        book.epilog.included = true
        book.blurb.paragraph = listOf("For everyone who ever left home.")
        book.blurb.included = false

        property.refresh()

        assertEquals(AIPrompt("Tell a story of a way back.", "Dry and short."), property.prompts)
        assertFalse(property.copyrightProperty.included)
        assertFalse(property.prologProperty.included)
        assertEquals(listOf("Chapter two"), property.chapters.map { it.name })
        assertTrue(property.epilogProperty.included)
        assertEquals(listOf("For everyone who ever left home."), property.blurbProperty.paragraph)
        assertFalse(property.blurbProperty.included)
    }

    /**
     * Use case: the whole manuscript is replaced - another project file was loaded - so every property
     * of the object tree belongs to another object afterwards and every binding shows the values of
     * that object instead of the previous ones.
     */
    @Test
    fun writesReplacedBookToModelAndNotifiesWholeTree() {
        val copyright = Copyright(included = false)
        val prolog = Prolog(prompts = AIPrompt("Tell what happened.", "Slow."))
        val epilog = Epilog(included = true)
        val blurb = Blurb(paragraph = listOf("For everyone who ever left home."))
        val chapters = listOf(Chapter(name = "Chapter two"))

        property.value = Book(
            copyright = copyright,
            prolog = prolog,
            chapters = chapters,
            epilog = epilog,
            blurb = blurb
        )

        assertTreeShows(copyright, prolog, chapters, epilog, blurb, AIPrompt(), Document())
        assertTrue(promptsViewChanges > 0) { "the binding on the prompts of the book was not re-evaluated" }
        assertTrue(copyrightIncludedViewChanges > 0) {
            "the binding on the switch of the copyright page was not re-evaluated"
        }
        assertTrue(copyrightViewChanges > 0) { "the binding on the copyright page was not re-evaluated" }
        assertTrue(prologPromptsViewChanges > 0) {
            "the binding on the prompts of the prolog was not re-evaluated"
        }
        assertTrue(prologIncludedViewChanges > 0) { "the binding on the switch of the prolog was not re-evaluated" }
        assertTrue(prologViewChanges > 0) { "the binding on the prolog was not re-evaluated" }
        assertTrue(chaptersViewChanges > 0) { "the binding on the chapters was not re-evaluated" }
        assertTrue(epilogIncludedViewChanges > 0) { "the binding on the switch of the epilog was not re-evaluated" }
        assertTrue(epilogViewChanges > 0) { "the binding on the epilog was not re-evaluated" }
        assertTrue(blurbParagraphViewChanges > 0) {
            "the binding on the paragraphs of the blurb was not re-evaluated"
        }
        assertTrue(blurbIncludedViewChanges > 0) { "the binding on the switch of the blurb was not re-evaluated" }
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
        assertEquals(0, promptsViewChanges) {
            "the prompts of the book were reported as changed although they did not change"
        }
        assertEquals(0, copyrightIncludedViewChanges) {
            "the switch of the copyright page was reported as changed although it did not change"
        }
        assertEquals(0, copyrightViewChanges) {
            "the copyright page was reported as changed although it did not change"
        }
        assertEquals(0, prologPromptsViewChanges) {
            "the prompts of the prolog were reported as changed although they did not change"
        }
        assertEquals(0, prologIncludedViewChanges) {
            "the switch of the prolog was reported as changed although it did not change"
        }
        assertEquals(0, prologViewChanges) { "the prolog was reported as changed although it did not change" }
        assertEquals(0, chaptersViewChanges) { "the chapters were reported as changed although they did not change" }
        assertEquals(0, epilogIncludedViewChanges) {
            "the switch of the epilog was reported as changed although it did not change"
        }
        assertEquals(0, epilogViewChanges) { "the epilog was reported as changed although it did not change" }
        assertEquals(0, blurbParagraphViewChanges) {
            "the paragraphs of the blurb were reported as changed although they did not change"
        }
        assertEquals(0, blurbIncludedViewChanges) {
            "the switch of the blurb was reported as changed although it did not change"
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

        assertNull(property.prompts)
        assertNull(property.copyright)
        assertFalse(property.copyrightProperty.included)
        assertNull(property.prolog)
        assertFalse(property.prologProperty.included)
        assertEquals(emptyList<Chapter>(), property.chapters)
        assertNull(property.epilog)
        assertFalse(property.epilogProperty.included)
        assertNull(property.blurb)
        assertEquals(emptyList<String>(), property.blurbProperty.paragraph)
        assertFalse(property.blurbProperty.included)
        assertTreeShows(null, null, emptyList(), null, null, null, null)
    }

    /**
     * Use case: the user interface writes into the property while no manuscript sits behind it, so the
     * values are dropped instead of creating a book nobody asked for.
     */
    @Test
    fun dropsWritesWhenBookIsAbsent() {
        property.value = null

        property.prompts = AIPrompt("Tell a story of a way back.", "Dry and short.")
        property.copyright = Copyright(included = true)
        property.copyrightProperty.included = true
        property.prolog = Prolog(included = true)
        property.prologProperty.included = true
        property.chapters = listOf(Chapter(name = "Chapter two"))
        property.epilog = Epilog(included = true)
        property.blurb = Blurb(paragraph = listOf("For everyone who ever left home."))

        assertNull(holder.book)
    }

    /**
     * Use case: the project is read from its file before the user interface is built, so the binding on
     * the manuscript's document delivers the document that already sits in the model object.
     */
    @Test
    fun readsInitialDocumentValue() {
        assertEquals(INITIAL_DOCUMENT.numbering.startNumber.toString(), documentView.get())
    }

    /**
     * Use case: the writing surface replaces the manuscript's document after an edit, so the new
     * document lands in the model object and the binding on it, and on the book, show it.
     */
    @Test
    fun writesDocumentToModelAndNotifiesTree() {
        val document = Document(numbering = PageNumbering.OFF.copy(startNumber = 7))

        property.document = document

        assertEquals(document, book().document)
        assertEquals("7", documentView.get())
        assertTrue(documentViewChanges > 0) { "the binding on the document was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the document is filled from a binding, so a value that binding produces reaches the
     * model object and the binding on it, and on the book, show it.
     */
    @Test
    fun writesBoundDocumentToModelAndNotifiesTree() {
        val document = Document(numbering = PageNumbering.OFF.copy(startNumber = 9))
        val source = SimpleObjectProperty(document)
        property.documentProperty.bind(source)

        assertEquals(document, book().document)
        assertEquals("9", documentView.get())
        assertTrue(documentViewChanges > 0) { "the binding on the document was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the document is changed by application code past the property - for instance a
     * migration writing straight onto the model - so [BookProperty.refresh] picks it up and the binding
     * on it delivers the current value afterwards.
     */
    @Test
    fun readsDocumentChangedOnModelAfterRefresh() {
        val document = Document(numbering = PageNumbering.OFF.copy(startNumber = 11))
        book().document = document

        property.refresh()

        assertEquals(document, property.document)
    }

    /**
     * Use case: the whole manuscript is replaced - another project file was loaded - so the document
     * property belongs to another object afterwards and the binding on it shows the document of that
     * object instead of the previous one.
     */
    @Test
    fun writesReplacedBookUpdatesDocumentAndNotifiesTree() {
        val document = Document(numbering = PageNumbering.OFF.copy(startNumber = 13))

        property.value = Book().apply { this.document = document }

        assertEquals("13", documentView.get())
        assertTrue(documentViewChanges > 0) { "the binding on the document was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the book was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the manuscript is exchanged for an object carrying the same document, so nothing the
     * user interface shows changes and the document field does not report a change of its own.
     */
    @Test
    fun keepsDocumentQuietWhenReplacedBookCarriesTheSameDocument() {
        property.value = newBook()

        assertEquals(0, documentViewChanges) {
            "the document was reported as changed although it did not change"
        }
    }

    /**
     * Use case: no project is open at all, so the document property carries no value either, the same
     * neutral state every other field falls back to.
     */
    @Test
    fun readsNeutralDocumentWhenBookIsAbsent() {
        property.value = null

        assertNull(property.document)
        assertEquals(MISSING, documentView.get())
    }

    /**
     * Use case: the user interface writes a document into the property while no manuscript sits behind
     * it, so the value is dropped instead of creating a book nobody asked for.
     */
    @Test
    fun dropsDocumentWriteWhenBookIsAbsent() {
        property.value = null

        property.document = Document(numbering = PageNumbering.OFF.copy(startNumber = 5))

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

        /** The manuscript's document every test starts from. */
        val INITIAL_DOCUMENT: Document
            get() = Document(numbering = PageNumbering.OFF.copy(startNumber = 3))
    }
}
