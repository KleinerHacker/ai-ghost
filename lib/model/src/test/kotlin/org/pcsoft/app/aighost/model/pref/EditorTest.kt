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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * Developer tests for [Editor].
 */
class EditorTest {

    private val mapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    /**
     * Use case: a user who never touched the settings gets the shipped typing pause, so consecutive
     * keystrokes are folded into one undo entry from the first start on without any setup.
     */
    @Test
    fun defaultsToTheShippedTypingPause() {
        val editor = Editor()

        assertEquals(600, editor.paragraphMergePauseMillis)
    }

    /**
     * Use case: the user changes the typing pause, so the new value is in effect right away without a
     * copy of the object having to be handed around.
     */
    @Test
    fun changesTheTypingPauseInPlace() {
        val editor = Editor()

        editor.paragraphMergePauseMillis = 800

        assertEquals(800, editor.paragraphMergePauseMillis)
    }

    /**
     * Use case: a caller wants the typing pause of another object without changing what it was handed,
     * so the copy carries the new value while the original keeps its own.
     */
    @Test
    fun copiesWithAChangedTypingPause() {
        val editor = Editor(paragraphMergePauseMillis = 400)

        val copy = editor.copy(paragraphMergePauseMillis = 900)

        assertEquals(900, copy.paragraphMergePauseMillis)
        assertEquals(400, editor.paragraphMergePauseMillis)
    }

    /**
     * Use case: two users configured the same typing pause, so the objects count as equal no matter
     * which one holds the settings.
     */
    @Test
    fun comparesByItsSettings() {
        val one = Editor(paragraphMergePauseMillis = 500)
        val other = Editor(paragraphMergePauseMillis = 500)

        assertEquals(one, other)
        assertEquals(one.hashCode(), other.hashCode())
    }

    /**
     * Use case: a typing pause outside the accepted range is configured, so the object refuses to be
     * built instead of driving the undo merging with a nonsensical value.
     */
    @Test
    fun rejectsATypingPauseOutsideTheAcceptedRange() {
        assertThrows(IllegalArgumentException::class.java) { Editor(paragraphMergePauseMillis = 10) }
        assertThrows(IllegalArgumentException::class.java) { Editor(paragraphMergePauseMillis = 10_000) }
    }

    /**
     * Use case: the setting is written to disk as part of the preferences, so it appears in the JSON
     * under the stable property name the file format promises.
     */
    @Test
    fun serialisesTheTypingPauseByName() {
        val json = mapper.writeValueAsString(Editor(paragraphMergePauseMillis = 700))

        assertEquals("""{"paragraphMergePauseMillis":700}""", json)
    }

    /**
     * Use case: a stored preferences file is read at start up, so the configured typing pause is
     * restored exactly as it was written.
     */
    @Test
    fun roundTripsTheTypingPause() {
        val editor = Editor(paragraphMergePauseMillis = 750)

        val restored: Editor = mapper.readValue(mapper.writeValueAsString(editor))

        assertEquals(editor, restored)
    }

    /**
     * Use case: a preferences file written by an older version does not know the setting yet, so
     * reading it falls back to the default instead of failing.
     */
    @Test
    fun readsEmptyDocumentWithDefaults() {
        val editor: Editor = mapper.readValue("{}")

        assertEquals(Editor(), editor)
    }

    /**
     * Use case: a preferences file written by a newer version carries additional properties, so
     * reading it ignores what is unknown instead of failing.
     */
    @Test
    fun ignoresUnknownProperties() {
        val editor: Editor = mapper.readValue("""{"paragraphMergePauseMillis":450,"caretBlinkRate":600}""")

        assertEquals(Editor(paragraphMergePauseMillis = 450), editor)
    }

    /**
     * Use case: the setting is part of the preferences document, so it is written and read back
     * together with the other settings.
     */
    @Test
    fun roundTripsInsidePreferences() {
        val preferences = Preferences(editor = Editor(paragraphMergePauseMillis = 550))

        val restored: Preferences = mapper.readValue(mapper.writeValueAsString(preferences))

        assertEquals(preferences, restored)
    }
}
