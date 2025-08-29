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
@file:OptIn(ExperimentalMaterialApi::class)
@file:Suppress("DEPRECATION")

package com.revenuecat.purchases.slidetounlock

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeProgress
import androidx.compose.material.SwipeableState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.revenuecat.purchases.slidetounlock.SlideToUnlockDefaults.ThumbSize
import com.skydoves.compose.effects.RememberedEffect
import kotlin.math.roundToInt

/**
 * Represents the two discrete states of the SlideToUnlock component.
 */
private enum class SlideToUnlockValue { Start, End }

/**
 * A fully customizable "slide-to-unlock" UI component for Jetpack Compose.
 *
 * This component displays a track with a draggable thumb. The user can slide the thumb
 * from the start to the end to trigger an action. The component supports a loading state
 * where the thumb is locked at the end and displays a progress indicator.
 *
 * Supports horizontal (default) and vertical orientations via [SlideOrientation].
 *
 * @param isSlided Whether the slider has been completed and locked. When `true`, the thumb
 * is fixed at the end and a loading indicator may be shown.
 * @param onSlideCompleted Invoked when the user successfully completes the slide gesture by
 * dragging the thumb past the [fractionalThreshold].
 * @param modifier Modifier to be applied to the layout.
 * @param colors Provides the color scheme for the track, thumb, and hint. Defaults to [DefaultSlideToUnlockColors].
 * @param hintTexts Provides the hint messages for both the default and slided states.
 * @param trackShape Defines the shape of the background track. Defaults to a rounded rectangle.
 * @param thumbSize The size of the draggable thumb.
 * @param fractionalThreshold The fraction of the track (from 0.0f to 1.0f) that the user must
 * slide the thumb past to trigger the `onSlideCompleted` callback. Defaults to 0.85f (85%).
 * @param paddings The external padding values to be applied to the entire component.
 * @param hintPaddings The internal padding values for the hint composable, used to prevent
 * the hint from overlapping with the thumb at its start and end positions.
 * @param onSlideFractionChanged Optional callback invoked with the current slide progress fraction (0f–1f)
 * as the user drags the thumb.
 * @param orientation The direction of the sliding gesture. Defaults to [SlideOrientation.Horizontal].
 * @param thumb A composable slot for customizing the draggable thumb. It provides the slide state,
 * current slide fraction, colors, size, and orientation.
 * @param hint A composable slot for customizing the hint text or visuals inside the track. It provides
 * the slide state, current slide fraction, hint texts, colors, paddings, and orientation.
 */
@Composable
public fun SlideToUnlock(
  isSlided: Boolean,
  onSlideCompleted: () -> Unit = {},
  modifier: Modifier = Modifier,
  colors: SlideToUnlockColors = DefaultSlideToUnlockColors(),
  hintTexts: HintTexts = HintTexts.defaultHintTexts(),
  trackShape: Shape = RoundedCornerShape(percent = 50),
  thumbSize: DpSize = DpSize(ThumbSize, ThumbSize),
  fractionalThreshold: Float = 0.85f,
  paddings: PaddingValues = PaddingValues(SlideToUnlockDefaults.Paddings),
  hintPaddings: PaddingValues = PaddingValues(start = thumbSize.width, end = thumbSize.width),
  onSlideFractionChanged: (Float) -> Unit = {},
  orientation: SlideOrientation = SlideOrientation.Horizontal,
  thumb: @Composable BoxScope.(isSlided: Boolean, slideFraction: Float, colors: SlideToUnlockColors, size: DpSize, orientation: SlideOrientation) -> Unit = { slided, _, _, size, orient ->
    SlideToUnlockDefaults.Thumb(
      modifier = Modifier.size(thumbSize),
      isSlided = slided,
      colors = colors,
      thumbSize = thumbSize,
      orientation = orient,
    )
  },
  hint: @Composable BoxScope.(isSlided: Boolean, slideFraction: Float, hintTexts: HintTexts, colors: SlideToUnlockColors, paddings: PaddingValues, orientation: SlideOrientation) -> Unit = { slided, fraction, _, _, paddings, orient ->
    SlideToUnlockDefaults.Hint(
      modifier = Modifier.align(Alignment.Center),
      slideFraction = fraction,
      hintTexts = hintTexts,
      isSlided = slided,
      colors = colors,
      paddingValues = hintPaddings,
      orientation = orient,
    )
  },
) {
  val hapticFeedback = LocalHapticFeedback.current
  val swipeState = rememberSwipeableState(
    initialValue = if (isSlided) SlideToUnlockValue.End else SlideToUnlockValue.Start,
    confirmStateChange = { anchor ->
      if (anchor == SlideToUnlockValue.End) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        onSlideCompleted.invoke()
      }
      true
    },
  )

  val slideFraction by remember {
    derivedStateOf { calculateSwipeFraction(swipeState.progress) }
  }

  LaunchedEffect(isSlided) {
    swipeState.animateTo(if (isSlided) SlideToUnlockValue.End else SlideToUnlockValue.Start)
  }

  RememberedEffect(slideFraction) {
    onSlideFractionChanged.invoke(slideFraction)
  }

  SlideToUnlockTrack(
    swipeState = swipeState,
    slideFraction = slideFraction,
    enabled = !isSlided,
    modifier = modifier,
    colors = colors,
    trackShape = trackShape,
    thumbSize = thumbSize,
    paddingValues = paddings,
    fractionalThreshold = fractionalThreshold,
    orientation = orientation,
  ) {
    hint(
      isSlided,
      slideFraction,
      hintTexts,
      colors,
      hintPaddings,
      orientation,
    )

    Box(
      modifier = Modifier.offset {
        when (orientation) {
          SlideOrientation.Horizontal -> IntOffset(swipeState.offset.value.roundToInt(), 0)
          SlideOrientation.Vertical -> IntOffset(0, swipeState.offset.value.roundToInt())
        }
      },
    ) {
      thumb(isSlided, slideFraction, colors, thumbSize, orientation)
    }
  }
}

