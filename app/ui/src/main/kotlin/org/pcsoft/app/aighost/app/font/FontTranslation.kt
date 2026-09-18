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

import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.framework.simplay.engine.model.Font
import org.pcsoft.framework.simplay.engine.model.FontFingerprint
import org.pcsoft.framework.simplay.engine.model.FontStyle
import org.pcsoft.framework.simplay.engine.model.FontWeight

/**
 * The way from the font of a design to the face `simplay-fx`'s `FxFontProbe` resolves and measures
 * with.
 *
 * `FxFontProbe`'s methods all take simPlay's own [Font], because a reusable renderer library must not
 * know that this application stores fonts as [FontData]. Family, size and cut are carried over as
 * they are; the stored fingerprint - encoded as a single line of text - is decoded back into simPlay's
 * [FontFingerprint] here, the only place that string is parsed on the way out of the model.
 */
fun FontData.toEngineFont(): Font =
    Font(
        family = name,
        size = size.toDouble(),
        weight = if (bold) FontWeight.BOLD else FontWeight.NORMAL,
        style = if (italic) FontStyle.ITALIC else FontStyle.NORMAL,
        fingerprint = fingerprint?.let { FontFingerprint.decode(it) },
    )
