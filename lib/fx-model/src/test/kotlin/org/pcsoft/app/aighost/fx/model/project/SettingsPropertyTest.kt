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
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.fx.model.common.StyleDataProperty
import org.pcsoft.app.aighost.model.common.Alignment
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project

/**
 * Developer tests for [SettingsProperty].
 *
 * The property wraps the typographic and page settings of a project and offers every field of that
 * object - and every field of the styles nested in it - as a property of its own. Every test checks the
 * object tree the way the user interface uses it: a binding hangs on each property of the tree - the
 * settings themselves, every single style below them, the font of that style and every single field -
 * and the tests assert that a change reaches every binding that has to know about it, upwards to the
 * parent the property reports to as well as downwards into the fields of an exchanged object.
 *
 * The settings carry seven styles that are wrapped exactly the same way, so the tests walk through all
 * of them instead of picking one.
 */
class SettingsPropertyTest {

    /** Stands for the project carrying the settings, the object a parent property writes into. */
    private class Holder(var settings: Project.Settings?)

    private lateinit var holder: Holder
    private lateinit var property: SettingsProperty

    /** Counts what the parent property is told, so the report up to the root becomes visible. */
    private var parentEvents = 0

    /** Binding on the whole settings, standing for a view bound to the root of this object tree. */
    private lateinit var rootView: StringProperty
    private var rootViewChanges = 0

    /** Binding on the flag of the separate copyright page. */
    private lateinit var copyrightPageView: StringProperty
    private var copyrightPageViewChanges = 0

    /** Binding on the flag of the leading empty page. */
    private lateinit var startWithEmptyPageView: StringProperty
    private var startWithEmptyPageViewChanges = 0

    /** Binding on the flag of the trailing empty page. */
    private lateinit var endWithEmptyPageView: StringProperty
    private var endWithEmptyPageViewChanges = 0

    /** All styles the settings carry, each with the bindings a view would hang on it. */
    private lateinit var styles: List<StyleUnderTest>

    /**
     * One of the styles of the settings together with the bindings of a view: on the style itself, on
     * the font nested in it, on the family name of that font and on the placement of the text.
     */
    private inner class StyleUnderTest(
        val label: String,
        val property: StyleDataProperty,
        val read: (Project.Settings) -> StyleData
    ) {
        var styleViewChanges = 0
        var fontViewChanges = 0
        var nameViewChanges = 0
        var alignmentViewChanges = 0

        val styleView: StringProperty = SimpleStringProperty()
        val fontView: StringProperty = SimpleStringProperty()
        val nameView: StringProperty = SimpleStringProperty()
        val alignmentView: StringProperty = SimpleStringProperty()

        init {
            val styleBinding = Bindings.createStringBinding({ styleState(property.value) }, property)
            styleBinding.addListener { _, _, _ -> styleViewChanges++ }
            styleView.bind(styleBinding)

            val fontBinding = Bindings.createStringBinding(
                { fontState(property.fontProperty.value) },
                property.fontProperty
            )
            fontBinding.addListener { _, _, _ -> fontViewChanges++ }
            fontView.bind(fontBinding)

            val nameBinding = Bindings.createStringBinding(
                { property.fontProperty.nameProperty.get() ?: MISSING },
                property.fontProperty.nameProperty
            )
            nameBinding.addListener { _, _, _ -> nameViewChanges++ }
            nameView.bind(nameBinding)

            val alignmentBinding = Bindings.createStringBinding(
                { property.alignmentProperty.get()?.toString() ?: MISSING },
                property.alignmentProperty
            )
            alignmentBinding.addListener { _, _, _ -> alignmentViewChanges++ }
            alignmentView.bind(alignmentBinding)
        }

        /** The style as it sits in the model object right now, absent while no settings are there. */
        fun current(): StyleData? = holder.settings?.let(read)

        fun resetCounters() {
            styleViewChanges = 0
            fontViewChanges = 0
            nameViewChanges = 0
            alignmentViewChanges = 0
        }
    }

