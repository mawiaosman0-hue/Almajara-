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
    data class ProductDetail(val productId: Int) : Screen()
}

class MajarahViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
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
        database.adminManagerDao()
    )

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
            profile.name.trim() == "معاوية عثمان أحمد ياسين"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isAdmin: StateFlow<Boolean> = combine(isGeneralAdmin, activeProfile, _isLoggedIn, allAdminManagers) { isGen, profile, loggedIn, managers ->
        loggedIn && profile != null && (
            isGen || managers.any { m -> 
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
        } else if (profile.email.trim().lowercase() == "mawiaosman0@gmail.com") {
            false
        } else {
            val emailClean = profile.email.trim().lowercase()
            sellers.any { s -> s.email.trim().lowercase() == emailClean }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isCourier: StateFlow<Boolean> = combine(activeProfile, _isLoggedIn, repository.allCouriers) { profile, loggedIn, couriers ->
        if (!loggedIn || profile == null) {
            false
        } else if (profile.email.trim().lowercase() == "mawiaosman0@gmail.com") {
            false
        } else {
            val phoneClean = profile.phone.trim().replace("+", "").replace(" ", "")
            couriers.any { c ->
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
        if (uppercaseCode == "COSMIC10" || uppercaseCode == "MAJARAH15" || uppercaseCode == "SUDAN50") {
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
        return when (coupon) {
            "COSMIC10" -> 10
            "MAJARAH15" -> 15
            "SUDAN50" -> 50
            else -> 0
        }
    }

    fun calculateDiscountedSum(items: List<CartItemWithProduct>, coupon: String?): Double {
        val total = calculateTotalSum(items)
        val discount = getCouponDiscountPercentage(coupon)
        return total * (1.0 - discount / 100.0)
    }

    // Success dialog state after order placement
    private val _checkoutSuccessMessage = MutableStateFlow<String?>(null)
    val checkoutSuccessMessage: StateFlow<String?> = _checkoutSuccessMessage.asStateFlow()

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
                    error = repository.registerUserProfile(name, phone, email, password)
                    
                    val isLocalSuccess = error == null || 
                                         error.contains("تم الحفظ محلياً") || 
                                         error.contains("profiles") || 
                                         error.contains("الرفع للسيرفر") ||
                                         error.contains("already registered", ignoreCase = true) ||
                                         error.contains("already exists", ignoreCase = true)

                    if (isLocalSuccess) {
                        // Create corresponding user type record if not customer
                        val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
                        if (role == "seller") {
                            repository.insertSeller(
                                com.example.data.db.SellerEntity(
                                    name = name,
                                    email = email,
                                    phone = phone,
                                    classification = "تاجر ذهبي ⭐",
                                    commissionRate = 0.10
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
                            val isPharmacistUser = role == "pharmacist" || sharedPrefs.getString("user_role_${p.email.trim().lowercase()}", "") == "pharmacist"
                            val isAdminUser = p.email.trim().lowercase() == "mawiaosman0@gmail.com" || role == "admin" || sharedPrefs.getString("user_role_${p.email.trim().lowercase()}", "") == "admin"

                            if (isAdminUser) {
                                _currentScreen.value = Screen.Admin
                            } else if (role == "courier" || matchesCourier) {
                                _currentScreen.value = Screen.Courier
                            } else if (role == "seller" || matchesSeller) {
                                _currentScreen.value = Screen.Seller
                            } else if (isPharmacistUser) {
                                _selectedCategory.value = "pharmacy"
                                _currentScreen.value = Screen.Home
                            } else {
                                _currentScreen.value = Screen.Home
                            }

                            // Clear register state
                            isRegisterMode.value = false
                            showOtpVerification.value = false
                            error = null // Clear error to represent success
                        } else {
                            // Fallback to manual local user profile construction
                            val fallbackProfile = database.profileDao().getAllProfiles().firstOrNull() ?: com.example.data.db.ProfileEntity(
                                id = java.util.UUID.randomUUID().toString(),
                                name = name,
                                phone = phone,
                                email = email,
                                password = password
                            )
                            database.profileDao().clearProfiles()
                            database.profileDao().insertProfile(fallbackProfile)

                            sharedPrefs.edit().putBoolean("is_logged_in_state", true).apply()

                            activeProfile.value = fallbackProfile
                            _isLoggedIn.value = true
                            checkoutName.value = fallbackProfile.name
                            checkoutPhone.value = fallbackProfile.phone

                            val isPharmacistUser = role == "pharmacist" || sharedPrefs.getString("user_role_${fallbackProfile.email.trim().lowercase()}", "") == "pharmacist"
                            val isAdminUser = fallbackProfile.email.trim().lowercase() == "mawiaosman0@gmail.com" || role == "admin" || sharedPrefs.getString("user_role_${fallbackProfile.email.trim().lowercase()}", "") == "admin"

                            if (isAdminUser) {
                                _currentScreen.value = Screen.Admin
                            } else if (role == "courier") {
                                _currentScreen.value = Screen.Courier
                            } else if (role == "seller") {
                                _currentScreen.value = Screen.Seller
                            } else if (isPharmacistUser) {
                                _selectedCategory.value = "pharmacy"
                                _currentScreen.value = Screen.Home
                            } else {
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
                            s.email.trim().lowercase() == p?.email?.trim()?.lowercase()
                        }
                        val isPharmacistUser = sharedPrefs.getString("user_role_${p?.email?.trim()?.lowercase()}", "") == "pharmacist"
                        val isAdminUser = p?.email?.trim()?.lowercase() == "mawiaosman0@gmail.com" || sharedPrefs.getString("user_role_${p?.email?.trim()?.lowercase()}", "") == "admin"

                        if (isAdminUser) {
                            _currentScreen.value = Screen.Admin
                        } else if (matchesCourier) {
                            _currentScreen.value = Screen.Courier
                        } else if (matchesSeller) {
                            _currentScreen.value = Screen.Seller
                        } else if (isPharmacistUser) {
                            _selectedCategory.value = "pharmacy"
                            _currentScreen.value = Screen.Home
                        } else {
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

                val profiles = database.profileDao().getAllProfiles()
                var p = profiles.firstOrNull { it.email.trim().lowercase() == email.lowercase() }
                if (p == null) {
                    p = com.example.data.db.ProfileEntity(
                        id = java.util.UUID.randomUUID().toString(),
                        name = if (name.isBlank()) "عميل المجرة ✨" else name,
                        phone = phone,
                        email = email,
                        password = password,
                        createdAt = System.currentTimeMillis()
                    )
                    database.profileDao().insertProfile(p)
                }
                activeProfile.value = p
                _isLoggedIn.value = true
                checkoutName.value = p.name
                checkoutPhone.value = p.phone

                showOtpVerification.value = false
                val cleanP = p.phone.trim().replace("+", "").replace(" ", "")
                val matchesCourier = repository.allCouriers.stateIn(viewModelScope).value.any { c ->
                    c.phone.trim().replace("+", "").replace(" ", "") == cleanP || c.phone.trim() == p.phone.trim()
                }
                if (email.trim().lowercase() == "mawiaosman0@gmail.com") {
                    _currentScreen.value = Screen.Admin
                } else if (matchesCourier) {
                    _currentScreen.value = Screen.Courier
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

            database.profileDao().clearProfiles()
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
            val error = repository.updateUserProfile(name, phone, email)
            if (error == null) {
                val profiles = database.profileDao().getAllProfiles()
                if (profiles.isNotEmpty()) {
                    activeProfile.value = profiles.first()
                }
                checkoutName.value = name
                checkoutPhone.value = phone
            }
            onResult(error)
        }
    }

    init {
        // Initialize the app with Room products seed and local session loading
        viewModelScope.launch {
            repository.checkAndPrepopulateProducts()
            repository.syncRemoteOrdersToLocal()
            
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
            
            if (isUserLoggedIn && profiles.isNotEmpty()) {
                val p = profiles.first()
                activeProfile.value = p
                checkoutName.value = p.name
                checkoutPhone.value = p.phone
                _isLoggedIn.value = true

                val cleanP = p.phone.trim().replace("+", "").replace(" ", "")
                val matchesCourier = database.courierDao().getAllCouriersSnapshot().any { c ->
                    c.phone.trim().replace("+", "").replace(" ", "") == cleanP || c.phone.trim() == p.phone.trim()
                }

                if (p.email.trim().lowercase() == "mawiaosman0@gmail.com") {
                    _currentScreen.value = Screen.Admin
                } else if (matchesCourier) {
                    _currentScreen.value = Screen.Courier
                } else {
                    _currentScreen.value = Screen.Home
                }
            } else {
                _currentScreen.value = Screen.Login
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
        if (isSeller.value) return
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

    fun submitCheckout(paymentMethod: String = "cash", transactionId: String = "") {
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
                paymentMethod = methodLabel
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
                onComplete(null)
            } catch (e: Exception) {
                onComplete(e.localizedMessage ?: "حدث خطأ غير معروف")
            }
        }
    }

    fun removeAdminManager(id: Int, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.adminManagerDao.deleteAdminManager(id)
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
                val error = repository.registerUserProfile(name, phone, email, password)
                
                // 2. Set admin role in prefs
                val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("user_role_${email.trim().lowercase()}", "admin").apply()
                sharedPrefs.edit().putBoolean("is_logged_in_state", true).apply()
                
                // 3. Auto login
                val loginResult = repository.loginUserProfile(email, password)
                val p = loginResult.first ?: com.example.data.db.ProfileEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    phone = phone,
                    email = email,
                    password = password
                )
                
                database.profileDao().clearProfiles()
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

    fun updateOrderStatus(orderId: String, status: String, courierName: String = "", courierPhone: String = "", deliveryFee: Double? = null, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val err = repository.updateOrderStatus(orderId, status, courierName, courierPhone, deliveryFee)
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

    val isPharmacist: StateFlow<Boolean> = combine(activeProfile, _isLoggedIn, allPharmacies) { profile, loggedIn, pharmacies ->
        if (!loggedIn || profile == null) {
            false
        } else if (profile.email.trim().lowercase() == "mawiaosman0@gmail.com") {
            false
        } else {
            val emailClean = profile.email.trim().lowercase()
            val sharedPrefs = getApplication<Application>().getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
            val isPharmPref = sharedPrefs.getString("user_role_${profile.email}", "") == "pharmacist"
            isPharmPref || pharmacies.any { p -> p.pharmacistEmail.trim().lowercase() == emailClean }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun getPharmacyByPharmacistEmail(email: String, onResult: (com.example.data.db.PharmacyEntity?) -> Unit) {
        viewModelScope.launch {
            val res = repository.getPharmacyByPharmacistEmail(email)
            onResult(res)
        }
    }

    fun addPharmacy(name: String, doctorName: String, phone: String, location: String, pharmacistEmail: String, onComplete: (String?) -> Unit) {
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
                    isApproved = false
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

    fun addPharmacyOrder(pharmacyId: Int, customerName: String, customerPhone: String, customerEmail: String, prescriptionBase64: String, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                val ord = com.example.data.db.PharmacyOrderEntity(
                    pharmacyId = pharmacyId,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerEmail = customerEmail,
                    prescriptionImageBase64 = prescriptionBase64,
                    status = "بانتظار الصيدلي"
                )
                repository.insertPharmacyOrder(ord)
            } catch (e: Exception) {
                error = e.localizedMessage ?: "حدث خطأ أثناء تقديم الروشتة"
            } finally {
                isGlobalLoading.value = false
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

    fun adminApprovePharmacyOrder(orderId: Int, courierName: String, courierPhone: String, deliveryFee: Double, onComplete: (String?) -> Unit) {
        isGlobalLoading.value = true
        viewModelScope.launch {
            var error: String? = null
            try {
                repository.assignPharmacyOrderCourierAndDeliveryFee(orderId, "تم تحديد السعر النهائي", courierName, courierPhone, deliveryFee)
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
}
