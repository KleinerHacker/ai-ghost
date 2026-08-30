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

package org.pcsoft.app.aighost.model.pref

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Represents configuration settings related to AI functionality in the application.
 *
 * The configuration is serialized and deserialized as JSON, with unknown properties being ignored
 * to ensure forward compatibility. Default values are provided to maintain backward compatibility.
 *
 * @property maxStoryCharacters The maximum number of characters allowed for story generation. Defaults to 5000.
 * @property maxStyleCharacters The maximum number of characters allowed for style definitions. Defaults to 1000.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Ai(
    var maxStoryCharacters: Long = 5000,
    var maxStyleCharacters: Long = 1000
)
