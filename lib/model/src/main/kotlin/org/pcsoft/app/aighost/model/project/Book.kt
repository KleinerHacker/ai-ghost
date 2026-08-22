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
 * The manuscript of a [Project]: its title and all parts that make it up.
 *
 * Every project holds a book, even an empty one that has not been written yet. The chapter order is
 * part of the data: the list is stored and read back in exactly the order the user arranged it in.
 * [prolog], [epilog] and [blurb] exist at most once and only after the user created them, so they
 * are empty until then.
 *
 * @property title Main title of the book.
 * @property titleAppendix Further title lines shown below the main title, empty by default.
 * @property prolog Prolog printed before the first chapter, absent by default.
 * @property chapters Chapters of the book in their user defined order, empty by default.
 * @property epilog Epilog printed after the last chapter, absent by default.
 * @property blurb Advertising text printed on the cover, absent by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Book(
    val title: String = "My Book",
    val titleAppendix: List<String> = listOf(),

    val prolog: Prolog? = null,
    val chapters: List<Chapter> = emptyList(),
    val epilog: Epilog? = null,
    val blurb: Blurb? = null
)
