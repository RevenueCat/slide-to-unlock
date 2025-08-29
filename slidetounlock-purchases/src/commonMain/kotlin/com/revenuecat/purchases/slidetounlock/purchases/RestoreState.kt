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
package com.revenuecat.purchases.slidetounlock.purchases

import androidx.compose.runtime.Stable
import com.revenuecat.purchases.kmp.models.CustomerInfo

/**
 * Represents the current state of an asynchronous purchase restoration process.
 *
 * This sealed class is used by the `onRestoreStateChanged` callback in the
 * [com.revenuecat.purchases.slidetounlock.SlideToRestore] composable to communicate
 * the status of the underlying `Purchases.restorePurchases()` operation.
 */
@Stable
public sealed class RestoreState {

  /**
   * Indicates that the purchase restoration process is currently in progress.
   * A network request has been initiated.
   */
  public data object Loading : RestoreState()

  /**
   * Indicates that the purchase restoration completed successfully.
   *
   * @property customerInfo The latest [CustomerInfo] object retrieved after restoring purchases.
   * This object contains the updated entitlement and subscription status for the user.
   */
  public data class Success(public val customerInfo: CustomerInfo) : RestoreState()

  /**
   * Indicates that an error occurred during the purchase restoration process.
   *
   * This state is typically reached due to network issues, device configuration problems,
   * or internal RevenueCat API errors.
   *
   * @property exception The [Exception] detailing the reason for the restoration failure.
   */
  public data class Error(public val exception: Exception) : RestoreState()
}
