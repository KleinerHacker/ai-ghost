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
import javafx.scene.control.ComboBox
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.design.PrologPageDesignProperty
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.StyleData
import org.pcsoft.app.aighost.model.project.Project
import org.pcsoft.app.aighost.model.project.design.Design
import org.pcsoft.app.aighost.model.project.design.PrologPageDesign
import org.testfx.framework.junit5.ApplicationTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [BookPartPageDesignSettingsViewModel].
 *
 * [BookPartPageDesignSettingsViewModel.bind] forwards each style property straight to the public
 * [StyleDataEditor.bindStyle] of an embedded editor, so the view model can only be exercised together
 * with real [StyleDataEditor] instances - constructing one loads its FXML and therefore needs a
 * running Java FX toolkit, which is why this class extends [ApplicationTest] like the sibling
 * `*Test` class, even though it targets the view model rather than the assembled component. The three
 * style properties bound here come from a [PrologPageDesignProperty] because it names them exactly
 * `titleStyle`, `titleAppendixStyle` and `textStyle`, as
 * [org.pcsoft.app.aighost.model.project.design.BookPartPageDesign] prescribes.
 */
class BookPartPageDesignSettingsViewModelTest : ApplicationTest() {

    private lateinit var viewModel: BookPartPageDesignSettingsViewModel

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        viewModel = BookPartPageDesignSettingsViewModel().also {
            it.titleEditor = StyleDataEditor()
            it.titleAppendixEditor = StyleDataEditor()
            it.textEditor = StyleDataEditor()
        }
    }

    private fun prologOf(
        titleStyle: StyleData,
        titleAppendixStyle: StyleData,
        textStyle: StyleData
    ): PrologPageDesignProperty {
        val design = ProjectProperty(
            Project(
                design = Design(
                    prologPage = PrologPageDesign(
                        titleStyle = titleStyle,
                        titleAppendixStyle = titleAppendixStyle,
                        textStyle = textStyle
                    )
                )
            )
        ).designProperty
        return design.prologPageProperty
    }

    private fun styleOf(family: String): StyleData = StyleData(font = FontData(name = family, size = 12))

    @Suppress("UNCHECKED_CAST")
    private fun StyleDataEditor.familyText(): String =
        (lookup("#cmbFamily") as ComboBox<String>).editor.text

    /**
     * Use case: three style properties are handed to [BookPartPageDesignSettingsViewModel.bind], so
     * each embedded editor follows the style meant for it - the title editor the title style, the
     * appendix editor the appendix style, the text editor the text style.
     */
    @Test
    fun boundStylesReachTheMatchingEditor() {
        val prolog = prologOf(styleOf("Georgia"), styleOf("Cambria"), styleOf("Verdana"))

        interact {
            viewModel.bind(
                prolog.titleStyleProperty,
                prolog.titleAppendixStyleProperty,
                prolog.textStyleProperty
            )
        }

        assertEquals("Georgia", viewModel.titleEditor.familyText())
        assertEquals("Cambria", viewModel.titleAppendixEditor.familyText())
        assertEquals("Verdana", viewModel.textEditor.familyText())
    }

    /**
     * Use case: [BookPartPageDesignSettingsViewModel.valid] is true only while every embedded editor
     * reports itself valid; an invalid field on any one of the three flips it to false.
     */
    @Test
    fun validReflectsEveryEmbeddedEditor() {
        val prolog = prologOf(styleOf("Georgia"), styleOf("Cambria"), styleOf("Verdana"))

        interact {
            viewModel.bind(
                prolog.titleStyleProperty,
                prolog.titleAppendixStyleProperty,
                prolog.textStyleProperty
            )
        }

        assertTrue(viewModel.valid.value)

        interact { prolog.titleAppendixStyleProperty.fontProperty.name = "" }
        assertFalse(viewModel.valid.value)
    }

    /**
     * Use case: a second triple of style properties is bound, so every embedded editor follows the new
     * style and writing into it reaches the new style only, the styles left behind stay untouched.
     */
    @Test
    fun rebindingShowsTheNewStylesAndLeavesTheOldUntouched() {
        val first = prologOf(styleOf("Georgia"), styleOf("Cambria"), styleOf("Verdana"))
        interact {
            viewModel.bind(
                first.titleStyleProperty,
                first.titleAppendixStyleProperty,
                first.textStyleProperty
            )
        }

        val second = prologOf(styleOf("Tahoma"), styleOf("Calibri"), styleOf("Consolas"))
        interact {
            viewModel.bind(
                second.titleStyleProperty,
                second.titleAppendixStyleProperty,
                second.textStyleProperty
            )
        }

        assertEquals("Tahoma", viewModel.titleEditor.familyText())

        interact { second.titleStyleProperty.fontProperty.name = "Palatino" }
        assertEquals("Palatino", second.titleStyleProperty.get()!!.font.name)
        assertEquals("Georgia", first.titleStyleProperty.get()!!.font.name)
    }
}
