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

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.property.LongProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyLongProperty
import javafx.beans.property.ReadOnlyLongWrapper
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.pcsoft.app.aighost.ai.util.TokenUtils

/**
 * View model of [AiPromptArea].
 *
 * [text] and [maxCharacters] are the two inputs the surrounding view sets. Everything the footer
 * shows is derived from them: how many characters are written, how many tokens they are expected to
 * cost and how close the prompt has come to its limit.
 */
class AiPromptAreaViewModel : ViewModel {

    /** The prompt the user wrote, empty while nothing has been typed. */
    val text: StringProperty = SimpleStringProperty(this, "text", "")

    /** Text shown while the prompt is empty, absent when no hint is wanted. */
    val promptText: StringProperty = SimpleStringProperty(this, "promptText", null)

    /** Number of characters the prompt may hold, zero or less for no limit at all. */
    val maxCharacters: LongProperty = SimpleLongProperty(this, "maxCharacters", 0L)

    private val lengthWrapper: ReadOnlyLongWrapper = ReadOnlyLongWrapper(this, "length", 0L)
    private val tokensWrapper: ReadOnlyLongWrapper = ReadOnlyLongWrapper(this, "tokens", 0L)
    private val usageWrapper: ReadOnlyObjectWrapper<AiPromptUsage> =
        ReadOnlyObjectWrapper(this, "usage", AiPromptUsage.NORMAL)
    private val limitedWrapper: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(this, "limited", false)

    /** Number of characters the prompt holds. */
    val length: ReadOnlyLongProperty get() = lengthWrapper.readOnlyProperty

    /** Estimated number of tokens the prompt costs, calculated by [TokenUtils]. */
    val tokens: ReadOnlyLongProperty get() = tokensWrapper.readOnlyProperty

    /** How far the prompt has eaten into the characters it is allowed to use. */
    val usage: ReadOnlyObjectProperty<AiPromptUsage> get() = usageWrapper.readOnlyProperty

    /** Whether the prompt carries a limit at all, which is what the counter is shown for. */
    val limited: ReadOnlyBooleanProperty get() = limitedWrapper.readOnlyProperty

    /**
     * Called when the user asks for the prompt to be optimised.
     *
     * Set by [AiPromptArea], which turns the request into the event the outside world listens to.
     */
    internal var onOptimize: (() -> Unit)? = null

    init {
        text.addListener { _, _, _ -> update() }
        maxCharacters.addListener { _, _, _ -> update() }
        update()
    }

    /**
     * Passes the request of the user on to [AiPromptArea].
     *
     * Called by [AiPromptAreaView] only.
     */
    internal fun optimize() {
        onOptimize?.invoke()
    }

    /**
     * Cuts a text down to the number of characters the prompt is allowed to hold.
     *
     * Called by [AiPromptAreaView] while the user types, so that text pasted in one go is limited
     * the same way as text typed character by character.
     *
     * @param value the text the user wants the prompt to hold
     * @return the text as it fits into the prompt
     */
    internal fun limit(value: String): String {
        val max = maxCharacters.get()
        if (max <= 0L || value.length <= max) {
            return value
        }

        return value.substring(0, max.toInt())
    }

    private fun update() {
        val value = text.value ?: ""

        // A limit set after the fact applies to what is already written, so the prompt never holds
        // more than it reports. Writing the cut text back runs this method again on a fitting text.
        val fitting = limit(value)
        if (fitting.length < value.length) {
            text.value = fitting
            return
        }

        lengthWrapper.set(fitting.length.toLong())
        tokensWrapper.set(TokenUtils.estimateTokens(fitting))
        limitedWrapper.set(maxCharacters.get() > 0L)
        usageWrapper.set(AiPromptUsage.of(fitting.length.toLong(), maxCharacters.get()))
    }
}
