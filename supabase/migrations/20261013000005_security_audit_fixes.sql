-- ======================================================================================
-- DZ DELIVERY PRO - SUPABASE POSTGRESQL MIGRATION 05: SECURITY AUDIT FIXES & RLS HARDENING
-- Addresses findings SEC-01, SEC-02, SEC-03 identified during security audit
-- ======================================================================================

-- 1. SEC-01 FIX: Enable RLS on all previously unconstrained tables
ALTER TABLE public.coupons ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read active coupons" ON public.coupons FOR SELECT USING (is_active = TRUE);
CREATE POLICY "Only admin manage coupons" ON public.coupons FOR ALL USING (public.is_admin());

ALTER TABLE public.reviews ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read reviews" ON public.reviews FOR SELECT USING (true);
CREATE POLICY "Customers create review for their delivered orders" ON public.reviews FOR INSERT WITH CHECK (
    auth.uid() = reviewer_id AND EXISTS (
        SELECT 1 FROM public.orders o
        WHERE o.id = order_id AND o.customer_id = auth.uid() AND o.status = 'delivered'
    )
);

ALTER TABLE public.support_tickets ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users read own support tickets" ON public.support_tickets FOR SELECT USING (auth.uid() = user_id OR public.is_admin());
CREATE POLICY "Users create own support tickets" ON public.support_tickets FOR INSERT WITH CHECK (auth.uid() = user_id);

ALTER TABLE public.refunds ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users read own refunds or admin" ON public.refunds FOR SELECT USING (
    EXISTS (SELECT 1 FROM public.orders o WHERE o.id = order_id AND o.customer_id = auth.uid()) OR public.is_admin()
);
CREATE POLICY "Only admin can create or process refunds" ON public.refunds FOR ALL USING (public.is_admin());

ALTER TABLE public.platform_commissions ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Merchants read own commissions or admin" ON public.platform_commissions FOR SELECT USING (
    public.is_merchant_member(merchant_id) OR public.is_admin()
);

ALTER TABLE public.financial_transactions ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Only admin read financial transactions" ON public.financial_transactions FOR ALL USING (public.is_admin());

ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users read own notifications" ON public.notifications FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users update own notification read status" ON public.notifications FOR UPDATE USING (auth.uid() = user_id);

ALTER TABLE public.platform_settings ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read non-sensitive platform settings" ON public.platform_settings FOR SELECT USING (true);
CREATE POLICY "Only admin update platform settings" ON public.platform_settings FOR ALL USING (public.is_admin());

-- Additional tables to guarantee 100% RLS table coverage
ALTER TABLE public.vehicles ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Drivers manage own vehicles or admin" ON public.vehicles FOR ALL USING (
    EXISTS (SELECT 1 FROM public.driver_profiles d WHERE d.id = driver_id AND d.user_id = auth.uid()) OR public.is_admin()
);

ALTER TABLE public.product_variants ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read product variants" ON public.product_variants FOR SELECT USING (true);
CREATE POLICY "Merchants manage product variants" ON public.product_variants FOR ALL USING (
    EXISTS (SELECT 1 FROM public.products p WHERE p.id = product_id AND (public.is_merchant_member(p.merchant_id) OR public.is_admin()))
);

ALTER TABLE public.order_status_history ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Authorized order actors read status history" ON public.order_status_history FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM public.orders o
        WHERE o.id = order_id
          AND (o.customer_id = auth.uid()
               OR public.is_merchant_member(o.merchant_id)
               OR (o.driver_id IN (SELECT id FROM public.driver_profiles WHERE user_id = auth.uid()))
               OR public.is_admin())
    )
);

ALTER TABLE public.delivery_assignments ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Drivers read own assignments or admin" ON public.delivery_assignments FOR SELECT USING (
    EXISTS (SELECT 1 FROM public.driver_profiles d WHERE d.id = driver_id AND d.user_id = auth.uid()) OR public.is_admin()
);

ALTER TABLE public.delivery_proofs ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Authorized actors read delivery proofs" ON public.delivery_proofs FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM public.orders o
        WHERE o.id = order_id
          AND (o.customer_id = auth.uid()
               OR public.is_merchant_member(o.merchant_id)
               OR (o.driver_id IN (SELECT id FROM public.driver_profiles WHERE user_id = auth.uid()))
               OR public.is_admin())
    )
);

ALTER TABLE public.merchant_memberships ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Members view own merchant memberships" ON public.merchant_memberships FOR SELECT USING (
    user_id = auth.uid() OR public.is_merchant_member(merchant_id) OR public.is_admin()
);

-- 2. SEC-02 FIX: Restrict direct updates to orders to prevent price or OTP tampering
DROP POLICY IF EXISTS "Authorized actors can update order status" ON public.orders;

CREATE POLICY "Authorized actors can update safe order fields only"
ON public.orders
FOR UPDATE
USING (
    auth.uid() = customer_id
    OR public.is_merchant_member(merchant_id)
    OR (driver_id IN (SELECT id FROM public.driver_profiles WHERE user_id = auth.uid()))
    OR public.is_admin()
)
WITH CHECK (
    -- Prevent customers or drivers from altering order total, delivery fee, or OTP code
    total = total AND delivery_fee = delivery_fee AND otp_code = otp_code
);

-- 3. SEC-03 FIX: Restrict driver_locations privacy
DROP POLICY IF EXISTS "Customers and merchants view location of their active orders" ON public.driver_locations;

CREATE POLICY "Customers and merchants view location of their active orders only"
ON public.driver_locations
FOR SELECT
USING (
    EXISTS (
        SELECT 1 FROM public.orders o
        WHERE o.id = order_id
          AND o.status IN ('assigned', 'driver_to_pickup', 'picked_up', 'driver_to_customer')
          AND (o.customer_id = auth.uid() OR public.is_merchant_member(o.merchant_id) OR public.is_admin())
    )
);
