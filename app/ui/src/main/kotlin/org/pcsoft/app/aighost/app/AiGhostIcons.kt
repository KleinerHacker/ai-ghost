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
