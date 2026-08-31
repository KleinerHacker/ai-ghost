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

package org.pcsoft.app.aighost.app.ui.dialog

import de.saxsys.mvvmfx.MvvmFX
import javafx.scene.control.ButtonType
import javafx.scene.control.Spinner
import javafx.scene.control.TreeView
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.ui.component.ProjectSettingsSection
import org.pcsoft.app.aighost.fx.model.project.ProjectProperty
import org.pcsoft.app.aighost.fx.model.project.design.DesignProperty
import org.pcsoft.app.aighost.model.project.Project
import org.testfx.framework.junit5.ApplicationTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [ProjectSettingsDialog].
 *
 * The dialog is built but never shown: showing it would wait for an answer of the user, while its
 * buttons, its navigation and its lock on invalid input are part of the dialog pane already.
 */
class ProjectSettingsDialogTest : ApplicationTest() {

    private companion object {
        const val DELTA: Double = 1e-6
    }

    private val target: DesignProperty
        get() = ProjectProperty(Project()).designProperty

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )
    }

    private fun build(design: DesignProperty): ProjectSettingsDialog {
        lateinit var dialog: ProjectSettingsDialog
        interact { dialog = ProjectSettingsDialog(design) }
        return dialog
    }

    /**
     * Use case: the settings are edited, so the dialog carries OK, CANCEL and APPLY and draws its own
     * header instead of the one of the plain alert.
     */
    @Test
    fun carriesTheEditorButtonsAndItsOwnHeader() {
        val dialog = build(target)

        assertEquals(
            listOf(ButtonType.OK, ButtonType.CANCEL, ButtonType.APPLY),
            dialog.dialogPane.buttonTypes
        )
        assertNull(dialog.dialogPane.headerText, "the alert still carries its own header")
        assertNull(dialog.dialogPane.graphic, "the alert still carries its own icon")
        assertNotNull(dialog.dialogPane.content, "the dialog shows no content of its own")
    }

    /**
     * Use case: the user picks a design child section, so its placeholder comes to the front and the
     * design editor steps back.
     */
    @Test
    fun pickingADesignSectionShowsItsPlaceholder() {
        val dialog = build(target)

        @Suppress("UNCHECKED_CAST")
        val tree = dialog.dialogPane.lookup(".tree-view") as TreeView<ProjectSettingsSection>
        val design = dialog.dialogPane.lookup(".design-settings")
        val placeholder = dialog.dialogPane.lookup(".placeholder-settings")

        assertTrue(design.isVisible, "the dialog opens on the design editor")

        interact { tree.selectionModel.select(tree.root.children[1].children[0]) }

        assertFalse(design.isVisible, "the design editor stayed in front of a design child section")
        assertTrue(placeholder.isVisible, "the placeholder of the design child section is not shown")
    }

    /**
     * Use case: the page geometry becomes impossible, so OK and APPLY are locked until the input can
     * be stored again.
     */
    @Test
    fun locksOkAndApplyWhileTheInputIsInvalid() {
        val dialog = build(target)
        val ok = dialog.dialogPane.lookupButton(ButtonType.OK)
        val apply = dialog.dialogPane.lookupButton(ButtonType.APPLY)
        val width = dialog.dialogPane.lookup("#spnWidth") as Spinner<*>

        assertFalse(ok.isDisabled, "OK is locked on a valid page")
        assertFalse(apply.isDisabled, "APPLY is locked on a valid page")

        interact { width.editor.text = "" }

        assertTrue(ok.isDisabled, "OK stays open on an impossible page")
        assertTrue(apply.isDisabled, "APPLY stays open on an impossible page")

        interact { width.editor.text = "148" }

        assertFalse(ok.isDisabled, "OK stays locked after the page became valid again")
    }

    /**
     * Use case: the user presses APPLY, so the working copy is written into the design of the open
     * project without the dialog closing.
     */
    @Test
    fun applyWritesTheWorkingCopyIntoTheProject() {
        val design = target
        val dialog = build(design)
        val width = dialog.dialogPane.lookup("#spnWidth") as Spinner<*>
        val original = design.get()!!.pageFormat.width

        interact { width.editor.text = "500" }
        assertEquals(original, design.get()!!.pageFormat.width, DELTA, "the working copy leaked into the project")

        interact { dialog.applyChanges() }

        assertEquals(500.0 * (72.0 / 25.4), design.get()!!.pageFormat.width, DELTA)
    }
}
