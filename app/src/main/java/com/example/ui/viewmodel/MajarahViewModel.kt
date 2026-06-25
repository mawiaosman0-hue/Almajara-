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
        database.sellerDao()
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

    val isAdmin: StateFlow<Boolean> = combine(activeProfile, _isLoggedIn) { profile, loggedIn ->
        loggedIn && profile?.email?.trim()?.lowercase() == "mawiaosman0@gmail.com"
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
            sellers.any { s -> s.email.trim().lowercase() == emailClean }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isCourier: StateFlow<Boolean> = combine(activeProfile, _isLoggedIn, repository.allCouriers) { profile, loggedIn, couriers ->
        if (!loggedIn || profile == null) {
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

    // Past Orders History
    val orderHistory: StateFlow<List<OrderEntity>> = repository.orderHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

        viewModelScope.launch {
            var error: String? = null
            if (isRegisterMode.value) {
                // Register
                error = repository.registerUserProfile(name, phone, email, password)
                if (error == null) {
                    otpVerificationEmail.value = email
                    showOtpVerification.value = true
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
                    // Let's get the latest couriers from database
                    val couriersList = database.courierDao().getCouriersCount() // quick check
                    val matchesCourier = database.courierDao().getAllCouriersSnapshot().any { c ->
                        c.phone.trim().replace("+", "").replace(" ", "") == cleanP || c.phone.trim() == pPhone.trim()
                    }

                    if (email.trim().lowercase() == "mawiaosman0@gmail.com") {
                        _currentScreen.value = Screen.Admin
                    } else if (matchesCourier) {
                        _currentScreen.value = Screen.Courier
                    } else {
                        _currentScreen.value = Screen.Home
                    }
                } else if (error != null && (error.contains("Email not confirmed", ignoreCase = true) || error.contains("تأكيد", ignoreCase = true))) {
                    otpVerificationEmail.value = email
                    showOtpVerification.value = true
                }
            }
            onSuccess(error)
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

    fun submitCheckout() {
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

            val err = repository.placeCompletedOrder(
                orderId = orderId,
                customerName = name,
                customerPhone = phone,
                customerAddress = address,
                items = currentItems,
                discountFactor = discountFactor
            )

            val netTotal = calculateDiscountedSum(currentItems, coupon)
            val couponMessage = if (coupon != null) "\n\n✨ تم تطبيق كود الخصم الكوني: $coupon (بخصم %$discountPercentage)" else ""

            if (err == null) {
                _checkoutSuccessMessage.value = "تهانينا $name! 🎉\n\nتم إرسال طلبك ومزامنته مع قاعدة بيانات Supabase بنجاح برقم: $orderId بقيمة إجمالية ${formatPrice(netTotal)} جنيه سوداني.$couponMessage\n\nالتوصيل إلى $address خلال 24 ساعة."
            } else {
                val translatedErr = translateError(err) ?: ""
                _checkoutSuccessMessage.value = "تم حفظ طلبك محلياً برقم: $orderId بقيمة إجمالية ${formatPrice(netTotal)} جنيه سوداني.$couponMessage\n\n⚠️ فشلت المزامنة المباشرة لجدول الطلبات (orders) مع Supabase بسبب:\n\n$translatedErr\n\n💡 يرجى التأكد من مطابقة أسماء الأعمدة في قاعدة البيانات SQL الخاصة بك مع الأعمدة المتوقعة وتفعيل صلاحيات الـ RLS."
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
            onComplete(err)
        }
    }
}
