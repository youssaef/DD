-- ======================================================================================
-- DZ DELIVERY PRO - SUPABASE POSTGRESQL MIGRATION 02: ROW LEVEL SECURITY (RLS)
-- ======================================================================================

-- Helper function to check if current authenticated user has an administrative role
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM public.user_roles
        WHERE user_id = auth.uid()
          AND role = 'admin'
    );
$$;

-- Helper function to check if current user is an authorized driver
CREATE OR REPLACE FUNCTION public.is_driver()
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM public.user_roles
        WHERE user_id = auth.uid()
          AND role = 'driver'
    );
$$;

-- Helper function to check if current user is owner/staff of a merchant
CREATE OR REPLACE FUNCTION public.is_merchant_member(m_id UUID)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
SECURITY DEFINER
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM public.merchants
        WHERE id = m_id AND owner_id = auth.uid()
    ) OR EXISTS (
        SELECT 1
        FROM public.merchant_memberships
        WHERE merchant_id = m_id AND user_id = auth.uid()
    );
$$;

-- 1. PROFILES RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read own profile or admin can read all"
ON public.profiles
FOR SELECT
USING (auth.uid() = id OR public.is_admin());

CREATE POLICY "Users can update own profile"
ON public.profiles
FOR UPDATE
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);

-- 2. USER ROLES RLS
ALTER TABLE public.user_roles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read own roles or admin can read all"
ON public.user_roles
FOR SELECT
USING (auth.uid() = user_id OR public.is_admin());

CREATE POLICY "Only admin can insert or update roles"
ON public.user_roles
FOR ALL
USING (public.is_admin())
WITH CHECK (public.is_admin());

-- 3. ADDRESSES RLS
ALTER TABLE public.addresses ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own addresses"
ON public.addresses
FOR ALL
USING (auth.uid() = user_id OR public.is_admin())
WITH CHECK (auth.uid() = user_id OR public.is_admin());

-- 4. SERVICE AREAS & CATEGORIES (Public Read, Admin Write)
ALTER TABLE public.service_categories ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read service categories" ON public.service_categories FOR SELECT USING (true);
CREATE POLICY "Admin write service categories" ON public.service_categories FOR ALL USING (public.is_admin());

ALTER TABLE public.service_areas ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read service areas" ON public.service_areas FOR SELECT USING (true);
CREATE POLICY "Admin write service areas" ON public.service_areas FOR ALL USING (public.is_admin());

-- 5. MERCHANTS & CATALOG
ALTER TABLE public.merchants ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read active merchants" ON public.merchants FOR SELECT USING (is_active = TRUE OR public.is_merchant_member(id) OR public.is_admin());
CREATE POLICY "Merchant owner or admin can update merchant" ON public.merchants FOR UPDATE USING (public.is_merchant_member(id) OR public.is_admin());

ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read available products" ON public.products FOR SELECT USING (is_available = TRUE OR public.is_merchant_member(merchant_id) OR public.is_admin());
CREATE POLICY "Merchant can manage products" ON public.products FOR ALL USING (public.is_merchant_member(merchant_id) OR public.is_admin());

ALTER TABLE public.inventory ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Merchant or admin can view/manage inventory" ON public.inventory FOR ALL USING (
    EXISTS (SELECT 1 FROM public.products p WHERE p.id = product_id AND (public.is_merchant_member(p.merchant_id) OR public.is_admin()))
);

-- 6. DRIVERS & FLEET
ALTER TABLE public.driver_profiles ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Drivers can view and update own profile" ON public.driver_profiles FOR ALL USING (auth.uid() = user_id OR public.is_admin());
CREATE POLICY "Public can view assigned driver limited info" ON public.driver_profiles FOR SELECT USING (is_approved = TRUE);

ALTER TABLE public.driver_documents ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Driver view own documents or admin" ON public.driver_documents FOR ALL USING (
    EXISTS (SELECT 1 FROM public.driver_profiles d WHERE d.id = driver_id AND d.user_id = auth.uid()) OR public.is_admin()
);

ALTER TABLE public.driver_locations ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Drivers insert own location" ON public.driver_locations FOR INSERT WITH CHECK (
    EXISTS (SELECT 1 FROM public.driver_profiles d WHERE d.id = driver_id AND d.user_id = auth.uid())
);
CREATE POLICY "Customers and merchants view location of their active orders" ON public.driver_locations FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM public.orders o
        WHERE o.id = order_id
          AND (o.customer_id = auth.uid() OR public.is_merchant_member(o.merchant_id) OR public.is_admin())
    )
);

-- 7. ORDERS & ORDER ITEMS
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Customers can view own orders"
ON public.orders
FOR SELECT
USING (auth.uid() = customer_id);

CREATE POLICY "Merchants can view orders placed at their store"
ON public.orders
FOR SELECT
USING (public.is_merchant_member(merchant_id));

CREATE POLICY "Drivers can view available or assigned orders"
ON public.orders
FOR SELECT
USING (
    (status = 'ready_for_pickup' AND driver_id IS NULL AND public.is_driver())
    OR (driver_id IN (SELECT id FROM public.driver_profiles WHERE user_id = auth.uid()))
    OR public.is_admin()
);

CREATE POLICY "Customers can create orders"
ON public.orders
FOR INSERT
WITH CHECK (auth.uid() = customer_id OR auth.uid() IS NOT NULL);

CREATE POLICY "Authorized actors can update order status"
ON public.orders
FOR UPDATE
USING (
    auth.uid() = customer_id
    OR public.is_merchant_member(merchant_id)
    OR (driver_id IN (SELECT id FROM public.driver_profiles WHERE user_id = auth.uid()))
    OR public.is_admin()
);

ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Read order items for authorized order actors"
ON public.order_items
FOR SELECT
USING (
    EXISTS (
        SELECT 1 FROM public.orders o
        WHERE o.id = order_id
          AND (o.customer_id = auth.uid()
               OR public.is_merchant_member(o.merchant_id)
               OR (o.driver_id IN (SELECT id FROM public.driver_profiles WHERE user_id = auth.uid()))
               OR public.is_admin())
    )
);

CREATE POLICY "Insert order items on order placement"
ON public.order_items
FOR INSERT
WITH CHECK (
    EXISTS (
        SELECT 1 FROM public.orders o
        WHERE o.id = order_id AND (o.customer_id = auth.uid() OR auth.uid() IS NOT NULL)
    )
);

-- 8. PAYMENTS & FINANCIALS
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Authorized order actors can read payments" ON public.payments FOR SELECT USING (
    EXISTS (SELECT 1 FROM public.orders o WHERE o.id = order_id AND (o.customer_id = auth.uid() OR public.is_admin()))
);

ALTER TABLE public.driver_earnings ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Drivers read own earnings" ON public.driver_earnings FOR SELECT USING (
    EXISTS (SELECT 1 FROM public.driver_profiles d WHERE d.id = driver_id AND d.user_id = auth.uid()) OR public.is_admin()
);

ALTER TABLE public.merchant_settlements ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Merchants read own settlements" ON public.merchant_settlements FOR SELECT USING (
    public.is_merchant_member(merchant_id) OR public.is_admin()
);

-- 9. AUDIT LOGS
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Only admin read audit logs" ON public.audit_logs FOR SELECT USING (public.is_admin());
