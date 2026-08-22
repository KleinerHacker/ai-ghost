package org.pcsoft.app.aighost.model.project

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * A single chapter of a [Book].
 *
 * A chapter is the smallest unit the user writes in: it carries its heading and the written text,
 * split into paragraphs. The text may be empty while the chapter is only outlined.
 *
 * @property title Heading of the chapter as shown in the chapter list.
 * @property titleAppendix Further heading lines shown below the title, empty by default.
 * @property paragraph Paragraphs of the chapter in their order, empty by default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Chapter(
    val title: String,
    val titleAppendix: List<String> = listOf(),

    val paragraph: List<String> = emptyList()
)
