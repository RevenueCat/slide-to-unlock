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

package com.revenuecat.slidetounlock

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import com.revenuecat.slidetounlock.SlideToUnlockDefaults.ThumbSize
import com.skydoves.compose.effects.RememberedEffect
import kotlin.math.roundToInt

/**
 * Represents the two discrete states of the SlideToUnlock component.
 */
private enum class SlideToUnlockValue { Start, End }

/**
 * A composable that provides a "slide to unlock" UI element.
 *
 * This component displays a track with a draggable thumb. The user can slide the thumb
 * from the start to the end to trigger an action. The component supports a loading state
 * where the thumb is locked at the end and displays a progress indicator.
 *
 * @param isSlided A boolean that indicates if the component should be in the loading state.
 * When true, the thumb is locked at the end and shows a progress indicator.
 * @param onSlideCompleted A lambda that is invoked when the user successfully slides the
 * thumb to the end.
 * @param modifier The [Modifier] to be applied to this component.
 * @param colors The [SlideToUnlockColors] used to customize the appearance of the component.
 * @param trackShape The [Shape] of the track.
 * @param onSlideFractionChanged A callback that will be invoked whenever the swipe fraction is changed.
 * @param thumb A composable lambda for the thumb that is dragged. By default, it uses [SlideToUnlockDefaults.Thumb].
 * @param hint A composable lambda for the hint text displayed on the track. By default, it uses [SlideToUnlockDefaults.Hint].
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
  factionalThreshold: Float = 0.85f,
  paddings: PaddingValues = PaddingValues(SlideToUnlockDefaults.Paddings),
  hintPaddings: PaddingValues = PaddingValues(start = thumbSize.width, end = thumbSize.width),
  onSlideFractionChanged: (Float) -> Unit = {},
  thumb: @Composable BoxScope.(isSlided: Boolean, slideFraction: Float, colors: SlideToUnlockColors, size: DpSize) -> Unit = { slided, _, _, size ->
    SlideToUnlockDefaults.Thumb(
      modifier = Modifier.size(thumbSize),
      isSlided = slided,
      colors = colors,
      thumbSize = thumbSize,
    )
  },
  hint: @Composable BoxScope.(isSlided: Boolean, slideFraction: Float, hintTexts: HintTexts, colors: SlideToUnlockColors, paddings: PaddingValues) -> Unit = { slided, fraction, _, _, paddings ->
    SlideToUnlockDefaults.Hint(
      modifier = Modifier.align(Alignment.Center),
      slideFraction = fraction,
      hintTexts = hintTexts,
      isSlided = slided,
      colors = colors,
      paddingValues = hintPaddings,
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
    factionalThreshold = factionalThreshold,
  ) {
    hint(
      isSlided,
      slideFraction,
      hintTexts,
      colors,
      hintPaddings,
    )

    Box(
      modifier = Modifier.offset {
        IntOffset(swipeState.offset.value.roundToInt(), 0)
      },
    ) {
      thumb(isSlided, slideFraction, colors, thumbSize)
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
  factionalThreshold: Float,
  thumbSize: DpSize,
  paddingValues: PaddingValues,
  content: @Composable BoxScope.() -> Unit,
) {
  val density = LocalDensity.current
  val layoutDirection = LocalLayoutDirection.current
  val trackColor = colors.trackColor(slideFraction)
  val trackBrush = colors.trackBrush(slideFraction)

  val startOfTrackPx = 0f
  var measuredWidth by remember { mutableIntStateOf(0) }
  val endOfTrackPx = remember(measuredWidth) {
    with(density) {
      val totalPadding = paddingValues.calculateStartPadding(layoutDirection) +
        paddingValues.calculateEndPadding(layoutDirection)
      measuredWidth - (totalPadding + thumbSize.width).toPx()
    }
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .onSizeChanged { measuredWidth = it.width }
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
        orientation = Orientation.Horizontal,
        anchors = mapOf(
          startOfTrackPx to SlideToUnlockValue.Start,
          endOfTrackPx to SlideToUnlockValue.End,
        ),
        thresholds = { _, _ -> FractionalThreshold(fraction = factionalThreshold) },
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
   * @param modifier The modifier to be applied to the thumb.
   */
  @Composable
  public fun Thumb(
    isSlided: Boolean,
    thumbSize: DpSize,
    colors: SlideToUnlockColors = DefaultSlideToUnlockColors(),
    modifier: Modifier = Modifier,
  ) {
    Box(
      modifier = modifier
        .size(thumbSize)
        .background(color = colors.thumbColor(), shape = CircleShape),
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
          imageVector = Icons.AutoMirrored.Filled.ArrowRight,
          tint = colors.thumbIconColor(),
          contentDescription = "Slide to unlock",
        )
      }
    }
  }

  /**
   * The default hint composable for the [SlideToUnlock] component.
   *
   * @param slideFraction The current progress of the swipe, from 0.0f to 1.0f.
   * @param modifier The modifier to be applied to the hint.
   */
  @Composable
  public fun Hint(
    hintTexts: HintTexts,
    isSlided: Boolean,
    slideFraction: Float,
    colors: SlideToUnlockColors,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
  ) {
    val layoutDirection = LocalLayoutDirection.current

    AnimatedContent(modifier = modifier.fillMaxWidth(), targetState = isSlided) { slided ->
      if (!slided) {
        Text(
          modifier = Modifier.fillMaxWidth().padding(
            start = paddingValues.calculateStartPadding(
              layoutDirection,
            ),
          ),
          text = hintTexts.defaultText,
          textAlign = TextAlign.Center,
          color = colors.hintColor(slideFraction),
          style = MaterialTheme.typography.titleMedium,
        )
      } else {
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = hintTexts.slidedText,
          textAlign = TextAlign.Center,
          color = colors.slidedHintColor(),
          style = MaterialTheme.typography.titleMedium,
        )
      }
    }
  }
}
