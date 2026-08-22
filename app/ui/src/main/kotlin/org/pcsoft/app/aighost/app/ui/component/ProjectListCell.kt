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
import javafx.scene.control.TreeCell
import org.pcsoft.app.aighost.app.AiGhostIcons
import java.util.*

/**
 * Renders one [ProjectListItem] inside the project tree of [ProjectList].
 *
 * The structural nodes are labelled from the message bundle, a chapter node by the name the user
 * gave the chapter - not by its printed heading, which may still be empty while the chapter is only
 * outlined.
 *
 * @property messages the bundle the view was loaded with, so a cell speaks the same language as the
 *   FXML around it
 */
internal class ProjectListCell(private val messages: ResourceBundle) : TreeCell<ProjectListItem>() {

    override fun updateItem(item: ProjectListItem?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            text = null
            graphic = null
            return
        }

        text = when (item) {
            is ProjectListItem.Root -> messages.getString("projectList.root")
            is ProjectListItem.PrologItem -> messages.getString("projectList.prolog")
            is ProjectListItem.Chapters -> messages.getString("projectList.chapter")
            is ProjectListItem.ChapterItem -> item.chapter.name
            is ProjectListItem.EpilogItem -> messages.getString("projectList.epilog")
            is ProjectListItem.BlurbItem -> messages.getString("projectList.blurb")
        }

        graphic = when (item) {
            is ProjectListItem.Root -> null
            is ProjectListItem.PrologItem -> AiGhostIcons.treeProlog()
            is ProjectListItem.Chapters -> AiGhostIcons.treeChapter()
            is ProjectListItem.ChapterItem -> AiGhostIcons.treeChapter()
            is ProjectListItem.EpilogItem -> AiGhostIcons.treeEpilog()
            is ProjectListItem.BlurbItem -> AiGhostIcons.treeBlurb()
        }

        contextMenu = when (item) {
            is ProjectListItem.Root -> null
            is ProjectListItem.PrologItem -> null
            is ProjectListItem.Chapters -> ContextMenu(
                MenuItem(messages.getString("projectList.menu.addChapter"), AiGhostIcons.treeChapter())
            )

            is ProjectListItem.ChapterItem -> ContextMenu(
                MenuItem(messages.getString("projectList.menu.addChapter"), AiGhostIcons.treeChapter()),
                MenuItem(messages.getString("projectList.menu.deleteChapter"))
            )

            is ProjectListItem.EpilogItem -> null
            is ProjectListItem.BlurbItem -> null
        }
    }
}
