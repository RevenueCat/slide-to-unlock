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

import androidx.compose.runtime.Stable

/**
 * Represents the high-level UI state of a [SlideToUnlock] component.
 *
 * Unlike the previous boolean flag, this models the full lifecycle of an action that is
 * triggered by sliding the thumb: it can be idle (draggable), loading, completed successfully,
 * or failed. The component renders a different thumb indicator and hint text for each state and,
 * for every state other than [Idle], the thumb is locked at the end of the track.
 */
@Stable
public enum class SlideState {
  /** Resting at the start position. The thumb is draggable and the slide gesture is enabled. */
  Idle,

  /** Locked at the end position while an asynchronous action is in progress. A progress indicator is shown. */
  Loading,

  /** Locked at the end position because the action completed successfully. A success indicator is shown. */
  Success,

  /** Locked at the end position because the action failed. An error indicator is shown. */
  Error,
  ;

  /** Whether the component is in a terminal/locked state, i.e. anything other than [Idle]. */
  public val isLocked: Boolean
    get() = this != Idle
}
