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

import com.github.jk1.license.render.ReportRenderer

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.20" apply false
    // Same version as the Kotlin compiler plugin above - the serialization plugin is versioned in
    // lock step with the Kotlin compiler it plugs into. Wired up in IP-37 (ai-ghost-model), for the
    // simPlay `Document` that is now embedded in `Book`.
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.10" apply false
    id("org.jetbrains.dokka") version "2.2.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.9" apply false
    id("com.github.jk1.dependency-license-report") version "3.1.4" apply false
    id("org.cyclonedx.bom") version "3.4.1" apply false
    id("app.cash.licensee") version "1.14.1" apply false
}

val junitVersion = "6.1.3"

// simPlay, consumed from GitHub Packages. Pinned exactly: a minor bump would move every line break
// and page break at once. The layout engine is the Kotlin Multiplatform module published as
// `org.pcsoft.framework:simplay-engine` / `simplay-engine-jvm`; the JavaFX renderer is the JVM
// module published as `org.pcsoft.framework:simplay-fx`. simPlay's jars set `Automatic-Module-Name`
// to `org.pcsoft.framework.simplay.<module>`, which is the name a `requires` in a module descriptor
// uses. Wired up in IP-30 (layouting-model), IP-31 and IP-34 (app/ui).
// 0.2.2 renames the Maven artifact ids with a `simplay-` prefix (previously `engine`/`engine-jvm`)
// and adds a proper <licenses> block to the POMs, closing the interim licensee exception in
// lib/layouting-model/build.gradle.kts.
// 0.3.2 fixes DocumentEditor.splice() dropping a TextAnchor from any block it touched during an
// edit, found while wiring up anchor-based editing in IP-38.
// 0.4.0 moves LineBreakerStrategy/WordBreakerStrategy and their implementations from
// org.pcsoft.framework.simplay.engine to org.pcsoft.framework.simplay.engine.strategy (breaking);
// ai-ghost references neither directly, so no source change was needed for the move.
// 0.5.0 adds PageDecoration, a node ui/fx anchors to one edge of a single page by its stable
// Page.id; used for the label naming the book part that begins on a page.
val simplayVersion by extra("0.5.0")

// The UI module shipping the distribution; the licence report and the API docs are taken from it.
val uiProject = ":app:ai-ghost-ui"

