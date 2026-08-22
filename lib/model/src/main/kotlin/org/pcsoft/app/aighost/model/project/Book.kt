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

package org.pcsoft.app.aighost.model.project

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * The manuscript of a [Project]: its title and all chapters that make it up.
 *
 * The chapter order is part of the data: the list is stored and read back in exactly the order the
 * user arranged it in.
 *
 * @property title Main title of the book.
 * @property titleAppendix Further title lines shown below the main title, empty by default.
 * @property chapters Chapters of the book in their user defined order, empty by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Book(
    val title: String,
    val titleAppendix: List<String> = listOf(),

    val chapters: List<Chapter> = emptyList()
)
