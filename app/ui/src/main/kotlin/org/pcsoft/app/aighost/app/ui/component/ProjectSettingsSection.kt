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

package org.pcsoft.app.aighost.app.ui.component

/**
 * One section of the project settings, shown as a node of the [ProjectSettingsTree] and picked by
 * the user to bring its editor to the front of the dialog.
 *
 * Modelled after [ProjectListItem]: a sealed hierarchy the tree tells apart in an exhaustive `when`
 * instead of comparing labels. The nodes are fixed objects today; a section that later stands for a
 * single book part becomes a `data class` carrying that part, the same way [ProjectListItem.ChapterItem]
 * does, without changing how the tree is built.
 *
 * Every design section now carries a real editor - the page geometry and the two empty pages under
 * [Design], and the typography of each page under its own child section. Only [General] still shows a
 * placeholder.
 */
sealed interface ProjectSettingsSection {

    /** Key of the node label in the message bundle. */
    val bundleKey: String

    /** Whether this section already carries a real editor instead of a placeholder. */
    val implemented: Boolean get() = false

    /** Global project settings; a placeholder until it carries its own options. */
    data object General : ProjectSettingsSection {
        override val bundleKey: String get() = "dialog.projectSettings.section.general"
    }

    /** Page format and the empty pages at the start and the end of the book. */
    data object Design : ProjectSettingsSection {
        override val bundleKey: String get() = "dialog.projectSettings.section.design"
        override val implemented: Boolean get() = true
    }

    /** Typography of the title page. */
    data object DesignTitle : ProjectSettingsSection {
        override val bundleKey: String get() = "dialog.projectSettings.section.design.title"
        override val implemented: Boolean get() = true
    }

    /** Typography of the copyright page. */
    data object DesignCopyright : ProjectSettingsSection {
        override val bundleKey: String get() = "dialog.projectSettings.section.design.copyright"
        override val implemented: Boolean get() = true
    }

    /** Typography of the epilog. */
    data object DesignEpilog : ProjectSettingsSection {
        override val bundleKey: String get() = "dialog.projectSettings.section.design.epilog"
        override val implemented: Boolean get() = true
    }

    /** Typography of the chapters. */
    data object DesignChapter : ProjectSettingsSection {
        override val bundleKey: String get() = "dialog.projectSettings.section.design.chapter"
        override val implemented: Boolean get() = true
    }

    /** Typography of the prolog. */
    data object DesignProlog : ProjectSettingsSection {
        override val bundleKey: String get() = "dialog.projectSettings.section.design.prolog"
        override val implemented: Boolean get() = true
    }

    /** Typography of the blurb. */
    data object DesignBlurb : ProjectSettingsSection {
        override val bundleKey: String get() = "dialog.projectSettings.section.design.blurb"
        override val implemented: Boolean get() = true
    }
}
