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

package org.pcsoft.app.aighost.app.ui.component.base

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.VBox
import org.pcsoft.app.aighost.app.Messages
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [AiTextFieldList], holding the entries, the hint of the empty list and the plus.
 *
 * The entries are not laid out in the FXML, because there are as many of them as the user wants:
 * they are built here, one [AiTextFieldListItem] per entry, and follow the entries of the view
 * model. An entry is only rebuilt when one was added or removed - an entry that was merely rewritten
 * keeps its item, so the user does not lose the cursor while typing.
 *
 * The requests of the entries are stopped here: they would climb up to the list itself, where they
 * would pass as the events the list publishes and reach the outside twice.
 */
class AiTextFieldListView : FxmlView<AiTextFieldListViewModel>, Initializable {

    @FXML
    private lateinit var boxEntries: VBox

    @FXML
    private lateinit var lblEmpty: Label

    @FXML
    private lateinit var btnAdd: Button

    @FXML
    private lateinit var tipAdd: Tooltip

    @InjectViewModel
    private lateinit var viewModel: AiTextFieldListViewModel

    // The item of every entry, in the order the entries are shown in, so an item reports the
    // position it sits at.
    private val items = ArrayList<AiTextFieldListItem>()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // The items and the wording of the plus are built outside of the FXML, so they need the same
        // bundle the FXML around them was resolved with.
        val messages = resources ?: Messages.bundle
        val defaultTooltip = messages.getString(ADD_TOOLTIP_KEY)

        // Whoever shows the list names what adding means here; as long as nobody does, the plus
        // explains itself in general words.
        tipAdd.textProperty().bind(
            Bindings.createStringBinding(
                { viewModel.addTooltip.value ?: defaultTooltip },
                viewModel.addTooltip
            )
        )

        // The hint stands in for the entries, so it is only shown while there is no entry at all and
        // while whoever shows the list said what an empty list means.
        val hintStandsIn = Bindings.createBooleanBinding(
            { viewModel.entries.isEmpty() && !viewModel.emptyText.value.isNullOrEmpty() },
            viewModel.entries,
            viewModel.emptyText
        )
        lblEmpty.textProperty().bind(viewModel.emptyText)
        lblEmpty.visibleProperty().bind(hintStandsIn)
        lblEmpty.managedProperty().bind(hintStandsIn)

        btnAdd.setOnAction { event ->
            event.consume()
            viewModel.add()
        }

        viewModel.entries.addListener { _: Observable -> update() }
        update()
    }

    /**
     * Lets the items follow the entries of the view model.
     *
     * As long as there are as many items as entries, only the texts are taken over, so an item keeps
     * the field the user is writing in. An entry that was added or removed changes the number of
     * items, and the items are built anew.
     */
    private fun update() {
        if (items.size != viewModel.entries.size) {
            rebuild()
            return
        }

        for (index in items.indices) {
            val entry = viewModel.entries[index]
            if (items[index].text.value != entry) {
                items[index].text.value = entry
            }
        }
    }

    /**
     * Builds one item per entry, each holding the field of the entry and the bin removing it.
     */
    private fun rebuild() {
        items.clear()

        for (entry in viewModel.entries) {
            val item = AiTextFieldListItem()
            item.text.value = entry
            item.promptText.bind(viewModel.promptText)
            item.deleteTooltip.bind(viewModel.deleteTooltip)
            // An item is asked for its position only when the user acts, so an item that moved
            // because an entry above it was removed still reports where it sits now.
            item.text.addListener { _, _, newValue ->
                viewModel.setEntry(items.indexOf(item), newValue ?: "")
            }
            item.setOnCreateAiText { event ->
                event.consume()
                viewModel.create(items.indexOf(item))
            }
            item.setOnDeleteAction { event ->
                event.consume()
                viewModel.delete(items.indexOf(item))
            }

            items += item
        }

        boxEntries.children.setAll(items)
    }

    private companion object {
        /** Key of the general wording of the plus inside the resource bundle. */
        const val ADD_TOOLTIP_KEY: String = "component.aiTextFieldList.add.tooltip"
    }
}
