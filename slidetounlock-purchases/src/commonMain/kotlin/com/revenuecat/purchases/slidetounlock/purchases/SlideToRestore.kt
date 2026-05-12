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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpSize
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.slidetounlock.DefaultSlideToUnlockColors
import com.revenuecat.purchases.slidetounlock.HintTexts
import com.revenuecat.purchases.slidetounlock.SlideOrientation
import com.revenuecat.purchases.slidetounlock.SlideState
import com.revenuecat.purchases.slidetounlock.SlideToUnlock
import com.revenuecat.purchases.slidetounlock.SlideToUnlockColors
import com.revenuecat.purchases.slidetounlock.SlideToUnlockDefaults
import com.revenuecat.purchases.slidetounlock.SlideToUnlockDefaults.ThumbSize
import com.revenuecat.purchases.slidetounlock.purchases.RestoreState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val SlideStateSaver: Saver<SlideState, String> =
  Saver(save = { it.name }, restore = { SlideState.valueOf(it) })

/**
 * A composable that provides a slide-to-restore functionality.
 *
 * This component is built on top of the [SlideToUnlock] composable and handles the logic for
 * restoring purchases using the RevenueCat SDK. It drives the component through the
 * [SlideState] lifecycle (loading → success / error) and also reports each transition through the
 * [onRestoreStateChanged] callback.
 *
 * @param modifier The modifier to be applied to the component.
 * @param onRestoreStateChanged A callback that is invoked when the restore state changes.
 * It provides the current [RestoreState].
 * @param colors The colors to be used for the component. Defaults to [DefaultSlideToUnlockColors].
 * @param hintTexts The texts to be displayed as hints. Defaults to a restore-specific hint.
 * @param trackShape Defines the shape of the background track. Defaults to a rounded rectangle.
 * @param thumbSize The size of the draggable thumb.
 * @param fractionalThreshold The fraction of the track (from 0.0f to 1.0f) that the user must
 * slide the thumb past to trigger the restore process. Defaults to 0.85f (85%).
 * @param paddings The external padding values to be applied to the entire component.
 * @param hintPaddings The internal padding values for the hint composable.
 * @param onSlideFractionChanged Optional callback invoked with the current slide progress fraction (0f–1f).
 * @param onSlideCompleted A callback that is invoked when the slide gesture is completed, just before
 * the restore process begins.
 * @param orientation The direction of the sliding gesture. Defaults to [SlideOrientation.Horizontal].
 * @param actionLabel The label announced for the accessibility "activate" action while idle.
 * @param contentDescription Optional content description for the track.
 * @param animationsEnabled When `false`, the thumb snaps between positions and the hint does not cross-fade.
 * @param thumb A composable slot for customizing the draggable thumb. It provides the slide state,
 * current slide fraction, colors, size, and orientation.
 * @param hint A composable slot for customizing the hint text or visuals inside the track. It provides
 * the slide state, current slide fraction, hint texts, colors, paddings, and orientation.
 */
@Composable
public fun SlideToRestore(
  modifier: Modifier = Modifier,
  onRestoreStateChanged: (RestoreState) -> Unit = {},
  colors: SlideToUnlockColors = DefaultSlideToUnlockColors(),
  hintTexts: HintTexts = HintTexts.defaultHintTexts().copy(defaultText = "Slide to restore"),
  trackShape: Shape = RoundedCornerShape(percent = 50),
  thumbSize: DpSize = DpSize(ThumbSize, ThumbSize),
  fractionalThreshold: Float = 0.85f,
  paddings: PaddingValues = PaddingValues(SlideToUnlockDefaults.Paddings),
  hintPaddings: PaddingValues = PaddingValues(start = thumbSize.width, end = thumbSize.width),
  onSlideFractionChanged: (Float) -> Unit = {},
  onSlideCompleted: () -> Unit = {},
  orientation: SlideOrientation = SlideOrientation.Horizontal,
  actionLabel: String = "Restore purchases",
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
  val scope = rememberCoroutineScope()
  var slideState by rememberSaveable(stateSaver = SlideStateSaver) { mutableStateOf(SlideState.Idle) }

  SlideToUnlock(
    modifier = modifier,
    state = slideState,
    onSlideCompleted = {
      onSlideCompleted.invoke()
      scope.launch {
        slideState = SlideState.Loading
        onRestoreStateChanged.invoke(RestoreState.Loading)
        try {
          val customerInfo = withContext(Dispatchers.IO) {
            Purchases.sharedInstance.awaitRestore()
          }
          slideState = SlideState.Success
          onRestoreStateChanged.invoke(RestoreState.Success(customerInfo = customerInfo))
        } catch (e: Exception) {
          slideState = SlideState.Error
          onRestoreStateChanged.invoke(RestoreState.Error(exception = e))
        }
      }
    },
    colors = colors,
    hintTexts = hintTexts,
    trackShape = trackShape,
    thumbSize = thumbSize,
    fractionalThreshold = fractionalThreshold,
    paddings = paddings,
    hintPaddings = hintPaddings,
    onSlideFractionChanged = onSlideFractionChanged,
    orientation = orientation,
    actionLabel = actionLabel,
    contentDescription = contentDescription,
    animationsEnabled = animationsEnabled,
    thumb = thumb,
    hint = hint,
  )
}