/**
 * Calculates the swipe progress fraction from 0.0f (start) to 1.0f (end).
 */
private fun calculateSwipeFraction(progress: SwipeProgress<SlideToUnlockValue>): Float {
  val atAnchor = progress.from == progress.to
  val fromStart = progress.from == SlideToUnlockValue.Start
  return if (atAnchor) {
    if (fromStart) 0f else 1f
  } else {
    if (fromStart) progress.fraction else 1f - progress.fraction
  }
}

/**
 * A private composable that represents the track of the SlideToUnlock component.
 * This composable handles the layout, gesture detection, and background drawing for the track.
 */
@Composable
private fun SlideToUnlockTrack(
  swipeState: SwipeableState<SlideToUnlockValue>,
  slideFraction: Float,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  colors: SlideToUnlockColors,
  trackShape: Shape,
  fractionalThreshold: Float,
  thumbSize: DpSize,
  paddingValues: PaddingValues,
  orientation: SlideOrientation,
  content: @Composable BoxScope.() -> Unit,
) {
  val density = LocalDensity.current
  val layoutDirection = LocalLayoutDirection.current
  val trackColor = colors.trackColor(slideFraction)
  val trackBrush = colors.trackBrush(slideFraction)

  val startOfTrackPx = 0f
  var measuredWidth by remember { mutableIntStateOf(0) }
  var measuredHeight by remember { mutableIntStateOf(0) }

  val endOfTrackPx = remember(measuredWidth, measuredHeight, orientation) {
    with(density) {
      when (orientation) {
        SlideOrientation.Horizontal -> {
          val totalPadding = paddingValues.calculateStartPadding(layoutDirection) +
            paddingValues.calculateEndPadding(layoutDirection)
          measuredWidth - (totalPadding + thumbSize.width).toPx()
        }
        SlideOrientation.Vertical -> {
          val totalPadding = paddingValues.calculateTopPadding() +
            paddingValues.calculateBottomPadding()
          measuredHeight - (totalPadding + thumbSize.height).toPx()
        }
      }
    }
  }

  Box(
    modifier = modifier
      .run {
        when (orientation) {
          SlideOrientation.Horizontal -> fillMaxWidth()
          SlideOrientation.Vertical -> fillMaxHeight()
        }
      }
      .onSizeChanged {
        measuredWidth = it.width
        measuredHeight = it.height
      }
      .run {
        if (trackBrush != null) {
          background(
            brush = trackBrush,
            shape = trackShape,
          )
        } else {
          background(
            color = trackColor,
            shape = trackShape,
          )
        }
      }
      .padding(paddingValues = paddingValues)
      .swipeable(
        enabled = enabled,
        state = swipeState,
        orientation = when (orientation) {
          SlideOrientation.Horizontal -> Orientation.Horizontal
          SlideOrientation.Vertical -> Orientation.Vertical
        },
        anchors = mapOf(
          startOfTrackPx to SlideToUnlockValue.Start,
          endOfTrackPx to SlideToUnlockValue.End,
        ),
        thresholds = { _, _ -> FractionalThreshold(fraction = fractionalThreshold) },
        velocityThreshold = with(density) { SlideToUnlockDefaults.VelocityThreshold },
      ),
    content = content,
  )
}

