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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.revenuecat.purchases.slidetounlock.SlideToUnlockDefaults.ThumbSize
import com.skydoves.compose.effects.RememberedEffect
import kotlin.math.roundToInt

/**
 * Represents the two discrete anchor positions of the [SlideToUnlock] thumb.
 */
private enum class SlideToUnlockValue { Start, End }

/**
 * A fully customizable "slide-to-unlock" UI component for Compose (Android, Desktop and iOS).
 *
 * This component displays a track with a draggable thumb. The user can slide the thumb from the
 * start to the end to trigger an action. The current lifecycle of that action is expressed via
 * [SlideState]: while [SlideState.Idle] the thumb is draggable; for every other state the thumb is
 * locked at the end of the track and the appropriate indicator (loading / success / error) is shown.
 *
 * Supports horizontal (default) and vertical orientations via [SlideOrientation].
 *
 * Accessibility: the track exposes a [Role.Button] semantics node with a state description derived
 * from [hintTexts] and, while idle, an accessibility "click" action labeled [actionLabel]. The track
 * is also focusable so that pressing Enter or Space (while focused) triggers [onSlideCompleted].
 *
 * @param state The current [SlideState] of the action driven by this component.
 * @param onSlideCompleted Invoked when the user successfully completes the slide gesture (by dragging
 * the thumb past [fractionalThreshold]), activates the component via an accessibility action, or
 * presses Enter/Space while the track is focused. Only fires while [state] is [SlideState.Idle].
 * @param modifier Modifier to be applied to the layout.
 * @param colors Provides the color scheme for the track, thumb, and hint. Defaults to [DefaultSlideToUnlockColors].
 * @param hintTexts Provides the hint messages for each [SlideState].
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
 * @param actionLabel The label announced for the accessibility "click" action while [state] is idle.
 * @param contentDescription Optional content description for the track. Defaults to [HintTexts.defaultText].
 * @param animationsEnabled When `false`, the thumb snaps between positions instantly and the hint
 * does not cross-fade between states. Set this from your "reduce motion" preference.
 * @param thumb A composable slot for customizing the draggable thumb. It provides the slide state,
 * current slide fraction, colors, size, and orientation.
 * @param hint A composable slot for customizing the hint text or visuals inside the track. It provides
 * the slide state, current slide fraction, hint texts, colors, paddings, and orientation.
 */
