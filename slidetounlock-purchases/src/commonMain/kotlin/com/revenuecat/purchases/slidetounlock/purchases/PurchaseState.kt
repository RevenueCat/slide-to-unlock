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
import com.revenuecat.purchases.kmp.ktx.SuccessfulPurchase
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import kotlin.coroutines.cancellation.CancellationException

/**
 * A sealed class representing the different states of a purchase operation.
 *
 * This class is designed to be used as UI state.
 *
 * By using a `sealed class`, we can ensure that all possible states of the purchase
 * flow (Loading, Success, Error) are handled exhaustively in a `when` expression,
 * which prevents runtime errors and makes the UI logic more robust.
 *
 * The class is marked as [Stable] to inform the Compose compiler that the type is stable.
 * This is a performance optimization, as it allows Compose to skip recomposing
 * composables that take this state as a parameter if the instance has not changed.
 */
@Stable
public sealed class PurchaseState {

  /**
   * Represents the state where the purchase operation is in progress.
   *
   * The UI should typically display a loading indicator, such as a CircularProgressIndicator,
   * and disable interactive elements when this state is active.
   * This is a `data object` because it has no properties and only one instance is needed.
   */
  public data object Loading : PurchaseState()

  /**
   * Represents the state where the purchase has completed successfully.
   *
   * @property successfulPurchase Contains the details of the successful transaction,
   * which can be used to update the UI or grant entitlements.
   */
  public data class Success(public val successfulPurchase: SuccessfulPurchase) : PurchaseState()

  /**
   * Represents the state where the purchase operation has failed.
   *
   * @property exception The exception that occurred during the purchase process.
   * This can be used to display an informative error message to the user
   * or for logging and debugging purposes. This can be one of [PurchasesTransactionException] or
   * [CancellationException].
   */
  public data class Error(public val exception: Exception) : PurchaseState()
}
