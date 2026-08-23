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

package org.pcsoft.app.aighost.fx.model

import arrow.core.Either
import org.pcsoft.app.aighost.fx.model.pref.PreferencesProperty
import org.pcsoft.app.aighost.model.PreferencesStorage

/**
 * JavaFX wrapper for [PreferencesStorage] that provides the current preferences as a property
 * and delegates load, save, and reset operations to the underlying storage while keeping the
 * property synchronized.
 */
object FXPreferencesStorage {
    /**
     * Property holding the current preferences of the user and offering every field of that object
     * as a property of its own. Automatically synchronized when preferences are loaded or reset.
     */
    val current: PreferencesProperty = PreferencesProperty(PreferencesStorage.current)

    /**
     * Loads the preferences from persistent storage and updates the [current] property with the
     * loaded values.
     *
     * @return Either [PreferencesStorage.Error] if loading failed or [Unit] on success
     */
    fun load(): Either<PreferencesStorage.Error, Unit> = PreferencesStorage.load().onRight {
        current.set(PreferencesStorage.current)
    }

    /**
     * Resets the preferences to their default values and updates the [current] property accordingly.
     */
    fun reset() = PreferencesStorage.reset().run { 
        current.set(PreferencesStorage.current)
    }

    /**
     * Saves the current preferences to persistent storage.
     *
     * @return Either [PreferencesStorage.Error] if saving failed or [Unit] on success
     */
    fun save(): Either<PreferencesStorage.Error, Unit> = PreferencesStorage.save()

}