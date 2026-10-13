// Supabase Edge Function: create-order
// Runtime: Deno / TypeScript
import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

interface CartItemInput {
  productId: string;
  quantity: number;
}

interface CreateOrderRequest {
  merchantId: string;
  items: CartItemInput[];
  deliveryAddress: string;
  wilayaCode: string;
  customerPhone: string;
  customerName: string;
  couponCode?: string;
  paymentMethod: "cash_on_delivery" | "edahabia_baridimob" | "cib_card";
  notes?: string;
}

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    const body: CreateOrderRequest = await req.json();

    if (!body.merchantId || !body.items || body.items.length === 0) {
      return new Response(JSON.stringify({ error: "Missing required order items or merchantId" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // 1. Fetch merchant & verify
    const { data: merchant, error: mErr } = await supabase
      .from("merchants")
      .select("id, name_fr, delivery_fee, min_order_amount, is_open")
      .eq("id", body.merchantId)
      .single();

    if (mErr || !merchant || !merchant.is_open) {
      return new Response(JSON.stringify({ error: "Merchant is closed or not found" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // 2. Fetch products & validate prices server-side
    const productIds = body.items.map((i) => i.productId);
    const { data: dbProducts, error: pErr } = await supabase
      .from("products")
      .select("id, name_ar, name_fr, price, is_available")
      .in("id", productIds)
      .eq("merchant_id", body.merchantId);

    if (pErr || !dbProducts || dbProducts.length !== productIds.length) {
      return new Response(JSON.stringify({ error: "Some products are invalid or unavailable" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Calculate subtotal
    let subtotal = 0;
    const validatedItems: Array<{
      productId: string;
      productNameAr: string;
      productNameFr: string;
      unitPrice: number;
      quantity: number;
    }> = [];

    for (const item of body.items) {
      const dbProd = dbProducts.find((p) => p.id === item.productId);
      if (!dbProd || !dbProd.is_available) {
        return new Response(JSON.stringify({ error: `Product unavailable: ${item.productId}` }), {
          status: 400,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        });
      }
      const price = Number(dbProd.price);
      subtotal += price * item.quantity;
      validatedItems.push({
        productId: dbProd.id,
        productNameAr: dbProd.name_ar,
        productNameFr: dbProd.name_fr,
        unitPrice: price,
        quantity: item.quantity,
      });
    }

    // Validate coupon if supplied
    let discount = 0;
    if (body.couponCode) {
      const { data: coupon } = await supabase
        .from("coupons")
        .select("discount_amount, min_order_amount, is_active")
        .eq("code", body.couponCode.toUpperCase().trim())
        .single();

      if (coupon && coupon.is_active && subtotal >= Number(coupon.min_order_amount)) {
        discount = Number(coupon.discount_amount);
      }
    }

    const deliveryFee = Number(merchant.delivery_fee);
    const total = Math.max(0, subtotal + deliveryFee - discount);
    const randomSuffix = Math.floor(1000 + Math.random() * 9000);
    const orderNumber = `DZ-${randomSuffix}`;
    const otpCode = Math.floor(1000 + Math.random() * 9000).toString();

    // 3. Insert order
    const { data: createdOrder, error: oErr } = await supabase
      .from("orders")
      .insert({
        order_number: orderNumber,
        customer_phone: body.customerPhone,
        customer_name: body.customerName,
        customer_address: body.deliveryAddress,
        wilaya_code: body.wilayaCode,
        merchant_id: merchant.id,
        merchant_name: merchant.name_fr,
        subtotal: subtotal,
        delivery_fee: deliveryFee,
        discount: discount,
        total: total,
        payment_method: body.paymentMethod,
        payment_status: body.paymentMethod === "cash_on_delivery" ? "pending" : "completed",
        otp_code: otpCode,
        notes: body.notes || "",
        status: "pending",
      })
      .select()
      .single();

    if (oErr || !createdOrder) {
      return new Response(JSON.stringify({ error: oErr?.message || "Failed to create order" }), {
        status: 500,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // 4. Insert order items
    const itemsToInsert = validatedItems.map((item) => ({
      order_id: createdOrder.id,
      product_id: item.productId,
      product_name_ar: item.productNameAr,
      product_name_fr: item.productNameFr,
      unit_price: item.unitPrice,
      quantity: item.quantity,
    }));

    await supabase.from("order_items").insert(itemsToInsert);

    return new Response(JSON.stringify({ success: true, order: createdOrder }), {
      status: 200,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : "Unknown error";
    return new Response(JSON.stringify({ error: message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