@Composable
public fun SlideToUnlock(
  state: SlideState,
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
  actionLabel: String = "Activate",
  contentDescription: String? = null,
  animationsEnabled: Boolean = true,
  thumb: @Composable BoxScope.(state: SlideState, slideFraction: Float, colors: SlideToUnlockColors, size: DpSize, orientation: SlideOrientation) -> Unit = { slideState, _, _, _, orient ->
    SlideToUnlockDefaults.Thumb(
      modifier = Modifier.size(thumbSize),
      state = slideState,
      colors = colors,
      thumbSize = thumbSize,
      orientation = orient,
    )
  },
  hint: @Composable BoxScope.(state: SlideState, slideFraction: Float, hintTexts: HintTexts, colors: SlideToUnlockColors, paddings: PaddingValues, orientation: SlideOrientation) -> Unit = { slideState, fraction, _, _, _, orient ->
    SlideToUnlockDefaults.Hint(
      modifier = Modifier.align(Alignment.Center),
      slideFraction = fraction,
      hintTexts = hintTexts,
      state = slideState,
      colors = colors,
      paddingValues = hintPaddings,
      orientation = orient,
      animationsEnabled = animationsEnabled,
    )
  },
) {
  val hapticFeedback = LocalHapticFeedback.current
  val anchoredDraggableState = remember {
    AnchoredDraggableState(
      initialValue = if (state.isLocked) SlideToUnlockValue.End else SlideToUnlockValue.Start,
    )
  }

  val slideFraction by remember {
    derivedStateOf {
      val offset = anchoredDraggableState.offset
      val end = anchoredDraggableState.anchors.positionOf(SlideToUnlockValue.End)
      if (offset.isNaN() || end.isNaN() || end <= 0f) {
        if (state.isLocked) 1f else 0f
      } else {
        (offset / end).coerceIn(0f, 1f)
      }
    }
  }

  // Animate (or snap) the thumb to the appropriate anchor whenever the state changes.
  LaunchedEffect(state, animationsEnabled) {
    if (anchoredDraggableState.offset.isNaN()) return@LaunchedEffect
    val target = if (state.isLocked) SlideToUnlockValue.End else SlideToUnlockValue.Start
    if (anchoredDraggableState.currentValue == target) return@LaunchedEffect
    if (animationsEnabled) {
      anchoredDraggableState.animateTo(target)
    } else {
      anchoredDraggableState.snapTo(target)
    }
  }

  RememberedEffect(slideFraction) {
    onSlideFractionChanged.invoke(slideFraction)
  }

  // Fire onSlideCompleted exactly once when a user-driven drag settles at the end while idle.
  val currentState by rememberUpdatedState(state)
  val currentOnSlideCompleted by rememberUpdatedState(onSlideCompleted)
  LaunchedEffect(anchoredDraggableState) {
    snapshotFlow { anchoredDraggableState.settledValue }
      .collect { settled ->
        if (settled == SlideToUnlockValue.End && currentState == SlideState.Idle) {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
          currentOnSlideCompleted.invoke()
        }
      }
  }

  val onActivate: () -> Unit = {
    if (currentState == SlideState.Idle) {
      hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
      currentOnSlideCompleted.invoke()
    }
  }

  SlideToUnlockTrack(
    anchoredDraggableState = anchoredDraggableState,
    slideFraction = slideFraction,
    enabled = state == SlideState.Idle,
    modifier = modifier,
    colors = colors,
    trackShape = trackShape,
    thumbSize = thumbSize,
    paddingValues = paddings,
    fractionalThreshold = fractionalThreshold,
    orientation = orientation,
    animationsEnabled = animationsEnabled,
    slideState = state,
    hintTexts = hintTexts,
    actionLabel = actionLabel,
    contentDescription = contentDescription,
    onActivate = onActivate,
  ) {
    hint(
      state,
      slideFraction,
      hintTexts,
      colors,
      hintPaddings,
      orientation,
    )

    Box(
      modifier = Modifier.offset {
        val offset = anchoredDraggableState.offset.let { if (it.isNaN()) 0f else it }
        when (orientation) {
          SlideOrientation.Horizontal -> IntOffset(offset.roundToInt(), 0)
          SlideOrientation.Vertical -> IntOffset(0, offset.roundToInt())
        }
      },
    ) {
      thumb(state, slideFraction, colors, thumbSize, orientation)
    }
  }
}

/**
 * A "slide-to-unlock" UI component for Compose.
 *
 * @deprecated Use the [SlideToUnlock] overload that takes a [SlideState] instead. The boolean flag
 * cannot express the success and error states introduced in this version.
 */
@Deprecated(
  message = "Use SlideToUnlock(state = ...) with SlideState instead.",
  replaceWith = ReplaceWith(
    "SlideToUnlock(state = if (isSlided) SlideState.Loading else SlideState.Idle, " +
      "onSlideCompleted = onSlideCompleted, modifier = modifier)",
  ),
)
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
  thumb: @Composable BoxScope.(isSlided: Boolean, slideFraction: Float, colors: SlideToUnlockColors, size: DpSize, orientation: SlideOrientation) -> Unit = { slided, _, _, _, orient ->
    SlideToUnlockDefaults.Thumb(
      modifier = Modifier.size(thumbSize),
      state = if (slided) SlideState.Loading else SlideState.Idle,
      colors = colors,
      thumbSize = thumbSize,
      orientation = orient,
    )
  },
  hint: @Composable BoxScope.(isSlided: Boolean, slideFraction: Float, hintTexts: HintTexts, colors: SlideToUnlockColors, paddings: PaddingValues, orientation: SlideOrientation) -> Unit = { slided, fraction, _, _, _, orient ->
    SlideToUnlockDefaults.Hint(
      modifier = Modifier.align(Alignment.Center),
      slideFraction = fraction,
      hintTexts = hintTexts,
      state = if (slided) SlideState.Loading else SlideState.Idle,
      colors = colors,
      paddingValues = hintPaddings,
      orientation = orient,
    )
  },
) {
  SlideToUnlock(
    state = if (isSlided) SlideState.Loading else SlideState.Idle,
    onSlideCompleted = onSlideCompleted,
    modifier = modifier,
    colors = colors,
    hintTexts = hintTexts,
    trackShape = trackShape,
    thumbSize = thumbSize,
    fractionalThreshold = fractionalThreshold,
    paddings = paddings,
    hintPaddings = hintPaddings,
    onSlideFractionChanged = onSlideFractionChanged,
    orientation = orientation,
    thumb = { st, fr, c, sz, or -> thumb(st.isLocked, fr, c, sz, or) },
    hint = { st, fr, ht, c, pd, or -> hint(st.isLocked, fr, ht, c, pd, or) },
  )
}

