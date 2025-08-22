/*
 * Copyright (c) 2025 RevenueCat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revenuecat.purchases.slidetounlock

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * A data class that holds the text content for the hint displayed in the SlideToUnlock component.
 *
 * This class is marked as [Immutable] to signal to the Compose compiler that its properties
 * will not change after creation. This allows for performance optimizations, as composables
 * that use this class as a parameter can be safely skipped during recomposition if the instance
 * has not changed.
 *
 * @property defaultText The text displayed when the thumb is at the start position.
 * @property slidedText The text displayed when the thumb is at the end position or in a loading state.
 */
@Immutable
public data class HintTexts(
  public val defaultText: String,
  public val slidedText: String,
) {
  /**
   * A companion object to provide default values and factory functions for [HintTexts].
   */
  public companion object {

    /**
     * Creates and returns a default instance of [HintTexts].
     *
     * This factory function provides a convenient way to use the component with
     * standard, pre-defined hint messages without needing to specify them manually.
     *
     * @return A [HintTexts] instance with default "Slide to unlock" and "Please wait a minute" messages.
     */
    @Stable
    public fun defaultHintTexts(): HintTexts {
      // Return a new instance with the default English text values.
      return HintTexts(
        defaultText = "Slide to unlock",
        slidedText = "Please wait a second..",
      )
    }
  }
}
