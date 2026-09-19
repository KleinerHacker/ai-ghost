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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path

/**
 * Exercises [PluginManager.load] end to end - directory scanning, the built-in-over-user and
 * first-loaded-wins precedence rules, and that a single defective plugin never stops the rest from
 * loading - against the fixture plugin JARs packaged under `build/fixtures/jars`.
 *
 * Every `@TempDir` that receives a copied JAR uses [CleanupMode.NEVER]: on Windows,
 * `URLClassLoader.close()` does not reliably release the OS-level lock on a JAR it opened within the
 * same JVM run - a long-standing JDK/Windows limitation, not a defect of [PluginLoader] or
 * [closeLoaders] - so JUnit's own best-effort deletion of these directories would otherwise fail the
 * test on a problem that has nothing to do with [PluginManager] itself. The directories are left
 * behind under the OS temp folder.
 */
class PluginManagerTest {

    private val fixturesDir = Path.of(System.getProperty("ai-ghost.fixtures.dir"))

    /** Every registry a test built through [load], so [closeLoaders] can release its class loaders. */
    private val registries = mutableListOf<AiProviderRegistry>()

    private fun load(directories: List<Path>): AiProviderRegistry =
        PluginManager.load(directories).also { registries += it }

    private fun copyFixture(name: String, into: Path) {
        Files.copy(fixturesDir.resolve(name), into.resolve(name))
    }

    /**
     * Closes every class loader opened by this test - `PluginLoader` deliberately keeps a provider's
     * `URLClassLoader` open for its lifetime in production, since the provider instance keeps using
     * it.
     */
    @AfterEach
    fun closeLoaders() {
        registries.asSequence()
            .flatMap { it.all.asSequence() }
            .mapNotNull { it.provider.javaClass.classLoader as? URLClassLoader }
            .forEach { it.close() }
        registries.clear()
    }

    /** A directory that does not exist is skipped, and loading otherwise still succeeds. */
    @Test
    fun `load skips a directory that does not exist`(@TempDir dir: Path) {
        val missing = dir.resolve("does-not-exist")

        val registry = load(listOf(missing))

        assertTrue(registry.all.isEmpty())
    }

    /** A well-formed plugin ends up registered, with its manifest and provider intact. */
    @Test
    fun `load registers a well-formed plugin`(@TempDir(cleanup = CleanupMode.NEVER) dir: Path) {
        copyFixture("good-provider.jar", dir)

        val registry = load(listOf(dir))

        val registered = registry.find("good")
        assertEquals("good-fixture", registered?.manifest?.id)
    }

    /** A defective plugin next to a good one is skipped, without stopping the good one from loading. */
    @Test
    fun `load skips a defective plugin without affecting the others`(@TempDir(cleanup = CleanupMode.NEVER) dir: Path) {
        copyFixture("good-provider.jar", dir)
        copyFixture("throwing-provider.jar", dir)
        copyFixture("wrong-contract-version-provider.jar", dir)
        copyFixture("missing-manifest-provider.jar", dir)
        copyFixture("incomplete-manifest-provider.jar", dir)
        copyFixture("unsupported-field-provider.jar", dir)

        val registry = load(listOf(dir))

        assertEquals(listOf("good"), registry.all.map { it.id })
    }

    /** On an id collision between two directories, the earlier directory in the list wins. */
    @Test
    fun `load lets the built-in directory win an id collision over the user directory`(
        @TempDir(cleanup = CleanupMode.NEVER) builtIn: Path, @TempDir(cleanup = CleanupMode.NEVER) user: Path
    ) {
        copyFixture("good-provider.jar", builtIn)
        copyFixture("duplicate-id-provider.jar", user)

        val registry = load(listOf(builtIn, user))

        assertEquals("good-fixture", registry.find("good")?.manifest?.id)
    }

    /** Within one directory, the JAR that sorts first by file name wins an id collision. */
    @Test
    fun `load lets the first loaded jar of a directory win an id collision`(@TempDir(cleanup = CleanupMode.NEVER) dir: Path) {
        copyFixture("good-provider.jar", dir)
        copyFixture("duplicate-id-provider.jar", dir)

        val registry = load(listOf(dir))

        // "duplicate-id-provider.jar" sorts before "good-provider.jar", so it is loaded first.
        assertEquals("duplicate-id-fixture", registry.find("good")?.manifest?.id)
    }
}
