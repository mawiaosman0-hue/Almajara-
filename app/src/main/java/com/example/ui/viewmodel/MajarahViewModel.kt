package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.OrderEntity
import com.example.data.db.ProductEntity
import com.example.data.repository.CartItemWithProduct
import com.example.data.repository.MajarahRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Home : Screen()
    object Categories : Screen()
    object Cart : Screen()
    object Favorites : Screen()
    object History : Screen()
    object Profile : Screen()
    object Admin : Screen()
    object Courier : Screen()
    object Seller : Screen()
    object Pharmacist : Screen()
    object Restaurant : Screen()
    data class ProductDetail(val productId: Int) : Screen()
}

class MajarahViewModel(application: Application) : AndroidViewModel(application) {
    val database = AppDatabase.getDatabase(application)
    private val repository = MajarahRepository(
        database.productDao(),
        database.cartDao(),
        database.orderDao(),
        database.profileDao(),
        database.courierDao(),
        database.sellerDao(),
        database.pharmacyDao(),
        database.pharmacyProductDao(),
        database.pharmacyOrderDao(),
        database.adminManagerDao(),
        database.restaurantDao(),
        database.restaurantOrderDao()
    )

    private val connectivityManager = application.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager

    private val _isInternetAvailable = MutableStateFlow(true)
    val isInternetAvailable: StateFlow<Boolean> = _isInternetAvailable.asStateFlow()

    // Current Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Dynamic Language Preference (false = Arabic, true = English)
    val isEnglish = MutableStateFlow(false)

    fun t(ar: String, en: String): String {
        return if (isEnglish.value) en else ar
    }

    // Auth state flows
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val activeProfile = MutableStateFlow<com.example.data.db.ProfileEntity?>(null)

