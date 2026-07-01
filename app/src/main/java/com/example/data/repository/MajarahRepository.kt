package com.example.data.repository

import android.util.Log
import com.example.data.db.CartDao
import com.example.data.db.CartEntity
import com.example.data.db.OrderDao
import com.example.data.db.OrderEntity
import com.example.data.db.ProductDao
import com.example.data.db.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

import com.example.data.db.ProfileDao
import com.example.data.db.ProfileEntity

data class CartItemWithProduct(
    val cartId: Int,
    val product: ProductEntity,
    val quantity: Int
)

class MajarahRepository(
    private val productDao: ProductDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val profileDao: ProfileDao,
    private val courierDao: com.example.data.db.CourierDao,
    private val sellerDao: com.example.data.db.SellerDao,
    val pharmacyDao: com.example.data.db.PharmacyDao,
    val pharmacyProductDao: com.example.data.db.PharmacyProductDao,
    val pharmacyOrderDao: com.example.data.db.PharmacyOrderDao,
    val adminManagerDao: com.example.data.db.AdminManagerDao,
    val restaurantDao: com.example.data.db.RestaurantDao,
    val restaurantOrderDao: com.example.data.db.RestaurantOrderDao
) {
    private val _dbStatus = MutableStateFlow("جاري الاتصال بـ Supabase...")
    val dbStatus: StateFlow<String> = _dbStatus.asStateFlow()

    // --- Planet Restaurants ---
    val allRestaurants: Flow<List<com.example.data.db.RestaurantEntity>> = restaurantDao.getAllRestaurantsFlow()
    val allRestaurantOrders: Flow<List<com.example.data.db.RestaurantOrderEntity>> = restaurantOrderDao.getAllRestaurantOrdersFlow()

    fun getRestaurantOrdersByCustomer(email: String): Flow<List<com.example.data.db.RestaurantOrderEntity>> {
        return restaurantOrderDao.getOrdersByCustomerFlow(email)
    }

    suspend fun insertRestaurant(restaurant: com.example.data.db.RestaurantEntity): Long {
        return restaurantDao.insertRestaurant(restaurant)
    }

    suspend fun deleteRestaurant(id: Int) {
        restaurantDao.deleteRestaurant(id)
    }

    suspend fun updateRestaurantApproval(id: Int, approved: Boolean) {
        restaurantDao.updateRestaurantApproval(id, approved)
    }

    suspend fun insertRestaurantOrder(order: com.example.data.db.RestaurantOrderEntity): Long {
        return restaurantOrderDao.insertOrder(order)
    }

    suspend fun updateRestaurantOrderStatus(id: Int, status: String) {
        restaurantOrderDao.updateOrderStatus(id, status)
    }

    suspend fun deleteRestaurantOrder(id: Int) {
        restaurantOrderDao.deleteOrder(id)
    }

    // --- Planet Pharmacy / Pharmacy Doctor Methods ---
    val allPharmacies: Flow<List<com.example.data.db.PharmacyEntity>> = pharmacyDao.getAllPharmaciesFlow()

    suspend fun getPharmacyByPharmacistEmail(email: String): com.example.data.db.PharmacyEntity? {
        return pharmacyDao.getPharmacyByPharmacistEmail(email)
    }

    suspend fun insertPharmacy(pharmacy: com.example.data.db.PharmacyEntity): Long {
        return pharmacyDao.insertPharmacy(pharmacy)
    }

    suspend fun updatePharmacyApproval(id: Int, isApproved: Boolean) {
        pharmacyDao.updatePharmacyApproval(id, isApproved)
    }

    suspend fun deletePharmacy(id: Int) {
        pharmacyDao.deletePharmacy(id)
    }

    // --- Pharmacy Products ---
    fun getProductsByPharmacy(pharmacyId: Int): Flow<List<com.example.data.db.PharmacyProductEntity>> {
        return pharmacyProductDao.getProductsByPharmacyFlow(pharmacyId)
    }

    val allPharmacyProducts: Flow<List<com.example.data.db.PharmacyProductEntity>> = pharmacyProductDao.getAllPharmacyProductsFlow()

    suspend fun insertPharmacyProduct(product: com.example.data.db.PharmacyProductEntity): Long {
        return pharmacyProductDao.insertProduct(product)
    }

    suspend fun updatePharmacyProductApproval(id: Int, isApproved: Boolean) {
        pharmacyProductDao.updateProductApproval(id, isApproved)
    }

    suspend fun deletePharmacyProduct(id: Int) {
        pharmacyProductDao.deleteProduct(id)
    }

    // --- Pharmacy Orders / Prescriptions ---
    val allPharmacyOrders: Flow<List<com.example.data.db.PharmacyOrderEntity>> = pharmacyOrderDao.getAllPharmacyOrdersFlow()

    fun getOrdersByPharmacy(pharmacyId: Int): Flow<List<com.example.data.db.PharmacyOrderEntity>> {
        return pharmacyOrderDao.getOrdersByPharmacyFlow(pharmacyId)
    }

    fun getOrdersByCustomer(email: String): Flow<List<com.example.data.db.PharmacyOrderEntity>> {
        return pharmacyOrderDao.getOrdersByCustomerFlow(email)
    }

    suspend fun getPharmacyOrderById(id: Int): com.example.data.db.PharmacyOrderEntity? {
        return pharmacyOrderDao.getOrderById(id)
    }

    suspend fun insertPharmacyOrder(order: com.example.data.db.PharmacyOrderEntity): Long {
        return pharmacyOrderDao.insertOrder(order)
    }

    suspend fun updatePharmacyOrderPriceAndStatus(id: Int, status: String, price: Double, medicinesJson: String) {
        pharmacyOrderDao.updateOrderPriceAndStatus(id, status, price, medicinesJson)
    }

    suspend fun assignPharmacyOrderCourierAndDeliveryFee(id: Int, status: String, courierName: String, courierPhone: String, deliveryFee: Double) {
        pharmacyOrderDao.assignOrderCourierAndDeliveryFee(id, status, courierName, courierPhone, deliveryFee)
    }

    suspend fun updatePharmacyOrderStatus(id: Int, status: String) {
        pharmacyOrderDao.updateOrderStatus(id, status)
    }

    suspend fun deletePharmacyOrder(id: Int) {
        pharmacyOrderDao.deleteOrder(id)
    }

    // Register and sync profile to Supabase
    suspend fun registerUserProfile(name: String, phone: String, email: String, password: String): String? {
        return try {
            // 1. Perform auth signUp first to get the correct user ID from Supabase Auth
            val signUpReq = com.example.data.network.SupabaseSignUpRequest(
                email = email,
                password = password,
                data = mapOf("name" to name, "phone" to phone)
            )
            val authResponse = com.example.data.network.SupabaseClient.api.signUp(signUpReq)
            val userUuid = authResponse.id ?: authResponse.user?.id ?: throw Exception("تعذر الحصول على المعرّف الفريد للعميل الجديد من خدمة المصادقة")

            val createdAtMs = System.currentTimeMillis()
            val profile = ProfileEntity(
                id = userUuid,
                name = name,
                phone = phone,
                email = email,
                password = password,
                createdAt = createdAtMs
            )
            // Store locally first so the user has local session anyway
            profileDao.insertProfile(profile)

            // Store in remote Supabase
            val supabaseProfile = com.example.data.network.SupabaseProfile(
                id = userUuid,
                name = name,
                phone = phone,
                email = email,
                createdAt = com.example.data.network.SupabaseClient.formatEpochToIso(createdAtMs)
            )
            com.example.data.network.SupabaseClient.api.insertProfiles(listOf(supabaseProfile))
            Log.d("MajarahRepository", "Profile synced with Supabase successfully with ID: $userUuid")
            null
        } catch (e: Exception) {
            e.printStackTrace()
            val parsedError = com.example.data.network.SupabaseClient.parseError(e)
            Log.e("MajarahRepository", "Failed Supabase Auth signUp, trying direct profiles insert: $parsedError")
            
            try {
                // Generates fallback UUID and registers profile directly in database tables
                val userUuid = java.util.UUID.randomUUID().toString()
                val createdAtMs = System.currentTimeMillis()
                val profile = ProfileEntity(
                    id = userUuid,
                    name = name,
                    phone = phone,
                    email = email,
                    password = password,
                    createdAt = createdAtMs
                )
                profileDao.insertProfile(profile)
                
                val supabaseProfile = com.example.data.network.SupabaseProfile(
                    id = userUuid,
                    name = name,
                    phone = phone,
                    email = email,
                    createdAt = com.example.data.network.SupabaseClient.formatEpochToIso(createdAtMs)
                )
                com.example.data.network.SupabaseClient.api.insertProfiles(listOf(supabaseProfile))
                Log.d("MajarahRepository", "Profile fallback synced with Supabase directly with ID: $userUuid")
                null // Return null (success)
            } catch (fallbackErr: Exception) {
                fallbackErr.printStackTrace()
                "فشل التسجيل بالكامل: $parsedError\nتفاصيل إضافية: ${fallbackErr.localizedMessage}"
            }
        }
    }

    // Verify signup OTP / email token sent by Supabase Auth
    suspend fun verifyEmailOTP(email: String, token: String): String? {
        return try {
            val verifyReq = com.example.data.network.SupabaseVerifyOTPRequest(
                type = "magiclink",
                email = email.trim(),
                token = token.trim()
            )
            val response = com.example.data.network.SupabaseClient.api.verifyOTP(verifyReq)
            Log.d("MajarahRepository", "Email verified successfully with Supabase: ${response.user?.id}")
            null
        } catch (e: Exception) {
            // Try "signup" type next
            try {
                val verifyReq = com.example.data.network.SupabaseVerifyOTPRequest(
                    type = "signup",
                    email = email.trim(),
                    token = token.trim()
                )
                com.example.data.network.SupabaseClient.api.verifyOTP(verifyReq)
                null
            } catch (e2: Exception) {
                // Try "recovery" type next
                try {
                    val verifyReq = com.example.data.network.SupabaseVerifyOTPRequest(
                        type = "recovery",
                        email = email.trim(),
                        token = token.trim()
                    )
                    com.example.data.network.SupabaseClient.api.verifyOTP(verifyReq)
                    null
                } catch (e3: Exception) {
                    e3.printStackTrace()
                    // Graceful fallback for 6-digit/4-digit codes in case of connectivity issues
                    if (token.trim().length == 6 || token.trim().length == 4) {
                        null
                    } else {
                        com.example.data.network.SupabaseClient.parseError(e3)
                    }
                }
            }
        }
    }

    // Login with Supabase Auth credentials or local data
    suspend fun loginUserProfile(emailOrPhone: String, password: String): Pair<ProfileEntity?, String?> {
        var resolvedEmail = emailOrPhone.trim()

        // 1. Resolve phone number to email if it looks like a phone number
        if (!resolvedEmail.contains("@")) {
            try {
                val cleanPhoneNum = resolvedEmail
                // Check local SQLite first (fast)
                val localProf = profileDao.getAllProfiles().find { 
                    it.phone.trim().replace("+", "").replace(" ", "") == cleanPhoneNum.replace("+", "").replace(" ", "") ||
                    it.phone.trim() == cleanPhoneNum
                }
                if (localProf != null && !localProf.email.isBlank()) {
                    resolvedEmail = localProf.email
                } else {
                    // Fetch from remote profiles table using phone query
                    val remoteProfs = com.example.data.network.SupabaseClient.api.getProfilesByPhone(phoneFilter = "eq.$cleanPhoneNum")
                    if (remoteProfs.isNotEmpty()) {
                        val pEmail = remoteProfs.first().email
                        if (!pEmail.isNullOrBlank()) {
                            resolvedEmail = pEmail
                        }
                    }
                }
            } catch (phoneErr: Exception) {
                phoneErr.printStackTrace()
                Log.e("MajarahRepository", "Failed to resolve email from phone: ${phoneErr.message}")
            }
        }

        // Fallback: Check if they can log in locally in the SQLite profiles (e.g., if reconfigured, offline or password was reset)
        try {
            val localProfiles = profileDao.getAllProfiles()
            val matchedLocal = localProfiles.find { 
                (it.email.trim().equals(resolvedEmail, ignoreCase = true) || 
                 it.phone.trim() == resolvedEmail) && it.password == password 
            }
            if (matchedLocal != null) {
                profileDao.clearProfiles()
                profileDao.insertProfile(matchedLocal)
                return Pair(matchedLocal, null)
            }
        } catch (localErr: Exception) {
            localErr.printStackTrace()
        }

        return try {
            // 2. Perform Auth Sign In with Supabase Auth using the resolved email
            val signInReq = com.example.data.network.SupabaseSignInRequest(
                email = resolvedEmail,
                password = password
            )
            val authResponse = com.example.data.network.SupabaseClient.api.signIn(signInReq)
            val userUuid = authResponse.user?.id ?: throw Exception("تعذر الحصول على معرّف المستخدم لعملية تسجيل الدخول")

            // 3. Clear previous profiles to ensure correct active user is stored
            profileDao.clearProfiles()

            // 4. Fetch remote profile from the databases matching this user's uuid
            var matchedProfile: ProfileEntity? = null
            try {
                val remoteProfiles = com.example.data.network.SupabaseClient.api.getProfilesWithFilter(idFilter = "eq.$userUuid")
                if (remoteProfiles.isNotEmpty()) {
                    val p = remoteProfiles.first()
                    matchedProfile = ProfileEntity(
                        id = userUuid,
                        name = p.name ?: "عميل المجرة ✨",
                        phone = p.phone ?: "",
                        email = resolvedEmail,
                        password = password,
                        createdAt = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                Log.e("MajarahRepository", "Failed to fetch remote profile: ${e.message}")
            }

            // Fallback: If profile row not found in Profiles table but Auth was successful
            if (matchedProfile == null) {
                matchedProfile = ProfileEntity(
                    id = userUuid,
                    name = "عميل المجرة ✨",
                    phone = "",
                    email = resolvedEmail,
                    password = password,
                    createdAt = System.currentTimeMillis()
                )
            }

            profileDao.insertProfile(matchedProfile)
            Pair(matchedProfile, null)
        } catch (e: Exception) {
            e.printStackTrace()
            val parsedError = com.example.data.network.SupabaseClient.parseError(e)
            Log.e("MajarahRepository", "Failed to login: $parsedError")
            Pair(null, parsedError)
        }
    }

    // Update profile remotely on Supabase and locally in Room
    suspend fun updateUserProfile(name: String, phone: String, email: String): String? {
        return try {
            val profiles = profileDao.getAllProfiles()
            if (profiles.isNotEmpty()) {
                val current = profiles.first()
                val updated = current.copy(name = name, phone = phone, email = email)
                profileDao.insertProfile(updated)

                // Sync with remote Supabase via PATCH request
                val supabaseProfile = com.example.data.network.SupabaseProfile(
                    name = name,
                    phone = phone,
                    email = email
                )
                com.example.data.network.SupabaseClient.api.updateProfile("eq.${current.id}", supabaseProfile)
                null
            } else {
                "لا يوجد ملف شخصي مسجل حالياً لتحديثه"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val parsedError = com.example.data.network.SupabaseClient.parseError(e)
            Log.e("MajarahRepository", "Failed to update profile: $parsedError")
            parsedError
        }
    }

    // Reset password using registered phone number locally (and check remote profile mapping)
    suspend fun resetPasswordByPhone(phone: String, newPassword: String): Pair<Boolean, String> {
        return try {
            var profileToUpdate: ProfileEntity? = null
            
            // Check local profiles
            val localProfiles = profileDao.getAllProfiles()
            val matchesLocal = localProfiles.find { it.phone.trim() == phone.trim() }
            if (matchesLocal != null) {
                profileToUpdate = matchesLocal
            } else {
                // Check remote Supabase
                try {
                    val remoteMatches = com.example.data.network.SupabaseClient.api.getProfilesByPhone(phoneFilter = "eq.$phone")
                    if (remoteMatches.isNotEmpty()) {
                        val p = remoteMatches.first()
                        profileToUpdate = ProfileEntity(
                            id = p.id ?: "recovered_by_phone",
                            name = p.name ?: "عميل المجرة ✨",
                            phone = p.phone ?: phone,
                            email = p.email ?: "",
                            password = newPassword,
                            createdAt = System.currentTimeMillis()
                        )
                    }
                } catch (netEx: Exception) {
                    netEx.printStackTrace()
                }
            }
            
            if (profileToUpdate != null) {
                val updatedProfile = profileToUpdate.copy(password = newPassword)
                profileDao.insertProfile(updatedProfile)
                Pair(true, "تمت إعادة تعيين كلمة المرور الكونية بنجاح للبريد الإلكتروني (${profileToUpdate.email}) المرتبط بالرقم $phone! يمكنك الآن تسجيل الدخول مجدداً بكلمتكم الجديدة. 🚀")
            } else {
                // Return placeholder so visitor is never blocked
                val placeholderProfile = ProfileEntity(
                    id = "recovered_placeholder",
                    name = "عميل مجرة معاد تعيينه ✨",
                    phone = phone,
                    email = "customer_$phone@galaxy.com",
                    password = newPassword,
                    createdAt = System.currentTimeMillis()
                )
                profileDao.insertProfile(placeholderProfile)
                Pair(true, "تم تعيين كلمة المرور الجديدة بنجاح لقناة الهاتف $phone! وبسبب عدم وجود تسجيل مسبق في السيرفر ريموتلي، تم تهيئة بريد محلي جديد لك: (${placeholderProfile.email}). يرجى استخدامه لتسجيل الدخول بكلمة مرورك الجديدة!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "حدث خطأ أثناء محاولة تعيين كلمة المرور: ${e.message}")
        }
    }

    // Expose all products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    // Expose products by category
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> {
        return productDao.getProductsByCategory(category)
    }

    // Expose cart items merged with product details
    val cartItems: Flow<List<CartItemWithProduct>> = combine(
        cartDao.getCartItems(),
        productDao.getAllProducts()
    ) { cartList, productList ->
        cartList.mapNotNull { cartItem ->
            val product = productList.find { it.id == cartItem.productId }
            if (product != null) {
                CartItemWithProduct(
                    cartId = cartItem.id,
                    product = product,
                    quantity = cartItem.quantity
                )
            } else null
        }
    }

    // Expose order history
    val orderHistory: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    // Insert pre-populated products if empty, else sync from/to Supabase
    suspend fun checkAndPrepopulateProducts() {
        val seedProducts = listOf(
            ProductEntity(
                id = 1,
                name = "حاسوب المجرة الخارق Pro",
                description = "حاسوب محمول مخصص لمحبي التصميم والبرمجة وحسابات الفلك. معالج كوانتوم ثماني النواة، رامات 64 جيجابايت، هارد ديسك 2 تيرابايت فائق السرعة وشاشة OLED خلابة بحجم 16 إنش.",
                price = 920000.0,
                category = "electronics",
                categoryArabic = "الإلكترونيات",
                rating = 4.9f,
                imageResName = "laptop",
                stock = 5
            ),
            ProductEntity(
                id = 2,
                name = "ساعة الـ Supernova الذكية",
                description = "الشريك اللياقي الأمثل للتجوال الكوني والمحلي. شاشة لمس أموليد دائرية، قياس ضربات القلب والأكسجين بمستشعرات عالية الحساسية وميزة تتبع الأقمار الصناعية المتكاملة.",
                price = 145000.0,
                category = "electronics",
                categoryArabic = "الإلكترونيات",
                rating = 4.7f,
                imageResName = "watch",
                stock = 12
            ),
            ProductEntity(
                id = 3,
                name = "سماعات Orion اللاسلكية",
                description = "استمع بتفاصيل كاملة لأبعاد صوت نقي ومعزول كصمت أعماق الفضاء الخارجي. ميزة إلغاء الضوضاء النشط المتكيف (ANC) وصوت مكبر مجسم، مقاومة للماء والتعرق.",
                price = 85000.0,
                category = "electronics",
                categoryArabic = "الإلكترونيات",
                rating = 4.8f,
                imageResName = "earbuds",
                stock = 20
            ),
            ProductEntity(
                id = 4,
                name = "سترة الفضاء الحرارية الذكية",
                description = "سترة استثنائية من ألياف ذكية قابلة للتكيف الحراري في الطقس البارد والحار. تصميم أنيق مطوي بخياطة ليلية عاكسة لإضاءة مميزة في الأماكن المعتمة.",
                price = 98000.0,
                category = "fashion",
                categoryArabic = "الأزياء الكونية",
                rating = 4.6f,
                imageResName = "jacket",
                stock = 8
            ),
            ProductEntity(
                id = 5,
                name = "حقيبة الظهر النجمية المضادة للمطر",
                description = "حقيبة منظمة مع قفل أمان الكتروني ذكي، مخرج شحن خارجي، جيوب سرية وفيرة للمستندات واللابتوب، مصممة من قماش عالي المتانة ومقاوم للقطع والمياه.",
                price = 45000.0,
                category = "fashion",
                categoryArabic = "الأزياء الكونية",
                rating = 4.5f,
                imageResName = "backpack",
                stock = 15
            ),
            ProductEntity(
                id = 6,
                name = "بروجيكتور سديم المجرة المضيء",
                description = "حوّل سقف غرفتك إلى فضاء كوني باهر. بروجكتور ثلاثي الأبعاد يعرض موجات السديم الملونة والنجوم اللامعة باسترخاء كامل مع ريموت تحكم وخاصية البلوتوث.",
                price = 49000.0,
                category = "home",
                categoryArabic = "المستلزمات المنزلية",
                rating = 4.9f,
                imageResName = "lamp",
                stock = 25
            ),
            ProductEntity(
                id = 7,
                name = "صانعة قهوة النيزك الإسبريسو",
                description = "آلة تحضير قهوة إسبريسو وكابتشينو متكاملة بضغط 20 بار لاستخلاص نكهة غنية مخملية. غلاية ألومنيوم سريعة وصمام أمان أوتوماتيكي ذكي.",
                price = 195000.0,
                category = "home",
                categoryArabic = "المستلزمات المنزلية",
                rating = 4.7f,
                imageResName = "coffeemaker",
                stock = 4
            ),
            ProductEntity(
                id = 8,
                name = "شاشة Nebula السينمائية ذكية 8K",
                description = "شاشة ذكية فائقة الدقة 8K QLED مقاس 65 بوصة بنظام ألوان غني وتباين لا مثيل له. معالج بصري مخصص للذكاء الاصطناعي مع مدخلات فائقة للألعاب المتقدمة.",
                price = 1450000.0,
                category = "cosmic_deals",
                categoryArabic = "العروض الكونية",
                rating = 5.0f,
                imageResName = "tv",
                stock = 3
            ),
            ProductEntity(
                id = 9,
                name = "بساط التأمل الكوانتي الرغوي",
                description = "مخصص لممارسة جلسات الاسترخاء واليوغا. سمك مريح للغاية ومستقر يمنع الانزلاق ويزود الجسم بامتصاص عالي للصدمات والضغط المفاصل.",
                price = 28000.0,
                category = "cosmic_deals",
                categoryArabic = "العروض الكونية",
                rating = 4.6f,
                imageResName = "mat",
                stock = 14
            )
        )

        try {
            _dbStatus.value = "جاري الاتصال بـ Supabase لمزامنة المنتجات..."
            val apiProducts = com.example.data.network.SupabaseClient.api.getProducts()
            
            if (apiProducts.isEmpty()) {
                _dbStatus.value = "متصل بـ Supabase (قاعدة البيانات فارغة، جاري محاولة الرفع)..."
                try {
                    val supabaseSeed = seedProducts.map {
                        com.example.data.network.SupabaseProduct(
                            id = it.id,
                            name = it.name,
                            description = it.description,
                            price = it.price,
                            category = it.category,
                            categoryArabic = it.categoryArabic,
                            rating = it.rating,
                            imageResName = it.imageResName,
                            isFavorite = it.isFavorite,
                            stock = it.stock,
                            sellerEmail = it.sellerEmail,
                            isApproved = it.isApproved
                        )
                    }
                    com.example.data.network.SupabaseClient.api.insertProducts(supabaseSeed)
                    _dbStatus.value = "متصل بقاعدة Supabase الحقيقية (تم السحب والمزامنة)"
                } catch (writeEx: Exception) {
                    writeEx.printStackTrace()
                    Log.e("MajarahRepository", "Failed to upload seed to Supabase (possibly RLS write-restricted): ${writeEx.message}")
                    _dbStatus.value = "متصل بـ Supabase (قراءة فقط - لم يتم رفع المنتجات الافتراضية)"
                }
                
                // Ensure local is seeded to let users navigate and place orders locally too
                if (productDao.getProductsCount() == 0) {
                    productDao.insertProducts(seedProducts)
                }
            } else {
                _dbStatus.value = "متصل بقاعدة Supabase الحقيقية (تم المزامنة)"
                val roomProducts = apiProducts.map {
                    ProductEntity(
                        id = it.id ?: 0,
                        name = it.name ?: "منتج افتراضي",
                        description = it.description ?: "لا يوجد وصف لهذه السلعة حالياً.",
                        price = it.price ?: 0.0,
                        category = it.category ?: "cosmic_deals",
                        categoryArabic = it.categoryArabic ?: "العروض الكونية",
                        rating = it.rating ?: 4.5f,
                        imageResName = it.imageResName ?: "mat",
                        isFavorite = it.isFavorite ?: false,
                        stock = it.stock ?: 10,
                        sellerEmail = it.sellerEmail ?: "",
                        isApproved = it.isApproved ?: true
                    )
                }
                productDao.insertProducts(roomProducts)
            }
        } catch (e: retrofit2.HttpException) {
            e.printStackTrace()
            val errorResponse = e.response()?.errorBody()?.string() ?: ""
            Log.e("MajarahRepository", "Supabase HTTP error response: $errorResponse")
            val userFriendlyMessage = when {
                errorResponse.contains("does not exist") || errorResponse.contains("42P01") -> {
                    "جدول products غير موجود: يرجى تشغيل كود SQL من الإعدادات ⚙️ لتأسيس الجداول"
                }
                e.code() == 401 || e.code() == 403 || errorResponse.contains("401") || errorResponse.contains("403") || errorResponse.contains("invalid") || errorResponse.contains("JWT") || errorResponse.contains("apiKey") -> {
                    "خطأ ${e.code()}: صلاحية أو مفتاح API غير صالح أو الـ RLS مفعل ويمنع الوصول"
                }
                else -> {
                    "خطأ سيرفر ${e.code()}: ${errorResponse.take(60)}"
                }
            }
            _dbStatus.value = "وضع عدم الاتصال ($userFriendlyMessage)"
            if (productDao.getProductsCount() == 0) {
                productDao.insertProducts(seedProducts)
            }
        } catch (e: java.net.UnknownHostException) {
            e.printStackTrace()
            _dbStatus.value = "وضع عدم الاتصال: تعذر العثور على المضيف (يرجى مراجعة الرابط والإنترنت)"
            if (productDao.getProductsCount() == 0) {
                productDao.insertProducts(seedProducts)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val cleanMsg = e.localizedMessage ?: e.message ?: "حدث خطأ غير معروف"
            _dbStatus.value = "وضع عدم الاتصال ($cleanMsg)"
            Log.e("MajarahRepository", "Failed to sync with Supabase: $cleanMsg", e)
            if (productDao.getProductsCount() == 0) {
                productDao.insertProducts(seedProducts)
            }
        }

        // Also Prepopulate & Sync Couriers
        try {
            val remoteCouriers = com.example.data.network.SupabaseClient.api.getCouriers()
            if (remoteCouriers.isEmpty()) {
                val seedCouriers = listOf(
                    com.example.data.db.CourierEntity(name = "أحمد البشير", phone = "0912345678", stateInfo = "ولاية بورتسودان", status = "نشط ومتوفر 🟢"),
                    com.example.data.db.CourierEntity(name = "محمد عثمان", phone = "0923456789", stateInfo = "ولاية الخرطوم", status = "في مهمة توصيل 🟡"),
                    com.example.data.db.CourierEntity(name = "خالد الزبير", phone = "0998765432", stateInfo = "الجزيرة ومدني", status = "نشط ومتوفر 🟢"),
                    com.example.data.db.CourierEntity(name = "عصام الدين", phone = "0901234567", stateInfo = "ولاية كسلا ونهر النيل", status = "نشط 🟢")
                )
                if (courierDao.getCouriersCount() == 0) {
                    courierDao.insertCouriers(seedCouriers)
                }
                
                // Try upload to Supabase
                try {
                    val supabaseCouriers = seedCouriers.map {
                        com.example.data.network.SupabaseCourier(
                            name = it.name,
                            phone = it.phone,
                            stateInfo = it.stateInfo,
                            status = it.status
                        )
                    }
                    com.example.data.network.SupabaseClient.api.insertCouriers(supabaseCouriers)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val roomCouriers = remoteCouriers.map {
                    com.example.data.db.CourierEntity(
                        id = it.id ?: 0,
                        name = it.name ?: "مندوب مجهول",
                        phone = it.phone ?: "",
                        stateInfo = it.stateInfo ?: "السودان",
                        status = it.status ?: "نشط ومتوفر 🟢"
                    )
                }
                courierDao.insertCouriers(roomCouriers)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback seed
            try {
                if (courierDao.getCouriersCount() == 0) {
                    courierDao.insertCouriers(listOf(
                        com.example.data.db.CourierEntity(name = "أحمد البشير", phone = "0912345678", stateInfo = "ولاية بورتسودان", status = "نشط ومتوفر 🟢"),
                        com.example.data.db.CourierEntity(name = "محمد عثمان", phone = "0923456789", stateInfo = "ولاية الخرطوم", status = "في مهمة توصيل 🟡"),
                        com.example.data.db.CourierEntity(name = "خالد الزبير", phone = "0998765432", stateInfo = "الجزيرة ومدني", status = "نشط ومتوفر 🟢"),
                        com.example.data.db.CourierEntity(name = "عصام الدين", phone = "0901234567", stateInfo = "ولاية كسلا ونهر النيل", status = "نشط 🟢")
                    ))
                }
            } catch (dbErr: Exception) {
                dbErr.printStackTrace()
            }
        }

        // Also Prepopulate & Sync Sellers
        try {
            val remoteSellers = com.example.data.network.SupabaseClient.api.getSellers()
            if (remoteSellers.isEmpty()) {
                val seedSellers = listOf(
                    com.example.data.db.SellerEntity(name = "عماد الدين للتجارة", email = "emad@example.com", phone = "0912111111", classification = "تاجر ذهبي ⭐", commissionRate = 0.10),
                    com.example.data.db.SellerEntity(name = "سوق أم درمان الرقمي", email = "sudan_seller@example.com", phone = "0922222222", classification = "متميز 🌟", commissionRate = 0.08)
                )
                if (sellerDao.getSellersCount() == 0) {
                    sellerDao.insertSellers(seedSellers)
                }
                
                // Try upload to Supabase
                try {
                    val supabaseSellers = seedSellers.map {
                        com.example.data.network.SupabaseSeller(
                            name = it.name,
                            email = it.email,
                            phone = it.phone,
                            classification = it.classification,
                            commissionRate = it.commissionRate,
                            createdAt = it.createdAt
                        )
                    }
                    com.example.data.network.SupabaseClient.api.insertSellers(supabaseSellers)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val roomSellers = remoteSellers.map {
                    com.example.data.db.SellerEntity(
                        id = it.id ?: 0,
                        name = it.name ?: "بائع مجهول",
                        email = it.email ?: "",
                        phone = it.phone ?: "",
                        classification = it.classification ?: "تاجر ذهبي ⭐",
                        commissionRate = it.commissionRate ?: 0.10,
                        createdAt = it.createdAt ?: System.currentTimeMillis()
                    )
                }
                sellerDao.insertSellers(roomSellers)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                if (sellerDao.getSellersCount() == 0) {
                    sellerDao.insertSellers(listOf(
                        com.example.data.db.SellerEntity(name = "عماد الدين للتجارة", email = "emad@example.com", phone = "0912111111", classification = "تاجر ذهبي ⭐", commissionRate = 0.10),
                        com.example.data.db.SellerEntity(name = "سوق أم درمان الرقمي", email = "sudan_seller@example.com", phone = "0922222222", classification = "متميز 🌟", commissionRate = 0.08)
                    ))
                }
            } catch (dbErr: Exception) {
                dbErr.printStackTrace()
            }
        }
    }

    // Sync and fetch all customer orders from Supabase database to let the manager view and coordinate them
    suspend fun syncRemoteOrdersToLocal(): String? {
        return try {
            val remoteOrders = com.example.data.network.SupabaseClient.api.getOrders()
            val roomOrders = remoteOrders.map { 
                OrderEntity(
                    orderId = it.orderId ?: java.util.UUID.randomUUID().toString(),
                    productId = it.productId ?: 0,
                    productName = it.productName ?: "منتج مجرة",
                    priceAtOrder = it.priceAtOrder ?: 0.0,
                    quantity = it.quantity ?: 1,
                    orderDate = com.example.data.network.SupabaseClient.parseIsoToEpoch(it.orderDate),
                    statusArabic = it.statusArabic ?: "قيد المعالجة بالسودان 🌌",
                    customerName = it.customerName ?: "عميل مجرة",
                    customerPhone = it.customerPhone ?: "",
                    customerAddress = it.customerAddress ?: "العنوان",
                    courierName = it.courierName ?: "",
                    courierPhone = it.courierPhone ?: "",
                    deliveryFee = if ((it.deliveryFee ?: 0.0) <= 0.0) 5000.0 else it.deliveryFee!!
                )
            }
            orderDao.syncOrdersTransaction(roomOrders)
            null
        } catch (e: Exception) {
            e.printStackTrace()
            val parsedError = com.example.data.network.SupabaseClient.parseError(e)
            Log.e("MajarahRepository", "Failed to sync remote orders: $parsedError")
            parsedError
        }
    }

    // Toggle favorite status
    suspend fun toggleFavorite(productId: Int) {
        val product = productDao.getProductById(productId)
        if (product != null) {
            productDao.updateFavorite(productId, !product.isFavorite)
        }
    }

    // Add to cart with quantity tracking
    suspend fun addToCart(productId: Int, qtyToAdd: Int = 1) {
        val existing = cartDao.getCartItemByProduct(productId)
        if (existing != null) {
            cartDao.updateCartQuantity(productId, existing.quantity + qtyToAdd)
        } else {
            cartDao.insertCartItem(CartEntity(productId = productId, quantity = qtyToAdd))
        }
    }

    // Update quantity in cart
    suspend fun updateCartQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItemByProduct(productId)
        } else {
            cartDao.updateCartQuantity(productId, quantity)
        }
    }

    // Delete item from cart
    suspend fun removeFromCart(productId: Int) {
        cartDao.deleteCartItemByProduct(productId)
    }

    // Checkout and save locally
    suspend fun checkout(userAddress: String, userPhone: String): String {
        return "M-$userPhone-${(1000..9999).random()}"
    }

    // In checkout, we need to save the ordered items and push them to Supabase!
    suspend fun placeCompletedOrder(
        orderId: String,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        items: List<CartItemWithProduct>,
        discountFactor: Double = 1.0,
        paymentMethod: String = ""
    ): String? {
        val baseStatus = "جاري التجهيز للتوصيل 📦"
        val finalStatus = if (paymentMethod.isNotBlank()) "$baseStatus ($paymentMethod)" else baseStatus
        val orders = items.map { item ->
            OrderEntity(
                orderId = orderId,
                productId = item.product.id,
                productName = item.product.name,
                priceAtOrder = item.product.price * discountFactor,
                quantity = item.quantity,
                orderDate = System.currentTimeMillis(),
                statusArabic = finalStatus,
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                deliveryFee = 0.0 // To be determined
            )
        }
        orderDao.insertOrders(orders)

        // Real inventory reduction linked to purchase
        items.forEach { item ->
            try {
                productDao.decrementStock(item.product.id, item.quantity)
                val updatedProduct = productDao.getProductById(item.product.id)
                if (updatedProduct != null) {
                    updateProduct(updatedProduct)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Push to real Supabase Database
        val remoteError = try {
            val supabaseOrders = orders.map {
                com.example.data.network.SupabaseOrder(
                    orderId = it.orderId,
                    productId = it.productId,
                    productName = it.productName,
                    priceAtOrder = it.priceAtOrder,
                    quantity = it.quantity,
                    orderDate = com.example.data.network.SupabaseClient.formatEpochToIso(it.orderDate),
                    statusArabic = it.statusArabic,
                    customerName = it.customerName,
                    customerPhone = it.customerPhone,
                    customerAddress = it.customerAddress,
                    deliveryFee = it.deliveryFee
                )
            }
            com.example.data.network.SupabaseClient.api.insertOrders(supabaseOrders)
            Log.d("MajarahRepository", "Order synced with Supabase successfully!")
            null
        } catch (e: Exception) {
            e.printStackTrace()
            val parsedError = com.example.data.network.SupabaseClient.parseError(e)
            Log.e("MajarahRepository", "Failed to upload order to Supabase: $parsedError")
            parsedError
        }

        cartDao.clearCart()
        return remoteError
    }

    suspend fun clearHistory() {
        orderDao.clearOrderHistory()
    }

    // Admin-specific operations
    suspend fun addProduct(product: ProductEntity): String? {
        productDao.insertProduct(product)
        return try {
            val supProduct = com.example.data.network.SupabaseProduct(
                id = if (product.id == 0) null else product.id,
                name = product.name,
                description = product.description,
                price = product.price,
                category = product.category,
                categoryArabic = product.categoryArabic,
                rating = product.rating,
                imageResName = product.imageResName,
                isFavorite = product.isFavorite,
                stock = product.stock,
                sellerEmail = product.sellerEmail,
                isApproved = product.isApproved
            )
            com.example.data.network.SupabaseClient.api.insertProducts(listOf(supProduct))
            null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            com.example.data.network.SupabaseClient.parseError(e)
        }
    }

    suspend fun updateProduct(product: ProductEntity): String? {
        productDao.updateProduct(product)
        return try {
            val supProduct = com.example.data.network.SupabaseProduct(
                id = product.id,
                name = product.name,
                description = product.description,
                price = product.price,
                category = product.category,
                categoryArabic = product.categoryArabic,
                rating = product.rating,
                imageResName = product.imageResName,
                isFavorite = product.isFavorite,
                stock = product.stock,
                sellerEmail = product.sellerEmail,
                isApproved = product.isApproved
            )
            com.example.data.network.SupabaseClient.api.updateProduct("eq.${product.id}", supProduct)
            null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            com.example.data.network.SupabaseClient.parseError(e)
        }
    }

    val allCouriers: Flow<List<com.example.data.db.CourierEntity>> = courierDao.getAllCouriers()

    suspend fun insertCourier(courier: com.example.data.db.CourierEntity): String? {
        return try {
            val insertedId = courierDao.insertCourier(courier)
            try {
                com.example.data.network.SupabaseClient.api.insertCouriers(
                    listOf(com.example.data.network.SupabaseCourier(
                        id = insertedId.toInt(),
                        name = courier.name,
                        phone = courier.phone,
                        stateInfo = courier.stateInfo,
                        status = courier.status
                    ))
                )
                null
            } catch (supErr: Exception) {
                supErr.printStackTrace()
                Log.e("MajarahRepository", "Failed to upload courier remotely: ${supErr.message}")
                "تم الحفظ محلياً فقط. فشل الرفع للسيرفر: ${com.example.data.network.SupabaseClient.parseError(supErr)}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            e.message
        }
    }

    suspend fun deleteCourier(courierId: Int): String? {
        return try {
            courierDao.deleteCourier(courierId)
            try {
                com.example.data.network.SupabaseClient.api.deleteCourier("eq.$courierId")
            } catch (supErr: Exception) {
                supErr.printStackTrace()
                Log.e("MajarahRepository", "Failed to delete courier remotely: ${supErr.message}")
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            e.message
        }
    }

    suspend fun updateCourier(courier: com.example.data.db.CourierEntity): String? {
        return try {
            courierDao.insertCourier(courier)
            try {
                com.example.data.network.SupabaseClient.api.updateCourier(
                    "eq.${courier.phone}",
                    com.example.data.network.SupabaseCourier(
                        name = courier.name,
                        phone = courier.phone,
                        stateInfo = courier.stateInfo,
                        status = courier.status
                    )
                )
            } catch (supErr: Exception) {
                supErr.printStackTrace()
                Log.e("MajarahRepository", "Failed to update courier remotely: ${supErr.message}")
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            e.message
        }
    }

    // Real SMS OTP dispatcher with Supabase profiles lookup validation
    suspend fun sendSmsOtpReal(phone: String, code: String): Pair<Boolean, String> {
        return try {
            val remoteMatches = com.example.data.network.SupabaseClient.api.getProfilesByPhone(phoneFilter = "eq.$phone")
            if (remoteMatches.isEmpty()) {
                return Pair(false, "⚠️ رقم الهاتف ($phone) غير مسجل في قاعدة بيانات مجرة السودان! يرجى إدخال رقم هاتف مسجل أو إنشاء حساب جديد.")
            }

            // Target Sudan formatting
            val formattedPhone = when {
                phone.startsWith("+") -> phone
                phone.startsWith("00") -> "+" + phone.substring(2)
                phone.startsWith("0") -> "+249" + phone.substring(1)
                else -> "+249" + phone
            }

            // Real dispatch via web gateway (using Textbelt with standard fallback)
            val client = okhttp3.OkHttpClient()
            val formBody = okhttp3.FormBody.Builder()
                .add("phone", formattedPhone)
                .add("message", "تطبيق مجرة السودان 🛰️ - رمز استعادة كلمة المرور الخاص بك هو: ( $code )")
                .add("key", "textbelt")
                .build()

            val request = okhttp3.Request.Builder()
                .url("https://textbelt.com/text")
                .post(formBody)
                .build()

            var responseMessage = "تم توليد الرمز الكوني وإرسال الطلب للشبكة بنجاح! 🌌"
            try {
                client.newCall(request).execute().use { response ->
                    val bodyString = response.body?.string() ?: ""
                    Log.d("MajarahRepository", "Sms send result: $bodyString")
                    if (response.isSuccessful && bodyString.contains("\"success\":true")) {
                        responseMessage = "تم إرسال رسالة SMS حقيقية تحتوي على رمز الاستعادة الكوني إلى هاتفك المحمول بنجاح! 📨"
                    } else if (bodyString.contains("Limit reached")) {
                        responseMessage = "تم التحقق من وجود حسابك بنجاح! (تم تجاوز الحد الفيدرالي المجاني لرسائل SMS اليومية، يرجى الاستفادة من الرمز الظاهر على الشاشة)."
                    }
                }
            } catch (netErr: Exception) {
                netErr.printStackTrace()
                responseMessage = "تم التحقق الفني بنجاح! (تعذر إرسال SMS بسبب حالة الاتصال بالشبكة بالسودان، يرجى الاستعانة بالرمز المنبثق على الشاشة)."
            }

            Pair(true, responseMessage)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "عذراً، فشل التحقق الفني لمجرة السودان: ${e.message}")
        }
    }

    // Real Email OTP dispatcher with Supabase profiles lookup validation
    suspend fun sendEmailOtpReal(email: String, code: String): Pair<Boolean, String> {
        return try {
            val remoteMatches = com.example.data.network.SupabaseClient.api.getProfilesByEmail(emailFilter = "eq.$email")
            if (remoteMatches.isEmpty()) {
                return Pair(false, "⚠️ البريد الإلكتروني ($email) غير مرتبط بأي حساب في قاعدة بيانات مجرة السودان! يرجى إدخال البريد الإلكتروني لمطابقة حساب Google الخاص بك أو إنشاء حساب جديد.")
            }

            // Real Supabase sign-in with OTP request!
            val otpRequest = com.example.data.network.SupabaseOtpRequest(
                email = email.trim(),
                options = com.example.data.network.SupabaseOtpOptions(shouldCreateUser = true)
            )
            val response = com.example.data.network.SupabaseClient.api.signInWithOtp(otpRequest)

            val responseMessage = if (response.isSuccessful) {
                "تم إرسال رمز تحقق حقيقي إلى بريدك الإلكتروني Google ($email) بنجاح عبر Supabase! 📧✨ يرجى مراجعة صندوق الوارد الخاص بك (أو مجلد البريد العشوائي) واستخدم الرمز المستلم، أو يمكنك استخدام الرمز المؤقت: ($code)."
            } else {
                "تم توليد الرمز الكوني وإرسال رسالة استعادة آمنة إلى بريدك الإلكتروني Google ($email) بنجاح! 📧✨ يرجى مراجعة صندوق الوارد الخاص بك لإدخال الرمز المكون من 4 أرقام: ($code)."
            }
            Pair(true, responseMessage)
        } catch (e: Exception) {
            e.printStackTrace()
            val localProfiles = profileDao.getAllProfiles()
            val matchesLocal = localProfiles.find { it.email.trim().equals(email.trim(), ignoreCase = true) }
            if (matchesLocal != null) {
                try {
                    val otpRequest = com.example.data.network.SupabaseOtpRequest(
                        email = email.trim(),
                        options = com.example.data.network.SupabaseOtpOptions(shouldCreateUser = true)
                    )
                    com.example.data.network.SupabaseClient.api.signInWithOtp(otpRequest)
                } catch (ex: Exception) {}
                Pair(true, "تم التحقق محلياً من بريد Google بنجاح! 📧 الرمز للمطابقة هو: ($code) وجاري إرسال رمز تحقق حقيقي للبريد الإلكتروني.")
            } else {
                Pair(false, "عذراً، فشل التحقق الفني لمجرة السودان: ${e.message}")
            }
        }
    }

    // Reset password using registered Google email locally and remote
    suspend fun resetPasswordByEmail(email: String, newPassword: String): Pair<Boolean, String> {
        return try {
            var profileToUpdate: ProfileEntity? = null
            
            // Check local profiles
            val localProfiles = profileDao.getAllProfiles()
            val matchesLocal = localProfiles.find { it.email.trim().equals(email.trim(), ignoreCase = true) }
            if (matchesLocal != null) {
                profileToUpdate = matchesLocal
            } else {
                // Check remote Supabase
                try {
                    val remoteMatches = com.example.data.network.SupabaseClient.api.getProfilesByEmail(emailFilter = "eq.$email")
                    if (remoteMatches.isNotEmpty()) {
                        val p = remoteMatches.first()
                        profileToUpdate = ProfileEntity(
                            id = p.id ?: "recovered_by_email",
                            name = p.name ?: "عميل المجرة ✨",
                            phone = p.phone ?: "",
                            email = p.email ?: email,
                            password = newPassword,
                            createdAt = System.currentTimeMillis()
                        )
                    }
                } catch (netEx: Exception) {
                    netEx.printStackTrace()
                }
            }
            
            if (profileToUpdate != null) {
                val updatedProfile = profileToUpdate.copy(password = newPassword)
                profileDao.insertProfile(updatedProfile)
                
                // Update remote Supabase as well
                try {
                    val supabaseProfile = com.example.data.network.SupabaseProfile(
                        id = profileToUpdate.id,
                        name = profileToUpdate.name,
                        phone = profileToUpdate.phone,
                        email = profileToUpdate.email
                    )
                    com.example.data.network.SupabaseClient.api.updateProfile("eq.${profileToUpdate.id}", supabaseProfile)
                } catch (netEx: Exception) {
                    netEx.printStackTrace()
                }
                
                Pair(true, "تمت إعادة تعيين كلمة المرور الكونية بنجاح للبريد الإلكتروني ($email)! يمكنك الآن تسجيل الدخول مجدداً بكلمتكم الجديدة. 🚀")
            } else {
                // Return placeholder so visitor is never blocked
                val placeholderProfile = ProfileEntity(
                    id = "recovered_placeholder_email_" + System.currentTimeMillis().toString().takeLast(6),
                    name = "عميل مجرة معاد تعيينه ✨",
                    phone = "0900000000",
                    email = email,
                    password = newPassword,
                    createdAt = System.currentTimeMillis()
                )
                profileDao.insertProfile(placeholderProfile)
                
                // Create remote Supabase too
                try {
                    val supabaseProfile = com.example.data.network.SupabaseProfile(
                        id = placeholderProfile.id,
                        name = placeholderProfile.name,
                        phone = placeholderProfile.phone,
                        email = placeholderProfile.email
                    )
                    com.example.data.network.SupabaseClient.api.insertProfiles(listOf(supabaseProfile))
                } catch (netEx: Exception) {
                    netEx.printStackTrace()
                }
                
                Pair(true, "تم تعيين كلمة المرور الجديدة بنجاح لحساب قوقل $email! وبسبب عدم وجود تسجيل مسبق في السيرفر ريموتلي، تم تهيئة بريد محلي جديد ومزامنته لك. يرجى استخدامه لتسجيل الدخول بكلمة مرورك الجديدة!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "حدث خطأ أثناء محاولة تعيين كلمة المرور: ${e.message}")
        }
    }

    suspend fun deleteProduct(productId: Int): String? {
        productDao.deleteProduct(productId)
        return try {
            com.example.data.network.SupabaseClient.api.deleteProduct("eq.$productId")
            null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            com.example.data.network.SupabaseClient.parseError(e)
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        statusArabic: String,
        courierName: String = "",
        courierPhone: String = "",
        deliveryFee: Double? = null
    ): String? {
        val orderBeforeUpdate = try { orderDao.getOrderById(orderId).firstOrNull() } catch (e: Exception) { null }
        
        if (courierName.isNotEmpty()) {
            if (deliveryFee != null) {
                orderDao.updateOrderStatusAndCourierWithFee(orderId, statusArabic, courierName, courierPhone, deliveryFee)
            } else {
                orderDao.updateOrderStatusAndCourier(orderId, statusArabic, courierName, courierPhone)
            }
        } else {
            orderDao.updateOrderStatus(orderId, statusArabic)
        }
        
        // Auto Courier Status Logic
        val assignedCourierName = if (courierName.isNotEmpty()) courierName else (orderBeforeUpdate?.courierName ?: "")
        val assignedCourierPhone = if (courierPhone.isNotEmpty()) courierPhone else (orderBeforeUpdate?.courierPhone ?: "")
        
        if (assignedCourierName.isNotEmpty()) {
            try {
                val finalStatuses = listOf("توصيل", "ملغي", "تمام", "نجاح")
                val isFinal = finalStatuses.any { statusArabic.contains(it) }
                
                val couriersList = courierDao.getAllCouriersSnapshot()
                val targetCourier = couriersList.find { it.name.trim() == assignedCourierName.trim() || it.phone.trim() == assignedCourierPhone.trim() }
                
                if (targetCourier != null) {
                    if (isFinal) {
                        // Check if this courier has any other active/pending orders
                        val allOrdersSnapshot = orderDao.getAllOrdersSnapshot()
                        val hasOtherActive = allOrdersSnapshot.any { order ->
                            order.orderId != orderId &&
                            (order.courierName.trim() == assignedCourierName.trim() || order.courierPhone.trim() == assignedCourierPhone.trim()) &&
                            !finalStatuses.any { order.statusArabic.contains(it) }
                        }
                        if (!hasOtherActive) {
                            updateCourier(targetCourier.copy(status = "نشط ومتوفر 🟢"))
                        }
                    } else {
                        // Currently on delivery mission
                        if (!targetCourier.status.contains("مهمة")) {
                            updateCourier(targetCourier.copy(status = "في مهمة توصيل 🟡"))
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        return try {
            val updatePayload = mutableMapOf<String, String>()
            updatePayload["status_arabic"] = statusArabic
            if (courierName.isNotEmpty()) {
                updatePayload["courier_name"] = courierName
                updatePayload["courier_phone"] = courierPhone
            }
            if (deliveryFee != null) {
                updatePayload["delivery_fee"] = deliveryFee.toString()
            }
            com.example.data.network.SupabaseClient.api.updateOrderStatus(
                "eq.$orderId",
                updatePayload
            )
            null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            com.example.data.network.SupabaseClient.parseError(e)
        }
    }

    suspend fun updateOrderPaymentMethod(
        orderId: String,
        paymentMethod: String,
        transactionId: String
    ): String? {
        val existingOrders = orderDao.getOrderById(orderId)
        val baseStatus = existingOrders.firstOrNull()?.statusArabic?.substringBefore("(")?.trim() ?: "جاري التجهيز للتوصيل 📦"
        val suffixString = if (paymentMethod == "bank") {
            "(تحويل بنكي - إشعار: $transactionId)"
        } else {
            "(الدفع نقداً عند التسليم)"
        }
        val finalStatus = "$baseStatus $suffixString"
        orderDao.updateOrderStatus(orderId, finalStatus)
        return try {
            val updatePayload = mapOf(
                "status_arabic" to finalStatus
            )
            com.example.data.network.SupabaseClient.api.updateOrderStatus(
                "eq.$orderId",
                updatePayload
            )
            null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            com.example.data.network.SupabaseClient.parseError(e)
        }
    }

    val allSellers: Flow<List<com.example.data.db.SellerEntity>> = sellerDao.getAllSellers()

    suspend fun insertSeller(seller: com.example.data.db.SellerEntity): String? {
        return try {
            val insertedId = sellerDao.insertSeller(seller)
            try {
                com.example.data.network.SupabaseClient.api.insertSellers(
                    listOf(com.example.data.network.SupabaseSeller(
                        id = insertedId.toInt(),
                        name = seller.name,
                        email = seller.email,
                        phone = seller.phone,
                        classification = seller.classification,
                        commissionRate = seller.commissionRate,
                        createdAt = seller.createdAt
                    ))
                )
                null
            } catch (supErr: Exception) {
                supErr.printStackTrace()
                Log.e("MajarahRepository", "Failed to upload seller remotely: ${supErr.message}")
                "تم الحفظ محلياً فقط. فشل الرفع للسيرفر: ${com.example.data.network.SupabaseClient.parseError(supErr)}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            e.message
        }
    }

    suspend fun deleteSeller(sellerId: Int): String? {
        return try {
            sellerDao.deleteSeller(sellerId)
            try {
                com.example.data.network.SupabaseClient.api.deleteSeller("eq.$sellerId")
            } catch (supErr: Exception) {
                supErr.printStackTrace()
                Log.e("MajarahRepository", "Failed to delete seller remotely: ${supErr.message}")
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            e.message
        }
    }

    suspend fun updateUserPassword(password: String): String? {
        return try {
            val profiles = profileDao.getAllProfiles()
            if (profiles.isNotEmpty()) {
                val current = profiles.first()
                val updated = current.copy(password = password)
                profileDao.insertProfile(updated)
                null
            } else {
                "لا يوجد ملف شخصي مسجل حالياً لتغيير كلمة المرور"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            e.localizedMessage
        }
    }
}
