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

/**
 * Annotation used to mark a class as a part of the project structure.
 * This annotation provides metadata for components conforming to the `ProjectPart` interface or similar constructs.
 *
 * Classes annotated with `@ProjectPartInfo` are expected to represent distinct elements of a project's model,
 * enabling better organization, management, and identification of individual project components.
 *
 * This annotation is retained at runtime for reflective operations and is part of the documented API.
 *
 * @param identifier A unique identifier for the project part, facilitating its identification and management.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ProjectPartInfo(
    val identifier: String
)