    @BeforeEach
    fun setUp() {
        holder = Holder(newSettings())
        parentEvents = 0
        property = SettingsProperty(
            { holder.settings = it },
            { holder.settings },
            { parentEvents++ }
        )
        // A parent property aligns a nested one with the model object as soon as that object arrives,
        // so the same alignment happens here before the views are built.
        property.refresh()

        rootView = SimpleStringProperty()
        val rootBinding = Bindings.createStringBinding({ settingsState(property.value) }, property)
        // A listener keeps the binding eager, so an invalidation that is never followed by a read
        // still shows up as a change.
        rootBinding.addListener { _, _, _ -> rootViewChanges++ }
        rootView.bind(rootBinding)

        copyrightPageView = SimpleStringProperty()
        val copyrightPageBinding = property.copyrightPageProperty.asString()
        copyrightPageBinding.addListener { _, _, _ -> copyrightPageViewChanges++ }
        copyrightPageView.bind(copyrightPageBinding)

        startWithEmptyPageView = SimpleStringProperty()
        val startWithEmptyPageBinding = property.startWithEmptyPageProperty.asString()
        startWithEmptyPageBinding.addListener { _, _, _ -> startWithEmptyPageViewChanges++ }
        startWithEmptyPageView.bind(startWithEmptyPageBinding)

        endWithEmptyPageView = SimpleStringProperty()
        val endWithEmptyPageBinding = property.endWithEmptyPageProperty.asString()
        endWithEmptyPageBinding.addListener { _, _, _ -> endWithEmptyPageViewChanges++ }
        endWithEmptyPageView.bind(endWithEmptyPageBinding)

        styles = listOf(
            StyleUnderTest("author style", property.authorFontProperty) { it.authorFont },
            StyleUnderTest("copyright style", property.copyrightFontProperty) { it.copyrightFont },
            StyleUnderTest("title style", property.titleFontProperty) { it.titleFont },
            StyleUnderTest("title appendix style", property.titleAppendixFontProperty) { it.titleAppendixFont },
            StyleUnderTest("chapter style", property.chapterFontProperty) { it.chapterFont },
            StyleUnderTest("chapter appendix style", property.chapterAppendixFontProperty) { it.chapterAppendixFont },
            StyleUnderTest("text style", property.textFontProperty) { it.textFont }
        )

        resetCounters()
    }

    private fun resetCounters() {
        parentEvents = 0
        rootViewChanges = 0
        copyrightPageViewChanges = 0
        startWithEmptyPageViewChanges = 0
        endWithEmptyPageViewChanges = 0
        styles.forEach { it.resetCounters() }
    }

    /** The settings every test starts from, built fresh so no test sees the objects of another. */
    private fun newSettings(): Project.Settings = Project.Settings(
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
    )

    /** Text form of the whole settings, used as the value of the binding on the root. */
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

    /** Text form of a single style, used as the value of the binding on that nested object. */
    private fun styleState(style: StyleData?): String =
        "${fontState(style?.font)}|${style?.alignment ?: MISSING}"

    /** Text form of the font of a style, used as the value of the binding on that nested object. */
    private fun fontState(font: FontData?): String =
        "${font?.name ?: MISSING}|${font?.size ?: 0}|${font?.bold ?: false}|${font?.italic ?: false}"

