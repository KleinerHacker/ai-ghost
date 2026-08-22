import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
}

// Jackson is exposed through the model API, so consumers can serialise the model themselves.
val jacksonVersion = "2.22.1"

// Arrow's Either appears in the signatures of the storage, so it is part of the API as well.
val arrowVersion = "2.1.2"

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    api("io.arrow-kt:arrow-core:${arrowVersion}")
}

// The module descriptor is the only Java source, while the classes it exports are written in Kotlin.
// javac only sees them as part of the module when both compilers write into the same output
// directory - otherwise the exported package is reported as empty.
val compileKotlin = tasks.named<KotlinCompile>("compileKotlin")

tasks.named<JavaCompile>("compileJava") {
    destinationDirectory.set(compileKotlin.flatMap { it.destinationDirectory })
}
