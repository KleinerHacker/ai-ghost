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

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ai-ghost"

include(":lib:plugin:api")
project(":lib:plugin:api").name = "ai-ghost-plugin-api"

include(":lib:model")
project(":lib:model").name = "ai-ghost-model"

include(":lib:fx-model")
project(":lib:fx-model").name = "ai-ghost-fx-model"

include(":lib:ai")
project(":lib:ai").name = "ai-ghost-ai"

include(":lib:layouting")
project(":lib:layouting").name = "ai-ghost-layouting"

include(":lib:layouting-model")
project(":lib:layouting-model").name = "ai-ghost-layouting-model"

include(":app:ui")
project(":app:ui").name = "ai-ghost-ui"
