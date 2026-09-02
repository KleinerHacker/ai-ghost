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
import javafx.scene.Scene
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
 * Developer tests for [BookPartPageDesignSettings].
 *
 * The checks run on the real controls the user works with - the family combo box of each embedded
 * [StyleDataEditor] - and prove the binding to the three style properties in both directions, for
 * every one of the three embedded editors on its own.
 */
class BookPartPageDesignSettingsTest : ApplicationTest() {

    private lateinit var component: BookPartPageDesignSettings

    private lateinit var prolog: PrologPageDesignProperty

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        prolog = prologOf(styleOf("Georgia"), styleOf("Cambria"), styleOf("Verdana"))

        component = BookPartPageDesignSettings()
        component.bindStyles(
            prolog.titleStyleProperty,
            prolog.titleAppendixStyleProperty,
            prolog.textStyleProperty
        )
        stage.scene = Scene(component, 480.0, 720.0)
        stage.show()
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
    private fun family(fxId: String): ComboBox<String> = component.lookup(fxId) as ComboBox<String>

    /**
     * Use case: three styles are handed to the component, so the family field of every one of the
     * three embedded editors shows the value belonging to it.
     */
    @Test
    fun boundStylesReachEveryEmbeddedEditor() {
        assertEquals("Georgia", family("#titleEditor #cmbFamily").editor.text)
        assertEquals("Cambria", family("#titleAppendixEditor #cmbFamily").editor.text)
        assertEquals("Verdana", family("#textEditor #cmbFamily").editor.text)
    }

    /**
     * Use case: a value is written through one of the three style properties, so the family field of
     * the matching embedded editor shows it, the other two stay unchanged.
     */
    @Test
    fun modelToUi() {
        interact { prolog.titleAppendixStyleProperty.fontProperty.name = "Palatino" }

        assertEquals("Georgia", family("#titleEditor #cmbFamily").editor.text)
        assertEquals("Palatino", family("#titleAppendixEditor #cmbFamily").editor.text)
        assertEquals("Verdana", family("#textEditor #cmbFamily").editor.text)
    }

    /**
     * Use case: the user edits the family field of every one of the three embedded editors, so the
     * plain style object behind each one carries the value written into it, read from the object
     * itself.
     */
    @Test
    fun uiToModel() {
        interact {
            family("#titleEditor #cmbFamily").editor.text = "Tahoma"
            family("#titleAppendixEditor #cmbFamily").editor.text = "Calibri"
            family("#textEditor #cmbFamily").editor.text = "Consolas"
        }

        assertEquals("Tahoma", prolog.titleStyleProperty.get()!!.font.name)
        assertEquals("Calibri", prolog.titleAppendixStyleProperty.get()!!.font.name)
        assertEquals("Consolas", prolog.textStyleProperty.get()!!.font.name)
    }

    /**
     * Use case: [BookPartPageDesignSettings.valid] is true only while every embedded editor reports
     * itself valid; clearing the family field of any one of the three flips it to false, and setting it
     * again flips it back to true.
     */
    @Test
    fun validReflectsEveryEmbeddedEditor() {
        assertTrue(component.valid.value)

        interact { family("#titleAppendixEditor #cmbFamily").editor.text = "" }
        assertFalse(component.valid.value)

        interact { family("#titleAppendixEditor #cmbFamily").editor.text = "Cambria" }
        assertTrue(component.valid.value)
    }

    /**
     * Use case: another triple of style properties takes the place of the first, so every embedded
     * editor shows the new styles and editing them reaches the new objects only, the ones left behind
     * stay untouched.
     */
    @Test
    fun exchangingTheStylesShowsTheNewOnesAndLeavesTheOldUntouched() {
        val second = prologOf(styleOf("Tahoma"), styleOf("Calibri"), styleOf("Consolas"))

        interact {
            component.bindStyles(
                second.titleStyleProperty,
                second.titleAppendixStyleProperty,
                second.textStyleProperty
            )
        }

        assertEquals("Tahoma", family("#titleEditor #cmbFamily").editor.text)

        interact { family("#titleEditor #cmbFamily").editor.text = "Georgia" }
        assertEquals("Georgia", second.titleStyleProperty.get()!!.font.name)
        assertEquals("Georgia", prolog.titleStyleProperty.get()!!.font.name)
    }
}
