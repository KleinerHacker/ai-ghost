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

package org.pcsoft.app.aighost.app.font

import org.pcsoft.app.aighost.layouting.fx.font.FontDescription
import org.pcsoft.app.aighost.model.common.FontData

/**
 * The way from the font of a design to the face the renderer library resolves and measures with.
 *
 * The component library takes its input as its own [FontDescription], because a reusable renderer
 * must not know that this application stores fonts as [FontData]. Both carry the same family, size
 * and cut, so the translation is a plain copy - it exists to keep the two worlds apart, not to
 * convert anything. The measurement fingerprint on [FontData] is not part of a description and is
 * dropped here.
 */
fun FontData.toFontDescription(): FontDescription =
    FontDescription(name, size, bold, italic)