/**
 * Contains default values and functions used by the [SlideToUnlock] component.
 */
public object SlideToUnlockDefaults {
  public val ThumbSize: Dp = 42.dp
  public val Paddings: Dp = 8.dp
  public val VelocityThreshold: Dp = 60.dp

  /**
   * The default thumb composable for the [SlideToUnlock] component.
   *
   * @param isSlided Whether the component is in the loading state.
   * @param colors The [SlideToUnlockColors] used to customize the appearance of the component.
   * @param orientation The slide orientation.
   * @param modifier The modifier to be applied to the thumb.
   */
  @Composable
  public fun Thumb(
    isSlided: Boolean,
    thumbSize: DpSize,
    colors: SlideToUnlockColors = DefaultSlideToUnlockColors(),
    orientation: SlideOrientation = SlideOrientation.Horizontal,
    modifier: Modifier = Modifier,
  ) {
    Box(
      modifier = modifier
        .size(thumbSize)
        .background(color = colors.thumbColor(), shape = CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      if (isSlided) {
        CircularProgressIndicator(
          modifier = Modifier.padding(8.dp),
          color = colors.progressColor(),
          strokeWidth = 3.dp,
        )
      } else {
        Icon(
          modifier = Modifier.align(Alignment.Center).size(40.dp),
          imageVector = when (orientation) {
            SlideOrientation.Horizontal -> Icons.AutoMirrored.Filled.ArrowRight
            SlideOrientation.Vertical -> Icons.Filled.KeyboardArrowDown
          },
          tint = colors.thumbIconColor(),
          contentDescription = when (orientation) {
            SlideOrientation.Horizontal -> "Slide to unlock"
            SlideOrientation.Vertical -> "Slide down to unlock"
          },
        )
      }
    }
  }

  /**
   * The default hint composable for the [SlideToUnlock] component.
   *
   * @param slideFraction The current progress of the swipe, from 0.0f to 1.0f.
   * @param orientation The slide orientation.
   * @param modifier The modifier to be applied to the hint.
   */
  @Composable
  public fun Hint(
    hintTexts: HintTexts,
    isSlided: Boolean,
    slideFraction: Float,
    colors: SlideToUnlockColors,
    paddingValues: PaddingValues,
    orientation: SlideOrientation = SlideOrientation.Horizontal,
    modifier: Modifier = Modifier,
  ) {
    val layoutDirection = LocalLayoutDirection.current

    AnimatedContent(
      modifier = modifier.run {
        when (orientation) {
          SlideOrientation.Horizontal -> fillMaxWidth()
          SlideOrientation.Vertical -> fillMaxHeight()
        }
      },
      targetState = isSlided,
    ) { slided ->
      if (!slided) {
        when (orientation) {
          SlideOrientation.Horizontal -> {
            Text(
              modifier = Modifier.fillMaxWidth().padding(
                start = paddingValues.calculateStartPadding(layoutDirection),
              ),
              text = hintTexts.defaultText,
              textAlign = TextAlign.Center,
              color = colors.hintColor(slideFraction),
              style = MaterialTheme.typography.titleMedium,
            )
          }
          SlideOrientation.Vertical -> {
            Column(
              modifier = Modifier.fillMaxHeight().padding(
                top = paddingValues.calculateTopPadding(),
              ),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Text(
                text = hintTexts.defaultText,
                textAlign = TextAlign.Center,
                color = colors.hintColor(slideFraction),
                style = MaterialTheme.typography.titleMedium,
              )
            }
          }
        }
      } else {
        when (orientation) {
          SlideOrientation.Horizontal -> {
            Text(
              modifier = Modifier.fillMaxWidth(),
              text = hintTexts.slidedText,
              textAlign = TextAlign.Center,
              color = colors.slidedHintColor(),
              style = MaterialTheme.typography.titleMedium,
            )
          }
          SlideOrientation.Vertical -> {
            Column(
              modifier = Modifier.fillMaxHeight(),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Text(
                text = hintTexts.slidedText,
                textAlign = TextAlign.Center,
                color = colors.slidedHintColor(),
                style = MaterialTheme.typography.titleMedium,
              )
            }
          }
        }
      }
    }
  }
}
