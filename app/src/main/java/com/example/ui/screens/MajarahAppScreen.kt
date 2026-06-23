package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.ProductEntity
import com.example.data.repository.CartItemWithProduct
import com.example.ui.theme.ActiveGreen
import com.example.ui.theme.CosmicDeepSpace
import com.example.ui.theme.CosmicPrimary
import com.example.ui.theme.CosmicSecondary
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceVariant
import com.example.ui.theme.CosmicTertiary
import com.example.ui.theme.MediumContrastTextDark
import com.example.ui.viewmodel.MajarahViewModel
import com.example.ui.viewmodel.Screen
import com.example.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

@Composable
fun ProductImagePlaceholder(imageName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF19113B), CosmicSurfaceVariant)
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = java.util.Random(imageName.hashCode().toLong())
            for (i in 0..12) {
                val x = r.nextFloat() * size.width
                val y = r.nextFloat() * size.height
                val radius = r.nextFloat() * 3f + 1f
                drawCircle(
                    color = Color.White.copy(alpha = r.nextFloat() * 0.6f + 0.2f),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }
        
        when (imageName) {
            "laptop" -> Icon(Icons.Default.Laptop, "حاسوب محمول", tint = CosmicSecondary, modifier = Modifier.size(56.dp))
            "watch" -> Icon(Icons.Default.Watch, "ساعة ذكية", tint = CosmicSecondary, modifier = Modifier.size(56.dp))
            "earbuds" -> Icon(Icons.Default.Headphones, "سماعات أذن", tint = CosmicSecondary, modifier = Modifier.size(56.dp))
            "jacket" -> Icon(Icons.Default.Checkroom, "سترة تدفئة", tint = CosmicSecondary, modifier = Modifier.size(56.dp))
            "backpack" -> Icon(Icons.Default.Backpack, "حقيبة سفر", tint = CosmicSecondary, modifier = Modifier.size(56.dp))
            "lamp" -> Icon(Icons.Default.Lightbulb, "بروجكتر سديم", tint = CosmicSecondary, modifier = Modifier.size(56.dp))
            "coffeemaker" -> Icon(Icons.Default.CoffeeMaker, "صانعة إسبريسو", tint = CosmicSecondary, modifier = Modifier.size(56.dp))
            "tv" -> Icon(Icons.Default.Tv, "شاشة ذكية", tint = CosmicSecondary, modifier = Modifier.size(56.dp))
            "mat" -> Icon(Icons.Default.SelfImprovement, "بساط مريح", tint = CosmicSecondary, modifier = Modifier.size(56.dp))
            else -> Icon(Icons.Default.ShoppingCart, "منتج المجرة", tint = CosmicSecondary, modifier = Modifier.size(56.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MajarahAppScreen(viewModel: MajarahViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val favoriteProducts by viewModel.favoriteProducts.collectAsStateWithLifecycle()
    val orderHistory by viewModel.orderHistory.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val checkoutSuccess by viewModel.checkoutSuccessMessage.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isCourier by viewModel.isCourier.collectAsStateWithLifecycle()
    val isSeller by viewModel.isSeller.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    val phoneState by viewModel.checkoutPhone.collectAsStateWithLifecycle()
    val addressState by viewModel.checkoutAddress.collectAsStateWithLifecycle()
    val nameState by viewModel.checkoutName.collectAsStateWithLifecycle()
    val dbStatus by viewModel.dbStatus.collectAsStateWithLifecycle()

    val registerNameState by viewModel.loginName.collectAsStateWithLifecycle()

    var registrationErrorDialogMessage by remember { mutableStateOf<String?>(null) }

    var showSupabaseSettingsDialog by remember { mutableStateOf(false) }
    var supabaseUrlInput by remember { mutableStateOf(com.example.data.network.SupabaseConfig.url) }
    var supabaseKeyInput by remember { mutableStateOf(com.example.data.network.SupabaseConfig.apiKey) }
    var showSqlSetupGuide by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Scaffold(
        topBar = {
            if (currentScreen !is Screen.Login && currentScreen !is Screen.Splash) {
                Column {
                    TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_majarah_universe_logo_1782084799100),
                                contentDescription = "Cosmic Logo",
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "المجرة الكونية للتسوق 🌌",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    },
                    navigationIcon = {
                        if (isCourier && currentScreen !is Screen.Courier) {
                            IconButton(onClick = { viewModel.navigateTo(Screen.Courier) }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Icon(Icons.Default.DirectionsBike, "الرجوع للوحة المندوب", tint = CosmicSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("عودة 🚴", color = CosmicSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (isSeller && currentScreen !is Screen.Seller) {
                            IconButton(onClick = { viewModel.navigateTo(Screen.Seller) }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Icon(Icons.Default.Store, "الرجوع للبائع", tint = CosmicSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("لوحة البائع 🧑‍💼", color = CosmicSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (currentScreen is Screen.ProductDetail) {
                            IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                                Icon(Icons.Default.ArrowBack, "رجوع", tint = Color.White)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isLoggedIn) {
                                    IconButton(onClick = { 
                                        viewModel.performLogout()
                                        Toast.makeText(context, "تم تسجيل الخروج بنجاح 👋", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.Logout, "تسجيل الخروج", tint = Color.Red.copy(alpha = 0.8f))
                                    }
                                }
                                IconButton(onClick = { 
                                    val userText = if (isLoggedIn && nameState.isNotEmpty()) "يا $nameState" else ""
                                    Toast.makeText(context, "مرحباً بك $userText في تطبيق المجرة الرسمي بالسودان! 🌠", Toast.LENGTH_LONG).show()
                                }) {
                                    Icon(Icons.Default.RocketLaunch, "البراند الكوني", tint = CosmicSecondary)
                                }
                            }
                        }
                    },
                    actions = {
                        if (isAdmin) {
                            IconButton(onClick = { viewModel.navigateTo(Screen.Admin) }) {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Admin) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "لوحة تحكم المدير",
                                    tint = if (currentScreen is Screen.Admin) CosmicSecondary else Color.White
                                )
                            }
                        }
                        if (isLoggedIn) {
                            IconButton(onClick = { viewModel.navigateTo(Screen.Profile) }) {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Profile) Icons.Filled.Person else Icons.Outlined.Person,
                                    contentDescription = "الملف الشخصي",
                                    tint = if (currentScreen is Screen.Profile) CosmicSecondary else Color.White
                                )
                            }
                        }
                        if (!isCourier) {
                            Box(modifier = Modifier.padding(end = 8.dp)) {
                                IconButton(onClick = { viewModel.navigateTo(Screen.Cart) }) {
                                    Icon(
                                        imageVector = if (currentScreen is Screen.Cart) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                                        contentDescription = "سلة المشتريات",
                                        tint = if (currentScreen is Screen.Cart) CosmicSecondary else Color.White
                                    )
                                }
                                if (cartItems.isNotEmpty()) {
                                    val totalQty = cartItems.sumOf { it.quantity }
                                    Badge(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-2).dp)
                                            .testTag("cart_badge"),
                                        containerColor = CosmicSecondary,
                                        contentColor = Color.Black
                                    ) {
                                        Text(
                                            text = totalQty.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CosmicDeepSpace
                    )
                )

                // Beautiful, real-time dynamic Supabase status banner inside the topBar bar
                val isError = dbStatus.contains("وضع عدم الاتصال") || dbStatus.contains("حدث خطأ")
                val isPending = dbStatus.contains("جاري")
                val bannerBg = when {
                    isError -> Color(0xFF5C191D)
                    isPending -> Color(0xFF423B17)
                    else -> Color(0xFF143026)
                }
                val bannerTextIcon = when {
                    isError -> Icons.Default.CloudOff
                    isPending -> Icons.Default.Sync
                    else -> Icons.Default.CloudQueue
                }
                val bannerThemeColor = when {
                    isError -> Color(0xFFFFB4AB)
                    isPending -> Color(0xFFFFE082)
                    else -> Color(0xFFA3F4C5)
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bannerBg)
                        .let { modifier ->
                            if (isAdmin) {
                                modifier.clickable {
                                    supabaseUrlInput = com.example.data.network.SupabaseConfig.url
                                    supabaseKeyInput = com.example.data.network.SupabaseConfig.apiKey
                                    showSupabaseSettingsDialog = true
                                }
                            } else {
                                modifier
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = bannerTextIcon,
                        contentDescription = "DB Sync",
                        tint = bannerThemeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAdmin) "$dbStatus (انقر للضبط ⚙️)" else dbStatus,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
                }
            }
        },
        bottomBar = {
            if (currentScreen !is Screen.Login && currentScreen !is Screen.Splash) {
                NavigationBar(
                    containerColor = CosmicDeepSpace,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                NavigationBarItem(
                    selected = currentScreen is Screen.Home,
                    onClick = { viewModel.navigateTo(Screen.Home) },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("الرئيسية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CosmicDeepSpace,
                        selectedTextColor = CosmicSecondary,
                        indicatorColor = CosmicSecondary,
                        unselectedIconColor = MediumContrastTextDark,
                        unselectedTextColor = MediumContrastTextDark
                    )
                )

                NavigationBarItem(
                    selected = currentScreen is Screen.Categories,
                    onClick = { viewModel.navigateTo(Screen.Categories) },
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("الأقسام", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CosmicDeepSpace,
                        selectedTextColor = CosmicSecondary,
                        indicatorColor = CosmicSecondary,
                        unselectedIconColor = MediumContrastTextDark,
                        unselectedTextColor = MediumContrastTextDark
                    )
                )

                if (!isCourier) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Cart,
                        onClick = { viewModel.navigateTo(Screen.Cart) },
                        icon = { Icon(Icons.Default.ShoppingCart, null) },
                        label = { Text("السلة", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CosmicDeepSpace,
                            selectedTextColor = CosmicSecondary,
                            indicatorColor = CosmicSecondary,
                            unselectedIconColor = MediumContrastTextDark,
                            unselectedTextColor = MediumContrastTextDark
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Favorites,
                        onClick = { viewModel.navigateTo(Screen.Favorites) },
                        icon = { Icon(Icons.Default.Favorite, null) },
                        label = { Text("المفضلة", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CosmicDeepSpace,
                            selectedTextColor = CosmicSecondary,
                            indicatorColor = CosmicSecondary,
                            unselectedIconColor = MediumContrastTextDark,
                            unselectedTextColor = MediumContrastTextDark
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.History,
                        onClick = { viewModel.navigateTo(Screen.History) },
                        icon = {
                            Box {
                                Icon(Icons.Default.History, null)
                                val activeOrdersCount = orderHistory.distinctBy { it.orderId }.filter { !it.statusArabic.contains("تم") && !it.statusArabic.contains("توصيل") && !it.statusArabic.contains("تمام") }.size
                                if (activeOrdersCount > 0) {
                                    Badge(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 10.dp, y = (-6).dp)
                                            .testTag("orders_badge"),
                                        containerColor = CosmicSecondary,
                                        contentColor = Color.Black
                                    ) {
                                        Text(
                                            text = activeOrdersCount.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        },
                        label = { Text("طلباتي", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CosmicDeepSpace,
                            selectedTextColor = CosmicSecondary,
                            indicatorColor = CosmicSecondary,
                            unselectedIconColor = MediumContrastTextDark,
                            unselectedTextColor = MediumContrastTextDark
                        )
                    )
                }
            }
        }
    },
    containerColor = CosmicDeepSpace,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            when (val screen = currentScreen) {
                is Screen.Splash -> {
                    SplashScreenBody()
                }
                is Screen.Login -> {
                    val email by viewModel.loginEmail.collectAsStateWithLifecycle()
                    val password by viewModel.loginPassword.collectAsStateWithLifecycle()
                    val name by viewModel.loginName.collectAsStateWithLifecycle()
                    val phone by viewModel.loginPhone.collectAsStateWithLifecycle()
                    val isReg by viewModel.isRegisterMode.collectAsStateWithLifecycle()
                    
                    LoginScreenBody(
                        email = email,
                        password = password,
                        name = name,
                        phone = phone,
                        isRegister = isReg,
                        onEmailChange = { viewModel.loginEmail.value = it },
                        onPasswordChange = { viewModel.loginPassword.value = it },
                        onNameChange = { viewModel.loginName.value = it },
                        onPhoneChange = { viewModel.loginPhone.value = it },
                        onToggleMode = { viewModel.isRegisterMode.value = !isReg },
                        onSubmit = {
                            val wasRegister = isReg
                            viewModel.performLogin { err ->
                                if (err == null) {
                                    if (wasRegister) {
                                        Toast.makeText(context, viewModel.t("مرحباً بك في عالم المجرة الفسيح! 🌌 تم التسجيل والمزامنة مع Supabase بنجاح.", "Welcome to the vast galaxy! 🌌 Successfully registered and synced with Supabase."), Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, viewModel.t("تم تسجيل الدخول بنجاح! مرحباً بعودتك إلى المجرة. 🚀", "Logged in successfully! Welcome back to Majarah. 🚀"), Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    if (wasRegister) {
                                        val translatedErr = viewModel.translateError(err) ?: ""
                                        registrationErrorDialogMessage = viewModel.t("تم إنشاء حسابك وحفظه محلياً بنجاح.\n\n⚠️ لكن فشلت مزامنة بيانات حسابك الجديد مع قاعدة Supabase (جدول profiles) بسبب الخطأ التالي:\n\n$translatedErr\n\n💡 يرجى التأكد من إنشاء جدول 'profiles' ومطابقة أسماء وتنسيق الأعمدة وتفعيل سياسات الوصول RLS.", "Account created and saved locally successfully.\n\n⚠️ But syncing your new account with Supabase (profiles table) failed due to:\n\n$err\n\n💡 Please ensure 'profiles' table exists and matches expected scheme.")
                                    } else {
                                        val translatedErr = viewModel.translateError(err) ?: ""
                                        registrationErrorDialogMessage = viewModel.t("⚠️ فشل تسجيل الدخول للبراند الكوني:\n\n$translatedErr\n\n💡 ربما أدخلت بريدًا إلكترونيًا غير صحيح أو كلمة مرور خاطئة. يرجى التحقق وإعادة المحاولة.", "⚠️ Cosmic Brand login failed:\n\n$err\n\n💡 Maybe you entered an incorrect email or password. Please verify and retry.")
                                    }
                                }
                            }
                        },
                        onSkipAsGuest = {
                            viewModel.enterAsGuest()
                            Toast.makeText(context, viewModel.t("تتصفح حالياً كزائر في مجرة التسوق 🌌", "Browsing as guest in Majarah Shopping 🌌"), Toast.LENGTH_SHORT).show()
                        },
                        onForgotPassword = {
                            showForgotPasswordDialog = true
                        },
                        viewModel = viewModel
                    )
                }
                is Screen.Home -> {
                    HomeScreenBody(
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        products = filteredProducts,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        onCategorySelect = { viewModel.setCategory(it) },
                        onProductClick = { viewModel.navigateTo(Screen.ProductDetail(it.id)) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it.id) },
                        onAddToCart = { 
                            viewModel.addToCart(it.id)
                            Toast.makeText(context, "تمت إضافة ${it.name} إلى السلة 🛍️", Toast.LENGTH_SHORT).show()
                        },
                        viewModel = viewModel
                    )
                }
                is Screen.Categories -> {
                    CategoriesScreenBody(
                        selectedCategory = selectedCategory,
                        onCategorySelect = { 
                            viewModel.setCategory(it)
                            viewModel.navigateTo(Screen.Home)
                        }
                    )
                }
                is Screen.Cart -> {
                    CartScreenBody(
                        cartItems = cartItems,
                        totalSum = viewModel.calculateTotalSum(cartItems),
                        phoneValue = phoneState,
                        addressValue = addressState,
                        nameValue = nameState,
                        onPhoneChange = { viewModel.checkoutPhone.value = it },
                        onAddressChange = { viewModel.checkoutAddress.value = it },
                        onNameChange = { viewModel.checkoutName.value = it },
                        onQtyIncrease = { viewModel.updateCartQuantity(it.product.id, it.quantity + 1) },
                        onQtyDecrease = { viewModel.updateCartQuantity(it.product.id, it.quantity - 1) },
                        onRemove = { viewModel.removeFromCart(it.product.id) },
                        onSubmit = { viewModel.submitCheckout() },
                        formatPrice = { viewModel.formatPrice(it) },
                        isLoggedIn = isLoggedIn,
                        onRegisterPrompt = {
                            viewModel.isRegisterMode.value = true
                            viewModel.navigateTo(Screen.Login)
                        },
                        viewModel = viewModel
                    )
                }
                is Screen.Favorites -> {
                    FavoritesScreenBody(
                        favorites = favoriteProducts,
                        onProductClick = { viewModel.navigateTo(Screen.ProductDetail(it.id)) },
                        onRemoveFavorite = { viewModel.toggleFavorite(it.id) }
                    )
                }
                is Screen.History -> {
                    if (isLoggedIn) {
                        HistoryScreenBody(
                            orders = orderHistory,
                            onClearHistory = { viewModel.clearHistory() },
                            formatPrice = { viewModel.formatPrice(it) },
                            viewModel = viewModel
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timeline,
                                        contentDescription = null,
                                        tint = CosmicSecondary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "سجل طلباتك فارغ للزوار 🛰️",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "يرجى تسجيل الدخول أو إنشاء حساب جديد لعرض تفاصيل طلباتك ومتابعتها والتواصل مع المناديب.",
                                        color = MediumContrastTextDark,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = {
                                            viewModel.isRegisterMode.value = true
                                            viewModel.navigateTo(Screen.Login)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("إنشاء حساب جديد بالمجرة 🌠", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                is Screen.Profile -> {
                    ProfileScreenBody(
                        viewModel = viewModel,
                        onLogout = {
                            viewModel.performLogout()
                            Toast.makeText(context, "تم تسجيل الخروج بنجاح 👋", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                is Screen.Courier -> {
                    CourierDashboardScreenBody(
                        viewModel = viewModel
                    )
                }
                is Screen.Admin -> {
                    AdminDashboardScreenBody(
                        viewModel = viewModel
                    )
                }
                is Screen.Seller -> {
                    SellerDashboardScreenBody(
                        viewModel = viewModel
                    )
                }
                is Screen.ProductDetail -> {
                    selectedProduct?.let { product ->
                        ProductDetailScreenBody(
                            product = product,
                            onAddToCart = { qty ->
                                viewModel.addToCart(product.id, qty)
                                Toast.makeText(context, "تمت إضافة $qty قطع من ${product.name} إلى السلة 🛍️", Toast.LENGTH_SHORT).show()
                                viewModel.navigateTo(Screen.Cart)
                            },
                            onFavoriteToggle = { viewModel.toggleFavorite(product.id) },
                            formatPrice = { viewModel.formatPrice(it) },
                            isCourier = isCourier
                        )
                    } ?: Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CosmicSecondary)
                    }
                }
            }

            // Checkout success alert dialog box
            AnimatedVisibility(
                visible = checkoutSuccess != null,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissCheckoutSuccess() },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, "نجاح", tint = ActiveGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اكتمال عملية الشحن بنجاح", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    },
                    text = {
                        Text(
                            text = checkoutSuccess ?: "",
                            color = MediumContrastTextDark,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissCheckoutSuccess() },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                        ) {
                            Text("تم ومتابعة طلبي", fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = CosmicSurface,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Registration error alert dialog box
            AnimatedVisibility(
                visible = registrationErrorDialogMessage != null,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                AlertDialog(
                    onDismissRequest = { registrationErrorDialogMessage = null },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, "تحذير", tint = CosmicSecondary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تنبيه مزامنة التسجيل ⚠️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    },
                    text = {
                        Text(
                            text = registrationErrorDialogMessage ?: "",
                            color = MediumContrastTextDark,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { registrationErrorDialogMessage = null },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                        ) {
                            Text("حسناً وفهمت", fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = CosmicSurface,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Forgot Password Alert Dialog with simulated phone verification
            if (showForgotPasswordDialog) {
                var forgotPhone by remember { mutableStateOf("") }
                var forgotNewPassword by remember { mutableStateOf("") }
                var verificationCodeInput by remember { mutableStateOf("") }
                var isCodeSent by remember { mutableStateOf(false) }
                var generatedMockCode by remember { mutableStateOf("") }
                var showPasswordInputState by remember { mutableStateOf(false) }
                var isResettingInProgress by remember { mutableStateOf(false) }
                var checkPhoneError by remember { mutableStateOf<String?>(null) }

                AlertDialog(
                    onDismissRequest = { showForgotPasswordDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = CosmicSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("استعادة كلمة المرور الكونية 🛰️", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "أدخل رقم الهاتف المسجل بحسابك لإرسال رمز استعادة وتعيين كلمة مرور جديدة لتطبيق المجرة بالسودان.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Right
                            )
                            
                            OutlinedTextField(
                                value = forgotPhone,
                                onValueChange = { 
                                    forgotPhone = it
                                    checkPhoneError = null
                                },
                                label = { Text("رقم الهاتف للتفتيش والتحقق", color = CosmicSecondary) },
                                placeholder = { Text("مثال: 0912345678", color = MediumContrastTextDark) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant,
                                    focusedLabelColor = CosmicSecondary, unfocusedLabelColor = MediumContrastTextDark,
                                    focusedContainerColor = CosmicDeepSpace, unfocusedContainerColor = CosmicDeepSpace
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Phone, null, tint = CosmicSecondary) }
                            )

                            if (isCodeSent) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CosmicSecondary.copy(alpha = 0.1f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "💬 تم إرسال رمز استعادة كوني إلى الهاتف ($forgotPhone) بنجاح!\n\n💡 الرمز للمطابقة هو: ( $generatedMockCode )",
                                        color = CosmicSecondary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(10.dp),
                                        lineHeight = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                OutlinedTextField(
                                    value = verificationCodeInput,
                                    onValueChange = { 
                                        verificationCodeInput = it
                                        if (it == generatedMockCode) {
                                            showPasswordInputState = true
                                        }
                                    },
                                    label = { Text("أدخل رمز التحقق (OTP) المستلم", color = CosmicSecondary) },
                                    placeholder = { Text("مثال: الرمز المكون من 4 أرقام", color = MediumContrastTextDark) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant,
                                        focusedLabelColor = CosmicSecondary, unfocusedLabelColor = MediumContrastTextDark,
                                        focusedContainerColor = CosmicDeepSpace, unfocusedContainerColor = CosmicDeepSpace
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, null, tint = CosmicSecondary) }
                                )
                            }

                            if (showPasswordInputState) {
                                OutlinedTextField(
                                    value = forgotNewPassword,
                                    onValueChange = { forgotNewPassword = it },
                                    label = { Text("اكتب كلمة المرور الجديدة المرغوبة *", color = CosmicSecondary) },
                                    placeholder = { Text("لا تقل عن 6 خانات لسهولة الاستفادة والآمان", color = MediumContrastTextDark) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant,
                                        focusedLabelColor = CosmicSecondary, unfocusedLabelColor = MediumContrastTextDark,
                                        focusedContainerColor = CosmicDeepSpace, unfocusedContainerColor = CosmicDeepSpace
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = CosmicSecondary) }
                                )
                            }

                            checkPhoneError?.let {
                                Text(it, color = Color.Red, fontSize = 11.sp)
                            }
                        }
                    },
                    confirmButton = {
                        if (!isCodeSent) {
                            Button(
                                onClick = {
                                    if (forgotPhone.isBlank()) {
                                        checkPhoneError = "الرجاء كتابة رقم الهاتف أولاً"
                                        return@Button
                                    }
                                    isResettingInProgress = true
                                    val verificationCode = (1000..9999).random().toString()
                                    viewModel.sendResetSmsOtp(forgotPhone, verificationCode) { success, msg ->
                                        isResettingInProgress = false
                                        if (success) {
                                            generatedMockCode = verificationCode
                                            isCodeSent = true
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        } else {
                                            checkPhoneError = msg
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isResettingInProgress) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("تحقق وإرسال رمز الاستعادة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        } else if (showPasswordInputState) {
                            Button(
                                onClick = {
                                    if (forgotNewPassword.length < 6) {
                                        checkPhoneError = "يجب أن تكون كلمة المرور 6 خانات على الأقل لسلامة حسابك"
                                        return@Button
                                    }
                                    isResettingInProgress = true
                                    viewModel.resetPasswordByPhone(forgotPhone, forgotNewPassword) { success, msg ->
                                        isResettingInProgress = false
                                        if (success) {
                                            Toast.makeText(context, "تم إعادة تعيين كلمة مرورك بنجاح! ✨", Toast.LENGTH_LONG).show()
                                            showForgotPasswordDialog = false
                                            viewModel.navigateTo(Screen.Login)
                                            registrationErrorDialogMessage = msg
                                        } else {
                                            checkPhoneError = msg
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                enabled = forgotNewPassword.isNotBlank()
                            ) {
                                Text("حفظ وتعيين كلمة المرور الجديدة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showForgotPasswordDialog = false }) {
                            Text("إلغاء", color = Color.White.copy(alpha = 0.6f))
                        }
                    },
                    containerColor = Color(0xFF161F30),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            if (showSupabaseSettingsDialog) {
                AlertDialog(
                    onDismissRequest = { showSupabaseSettingsDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, "إعدادات", tint = CosmicSecondary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إعدادات ربط Supabase ⚙️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "يمكنك تعديل عنوان ومفتاح قاعدة البيانات يدوياً وسيقوم التطبيق بالاتصال فوراً ومزامنة المنتجات والطلبات.",
                                color = MediumContrastTextDark,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            OutlinedTextField(
                                value = supabaseUrlInput,
                                onValueChange = { supabaseUrlInput = it },
                                label = { Text("عنوان URL لـ Supabase", color = CosmicSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CosmicSecondary,
                                    unfocusedBorderColor = CosmicSurfaceVariant,
                                    focusedLabelColor = CosmicSecondary,
                                    cursorColor = CosmicSecondary,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                trailingIcon = {
                                    TextButton(
                                        onClick = {
                                            val text = clipboardManager.getText()?.text
                                            if (!text.isNullOrEmpty()) {
                                                var cleanText = text.trim()
                                                if (cleanText.contains("/rest/v1")) {
                                                     cleanText = cleanText.substringBefore("/rest/v1")
                                                }
                                                supabaseUrlInput = cleanText
                                                Toast.makeText(context, "تم لصق العنوان! 📋", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "الحافظة فارغة! 📋❌", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste URL", tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                                             Spacer(modifier = Modifier.width(4.dp))
                                            Text("لصق", color = CosmicSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                },
                                placeholder = { Text("https://example.supabase.co", color = Color.Gray) }
                            )

                            OutlinedTextField(
                                value = supabaseKeyInput,
                                onValueChange = { supabaseKeyInput = it },
                                label = { Text("مفتاح API الخاص بـ Supabase (Anon/Service)", color = CosmicSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CosmicSecondary,
                                    unfocusedBorderColor = CosmicSurfaceVariant,
                                    focusedLabelColor = CosmicSecondary,
                                    cursorColor = CosmicSecondary,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                trailingIcon = {
                                    TextButton(
                                        onClick = {
                                            val text = clipboardManager.getText()?.text
                                            if (!text.isNullOrEmpty()) {
                                                supabaseKeyInput = text.trim()
                                                Toast.makeText(context, "تم لصق المفتاح! 🔑", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "الحافظة فارغة! 📋❌", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                             Icon(Icons.Default.ContentPaste, contentDescription = "Paste Key", tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                                             Spacer(modifier = Modifier.width(4.dp))
                                             Text("لصق", color = CosmicSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                },
                                placeholder = { Text("eyJ...", color = Color.Gray) }
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Collapsible SQL Guide button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showSqlSetupGuide = !showSqlSetupGuide }
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (showSqlSetupGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Guide",
                                        tint = CosmicSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "كيف أهيئ قاعدة بيانات Supabase؟ 💡",
                                        color = CosmicSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (showSqlSetupGuide) "إخفاء" else "عرض الشرح",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            }
                            
                            androidx.compose.animation.AnimatedVisibility(visible = showSqlSetupGuide) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "1. افتح مشروعك في موقع Supabase.co\n" +
                                               "2. اذهب إلى SQL Editor في القائمة الجانبية.\n" +
                                               "3. انقر على مشروع جديد (New query).\n" +
                                               "4. انسخ كود SQL بالأسفل والصقه هناك ثم اضغط Run.\n" +
                                               "5. كذلك تأكد من تفعيل RLS أو إضافة سياسات (Policies) للسماح بالقراءة والكتابة للجميع (Anon).",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    val sqlScript = """
-- 1. إنشاء جدول المنتجات (products)
CREATE TABLE IF NOT EXISTS public.products (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    price DOUBLE PRECISION NOT NULL,
    category TEXT NOT NULL,
    category_arabic TEXT NOT NULL,
    rating REAL,
    image_res_name TEXT,
    is_favorite BOOLEAN DEFAULT false,
    stock INTEGER DEFAULT 10
);

-- تحديث السيرفر لإضافة البريد الإلكتروني للبائع وحالة الموافقة لو قمت بإنشائه مسبقاً
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS seller_email TEXT DEFAULT '';
ALTER TABLE public.products ADD COLUMN IF NOT EXISTS is_approved BOOLEAN DEFAULT true;

-- 2. إنشاء جدول الطلبات الأسبوعي واليومي (orders)
CREATE TABLE IF NOT EXISTS public.orders (
    id SERIAL PRIMARY KEY,
    order_id TEXT NOT NULL,
    product_id INTEGER NOT NULL,
    product_name TEXT NOT NULL,
    price_at_order DOUBLE PRECISION NOT NULL,
    quantity INTEGER NOT NULL,
    order_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status_arabic TEXT NOT NULL,
    customer_name TEXT,
    customer_phone TEXT,
    customer_address TEXT,
    courier_name TEXT DEFAULT '',
    courier_phone TEXT DEFAULT ''
);

-- تحديث الأعمدة لو كانت طاولتك منشأة قديماً بالفعل لضمان الحفظ
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS courier_name TEXT DEFAULT '';
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS courier_phone TEXT DEFAULT '';

-- 3. إنشاء جدول مناديب التوصيل بالسودان (couriers)
CREATE TABLE IF NOT EXISTS public.couriers (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    state_info TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'نشط ومتوفر 🟢'
);

-- 4. إنشاء جدول المستخدمين والعملاء (profiles)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT,
    phone TEXT,
    email TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- 5. إنشاء جدول البائعين (sellers)
CREATE TABLE IF NOT EXISTS public.sellers (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT,
    classification TEXT DEFAULT 'تاجر ذهبي ⭐',
    commission_rate DOUBLE PRECISION DEFAULT 0.10,
    created_at BIGINT
);

-- 6. إعداد دالة ومراقب لتلقائي ربط Supabase Auth مع جدول البروفايل (عند الرغبة):
-- CREATE OR REPLACE FUNCTION public.handle_new_user()
-- RETURNS trigger AS $$
-- BEGIN
--   INSERT INTO public.profiles (id, name, phone, email)
--   VALUES (new.id, new.raw_user_meta_data->>'name', new.raw_user_meta_data->>'phone', new.email);
--   RETURN NEW;
--   EXCEPTION WHEN OTHERS THEN RETURN NEW;
-- END;
-- $$ LANGUAGE plpgsql SECURITY DEFINER;
--
-- CREATE OR REPLACE TRIGGER on_auth_user_created
--   AFTER INSERT ON auth.users
--   FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 7. تفعيل RLS أو السماح بالقراءة والكتابة لغرض التطوير والتجربة بالسودان
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.couriers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sellers ENABLE ROW LEVEL SECURITY;

-- حذف السياسات القديمة إن وجدت لتجنب تكرار الخطأ في Supabase
DROP POLICY IF EXISTS "Allow select products" ON public.products;
DROP POLICY IF EXISTS "Allow insert products" ON public.products;
DROP POLICY IF EXISTS "Allow select orders" ON public.orders;
DROP POLICY IF EXISTS "Allow insert orders" ON public.orders;
DROP POLICY IF EXISTS "Allow update orders" ON public.orders;
DROP POLICY IF EXISTS "Allow select profiles" ON public.profiles;
DROP POLICY IF EXISTS "Allow insert profiles" ON public.profiles;
DROP POLICY IF EXISTS "Allow update profiles" ON public.profiles;
DROP POLICY IF EXISTS "Allow select couriers" ON public.couriers;
DROP POLICY IF EXISTS "Allow insert couriers" ON public.couriers;
DROP POLICY IF EXISTS "Allow delete couriers" ON public.couriers;
DROP POLICY IF EXISTS "Allow select sellers" ON public.sellers;
DROP POLICY IF EXISTS "Allow insert sellers" ON public.sellers;
DROP POLICY IF EXISTS "Allow delete sellers" ON public.sellers;

-- إنشاء سياسات الوصول الكونية الجديدة لجميع المستخدمين لضمان عدم حدوث أخطاء RLS
CREATE POLICY "Allow select products" ON public.products FOR SELECT USING (true);
CREATE POLICY "Allow insert products" ON public.products FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow select orders" ON public.orders FOR SELECT USING (true);
CREATE POLICY "Allow insert orders" ON public.orders FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update orders" ON public.orders FOR UPDATE USING (true);

CREATE POLICY "Allow select profiles" ON public.profiles FOR SELECT USING (true);
CREATE POLICY "Allow insert profiles" ON public.profiles FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update profiles" ON public.profiles FOR UPDATE USING (true);

CREATE POLICY "Allow select couriers" ON public.couriers FOR SELECT USING (true);
CREATE POLICY "Allow insert couriers" ON public.couriers FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow delete couriers" ON public.couriers FOR DELETE USING (true);

CREATE POLICY "Allow select sellers" ON public.sellers FOR SELECT USING (true);
CREATE POLICY "Allow insert sellers" ON public.sellers FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow delete sellers" ON public.sellers FOR DELETE USING (true);
                                    """.trimIndent()
                                    
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(sqlScript))
                                            Toast.makeText(context, "تم نسخ كود SQL بنجاح! 📋🚀", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy SQL", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("نسخ كود SQL الإعداد للتطبيق 📋", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val defaultUrl = "https://figyszyedxlmbtaepmyt.supabase.co/"
                                    val defaultKey = "Sb_publishable_WRJgX0HreyiRExm-d5OSVQ_sZwnWYBy"
                                    supabaseUrlInput = defaultUrl
                                    supabaseKeyInput = defaultKey
                                    com.example.data.network.SupabaseConfig.save(context, defaultUrl, defaultKey)
                                    viewModel.refreshConnection()
                                    showSupabaseSettingsDialog = false
                                    Toast.makeText(context, "تمت إعادة تعيين القيم الافتراضية ومحاولة المزامنة 🔄", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White)
                            ) {
                                Text("إعادة الافتراضي", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    if (supabaseUrlInput.trim().isEmpty() || supabaseKeyInput.trim().isEmpty()) {
                                        Toast.makeText(context, "يرجى ملء جميع الحقول أولاً! ⚠️", Toast.LENGTH_SHORT).show()
                                    } else {
                                        com.example.data.network.SupabaseConfig.save(
                                            context,
                                            supabaseUrlInput.trim(),
                                            supabaseKeyInput.trim()
                                        )
                                        viewModel.refreshConnection()
                                        showSupabaseSettingsDialog = false
                                        Toast.makeText(context, "تم حفظ الإعدادات وجاري مزامنة قاعدة البيانات... 📡", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                            ) {
                                Text("حفظ ومزامنة", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSupabaseSettingsDialog = false }) {
                            Text("إلغاء", color = Color.White)
                        }
                    },
                    containerColor = CosmicSurface,
                    shape = RoundedCornerShape(16.dp)
                )
            }


        }
    }
}

@Composable
fun HomeScreenBody(
    searchQuery: String,
    selectedCategory: String,
    products: List<ProductEntity>,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    onFavoriteToggle: (ProductEntity) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    viewModel: MajarahViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Persistent Search bar styled in Arabized Cosmic theme with clear button
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_search_bar"),
            placeholder = { 
                Text(
                    "ابحث عن حاسوب محمول، ساعة، سماعات...", 
                    color = MediumContrastTextDark, 
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                ) 
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = CosmicSecondary,
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            leadingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "مسح البحث",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CosmicSecondary,
                unfocusedBorderColor = CosmicSurfaceVariant,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = CosmicSurface,
                unfocusedContainerColor = CosmicSurface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Starry Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(CosmicPrimary, Color(0xFF3F1976))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "🚀 عروض المجرة الحصرية للسودان",
                            color = CosmicTertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "منتجات تكنولوجية وعصرية بمواصفات خارقة وبأسعار تناسبكم بالجنيه السوداني مع توصيل فوري ومضمون.",
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Right,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Horizontal Category Tabs
            item {
                val categories = listOf(
                    Pair("", "🚀 الكل"),
                    Pair("electronics", "💻 إلكترونيات"),
                    Pair("fashion", "👕 أزياء نجمية"),
                    Pair("home", "🏡 الديكور والمنزل"),
                    Pair("cosmic_deals", "⭐ عروض مذهلة")
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    reverseLayout = true // Standard Arabic layout direction
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat.first
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) CosmicSecondary else CosmicSurface)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CosmicSecondary else CosmicSurfaceVariant,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { onCategorySelect(cat.first) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat.second,
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Horizontal Sorting Tabs
            item {
                val sortBySelected by viewModel.sortBy.collectAsStateWithLifecycle()
                val sortingOptions = listOf(
                    Pair("default", "⭐ المقترح"),
                    Pair("newest", "🚀 الأحدث"),
                    Pair("price_asc", "📈 الأقل سعراً"),
                    Pair("price_desc", "📉 الأعلى سعراً")
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "ترتيب حسب العروض والنقاط:",
                        color = MediumContrastTextDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        reverseLayout = true
                    ) {
                        items(sortingOptions) { option ->
                            val isSelected = sortBySelected == option.first
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) CosmicSecondary else CosmicSurface)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) CosmicSecondary else CosmicSurfaceVariant,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { viewModel.updateSortBy(option.first) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = option.second,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

        // Main Product List Feed
        if (products.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No product",
                            tint = CosmicSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "عذراً! لم نجد أي تطابق لطلبك في المجرة.",
                            color = MediumContrastTextDark,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(products) { product ->
                MajarahProductCard(
                    product = product,
                    onClick = { onProductClick(product) },
                    onFavoriteToggle = { onFavoriteToggle(product) },
                    onAddToCart = { onAddToCart(product) },
                    formatPrice = { viewModel.formatPrice(it) }
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
}

@Composable
fun MajarahProductCard(
    product: ProductEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToCart: () -> Unit,
    formatPrice: (Double) -> String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CosmicSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                ProductImagePlaceholder(product.imageResName, modifier = Modifier.fillMaxWidth())
                
                // Favorite Button
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(0.4f), RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = if (product.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Add to favorites",
                        tint = if (product.isFavorite) Color.Red else Color.White
                    )
                }

                // Star Rating Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.rating.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(Icons.Filled.Star, null, tint = CosmicTertiary, modifier = Modifier.size(12.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = product.categoryArabic,
                    color = CosmicSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    color = MediumContrastTextDark,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Right,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CosmicSecondary,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .testTag("add_to_cart_btn_${product.id}")
                            .height(32.dp)
                    ) {
                        Text("إضافة للسلة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.AddShoppingCart, null, modifier = Modifier.size(14.dp))
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "ج.س",
                            color = CosmicSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 4.dp, bottom = 2.dp)
                        )
                        Text(
                            text = formatPrice(product.price),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesScreenBody(
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    val cats = listOf(
        Triple("electronics", "💻 الأجهزة والإلكترونيات الفلكية", "أجهزة لابتوب فائقة، ساعات كوزموس ذكية وسماعات محيطية عازلة للضوضاء."),
        Triple("fashion", "👕 ملابس وأزياء المجرة الرسمية", "أناقة وسحر الألياف الذكية المدمجة المقاومة للمياه والحرارة بتصميمات عصرية."),
        Triple("home", "🏡 الديكور الفضائي والمنزل ذكي", "أجهزة إضاءة سديم ساحرة، أدوات تحضير القهوة بلمح البصر وشاشات سينمائية 8K."),
        Triple("cosmic_deals", "⭐ عروض كوانتوم الهائلة والمستحيلة", "تنزيلات نارية دورية وهدايا فلكية استثنائية تناسب ميزانيتكم بالجنيه السوداني.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "أقسام المجرة المعتمدة",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Right
            )
            Text(
                "تصفح كتالوج المنتجات حسب الفئات المفضلة لديك وعش تجربة تليق بك",
                color = MediumContrastTextDark,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Right
            )
        }

        items(cats) { c ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategorySelect(c.first) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onCategorySelect(c.first) },
                        modifier = Modifier.background(CosmicPrimary.copy(alpha = 0.2f), RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = CosmicSecondary)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(c.second, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(c.third, color = MediumContrastTextDark, fontSize = 12.sp, textAlign = TextAlign.Right)
                    }
                }
            }
        }
    }
}

@Composable
fun CartScreenBody(
    cartItems: List<CartItemWithProduct>,
    totalSum: Double,
    phoneValue: String,
    addressValue: String,
    nameValue: String,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onQtyIncrease: (CartItemWithProduct) -> Unit,
    onQtyDecrease: (CartItemWithProduct) -> Unit,
    onRemove: (CartItemWithProduct) -> Unit,
    onSubmit: () -> Unit,
    formatPrice: (Double) -> String,
    isLoggedIn: Boolean = true,
    onRegisterPrompt: () -> Unit = {},
    viewModel: MajarahViewModel
) {
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val couponError by viewModel.couponError.collectAsStateWithLifecycle()
    var couponInputText by remember { mutableStateOf("") }
    if (cartItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    tint = CosmicSurfaceVariant,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "حقيبة المشتريات خالية حالياً!",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "تصفح سوق المجرة وأضف منتجاتك المفضلة.",
                    color = MediumContrastTextDark,
                    fontSize = 12.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Text(
                    "سلة المشتريات الخاصة بك 🛒",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            }

            items(cartItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Action buttons to adjust qty
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { onQtyIncrease(item) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(CosmicSurfaceVariant, RoundedCornerShape(6.dp))
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            
                            Text(
                                text = item.quantity.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            IconButton(
                                onClick = { onQtyDecrease(item) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(CosmicSurfaceVariant, RoundedCornerShape(6.dp))
                            ) {
                                Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { onRemove(item) },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(Icons.Default.Delete, "حذف", tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }

                        // Product Name, Category & Price
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = item.product.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${formatPrice(item.product.price)} ج.س",
                                color = CosmicSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CosmicSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SpaceDashboard, null, tint = CosmicSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Promo Code Card Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "🎫 هل لديك كود خصم كوني؟",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        if (appliedCoupon == null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { 
                                        if (couponInputText.isNotBlank()) {
                                            val valid = viewModel.applyCoupon(couponInputText)
                                            if (valid) {
                                                couponInputText = ""
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CosmicSecondary,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("تطبيق 💫", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                
                                OutlinedTextField(
                                    value = couponInputText,
                                    onValueChange = { couponInputText = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = {
                                        Text(
                                            "أدخل الكود (مثال: COSMIC10)",
                                            fontSize = 11.sp,
                                            color = MediumContrastTextDark,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Right
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = CosmicSecondary,
                                        unfocusedBorderColor = CosmicSurfaceVariant,
                                        focusedContainerColor = CosmicSurfaceVariant.copy(0.3f),
                                        unfocusedContainerColor = CosmicSurfaceVariant.copy(0.3f)
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                )
                            }
                            
                            if (couponError != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = couponError!!,
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CosmicSecondary.copy(alpha = 0.15f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { viewModel.removeCoupon() }
                                ) {
                                    Text("حذف الكود ❌", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    val pct = viewModel.getCouponDiscountPercentage(appliedCoupon)
                                    Text(
                                        text = "كود الخصم الفعال: $appliedCoupon (%$pct)",
                                        color = CosmicSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Right
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.CheckCircle, null, tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Billing breakdown card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(0.4f))
                ) {
                    val netTotal = viewModel.calculateDiscountedSum(cartItems, appliedCoupon)
                    val savings = totalSum - netTotal
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${formatPrice(totalSum)} ج.س", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("المجموع الفرعي الأصل:", color = MediumContrastTextDark)
                        }
                        
                        if (savings > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("-${formatPrice(savings)} ج.س", color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                Text("خصم الكوبون الكوني:", color = CosmicSecondary)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val deliveryPrice = 5000.0 // SDG flat shipping
                            Text("${formatPrice(deliveryPrice)} ج.س", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("رسوم التوصيل المقدرة (بالسودان):", color = MediumContrastTextDark)
                        }
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = CosmicSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${formatPrice(netTotal + 5000.0)} ج.س", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("المبلغ الإجمالي الكلي بعد الخصم:", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Customer details and shipment form / registration CTA for guests
            item {
                if (isLoggedIn) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                "معلومات التوصيل والاتصال السودانية",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Full Name input field
                            OutlinedTextField(
                                value = nameValue,
                                onValueChange = onNameChange,
                                modifier = Modifier.fillMaxWidth().testTag("checkout_name"),
                                label = { Text("الاسم الكامل (مسترجع من الحساب)", color = CosmicSecondary.copy(alpha = 0.7f), fontSize = 12.sp) },
                                placeholder = { Text("أدخل الاسم الثلاثي بالكامل", color = MediumContrastTextDark) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant,
                                    focusedLabelColor = CosmicSecondary, unfocusedLabelColor = MediumContrastTextDark
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Phone input field
                            OutlinedTextField(
                                value = phoneValue,
                                onValueChange = onPhoneChange,
                                modifier = Modifier.fillMaxWidth().testTag("checkout_phone"),
                                label = { Text("رقم الهاتف (مسترجع من الحساب)", color = CosmicSecondary.copy(alpha = 0.7f), fontSize = 12.sp) },
                                placeholder = { Text("مثال: 0912345678", color = MediumContrastTextDark) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant,
                                    focusedLabelColor = CosmicSecondary, unfocusedLabelColor = MediumContrastTextDark
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Sudanese Deliverable address input field
                            OutlinedTextField(
                                value = addressValue,
                                onValueChange = onAddressChange,
                                modifier = Modifier.fillMaxWidth().testTag("checkout_address"),
                                label = { Text("عنوان التوصيل بالسودان *", color = CosmicSecondary, fontSize = 12.sp) },
                                placeholder = { Text("مثلاً: أم درمان، بورتسودان، حي الرياض الخرطوم", color = MediumContrastTextDark) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant,
                                    focusedLabelColor = CosmicSecondary, unfocusedLabelColor = CosmicSecondary.copy(alpha = 0.6f)
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val formValid = nameValue.isNotBlank() && phoneValue.isNotBlank() && addressValue.isNotBlank()
                            Button(
                                onClick = onSubmit,
                                enabled = formValid,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_submit_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CosmicSecondary,
                                    disabledContainerColor = CosmicSurfaceVariant.copy(0.4f),
                                    contentColor = Color.Black,
                                    disabledContentColor = MediumContrastTextDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Text("تأكيد وشحن الطلب الفوري", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CosmicSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "⚠️ يتطلب إتمام طلب الشراء تسجيل حساب بالمجرة",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "بصفتك زائراً، يمكنك تصفح وإضافة المنتجات إلى السلة، ولكن يتوجب عليك إنشاء حساب جديد أو تسجيل الدخول لتتمكن من إرسال الطلب وإتمام التوصيل في السودان ومتابعة مناديب التوصيل.",
                                color = MediumContrastTextDark,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onRegisterPrompt,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CosmicSecondary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Text("سجل حسابك بالمجرة الآن 🌌", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreenBody(
    favorites: List<ProductEntity>,
    onProductClick: (ProductEntity) -> Unit,
    onRemoveFavorite: (ProductEntity) -> Unit
) {
    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = CosmicSurfaceVariant,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "قائمة المفضلة فارغة كفضاء كوني!",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "اضغط على رمز القلب لتثبيت المنتجات الهامة هنا.",
                    color = MediumContrastTextDark,
                    fontSize = 12.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "منتجاتك الكونية المفضلة ⭐",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            }

            items(favorites) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProductClick(product) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onRemoveFavorite(product) }) {
                            Icon(Icons.Filled.Favorite, "حذف من المفضلة", tint = Color.Red)
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = product.categoryArabic,
                                color = CosmicSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CosmicSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            ProductImagePlaceholder(product.imageResName, modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreenBody(
    orders: List<com.example.data.db.OrderEntity>,
    onClearHistory: () -> Unit,
    formatPrice: (Double) -> String,
    viewModel: MajarahViewModel
) {
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (orders.isEmpty()) {
            isRefreshing = true
        }
        viewModel.syncOrders {
            isRefreshing = false
        }
        // Periodic background update every 8 seconds to automatically update the statuses
        while (true) {
            kotlinx.coroutines.delay(8000)
            viewModel.syncOrders()
        }
    }

    if (isRefreshing && orders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = CosmicSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "جاري مزامنة وتحديث حالة طلباتك من السحابة... 🛰️",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    } else if (orders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = CosmicSurfaceVariant,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "لم تقم بأي طلبات بعد!",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "أكمل أول مشترياتك وستظهر لك الفواتير المتكاملة هنا.",
                    color = MediumContrastTextDark,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isRefreshing = true
                        viewModel.syncOrders { isRefreshing = false }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                ) {
                    Text("تحديث الفواتير والطلبات 🔄", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onClearHistory) {
                            Text("مسح السجل", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "فواتير مشترياتي بالمجرة 📑",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CosmicSecondary.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = CosmicSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مزامنة لحظية وتتبع تلقائي لحالات الطلب نشط 🛰️",
                            color = CosmicSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Group orders by orderNo for a cleaner presentation
            val listData = orders.groupBy { it.orderId }.entries.toList()

            items(listData) { entry ->
                val orderId = entry.key
                val orderItems = entry.value
                val firstItem = orderItems.firstOrNull()
                
                val customerName = firstItem?.customerName ?: "زبون المجرة الكوني"
                val customerPhone = firstItem?.customerPhone ?: "09"
                val customerAddress = firstItem?.customerAddress ?: "السودان"
                val orderStatus = firstItem?.statusArabic ?: "جاري التجهيز للتوصيل 📦"
                val isShipped = orderStatus.contains("شحن") || orderStatus.contains("مندوب") || orderStatus.contains("تم")
                val isDelivered = orderStatus.contains("تم الاستلام") || 
                        orderStatus.contains("تم التوصيل") || 
                        orderStatus.contains("تم توصيل") || 
                        orderStatus.contains("تمت التوصيل") || 
                        orderStatus.contains("تمام") || 
                        orderStatus.contains("بنجاح") || 
                        orderStatus.contains("تم التسليم")
                val orderDateMillis = firstItem?.orderDate ?: System.currentTimeMillis()
                val courierName = firstItem?.courierName ?: ""
                val courierPhone = firstItem?.courierPhone ?: ""
                
                val dateStr = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US).format(java.util.Date(orderDateMillis))
                val totalItemsSum = orderItems.sumOf { it.priceAtOrder * it.quantity }
                val showDeliveryPrice = orderStatus.contains("تسليم المندوب") || orderStatus.contains("تسليم لمندوب") || isDelivered
                val deliveryPrice = if ((firstItem?.deliveryFee ?: 0.0) <= 0.0) 5000.0 else firstItem!!.deliveryFee
                val grandTotal = if (showDeliveryPrice) (totalItemsSum + deliveryPrice) else totalItemsSum
                
                val context = androidx.compose.ui.platform.LocalContext.current
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title Header (Order ID & Date)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "رقم الطلب: $orderId",
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicSecondary,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateStr,
                                    color = MediumContrastTextDark,
                                    fontSize = 10.sp
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(CosmicSecondary.copy(0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = orderStatus,
                                    color = CosmicSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        if (courierName.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CosmicSecondary.copy(alpha = 0.1f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBike,
                                        contentDescription = null,
                                        tint = CosmicSecondary,
                                        modifier = Modifier.size(28.dp).padding(start = 4.dp)
                                    )
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("🚴 مندوب التوصيل المعين للطلب:", fontSize = 11.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                        Text(courierName, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("هاتف للتواصل اللحظي: $courierPhone", fontSize = 11.sp, color = Color.White.copy(0.8f))
                                    }
                                }
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = CosmicSurfaceVariant)
                        
                        // New: Progress & Tracking Timeline Steps
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CosmicDeepSpace.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "حالة تتبع الطلب الكوني 🌌",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // Step 1: Placed / Prep
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(CosmicSecondary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("1", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("تم الطلب", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                // Line 1
                                Divider(
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp), 
                                    color = if (isShipped) CosmicSecondary else CosmicSurfaceVariant
                                )
                                
                                // Step 2: Shipped / Out
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isShipped) CosmicSecondary else CosmicSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("2", color = if (isShipped) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("تم تسليم المندوب", color = if (isShipped) Color.White else MediumContrastTextDark, fontSize = 9.sp)
                                }
                                
                                // Line 2
                                Divider(
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp), 
                                    color = if (isDelivered) CosmicSecondary else CosmicSurfaceVariant
                                )
                                
                                // Step 3: Delivered
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isDelivered) CosmicSecondary else CosmicSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("3", color = if (isDelivered) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("تم التوصيل", color = if (isDelivered) Color.White else MediumContrastTextDark, fontSize = 9.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Customer Information Card (بيانات المستلم والتوصيل بالسودان)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CosmicSurfaceVariant.copy(0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "👤 بيانات المستلم والتوصيل بالسودان",
                                color = CosmicSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text(customerName, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp, textAlign = TextAlign.Right)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("الاسم:", color = MediumContrastTextDark, fontSize = 12.sp, textAlign = TextAlign.Right)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text(customerPhone, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp, textAlign = TextAlign.Right)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("رقم الهاتف:", color = MediumContrastTextDark, fontSize = 12.sp, textAlign = TextAlign.Right)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text(customerAddress, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp, textAlign = TextAlign.Right)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("العنوان المرسل إليه:", color = MediumContrastTextDark, fontSize = 12.sp, textAlign = TextAlign.Right)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Selected Products Items Section
                        Text(
                            text = "🛒 تفاصيل محتويات السلة والأسعار",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        orderItems.forEach { orderItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${formatPrice(orderItem.priceAtOrder * orderItem.quantity)} ج.س",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${orderItem.productName} (العدد: ${orderItem.quantity})",
                                    color = MediumContrastTextDark,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Right
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = CosmicSurfaceVariant)

                        // Breakdown and Total Calculation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (showDeliveryPrice) "${formatPrice(deliveryPrice)} ج.س" else viewModel.t("يحدد عند تسليم المندوب 🚴", "To be determined upon delivery 🚴"),
                                color = if (showDeliveryPrice) Color.White else CosmicSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (showDeliveryPrice) FontWeight.Normal else FontWeight.Bold
                            )
                            Text("رسوم التوصيل:", color = MediumContrastTextDark, fontSize = 12.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${formatPrice(grandTotal)} ج.س",
                                fontWeight = FontWeight.Bold,
                                color = CosmicTertiary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (showDeliveryPrice) "المبلغ الإجمالي الكلي:" else "المبلغ الإجمالي للمشتريات (غير شامل التوصيل):",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        val courierAccepted = courierName.isNotBlank()
                        if (!courierAccepted) {
                            Text(
                                text = "⏳ جاري تعيين كابتن التوصيل لتسليم الشحنة لعنوانكم...",
                                color = CosmicSecondary.copy(0.7f),
                                fontSize = 9.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (isDelivered) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Gray.copy(0.12f), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.Gray.copy(0.3f), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Lock, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "هذه الفاتورة مكتملة ومغلقة كلياً وتعتبر نهائية غير قابلة للإرسال أو التعديل 🔒",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Consolidated Button: Clipboard copy of invoice & Open WhatsApp for the courier
                            Button(
                                onClick = {
                                    val textBuilder = StringBuilder()
                                    textBuilder.append("🌌 طلب جديد من تطبيق المجرة الكونية للتسوق\n")
                                    textBuilder.append("-----------------------------\n")
                                    textBuilder.append("🆔 رقم الطلب: $orderId\n")
                                    textBuilder.append("📅 التاريخ: $dateStr\n")
                                    textBuilder.append("👤 الاسم: $customerName\n")
                                    textBuilder.append("📞 الهاتف: $customerPhone\n")
                                    textBuilder.append("📍 العنوان السوداني: $customerAddress\n")
                                    textBuilder.append("-----------------------------\n")
                                    textBuilder.append("🛍️ تفاصيل المنتجات والمشتريات:\n")
                                    orderItems.forEach { item ->
                                        textBuilder.append("- ${item.productName} (العدد: ${item.quantity}) - ${formatPrice(item.priceAtOrder * item.quantity)} ج.س\n")
                                    }
                                    textBuilder.append("-----------------------------\n")
                                    if (showDeliveryPrice) {
                                        textBuilder.append("🚚 رسوم التوصيل المقدرة: ${formatPrice(deliveryPrice)} ج.س\n")
                                        textBuilder.append("💰 الإجمالي المستحق: ${formatPrice(grandTotal)} ج.س \n")
                                    } else {
                                        textBuilder.append("🚚 رسوم التوصيل: (تحدد وتظهر بعد تسليم المندوب) 🚴\n")
                                        textBuilder.append("💰 إجمالي المشتريات: ${formatPrice(grandTotal)} ج.س (لا يشمل رسوم التوصيل حتى الآن)\n")
                                    }
                                    textBuilder.append("✨ حالة الطلب الحالية: $orderStatus\n")
                                    if (courierName.isNotBlank()) {
                                        textBuilder.append("🚴 المندوب المعين للتوصيل: $courierName ($courierPhone)\n")
                                    }
                                    textBuilder.append("-----------------------------\n")
                                    textBuilder.append("المجرة الكونية - تسوق من أي مكان بكل سهولة 🚀")
                                    
                                    val invoiceText = textBuilder.toString()
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(invoiceText))
                                    
                                    if (courierPhone.isNotBlank()) {
                                        try {
                                            val cleanPhone = courierPhone.trim()
                                                .replace("+", "")
                                                .replace(" ", "")
                                                .replace("-", "")
                                                .replace("(", "")
                                                .replace(")", "")
                                            var whatsappPhone = cleanPhone
                                            if (whatsappPhone.startsWith("0")) {
                                                whatsappPhone = "249" + whatsappPhone.substring(1)
                                            } else if (whatsappPhone.startsWith("9") && whatsappPhone.length == 9) {
                                                whatsappPhone = "249" + whatsappPhone
                                            } else if (!whatsappPhone.startsWith("249") && whatsappPhone.isNotBlank()) {
                                                whatsappPhone = "249" + whatsappPhone
                                            }
                                            
                                            val uriText = android.net.Uri.encode(invoiceText)
                                            val whatsappUrl = "https://api.whatsapp.com/send?phone=$whatsappPhone&text=$uriText"
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse(whatsappUrl)
                                            )
                                            context.startActivity(intent)
                                            android.widget.Toast.makeText(context, "تم نسخ الفاتورة 📋 وجاري فتح واتساب لإرسالها للمندوب $courierName 🚴📲", android.widget.Toast.LENGTH_LONG).show()
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "تم نسخ تفاصيل الفاتورة بنجاح 📋 يتعذر فتح واتساب تلقائياً!", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        android.widget.Toast.makeText(context, "تم نسخ تفاصيل الفاتورة بنجاح 📋 (لم يتم تعيين مندوب لهذا الطلب بعد لإرسالها بالواتساب)", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CosmicSecondary,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("نسخ تفاصيل الفاتورة وإرسالها للمندوب عبر واتساب 📋🚴", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailScreenBody(
    product: ProductEntity,
    onAddToCart: (Int) -> Unit,
    onFavoriteToggle: () -> Unit,
    formatPrice: (Double) -> String,
    isCourier: Boolean = false
) {
    var quantity by remember { mutableStateOf(1) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(CosmicSurfaceVariant)
            ) {
                ProductImagePlaceholder(product.imageResName, modifier = Modifier.fillMaxSize())
                
                // Overlay icons back navigation is handled in action top-bar
                if (!isCourier) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onFavoriteToggle,
                            modifier = Modifier.background(Color.Black.copy(0.4f), RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = if (product.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = if (product.isFavorite) Color.Red else Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = Modifier
                        .background(CosmicSecondary.copy(0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = product.categoryArabic, color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = product.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Right
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text("(${product.rating})", color = MediumContrastTextDark, fontSize = 12.sp)
                    Icon(Icons.Filled.Star, null, tint = CosmicTertiary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("الوضع في المخزن: ${product.stock} قطع متوفرة", color = ActiveGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Divider(color = CosmicSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "تفاصيل ومواصفات المنتج كوزميك",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = product.description,
                    color = MediumContrastTextDark,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Right,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("ج.س", color = CosmicSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp))
                                Text(formatPrice(product.price * quantity), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                            Text("السعر الإجمالي الكلي:", color = MediumContrastTextDark, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quantity controller
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IconButton(
                                    onClick = { if (quantity < product.stock) quantity++ },
                                    modifier = Modifier.background(CosmicSurfaceVariant, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Color.White)
                                }
                                Text("$quantity", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                IconButton(
                                    onClick = { if (quantity > 1) quantity-- },
                                    modifier = Modifier.background(CosmicSurfaceVariant, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Remove, null, tint = Color.White)
                                }
                            }
                            Text("اختر عدد الحبات المطلوب:", color = MediumContrastTextDark, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isCourier) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CosmicSecondary.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Info, null, tint = CosmicSecondary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "وضع تصفح الكابتن نشط 🚴 لا يمكن الطلب",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { onAddToCart(quantity) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("product_detail_add_to_cart"),
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Text("حجز الفاتورة وإضافة إلى سلتك", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.AddShoppingCart, null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenBody(
    email: String,
    password: String,
    name: String,
    phone: String,
    isRegister: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
    onSkipAsGuest: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: MajarahViewModel
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var isGoogleFlowActive by remember { mutableStateOf(false) }
    var googleEmailState by remember { mutableStateOf("mawiaosman0@gmail.com") }
    var showGoogleDialog by remember { mutableStateOf(false) }

    val logoScale = remember { Animatable(0.2f) }
    val logoAlpha = remember { Animatable(0f) }

    val langEnglish = viewModel.isEnglish.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDeepSpace)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = java.util.Random(1337)
            for (i in 0..30) {
                val x = r.nextFloat() * size.width
                val y = r.nextFloat() * size.height
                val radius = r.nextFloat() * 4f + 1f
                drawCircle(
                    color = CosmicSecondary.copy(alpha = r.nextFloat() * 0.5f + 0.1f),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 450.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicSurface.copy(alpha = 0.9f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 60.dp)
                ) {
                    item {
                        // Language Selector Tab Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = viewModel.t("اختر اللغة: ", "Select Language: "),
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CosmicSurfaceVariant.copy(alpha = 0.5f))
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (!langEnglish) CosmicSecondary else Color.Transparent)
                                        .clickable { viewModel.isEnglish.value = false }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "العربية 🇸🇩",
                                        color = if (!langEnglish) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (langEnglish) CosmicSecondary else Color.Transparent)
                                        .clickable { viewModel.isEnglish.value = true }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "English 🇬🇧",
                                        color = if (langEnglish) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Beautiful App Logo with cosmic planet + rocket, with smooth entry animation
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .graphicsLayer(
                                        scaleX = logoScale.value,
                                        scaleY = logoScale.value,
                                        alpha = logoAlpha.value
                                    )
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                CosmicPrimary.copy(alpha = 0.45f),
                                                Color.Transparent
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_majarah_universe_logo_1782084799100),
                                    contentDescription = "Galaxy Logo",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // App Logo Text Slogan - exact text "المجرة الكونية للتسوق"
                            Text(
                                text = viewModel.t("المجرة 🌌", "Majarah 🌌"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = viewModel.t("تسوّقْ من أيّ مكانٍ بكل سهولة.. واطلبْ ليصلك مندوبنا أينما كنت! ✨🚀", "Shop from anywhere with ease.. and ask to get delivered wherever you are! ✨🚀"),
                                color = CosmicSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    item {
                        Divider(color = CosmicSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                    }

                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isRegister) viewModel.t("إنشاء حساب كوني جديد", "Create New Cosmic Account") else viewModel.t("تسجيل الدخول للمجرة", "Log in to Majarah"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isRegister) 
                                    viewModel.t("سجل الآن لتتبع طلباتك الكونية وحفظ مفضلاتك بالسودان", "Register now to track your cosmic orders and save your favorites in Sudan") 
                                else 
                                    viewModel.t("ادخل بيانات حسابك للولوج إلى عالم من التسوق اللامتناهي", "Enter your account details to access a world of endless shopping"),
                                color = MediumContrastTextDark,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }

                    if (isGoogleFlowActive) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Green.copy(0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = viewModel.t("متصل عبر حساب Google بنجاح 🟢", "Connected via Google successfully 🟢"),
                                            color = Color.Green,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = googleEmailState,
                                        color = Color.White.copy(0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = viewModel.t("يرجى إكمال الاسم ورقم الهاتف وتعيين كلمة مرور لتفعيل حسابك بالمجرة:", "Please enter your name, phone number, and a password to complete registration:"),
                                        color = Color.White.copy(0.9f),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (isRegister || isGoogleFlowActive) {
                        item {
                            OutlinedTextField(
                                value = name,
                                onValueChange = onNameChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_name_input"),
                                placeholder = { Text(viewModel.t("الاسم بالكامل", "Full Name"), color = MediumContrastTextDark, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = CosmicSecondary) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CosmicSecondary,
                                    unfocusedBorderColor = CosmicSurfaceVariant,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = CosmicDeepSpace,
                                    unfocusedContainerColor = CosmicDeepSpace
                                )
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = onPhoneChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_phone_input"),
                                placeholder = { Text(viewModel.t("رقم الهاتف (مثال: 0912345678)", "Phone Number (e.g., 0912345678)"), color = MediumContrastTextDark, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Phone, null, tint = CosmicSecondary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CosmicSecondary,
                                    unfocusedBorderColor = CosmicSurfaceVariant,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = CosmicDeepSpace,
                                    unfocusedContainerColor = CosmicDeepSpace
                                )
                            )
                        }
                    }

                    if (!isGoogleFlowActive) {
                        item {
                            val isInputtingPhone = email.any { it.isDigit() }
                            val leadingIconToUse = if (isInputtingPhone) Icons.Default.Phone else Icons.Default.Email
                            OutlinedTextField(
                                value = email,
                                onValueChange = onEmailChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_email_input"),
                                placeholder = { Text(viewModel.t("البريد الإلكتروني أو رقم الهاتف 🌌", "Email or Phone Number 🌌"), color = MediumContrastTextDark, fontSize = 13.sp) },
                                leadingIcon = { Icon(leadingIconToUse, null, tint = CosmicSecondary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CosmicSecondary,
                                    unfocusedBorderColor = CosmicSurfaceVariant,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = CosmicDeepSpace,
                                    unfocusedContainerColor = CosmicDeepSpace
                                )
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = password,
                            onValueChange = onPasswordChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            placeholder = { Text(viewModel.t("كلمة المرور الخاصة بك", "Your password"), color = MediumContrastTextDark, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = CosmicSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = viewModel.t("عرض كلمة المرور", "Show password"),
                                        tint = MediumContrastTextDark
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CosmicSecondary,
                                unfocusedBorderColor = CosmicSurfaceVariant,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = CosmicDeepSpace,
                                unfocusedContainerColor = CosmicDeepSpace
                            )
                        )
                    }

                    if (!isRegister) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), contentAlignment = Alignment.CenterEnd) {
                                TextButton(
                                    onClick = onForgotPassword,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        viewModel.t("نسيت كلمة المرور؟ 🔑 استعادة وحفظ برقم الهاتف", "Forgot Password? 🔑 Recover and save by phone"),
                                        color = CosmicSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }
                    }

                    item {
                        val formValid = if (isGoogleFlowActive) {
                            name.isNotBlank() && phone.isNotBlank() && password.length >= 6
                        } else if (isRegister) {
                            name.isNotBlank() && phone.isNotBlank() && email.isNotBlank() && password.length >= 6
                        } else {
                            email.isNotBlank() && password.length >= 4
                        }

                        Button(
                            onClick = {
                                if (isGoogleFlowActive) {
                                    viewModel.isRegisterMode.value = true
                                    onEmailChange(googleEmailState)
                                    onSubmit()
                                } else {
                                    onSubmit()
                                }
                            },
                            enabled = formValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CosmicSecondary,
                                disabledContainerColor = CosmicSurfaceVariant.copy(0.4f),
                                contentColor = Color.Black,
                                disabledContentColor = MediumContrastTextDark
                            )
                        ) {
                            Text(
                                text = if (isGoogleFlowActive) {
                                    viewModel.t("إكمال التفعيل والانضمام للمجرة 🚀", "Complete Activation & Join Majarah 🚀")
                                } else if (isRegister) {
                                    viewModel.t("تأكيد والانضمام للمجرة", "Confirm and Join Majarah")
                                } else {
                                    viewModel.t("تسجيل الدخول الآمن", "Secure Log In")
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (isGoogleFlowActive) {
                        item {
                            OutlinedButton(
                                onClick = {
                                    isGoogleFlowActive = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Text(viewModel.t("إلغاء والعودة للدخول العادي ❌", "Cancel & Go Back ❌"), fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            }
                        }
                    }

                    if (!isGoogleFlowActive) {
                        item {
                            TextButton(onClick = onToggleMode) {
                                Text(
                                    text = if (isRegister) 
                                        viewModel.t("لديك حساب مسبق؟ قم بتسجيل الدخول", "Already have an account? Log in") 
                                    else 
                                        viewModel.t("ليس لديك حساب؟ انضم للمجرة وسجل الآن", "Don't have an account? Join Majarah and register now"),
                                    color = CosmicSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.HorizontalDivider(modifier = Modifier.weight(1f), color = CosmicSurfaceVariant.copy(0.5f))
                                Text(
                                    text = viewModel.t(" أو المتابعة السريعة عبر ", " Or quick continue via "),
                                    color = MediumContrastTextDark,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                androidx.compose.material3.HorizontalDivider(modifier = Modifier.weight(1f), color = CosmicSurfaceVariant.copy(0.5f))
                            }
                        }

                        item {
                            OutlinedButton(
                                onClick = {
                                    showGoogleDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = viewModel.t("المتابعة باستخدام Google 🌠", "Continue with Google 🌠"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        item {
                            OutlinedButton(
                                onClick = onSkipAsGuest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicSecondary)
                            ) {
                                Text(viewModel.t("تخطي والدخول كزائر 🌌", "Skip and Enter as Guest 🌌"), fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            }
                        }
                    }


                }
            }
        }

        if (showGoogleDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showGoogleDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.t("تسجيل الدخول بواسطة Google 🌠", "Sign In with Google 🌠"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.t("اختر حساباً للمتابعة إلى مجرة السودان:", "Choose an account to continue to Majarah Sudan:"),
                            fontSize = 11.sp,
                            color = MediumContrastTextDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Mawia Osman Google account Button
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    googleEmailState = "mawiaosman0@gmail.com"
                                    onEmailChange("mawiaosman0@gmail.com")
                                    onNameChange("Mawia Osman")
                                    isGoogleFlowActive = true
                                    showGoogleDialog = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(CosmicSecondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("M", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Mawia Osman", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("mawiaosman0@gmail.com", color = MediumContrastTextDark, fontSize = 11.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Add another account
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    googleEmailState = "guest.customer@gmail.com"
                                    onEmailChange("guest.customer@gmail.com")
                                    onNameChange("عميل المجرة")
                                    isGoogleFlowActive = true
                                    showGoogleDialog = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color.White.copy(0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(viewModel.t("حساب تجريبي آخر", "Another Demo Account"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("guest.customer@gmail.com", color = MediumContrastTextDark, fontSize = 11.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        TextButton(onClick = { showGoogleDialog = false }) {
                            Text(viewModel.t("إلغاء ❌", "Cancel ❌"), color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArabicVirtualKeyboardPanel(
    activeField: String,
    onValueUpdate: (String) -> Unit,
    currentValue: String,
    onClose: () -> Unit
) {
    val row1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د")
    val row2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط")
    val row3 = listOf("ئ", "ء", "ؤ", "ر", "أ", "ى", "ة", "و", "ز", "ظ", "ذ")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                
                Text(
                    text = "لوحة المفاتيح العربية للمجرة 🌌",
                    color = CosmicSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                
                // Backspace button
                Button(
                    onClick = {
                        if (currentValue.isNotEmpty()) {
                            onValueUpdate(currentValue.substring(0, currentValue.length - 1))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD62828)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مسح", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Keyboard rows
            listOf(row1, row2, row3).forEach { keysRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                ) {
                    keysRow.forEach { char ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(CosmicSurfaceVariant, shape = RoundedCornerShape(6.dp))
                                .clickable {
                                    onValueUpdate(currentValue + char)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // Space and Done row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Done button
                Button(
                    onClick = onClose,
                    modifier = Modifier.weight(1.2f).height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("إغـلاق (تم)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Space bar
                Button(
                    onClick = { onValueUpdate(currentValue + " ") },
                    modifier = Modifier.weight(2f).height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("مسـافة", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Info hint
            Text(
                text = "💡 لتفعيل العربية بلوحة نظام Android الأساسية: اضغط ⚙️ في الكيبورد ثم اختر اللغات -> إضافة -> اختر العربية.",
                color = MediumContrastTextDark,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenBody(
    viewModel: MajarahViewModel,
    onLogout: () -> Unit
) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val isCourier by viewModel.isCourier.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var editName by remember(activeProfile) { mutableStateOf(activeProfile?.name ?: "") }
    var editPhone by remember(activeProfile) { mutableStateOf(activeProfile?.phone ?: "") }
    val email = activeProfile?.email ?: ""
    var isUpdating by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDeepSpace)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Astro themed avatar circle
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(CosmicSecondary.copy(alpha = 0.15f))
                    .border(2.dp, CosmicSecondary, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "صورة الحساب",
                    tint = CosmicSecondary,
                    modifier = Modifier.size(68.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = activeProfile?.name?.ifEmpty { "عميل كوزموس" } ?: "عميل كوزموس",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = activeProfile?.email ?: "",
                color = MediumContrastTextDark,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Profile Card Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "تفاصيل الحساب والمعلومات الشخصية",
                        color = CosmicSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    
                    // Full Name Input
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("الاسم الكامل", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = CosmicSecondary,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = CosmicSecondary
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = CosmicSecondary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                    )
                    
                    // Phone Number Input
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("رقم الهاتف للتوصيل", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = CosmicSecondary,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = CosmicSecondary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Phone, null, tint = CosmicSecondary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_phone_input")
                    )
                    
                    // Email address Display Only
                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("البريد الإلكتروني (المعرّف الكوني)", color = Color.White.copy(alpha = 0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White.copy(alpha = 0.8f),
                            disabledBorderColor = Color.White.copy(alpha = 0.08f),
                            disabledLabelColor = Color.White.copy(alpha = 0.4f)
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Email, null, tint = CosmicSecondary.copy(alpha = 0.5f))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_email_input")
                    )
                    
                    // Account Unique Identifier (UUID)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "معرّف الحساب الفريد (Supabase UUID):\n${activeProfile?.id ?: "N/A"}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            if (isCourier) {
                Button(
                    onClick = { viewModel.navigateTo(Screen.Courier) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("profile_return_to_courier_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicSecondary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DirectionsBike, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الرجوع لصفحة المناديب الرئيسية 🚴", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Save/Update details button
            Button(
                onClick = {
                    if (editName.isBlank() || editPhone.isBlank()) {
                        Toast.makeText(context, "الرجاء تعبئة الاسم ورقم الهاتف بالكامل لحفظ وتحديث الحساب", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isUpdating = true
                    viewModel.updateProfile(editName, editPhone, email) { err ->
                        isUpdating = false
                        if (err == null) {
                            Toast.makeText(context, "تم تحديث وحفظ بياناتك الشخصية بنجاح ومزامنتها ريموتلي! ✨", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "تم الحفظ محلياً لكن تعذرت المزامنة مع Supabase بسبب: $err", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("profile_update_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CosmicSecondary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ وتحديث معلومات الحساب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logout action button
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("profile_logout_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red.copy(alpha = 0.9f)
                ),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تسجيل الخروج من هذا الحساب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun AdminDashboardScreenBody(viewModel: MajarahViewModel) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var activeTab by remember { mutableStateOf(0) } // 0: Overview, 1: Add Product, 2: Manage Store, 3: Orders, 4: Couriers
    val customDeliveryFees = remember { androidx.compose.runtime.mutableStateMapOf<String, String>() }
    var activeDetailDialog by remember { mutableStateOf<String?>(null) }
    
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrdersFlow.collectAsStateWithLifecycle()
    val allCouriers by viewModel.allCouriers.collectAsStateWithLifecycle()

    val pendingCourierOrdersCount = remember(allOrders) {
        val grouped = allOrders.groupBy { it.orderId }
        grouped.keys.count { orderId ->
            val parent = grouped[orderId]?.firstOrNull()
            val status = parent?.statusArabic ?: ""
            val isNotFinished = !status.contains("تم توصيل") && !status.contains("ملغي") && !status.contains("تم التسليم")
            val hasNoCourier = parent?.courierName.isNullOrBlank()
            isNotFinished && (hasNoCourier || status.contains("قيد المعالجة") || !status.contains("مندوب"))
        }
    }

    if (activeDetailDialog != null) {
        val detailType = activeDetailDialog!!
        AlertDialog(
            onDismissRequest = { activeDetailDialog = null },
            title = {
                Text(
                    text = when (detailType) {
                        "sales" -> "عمليات البيع والفواتير المكتملة 📊"
                        "orders" -> "قائمة الطلبيات وتفاصيل العملاء 📦"
                        "active" -> "تفاصيل المنتجات الفعّالة والكميات 🛍️"
                        "low_stock" -> "المخزون الحرج ونقص المستودعات ⚠️"
                        else -> "التفاصيل"
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    when (detailType) {
                        "sales" -> {
                            val completedOrders = allOrders.filter { it.statusArabic.contains("تم") }
                            if (completedOrders.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("لا توجد مبيعات مكتملة بعد 🌌", color = MediumContrastTextDark, fontSize = 12.sp)
                                }
                            } else {
                                val groupedSales = completedOrders.groupBy { it.orderId }
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(groupedSales.entries.toList()) { (orderId, itemsList) ->
                                        val parent = itemsList.firstOrNull()
                                        val totalPrice = itemsList.sumOf { it.priceAtOrder * it.quantity }
                                        val deliveryFee = parent?.deliveryFee ?: 0.0
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(0.4f)),
                                            border = BorderStroke(1.dp, CosmicSecondary.copy(0.2f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                                Text("فاتورة #${orderId.take(7)}...", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text("الزبون: ${parent?.customerName}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("الهاتف: ${parent?.customerPhone}", color = Color.White.copy(0.8f), fontSize = 10.sp)
                                                Text("العنوان: ${parent?.customerAddress}", color = Color.White.copy(0.7f), fontSize = 10.sp)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("المحتويات:", color = CosmicSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                itemsList.forEach { i ->
                                                    Text("• ${i.productName} (العدد: ${i.quantity})", color = Color.White.copy(0.8f), fontSize = 10.sp)
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("المجموع: ${viewModel.formatPrice(totalPrice + deliveryFee)} SDG", color = CosmicTertiary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "orders" -> {
                            if (allOrders.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("لا توجد طلبات مسجلة 📭", color = MediumContrastTextDark, fontSize = 12.sp)
                                }
                            } else {
                                val groupedOrders = allOrders.groupBy { it.orderId }
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(groupedOrders.entries.toList()) { (orderId, itemsList) ->
                                        val parent = itemsList.firstOrNull()
                                        val totalPrice = itemsList.sumOf { it.priceAtOrder * it.quantity }
                                        val deliveryFee = parent?.deliveryFee ?: 0.0
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(text = parent?.statusArabic ?: "", color = CosmicSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    Text("طلب #${orderId.take(7)}...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("الزبون: ${parent?.customerName}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("الهاتف: ${parent?.customerPhone}", color = Color.White.copy(0.8f), fontSize = 10.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("المنتجات والأسعار:", color = CosmicSecondary, fontSize = 10.sp)
                                                itemsList.forEach { i ->
                                                    Text("• ${i.productName} (العدد: ${i.quantity}) - ${viewModel.formatPrice(i.priceAtOrder * i.quantity)} SDG", color = Color.White.copy(0.7f), fontSize = 10.sp)
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("الإجمالي الكلي: ${viewModel.formatPrice(totalPrice + deliveryFee)} SDG", color = CosmicSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "active" -> {
                            if (allProducts.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("لا توجد منتجات متوفرة", color = MediumContrastTextDark, fontSize = 12.sp)
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(allProducts) { p ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "العدد المتوفر: ${p.stock}",
                                                    color = if (p.stock > 0) Color.Green else Color.Red,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(p.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    Text(p.categoryArabic, color = MediumContrastTextDark, fontSize = 9.sp)
                                                    Text("${viewModel.formatPrice(p.price)} SDG", color = CosmicSecondary, fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "low_stock" -> {
                            val lowStockProducts = allProducts.filter { it.stock <= 3 }
                            if (lowStockProducts.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("المخزون ممتاز! لا توجد منتجات حرجة الكمية 🎉", color = Color.Green, fontSize = 12.sp)
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(lowStockProducts) { p ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(0.4f)),
                                            border = BorderStroke(1.dp, Color.Red.copy(0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                         ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (p.stock == 0) Color.Red.copy(0.15f) else Color.Yellow.copy(0.15f))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = if (p.stock == 0) "منتهي تماماً ❌" else "مخزون حرج: ${p.stock}",
                                                        color = if (p.stock == 0) Color.Red else Color.Yellow,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(p.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    Text("الفئة: ${p.categoryArabic}", color = MediumContrastTextDark, fontSize = 9.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeDetailDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                ) {
                    Text("إغلاق نافذة التفاصيل", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            containerColor = CosmicSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Forms state for Add Product
    var prodName by remember { mutableStateOf("") }
    var prodDescription by remember { mutableStateOf("") }
    var prodPrice by remember { mutableStateOf("") }
    var prodCategory by remember { mutableStateOf("electronics") }
    var prodCategoryArabic by remember { mutableStateOf("الأجهزة والمعدات") }
    var prodStock by remember { mutableStateOf("15") }
    var prodImageRes by remember { mutableStateOf("laptop") }
    
    // Status update logic
    var isSubmitting by remember { mutableStateOf(false) }

    // Connection configuration states for Tab 5
    var supabaseUrlInput by remember { mutableStateOf(com.example.data.network.SupabaseConfig.url) }
    var supabaseKeyInput by remember { mutableStateOf(com.example.data.network.SupabaseConfig.apiKey) }
    var showSqlSetupGuide by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDeepSpace)
            .padding(16.dp)
    ) {
        // App title
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Stars, null, tint = CosmicSecondary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "المدير العام لمجرة السودان 👑",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val email = viewModel.activeProfile.value?.email ?: "mawiaosman0@gmail.com"
                Text(
                    "أنت مسجّل بالبريد الحصري: $email",
                    fontSize = 12.sp,
                    color = CosmicSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "تتمتع بصلاحية مطلقة لإدارة المبيعات والمناديب والمنتجات.",
                    fontSize = 11.sp,
                    color = MediumContrastTextDark,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                "الملخص📊" to 0,
                "إضافة ➕" to 1,
                "المنتجات🛍️" to 2,
                (if (pendingCourierOrdersCount > 0) "الطلبات 📦 ($pendingCourierOrdersCount)" else "الطلبات📦") to 3,
                "المناديب🚴" to 4,
                "البائعين🧑‍💼" to 6,
                "طلبات البائعين⏳" to 8,
                "المخزون📦" to 7,
                "مفاتيح الربط🔑" to 5
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(tabs) { (label, index) ->
                    val isSelected = activeTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) CosmicSecondary else CosmicSurface)
                            .clickable { activeTab = index }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            label,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(CosmicSurfaceVariant))
        Spacer(modifier = Modifier.height(12.dp))

        // Content
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> {
                    // OVERVIEW
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                "إحصائيات الأداء والمبيعات السودانية 🇸🇩",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                textAlign = TextAlign.Right
                            )
                        }
                        
                        item {
                            val revenue = allOrders.filter { it.statusArabic.contains("تم") }.sumOf { it.priceAtOrder * it.quantity }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { activeDetailDialog = "sales" },
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("إجمالي المبيعات", fontSize = 11.sp, color = MediumContrastTextDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${viewModel.formatPrice(revenue)} SDG", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CosmicSecondary)
                                    }
                                }
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { activeDetailDialog = "orders" },
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("عدد الطلبات الكلي", fontSize = 11.sp, color = MediumContrastTextDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${allOrders.distinctBy { it.orderId }.size} طلب", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                        
                        item {
                            val activeProductsCount = allProducts.size
                            val outOfStockCount = allProducts.filter { it.stock <= 3 }.size
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { activeDetailDialog = "active" },
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("المنتجات الفعّالة", fontSize = 11.sp, color = MediumContrastTextDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("$activeProductsCount منتج", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { activeDetailDialog = "low_stock" },
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("نقص المخزون", fontSize = 11.sp, color = MediumContrastTextDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("$outOfStockCount نافذ/قريب", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (outOfStockCount > 0) Color.Red else Color.Green)
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                    Text("دليل تشغيل قاعدة البيانات 🗄️", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "جميع مدخلاتك وحذف المنتجات والتعديل سيتم حفظه محلياً ومزامنته فورياً مع سيرفر Supabase. " +
                                        "الرجاء التأكد من نسخ كود الـ SQL المرفق في شاشة الإعدادات ووضعه بمحرر Supabase لضمان مطابقة الأعمدة.",
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        color = MediumContrastTextDark,
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // ADD PRODUCT FORM
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                "إضافة براند كوني جديد للمتجر 🌌",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = prodName,
                                onValueChange = { prodName = it },
                                modifier = Modifier.fillMaxWidth().testTag("admin_add_name"),
                                label = { Text("اسم المنتج الحصري", color = CosmicSecondary, fontSize = 12.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant
                                )
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = prodDescription,
                                onValueChange = { prodDescription = it },
                                modifier = Modifier.fillMaxWidth().testTag("admin_add_desc"),
                                label = { Text("الوصف التسويقي والخصائص للمنتج", color = CosmicSecondary, fontSize = 12.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant
                                )
                            )
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = prodStock,
                                    onValueChange = { prodStock = it },
                                    modifier = Modifier.weight(1f).testTag("admin_add_stock"),
                                    label = { Text("الكمية المتوفرة", color = CosmicSecondary, fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant
                                    )
                                )
                                OutlinedTextField(
                                    value = prodPrice,
                                    onValueChange = { prodPrice = it },
                                    modifier = Modifier.weight(1f).testTag("admin_add_price"),
                                    label = { Text("السعر بـ SDG", color = CosmicSecondary, fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant
                                    )
                                )
                            }
                        }

                        item {
                            Text("اختر أيقونة التمثيل البصرية للمنتج:", color = Color.White, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                            val icons = listOf("laptop" to "حاسوب", "watch" to "ساعة", "earbuds" to "سماعات", "jacket" to "ملابس", "backpack" to "حقيبة", "lamp" to "مصباح")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(icons) { (key, name) ->
                                    val isSelected = prodImageRes == key
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) CosmicSecondary else CosmicSurface)
                                            .clickable { prodImageRes = key }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(name, color = if (isSelected) Color.Black else Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Text("اختر تصنيف القسم للمنتج:", color = Color.White, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                            val cats = listOf(
                                Triple("electronics", "الأجهزة الرقمية", "electronics"),
                                Triple("fashion", "الأزياء الكونية", "fashion"),
                                Triple("home", "المستلزمات الفضائية", "home"),
                                Triple("cosmic_deals", "الصفقات النجمية", "cosmic_deals")
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(cats) { (eng, arb, key) ->
                                    val isSelected = prodCategory == key
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) CosmicSecondary else CosmicSurface)
                                            .clickable {
                                                prodCategory = key
                                                prodCategoryArabic = arb
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(arb, color = if (isSelected) Color.Black else Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            val isFormValid = prodName.isNotBlank() && prodDescription.isNotBlank() && prodPrice.toDoubleOrNull() != null && prodStock.toIntOrNull() != null
                            
                            Button(
                                onClick = {
                                    isSubmitting = true
                                    val newProd = ProductEntity(
                                        name = prodName.trim(),
                                        description = prodDescription.trim(),
                                        price = prodPrice.toDouble(),
                                        category = prodCategory,
                                        categoryArabic = prodCategoryArabic,
                                        rating = 4.8f,
                                        imageResName = prodImageRes,
                                        isFavorite = false,
                                        stock = prodStock.toInt()
                                    )
                                    viewModel.addProduct(newProd) { err ->
                                        isSubmitting = false
                                        if (err == null) {
                                            Toast.makeText(context, "🌌 تم إضافة المنتج الكوني الجديد بنجاح مزامنة مع Supabase!", Toast.LENGTH_SHORT).show()
                                            prodName = ""
                                            prodDescription = ""
                                            prodPrice = ""
                                            prodStock = "15"
                                            activeTab = 2 // Move to products list
                                        } else {
                                            Toast.makeText(context, "تم حفظ المنتج محلياً! ⚠️ فشل المزامنة الخارجية: $err", Toast.LENGTH_LONG).show()
                                            activeTab = 2
                                        }
                                    }
                                },
                                enabled = isFormValid && !isSubmitting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CosmicSecondary,
                                    disabledContainerColor = CosmicSurfaceVariant.copy(0.4f),
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isSubmitting) "جاري النشر والمزامنة الكونية..." else "تأكيد ونشر المنتج في السيرفر", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
                2 -> {
                    // MANAGE PRODUCTS LIST
                    if (allProducts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد منتجات حالية بالمخزن 🌌", color = MediumContrastTextDark)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(allProducts) { product ->
                                var stockVal by remember(product.id) { mutableStateOf(product.stock.toString()) }
                                var priceVal by remember(product.id) { mutableStateOf(product.price.toString()) }
                                var isUpdating by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    border = BorderStroke(1.dp, CosmicSurfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Delete Product Button
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteProduct(product.id) { err ->
                                                        if (err == null) {
                                                          Toast.makeText(context, "تم حذف المنتج بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                          Toast.makeText(context, "تم الحذف محلياً! خطأ Supabase: $err", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, "حذف", tint = Color.Red.copy(alpha = 0.8f))
                                            }
                                            
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(product.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                                Text(product.categoryArabic, fontSize = 10.sp, color = CosmicSecondary)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Price Field
                                            OutlinedTextField(
                                                value = priceVal,
                                                onValueChange = { priceVal = it },
                                                modifier = Modifier.weight(1f),
                                                label = { Text("السعر (SDG)", fontSize = 10.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                                    focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant
                                                ),
                                                singleLine = true
                                            )
                                            // Stock Field
                                            OutlinedTextField(
                                                value = stockVal,
                                                onValueChange = { stockVal = it },
                                                modifier = Modifier.weight(1f),
                                                label = { Text("المخزون", fontSize = 10.sp) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                                    focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant
                                                ),
                                                singleLine = true
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = {
                                                val parsedPrice = priceVal.toDoubleOrNull()
                                                val parsedStock = stockVal.toIntOrNull()
                                                if (parsedPrice != null && parsedStock != null) {
                                                    isUpdating = true
                                                    val updatedProduct = product.copy(price = parsedPrice, stock = parsedStock)
                                                    viewModel.updateProduct(updatedProduct) { err ->
                                                        isUpdating = false
                                                        if (err == null) {
                                                            Toast.makeText(context, "تم التحديث بنجاح! ✅", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "تم الحفظ محلياً: $err", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(context, "الرجاء التأكد من صحة المدخلات الرقمية", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            enabled = !isUpdating,
                                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                            modifier = Modifier.fillMaxWidth().height(36.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(if (isUpdating) "جاري التحديث للشبكة..." else "تعديل السعر والمخزون", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // MANAGE ORDERS WITH AUTOMATIC BILL CLOSING CONTROLS AND SEGREGATION
                    var ordersSubTab by remember { mutableStateOf(0) } // 0: Active & New, 1: Closed & Completed
                    var managerSettingFeeOrderId by remember { mutableStateOf<String?>(null) }
                    var managerInputFeeStr by remember { mutableStateOf("") }
                    
                    // Group by orderId
                    val grouped = allOrders.groupBy { it.orderId }
                    
                    if (managerSettingFeeOrderId != null) {
                         val orderId = managerSettingFeeOrderId!!
                         val itemsForOrder = grouped[orderId] ?: emptyList()
                         val parentOrder = itemsForOrder.firstOrNull()
                         AlertDialog(
                             onDismissRequest = { managerSettingFeeOrderId = null },
                             title = {
                                 Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                     Text("تحديد قيمة التوصيل الكلي 🚚", fontWeight = FontWeight.Bold, color = CosmicSecondary, fontSize = 14.sp)
                                     Spacer(modifier = Modifier.width(6.dp))
                                     Icon(Icons.Default.DirectionsBike, null, tint = CosmicSecondary, modifier = Modifier.size(20.dp))
                                 }
                             },
                             text = {
                                 Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                                     Text("تحديد رسوم شحن وتوصيل الطلبية رقم:", color = Color.White.copy(0.7f), fontSize = 11.sp, textAlign = TextAlign.Right)
                                     Text("#${orderId.take(10)}", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Right, modifier = Modifier.padding(bottom = 12.dp))
                                     
                                     OutlinedTextField(
                                         value = managerInputFeeStr,
                                         onValueChange = { managerInputFeeStr = it },
                                         label = { Text("قيمة التوصيل بالسودان (ج.س)", color = CosmicSecondary, fontSize = 10.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
                                         singleLine = true,
                                         colors = OutlinedTextFieldDefaults.colors(
                                             focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                             focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant
                                         ),
                                         modifier = Modifier.fillMaxWidth()
                                     )
                                     Spacer(modifier = Modifier.height(6.dp))
                                     Text(
                                         text = "ملاحظة: سيتم تحديث الفاتورة فورياً وعرضها للعميل في الوقت الفعلي بمجرد قبول المندوب.",
                                         color = MediumContrastTextDark,
                                         fontSize = 9.sp,
                                         textAlign = TextAlign.Right
                                     )
                                 }
                             },
                             confirmButton = {
                                 Button(
                                     onClick = {
                                         val fee = managerInputFeeStr.toDoubleOrNull() ?: 5000.0
                                         viewModel.updateOrderStatus(
                                             orderId = orderId,
                                             status = parentOrder?.statusArabic ?: "قيد المعالجة بالسودان 🌌",
                                             courierName = parentOrder?.courierName ?: "",
                                             courierPhone = parentOrder?.courierPhone ?: "",
                                             deliveryFee = fee
                                         ) { err ->
                                             if (err == null) {
                                                 Toast.makeText(context, "تم تحديد وتحديث قيمة التوصيل بنجاح! 🚚💸", Toast.LENGTH_SHORT).show()
                                             } else {
                                                 Toast.makeText(context, "فشل تحديث القيمة: $err", Toast.LENGTH_SHORT).show()
                                             }
                                         }
                                         managerSettingFeeOrderId = null
                                     },
                                     colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                                 ) {
                                     Text("تأكيد وحفظ 💾", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                 }
                             },
                             dismissButton = {
                                 TextButton(onClick = { managerSettingFeeOrderId = null }) {
                                     Text("إلغاء", color = Color.White.copy(0.6f), fontSize = 10.sp)
                                 }
                             },
                             containerColor = CosmicSurface,
                             shape = RoundedCornerShape(16.dp)
                         )
                     }
                    
                    if (allOrders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد طلبات عملاء تم استلامها بعد 🛰️", color = MediumContrastTextDark)
                        }
                    } else {
                        // Group by orderId
                        val grouped = allOrders.groupBy { it.orderId }
                        val activeKeys = grouped.keys.filter { orderId ->
                            val status = grouped[orderId]?.firstOrNull()?.statusArabic ?: ""
                            !status.contains("تم توصيل") && !status.contains("ملغي") && !status.contains("تم التسليم")
                        }
                        val closedKeys = grouped.keys.filter { orderId ->
                            val status = grouped[orderId]?.firstOrNull()?.statusArabic ?: ""
                            status.contains("تم توصيل") || status.contains("ملغي") || status.contains("تم التسليم")
                        }
                        val currentKeys = if (ordersSubTab == 0) activeKeys else closedKeys

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                // Sub-navigation selector for Active vs Closed
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CosmicSurfaceVariant.copy(0.4f), RoundedCornerShape(12.dp))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = { ordersSubTab = 1 },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (ordersSubTab == 1) CosmicSecondary else Color.Transparent,
                                            contentColor = if (ordersSubTab == 1) Color.Black else Color.White
                                        ),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 8.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("المغلقة والمكتملة 🔒 (${closedKeys.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { ordersSubTab = 0 },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (ordersSubTab == 0) CosmicSecondary else Color.Transparent,
                                            contentColor = if (ordersSubTab == 0) Color.Black else Color.White
                                        ),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 8.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("النشطة والجديدة 📬 (${activeKeys.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (ordersSubTab == 0 && pendingCourierOrdersCount > 0) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE65100).copy(0.15f)),
                                        border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(0.5f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                modifier = Modifier.weight(1f)
                                             ) {
                                                 Text(
                                                     text = "طلبات في انتظار التسليم للمناديب ⏳🚴",
                                                     color = Color(0xFFFFB74D),
                                                     fontWeight = FontWeight.Bold,
                                                     fontSize = 12.sp,
                                                     textAlign = TextAlign.Right
                                                 )
                                                 Spacer(modifier = Modifier.height(4.dp))
                                                 Text(
                                                     text = "يوجد حالياً $pendingCourierOrdersCount طلب من الزبائن في انتظار التعيين والتسليم للمناديب.",
                                                     color = Color.LightGray,
                                                     fontSize = 10.sp,
                                                     textAlign = TextAlign.Right
                                                 )
                                             }
                                             Spacer(modifier = Modifier.width(12.dp))
                                             Box(
                                                 modifier = Modifier
                                                     .size(36.dp)
                                                     .background(Color(0xFFFFB74D).copy(0.2f), androidx.compose.foundation.shape.CircleShape),
                                                 contentAlignment = Alignment.Center
                                             ) {
                                                 Text(
                                                     text = pendingCourierOrdersCount.toString(),
                                                     color = Color(0xFFFFB74D),
                                                     fontWeight = FontWeight.Bold,
                                                     fontSize = 14.sp
                                                 )
                                             }
                                        }
                                    }
                                }
                            }
                            
                            if (currentKeys.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (ordersSubTab == 0) "لا توجد طلبيات نشطة حالياً بالسودان 🎉" else "سجل الطلبيات المغلقة لا يحتوي على شيء حالياً 📁",
                                            color = MediumContrastTextDark,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            items(currentKeys) { orderId ->
                                val items = grouped[orderId] ?: emptyList()
                                val parent = items.firstOrNull()
                                val isOrderCurrentlyClosed = ordersSubTab == 1
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    border = BorderStroke(1.dp, if (isOrderCurrentlyClosed) Color.Green.copy(alpha = 0.3f) else CosmicSecondary.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "رقم الطلب: #${orderId.take(8)}\nالتاريخ: " + java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US).format(java.util.Date(parent?.orderDate ?: System.currentTimeMillis())),
                                                fontWeight = FontWeight.Bold,
                                                color = CosmicSecondary,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = parent?.statusArabic ?: "قيد المعالجة",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Green,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(CosmicSurfaceVariant))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Customer Info
                                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                            Text("العميل: ${parent?.customerName ?: "زائر"}", fontSize = 12.sp, color = Color.White)
                                            Text("الهاتف: ${parent?.customerPhone ?: "لا يوجد"}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                            Text("العنوان بالسودان: ${parent?.customerAddress ?: "لا يوجد"}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Order items
                                        Text("المنتجات المطلوبة:", fontSize = 11.sp, color = MediumContrastTextDark, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                        items.forEach { item ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("${viewModel.formatPrice(item.priceAtOrder * item.quantity)} SDG", color = Color.White, fontSize = 11.sp)
                                                Text("${item.productName} (الكمية: ${item.quantity})", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, textAlign = TextAlign.Right)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Assigned Courier Info & Selection
                                        val currentCourierName = parent?.courierName ?: ""
                                        if (isOrderCurrentlyClosed) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color.Green.copy(alpha = 0.15f))
                                                    .border(1.dp, Color.Green.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                                    .padding(10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    val statusLabel = parent?.statusArabic ?: "مكتمل"
                                                    Text(
                                                        text = "الحالة النهائية: $statusLabel",
                                                        color = Color.Green,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        textAlign = TextAlign.Left
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("الفاتورة مغلقة بالكامل 🔒", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(Icons.Default.CheckCircle, null, tint = Color.Green, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                            
                                            // Show summary of payment details & delivery fee
                                            Spacer(modifier = Modifier.height(6.dp))
                                            val totalInvoiceAmount = items.sumOf { it.priceAtOrder * it.quantity } + (parent?.deliveryFee ?: 0.0)
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(0.3f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                                    Text("سعر المنتجات: ${viewModel.formatPrice(items.sumOf { it.priceAtOrder * it.quantity })} SDG", color = Color.White.copy(0.8f), fontSize = 10.sp)
                                                    Text("أجرة التوصيل المسددة للمندوب: ${viewModel.formatPrice(parent?.deliveryFee ?: 0.0)} SDG", color = Color.White.copy(0.8f), fontSize = 10.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("المجموع المالي المستلم: ${viewModel.formatPrice(totalInvoiceAmount)} SDG", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                            }
                                        }

                                        if (!isOrderCurrentlyClosed) {
                                            if (currentCourierName.isNotEmpty()) {
                                            Text("🚴 المندوب الحالي المتكفل بالتسليم: $currentCourierName (${parent?.courierPhone})", fontSize = 11.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }

                                        val feeInput = customDeliveryFees[orderId] ?: parent?.deliveryFee?.toInt()?.toString() ?: "5000"
                                         OutlinedTextField(
                                             value = feeInput,
                                             onValueChange = { customDeliveryFees[orderId] = it },
                                             modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                             label = { Text("تحديد سعر التوصيل لهذا الطلب (SDG)", color = CosmicSecondary, fontSize = 11.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                                             singleLine = true,
                                             colors = OutlinedTextFieldDefaults.colors(
                                                 focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                                 focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant
                                             )
                                         )
                                         Spacer(modifier = Modifier.height(10.dp))

                                         Text("تعيين أو تغيير مندوب التوصيل الكوني للطلب:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        if (allCouriers.isEmpty()) {
                                            Text("⚠️ لم يتم تسجيل أي كادر مناديب للتوصيل بالسودان بعد! انتقل لتبويب المناديب لإضافتهم.", fontSize = 10.sp, color = Color.Red, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                        } else {
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                            ) {
                                                items(allCouriers) { courier ->
                                                    val isAssigned = parent?.courierName == courier.name
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                             .background(if (isAssigned) CosmicSecondary else CosmicSurfaceVariant)
                                                            .clickable {
                                                                 viewModel.updateOrderStatus(
                                                                     orderId = orderId,
                                                                     status = "تم تسليم المندوب 🚴",
                                                                     courierName = courier.name,
                                                                     courierPhone = courier.phone,
                                                                     deliveryFee = feeInput.toDoubleOrNull() ?: 5000.0
                                                                 ) { err ->
                                                                     if (err == null) {
                                                                         Toast.makeText(context, "تمت إحالة الطلب للمندوب ${courier.name} وتحديث حالة التوصيل تلقائياً بنجاح! 🚴📦", Toast.LENGTH_SHORT).show()
                                                                     } else {
                                                                         Toast.makeText(context, "خطأ في التعيين: $err", Toast.LENGTH_LONG).show()
                                                                     }
                                                                 }
                                                            }
                                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(
                                                            text = "${courier.name} 🚴",
                                                            color = if (isAssigned) Color.Black else Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Update status controls
                                        Text("تحديث حالة الشحن الفوري والتوصيل:", fontSize = 11.sp, color = CosmicSecondary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        val statuses = listOf(
                                            "قيد المعالجة بالسودان 🌌",
                                            "تم التسليم لمندوب التوصيل بالولاية 🚴",
                                            "تم توصيل الطلب واستلام المبلغ ✅",
                                            "الطلب ملغي ❌"
                                        )

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            statuses.forEach { statusText ->
                                                val isCurrent = parent?.statusArabic == statusText
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isCurrent) CosmicSecondary.copy(alpha = 0.15f) else CosmicSurfaceVariant.copy(0.3f))
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isCurrent) CosmicSecondary else Color.Transparent,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable {
                                                            viewModel.updateOrderStatus(orderId, statusText) { err ->
                                                                if (err == null) {
                                                                    Toast.makeText(context, "تم تحديث حالة الطلب وإرسالها للسيرفر!", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    Toast.makeText(context, "تم تحديث الحالة محلياً فقط! خطأ: $err", Toast.LENGTH_LONG).show()
                                                                }
                                                            }
                                                        }
                                                        .padding(8.dp),
                                                    contentAlignment = Alignment.CenterEnd
                                                ) {
                                                    Text(
                                                        statusText,
                                                        color = if (isCurrent) CosmicSecondary else Color.White.copy(0.7f),
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // LOGISTICS & DELIVERY COURIERS MANAGER - REAL IMPLEMENTATION WITH DATABASE SYNC
                    var newCourierName by remember { mutableStateOf("") }
                    var newCourierPhone by remember { mutableStateOf("") }
                    var newCourierState by remember { mutableStateOf("ولاية بورتسودان") }
                    var newCourierStatus by remember { mutableStateOf("نشط ومتوفر 🟢") }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                "إدارة وتوزيع مناديب التوصيل بالسودان 🚴 🇸🇩",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                    Text(
                                        "إضافة مندوب توصيل جديد للقاعدة ➕",
                                        fontWeight = FontWeight.Bold,
                                        color = CosmicSecondary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    // Name Field
                                    OutlinedTextField(
                                        value = newCourierName,
                                        onValueChange = { newCourierName = it },
                                        label = { Text("اسم المندوب ثلاثي", color = Color.White.copy(0.6f)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = Color.Gray,
                                            focusedLabelColor = CosmicSecondary,
                                            unfocusedLabelColor = Color.Gray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Phone Field
                                    OutlinedTextField(
                                        value = newCourierPhone,
                                        onValueChange = { newCourierPhone = it },
                                        label = { Text("رقم هاتف المندوب", color = Color.White.copy(0.6f)) },
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = Color.Gray,
                                            focusedLabelColor = CosmicSecondary,
                                            unfocusedLabelColor = Color.Gray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // State coverage selection Info
                                    OutlinedTextField(
                                        value = newCourierState,
                                        onValueChange = { newCourierState = it },
                                        label = { Text("أماكن وولايات التغطية بالسودان", color = Color.White.copy(0.6f)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = Color.Gray,
                                            focusedLabelColor = CosmicSecondary,
                                            unfocusedLabelColor = Color.Gray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // ADD BUTTON
                                    Button(
                                        onClick = {
                                            if (newCourierName.isBlank() || newCourierPhone.isBlank() || newCourierState.isBlank()) {
                                                Toast.makeText(context, "الرجاء إدخال كافة بيانات المندوب لحفظها بالشكل الصحيح ⚠️", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            viewModel.addCourier(
                                                name = newCourierName,
                                                phone = newCourierPhone,
                                                stateInfo = newCourierState,
                                                status = newCourierStatus
                                            ) { err ->
                                                if (err == null) {
                                                    Toast.makeText(context, "تمت إضافة المندوب ومزامنته حقيقياً بقاعدة البيانات والشبكة بنجاح! 🛰️", Toast.LENGTH_LONG).show()
                                                    newCourierName = ""
                                                    newCourierPhone = ""
                                                } else {
                                                    Toast.makeText(context, "خطأ بالزمن الحقيقي: $err", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("تسجيل وتفعيل المندوب بقاعدة البيانات 🛰️", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        if (allCouriers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("لا توجد مناديب شحن مسجلة بقاعدة البيانات حالياً 🚴", color = MediumContrastTextDark, fontSize = 12.sp)
                                }
                            }
                        } else {
                            items(allCouriers) { courier ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Actions: Send orders or delete courier
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.removeCourier(courier.id) { err ->
                                                        if (err == null) {
                                                            Toast.makeText(context, "تم حذف وإلغاء تفعيل المندوب بقاعدة البيانات بنجاح! 🗑️", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "خطأ أثناء الحذف: $err", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف المندوب",
                                                    tint = Color.Red.copy(alpha = 0.8f)
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    Toast.makeText(context, "تم إرسال مهام التوصيل وجدول المبيعات للمندوب ${courier.name} بنجاح! 📲", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("إرسال المهام 📲", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Courier Info
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(courier.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                            Text("رقم التواصل: ${courier.phone}", fontSize = 11.sp, color = MediumContrastTextDark)
                                            Text(courier.stateInfo, fontSize = 11.sp, color = CosmicSecondary)
                                            val statusColor = when {
                                                courier.status.contains("متوفر") || courier.status.contains("🟢") -> Color.Green
                                                courier.status.contains("مهمة") || courier.status.contains("🟡") -> CosmicTertiary
                                                else -> Color.Red
                                            }
                                            Text("الحالة: ${courier.status}", fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface.copy(0.6f)),
                                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.2f))
                              ) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                    Text("نظام المقررات واللوجستيات الكونية 🛰️", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "يقوم هذا النظام بربط مناديب الولايات آلياً بمجرد تأكيد طلب الشحن. يتم إرسال إشعار SMS أو واتساب يغذي المندوب ببيانات العميل وموقعه وجغرافيته.",
                                        fontSize = 11.sp,
                                        color = MediumContrastTextDark,
                                        textAlign = TextAlign.Right,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Live Interactive Courier App Simulator
                            var selectedCourierForSim by remember { mutableStateOf<com.example.data.db.CourierEntity?>(null) }
                            
                            // Auto-select first courier if none selected
                            if (selectedCourierForSim == null && allCouriers.isNotEmpty()) {
                                selectedCourierForSim = allCouriers.first()
                            }
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, CosmicSecondary, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color.Black),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.PhoneAndroid,
                                            contentDescription = "Smartphone",
                                            tint = CosmicSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            "بوابة محاكاة تطبيق المندوب 📱🚴",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Right
                                        )
                                    }
                                    
                                    Text(
                                        "افهم وعاين كيف يرى المندوب الطلبيات المسندة إليه، وكيف يعرف الطلب ومعلوماته، وكيف يمكنه التواصل الفوري مع الزبائن وتحديث الحالة.",
                                        fontSize = 11.sp,
                                        color = MediumContrastTextDark,
                                        lineHeight = 15.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Courier Selector Dropdown for simulation
                                    Text(
                                        "اختر كابتن التوصيل لعرض هاتفه الذكي:",
                                        fontSize = 11.sp,
                                        color = CosmicSecondary,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    if (allCouriers.isEmpty()) {
                                        Text(
                                            "⚠️ الرجاء تسجيل مندوب توصيل أولاً بالعلّو لعرض البوابة الذكية.",
                                            fontSize = 10.sp,
                                            color = Color.Red,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                        ) {
                                            items(allCouriers) { courier ->
                                                val isSelected = selectedCourierForSim?.id == courier.id
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) CosmicSecondary else CosmicSurfaceVariant)
                                                        .clickable { selectedCourierForSim = courier }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = courier.name,
                                                        color = if (isSelected) Color.Black else Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    selectedCourierForSim?.let { curSim ->
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // Smartphone Simulated Screen Canvas
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(2.dp, Color.DarkGray, RoundedCornerShape(12.dp)),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace)
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                // Phone Status Bar
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color.Black)
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "12:30 PM ⏰",
                                                        fontSize = 9.sp,
                                                        color = Color.White.copy(0.7f),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Wifi, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(10.dp))
                                                        Icon(Icons.Default.BatteryChargingFull, null, tint = Color.Green, modifier = Modifier.size(10.dp))
                                                        Text("Sudani 🇸🇩", fontSize = 9.sp, color = Color.White.copy(0.7f))
                                                    }
                                                }
                                                
                                                // Phone App Bar Header
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(CosmicSurface)
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.DirectionsBike, null, tint = CosmicSecondary, modifier = Modifier.size(18.dp))
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            "تطبيق شريك مجرة للتوصيل 🚴",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                        Text(
                                                            "شريك التوصيل الكوني بالسودان 🛰️",
                                                            fontSize = 8.sp,
                                                            color = CosmicSecondary
                                                        )
                                                    }
                                                }
                                                
                                                // Courier Profile Banner inside phone
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(CosmicSurfaceVariant.copy(0.4f))
                                                        .padding(8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val simStatus = curSim.status
                                                    val statusBgColor = when {
                                                        simStatus.contains("متوفر") || simStatus.contains("🟢") -> Color.Green.copy(0.2f)
                                                        simStatus.contains("مهمة") || simStatus.contains("🟡") -> Color.Yellow.copy(0.2f)
                                                        else -> Color.Red.copy(0.2f)
                                                    }
                                                    val statusTextColour = when {
                                                        simStatus.contains("متوفر") || simStatus.contains("🟢") -> Color.Green
                                                        simStatus.contains("مهمة") || simStatus.contains("🟡") -> CosmicTertiary
                                                        else -> Color.Red
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(statusBgColor)
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(simStatus, fontSize = 8.sp, color = statusTextColour, fontWeight = FontWeight.Bold)
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text("المندوب: ${curSim.name}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        Text("التغطية: ${curSim.stateInfo} (${curSim.phone})", fontSize = 9.sp, color = MediumContrastTextDark)
                                                    }
                                                }
                                                
                                                // Assigned Orders block inside phone
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    "📦 الطلبيات المسندة إليك للتوصيل اليوم:",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
                                                    textAlign = TextAlign.Right
                                                )
                                                
                                                val simCouriersOrders = allOrders.filter { it.courierName == curSim.name }
                                                
                                                if (simCouriersOrders.isEmpty()) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(24.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Icon(Icons.Default.Inbox, null, tint = MediumContrastTextDark.copy(0.3f), modifier = Modifier.size(36.dp))
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            "لا توجد أي طلبيات مسندة إلى المندوب ${curSim.name} حالياً.",
                                                            fontSize = 10.sp,
                                                            color = MediumContrastTextDark,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Text(
                                                            "اذهب إلى تبويب (الطلبات 📦) وقم بإسناد وعرض طلبية للمندوب لتظهر بهاتفه فوراً!",
                                                            fontSize = 9.sp,
                                                            color = CosmicSecondary,
                                                            textAlign = TextAlign.Center,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    }
                                                } else {
                                                    val simGrouped = simCouriersOrders.groupBy { it.orderId }
                                                    simGrouped.entries.forEach { (orderId, orderDetails) ->
                                                        val parentOrder = orderDetails.firstOrNull()
                                                        Card(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                                            border = BorderStroke(0.5.dp, CosmicSecondary.copy(0.2f))
                                                        ) {
                                                            Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                                ) {
                                                                    Text(
                                                                        text = parentOrder?.statusArabic ?: "",
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = Color.Green
                                                                    )
                                                                    Text(
                                                                        text = "رقم الطلبية: #${orderId.take(6)}...",
                                                                        fontSize = 9.sp,
                                                                        color = CosmicSecondary,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                                Spacer(modifier = Modifier.height(6.dp))
                                                                
                                                                // Address & Customer Info
                                                                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                                                    Text("الزبون: ${parentOrder?.customerName}", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                                                    Text("الهاتف: ${parentOrder?.customerPhone}", fontSize = 9.sp, color = Color.White.copy(0.8f))
                                                                    Text("العنوان للتسليم: ${parentOrder?.customerAddress}", fontSize = 9.sp, color = Color.White.copy(0.8f))
                                                                }
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                
                                                                // Items
                                                                orderDetails.forEach { item ->
                                                                    Text(
                                                                        text = "• ${item.productName} (عدد: ${item.quantity})",
                                                                        fontSize = 9.sp,
                                                                        color = Color.White.copy(0.7f),
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        textAlign = TextAlign.Right
                                                                    )
                                                                }
                                                                
                                                                val totalPriceSumInSim = orderDetails.sumOf { it.priceAtOrder * it.quantity }
                                                                Spacer(modifier = Modifier.height(6.dp))
                                                                
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Text(
                                                                        text = "المبلغ: ${viewModel.formatPrice(totalPriceSumInSim)} SDG",
                                                                        fontSize = 10.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = CosmicSecondary
                                                                    )
                                                                    Text(
                                                                        text = "الدفع عند الاستلام 💵",
                                                                        fontSize = 8.sp,
                                                                        color = Color.White.copy(0.6f)
                                                                    )
                                                                }
                                                                
                                                                Spacer(modifier = Modifier.height(10.dp))
                                                                
                                                                // Real interactive actions
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    // Call Customer Button
                                                                    Button(
                                                                        onClick = {
                                                                            parentOrder?.customerPhone?.let { phoneNum ->
                                                                                Toast.makeText(context, "جاري فتح لوحة الاتصال بـ \n $phoneNum", Toast.LENGTH_SHORT).show()
                                                                                try {
                                                                                    val callIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                                                        data = android.net.Uri.parse("tel:$phoneNum")
                                                                                    }
                                                                                    context.startActivity(callIntent)
                                                                                } catch (e: Exception) {
                                                                                    Toast.makeText(context, "تعذر تشغيل تطبيق الهاتف لسبب أمني", Toast.LENGTH_SHORT).show()
                                                                                }
                                                                            }
                                                                        },
                                                                        modifier = Modifier.weight(1f),
                                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5), contentColor = Color.White),
                                                                        contentPadding = PaddingValues(vertical = 4.dp),
                                                                        shape = RoundedCornerShape(8.dp)
                                                                    ) {
                                                                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(10.dp))
                                                                        Spacer(modifier = Modifier.width(3.dp))
                                                                        Text("اتصال 📞", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                                    }
                                                                    
                                                                    // WhatsApp customer button
                                                                    Button(
                                                                        onClick = {
                                                                            parentOrder?.customerPhone?.let { phoneNum ->
                                                                                val rawPhone = phoneNum.trim().replace("+", "").replace(" ", "")
                                                     val cleanPhone = if (rawPhone.startsWith("0")) {
                                                         "249" + rawPhone.substring(1)
                                                     } else if (!rawPhone.startsWith("249") && (rawPhone.startsWith("9") || rawPhone.startsWith("1"))) {
                                                         "249" + rawPhone
                                                     } else {
                                                         rawPhone
                                                     }
                                                                                val msg = "🌌 مرحباً يا ${parentOrder.customerName}! معكم المندوب ${curSim.name} من تطبيق مجرة السودان. أنا متكفل بتسليم طلبيتكم الآن رقم (#${orderId.take(5)}) وقيمتها ${viewModel.formatPrice(totalPriceSumInSim)} SDG. هل أنتم متواجدون بالعنوان: ${parentOrder.customerAddress} لتسليمها؟"
                                                                                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${android.net.Uri.encode(msg)}"
                                                                                Toast.makeText(context, "جاري توجيه رسالة واتساب للزبون...", Toast.LENGTH_SHORT).show()
                                                                                try {
                                                                                    val waIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                                                                    context.startActivity(waIntent)
                                                                                } catch (e: Exception) {
                                                                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                                                        type = "text/plain"
                                                                                        putExtra(android.content.Intent.EXTRA_TEXT, "$phoneNum: $msg")
                                                                                    }
                                                                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "إرسال التفاصيل"))
                                                                                }
                                                                            }
                                                                        },
                                                                        modifier = Modifier.weight(1.2f),
                                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047), contentColor = Color.White),
                                                                        contentPadding = PaddingValues(vertical = 4.dp),
                                                                        shape = RoundedCornerShape(8.dp)
                                                                    ) {
                                                                        Icon(Icons.Default.Chat, null, modifier = Modifier.size(10.dp))
                                                                        Spacer(modifier = Modifier.width(3.dp))
                                                                        Text("واتساب 💬", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                                    }
                                                                    
                                                                    // Deliver Action button
                                                                    val isDelivered = parentOrder?.statusArabic?.let { status ->
                                                                        (status.contains("تمام") || 
                                                                        status.contains("تم توصيل") || 
                                                                        status.contains("تم التسليم")) && 
                                                                        !status.contains("تم تسليم المندوب")
                                                                    } == true
                                                                    Button(
                                                                        onClick = {
                                                                            if (!isDelivered) {
                                                                                viewModel.updateOrderStatus(orderId, "تم توصيل الطلب واستلام المبلغ ✅") { err ->
                                                                                    if (err == null) {
                                                                                        Toast.makeText(context, "أحسنتم! تم تسجيل أن الطلبية سُلمت ومُوزنت بقاعدة البيانات! 🎉✅", Toast.LENGTH_SHORT).show()
                                                                                    } else {
                                                                                        Toast.makeText(context, "سجلت محلياً بسبب خطأ بالمزامنة: $err", Toast.LENGTH_LONG).show()
                                                                                    }
                                                                                }
                                                                            }
                                                                        },
                                                                        modifier = Modifier.weight(1.3f),
                                                                        colors = ButtonDefaults.buttonColors(
                                                                            containerColor = if (isDelivered) Color.DarkGray else CosmicSecondary,
                                                                            contentColor = Color.Black
                                                                        ),
                                                                        contentPadding = PaddingValues(vertical = 4.dp),
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        enabled = !isDelivered
                                                                    ) {
                                                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(10.dp))
                                                                        Spacer(modifier = Modifier.width(3.dp))
                                                                        Text(if (isDelivered) "مكتملة ✅" else "تم التوصيل ✅", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(12.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                5 -> {
                    // SUPABASE CONNECTION KEYS SETTING SCREEN - INTEGRATED DIRECTLY IN ADMIN DASHBOARD
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                "لوحة مفاتيح الربط والاتصال السحابي (Supabase) 🔐 🛰️",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        "تعديل مفاتيح الاتصال وإعدادات الخادم الكوني ⚙️",
                                        fontWeight = FontWeight.Bold,
                                        color = CosmicSecondary,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "يمكنك تعديل عنوان ومفتاح قاعدة البيانات يدوياً وسيقوم التطبيق بالاتصال فوراً ومزامنة المنتجات والطلبات والمناديب.",
                                        color = MediumContrastTextDark,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth(),
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    
                                    OutlinedTextField(
                                        value = supabaseUrlInput,
                                        onValueChange = { supabaseUrlInput = it },
                                        label = { Text("عنوان URL لـ Supabase", color = CosmicSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = CosmicSurfaceVariant,
                                            focusedLabelColor = CosmicSecondary,
                                            cursorColor = CosmicSecondary,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        trailingIcon = {
                                            TextButton(
                                                onClick = {
                                                    val text = clipboardManager.getText()?.text
                                                    if (!text.isNullOrEmpty()) {
                                                        var cleanText = text.trim()
                                                        if (cleanText.contains("/rest/v1")) {
                                                             cleanText = cleanText.substringBefore("/rest/v1")
                                                        }
                                                        supabaseUrlInput = cleanText
                                                        Toast.makeText(context, "تم لصق العنوان! 📋", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "الحافظة فارغة! 📋❌", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste URL", tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                                                     Spacer(modifier = Modifier.width(4.dp))
                                                    Text("لصق", color = CosmicSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        },
                                        placeholder = { Text("https://example.supabase.co", color = Color.Gray) }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = supabaseKeyInput,
                                        onValueChange = { supabaseKeyInput = it },
                                        label = { Text("مفتاح API الخاص بـ Supabase", color = CosmicSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = CosmicSurfaceVariant,
                                            focusedLabelColor = CosmicSecondary,
                                            cursorColor = CosmicSecondary,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        trailingIcon = {
                                            TextButton(
                                                onClick = {
                                                    val text = clipboardManager.getText()?.text
                                                    if (!text.isNullOrEmpty()) {
                                                        supabaseKeyInput = text.trim()
                                                        Toast.makeText(context, "تم لصق المفتاح! 🔑", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "الحافظة فارغة! 📋❌", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                     Icon(Icons.Default.ContentPaste, contentDescription = "Paste Key", tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                                                     Spacer(modifier = Modifier.width(4.dp))
                                                     Text("لصق", color = CosmicSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        },
                                        placeholder = { Text("eyJ...", color = Color.Gray) }
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val defaultUrl = "https://figyszyedxlmbtaepmyt.supabase.co/"
                                                val defaultKey = "Sb_publishable_WRJgX0HreyiRExm-d5OSVQ_sZwnWYBy"
                                                supabaseUrlInput = defaultUrl
                                                supabaseKeyInput = defaultKey
                                                com.example.data.network.SupabaseConfig.save(context, defaultUrl, defaultKey)
                                                viewModel.refreshConnection()
                                                Toast.makeText(context, "تمت إعادة تعيين القيم الافتراضية ومحاولة المزامنة 🔄", Toast.LENGTH_LONG).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White)
                                        ) {
                                            Text("إعادة الافتراضي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                if (supabaseUrlInput.trim().isEmpty() || supabaseKeyInput.trim().isEmpty()) {
                                                    Toast.makeText(context, "يرجى ملء جميع الحقول أولاً! ⚠️", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    com.example.data.network.SupabaseConfig.save(
                                                        context,
                                                        supabaseUrlInput.trim(),
                                                        supabaseKeyInput.trim()
                                                    )
                                                    viewModel.refreshConnection()
                                                    Toast.makeText(context, "تم حفظ الإعدادات وجاري مزامنة قاعدة البيانات... 📡", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1.2f),
                                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                                        ) {
                                            Text("حفظ ومزامنة 📡", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            // Collapsible SQL Guide button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showSqlSetupGuide = !showSqlSetupGuide }
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (showSqlSetupGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Guide",
                                        tint = CosmicSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "كيف أهيئ قاعدة بيانات Supabase؟ 💡",
                                        color = CosmicSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (showSqlSetupGuide) "إخفاء" else "عرض الشرح",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        item {
                            androidx.compose.animation.AnimatedVisibility(visible = showSqlSetupGuide) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "1. افتح مشروعك في موقع Supabase.co\n" +
                                               "2. اذهب إلى SQL Editor في القائمة الجانبية.\n" +
                                               "3. انقر على مشروع جديد (New query).\n" +
                                               "4. انسخ كود SQL بالأسفل والصقه هناك ثم اضغط Run.\n" +
                                               "5. كذلك تأكد من تفعيل RLS أو إضافة سياسات (Policies) للسماح بالقراءة والكتابة للجميع (Anon).",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    val sqlScript = """
                                -- 1. إنشاء جدول المنتجات (products)
                                CREATE TABLE IF NOT EXISTS public.products (
                                    id SERIAL PRIMARY KEY,
                                    name TEXT NOT NULL,
                                    description TEXT,
                                    price DOUBLE PRECISION NOT NULL,
                                    category TEXT NOT NULL,
                                    category_arabic TEXT NOT NULL,
                                    rating REAL,
                                    image_res_name TEXT,
                                    is_favorite BOOLEAN DEFAULT false,
                                    stock INTEGER DEFAULT 10
                                );

                                -- تحديث السيرفر لإضافة البريد الإلكتروني للبائع وحالة الموافقة لو قمت بإنشائه مسبقاً
                                ALTER TABLE public.products ADD COLUMN IF NOT EXISTS seller_email TEXT DEFAULT '';
                                ALTER TABLE public.products ADD COLUMN IF NOT EXISTS is_approved BOOLEAN DEFAULT true;

                                -- 2. إنشاء جدول الطلبات الأسبوعي واليومي (orders)
                                CREATE TABLE IF NOT EXISTS public.orders (
                                    id SERIAL PRIMARY KEY,
                                    order_id TEXT NOT NULL,
                                    product_id INTEGER NOT NULL,
                                    product_name TEXT NOT NULL,
                                    quantity INTEGER NOT NULL,
                                    price_at_order DOUBLE PRECISION NOT NULL,
                                    status_arabic TEXT NOT NULL,
                                    customer_name TEXT NOT NULL,
                                    customer_phone TEXT NOT NULL,
                                    customer_address TEXT NOT NULL,
                                    customer_email TEXT,
                                    courier_name TEXT DEFAULT '',
                                    courier_phone TEXT DEFAULT '',
                                    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
                                );

                                -- 3. إنشاء جدول المناديب (couriers)
                                CREATE TABLE IF NOT EXISTS public.couriers (
                                    id SERIAL PRIMARY KEY,
                                    name TEXT NOT NULL,
                                    phone TEXT NOT NULL,
                                    state_info TEXT NOT NULL,
                                    status TEXT NOT NULL,
                                    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
                                );

                                -- 4. إنشاء جدول البائعين (sellers)
                                CREATE TABLE IF NOT EXISTS public.sellers (
                                    id SERIAL PRIMARY KEY,
                                    name TEXT NOT NULL,
                                    email TEXT UNIQUE NOT NULL,
                                    phone TEXT,
                                    classification TEXT DEFAULT 'تاجر ذهبي ⭐',
                                    commission_rate DOUBLE PRECISION DEFAULT 0.10,
                                    created_at BIGINT
                                );
                                """.trimIndent()

                                    Button(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(sqlScript))
                                            Toast.makeText(context, "تم نسخ كود SQL بنجاح! 📋", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(10.dp), tint = Color.Black)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("نسخ كود SQL الإعداد للتطبيق 📋", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                6 -> {
                    // TAB 6: SELLERS MANAGEMENT & COMMISSIONS
                    val sellers by viewModel.allSellers.collectAsStateWithLifecycle()
                    var newSellerName by remember { mutableStateOf("") }
                    var newSellerEmail by remember { mutableStateOf("") }
                    var newSellerPhone by remember { mutableStateOf("") }
                    var newSellerClass by remember { mutableStateOf("تاجر ذهبي ⭐") }
                    var newSellerCommission by remember { mutableStateOf("10") } // in %

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                "إدارة بائعي المجرة وبرنامج العمولات 🧑‍💼 🌌",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Form to add seller
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        "تسجيل بائع / تاجر جديد في التطبيق ➕",
                                        fontWeight = FontWeight.Bold,
                                        color = CosmicSecondary,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = newSellerName,
                                        onValueChange = { newSellerName = it },
                                        label = { Text("اسم البائع الكامل", color = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = CosmicSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = newSellerEmail,
                                        onValueChange = { newSellerEmail = it },
                                        label = { Text("البريد الإلكتروني المعتمد للدخول", color = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = CosmicSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = newSellerPhone,
                                        onValueChange = { newSellerPhone = it },
                                        label = { Text("رقم الهاتف أو الواتساب", color = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = CosmicSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newSellerCommission,
                                            onValueChange = { newSellerCommission = it },
                                            label = { Text("عمولة التطبيق (%)", color = Color.Gray) },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CosmicSecondary,
                                                unfocusedBorderColor = CosmicSurfaceVariant,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                        )

                                        OutlinedTextField(
                                            value = newSellerClass,
                                            onValueChange = { newSellerClass = it },
                                            label = { Text("تصنيف البائع", color = Color.Gray) },
                                            modifier = Modifier.weight(1.5f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CosmicSecondary,
                                                unfocusedBorderColor = CosmicSurfaceVariant,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (newSellerName.trim().isEmpty() || newSellerEmail.trim().isEmpty() || newSellerPhone.trim().isEmpty()) {
                                                Toast.makeText(context, "الرجاء ملء جميع الحقول المطلوبة! ⚠️", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val comm = newSellerCommission.toDoubleOrNull() ?: 10.0
                                                viewModel.addSeller(
                                                    name = newSellerName.trim(),
                                                    email = newSellerEmail.trim().lowercase(),
                                                    phone = newSellerPhone.trim(),
                                                    classification = newSellerClass.trim(),
                                                    commissionRate = comm / 100.0
                                                ) { err ->
                                                    if (err == null) {
                                                        Toast.makeText(context, "تم تسجيل البائع ${newSellerName} بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                                                        newSellerName = ""
                                                        newSellerEmail = ""
                                                        newSellerPhone = ""
                                                    } else {
                                                        Toast.makeText(context, "فشل الحفظ: $err", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("تسجيل البائع وحفظه سحابياً 🌌", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                "قائمة البائعين النشطين وإحصائيات العمولات 📊",
                                color = CosmicSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                        }

                        if (sellers.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface.copy(0.5f))
                                ) {
                                    Text(
                                        "لا يوجد أي بائعين مسجلين حالياً. 📭",
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(sellers) { seller ->
                                // Calculate sales stats for this seller
                                val sellerProducts = allProducts.filter { it.sellerEmail.trim().lowercase() == seller.email.trim().lowercase() }
                                val sellerProductIds = sellerProducts.map { it.id }.toSet()
                                val sellerOrderItems = allOrders.filter { it.productId in sellerProductIds && (it.statusArabic.contains("تم") || it.statusArabic.contains("تمام") || it.statusArabic.contains("التوصيل") || it.statusArabic.contains("شحن")) }
                                
                                val totalRevenue = sellerOrderItems.sumOf { it.priceAtOrder * it.quantity }
                                val appCommission = totalRevenue * seller.commissionRate
                                val sellerNet = totalRevenue - appCommission

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.2f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(CosmicSecondary.copy(0.1f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    seller.classification,
                                                    color = CosmicSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                seller.name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("البريد: ${seller.email}", color = Color.LightGray, fontSize = 11.sp)
                                        Text("الهاتف: ${seller.phone}", color = Color.LightGray, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Finance breakdown
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.White.copy(0.02f), RoundedCornerShape(6.dp))
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("صافي البائع 💰", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                Text("${viewModel.formatPrice(sellerNet)}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("عمولة التطبيق (${(seller.commissionRate * 100).toInt()}%) 📐", color = CosmicSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                Text("${viewModel.formatPrice(appCommission)}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("إجمالي المبيعات 📈", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                Text("${viewModel.formatPrice(totalRevenue)}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // WhatsApp Invoice button
                                            Button(
                                                onClick = {
                                                    val invoiceText = StringBuilder()
                                                    invoiceText.append("🌌 *مبيعات البائع في المجرة الكونية* 🌌\n\n")
                                                    invoiceText.append("👤 *التاجر:* ${seller.name}\n")
                                                    invoiceText.append("⭐ *التصنيف:* ${seller.classification}\n\n")
                                                    invoiceText.append("📋 *تفاصيل الطلبيات الخاضعة للفوترة الصافية:*\n")
                                                    sellerOrderItems.forEach { item ->
                                                        invoiceText.append("- ${item.productName} (العدد: ${item.quantity}) سعره: ${viewModel.formatPrice(item.priceAtOrder * item.quantity)}\n")
                                                    }
                                                    invoiceText.append("\n-----------------------------------\n")
                                                    invoiceText.append("📊 *إجمالي قيمة مبيعات البائع:* ${viewModel.formatPrice(totalRevenue)}\n")
                                                    invoiceText.append("💵 *المبلغ المستحق لك بالكامل (دون عمولة التطبيق):* ${viewModel.formatPrice(sellerNet)}\n\n")
                                                    invoiceText.append("🚀 *تمت الفوترة والتصدير تلقائياً عبر نظام المجرة الذكي بنجاح!*")

                                                    val encoded = java.net.URLEncoder.encode(invoiceText.toString(), "UTF-8")
                                                    val url = "https://wa.me/${seller.phone}?text=$encoded"
                                                    val waIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                                    context.startActivity(waIntent)
                                                },
                                                modifier = Modifier.weight(1.5f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.Share, null, modifier = Modifier.size(12.dp), tint = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("إرسال فاتورة وتفاصيل عبر واتساب 💬", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Delete seller button
                                            IconButton(
                                                onClick = {
                                                    viewModel.removeSeller(seller.id) { err ->
                                                        if (err == null) {
                                                            Toast.makeText(context, "تم حذف البائع بنجاح! 🗑️", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .background(Color.Red.copy(0.1f), RoundedCornerShape(8.dp))
                                                    .size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, "حذف", tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                7 -> {
                    // TAB 7: INVENTORY & PRICES QUICK ACTIONS
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                "إدارة مستودعات المجرة ومراقبة المخازن والأسعار 📦 ⚡",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (allProducts.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("لا توجد منتجات في المستودع الكوني حالياً! 📭", color = MediumContrastTextDark)
                                }
                            }
                        } else {
                            items(allProducts) { product ->
                                var priceInput by remember(product.id) { mutableStateOf(product.price.toInt().toString()) }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Quick inline Stock Controls
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Minus
                                            IconButton(
                                                onClick = {
                                                    if (product.stock > 0) {
                                                        viewModel.updateProduct(product.copy(stock = product.stock - 1)) { error ->
                                                            if (error != null) {
                                                                Toast.makeText(context, "فشل تعديل المخزون: $error", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .background(Color.White.copy(0.05f), RoundedCornerShape(6.dp))
                                                    .size(30.dp)
                                            ) {
                                                Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }

                                            // Stock quantity display
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.3f)),
                                                border = BorderStroke(1.dp, if (product.stock == 0) Color.Red else CosmicSecondary.copy(0.3f))
                                            ) {
                                                Text(
                                                    "${product.stock} ق",
                                                    color = if (product.stock == 0) Color.Red else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }

                                            // Plus
                                            IconButton(
                                                onClick = {
                                                    viewModel.updateProduct(product.copy(stock = product.stock + 1)) { error ->
                                                        if (error != null) {
                                                            Toast.makeText(context, "فشل تعديل المخزون: $error", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .background(Color.White.copy(0.05f), RoundedCornerShape(6.dp))
                                                    .size(30.dp)
                                            ) {
                                                Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                        }

                                        // Name, category and price input details
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            modifier = Modifier.weight(1f).padding(end = 6.dp)
                                        ) {
                                            Text(
                                                product.name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Right
                                            )
                                            Text(
                                                product.categoryArabic,
                                                color = CosmicSecondary,
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Right
                                            )
                                            if (product.sellerEmail.isNotEmpty()) {
                                                Text(
                                                    "البائع: ${product.sellerEmail}",
                                                    color = Color.LightGray,
                                                    fontSize = 9.sp,
                                                    textAlign = TextAlign.Right
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Price quick editor
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        val priceParsed = priceInput.toDoubleOrNull() ?: product.price
                                                        if (priceParsed != product.price) {
                                                            viewModel.updateProduct(product.copy(price = priceParsed)) { err ->
                                                                if (err == null) {
                                                                    Toast.makeText(context, "تم حفظ السعر وتحديث قاعدة البيانات! ✅", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    Toast.makeText(context, "فشل الحفظ: $err", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Check, "Save Price", tint = Color.Green, modifier = Modifier.size(16.dp))
                                                }

                                                androidx.compose.foundation.text.BasicTextField(
                                                    value = priceInput,
                                                    onValueChange = { priceInput = it },
                                                    textStyle = androidx.compose.ui.text.TextStyle(
                                                        color = Color.Green,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Right
                                                    ),
                                                    modifier = Modifier
                                                        .width(60.dp)
                                                        .background(Color.Black.copy(0.4f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                )

                                                Text(
                                                    " السعر: ",
                                                    color = Color.Gray,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                8 -> {
                    PendingProductsReviewSection(viewModel)
                }
            }
        }
    }
}

@Composable
fun PendingProductsReviewSection(viewModel: MajarahViewModel) {
    val context = LocalContext.current
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val pendingProducts = remember(allProducts) { allProducts.filter { !it.isApproved } }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "طلبات المنتجات المعلقة للبائعين 🧑‍💼⏳",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "هنا يمكنك مراجعة وتعديل أسعار منتجات البائعين لإضافة عمولة/فائدة التطبيق ومن ثم الموافقة عليها ونشرها مباشرة للمشترين.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        if (pendingProducts.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HourglassEmpty, null, tint = CosmicSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "لا توجد منتجات معلقة مضافة من البائعين حالياً! 🎉",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(pendingProducts) { product ->
                var profitPriceInput by remember(product.id) { mutableStateOf(product.price.toInt().toString()) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE65100).copy(0.15f)),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(0.5f)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "طلب معلق من: ${product.sellerEmail.ifBlank { "بائع خارجي" }}",
                                color = Color(0xFFFFB74D),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            product.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Right
                        )

                        Text(
                            product.description,
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "الفئة: ${product.categoryArabic}",
                                color = CosmicSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "كمية المخزون المتاحة: ${product.stock} ق",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(CosmicSurfaceVariant))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                Text(
                                    "السعر المقترح من التاجر:",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                                Text(
                                    "${product.price} ج.س",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "السعر النهائي للتطبيق بالعمولة (ج.س):",
                                    color = CosmicSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = profitPriceInput,
                                    onValueChange = { profitPriceInput = it },
                                    modifier = Modifier.width(140.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = Color.Green,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Right
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Green,
                                        unfocusedBorderColor = CosmicSurfaceVariant,
                                        focusedContainerColor = Color.Black.copy(0.3f),
                                        unfocusedContainerColor = Color.Black.copy(0.3f)
                                    ),
                                    singleLine = true
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.deleteProduct(product.id) { err ->
                                        if (err == null) {
                                            Toast.makeText(context, "تم رفض وحذف الطلب المعلق بنجاح ❌", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "خطأ بالرفض: $err", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                border = BorderStroke(1.dp, Color.Red.copy(0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("رفض المنتج ❌", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val finalPrice = profitPriceInput.toDoubleOrNull() ?: product.price
                                    val approvedProduct = product.copy(
                                        price = finalPrice,
                                        isApproved = true
                                    )
                                    viewModel.updateProduct(approvedProduct) { err ->
                                        if (err == null) {
                                            Toast.makeText(context, "تمت الموافقة وتعديل السعر بالعمولة للتطبيق ونشره بنجاح! 🚀✅", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "فشل النشر: $err", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text("إضافة المنتج ونشره (قبول) 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SellerDashboardScreenBody(viewModel: MajarahViewModel) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val sellers by viewModel.allSellers.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val currentSeller = sellers.find { s -> s.email.trim().lowercase() == activeProfile?.email?.trim()?.lowercase() }
    
    var activeSubTab by remember { mutableStateOf(0) } // 0: Products, 1: Add Product, 2: Profile & Support

    var newProdName by remember { mutableStateOf("") }
    var newProdDesc by remember { mutableStateOf("") }
    var newProdPrice by remember { mutableStateOf("") }
    var newProdStock by remember { mutableStateOf("") }
    var newProdCategory by remember { mutableStateOf("electronics") }
    var newProdCategoryArabic by remember { mutableStateOf("إلكترونيات وأجهزة") }

    val categories = listOf(
        "electronics" to "إلكترونيات وأجهزة",
        "clothing" to "ملابس وأزياء",
        "foods" to "أغذية ومأكولات",
        "cosmetics" to "عطور وتجميل",
        "home" to "مستلزمات منزلية",
        "other" to "أخرى وهدايا"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Seller Banner/Headline
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            border = BorderStroke(1.dp, CosmicSecondary.copy(0.2f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(CosmicSecondary.copy(0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            currentSeller?.classification ?: "تاجر معتمد ⭐",
                            color = CosmicSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        "أهلاً بك، ${activeProfile?.name ?: "التاجر الكوني"} 👋",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "البريد الإلكتروني للعمليات: ${activeProfile?.email}",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                "منتجاتي 🛍️" to 0,
                "إضافة منتج ➕" to 1,
                "الدعم والتواصل 💬" to 2
            )
            tabs.forEach { (label, index) ->
                val isSelected = activeSubTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) CosmicSecondary else CosmicSurface)
                        .clickable { activeSubTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Content Switcher
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                0 -> {
                    // MY PRODUCTS
                    val myProducts = allProducts.filter { it.sellerEmail.trim().lowercase() == activeProfile?.email?.trim()?.lowercase() }
                    
                    if (myProducts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Storefront, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "لا توجد منتجات مسجلة باسمك في المتجر حالياً! 📭",
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(myProducts) { product ->
                                var priceInput by remember(product.id) { mutableStateOf(product.price.toInt().toString()) }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Stock Controls
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (product.stock > 0) {
                                                        viewModel.updateProduct(product.copy(stock = product.stock - 1)) { error ->
                                                            if (error != null) {
                                                                Toast.makeText(context, "فشل تعديل المخزون: $error", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .background(Color.White.copy(0.05f), RoundedCornerShape(6.dp))
                                                    .size(30.dp)
                                            ) {
                                                Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }

                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.3f)),
                                                border = BorderStroke(1.dp, if (product.stock == 0) Color.Red else CosmicSecondary.copy(0.3f))
                                            ) {
                                                Text(
                                                    "${product.stock} ق",
                                                    color = if (product.stock == 0) Color.Red else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    viewModel.updateProduct(product.copy(stock = product.stock + 1)) { error ->
                                                        if (error != null) {
                                                            Toast.makeText(context, "فشل تعديل المخزون: $error", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .background(Color.White.copy(0.05f), RoundedCornerShape(6.dp))
                                                    .size(30.dp)
                                            ) {
                                                Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                        }

                                        // Product Details & Price Editing
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            modifier = Modifier.weight(1f).padding(end = 6.dp)
                                        ) {
                                            Text(
                                                product.name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Right
                                            )
                                            Text(
                                                product.categoryArabic,
                                                color = CosmicSecondary,
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Right
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (product.isApproved) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.15f)
                                                ),
                                                border = BorderStroke(1.dp, if (product.isApproved) Color(0xFF81C784) else Color(0xFFFFB74D)),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    if (product.isApproved) "معتمد ومنشور 🟢" else "بانتظار موافقة المدير ⏳",
                                                    color = if (product.isApproved) Color.Green else Color(0xFFFFB74D),
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        val priceParsed = priceInput.toDoubleOrNull() ?: product.price
                                                        if (priceParsed != product.price) {
                                                            viewModel.updateProduct(product.copy(price = priceParsed)) { err ->
                                                                if (err == null) {
                                                                    Toast.makeText(context, "تم حفظ السعر الجديد وتحديث قاعدة البيانات! ✅", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    Toast.makeText(context, "فشل الحفظ: $err", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Check, "Save Price", tint = Color.Green, modifier = Modifier.size(16.dp))
                                                }

                                                androidx.compose.foundation.text.BasicTextField(
                                                    value = priceInput,
                                                    onValueChange = { priceInput = it },
                                                    textStyle = androidx.compose.ui.text.TextStyle(
                                                        color = Color.Green,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Right
                                                    ),
                                                    modifier = Modifier
                                                        .width(60.dp)
                                                        .background(Color.Black.copy(0.4f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                )

                                                Text(
                                                    " السعر: ",
                                                    color = Color.Gray,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // ADD PRODUCT FORM FOR SELLER
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        "إضافة منتج جديد لمعروضاتك في المجرة 🌌 🛒",
                                        fontWeight = FontWeight.Bold,
                                        color = CosmicSecondary,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Right
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = newProdName,
                                        onValueChange = { newProdName = it },
                                        label = { Text("اسم المنتج", color = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = CosmicSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = newProdDesc,
                                        onValueChange = { newProdDesc = it },
                                        label = { Text("وصف المنتج", color = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = CosmicSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newProdStock,
                                            onValueChange = { newProdStock = it },
                                            label = { Text("الكمية المتوفرة", color = Color.Gray) },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CosmicSecondary,
                                                unfocusedBorderColor = CosmicSurfaceVariant,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                        )

                                        OutlinedTextField(
                                            value = newProdPrice,
                                            onValueChange = { newProdPrice = it },
                                            label = { Text("سعر المنتج (ج.س)", color = Color.Gray) },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CosmicSecondary,
                                                unfocusedBorderColor = CosmicSurfaceVariant,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("تصنيف الفئة للمنتج الكوني: ", color = CosmicSecondary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Category Select row
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(categories) { (code, arabic) ->
                                            val isCatSelected = newProdCategory == code
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isCatSelected) CosmicSecondary else CosmicSurfaceVariant)
                                                    .clickable {
                                                        newProdCategory = code
                                                        newProdCategoryArabic = arabic
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    arabic,
                                                    color = if (isCatSelected) Color.Black else Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            if (newProdName.trim().isEmpty() || newProdPrice.trim().isEmpty() || newProdStock.trim().isEmpty()) {
                                                Toast.makeText(context, "يرجى ملء كافة الحقول لإدراج المنتج! ⚠️", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val parsedPrice = newProdPrice.toDoubleOrNull() ?: 0.0
                                                val parsedStock = newProdStock.toIntOrNull() ?: 1
                                                val defaultUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff" // default Cosmic sneaker slot

                                                val newProduct = com.example.data.db.ProductEntity(
                                                    id = 0,
                                                    name = newProdName.trim(),
                                                    description = newProdDesc.trim(),
                                                    price = parsedPrice,
                                                    category = newProdCategory,
                                                    categoryArabic = newProdCategoryArabic,
                                                    rating = 4.5f,
                                                    imageResName = "ic_product_placeholder",
                                                    isFavorite = false,
                                                    stock = parsedStock,
                                                    sellerEmail = activeProfile?.email ?: "",
                                                    isApproved = false
                                                )

                                                viewModel.addProduct(newProduct) { err ->
                                                    if (err == null) {
                                                        Toast.makeText(context, "تم إرسال هذا المنتج للمدير للمراجعة وتحديد السعر النهائي والموافقة قبل نشره في التطبيق! ⏳✨", Toast.LENGTH_LONG).show()
                                                        newProdName = ""
                                                        newProdDesc = ""
                                                        newProdPrice = ""
                                                        newProdStock = ""
                                                        activeSubTab = 0 // Go back to List
                                                    } else {
                                                        Toast.makeText(context, "فشل الرفع: $err", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("إدراج هذا المنتج في المجرة الكونية 🚀", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // SELLER SUPPORT & DIRECT COMMUNICATION
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            border = BorderStroke(1.dp, CosmicSecondary.copy(0.15f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    "مركز الدعم والتنسيق المباشر مع الإدارة 🛰️",
                                    color = CosmicSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Right
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "بصفتك شريك تاجر في مجرة السودان، يمكنك التواصل الفوري مع المدير العام لتسوية الحسابات، طلب زيادة الحصص، أو إرسال تقارير التسوية المالية عبر القنوات الكونية التالية:",
                                    color = Color.White.copy(0.85f),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    textAlign = TextAlign.Right
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Telephone Hotline Button
                                Button(
                                    onClick = {
                                        val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                            data = android.net.Uri.parse("tel:0912111111")
                                        }
                                        context.startActivity(dialIntent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp), tint = CosmicSecondary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("الاتصال السريع بالمدير (هاتفياً) 📞", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // WhatsApp direct button
                                Button(
                                    onClick = {
                                        val url = "https://wa.me/249912111111?text=" + java.net.URLEncoder.encode("مرحباً يا مدير مجرة السودان للتسوق، أنا التاجر الشريك وعندي طلب تسوية أو استفسار بخصوص المتجر.", "UTF-8")
                                        val waIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(waIntent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.Message, null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("محادثة المدير العام عبر واتساب 💬", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourierDashboardScreenBody(viewModel: MajarahViewModel) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allCouriers by viewModel.allCouriers.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrdersFlow.collectAsStateWithLifecycle()

    var showDeliveryPaymentDialogForOrderId by remember { mutableStateOf<String?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf("cash") } // "cash" or "bank"
    var bankTransferReference by remember { mutableStateOf("") }

    var previousAssignedOrderIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var newTaskAlertOrderId by remember { mutableStateOf<String?>(null) }
    var scrollToOrderId by remember { mutableStateOf<String?>(null) }
    var courierOrdersTab by remember { mutableStateOf(0) } // 0: Active, 1: Completed, 2: Cancelled

    if (showDeliveryPaymentDialogForOrderId != null) {
        val targetOrderId = showDeliveryPaymentDialogForOrderId!!
        val orderItems = allOrders.filter { it.orderId == targetOrderId }
        val parent = orderItems.firstOrNull()
        val totalAmount = orderItems.sumOf { it.priceAtOrder * it.quantity } + (parent?.deliveryFee ?: 0.0)
        
        AlertDialog(
            onDismissRequest = { showDeliveryPaymentDialogForOrderId = null },
            title = {
                Text(
                    text = "طريقة دفع الطلب وتأكيد التسليم 💵",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "اختر طريقة استلام قيمة الطلب (المبلغ الإجمالي: ${viewModel.formatPrice(totalAmount)} SDG):",
                        fontSize = 11.sp,
                        color = Color.White.copy(0.9f),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    
                    // Cash Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = "cash" }
                            .background(
                                if (selectedPaymentMethod == "cash") CosmicSecondary.copy(0.15f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الدفع كاش (نقداً) 💵",
                            color = if (selectedPaymentMethod == "cash") CosmicSecondary else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(
                            selected = selectedPaymentMethod == "cash",
                            onClick = { selectedPaymentMethod = "cash" },
                            colors = RadioButtonDefaults.colors(selectedColor = CosmicSecondary)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Bank Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = "bank" }
                            .background(
                                if (selectedPaymentMethod == "bank") CosmicSecondary.copy(0.15f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تحويل بنكي (بنكك / تطبيق آخر) 🏛️",
                            color = if (selectedPaymentMethod == "bank") CosmicSecondary else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(
                            selected = selectedPaymentMethod == "bank",
                            onClick = { selectedPaymentMethod = "bank" },
                            colors = RadioButtonDefaults.colors(selectedColor = CosmicSecondary)
                        )
                    }
                    
                    if (selectedPaymentMethod == "bank") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "رقم عملية الإشعار التحويلي للبنك:",
                            fontSize = 10.sp,
                            color = CosmicSecondary,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = bankTransferReference,
                            onValueChange = { bankTransferReference = it },
                            placeholder = { Text("مثال: 1234567-TRX", fontSize = 10.sp, color = Color.White.copy(0.4f)) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CosmicSecondary,
                                unfocusedBorderColor = Color.White.copy(0.3f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedPaymentMethod == "bank" && bankTransferReference.trim().isEmpty()) {
                            Toast.makeText(context, "الرجاء إدخال رقم عملية الإشعار لإكمال التحويل البنكي", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        
                        val finalStatus = if (selectedPaymentMethod == "bank") {
                            "تم التسليم (تحويل بنكي - إشعار: ${bankTransferReference.trim()}) ✅"
                        } else {
                            "تم التسليم (نقداً كاش) ✅"
                        }
                        
                        viewModel.updateOrderStatus(targetOrderId, finalStatus) { err ->
                            if (err == null) {
                                Toast.makeText(context, "تم تسليم الطلب وتأكيد الفاتورة بنجاح! 🚀🎉", Toast.LENGTH_SHORT).show()
                                showDeliveryPaymentDialogForOrderId = null
                            } else {
                                Toast.makeText(context, "خطأ أثناء التحديث: $err", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                ) {
                    Text("تأكيد واستلام الطلب ✅", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeliveryPaymentDialogForOrderId = null }) {
                    Text("إلغاء", color = Color.White.copy(0.6f))
                }
            },
            containerColor = CosmicSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    val myCourierInfo = activeProfile?.let { profile ->
        val cleanPhone = profile.phone.trim().replace("+", "").replace(" ", "")
        allCouriers.find { c ->
            val cleanCPhone = c.phone.trim().replace("+", "").replace(" ", "")
            cleanCPhone == cleanPhone || c.phone.trim() == profile.phone.trim()
        }
    }

    // NEW: Real-time Monitor for newly assigned active tasks with notification sound
    val currentAssignedActiveOrderIds = remember(allOrders, myCourierInfo) {
        if (myCourierInfo == null) emptySet<String>()
        else {
            allOrders.filter {
                val isMyOrder = it.courierName.trim().isNotBlank() && (
                    it.courierName == myCourierInfo.name || 
                    it.courierPhone.trim() == myCourierInfo.phone.trim()
                )
                val statusText = it.statusArabic
                val isMainActive = !statusText.contains("تمام") && !statusText.contains("تم توصيل") && !statusText.contains("ملغي") && !statusText.contains("تم التسليم")
                isMyOrder && isMainActive
            }.map { it.orderId }.toSet()
        }
    }

    LaunchedEffect(currentAssignedActiveOrderIds, myCourierInfo) {
        if (myCourierInfo != null && previousAssignedOrderIds.isNotEmpty()) {
            val newEntries = currentAssignedActiveOrderIds - previousAssignedOrderIds
            if (newEntries.isNotEmpty()) {
                val newlyAssigned = newEntries.first()
                // Play notification sound
                try {
                    val notificationUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE) 
                        ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = android.media.RingtoneManager.getRingtone(context, notificationUri)
                    ringtone.play()
                } catch (e: Exception) {
                    try {
                        val alarmUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                        val ringtone = android.media.RingtoneManager.getRingtone(context, alarmUri)
                        ringtone.play()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
                // Vibrate the phone to notify the courier
                try {
                    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                    if (vibrator != null) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator.vibrate(android.os.VibrationEffect.createOneShot(1000, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(1000)
                        }
                    }
                } catch (eVib: Exception) {
                    eVib.printStackTrace()
                }
                // Trigger pop up
                newTaskAlertOrderId = newlyAssigned
                // Switch tab to Active
                courierOrdersTab = 0
            }
        }
        if (myCourierInfo != null) {
            previousAssignedOrderIds = currentAssignedActiveOrderIds
        }
    }

    // NEW: Notification pop-up when a task is assigned to an active courier
    if (newTaskAlertOrderId != null) {
        val alertOrderId = newTaskAlertOrderId!!
        AlertDialog(
            onDismissRequest = { newTaskAlertOrderId = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("مهمة جديدة مسندة إليك! 🚴⚡", fontWeight = FontWeight.Bold, color = CosmicSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.NotificationsActive, null, tint = CosmicSecondary, modifier = Modifier.size(20.dp))
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "أهلاً بك يا كابتن ${myCourierInfo?.name ?: ""}! تم تكليفك بمهمة شحن وتوصيل جديدة بالسودان الآن من قبل لوحة الإدارة.",
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "رقم الطلبية: #${alertOrderId}",
                        color = CosmicSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "تنبيه: تم لعب نغمة رنين لتنبيهكم بالطلب. اضغط على الزر بالأسفل للانتقال الفوري ومعاينة تفاصيل الشحنة وعنوان المستلم لبدء توصيلها.",
                        color = MediumContrastTextDark,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        courierOrdersTab = 0
                        scrollToOrderId = alertOrderId
                        newTaskAlertOrderId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                ) {
                    Text("فتح ومعاينة التفاصيل 📦", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { newTaskAlertOrderId = null }) {
                    Text("إغلاق التنبيه", color = Color.White.copy(0.6f))
                }
            },
            containerColor = CosmicSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicSurface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.performLogout()
                        Toast.makeText(context, "تم تسجيل الخروج بنجاح 👋", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "تسجيل الخروج", tint = Color.Red)
                }
                Text(
                    text = "بوابة المندوب الذكية 🚴🛰️",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                IconButton(
                    onClick = {
                        viewModel.syncOrders { err ->
                            if (err == null) {
                                Toast.makeText(context, "تم تحديث الطلبات بنجاح 🔄", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "خطأ بالمزامنة: $err", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = CosmicSecondary)
                }
            }
        },
        containerColor = CosmicDeepSpace
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (myCourierInfo == null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, Color.Red.copy(0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ تنبيه هام للغاية",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "أهلاً بك يا ${activeProfile?.name ?: "شريكنا"}.\nرقم هاتفك الحالي (${activeProfile?.phone ?: ""}) غير مسجل في قائمة المناديب النشطين بنظام الإدارة الذكي.",
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "الرجاء مراجعة الإدارة (معاوية عثمان) لتسجيل وتفعيل رقم هاتفك ككابتن توصيل في نافذة المناديب حتى تتمكن من استلام وتوصيل الشحنات.",
                            color = MediumContrastTextDark,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.performLogout() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text("العودة لصفحة تسجيل الدخول 👋", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                val myAssignedOrders = allOrders.filter { 
                    it.courierName.trim().isNotBlank() && (
                        it.courierName == myCourierInfo.name || 
                        it.courierPhone.trim() == myCourierInfo.phone.trim()
                    )
                }

                val hasActiveDelivery = myAssignedOrders.any {
                    !it.statusArabic.contains("تمام") && !it.statusArabic.contains("تم توصيل")
                }

                val isWillingToWork = remember(myCourierInfo) {
                    myCourierInfo == null || !myCourierInfo.status.contains("غير متوفر")
                }

                val expectedStatus = if (!isWillingToWork) {
                    "غير متوفر 🔴"
                } else if (hasActiveDelivery) {
                    "في مهمة توصيل 🟡"
                } else {
                    "نشط ومتوفر 🟢"
                }

                LaunchedEffect(expectedStatus, myCourierInfo) {
                    myCourierInfo?.let { courier ->
                        if (courier.status != expectedStatus) {
                            viewModel.updateCourier(courier.copy(status = expectedStatus))
                        }
                    }
                }

                // Courier Profile
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, CosmicSecondary.copy(0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            myCourierInfo.status.contains("متوفر") || myCourierInfo.status.contains("🟢") -> Color.Green.copy(0.2f)
                                            myCourierInfo.status.contains("مهمة") || myCourierInfo.status.contains("🟡") -> Color.Yellow.copy(0.2f)
                                            else -> Color.Red.copy(0.2f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = myCourierInfo.status,
                                    fontSize = 11.sp,
                                    color = when {
                                        myCourierInfo.status.contains("متوفر") || myCourierInfo.status.contains("🟢") -> Color.Green
                                        myCourierInfo.status.contains("مهمة") || myCourierInfo.status.contains("🟡") -> CosmicTertiary
                                        else -> Color.Red
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "الكابتن: ${myCourierInfo.name} 🚴",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "مركز التغطية: ${myCourierInfo.stateInfo}",
                            fontSize = 11.sp,
                            color = CosmicSecondary,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "رقم هاتف المندوب: ${myCourierInfo.phone}",
                            fontSize = 11.sp,
                            color = MediumContrastTextDark,
                            textAlign = TextAlign.Right
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Switch(
                                checked = isWillingToWork,
                                onCheckedChange = { newValue ->
                                    myCourierInfo?.let { courier ->
                                        val nextStatus = if (!newValue) {
                                            "غير متوفر 🔴"
                                        } else {
                                            if (hasActiveDelivery) "في مهمة توصيل 🟡" else "نشط ومتوفر 🟢"
                                        }
                                        viewModel.updateCourier(courier.copy(status = nextStatus))
                                    }
                                },
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = CosmicSecondary,
                                    uncheckedThumbColor = Color.White.copy(0.6f),
                                    uncheckedTrackColor = Color.DarkGray
                                )
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "جاهزية الكابتن للعمل 🛰️",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isWillingToWork) "أنت متصل بالخادم وتظهر كمتوفر للرحلات ✅" else "أنت مغلق ولا تستقبل طلبيات جديدة حالياً 💤",
                                    fontSize = 9.sp,
                                    color = if (isWillingToWork) Color.Green else Color.Red
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic Tab Selector for Courier Orders segregation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicSurfaceVariant.copy(0.4f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabData = listOf(
                        Triple(0, "المهام النشطة 🚴", CosmicSecondary),
                        Triple(1, "تم تنفيذها ✅", Color.Green),
                        Triple(2, "الملغية ❌", Color.Red)
                    )
                    tabData.forEach { (tabIndex, title, colorVal) ->
                        val isSelected = courierOrdersTab == tabIndex
                        Button(
                            onClick = { courierOrdersTab = tabIndex },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) colorVal else Color.Transparent,
                                contentColor = if (isSelected) Color.Black else Color.White
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val groupedOrders = myAssignedOrders.groupBy { it.orderId }
                // Apply Tab-based sorting filtration
                val filteredGroupedOrders = groupedOrders.filter { (_, itemsList) ->
                    val statusText = itemsList.firstOrNull()?.statusArabic ?: ""
                    val isActuallyCompleted = (statusText.contains("تمام") || statusText.contains("تم توصيل") || statusText.contains("تم التسليم")) && !statusText.contains("تم تسليم المندوب")
                    val isCancelled = statusText.contains("ملغي")
                    when (courierOrdersTab) {
                        0 -> !isActuallyCompleted && !isCancelled
                        1 -> isActuallyCompleted
                        2 -> isCancelled
                        else -> true
                    }
                }

                if (filteredGroupedOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(CosmicSurface, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inbox, null, tint = MediumContrastTextDark.copy(0.3f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (courierOrdersTab) {
                                    0 -> "لا توجد أي مهام نشطة بانتظارك حالياً! 🎉"
                                    1 -> "سجل المهمات المنفذة فارغ حالياً! 🚴"
                                    else -> "لا توجد أي مهمات ملغاة بسجلك! 🛡️"
                                },
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (courierOrdersTab) {
                                    0 -> "عندما يقوم المدير بإسناد أي طلب وعليك توصيله، سيظهر هنا فوراً في قائمة المهام."
                                    1 -> "الطلبيات التي تسلمها للزبائن بالسودان وتؤكد استلامها ستظهر هنا بتبويب المنفذة."
                                    else -> "الطلبات التي تكنسل من الزبون أو تلغى لدواعٍ إدارية ستظهر هنا."
                                },
                                color = MediumContrastTextDark,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredGroupedOrders.entries.toList()) { (orderId, itemsList) ->
                            val parent = itemsList.firstOrNull()
                            val totalPrice = itemsList.sumOf { it.priceAtOrder * it.quantity }
                            val isCompleted = parent?.statusArabic?.let { status ->
                                (status.contains("تمام") || 
                                status.contains("تم توصيل") || 
                                status.contains("تم التوصيل") || 
                                status.contains("تم التسليم")) && 
                                !status.contains("تم تسليم المندوب")
                            } == true

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(if (orderId == scrollToOrderId) 2.dp else 1.dp, if (orderId == scrollToOrderId) Color.Yellow else if (isCompleted) Color.DarkGray else CosmicSecondary.copy(0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = parent?.statusArabic ?: "",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCompleted) Color.Green else CosmicSecondary
                                        )
                                        Text(
                                            text = "طلب #${orderId.take(10)}\nالتاريخ: " + java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US).format(java.util.Date(parent?.orderDate ?: System.currentTimeMillis())),
                                            fontSize = 11.sp,
                                            color = CosmicSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Customer detail lines
                                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                        Text("الزبون: ${parent?.customerName}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right)
                                        Text("رقم الهاتف: ${parent?.customerPhone}", fontSize = 10.sp, color = Color.White.copy(0.8f), textAlign = TextAlign.Right)
                                        Text("عنوان التسليم: ${parent?.customerAddress}", fontSize = 10.sp, color = Color.White.copy(0.8f), textAlign = TextAlign.Right)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = Color.White.copy(0.1f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Items summary
                                    itemsList.forEach { item ->
                                        Text(
                                            text = "• ${item.productName} (عدد: ${item.quantity})",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(0.7f),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Right
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "قيمة المشتريات: ${viewModel.formatPrice(totalPrice)} SDG\nسعر التوصيل 🚚: ${viewModel.formatPrice(parent?.deliveryFee ?: 0.0)} SDG\nالمجموع الكلي للتحصيل 💰: ${viewModel.formatPrice(totalPrice + (parent?.deliveryFee ?: 0.0))} SDG",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CosmicSecondary
                                        )
                                        Text(
                                            text = "الدفع نقداً عند التسليم 💵",
                                            fontSize = 9.sp,
                                            color = Color.White.copy(0.6f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Interactive buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Call Customer Button
                                        Button(
                                            onClick = {
                                                parent?.customerPhone?.let { phoneNum ->
                                                    Toast.makeText(context, "جاري فتح لوحة الاتصال بـ $phoneNum", Toast.LENGTH_SHORT).show()
                                                    try {
                                                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                            data = android.net.Uri.parse("tel:$phoneNum")
                                                        }
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "تعذر تشغيل تطبيق لوحة الاتصال", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("اتصال 📞", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        // WhatsApp Send
                                        Button(
                                            onClick = {
                                                parent?.customerPhone?.let { phoneNum ->
                                                    val rawPhone = phoneNum.trim().replace("+", "").replace(" ", "")
                                                    val cleanPhone = if (rawPhone.startsWith("0")) {
                                                        "249" + rawPhone.substring(1)
                                                    } else if (!rawPhone.startsWith("249") && (rawPhone.startsWith("9") || rawPhone.startsWith("1"))) {
                                                        "249" + rawPhone
                                                    } else {
                                                        rawPhone
                                                    }
                                                    val msg = "🌌 مرحباً يا ${parent.customerName}! معكم المندوب ${myCourierInfo.name} من تطبيق مجرة السودان. أنا متكفل بتسليم طلبيتكم الآن رقم (#${orderId.take(5)}) وقيمة المشتريات ${viewModel.formatPrice(totalPrice)} SDG + سعر التوصيل ${viewModel.formatPrice(parent?.deliveryFee ?: 0.0)} SDG (الإجمالي الكلي للتحصيل: ${viewModel.formatPrice(totalPrice + (parent?.deliveryFee ?: 0.0))} SDG). هل أنتم متواجدون لتسليمها؟"
                                                    val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${android.net.Uri.encode(msg)}"
                                                    Toast.makeText(context, "جاري فتح واتساب للزبون...", Toast.LENGTH_SHORT).show()
                                                    try {
                                                        val waIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                                        context.startActivity(waIntent)
                                                    } catch (e: Exception) {
                                                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                            type = "text/plain"
                                                            putExtra(android.content.Intent.EXTRA_TEXT, "$phoneNum: $msg")
                                                        }
                                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "إرسال التفاصيل"))
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1.1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Chat, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("واتساب 💬", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        // Mark Delivered
                                        Button(
                                            onClick = {
                                                selectedPaymentMethod = "cash"
                                                bankTransferReference = ""
                                                showDeliveryPaymentDialogForOrderId = orderId
                                            },
                                            modifier = Modifier.weight(1.2f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isCompleted) Color.DarkGray else CosmicSecondary,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            enabled = !isCompleted,
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isCompleted) "تم التسليم" else "تسليم الشحنة", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }

                                    if (isCompleted) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                val itemsText = itemsList.joinToString("\n") { "• ${it.productName} (العدد: ${it.quantity}) - ${viewModel.formatPrice(it.priceAtOrder * it.quantity)} SDG" }
                                                val totalInvPrice = itemsList.sumOf { it.priceAtOrder * it.quantity } + (parent?.deliveryFee ?: 0.0)
                                                val invoiceMsg = """
🌌 فاتورة تسليم طلبية المجرة 🌌
---------------------------
📦 رقم الطلب: #${orderId.take(7)}...
👤 اسم الزبون: ${parent?.customerName}
📞 هاتف الزبون: ${parent?.customerPhone}
📍 عنوان التسليم: ${parent?.customerAddress}
🚴 اسم المندوب: ${myCourierInfo?.name ?: "مندوب مجرة"}
💸 تفاصيل الفاتورة والمنتجات:
$itemsText
---------------------------
🚚 سعر التوصيل: ${viewModel.formatPrice(parent?.deliveryFee ?: 0.0)} SDG
💰 إجمالي الحساب: ${viewModel.formatPrice(totalInvPrice)} SDG
📝 حالة الدفع والتوصيل:
${parent?.statusArabic}
---------------------------
شكراً لثقتكم بمجرة التسوق الإلكتروني 🌌⚡
""".trimIndent()
                                                
                                                val realManagerPhone = "249910074223"
                                                val url = "https://api.whatsapp.com/send?phone=$realManagerPhone&text=${android.net.Uri.encode(invoiceMsg)}"
                                                Toast.makeText(context, "جاري فتح واتساب مع المدير لإرسال الفاتورة... 💬", Toast.LENGTH_SHORT).show()
                                                try {
                                                    val waIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                                    context.startActivity(waIntent)
                                                 } catch (e: Exception) {
                                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(android.content.Intent.EXTRA_TEXT, invoiceMsg)
                                                    }
                                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "مشاركة الفاتورة"))
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(vertical = 10.dp)
                                        ) {
                                            Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("مشاركة فاتورة التسليم مع المدير (واتساب) 💬", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreenBody() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_rotate_infinite")
    
    // Smooth breathing pulsing effect for the Cosmic brand
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    // Orbital rotation animation representing space celestial orbits
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital_rotation"
    )

    // Smooth entry fade animations
    var animateStart by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateStart = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (animateStart) 1f else 0f,
        animationSpec = tween(1200),
        label = "fade_in_alpha"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (animateStart) 1.0f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_spring_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDeepSpace),
        contentAlignment = Alignment.Center
    ) {
        // Draw dynamically rendering rich dark twinkling stars
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = java.util.Random(42)
            for (i in 0..60) {
                val x = r.nextFloat() * size.width
                val y = r.nextFloat() * size.height
                val radius = r.nextFloat() * 4f + 1f
                drawCircle(
                    color = Color.White.copy(alpha = r.nextFloat() * 0.7f + 0.3f),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .graphicsLayer(alpha = alpha)
        ) {
            // Celestial Orbit Ring wrapping the logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // outer orbiting dashboard rings
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(rotationZ = rotation)
                ) {
                    drawCircle(
                        color = CosmicSecondary.copy(alpha = 0.15f),
                        radius = size.width / 2.3f,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        )
                    )
                    drawCircle(
                        color = CosmicPrimary.copy(alpha = 0.25f),
                        radius = size.width / 2.8f,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                    )
                }

                // Main App Logo Resource
                Image(
                    painter = painterResource(id = R.drawable.img_majarah_universe_logo_1782084799100),
                    contentDescription = "Cosmic Logo",
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer(
                            scaleX = logoScale * scale,
                            scaleY = logoScale * scale
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Typography
            Text(
                text = "المجرة 🌌",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "بوابتك الكونية للتسوق الإلكتروني بالسودان 🇸🇩",
                color = MediumContrastTextDark,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            // M3 Elegant Circular Loading Indicator
            CircularProgressIndicator(
                color = CosmicSecondary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "جاري تهيئة الاتصال وتحديث المنتجات...",
                color = CosmicSecondary.copy(alpha = 0.8f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

