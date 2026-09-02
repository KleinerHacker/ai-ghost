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

package org.pcsoft.app.aighost.app

import javafx.scene.image.Image
import javafx.scene.image.ImageView

/**
 * Central access point for every icon shipped with the application.
 *
 * All icons live below `app/ui/src/main/resources/icons` and are loaded lazily on first access, so
 * that no image is decoded before it is actually shown.
 */
object AiGhostIcons {

    /** Default edge length in pixels used when an icon is rendered into a menu. */
    const val MENU_ICON_SIZE: Double = 16.0

    /** Edge length in pixels the menu icons are stored in. */
    const val MENU_ICON_STORED_SIZE: Int = 32

    /** Default edge length in pixels used when an icon is rendered into a tree node. */
    const val TREE_ICON_SIZE: Double = 16.0

    /** Default edge length in pixels used when an icon is rendered into a dialog. */
    const val DIALOG_ICON_SIZE: Double = 48.0

    /** Edge length in pixels the dialog icons are stored in. */
    const val DIALOG_ICON_STORED_SIZE: Int = 48

    /** Edge lengths in pixels for which the application icon is available. */
    val APPLICATION_ICON_SIZES: List<Int> = listOf(16, 24, 32, 48, 64, 128, 256, 512)

    /** The application icon in every available size, ordered from smallest to largest. */
    val application: List<Image> by lazy { APPLICATION_ICON_SIZES.map { load("app", it) } }

    /** Icon for creating an epilog. */
    val epilog: Image by lazy { load("epilog") }

    /** Icon for creating a prolog. */
    val prolog: Image by lazy { load("prolog") }

    /** Icon for creating a chapter. */
    val chapter: Image by lazy { load("chapter") }

    /** Icon for creating a blurb. */
    val blurb: Image by lazy { load("blurb") }

    /** Icon for opening a project. */
    val open: Image by lazy { load("open") }

    /** Icon for saving the current project. */
    val save: Image by lazy { load("save") }

    /** Icon for saving the current project under a new name. */
    val saveAs: Image by lazy { load("save-as") }

    /** Icon for the application preferences. */
    val preferences: Image by lazy { load("preferences") }

    /** Icon for the project settings. */
    val projectSettings: Image by lazy { load("project-settings") }

    /** Icon for the export menu. */
    val export: Image by lazy { load("export") }

    /** Icon for the online help. */
    val helpOnline: Image by lazy { load("help-online") }

    /** Icon for handing a text over to the AI, be it to write it or to improve its wording. */
    val aiAction: Image by lazy { load("ai-action") }

    /** Icon for adding another entry to a list the user builds up. */
    val add: Image by lazy { load("add") }

    /** Icon for removing a single entry from a list the user builds up. */
    val remove: Image by lazy { load("remove") }

    /** Icon for undoing the most recent change. */
    val undo: Image by lazy { load("undo") }

    /** Icon for redoing the most recently undone change. */
    val redo: Image by lazy { load("redo") }

    /**
     * Creates the button graphic for handing a text over to the AI, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [aiAction]
     */
    @JvmStatic
    fun buttonAiAction(): ImageView = aiAction.toImageView()

    /**
     * Creates the button graphic for adding an entry, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [add]
     */
    @JvmStatic
    fun buttonAdd(): ImageView = add.toImageView()

    /**
     * Creates the button graphic for removing an entry, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [remove]
     */
    @JvmStatic
    fun buttonRemove(): ImageView = remove.toImageView()

    /**
     * Creates the menu graphic for creating an epilog, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [epilog]
     */
    @JvmStatic
    fun menuEpilog(): ImageView = epilog.toImageView()

    /**
     * Creates the menu graphic for creating a prolog, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [prolog]
     */
    @JvmStatic
    fun menuProlog(): ImageView = prolog.toImageView()

    /**
     * Creates the menu graphic for creating a chapter, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [chapter]
     */
    @JvmStatic
    fun menuChapter(): ImageView = chapter.toImageView()

    /**
     * Creates the menu graphic for creating a blurb, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [blurb]
     */
    @JvmStatic
    fun menuBlurb(): ImageView = blurb.toImageView()

    /**
     * Creates the menu graphic for undoing the most recent change, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [undo]
     */
    @JvmStatic
    fun menuUndo(): ImageView = undo.toImageView()

    /**
     * Creates the menu graphic for redoing the most recently undone change, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [redo]
     */
    @JvmStatic
    fun menuRedo(): ImageView = redo.toImageView()

    /**
     * Creates the menu graphic for opening a project, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [open]
     */
    @JvmStatic
    fun menuOpen(): ImageView = open.toImageView()

    /**
     * Creates the menu graphic for saving the current project, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [save]
     */
    @JvmStatic
    fun menuSave(): ImageView = save.toImageView()

    /**
     * Creates the menu graphic for saving the current project under a new name, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [saveAs]
     */
    @JvmStatic
    fun menuSaveAs(): ImageView = saveAs.toImageView()

