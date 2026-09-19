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

/**
 * Loads a class named by a plugin (in its manifest, in a configuration field, ...) by its fully
 * qualified name and checks it against a set of requirements, so every such lookup in this module
 * reports the same kind of detailed [PluginLoadException] instead of each caller inventing its own.
 *
 * Kept generic on purpose: [PluginLoader] uses it today to load an
 * [org.pcsoft.app.aighost.plugin.api.provider.AiProvider] that MUST carry
 * `@`[org.pcsoft.app.aighost.plugin.api.provider.AiProviderInfo], but any future lookup of a
 * plugin-declared class against a required type and required annotations belongs here as well.
 */
internal object PluginClassRequirements {

    /**
     * Loads [className] through [classLoader] and checks it satisfies every requirement.
     *
     * @param className Fully qualified name of the class to load.
     * @param classLoader Class loader to resolve [className] against - a plugin's own, isolated one.
     * @param requiredType The type [className] MUST implement or extend.
     * @param requiredAnnotations Annotation types the class MUST carry, checked in the given order.
     * @return [className], loaded and confirmed to satisfy every requirement.
     * @throws PluginLoadException [className] was not found, does not implement [requiredType], or
     *                              is missing one of [requiredAnnotations] - naming the exact reason.
     */
    fun <T : Any> load(
        className: String,
        classLoader: ClassLoader,
        requiredType: Class<T>,
        requiredAnnotations: List<Class<out Annotation>> = emptyList(),
    ): Class<out T> {
        val loadedClass = try {
            Class.forName(className, false, classLoader)
        } catch (e: ClassNotFoundException) {
            throw PluginLoadException("class '$className' was not found", e)
        }

        if (!requiredType.isAssignableFrom(loadedClass)) {
            throw PluginLoadException("class '$className' does not implement '${requiredType.name}'")
        }

        val missingAnnotations = requiredAnnotations.filter { loadedClass.getAnnotation(it) == null }
        if (missingAnnotations.isNotEmpty()) {
            throw PluginLoadException(
                "class '$className' is missing the required annotation" +
                        (if (missingAnnotations.size > 1) "s " else " ") +
                        missingAnnotations.joinToString(", ") { "@${it.simpleName}" }
            )
        }

        @Suppress("UNCHECKED_CAST")
        return loadedClass as Class<out T>
    }
}
