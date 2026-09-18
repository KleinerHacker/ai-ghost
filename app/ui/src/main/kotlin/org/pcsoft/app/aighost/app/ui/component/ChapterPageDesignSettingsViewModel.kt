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

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import org.pcsoft.app.aighost.fx.model.project.design.ChapterPageDesignProperty

/**
 * View model of [ChapterPageDesignSettings].
 *
 * The three styles are handed straight to a [StyleDataEditor] each by
 * [ChapterPageDesignSettingsView.bindEditors] - this model does not shadow their fields, it only
 * carries [titleOnSeparatePage], bound bidirectionally to
 * [ChapterPageDesignProperty.titleOnSeparatePageProperty]. The model object it edits is handed in
 * through [bind]; the component owns no state of its own.
 */
class ChapterPageDesignSettingsViewModel : ViewModel {

    /** Whether the chapter heading stands on a page of its own. */
    val titleOnSeparatePage: BooleanProperty = SimpleBooleanProperty(this, "titleOnSeparatePage", false)

    // The model this form follows right now, so it can be released again when another one takes its
    // place.
    private var design: ChapterPageDesignProperty? = null

    /**
     * Lets the form follow [design] and releases the model it followed before.
     *
     * @param design the design property of the working copy
     */
    fun bind(design: ChapterPageDesignProperty) {
        release()

        this.design = design
        titleOnSeparatePage.bindBidirectional(design.titleOnSeparatePageProperty)
    }

    /** Drops every binding of the current model, so it can be handed a new one. */
    internal fun release() {
        val design = this.design ?: return

        titleOnSeparatePage.unbindBidirectional(design.titleOnSeparatePageProperty)

        this.design = null
    }
}
