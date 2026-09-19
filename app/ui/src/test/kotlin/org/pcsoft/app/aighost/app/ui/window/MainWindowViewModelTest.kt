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

package org.pcsoft.app.aighost.app.ui.window

import java.io.File
import javafx.beans.property.SimpleStringProperty
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.aighost.model.project.meta.Meta
import org.testfx.framework.junit5.ApplicationExtension

/**
 * Developer tests for [MainWindowViewModel]'s undo history reset on a project switch ([undoStack],
 * IP-33) - [BookPartEditorTest] proves [org.pcsoft.app.aighost.app.undo.UndoStack.clear] itself, this
 * class proves that [MainWindowViewModel.newProject] and [MainWindowViewModel.openProject] actually call
 * it, so a history reaching into a project that no longer applies cannot survive a switch.
 */
@ExtendWith(ApplicationExtension::class)
class MainWindowViewModelTest {

    /** Records one change on a throwaway property, so [MainWindowViewModel.undoStack] has history. */
    private fun MainWindowViewModel.recordAThrowawayChange() {
        val property = SimpleStringProperty("before")
        undoStack.record("change", property, "before", "after")
    }

    /**
     * Use case: the user starts a fresh project while the previous one still had undo history, so
     * neither an undo nor a redo can reach back into the project that was just closed.
     */
    @Test
    fun newProjectClearsUndoAndRedoHistory() {
        val viewModel = MainWindowViewModel()
        viewModel.recordAThrowawayChange()
        viewModel.undoStack.undo()
        assertTrue(viewModel.undoStack.canRedoProperty.get(), "the undone change must be redoable before the switch")

        viewModel.newProject()

        assertFalse(viewModel.undoStack.canUndoProperty.get(), "a fresh project must have nothing left to undo")
        assertFalse(viewModel.undoStack.canRedoProperty.get(), "a fresh project must have nothing left to redo")
    }

    /**
     * Use case: the user opens a different project file while the previous one still had undo history,
     * so neither an undo nor a redo can reach back into the project that was just replaced.
     */
    @Test
    fun openProjectClearsUndoAndRedoHistory(@TempDir tempDir: File) {
        val file = File(tempDir, "novel.ghost")
        val savingViewModel = MainWindowViewModel()
        savingViewModel.project.value.meta = Meta(name = "My Novel", author = "Jane Doe")
        assertTrue(savingViewModel.saveProject(file), "the fixture project must save without error")

        val viewModel = MainWindowViewModel()
        viewModel.recordAThrowawayChange()
        viewModel.undoStack.undo()
        assertTrue(viewModel.undoStack.canRedoProperty.get(), "the undone change must be redoable before the switch")

        assertTrue(viewModel.openProject(file), "the fixture project must open without error")

        assertFalse(viewModel.undoStack.canUndoProperty.get(), "an opened project must have nothing left to undo")
        assertFalse(viewModel.undoStack.canRedoProperty.get(), "an opened project must have nothing left to redo")
    }
}
