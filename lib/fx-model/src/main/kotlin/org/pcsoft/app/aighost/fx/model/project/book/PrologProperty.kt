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

package org.pcsoft.app.aighost.fx.model.project.book

import org.pcsoft.app.aighost.model.project.book.Prolog

/**
 * Property wrapping the prolog of a book and offering every field of it as a property of its own.
 *
 * A book carries a prolog only after the user created it, so the wrapped object is absent until then
 * and every field property answers with a neutral value.
 */
internal class PrologProperty(
    setter: (Prolog?) -> Unit,
    getter: () -> Prolog?,
    fireEvent: () -> Unit
) : BookPartProperty<Prolog?>(setter, getter, fireEvent)
