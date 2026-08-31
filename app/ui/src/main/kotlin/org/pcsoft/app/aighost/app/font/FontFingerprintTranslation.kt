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

import org.pcsoft.app.aighost.layouting.fx.font.FontFingerprint
import org.pcsoft.app.aighost.model.common.FontMetricsData

/**
 * The two forms of a font fingerprint and the way between them.
 *
 * The component library takes the measurement and hands out its own [FontFingerprint], because a
 * reusable renderer must not know that this application stores what a manuscript was written with.
 * The project keeps the very same numbers as [FontMetricsData]. Both carry the same four values, so
 * the translation is a plain copy - it exists to keep the two worlds apart, not to convert anything.
 */

/** The measured fingerprint in the form the project stores it in. */
fun FontFingerprint.toMetricsData(): FontMetricsData =
    FontMetricsData(widths, ascent, descent, leading)

/** The stored fingerprint in the form the component library compares in. */
fun FontMetricsData.toFingerprint(): FontFingerprint =
    FontFingerprint(widths, ascent, descent, leading)
