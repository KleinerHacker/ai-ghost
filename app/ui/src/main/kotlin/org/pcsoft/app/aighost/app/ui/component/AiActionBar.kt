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

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.scene.layout.HBox

/**
 * Floating bar of AI actions - Rewrite, Expand, Shorten - shown next to the focused paragraph or
 * heading of the writing surface (IP-18).
 *
 * The bar is placed as the [content][org.pcsoft.framework.simplay.fx.FloatingOverlay.content] of a
 * `FloatingOverlay` registered on `PaperSheetView`; simPlay itself decides when it is shown,
 * positioned and hidden. The bar owns no project data and every one of its buttons ends at an
 * unimplemented `*View` method, so there is nothing to bind here.
 */
class AiActionBar : HBox() {

    init {
        FluentViewLoader.fxmlView(AiActionBarView::class.java).let {
            it.root(this)
            it.load()
        }
    }
}
