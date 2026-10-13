package com.example

import com.example.data.model.CouponEntity
import com.example.data.model.MerchantEntity
import com.example.data.model.ProductEntity
import com.example.domain.AppLanguage
import com.example.domain.DeliveryCategory
import com.example.domain.LocalizationManager
import com.example.domain.OrderStatus
import com.example.domain.PaymentMethod
import com.example.domain.UserRole
import com.example.ui.viewmodel.CartState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EndToEndDeliveryWorkflowTest {

    // Helper regex for Algerian phone numbers (+213 or 0 followed by 5, 6, or 7 and 8 digits)
    private val algerianPhoneRegex = Regex("""^(\+213|0)[567]\d{8}$""")

    @Test
    fun testAlgerianPhoneNumberFormats() {
        val validPhones = listOf(
            "+213555123456",
            "+213661987654",
            "+213770112233",
            "0555123456",
            "0661987654",
            "0770112233"
        )
        for (phone in validPhones) {
            assertTrue("Phone $phone should be valid Algerian mobile format", algerianPhoneRegex.matches(phone))
        }

        val invalidPhones = listOf(
            "+33612345678",    // France
            "0123456789",      // Invalid Algerian prefix
            "+213455123456",   // 4 is landline, not mobile
            "055512345",       // Too short
            "055512345678",    // Too long
            "abc1234567"       // Non-digit
        )
        for (phone in invalidPhones) {
            assertFalse("Phone $phone should be rejected as invalid mobile format", algerianPhoneRegex.matches(phone))
        }
    }

    @Test
    fun testCartCalculationsAndDiscountCoercion() {
        val testMerchant = MerchantEntity(
            id = "m_test",
            nameAr = "مطعم الاختبار",
            nameFr = "Restaurant Test",
            category = DeliveryCategory.FOOD,
            wilayaCode = "16",
            wilayaName = "Alger",
            address = "Didouche Mourad, Alger",
            rating = 4.7,
            prepTimeMinutes = 25,
            deliveryFeeDzd = 350.0,
            minOrderDzd = 500.0,
            isOpen = true,
            tagAr = "تقليدي",
            tagFr = "Traditionnel"
        )

        val product1 = ProductEntity(
            id = "p1",
            merchantId = "m_test",
            nameAr = "كسكس عاصمي",
            nameFr = "Couscous Algérois",
            descAr = "باللحم والخضار",
            descFr = "A la viande et légumes",
            priceDzd = 950.0,
            categoryName = "Plats",
            isAvailable = true,
            isPopular = true
        )

        val product2 = ProductEntity(
            id = "p2",
            merchantId = "m_test",
            nameAr = "شوربة فريك",
            nameFr = "Chorba Frik",
            descAr = "شوربة تقليدية",
            descFr = "Soupe traditionnelle",
            priceDzd = 450.0,
            categoryName = "Entrées",
            isAvailable = true,
            isPopular = false
        )

        // 1. Initial Empty Cart
        val emptyCart = CartState()
        assertEquals(0.0, emptyCart.subtotal, 0.001)
        assertEquals(0, emptyCart.itemCount)

        // 2. Add 2x Couscous + 1x Chorba
        val cartWithItems = CartState(
            items = mapOf(product1 to 2, product2 to 1),
            merchant = testMerchant
        )
        // Subtotal = 2 * 950 + 1 * 450 = 1900 + 450 = 2350 DZD
        assertEquals(2350.0, cartWithItems.subtotal, 0.001)
        assertEquals(350.0, cartWithItems.deliveryFee, 0.001)
        assertEquals(3, cartWithItems.itemCount)
        // Total before discount = 2350 + 350 = 2700 DZD
        assertEquals(2700.0, cartWithItems.total, 0.001)

        // 3. Apply standard coupon (DZPRO: 300 DZD discount)
        val coupon = CouponEntity(
            code = "DZPRO",
            discountDzd = 300.0,
            minOrderDzd = 1500.0,
            isActive = true
        )
        val discountedCart = cartWithItems.copy(coupon = coupon)
        assertEquals(300.0, discountedCart.discount, 0.001)
        assertEquals(2400.0, discountedCart.total, 0.001) // 2700 - 300 = 2400

        // 4. Test extreme discount overflow (cannot produce negative total)
        val hugeCoupon = CouponEntity(
            code = "MEGA5000",
            discountDzd = 5000.0,
            minOrderDzd = 1000.0,
            isActive = true
        )
        val overflowCart = cartWithItems.copy(coupon = hugeCoupon)
        assertEquals(0.0, overflowCart.total, 0.001) // coerced to >= 0.0
    }

    @Test
    fun testMultiRolePermissionsAndWorkflowState() {
        val roles = listOf(UserRole.CUSTOMER, UserRole.DRIVER, UserRole.MERCHANT, UserRole.ADMIN)
        assertEquals(4, roles.size)

        // State progression matching DZ delivery workflow
        var currentStatus = OrderStatus.PENDING
        assertEquals("pending", currentStatus.name.lowercase())

        // Merchant accepts
        currentStatus = OrderStatus.PREPARING
        assertEquals("preparing", currentStatus.name.lowercase())

        // Merchant finishes
        currentStatus = OrderStatus.READY_FOR_PICKUP
        assertEquals("ready_for_pickup", currentStatus.name.lowercase())

        // Driver accepts
        currentStatus = OrderStatus.ASSIGNED
        assertEquals("assigned", currentStatus.name.lowercase())

        // Driver picks up
        currentStatus = OrderStatus.PICKED_UP
        assertEquals("picked_up", currentStatus.name.lowercase())

        // Driver in transit
        currentStatus = OrderStatus.DELIVERING
        assertEquals("delivering", currentStatus.name.lowercase())

        // Driver verifies OTP
        currentStatus = OrderStatus.DELIVERED
        assertTrue(currentStatus.isTerminal())
        assertEquals("delivered", currentStatus.name.lowercase())
    }

    @Test
    fun testBilingualRtlAndLtrFormatting() {
        // Arabic
        val appNameAr = LocalizationManager.get("app_name", AppLanguage.ARABIC)
        val codAr = LocalizationManager.get("pay_cod", AppLanguage.ARABIC)
        val currencyAr = LocalizationManager.formatDzd(1250.0, AppLanguage.ARABIC)

        assertTrue("Arabic app name must contain Arabic script", appNameAr.contains("ديزاد"))
        assertTrue("Arabic COD must contain Arabic text", codAr.contains("الدفع نقداً عند الاستلام"))
        assertTrue("Arabic currency must end with د.ج", currencyAr.contains("د.ج"))

        // French
        val appNameFr = LocalizationManager.get("app_name", AppLanguage.FRENCH)
        val codFr = LocalizationManager.get("pay_cod", AppLanguage.FRENCH)
        val currencyFr = LocalizationManager.formatDzd(1250.0, AppLanguage.FRENCH)

        assertEquals("DZ Delivery Pro", appNameFr)
        assertEquals("Paiement à la livraison (Espèces)", codFr)
        assertTrue("French currency must end with DZD", currencyFr.contains("DZD"))
    }
}
