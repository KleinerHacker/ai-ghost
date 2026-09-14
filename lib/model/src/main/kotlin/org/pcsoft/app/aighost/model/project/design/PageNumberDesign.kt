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

package org.pcsoft.app.aighost.model.project.design

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Page numbering settings of a [Design].
 *
 * The title page and the copyright page never carry a number of their own, regardless of these
 * settings; [PageNumberCountingMode] only decides whether they still advance the counter.
 *
 * @property position Where the number is printed, or [PageNumberPosition.OFF] to print none. Off by
 * default.
 * @property startNumber Display value of the first counted sheet, `1` by default.
 * @property countingMode How the title page and the copyright page affect the counter.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PageNumberDesign(
    var position: PageNumberPosition = PageNumberPosition.OFF,
    var startNumber: Int = 1,
    var countingMode: PageNumberCountingMode = PageNumberCountingMode.CONTINUOUS
)