    /**
     * Asserts that every binding of the object tree - the settings, every style, every font and every
     * single field - delivers exactly what the model object carries right now, so no view keeps the
     * value of a previous object or of a previous field value.
     */
    private fun assertTreeShowsModel() {
        val settings = holder.settings

        assertEquals(settingsState(settings), rootView.get()) {
            "the binding on the settings delivers an outdated state"
        }
        assertEquals((settings?.copyrightPage ?: false).toString(), copyrightPageView.get()) {
            "the binding on the copyright page delivers an outdated value"
        }
        assertEquals((settings?.startWithEmptyPage ?: false).toString(), startWithEmptyPageView.get()) {
            "the binding on the leading empty page delivers an outdated value"
        }
        assertEquals((settings?.endWithEmptyPage ?: false).toString(), endWithEmptyPageView.get()) {
            "the binding on the trailing empty page delivers an outdated value"
        }
        styles.forEach { style ->
            val current = style.current()

            assertEquals(styleState(current), style.styleView.get()) {
                "the binding on the ${style.label} delivers an outdated state"
            }
            assertEquals(fontState(current?.font), style.fontView.get()) {
                "the binding on the font of the ${style.label} delivers an outdated state"
            }
            assertEquals(current?.font?.name ?: MISSING, style.nameView.get()) {
                "the binding on the family name of the ${style.label} delivers an outdated value"
            }
            assertEquals((current?.alignment ?: MISSING).toString(), style.alignmentView.get()) {
                "the binding on the placement of the ${style.label} delivers an outdated value"
            }
        }
    }

