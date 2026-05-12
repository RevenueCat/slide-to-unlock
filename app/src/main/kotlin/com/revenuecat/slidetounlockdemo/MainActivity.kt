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
package com.revenuecat.slidetounlockdemo

import SlideToRestore
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revenuecat.purchases.slidetounlock.DefaultSlideToUnlockColors
import com.revenuecat.purchases.slidetounlock.HintTexts
import com.revenuecat.purchases.slidetounlock.SlideOrientation
import com.revenuecat.purchases.slidetounlock.SlideState
import com.revenuecat.purchases.slidetounlock.SlideToUnlock
import com.revenuecat.purchases.slidetounlock.SlideToUnlockColors
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      MaterialTheme {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2B292B))
            .verticalScroll(rememberScrollState())
            .padding(vertical = 60.dp, horizontal = 20.dp),
          verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
          SlideToUnlockStyle0()

          SlideToUnlockStyle1()

          SlideToUnlockStyle2()

          SlideToUnlockStyle3()

          SlideToUnlockStyle4()

          SlideToUnlockStyle5()

          SlideToUnlockAccessible()
        }
      }
    }
  }
}

@Preview
@Composable
private fun SlideToUnlockStyle0() {
  var slideState by remember { mutableStateOf(SlideState.Idle) }

  SlideToUnlock(
    state = slideState,
    modifier = Modifier.fillMaxWidth(),
    hintTexts = HintTexts.defaultHintTexts().copy(
      defaultText = "Slide to purchase",
    ),
    colors = DefaultSlideToUnlockColors(slidedHintColor = Color.White),
    onSlideCompleted = { slideState = SlideState.Loading },
  )
}

@Preview
@Composable
private fun SlideToUnlockStyle1() {
  var slideState by remember { mutableStateOf(SlideState.Idle) }

  SlideToUnlock(
    state = slideState,
    modifier = Modifier.fillMaxWidth(),
    trackShape = RoundedCornerShape(10.dp),
    hintTexts = HintTexts(
      defaultText = "Slide to subscribe",
      slidedText = "Subscribing...",
    ),
    colors = DefaultSlideToUnlockColors(
      endTrackColor = Color(0xFFF2545B),
      slidedHintColor = Color.White,
    ),
    onSlideCompleted = { slideState = SlideState.Loading },

    thumb = { _, _, _, size, _ ->
      Box(
        modifier = Modifier.size(size),
      ) {
        Image(
          modifier = Modifier
            .align(Alignment.Center)
            .size(40.dp),
          painter = painterResource(R.drawable.app_icon),
          contentDescription = "Slide to subscribe",
        )
      }
    },

    hint = { state, fraction, hintTexts, colors, paddings, _ ->
      val layoutDirection = LocalLayoutDirection.current

      AnimatedContent(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.Center),
        targetState = state,
      ) { current ->
        if (current == SlideState.Idle) {
          Text(
            modifier = Modifier
              .fillMaxWidth()
              .padding(
                start = paddings.calculateStartPadding(
                  layoutDirection,
                ),
              ),
            text = hintTexts.defaultText,
            textAlign = TextAlign.Center,
            color = colors.hintColor(fraction),
            style = MaterialTheme.typography.titleMedium,
          )
        } else {
          Row(
            horizontalArrangement = Arrangement.Center,
          ) {
            CircularProgressIndicator(
              modifier = Modifier
                .padding(end = 6.dp)
                .size(30.dp),
              color = Color.White,
            )

            Text(
              modifier = Modifier.align(Alignment.CenterVertically),
              text = hintTexts.slidedText,
              textAlign = TextAlign.Center,
              color = colors.slidedHintColor(),
              style = MaterialTheme.typography.titleMedium,
            )
          }
        }
      }
    },
  )
}

@Preview
@Composable
private fun SlideToUnlockStyle2() {
  val context = LocalContext.current
  var slideState by remember { mutableStateOf(SlideState.Idle) }

  val colorStops = arrayOf(
    0.0f to Color.Black,
    1f to Color(0xDC393636),
  )
  SlideToUnlock(
    state = slideState,
    modifier = Modifier.fillMaxWidth(),
    trackShape = RoundedCornerShape(10.dp),
    thumbSize = DpSize(width = 65.dp, height = 45.dp),
    colors = DefaultSlideToUnlockColors(
      slidedHintColor = Color.White,
      thumbColor = Color(0xDC9B9B9F),
      thumbIconColor = Color(0xDC49494B),
      trackBrush = Brush.verticalGradient(colorStops = colorStops),
    ),
    onSlideCompleted = {
      slideState = SlideState.Loading
      Toast.makeText(context, "unlocked!", Toast.LENGTH_SHORT).show()
    },
    thumb = { _, _, colors, size, _ ->
      val thumbColorStops = arrayOf(
        0.0f to Color.White,
        1f to colors.thumbColor(),
      )
      Box(
        modifier = Modifier
          .size(size)
          .background(
            brush = Brush.verticalGradient(colorStops = thumbColorStops),
            shape = RoundedCornerShape(10.dp),
          ),
      ) {
        Icon(
          modifier = Modifier
            .align(Alignment.Center)
            .size(30.dp),
          painter = painterResource(R.drawable.arrow),
          tint = colors.thumbIconColor(),
          contentDescription = "Slide to unlock",
        )
      }
    },
    hint = { _, fraction, hintTexts, colors, paddings, _ ->
      val layoutDirection = LocalLayoutDirection.current
      Crossfade(
        modifier = Modifier
          .shimmer()
          .align(Alignment.Center),
        targetState = slideState,
      ) { _ ->
        Text(
          modifier = Modifier.padding(
            start = paddings.calculateStartPadding(layoutDirection),
          ),
          text = hintTexts.defaultText,
          color = colors.hintColor(fraction),
          fontSize = 21.sp,
        )
      }
    },
  )
}

