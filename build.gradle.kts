plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0" apply false
}

val junitVersion = "5.12.1"

allprojects {
    group = "org.pcsoft.app.aighost"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(25)
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