    /**
     * Creates the menu graphic for the application preferences, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [preferences]
     */
    @JvmStatic
    fun menuPreferences(): ImageView = preferences.toImageView()

    /**
     * Creates the menu graphic for the project settings, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [projectSettings]
     */
    @JvmStatic
    fun menuProjectSettings(): ImageView = projectSettings.toImageView()

    /**
     * Creates the menu graphic for the export menu, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [export]
     */
    @JvmStatic
    fun menuExport(): ImageView = export.toImageView()

    /**
     * Creates the menu graphic for the online help, scaled to [MENU_ICON_SIZE].
     *
     * Referenced from FXML through `fx:factory`.
     *
     * @return a new image view showing [helpOnline]
     */
    @JvmStatic
    fun menuHelpOnline(): ImageView = helpOnline.toImageView()

    /**
     * Creates the tree graphic for the prolog, scaled to [TREE_ICON_SIZE].
     *
     * @return a new image view showing [prolog]
     */
    @JvmStatic
    fun treeProlog(): ImageView = prolog.toImageView(TREE_ICON_SIZE)

    /**
     * Creates the tree graphic for a chapter, scaled to [TREE_ICON_SIZE].
     *
     * @return a new image view showing [chapter]
     */
    @JvmStatic
    fun treeChapter(): ImageView = chapter.toImageView(TREE_ICON_SIZE)

    /**
     * Creates the tree graphic for the epilog, scaled to [TREE_ICON_SIZE].
     *
     * @return a new image view showing [epilog]
     */
    @JvmStatic
    fun treeEpilog(): ImageView = epilog.toImageView(TREE_ICON_SIZE)

    /**
     * Creates the tree graphic for the blurb, scaled to [TREE_ICON_SIZE].
     *
     * @return a new image view showing [blurb]
     */
    @JvmStatic
    fun treeBlurb(): ImageView = blurb.toImageView(TREE_ICON_SIZE)

    /**
     * Reads the icon of an error dialog for the given colour scheme.
     *
     * The dialog icons are the only ones drawn twice, because they sit on a coloured surface and
     * would lose their contrast in the other scheme.
     *
     * @param scheme colour scheme the icon is shown in, the one of the theme by default
     * @return the loaded image
     */
    @JvmStatic
    @JvmOverloads
    fun error(scheme: AiGhostColorScheme = AiGhostTheme.colorScheme): Image = themed("error", scheme)

    /**
     * Reads the icon of a warning dialog for the given colour scheme.
     *
     * @param scheme colour scheme the icon is shown in, the one of the theme by default
     * @return the loaded image
     */
    @JvmStatic
    @JvmOverloads
    fun warning(scheme: AiGhostColorScheme = AiGhostTheme.colorScheme): Image = themed("warning", scheme)

    /**
     * Loads a themed icon, which is shipped once per colour scheme.
     *
     * The dark variant of an icon carries the suffix `-dark`, the light one the plain name. Loaded
     * images are kept, so showing a dialog again does not decode its icon a second time.
     *
     * @param name name of the icon without variant, size and extension
     * @param scheme colour scheme the icon is shown in
     * @return the loaded image
     */
    private fun themed(name: String, scheme: AiGhostColorScheme): Image =
        themedIcons.getOrPut(name to scheme) {
            val variant = if (scheme == AiGhostColorScheme.DARK) "$name-dark" else name
            load(variant, DIALOG_ICON_STORED_SIZE)
        }

    /** Themed icons already decoded, keyed by name and colour scheme. */
    private val themedIcons: MutableMap<Pair<String, AiGhostColorScheme>, Image> = mutableMapOf()

    /**
     * Loads an icon from the `icons` resource folder, which names its files `<name>@<size>.png`.
     *
     * @param name name of the icon without size and extension, for example `save`
     * @param size edge length in pixels, defaults to [MENU_ICON_STORED_SIZE]
     * @return the loaded image
     * @throws IllegalArgumentException if no resource exists for the given name and size
     */
    private fun load(name: String, size: Int = MENU_ICON_STORED_SIZE): Image {
        val path = "/icons/$name@$size.png"
        val stream = requireNotNull(AiGhostIcons::class.java.getResourceAsStream(path)) {
            "Icon resource not found: $path"
        }
        return stream.use { Image(it) }
    }
}

/**
 * Wraps this image into an [ImageView] scaled to a square of the given edge length.
 *
 * Implementation detail of [AiGhostIcons]: consumers use its `menu*` factory methods instead.
 *
 * @param size edge length in pixels, defaults to [AiGhostIcons.MENU_ICON_SIZE]
 * @return a new image view showing this image
 */
private fun Image.toImageView(size: Double = AiGhostIcons.MENU_ICON_SIZE): ImageView =
    ImageView(this).apply {
        fitWidth = size
        fitHeight = size
        isPreserveRatio = true
        isSmooth = true
    }
