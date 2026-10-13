package com.example.data.remote

import com.example.BuildConfig

object SupabaseConfig {
    val url: String
        get() = runCatching {
            val key = BuildConfig.SUPABASE_URL
            if (!key.isNullOrBlank() && !key.startsWith("placeholder")) key else "https://dz-delivery-pro.supabase.co"
        }.getOrDefault("https://dz-delivery-pro.supabase.co")

    val anonKey: String
        get() = runCatching {
            val key = BuildConfig.SUPABASE_ANON_KEY
            if (!key.isNullOrBlank() && !key.startsWith("placeholder")) key else "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.placeholder_supabase_anon_key"
        }.getOrDefault("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.placeholder_supabase_anon_key")

    const val HEADER_APIKEY = "apikey"
    const val HEADER_AUTHORIZATION = "Authorization"
}
