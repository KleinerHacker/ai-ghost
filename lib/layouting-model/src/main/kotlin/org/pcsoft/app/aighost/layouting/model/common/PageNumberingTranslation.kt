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

import org.pcsoft.app.aighost.model.project.design.PageNumberCountingMode
import org.pcsoft.app.aighost.model.project.design.PageNumberDesign
import org.pcsoft.app.aighost.model.project.design.PageNumberPosition
import org.pcsoft.framework.simplay.engine.PageCountingMode
import org.pcsoft.framework.simplay.engine.model.PageNumberPosition as SimplayPageNumberPosition
import org.pcsoft.framework.simplay.engine.model.PageNumbering

/**
 * The one place where a stored page numbering becomes the [PageNumbering] the simPlay engine numbers
 * sheets with.
 *
 * @param excludedPageIds Stable ids ([org.pcsoft.framework.simplay.engine.model.Page.id]) of the
 * sheets that never carry a number - the title page and the copyright page.
 */
fun PageNumberDesign.toPageNumbering(excludedPageIds: Set<String>): PageNumbering =
    PageNumbering(
        position = position.toSimplayPosition(),
        startNumber = startNumber,
        excludedPageIds = excludedPageIds,
        counting = countingMode.toPageCountingMode(),
    )

/**
 * Translates the stored page number position into simPlay's position enum, constant for constant.
 */
fun PageNumberPosition.toSimplayPosition(): SimplayPageNumberPosition =
    when (this) {
        PageNumberPosition.OFF -> SimplayPageNumberPosition.OFF
        PageNumberPosition.TOP_LEFT -> SimplayPageNumberPosition.TOP_LEFT
        PageNumberPosition.TOP_CENTER -> SimplayPageNumberPosition.TOP_CENTER
        PageNumberPosition.TOP_RIGHT -> SimplayPageNumberPosition.TOP_RIGHT
        PageNumberPosition.TOP_INNER -> SimplayPageNumberPosition.TOP_INNER
        PageNumberPosition.TOP_OUTER -> SimplayPageNumberPosition.TOP_OUTER
        PageNumberPosition.BOTTOM_LEFT -> SimplayPageNumberPosition.BOTTOM_LEFT
        PageNumberPosition.BOTTOM_CENTER -> SimplayPageNumberPosition.BOTTOM_CENTER
        PageNumberPosition.BOTTOM_RIGHT -> SimplayPageNumberPosition.BOTTOM_RIGHT
        PageNumberPosition.BOTTOM_INNER -> SimplayPageNumberPosition.BOTTOM_INNER
        PageNumberPosition.BOTTOM_OUTER -> SimplayPageNumberPosition.BOTTOM_OUTER
    }

/**
 * Translates the stored counting mode into simPlay's counting mode enum, constant for constant.
 */
fun PageNumberCountingMode.toPageCountingMode(): PageCountingMode =
    when (this) {
        PageNumberCountingMode.CONTINUOUS -> PageCountingMode.CONTINUOUS
        PageNumberCountingMode.SKIP_EXCLUDED -> PageCountingMode.SKIP_EXCLUDED
    }
