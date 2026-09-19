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

package org.pcsoft.app.aighost.plugin.manager

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PluginClassRequirementsTest {

    interface RequiredType

    @Retention(AnnotationRetention.RUNTIME)
    annotation class FirstRequiredAnnotation

    @Retention(AnnotationRetention.RUNTIME)
    annotation class SecondRequiredAnnotation

    @FirstRequiredAnnotation
    @SecondRequiredAnnotation
    class Compliant : RequiredType

    class NotImplementingRequiredType

    class MissingBothAnnotations : RequiredType

    @FirstRequiredAnnotation
    class MissingOneAnnotation : RequiredType

    private val classLoader = javaClass.classLoader

    /** A class implementing the required type and carrying every required annotation loads cleanly. */
    @Test
    fun `load returns the class when every requirement is met`() {
        val loaded = PluginClassRequirements.load(
            Compliant::class.java.name, classLoader,
            requiredType = RequiredType::class.java,
            requiredAnnotations = listOf(FirstRequiredAnnotation::class.java, SecondRequiredAnnotation::class.java),
        )

        assertEquals(Compliant::class.java, loaded)
    }

    /** A class name that does not resolve is reported with a detailed reason, not a bare ClassNotFoundException. */
    @Test
    fun `load throws a detailed exception when the class does not exist`() {
        val exception = assertThrows(PluginLoadException::class.java) {
            PluginClassRequirements.load(
                "org.pcsoft.app.aighost.plugin.manager.DoesNotExist", classLoader,
                requiredType = RequiredType::class.java,
            )
        }

        assertEquals(true, exception.message?.contains("DoesNotExist"))
    }

    /** A class that does not implement the required type is reported with a detailed reason. */
    @Test
    fun `load throws a detailed exception when the required type is not implemented`() {
        val exception = assertThrows(PluginLoadException::class.java) {
            PluginClassRequirements.load(
                NotImplementingRequiredType::class.java.name, classLoader,
                requiredType = RequiredType::class.java,
            )
        }

        assertEquals(true, exception.message?.contains(RequiredType::class.java.name))
    }

    /** A class missing every required annotation is reported naming all of them. */
    @Test
    fun `load throws a detailed exception naming every missing annotation`() {
        val exception = assertThrows(PluginLoadException::class.java) {
            PluginClassRequirements.load(
                MissingBothAnnotations::class.java.name, classLoader,
                requiredType = RequiredType::class.java,
                requiredAnnotations = listOf(FirstRequiredAnnotation::class.java, SecondRequiredAnnotation::class.java),
            )
        }

        assertEquals(true, exception.message?.contains("FirstRequiredAnnotation"))
        assertEquals(true, exception.message?.contains("SecondRequiredAnnotation"))
    }

    /** A class missing only one of several required annotations is reported naming just that one. */
    @Test
    fun `load throws a detailed exception naming only the missing annotation`() {
        val exception = assertThrows(PluginLoadException::class.java) {
            PluginClassRequirements.load(
                MissingOneAnnotation::class.java.name, classLoader,
                requiredType = RequiredType::class.java,
                requiredAnnotations = listOf(FirstRequiredAnnotation::class.java, SecondRequiredAnnotation::class.java),
            )
        }

        assertEquals(false, exception.message?.contains("FirstRequiredAnnotation"))
        assertEquals(true, exception.message?.contains("SecondRequiredAnnotation"))
    }
}
