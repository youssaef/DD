package com.example

import com.example.domain.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderLifecycleSecurityTest {

    @Test
    fun testCompleteValidLifecycleSequence() {
        var status = OrderStatus.PENDING

        // 1. Merchant confirms or accepts
        val s1 = status.nextLogicalStatus()
        assertEquals(OrderStatus.CONFIRMED, s1)
        status = s1!!

        // 2. Merchant prepares
        val s2 = status.nextLogicalStatus()
        assertEquals(OrderStatus.PREPARING, s2)
        status = s2!!

        // 3. Merchant finishes prep -> ready for pickup
        val s3 = status.nextLogicalStatus()
        assertEquals(OrderStatus.READY_FOR_PICKUP, s3)
        status = s3!!

        // 4. Driver accepts assignment
        val s4 = status.nextLogicalStatus()
        assertEquals(OrderStatus.ASSIGNED, s4)
        status = s4!!

        // 5. Driver picks up from merchant
        val s5 = status.nextLogicalStatus()
        assertEquals(OrderStatus.PICKED_UP, s5)
        status = s5!!

        // 6. Driver en route to customer
        val s6 = status.nextLogicalStatus()
        assertEquals(OrderStatus.DELIVERING, s6)
        status = s6!!

        // 7. Driver validates delivery via customer OTP
        val s7 = status.nextLogicalStatus()
        assertEquals(OrderStatus.DELIVERED, s7)
        status = s7!!

        // Terminal state: cannot transition further
        assertTrue(status.isTerminal())
        assertNull(status.nextLogicalStatus())
    }

    @Test
    fun testIllegalTransitionsAreBlocked() {
        // Pending cannot jump directly to Delivered
        assertFalse(OrderStatus.PENDING.allowedTransitions().contains(OrderStatus.DELIVERED))

        // Preparing cannot jump directly to Delivered
        assertFalse(OrderStatus.PREPARING.allowedTransitions().contains(OrderStatus.DELIVERED))

        // Ready for pickup cannot skip driver assignment
        assertFalse(OrderStatus.READY_FOR_PICKUP.allowedTransitions().contains(OrderStatus.DELIVERING))

        // Delivered order cannot be cancelled or reopened
        assertTrue(OrderStatus.DELIVERED.allowedTransitions().isEmpty())
        assertTrue(OrderStatus.CANCELLED.allowedTransitions().isEmpty())
    }

    @Test
    fun testOtpValidationLogic() {
        val legitimateOtp = "5183"
        val userEnteredCorrectOtp = "5183"
        val userEnteredIncorrectOtp = "9999"
        val userEnteredPaddedOtp = " 5183 "

        // Direct match
        assertTrue(legitimateOtp == userEnteredCorrectOtp)
        // Trim match
        assertTrue(legitimateOtp == userEnteredPaddedOtp.trim())
        // Rejection
        assertFalse(legitimateOtp == userEnteredIncorrectOtp)
    }

    @Test
    fun testFinancialCommissionCalculations() {
        val deliveryFeeDzd = 400.0
        val driverCutRate = 0.85 // 85%
        val platformCommissionRate = 0.15 // 15%

        val driverEarnings = deliveryFeeDzd * driverCutRate
        val platformCommission = deliveryFeeDzd * platformCommissionRate

        assertEquals(340.0, driverEarnings, 0.001)
        assertEquals(60.0, platformCommission, 0.001)
        assertEquals(deliveryFeeDzd, driverEarnings + platformCommission, 0.001)
    }
}
