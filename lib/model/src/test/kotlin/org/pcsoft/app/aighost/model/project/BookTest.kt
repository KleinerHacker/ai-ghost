package org.pcsoft.app.aighost.model.project

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.model.TestData

/**
 * Developer tests for [Book].
 */
class BookTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: the user creates a book before writing anything, so it starts without appendix lines
     * and without chapters instead of requiring one up front.
     */
    @Test
    fun defaultsToEmptyAppendixAndChapters() {
        val book = Book("My Novel")

        assertEquals(emptyList<String>(), book.titleAppendix)
        assertEquals(emptyList<Chapter>(), book.chapters)
    }

    /**
     * Use case: a book is written to disk, so title, appendix lines and chapters appear in the JSON
     * under the stable property names the file format promises.
     */
    @Test
    fun serialisesTitleAppendixAndChapters() {
        val book = Book("My Novel", listOf("A Story"), listOf(Chapter("Prologue")))

        val json = mapper.writeValueAsString(book)

        assertEquals(
            """{"title":"My Novel","titleAppendix":["A Story"],""" +
                """"chapters":[{"title":"Prologue","titleAppendix":[],"paragraph":[]}]}""",
            json
        )
    }

    /**
     * Use case: a stored book is opened again, so all chapters come back with their content and in
     * the order the user arranged them.
     */
    @Test
    fun roundTripsChaptersInOrder() {
        val book = TestData.book()

        val restored: Book = mapper.readValue(mapper.writeValueAsString(book))

        assertEquals(book, restored)
        assertEquals(listOf("Prologue", "Epilogue"), restored.chapters.map(Chapter::title))
    }

    /**
     * Use case: a book file holds only the title, so it is read back as a book without chapters
     * instead of failing.
     */
    @Test
    fun readsDocumentWithTitleOnly() {
        val book: Book = mapper.readValue("""{"title":"My Novel"}""")

        assertEquals(Book("My Novel"), book)
    }

    /**
     * Use case: a book written by a newer version carries additional properties, so reading it
     * ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val book: Book = mapper.readValue("""{"title":"My Novel","isbn":"123"}""")

        assertEquals(Book("My Novel"), book)
    }
}
