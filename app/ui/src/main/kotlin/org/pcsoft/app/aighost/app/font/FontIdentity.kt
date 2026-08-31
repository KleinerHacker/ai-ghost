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

import org.pcsoft.app.aighost.layouting.fx.font.FontFingerprints
import org.pcsoft.app.aighost.model.common.FontData
import org.pcsoft.app.aighost.model.common.FontMetricsData

/**
 * Whether the font a piece of text was written in is the font it is set in here.
 *
 * A stored font is a family name, and a family name is not an identity: two machines can both know a
 * `Garamond` and still set it differently, and a machine that knows none at all falls back to a
 * substitute. The fingerprint stored beside the name answers the question the name cannot.
 *
 * A font that was never fingerprinted is [Unknown] and never a deviation: a project written before
 * the fingerprint existed must not report a mismatch against every machine in the world.
 */
sealed interface FontIdentity {

    /** The font carries no fingerprint, so nothing can be said about it. */
    data object Unknown : FontIdentity

    /** The family is installed and measures exactly as it did when the project was written. */
    data object Matches : FontIdentity

    /**
     * The family is not installed at all and the fallback chain picked another one.
     *
     * @property requestedFamily Family the project was written in.
     * @property substituteFamily Family that is set instead.
     */
    data class Substituted(
        val requestedFamily: String,
        val substituteFamily: String
    ) : FontIdentity

    /**
     * The family is installed under the name the project asks for, but measures differently.
     *
     * Another version of the face, another file behind the same name, or a different rendering of
     * the platform - the cause is not knowable from here, and the consequence is the same in every
     * case: lines and pages break elsewhere.
     *
     * @property family Family the project was written in and that is set.
     * @property stored Fingerprint the project carries.
     * @property measured Fingerprint this machine takes today.
     */
    data class Deviates(
        val family: String,
        val stored: FontMetricsData,
        val measured: FontMetricsData
    ) : FontIdentity

    companion object {

        /**
         * Compares the fingerprint stored on [data] against the one this machine takes.
         *
         * **Threading:** the comparison measures, so it must run on the JavaFX application thread.
         *
         * @param data Font of a design, as it is stored in the project.
         */
        fun of(data: FontData): FontIdentity {
            val stored = data.metrics ?: return Unknown

            return when (val resolution = FontResolver.resolve(data)) {
                is FontResolution.NotInstalled ->
                    Substituted(resolution.requestedFamily, resolution.substituteFamily)

                is FontResolution.Installed -> {
                    // The catalogue and the measuring agree on the installed families in every
                    // normal case; when they do not, the family cannot be measured and is therefore
                    // not the one the project was written in either.
                    val measured = FontFingerprints.of(data.name)?.toMetricsData()
                        ?: return Substituted(data.name, resolution.font.family)

                    if (measured == stored) Matches else Deviates(data.name, stored, measured)
                }
            }
        }
    }
}
