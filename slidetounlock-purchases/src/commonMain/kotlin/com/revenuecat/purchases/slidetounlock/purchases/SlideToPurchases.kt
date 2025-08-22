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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.SuccessfulPurchase
import com.revenuecat.purchases.kmp.ktx.awaitEligibleWinBackOffersForPackage
import com.revenuecat.purchases.kmp.ktx.awaitEligibleWinBackOffersForProduct
import com.revenuecat.purchases.kmp.ktx.awaitPromotionalOffer
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.models.GoogleReplacementMode
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PromotionalOffer
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.SubscriptionOption
import com.revenuecat.purchases.kmp.models.WinBackOffer
import com.revenuecat.purchases.slidetounlock.DefaultSlideToUnlockColors
import com.revenuecat.purchases.slidetounlock.HintTexts
import com.revenuecat.purchases.slidetounlock.SlideToUnlock
import com.revenuecat.purchases.slidetounlock.SlideToUnlockColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A composable that provides a "slide to unlock" UI to initiate a purchase for a [StoreProduct].
 * When the user slides the component to the end, it triggers the RevenueCat purchase flow.
 *
 * On the Play Store, this function supports upgrading a subscription by providing [oldProductId].
 * The default [SubscriptionOption] is chosen automatically based on RevenueCat's logic, which
 * prioritizes free trials and introductory offers.
 *
 * @param storeProduct The [StoreProduct] the user intends to purchase.
 * @param modifier The [Modifier] to be applied to this component.
 * @param colors The [SlideToUnlockColors] used to customize the appearance.
 * @param hintTexts The [HintTexts] to display in the component's track.
 * @param onSlideCompleted A callback invoked immediately when the user completes the slide gesture,
 *                         before the purchase flow begins.
 * @param onPurchaseStateChanged A callback that reports the current [PurchaseState] of the transaction
 *                               (Loading, Success, or Error).
 * @param isPersonalizedPrice (Google Play only) An optional boolean indicating personalized pricing.
 *                            See [Google's documentation](https://developer.android.com/google/play/billing/integrate#personalized-price) for details.
 * @param oldProductId (Google Play only) The product ID of a subscription to upgrade from.
 * @param replacementMode (Google Play only) The [GoogleReplacementMode] to use for the subscription upgrade.
 *                        This is ignored if [oldProductId] is null.
 */
@Composable
public fun SlideToPurchases(
  storeProduct: StoreProduct,
  modifier: Modifier = Modifier,
  colors: SlideToUnlockColors = DefaultSlideToUnlockColors(),
  hintTexts: HintTexts = HintTexts.defaultHintTexts(),
  onSlideCompleted: () -> Unit = {},
  onPurchaseStateChanged: (PurchaseState) -> Unit = {},
  isPersonalizedPrice: Boolean? = null,
  oldProductId: String? = null,
  replacementMode: GoogleReplacementMode = GoogleReplacementMode.WITHOUT_PRORATION,
) {
  InternalSlideToPurchases(
    modifier = modifier,
    onPurchaseStateChanged = onPurchaseStateChanged,
    onPurchase = {
      onSlideCompleted.invoke()
      Purchases.sharedInstance.awaitPurchase(
        storeProduct = storeProduct,
        isPersonalizedPrice = isPersonalizedPrice,
        oldProductId = oldProductId,
        replacementMode = replacementMode,
      )
    },
    colors = colors,
    hintTexts = hintTexts,
  )
}

/**
 * A composable that provides a "slide to unlock" UI to initiate a purchase for a [Package].
 * When the user slides the component to the end, it triggers the RevenueCat purchase flow.
 *
 * On the Play Store, this function supports upgrading a subscription by providing [oldProductId].
 * The default [SubscriptionOption] is chosen automatically from the [packageToPurchase].
 *
 * @param packageToPurchase The [Package] the user intends to purchase.
 * @param modifier The [Modifier] to be applied to this component.
 * @param colors The [SlideToUnlockColors] used to customize the appearance.
 * @param hintTexts The [HintTexts] to display in the component's track.
 * @param onSlideCompleted A callback invoked immediately when the user completes the slide gesture,
 *                         before the purchase flow begins.
 * @param onPurchaseStateChanged A callback that reports the current [PurchaseState] of the transaction
 *                               (Loading, Success, or Error).
 * @param isPersonalizedPrice (Google Play only) An optional boolean indicating personalized pricing.
 * @param oldProductId (Google Play only) The product ID of a subscription to upgrade from.
 * @param replacementMode (Google Play only) The [GoogleReplacementMode] to use for the subscription upgrade.
 */
@Composable
public fun SlideToPurchases(
  packageToPurchase: Package,
  modifier: Modifier = Modifier,
  colors: SlideToUnlockColors = DefaultSlideToUnlockColors(),
  hintTexts: HintTexts = HintTexts.defaultHintTexts(),
  onSlideCompleted: () -> Unit = {},
  onPurchaseStateChanged: (PurchaseState) -> Unit = {},
  isPersonalizedPrice: Boolean? = null,
  oldProductId: String? = null,
  replacementMode: GoogleReplacementMode = GoogleReplacementMode.WITHOUT_PRORATION,
) {
  InternalSlideToPurchases(
    modifier = modifier,
    onPurchaseStateChanged = onPurchaseStateChanged,
    onPurchase = {
      onSlideCompleted.invoke()
      Purchases.sharedInstance.awaitPurchase(
        packageToPurchase = packageToPurchase,
        isPersonalizedPrice = isPersonalizedPrice,
        oldProductId = oldProductId,
        replacementMode = replacementMode,
      )
    },
    colors = colors,
    hintTexts = hintTexts,
  )
}

/**
 * A composable that provides a "slide to unlock" UI to initiate a purchase for a specific [SubscriptionOption].
 * This is a Google Play-only function.
 *
 * @param subscriptionOption The specific [SubscriptionOption] the user intends to purchase.
 * @param modifier The [Modifier] to be applied to this component.
 * @param colors The [SlideToUnlockColors] used to customize the appearance.
 * @param hintTexts The [HintTexts] to display in the component's track.
 * @param onSlideCompleted A callback invoked immediately when the user completes the slide gesture.
 * @param onPurchaseStateChanged A callback that reports the current [PurchaseState] of the transaction.
 * @param isPersonalizedPrice An optional boolean indicating personalized pricing.
 * @param oldProductId The product ID of a subscription to upgrade from.
 * @param replacementMode The [GoogleReplacementMode] to use for the subscription upgrade.
 */
@Composable
public fun SlideToPurchases(
  subscriptionOption: SubscriptionOption,
  modifier: Modifier = Modifier,
  colors: SlideToUnlockColors = DefaultSlideToUnlockColors(),
  hintTexts: HintTexts = HintTexts.defaultHintTexts(),
  onSlideCompleted: () -> Unit = {},
  onPurchaseStateChanged: (PurchaseState) -> Unit = {},
  isPersonalizedPrice: Boolean? = null,
  oldProductId: String? = null,
  replacementMode: GoogleReplacementMode = GoogleReplacementMode.WITHOUT_PRORATION,
) {
  InternalSlideToPurchases(
    modifier = modifier,
    onPurchaseStateChanged = onPurchaseStateChanged,
    onPurchase = {
      onSlideCompleted.invoke()
      Purchases.sharedInstance.awaitPurchase(
        subscriptionOption = subscriptionOption,
        isPersonalizedPrice = isPersonalizedPrice,
        oldProductId = oldProductId,
        replacementMode = replacementMode,
      )
    },
    colors = colors,
    hintTexts = hintTexts,
  )
}

/**
 * A composable that provides a "slide to unlock" UI to initiate a purchase for a [StoreProduct]
 * with an applied promotional offer. This is an App Store-only function.
 *
 * Use this function if you are not using RevenueCat's Offerings system.
 * If you are using Offerings, use the overload with a [Package] parameter instead.
 *
 * @param storeProduct The [StoreProduct] the user intends to purchase.
 * @param promotionalOffer The [PromotionalOffer] to apply to the purchase.
 * @param modifier The [Modifier] to be applied to this component.
 * @param colors The [SlideToUnlockColors] used to customize the appearance.
 * @param hintTexts The [HintTexts] to display in the component's track.
 * @param onSlideCompleted A callback invoked immediately when the user completes the slide gesture.
 * @param onPurchaseStateChanged A callback that reports the current [PurchaseState] of the transaction.
 * @see [awaitPromotionalOffer]
 */
@Composable
public fun SlideToPurchases(
  storeProduct: StoreProduct,
  promotionalOffer: PromotionalOffer,
  modifier: Modifier = Modifier,
  colors: SlideToUnlockColors = DefaultSlideToUnlockColors(),
  hintTexts: HintTexts = HintTexts.defaultHintTexts(),
  onSlideCompleted: () -> Unit = {},
  onPurchaseStateChanged: (PurchaseState) -> Unit = {},
) {
  InternalSlideToPurchases(
    modifier = modifier,
    onPurchaseStateChanged = onPurchaseStateChanged,
    onPurchase = {
      onSlideCompleted.invoke()
      Purchases.sharedInstance.awaitPurchase(
        storeProduct = storeProduct,
        promotionalOffer = promotionalOffer,
      )
    },
    colors = colors,
    hintTexts = hintTexts,
  )
}

/**
 * A composable that provides a "slide to unlock" UI to initiate a purchase for a [Package]
 * with an applied promotional offer. This is an App Store-only function.
 *
 * Only call this in direct response to user input.
 *
 * @param packageToPurchase The [Package] the user intends to purchase.
 * @param promotionalOffer The [PromotionalOffer] to apply to the purchase.
 * @param modifier The [Modifier] to be applied to this component.
 * @param colors The [SlideToUnlockColors] used to customize the appearance.
 * @param hintTexts The [HintTexts] to display in the component's track.
 * @param onSlideCompleted A callback invoked immediately when the user completes the slide gesture.
 * @param onPurchaseStateChanged A callback that reports the current [PurchaseState] of the transaction.
 * @see [awaitPromotionalOffer]
 */
@Composable
public fun SlideToPurchases(
  packageToPurchase: Package,
  promotionalOffer: PromotionalOffer,
  modifier: Modifier = Modifier,
  colors: SlideToUnlockColors = DefaultSlideToUnlockColors(),
  hintTexts: HintTexts = HintTexts.defaultHintTexts(),
  onSlideCompleted: () -> Unit = {},
  onPurchaseStateChanged: (PurchaseState) -> Unit = {},
) {
  InternalSlideToPurchases(
    modifier = modifier,
    onPurchaseStateChanged = onPurchaseStateChanged,
    onPurchase = {
      onSlideCompleted.invoke()
      Purchases.sharedInstance.awaitPurchase(
        packageToPurchase = packageToPurchase,
        promotionalOffer = promotionalOffer,
      )
    },
    colors = colors,
    hintTexts = hintTexts,
  )
}

/**
 * A composable that provides a "slide to unlock" UI to initiate a purchase for a [StoreProduct]
 * with a given win-back offer. This is an iOS-only function and requires iOS 18.0+.
 *
 * If you are using RevenueCat's Offerings system, use the overload with a [Package] parameter instead.
 *
 * @param storeProduct The [StoreProduct] to purchase.
 * @param winBackOffer The [WinBackOffer] to apply to the purchase.
 * @param modifier The [Modifier] to be applied to this component.
 * @param colors The [SlideToUnlockColors] used to customize the appearance.
 * @param hintTexts The [HintTexts] to display in the component's track.
 * @param onSlideCompleted A callback invoked immediately when the user completes the slide gesture.
 * @param onPurchaseStateChanged A callback that reports the current [PurchaseState] of the transaction.
 * @see [awaitEligibleWinBackOffersForProduct]
 */
@Composable
public fun SlideToPurchases(
  storeProduct: StoreProduct,
  winBackOffer: WinBackOffer,
  modifier: Modifier = Modifier,
  colors: SlideToUnlockColors = DefaultSlideToUnlockColors(),
  hintTexts: HintTexts = HintTexts.defaultHintTexts(),
  onSlideCompleted: () -> Unit = {},
  onPurchaseStateChanged: (PurchaseState) -> Unit = {},
) {
  InternalSlideToPurchases(
    modifier = modifier,
    onPurchaseStateChanged = onPurchaseStateChanged,
    onPurchase = {
      onSlideCompleted.invoke()
      Purchases.sharedInstance.awaitPurchase(
        storeProduct = storeProduct,
        winBackOffer = winBackOffer,
      )
    },
    colors = colors,
    hintTexts = hintTexts,
  )
}

/**
 * A composable that provides a "slide to unlock" UI to initiate a purchase for a [Package]
 * with a given win-back offer. This is an iOS-only function and requires iOS 18.0+.
 *
 * @param packageToPurchase The [Package] to purchase.
 * @param winBackOffer The [WinBackOffer] to apply to the purchase.
 * @param modifier The [Modifier] to be applied to this component.
 * @param colors The [SlideToUnlockColors] used to customize the appearance.
 * @param hintTexts The [HintTexts] to display in the component's track.
 * @param onSlideCompleted A callback invoked immediately when the user completes the slide gesture.
 * @param onPurchaseStateChanged A callback that reports the current [PurchaseState] of the transaction.
 * @see [awaitEligibleWinBackOffersForPackage]
 */
@Composable
public fun SlideToPurchases(
  packageToPurchase: Package,
  winBackOffer: WinBackOffer,
  modifier: Modifier = Modifier,
  colors: SlideToUnlockColors = DefaultSlideToUnlockColors(),
  hintTexts: HintTexts = HintTexts.defaultHintTexts(),
  onSlideCompleted: () -> Unit = {},
  onPurchaseStateChanged: (PurchaseState) -> Unit = {},
) {
  InternalSlideToPurchases(
    modifier = modifier,
    onPurchaseStateChanged = onPurchaseStateChanged,
    onPurchase = {
      onSlideCompleted.invoke()
      Purchases.sharedInstance.awaitPurchase(
        packageToPurchase = packageToPurchase,
        winBackOffer = winBackOffer,
      )
    },
    colors = colors,
    hintTexts = hintTexts,
  )
}

/**
 * A private, internal composable that handles the core logic for the SlideToPurchases component.
 * It uses a generic `onPurchase` lambda to abstract away the specific purchase method
 * (e.g., by StoreProduct, Package, etc.), while managing the UI state and coroutine scope.
 *
 * This pattern avoids code duplication across the multiple public-facing overloads.
 *
 * @param onPurchase A suspending lambda that executes the desired purchase call from the
 *                   RevenueCat SDK and returns a [SuccessfulPurchase].
 */
@Composable
private fun InternalSlideToPurchases(
  modifier: Modifier = Modifier,
  colors: SlideToUnlockColors = DefaultSlideToUnlockColors(),
  hintTexts: HintTexts = HintTexts.defaultHintTexts(),
  onPurchase: suspend () -> SuccessfulPurchase,
  onPurchaseStateChanged: (PurchaseState) -> Unit = {},
) {
  val scope = rememberCoroutineScope()
  var isSlided by rememberSaveable { mutableStateOf(false) }

  SlideToUnlock(
    modifier = modifier,
    isSlided = isSlided,
    onSlideCompleted = {
      scope.launch {
        onPurchaseStateChanged.invoke(PurchaseState.Loading)
        try {
          val successfulPurchase = withContext(Dispatchers.IO) {
            onPurchase.invoke()
          }
          onPurchaseStateChanged.invoke(
            PurchaseState.Success(
              successfulPurchase = successfulPurchase,
            ),
          )
        } catch (e: Exception) {
          onPurchaseStateChanged.invoke(
            PurchaseState.Error(exception = e),
          )
        }
      }
    },
    colors = colors,
    hintTexts = hintTexts,
  )
}
