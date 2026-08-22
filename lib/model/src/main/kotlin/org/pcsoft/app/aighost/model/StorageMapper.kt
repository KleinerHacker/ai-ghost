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

package org.pcsoft.app.aighost.model

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule

/**
 * The Jackson mapper every storage of this module reads and writes its documents with.
 *
 * Preferences and projects are stored the same way, so they share one mapper instead of configuring
 * their own: the Kotlin module makes the data classes readable with their default values, and the
 * indentation keeps every stored document editable by hand.
 *
 * The mapper is thread safe once it is built, so the storages use it as it is.
 */
internal object StorageMapper {

    /** The configured mapper, shared by all storages of this module. */
    val mapper: JsonMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .build()
}
