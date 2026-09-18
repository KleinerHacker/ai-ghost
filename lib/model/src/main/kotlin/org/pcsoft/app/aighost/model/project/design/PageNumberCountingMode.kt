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

/**
 * How an unnumbered sheet - the title page or the copyright page - affects the count of the sheets
 * around it.
 *
 * The value is part of a [PageNumberDesign] and is written to JSON by its constant name, so a stored
 * document stays readable when new constants are added.
 */
enum class PageNumberCountingMode {

    /** Every sheet advances the counter, including an unnumbered one; only its own label is suppressed. */
    CONTINUOUS,

    /**
     * An unnumbered sheet shows no label and does not advance the counter, so the following sheet gets
     * the number the unnumbered one would have carried.
     */
    SKIP_EXCLUDED
}
