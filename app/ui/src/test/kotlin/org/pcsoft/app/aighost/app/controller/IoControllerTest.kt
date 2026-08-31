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

package org.pcsoft.app.aighost.app.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.aighost.app.Messages
import org.pcsoft.app.aighost.app.font.FontIdentity
import org.pcsoft.app.aighost.app.font.FontIdentityCheck
import org.pcsoft.app.aighost.model.common.FontMetricsData
import org.pcsoft.app.aighost.model.PreferencesStorage
import org.pcsoft.app.aighost.model.ProjectStorage
import org.pcsoft.app.aighost.model.pref.ThemeMode
import org.pcsoft.app.aighost.model.project.Project
import java.io.File
import java.io.IOException

/**
 * Developer tests for [IoController].
 *
 * Everything the controller does around a failure ends in a dialog and needs a running user
 * interface, so what is checked here is the part that decides what the user is told: which text a
 * reported failure is turned into. Reading a text that is not in the bundle fails on the spot, so
 * these tests also prove that every key the controller names exists.
 */
class IoControllerTest {

    private val file = File("project.aig")

    /**
     * Use case: the settings belong to the process and not to a window, so the controller carries them
     * as a property model whose fields reach the value object the application works on.
     */
    @Test
    fun offersTheSettingsAsAPropertyModel() {
        val preferences = IoController.preferences.value
        val stored = preferences.appearance.themeMode

        try {
            IoController.preferences.appearanceProperty.themeModeProperty.value = ThemeMode.DARK

            assertEquals(ThemeMode.DARK, preferences.appearance.themeMode)
        } finally {
            IoController.preferences.appearanceProperty.themeModeProperty.value = stored
        }
    }

    /**
     * Use case: a recently opened project was deleted meanwhile, so the dialog says the file is gone
     * rather than blaming its content.
     */
    @Test
    fun namesTheMissingProjectFile() {
        assertEquals(
            Messages["text.project.error.notFound"],
            IoController.reasonOfProjectError(ProjectStorage.Error.NotFound(file))
        )
    }

    /**
     * Use case: a directory sits where the project file is expected, so the dialog says so instead of
     * reporting the project as damaged.
     */
    @Test
    fun namesADirectoryInPlaceOfTheProjectFile() {
        assertEquals(
            Messages["text.project.error.notAFile"],
            IoController.reasonOfProjectError(ProjectStorage.Error.NotAFile(file))
        )
    }

    /**
     * Use case: the project file cannot be read or written, because of its permissions for instance,
     * so the dialog points at the file rather than at its content.
     */
    @Test
    fun namesTheUnreadableProjectFile() {
        assertEquals(
            Messages["text.project.error.unreadable"],
            IoController.reasonOfProjectError(ProjectStorage.Error.Unreadable(file, IOException("denied")))
        )
    }

    /**
     * Use case: the project file was damaged by another program, so the dialog says the content is
     * not the expected document.
     */
    @Test
    fun namesTheDamagedProjectFile() {
        assertEquals(
            Messages["text.project.error.malformed"],
            IoController.reasonOfProjectError(ProjectStorage.Error.Malformed(file, IOException("broken")))
        )
    }

    /**
     * Use case: the preferences file does not exist yet, which is the normal first start, so the text
     * for that case is its own instead of the generic failure.
     */
    @Test
    fun namesTheMissingPreferencesFile() {
        assertEquals(
            Messages["text.preferences.error.notFound"],
            IoController.reasonOfPreferencesError(PreferencesStorage.Error.NotFound(file))
        )
    }

    /**
     * Use case: a directory sits where the preferences file is expected - the application must not
     * remove it on its own, and the failure that stops the application names its own reason.
     */
    @Test
    fun namesADirectoryInPlaceOfThePreferencesFile() {
        assertEquals(
            Messages["text.preferences.error.notAFile"],
            IoController.reasonOfPreferencesError(PreferencesStorage.Error.NotAFile(file))
        )
    }

    /**
     * Use case: the preferences hold content the application cannot understand, so the text says the
     * settings are damaged and not that they are missing.
     */
    @Test
    fun namesTheDamagedPreferencesFile() {
        assertEquals(
            Messages["text.preferences.error.malformed"],
            IoController.reasonOfPreferencesError(PreferencesStorage.Error.Malformed(file, IOException("broken")))
        )
    }