@Preview
@Composable
private fun SlideToUnlockStyle3() {
  var slideState by remember { mutableStateOf(SlideState.Idle) }
  val animateColor: Float by animateFloatAsState(
    if (slideState == SlideState.Success) 1f else 0f,
    label = "alpha",
    animationSpec = tween(durationMillis = 700),
  )
  val colors = if (slideState == SlideState.Success) {
    DefaultSlideToUnlockColors(
      endTrackColor = lerp(Color(0xFFB4AFB4), Color(0xFF11D483), animateColor),
      slidedHintColor = Color.White,
      thumbIconColor = Color(0xFF11D483),
    )
  } else {
    DefaultSlideToUnlockColors(
      endTrackColor = Color(0xFFB4AFB4),
      slidedHintColor = Color.White,
    )
  }

  LaunchedEffect(slideState) {
    if (slideState == SlideState.Loading) {
      delay(1500)
      slideState = SlideState.Success
    }
  }

  SlideToUnlock(
    state = slideState,
    modifier = Modifier.fillMaxWidth(),
    hintTexts = HintTexts.defaultHintTexts().copy(
      defaultText = "Restore my products",
      slidedText = "Restoring...",
      successText = "Restored!",
    ),
    colors = colors,
    onSlideCompleted = { slideState = SlideState.Loading },
    thumb = { state, fraction, thumbColors, size, _ ->
      Box(
        modifier = Modifier
          .size(size)
          .background(color = thumbColors.thumbColor(), shape = CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        when (state) {
          SlideState.Loading -> CircularProgressIndicator(
            modifier = Modifier.padding(8.dp),
            color = thumbColors.progressColor(),
            strokeWidth = 3.dp,
          )
          SlideState.Idle -> Icon(
            modifier = Modifier
              .size(30.dp)
              .rotate(fraction * -360),
            imageVector = Icons.Default.Restore,
            tint = thumbColors.thumbIconColor(),
            contentDescription = "Slide to restore",
          )
          else -> SlideToUnlockDefaultThumbIcon(state, thumbColors)
        }
      }
    },
  )
}

@Preview
@Composable
private fun SlideToUnlockStyle4() {
  var slideState by remember { mutableStateOf(SlideState.Idle) }
  val animateColor: Float by animateFloatAsState(
    if (slideState == SlideState.Error) 1f else 0f,
    label = "alpha",
    animationSpec = tween(durationMillis = 700),
  )
  val colors = if (slideState == SlideState.Error) {
    DefaultSlideToUnlockColors(
      endTrackColor = lerp(Color(0xFFB4AFB4), Color(0xFFC91224), animateColor),
      slidedHintColor = Color.White,
      thumbIconColor = Color(0xFFC91224),
    )
  } else {
    DefaultSlideToUnlockColors(
      endTrackColor = Color(0xFFB4AFB4),
      slidedHintColor = Color.White,
    )
  }

  LaunchedEffect(slideState) {
    if (slideState == SlideState.Loading) {
      delay(1500)
      slideState = SlideState.Error
    }
  }

  SlideToUnlock(
    state = slideState,
    modifier = Modifier.fillMaxWidth(),
    hintTexts = HintTexts.defaultHintTexts().copy(
      defaultText = "Restore my products",
      slidedText = "Restoring...",
      errorText = "Failed: check your account",
    ),
    colors = colors,
    onSlideCompleted = { slideState = SlideState.Loading },
    thumb = { state, fraction, thumbColors, size, _ ->
      Box(
        modifier = Modifier
          .size(size)
          .background(color = thumbColors.thumbColor(), shape = CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        when (state) {
          SlideState.Loading -> CircularProgressIndicator(
            modifier = Modifier.padding(8.dp),
            color = thumbColors.progressColor(),
            strokeWidth = 3.dp,
          )
          SlideState.Idle -> Icon(
            modifier = Modifier
              .size(30.dp)
              .rotate(fraction * -360),
            imageVector = Icons.Default.Restore,
            tint = thumbColors.thumbIconColor(),
            contentDescription = "Slide to restore",
          )
          else -> SlideToUnlockDefaultThumbIcon(state, thumbColors)
        }
      }
    },
  )
}

@Composable
private fun SlideToUnlockStyle5() {
  var slideState by remember { mutableStateOf(SlideState.Idle) }
  val animateColor: Float by animateFloatAsState(
    if (slideState == SlideState.Error) 1f else 0f,
    label = "alpha",
    animationSpec = tween(durationMillis = 700),
  )
  val colors = if (slideState == SlideState.Error) {
    DefaultSlideToUnlockColors(
      endTrackColor = lerp(Color(0xFFB4AFB4), Color(0xFFC91224), animateColor),
      slidedHintColor = Color.White,
      thumbIconColor = Color(0xFFC91224),
    )
  } else {
    DefaultSlideToUnlockColors(
      endTrackColor = Color(0xFFB4AFB4),
      slidedHintColor = Color.White,
    )
  }

  LaunchedEffect(slideState) {
    if (slideState == SlideState.Loading) {
      delay(1500)
      slideState = SlideState.Error
    }
  }

  SlideToUnlock(
    state = slideState,
    hintTexts = HintTexts.defaultHintTexts().copy(
      defaultText = "Slide down",
      slidedText = "Loading..",
      errorText = "Failed",
    ),
    colors = colors,
    onSlideCompleted = { slideState = SlideState.Loading },
    orientation = SlideOrientation.Vertical,
    thumb = { state, fraction, thumbColors, size, _ ->
      Box(
        modifier = Modifier
          .size(size)
          .background(color = thumbColors.thumbColor(), shape = CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        when (state) {
          SlideState.Loading -> CircularProgressIndicator(
            modifier = Modifier.padding(8.dp),
            color = thumbColors.progressColor(),
            strokeWidth = 3.dp,
          )
          SlideState.Idle -> Icon(
            modifier = Modifier
              .size(30.dp)
              .rotate(fraction * -360),
            imageVector = Icons.Default.Restore,
            tint = thumbColors.thumbIconColor(),
            contentDescription = "Slide down to unlock",
          )
          else -> SlideToUnlockDefaultThumbIcon(state, thumbColors)
        }
      }
    },
    hint = { state, fraction, hintTexts, hintColors, _, _ ->
      AnimatedContent(
        modifier = Modifier
          .align(Alignment.Center)
          .onGloballyPositioned {},
        targetState = state,
      ) { current ->
        val text = when (current) {
          SlideState.Idle -> hintTexts.defaultText
          SlideState.Loading -> hintTexts.slidedText
          SlideState.Success -> hintTexts.successText ?: hintTexts.slidedText
          SlideState.Error -> hintTexts.errorText ?: hintTexts.slidedText
        }
        StackedVerticalText(
          text = text,
          textColor = if (current == SlideState.Idle) hintColors.hintColor(fraction) else hintColors.slidedHintColor(),
        )
      }
    },
  )
}

/**
 * Showcases the accessibility hooks: a custom [SlideToUnlock] action label, a content description,
 * and the `successText` hint. The component is fully usable with a screen reader or a keyboard
 * (focus the track and press Enter/Space).
 */
@Composable
private fun SlideToUnlockAccessible() {
  var slideState by remember { mutableStateOf(SlideState.Idle) }

  LaunchedEffect(slideState) {
    if (slideState == SlideState.Loading) {
      delay(1500)
      slideState = SlideState.Success
    }
  }

  SlideToUnlock(
    state = slideState,
    modifier = Modifier.fillMaxWidth(),
    hintTexts = HintTexts(
      defaultText = "Slide to confirm",
      slidedText = "Confirming...",
      successText = "Confirmed",
    ),
    colors = DefaultSlideToUnlockColors(
      endTrackColor = Color(0xFF11D483),
      slidedHintColor = Color.White,
    ),
    actionLabel = "Confirm",
    contentDescription = "Confirm your action by sliding the thumb to the end",
    onSlideCompleted = { slideState = SlideState.Loading },
  )
}

@Composable
private fun SlideToUnlockDefaultThumbIcon(
  state: SlideState,
  colors: SlideToUnlockColors,
) {
  when (state) {
    SlideState.Success -> Icon(
      modifier = Modifier.size(30.dp),
      imageVector = Icons.Default.Check,
      tint = colors.successIconColor(),
      contentDescription = "Completed",
    )
    SlideState.Error -> Icon(
      modifier = Modifier.size(30.dp),
      imageVector = Icons.Default.Close,
      tint = colors.errorIconColor(),
      contentDescription = "Failed",
    )
    else -> Unit
  }
}

@Composable
private fun StackedVerticalText(
  text: String,
  modifier: Modifier = Modifier,
  textColor: Color = Color.Black,
) {
  Column(
    modifier = modifier
      .heightIn(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    text.forEach { char ->
      Text(
        text = char.toString(),
        textAlign = TextAlign.Center,
        color = textColor,
        style = MaterialTheme.typography.titleMedium,
      )
    }
  }
}

@Preview
@Composable
private fun SlideToPurchasesPreview() {
  SlideToRestore()
}
