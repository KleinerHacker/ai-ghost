plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ai-ghost"

include(":app:ui")
project(":app:ui").name = "ai-ghost-ui"