allprojects {
    group = "org.pcsoft.app.aighost"

    // A release passes the tag as -PreleaseVersion=<tag>; a local build stays on the snapshot.
    version = (project.findProperty("releaseVersion") as String?)?.takeIf { it.isNotBlank() } ?: "1.0-SNAPSHOT"

    repositories {
        mavenCentral()

        // A locally published simPlay (`./gradlew publishToMavenLocal` in a simPlay checkout) is
        // picked up here first, so a patched build of the pinned version shadows the remote one -
        // used while simPlay changes are still local. `org.pcsoft.framework` artifacts are not on
        // Maven Central, so this only ever matters for simPlay.
        mavenLocal()

        // simPlay's published artifacts live on GitHub Packages, which always requires authentication
        // even for a read. Credentials come from `gpr.user` / `gpr.key` (for instance in
        // ~/.gradle/gradle.properties) or, in CI, from the GITHUB_ACTOR / GITHUB_TOKEN environment.
        maven {
            name = "simPlayGitHubPackages"
            url = uri("https://maven.pkg.github.com/KleinerHacker/simPlay")
            credentials {
                username = (findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR")
                password = (findProperty("gpr.key") as String?) ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.kotlinx.kover")
    apply(plugin = "com.github.jk1.dependency-license-report")
    apply(plugin = "org.cyclonedx.bom")
    apply(plugin = "app.cash.licensee")

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(25)
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
        "testImplementation"("org.junit.jupiter:junit-jupiter-params:${junitVersion}")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
        // Since JUnit 6 the platform launcher is no longer contributed automatically.
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    // Every module writes its licence report into the root build dir, so the MkDocs tasks find one place.
    extensions.configure<com.github.jk1.license.LicenseReportExtension> {
        outputDir = rootProject.layout.buildDirectory.dir("licences").get().asFile.absolutePath
        renderers = arrayOf<ReportRenderer>(
            com.github.jk1.license.render.JsonReportRenderer(),
            com.github.jk1.license.render.SimpleHtmlReportRenderer()
        )
    }

    extensions.configure<app.cash.licensee.LicenseeExtension> {
        listOf(
            "Apache-2.0",
            // ControlsFX
            "BSD-2-Clause",
            // SLF4J
            "MIT",
        ).forEach(::allow)

        // JavaFX publishes GPL-2.0 with Classpath Exception, but only as a URL - it carries no SPDX id
        // in its POM, so it has to be allowed by that URL.
        allowUrl("https://openjdk.java.net/legal/gplv2+ce.html") {
            because("GPL-2.0 with Classpath Exception")
        }

        // typetools names Apache-2.0 only as a plain http URL in its POM, without an SPDX id.
        allowUrl("http://apache.org/licenses/LICENSE-2.0") {
            because("Apache-2.0")
        }

        // MvvmFX's doc-annotations carries the MIT URL wrapped in quotes in its POM, so no SPDX id
        // is derived from it and it has to be allowed by that exact URL.
        allowUrl("'http://opensource.org/licenses/mit-license'") {
            because("MIT")
        }

        // SLF4J names MIT only by the URL of the licence text in its POM, without an SPDX id.
        allowUrl("https://opensource.org/license/mit") {
            because("MIT")
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks {
    //region Dokka
    register<Copy>("copyDokka") {
        group = "dokka"
        description = "Copy all Dokka to MkDocs"
        from(project(uiProject).layout.buildDirectory.dir("dokka"))
        into(File("docs/docs/dokka"))
        dependsOn("$uiProject:dokkaGeneratePublicationHtml")
    }

    register<Delete>("deleteDokka") {
        group = "dokka"
        description = "Delete Dokka"
        delete(File("docs/docs/dokka"))
    }
    //endregion

    //region Licencing
    register<Copy>("copyLicenceReport") {
        group = "licencing"
        description = "Copy licence report to MkDocs"
        from(layout.buildDirectory.dir("licences"))
        into(File("docs/docs/licences"))
        dependsOn("$uiProject:generateLicenseReport")
    }

    register<Delete>("deleteLicenceReport") {
        group = "licencing"
        description = "Delete licence report"
        delete(File("docs/docs/licences"))
    }
    //endregion

    //region MkDocs
    // mike spawns `mkdocs` as a subprocess; on Windows the Python Scripts dir
    // (where mkdocs.exe lives) is often not on PATH. Resolve it once and prepend
    // it to PATH for the mike tasks. In CI (setup-python) it is already on PATH.
    val pythonScriptsDir: String? by lazy {
        runCatching {
            providers.exec {
                commandLine("python", "-c", "import sysconfig; print(sysconfig.get_path('scripts'))")
            }.standardOutput.asText.get().trim().ifEmpty { null }
        }.getOrNull()
    }

    fun Exec.withMikePath() {
        pythonScriptsDir?.let { dir ->
            environment("PATH", dir + File.pathSeparator + System.getenv("PATH"))
        }
    }

    register<Exec>("installMkDocs") {
        group = null
        description = "Install mkdocs"
        workingDir = file("docs")
        commandLine("python", "-m", "pip", "install", "--upgrade", "mkdocs")
    }

    register<Exec>("installMkDocsMaterial") {
        group = null
        description = "Install mkdocs-material"
        workingDir = file("docs")
        commandLine("python", "-m", "pip", "install", "--upgrade", "mkdocs-material")
    }

    register<Exec>("installGitHubPages") {
        group = null
        description = "Install ghp-import"
        workingDir = file("docs")
        commandLine("python", "-m", "pip", "install", "--upgrade", "ghp-import")
    }

    register<Exec>("installMike") {
        group = null
        description = "Install mike for versioned docs deployment"
        workingDir = file("docs")
        commandLine("python", "-m", "pip", "install", "--upgrade", "mike")
    }

    register("installDocs") {
        group = "MKDocs"
        description = "Install mkdocs and dependencies"
        dependsOn("installMkDocs")
        dependsOn("installMkDocsMaterial")
        dependsOn("installGitHubPages")
        dependsOn("installMike")
    }

    register<Exec>("runDocs") {
        group = "MKDocs"
        description = "Run mkdocs serve and open browser (no version selector - that only appears on the deployed site)"
        workingDir = file("docs")
        commandLine("python", "-m", "mkdocs", "serve", "-o", "-w", ".", "-w", "./docs")
        dependsOn("installDocs", "copyDokka", "copyLicenceReport")
        finalizedBy("deleteDokka", "deleteLicenceReport")
    }

    register<Exec>("buildDocs") {
        group = "MKDocs"
        description =
            "Build the mkdocs site into build/docs (per mkdocs.yml site_dir; no serve, no deploy) - usable as a generation test"
        workingDir = file("docs")
        // --strict fails the build on warnings (broken links, missing pages ...) so it acts as a test;
        // --clean wipes the previous output first.
        commandLine("python", "-m", "mkdocs", "build", "--clean", "--strict")
        dependsOn("installDocs", "copyDokka", "copyLicenceReport")
        finalizedBy("deleteDokka", "deleteLicenceReport")
    }

    register<Exec>("deployDocs") {
        group = "MKDocs"
        description =
            "Deploy a versioned docs snapshot via mike. Pass -PdocsVersion=<tag>; falls back to \"snapshot\" if no tag is given. Requires a pre-configured git push target."
        workingDir = file("docs")
        val ver = (project.findProperty("docsVersion") as String?)?.takeIf { it.isNotBlank() }
            ?: "snapshot"
        val setLatest = ver != "snapshot" && (project.findProperty("setLatest") as String?) != "false"
        val args = buildList {
            add("python"); add("-c"); add("from mike.driver import main; main()"); add("deploy"); add("--push")
            if (setLatest) {
                add("--update-aliases"); add(ver); add("latest")
            } else add(ver)
        }
        commandLine(args)
        withMikePath()
        dependsOn("installDocs", "copyDokka", "copyLicenceReport")
        finalizedBy("deleteDokka", "deleteLicenceReport")
    }

    register<Exec>("setDefaultDocs") {
        group = "MKDocs"
        description =
            "Set the default docs version shown at the root URL via mike (run once after the first release deploy)."
        workingDir = file("docs")
        commandLine("python", "-c", "from mike.driver import main; main()", "set-default", "--push", "latest")
        withMikePath()
        dependsOn("installDocs")
    }
    //endregion
}
