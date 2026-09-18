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
import org.pcsoft.app.aighost.model.common.StyleData

/**
 * Typographic settings for the copyright page.
 *
 * The copyright page carries the copyright notice, the further copyright lines below it and, when
 * asked for, the author name. Each of the three is set with its own style. Whether the page is
 * printed at all is decided by the book, not here.
 *
 * @property copyrightStyle Appearance of the copyright notice.
 * @property copyrightAppendixStyle Appearance of the further copyright lines.
 * @property showAuthor Whether the author name is printed on the copyright page, true by default.
 * @property authorStyle Appearance of the author name.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class CopyrightPageDesign(
    var copyrightStyle: StyleData = StyleData(),
    var copyrightAppendixStyle: StyleData = StyleData(),

    var showAuthor: Boolean = true,
    var authorStyle: StyleData = StyleData()
)