    /**
     * Use case: the preferences file cannot be read, because of its permissions for instance, so the
     * text points at the file rather than at its content.
     */
    @Test
    fun namesTheUnreadablePreferencesFile() {
        assertEquals(
            Messages["text.preferences.error.unreadable"],
            IoController.reasonOfPreferencesError(PreferencesStorage.Error.Unreadable(file, IOException("denied")))
        )
    }

    /**
     * Use case: the project file does not hold every part a project is made of, so the dialog says
     * the project is corrupt.
     */
    @Test
    fun namesTheCorruptProject() {
        assertEquals(
            Messages["text.project.error.corrupt"],
            IoController.reasonOfProjectError(ProjectStorage.Error.Corrupt(file, setOf("book")))
        )
    }

    /**
     * Use case: the project file lost a part beyond the standard ones, so the dialog says the project
     * lost content instead of calling the whole project corrupt.
     */
    @Test
    fun namesTheIncompleteProject() {
        assertEquals(
            Messages["text.project.error.incomplete"],
            IoController.reasonOfProjectError(incompleteError())
        )
    }

    /**
     * Use case: the user is asked whether an incomplete project may be opened anyway, so every text
     * of that dialog is in the bundle - reading a missing key fails right here - and no two of them
     * say the same thing.
     */
    @Test
    fun carriesEveryTextOfTheRescueDialog() {
        val texts = listOf(
            Messages["text.project.incomplete.title"],
            Messages["text.project.incomplete.header"],
            Messages["text.project.incomplete.content"],
            Messages["text.project.incomplete.hint"]
        )

        assertEquals(texts.size, texts.toSet().size, "two texts of the rescue dialog are the same")
    }

    /**
     * Use case: the dialogs of two failures are told apart by their text, so no two failures of the
     * same storage are described with one and the same sentence.
     */
    @Test
    fun tellsEveryFailureApartByItsText() {
        val projectReasons = listOf(
            ProjectStorage.Error.NotFound(file),
            ProjectStorage.Error.NotAFile(file),
            ProjectStorage.Error.Unreadable(file, IOException("denied")),
            ProjectStorage.Error.Malformed(file, IOException("broken")),
            ProjectStorage.Error.Corrupt(file, setOf("book")),
            incompleteError()
        ).map { IoController.reasonOfProjectError(it) }

        val preferencesReasons = listOf(
            PreferencesStorage.Error.NotFound(file),
            PreferencesStorage.Error.NotAFile(file),
            PreferencesStorage.Error.Unreadable(file, IOException("denied")),
            PreferencesStorage.Error.Malformed(file, IOException("broken"))
        ).map { IoController.reasonOfPreferencesError(it) }

        assertEquals(projectReasons.size, projectReasons.toSet().size, "two project failures share one text")
        assertEquals(
            preferencesReasons.size,
            preferencesReasons.toSet().size,
            "two preferences failures share one text"
        )
    }

    /**
     * Use case: the family a manuscript was written in is missing on this machine. The line the user
     * reads names the element, the font the project asks for and the font that is set instead.
     */
    @Test
    fun namesTheSubstituteOfAFontThatIsNotInstalled() {
        val line = IoController.describe(
            FontIdentityCheck.Finding(
                FontIdentityCheck.ELEMENT_TEXT,
                FontIdentity.Substituted("Garamond", "Arial")
            )
        )

        assertTrue(line.contains(Messages[FontIdentityCheck.ELEMENT_TEXT]), "the element is named")
        assertTrue(line.contains("Garamond"), "the font of the project is named")
        assertTrue(line.contains("Arial"), "the font that is set instead is named")
    }

    /**
     * Use case: the family is installed under the name of the project but sets differently. There is
     * no substitute to name, so the line names the element and that one font only.
     */
    @Test
    fun namesTheFontThatSetsDifferently() {
        val line = IoController.describe(
            FontIdentityCheck.Finding(
                FontIdentityCheck.ELEMENT_TITLE,
                FontIdentity.Deviates(
                    "Garamond",
                    FontMetricsData("0123456789abcdef", 11.0, 3.0, 0.0),
                    FontMetricsData("fedcba9876543210", 11.5, 3.0, 0.0)
                )
            )
        )

        assertTrue(line.contains(Messages[FontIdentityCheck.ELEMENT_TITLE]), "the element is named")
        assertTrue(line.contains("Garamond"), "the font of the project is named")
    }

    /** A project that lost one part beyond the standard ones, the rescued project beside it. */
    private fun incompleteError(): ProjectStorage.Error.Incomplete =
        ProjectStorage.Error.Incomplete(file, setOf("outline"), Project())
}
