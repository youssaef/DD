-- ======================================================================================
-- DZ DELIVERY PRO - SUPABASE POSTGRESQL MIGRATION 03: FUNCTIONS & TRIGGERS
-- Server-side validation of State Machine, Driver Assignment & OTP Proof
-- ======================================================================================

-- 1. FUNCTION: Atomic State Transition with Status History Logging
CREATE OR REPLACE FUNCTION public.transition_order_status(
    p_order_id UUID,
    p_new_status order_status_enum,
    p_reason TEXT DEFAULT NULL
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_current_status order_status_enum;
    v_is_valid BOOLEAN := FALSE;
BEGIN
    SELECT status INTO v_current_status
    FROM public.orders
    WHERE id = p_order_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'error', 'Order not found');
    END IF;

    -- Validate state transition
    IF (v_current_status = 'pending' AND p_new_status IN ('confirmed', 'cancelled')) OR
       (v_current_status = 'confirmed' AND p_new_status IN ('preparing', 'cancelled')) OR
       (v_current_status = 'preparing' AND p_new_status IN ('ready_for_pickup', 'cancelled')) OR
       (v_current_status = 'ready_for_pickup' AND p_new_status IN ('assigned', 'cancelled')) OR
       (v_current_status = 'assigned' AND p_new_status IN ('driver_to_pickup', 'picked_up', 'cancelled')) OR
       (v_current_status = 'driver_to_pickup' AND p_new_status IN ('picked_up', 'cancelled')) OR
       (v_current_status = 'picked_up' AND p_new_status IN ('driver_to_customer')) OR
       (v_current_status = 'driver_to_customer' AND p_new_status IN ('delivered', 'failed')) THEN
        v_is_valid := TRUE;
    END IF;

    IF NOT v_is_valid THEN
        RETURN jsonb_build_object(
            'success', false,
            'error', format('Invalid transition from %s to %s', v_current_status, p_new_status)
        );
    END IF;

    -- Update order
    UPDATE public.orders
    SET status = p_new_status,
        updated_at = NOW()
    WHERE id = p_order_id;

    -- Log transition
    INSERT INTO public.order_status_history (
        order_id,
        previous_status,
        new_status,
        changed_by,
        reason
    ) VALUES (
        p_order_id,
        v_current_status,
        p_new_status,
        auth.uid(),
        p_reason
    );

    RETURN jsonb_build_object(
        'success', true,
        'order_id', p_order_id,
        'previous_status', v_current_status,
        'new_status', p_new_status
    );
END;
$$;

-- 2. FUNCTION: Atomic Driver Assignment with Concurrency Lock
CREATE OR REPLACE FUNCTION public.assign_driver_to_order(
    p_order_id UUID,
    p_driver_id UUID
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_order public.orders%ROWTYPE;
    v_driver public.driver_profiles%ROWTYPE;
    v_driver_user public.profiles%ROWTYPE;
BEGIN
    -- Acquire exclusive row lock on order
    SELECT * INTO v_order
    FROM public.orders
    WHERE id = p_order_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'error', 'Order not found');
    END IF;

    IF v_order.driver_id IS NOT NULL THEN
        RETURN jsonb_build_object('success', false, 'error', 'Order is already assigned to another driver');
    END IF;

    IF v_order.status != 'ready_for_pickup' AND v_order.status != 'confirmed' THEN
        RETURN jsonb_build_object('success', false, 'error', 'Order is not ready for pickup yet');
    END IF;

    -- Fetch driver info
    SELECT * INTO v_driver
    FROM public.driver_profiles
    WHERE id = p_driver_id;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'error', 'Driver not found');
    END IF;

    SELECT * INTO v_driver_user
    FROM public.profiles
    WHERE id = v_driver.user_id;

    -- Update order with driver details
    UPDATE public.orders
    SET driver_id = v_driver.id,
        driver_name = v_driver_user.full_name,
        driver_phone = v_driver_user.phone,
        driver_vehicle = v_driver.vehicle_type,
        status = 'assigned',
        updated_at = NOW()
    WHERE id = p_order_id;

    -- Record assignment in delivery_assignments
    INSERT INTO public.delivery_assignments (
        order_id,
        driver_id,
        status
    ) VALUES (
        p_order_id,
        p_driver_id,
        'accepted'
    );

    -- Log status history
    INSERT INTO public.order_status_history (
        order_id,
        previous_status,
        new_status,
        changed_by,
        reason
    ) VALUES (
        p_order_id,
        v_order.status,
        'assigned',
        auth.uid(),
        'Driver accepted delivery request'
    );

    RETURN jsonb_build_object(
        'success', true,
        'order_id', p_order_id,
        'driver_id', p_driver_id,
        'status', 'assigned'
    );
END;
$$;

-- 3. FUNCTION: Proof of Delivery Verification using Customer OTP
CREATE OR REPLACE FUNCTION public.verify_delivery_otp(
    p_order_id UUID,
    p_entered_otp TEXT,
    p_driver_id UUID
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_order public.orders%ROWTYPE;
    v_driver_cut NUMERIC(12, 2);
BEGIN
    SELECT * INTO v_order
    FROM public.orders
    WHERE id = p_order_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'error', 'Order not found');
    END IF;

    IF trim(v_order.otp_code) != trim(p_entered_otp) THEN
        RETURN jsonb_build_object('success', false, 'error', 'Invalid OTP code');
    END IF;

    -- Transition order to delivered
    UPDATE public.orders
    SET status = 'delivered',
        payment_status = 'completed',
        updated_at = NOW()
    WHERE id = p_order_id;

    -- Record delivery proof
    INSERT INTO public.delivery_proofs (
        order_id,
        driver_id,
        otp_verified,
        notes
    ) VALUES (
        p_order_id,
        p_driver_id,
        true,
        'OTP verified by customer upon physical delivery'
    );

    -- Calculate driver 85% delivery fee compensation
    v_driver_cut := v_order.delivery_fee * 0.85;

    INSERT INTO public.driver_earnings (
        driver_id,
        order_id,
        delivery_fee_earned
    ) VALUES (
        p_driver_id,
        p_order_id,
        v_driver_cut
    );

    -- Update driver ledger
    UPDATE public.driver_profiles
    SET total_deliveries = total_deliveries + 1,
        today_earnings = today_earnings + v_driver_cut,
        updated_at = NOW()
    WHERE id = p_driver_id;

    -- Log status history
    INSERT INTO public.order_status_history (
        order_id,
        previous_status,
        new_status,
        changed_by,
        reason
    ) VALUES (
        p_order_id,
        v_order.status,
        'delivered',
        auth.uid(),
        'Delivery validated via Customer OTP'
    );

    RETURN jsonb_build_object(
        'success', true,
        'order_id', p_order_id,
        'status', 'delivered',
        'earnings_dzd', v_driver_cut
    );
END;
$$;
