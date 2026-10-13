package com.example

import com.example.data.remote.SupabaseConfig
import com.example.data.remote.SupabaseMerchantDto
import com.example.data.remote.TransitionStatusRequest
import com.example.data.remote.VerifyOtpRequest
import com.example.domain.AppLanguage
import com.example.domain.LocalizationManager
import com.example.domain.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testOrderStateMachineTransitions() {
    // PENDING state
    val pending = OrderStatus.PENDING
    assertTrue(pending.allowedTransitions().contains(OrderStatus.CONFIRMED))
    assertTrue(pending.allowedTransitions().contains(OrderStatus.CANCELLED))
    assertFalse(pending.allowedTransitions().contains(OrderStatus.DELIVERED))
    assertFalse(pending.isTerminal())

    // Normal sequential progression
    assertEquals(OrderStatus.CONFIRMED, OrderStatus.PENDING.nextLogicalStatus())
    assertEquals(OrderStatus.PREPARING, OrderStatus.CONFIRMED.nextLogicalStatus())
    assertEquals(OrderStatus.READY_FOR_PICKUP, OrderStatus.PREPARING.nextLogicalStatus())
    assertEquals(OrderStatus.ASSIGNED, OrderStatus.READY_FOR_PICKUP.nextLogicalStatus())
    assertEquals(OrderStatus.PICKED_UP, OrderStatus.ASSIGNED.nextLogicalStatus())
    assertEquals(OrderStatus.DELIVERING, OrderStatus.PICKED_UP.nextLogicalStatus())
    assertEquals(OrderStatus.DELIVERED, OrderStatus.DELIVERING.nextLogicalStatus())

    // Terminal states
    assertTrue(OrderStatus.DELIVERED.isTerminal())
    assertTrue(OrderStatus.CANCELLED.isTerminal())
    assertNull(OrderStatus.DELIVERED.nextLogicalStatus())
    assertNull(OrderStatus.CANCELLED.nextLogicalStatus())
  }

  @Test
  fun testDzdCurrencyFormatting() {
    val amount = 1450.0
    val formattedAr = LocalizationManager.formatDzd(amount, AppLanguage.ARABIC)
    val formattedFr = LocalizationManager.formatDzd(amount, AppLanguage.FRENCH)

    assertTrue(formattedAr.contains("د.ج"))
    assertTrue(formattedFr.contains("DZD"))
  }

  @Test
  fun testBilingualStringLookup() {
    val titleAr = LocalizationManager.get("app_name", AppLanguage.ARABIC)
    val titleFr = LocalizationManager.get("app_name", AppLanguage.FRENCH)

    assertEquals("ديزاد دليفري برو", titleAr)
    assertEquals("DZ Delivery Pro", titleFr)
  }

  @Test
  fun testSupabaseConfigDefaults() {
    assertNotNull(SupabaseConfig.url)
    assertNotNull(SupabaseConfig.anonKey)
    assertTrue(SupabaseConfig.url.startsWith("https://"))
  }

  @Test
  fun testSupabaseDtoRequests() {
    val transitionReq = TransitionStatusRequest(
      orderId = "DZ-8022",
      newStatus = "ready_for_pickup",
      reason = "Order ready in kitchen"
    )
    assertEquals("DZ-8022", transitionReq.orderId)
    assertEquals("ready_for_pickup", transitionReq.newStatus)

    val otpReq = VerifyOtpRequest(
      orderId = "DZ-8021",
      enteredOtp = "5183",
      driverId = "40000000-0000-0000-0000-000000000001"
    )
    assertEquals("5183", otpReq.enteredOtp)
  }
}
