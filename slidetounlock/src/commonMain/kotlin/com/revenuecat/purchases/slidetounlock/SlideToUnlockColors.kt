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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Defines the customizable colors used by a [SlideToUnlock] component.
 *
 * This interface allows for a flexible and theme-aware way to style the component.
 * By providing functions that are `@Composable`, implementations can access `MaterialTheme`
 * or other `CompositionLocal` values to create a theme-consistent appearance.
 * The entire interface is marked as [Stable] to signal to the Compose compiler that
 * implementations can be trusted to be stable, enabling performance optimizations.
 *
 * @see DefaultSlideToUnlockColors
 */
@Stable
public interface SlideToUnlockColors {
  /**
   * Represents the color of the track. This color can change based on the swipe progress.
   *
   * @param slideFraction The current progress of the swipe, from 0.0f (start) to 1.0f (end).
   * @return The calculated color for the track at the given swipe fraction.
   */
  @Stable
  @Composable
  public fun trackColor(slideFraction: Float): Color

  /**
   * Optional gradient brush for the track. If not null, this brush will be used instead of a solid color.
   *
   * @param slideFraction The current progress of the swipe.
   * @return A Brush for the track, or null if not used.
   */
  @Stable
  @Composable
  public fun trackBrush(slideFraction: Float): Brush?

  /**
   * Represents the color of the hint text during the swipe gesture.
   * This color typically fades out as the user slides the thumb.
   *
   * @param slideFraction The current progress of the swipe, from 0.0f to 1.0f.
   * @return The calculated color for the hint text at the given swipe fraction.
   */
  @Stable
  @Composable
  public fun hintColor(slideFraction: Float): Color

  /**
   * Represents the color of the hint text after the slide has been completed
   * and the component is in its final state (e.g., loading or success).
   *
   * @return The color for the hint text in the "slided" state.
   */
  @Stable
  @Composable
  public fun slidedHintColor(): Color

  /**
   * Represents the background color of the draggable thumb.
   *
   * @return The color of the thumb.
   */
  @Stable
  @Composable
  public fun thumbColor(): Color

  /**
   * Optional gradient brush for the thumb. If not null, this brush will be used instead of a solid color.
   *
   * @param slideFraction The current progress of the swipe.
   * @return A Brush for the thumb, or null if not used.
   */
  @Stable
  @Composable
  public fun thumbBrush(slideFraction: Float): Brush?

  /**
   * Represents the color of the icon displayed inside the thumb (e.g., the arrow).
   *
   * @return The color of the thumb's icon.
   */
  @Stable
  @Composable
  public fun thumbIconColor(): Color

  /**
   * Represents the color of the circular progress indicator shown during the loading state.
   *
   * @return The color of the progress indicator.
   */
  @Stable
  @Composable
  public fun progressColor(): Color
}

/**
 * The default, concrete implementation of the [SlideToUnlockColors] interface.
 *
 * This data class holds the specific color values and implements the logic for interpolating
 * colors based on the swipe fraction. It is marked as [Stable] to ensure that Compose
 * can perform optimizations when it is used as a parameter in a composable.
 *
 * @property startTrackColor The color of the track when the thumb is at the start.
 * @property endTrackColor The color of the track when the thumb is at the end.
 * @property trackBrush An optional brush for the track, which overrides color if non-null.
 * @property startHintColor The color of the hint text at the start of the swipe.
 * @property endHintColor The color of the hint text at the end of the swipe (usually transparent).
 * @property slidedHintColor The color of the hint text after the slide is completed.
 * @property thumbColor The solid background color of the thumb.
 * @property thumbIconColor The color of the icon displayed inside the thumb.
 * @property thumbBrush An optional brush for the thumb.
 * @property progressColor The color used for the circular progress indicator.
 */
@Stable
public data class DefaultSlideToUnlockColors(
  public val startTrackColor: Color = Color(0xFF111111),
  public val endTrackColor: Color = Color(0x9F9C9399),
  public val trackBrush: Brush? = null,
  public val startHintColor: Color = Color.White,
  public val endHintColor: Color = Color.White.copy(alpha = 0f),
  public val slidedHintColor: Color = Color.Black,
  public val thumbColor: Color = Color.White,
  public val thumbIconColor: Color = Color.Black,
  public val thumbBrush: Brush? = null,
  public val progressColor: Color = Color(0xFF11D483),
) : SlideToUnlockColors {

  /**
   * Interpolates between [startTrackColor] and [endTrackColor] based on the swipe progress.
   */
  @Stable
  @Composable
  override fun trackColor(slideFraction: Float): Color {
    val endOfColorChangeFraction = 0.85f
    val fraction = (slideFraction / endOfColorChangeFraction).coerceIn(0f..1f)
    return lerp(startTrackColor, endTrackColor, fraction)
  }

  /**
   * Returns the brush to be used for the track, if provided.
   */
  @Stable
  @Composable
  override fun trackBrush(slideFraction: Float): Brush? {
    return trackBrush
  }

  /**
   * Returns the brush to be used for the thumb, if provided.
   */
  @Stable
  @Composable
  override fun thumbBrush(slideFraction: Float): Brush? {
    return thumbBrush
  }

  /**
   * Fades out the hint color as the user progresses the swipe.
   */
  @Stable
  @Composable
  override fun hintColor(slideFraction: Float): Color {
    val endOfFadeFraction = 0.45f
    val fraction = (slideFraction / endOfFadeFraction).coerceIn(0f..1f)
    return lerp(startHintColor, endHintColor, fraction)
  }

  /**
   * Returns the hint text color after the swipe is completed.
   */
  @Stable
  @Composable
  override fun slidedHintColor(): Color {
    return slidedHintColor
  }

  /**
   * Returns the color for the thumb background.
   */
  @Stable
  @Composable
  override fun thumbColor(): Color {
    return thumbColor
  }

  /**
   * Returns the color for the icon inside the thumb.
   */
  @Stable
  @Composable
  override fun thumbIconColor(): Color {
    return thumbIconColor
  }

  /**
   * Returns the color used for the loading indicator.
   */
  @Stable
  @Composable
  override fun progressColor(): Color {
    return progressColor
  }
}
