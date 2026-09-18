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
import org.pcsoft.app.aighost.fx.model.project.design.CopyrightPageDesignProperty

/**
 * View model of [CopyrightPageDesignSettings].
 *
 * The three style properties [CopyrightPageDesignProperty] carries are handled by
 * [CopyrightPageDesignSettingsView] directly, through the public API of the embedded
 * [StyleDataEditor] instances; this model only owns [showAuthor], mirroring
 * [CopyrightPageDesignProperty.showAuthorProperty] with a plain bidirectional binding.
 */
class CopyrightPageDesignSettingsViewModel : ViewModel {

    /** Whether the author name is printed on the copyright page. */
    val showAuthor: BooleanProperty = SimpleBooleanProperty(this, "showAuthor", false)

    // The model this form follows right now, so it can be released again when another one takes its
    // place.
    private var design: CopyrightPageDesignProperty? = null

    /**
     * Lets [showAuthor] follow [design], and releases the model it followed before.
     *
     * @param design the copyright page design property of the working copy
     */
    fun bind(design: CopyrightPageDesignProperty) {
        release()

        this.design = design
        showAuthor.bindBidirectional(design.showAuthorProperty)
    }

    /** Drops every binding of the current model, so it can be handed a new one. */
    internal fun release() {
        val design = this.design ?: return

        showAuthor.unbindBidirectional(design.showAuthorProperty)

        this.design = null
    }
}
