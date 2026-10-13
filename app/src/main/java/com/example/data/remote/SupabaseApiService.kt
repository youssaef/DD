package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface SupabaseApiService {

    @GET("rest/v1/merchants?select=*")
    suspend fun getMerchants(): Response<List<SupabaseMerchantDto>>

    @GET("rest/v1/products?select=*")
    suspend fun getProducts(
        @Query("merchant_id") merchantFilter: String? = null
    ): Response<List<SupabaseProductDto>>

    @GET("rest/v1/orders?select=*")
    suspend fun getOrders(): Response<List<SupabaseOrderDto>>

    @POST("rest/v1/rpc/transition_order_status")
    @Headers("Prefer: return=representation")
    suspend fun transitionOrderStatus(
        @Body request: TransitionStatusRequest
    ): Response<RpcStatusResponse>

    @POST("rest/v1/rpc/assign_driver_to_order")
    @Headers("Prefer: return=representation")
    suspend fun assignDriver(
        @Body request: AssignDriverRequest
    ): Response<RpcStatusResponse>

    @POST("rest/v1/rpc/verify_delivery_otp")
    @Headers("Prefer: return=representation")
    suspend fun verifyDeliveryOtp(
        @Body request: VerifyOtpRequest
    ): Response<RpcStatusResponse>

    @POST("functions/v1/create-order")
    suspend fun createOrderViaEdgeFunction(
        @Body request: CreateOrderRpcRequest
    ): Response<Map<String, Any>>

    companion object {
        fun create(): SupabaseApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader(SupabaseConfig.HEADER_APIKEY, SupabaseConfig.anonKey)
                    .addHeader(SupabaseConfig.HEADER_AUTHORIZATION, "Bearer ${SupabaseConfig.anonKey}")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val baseUrl = if (SupabaseConfig.url.endsWith("/")) SupabaseConfig.url else "${SupabaseConfig.url}/"

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SupabaseApiService::class.java)
        }
    }
}
