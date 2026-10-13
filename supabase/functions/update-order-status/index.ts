// Supabase Edge Function: update-order-status
import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.0";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    const { orderId, newStatus, reason, otpCode, driverId } = await req.json();

    if (!orderId || !newStatus) {
      return new Response(JSON.stringify({ error: "Missing orderId or newStatus" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // If newStatus is 'delivered', verify OTP
    if (newStatus === "delivered") {
      if (!otpCode || !driverId) {
        return new Response(JSON.stringify({ error: "OTP code and driverId are required for delivery validation" }), {
          status: 400,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        });
      }

      const { data: otpResult, error: otpErr } = await supabase.rpc("verify_delivery_otp", {
        p_order_id: orderId,
        p_entered_otp: otpCode,
        p_driver_id: driverId,
      });

      if (otpErr || !otpResult?.success) {
        return new Response(JSON.stringify({ error: otpErr?.message || otpResult?.error || "OTP verification failed" }), {
          status: 400,
          headers: { ...corsHeaders, "Content-Type": "application/json" },
        });
      }

      return new Response(JSON.stringify(otpResult), {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Otherwise, transition status through validated state machine
    const { data: transitionResult, error: transErr } = await supabase.rpc("transition_order_status", {
      p_order_id: orderId,
      p_new_status: newStatus,
      p_reason: reason || null,
    });

    if (transErr || !transitionResult?.success) {
      return new Response(JSON.stringify({ error: transErr?.message || transitionResult?.error || "Transition failed" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    return new Response(JSON.stringify(transitionResult), {
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