    /** Asserts that a change of the given style was reported by every binding above that field. */
    private fun assertStyleTreeNotified(style: StyleUnderTest) {
        assertTrue(style.styleViewChanges > 0) { "the binding on the ${style.label} was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the project is read from its file before the user interface is built, so every binding
     * of the object tree delivers the settings that already sit in the model object.
     */
    @Test
    fun readsInitialValuesFromModel() {
        assertTreeShowsModel()
        assertEquals("Text Serif", styles.last().nameView.get())
        assertEquals(Alignment.BLOCK.toString(), styles.last().alignmentView.get())
    }

    /**
     * Use case: the user changes the placement of every single style in the settings dialog, so each
     * value lands in the style it belongs to and the bindings on that field, on the style and on the
     * settings show it.
     */
    @Test
    fun writesStyleAlignmentToModelAndNotifiesTree() {
        styles.forEach { style ->
            resetCounters()
            val target = if (style.property.alignment == Alignment.BLOCK) Alignment.LEFT else Alignment.BLOCK

            style.property.alignment = target

            assertEquals(target, style.current()?.alignment) {
                "the placement did not reach the ${style.label}"
            }
            assertTreeShowsModel()
            assertTrue(style.alignmentViewChanges > 0) {
                "the binding on the placement of the ${style.label} was not re-evaluated"
            }
            assertStyleTreeNotified(style)
        }
    }

    /**
     * Use case: the placement of every style is bound to the choice box of the settings dialog, so each
     * value that choice box produces lands in the style it belongs to and every binding above it shows
     * it.
     */
    @Test
    fun writesBoundStyleAlignmentToModelAndNotifiesTree() {
        styles.forEach { style ->
            resetCounters()
            val source = SimpleObjectProperty<Alignment?>(style.current()?.alignment)
            style.property.alignmentProperty.bind(source)
            val target = if (style.property.alignment == Alignment.BLOCK) Alignment.LEFT else Alignment.BLOCK

            source.set(target)

            assertEquals(target, style.current()?.alignment) {
                "the placement did not reach the ${style.label}"
            }
            assertTreeShowsModel()
            assertTrue(style.alignmentViewChanges > 0) {
                "the binding on the placement of the ${style.label} was not re-evaluated"
            }
            assertStyleTreeNotified(style)
            style.property.alignmentProperty.unbind()
        }
    }

    /**
     * Use case: the user picks another type face for every single style, so each family name lands in
     * the font of the style it belongs to and the bindings on that field, on the font, on the style and
     * on the settings show it.
     */
    @Test
    fun writesNestedFontNameToModelAndNotifiesTree() {
        styles.forEach { style ->
            resetCounters()
            val target = "Modern ${style.label}"

            style.property.fontProperty.name = target

            assertEquals(target, style.current()?.font?.name) {
                "the family name did not reach the font of the ${style.label}"
            }
            assertTreeShowsModel()
            assertTrue(style.nameViewChanges > 0) {
                "the binding on the family name of the ${style.label} was not re-evaluated"
            }
            assertTrue(style.fontViewChanges > 0) {
                "the binding on the font of the ${style.label} was not re-evaluated"
            }
            assertStyleTreeNotified(style)
        }
    }

    /**
     * Use case: the family name of every style is bound to the choice box of the font dialog, so each
     * name that choice box produces lands in the font of the style it belongs to and every binding
     * above it shows it.
     */
    @Test
    fun writesBoundNestedFontNameToModelAndNotifiesTree() {
        styles.forEach { style ->
            resetCounters()
            val source = SimpleStringProperty(style.current()?.font?.name)
            style.property.fontProperty.nameProperty.bind(source)
            val target = "Modern ${style.label}"

            source.set(target)

            assertEquals(target, style.current()?.font?.name) {
                "the family name did not reach the font of the ${style.label}"
            }
            assertTreeShowsModel()
            assertTrue(style.nameViewChanges > 0) {
                "the binding on the family name of the ${style.label} was not re-evaluated"
            }
            assertStyleTreeNotified(style)
            style.property.fontProperty.nameProperty.unbind()
        }
    }

    /**
     * Use case: the user enlarges the text of every single style, so each point size lands in the font
     * of the style it belongs to and every binding above it shows it.
     */
    @Test
    fun writesNestedFontSizeToModelAndNotifiesTree() {
        styles.forEach { style ->
            resetCounters()
            val target = (style.current()?.font?.size ?: 0) + 5

            style.property.fontProperty.size = target

            assertEquals(target, style.current()?.font?.size) {
                "the point size did not reach the font of the ${style.label}"
            }
            assertTreeShowsModel()
            assertTrue(style.fontViewChanges > 0) {
                "the binding on the font of the ${style.label} was not re-evaluated"
            }
            assertStyleTreeNotified(style)
        }
    }

    /**
     * Use case: the point size of every style is bound to the spinner of the font dialog, so each
     * number that spinner produces lands in the font of the style it belongs to and every binding above
     * it shows it.
     */
    @Test
    fun writesBoundNestedFontSizeToModelAndNotifiesTree() {
        styles.forEach { style ->
            resetCounters()
            val source = SimpleIntegerProperty(style.current()?.font?.size ?: 0)
            style.property.fontProperty.sizeProperty.bind(source)
            val target = (style.current()?.font?.size ?: 0) + 5

            source.set(target)

            assertEquals(target, style.current()?.font?.size) {
                "the point size did not reach the font of the ${style.label}"
            }
            assertTreeShowsModel()
            assertStyleTreeNotified(style)
            style.property.fontProperty.sizeProperty.unbind()
        }
    }

    /**
     * Use case: the user switches the weight of every single style, so each flag lands in the font of
     * the style it belongs to and every binding above it shows it.
     */
    @Test
    fun writesNestedFontBoldToModelAndNotifiesTree() {
        styles.forEach { style ->
            resetCounters()
            val target = !(style.current()?.font?.bold ?: false)

            style.property.fontProperty.bold = target

            assertEquals(target, style.current()?.font?.bold) {
                "the bold flag did not reach the font of the ${style.label}"
            }
            assertTreeShowsModel()
            assertTrue(style.fontViewChanges > 0) {
                "the binding on the font of the ${style.label} was not re-evaluated"
            }
            assertStyleTreeNotified(style)
        }
    }

    /**
     * Use case: the bold flag of every style is bound to the toggle button of the font dialog, so each
     * state that button produces lands in the font of the style it belongs to and every binding above
     * it shows it.
     */
    @Test
    fun writesBoundNestedFontBoldToModelAndNotifiesTree() {
        styles.forEach { style ->
            resetCounters()
            val source = SimpleBooleanProperty(style.current()?.font?.bold ?: false)
            style.property.fontProperty.boldProperty.bind(source)
            val target = !(style.current()?.font?.bold ?: false)

            source.set(target)

            assertEquals(target, style.current()?.font?.bold) {
                "the bold flag did not reach the font of the ${style.label}"
            }
            assertTreeShowsModel()
            assertStyleTreeNotified(style)
            style.property.fontProperty.boldProperty.unbind()
        }
    }

    /**
     * Use case: the user switches every single style to a slanted face and back, so each flag lands in
     * the font of the style it belongs to and every binding above it shows it.
     */
    @Test
    fun writesNestedFontItalicToModelAndNotifiesTree() {
        styles.forEach { style ->
            resetCounters()
            val target = !(style.current()?.font?.italic ?: false)

            style.property.fontProperty.italic = target

            assertEquals(target, style.current()?.font?.italic) {
                "the italic flag did not reach the font of the ${style.label}"
            }
            assertTreeShowsModel()
            assertTrue(style.fontViewChanges > 0) {
                "the binding on the font of the ${style.label} was not re-evaluated"
            }
            assertStyleTreeNotified(style)
        }
    }

    /**
     * Use case: the italic flag of every style is bound to the toggle button of the font dialog, so
     * each state that button produces lands in the font of the style it belongs to and every binding
     * above it shows it.
     */
    @Test
    fun writesBoundNestedFontItalicToModelAndNotifiesTree() {
        styles.forEach { style ->
            resetCounters()
            val source = SimpleBooleanProperty(style.current()?.font?.italic ?: false)
            style.property.fontProperty.italicProperty.bind(source)
            val target = !(style.current()?.font?.italic ?: false)

            source.set(target)

            assertEquals(target, style.current()?.font?.italic) {
                "the italic flag did not reach the font of the ${style.label}"
            }
            assertTreeShowsModel()
            assertStyleTreeNotified(style)
            style.property.fontProperty.italicProperty.unbind()
        }
    }

    /**
     * Use case: a whole style is replaced - the user applied a prepared style to one of the parts - so
     * the properties below it belong to another object afterwards and every binding of the object tree
     * shows the values of that object instead of the previous ones.
     */
    @Test
    fun writesStyleToModelAndNotifiesTree() {
        styles.forEach { style ->
            resetCounters()
            val target = StyleData(
                font = FontData("Replaced ${style.label}", 21, bold = true, italic = true),
                alignment = Alignment.RIGHT
            )

            style.property.value = target

            assertEquals(target, style.current()) { "the style did not reach the ${style.label}" }
            assertTreeShowsModel()
            assertTrue(style.nameViewChanges > 0) {
                "the binding on the family name of the ${style.label} was not re-evaluated"
            }
            assertTrue(style.fontViewChanges > 0) {
                "the binding on the font of the ${style.label} was not re-evaluated"
            }
            assertStyleTreeNotified(style)
        }
    }

    /**
     * Use case: the user asks for a separate copyright page, so the flag lands in the model object and
     * both the binding on that field and the binding on the settings show it.
     */
    @Test
    fun writesCopyrightPageToModelAndNotifiesTree() {
        property.copyrightPage = true

        assertEquals(true, holder.settings?.copyrightPage)
        assertTreeShowsModel()
        assertTrue(copyrightPageViewChanges > 0) { "the binding on the copyright page was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the copyright page is bound to the check box of the settings dialog, so every state
     * that check box produces reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundCopyrightPageToModelAndNotifiesTree() {
        val source = SimpleBooleanProperty(false)
        property.copyrightPageProperty.bind(source)

        source.set(true)

        assertEquals(true, holder.settings?.copyrightPage)
        assertTreeShowsModel()
        assertTrue(copyrightPageViewChanges > 0) { "the binding on the copyright page was not re-evaluated" }
        assertTrue(rootViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user drops the empty page at the beginning of the manuscript, so the flag lands in
     * the model object and both the binding on that field and the binding on the settings show it.
     */
    @Test
    fun writesStartWithEmptyPageToModelAndNotifiesTree() {
        property.startWithEmptyPage = false

        assertEquals(false, holder.settings?.startWithEmptyPage)
        assertTreeShowsModel()
        assertTrue(startWithEmptyPageViewChanges > 0) {
            "the binding on the leading empty page was not re-evaluated"
        }
        assertTrue(rootViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the leading empty page is bound to the check box of the settings dialog, so every state
     * that check box produces reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundStartWithEmptyPageToModelAndNotifiesTree() {
        val source = SimpleBooleanProperty(true)
        property.startWithEmptyPageProperty.bind(source)

        source.set(false)

        assertEquals(false, holder.settings?.startWithEmptyPage)
        assertTreeShowsModel()
        assertTrue(startWithEmptyPageViewChanges > 0) {
            "the binding on the leading empty page was not re-evaluated"
        }
        assertTrue(rootViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the user drops the empty page at the end of the manuscript, so the flag lands in the
     * model object and both the binding on that field and the binding on the settings show it.
     */
    @Test
    fun writesEndWithEmptyPageToModelAndNotifiesTree() {
        property.endWithEmptyPage = false

        assertEquals(false, holder.settings?.endWithEmptyPage)
        assertTreeShowsModel()
        assertTrue(endWithEmptyPageViewChanges > 0) {
            "the binding on the trailing empty page was not re-evaluated"
        }
        assertTrue(rootViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: the trailing empty page is bound to the check box of the settings dialog, so every
     * state that check box produces reaches the model object and every binding above it shows it.
     */
    @Test
    fun writesBoundEndWithEmptyPageToModelAndNotifiesTree() {
        val source = SimpleBooleanProperty(true)
        property.endWithEmptyPageProperty.bind(source)

        source.set(false)

        assertEquals(false, holder.settings?.endWithEmptyPage)
        assertTreeShowsModel()
        assertTrue(endWithEmptyPageViewChanges > 0) {
            "the binding on the trailing empty page was not re-evaluated"
        }
        assertTrue(rootViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        assertTrue(parentEvents > 0) { "the parent property was not told about the change" }
    }

    /**
     * Use case: a field of the settings or of a style nested in them is changed by application code
     * past the property, so every field property delivers the current value instead of a cached copy.
     */
    @Test
    fun readsFieldsChangedOnModel() {
        holder.settings?.copyrightPage = true
        holder.settings?.startWithEmptyPage = false
        holder.settings?.endWithEmptyPage = false
        holder.settings?.textFont?.alignment = Alignment.LEFT
        holder.settings?.textFont?.font?.name = "Modern Text"
        holder.settings?.textFont?.font?.size = 13
        holder.settings?.textFont?.font?.bold = true
        holder.settings?.textFont?.font?.italic = true

        assertTrue(property.copyrightPage)
        assertFalse(property.startWithEmptyPage)
        assertFalse(property.endWithEmptyPage)
        assertEquals(Alignment.LEFT, property.textFontProperty.alignment)
        assertEquals("Modern Text", property.textFontProperty.fontProperty.name)
        assertEquals(13, property.textFontProperty.fontProperty.size)
        assertTrue(property.textFontProperty.fontProperty.bold)
        assertTrue(property.textFontProperty.fontProperty.italic)
    }

    /**
     * Use case: the whole settings object is replaced - another project file was loaded - so every
     * property of the object tree belongs to another object afterwards and every binding shows the
     * values of that object instead of the previous ones.
     */
    @Test
    fun writesReplacedSettingsToModelAndNotifiesWholeTree() {
        val replacement = Project.Settings(
            authorFont = StyleData(FontData("Modern Author", 11, bold = true, italic = false), Alignment.LEFT),
            copyrightFont = StyleData(FontData("Modern Copyright", 9, bold = true, italic = true), Alignment.RIGHT),
            titleFont = StyleData(FontData("Modern Title", 30, bold = false, italic = true), Alignment.LEFT),
            titleAppendixFont = StyleData(
                FontData("Modern Title Appendix", 19, bold = true, italic = false),
                Alignment.LEFT
            ),
            chapterFont = StyleData(FontData("Modern Chapter", 22, bold = false, italic = true), Alignment.CENTER),
            chapterAppendixFont = StyleData(
                FontData("Modern Chapter Appendix", 15, bold = true, italic = false),
                Alignment.CENTER
            ),
            textFont = StyleData(FontData("Modern Text", 13, bold = true, italic = true), Alignment.LEFT),
            copyrightPage = true,
            startWithEmptyPage = false,
            endWithEmptyPage = false
        )

        property.value = replacement

        assertEquals(replacement, holder.settings)
        assertTreeShowsModel()
        assertTrue(copyrightPageViewChanges > 0) { "the binding on the copyright page was not re-evaluated" }
        assertTrue(startWithEmptyPageViewChanges > 0) {
            "the binding on the leading empty page was not re-evaluated"
        }
        assertTrue(endWithEmptyPageViewChanges > 0) {
            "the binding on the trailing empty page was not re-evaluated"
        }
        assertTrue(rootViewChanges > 0) { "the binding on the settings was not re-evaluated" }
        styles.forEach { style ->
            assertTrue(style.nameViewChanges > 0) {
                "the binding on the family name of the ${style.label} was not re-evaluated"
            }
            assertTrue(style.fontViewChanges > 0) {
                "the binding on the font of the ${style.label} was not re-evaluated"
            }
            assertTrue(style.styleViewChanges > 0) {
                "the binding on the ${style.label} was not re-evaluated"
            }
        }
    }

    /**
     * Use case: the settings are exchanged for an object carrying the same values, so nothing the user
     * interface shows changes and no field property reports a change of its own.
     */
    @Test
    fun keepsFieldsQuietWhenReplacedSettingsCarryTheSameValues() {
        property.value = newSettings()

        assertTreeShowsModel()
        assertEquals(0, copyrightPageViewChanges) {
            "the copyright page was reported as changed although it did not change"
        }
        assertEquals(0, startWithEmptyPageViewChanges) {
            "the leading empty page was reported as changed although it did not change"
        }
        assertEquals(0, endWithEmptyPageViewChanges) {
            "the trailing empty page was reported as changed although it did not change"
        }
        styles.forEach { style ->
            assertEquals(0, style.nameViewChanges) {
                "the family name of the ${style.label} was reported as changed although it did not change"
            }
            assertEquals(0, style.alignmentViewChanges) {
                "the placement of the ${style.label} was reported as changed although it did not change"
            }
            assertEquals(0, style.fontViewChanges) {
                "the font of the ${style.label} was reported as changed although it did not change"
            }
            assertEquals(0, style.styleViewChanges) {
                "the ${style.label} was reported as changed although it did not change"
            }
        }
    }

    /**
     * Use case: no project is open at all, so the property carries no settings and every field property
     * - down to the fields of the styles nested in them - answers with a neutral value, which lets the
     * settings dialog be built before a project is loaded.
     */
    @Test
    fun readsNeutralValuesWhenSettingsAreAbsent() {
        property.value = null

        assertFalse(property.copyrightPage)
        assertFalse(property.startWithEmptyPage)
        assertFalse(property.endWithEmptyPage)
        styles.forEach { style ->
            assertNull(style.property.value) { "the ${style.label} still carries a style" }
            assertNull(style.property.alignment) { "the placement of the ${style.label} is not neutral" }
            assertNull(style.property.fontProperty.name) {
                "the family name of the ${style.label} is not neutral"
            }
            assertEquals(0, style.property.fontProperty.size) {
                "the point size of the ${style.label} is not neutral"
            }
        }
        assertTreeShowsModel()
    }

    /**
     * Use case: the settings dialog writes into the property while no project is open, so the values
     * are dropped instead of creating a settings object nobody asked for.
     */
    @Test
    fun dropsWritesWhenSettingsAreAbsent() {
        property.value = null

        property.copyrightPage = true
        property.startWithEmptyPage = true
        property.endWithEmptyPage = true
        styles.forEach { style ->
            style.property.alignment = Alignment.RIGHT
            style.property.fontProperty.name = "Modern ${style.label}"
        }

        assertNull(holder.settings)
    }

    private companion object {
        /** Stands for a value the model object does not carry at all. */
        const val MISSING = "-"
    }
}
