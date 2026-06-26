package com.example.data.network

import android.util.Log
import com.example.data.db.OrderEntity
import com.example.data.db.ProductEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class SupabaseProduct(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "price") val price: Double? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "category_arabic") val categoryArabic: String? = null,
    @Json(name = "rating") val rating: Float? = null,
    @Json(name = "image_res_name") val imageResName: String? = null,
    @Json(name = "is_favorite") val isFavorite: Boolean? = false,
    @Json(name = "stock") val stock: Int? = 10,
    @Json(name = "seller_email") val sellerEmail: String? = "",
    @Json(name = "is_approved") val isApproved: Boolean? = true
)

@JsonClass(generateAdapter = true)
data class SupabaseOrder(
    @Json(name = "order_id") val orderId: String? = null,
    @Json(name = "product_id") val productId: Int? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "price_at_order") val priceAtOrder: Double? = null,
    @Json(name = "quantity") val quantity: Int? = null,
    @Json(name = "order_date") val orderDate: String? = null,
    @Json(name = "status_arabic") val statusArabic: String? = null,
    @Json(name = "customer_name") val customerName: String? = null,
    @Json(name = "customer_phone") val customerPhone: String? = null,
    @Json(name = "customer_address") val customerAddress: String? = null,
    @Json(name = "courier_name") val courierName: String? = "",
    @Json(name = "courier_phone") val courierPhone: String? = "",
    @Json(name = "delivery_fee") val deliveryFee: Double? = 0.0
)

@JsonClass(generateAdapter = true)
data class SupabaseCourier(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "state_info") val stateInfo: String? = null,
    @Json(name = "status") val status: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseSeller(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "classification") val classification: String? = null,
    @Json(name = "commission_rate") val commissionRate: Double? = null,
    @Json(name = "created_at") val createdAt: Long? = null
)


@JsonClass(generateAdapter = true)
data class SupabaseProfile(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseSignUpRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "data") val data: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseSignInRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    @Json(name = "id") val id: String? = null,
    @Json(name = "email") val email: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    @Json(name = "id") val id: String? = null,
    @Json(name = "user") val user: SupabaseUser? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseTokenResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "user") val user: SupabaseUser? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseVerifyOTPRequest(
    @Json(name = "type") val type: String = "signup",
    @Json(name = "email") val email: String,
    @Json(name = "token") val token: String
)

@JsonClass(generateAdapter = true)
data class SupabaseOtpOptions(
    @Json(name = "should_create_user") val shouldCreateUser: Boolean = true
)

@JsonClass(generateAdapter = true)
data class SupabaseOtpRequest(
    @Json(name = "email") val email: String,
    @Json(name = "options") val options: SupabaseOtpOptions = SupabaseOtpOptions()
)

