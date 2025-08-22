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

/**
 * Represents the orientation of the sliding gesture for the component.
 *
 * This enum is used to specify whether the draggable thumb should move
 * horizontally (left-to-right) or vertically (top-to-bottom).
 */
public enum class SlideOrientation {
  /**
   * Indicates that the sliding gesture is horizontal.
   * The thumb will move along the x-axis.
   */
  Horizontal,

  /**
   * Indicates that the sliding gesture is vertical.
   * The thumb will move along the y-axis.
   */
  Vertical,
}