    val allAdminManagers: StateFlow<List<com.example.data.db.AdminManagerEntity>> = repository.adminManagerDao.getAllAdminManagers().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val isGeneralAdmin: StateFlow<Boolean> = combine(activeProfile, _isLoggedIn) { profile, loggedIn ->
        loggedIn && profile != null && (
            profile.email.trim().lowercase() == "mawiaosman0@gmail.com" || 
            profile.phone.trim() == "0910074223" || 
            profile.phone.trim().replace(" ", "").replace("+", "").endsWith("910074223") ||
            profile.name.trim().replace("أ", "ا") == "معاوية عثمان احمد ياسين" ||
            profile.name.trim() == "معاوية عثمان أحمد ياسين"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isAdmin: StateFlow<Boolean> = combine(isGeneralAdmin, activeProfile, _isLoggedIn, allAdminManagers) { isGen, profile, loggedIn, managers ->
        loggedIn && profile != null && (
            profile.role == "admin" || isGen || managers.any { m -> 
                m.email.trim().lowercase() == profile.email.trim().lowercase() || 
                m.phone.trim() == profile.phone.trim() 
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isAdministrativeManager: StateFlow<Boolean> = combine(isGeneralAdmin, isAdmin) { isGen, isAdm ->
        isAdm && !isGen
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val allSellers = repository.allSellers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val isSeller: StateFlow<Boolean> = combine(activeProfile, _isLoggedIn, repository.allSellers) { profile, loggedIn, sellers ->
        if (!loggedIn || profile == null) {
            false
        } else {
            val emailClean = profile.email.trim().lowercase()
            val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
            val storedRole = sharedPrefs.getString("user_role_${emailClean}", "") ?: ""
            profile.role == "seller" || storedRole == "seller" || sellers.any { s -> s.email.trim().lowercase() == emailClean }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isCourier: StateFlow<Boolean> = combine(activeProfile, _isLoggedIn, repository.allCouriers) { profile, loggedIn, couriers ->
        if (!loggedIn || profile == null) {
            false
        } else {
            val emailClean = profile.email.trim().lowercase()
            val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
            val storedRole = sharedPrefs.getString("user_role_${emailClean}", "") ?: ""
            val phoneClean = profile.phone.trim().replace("+", "").replace(" ", "")
            profile.role == "courier" || storedRole == "courier" || couriers.any { c ->
                val cPhoneClean = c.phone.trim().replace("+", "").replace(" ", "")
                cPhoneClean == phoneClean || c.phone.trim() == profile.phone.trim()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val loginEmail = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    val loginName = MutableStateFlow("")
    val loginPhone = MutableStateFlow("")
    val isRegisterMode = MutableStateFlow(false)
    val isLoginLoading = MutableStateFlow(false)
    val isGlobalLoading = MutableStateFlow(false)
    val registrationRole = MutableStateFlow("customer") // "customer", "seller", "courier"
    val showOtpVerification = MutableStateFlow(false)
    val otpVerificationEmail = MutableStateFlow("")
    val otpCode = MutableStateFlow("")

    // App Update properties (15-day grace period check)
    val latestVersionCode = MutableStateFlow(1)
    val latestVersionName = MutableStateFlow("1.0.0")
    val releaseDateMs = MutableStateFlow(System.currentTimeMillis())
    val showUpdateDialog = MutableStateFlow(false)
    val isUpdateMandatory = MutableStateFlow(false)
    val daysRemaining = MutableStateFlow(15L)
    val isGooglePlayUpdateAvailable = MutableStateFlow(false)
    var playAppUpdateInfo: com.google.android.play.core.appupdate.AppUpdateInfo? = null

    // Current Search Query State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Current category filtering (empty for all)
    private val _selectedCategory = MutableStateFlow<String>("")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Sorting option state: "default", "price_asc", "price_desc", "newest"
    private val _sortBy = MutableStateFlow("default")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    fun updateSortBy(sort: String) {
        _sortBy.value = sort
    }

    // Base Products list
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products for the Home/Catalog screen based on Category, Search Query, and Sorting
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        repository.allProducts,
        _searchQuery,
        _selectedCategory,
        _sortBy
    ) { products, query, category, sort ->
        var list = products.filter { it.isApproved }
        if (category.isNotEmpty()) {
            list = list.filter { it.category == category }
        }
        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true)
            }
        }
        when (sort) {
            "price_asc" -> list = list.sortedBy { it.price }
            "price_desc" -> list = list.sortedByDescending { it.price }
            "newest" -> list = list.sortedByDescending { it.id }
            else -> list = list.sortedByDescending { it.rating }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Items with combined products
    val cartItems: StateFlow<List<CartItemWithProduct>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites products
    val favoriteProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .combine(MutableStateFlow(Unit)) { products, _ ->
            products.filter { it.isFavorite }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Past Orders History - Filters so each standard customer only sees their own orders
    val orderHistory: StateFlow<List<OrderEntity>> = combine(
        repository.orderHistory,
        activeProfile,
        isAdmin,
        isCourier,
        isSeller
    ) { orders, profile, admin, courier, seller ->
        if (admin || courier || seller) {
            orders
        } else if (profile != null) {
            val profilePhone = profile.phone.trim().replace("+", "").replace(" ", "")
            val profileName = profile.name.trim().lowercase()
            orders.filter { order ->
                val orderPhone = order.customerPhone.trim().replace("+", "").replace(" ", "")
                val orderName = order.customerName.trim().lowercase()
                orderPhone == profilePhone || 
                (orderPhone.isNotEmpty() && profilePhone.contains(orderPhone)) ||
                (profilePhone.isNotEmpty() && orderPhone.contains(profilePhone)) ||
                orderName == profileName
            }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // database connection live status from repository
    val dbStatus: StateFlow<String> = repository.dbStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "جاري الاتصال بـ Supabase...")

    // Selected Product for Details Screen
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    // Checkout Form state
    val checkoutPhone = MutableStateFlow("")
    val checkoutAddress = MutableStateFlow("")
    val checkoutName = MutableStateFlow("")

    // Promo code / Coupon State. Supports COSMIC10 (10%), MAJARAH15 (15%), SUDAN50 (50% test)
    private val _appliedCoupon = MutableStateFlow<String?>(null)
    val appliedCoupon: StateFlow<String?> = _appliedCoupon.asStateFlow()
    
    private val _couponError = MutableStateFlow<String?>(null)
    val couponError: StateFlow<String?> = _couponError.asStateFlow()

    fun applyCoupon(code: String): Boolean {
        val uppercaseCode = code.trim().uppercase()
        val staticCodes = listOf("COSMIC10", "MAJARAH15", "SUDAN50")
        
        if (uppercaseCode in staticCodes) {
            _appliedCoupon.value = uppercaseCode
            _couponError.value = null
            return true
        }
        
        // Check dynamic coupons from database
        val dbCoupon = allCouponsFlow.value.find { it.code.trim().uppercase() == uppercaseCode }
        if (dbCoupon != null) {
            if (dbCoupon.isUsed) {
                _couponError.value = "هذا الكوبون تم استخدامه مسبقاً ❌"
                return false
            }
            _appliedCoupon.value = uppercaseCode
            _couponError.value = null
            return true
        } else {
            _couponError.value = "كود الخصم غير صحيح أو منتهي الصلاحية ❌"
            return false
        }
    }
    
    fun removeCoupon() {
        _appliedCoupon.value = null
        _couponError.value = null
    }

    fun getCouponDiscountPercentage(coupon: String?): Int {
        if (coupon == null) return 0
        val uppercaseCoupon = coupon.trim().uppercase()
        return when (uppercaseCoupon) {
            "COSMIC10" -> 10
            "MAJARAH15" -> 15
            "SUDAN50" -> 50
            else -> {
                val dbCoupon = allCouponsFlow.value.find { it.code.trim().uppercase() == uppercaseCoupon }
                if (dbCoupon != null) {
                    if (dbCoupon.isBogo) {
                        50 // 50% discount for Buy 1 Get 1 equivalent discount
                    } else if (dbCoupon.isFreeDelivery) {
                        0 // free delivery gets 0% product discount but free delivery charge
                    } else {
                        dbCoupon.discountPercent.toInt()
                    }
                } else 0
            }
        }
    }

    fun calculateDiscountedSum(items: List<CartItemWithProduct>, coupon: String?): Double {
        val total = calculateTotalSum(items)
        var discount = getCouponDiscountPercentage(coupon).toDouble()
        
        // Apply customer classification-based discount
        val classification = userClassification.value
        if (classification.contains("عميل مميز")) {
            discount += 5.0
        } else if (classification.contains("عميل ذهبي")) {
            discount += 15.0
        } else if (classification.contains("مندوب ذهبي")) {
            discount += 10.0
        }
        
        // Ensure discount doesn't exceed 100%
        if (discount > 100.0) discount = 100.0
        
        return total * (1.0 - discount / 100.0)
    }

    // Success dialog state after order placement
    private val _checkoutSuccessMessage = MutableStateFlow<String?>(null)
    val checkoutSuccessMessage: StateFlow<String?> = _checkoutSuccessMessage.asStateFlow()

    fun loginGoogleVerifiedAccountDirect(email: String, onComplete: (String?, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val remoteProfs = com.example.data.network.SupabaseClient.api.getProfilesByEmail(emailFilter = "eq.${email.trim()}")
                if (remoteProfs.isNotEmpty()) {
                    val p = remoteProfs.first()
                    val profileEntity = com.example.data.db.ProfileEntity(
                        id = p.id ?: java.util.UUID.randomUUID().toString(),
                        name = p.name ?: "مستخدم جوجل",
                        phone = p.phone ?: "",
                        email = p.email ?: email,
                        password = "google_authenticated_bypass_1782"
                    )
                    database.profileDao().insertProfile(profileEntity)
                    
                    val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
                    sharedPrefs.edit().putBoolean("is_logged_in_state", true).apply()
                    sharedPrefs.edit().putString("logged_in_email", profileEntity.email.trim().lowercase()).apply()
                    
                    activeProfile.value = profileEntity
                    _isLoggedIn.value = true
                    checkoutName.value = profileEntity.name
                    checkoutPhone.value = profileEntity.phone

                    // Resolve roles or current screen
                    val cleanP = profileEntity.phone.trim().replace("+", "").replace(" ", "")
                    val matchesCourier = database.courierDao().getAllCouriersSnapshot().any { c ->
                        c.phone.trim().replace("+", "").replace(" ", "") == cleanP || c.phone.trim() == profileEntity.phone.trim()
                    }
                    val matchesSeller = database.sellerDao().getAllSellersSnapshot().any { s ->
                        s.email.trim().lowercase() == profileEntity.email.trim().lowercase()
                    }
                    val matchesPharmacist = database.pharmacyDao().getAllPharmaciesSnapshot().any { pharm ->
                        (pharm.pharmacistEmail.isNotBlank() && pharm.pharmacistEmail.trim().lowercase() == profileEntity.email.trim().lowercase()) ||
                        (pharm.phone.isNotBlank() && isPhoneMatchHelper(pharm.phone, profileEntity.phone)) ||
                        (pharm.doctorName.isNotBlank() && pharm.doctorName.trim().lowercase() == profileEntity.name.trim().lowercase())
                    }
                    val matchesRestaurant = database.restaurantDao().getAllRestaurantsSnapshot().any { rest ->
                        (rest.phone.isNotBlank() && isPhoneMatchHelper(rest.phone, profileEntity.phone)) ||
                        (rest.name.isNotBlank() && rest.name.trim().lowercase() == profileEntity.name.trim().lowercase())
                    }
                    val matchesAdminManager = database.adminManagerDao().getAllAdminManagersSnapshot().any { m ->
                        (m.email.isNotBlank() && m.email.trim().lowercase() == profileEntity.email.trim().lowercase()) ||
                        (m.phone.isNotBlank() && isPhoneMatchHelper(m.phone, profileEntity.phone))
                    }

                    val role = sharedPrefs.getString("user_role_${profileEntity.email.trim().lowercase()}", "")
                    val isPharmacistUser = role == "pharmacist" || profileEntity.role == "pharmacist" || matchesPharmacist
                    val isRestaurantUser = role == "restaurant" || profileEntity.role == "restaurant" || matchesRestaurant
                    val isManagerUser = role == "admin" || profileEntity.role == "admin" || matchesAdminManager
                    val isAdminUser = (profileEntity.email.trim().lowercase() == "mawiaosman0@gmail.com") || isManagerUser

                    if (isAdminUser) {
                        _currentScreen.value = Screen.Admin
                    } else if (isPharmacistUser) {
                        _currentScreen.value = Screen.Pharmacist
                    } else if (isRestaurantUser) {
                        _selectedCategory.value = "restaurant"
                        _currentScreen.value = Screen.Restaurant
                    } else if (matchesCourier || role == "courier") {
                        _currentScreen.value = Screen.Courier
                    } else if (matchesSeller || role == "seller") {
                        _currentScreen.value = Screen.Seller
                    } else {
                        _selectedCategory.value = ""
                        _currentScreen.value = Screen.Home
                    }
                    
                    onComplete(null, true) // exists, logged in successfully!
                } else {
                    onComplete(null, false) // doesn't exist, need registration
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(e.message ?: "خطأ غير معروف", false)
            }
        }
    }

    fun performLogin(onSuccess: (String?) -> Unit) {
        val name = if (isRegisterMode.value) loginName.value.trim() else ""
        val phone = loginPhone.value.trim()
        val email = loginEmail.value.trim()
        val password = loginPassword.value.trim()
        val role = registrationRole.value

        isLoginLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                if (isRegisterMode.value) {
                    // Register
                    error = repository.registerUserProfile(name, phone, email, password, role)
                    
                    val isLocalSuccess = error == null || 
                                         error.contains("تم الحفظ محلياً") || 
                                         error.contains("profiles") || 
                                         error.contains("الرفع للسيرفر") ||
                                         error.contains("already registered", ignoreCase = true) ||
                                         error.contains("already exists", ignoreCase = true)

                    if (isLocalSuccess) {
                        // Create corresponding user type record if not customer
                        val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
                        sharedPrefs.edit().putString("user_role_${email.trim().lowercase()}", role).apply()
                        if (role == "seller") {
                            repository.insertSeller(
                                com.example.data.db.SellerEntity(
                                    name = name,
                                    email = email,
                                    phone = phone,
                                    classification = "تاجر المجرة",
                                    commissionRate = 0.05
                                )
                            )
                        } else if (role == "courier") {
                            repository.insertCourier(
                                com.example.data.db.CourierEntity(
                                    name = name,
                                    phone = phone,
                                    stateInfo = "ولاية بورتسودان",
                                    status = "نشط ومتوفر 🟢"
                                )
                            )
                        } else if (role == "pharmacist") {
                            sharedPrefs.edit().putString("user_role_${email.trim().lowercase()}", "pharmacist").apply()
                        } else if (role == "restaurant") {
                            sharedPrefs.edit().putString("user_role_${email.trim().lowercase()}", "restaurant").apply()
                        } else if (role == "admin") {
                            sharedPrefs.edit().putString("user_role_${email.trim().lowercase()}", "admin").apply()
                        }

                        // Show success Toast for local and cloud save
                        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                            android.widget.Toast.makeText(
                                getApplication(),
                                "نجاح الحفظ سحابياً ومحلياً ✅",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }

                        // Log in directly
                        val loginResult = repository.loginUserProfile(email, password)
                        val p = loginResult.first
                        if (p != null) {
                            sharedPrefs.edit().putBoolean("is_logged_in_state", true).apply()
                            sharedPrefs.edit().putString("logged_in_email", p.email.trim().lowercase()).apply()

                            activeProfile.value = p
                            _isLoggedIn.value = true
                            checkoutName.value = p.name
                            checkoutPhone.value = p.phone

                            val cleanP = p.phone.trim().replace("+", "").replace(" ", "")
                            val matchesCourier = database.courierDao().getAllCouriersSnapshot().any { c ->
                                c.phone.trim().replace("+", "").replace(" ", "") == cleanP || c.phone.trim() == p.phone.trim()
                            }
                            val matchesSeller = database.sellerDao().getAllSellersSnapshot().any { s ->
                                s.email.trim().lowercase() == p.email.trim().lowercase()
                            }
                            val matchesPharmacist = database.pharmacyDao().getAllPharmaciesSnapshot().any { pharm ->
                                (pharm.pharmacistEmail.isNotBlank() && pharm.pharmacistEmail.trim().lowercase() == p.email.trim().lowercase()) ||
                                (pharm.phone.isNotBlank() && isPhoneMatchHelper(pharm.phone, p.phone)) ||
                                (pharm.doctorName.isNotBlank() && pharm.doctorName.trim().lowercase() == p.name.trim().lowercase())
                            }
                            val matchesRestaurant = database.restaurantDao().getAllRestaurantsSnapshot().any { rest ->
                                (rest.phone.isNotBlank() && isPhoneMatchHelper(rest.phone, p.phone)) ||
                                (rest.name.isNotBlank() && rest.name.trim().lowercase() == p.name.trim().lowercase())
                            }
                            val matchesAdminManager = database.adminManagerDao().getAllAdminManagersSnapshot().any { m ->
                                (m.email.isNotBlank() && m.email.trim().lowercase() == p.email.trim().lowercase()) ||
                                (m.phone.isNotBlank() && isPhoneMatchHelper(m.phone, p.phone))
                            }

                            val isPharmacistUser = role == "pharmacist" || sharedPrefs.getString("user_role_${p.email.trim().lowercase()}", "") == "pharmacist" || p.role == "pharmacist" || matchesPharmacist
                            val isRestaurantUser = role == "restaurant" || sharedPrefs.getString("user_role_${p.email.trim().lowercase()}", "") == "restaurant" || p.role == "restaurant" || matchesRestaurant
                            val isManagerUser = role == "admin" || sharedPrefs.getString("user_role_${p.email.trim().lowercase()}", "") == "admin" || p.role == "admin" || matchesAdminManager
                            val isAdminUser = (p.email.trim().lowercase() == "mawiaosman0@gmail.com") || isManagerUser

                            if (isAdminUser) {
                                _currentScreen.value = Screen.Admin
                            } else if (isPharmacistUser) {
                                _currentScreen.value = Screen.Pharmacist
                            } else if (isRestaurantUser) {
                                _selectedCategory.value = "restaurant"
                                _currentScreen.value = Screen.Restaurant
                            } else if (role == "courier" || matchesCourier) {
                                _currentScreen.value = Screen.Courier
                            } else if (role == "seller" || matchesSeller) {
                                _currentScreen.value = Screen.Seller
                            } else {
                                _selectedCategory.value = ""
                                _currentScreen.value = Screen.Home
                            }

                            // Clear register state
                            isRegisterMode.value = false
                            showOtpVerification.value = false
                            error = null // Clear error to represent success
                        } else {
                            // Fallback to manual local user profile construction
                            val fallbackProfile = com.example.data.db.ProfileEntity(
                                id = java.util.UUID.randomUUID().toString(),
                                name = name,
                                phone = phone,
                                email = email,
                                password = password,
                                role = role,
                                createdAt = System.currentTimeMillis()
                            )
                            database.profileDao().insertProfile(fallbackProfile)

                            sharedPrefs.edit().putBoolean("is_logged_in_state", true).apply()
                            sharedPrefs.edit().putString("logged_in_email", fallbackProfile.email.trim().lowercase()).apply()

                            activeProfile.value = fallbackProfile
                            _isLoggedIn.value = true
                            checkoutName.value = fallbackProfile.name
                            checkoutPhone.value = fallbackProfile.phone

                            val isPharmacistUser = role == "pharmacist" || sharedPrefs.getString("user_role_${fallbackProfile.email.trim().lowercase()}", "") == "pharmacist"
                            val isRestaurantUser = role == "restaurant" || sharedPrefs.getString("user_role_${fallbackProfile.email.trim().lowercase()}", "") == "restaurant"
                            val isAdminUser = (fallbackProfile.email.trim().lowercase() == "mawiaosman0@gmail.com" && !isPharmacistUser && !isRestaurantUser) || role == "admin" || sharedPrefs.getString("user_role_${fallbackProfile.email.trim().lowercase()}", "") == "admin" || fallbackProfile.role == "admin"

                            if (isAdminUser) {
                                _currentScreen.value = Screen.Admin
                            } else if (role == "courier") {
                                _currentScreen.value = Screen.Courier
                            } else if (role == "seller") {
                                _currentScreen.value = Screen.Seller
                            } else if (isPharmacistUser) {
                                _currentScreen.value = Screen.Pharmacist
                            } else if (isRestaurantUser) {
                                _selectedCategory.value = "restaurant"
                                _currentScreen.value = Screen.Restaurant
                            } else {
                                _selectedCategory.value = ""
                                _currentScreen.value = Screen.Home
                            }

                            isRegisterMode.value = false
                            showOtpVerification.value = false
                            error = null
                        }
                    }
                } else {
                    // Sign In
                    val result = repository.loginUserProfile(email, password)
                    error = result.second
                    if (error == null) {
                        val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
                        sharedPrefs.edit().putBoolean("is_logged_in_state", true).apply()

                        val p = result.first
                        if (p != null) {
                            sharedPrefs.edit().putString("logged_in_email", p.email.trim().lowercase()).apply()
                            sharedPrefs.edit().putString("user_role_${p.email.trim().lowercase()}", p.role).apply()
                        }
                        activeProfile.value = p
                        _isLoggedIn.value = true
                        val pPhone = p?.phone ?: ""
                        checkoutName.value = p?.name ?: ""
                        checkoutPhone.value = pPhone

                        val cleanP = pPhone.trim().replace("+", "").replace(" ", "")
                        val matchesCourier = database.courierDao().getAllCouriersSnapshot().any { c ->
                            c.phone.trim().replace("+", "").replace(" ", "") == cleanP || c.phone.trim() == pPhone.trim()
                        }
                        val matchesSeller = database.sellerDao().getAllSellersSnapshot().any { s ->
                            s.email.trim().lowercase() == p?.email?.trim()?.lowercase() || (cleanP.isNotBlank() && s.phone.trim() == pPhone.trim())
                        }
                        val matchesPharmacist = database.pharmacyDao().getAllPharmaciesSnapshot().any { pharm ->
                            (pharm.pharmacistEmail.isNotBlank() && pharm.pharmacistEmail.trim().lowercase() == p?.email?.trim()?.lowercase()) ||
                            (pharm.phone.isNotBlank() && isPhoneMatchHelper(pharm.phone, pPhone)) ||
                            (pharm.doctorName.isNotBlank() && pharm.doctorName.trim().lowercase() == p?.name?.trim()?.lowercase())
                        }
                        val matchesRestaurant = database.restaurantDao().getAllRestaurantsSnapshot().any { rest ->
                            (rest.phone.isNotBlank() && isPhoneMatchHelper(rest.phone, pPhone)) ||
                            (rest.name.isNotBlank() && rest.name.trim().lowercase() == p?.name?.trim()?.lowercase())
                        }
                        val matchesAdminManager = database.adminManagerDao().getAllAdminManagersSnapshot().any { m ->
                            (m.email.isNotBlank() && m.email.trim().lowercase() == p?.email?.trim()?.lowercase()) ||
                            (m.phone.isNotBlank() && isPhoneMatchHelper(m.phone, pPhone))
                        }

                        val savedRole = if (p != null) sharedPrefs.getString("user_role_${p.email.trim().lowercase()}", "") else ""
                        val isPharmacistUser = savedRole == "pharmacist" || p?.role == "pharmacist" || matchesPharmacist
                        val isRestaurantUser = savedRole == "restaurant" || p?.role == "restaurant" || matchesRestaurant
                        val isManagerUser = savedRole == "admin" || p?.role == "admin" || matchesAdminManager
                        val isAdminUser = (p?.email?.trim()?.lowercase() == "mawiaosman0@gmail.com") || isManagerUser

                        if (isPharmacistUser && p != null) {
                            sharedPrefs.edit().putString("user_role_${p.email.trim().lowercase()}", "pharmacist").apply()
                        } else if (isRestaurantUser && p != null) {
                            sharedPrefs.edit().putString("user_role_${p.email.trim().lowercase()}", "restaurant").apply()
                        } else if (isManagerUser && p != null) {
                            sharedPrefs.edit().putString("user_role_${p.email.trim().lowercase()}", "admin").apply()
                        }

                        if (isAdminUser) {
                            _currentScreen.value = Screen.Admin
                        } else if (isPharmacistUser) {
                            _currentScreen.value = Screen.Pharmacist
                        } else if (isRestaurantUser) {
                            _selectedCategory.value = "restaurant"
                            _currentScreen.value = Screen.Restaurant
                        } else if (matchesCourier || savedRole == "courier" || p?.role == "courier") {
                            _currentScreen.value = Screen.Courier
                        } else if (matchesSeller || savedRole == "seller" || p?.role == "seller") {
                            _currentScreen.value = Screen.Seller
                        } else {
                            _selectedCategory.value = ""
                            _currentScreen.value = Screen.Home
                        }
                    } else if (error != null && (error.contains("Email not confirmed", ignoreCase = true) || error.contains("تأكيد", ignoreCase = true))) {
                        otpVerificationEmail.value = email
                        showOtpVerification.value = true
                    }
                }
            } catch (e: Exception) {
                error = e.localizedMessage ?: e.message
            } finally {
                isLoginLoading.value = false
                onSuccess(error)
            }
        }
    }

    fun verifyEmailAndFinishLogin(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val email = otpVerificationEmail.value.trim()
        val code = otpCode.value.trim()
        val name = loginName.value.trim()
        val phone = loginPhone.value.trim()
        val password = loginPassword.value.trim()

        viewModelScope.launch {
            val error = repository.verifyEmailOTP(email, code)
            if (error == null) {
                // Verification successful! Activate profile and login
                val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
                sharedPrefs.edit().putBoolean("is_logged_in_state", true).apply()

                val dbProfiles = database.profileDao().getAllProfiles()
                val p = dbProfiles.firstOrNull { it.email.trim().lowercase() == email.lowercase() }
                if (p == null) {
                    val fallbackName = if (name.isBlank()) "عميل المجرة ✨" else name
                    val newProfile = com.example.data.db.ProfileEntity(
                        id = java.util.UUID.randomUUID().toString(),
                        name = fallbackName,
                        phone = phone,
                        email = email,
                        password = password,
                        createdAt = System.currentTimeMillis()
                    )
                    database.profileDao().insertProfile(newProfile)
                    activeProfile.value = newProfile
                } else {
                    activeProfile.value = p
                }
                val finalProfile = activeProfile.value!!
                _isLoggedIn.value = true
                checkoutName.value = finalProfile.name
                checkoutPhone.value = finalProfile.phone

                showOtpVerification.value = false
                val role = registrationRole.value
                sharedPrefs.edit().putString("user_role_${email.trim().lowercase()}", role).apply()
                
                if (role == "seller") {
                    repository.insertSeller(
                        com.example.data.db.SellerEntity(
                            name = finalProfile.name,
                            email = finalProfile.email,
                            phone = finalProfile.phone,
                            classification = "تاجر المجرة ⭐",
                            commissionRate = 0.05
                        )
                    )
                } else if (role == "courier") {
                    repository.insertCourier(
                        com.example.data.db.CourierEntity(
                            name = finalProfile.name,
                            phone = finalProfile.phone,
                            stateInfo = "ولاية بورتسودان",
                            status = "نشط ومتوفر 🟢"
                        )
                    )
                }

                val cleanP = finalProfile.phone.trim().replace("+", "").replace(" ", "")
                val matchesCourier = repository.allCouriers.stateIn(viewModelScope).value.any { c ->
                    c.phone.trim().replace("+", "").replace(" ", "") == cleanP || c.phone.trim() == finalProfile.phone.trim()
                }
                if (email.trim().lowercase() == "mawiaosman0@gmail.com") {
                    _currentScreen.value = Screen.Admin
                } else if (matchesCourier || role == "courier") {
                    _currentScreen.value = Screen.Courier
                } else if (role == "seller") {
                    _currentScreen.value = Screen.Seller
                } else if (role == "pharmacist") {
                    _selectedCategory.value = "pharmacy"
                    _currentScreen.value = Screen.Pharmacist
                } else if (role == "restaurant") {
                    _selectedCategory.value = "restaurant"
                    _currentScreen.value = Screen.Restaurant
                } else {
                    _currentScreen.value = Screen.Home
                }
                onSuccess()
            } else {
                onError(error)
            }
        }
    }

    fun enterAsGuest() {
        _isLoggedIn.value = false
        activeProfile.value = null
        checkoutName.value = ""
        checkoutPhone.value = ""
        val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("is_logged_in_state", false).apply()
        _currentScreen.value = Screen.Home
    }

    fun resetPasswordByPhone(phone: String, newPassword: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.resetPasswordByPhone(phone, newPassword)
            if (result.first) {
                // Pre-fill fields with recovered info so they can login directly
                loginPhone.value = phone
                loginPassword.value = newPassword
            }
            onComplete(result.first, result.second)
        }
    }

    fun resetPasswordByEmail(email: String, newPassword: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.resetPasswordByEmail(email, newPassword)
            if (result.first) {
                loginEmail.value = email
                loginPassword.value = newPassword
            }
            onComplete(result.first, result.second)
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
            sharedPrefs.edit().putBoolean("is_logged_in_state", false).apply()
            sharedPrefs.edit().putString("logged_in_email", "").apply()

            activeProfile.value = null
            _isLoggedIn.value = false
            loginEmail.value = ""
            loginPassword.value = ""
            loginName.value = ""
            loginPhone.value = ""
            _currentScreen.value = Screen.Login
        }
    }

    fun updateProfile(name: String, phone: String, email: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val oldEmail = activeProfile.value?.email ?: ""
            val activeId = activeProfile.value?.id ?: ""
            val error = repository.updateUserProfile(name, phone, email, activeId, oldEmail)
            if (error == null) {
                val profiles = database.profileDao().getAllProfiles()
                val found = if (profiles.isNotEmpty()) {
                    if (activeId.isNotEmpty()) {
                        profiles.find { it.id == activeId }
                    } else {
                        profiles.find { it.email.trim().lowercase() == email.trim().lowercase() }
                    }
                } else null
                
                if (found != null) {
                    activeProfile.value = found
                } else {
                    activeProfile.value = activeProfile.value?.copy(name = name, phone = phone, email = email)
                }
                
                val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
                val oldClean = oldEmail.trim().lowercase()
                val newClean = email.trim().lowercase()
                if (oldClean.isNotEmpty() && newClean.isNotEmpty() && oldClean != newClean) {
                    val oldRole = sharedPrefs.getString("user_role_$oldClean", "") ?: ""
                    if (oldRole.isNotEmpty()) {
                        sharedPrefs.edit()
                            .putString("user_role_$newClean", oldRole)
                            .remove("user_role_$oldClean")
                            .apply()
                    }
                }
                sharedPrefs.edit()
                    .putString("logged_in_email", email)
                    .apply()

                checkoutName.value = name
                checkoutPhone.value = phone
            }
            onResult(error)
        }
    }

    fun updatePassword(password: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val activeId = activeProfile.value?.id ?: ""
            val error = repository.updateUserPassword(password, activeId, activeProfile.value?.email)
            if (error == null) {
                val emailClean = activeProfile.value?.email?.trim()?.lowercase() ?: ""
                val profiles = database.profileDao().getAllProfiles()
                val found = if (profiles.isNotEmpty()) {
                    if (activeId.isNotEmpty()) {
                        profiles.find { it.id == activeId }
                    } else {
                        profiles.find { it.email.trim().lowercase() == emailClean }
                    }
                } else null
                
                if (found != null) {
                    activeProfile.value = found
                } else {
                    activeProfile.value = activeProfile.value?.copy(password = password)
                }
            }
            onResult(error)
        }
    }

    init {
        // Initialize real-time network connectivity monitoring
        try {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            _isInternetAvailable.value = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            val networkRequest = android.net.NetworkRequest.Builder()
                .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(networkRequest, object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    _isInternetAvailable.value = true
                }

                override fun onLost(network: android.net.Network) {
                    _isInternetAvailable.value = false
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("MajarahViewModel", "Failed to register network callback: ${e.message}")
            _isInternetAvailable.value = true
        }

        // Initialize the app with Room products seed and local session loading
        viewModelScope.launch {
            checkForUpdates()
            repository.checkAndPrepopulateProducts()
            syncOrders()
            
            // Start background polling sync loop to fetch orders in real-time every 12 seconds
            launch {
                while (true) {
                    kotlinx.coroutines.delay(12000)
                    try {
                        syncOrders()
                    } catch (e: Exception) {
                        android.util.Log.e("MajarahViewModel", "Background polling sync error: ${e.message}")
                    }
                }
            }
            
            // Check for saved local profile session
            var profiles = database.profileDao().getAllProfiles()
            if (profiles.isEmpty()) {
                // Prepopulate with requested admin profile
                val adminProfile = com.example.data.db.ProfileEntity(
                    id = "mawiaosman-admin-uuid",
                    name = "معاوية عثمان أحمد ياسين",
                    phone = "0910074223",
                    email = "mawiaosman0@gmail.com",
                    password = "admin",
                    createdAt = System.currentTimeMillis()
                )
                database.profileDao().insertProfile(adminProfile)
                profiles = listOf(adminProfile)
            }
            
            // Wait for 2.5 seconds to show the beautiful animated Cosmic Splash screen
            delay(2500)
            
            val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
            val isUserLoggedIn = sharedPrefs.getBoolean("is_logged_in_state", false)
            val loggedInEmail = sharedPrefs.getString("logged_in_email", "") ?: ""
            
            if (isUserLoggedIn && loggedInEmail.isNotBlank()) {
                val p = profiles.find { it.email.trim().lowercase() == loggedInEmail.trim().lowercase() || it.phone.trim() == loggedInEmail.trim() } ?: profiles.first()
                activeProfile.value = p
                checkoutName.value = p.name
                checkoutPhone.value = p.phone
                _isLoggedIn.value = true

                // Background resolution of placeholder IDs (like mawiaosman-admin-uuid) to real Supabase UID
                val currentId = p.id.replace("\"", "").replace("'", "").trim()
                if (currentId == "mawiaosman-admin-uuid" || currentId.contains("admin") || currentId.contains("placeholder") || currentId.contains("recovered_")) {
                    viewModelScope.launch {
                        try {
                            val remoteList = com.example.data.network.SupabaseClient.api.getProfilesByEmail(emailFilter = "eq.${p.email.trim().lowercase()}")
                            if (remoteList.isNotEmpty()) {
                                val realId = remoteList.first().id?.replace("\"", "")?.replace("'", "")?.trim()
                                if (!realId.isNullOrBlank() && realId != p.id) {
                                    val updatedProfile = p.copy(id = realId)
                                    database.profileDao().deleteProfileById(p.id)
                                    database.profileDao().insertProfile(updatedProfile)
                                    activeProfile.value = updatedProfile
                                    android.util.Log.d("MajarahViewModel", "Background startup resolved real ID from Supabase profiles: $realId")
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MajarahViewModel", "Failed background startup ID resolution: ${e.message}")
                        }
                    }
                }

                val cleanP = p.phone.trim().replace("+", "").replace(" ", "")
                val matchesCourier = database.courierDao().getAllCouriersSnapshot().any { c ->
                    c.phone.trim().replace("+", "").replace(" ", "") == cleanP || c.phone.trim() == p.phone.trim()
                }
                val matchesSeller = database.sellerDao().getAllSellersSnapshot().any { s ->
                    s.email.trim().lowercase() == p.email.trim().lowercase()
                }
                val matchesPharmacist = database.pharmacyDao().getAllPharmaciesSnapshot().any { pharm ->
                    (pharm.pharmacistEmail.isNotBlank() && pharm.pharmacistEmail.trim().lowercase() == p.email.trim().lowercase()) ||
                    (pharm.phone.isNotBlank() && isPhoneMatchHelper(pharm.phone, p.phone)) ||
                    (pharm.doctorName.isNotBlank() && pharm.doctorName.trim().lowercase() == p.name.trim().lowercase())
                }
                val matchesRestaurant = database.restaurantDao().getAllRestaurantsSnapshot().any { rest ->
                    (rest.phone.isNotBlank() && isPhoneMatchHelper(rest.phone, p.phone)) ||
                    (rest.name.isNotBlank() && rest.name.trim().lowercase() == p.name.trim().lowercase())
                }
                val matchesAdminManager = database.adminManagerDao().getAllAdminManagersSnapshot().any { m ->
                    (m.email.isNotBlank() && m.email.trim().lowercase() == p.email.trim().lowercase()) ||
                    (m.phone.isNotBlank() && isPhoneMatchHelper(m.phone, p.phone))
                }

                val isPharmacistUser = p.role == "pharmacist" || sharedPrefs.getString("user_role_${p.email.trim().lowercase()}", "") == "pharmacist" || matchesPharmacist
                val isRestaurantUser = p.role == "restaurant" || sharedPrefs.getString("user_role_${p.email.trim().lowercase()}", "") == "restaurant" || matchesRestaurant
                val isManagerUser = p.role == "admin" || sharedPrefs.getString("user_role_${p.email.trim().lowercase()}", "") == "admin" || matchesAdminManager
                val isAdminUser = (p.email.trim().lowercase() == "mawiaosman0@gmail.com") || isManagerUser

                if (isAdminUser) {
                    _currentScreen.value = Screen.Admin
                } else if (isPharmacistUser) {
                    _currentScreen.value = Screen.Pharmacist
                } else if (isRestaurantUser) {
                    _selectedCategory.value = "restaurant"
                    _currentScreen.value = Screen.Restaurant
                } else if (matchesCourier || p.role == "courier") {
                    _currentScreen.value = Screen.Courier
                } else if (matchesSeller || p.role == "seller") {
                    _currentScreen.value = Screen.Seller
                } else {
                    _selectedCategory.value = ""
                    _currentScreen.value = Screen.Home
                }
            } else {
                _currentScreen.value = Screen.Login
            }

            // Auto classification-to-role synchronization with Supabase
            viewModelScope.launch {
                userClassification.collect { classification ->
                    if (classification.isNotEmpty() && classification != "زائر 🌌") {
                        val profile = activeProfile.value
                        if (profile != null) {
                            val currentRoleInDb = profile.role
                            if (currentRoleInDb != classification) {
                                val updatedProf = profile.copy(role = classification)
                                // Save locally
                                database.profileDao().insertProfile(updatedProf)
                                // Sync remote
                                val cleanId = profile.id.replace("\"", "").replace("'", "").trim()
                                if (cleanId.isNotEmpty() && cleanId != "mawiaosman-admin-uuid") {
                                    try {
                                        com.example.data.network.SupabaseClient.api.updateProfile(
                                            "eq.$cleanId",
                                            com.example.data.network.SupabaseProfile(
                                                name = profile.name,
                                                phone = profile.phone,
                                                email = profile.email,
                                                role = classification
                                            )
                                        )
                                    } catch (e: Exception) {
                                        android.util.Log.e("MajarahViewModel", "Failed to sync dynamic classification role: ${e.message}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun refreshConnection() {
        viewModelScope.launch {
            repository.checkAndPrepopulateProducts()
            repository.syncRemoteOrdersToLocal()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        if (screen is Screen.ProductDetail) {
            viewModelScope.launch {
                _selectedProduct.value = database.productDao().getProductById(screen.productId)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(productId)
            // If the active product details correspond to this product, refresh it as well
            val activePr = _selectedProduct.value
            if (activePr != null && activePr.id == productId) {
                _selectedProduct.value = database.productDao().getProductById(productId)
            }
        }
    }

    fun addToCart(productId: Int, quantity: Int = 1) {
        if (isSeller.value || isAdmin.value || isGeneralAdmin.value || isPharmacist.value || isCourier.value || isRestaurant.value || isAdministrativeManager.value) return
        viewModelScope.launch {
            repository.addToCart(productId, quantity)
        }
    }

    fun updateCartQuantity(productId: Int, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, quantity)
        }
    }

    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun submitCheckout(paymentMethod: String = "cash", transactionId: String = "", bankReceiptBase64: String? = null) {
        val phone = checkoutPhone.value.trim()
        val address = checkoutAddress.value.trim()
        val name = checkoutName.value.trim()
        val currentItems = cartItems.value

        if (phone.isEmpty() || address.isEmpty() || name.isEmpty() || currentItems.isEmpty()) {
            return
        }

        viewModelScope.launch {
            val orderId = "M-${(1000..9999).random()}"
            val coupon = _appliedCoupon.value
            val discountPercentage = getCouponDiscountPercentage(coupon)
            val discountFactor = 1.0 - discountPercentage / 100.0

            val methodLabel = if (paymentMethod == "bank") {
                "تحويل بنكي - إشعار: ${transactionId.trim()}"
            } else {
                "الدفع نقداً عند التسليم"
            }

            val err = repository.placeCompletedOrder(
                orderId = orderId,
                customerName = name,
                customerPhone = phone,
                customerAddress = address,
                items = currentItems,
                discountFactor = discountFactor,
                paymentMethod = methodLabel,
                bankReceiptImageUri = bankReceiptBase64
            )

            val netTotal = calculateDiscountedSum(currentItems, coupon)
            val couponMessage = if (coupon != null) "✨ كود الخصم الكوني: $coupon (خصم %$discountPercentage)\n" else ""

            val infoMethod = if (paymentMethod == "bank") {
                "تحويل بنكي 💳 (رقم العملية: $transactionId)"
            } else {
                "الدفع نقداً عند التسليم 💵"
            }

            val itemsText = currentItems.joinToString("\n") { "• ${it.product.name} (العدد: ${it.quantity}) - ${formatPrice(it.product.price * it.quantity)}" }
            val formattedDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

            val invoiceContent = """
🌌 فاتورة المجرة الإلكترونية 🌌
---------------------------
👤 نوع الفاتورة: فاتورة عميل
✍️ اسم العميل: $name
📞 هاتف العميل: $phone
📍 عنوان التوصيل: $address
📦 رقم الطلب: #$orderId
📅 تاريخ الطلب: $formattedDate
💳 طريقة الدفع: $infoMethod
---------------------------
💸 تفاصيل المنتجات:
$itemsText
$couponMessage---------------------------
🚚 قيمة التوصيل: ${formatPrice(0.0)} SDG
💰 الإجمالي النهائي الفعلي: ${formatPrice(netTotal)} SDG
---------------------------
التوصيل خلال 24 ساعة بمشيئة الله.
شكراً لثقتكم بمجرة التسوق الإلكتروني 🌌✨
            """.trimIndent()

            if (err == null) {
                _checkoutSuccessMessage.value = "تهانينا $name! 🎉\n\nتم إرسال طلبك ومزامنته سحابياً بنجاح! إليك الفاتورة التفصيلية للطلب:\n\n$invoiceContent"
            } else {
                val translatedErr = translateError(err) ?: ""
                _checkoutSuccessMessage.value = "تم حفظ طلبك محلياً بنجاح! إليك الفاتورة التفصيلية للطلب:\n\n$invoiceContent\n\n⚠️ فشلت المزامنة المباشرة لجدول الطلبات (orders) مع Supabase بسبب:\n\n$translatedErr"
            }
            
            // Reset form & coupon details
            val usedCoupon = _appliedCoupon.value
            if (usedCoupon != null) {
                database.appCouponDao().markCouponAsUsed(usedCoupon)
            }
            _appliedCoupon.value = null
            _couponError.value = null
            checkoutAddress.value = ""
            val active = activeProfile.value
            checkoutName.value = active?.name ?: ""
            checkoutPhone.value = active?.phone ?: ""
        }
    }

    fun dismissCheckoutSuccess() {
        _checkoutSuccessMessage.value = null
        navigateTo(Screen.History)
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Helper utilities
    fun calculateTotalSum(items: List<CartItemWithProduct>): Double {
        return items.sumOf { it.product.price * it.quantity }
    }

    fun formatPrice(price: Double): String {
        return "%,.0f".format(java.util.Locale.ENGLISH, price)
    }

    fun translateError(error: String?): String? {
        if (error == null) return null
        if (isEnglish.value) return error

        val lowercase = error.lowercase()
        return when {
            lowercase.contains("invalid login credentials") || lowercase.contains("invalid password") || lowercase.contains("user not found") || lowercase.contains("invalid email/password") ->
                "بيانات الدخول غير صحيحة! يرجى التحقق من البريد الإلكتروني وكلمة المرور."
            lowercase.contains("already exists") || lowercase.contains("unique constraint") || lowercase.contains("already registered") ->
                "الحساب مسجّل مسبقاً! يرجى تسجيل الدخول أو استخدام بريد إلكتروني/هاتف آخر للحساب."
            lowercase.contains("email not confirmed") ->
                "البريد الإلكتروني لم يتم تأكيده بعد، يرجى تفعيل البريد الإلكتروني."
            lowercase.contains("network error") || lowercase.contains("timeout") || lowercase.contains("failed to connect") || lowercase.contains("unable to resolve host") ->
                "فشل الاتصال بالشبكة! يرجى التحقق من اتصالك بالإنترنت وتجربة تشغيل تطبيق VPN إن كنت داخل السودان."
            lowercase.contains("password should be") || lowercase.contains("weak password") ->
                "كلمة المرور ضعيفة! يجب أن تتكون كلمة المرور من 6 خانات على الأقل لسلامة حسابك."
            lowercase.contains("bad request") ->
                "طلب غير صالح! يرجى التأكد من ملء جميع الحقول بشكل سليم وصحيح."
            else -> error
        }
    }

    val allOrdersFlow: StateFlow<List<OrderEntity>> = repository.orderHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun syncOrders(onComplete: (String?) -> Unit = {}) {
        viewModelScope.launch {
            val err = repository.syncRemoteOrdersToLocal()
            
            try {
                repository.syncRemoteRestaurantOrdersToLocal()
            } catch (e: Exception) {
                android.util.Log.e("MajarahViewModel", "Failed to sync restaurant orders: ${e.message}")
            }
            
            try {
                repository.syncRemotePharmacyOrdersToLocal()
            } catch (e: Exception) {
                android.util.Log.e("MajarahViewModel", "Failed to sync pharmacy orders: ${e.message}")
            }

            try {
                repository.syncRemotePharmacyProductsToLocal()
            } catch (e: Exception) {
                android.util.Log.e("MajarahViewModel", "Failed to sync pharmacy products: ${e.message}")
            }

            try {
                repository.syncRemotePharmaciesToLocal()
            } catch (e: Exception) {
                android.util.Log.e("MajarahViewModel", "Failed to sync pharmacies: ${e.message}")
            }

            try {
                repository.syncRemoteRestaurantsToLocal()
            } catch (e: Exception) {
                android.util.Log.e("MajarahViewModel", "Failed to sync restaurants: ${e.message}")
            }
            
            // Sync remote ratings to local Room
            try {
                val remoteRatings = com.example.data.network.SupabaseClient.api.getAppRatings()
                if (remoteRatings.isNotEmpty()) {
                    database.appRatingDao().clearRatings()
                    remoteRatings.forEach { r ->
                        database.appRatingDao().insertRating(
                            com.example.data.db.AppRatingEntity(
                                id = r.id ?: 0,
                                customerName = r.customerName ?: "عميل المجرة للتسوق",
                                customerEmail = r.customerEmail ?: "guest@majarah.com",
                                customerPhone = r.customerPhone ?: "",
                                customerClassification = r.customerClassification ?: "",
                                ratingStars = r.ratingStars ?: 5,
                                comment = r.comment ?: "",
                                ratingDate = r.ratingDate ?: System.currentTimeMillis()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MajarahViewModel", "Failed to sync remote ratings: ${e.message}")
            }

            // Sync remote coupons to local Room
            try {
                val remoteCoupons = com.example.data.network.SupabaseClient.api.getAppCoupons()
                if (remoteCoupons.isNotEmpty()) {
                    database.appCouponDao().clearCoupons()
                    remoteCoupons.forEach { c ->
                        database.appCouponDao().insertCoupon(
                            com.example.data.db.AppCouponEntity(
                                code = c.code,
                                discountPercent = c.discountPercent ?: 0.0,
                                isFreeDelivery = c.isFreeDelivery ?: false,
                                isBogo = c.isBogo ?: false,
                                forUserEmail = c.forUserEmail ?: "",
                                isUsed = c.isUsed ?: false,
                                offerTitle = c.offerTitle ?: ""
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MajarahViewModel", "Failed to sync remote coupons: ${e.message}")
            }

            // Sync remote admin managers to local Room
            try {
                val remoteManagers = com.example.data.network.SupabaseClient.api.getAdminManagers()
                if (remoteManagers.isNotEmpty()) {
                    database.adminManagerDao().clearAdminManagers()
                    remoteManagers.forEach { m ->
                        database.adminManagerDao().insertAdminManager(
                            com.example.data.db.AdminManagerEntity(
                                email = m.email ?: "",
                                name = m.name ?: "مدير إداري",
                                phone = m.phone ?: ""
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MajarahViewModel", "Failed to sync remote admin managers: ${e.message}")
            }

            onComplete(err)
        }
    }

    fun addProduct(product: ProductEntity, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val err = repository.addProduct(product)
            onComplete(err)
        }
    }

    fun updateProduct(product: ProductEntity, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val err = repository.updateProduct(product)
            onComplete(err)
        }
    }

    fun deleteProduct(productId: Int, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val err = repository.deleteProduct(productId)
            onComplete(err)
        }
    }

    val allCouriers: StateFlow<List<com.example.data.db.CourierEntity>> = repository.allCouriers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addCourier(name: String, phone: String, stateInfo: String, status: String, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val err = repository.insertCourier(
                com.example.data.db.CourierEntity(
                    name = name,
                    phone = phone,
                    stateInfo = stateInfo,
                    status = status
                )
            )
            onComplete(err)
        }
    }

    fun removeCourier(id: Int, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val err = repository.deleteCourier(id)
            onComplete(err)
        }
    }

    fun addSeller(name: String, email: String, phone: String, classification: String, commissionRate: Double, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val err = repository.insertSeller(
                com.example.data.db.SellerEntity(
                    name = name,
                    email = email,
                    phone = phone,
                    classification = classification,
                    commissionRate = commissionRate
                )
            )
            onComplete(err)
        }
    }

    fun removeSeller(id: Int, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val err = repository.deleteSeller(id)
            onComplete(err)
        }
    }

    fun addAdminManager(name: String, email: String, phone: String, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.adminManagerDao.insertAdminManager(
                    com.example.data.db.AdminManagerEntity(
                        name = name,
                        email = email,
                        phone = phone
                    )
                )
                try {
                    com.example.data.network.SupabaseClient.api.insertAdminManagers(
                        listOf(
                            com.example.data.network.SupabaseAdminManager(
                                email = email,
                                name = name,
                                phone = phone
                            )
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.e("MajarahViewModel", "Failed to sync added admin manager: ${e.message}")
                }
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e.localizedMessage ?: "حدث خطأ غير معروف")
            }
        }
    }

    fun removeAdminManager(id: Int, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val manager = database.adminManagerDao().getAllAdminManagersSnapshot().find { it.id == id }
                repository.adminManagerDao.deleteAdminManager(id)
                if (manager != null) {
                    try {
                        com.example.data.network.SupabaseClient.api.deleteAdminManager("eq.${manager.email}")
                    } catch (e: Exception) {
                        android.util.Log.e("MajarahViewModel", "Failed to sync removed admin manager: ${e.message}")
                    }
                }
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e.localizedMessage ?: "حدث خطأ غير معروف")
            }
        }
    }

    fun activateAdminManager(name: String, email: String, phone: String, password: String, onComplete: (String?) -> Unit) {
        isLoginLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Register profile
                val error = repository.registerUserProfile(name, phone, email, password, "admin")
                
                // 2. Set admin role in prefs
                val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("user_role_${email.trim().lowercase()}", "admin").apply()
                sharedPrefs.edit().putBoolean("is_logged_in_state", true).apply()
                sharedPrefs.edit().putString("logged_in_email", email.trim().lowercase()).apply()
                
                // 3. Auto login
                val loginResult = repository.loginUserProfile(email, password)
                val p = loginResult.first ?: com.example.data.db.ProfileEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    phone = phone,
                    email = email,
                    password = password,
                    role = "admin",
                    createdAt = System.currentTimeMillis()
                )
                
                database.profileDao().insertProfile(p)
                
                activeProfile.value = p
                _isLoggedIn.value = true
                checkoutName.value = p.name
                checkoutPhone.value = p.phone
                
                _currentScreen.value = Screen.Admin
                
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e.localizedMessage ?: "حدث خطأ غير معروف")
            } finally {
                isLoginLoading.value = false
            }
        }
    }

    fun updateCourier(courier: com.example.data.db.CourierEntity, onComplete: (String?) -> Unit = {}) {
        viewModelScope.launch {
            val err = repository.updateCourier(courier)
            onComplete(err)
        }
    }

    // Real SMS OTP dispatcher trigger
    fun sendResetSmsOtp(phone: String, code: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.sendSmsOtpReal(phone, code)
            onComplete(result.first, result.second)
        }
    }

    // Real Email OTP dispatcher trigger for Google accounts
    fun sendResetEmailOtp(email: String, code: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.sendEmailOtpReal(email, code)
            onComplete(result.first, result.second)
        }
    }

    fun updateOrderPaymentMethod(orderId: String, paymentMethod: String, transactionId: String, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val err = repository.updateOrderPaymentMethod(orderId, paymentMethod, transactionId)
            if (err == null) {
                syncOrders()
            }
            onComplete(err)
        }
    }

    fun updateOrderStatus(orderId: String, status: String, courierName: String = "", courierPhone: String = "", deliveryFee: Double? = null, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val existingOrder = allOrdersFlow.value.find { it.orderId == orderId }
            val existingStatus = existingOrder?.statusArabic ?: ""
            val suffix = if (existingStatus.contains("(")) {
                existingStatus.substring(existingStatus.indexOf("("))
            } else {
                ""
            }
            val finalStatusWithSuffix = if (suffix.isNotEmpty() && !status.contains("(")) {
                "$status $suffix"
            } else {
                status
            }

            val err = repository.updateOrderStatus(orderId, finalStatusWithSuffix, courierName, courierPhone, deliveryFee)
            if (err == null) {
                // If a courier was assigned, set their status to "في مهمة توصيل 🟡"
                val finalName = courierName.trim()
                val finalPhone = courierPhone.trim()
                if (finalPhone.isNotEmpty() || finalName.isNotEmpty()) {
                    val matchingCourier = allCouriers.value.find { c ->
                        (finalName.isNotEmpty() && c.name.trim().equals(finalName, ignoreCase = true)) ||
                        (finalPhone.isNotEmpty() && c.phone.trim().replace("+", "").replace(" ", "") == finalPhone.replace("+", "").replace(" ", ""))
                    }
                    if (matchingCourier != null && !matchingCourier.status.contains("مهمة")) {
                        repository.updateCourier(matchingCourier.copy(status = "في مهمة توصيل 🟡"))
                    }
                }
                
                // If the status is "تم توصيل الطلب واستلام المبلغ ✅" or "الطلب ملغي ❌"
                if (status.contains("تم توصيل") || status.contains("ملغي")) {
                    val currentOrder = allOrdersFlow.value.find { it.orderId == orderId }
                    val assignedCourierPhone = (currentOrder?.courierPhone ?: courierPhone).trim()
                    val assignedCourierName = (currentOrder?.courierName ?: courierName).trim()
                    
                    if (assignedCourierPhone.isNotEmpty() || assignedCourierName.isNotEmpty()) {
                        val matchingCourier = allCouriers.value.find { c ->
                            (assignedCourierName.isNotEmpty() && c.name.trim().equals(assignedCourierName, ignoreCase = true)) ||
                            (assignedCourierPhone.isNotEmpty() && c.phone.trim().replace("+", "").replace(" ", "") == assignedCourierPhone.replace("+", "").replace(" ", ""))
                        }
                        if (matchingCourier != null) {
                            // Check if this courier has any other active/pending orders in "تم تسليم المندوب"
                            val hasOtherActiveOrders = allOrdersFlow.value.any { o ->
                                o.orderId != orderId &&
                                ((assignedCourierName.isNotEmpty() && o.courierName.trim().equals(assignedCourierName, ignoreCase = true)) ||
                                 (assignedCourierPhone.isNotEmpty() && o.courierPhone.trim().replace("+", "").replace(" ", "") == assignedCourierPhone.replace("+", "").replace(" ", ""))) &&
                                o.statusArabic.contains("تم تسليم")
                            }
                            if (!hasOtherActiveOrders) {
                                repository.updateCourier(matchingCourier.copy(status = "نشط ومتوفر 🟢"))
                            }
                        }
                    }
                }
            }
            onComplete(err)
        }
    }

    fun updateOrderPayment(orderId: String, paymentMethod: String, receiptBase64: String?, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                database.orderDao().updateOrderPayment(orderId, paymentMethod, receiptBase64)
                
                // Sync to Supabase remotely
                val updateFields = mutableMapOf<String, String>()
                updateFields["payment_method"] = paymentMethod
                if (receiptBase64 != null) {
                    updateFields["bank_receipt_image_uri"] = receiptBase64
                }
                try {
                    com.example.data.network.SupabaseClient.api.updateOrderStatus("eq.$orderId", updateFields)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                syncOrders {}
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e.localizedMessage ?: e.toString())
            }
        }
    }

    fun updateRestaurantOrderPayment(id: Int, paymentMethod: String, receiptBase64: String?, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateRestaurantOrderPayment(id, paymentMethod, receiptBase64)
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e.localizedMessage ?: e.toString())
            }
        }
    }

    fun updatePharmacyOrderPayment(id: Int, paymentMethod: String, receiptBase64: String?, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updatePharmacyOrderPayment(id, paymentMethod, receiptBase64)
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e.localizedMessage ?: e.toString())
            }
        }
    }

    // --- Planet Pharmacy State Flows ---
    val allPharmacies = repository.allPharmacies.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allPharmacyProducts = repository.allPharmacyProducts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allPharmacyOrders = repository.allPharmacyOrders.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val myPlacedPharmacyOrderIds = MutableStateFlow<Set<Int>>(emptySet())
    val myPlacedRestaurantOrderIds = MutableStateFlow<Set<Int>>(emptySet())
    val myPlacedPhones = MutableStateFlow<Set<String>>(emptySet())

    val isPharmacist: StateFlow<Boolean> = combine(activeProfile, _isLoggedIn, allPharmacies) { profile, loggedIn, pharmacies ->
        if (!loggedIn || profile == null) {
            false
        } else {
            val emailClean = profile.email.trim().lowercase()
            val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
            val storedRole = sharedPrefs.getString("user_role_${emailClean}", "") ?: ""
            profile.role == "pharmacist" || storedRole == "pharmacist" || pharmacies.any { p -> p.pharmacistEmail.trim().lowercase() == emailClean }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun getPharmacyByPharmacistEmail(email: String, onResult: (com.example.data.db.PharmacyEntity?) -> Unit) {
        viewModelScope.launch {
            val res = repository.getPharmacyByPharmacistEmail(email)
            onResult(res)
        }
    }

    fun addPharmacy(name: String, doctorName: String, phone: String, location: String, pharmacistEmail: String, imageBase64: String = "", hasCosmetics: Boolean = false, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                val p = com.example.data.db.PharmacyEntity(
                    name = name,
                    doctorName = doctorName,
                    phone = phone,
                    location = location,
                    pharmacistEmail = pharmacistEmail,
                    isApproved = false,
                    imageBase64 = imageBase64,
                    hasCosmetics = hasCosmetics
                )
                repository.insertPharmacy(p)
            } catch (e: Exception) {
                error = e.localizedMessage ?: "حدث خطأ أثناء حفظ الصيدلية"
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun updatePharmacy(pharmacy: com.example.data.db.PharmacyEntity, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.updatePharmacy(pharmacy)
            } catch (e: Exception) {
                error = e.localizedMessage ?: "حدث خطأ أثناء تعديل بيانات الصيدلية"
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun approvePharmacy(id: Int, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.updatePharmacyApproval(id, true)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun deletePharmacy(id: Int, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.deletePharmacy(id)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun addPharmacyProduct(pharmacyId: Int, type: String, name: String, company: String, price: Double, imageBase64: String, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                val prod = com.example.data.db.PharmacyProductEntity(
                    pharmacyId = pharmacyId,
                    type = type,
                    name = name,
                    company = company,
                    price = price,
                    imageBase64 = imageBase64,
                    isApproved = false
                )
                repository.insertPharmacyProduct(prod)
            } catch (e: Exception) {
                error = e.localizedMessage ?: "حدث خطأ أثناء حفظ المنتج"
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun approvePharmacyProduct(id: Int, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.updatePharmacyProductApproval(id, true)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun deletePharmacyProduct(id: Int, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.deletePharmacyProduct(id)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun getProductsByPharmacy(pharmacyId: Int): kotlinx.coroutines.flow.Flow<List<com.example.data.db.PharmacyProductEntity>> {
        return repository.getProductsByPharmacy(pharmacyId)
    }

    fun addPharmacyOrder(pharmacyId: Int, customerName: String, customerPhone: String, customerEmail: String, prescriptionBase64: String, deliveryLocation: String = "", onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            var error: String? = null
            try {
                val ord = com.example.data.db.PharmacyOrderEntity(
                    pharmacyId = pharmacyId,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerEmail = customerEmail,
                    prescriptionImageBase64 = prescriptionBase64,
                    status = "بانتظار الصيدلي",
                    deliveryLocation = deliveryLocation
                )
                val newId = repository.insertPharmacyOrder(ord)
                if (newId > 0) {
                    myPlacedPharmacyOrderIds.value = myPlacedPharmacyOrderIds.value + newId.toInt()
                }
                if (customerPhone.isNotBlank()) {
                    myPlacedPhones.value = myPlacedPhones.value + customerPhone.trim()
                }
            } catch (e: Exception) {
                error = e.localizedMessage ?: "حدث خطأ أثناء تقديم الروشتة"
            } finally {
                onComplete(error)
            }
        }
    }

    fun pharmacistExecuteOrder(orderId: Int, medicinesJson: String, totalPrice: Double, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.updatePharmacyOrderPriceAndStatus(orderId, "بانتظار المدير", totalPrice, medicinesJson)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun adminApprovePharmacyOrder(
        orderId: Int, 
        courierName: String, 
        courierPhone: String, 
        deliveryFee: Double, 
        medicinesJson: String = "", 
        medicinePrice: Double = 0.0, 
        onComplete: (String?) -> Unit
    ) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                if (medicinesJson.isNotBlank() || medicinePrice > 0.0) {
                    repository.updatePharmacyOrderPriceAndStatus(orderId, "بانتظار المدير", medicinePrice, medicinesJson)
                }
                repository.assignPharmacyOrderCourierAndDeliveryFee(orderId, "تم تسليم المندوب", courierName, courierPhone, deliveryFee)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun updatePharmacyOrderStatus(orderId: Int, status: String, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.updatePharmacyOrderStatus(orderId, status)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    // --- Planet Restaurants View & Actions ---
    val allRestaurants = repository.allRestaurants.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allRestaurantOrders = repository.allRestaurantOrders.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addRestaurant(id: Int = 0, name: String, phone: String, menuImageUri: String?, logoImageUri: String? = null, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                val restaurant = com.example.data.db.RestaurantEntity(
                    id = id,
                    name = name,
                    phone = phone,
                    menuImageUri = menuImageUri,
                    logoImageUri = logoImageUri
                )
                repository.insertRestaurant(restaurant)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun deleteRestaurant(id: Int, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.deleteRestaurant(id)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun approveRestaurant(id: Int, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.updateRestaurantApproval(id, true)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun addRestaurantOrder(
        restaurantId: Int,
        restaurantName: String,
        restaurantPhone: String,
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        itemsAndNotes: String,
        paymentMethod: String,
        deliveryFee: Double,
        bankReceiptImageUri: String?,
        onComplete: (String?, com.example.data.db.RestaurantOrderEntity?) -> Unit
    ) {
        viewModelScope.launch {
            var error: String? = null
            var savedOrder: com.example.data.db.RestaurantOrderEntity? = null
            try {
                val order = com.example.data.db.RestaurantOrderEntity(
                    restaurantId = restaurantId,
                    restaurantName = restaurantName,
                    restaurantPhone = restaurantPhone,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerEmail = customerEmail,
                    itemsAndNotes = itemsAndNotes,
                    status = "معلق",
                    paymentMethod = paymentMethod,
                    deliveryFee = deliveryFee,
                    bankReceiptImageUri = bankReceiptImageUri
                )
                val newId = repository.insertRestaurantOrder(order)
                if (newId > 0) {
                    savedOrder = order.copy(id = newId.toInt())
                    myPlacedRestaurantOrderIds.value = myPlacedRestaurantOrderIds.value + newId.toInt()
                }
                if (customerPhone.isNotBlank()) {
                    myPlacedPhones.value = myPlacedPhones.value + customerPhone.trim()
                }
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                onComplete(error, savedOrder)
            }
        }
    }

    fun updateRestaurantOrderStatus(id: Int, status: String, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.updateRestaurantOrderStatus(id, status)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun updateRestaurantOrderPriceAndStatus(id: Int, status: String, foodPrice: Double, detailedPrice: String, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.updateRestaurantOrderPriceAndStatus(id, status, foodPrice, detailedPrice)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun assignCourierToRestaurantOrder(id: Int, status: String, courierName: String, courierPhone: String, deliveryFee: Double, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.assignCourierToRestaurantOrder(id, status, courierName, courierPhone, deliveryFee)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun deleteRestaurantOrder(id: Int, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.deleteRestaurantOrder(id)
            } catch (e: Exception) {
                error = e.localizedMessage
            } finally {
                isGlobalLoading.value = false
                onComplete(error)
            }
        }
    }

    fun updateProfileImage(imageUri: String, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val active = activeProfile.value
                val profiles = database.profileDao().getAllProfiles()
                if (profiles.isNotEmpty()) {
                    val current = if (active != null) {
                        profiles.find { it.id == active.id } ?: profiles.first()
                    } else {
                        profiles.first()
                    }
                    val updated = current.copy(profileImageUri = imageUri)
                    database.profileDao().insertProfile(updated)
                    activeProfile.value = updated
                    onComplete(null)
                } else {
                    onComplete("لم يتم العثور على ملف شخصي")
                }
            } catch (e: Exception) {
                onComplete(e.localizedMessage ?: "فشل تحديث صورة الملف الشخصي")
            }
        }
    }

    // --- NEW REAL-TIME CLASSIFICATIONS & COUPONS FLOWS ---
    val isRestaurant: StateFlow<Boolean> = combine(activeProfile, _isLoggedIn, allRestaurants) { profile, loggedIn, restaurants ->
        if (!loggedIn || profile == null) {
            false
        } else {
            val emailClean = profile.email.trim().lowercase()
            val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
            val storedRole = sharedPrefs.getString("user_role_${emailClean}", "") ?: ""
            profile.role == "restaurant" || storedRole == "restaurant" || restaurants.any { r -> r.phone.trim() == profile.phone.trim() || r.name.trim().lowercase() == profile.name.trim().lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userClassification: StateFlow<String> = combine(
        listOf(
            activeProfile,
            _isLoggedIn,
            isGeneralAdmin,
            isAdministrativeManager,
            isSeller,
            isCourier,
            isPharmacist,
            isRestaurant,
            orderHistory,
            repository.allProducts,
            allRestaurantOrders,
            allPharmacyOrders,
            allPharmacies,
            allRestaurants
        )
    ) { array ->
        val profile = array[0] as? com.example.data.db.ProfileEntity
        val loggedIn = array[1] as? Boolean ?: false
        val isGen = array[2] as? Boolean ?: false
        val isAdm = array[3] as? Boolean ?: false
        val isSel = array[4] as? Boolean ?: false
        val isCou = array[5] as? Boolean ?: false
        val isPhar = array[6] as? Boolean ?: false
        val isRest = array[7] as? Boolean ?: false
        val orders = array[8] as? List<com.example.data.db.OrderEntity> ?: emptyList()
        val products = array[9] as? List<com.example.data.db.ProductEntity> ?: emptyList()
        val restOrders = array[10] as? List<com.example.data.db.RestaurantOrderEntity> ?: emptyList()
        val pharOrders = array[11] as? List<com.example.data.db.PharmacyOrderEntity> ?: emptyList()
        val pharmacies = array[12] as? List<com.example.data.db.PharmacyEntity> ?: emptyList()
        val restaurants = array[13] as? List<com.example.data.db.RestaurantEntity> ?: emptyList()

        if (!loggedIn || profile == null) return@combine "زائر 🌌"
        if (isGen) return@combine "مدير عام 👑"
        if (isAdm) return@combine "مدير إداري 🧑‍💼"
        
        val emailClean = profile.email.trim().lowercase()
        val phoneClean = profile.phone.trim().replace("+", "").replace(" ", "")
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        
        if (isCou) {
            val courierOrdersCount = orders.filter {
                val cPhone = it.courierPhone.trim().replace("+", "").replace(" ", "")
                (cPhone == phoneClean || it.courierPhone.trim() == profile.phone.trim()) &&
                it.orderDate >= oneWeekAgo &&
                (it.statusArabic.contains("تم") || it.statusArabic.contains("توصيل") || it.statusArabic.contains("تمام"))
            }.distinctBy { it.orderId }.size +
            restOrders.filter {
                val cPhone = it.courierPhone?.trim()?.replace("+", "")?.replace(" ", "") ?: ""
                (cPhone == phoneClean || it.courierPhone?.trim() == profile.phone.trim()) &&
                it.createdAt >= oneWeekAgo &&
                (it.status.contains("تم") || it.status.contains("توصيل") || it.status.contains("تمام"))
            }.size +
            pharOrders.filter {
                val cPhone = it.courierPhone?.trim()?.replace("+", "")?.replace(" ", "") ?: ""
                (cPhone == phoneClean || it.courierPhone?.trim() == profile.phone.trim()) &&
                it.createdAt >= oneWeekAgo &&
                (it.status.contains("تم") || it.status.contains("توصيل") || it.status.contains("تمام"))
            }.size

            return@combine when {
                courierOrdersCount >= 15 -> "مندوب ذهبي 👑"
                courierOrdersCount >= 6 -> "مندوب مميز ⭐"
                else -> "مندوب المجرة 🚴"
            }
        }
        
        if (isSel) {
            val sellerProducts = products.filter { it.sellerEmail.trim().lowercase() == emailClean }.size
            return@combine when {
                sellerProducts >= 10 -> "تاجر المجرة 👑"
                sellerProducts >= 4 -> "تاجر مميز ⭐"
                else -> "تاجر المجرة 🛍️"
            }
        }
        
        if (isRest) {
            val rOrders = restOrders.filter {
                (it.restaurantPhone.trim() == profile.phone.trim() ||
                it.restaurantName.trim().lowercase() == profile.name.trim().lowercase()) &&
                it.status.contains("تم")
            }.size
            return@combine when {
                rOrders >= 12 -> "مطعم ذهبي 👑"
                rOrders >= 5 -> "مطعم مميز ⭐"
                else -> "مطعم المجرة 🍔"
            }
        }
        
        if (isPhar) {
            val myPharmacy = pharmacies.find { it.pharmacistEmail.trim().lowercase() == emailClean }
            val pOrders = if (myPharmacy != null) {
                pharOrders.filter { it.pharmacyId == myPharmacy.id && it.status.contains("تم") }.size
            } else 0
            
            return@combine when {
                pOrders >= 10 -> "صيدلي ذهبي 👑"
                pOrders >= 4 -> "صيدلي مميز ⭐"
                else -> "صيدلي المجرة 💊"
            }
        }
        
        val customerOrdersCount = orders.filter {
            it.customerPhone.trim() == profile.phone.trim() ||
            it.customerName.trim().lowercase() == profile.name.trim().lowercase()
        }.distinctBy { it.orderId }.size +
        restOrders.filter {
            it.customerPhone.trim() == profile.phone.trim()
        }.size +
        pharOrders.filter {
            it.customerPhone.trim() == profile.phone.trim()
        }.size

        return@combine when {
            customerOrdersCount >= 15 -> "عميل ذهبي 👑"
            customerOrdersCount >= 5 -> "عميل مميز ⭐"
            else -> "عميل المجرة 👤"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "زائر 🌌")

    val allRatingsFlow: StateFlow<List<com.example.data.db.AppRatingEntity>> = database.appRatingDao().getAllRatings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCouponsFlow: StateFlow<List<com.example.data.db.AppCouponEntity>> = database.appCouponDao().getAllCoupons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun submitAppRating(stars: Int, comment: String) {
        viewModelScope.launch {
            val profile = activeProfile.value
            val rating = com.example.data.db.AppRatingEntity(
                customerName = profile?.name ?: "عميل المجرة للتسوق",
                customerEmail = profile?.email ?: "guest@majarah.com",
                customerPhone = profile?.phone ?: "",
                customerClassification = userClassification.value,
                ratingStars = stars,
                comment = comment,
                ratingDate = System.currentTimeMillis()
            )
            database.appRatingDao().insertRating(rating)
            try {
                com.example.data.network.SupabaseClient.api.insertAppRatings(
                    listOf(
                        com.example.data.network.SupabaseAppRating(
                            customerName = rating.customerName,
                            customerEmail = rating.customerEmail,
                            customerPhone = rating.customerPhone,
                            customerClassification = rating.customerClassification,
                            ratingStars = rating.ratingStars,
                            comment = rating.comment,
                            ratingDate = rating.ratingDate
                        )
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e("MajarahViewModel", "Failed to sync rating to Supabase: ${e.message}")
            }
            
            // Generate customized promo coupon based on rating stars ONLY if the user is distinguished or gold
            val classification = userClassification.value
            val isEligibleForCoupon = classification.contains("مميز") || classification.contains("ذهبي")
            
            if (isEligibleForCoupon) {
                val randSuffix = (1000..9999).random()
                val couponCode = if (stars >= 5) "MAJARAH_FREE_$randSuffix" else "HAPPY_BOGO_$randSuffix"
                val coupon = com.example.data.db.AppCouponEntity(
                    code = couponCode,
                    discountPercent = if (stars >= 5) 0.0 else 50.0,
                    isFreeDelivery = (stars >= 5),
                    isBogo = (stars < 5),
                    forUserEmail = profile?.email ?: "guest@majarah.com",
                    isUsed = false,
                    offerTitle = if (stars >= 5) "توصيل مجاني لتقييمك المتميز 🚚🌌" else "عرض اطلب واحد والثاني هدية لتقييمك الغالي 🎁🍔"
                )
                database.appCouponDao().insertCoupon(coupon)
                try {
                    com.example.data.network.SupabaseClient.api.insertAppCoupons(
                        listOf(
                            com.example.data.network.SupabaseAppCoupon(
                                code = coupon.code,
                                discountPercent = coupon.discountPercent,
                                isFreeDelivery = coupon.isFreeDelivery,
                                isBogo = coupon.isBogo,
                                forUserEmail = coupon.forUserEmail,
                                isUsed = coupon.isUsed,
                                offerTitle = coupon.offerTitle
                            )
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.e("MajarahViewModel", "Failed to sync coupon to Supabase: ${e.message}")
                }
                
                val toastMessage = when {
                    classification.contains("ذهبي") -> {
                        "شكراً لك عميلنا الذهبي على تقييمك ورأيك الغالي! 👑✨ لقد فزت بكوبون عرض كوني: $couponCode 🌌✨"
                    }
                    classification.contains("مميز") -> {
                        "شكراً لك عميلنا المميز على تقييمك ورأيك الغالي! ⭐✨ لقد فزت بكوبون عرض كوني: $couponCode 🌌✨"
                    }
                    else -> {
                        "تهانينا يا عميلنا الذهبي/المميز! لقد فزت بكوبون عرض كوني: $couponCode 🌌✨"
                    }
                }
                android.widget.Toast.makeText(
                    getApplication(), 
                    toastMessage, 
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } else {
                android.widget.Toast.makeText(
                    getApplication(), 
                    "شكراً لك عميل المجرة على تقييمك ورأيك الغالي! 🌌✨", 
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val allProfilesFlow = MutableStateFlow<List<com.example.data.db.ProfileEntity>>(emptyList())
    
    fun refreshAllProfiles() {
        viewModelScope.launch {
            val list = database.profileDao().getAllProfiles()
            allProfilesFlow.value = list
        }
    }

    fun deleteProfileAdmin(id: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                database.profileDao().deleteProfileById(id)
                val cleanId = id.replace("\"", "").replace("'", "").trim()
                com.example.data.network.SupabaseClient.api.deleteProfile("eq." + cleanId)
                refreshAllProfiles()
                onResult(null)
            } catch (e: Exception) {
                refreshAllProfiles()
                onResult(e.localizedMessage)
            }
        }
    }

    fun addProfileAdmin(profile: com.example.data.db.ProfileEntity, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                database.profileDao().insertProfile(profile)
                repository.registerUserProfile(profile.name, profile.phone, profile.email, profile.password)
                refreshAllProfiles()
                onResult(null)
            } catch (e: Exception) {
                refreshAllProfiles()
                onResult(e.localizedMessage)
            }
        }
    }

    fun checkForGooglePlayUpdate(context: android.content.Context, onCheckFinished: ((Boolean) -> Unit)? = null) {
        try {
            val appUpdateManager = com.google.android.play.core.appupdate.AppUpdateManagerFactory.create(context)
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                val isAvailable = appUpdateInfo.updateAvailability() == com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
                val inProgress = appUpdateInfo.updateAvailability() == com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                
                if (isAvailable || inProgress) {
                    playAppUpdateInfo = appUpdateInfo
                    isGooglePlayUpdateAvailable.value = true
                    latestVersionCode.value = appUpdateInfo.availableVersionCode()
                    latestVersionName.value = "PlayStore v${appUpdateInfo.availableVersionCode()}"
                    showUpdateDialog.value = true
                    onCheckFinished?.invoke(true)
                } else {
                    isGooglePlayUpdateAvailable.value = false
                    onCheckFinished?.invoke(false)
                }
            }.addOnFailureListener { e ->
                isGooglePlayUpdateAvailable.value = false
                onCheckFinished?.invoke(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isGooglePlayUpdateAvailable.value = false
            onCheckFinished?.invoke(false)
        }
    }

    fun startGooglePlayUpdate(context: android.content.Context) {
        val activity = context as? android.app.Activity
        val info = playAppUpdateInfo
        if (activity != null && info != null) {
            try {
                val appUpdateManager = com.google.android.play.core.appupdate.AppUpdateManagerFactory.create(context)
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE,
                    activity,
                    1001
                )
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback: Launch official Google Play Store page
        val packageName = context.packageName
        val marketIntent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("market://details?id=$packageName")
        ).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }

        try {
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            val webIntent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
            try {
                context.startActivity(webIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                val updates = com.example.data.network.SupabaseClient.api.getAppUpdates()
                if (updates.isNotEmpty()) {
                    val update = updates.first()
                    processUpdateInfo(update.latestVersionCode, update.latestVersionName, update.releaseDateMs)
                } else {
                    checkLocalUpdateSimulation()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                checkLocalUpdateSimulation()
            }
        }
    }

    private fun checkLocalUpdateSimulation() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
        val testCode = sharedPrefs.getInt("test_latest_version_code", 1)
        val testName = sharedPrefs.getString("test_latest_version_name", "1.0.0") ?: "1.0.0"
        val testReleaseMs = sharedPrefs.getLong("test_release_date_ms", System.currentTimeMillis())
        processUpdateInfo(testCode, testName, testReleaseMs)
    }

    fun processUpdateInfo(latestCode: Int, latestName: String, releaseMs: Long) {
        val currentCode = 1 // Our current version code
        if (latestCode > currentCode) {
            val now = System.currentTimeMillis()
            val fifteenDaysMs = 15L * 24 * 60 * 60 * 1000L
            val expiryMs = releaseMs + fifteenDaysMs
            
            latestVersionCode.value = latestCode
            latestVersionName.value = latestName
            releaseDateMs.value = releaseMs
            
            if (now > expiryMs) {
                isUpdateMandatory.value = true
                showUpdateDialog.value = true
                daysRemaining.value = 0
            } else {
                isUpdateMandatory.value = false
                showUpdateDialog.value = true
                val diffMs = expiryMs - now
                val diffDays = diffMs / (24L * 60 * 60 * 1000L)
                daysRemaining.value = diffDays.coerceAtLeast(0)
            }
        } else {
            showUpdateDialog.value = false
        }
    }

    fun publishNewUpdate(code: Int, name: String, releaseMs: Long, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
                sharedPrefs.edit()
                    .putInt("test_latest_version_code", code)
                    .putString("test_latest_version_name", name)
                    .putLong("test_release_date_ms", releaseMs)
                    .apply()
                
                try {
                    val updateObj = com.example.data.network.SupabaseAppUpdate(
                        latestVersionCode = code,
                        latestVersionName = name,
                        releaseDateMs = releaseMs
                    )
                    com.example.data.network.SupabaseClient.api.insertAppUpdate(listOf(updateObj))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                processUpdateInfo(code, name, releaseMs)
                onResult(null)
            } catch (e: Exception) {
                onResult(e.localizedMessage)
            }
        }
    }

    private fun isPhoneMatchHelper(p1: String, p2: String): Boolean {
        val clean1 = p1.trim().replace("+", "").replace(" ", "").removePrefix("249").removePrefix("0")
        val clean2 = p2.trim().replace("+", "").replace(" ", "").removePrefix("249").removePrefix("0")
        return clean1.isNotBlank() && clean2.isNotBlank() && (clean1 == clean2 || clean1.contains(clean2) || clean2.contains(clean1))
    }
}
