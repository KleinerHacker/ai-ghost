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