interface SupabaseApi {
    @POST("auth/v1/otp")
    suspend fun signInWithOtp(
        @Body request: SupabaseOtpRequest
    ): retrofit2.Response<okhttp3.ResponseBody>

    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body request: SupabaseSignUpRequest
    ): SupabaseAuthResponse

    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(
        @Body request: SupabaseSignInRequest
    ): SupabaseTokenResponse

    @POST("auth/v1/verify")
    suspend fun verifyOTP(
        @Body request: SupabaseVerifyOTPRequest
    ): SupabaseTokenResponse

    @GET("rest/v1/products")
    suspend fun getProducts(
        @Query("select") select: String = "*"
    ): List<SupabaseProduct>

    @POST("rest/v1/products")
    suspend fun insertProducts(
        @Body products: List<SupabaseProduct>
    ): okhttp3.ResponseBody

    @retrofit2.http.PATCH("rest/v1/products")
    suspend fun updateProduct(
        @Query("id") idFilter: String,
        @Body product: SupabaseProduct
    ): List<SupabaseProduct>

    @retrofit2.http.DELETE("rest/v1/products")
    suspend fun deleteProduct(
        @Query("id") idFilter: String
    )

    @retrofit2.http.PATCH("rest/v1/orders")
    suspend fun updateOrderStatus(
        @Query("order_id") orderIdFilter: String,
        @Body statusFields: Map<String, String>
    ): List<SupabaseOrder>

    @POST("rest/v1/orders")
    suspend fun insertOrders(
        @Body orders: List<SupabaseOrder>
    ): okhttp3.ResponseBody

    @GET("rest/v1/orders")
    suspend fun getOrders(
        @Query("select") select: String = "*"
    ): List<SupabaseOrder>

    @POST("rest/v1/profiles")
    suspend fun insertProfiles(
        @Body profiles: List<SupabaseProfile>
    ): okhttp3.ResponseBody

    @GET("rest/v1/profiles")
    suspend fun getProfiles(
        @Query("select") select: String = "*"
    ): List<SupabaseProfile>

    @GET("rest/v1/profiles")
    suspend fun getProfilesByPhone(
        @Query("select") select: String = "*",
        @Query("phone") phoneFilter: String
    ): List<SupabaseProfile>

    @GET("rest/v1/profiles")
    suspend fun getProfilesByEmail(
        @Query("select") select: String = "*",
        @Query("email") emailFilter: String
    ): List<SupabaseProfile>

    @GET("rest/v1/profiles")
    suspend fun getProfilesWithFilter(
        @Query("select") select: String = "*",
        @Query("id") idFilter: String
    ): List<SupabaseProfile>

    @retrofit2.http.PATCH("rest/v1/profiles")
    suspend fun updateProfile(
        @Query("id") idFilter: String,
        @Body profile: SupabaseProfile
    ): List<SupabaseProfile>

    @GET("rest/v1/couriers")
    suspend fun getCouriers(
        @Query("select") select: String = "*"
    ): List<SupabaseCourier>

    @POST("rest/v1/couriers")
    suspend fun insertCouriers(
        @Body couriers: List<SupabaseCourier>
    ): okhttp3.ResponseBody

    @retrofit2.http.DELETE("rest/v1/couriers")
    suspend fun deleteCourier(
        @Query("id") idFilter: String
    )

    @retrofit2.http.PATCH("rest/v1/couriers")
    suspend fun updateCourier(
        @Query("phone") phoneFilter: String,
        @Body courier: SupabaseCourier
    ): List<SupabaseCourier>

    @GET("rest/v1/sellers")
    suspend fun getSellers(
        @Query("select") select: String = "*"
    ): List<SupabaseSeller>

    @POST("rest/v1/sellers")
    suspend fun insertSellers(
        @Body sellers: List<SupabaseSeller>
    ): okhttp3.ResponseBody

    @retrofit2.http.DELETE("rest/v1/sellers")
    suspend fun deleteSeller(
        @Query("id") idFilter: String
    )

    @retrofit2.http.PATCH("rest/v1/sellers")
    suspend fun updateSeller(
        @Query("email") emailFilter: String,
        @Body seller: SupabaseSeller
    ): List<SupabaseSeller>
}

object SupabaseClient {
    private const val BASE_URL = "https://figyszyedxlmbtaepmyt.supabase.co/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val currentKey = SupabaseConfig.apiKey
            val currentUrl = SupabaseConfig.url.trimEnd('/') + "/"
            
            var request = chain.request()
            val originalUrlObj = request.url
            
            try {
                val parsedUri = android.net.Uri.parse(currentUrl)
                val newUrlObj = originalUrlObj.newBuilder()
                    .scheme(parsedUri.scheme ?: "https")
                    .host(parsedUri.host ?: originalUrlObj.host)
                    .build()
                
                request = request.newBuilder()
                    .url(newUrlObj)
                    .removeHeader("apikey")
                    .removeHeader("Authorization")
                    .addHeader("apikey", currentKey)
                    .addHeader("Authorization", "Bearer $currentKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build()
            } catch (e: Exception) {
                request = request.newBuilder()
                    .removeHeader("apikey")
                    .removeHeader("Authorization")
                    .addHeader("apikey", currentKey)
                    .addHeader("Authorization", "Bearer $currentKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build()
            }
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .build()

    val api: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApi::class.java)
    }

