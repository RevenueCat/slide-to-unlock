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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revenuecat.slidetounlock.DefaultSlideToUnlockColors
import com.revenuecat.slidetounlock.HintTexts
import com.revenuecat.slidetounlock.SlideToUnlock
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
            .padding(vertical = 60.dp, horizontal = 20.dp),
          verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
          SlideToUnlockStyle0()

          SlideToUnlockStyle1()

          SlideToUnlockStyle2()

          SlideToUnlockStyle3()

          SlideToUnlockStyle4()
        }
      }
    }
  }
}

@Composable
private fun SlideToUnlockStyle0() {
  var isSlided by remember { mutableStateOf(false) }

  SlideToUnlock(
    isSlided = isSlided,
    modifier = Modifier.fillMaxWidth(),
    hintTexts = HintTexts.defaultHintTexts().copy(
      defaultText = "Slide to purchase",
    ),
    colors = DefaultSlideToUnlockColors(slidedHintColor = Color.White),
    onSlideCompleted = { isSlided = true },
  )
}

@Composable
private fun SlideToUnlockStyle1() {
  var isSlided by remember { mutableStateOf(false) }

  SlideToUnlock(
    isSlided = isSlided,
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
    onSlideCompleted = { isSlided = true },
    thumb = { slided, fraction, colors, size ->
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
    hint = { slided, fraction, hintTexts, colors, paddings ->
      val layoutDirection = LocalLayoutDirection.current

      AnimatedContent(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.Center),
        targetState = isSlided,
      ) { slided ->
        if (!slided) {
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

@Composable
private fun SlideToUnlockStyle2() {
  val context = LocalContext.current
  var isSlided by remember { mutableStateOf(false) }

  val colorStops = arrayOf(
    0.0f to Color.Black,
    1f to Color(0xDC393636),
  )
  SlideToUnlock(
    isSlided = isSlided,
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
      isSlided = true
      Toast.makeText(context, "unlocked!", Toast.LENGTH_SHORT).show()
    },
    thumb = { slided, fraction, colors, size ->
      val colorStops = arrayOf(
        0.0f to Color.White,
        1f to colors.thumbColor(),
      )
      Box(
        modifier = Modifier
          .size(size)
          .background(
            brush = Brush.verticalGradient(colorStops = colorStops),
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
    hint = { slided, fraction, hintTexts, colors, paddings ->
      val layoutDirection = LocalLayoutDirection.current
      Crossfade(
        modifier = Modifier
          .shimmer()
          .align(Alignment.Center),
        targetState = isSlided,
      ) { slided ->
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

@Composable
private fun SlideToUnlockStyle3() {
  var isSlided by remember { mutableStateOf(false) }
  var isCompleted by remember { mutableStateOf(false) }
  val animateColor: Float by animateFloatAsState(
    if (isCompleted) 1f else 0f,
    label = "alpha",
    animationSpec = tween(durationMillis = 700),
  )
  val colors = if (isCompleted) {
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

  LaunchedEffect(isSlided) {
    if (isSlided) {
      delay(1500)
      isCompleted = true
    }
  }

  SlideToUnlock(
    isSlided = isSlided,
    modifier = Modifier.fillMaxWidth(),
    hintTexts = HintTexts.defaultHintTexts().copy(
      defaultText = "Restore my products",
    ),
    colors = colors,
    onSlideCompleted = { isSlided = true },
    thumb = { slided, fraction, colors, size ->
      Box(
        modifier = Modifier
          .size(size)
          .background(color = colors.thumbColor(), shape = CircleShape),
      ) {
        if (isCompleted) {
          Icon(
            modifier = Modifier
              .align(Alignment.Center)
              .size(30.dp),
            imageVector = Icons.Default.Done,
            tint = colors.thumbIconColor(),
            contentDescription = "Completed",
          )
        } else if (isSlided) {
          CircularProgressIndicator(
            modifier = Modifier.padding(8.dp),
            color = colors.progressColor(),
            strokeWidth = 3.dp,
          )
        } else {
          Icon(
            modifier = Modifier
              .align(Alignment.Center)
              .size(30.dp)
              .rotate(fraction * -360),
            imageVector = Icons.Default.Restore,
            tint = colors.thumbIconColor(),
            contentDescription = "Slide to unlock",
          )
        }
      }
    },
    hint = { slided, fraction, hintTexts, colors, paddings ->
      val layoutDirection = LocalLayoutDirection.current

      AnimatedContent(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.Center),
        targetState = isSlided,
      ) { slided ->
        if (isCompleted) {
          Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Completed!",
            textAlign = TextAlign.Center,
            color = colors.slidedHintColor(),
            style = MaterialTheme.typography.titleMedium,
          )
        } else if (!slided) {
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
          Text(
            modifier = Modifier.fillMaxWidth(),
            text = hintTexts.slidedText,
            textAlign = TextAlign.Center,
            color = colors.slidedHintColor(),
            style = MaterialTheme.typography.titleMedium,
          )
        }
      }
    },
  )
}

@Composable
private fun SlideToUnlockStyle4() {
  var isSlided by remember { mutableStateOf(false) }
  var isCompleted by remember { mutableStateOf(false) }
  val animateColor: Float by animateFloatAsState(
    if (isCompleted) 1f else 0f,
    label = "alpha",
    animationSpec = tween(durationMillis = 700),
  )
  val colors = if (isCompleted) {
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

  LaunchedEffect(isSlided) {
    if (isSlided) {
      delay(1500)
      isCompleted = true
    }
  }

  SlideToUnlock(
    isSlided = isSlided,
    modifier = Modifier.fillMaxWidth(),
    hintTexts = HintTexts.defaultHintTexts().copy(
      defaultText = "Restore my products",
    ),
    colors = colors,
    onSlideCompleted = { isSlided = true },
    thumb = { slided, fraction, colors, size ->
      Box(
        modifier = Modifier
          .size(size)
          .background(color = colors.thumbColor(), shape = CircleShape),
      ) {
        if (isCompleted) {
          Icon(
            modifier = Modifier
              .align(Alignment.Center)
              .size(30.dp),
            imageVector = Icons.Default.Error,
            tint = colors.thumbIconColor(),
            contentDescription = "Failed",
          )
        } else if (isSlided) {
          CircularProgressIndicator(
            modifier = Modifier.padding(8.dp),
            color = colors.progressColor(),
            strokeWidth = 3.dp,
          )
        } else {
          Icon(
            modifier = Modifier
              .align(Alignment.Center)
              .size(30.dp)
              .rotate(fraction * -360),
            imageVector = Icons.Default.Restore,
            tint = colors.thumbIconColor(),
            contentDescription = "Slide to unlock",
          )
        }
      }
    },
    hint = { slided, fraction, hintTexts, colors, paddings ->
      val layoutDirection = LocalLayoutDirection.current

      AnimatedContent(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.Center),
        targetState = isSlided,
      ) { slided ->
        if (isCompleted) {
          Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Failed: Check out your account",
            textAlign = TextAlign.Center,
            color = colors.slidedHintColor(),
            style = MaterialTheme.typography.titleMedium,
          )
        } else if (!slided) {
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
          Text(
            modifier = Modifier.fillMaxWidth(),
            text = hintTexts.slidedText,
            textAlign = TextAlign.Center,
            color = colors.slidedHintColor(),
            style = MaterialTheme.typography.titleMedium,
          )
        }
      }
    },
  )
}
