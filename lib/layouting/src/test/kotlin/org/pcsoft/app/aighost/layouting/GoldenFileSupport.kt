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

package org.pcsoft.app.aighost.layouting

import java.io.File

/**
 * Reads, writes and compares golden files of the layout regression harness (IP-06).
 *
 * A golden file holds a computed page structure as plain numbers, one line per page, never as an
 * image - so a diff of the checked in file is meaningful by itself. [verify] compares the current
 * result against the checked in file and names the first differing line when they diverge.
 *
 * A golden file is regenerated intentionally by running the owning test once with the system
 * property `-D$UPDATE_PROPERTY=true`; the test itself always passes in that mode, since it only
 * writes the new reference instead of comparing against it. The result must then be reviewed with
 * `git diff` before it is committed.
 */
internal object GoldenFileSupport {

    private const val UPDATE_PROPERTY = "layoutGoldenFiles.update"
    private val resourceDir = File("src/test/resources/org/pcsoft/app/aighost/layouting/goldenfiles")

    /**
     * Compares [actual] against the golden file named [name], failing with the first differing line
     * when they diverge, or writes [actual] as the new golden file when the update mode is active.
     */
    fun verify(name: String, actual: List<String>) {
        val file = File(resourceDir, "$name.golden")

        if (System.getProperty(UPDATE_PROPERTY) == "true") {
            resourceDir.mkdirs()
            file.writeText(actual.joinToString(separator = "\n", postfix = "\n"))
            return
        }

        check(file.exists()) {
            "Golden file '${file.path}' is missing. Create it by running this test once with " +
                "-D$UPDATE_PROPERTY=true, then review the result with `git diff` before committing it."
        }

        val expected = file.readLines()
        val mismatch = (0 until maxOf(expected.size, actual.size)).firstOrNull { index ->
            expected.getOrNull(index) != actual.getOrNull(index)
        }

        if (mismatch != null) {
            error(
                "Golden file '$name' differs at line ${mismatch + 1}:\n" +
                    "  expected: ${expected.getOrNull(mismatch) ?: "<missing line>"}\n" +
                    "  actual:   ${actual.getOrNull(mismatch) ?: "<missing line>"}\n" +
                    "If this change is intentional, regenerate it with -D$UPDATE_PROPERTY=true and " +
                    "review the diff before committing."
            )
        }
    }
}
