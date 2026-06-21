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

sealed class Screen {
    object Login : Screen()
    object Home : Screen()
    object Categories : Screen()
    object Cart : Screen()
    object Favorites : Screen()
    object History : Screen()
    object Profile : Screen()
    object Admin : Screen()
    object Courier : Screen()
    data class ProductDetail(val productId: Int) : Screen()
}

class MajarahViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = MajarahRepository(
        database.productDao(),
        database.cartDao(),
        database.orderDao(),
        database.profileDao(),
        database.courierDao()
    )

    // Current Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Auth state flows
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val activeProfile = MutableStateFlow<com.example.data.db.ProfileEntity?>(null)

    val isAdmin: StateFlow<Boolean> = combine(activeProfile, _isLoggedIn) { profile, loggedIn ->
        loggedIn && profile?.email?.trim()?.lowercase() == "mawiaosman0@gmail.com"
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

    // Current Search Query State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Current category filtering (empty for all)
    private val _selectedCategory = MutableStateFlow<String>("")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Base Products list
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products for the Home/Catalog screen based on Category and Search Query
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        repository.allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, category ->
        var list = products
        if (category.isNotEmpty()) {
            list = list.filter { it.category == category }
        }
        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true)
            }
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
                    val profiles = database.profileDao().getAllProfiles()
                    val p = profiles.firstOrNull()
                    activeProfile.value = p
                    _isLoggedIn.value = true
                    checkoutName.value = name
                    checkoutPhone.value = phone

                    val cleanP = phone.trim().replace("+", "").replace(" ", "")
                    val matchesCourier = repository.allCouriers.stateIn(viewModelScope).value.any { c ->
                        c.phone.trim().replace("+", "").replace(" ", "") == cleanP || c.phone.trim() == phone.trim()
                    }
                    if (email.trim().lowercase() == "mawiaosman0@gmail.com") {
                        _currentScreen.value = Screen.Admin
                    } else if (matchesCourier) {
                        _currentScreen.value = Screen.Courier
                    } else {
                        _currentScreen.value = Screen.Home
                    }
                }
            } else {
                // Sign In
                val result = repository.loginUserProfile(email, password)
                error = result.second
                if (error == null) {
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
                }
            }
            onSuccess(error)
        }
    }

    fun enterAsGuest() {
        _isLoggedIn.value = false
        activeProfile.value = null
        checkoutName.value = ""
        checkoutPhone.value = ""
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

    fun performLogout() {
        viewModelScope.launch {
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
        // Pre-fill user email with default email
        loginEmail.value = "mawiaosman0@gmail.com"
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
            
            if (profiles.isNotEmpty()) {
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
            val err = repository.placeCompletedOrder(
                orderId = orderId,
                customerName = name,
                customerPhone = phone,
                customerAddress = address,
                items = currentItems
            )
            if (err == null) {
                _checkoutSuccessMessage.value = "تهانينا $name! 🎉\n\nتم إرسال طلبك ومزامنته مع قاعدة بيانات Supabase بنجاح برقم: $orderId بقيمة إجمالية ${formatPrice(calculateTotalSum(currentItems))} جنيه سوداني.\n\nالتوصيل إلى $address خلال 24 ساعة."
            } else {
                _checkoutSuccessMessage.value = "تم حفظ طلبك محلياً برقم: $orderId بقيمة إجمالية ${formatPrice(calculateTotalSum(currentItems))} جنيه سوداني.\n\n⚠️ فشلت المزامنة المباشرة لجدول الطلبات (orders) مع Supabase بسبب:\n\n$err\n\n💡 يرجى التأكد من مطابقة أسماء الأعمدة في قاعدة البيانات SQL الخاصة بك مع الأعمدة المتوقعة وتفعيل صلاحيات الـ RLS."
            }
            
            // Reset form details
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
        return "%,.0f".format(price)
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

    fun updateOrderStatus(orderId: String, status: String, courierName: String = "", courierPhone: String = "", deliveryFee: Double? = null, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val err = repository.updateOrderStatus(orderId, status, courierName, courierPhone, deliveryFee)
            onComplete(err)
        }
    }
}
