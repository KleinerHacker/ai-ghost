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

package org.pcsoft.app.aighost.layouting.model.common

import org.pcsoft.app.aighost.layouting.PageGeometry
import org.pcsoft.app.aighost.model.project.design.PageFormat

/**
 * The one place where a stored page format becomes the page geometry [org.pcsoft.app.aighost.layouting.LayoutEngine]
 * paginates onto.
 *
 * The two types look alike but belong to different worlds, the same way [PageFormat] and
 * [PageGeometry] mirror [org.pcsoft.app.aighost.model.common.StyleData] and
 * [org.pcsoft.app.aighost.layouting.TextStyle].
 */
object PageGeometryTranslation {

    /**
     * Translates a stored page format into the page geometry of the layout core.
     *
     * @param pageFormat The stored page format of the design.
     */
    fun toPageGeometry(pageFormat: PageFormat): PageGeometry =
        PageGeometry(
            width = pageFormat.width,
            height = pageFormat.height,
            innerMargin = pageFormat.innerMargin,
            outerMargin = pageFormat.outerMargin,
            topMargin = pageFormat.topMargin,
            bottomMargin = pageFormat.bottomMargin,
            mirroredMargins = pageFormat.mirroredMargins
        )
}
