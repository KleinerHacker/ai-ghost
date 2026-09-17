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

package org.pcsoft.app.aighost.app.ui.component

import de.saxsys.mvvmfx.ViewModel

/**
 * View model of [AiActionBar].
 *
 * The bar owns no project data and follows no model: each of its buttons ends at an unimplemented
 * `*View` method (IP-18), so there is nothing for this class to hold. It exists only so [AiActionBar]
 * can follow the same Component/View/ViewModel structure every other component of `ui.component`
 * uses.
 */
class AiActionBarViewModel : ViewModel
