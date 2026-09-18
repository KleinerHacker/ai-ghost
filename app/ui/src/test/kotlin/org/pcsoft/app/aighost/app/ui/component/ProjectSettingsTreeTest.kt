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
import javafx.scene.control.TreeView
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.testfx.framework.junit5.ApplicationTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [ProjectSettingsTree].
 */
class ProjectSettingsTreeTest : ApplicationTest() {

    private lateinit var component: ProjectSettingsTree

    @Suppress("UNCHECKED_CAST")
    private val tree: TreeView<ProjectSettingsSection>
        get() = component.lookup(".tree-view") as TreeView<ProjectSettingsSection>

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        component = ProjectSettingsTree()
        stage.scene = Scene(component, 260.0, 400.0)
        stage.show()
    }

    /**
     * Use case: the dialog needs a flat navigation, so the tree hides its root and shows "General"
     * and "Design" as the top level, with the six part sections below "Design" in the fixed order
     * title page, copyright page, prolog, chapter, epilog, blurb.
     */
    @Test
    fun carriesEverySectionWithoutARoot() {
        assertFalse(tree.isShowRoot, "the tree shows its synthetic root")

        assertEquals(
            listOf(ProjectSettingsSection.General, ProjectSettingsSection.Design),
            tree.root.children.map { it.value }
        )
        assertEquals(
            listOf(
                ProjectSettingsSection.DesignTitle,
                ProjectSettingsSection.DesignCopyright,
                ProjectSettingsSection.DesignProlog,
                ProjectSettingsSection.DesignChapter,
                ProjectSettingsSection.DesignEpilog,
                ProjectSettingsSection.DesignBlurb
            ),
            tree.root.children[1].children.map { it.value }
        )
    }

    /**
     * Use case: the dialog opens on the only section with a real editor, so "Design" is selected
     * from the start.
     */
    @Test
    fun startsOnTheDesignSection() {
        assertEquals(ProjectSettingsSection.Design, component.selectedSection.value)
    }

    /**
     * Use case: the user clicks a node, so the picked section is reported to the dialog.
     */
    @Test
    fun reportsThePickedSection() {
        interact { tree.selectionModel.select(tree.root.children[1].children[0]) }

        assertEquals(ProjectSettingsSection.DesignTitle, component.selectedSection.value)
    }

    /**
     * Use case: the dialog moves the selection itself, so the tree follows and reports the new
     * section.
     */
    @Test
    fun selectionCanBeDrivenFromOutside() {
        interact { component.select(ProjectSettingsSection.DesignBlurb) }

        assertEquals(ProjectSettingsSection.DesignBlurb, component.selectedSection.value)
        assertEquals(ProjectSettingsSection.DesignBlurb, tree.selectionModel.selectedItem.value)
    }
}
