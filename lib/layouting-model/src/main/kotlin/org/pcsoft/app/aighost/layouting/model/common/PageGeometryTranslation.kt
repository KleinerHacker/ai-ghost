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
 * The one place where a stored page format becomes the page geometry the layout core paginates onto.
 *
 * The two types look alike but belong to different worlds, the same way [PageFormat] and
 * [PageGeometry] mirror [org.pcsoft.app.aighost.model.common.StyleData] and
 * [org.pcsoft.app.aighost.layouting.TextStyle]. The translation is written as an extension of the
 * stored type, the same way the style translation is.
 */

/**
 * Translates a stored page format into the page geometry of the layout core, a plain field-by-field
 * copy that only exists to keep the two worlds apart.
 */
fun PageFormat.toPageGeometry(): PageGeometry =
    PageGeometry(
        width = width,
        height = height,
        innerMargin = innerMargin,
        outerMargin = outerMargin,
        topMargin = topMargin,
        bottomMargin = bottomMargin,
        mirroredMargins = mirroredMargins
    )
