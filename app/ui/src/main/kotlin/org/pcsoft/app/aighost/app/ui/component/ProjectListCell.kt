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

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TextInputDialog
import javafx.scene.control.TreeCell
import org.pcsoft.app.aighost.app.AiGhostIcons
import org.pcsoft.app.aighost.app.ui.AiGhostDialog
import org.pcsoft.app.aighost.model.project.book.Chapter
import java.util.*

/**
 * Renders one [ProjectListItem] inside the project tree of [ProjectList].
 *
 * The structural nodes are labelled from the message bundle, a chapter node by the name the user
 * gave the chapter - not by its printed heading, which may still be empty while the chapter is only
 * outlined.
 *
 * Adding, renaming and removing a chapter (IP-38) is carried out here, in the one place that already
 * shows the tree and can ask the user - [viewModel] only ever changes the manuscript once it is told
 * to, unconditionally.
 *
 * @property messages the bundle the view was loaded with, so a cell speaks the same language as the
 *   FXML around it
 * @property viewModel the view model of the surrounding [ProjectList], carrying out every change a
 *   context menu action here decides on
 */
internal class ProjectListCell(
    private val messages: ResourceBundle,
    private val viewModel: ProjectListViewModel
) : TreeCell<ProjectListItem>() {

    override fun updateItem(item: ProjectListItem?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            text = null
            graphic = null
            contextMenu = null
            return
        }

        text = when (item) {
            is ProjectListItem.Root -> messages.getString("component.projectList.root")
            is ProjectListItem.TitlePageItem -> messages.getString("component.projectList.titlePage")
            is ProjectListItem.CopyrightPageItem -> messages.getString("component.projectList.copyrightPage")
            is ProjectListItem.PrologItem -> messages.getString("component.projectList.prolog")
            is ProjectListItem.Chapters -> messages.getString("component.projectList.chapter")
            is ProjectListItem.ChapterItem -> item.chapter.name
            is ProjectListItem.EpilogItem -> messages.getString("component.projectList.epilog")
            is ProjectListItem.BlurbItem -> messages.getString("component.projectList.blurb")
        }

        graphic = when (item) {
            is ProjectListItem.Root -> null
            is ProjectListItem.TitlePageItem -> null
            is ProjectListItem.CopyrightPageItem -> null
            is ProjectListItem.PrologItem -> AiGhostIcons.treeProlog()
            is ProjectListItem.Chapters -> AiGhostIcons.treeChapter()
            is ProjectListItem.ChapterItem -> AiGhostIcons.treeChapter()
            is ProjectListItem.EpilogItem -> AiGhostIcons.treeEpilog()
            is ProjectListItem.BlurbItem -> AiGhostIcons.treeBlurb()
        }

        contextMenu = when (item) {
            is ProjectListItem.Root -> null
            is ProjectListItem.TitlePageItem -> null
            is ProjectListItem.CopyrightPageItem -> null
            is ProjectListItem.PrologItem -> null
            is ProjectListItem.Chapters -> ContextMenu(addChapterItem())
            is ProjectListItem.ChapterItem -> ContextMenu(
                addChapterItem(),
                renameChapterItem(item.chapter),
                deleteChapterItem(item.chapter)
            )

            is ProjectListItem.EpilogItem -> null
            is ProjectListItem.BlurbItem -> null
        }
    }

    private fun addChapterItem(): MenuItem =
        MenuItem(messages.getString("component.projectList.menu.addChapter"), AiGhostIcons.treeChapter()).apply {
            setOnAction { viewModel.addChapter() }
        }

    private fun renameChapterItem(chapter: Chapter): MenuItem =
        MenuItem(messages.getString("component.projectList.menu.renameChapter")).apply {
            setOnAction {
                val dialog = TextInputDialog(chapter.name)
                dialog.title = messages.getString("dialog.renameChapter.title")
                dialog.headerText = null
                dialog.contentText = messages.getString("dialog.renameChapter.label")
                scene?.window?.let(dialog::initOwner)

                dialog.showAndWait().ifPresent { name ->
                    if (name.isNotBlank()) {
                        viewModel.renameChapter(chapter, name)
                    }
                }
            }
        }

    private fun deleteChapterItem(chapter: Chapter): MenuItem =
        MenuItem(messages.getString("component.projectList.menu.deleteChapter")).apply {
            setOnAction {
                val confirmed = AiGhostDialog.showWarningConfirm(
                    title = messages.getString("dialog.deleteChapter.title"),
                    caption = messages.getString("dialog.deleteChapter.caption"),
                    message = messages.getString("dialog.deleteChapter.message"),
                    owner = scene?.window
                )
                if (confirmed) {
                    viewModel.removeChapter(chapter)
                }
            }
        }
}
