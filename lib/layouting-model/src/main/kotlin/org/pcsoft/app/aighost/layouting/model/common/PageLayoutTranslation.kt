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

import org.pcsoft.app.aighost.model.project.design.PageFormat
import org.pcsoft.framework.simplay.engine.geometry.Margins
import org.pcsoft.framework.simplay.engine.geometry.Size
import org.pcsoft.framework.simplay.engine.model.PageLayout

/**
 * The one place where a stored page format becomes the [PageLayout] the simPlay engine paginates
 * onto.
 *
 * [PageFormat] names its horizontal margins after the binding - an inner margin at the spine and an
 * outer margin at the open edge - and can mirror them between an odd and an even page. simPlay's
 * [Margins] are plain left/top/right/bottom and are the same for every page. The inner margin is
 * mapped to the left, the outer margin to the right.
 *
 * TODO(simPlay page policy): [PageFormat.mirroredMargins] cannot be expressed - simPlay has no
 *  recto/verso notion, so every page carries these same margins. The alternation moves into simPlay
 *  and this translation adopts it from there.
 */
fun PageFormat.toPageLayout(): PageLayout =
    PageLayout(
        size = Size(width = width, height = height),
        margins = Margins(
            left = innerMargin,
            top = topMargin,
            right = outerMargin,
            bottom = bottomMargin,
        ),
    )
