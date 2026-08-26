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

package org.pcsoft.app.aighost.plugin.api.model.project

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * The project parts the application knows, by the identifier each of them is stored under.
 *
 * A project is written as an archive of its parts, one entry per part named after the identifier of
 * that part. Reading such an entry back needs the class behind the identifier, and that class is what
 * this registry hands out: the application registers the parts it ships with, a plugin registers the
 * ones it brings along, and the storage asks here instead of knowing a fixed list.
 *
 * The registry is filled while the application runs, so it is safe to use from several threads. An
 * entry no class is registered for is not an error - the storage skips such a part and keeps the rest
 * of the document, so a project written with a plugin still opens without it.
 */
object ProjectPartRegistry {

    private val partClasses = ConcurrentHashMap<String, KClass<out ProjectPart>>()

    /**
     * The registered part classes by their identifier, as a snapshot of the moment it is read.
     */
    val registered: Map<String, KClass<out ProjectPart>>
        get() = partClasses.toMap()

    /**
     * Registers [partClass] under the identifier it declares through [ProjectPartInfo].
     *
     * Registering the same class again does nothing, so a plugin loaded twice is no problem.
     *
     * @param partClass The part class to register.
     * @throws IllegalArgumentException When the class declares no [ProjectPartInfo].
     * @throws IllegalStateException When another class is already registered under that identifier.
     */
    fun register(partClass: KClass<out ProjectPart>) {
        val info = partClass.java.getAnnotation(ProjectPartInfo::class.java)
            ?: throw IllegalArgumentException(
                "The project part ${nameOf(partClass)} declares no ProjectPartInfo and cannot be registered."
            )

        val present = partClasses.putIfAbsent(info.identifier, partClass)
        check(present == null || present == partClass) {
            "The project part identifier '${info.identifier}' is already taken by ${nameOf(present!!)}."
        }
    }

    /**
     * Removes the part class registered under [identifier], for instance when a plugin is unloaded.
     *
     * @param identifier The identifier to release.
     * @return `true` when a class was registered under that identifier, `false` otherwise.
     */
    fun unregister(identifier: String): Boolean = partClasses.remove(identifier) != null

    /**
     * The part class registered under [identifier], or `null` when none is.
     *
     * @param identifier The identifier a part is stored under.
     */
    fun partClassOf(identifier: String): KClass<out ProjectPart>? = partClasses[identifier]

    /**
     * The identifier [partClass] is stored under: the one it declares through [ProjectPartInfo], or
     * the simple name of the class when it declares none.
     *
     * A part is written under this name whether it is registered or not, so a part that only lives in
     * memory still travels with the document.
     *
     * @param partClass The part class to name.
     */
    fun identifierOf(partClass: KClass<out ProjectPart>): String =
        partClass.java.getAnnotation(ProjectPartInfo::class.java)?.identifier ?: nameOf(partClass)

    /**
     * The name a class is reported with, falling back to the name of the underlying Java class for the
     * ones that carry no simple name of their own, an anonymous object for instance.
     */
    private fun nameOf(partClass: KClass<out ProjectPart>): String =
        partClass.simpleName ?: partClass.java.name

}
