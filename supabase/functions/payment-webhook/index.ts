// Supabase Edge Function: payment-webhook
// Handles Algérie Poste (Edahabia / BaridiMob) and SATIM CIB webhooks
import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type, x-signature",
};

serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const webhookSecret = Deno.env.get("PAYMENT_WEBHOOK_SECRET") ?? "dz_secret_test";
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    const signature = req.headers.get("x-signature");
    const rawBody = await req.text();
    const payload = JSON.parse(rawBody);

    const { orderId, amount, transactionId, status, provider } = payload;

    if (!orderId || !amount || !transactionId) {
      return new Response(JSON.stringify({ error: "Missing required webhook parameters" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Check for idempotency: if payment with transactionId exists, return 200
    const { data: existingPayment } = await supabase
      .from("payments")
      .select("id, status")
      .eq("provider_transaction_id", transactionId)
      .single();

    if (existingPayment) {
      return new Response(JSON.stringify({ success: true, message: "Payment already processed" }), {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const isSuccess = status === "SUCCESS" || status === "APPROVED";

    // Insert payment record
    await supabase.from("payments").insert({
      order_id: orderId,
      amount: amount,
      provider: provider || "edahabia_baridimob",
      status: isSuccess ? "completed" : "failed",
      provider_transaction_id: transactionId,
    });

    if (isSuccess) {
      // Update order payment status
      await supabase
        .from("orders")
        .update({ payment_status: "completed", updated_at: new Date().toISOString() })
        .eq("id", orderId);
    }

    return new Response(JSON.stringify({ success: true, processed: true }), {
      status: 200,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : "Internal error";
    return new Response(JSON.stringify({ error: msg }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