    fun formatEpochToIso(epochMs: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(epochMs))
    }

    fun parseIsoToEpoch(isoStr: String?): Long {
        if (isoStr.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            val format1 = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            format1.timeZone = java.util.TimeZone.getTimeZone("UTC")
            format1.parse(isoStr)?.time ?: System.currentTimeMillis()
        } catch (e1: Exception) {
            try {
                val format2 = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                format2.timeZone = java.util.TimeZone.getTimeZone("UTC")
                format2.parse(isoStr)?.time ?: System.currentTimeMillis()
            } catch (e2: Exception) {
                try {
                    val format3 = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.US)
                    format3.parse(isoStr)?.time ?: System.currentTimeMillis()
                } catch (e3: Exception) {
                    System.currentTimeMillis()
                }
            }
        }
    }

    fun parseError(e: Throwable): String {
        if (e is retrofit2.HttpException) {
            return try {
                val errorBody = e.response()?.errorBody()?.string()
                if (!errorBody.isNullOrBlank()) {
                    val mapAdapter = moshi.adapter(Map::class.java)
                    val map = mapAdapter.fromJson(errorBody)
                    val msg = map?.get("message")?.toString()
                    val details = map?.get("details")?.toString()
                    val hint = map?.get("hint")?.toString()
                    
                    var arabicMsg = when {
                        msg?.contains("invalid login credentials", ignoreCase = true) == true || msg?.contains("invalid_credentials", ignoreCase = true) == true -> {
                            "⚠️ البريد الإلكتروني أو كلمة المرور غير صحيحة! يرجى التحقق من صحة بريدك الإلكتروني ومدخلات كلمة المرور وإعادة المحاولة."
                        }
                        msg?.contains("already registered", ignoreCase = true) == true || msg?.contains("user already exists", ignoreCase = true) == true -> {
                            "⚠️ هذا الحساب مسجل مسبقاً لدينا! يرجى تسجيل الدخول مباشرة بالمنصة."
                        }
                        msg?.contains("Password should be at least", ignoreCase = true) == true -> {
                            "⚠️ يجب أن تتكون كلمة المرور من 6 خانات أو أكثر!"
                        }
                        msg?.contains("Email not confirmed", ignoreCase = true) == true -> {
                            "⚠️ لم يتم تأكيد هذا البريد الإلكتروني بعد! يرجى تفعيل حسابك من رابط التأكيد المرسل لبريدك الإلكتروني."
                        }
                        msg?.contains("Signup requires a valid email", ignoreCase = true) == true -> {
                            "⚠️ صيغة البريد الإلكتروني غير صالحة! يرجى التحقق والتبديل لكتابة بريد إلكتروني حقيقي مجدداً."
                        }
                        else -> msg ?: ""
                    }

                    val builder = java.lang.StringBuilder()
                    if (arabicMsg.isNotEmpty()) {
                        builder.append(arabicMsg)
                    } else if (!msg.isNullOrEmpty()) {
                        builder.append("الرسالة: $msg")
                    }
                    if (!details.isNullOrEmpty()) {
                        if (builder.isNotEmpty()) builder.append("\n")
                        builder.append("التفاصيل: $details")
                    }
                    if (!hint.isNullOrEmpty() && hint != "null") {
                        if (builder.isNotEmpty()) builder.append("\n")
                        builder.append("تلميح: $hint")
                    }
                    if (builder.isEmpty()) errorBody else builder.toString()
                } else {
                    "رمز الخطأ من الخادم (Supabase HTTP ${e.code()})"
                }
            } catch (ex: java.lang.Exception) {
                "خطأ HTTP ${e.code()}: ${e.message()}"
            }
        }
        return e.localizedMessage ?: e.message ?: "حدث خطأ غير متوقع في جلب أو إرسال البيانات"
    }
}