/**
 * A private composable that represents the track of the SlideToUnlock component.
 * This composable handles the layout, gesture detection, accessibility and background drawing.
 */
@Composable
private fun SlideToUnlockTrack(
  anchoredDraggableState: AnchoredDraggableState<SlideToUnlockValue>,
  slideFraction: Float,
  enabled: Boolean,
  modifier: Modifier,
  colors: SlideToUnlockColors,
  trackShape: Shape,
  fractionalThreshold: Float,
  thumbSize: DpSize,
  paddingValues: PaddingValues,
  orientation: SlideOrientation,
  animationsEnabled: Boolean,
  slideState: SlideState,
  hintTexts: HintTexts,
  actionLabel: String,
  contentDescription: String?,
  onActivate: () -> Unit,
  content: @Composable BoxScope.() -> Unit,
) {
  val density = LocalDensity.current
  val layoutDirection = LocalLayoutDirection.current
  val trackColor = colors.trackColor(slideFraction)
  val trackBrush = colors.trackBrush(slideFraction)

  var measuredWidth by remember { mutableIntStateOf(0) }
  var measuredHeight by remember { mutableIntStateOf(0) }

  val endOfTrackPx = remember(
    measuredWidth,
    measuredHeight,
    orientation,
    thumbSize,
    paddingValues,
    density,
    layoutDirection,
  ) {
    with(density) {
      when (orientation) {
        SlideOrientation.Horizontal -> {
          val totalPadding = paddingValues.calculateStartPadding(layoutDirection) +
            paddingValues.calculateEndPadding(layoutDirection)
          (measuredWidth - (totalPadding + thumbSize.width).toPx()).coerceAtLeast(0f)
        }
        SlideOrientation.Vertical -> {
          val totalPadding = paddingValues.calculateTopPadding() +
            paddingValues.calculateBottomPadding()
          (measuredHeight - (totalPadding + thumbSize.height).toPx()).coerceAtLeast(0f)
        }
      }
    }
  }

  LaunchedEffect(endOfTrackPx) {
    anchoredDraggableState.updateAnchors(
      DraggableAnchors {
        SlideToUnlockValue.Start at 0f
        SlideToUnlockValue.End at endOfTrackPx
      },
    )
  }

  val flingBehavior = if (animationsEnabled) {
    AnchoredDraggableDefaults.flingBehavior(
      state = anchoredDraggableState,
      positionalThreshold = { totalDistance -> totalDistance * fractionalThreshold },
    )
  } else {
    AnchoredDraggableDefaults.flingBehavior(
      state = anchoredDraggableState,
      positionalThreshold = { totalDistance -> totalDistance * fractionalThreshold },
      animationSpec = snap(),
    )
  }

  val stateDescriptionText = when (slideState) {
    SlideState.Idle -> hintTexts.defaultText
    SlideState.Loading -> hintTexts.slidedText
    SlideState.Success -> hintTexts.successText ?: hintTexts.slidedText
    SlideState.Error -> hintTexts.errorText ?: hintTexts.slidedText
  }
  val trackContentDescription = contentDescription ?: hintTexts.defaultText

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
          background(brush = trackBrush, shape = trackShape)
        } else {
          background(color = trackColor, shape = trackShape)
        }
      }
      .semantics(mergeDescendants = true) {
        role = Role.Button
        this.contentDescription = trackContentDescription
        stateDescription = stateDescriptionText
        if (enabled) {
          onClick(label = actionLabel) {
            onActivate()
            true
          }
        } else {
          disabled()
        }
      }
      .onKeyEvent { event ->
        if (
          enabled &&
          event.type == KeyEventType.KeyUp &&
          (event.key == Key.Enter || event.key == Key.NumPadEnter || event.key == Key.Spacebar)
        ) {
          onActivate()
          true
        } else {
          false
        }
      }
      .focusable(enabled = enabled)
      .padding(paddingValues = paddingValues)
      .anchoredDraggable(
        state = anchoredDraggableState,
        orientation = when (orientation) {
          SlideOrientation.Horizontal -> Orientation.Horizontal
          SlideOrientation.Vertical -> Orientation.Vertical
        },
        enabled = enabled,
        flingBehavior = flingBehavior,
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

  /**
   * Retained for source compatibility. The fling/settle velocity is now managed internally by
   * [androidx.compose.foundation.gestures.anchoredDraggable] and this value is no longer wired up.
   */
  public val VelocityThreshold: Dp = 60.dp

  /**
   * The default thumb composable for the [SlideToUnlock] component.
   *
   * @param state The current [SlideState] of the component.
   * @param colors The [SlideToUnlockColors] used to customize the appearance of the component.
   * @param orientation The slide orientation.
   * @param modifier The modifier to be applied to the thumb.
   */
  @Composable
  public fun Thumb(
    state: SlideState,
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
      when (state) {
        SlideState.Loading -> {
          CircularProgressIndicator(
            modifier = Modifier.padding(8.dp),
            color = colors.progressColor(),
            strokeWidth = 3.dp,
          )
        }
        SlideState.Success -> {
          Icon(
            modifier = Modifier.align(Alignment.Center).size(36.dp),
            imageVector = Icons.Filled.Check,
            tint = colors.successIconColor(),
            contentDescription = "Completed",
          )
        }
        SlideState.Error -> {
          Icon(
            modifier = Modifier.align(Alignment.Center).size(36.dp),
            imageVector = Icons.Filled.Close,
            tint = colors.errorIconColor(),
            contentDescription = "Failed",
          )
        }
        SlideState.Idle -> {
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
  }

  /**
   * The default hint composable for the [SlideToUnlock] component.
   *
   * @param state The current [SlideState] of the component.
   * @param slideFraction The current progress of the swipe, from 0.0f to 1.0f.
   * @param orientation The slide orientation.
   * @param animationsEnabled When `false`, the hint switches between states without a cross-fade.
   * @param modifier The modifier to be applied to the hint.
   */
  @Composable
  public fun Hint(
    hintTexts: HintTexts,
    state: SlideState,
    slideFraction: Float,
    colors: SlideToUnlockColors,
    paddingValues: PaddingValues,
    orientation: SlideOrientation = SlideOrientation.Horizontal,
    animationsEnabled: Boolean = true,
    modifier: Modifier = Modifier,
  ) {
    val layoutDirection = LocalLayoutDirection.current

    val renderText: @Composable (text: String, color: Color, leading: Boolean) -> Unit = { text, color, leading ->
      when (orientation) {
        SlideOrientation.Horizontal -> {
          Text(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = if (leading) paddingValues.calculateStartPadding(layoutDirection) else 0.dp),
            text = text,
            textAlign = TextAlign.Center,
            color = color,
            style = MaterialTheme.typography.titleMedium,
          )
        }
        SlideOrientation.Vertical -> {
          Column(
            modifier = Modifier
              .fillMaxHeight()
              .padding(top = if (leading) paddingValues.calculateTopPadding() else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = text,
              textAlign = TextAlign.Center,
              color = color,
              style = MaterialTheme.typography.titleMedium,
            )
          }
        }
      }
    }

    val renderForState: @Composable (SlideState) -> Unit = { current ->
      when (current) {
        SlideState.Idle -> renderText(hintTexts.defaultText, colors.hintColor(slideFraction), true)
        SlideState.Loading -> renderText(hintTexts.slidedText, colors.slidedHintColor(), false)
        SlideState.Success -> renderText(hintTexts.successText ?: hintTexts.slidedText, colors.slidedHintColor(), false)
        SlideState.Error -> renderText(hintTexts.errorText ?: hintTexts.slidedText, colors.slidedHintColor(), false)
      }
    }

    val outerModifier = modifier.run {
      when (orientation) {
        SlideOrientation.Horizontal -> fillMaxWidth()
        SlideOrientation.Vertical -> fillMaxHeight()
      }
    }

    if (animationsEnabled) {
      AnimatedContent(modifier = outerModifier, targetState = state) { renderForState(it) }
    } else {
      Box(modifier = outerModifier) { renderForState(state) }
    }
  }
}
