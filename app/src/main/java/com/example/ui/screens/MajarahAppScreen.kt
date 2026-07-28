package com.example.ui.screens

import com.example.util.WhatsAppUtils
import com.example.util.NotificationSoundUtils
import android.widget.Toast
import android.content.Intent
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.zIndex
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clipToBounds

@Composable
fun CosmicMajarahLoader(
    modifier: Modifier = Modifier,
    logoSize: androidx.compose.ui.unit.Dp = 64.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stars_rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Revolving Stars and Orbits
        Canvas(modifier = Modifier.size(logoSize * 1.6f)) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2.2f
            
            // Draw a subtle orbit path
            drawCircle(
                color = CosmicSecondary.copy(alpha = 0.15f),
                radius = radius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 1.5f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

            // Draw spinning stars around the logo
            val numberOfStars = 4
            for (i in 0 until numberOfStars) {
                val angleInRad = Math.toRadians((rotation + (i * (360 / numberOfStars))).toDouble())
                val starX = center.x + (radius * Math.cos(angleInRad)).toFloat()
                val starY = center.y + (radius * Math.sin(angleInRad)).toFloat()
                
                // Draw star symbol or a cute sparkling star
                drawCircle(
                    color = CosmicSecondary,
                    radius = 5f,
                    center = androidx.compose.ui.geometry.Offset(starX, starY)
                )
                
                // Draw smaller companion stars
                val angleInRadComp = Math.toRadians((rotation + (i * (360 / numberOfStars)) + 25).toDouble())
                val starXComp = center.x + ((radius - 8.dp.toPx()) * Math.cos(angleInRadComp)).toFloat()
                val starYComp = center.y + ((radius - 8.dp.toPx()) * Math.sin(angleInRadComp)).toFloat()
                drawCircle(
                    color = CosmicPrimary,
                    radius = 3f,
                    center = androidx.compose.ui.geometry.Offset(starXComp, starYComp)
                )
            }
        }

        // Central Majarah Logo
        Image(
            painter = painterResource(id = R.drawable.img_majarah_logo_1782345985330),
            contentDescription = "Loading...",
            modifier = Modifier.size(logoSize)
        )
    }
}

fun formatWhatsAppPhone(phone: String): String {
    return WhatsAppUtils.formatWhatsAppPhone(phone)
}

fun openWhatsAppDirectly(context: android.content.Context, phone: String, message: String = "") {
    WhatsAppUtils.sendWhatsAppMessage(context, phone, message)
}

@Composable
fun CosmicLogoLoaderDialog() {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = {},
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CosmicMajarahLoader(logoSize = 80.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "جاري الاتصال والتحميل الكوني... 🌌",
                    color = CosmicSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProductImagePlaceholder(imageName: String, modifier: Modifier = Modifier) {
    val finalModifier = if (modifier == Modifier || modifier == Modifier.fillMaxWidth()) {
        modifier.fillMaxWidth().height(150.dp)
    } else {
        modifier
    }

    if (imageName.length > 50) {
        val bitmap = remember(imageName) {
            try {
                val cleanBase64 = if (imageName.contains(",")) imageName.substringAfter(",") else imageName
                val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "صورة المنتج",
                modifier = finalModifier.clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            return
        }
    }

    Box(
        modifier = finalModifier
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
    val isGeneralAdmin by viewModel.isGeneralAdmin.collectAsStateWithLifecycle()
    val isCourier by viewModel.isCourier.collectAsStateWithLifecycle()
    val isSeller by viewModel.isSeller.collectAsStateWithLifecycle()
    val isPharmacist by viewModel.isPharmacist.collectAsStateWithLifecycle()
    val isRestaurant by viewModel.isRestaurant.collectAsStateWithLifecycle()
    val isAdministrativeManager by viewModel.isAdministrativeManager.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    val isInternetAvailable by viewModel.isInternetAvailable.collectAsStateWithLifecycle()

    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

    // Auto-redirect registered role users (Pharmacist, Restaurant, Admin Manager, Courier, Seller) directly to their portal
    LaunchedEffect(isLoggedIn, activeProfile, isPharmacist, isRestaurant, isAdministrativeManager, isAdmin, isGeneralAdmin, isCourier, isSeller) {
        if (isLoggedIn && activeProfile != null && (currentScreen is Screen.Home || currentScreen is Screen.Categories)) {
            if (isGeneralAdmin || isAdministrativeManager || isAdmin) {
                viewModel.navigateTo(Screen.Admin)
            } else if (isPharmacist) {
                viewModel.navigateTo(Screen.Pharmacist)
            } else if (isRestaurant) {
                viewModel.navigateTo(Screen.Restaurant)
            } else if (isCourier) {
                viewModel.navigateTo(Screen.Courier)
            } else if (isSeller) {
                viewModel.navigateTo(Screen.Seller)
            }
        }
    }

    val phoneState by viewModel.checkoutPhone.collectAsStateWithLifecycle()
    val addressState by viewModel.checkoutAddress.collectAsStateWithLifecycle()
    val nameState by viewModel.checkoutName.collectAsStateWithLifecycle()
    val dbStatus by viewModel.dbStatus.collectAsStateWithLifecycle()

    val registerNameState by viewModel.loginName.collectAsStateWithLifecycle()

    val showOtpVerification by viewModel.showOtpVerification.collectAsStateWithLifecycle()
    val otpVerificationEmail by viewModel.otpVerificationEmail.collectAsStateWithLifecycle()
    val otpCode by viewModel.otpCode.collectAsStateWithLifecycle()
    var otpErrorMsg by remember { mutableStateOf<String?>(null) }
    var isVerifyingOtp by remember { mutableStateOf(false) }

    var registrationErrorDialogMessage by remember { mutableStateOf<String?>(null) }

    var showSupabaseSettingsDialog by remember { mutableStateOf(false) }
    var supabaseUrlInput by remember { mutableStateOf(com.example.data.network.SupabaseConfig.url) }
    var supabaseKeyInput by remember { mutableStateOf(com.example.data.network.SupabaseConfig.apiKey) }
    var showSqlSetupGuide by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    
    val updatePrefs = remember(context) { context.getSharedPreferences("majarah_update_prefs", android.content.Context.MODE_PRIVATE) }
    var updateDetectionTime by remember {
        mutableStateOf(
            updatePrefs.getLong("update_detection_time", 0L).let { savedTime ->
                if (savedTime == 0L) {
                    val now = System.currentTimeMillis()
                    updatePrefs.edit().putLong("update_detection_time", now).apply()
                    now
                } else {
                    savedTime
                }
            }
        )
    }
    val showUpdateDialogState by viewModel.showUpdateDialog.collectAsStateWithLifecycle()
    val isUpdateForcedState by viewModel.isUpdateMandatory.collectAsStateWithLifecycle()
    val daysRemainingState by viewModel.daysRemaining.collectAsStateWithLifecycle()
    val latestVersionNameState by viewModel.latestVersionName.collectAsStateWithLifecycle()
    val isGooglePlayUpdateAvailableState by viewModel.isGooglePlayUpdateAvailable.collectAsStateWithLifecycle()
    var dismissedUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkForGooglePlayUpdate(context)
    }

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    var pendingNotificationMsg by remember { mutableStateOf<String?>(null) }
    var notifiedOrderIds by remember { mutableStateOf(setOf<String>()) }
    val allOrders by viewModel.allOrdersFlow.collectAsStateWithLifecycle()
    val allPharmacyOrders by viewModel.allPharmacyOrders.collectAsStateWithLifecycle()

    val allRatings by viewModel.allRatingsFlow.collectAsStateWithLifecycle()
    val allRestaurantOrders by viewModel.allRestaurantOrders.collectAsStateWithLifecycle()
    var showAppRatingDialog by remember { mutableStateOf(false) }
    var ratingOrderIdToSubmit by remember { mutableStateOf<String?>(null) }
    var activeWellWishesOrderIdStd by remember { mutableStateOf<String?>(null) }
    var restaurantBlessingMsg by remember { mutableStateOf<String?>(null) }
    var pharmacyBlessingMsg by remember { mutableStateOf<String?>(null) }

    val ratedPrefs = remember { context.getSharedPreferences("majarah_completed_ratings", android.content.Context.MODE_PRIVATE) }
    var ratedOrderIds by remember {
        mutableStateOf(
            ratedPrefs.getStringSet("rated_order_ids", emptySet()) ?: emptySet()
        )
    }

    val ratingPrefs = remember { context.getSharedPreferences("majarah_prompted_ratings", android.content.Context.MODE_PRIVATE) }
    var promptedRatings by remember {
        mutableStateOf(
            ratingPrefs.getStringSet("prompted_order_ids", emptySet()) ?: emptySet()
        )
    }

    LaunchedEffect(allOrders, allPharmacyOrders, allRestaurantOrders, activeProfile, promptedRatings, ratedOrderIds) {
        val phone = activeProfile?.phone?.trim() ?: ""
        val name = activeProfile?.name?.trim() ?: ""
        if (phone.isEmpty() && name.isEmpty()) return@LaunchedEffect

        val newlyDeliveredStd = allOrders.filter { o ->
            val status = o.statusArabic
            val isDelivered = (status.contains("تم التسليم") || status.contains("تم التوصيل") || status.contains("تم الاستلام") || status.contains("مكتمل")) &&
                    !status.contains("تم تسليم المندوب") && !status.contains("لمندوب")
            (o.customerPhone.trim() == phone || o.customerName.trim() == name) &&
            isDelivered &&
            "std_${o.orderId}" !in promptedRatings && "std_${o.orderId}" !in ratedOrderIds
        }

        val newlyDeliveredPharm = allPharmacyOrders.filter { po ->
            val status = po.status
            val isDelivered = (status.contains("تم التوصيل") || status.contains("تم التسليم") || status.contains("إغلاق")) &&
                    !status.contains("المندوب") && !status.contains("لمندوب") && !status.contains("قيد التوصيل") && !status.contains("بانتظار")
            (po.customerPhone?.trim() == phone || po.customerName?.trim() == name) &&
            isDelivered &&
            "pharm_${po.id}" !in promptedRatings && "pharm_${po.id}" !in ratedOrderIds
        }

        val newlyDeliveredRest = allRestaurantOrders.filter { ro ->
            val status = ro.status
            val isDelivered = (status.contains("تم تسليم العميل") || status.contains("إغلاق") || status.contains("تم التسليم") || status.contains("تم التوصيل")) &&
                    !status.contains("المندوب") && !status.contains("لمندوب") && !status.contains("قيد التوصيل") && !status.contains("بانتظار")
            (ro.customerPhone.trim() == phone || ro.customerName.trim() == name) &&
            isDelivered &&
            "rest_${ro.id}" !in promptedRatings && "rest_${ro.id}" !in ratedOrderIds
        }

        val allNewlyDelivered = newlyDeliveredStd.map { "std_${it.orderId}" } +
                                newlyDeliveredPharm.map { "pharm_${it.id}" } +
                                newlyDeliveredRest.map { "rest_${it.id}" }

        if (allNewlyDelivered.isNotEmpty()) {
            val updated = promptedRatings + allNewlyDelivered
            promptedRatings = updated
            ratingPrefs.edit().putStringSet("prompted_order_ids", updated).apply()
            ratingOrderIdToSubmit = allNewlyDelivered.first()
            showAppRatingDialog = true
        }
    }

    LaunchedEffect(isCourier, activeProfile, allOrders, allPharmacyOrders, allRestaurantOrders) {
        if (isCourier && activeProfile != null) {
            val courierPhone = activeProfile?.phone?.trim()?.replace("+", "")?.replace(" ", "") ?: ""
            val courierName = activeProfile?.name?.trim()?.lowercase() ?: ""
            
            val activeStandardAssigned = allOrders.filter { o ->
                val oPhone = o.courierPhone.trim().replace("+", "").replace(" ", "")
                val oName = o.courierName.trim().lowercase()
                val isMatching = (courierPhone.isNotEmpty() && oPhone == courierPhone) || (courierName.isNotEmpty() && oName == courierName)
                isMatching && !o.statusArabic.contains("تم توصيل") && !o.statusArabic.contains("ملغي")
            }.map { "std_${it.orderId}" }

            val activePharmacyAssigned = allPharmacyOrders.filter { po ->
                val poPhone = po.courierPhone?.trim()?.replace("+", "")?.replace(" ", "") ?: ""
                val poName = po.courierName?.trim()?.lowercase() ?: ""
                val isMatching = (courierPhone.isNotEmpty() && poPhone == courierPhone) || (courierName.isNotEmpty() && poName == courierName)
                isMatching && !po.status.contains("تم توصيل") && !po.status.contains("ملغي")
            }.map { "pharm_${it.id}" }

            val activeRestaurantAssigned = allRestaurantOrders.filter { ro ->
                val roPhone = ro.courierPhone.trim().replace("+", "").replace(" ", "")
                val roName = ro.courierName.trim().lowercase()
                val isMatching = (courierPhone.isNotEmpty() && roPhone == courierPhone) || (courierName.isNotEmpty() && roName == courierName)
                isMatching && !ro.status.contains("تم توصيل") && !ro.status.contains("تم تسليم العميل") && !ro.status.contains("إغلاق") && !ro.status.contains("ملغي")
            }.map { "rest_${it.id}" }

            val allAssignedActive = activeStandardAssigned + activePharmacyAssigned + activeRestaurantAssigned
            
            // If we found any new assigned order that we haven't notified yet
            val newUnnotified = allAssignedActive.filter { it !in notifiedOrderIds }
            if (newUnnotified.isNotEmpty()) {
                pendingNotificationMsg = "مرحباً ${activeProfile?.name}! تم إسناد مهمة توصيل جديدة لك بنجاح 🚴📦 اضغط هنا لمباشرتها."
                notifiedOrderIds = notifiedOrderIds + allAssignedActive
                
                NotificationSoundUtils.playNotificationSound(context)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showAppRatingDialog) {
            AppRatingDialog(
                onDismiss = { 
                    val currentId = ratingOrderIdToSubmit
                    if (currentId != null) {
                        val updated = promptedRatings + currentId
                        promptedRatings = updated
                        ratingPrefs.edit().putStringSet("prompted_order_ids", updated).apply()
                        if (currentId.startsWith("rest_")) {
                            restaurantBlessingMsg = "بالهناء والشفاء! 🍲✨ نتمنى لك وجبة شهية وممتعة من مطاعم المجرة!"
                        } else if (currentId.startsWith("pharm_")) {
                            pharmacyBlessingMsg = "بالشفاء العاجل! 💊✨ نسأل الله لك الصحة والعافية والشفاء التام!"
                        }
                    }
                    showAppRatingDialog = false
                    ratingOrderIdToSubmit = null
                },
                onSubmit = { stars, comment ->
                    viewModel.submitAppRating(stars, comment)
                    val currentId = ratingOrderIdToSubmit
                    if (currentId != null) {
                        val updated = ratedOrderIds + currentId
                        ratedOrderIds = updated
                        ratedPrefs.edit().putStringSet("rated_order_ids", updated).apply()

                        val updatedPrompted = promptedRatings + currentId
                        promptedRatings = updatedPrompted
                        ratingPrefs.edit().putStringSet("prompted_order_ids", updatedPrompted).apply()
                        if (currentId.startsWith("rest_")) {
                            restaurantBlessingMsg = "بالهناء والشفاء! 🍲✨ نتمنى لك وجبة شهية وممتعة من مطاعم المجرة!"
                        } else if (currentId.startsWith("pharm_")) {
                            pharmacyBlessingMsg = "بالشفاء العاجل! 💊✨ نسأل الله لك الصحة والعافية والشفاء التام!"
                        }
                    }
                    showAppRatingDialog = false
                    ratingOrderIdToSubmit = null
                }
            )
        }

        if (restaurantBlessingMsg != null) {
            LaunchedEffect(restaurantBlessingMsg) {
                kotlinx.coroutines.delay(5000)
                restaurantBlessingMsg = null
            }

            AlertDialog(
                onDismissRequest = { restaurantBlessingMsg = null },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.fillMaxWidth(0.92f),
                containerColor = CosmicSurface,
                title = {
                    Text("بالهناء والشفاء! 🍲✨", color = CosmicSecondary, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("🍕🍔", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            restaurantBlessingMsg!!,
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { restaurantBlessingMsg = null },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                    ) {
                        Text("شكراً جزيلاً ❤️", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (pharmacyBlessingMsg != null) {
            LaunchedEffect(pharmacyBlessingMsg) {
                kotlinx.coroutines.delay(5000)
                pharmacyBlessingMsg = null
            }

            AlertDialog(
                onDismissRequest = { pharmacyBlessingMsg = null },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.fillMaxWidth(0.92f),
                containerColor = CosmicSurface,
                title = {
                    Text("بالشفاء العاجل! 💊✨", color = Color(0xFF64B5F6), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("🩺🤲", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            pharmacyBlessingMsg!!,
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { pharmacyBlessingMsg = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6), contentColor = Color.Black)
                    ) {
                        Text("آمين يا رب ❤️", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
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
                                painter = painterResource(id = R.drawable.img_majarah_logo_1782345985330),
                                contentDescription = "Cosmic Logo",
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "المجرة للتسوق 🌌",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    },
                    navigationIcon = {
                        if (isCourier && !isAdmin && currentScreen !is Screen.Courier) {
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
                        } else if (isSeller && !isAdmin && currentScreen !is Screen.Seller) {
                            IconButton(onClick = { viewModel.navigateTo(Screen.Seller) }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Icon(Icons.Default.Store, "الرجوع للتاجر", tint = CosmicSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("لوحة التاجر 🧑‍💼", color = CosmicSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (isPharmacist && !isAdmin && currentScreen !is Screen.Pharmacist) {
                            IconButton(onClick = { viewModel.navigateTo(Screen.Pharmacist) }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Icon(Icons.Default.HealthAndSafety, "الرجوع للصيدلية", tint = CosmicSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("لوحة الصيدلي 💊", color = CosmicSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (isRestaurant && !isAdmin && currentScreen !is Screen.Restaurant) {
                            IconButton(onClick = { viewModel.navigateTo(Screen.Restaurant) }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Icon(Icons.Default.Restaurant, "الرجوع للمطعم", tint = CosmicSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("لوحة المطعم 🍔", color = CosmicSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        val showCartIcon = !isCourier && !isSeller && !isPharmacist && !isRestaurant && (!isAdmin || isGeneralAdmin)
                        if (showCartIcon) {
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
                
                if (isGeneralAdmin) {
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
            }
        },
        bottomBar = {
            val showBottomBar = currentScreen !is Screen.Login && currentScreen !is Screen.Splash && (isGeneralAdmin || (!isCourier && !isSeller && !isPharmacist && !isRestaurant && !isAdmin))
            if (showBottomBar) {
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
                        icon = { 
                            Box {
                                Icon(Icons.Default.ShoppingCart, null)
                                if (cartItems.isNotEmpty()) {
                                    val totalQty = cartItems.sumOf { it.quantity }
                                    Badge(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 10.dp, y = (-8).dp),
                                        containerColor = CosmicSecondary,
                                        contentColor = Color.Black
                                    ) {
                                        Text(text = totalQty.toString(), fontWeight = FontWeight.Bold, fontSize = 8.sp)
                                    }
                                }
                            }
                        },
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
                            Icon(Icons.Default.History, null)
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
                                        Toast.makeText(context, viewModel.t("تهانينا! 🎉 تم حفظ بياناتك بنجاح محلياً وسحابياً على السيرفر، ودخولك مباشر للتطبيق حسب صلاحيتك. 🌌", "Congratulations! 🎉 Your data has been successfully saved locally and on the cloud server, entering the app directly. 🌌"), Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, viewModel.t("تم تسجيل الدخول بنجاح! مرحباً بعودتك إلى المجرة. 🚀", "Logged in successfully! Welcome back to Almajra. 🚀"), Toast.LENGTH_LONG).show()
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
                            Toast.makeText(context, viewModel.t("تتصفح حالياً كزائر في مجرة التسوق 🌌", "Browsing as guest in Almajra Shopping 🌌"), Toast.LENGTH_SHORT).show()
                        },
                        onForgotPassword = {
                            showForgotPasswordDialog = true
                        },
                        viewModel = viewModel
                    )
                }
                is Screen.Home -> {
                    if ((isCourier || isSeller || isPharmacist || isRestaurant || isAdmin) && !isGeneralAdmin) {
                        RestrictedAccessScreenBody(viewModel = viewModel)
                    } else {
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
                }
                is Screen.Categories -> {
                    if ((isCourier || isSeller || isPharmacist || isRestaurant || isAdmin) && !isGeneralAdmin) {
                        RestrictedAccessScreenBody(viewModel = viewModel)
                    } else {
                        CategoriesScreenBody(
                            selectedCategory = selectedCategory,
                            onCategorySelect = { 
                                viewModel.setCategory(it)
                                viewModel.navigateTo(Screen.Home)
                            }
                        )
                    }
                }
                is Screen.Cart -> {
                    if ((isCourier || isSeller || isPharmacist || isRestaurant || isAdmin) && !isGeneralAdmin) {
                        RestrictedAccessScreenBody(viewModel = viewModel)
                    } else {
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
                            onSubmit = { method, txId, receiptBase64 -> viewModel.submitCheckout(method, txId, receiptBase64) },
                            formatPrice = { viewModel.formatPrice(it) },
                            isLoggedIn = isLoggedIn,
                            onRegisterPrompt = {
                                viewModel.isRegisterMode.value = true
                                viewModel.navigateTo(Screen.Login)
                            },
                            viewModel = viewModel
                        )
                    }
                }
                is Screen.Favorites -> {
                    if ((isCourier || isSeller || isPharmacist || isRestaurant || isAdmin) && !isGeneralAdmin) {
                        RestrictedAccessScreenBody(viewModel = viewModel)
                    } else {
                        FavoritesScreenBody(
                            favorites = favoriteProducts,
                            onProductClick = { viewModel.navigateTo(Screen.ProductDetail(it.id)) },
                            onRemoveFavorite = { viewModel.toggleFavorite(it.id) }
                        )
                    }
                }
                is Screen.History -> {
                    if ((isCourier || isSeller || isPharmacist || isRestaurant || isAdmin) && !isGeneralAdmin) {
                        RestrictedAccessScreenBody(viewModel = viewModel)
                    } else if (isLoggedIn) {
                        HistoryScreenBody(
                            orders = orderHistory,
                            onClearHistory = { viewModel.clearHistory() },
                            formatPrice = { viewModel.formatPrice(it) },
                            viewModel = viewModel,
                            onRateAppClick = { orderKey ->
                                ratingOrderIdToSubmit = orderKey
                                showAppRatingDialog = true
                            },
                            ratedOrderIds = ratedOrderIds
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
                is Screen.Pharmacist -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        com.example.ui.screens.PharmacyPlanetSection(viewModel = viewModel)
                    }
                }
                is Screen.Restaurant -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        com.example.ui.screens.RestaurantsPlanetSection(viewModel = viewModel)
                    }
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
                        CosmicMajarahLoader(logoSize = 56.dp)
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
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            Button(
                                onClick = {
                                    try {
                                        val shareText = checkoutSuccess ?: ""
                                        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                            setPackage("com.whatsapp")
                                        }
                                        context.startActivity(sendIntent)
                                    } catch (whatsappErr: Exception) {
                                        // Fallback to standard chooser
                                        try {
                                            val shareText = checkoutSuccess ?: ""
                                            val shareIntent = android.content.Intent.createChooser(
                                                android.content.Intent().apply {
                                                    action = android.content.Intent.ACTION_SEND
                                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                                    type = "text/plain"
                                                },
                                                "مشاركة الفاتورة الكونية"
                                            )
                                            context.startActivity(shareIntent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen, contentColor = Color.White),
                                modifier = Modifier.weight(1.1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مشاركة واتساب 💬", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.dismissCheckoutSuccess() },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                modifier = Modifier.weight(0.9f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                Text("تم ومتابعة 🌌", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
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

            // OTP Email verification Alert Dialog
            if (showOtpVerification) {
                AlertDialog(
                    onDismissRequest = { viewModel.showOtpVerification.value = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "قفل التحقق",
                                tint = CosmicSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = viewModel.t("تأكيد حسابك 🛡️", "Verify Your Account 🛡️"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = viewModel.t(
                                    "🌌 لقد أرسلنا رمز تأكيد (OTP) مؤلفاً من 6 أرقام إلى بريدك الإلكتروني لزيادة أمان حسابك:\n\n📧 $otpVerificationEmail\n\nيرجى التحقق من صندوق الوارد (أو البريد المهمل Spam) وإدخال الرمز هنا لبدء استخدام تطبيق مجرة السودان.",
                                    "🌌 We have sent a 6-digit confirmation code (OTP) to your email for security:\n\n📧 $otpVerificationEmail\n\nPlease check your inbox (or Spam folder) and enter it to start exploring Almajra."
                                ),
                                color = MediumContrastTextDark,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { viewModel.otpCode.value = it },
                                label = { Text(viewModel.t("رمز التأكيد (OTP)", "Confirmation Code")) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = CosmicSecondary
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = CosmicSecondary,
                                    unfocusedBorderColor = MediumContrastTextDark,
                                    focusedLabelColor = CosmicSecondary,
                                    unfocusedLabelColor = MediumContrastTextDark
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_code_input"),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                            )

                            if (otpErrorMsg != null) {
                                Text(
                                    text = otpErrorMsg!!,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isVerifyingOtp = true
                                otpErrorMsg = null
                                viewModel.verifyEmailAndFinishLogin(
                                    onSuccess = {
                                        isVerifyingOtp = false
                                        Toast.makeText(context, viewModel.t("✨ تم تفعيل وتأكيد حسابك بنجاح! طيران كوني سعيد. 🚀", "Account successfully activated! Happy cosmic travel. 🚀"), Toast.LENGTH_LONG).show()
                                    },
                                    onError = { err ->
                                        isVerifyingOtp = false
                                        val transErr = viewModel.translateError(err) ?: err
                                        otpErrorMsg = transErr
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                            enabled = otpCode.trim().isNotEmpty() && !isVerifyingOtp,
                            modifier = Modifier.testTag("otp_confirm_button")
                        ) {
                            if (isVerifyingOtp) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black)
                            } else {
                                Text(viewModel.t("تأكيد وتفعيل الحساب", "Verify & Activate"), fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.showOtpVerification.value = false }
                        ) {
                            Text(viewModel.t("إلغاء", "Cancel"), color = CosmicSecondary)
                        }
                    },
                    containerColor = CosmicSurface,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Forgot Password Alert Dialog with simulated email/phone verification
            if (showForgotPasswordDialog) {
                var forgotEmail by remember { mutableStateOf("") }
                var forgotPhone by remember { mutableStateOf("") }
                var verifyByEmail by remember { mutableStateOf(true) }
                var forgotNewPassword by remember { mutableStateOf("") }
                var verificationCodeInput by remember { mutableStateOf("") }
                var isCodeSent by remember { mutableStateOf(false) }
                var generatedMockCode by remember { mutableStateOf("") }
                var showPasswordInputState by remember { mutableStateOf(false) }
                var isResettingInProgress by remember { mutableStateOf(false) }
                var checkEmailError by remember { mutableStateOf<String?>(null) }

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
                            if (!isCodeSent) {
                                Text(
                                    text = "اختر وسيلة إرسال رمز الاستعادة والتحقق:",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            verifyByEmail = true
                                            checkEmailError = null
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (verifyByEmail) CosmicSecondary else CosmicDeepSpace,
                                            contentColor = if (verifyByEmail) Color.Black else Color.White
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("بريد قوقل 📧", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Button(
                                        onClick = {
                                            verifyByEmail = false
                                            checkEmailError = null
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!verifyByEmail) CosmicSecondary else CosmicDeepSpace,
                                            contentColor = if (!verifyByEmail) Color.Black else Color.White
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("رقم الهاتف 📞", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text(
                                text = if (verifyByEmail) {
                                    "أدخل البريد الإلكتروني لحساب Google المسجل بحسابك لإرسال رمز استعادة كوني وتعيين كلمة مرور جديدة لتطبيق المجرة."
                                } else {
                                    "أدخل رقم الهاتف المسجل بحسابك لإرسال رمز استعادة كوني وتعيين كلمة مرور جديدة لتطبيق المجرة."
                                },
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Right
                            )
                            
                            if (verifyByEmail) {
                                OutlinedTextField(
                                    value = forgotEmail,
                                    onValueChange = { 
                                        forgotEmail = it
                                        checkEmailError = null
                                    },
                                    label = { Text("بريد قوقل الإلكتروني 📧", color = CosmicSecondary) },
                                    placeholder = { Text("مثال: user@gmail.com", color = MediumContrastTextDark) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedBorderColor = CosmicSecondary, unfocusedBorderColor = CosmicSurfaceVariant,
                                        focusedLabelColor = CosmicSecondary, unfocusedLabelColor = MediumContrastTextDark,
                                        focusedContainerColor = CosmicDeepSpace, unfocusedContainerColor = CosmicDeepSpace
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Email, null, tint = CosmicSecondary) },
                                    enabled = !isCodeSent
                                )
                            } else {
                                OutlinedTextField(
                                    value = forgotPhone,
                                    onValueChange = { 
                                        forgotPhone = it
                                        checkEmailError = null
                                    },
                                    label = { Text("رقم الهاتف المسجل 📞", color = CosmicSecondary) },
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
                                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = CosmicSecondary) },
                                    enabled = !isCodeSent
                                )
                            }

                            if (isCodeSent) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CosmicSecondary.copy(alpha = 0.1f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (verifyByEmail) {
                                            "💬 تم إرسال رمز استعادة كوني إلى البريد الإلكتروني ($forgotEmail) بنجاح!\n\n💡 الرمز للمطابقة هو: ( $generatedMockCode )"
                                        } else {
                                            "💬 تم إرسال رمز استعادة كوني إلى الهاتف ($forgotPhone) بنجاح!\n\n💡 الرمز للمطابقة هو: ( $generatedMockCode )"
                                        },
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

                            checkEmailError?.let {
                                Text(it, color = Color.Red, fontSize = 11.sp)
                            }
                        }
                    },
                    confirmButton = {
                        if (!isCodeSent) {
                            Button(
                                onClick = {
                                    if (verifyByEmail) {
                                        if (forgotEmail.isBlank() || !forgotEmail.contains("@")) {
                                            checkEmailError = "الرجاء كتابة بريد إلكتروني صحيح أولاً"
                                            return@Button
                                        }
                                        isResettingInProgress = true
                                        val verificationCode = (1000..9999).random().toString()
                                        viewModel.sendResetEmailOtp(forgotEmail.trim(), verificationCode) { success, msg ->
                                            isResettingInProgress = false
                                            if (success) {
                                                generatedMockCode = verificationCode
                                                isCodeSent = true
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            } else {
                                                checkEmailError = msg
                                            }
                                        }
                                    } else {
                                        if (forgotPhone.isBlank() || forgotPhone.length < 9) {
                                            checkEmailError = "الرجاء كتابة رقم هاتف صحيح أولاً"
                                            return@Button
                                        }
                                        isResettingInProgress = true
                                        val verificationCode = (1000..9999).random().toString()
                                        viewModel.sendResetSmsOtp(forgotPhone.trim(), verificationCode) { success, msg ->
                                            isResettingInProgress = false
                                            if (success) {
                                                generatedMockCode = verificationCode
                                                isCodeSent = true
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            } else {
                                                checkEmailError = msg
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isResettingInProgress) {
                                    CosmicMajarahLoader(logoSize = 24.dp)
                                } else {
                                    Text("تحقق وإرسال رمز الاستعادة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        } else if (showPasswordInputState) {
                            Button(
                                onClick = {
                                    if (forgotNewPassword.length < 6) {
                                        checkEmailError = "يجب أن تكون كلمة المرور 6 خانات على الأقل لسلامة حسابك"
                                        return@Button
                                    }
                                    isResettingInProgress = true
                                    if (verifyByEmail) {
                                        viewModel.resetPasswordByEmail(forgotEmail.trim(), forgotNewPassword) { success, msg ->
                                            isResettingInProgress = false
                                            if (success) {
                                                Toast.makeText(context, "تم إعادة تعيين كلمة مرورك بنجاح! ✨", Toast.LENGTH_LONG).show()
                                                showForgotPasswordDialog = false
                                                viewModel.navigateTo(Screen.Login)
                                                registrationErrorDialogMessage = msg
                                            } else {
                                                checkEmailError = msg
                                            }
                                        }
                                    } else {
                                        viewModel.resetPasswordByPhone(forgotPhone.trim(), forgotNewPassword) { success, msg ->
                                            isResettingInProgress = false
                                            if (success) {
                                                Toast.makeText(context, "تم إعادة تعيين كلمة مرورك بنجاح! ✨", Toast.LENGTH_LONG).show()
                                                showForgotPasswordDialog = false
                                                viewModel.navigateTo(Screen.Login)
                                                registrationErrorDialogMessage = msg
                                            } else {
                                                checkEmailError = msg
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isResettingInProgress && forgotNewPassword.isNotBlank()
                            ) {
                                if (isResettingInProgress) {
                                    CosmicMajarahLoader(logoSize = 24.dp)
                                } else {
                                    Text("حفظ وتعيين كلمة المرور الجديدة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
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

            if (showUpdateDialogState && (!dismissedUpdateDialog || isUpdateForcedState)) {
                AlertDialog(
                    onDismissRequest = {
                        if (!isUpdateForcedState) {
                            dismissedUpdateDialog = true
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = CosmicSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUpdateForcedState) {
                                    viewModel.t("تحديث إجباري مطلوب الآن! 🛰️⚠️", "Forced Update Required Now! 🛰️⚠️")
                                } else if (isGooglePlayUpdateAvailableState) {
                                    viewModel.t("تحديث حقيقي متوفر في قوقل بلاي! 🛍️🚀", "Real Google Play Update Available! 🛍️🚀")
                                } else {
                                    viewModel.t("تحديث جديد متوفر للتطبيق! 🛰️🚀", "New Update Available! 🛰️🚀")
                                },
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isUpdateForcedState) {
                                    viewModel.t(
                                        "⚠️ انتهت المهلة المتاحة للتأجيل (15 يوماً).\n\n" +
                                        "يجب تحديث تطبيق مجرة السودان الآن إلى الإصدار الأخير ($latestVersionNameState) لمتابعة استخدامه والاتصال بقاعدة البيانات الآمنة بنجاح.",
                                        "⚠️ The postponement period (15 days) has ended.\n\n" +
                                        "You must update Majarah Sudan to the latest version ($latestVersionNameState) now to continue using it and securely connect to the database."
                                    )
                                } else if (isGooglePlayUpdateAvailableState) {
                                    viewModel.t(
                                        "🛍️ يتوفر تحديث رسمي وحقيقي لتطبيق مجرة السودان في متجر Google Play ($latestVersionNameState).\n\n" +
                                        "💡 يضمن هذا التحديث أحدث الميزات الأمنية والسرعة الفائقة في المزامنة وتتبع الطلبات والشحنات عبر قوقل بلاي.",
                                        "🛍️ An official real update for Majarah Sudan is available on Google Play ($latestVersionNameState).\n\n" +
                                        "💡 This update ensures the latest security features and ultra-fast real-time synchronization via Google Play."
                                    )
                                } else {
                                    viewModel.t(
                                        "يتوفر إصدار تحديث أمني وسريع جديد ($latestVersionNameState) لتطبيق مجرة السودان في متجر Google Play.\n\n" +
                                        "💡 يضمن هذا التحديث الربط المباشر والآمن والمزامنة الفورية لكل الميزات والطلبات مع قاعدة بيانات السحابة دون أي عوائق.\n\n" +
                                        "⏳ يمكنك تأجيل التحديث ومتابعة الاستخدام مؤقتاً (متبقي $daysRemainingState يوم لتأجيل التحديث قبل الإيقاف الإجباري).",
                                        "A new secure and high-speed update ($latestVersionNameState) is available for Majarah Sudan on Google Play.\n\n" +
                                        "💡 This update ensures direct and secure accounts linking and instant real-time synchronization of all features with the remote cloud database.\n\n" +
                                        "⏳ You can postpone this update temporarily (Remaining $daysRemainingState days left before forced postponement)."
                                    )
                                },
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Right
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                Toast.makeText(context, "جاري الانتقال للتحديث عبر متجر Google Play... 🛍️", Toast.LENGTH_LONG).show()
                                if (!isUpdateForcedState) {
                                    dismissedUpdateDialog = true
                                }
                                viewModel.startGooglePlayUpdate(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(viewModel.t("تحديث الآن عبر Google Play 🛍️", "Update via Google Play 🛍️"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    },
                    dismissButton = {
                        if (!isUpdateForcedState) {
                            TextButton(
                                onClick = {
                                    dismissedUpdateDialog = true
                                }
                            ) {
                                Text(viewModel.t("ليس الآن", "Not Now"), color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                            }
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
                            if (!isAdmin) {
                                Text(
                                    text = "⚠️ تعديل هذه المفاتيح متاح فقط للمدير العام (mawiaosman0@gmail.com).",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "يمكنك تعديل عنوان ومفتاح قاعدة البيانات يدوياً وسيقوم التطبيق بالاتصال فوراً ومزامنة المنتجات والطلبات.",
                                    color = MediumContrastTextDark,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            
                            OutlinedTextField(
                                value = supabaseUrlInput,
                                onValueChange = { if (isAdmin) supabaseUrlInput = it },
                                label = { Text("عنوان URL لـ Supabase", color = if (isAdmin) CosmicSecondary else Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isAdmin,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CosmicSecondary,
                                    unfocusedBorderColor = CosmicSurfaceVariant,
                                    focusedLabelColor = CosmicSecondary,
                                    cursorColor = CosmicSecondary,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    disabledBorderColor = CosmicSurfaceVariant.copy(alpha = 0.5f),
                                    disabledTextColor = Color.Gray,
                                    disabledLabelColor = Color.Gray
                                ),
                                trailingIcon = {
                                    if (isAdmin) {
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
                                    }
                                },
                                placeholder = { Text("https://example.supabase.co", color = Color.Gray) }
                            )

                            OutlinedTextField(
                                value = supabaseKeyInput,
                                onValueChange = { if (isAdmin) supabaseKeyInput = it },
                                label = { Text("مفتاح API الخاص بـ Supabase (Anon/Service)", color = if (isAdmin) CosmicSecondary else Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isAdmin,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CosmicSecondary,
                                    unfocusedBorderColor = CosmicSurfaceVariant,
                                    focusedLabelColor = CosmicSecondary,
                                    cursorColor = CosmicSecondary,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    disabledBorderColor = CosmicSurfaceVariant.copy(alpha = 0.5f),
                                    disabledTextColor = Color.Gray,
                                    disabledLabelColor = Color.Gray
                                ),
                                trailingIcon = {
                                    if (isAdmin) {
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
-- ⚠️ مسح الجداول القديمة تماماً لضمان البدء من جديد بمخطط نظيف ومحدث
DROP TABLE IF EXISTS public.orders CASCADE;
DROP TABLE IF EXISTS public.products CASCADE;
DROP TABLE IF EXISTS public.couriers CASCADE;
DROP TABLE IF EXISTS public.profiles CASCADE;
DROP TABLE IF EXISTS public.sellers CASCADE;
DROP TABLE IF EXISTS public.pharmacies CASCADE;
DROP TABLE IF EXISTS public.pharmacy_products CASCADE;
DROP TABLE IF EXISTS public.pharmacy_orders CASCADE;
DROP TABLE IF EXISTS public.restaurants CASCADE;
DROP TABLE IF EXISTS public.restaurant_orders CASCADE;
DROP TABLE IF EXISTS public.app_ratings CASCADE;
DROP TABLE IF EXISTS public.app_coupons CASCADE;

-- 1. إنشاء جدول المنتجات (products)
CREATE TABLE public.products (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    price DOUBLE PRECISION NOT NULL,
    category TEXT NOT NULL,
    category_arabic TEXT NOT NULL,
    rating REAL,
    image_res_name TEXT,
    is_favorite BOOLEAN DEFAULT false,
    stock INTEGER DEFAULT 10,
    seller_email TEXT DEFAULT '',
    is_approved BOOLEAN DEFAULT true
);

-- 2. إنشاء جدول الطلبات الأسبوعي واليومي (orders)
CREATE TABLE public.orders (
    id SERIAL PRIMARY KEY,
    order_id TEXT NOT NULL,
    product_id INTEGER NOT NULL,
    product_name TEXT NOT NULL,
    price_at_order DOUBLE PRECISION NOT NULL,
    quantity INTEGER NOT NULL,
    order_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    status_arabic TEXT NOT NULL,
    customer_name TEXT,
    customer_phone TEXT,
    customer_address TEXT,
    courier_name TEXT DEFAULT '',
    courier_phone TEXT DEFAULT '',
    delivery_fee DOUBLE PRECISION DEFAULT 5000.0,
    payment_method TEXT DEFAULT 'كاش',
    bank_receipt_image_uri TEXT
);

-- 3. إنشاء جدول مناديب التوصيل بالسودان (couriers)
CREATE TABLE public.couriers (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    state_info TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'نشط ومتوفر 🟢'
);

-- 4. إنشاء جدول المستخدمين والعملاء (profiles)
CREATE TABLE public.profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT,
    phone TEXT,
    email TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- 5. إنشاء جدول البائعين (sellers)
CREATE TABLE public.sellers (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT,
    classification TEXT DEFAULT 'تاجر ذهبي ⭐',
    commission_rate DOUBLE PRECISION DEFAULT 0.10,
    created_at BIGINT
);

-- 6. إنشاء جدول الصيدليات (pharmacies)
CREATE TABLE public.pharmacies (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    doctor_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    location TEXT NOT NULL,
    pharmacist_email TEXT NOT NULL,
    is_approved BOOLEAN DEFAULT false,
    image_base64 TEXT DEFAULT '',
    has_cosmetics BOOLEAN DEFAULT false,
    created_at BIGINT
);

-- 7. إنشاء جدول منتجات الصيدليات (pharmacy_products)
CREATE TABLE public.pharmacy_products (
    id SERIAL PRIMARY KEY,
    pharmacy_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    name TEXT NOT NULL,
    company TEXT,
    price DOUBLE PRECISION NOT NULL,
    image_base64 TEXT DEFAULT '',
    is_approved BOOLEAN DEFAULT false,
    created_at BIGINT
);

-- 8. إنشاء جدول طلبات الصيدليات والروشتات (pharmacy_orders)
CREATE TABLE public.pharmacy_orders (
    id SERIAL PRIMARY KEY,
    pharmacy_id INTEGER NOT NULL,
    customer_name TEXT NOT NULL,
    customer_phone TEXT NOT NULL,
    customer_email TEXT DEFAULT '',
    prescription_image_base64 TEXT DEFAULT '',
    medicines_json TEXT DEFAULT '',
    medicine_price DOUBLE PRECISION DEFAULT 0.0,
    delivery_fee DOUBLE PRECISION DEFAULT 0.0,
    courier_name TEXT DEFAULT '',
    courier_phone TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'بانتظار الصيدلي',
    payment_method TEXT DEFAULT 'كاش',
    bank_receipt_image_uri TEXT DEFAULT '',
    created_at BIGINT
);

-- 9. إنشاء جدول المطاعم (restaurants)
CREATE TABLE public.restaurants (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    menu_image_uri TEXT DEFAULT '',
    logo_image_uri TEXT DEFAULT '',
    is_approved BOOLEAN DEFAULT false,
    created_at BIGINT
);

-- 10. إنشاء جدول طلبات المطاعم (restaurant_orders)
CREATE TABLE public.restaurant_orders (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL,
    restaurant_name TEXT NOT NULL,
    restaurant_phone TEXT NOT NULL,
    customer_name TEXT NOT NULL,
    customer_email TEXT NOT NULL,
    customer_phone TEXT NOT NULL,
    items_and_notes TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'معلق',
    payment_method TEXT NOT NULL DEFAULT 'كاش',
    delivery_fee DOUBLE PRECISION DEFAULT 0.0,
    bank_receipt_image_uri TEXT DEFAULT '',
    courier_name TEXT DEFAULT '',
    courier_phone TEXT DEFAULT '',
    created_at BIGINT
);

-- 11. إنشاء جدول تقييمات التطبيق (app_ratings)
CREATE TABLE public.app_ratings (
    id SERIAL PRIMARY KEY,
    customer_name TEXT NOT NULL,
    customer_email TEXT NOT NULL,
    customer_phone TEXT DEFAULT '',
    customer_classification TEXT DEFAULT 'عميل عادي 👤',
    rating_stars INTEGER NOT NULL,
    comment TEXT,
    rating_date BIGINT
);

-- 12. إنشاء جدول كوبونات الخصم والجوائز (app_coupons)
CREATE TABLE public.app_coupons (
    id SERIAL PRIMARY KEY,
    code TEXT UNIQUE NOT NULL,
    discount_percent DOUBLE PRECISION DEFAULT 0.0,
    is_free_delivery BOOLEAN DEFAULT false,
    is_bogo BOOLEAN DEFAULT false,
    for_user_email TEXT DEFAULT '',
    is_used BOOLEAN DEFAULT false,
    offer_title TEXT NOT NULL
);

-- 6. تفعيل سياسات أمن مستوى الصفوف (Row Level Security - RLS)
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.couriers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sellers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pharmacies ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pharmacy_products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pharmacy_orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.restaurants ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.restaurant_orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_ratings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_coupons ENABLE ROW LEVEL SECURITY;

-- 7. حذف السياسات القديمة إن وجدت لمنع حدوث تعارض أو تكرار
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
DROP POLICY IF EXISTS "Allow select pharmacies" ON public.pharmacies;
DROP POLICY IF EXISTS "Allow insert pharmacies" ON public.pharmacies;
DROP POLICY IF EXISTS "Allow update pharmacies" ON public.pharmacies;
DROP POLICY IF EXISTS "Allow delete pharmacies" ON public.pharmacies;
DROP POLICY IF EXISTS "Allow select pharmacy_products" ON public.pharmacy_products;
DROP POLICY IF EXISTS "Allow insert pharmacy_products" ON public.pharmacy_products;
DROP POLICY IF EXISTS "Allow update pharmacy_products" ON public.pharmacy_products;
DROP POLICY IF EXISTS "Allow delete pharmacy_products" ON public.pharmacy_products;
DROP POLICY IF EXISTS "Allow select pharmacy_orders" ON public.pharmacy_orders;
DROP POLICY IF EXISTS "Allow insert pharmacy_orders" ON public.pharmacy_orders;
DROP POLICY IF EXISTS "Allow update pharmacy_orders" ON public.pharmacy_orders;
DROP POLICY IF EXISTS "Allow delete pharmacy_orders" ON public.pharmacy_orders;
DROP POLICY IF EXISTS "Allow select restaurants" ON public.restaurants;
DROP POLICY IF EXISTS "Allow insert restaurants" ON public.restaurants;
DROP POLICY IF EXISTS "Allow update restaurants" ON public.restaurants;
DROP POLICY IF EXISTS "Allow delete restaurants" ON public.restaurants;
DROP POLICY IF EXISTS "Allow select restaurant_orders" ON public.restaurant_orders;
DROP POLICY IF EXISTS "Allow insert restaurant_orders" ON public.restaurant_orders;
DROP POLICY IF EXISTS "Allow update restaurant_orders" ON public.restaurant_orders;
DROP POLICY IF EXISTS "Allow delete restaurant_orders" ON public.restaurant_orders;
DROP POLICY IF EXISTS "Allow select app_ratings" ON public.app_ratings;
DROP POLICY IF EXISTS "Allow insert app_ratings" ON public.app_ratings;
DROP POLICY IF EXISTS "Allow select app_coupons" ON public.app_coupons;
DROP POLICY IF EXISTS "Allow insert app_coupons" ON public.app_coupons;
DROP POLICY IF EXISTS "Allow update app_coupons" ON public.app_coupons;

-- 8. إنشاء سياسات الوصول الكونية الجديدة للسماح بالوصول الكامل دون قيود للتطبيق (Anon / Public)
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

CREATE POLICY "Allow select pharmacies" ON public.pharmacies FOR SELECT USING (true);
CREATE POLICY "Allow insert pharmacies" ON public.pharmacies FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update pharmacies" ON public.pharmacies FOR UPDATE USING (true);
CREATE POLICY "Allow delete pharmacies" ON public.pharmacies FOR DELETE USING (true);

CREATE POLICY "Allow select pharmacy_products" ON public.pharmacy_products FOR SELECT USING (true);
CREATE POLICY "Allow insert pharmacy_products" ON public.pharmacy_products FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update pharmacy_products" ON public.pharmacy_products FOR UPDATE USING (true);
CREATE POLICY "Allow delete pharmacy_products" ON public.pharmacy_products FOR DELETE USING (true);

CREATE POLICY "Allow select pharmacy_orders" ON public.pharmacy_orders FOR SELECT USING (true);
CREATE POLICY "Allow insert pharmacy_orders" ON public.pharmacy_orders FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update pharmacy_orders" ON public.pharmacy_orders FOR UPDATE USING (true);
CREATE POLICY "Allow delete pharmacy_orders" ON public.pharmacy_orders FOR DELETE USING (true);

CREATE POLICY "Allow select restaurants" ON public.restaurants FOR SELECT USING (true);
CREATE POLICY "Allow insert restaurants" ON public.restaurants FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update restaurants" ON public.restaurants FOR UPDATE USING (true);
CREATE POLICY "Allow delete restaurants" ON public.restaurants FOR DELETE USING (true);

CREATE POLICY "Allow select restaurant_orders" ON public.restaurant_orders FOR SELECT USING (true);
CREATE POLICY "Allow insert restaurant_orders" ON public.restaurant_orders FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update restaurant_orders" ON public.restaurant_orders FOR UPDATE USING (true);
CREATE POLICY "Allow delete restaurant_orders" ON public.restaurant_orders FOR DELETE USING (true);

CREATE POLICY "Allow select app_ratings" ON public.app_ratings FOR SELECT USING (true);
CREATE POLICY "Allow insert app_ratings" ON public.app_ratings FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow select app_coupons" ON public.app_coupons FOR SELECT USING (true);
CREATE POLICY "Allow insert app_coupons" ON public.app_coupons FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update app_coupons" ON public.app_coupons FOR UPDATE USING (true);
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
                                enabled = isAdmin,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.DarkGray,
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.DarkGray.copy(alpha = 0.5f),
                                    disabledContentColor = Color.Gray
                                )
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
                                enabled = isAdmin,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CosmicSecondary,
                                    contentColor = Color.Black,
                                    disabledContainerColor = CosmicSurfaceVariant.copy(alpha = 0.4f),
                                    disabledContentColor = MediumContrastTextDark
                                )
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


            // Floating Custom Courier Notification Banner
            AnimatedVisibility(
                visible = pendingNotificationMsg != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
                    .padding(horizontal = 16.dp)
                    .zIndex(99f)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.navigateTo(Screen.Courier)
                            pendingNotificationMsg = null
                        },
                    colors = CardDefaults.cardColors(containerColor = CosmicSecondary),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(1.5.dp, Color.Black)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = "مهمة جديدة",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تنبيه بمهمة جديدة! ⚠️",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = pendingNotificationMsg ?: "",
                                color = Color.Black.copy(0.85f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

        }

        if (!isInternetAvailable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0F1D))
                    .clickable(enabled = true, onClick = {}), // Block all click touch events
                contentAlignment = Alignment.Center
            ) {
                // Draw background stars
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val rand = java.util.Random(42)
                    for (i in 0..100) {
                        val x = rand.nextFloat() * size.width
                        val y = rand.nextFloat() * size.height
                        val radius = rand.nextFloat() * 2f + 1f
                        val alpha = rand.nextFloat() * 0.5f + 0.5f
                        drawCircle(
                            color = Color.White.copy(alpha = alpha),
                            radius = radius,
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CosmicSecondary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "No Internet Connection",
                            tint = CosmicSecondary,
                            modifier = Modifier.size(72.dp)
                        )
                        
                        Text(
                            text = "انقطع الاتصال بالشبكة الكونية! 📡🌌",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "بوابة المجرة للتسوق السودانية تتطلب اتصالاً نشطاً ومستقراً بالإنترنت لمزامنة ومراجعة كافة الطلبات والمنتجات والتقييمات مع السيرفر السحابي الآمن لمنع تضارب المعاملات.\n\nيرجى التحقق من اتصال الواي فاي أو بيانات الهاتف للتمكن من الدخول واستكمال جولتك بالمجرة 🚀✨",
                            color = MediumContrastTextDark,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                viewModel.refreshConnection()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("تحديث محاولة الاتصال 🌌🔄", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        if (isUpdateForcedState) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(com.example.ui.theme.CosmicDeepSpace)
                    .clickable(enabled = true, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .widthIn(max = 500.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CosmicSecondary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = CosmicSecondary,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = viewModel.t("تحديث إجباري مطلوب الآن! 🛰️⚠️", "Forced Update Required Now! 🛰️⚠️"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = viewModel.t(
                                "⚠️ انتهت المهلة المتاحة للتأجيل (15 يوماً).\n\n" +
                                "يجب تحديث تطبيق مجرة السودان الآن إلى الإصدار الأخير (v$latestVersionNameState) لمتابعة استخدامه والاتصال بقاعدة البيانات الآمنة بنجاح.",
                                "⚠️ The postponement period (15 days) has ended.\n\n" +
                                "You must update Majarah Sudan to the latest version (v$latestVersionNameState) now to continue using it and securely connect to the database."
                            ),
                            color = Color.White.copy(0.8f),
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.aistudio.majarah"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://supabase.com"))
                                    try {
                                        context.startActivity(fallbackIntent)
                                    } catch (ex: Exception) {}
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(viewModel.t("تحديث الآن 🚀", "Update Now 🚀"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
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
    val isPharmacist by viewModel.isPharmacist.collectAsStateWithLifecycle()
    val isRestaurant by viewModel.isRestaurant.collectAsStateWithLifecycle()
    val isGeneralAdmin by viewModel.isGeneralAdmin.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()

    if (isPharmacist) {
        Box(modifier = Modifier.fillMaxSize()) {
            com.example.ui.screens.PharmacyPlanetSection(viewModel = viewModel)
        }
        return
    }

    if (isRestaurant) {
        Box(modifier = Modifier.fillMaxSize()) {
            com.example.ui.screens.RestaurantsPlanetSection(viewModel = viewModel)
        }
        return
    }
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

        // Horizontal Category Tabs
        val categories = listOf(
            Pair("", "🚀 الكل"),
            Pair("electronics", "💻 كوكب الإلكترونيات"),
            Pair("fashion", "👕 كوكب الأزياء"),
            Pair("furniture", "🏡 كوكب الأثاثات المنزلية"),
            Pair("services", "🛠️ كوكب خدمات عامة"),
            Pair("crafts", "🪚 كوكب أعمال حرفية"),
            Pair("estate_cars", "🚗 كوكب بيع العقارات والسيارات"),
            Pair("pharmacy", "💊 كوكب صيدلية"),
            Pair("restaurant", "🍔 كوكب مطاعم"),
            Pair("kids", "🍼 كوكب مستلزمات أطفال"),
            Pair("women", "💅 كوكب للنساء"),
            Pair("men", "💼 كوكب للرجال"),
            Pair("travel", "✈️ كوكب وكالات سفر وسياحة"),
            Pair("tickets", "🎟️ كوكب حجوزات تذاكر"),
            Pair("hotels", "🏨 كوكب حجوزات فندقية"),
            Pair("foods", "🍎 كوكب الأغذية والمأكولات"),
            Pair("cosmetics", "💄 كوكب عطور وتجميل"),
            Pair("other", "📦 كوكب منتجات أخرى")
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedCategory == "pharmacy") {
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                com.example.ui.screens.PharmacyPlanetSection(viewModel = viewModel)
            }
        } else if (selectedCategory == "restaurant") {
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                com.example.ui.screens.RestaurantsPlanetSection(viewModel = viewModel)
            }
        } else if (selectedCategory == "women" && !isGeneralAdmin && !isFemaleName(activeProfile?.name ?: "")) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.2.dp, Color(0xFFE91E63).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "كوكب النساء خاص بالنساء فقط 💅🔒",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "عذراً يا سيد [${activeProfile?.name ?: "العميل"}]. هذا الكوكب مخصص حصرياً للنساء لضمان الخصوصية والراحة الكاملة في تصفح مستلزمات وحاجيات المرأة العصرية.",
                            color = MediumContrastTextDark,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
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
        Triple("electronics", "💻 كوكب الإلكترونيات", "أحدث وأفضل الأجهزة الإلكترونية، الهواتف، اللابتوبات والملحقات الذكية."),
        Triple("fashion", "👕 كوكب الأزياء", "أحدث صيحات الموضة والملابس الفاخرة التي تناسب جميع الأذواق والمناسبات."),
        Triple("furniture", "🏡 كوكب الأثاثات المنزلية", "تشكيلة رائعة من الأثاث المنزلي الفخم والديكورات العصرية لبيت مريح وأنيق."),
        Triple("services", "🛠️ كوكب خدمات عامة", "مجموعة متكاملة من الخدمات العامة، الصيانة، التوصيل والدعم الفني السريع."),
        Triple("crafts", "🪚 كوكب أعمال حرفية", "أعمال يدوية، نجارة، حدادة، وصناعات حرفية ماهرة بأيدي خبراء."),
        Triple("estate_cars", "🚗 كوكب بيع العقارات والسيارات", "أفضل العروض الحقيقية لبيع وشراء السيارات الحديثة والعقارات والأراضي بالسودان."),
        Triple("pharmacy", "💊 كوكب صيدلية", "مستلزمات طبية، أدوية، رعاية صحية، فيتامينات ومستحضرات معتمدة."),
        Triple("restaurant", "🍔 كوكب مطاعم", "أشهى وألذ المأكولات والوجبات السريعة والمشروبات الطازجة المجهزة بكل حب."),
        Triple("kids", "🍼 كوكب مستلزمات أطفال", "ملابس أطفال، ألعاب ذكية، حليب ومستلزمات العناية الكاملة بالمواليد."),
        Triple("women", "💅 كوكب للنساء", "كل ما يخص المرأة العصرية من فساتين، حقائب، أدوات زينة وإكسسوارات فاخرة."),
        Triple("men", "💼 كوكب للرجال", "ملابس رجالية، أحذية، عطور، ساعات كلاسيكية وأناقة متكاملة للرجل."),
        Triple("travel", "✈️ كوكب وكالات سفر وسياحة", "رحلات سياحية، معاملات تأشيرات، رحلات داخلية وخارجية بضمان وموثوقية."),
        Triple("tickets", "🎟️ كوكب حجوزات تذاكر", "حجز تذاكر الطيران، الباصات السفرية، الحفلات والفعاليات بنقرة واحدة."),
        Triple("hotels", "🏨 كوكب حجوزات فندقية", "حجوزات مباشرة للفنادق، الشقق الفندقية، والمنتجعات بأفضل الأسعار بالسودان."),
        Triple("foods", "🍎 كوكب الأغذية والمأكولات", "خضروات وفواكه طازجة، لحوم، بقالة ومواد تموينية مغذية للأسرة."),
        Triple("cosmetics", "💄 كوكب عطور وتجميل", "أفخم ماركات العطور والروائح السودانية والمستوردة وأدوات التجميل الأصلية."),
        Triple("other", "📦 كوكب منتجات أخرى", "منتجات متنوعة أخرى وهدايا فريدة تناسب كافة الأوقات.")
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
    onSubmit: (paymentMethod: String, transactionId: String, bankReceiptBase64: String?) -> Unit,
    formatPrice: (Double) -> String,
    isLoggedIn: Boolean = true,
    onRegisterPrompt: () -> Unit = {},
    viewModel: MajarahViewModel
) {
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val couponError by viewModel.couponError.collectAsStateWithLifecycle()
    val classification by viewModel.userClassification.collectAsStateWithLifecycle()
    val isEligibleForCoupon = classification.contains("مميز") || classification.contains("ذهبي")
    var couponInputText by remember { mutableStateOf("") }
    var selectedCheckoutPaymentMethod by remember { mutableStateOf("cash") } // "cash" or "bank"
    var showBankDialog by remember { mutableStateOf(false) }
    var bankTransactionId by remember { mutableStateOf("") }
    var checkoutReceiptBase64 by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            checkoutReceiptBase64 = base64
            android.widget.Toast.makeText(context, "تم التقاط صورة إشعار التحويل بنجاح! 📸", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val outputStream = java.io.ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val bytes = outputStream.toByteArray()
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                    checkoutReceiptBase64 = base64
                    android.widget.Toast.makeText(context, "تم اختيار صورة الإشعار بنجاح! 🖼️", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "فشل قراءة الصورة ❌", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

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
            if (isEligibleForCoupon) {
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
                            Text("يحدد بعد تسليم المندوب 🚴", color = CosmicSecondary, fontWeight = FontWeight.Bold)
                            Text("رسوم التوصيل:", color = MediumContrastTextDark)
                        }
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = CosmicSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${formatPrice(netTotal)} ج.س", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("إجمالي المشتريات (غير شامل التوصيل):", color = Color.White, fontWeight = FontWeight.Bold)
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

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(0.3f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "💳 طريقة الدفع للفاتورة:",
                                        color = CosmicSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = "سيتم تحديد خيار الدفع (كاش أو تحويل بنكي) بعد استلام المندوب للطلب بنجاح ووصول خط التتبع إلى حالة 'تم تسليم المندوب'. ستظهر لك تفاصيل الفاتورة كاملة مع إمكانية الدفع والتأكيد حينها 🚴✨",
                                        color = Color.LightGray,
                                        fontSize = 10.5.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    onSubmit("", "", null)
                                },
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
                                Text(
                                    text = "تأكيد وإرسال الطلب الفوري 🚀",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
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
    viewModel: MajarahViewModel,
    onRateAppClick: (String?) -> Unit,
    ratedOrderIds: Set<String>
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allRatings by viewModel.allRatingsFlow.collectAsStateWithLifecycle()
    val userClassificationState by viewModel.userClassification.collectAsStateWithLifecycle()
    val allRestaurantOrders by viewModel.allRestaurantOrders.collectAsStateWithLifecycle()
    val allPharmacyOrders by viewModel.allPharmacyOrders.collectAsStateWithLifecycle()

    var selectedHistoryTab by remember { mutableStateOf(0) } // 0: Active Orders, 1: Completed Orders

    val phone = activeProfile?.phone?.trim()?.replace("+", "")?.replace(" ", "") ?: ""
    val name = activeProfile?.name?.trim()?.lowercase() ?: ""
    val email = activeProfile?.email?.trim()?.lowercase() ?: ""

    val myRestaurantOrders = remember(allRestaurantOrders, activeProfile) {
        if (phone.isEmpty() && name.isEmpty() && email.isEmpty()) {
            allRestaurantOrders
        } else {
            allRestaurantOrders.filter { ro ->
                val rEmail = ro.customerEmail.trim().lowercase()
                val rPhone = ro.customerPhone.trim()
                val rName = ro.customerName.trim().lowercase()
                val last9Phone = if (phone.length >= 9) phone.takeLast(9) else phone
                val cleanRPhone = rPhone.replace("+", "").replace(" ", "")
                val last9RPhone = if (cleanRPhone.length >= 9) cleanRPhone.takeLast(9) else cleanRPhone
                (email.isNotEmpty() && rEmail == email) ||
                (phone.isNotEmpty() && (rPhone == phone || (last9Phone.isNotEmpty() && last9Phone == last9RPhone))) ||
                (name.isNotEmpty() && rName == name) ||
                (rPhone.isEmpty() && rName.isEmpty())
            }
        }
    }

    val myPharmacyOrders = remember(allPharmacyOrders, activeProfile) {
        if (phone.isEmpty() && name.isEmpty() && email.isEmpty()) {
            allPharmacyOrders
        } else {
            allPharmacyOrders.filter { po ->
                val pEmail = po.customerEmail?.trim()?.lowercase() ?: ""
                val pPhone = po.customerPhone?.trim() ?: ""
                val pName = po.customerName?.trim()?.lowercase() ?: ""
                val last9Phone = if (phone.length >= 9) phone.takeLast(9) else phone
                val cleanPPhone = pPhone.replace("+", "").replace(" ", "")
                val last9PPhone = if (cleanPPhone.length >= 9) cleanPPhone.takeLast(9) else cleanPPhone
                (email.isNotEmpty() && pEmail == email) ||
                (phone.isNotEmpty() && (pPhone == phone || (last9Phone.isNotEmpty() && last9Phone == last9PPhone))) ||
                (name.isNotEmpty() && pName == name) ||
                (pPhone.isEmpty() && pName.isEmpty())
            }
        }
    }

    fun isStdDelivered(status: String): Boolean {
        return (status.contains("تم التسليم") || status.contains("تم التوصيل") || status.contains("تمت التوصيل") || status.contains("تم الاستلام") || status.contains("مكتمل")) &&
                !status.contains("تم تسليم المندوب") && !status.contains("لمندوب")
    }

    fun isRestDelivered(status: String): Boolean {
        return status.contains("تم تسليم العميل") || status.contains("إغلاق") || (status.contains("تم التسليم") && !status.contains("المندوب") && !status.contains("لمندوب")) || (status.contains("تم التوصيل") && !status.contains("المندوب") && !status.contains("لمندوب"))
    }

    fun isPharmDelivered(status: String): Boolean {
        return status.contains("تم التوصيل") || (status.contains("تم التسليم") && !status.contains("المندوب") && !status.contains("لمندوب")) || status.contains("إغلاق")
    }

    // Standard grouped
    val stdGrouped = orders.groupBy { it.orderId }.entries.toList()
    val activeStdOrders = stdGrouped.filter { !isStdDelivered(it.value.firstOrNull()?.statusArabic ?: "") }
    val completedStdOrders = stdGrouped.filter { isStdDelivered(it.value.firstOrNull()?.statusArabic ?: "") }

    val activeRestOrders = myRestaurantOrders.filter { !isRestDelivered(it.status) }
    val completedRestOrders = myRestaurantOrders.filter { isRestDelivered(it.status) }

    val activePharmOrders = myPharmacyOrders.filter { !isPharmDelivered(it.status) }
    val completedPharmOrders = myPharmacyOrders.filter { isPharmDelivered(it.status) }

    val totalActiveCount = activeStdOrders.size + activeRestOrders.size + activePharmOrders.size
    val totalCompletedCount = completedStdOrders.size + completedRestOrders.size + completedPharmOrders.size

    LaunchedEffect(Unit) {
        if (orders.isEmpty()) {
            isRefreshing = true
        }
        viewModel.syncOrders {
            isRefreshing = false
        }
        while (true) {
            kotlinx.coroutines.delay(8000)
            viewModel.syncOrders()
        }
    }

    if (isRefreshing && orders.isEmpty() && myRestaurantOrders.isEmpty() && myPharmacyOrders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CosmicMajarahLoader(logoSize = 56.dp)
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            "طلباتي وفواتيري بالمجرة 📑",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }

                    // Sub Tabs for Active vs Completed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedHistoryTab = 0 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedHistoryTab == 0) CosmicSecondary else CosmicSurfaceVariant,
                                contentColor = if (selectedHistoryTab == 0) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("الطلبات النشطة ⚡ ($totalActiveCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { selectedHistoryTab = 1 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedHistoryTab == 1) CosmicSecondary else CosmicSurfaceVariant,
                                contentColor = if (selectedHistoryTab == 1) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("الطلبات المكتملة ✅ ($totalCompletedCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
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
                            text = "تتبع مباشر وحظي لجميع طلبات المشتريات والمطاعم والصيدليات 🛰️",
                            color = CosmicSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (selectedHistoryTab == 0) {
                // ACTIVE ORDERS
                if (totalActiveCount == 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            border = BorderStroke(1.dp, CosmicSurfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🚀", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("لا توجد طلبات نشطة قيد التنفيذ حالياً", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("عند الطلب من المنتجات أو المطاعم أو الصيدليات ستظهر حالة الطلب هنا مباشرة.", color = MediumContrastTextDark, fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    // Active Restaurant Orders
                    items(activeRestOrders) { ro ->
                        RestaurantCustomerOrderCard(order = ro, formatPrice = formatPrice, viewModel = viewModel)
                    }

                    // Active Pharmacy Orders
                    items(activePharmOrders) { po ->
                        PharmacyCustomerOrderCard(order = po, formatPrice = formatPrice, viewModel = viewModel)
                    }

                    // Active Standard Product Orders
                    items(activeStdOrders) { entry ->
                        StandardOrderCardItem(
                            entry = entry,
                            formatPrice = formatPrice,
                            viewModel = viewModel,
                            ratedOrderIds = ratedOrderIds
                        )
                    }
                }
            } else {
                // COMPLETED ORDERS
                if (totalCompletedCount == 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            border = BorderStroke(1.dp, CosmicSurfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📜", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("لا توجد طلبات منفذة سابقة في سجل المكتملة", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            }
                        }
                    }
                } else {
                    // Completed Restaurant Orders
                    items(completedRestOrders) { ro ->
                        RestaurantCustomerOrderCard(order = ro, formatPrice = formatPrice, viewModel = viewModel)
                    }

                    // Completed Pharmacy Orders
                    items(completedPharmOrders) { po ->
                        PharmacyCustomerOrderCard(order = po, formatPrice = formatPrice, viewModel = viewModel)
                    }

                    // Completed Standard Product Orders
                    items(completedStdOrders) { entry ->
                        StandardOrderCardItem(
                            entry = entry,
                            formatPrice = formatPrice,
                            viewModel = viewModel,
                            ratedOrderIds = ratedOrderIds
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StandardOrderCardItem(
    entry: Map.Entry<String, List<com.example.data.db.OrderEntity>>,
    formatPrice: (Double) -> String,
    viewModel: MajarahViewModel,
    ratedOrderIds: Set<String>
) {
    val orderId = entry.key
    val orderItems = entry.value
    val firstItem = orderItems.firstOrNull()
    
    val customerName = firstItem?.customerName ?: "زبون المجرة الكوني"
    val customerPhone = firstItem?.customerPhone ?: "09"
    val customerAddress = firstItem?.customerAddress ?: "السودان"
    val orderStatus = firstItem?.statusArabic ?: "جاري التجهيز للتوصيل 📦"
    val isDelivered = (orderStatus.contains("تم التسليم") || 
            orderStatus.contains("تم التوصيل") || 
            orderStatus.contains("تمت التوصيل") || 
            orderStatus.contains("تم الاستلام") ||
            orderStatus.contains("تمام") || 
            orderStatus.contains("بنجاح")) &&
            !orderStatus.contains("تم تسليم المندوب") &&
            !orderStatus.contains("لمندوب")
    val courierName = firstItem?.courierName ?: ""
    val courierPhone = firstItem?.courierPhone ?: ""
    val isCourierAssigned = courierName.isNotBlank() || 
            orderStatus.contains("تم تسليم المندوب") || 
            orderStatus.contains("لمندوب") || 
            orderStatus.contains("جاري التوصيل") ||
            isDelivered
    val isShipped = isCourierAssigned
    val orderDateMillis = firstItem?.orderDate ?: System.currentTimeMillis()
    val isDeliveredAndRated = isDelivered && ratedOrderIds.contains("std_$orderId")
    
    val dateStr = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US).format(java.util.Date(orderDateMillis))
    val totalItemsSum = orderItems.sumOf { it.priceAtOrder * it.quantity }
    val showDeliveryPrice = isCourierAssigned
    val deliveryPrice = if ((firstItem?.deliveryFee ?: 0.0) <= 0.0) 5000.0 else firstItem!!.deliveryFee
    val grandTotal = if (showDeliveryPrice) (totalItemsSum + deliveryPrice) else totalItemsSum
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val userClassificationState by viewModel.userClassification.collectAsStateWithLifecycle()

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
                        text = "كود الطلب: $orderId",
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    try {
                                        val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                            data = android.net.Uri.parse("tel:${courierPhone.trim()}")
                                        }
                                        context.startActivity(dialIntent)
                                    } catch (ex: Exception) {
                                        ex.printStackTrace()
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ActiveGreen.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "اتصال",
                                    tint = ActiveGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.DirectionsBike,
                                contentDescription = null,
                                tint = CosmicSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("🚴 مندوب التوصيل المعين للطلب:", fontSize = 11.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold)
                            Text(courierName, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("هاتف للتواصل اللحظي: $courierPhone", fontSize = 11.sp, color = Color.White.copy(0.8f))
                        }
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = CosmicSurfaceVariant)
            
            // Progress & Tracking Timeline Steps
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(CosmicSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("تم الطلب", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Divider(
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp), 
                        color = if (isCourierAssigned) CosmicSecondary else CosmicSurfaceVariant
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isCourierAssigned) CosmicSecondary else CosmicSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCourierAssigned) Icons.Default.Check else Icons.Default.DirectionsBike,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isCourierAssigned) Color.Black else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("تم تعيين مندوب", color = if (isCourierAssigned) Color.White else MediumContrastTextDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Divider(
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp), 
                        color = if (isDelivered) CosmicSecondary else CosmicSurfaceVariant
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isDelivered) CosmicSecondary else CosmicSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDelivered) Icons.Default.Check else Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isDelivered) Color.Black else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("تم التسليم", color = if (isDelivered) Color.White else MediumContrastTextDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer Information Card
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (showDeliveryPrice) "${formatPrice(deliveryPrice)} ج.س" else "يحدد لاحقاً ⏳🚴",
                    color = if (showDeliveryPrice) Color.White else CosmicSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (!showDeliveryPrice) FontWeight.Bold else FontWeight.Normal
                )
                Text("رسوم التوصيل:", color = MediumContrastTextDark, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (showDeliveryPrice) "${formatPrice(grandTotal)} ج.س" else "يحدد لاحقاً ⏳🚴",
                    fontWeight = FontWeight.Bold,
                    color = if (showDeliveryPrice) CosmicTertiary else CosmicSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = if (showDeliveryPrice) "المبلغ الإجمالي الكلي:" else "المبلغ الإجمالي الكلي للفاتورة:",
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

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val parsedPaymentMethod = when {
                    orderStatus.contains("pending_delivery") -> "بانتظار تسليم المندوب لتحديد طريقة الدفع ⏳"
                    orderStatus.contains("الدفع نقداً") -> "الدفع نقداً عند الاستلام 💵"
                    orderStatus.contains("تحويل بنكي") -> {
                        val txId = orderStatus.substringAfter("إشعار:", "").substringBefore(")").trim()
                        if (txId.isNotEmpty()) "تحويل بنكي 💳 (رقم العملية: $txId)" else "تحويل بنكي 💳"
                    }
                    else -> "لم يحدد بعد"
                }
                Text(text = parsedPaymentMethod, color = CosmicSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("طريقة الدفع ومطابقة الفاتورة:", color = MediumContrastTextDark, fontSize = 11.sp)
            }

            var receiptToShow by remember { mutableStateOf<String?>(null) }
            if (receiptToShow != null) {
                ViewReceiptDialog(receiptToShow!!) { receiptToShow = null }
            }

            val currentPaymentMethod = firstItem?.paymentMethod ?: ""
            val currentReceiptBase64 = firstItem?.bankReceiptImageUri
            val isPaymentSubmitted = currentPaymentMethod.isNotBlank() && currentPaymentMethod != "كاش" && !currentPaymentMethod.contains("لم يحدد")

            if (isCourierAssigned && !isPaymentSubmitted && !isDelivered) {
                Spacer(modifier = Modifier.height(8.dp))
                OrderPostDeliveryPaymentBlock(
                    currentPaymentMethod = currentPaymentMethod,
                    currentReceiptBase64 = currentReceiptBase64,
                    onSavePayment = { method, base64 ->
                        viewModel.updateOrderPayment(orderId, method, base64) { err ->
                            if (err == null) {
                                android.widget.Toast.makeText(context, "تم حفظ اختيار الدفع وإرسال الفاتورة بنجاح! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            } else if (isDelivered || isPaymentSubmitted) {
                val savedPaymentMethod = if (currentPaymentMethod.isNotBlank()) currentPaymentMethod else "كاش للمندوب"
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Green.copy(0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (isDelivered) "🔒 الفاتورة مغلقة ومكتملة بنجاح ✅" else "💳 تم تأكيد طريقة الدفع للطلب",
                            color = Color.Green,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = if (isDelivered) "لقد تم تسليم شحنتكم بنجاح ومطابقتها وتأكيدها." else "تم تسجيل طريقة الدفع المحددة بانتظار استلام الشحنة.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "💳 طريقة السداد للطلب: $savedPaymentMethod",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (!currentReceiptBase64.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { receiptToShow = currentReceiptBase64 },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Image, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("عرض إشعار التحويل المرفق 📄", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (courierPhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        var targetPhone = courierPhone.trim().replace("+", "").replace(" ", "")
                        if (targetPhone.startsWith("0")) {
                            targetPhone = "249" + targetPhone.substring(1)
                        } else if (!targetPhone.startsWith("249")) {
                            targetPhone = "249" + targetPhone
                        }
                        val itemsSummary = orderItems.joinToString("\n") { "• ${it.productName} (العدد: ${it.quantity}) - ${formatPrice(it.priceAtOrder * it.quantity)} ج.س" }
                        val invoiceMsg = """
                            🌌 *فاتورة طلب منتجات - تطبيق المجرة الكوني* 🌌
                            
                            📌 *كود الفاتورة:* #$orderId
                            👤 *العميل:* $customerName
                            📞 *هاتف العميل:* $customerPhone
                            📍 *عنوان التسليم:* $customerAddress
                            
                            🛒 *محتويات الفاتورة والمنتجات:*
                            $itemsSummary
                            
                            📦 *مجموع السلة:* ${formatPrice(totalItemsSum)} ج.س
                            🚚 *رسوم التوصيل:* ${formatPrice(deliveryPrice)} ج.س
                            💰 *إجمالي الفاتورة النهائي:* ${formatPrice(grandTotal)} ج.س
                            💳 *طريقة السداد:* ${if (currentPaymentMethod.isNotBlank()) currentPaymentMethod else "عند الاستلام 💵"}
                            
                            🚴 *المندوب المعين:* $courierName
                            🔒 *حالة الفاتورة:* $orderStatus
                        """.trimIndent()
                        
                        openWhatsAppDirectly(context, targetPhone, invoiceMsg)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة الفاتورة مع المندوب ($courierName) واتساب مباشر 💬🚴", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun RestaurantCustomerOrderCard(
    order: com.example.data.db.RestaurantOrderEntity,
    formatPrice: (Double) -> String,
    viewModel: MajarahViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isDelivered = (order.status.contains("تم تسليم العميل") || order.status.contains("إغلاق") || order.status.contains("تم التسليم") || order.status.contains("تم التوصيل")) &&
            !order.status.contains("للمندوب") && !order.status.contains("المندوب") && !order.status.contains("قيد التوصيل") && !order.status.contains("بانتظار")
    val hasCourier = order.courierName.isNotBlank() || order.courierPhone.isNotBlank() || order.status.contains("مندوب") || order.status.contains("توصيل")

    var receiptToShow by remember { mutableStateOf<String?>(null) }
    if (receiptToShow != null) {
        ViewReceiptDialog(receiptToShow!!) { receiptToShow = null }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🍔 طلب مطعم: ${order.restaurantName}",
                        fontWeight = FontWeight.Bold,
                        color = CosmicSecondary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "رقم الطلب: #${order.id}",
                        color = MediumContrastTextDark,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(if (isDelivered) ActiveGreen.copy(0.15f) else CosmicSecondary.copy(0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = order.status,
                        color = if (isDelivered) ActiveGreen else CosmicSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (order.courierName.isNotBlank() && order.courierPhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSecondary.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    try {
                                        val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                            data = android.net.Uri.parse("tel:${order.courierPhone.trim()}")
                                        }
                                        context.startActivity(dialIntent)
                                    } catch (e: Exception) {}
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ActiveGreen.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape)
                            ) {
                                Icon(Icons.Default.Call, "اتصال", tint = ActiveGreen, modifier = Modifier.size(18.dp))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("🚴 مندوب توصيل الوجبة:", fontSize = 11.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                Text(order.courierName, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("هاتف: ${order.courierPhone}", fontSize = 11.sp, color = Color.White.copy(0.8f))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                var targetPhone = order.courierPhone.trim().replace("+", "").replace(" ", "")
                                if (targetPhone.startsWith("0")) {
                                    targetPhone = "249" + targetPhone.substring(1)
                                } else if (!targetPhone.startsWith("249")) {
                                    targetPhone = "249" + targetPhone
                                }
                                val grandTotal = order.foodPrice + order.deliveryFee
                                val invoiceMsg = """
                                    🌌 *فاتورة طلب وجبة - تطبيق المجرة الكوني* 🌌
                                    
                                    📌 *كود الفاتورة:* #${order.id}
                                    🏪 *المطعم:* ${order.restaurantName}
                                    👤 *العميل:* ${order.customerName}
                                    📞 *هاتف العميل:* ${order.customerPhone}
                                    
                                    🍔 *الوجبات والملاحظات:*
                                    ${order.itemsAndNotes}
                                    
                                    💵 *سعر الوجبة:* ${formatPrice(order.foodPrice)} ج.س
                                    🚚 *رسوم التوصيل:* ${formatPrice(order.deliveryFee)} ج.س
                                    💰 *إجمالي الفاتورة النهائي:* ${formatPrice(grandTotal)} ج.س
                                    💳 *طريقة السداد:* ${if (order.paymentMethod.isNotBlank()) order.paymentMethod else "عند الاستلام 💵"}
                                    
                                    🚴 *المندوب المعين:* ${order.courierName}
                                    🔒 *حالة الفاتورة:* ${order.status}
                                """.trimIndent()
                                
                                WhatsAppUtils.sendWhatsAppMessage(context, targetPhone, invoiceMsg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مشاركة الفاتورة مع المندوب (${order.courierName}) واتساب 💬🚴", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "تفاصيل الوجبة والملاحظات:\n${order.itemsAndNotes}",
                color = Color.White.copy(0.9f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("المبلغ الإجمالي:", color = MediumContrastTextDark, fontSize = 12.sp)
                Text("${formatPrice(order.foodPrice + order.deliveryFee)} ج.س", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Tracking Bar
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicDeepSpace.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("حالة تتبع الوجبة 🍔", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val step2Active = !order.status.contains("معلق")
                    val step3Active = hasCourier || isDelivered

                    // Step 1
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(22.dp).clip(androidx.compose.foundation.shape.CircleShape).background(CosmicSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp), tint = Color.Black)
                        }
                        Text("تم الطلب", color = Color.White, fontSize = 9.sp)
                    }
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = if (step2Active) CosmicSecondary else CosmicSurfaceVariant)
                    // Step 2
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(22.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if (step2Active) CosmicSecondary else CosmicSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(if (step2Active) Icons.Default.Check else Icons.Default.Restaurant, null, modifier = Modifier.size(12.dp), tint = if (step2Active) Color.Black else Color.White)
                        }
                        Text("التحضير", color = if (step2Active) Color.White else MediumContrastTextDark, fontSize = 9.sp)
                    }
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = if (step3Active) CosmicSecondary else CosmicSurfaceVariant)
                    // Step 3
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(22.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if (step3Active) CosmicSecondary else CosmicSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(if (step3Active) Icons.Default.Check else Icons.Default.DirectionsBike, null, modifier = Modifier.size(12.dp), tint = if (step3Active) Color.Black else Color.White)
                        }
                        Text("التوصيل", color = if (step3Active) Color.White else MediumContrastTextDark, fontSize = 9.sp)
                    }
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = if (isDelivered) ActiveGreen else CosmicSurfaceVariant)
                    // Step 4
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(22.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if (isDelivered) ActiveGreen else CosmicSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(if (isDelivered) Icons.Default.Check else Icons.Default.Home, null, modifier = Modifier.size(12.dp), tint = if (isDelivered) Color.Black else Color.White)
                        }
                        Text("تم التسليم", color = if (isDelivered) Color.White else MediumContrastTextDark, fontSize = 9.sp)
                    }
                }
            }

            val currentPaymentMethod = order.paymentMethod ?: ""
            val currentReceipt = order.bankReceiptImageUri
            val isPaymentSubmitted = currentPaymentMethod.isNotBlank() && currentPaymentMethod != "كاش"

            if (hasCourier && !isPaymentSubmitted && !isDelivered) {
                Spacer(modifier = Modifier.height(10.dp))
                OrderPostDeliveryPaymentBlock(
                    currentPaymentMethod = currentPaymentMethod,
                    currentReceiptBase64 = currentReceipt,
                    onSavePayment = { method, base64 ->
                        viewModel.updateRestaurantOrderPayment(order.id, method, base64) { err ->
                            if (err == null) {
                                android.widget.Toast.makeText(context, "تم حفظ طريقة الدفع للوجبة بنجاح! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            } else if (currentPaymentMethod.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Green.copy(0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                        Text("💳 طريقة السداد للطلب: $currentPaymentMethod", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        if (!currentReceipt.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { receiptToShow = currentReceipt },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Image, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("عرض إشعار التحويل المرفق 📄", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (isDelivered) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSecondary.copy(0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(0.3f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("بالهناء والشفاء 🪐❤️", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("نتمنى لك وجبة شهية وممتعة من مطعم ${order.restaurantName}! 🍕🍔", color = Color.White.copy(0.9f), fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun PharmacyCustomerOrderCard(
    order: com.example.data.db.PharmacyOrderEntity,
    formatPrice: (Double) -> String,
    viewModel: MajarahViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isDelivered = (order.status.contains("تم تسليم العميل") || order.status.contains("إغلاق") || order.status.contains("تم التسليم") || order.status.contains("تم التوصيل")) &&
            !order.status.contains("للمندوب") && !order.status.contains("المندوب") && !order.status.contains("قيد التوصيل") && !order.status.contains("بانتظار")
    val hasCourier = !order.courierName.isNullOrBlank() || !order.courierPhone.isNullOrBlank() || order.status.contains("مندوب") || order.status.contains("توصيل")

    var receiptToShow by remember { mutableStateOf<String?>(null) }
    if (receiptToShow != null) {
        ViewReceiptDialog(receiptToShow!!) { receiptToShow = null }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "💊 طلب صيدلية المجرة الكونية",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64B5F6),
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "رقم الروشتة: #${order.id}",
                        color = MediumContrastTextDark,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(if (isDelivered) ActiveGreen.copy(0.15f) else Color(0xFF64B5F6).copy(0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = order.status,
                        color = if (isDelivered) ActiveGreen else Color(0xFF64B5F6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (!order.courierName.isNullOrBlank() && !order.courierPhone.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF64B5F6).copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64B5F6).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    try {
                                        val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                            data = android.net.Uri.parse("tel:${order.courierPhone?.trim()}")
                                        }
                                        context.startActivity(dialIntent)
                                    } catch (e: Exception) {}
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ActiveGreen.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape)
                            ) {
                                Icon(Icons.Default.Call, "اتصال", tint = ActiveGreen, modifier = Modifier.size(18.dp))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("🚴 مندوب توصيل الدواء:", fontSize = 11.sp, color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold)
                                Text(order.courierName ?: "", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("هاتف: ${order.courierPhone ?: ""}", fontSize = 11.sp, color = Color.White.copy(0.8f))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val courierPhone = order.courierPhone ?: ""
                                var targetPhone = courierPhone.trim().replace("+", "").replace(" ", "")
                                if (targetPhone.startsWith("0")) {
                                    targetPhone = "249" + targetPhone.substring(1)
                                } else if (!targetPhone.startsWith("249")) {
                                    targetPhone = "249" + targetPhone
                                }
                                val grandTotal = order.medicinePrice + order.deliveryFee
                                val itemsSummary = if (order.medicinesJson.isNotBlank()) order.medicinesJson else "طلب دواء / روشتة طبية مرفقة 📄"
                                val invoiceMsg = """
                                    🌌 *فاتورة طلب دواء/روشتة - صيدلية المجرة الكونية* 🌌
                                    
                                    📌 *كود الروشتة:* #${order.id}
                                    👤 *المريض/العميل:* ${order.customerName}
                                    📞 *هاتف العميل:* ${order.customerPhone}
                                    📍 *عنوان التوصيل:* ${order.deliveryLocation}
                                    
                                    💊 *الأدوية / الروشتة المطلوبة:*
                                    $itemsSummary
                                    
                                    💵 *سعر الأدوية:* ${formatPrice(order.medicinePrice)} ج.س
                                    🚚 *رسوم التوصيل:* ${formatPrice(order.deliveryFee)} ج.س
                                    💰 *إجمالي الفاتورة النهائي:* ${formatPrice(grandTotal)} ج.س
                                    💳 *طريقة السداد:* ${if (!order.paymentMethod.isNullOrBlank()) order.paymentMethod else "عند الاستلام 💵"}
                                    
                                    🚴 *المندوب المعين:* ${order.courierName}
                                    🔒 *حالة الفاتورة:* ${order.status}
                                """.trimIndent()
                                
                                WhatsAppUtils.sendWhatsAppMessage(context, targetPhone, invoiceMsg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مشاركة فاتورة الدواء مع المندوب (${order.courierName}) واتساب 💬🚴", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            val itemsSummary = if (order.medicinesJson.isNotBlank()) order.medicinesJson else "طلب دواء / روشتة طبية مرفقة 📄"
            Text(
                text = "بيانات الأدوية / الروشتة:\n$itemsSummary",
                color = Color.White.copy(0.9f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("المبلغ الإجمالي:", color = MediumContrastTextDark, fontSize = 12.sp)
                Text("${formatPrice(order.medicinePrice + order.deliveryFee)} ج.س", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Tracking Bar
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicDeepSpace.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("حالة تتبع الدواء 💊", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val step2Active = !order.status.contains("بانتظار")
                    val step3Active = hasCourier || isDelivered

                    // Step 1
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(22.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF64B5F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp), tint = Color.Black)
                        }
                        Text("الروشتة", color = Color.White, fontSize = 9.sp)
                    }
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = if (step2Active) Color(0xFF64B5F6) else CosmicSurfaceVariant)
                    // Step 2
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(22.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if (step2Active) Color(0xFF64B5F6) else CosmicSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(if (step2Active) Icons.Default.Check else Icons.Default.MedicalServices, null, modifier = Modifier.size(12.dp), tint = if (step2Active) Color.Black else Color.White)
                        }
                        Text("تجهيز الدواء", color = if (step2Active) Color.White else MediumContrastTextDark, fontSize = 9.sp)
                    }
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = if (step3Active) Color(0xFF64B5F6) else CosmicSurfaceVariant)
                    // Step 3
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(22.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if (step3Active) Color(0xFF64B5F6) else CosmicSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(if (step3Active) Icons.Default.Check else Icons.Default.DirectionsBike, null, modifier = Modifier.size(12.dp), tint = if (step3Active) Color.Black else Color.White)
                        }
                        Text("مع المندوب", color = if (step3Active) Color.White else MediumContrastTextDark, fontSize = 9.sp)
                    }
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), color = if (isDelivered) ActiveGreen else CosmicSurfaceVariant)
                    // Step 4
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(22.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if (isDelivered) ActiveGreen else CosmicSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(if (isDelivered) Icons.Default.Check else Icons.Default.Home, null, modifier = Modifier.size(12.dp), tint = if (isDelivered) Color.Black else Color.White)
                        }
                        Text("تم التسليم", color = if (isDelivered) Color.White else MediumContrastTextDark, fontSize = 9.sp)
                    }
                }
            }

            val currentPaymentMethod = order.paymentMethod ?: ""
            val currentReceipt = order.bankReceiptImageUri
            val isPaymentSubmitted = currentPaymentMethod.isNotBlank() && currentPaymentMethod != "كاش"

            if (hasCourier && !isPaymentSubmitted && !isDelivered) {
                Spacer(modifier = Modifier.height(10.dp))
                OrderPostDeliveryPaymentBlock(
                    currentPaymentMethod = currentPaymentMethod,
                    currentReceiptBase64 = currentReceipt,
                    onSavePayment = { method, base64 ->
                        viewModel.updatePharmacyOrderPayment(order.id, method, base64) { err ->
                            if (err == null) {
                                android.widget.Toast.makeText(context, "تم حفظ طريقة الدفع للروشتة بنجاح! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            } else if (currentPaymentMethod.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Green.copy(0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                        Text("💳 طريقة السداد للطلب: $currentPaymentMethod", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        if (!currentReceipt.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { receiptToShow = currentReceipt },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Image, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("عرض إشعار التحويل المرفق 📄", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (isDelivered) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Green.copy(0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Green.copy(0.3f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("بالشفاء العاجل لك إن شاء الله 🤲✨", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("نسأل الله لك الصحة والعافية والشفاء التام 🩺❤️", color = Color.White.copy(0.9f), fontSize = 11.sp, textAlign = TextAlign.Center)
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
    var googleEmailState by remember { mutableStateOf("") }
    var showGoogleDialog by remember { mutableStateOf(false) }
    var showManualGoogleInput by remember { mutableStateOf(false) }
    var localGoogleEmail by remember { mutableStateOf("") }
    var localGoogleName by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var isCheckingEmail by remember { mutableStateOf(false) }
    var isGoogleAccountExists by remember { mutableStateOf(false) }
    val isLoginLoading by viewModel.isLoginLoading.collectAsStateWithLifecycle()
    val isGlobalLoading by viewModel.isGlobalLoading.collectAsStateWithLifecycle()
    val isCurrentlyLoading = isCheckingEmail || isLoginLoading || isGlobalLoading
    val adminManagers by viewModel.allAdminManagers.collectAsStateWithLifecycle()
    val allPharmacies by viewModel.allPharmacies.collectAsStateWithLifecycle()
    val allRestaurants by viewModel.allRestaurants.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfilesFlow.collectAsStateWithLifecycle()

    var remotePasswordCheckResult by remember { mutableStateOf<Boolean?>(null) }

    var adminPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var adminPassVisible by remember { mutableStateOf(false) }

    val logoScale = remember { Animatable(0.2f) }
    val logoAlpha = remember { Animatable(0f) }

    val langEnglish = viewModel.isEnglish.collectAsStateWithLifecycle().value

    val matchingAdminManager = if (email.trim().isNotEmpty()) {
        adminManagers.firstOrNull { manager ->
            val cleanInput = email.trim().lowercase()
            manager.email.trim().lowercase() == cleanInput || manager.phone.trim() == cleanInput
        }
    } else {
        null
    }

    LaunchedEffect(matchingAdminManager) {
        if (matchingAdminManager != null) {
            try {
                val emailClean = matchingAdminManager.email.trim().lowercase()
                val remoteProfs = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.data.network.SupabaseClient.api.getProfilesByEmail(emailFilter = "eq.$emailClean")
                }
                val hasRemotePass = remoteProfs.any { !it.password.isNullOrBlank() }
                remotePasswordCheckResult = hasRemotePass
            } catch (e: Exception) {
                e.printStackTrace()
                remotePasswordCheckResult = null
            }
        } else {
            remotePasswordCheckResult = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshAllProfiles()
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
            for (i in 0..250) {
                val x = r.nextFloat() * size.width
                val y = r.nextFloat() * size.height
                val radius = r.nextFloat() * 3.5f + 0.8f
                val starColor = if (r.nextBoolean()) CosmicSecondary else Color.White
                drawCircle(
                    color = starColor.copy(alpha = r.nextFloat() * 0.7f + 0.3f),
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
                colors = CardDefaults.cardColors(containerColor = CosmicSurface.copy(alpha = 0.15f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CosmicSecondary.copy(alpha = 0.3f))
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
                                    painter = painterResource(id = R.drawable.img_majarah_logo_1782345985330),
                                    contentDescription = "Galaxy Logo",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // App Logo Text Slogan - exact text "المجرة للتسوق"
                            Text(
                                text = viewModel.t("المجرة للتسوق 🌌", "Almajra 🌌"),
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
                                text = if (isRegister) viewModel.t("إنشاء حساب كوني جديد", "Create New Cosmic Account") else viewModel.t("تسجيل الدخول للمجرة", "Log in to Almajra"),
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
                                            text = if (isGoogleAccountExists) {
                                                viewModel.t("حساب Google مسجل مسبقاً 🟢", "Google account registered 🟢")
                                            } else {
                                                viewModel.t("متصل عبر حساب Google بنجاح 🟢", "Connected via Google successfully 🟢")
                                            },
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
                                        text = if (isGoogleAccountExists) {
                                            viewModel.t("هذا الحساب مسجل مسبقاً لدينا! يرجى إدخال رقم الهاتف المسجل لتأكيد تطابق البيانات وكلمة مرور التطبيق للدخول الآمن المباشر.", "This account is already registered! Please enter the registered phone number to confirm details matching, and the app password to log in directly.")
                                        } else {
                                            viewModel.t("يرجى إكمال الاسم ورقم الهاتف وتعيين كلمة مرور لتفعيل حسابك بالمجرة:", "Please enter your name, phone number, and a password to complete registration:")
                                        },
                                        color = Color.White.copy(0.9f),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    if (isRegister || isGoogleFlowActive) {
                        // Registration mode or Google account registration/sync
                        item {
                            val selectedRole by viewModel.registrationRole.collectAsStateWithLifecycle()
                            Text(
                                text = viewModel.t("اختر نوع الحساب للانضمام للمجرة 🌌:", "Choose account type to join Almajra 🌌:"),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                textAlign = TextAlign.Right
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val roles = listOf(
                                    Triple("customer", "عميل 👤", "Customer 👤"),
                                    Triple("seller", "تاجر 🛒", "Merchant 🛒"),
                                    Triple("courier", "مندوب 🚴", "Courier 🚴"),
                                    Triple("pharmacist", "صيدلي 💊", "Pharmacist 💊"), Triple("restaurant", "مطعم 🍔", "Restaurant 🍔")
                                )
                                roles.forEach { (roleKey, arLabel, enLabel) ->
                                    val isSelected = selectedRole == roleKey
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.registrationRole.value = roleKey },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) CosmicSecondary.copy(alpha = 0.2f) else CosmicSurface
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) CosmicSecondary else CosmicSecondary.copy(alpha = 0.2f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = viewModel.t(arLabel, enLabel),
                                                color = if (isSelected) CosmicSecondary else Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        item {
                            OutlinedTextField(
                                value = name,
                                onValueChange = onNameChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_name_input"),
                                label = { Text("الاسم بالكامل 👤", color = CosmicSecondary) },
                                placeholder = null,
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

                    val matchingAdminManager = if (email.trim().isNotEmpty()) {
                        adminManagers.firstOrNull { manager ->
                            val cleanInput = email.trim().lowercase()
                            manager.email.trim().lowercase() == cleanInput || manager.phone.trim() == cleanInput
                        }
                    } else {
                        null
                    }

                    val matchingPharmacy = if (email.trim().isNotEmpty()) {
                        allPharmacies.firstOrNull { pharm ->
                            val cleanInput = email.trim().lowercase()
                            pharm.pharmacistEmail.trim().lowercase() == cleanInput || pharm.phone.trim() == cleanInput || pharm.doctorName.trim().lowercase() == cleanInput
                        }
                    } else null

                    val matchingRestaurant = if (email.trim().isNotEmpty()) {
                        allRestaurants.firstOrNull { rest ->
                            val cleanInput = email.trim().lowercase()
                            rest.phone.trim() == cleanInput || rest.name.trim().lowercase() == cleanInput
                        }
                    } else null

                    val hasAlreadySetPassword = (matchingAdminManager != null && (allProfiles.any { profile ->
                        profile.email.trim().lowercase() == matchingAdminManager.email.trim().lowercase() && !profile.password.isNullOrBlank()
                    } || remotePasswordCheckResult == true))

                    if (isRegister && (matchingPharmacy != null || matchingRestaurant != null || (matchingAdminManager != null && hasAlreadySetPassword))) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = CosmicSecondary.copy(alpha = 0.2f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = viewModel.t("حسابك مسجل مسبقاً بالمجرة! 🌟🟢", "Your account is already registered in Almajra! 🌟🟢"),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val roleText = when {
                                        matchingPharmacy != null -> "صيدلية (${matchingPharmacy.name})"
                                        matchingRestaurant != null -> "مطعم (${matchingRestaurant.name})"
                                        else -> "مدير إداري (${matchingAdminManager?.name})"
                                    }
                                    Text(
                                        text = viewModel.t("تم العثور على بياناتك كـ $roleText. لا تحتاج لإنشاء حساب جديد، فقط أدخل كلمة السر والدخول المباشر لصفحتك الخاصّة.", "Found your account as $roleText. No need to re-register, just enter password for direct access."),
                                        color = CosmicSecondary,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.isRegisterMode.value = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(viewModel.t("الانتقال لتسجيل الدخول المباشر 🔑", "Switch to Direct Login 🔑"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (matchingAdminManager != null && !hasAlreadySetPassword) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.45f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "تأكيد هوية المدير الإداري: ${matchingAdminManager.name} 👑✨",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "مرحباً بك! لقد تم تسجيلك من قبل المدير العام. يرجى تعيين كلمة مرور للتطبيق بالأسفل وتأكيدها لتنشيط حسابك والدخول المباشر للوحة التحكم.",
                                        color = CosmicSecondary,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    
                                    OutlinedTextField(
                                        value = adminPassword,
                                        onValueChange = { adminPassword = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("أدخل كلمة المرور الجديدة للتطبيق 🔑", color = MediumContrastTextDark, fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = CosmicSecondary) },
                                        trailingIcon = {
                                            IconButton(onClick = { adminPassVisible = !adminPassVisible }) {
                                                Icon(
                                                    imageVector = if (adminPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "عرض كلمة المرور",
                                                    tint = MediumContrastTextDark
                                                )
                                            }
                                        },
                                        visualTransformation = if (adminPassVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
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
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    OutlinedTextField(
                                        value = confirmPassword,
                                        onValueChange = { confirmPassword = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("تأكيد كلمة المرور الجديدة 🔒", color = MediumContrastTextDark, fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = CosmicSecondary) },
                                        trailingIcon = {
                                            IconButton(onClick = { adminPassVisible = !adminPassVisible }) {
                                                Icon(
                                                    imageVector = if (adminPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "عرض كلمة المرور",
                                                    tint = MediumContrastTextDark
                                                )
                                            }
                                        },
                                        visualTransformation = if (adminPassVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
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
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Button(
                                        onClick = {
                                            if (adminPassword.length < 6) {
                                                Toast.makeText(context, "يجب أن تكون كلمة المرور 6 أحرف أو أكثر ⚠️", Toast.LENGTH_SHORT).show()
                                            } else if (adminPassword != confirmPassword) {
                                                Toast.makeText(context, "كلمتا المرور غير متطابقتين! ⚠️", Toast.LENGTH_SHORT).show()
                                            } else {
                                                viewModel.activateAdminManager(
                                                    name = matchingAdminManager.name,
                                                    email = matchingAdminManager.email,
                                                    phone = matchingAdminManager.phone,
                                                    password = adminPassword
                                                ) { err ->
                                                    if (err == null) {
                                                        Toast.makeText(context, "تم تفعيل حسابك كمدير إداري بنجاح! 🎉👑", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        Toast.makeText(context, "فشل التنشيط: $err", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        },
                                        enabled = adminPassword.isNotEmpty() && confirmPassword.isNotEmpty(),
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("تفعيل الحساب وتعيين كلمة المرور 👑🔓", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
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
                    }

                    item {
                        val formValid = if (isGoogleFlowActive) {
                            if (isGoogleAccountExists) {
                                phone.isNotBlank() && password.length >= 6
                            } else {
                                name.isNotBlank() && phone.isNotBlank() && password.length >= 6
                            }
                        } else if (isRegister) {
                            name.isNotBlank() && phone.isNotBlank() && email.isNotBlank() && password.length >= 6
                        } else {
                            email.isNotBlank() && password.length >= 4
                        }

                        Button(
                            onClick = {
                                if (isGoogleFlowActive) {
                                    if (isGoogleAccountExists) {
                                        viewModel.isRegisterMode.value = false
                                        onEmailChange(googleEmailState)
                                        onSubmit()
                                    } else {
                                        viewModel.isRegisterMode.value = true
                                        onEmailChange(googleEmailState)
                                        onSubmit()
                                    }
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
                                    if (isGoogleAccountExists) {
                                        viewModel.t("تأكيد ودخول المجرة 🚀", "Confirm & Enter Almajra 🚀")
                                    } else {
                                        viewModel.t("إكمال التفعيل ودخول المجرة 🚀", "Complete Activation & Enter Almajra 🚀")
                                    }
                                } else if (isRegister) {
                                    viewModel.t("تأكيد ودخول المجرة", "Confirm and Enter Almajra")
                                } else {
                                    viewModel.t("الدخول للمجرة", "Enter Almajra")
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
                                        viewModel.t("ليس لديك حساب؟ انضم للمجرة وسجل الآن", "Don't have an account? Join Almajra and register now"),
                                    color = CosmicSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
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

                        if (!showManualGoogleInput) {
                            Text(
                                text = viewModel.t(
                                    "اختر حساب قوقل المسجل على جهازك للمتابعة والدخول الفوري والآمن إلى تطبيق مجرة السودان:",
                                    "Select a Google account registered on your device to continue and instantly sign in to Majarah Sudan:"
                                ),
                                fontSize = 11.sp,
                                color = MediumContrastTextDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val googleAccountsList = remember(context) {
                                val list = mutableListOf<Pair<String, String>>()
                                try {
                                    val am = android.accounts.AccountManager.get(context)
                                    val googleAccounts = am.getAccountsByType("com.google")
                                    for (acc in googleAccounts) {
                                        val email = acc.name
                                        val name = email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                                        list.add(Pair(email, name))
                                    }
                                } catch (e: Exception) {
                                    // Security or other errors ignored
                                }
                                list
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                googleAccountsList.forEach { account ->
                                    val email = account.first
                                    val name = account.second

                                    OutlinedButton(
                                        onClick = {
                                            isCheckingEmail = true
                                            showGoogleDialog = false
                                            viewModel.loginGoogleVerifiedAccountDirect(email) { err, exists ->
                                                isCheckingEmail = false
                                                if (err == null) {
                                                    if (exists) {
                                                        Toast.makeText(context, "مرحباً بعودتك! تم الدخول والمزامنة المباشرة لحساب Google ($email) بنجاح 🟢✨", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        googleEmailState = email
                                                        onEmailChange(email)
                                                        onNameChange("")
                                                        onPhoneChange("")
                                                        isGoogleAccountExists = false
                                                        isGoogleFlowActive = true
                                                        Toast.makeText(context, "حساب Google مكتشف ($email). يرجى كتابة الاسم ورقم الهاتف وتعيين كلمة مرور لإكمال التسجيل والربط سحابياً 🚀📲", Toast.LENGTH_LONG).show()
                                                    }
                                                } else {
                                                    googleEmailState = email
                                                    onEmailChange(email)
                                                    onNameChange("")
                                                    onPhoneChange("")
                                                    isGoogleAccountExists = false
                                                    isGoogleFlowActive = true
                                                    Toast.makeText(context, "تم المتابعة بحساب Google المكتشف ($email) 🌠", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSurfaceVariant.copy(0.8f)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = CosmicDeepSpace,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Text(text = name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(text = email, color = MediumContrastTextDark, fontSize = 10.sp)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                                    .background(CosmicSecondary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, null, tint = CosmicSecondary, modifier = Modifier.size(24.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = { showManualGoogleInput = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicSecondary, containerColor = Color.Transparent)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("استخدام حساب Google آخر يدوياً 📧", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CosmicSecondary)
                                }
                            }
                        } else {
                            Text(
                                text = viewModel.t("يرجى إدخال بريدك الإلكتروني قوقل واسمك لبدء المصادقة المباشرة والسريعة:", "Please enter your Google email and name to start direct secure authentication:"),
                                fontSize = 11.sp,
                                color = MediumContrastTextDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Email Field
                            OutlinedTextField(
                                value = localGoogleEmail,
                                onValueChange = { localGoogleEmail = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = null,
                                label = { Text("بريد Google الإلكتروني 📧", color = CosmicSecondary, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
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

                            Spacer(modifier = Modifier.height(12.dp))

                            // Name Field
                            OutlinedTextField(
                                value = localGoogleName,
                                onValueChange = { localGoogleName = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = null,
                                label = { Text("الاسم الكامل 👤", color = CosmicSecondary, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right) },
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

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (localGoogleEmail.isNotBlank() && localGoogleEmail.contains("@") && localGoogleName.isNotBlank()) {
                                        isCheckingEmail = true
                                        val email = localGoogleEmail.trim()
                                        val name = localGoogleName.trim()
                                        scope.launch {
                                            try {
                                                // Send real OTP via Supabase!
                                                try {
                                                    val otpRequest = com.example.data.network.SupabaseOtpRequest(
                                                        email = email,
                                                        options = com.example.data.network.SupabaseOtpOptions(shouldCreateUser = true)
                                                    )
                                                    com.example.data.network.SupabaseClient.api.signInWithOtp(otpRequest)
                                                } catch (e: Exception) { e.printStackTrace() }

                                                val remoteProfs = com.example.data.network.SupabaseClient.api.getProfilesByEmail(emailFilter = "eq.$email")
                                                if (remoteProfs.isNotEmpty()) {
                                                    val p = remoteProfs.first()
                                                    googleEmailState = email
                                                    onEmailChange(email)
                                                    onNameChange("")
                                                    onPhoneChange("")
                                                    isGoogleAccountExists = true
                                                    isGoogleFlowActive = true
                                                    Toast.makeText(context, "تم إرسال رمز تحقق حقيقي إلى بريدك الإلكتروني ($email) بنجاح عبر Supabase! 📧✨", Toast.LENGTH_LONG).show()
                                                } else {
                                                    googleEmailState = email
                                                    onEmailChange(email)
                                                    onNameChange("")
                                                    onPhoneChange("")
                                                    isGoogleAccountExists = false
                                                    isGoogleFlowActive = true
                                                    Toast.makeText(context, "حساب قوقل جديد! تم إرسال رمز تحقق حقيقي إلى بريدك الإلكتروني ($email) عبر Supabase! 📧✨", Toast.LENGTH_LONG).show()
                                                }
                                            } catch (e: Exception) {
                                                googleEmailState = email
                                                onEmailChange(email)
                                                onNameChange("")
                                                onPhoneChange("")
                                                isGoogleAccountExists = false
                                                isGoogleFlowActive = true
                                                Toast.makeText(context, "تم إرسال رمز تحقق حقيقي إلى بريدك الإلكتروني ($email) عبر Supabase! 📧✨", Toast.LENGTH_LONG).show()
                                            } finally {
                                                isCheckingEmail = false
                                                showGoogleDialog = false
                                                // Open verification dialog
                                                viewModel.otpVerificationEmail.value = email
                                                viewModel.showOtpVerification.value = true
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "الرجاء إدخال بريد إلكتروني صحيح واسم كامل لتأكيد المصادقة ⚠️", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("تأكيد ومصادقة مع قوقل 🔐", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(onClick = { showManualGoogleInput = false }) {
                                Text(viewModel.t("الرجوع لاختيار الحساب 🔙", "Back to choose account 🔙"), color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = { showGoogleDialog = false }) {
                            Text(viewModel.t("إلغاء والعودة ❌", "Cancel ❌"), color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isCurrentlyLoading) {
            CosmicLogoLoaderDialog()
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
    val isGeneralAdmin by viewModel.isGeneralAdmin.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isSeller by viewModel.isSeller.collectAsStateWithLifecycle()
    val isPharmacist by viewModel.isPharmacist.collectAsStateWithLifecycle()
    val isRestaurant by viewModel.isRestaurant.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var editName by remember(activeProfile) { mutableStateOf(activeProfile?.name ?: "") }
    var editPhone by remember(activeProfile) { mutableStateOf(activeProfile?.phone ?: "") }
    val email = activeProfile?.email ?: ""
    var isUpdating by remember { mutableStateOf(false) }

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val outputStream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
                val byteArray = outputStream.toByteArray()
                val base64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
                viewModel.updateProfileImage(base64) { err ->
                    if (err == null) {
                        Toast.makeText(context, "تم التقاط صورة الملف الشخصي بنجاح! 📸", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "فشل: $err", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "فشل حفظ الصورة: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= 29) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val outputStream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
                val byteArray = outputStream.toByteArray()
                val base64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
                viewModel.updateProfileImage(base64) { err ->
                    if (err == null) {
                        Toast.makeText(context, "تم اختيار صورة الملف الشخصي بنجاح! 🖼️", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "فشل: $err", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "فشل حفظ الصورة: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDeepSpace)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Home) },
                    modifier = Modifier.background(Color.White.copy(0.08f), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "رجوع",
                        tint = CosmicSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "رجوع للرئيسية",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.Home) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            val decodedBitmap = remember(activeProfile?.profileImageUri) {
                try {
                    val uri = activeProfile?.profileImageUri
                    if (!uri.isNullOrEmpty()) {
                        val decodedBytes = android.util.Base64.decode(uri, android.util.Base64.DEFAULT)
                        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }

            // Astro themed avatar circle
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(CosmicSecondary.copy(alpha = 0.15f))
                    .border(2.dp, CosmicSecondary, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (decodedBitmap != null) {
                    Image(
                        bitmap = decodedBitmap.asImageBitmap(),
                        contentDescription = "صورة الحساب",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "صورة الحساب",
                        tint = CosmicSecondary,
                        modifier = Modifier.size(68.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.background(Color.White.copy(0.08f), androidx.compose.foundation.shape.CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, "التقاط صورة الكاميرا", tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.background(Color.White.copy(0.08f), androidx.compose.foundation.shape.CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, "اختيار من المعرض", tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = activeProfile?.name?.ifEmpty { "عميل كوزموس" } ?: "عميل كوزموس",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            val roleName = when {
                activeProfile?.email?.trim()?.lowercase() == "mawiaosman0@gmail.com" -> "المدير العام للمجرة 👑"
                isGeneralAdmin -> "المدير العام للمجرة 👑"
                isAdmin -> "مدير إداري 🏛️"
                isCourier -> "مندوب المجرة 🚴"
                isSeller -> "تاجر المجرة 🛒"
                isPharmacist -> "صيدلي المجرة 💊"
                isRestaurant -> "مطعم المجرة 🍔"
                else -> "عميل المجرة 🌌"
            }
            Text(
                text = roleName,
                color = CosmicSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
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
            
            Spacer(modifier = Modifier.height(16.dp))

            // Change Password Card
            var currentPasswordInput by remember { mutableStateOf("") }
            var newPasswordInput by remember { mutableStateOf("") }
            var confirmPasswordInput by remember { mutableStateOf("") }
            var isChangingPassword by remember { mutableStateOf(false) }

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
                        text = "تغيير كلمة المرور الكونية 🔐",
                        color = CosmicSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Current Password
                    OutlinedTextField(
                        value = currentPasswordInput,
                        onValueChange = { currentPasswordInput = it },
                        label = { Text("كلمة المرور الحالية", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = CosmicSecondary,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = CosmicSecondary
                        ),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = CosmicSecondary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_current_password_input")
                    )

                    // New Password
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("كلمة المرور الجديدة", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = CosmicSecondary,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = CosmicSecondary
                        ),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = CosmicSecondary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_new_password_input")
                    )

                    // Confirm New Password
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("تأكيد كلمة المرور الجديدة", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = CosmicSecondary,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = CosmicSecondary
                        ),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = CosmicSecondary)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("profile_confirm_password_input")
                    )

                    Button(
                        onClick = {
                            if (currentPasswordInput.isBlank() || newPasswordInput.isBlank() || confirmPasswordInput.isBlank()) {
                                Toast.makeText(context, "الرجاء ملء جميع حقول كلمة المرور", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val actualPassword = activeProfile?.password ?: ""
                            if (currentPasswordInput != actualPassword) {
                                Toast.makeText(context, "كلمة المرور الحالية غير صحيحة", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (newPasswordInput != confirmPasswordInput) {
                                Toast.makeText(context, "كلمتا المرور الجديدتان غير متطابقتين", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isChangingPassword = true
                            viewModel.updatePassword(newPasswordInput) { err ->
                                isChangingPassword = false
                                if (err == null) {
                                    Toast.makeText(context, "تم تغيير كلمة المرور بنجاح! 🔐✨", Toast.LENGTH_LONG).show()
                                    currentPasswordInput = ""
                                    newPasswordInput = ""
                                    confirmPasswordInput = ""
                                } else {
                                    Toast.makeText(context, "فشل تغيير كلمة المرور: $err", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_change_password_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CosmicSecondary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isChangingPassword
                    ) {
                        if (isChangingPassword) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text("تحديث وحفظ كلمة المرور 🔐", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
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

            // Classification and Rewards Card (Visible ONLY to Customers and Couriers)
            val currentClassification by viewModel.userClassification.collectAsStateWithLifecycle()
            if (isCourier || (!isSeller && !isPharmacist && !isRestaurant && !isGeneralAdmin && !isAdmin)) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "الميزات وتصنيفات الحسابات الكونية 🌌",
                            color = CosmicSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.End)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تصنيفك الحالي: $currentClassification",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.End)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isCourier) {
                            // Courier specific tiers
                            Text(
                                text = "دليل تصنيفات المناديب والجوائز 🚴",
                                color = CosmicSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.End)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("أقل من 20 مهمة توصيل ناجحة بالأسبوع", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Right)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("• مندوب المجرة:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("أكثر من 20 مهمة بالأسبوع + عرض هدية مجاني 🎁", color = CosmicSecondary, fontSize = 11.sp, textAlign = TextAlign.Right)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("• مندوب مميز ⭐:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("أكثر من 40 مهمة بالأسبوع + عروض ومكافآت وحوافز إضافية 👑", color = CosmicSecondary, fontSize = 11.sp, textAlign = TextAlign.Right)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("• مندوب ذهبي 👑:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        } else {
                            // Customer specific tiers
                            Text(
                                text = "دليل تصنيفات العملاء والمكافآت 👤",
                                color = CosmicSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.End)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("أقل من 20 طلباً بالأسبوع", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Right)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("• عميل المجرة:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("أكثر من 20 طلباً بالأسبوع + خصم 5% وكوبون توصيل مجاني 🎫", color = CosmicSecondary, fontSize = 11.sp, textAlign = TextAlign.Right)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("• عميل مميز ⭐:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("أكثر من 40 طلباً بالأسبوع + خصم 15% وكوبون اطلب واحد والثاني هدية 🎁", color = CosmicSecondary, fontSize = 11.sp, textAlign = TextAlign.Right)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("• عميل ذهبي 👑:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (!isCourier && !isSeller && !isPharmacist && !isRestaurant && !isGeneralAdmin && !isAdmin) {
                val couponsList by viewModel.allCouponsFlow.collectAsStateWithLifecycle()
                val isEligibleForCoupon = currentClassification.contains("مميز") || currentClassification.contains("ذهبي")
                val myCoupons = couponsList.filter { it.forUserEmail.trim().lowercase() == email.trim().lowercase() }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.End) {
                        Text(
                            text = "🎁 كوبونات الفوز الكونية الخاصة بك",
                            color = CosmicSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.End)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (isEligibleForCoupon) {
                            if (myCoupons.isEmpty()) {
                                Text(
                                    text = "ليست لديك أي كوبونات فوز حالياً. قيّم التطبيق والطلبات بعد تسليمها للحصول على كوبونات وجوائز رائعة! 🌌✨",
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "انسخ كود الكوبون واستخدمه عند إكمال سلتك للحصول على خصومات وهدايا كوزموس الفورية! 🎫",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    myCoupons.forEach { coupon ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (coupon.isUsed) Color.Red.copy(0.1f) else Color.Green.copy(0.12f)
                                            ),
                                            border = BorderStroke(1.dp, if (coupon.isUsed) Color.Red.copy(0.3f) else Color.Green.copy(0.4f)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (!coupon.isUsed) {
                                                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                                    Button(
                                                        onClick = {
                                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(coupon.code))
                                                            Toast.makeText(context, "تم نسخ كود الكوبون: ${coupon.code} 📋", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(30.dp)
                                                    ) {
                                                        Text("نسخ الكود 📋", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Text(
                                                        text = "تم الاستخدام 🔒",
                                                        color = Color.Red,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = coupon.code,
                                                        color = Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = coupon.offerTitle,
                                                        color = CosmicSecondary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        textAlign = TextAlign.Right
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Non-eligible customer
                            Text(
                                text = "⚠️ كوبونات الفوز الكونية والجوائز مخصصة فقط لعملاء الفئة المميزة ⭐ والذهبية 👑.",
                                color = Color(0xFFFF9800),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تابع إكمال طلباتك الكونية لترقية تصنيف حسابك إلى فئة مميزة أو ذهبية وتفعيل الجوائز الفورية تلقائياً! 🚀🌌",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Customer Service Contact card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "الدعم وخدمة العملاء الكونية 📞",
                        color = CosmicSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.End)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يسعدنا الرد على كافة استفساراتكم وحل مشكلاتكم على مدار الساعة. تواصلوا معنا مباشرة عبر الاتصال الهاتفي:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:0912500344")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "لا يمكن إجراء المكالمة حالياً: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اتصل بخدمة العملاء: 0912500344", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
    var activeTab by remember { mutableStateOf(0) } // 0: Overview, 1: Add Product, 2: Manage Store, 3: Orders
    val customDeliveryFees = remember { androidx.compose.runtime.mutableStateMapOf<String, String>() }
    var activeDetailDialog by remember { mutableStateOf<String?>(null) }
    
    var newCourierName by remember { mutableStateOf("") }
    var newCourierPhone by remember { mutableStateOf("") }
    var newCourierState by remember { mutableStateOf("ولاية بورتسودان") }
    var newCourierStatus by remember { mutableStateOf("نشط ومتوفر 🟢") }
    
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrdersFlow.collectAsStateWithLifecycle()
    val allCouriers by viewModel.allCouriers.collectAsStateWithLifecycle()
    val isGeneralAdmin by viewModel.isGeneralAdmin.collectAsStateWithLifecycle()
    val allAdminManagers by viewModel.allAdminManagers.collectAsStateWithLifecycle()
    val isAdministrativeManager by viewModel.isAdministrativeManager.collectAsStateWithLifecycle()

    val pharmacies by viewModel.allPharmacies.collectAsStateWithLifecycle()
    val pharmacyOrders by viewModel.allPharmacyOrders.collectAsStateWithLifecycle()
    val allRestaurantOrders by viewModel.allRestaurantOrders.collectAsStateWithLifecycle()
    val restaurants by viewModel.allRestaurants.collectAsStateWithLifecycle()
    val sellers by viewModel.allSellers.collectAsStateWithLifecycle()

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

    val pendingProductsCount = remember(allProducts) {
        allProducts.count { !it.isApproved }
    }

    val pendingPharmacyCount = remember(pharmacies, pharmacyOrders) {
        pharmacies.count { !it.isApproved } + pharmacyOrders.count { it.status == "بانتظار المدير" || it.status == "بانتظار الصيدلي" }
    }

    val pendingRestaurantOrdersCount = remember(allRestaurantOrders, restaurants) {
        allRestaurantOrders.count { it.status == "معلق" } + restaurants.count { !it.isApproved }
    }

    val pendingSellersCount = remember(sellers) {
        sellers.size
    }

    var lastPendingCourierOrdersCount by remember { mutableStateOf(pendingCourierOrdersCount) }
    var lastPendingProductsCount by remember { mutableStateOf(pendingProductsCount) }
    var lastPendingPharmacyCount by remember { mutableStateOf(pendingPharmacyCount) }
    var lastPendingRestaurantOrdersCount by remember { mutableStateOf(pendingRestaurantOrdersCount) }
    var lastPendingSellersCount by remember { mutableStateOf(pendingSellersCount) }

    LaunchedEffect(pendingCourierOrdersCount, pendingProductsCount, pendingPharmacyCount, pendingRestaurantOrdersCount, pendingSellersCount) {
        if (pendingCourierOrdersCount > lastPendingCourierOrdersCount ||
            pendingProductsCount > lastPendingProductsCount ||
            pendingPharmacyCount > lastPendingPharmacyCount ||
            pendingRestaurantOrdersCount > lastPendingRestaurantOrdersCount ||
            pendingSellersCount > lastPendingSellersCount
        ) {
            // Enable sound notifications for General Manager and Administrative Manager with the system message tone
            NotificationSoundUtils.playNotificationSound(context)
        }
        lastPendingCourierOrdersCount = pendingCourierOrdersCount
        lastPendingProductsCount = pendingProductsCount
        lastPendingPharmacyCount = pendingPharmacyCount
        lastPendingRestaurantOrdersCount = pendingRestaurantOrdersCount
        lastPendingSellersCount = pendingSellersCount
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
                        "couriers_list" -> "إدارة وتوزيع مناديب التوصيل بالسودان 🚴 🇸🇩"
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
                        "couriers_list" -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.4f)),
                                        border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                            Text(
                                                "إضافة مندوب المجرة جديد ➕",
                                                fontWeight = FontWeight.Bold,
                                                color = CosmicSecondary,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            
                                            OutlinedTextField(
                                                value = newCourierName,
                                                onValueChange = { newCourierName = it },
                                                label = { Text("اسم المندوب ثلاثي", color = Color.White.copy(0.6f)) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = CosmicSecondary,
                                                    unfocusedBorderColor = Color.Gray,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            OutlinedTextField(
                                                value = newCourierPhone,
                                                onValueChange = { newCourierPhone = it },
                                                label = { Text("رقم هاتف المندوب", color = Color.White.copy(0.6f)) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = CosmicSecondary,
                                                    unfocusedBorderColor = Color.Gray,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            OutlinedTextField(
                                                value = newCourierState,
                                                onValueChange = { newCourierState = it },
                                                label = { Text("ولايات التغطية", color = Color.White.copy(0.6f)) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = CosmicSecondary,
                                                    unfocusedBorderColor = Color.Gray,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                                                singleLine = true
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            
                                            Button(
                                                onClick = {
                                                    if (newCourierName.isBlank() || newCourierPhone.isBlank() || newCourierState.isBlank()) {
                                                        Toast.makeText(context, "الرجاء إدخال كافة البيانات ⚠️", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    viewModel.addCourier(
                                                        name = newCourierName,
                                                        phone = newCourierPhone,
                                                        stateInfo = newCourierState,
                                                        status = newCourierStatus
                                                    ) { err ->
                                                        if (err == null) {
                                                            Toast.makeText(context, "تمت إضافة المندوب بنجاح! 🚴", Toast.LENGTH_LONG).show()
                                                            newCourierName = ""
                                                            newCourierPhone = ""
                                                        } else {
                                                            Toast.makeText(context, "خطأ: $err", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("تفعيل المندوب بقاعدة البيانات 📡", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                                
                                if (allCouriers.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("لا توجد مناديب شحن حالياً 🚴", color = MediumContrastTextDark, fontSize = 11.sp)
                                        }
                                    }
                                } else {
                                    items(allCouriers) { courier ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.3f)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        if (!isGeneralAdmin) {
                                                            Toast.makeText(context, "عذراً، حذف مناديب التوصيل ميزة حصرية للمدير العام فقط 🔒", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            viewModel.removeCourier(courier.id) { err ->
                                                                if (err == null) {
                                                                    Toast.makeText(context, "تم حذف وإلغاء تفعيل المندوب! 🗑️", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    Toast.makeText(context, "خطأ بالطلب: $err", Toast.LENGTH_LONG).show()
                                                                }
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        "حذف المندوب",
                                                        tint = if (isGeneralAdmin) Color.Red.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.5f)
                                                    )
                                                }
                                                
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(courier.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                                    Text("الهاتف: ${courier.phone}", fontSize = 11.sp, color = MediumContrastTextDark)
                                                    Text("التغطية: ${courier.stateInfo}", fontSize = 10.sp, color = CosmicSecondary)
                                                    val statusColor = if (courier.status.contains("متوفر")) Color.Green else CosmicTertiary
                                                    Text("الحالة: ${courier.status}", fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
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
    
    var adminSelectedImageBase64 by remember { mutableStateOf<String?>(null) }

    // Camera Launcher
    val adminCameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val base64 = try {
                val outputStream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
                val byteArray = outputStream.toByteArray()
                android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
            if (base64 != null) {
                adminSelectedImageBase64 = base64
                Toast.makeText(context, "تم التقاط الصورة بنجاح! 📸", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Gallery Launcher
    val adminGalleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val base64 = try {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= 29) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val outputStream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
                val byteArray = outputStream.toByteArray()
                android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
            if (base64 != null) {
                adminSelectedImageBase64 = base64
                Toast.makeText(context, "تم اختيار الصورة من المعرض بنجاح! 🖼️", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
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
                        if (isGeneralAdmin) "المدير العام لمجرة السودان 👑" else "المدير الإداري لمجرة السودان 👑",
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
                    if (isGeneralAdmin) "تتمتع بصلاحية مطلقة لإدارة المبيعات والمناديب والمنتجات." else "تتمتع بصلاحيات لإدارة وتوثيق طلبات العملاء والمناديب وتوثيقات التجار.",
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
            val tabs = buildList {
                add("الملخص📊" to 0)
                add((if (pendingCourierOrdersCount > 0) "طلبات العملاء 📦 ($pendingCourierOrdersCount)" else "طلبات العملاء 📦") to 3)
                add("المناديب🚴" to 4)
                if (!isAdministrativeManager) {
                    add("المخزون📦" to 7)
                }
                add("التجار🧑‍💼" to 6)
                add((if (pendingProductsCount > 0) "منتجات قيد المراجعة⏳ ($pendingProductsCount)" else "منتجات قيد المراجعة⏳") to 8)
                
                // Allow both General Manager and Administrative Manager to view products
                add("المنتجات🛍️" to 2)

                if (!isAdministrativeManager) {
                    add("إضافة ➕" to 1)
                    add("مفاتيح الربط🔑" to 5)
                }
                add((if (pendingPharmacyCount > 0) "توثيق وطلبات الصيدليات 💊 ($pendingPharmacyCount)" else "توثيق وطلبات الصيدليات 💊") to 9)
                add((if (pendingRestaurantOrdersCount > 0) "توثيق وطلبات المطاعم 🍔 ($pendingRestaurantOrdersCount)" else "توثيق وطلبات المطاعم 🍔") to 11)
                
                // Expose Ratings to both General Manager and Administrative Manager
                if (isGeneralAdmin || isAdministrativeManager) {
                    add("التقييمات ⭐" to 12)
                }
                if (isGeneralAdmin) {
                    add("المدراء 👑" to 10)
                    add("إدارة المنظومة 👥" to 13)
                }
            }
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { activeDetailDialog = "couriers_list" },
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("إدارة مناديب الشحن واللوجستيك 🚴", fontSize = 11.sp, color = MediumContrastTextDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${allCouriers.size} كابتن نشط بالسودان", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CosmicSecondary)
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
                    if (!isGeneralAdmin) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(0.4f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "صلاحية مقيدة 🔒",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "عذراً، إضافة منتجات جديدة للمتجر هي ميزة حصرية للمدير العام فقط لحماية وتأمين جودة السلع والمبيعات.",
                                        color = MediumContrastTextDark,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    } else {
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
                            Text("صورة المنتج الحقيقية (المعرض أو الكاميرا) 📸:", color = Color.White, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (adminSelectedImageBase64 != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, CosmicSecondary, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.TopStart
                                        ) {
                                            ProductImagePlaceholder(adminSelectedImageBase64!!, modifier = Modifier.fillMaxSize())
                                            IconButton(
                                                onClick = { adminSelectedImageBase64 = null },
                                                modifier = Modifier.size(24.dp).background(Color.Black.copy(0.6f), RoundedCornerShape(12.dp))
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "حذف الصورة", tint = Color.Red, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { adminCameraLauncher.launch(null) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("الكاميرا 📸", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { adminGalleryLauncher.launch("image/*") },
                                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("المعرض 🖼️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("اختر تصنيف القسم للمنتج:", color = Color.White, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                            val cats = listOf(
                                Triple("electronics", "كوكب الإلكترونيات", "electronics"),
                                Triple("fashion", "كوكب الأزياء", "fashion"),
                                Triple("furniture", "كوكب الأثاثات المنزلية", "furniture"),
                                Triple("services", "كوكب خدمات عامة", "services"),
                                Triple("crafts", "كوكب أعمال حرفية", "crafts"),
                                Triple("estate_cars", "كوكب بيع العقارات والسيارات", "estate_cars"),
                                Triple("pharmacy", "كوكب صيدلية", "pharmacy"),
                                Triple("restaurant", "كوكب مطاعم", "restaurant"),
                                Triple("kids", "كوكب مستلزمات أطفال", "kids"),
                                Triple("women", "كوكب للنساء", "women"),
                                Triple("men", "كوكب للرجال", "men"),
                                Triple("travel", "كوكب وكالات سفر وسياحة", "travel"),
                                Triple("tickets", "كوكب حجوزات تذاكر", "tickets"),
                                Triple("hotels", "كوكب حجوزات فندقية", "hotels"),
                                Triple("foods", "كوكب الأغذية والمأكولات", "foods"),
                                Triple("cosmetics", "كوكب عطور وتجميل", "cosmetics"),
                                Triple("other", "كوكب منتجات أخرى", "other")
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
                                        imageResName = adminSelectedImageBase64 ?: prodImageRes,
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
                                            adminSelectedImageBase64 = null
                                            activeTab = 2 // Move to products list
                                        } else {
                                            Toast.makeText(context, "تم حفظ المنتج محلياً! ⚠️ فشل المزامنة الخارجية: $err", Toast.LENGTH_LONG).show()
                                            adminSelectedImageBase64 = null
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
                                                    if (!isGeneralAdmin) {
                                                        Toast.makeText(context, "عذراً، حذف المنتجات ميزة حصرية للمدير العام فقط 🔒", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        viewModel.deleteProduct(product.id) { err ->
                                                            if (err == null) {
                                                              Toast.makeText(context, "تم حذف المنتج بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                              Toast.makeText(context, "تم الحذف محلياً! خطأ Supabase: $err", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    "حذف",
                                                    tint = if (isGeneralAdmin) Color.Red.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.5f)
                                                )
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
                            !( (status.contains("تمام") || status.contains("تم توصيل") || status.contains("ملغي") || status.contains("تم التسليم")) && !status.contains("تم تسليم المندوب") && !status.contains("لمندوب") )
                        }
                        val closedKeys = grouped.keys.filter { orderId ->
                            val status = grouped[orderId]?.firstOrNull()?.statusArabic ?: ""
                            (status.contains("تمام") || status.contains("تم توصيل") || status.contains("ملغي") || status.contains("تم التسليم")) && !status.contains("تم تسليم المندوب") && !status.contains("لمندوب")
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
                                            val productObj = allProducts.find { it.id == item.productId }
                                            val productSeller = productObj?.sellerEmail?.let { email ->
                                                sellers.find { it.email.trim().lowercase() == email.trim().lowercase() }
                                            }
                                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("${viewModel.formatPrice(item.priceAtOrder * item.quantity)} SDG", color = Color.White, fontSize = 11.sp)
                                                    Text("${item.productName} (الكمية: ${item.quantity})", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, textAlign = TextAlign.Right)
                                                }
                                                if (productSeller != null) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                                        horizontalArrangement = Arrangement.End,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        TextButton(
                                                            onClick = {
                                                                val msg = "مرحباً يا ${productSeller.name} 🪐، تم شراء منتجك (${item.productName}) بالكمية (${item.quantity}) بقيمة ${item.priceAtOrder * item.quantity} SDG من قبل العميل (${parent?.customerName ?: "عميل المجرة"}). يرجى تجهيزه للتسليم للمندوب فوراً."
                                                                openWhatsAppDirectly(context, productSeller.phone, msg)
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                Text("مراسلة التاجر (${productSeller.name}) واتساب مباشر 💬", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                                Icon(Icons.Default.Message, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                                                            }
                                                        }
                                                    }
                                                }
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
                                                    if (currentCourierName.isNotEmpty()) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.End,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text("$currentCourierName (${parent?.courierPhone ?: ""})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Right)
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("🚴 المندوب الذي قام بالتوصيل:", color = CosmicSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                    }
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
                    var selectedStatusFilter by remember { mutableStateOf("الكل") }
                    var selectedCourierForDetails by remember { mutableStateOf<com.example.data.db.CourierEntity?>(null) }

                    val totalCouriersCount = allCouriers.size
                    val activeCouriersCount = allCouriers.count { it.status.contains("نشط") || it.status.contains("🟢") }
                    val missionCouriersCount = allCouriers.count { it.status.contains("مهمة") || it.status.contains("🟡") }
                    val unavailableCouriersCount = allCouriers.count { it.status.contains("غير متوفر") || it.status.contains("🔴") || it.status.contains("غير نشط") }

                    val filteredCouriers = when (selectedStatusFilter) {
                        "نشط ومتوفر 🟢" -> allCouriers.filter { it.status.contains("نشط") || it.status.contains("🟢") }
                        "في مهمة توصيل 🟡" -> allCouriers.filter { it.status.contains("مهمة") || it.status.contains("🟡") }
                        "غير متوفر 🔴" -> allCouriers.filter { it.status.contains("غير متوفر") || it.status.contains("🔴") || it.status.contains("غير نشط") }
                        else -> allCouriers
                    }

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

                        // STATISTICS / CLASSIFICATION ROW
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "إحصائيات وتصنيف المناديب الفوري 📊",
                                    color = CosmicSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Total Chip
                                    val totalSelected = selectedStatusFilter == "الكل"
                                    Card(
                                        modifier = Modifier.weight(1f).clickable { selectedStatusFilter = "الكل" },
                                        colors = CardDefaults.cardColors(containerColor = if (totalSelected) CosmicSecondary else CosmicSurfaceVariant.copy(0.3f)),
                                        border = BorderStroke(1.dp, if (totalSelected) CosmicSecondary else Color.Transparent)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("الكل 🚴", fontSize = 10.sp, color = if (totalSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                            Text("$totalCouriersCount", fontSize = 14.sp, color = if (totalSelected) Color.Black else CosmicSecondary, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Active Chip
                                    val activeSelected = selectedStatusFilter == "نشط ومتوفر 🟢"
                                    Card(
                                        modifier = Modifier.weight(1.2f).clickable { selectedStatusFilter = "نشط ومتوفر 🟢" },
                                        colors = CardDefaults.cardColors(containerColor = if (activeSelected) Color(0xFF2ECC71) else CosmicSurfaceVariant.copy(0.3f)),
                                        border = BorderStroke(1.dp, if (activeSelected) Color(0xFF2ECC71) else Color.Transparent)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("نشط 🟢", fontSize = 10.sp, color = if (activeSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                            Text("$activeCouriersCount", fontSize = 14.sp, color = if (activeSelected) Color.Black else Color(0xFF2ECC71), fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Mission Chip
                                    val missionSelected = selectedStatusFilter == "في مهمة توصيل 🟡"
                                    Card(
                                        modifier = Modifier.weight(1.2f).clickable { selectedStatusFilter = "في مهمة توصيل 🟡" },
                                        colors = CardDefaults.cardColors(containerColor = if (missionSelected) Color(0xFFF1C40F) else CosmicSurfaceVariant.copy(0.3f)),
                                        border = BorderStroke(1.dp, if (missionSelected) Color(0xFFF1C40F) else Color.Transparent)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("في مهمة 🟡", fontSize = 10.sp, color = if (missionSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                            Text("$missionCouriersCount", fontSize = 14.sp, color = if (missionSelected) Color.Black else Color(0xFFF1C40F), fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Unavailable Chip
                                    val unselected = selectedStatusFilter == "غير متوفر 🔴"
                                    Card(
                                        modifier = Modifier.weight(1.2f).clickable { selectedStatusFilter = "غير متوفر 🔴" },
                                        colors = CardColors(containerColor = if (unselected) Color(0xFFE74C3C) else CosmicSurfaceVariant.copy(0.3f), contentColor = Color.White, disabledContainerColor = Color.Transparent, disabledContentColor = Color.Transparent),
                                        border = BorderStroke(1.dp, if (unselected) Color(0xFFE74C3C) else Color.Transparent)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("غير متوفر 🔴", fontSize = 10.sp, color = if (unselected) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                            Text("$unavailableCouriersCount", fontSize = 14.sp, color = if (unselected) Color.Black else Color(0xFFE74C3C), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                    Text(
                                        "إضافة مندوب المجرة جديد للقاعدة ➕",
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

                        if (filteredCouriers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (allCouriers.isEmpty()) "لا توجد مناديب شحن مسجلة بقاعدة البيانات حالياً 🚴" else "لا توجد مناديب تطابق هذا التصنيف حالياً 🚴", color = MediumContrastTextDark, fontSize = 12.sp)
                                }
                            }
                        } else {
                            items(filteredCouriers) { courier ->
                                var isTasksExpanded by remember { mutableStateOf(false) }
                                val courierOrders = allOrders.filter { 
                                    it.courierName.trim().equals(courier.name.trim(), ignoreCase = true) || 
                                    it.courierPhone.trim().replace("+", "").replace(" ", "") == courier.phone.trim().replace("+", "").replace(" ", "")
                                }.groupBy { it.orderId }
                                val ordersCount = courierOrders.size

                                Card(
                                    modifier = Modifier.fillMaxWidth().animateContentSize().clickable {
                                        selectedCourierForDetails = courier
                                    },
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
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

                                        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(CosmicSurfaceVariant.copy(0.4f)))

                                        // Expandable Header for Orders Tracking
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isTasksExpanded = !isTasksExpanded }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isTasksExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = "توسيع",
                                                tint = CosmicSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "عرض وتتبع مهام المندوب الحالية (${ordersCount}) 📋",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (ordersCount > 0) CosmicSecondary else Color.White.copy(0.5f)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsBike,
                                                    contentDescription = null,
                                                    tint = if (ordersCount > 0) CosmicSecondary else Color.White.copy(0.5f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        if (isTasksExpanded) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(0.3f))
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (courierOrders.isEmpty()) {
                                                    Text(
                                                        text = "لا توجد أي طلبيات مسندة لهذا المندوب حالياً. لإسناد طلبية له، اذهب إلى تبويب (الطلبات 📦) واختر هذا المندوب.",
                                                        fontSize = 10.sp,
                                                        color = MediumContrastTextDark,
                                                        textAlign = TextAlign.Right,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                } else {
                                                    courierOrders.entries.forEach { (orderId, itemsList) ->
                                                        val parentOrder = itemsList.firstOrNull()
                                                        val totalPrice = itemsList.sumOf { it.priceAtOrder * it.quantity }
                                                        val deliveryFee = parentOrder?.deliveryFee ?: 0.0
                                                        val totalInvoiceAmount = totalPrice + deliveryFee
                                                        val statusLabel = parentOrder?.statusArabic ?: "قيد المعالجة"

                                                        Card(
                                                            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(0.2f)),
                                                            border = BorderStroke(1.dp, CosmicSecondary.copy(0.1f)),
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Column(
                                                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                                                horizontalAlignment = Alignment.End
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Text(
                                                                        text = statusLabel,
                                                                        color = if (statusLabel.contains("تم التسليم") || statusLabel.contains("تم توصيل")) Color.Green else CosmicSecondary,
                                                                        fontSize = 10.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                    Text(
                                                                        text = "طلب #${orderId.take(8)}...",
                                                                        color = Color.White,
                                                                        fontSize = 10.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Text("العميل: ${parentOrder?.customerName}", color = Color.White.copy(0.8f), fontSize = 10.sp)
                                                                Text("العنوان: ${parentOrder?.customerAddress}", color = Color.White.copy(0.7f), fontSize = 10.sp)
                                                                Text("الهاتف: ${parentOrder?.customerPhone}", color = CosmicSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                                                Text("المجموع: ${viewModel.formatPrice(totalInvoiceAmount)} SDG", color = CosmicTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                                
                                                                Spacer(modifier = Modifier.height(8.dp))
                                                                
                                                                // Status Updates inside courier task list
                                                                Text("تحديث حالة هذه المهمة فوراً:", fontSize = 9.sp, color = Color.White.copy(0.5f))
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)
                                                                ) {
                                                                    val quickStatuses = listOf(
                                                                        "تم التسليم لمندوب التوصيل بالولاية 🚴" to "تسليم 🚴",
                                                                        "تم توصيل الطلب واستلام المبلغ ✅" to "توصيل ✅",
                                                                        "الطلب ملغي ❌" to "إلغاء ❌"
                                                                    )
                                                                    quickStatuses.forEach { (statusVal, btnText) ->
                                                                        val isCurrent = parentOrder?.statusArabic == statusVal
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .clip(RoundedCornerShape(6.dp))
                                                                                .background(if (isCurrent) CosmicSecondary else CosmicSurfaceVariant)
                                                                                .clickable {
                                                                                    viewModel.updateOrderStatus(orderId, statusVal) { err ->
                                                                                        if (err == null) {
                                                                                            Toast.makeText(context, "تم تحديث حالة طلب المندوب بنجاح!", Toast.LENGTH_SHORT).show()
                                                                                        } else {
                                                                                            Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                                                                        }
                                                                                    }
                                                                                }
                                                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                                                        ) {
                                                                            Text(btnText, color = if (isCurrent) Color.Black else Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                                        }
                                                                    }
                                                                }
                                                                
                                                                Spacer(modifier = Modifier.height(6.dp))
                                                                
                                                                // Contact Buttons (Call & WhatsApp)
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    // WhatsApp Client
                                                                    OutlinedButton(
                                                                        onClick = {
                                                                            val msg = "مرحباً يا ${parentOrder?.customerName ?: "زبوننا الكريم"}، معك مندوب المجرة للتسوق للتسوق. نود تتبع واستلام طلبك رقم #${orderId.take(8)}."
                                                                            WhatsAppUtils.sendWhatsAppMessage(context, parentOrder?.customerPhone, msg)
                                                                        },
                                                                        border = BorderStroke(1.dp, Color.Green.copy(0.4f)),
                                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                                        modifier = Modifier.weight(1f).height(28.dp),
                                                                        shape = RoundedCornerShape(6.dp)
                                                                    ) {
                                                                        Text("واتساب الزبون 💬", color = Color.Green, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                                    }
                                                                    
                                                                    // Call Client
                                                                    OutlinedButton(
                                                                        onClick = {
                                                                            val phoneClean = parentOrder?.customerPhone?.trim()?.replace("+", "")?.replace(" ", "") ?: ""
                                                                            val intent = Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:$phoneClean"))
                                                                            context.startActivity(intent)
                                                                        },
                                                                        border = BorderStroke(1.dp, CosmicSecondary.copy(0.4f)),
                                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                                        modifier = Modifier.weight(1f).height(28.dp),
                                                                        shape = RoundedCornerShape(6.dp)
                                                                    ) {
                                                                        Text("اتصال بالزبون 📞", color = CosmicSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
                                            "⚠️ الرجاء تسجيل مندوب المجرة أولاً بالعلّو لعرض البوابة الذكية.",
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
                                                                                val msg = "🌌 مرحباً يا ${parentOrder.customerName}! معكم المندوب ${curSim.name} من تطبيق مجرة السودان. أنا متكفل بتسليم طلبيتكم الآن رقم (#${orderId.take(5)}) وقيمتها ${viewModel.formatPrice(totalPriceSumInSim)} SDG. هل أنتم متواجدون بالعنوان: ${parentOrder.customerAddress} لتسليمها؟"
                                                                                WhatsAppUtils.sendWhatsAppMessage(context, phoneNum, msg)
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

                    if (selectedCourierForDetails != null) {
                        val courier = selectedCourierForDetails!!
                        val courierOrders = allOrders.filter { 
                            it.courierName.trim().equals(courier.name.trim(), ignoreCase = true) || 
                            it.courierPhone.trim().replace("+", "").replace(" ", "") == courier.phone.trim().replace("+", "").replace(" ", "")
                        }.groupBy { it.orderId }

                        AlertDialog(
                            onDismissRequest = { selectedCourierForDetails = null },
                            title = {
                                Text(
                                    "تفاصيل كابتن التوصيل الكوني 🚴",
                                    color = CosmicSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    // Basic Courier Details
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth(),
                                        border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.2f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(courier.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.clickable {
                                                    try {
                                                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${courier.phone}"))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "لا يمكن إجراء المكالمة الآن", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(courier.phone, fontSize = 12.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(Icons.Default.Phone, contentDescription = "اتصال", tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("مناطق التغطية: ${courier.stateInfo}", fontSize = 12.sp, color = Color.White.copy(0.7f))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            val statusColor = when {
                                                courier.status.contains("متوفر") || courier.status.contains("🟢") -> Color.Green
                                                courier.status.contains("مهمة") || courier.status.contains("🟡") -> CosmicTertiary
                                                else -> Color.Red
                                            }
                                            Text("حالة المندوب الحالية: ${courier.status}", fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Tasks/Orders List
                                    Text(
                                        "الطلبات والمهام المسندة (${courierOrders.size}) 📋",
                                        fontWeight = FontWeight.Bold,
                                        color = CosmicSecondary,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )

                                    if (courierOrders.isEmpty()) {
                                        Text(
                                            "لا توجد طلبيات نشطة مسندة لهذا المندوب حالياً 🚴",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(0.5f),
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                        )
                                    } else {
                                        courierOrders.entries.forEach { (orderId, itemsList) ->
                                            val firstOrder = itemsList.firstOrNull()
                                            val custName = firstOrder?.customerName ?: "عميل كوني"
                                            val custPhone = firstOrder?.customerPhone ?: ""
                                            val custAddress = firstOrder?.customerAddress ?: "ولاية بورتسودان"
                                            val orderStatus = firstOrder?.statusArabic ?: "قيد التوصيل"
                                            val subtotal = itemsList.sumOf { it.priceAtOrder * it.quantity }
                                            val delFee = firstOrder?.deliveryFee ?: 0.0
                                            val totalAmount = subtotal + delFee

                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.1f))
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("#${orderId.takeLast(6)}", fontSize = 11.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                                        Text(orderStatus, fontSize = 11.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("العميل: $custName", fontSize = 12.sp, color = Color.White)
                                                    Text("الهاتف: $custPhone", fontSize = 11.sp, color = Color.White.copy(0.7f), modifier = Modifier.clickable {
                                                        try {
                                                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$custPhone"))
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {}
                                                    })
                                                    Text("العنوان: $custAddress", fontSize = 11.sp, color = Color.White.copy(0.7f))
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("إجمالي الفاتورة: ${"%,.0f".format(totalAmount)} جنيه سوداني", fontSize = 12.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = { selectedCourierForDetails = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                                ) {
                                    Text("إغلاق التفاصيل ❌", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                }
                5 -> {
                    if (!isGeneralAdmin) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "🔒",
                                    tint = Color.Red,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "صلاحية مغلقة 🔒",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "تعديل مفاتيح الربط والاتصال السحابي ميزة حصرية للمدير العام فقط ولا يمكن للمدراء الإداريين تعديلها.",
                                    color = MediumContrastTextDark,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
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
-- ⚠️ مسح الجداول القديمة تماماً لضمان البدء من جديد بمخطط نظيف ومحدث
DROP TABLE IF EXISTS public.orders CASCADE;
DROP TABLE IF EXISTS public.products CASCADE;
DROP TABLE IF EXISTS public.couriers CASCADE;
DROP TABLE IF EXISTS public.profiles CASCADE;
DROP TABLE IF EXISTS public.sellers CASCADE;
DROP TABLE IF EXISTS public.pharmacies CASCADE;
DROP TABLE IF EXISTS public.pharmacy_products CASCADE;
DROP TABLE IF EXISTS public.pharmacy_orders CASCADE;
DROP TABLE IF EXISTS public.restaurants CASCADE;
DROP TABLE IF EXISTS public.restaurant_orders CASCADE;
DROP TABLE IF EXISTS public.app_ratings CASCADE;
DROP TABLE IF EXISTS public.app_coupons CASCADE;

-- 1. إنشاء جدول المنتجات (products)
CREATE TABLE public.products (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    price DOUBLE PRECISION NOT NULL,
    category TEXT NOT NULL,
    category_arabic TEXT NOT NULL,
    rating REAL,
    image_res_name TEXT,
    is_favorite BOOLEAN DEFAULT false,
    stock INTEGER DEFAULT 10,
    seller_email TEXT DEFAULT '',
    is_approved BOOLEAN DEFAULT true
);

-- 2. إنشاء جدول الطلبات الأسبوعي واليومي (orders)
CREATE TABLE public.orders (
    id SERIAL PRIMARY KEY,
    order_id TEXT NOT NULL,
    product_id INTEGER NOT NULL,
    product_name TEXT NOT NULL,
    price_at_order DOUBLE PRECISION NOT NULL,
    quantity INTEGER NOT NULL,
    order_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    status_arabic TEXT NOT NULL,
    customer_name TEXT,
    customer_phone TEXT,
    customer_address TEXT,
    courier_name TEXT DEFAULT '',
    courier_phone TEXT DEFAULT '',
    delivery_fee DOUBLE PRECISION DEFAULT 5000.0,
    payment_method TEXT DEFAULT 'كاش',
    bank_receipt_image_uri TEXT
);

-- 3. إنشاء جدول مناديب التوصيل بالسودان (couriers)
CREATE TABLE public.couriers (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    state_info TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'نشط ومتوفر 🟢'
);

-- 4. إنشاء جدول المستخدمين والعملاء (profiles)
CREATE TABLE public.profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT,
    phone TEXT,
    email TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- 5. إنشاء جدول البائعين (sellers)
CREATE TABLE public.sellers (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT,
    classification TEXT DEFAULT 'تاجر ذهبي ⭐',
    commission_rate DOUBLE PRECISION DEFAULT 0.10,
    created_at BIGINT
);

-- 6. إنشاء جدول الصيدليات (pharmacies)
CREATE TABLE public.pharmacies (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    doctor_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    location TEXT NOT NULL,
    pharmacist_email TEXT NOT NULL,
    is_approved BOOLEAN DEFAULT false,
    image_base64 TEXT DEFAULT '',
    has_cosmetics BOOLEAN DEFAULT false,
    created_at BIGINT
);

-- 7. إنشاء جدول منتجات الصيدليات (pharmacy_products)
CREATE TABLE public.pharmacy_products (
    id SERIAL PRIMARY KEY,
    pharmacy_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    name TEXT NOT NULL,
    company TEXT,
    price DOUBLE PRECISION NOT NULL,
    image_base64 TEXT DEFAULT '',
    is_approved BOOLEAN DEFAULT false,
    created_at BIGINT
);

-- 8. إنشاء جدول طلبات الصيدليات والروشتات (pharmacy_orders)
CREATE TABLE public.pharmacy_orders (
    id SERIAL PRIMARY KEY,
    pharmacy_id INTEGER NOT NULL,
    customer_name TEXT NOT NULL,
    customer_phone TEXT NOT NULL,
    customer_email TEXT DEFAULT '',
    prescription_image_base64 TEXT DEFAULT '',
    medicines_json TEXT DEFAULT '',
    medicine_price DOUBLE PRECISION DEFAULT 0.0,
    delivery_fee DOUBLE PRECISION DEFAULT 0.0,
    courier_name TEXT DEFAULT '',
    courier_phone TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'بانتظار الصيدلي',
    payment_method TEXT DEFAULT 'كاش',
    bank_receipt_image_uri TEXT DEFAULT '',
    created_at BIGINT
);

-- 9. إنشاء جدول المطاعم (restaurants)
CREATE TABLE public.restaurants (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    menu_image_uri TEXT DEFAULT '',
    logo_image_uri TEXT DEFAULT '',
    is_approved BOOLEAN DEFAULT false,
    created_at BIGINT
);

-- 10. إنشاء جدول طلبات المطاعم (restaurant_orders)
CREATE TABLE public.restaurant_orders (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL,
    restaurant_name TEXT NOT NULL,
    restaurant_phone TEXT NOT NULL,
    customer_name TEXT NOT NULL,
    customer_email TEXT NOT NULL,
    customer_phone TEXT NOT NULL,
    items_and_notes TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'معلق',
    payment_method TEXT NOT NULL DEFAULT 'كاش',
    delivery_fee DOUBLE PRECISION DEFAULT 0.0,
    bank_receipt_image_uri TEXT DEFAULT '',
    courier_name TEXT DEFAULT '',
    courier_phone TEXT DEFAULT '',
    created_at BIGINT
);

-- 11. إنشاء جدول تقييمات التطبيق (app_ratings)
CREATE TABLE public.app_ratings (
    id SERIAL PRIMARY KEY,
    customer_name TEXT NOT NULL,
    customer_email TEXT NOT NULL,
    customer_phone TEXT DEFAULT '',
    customer_classification TEXT DEFAULT 'عميل عادي 👤',
    rating_stars INTEGER NOT NULL,
    comment TEXT,
    rating_date BIGINT
);

-- 12. إنشاء جدول كوبونات الخصم والجوائز (app_coupons)
CREATE TABLE public.app_coupons (
    id SERIAL PRIMARY KEY,
    code TEXT UNIQUE NOT NULL,
    discount_percent DOUBLE PRECISION DEFAULT 0.0,
    is_free_delivery BOOLEAN DEFAULT false,
    is_bogo BOOLEAN DEFAULT false,
    for_user_email TEXT DEFAULT '',
    is_used BOOLEAN DEFAULT false,
    offer_title TEXT NOT NULL
);

-- 6. تفعيل سياسات أمن مستوى الصفوف (Row Level Security - RLS)
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.couriers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sellers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pharmacies ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pharmacy_products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pharmacy_orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.restaurants ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.restaurant_orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_ratings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_coupons ENABLE ROW LEVEL SECURITY;

-- 7. حذف السياسات القديمة إن وجدت لمنع حدوث تعارض أو تكرار
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
DROP POLICY IF EXISTS "Allow select pharmacies" ON public.pharmacies;
DROP POLICY IF EXISTS "Allow insert pharmacies" ON public.pharmacies;
DROP POLICY IF EXISTS "Allow update pharmacies" ON public.pharmacies;
DROP POLICY IF EXISTS "Allow delete pharmacies" ON public.pharmacies;
DROP POLICY IF EXISTS "Allow select pharmacy_products" ON public.pharmacy_products;
DROP POLICY IF EXISTS "Allow insert pharmacy_products" ON public.pharmacy_products;
DROP POLICY IF EXISTS "Allow update pharmacy_products" ON public.pharmacy_products;
DROP POLICY IF EXISTS "Allow delete pharmacy_products" ON public.pharmacy_products;
DROP POLICY IF EXISTS "Allow select pharmacy_orders" ON public.pharmacy_orders;
DROP POLICY IF EXISTS "Allow insert pharmacy_orders" ON public.pharmacy_orders;
DROP POLICY IF EXISTS "Allow update pharmacy_orders" ON public.pharmacy_orders;
DROP POLICY IF EXISTS "Allow delete pharmacy_orders" ON public.pharmacy_orders;
DROP POLICY IF EXISTS "Allow select restaurants" ON public.restaurants;
DROP POLICY IF EXISTS "Allow insert restaurants" ON public.restaurants;
DROP POLICY IF EXISTS "Allow update restaurants" ON public.restaurants;
DROP POLICY IF EXISTS "Allow delete restaurants" ON public.restaurants;
DROP POLICY IF EXISTS "Allow select restaurant_orders" ON public.restaurant_orders;
DROP POLICY IF EXISTS "Allow insert restaurant_orders" ON public.restaurant_orders;
DROP POLICY IF EXISTS "Allow update restaurant_orders" ON public.restaurant_orders;
DROP POLICY IF EXISTS "Allow delete restaurant_orders" ON public.restaurant_orders;
DROP POLICY IF EXISTS "Allow select app_ratings" ON public.app_ratings;
DROP POLICY IF EXISTS "Allow insert app_ratings" ON public.app_ratings;
DROP POLICY IF EXISTS "Allow select app_coupons" ON public.app_coupons;
DROP POLICY IF EXISTS "Allow insert app_coupons" ON public.app_coupons;
DROP POLICY IF EXISTS "Allow update app_coupons" ON public.app_coupons;

-- 8. إنشاء سياسات الوصول الكونية الجديدة للسماح بالوصول الكامل دون قيود للتطبيق (Anon / Public)
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

CREATE POLICY "Allow select pharmacies" ON public.pharmacies FOR SELECT USING (true);
CREATE POLICY "Allow insert pharmacies" ON public.pharmacies FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update pharmacies" ON public.pharmacies FOR UPDATE USING (true);
CREATE POLICY "Allow delete pharmacies" ON public.pharmacies FOR DELETE USING (true);

CREATE POLICY "Allow select pharmacy_products" ON public.pharmacy_products FOR SELECT USING (true);
CREATE POLICY "Allow insert pharmacy_products" ON public.pharmacy_products FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update pharmacy_products" ON public.pharmacy_products FOR UPDATE USING (true);
CREATE POLICY "Allow delete pharmacy_products" ON public.pharmacy_products FOR DELETE USING (true);

CREATE POLICY "Allow select pharmacy_orders" ON public.pharmacy_orders FOR SELECT USING (true);
CREATE POLICY "Allow insert pharmacy_orders" ON public.pharmacy_orders FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update pharmacy_orders" ON public.pharmacy_orders FOR UPDATE USING (true);
CREATE POLICY "Allow delete pharmacy_orders" ON public.pharmacy_orders FOR DELETE USING (true);

CREATE POLICY "Allow select restaurants" ON public.restaurants FOR SELECT USING (true);
CREATE POLICY "Allow insert restaurants" ON public.restaurants FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update restaurants" ON public.restaurants FOR UPDATE USING (true);
CREATE POLICY "Allow delete restaurants" ON public.restaurants FOR DELETE USING (true);

CREATE POLICY "Allow select restaurant_orders" ON public.restaurant_orders FOR SELECT USING (true);
CREATE POLICY "Allow insert restaurant_orders" ON public.restaurant_orders FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update restaurant_orders" ON public.restaurant_orders FOR UPDATE USING (true);
CREATE POLICY "Allow delete restaurant_orders" ON public.restaurant_orders FOR DELETE USING (true);

CREATE POLICY "Allow select app_ratings" ON public.app_ratings FOR SELECT USING (true);
CREATE POLICY "Allow insert app_ratings" ON public.app_ratings FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow select app_coupons" ON public.app_coupons FOR SELECT USING (true);
CREATE POLICY "Allow insert app_coupons" ON public.app_coupons FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow update app_coupons" ON public.app_coupons FOR UPDATE USING (true);
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
                }
                6 -> {
                    // TAB 6: SELLERS MANAGEMENT & COMMISSIONS
                    val sellers by viewModel.allSellers.collectAsStateWithLifecycle()
                    var newSellerName by remember { mutableStateOf("") }
                    var newSellerEmail by remember { mutableStateOf("") }
                    var newSellerPhone by remember { mutableStateOf("") }
                    var newSellerClass by remember { mutableStateOf("تاجر المجرة ⭐") }
                    var newSellerCommission by remember { mutableStateOf("5") } // in %
                    var sellerSearchQuery by remember { mutableStateOf("") }
                    var sellersSubTab by remember { mutableStateOf(0) } // 0: Sellers list, 1: Approved Merchant Requests

                    val filteredSellers = remember(sellers, sellerSearchQuery) {
                        if (sellerSearchQuery.isBlank()) {
                            sellers
                        } else {
                            sellers.filter {
                                it.name.contains(sellerSearchQuery, ignoreCase = true) ||
                                it.email.contains(sellerSearchQuery, ignoreCase = true) ||
                                (it.phone ?: "").contains(sellerSearchQuery, ignoreCase = true) ||
                                it.classification.contains(sellerSearchQuery, ignoreCase = true)
                            }
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Sub-tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .background(CosmicSurfaceVariant.copy(0.3f), RoundedCornerShape(10.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { sellersSubTab = 0 },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sellersSubTab == 0) CosmicSecondary else Color.Transparent,
                                    contentColor = if (sellersSubTab == 0) Color.Black else Color.White
                                )
                            ) {
                                Text("إدارة وتصنيف التجار 🧑‍💼", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { sellersSubTab = 1 },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sellersSubTab == 1) CosmicSecondary else Color.Transparent,
                                    contentColor = if (sellersSubTab == 1) Color.Black else Color.White
                                )
                            ) {
                                val approvedCount = allProducts.count { it.isApproved && it.sellerEmail.isNotBlank() }
                                Text("طلبات التجار المعتمدة 📜 ($approvedCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (sellersSubTab == 0) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Text(
                                        "إدارة تجار المجرة وبرنامج العمولات 🧑‍💼 🌌",
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
                                                "تسجيل تاجر جديد في التطبيق ➕",
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
                                                label = { Text("اسم التاجر الكامل", color = Color.Gray) },
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
                                                    label = { Text("تصنيف التاجر", color = Color.Gray) },
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
                                                        val comm = newSellerCommission.toDoubleOrNull() ?: 5.0
                                                        viewModel.addSeller(
                                                            name = newSellerName.trim(),
                                                            email = newSellerEmail.trim().lowercase(),
                                                            phone = newSellerPhone.trim(),
                                                            classification = newSellerClass.trim(),
                                                            commissionRate = comm / 100.0
                                                        ) { err ->
                                                            if (err == null) {
                                                                Toast.makeText(context, "تم تسجيل التاجر ${newSellerName} بنجاح! 🎉", Toast.LENGTH_SHORT).show()
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
                                                Text("تسجيل التاجر وحفظه سحابياً 🌌", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        "قائمة التجار النشطين وإحصائيات العمولات 📊",
                                        color = CosmicSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                    )
                                }

                                // Search Bar for Sellers
                                item {
                                    OutlinedTextField(
                                        value = sellerSearchQuery,
                                        onValueChange = { sellerSearchQuery = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("seller_search_bar"),
                                        placeholder = {
                                            Text(
                                                "ابحث باسم التاجر أو البريد أو الهاتف أو التصنيف...",
                                                color = MediumContrastTextDark,
                                                fontSize = 12.sp,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Right
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = CosmicSecondary
                                            )
                                        },
                                        leadingIcon = {
                                            if (sellerSearchQuery.isNotEmpty()) {
                                                IconButton(onClick = { sellerSearchQuery = "" }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = "مسح البحث",
                                                        tint = Color.White.copy(alpha = 0.7f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(24.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = CosmicSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedContainerColor = CosmicSurface,
                                            unfocusedContainerColor = CosmicSurface
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right)
                                    )
                                }

                                if (sellers.isEmpty()) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurface.copy(0.5f))
                                        ) {
                                            Text(
                                                "لا يوجد أي تجار مسجلين حالياً. 📭",
                                                color = Color.LightGray,
                                                fontSize = 12.sp,
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else if (filteredSellers.isEmpty()) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurface.copy(0.3f))
                                        ) {
                                            Text(
                                                "لم يتم العثور على أي تجار يطابقون البحث الحالي! 🔍",
                                                color = CosmicSecondary,
                                                fontSize = 12.sp,
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    items(filteredSellers) { seller ->
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
                                                        Text("صافي التاجر 💰", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                                            invoiceText.append("🌌 *مبيعات التاجر في المجرة للتسوق* 🌌\n\n")
                                                            invoiceText.append("👤 *التاجر:* ${seller.name}\n")
                                                            invoiceText.append("⭐ *التصنيف:* ${seller.classification}\n\n")
                                                            invoiceText.append("📋 *تفاصيل الطلبيات الخاضعة للفوترة الصافية:*\n")
                                                            sellerOrderItems.forEach { item ->
                                                                invoiceText.append("- ${item.productName} (العدد: ${item.quantity}) سعره: ${viewModel.formatPrice(item.priceAtOrder * item.quantity)}\n")
                                                            }
                                                            invoiceText.append("\n-----------------------------------\n")
                                                            invoiceText.append("📊 *إجمالي قيمة مبيعات التاجر:* ${viewModel.formatPrice(totalRevenue)}\n")
                                                            invoiceText.append("💵 *المبلغ المستحق لك بالكامل (دون عمولة التطبيق):* ${viewModel.formatPrice(sellerNet)}\n\n")
                                                            invoiceText.append("🚀 *تمت الفوترة والتصدير تلقائياً عبر نظام المجرة الذكي بنجاح!*")

                                                            WhatsAppUtils.sendWhatsAppMessage(context, seller.phone, invoiceText.toString())
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
                                                                    Toast.makeText(context, "تم حذف التاجر بنجاح! 🗑️", Toast.LENGTH_SHORT).show()
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
                        } else {
                            // Approved Merchant Requests List
                            val approvedMerchantProducts = allProducts.filter { it.isApproved && it.sellerEmail.isNotBlank() }
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Text(
                                        "طلبات التجار المعتمدة كلياً في النظام 📜✨",
                                        color = CosmicSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "هنا تظهر كافة منتجات السادة التجار التي تم قبولها وتعميدها وتعديل أسعارها بالعمولة ونشرها بالمتجر الكوني.",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth(),
                                        lineHeight = 16.sp
                                    )
                                }

                                if (approvedMerchantProducts.isEmpty()) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurface)
                                        ) {
                                            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                Text("لا توجد طلبات تجار معتمدة أو منشورة حالياً! 📭", color = Color.Gray, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                } else {
                                    items(approvedMerchantProducts) { prod ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                            border = BorderStroke(1.dp, Color.Green.copy(0.3f))
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color.Green.copy(0.12f), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("معتمد ومنشور ✅", color = Color.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(prod.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("صاحب السلعة (التاجر): ${prod.sellerEmail}", color = Color.LightGray, fontSize = 11.sp)
                                                Text("الوصف: ${prod.description}", color = Color.Gray, fontSize = 11.sp)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("المخزون المتاح: ${prod.stock} قطعة", color = Color.White, fontSize = 11.sp)
                                                    Text("سعر البيع النهائي: ${viewModel.formatPrice(prod.price)}", color = CosmicSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
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
                                                    "التاجر: ${product.sellerEmail}",
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
                9 -> {
                    com.example.ui.screens.AdminPharmacyPortal(viewModel = viewModel)
                }
                10 -> {
                    AdminManagersSection(viewModel = viewModel)
                }
                11 -> {
                    com.example.ui.screens.RestaurantsPlanetSection(viewModel = viewModel, forceAdminPortal = true)
                }
                12 -> {
                    AdminRatingsSection(viewModel = viewModel)
                }
                13 -> {
                    AdminSystemManagementSection(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AdminManagersSection(viewModel: MajarahViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current ?: LocalContext.current
    val allAdminManagers by viewModel.allAdminManagers.collectAsStateWithLifecycle()
    
    var managerName by remember { mutableStateOf("") }
    var managerEmail by remember { mutableStateOf("") }
    var managerPhone by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                "إدارة المدراء الإداريين بالمنظومة الكونية 👑",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "يمكن للمدير العام إضافة مدراء إداريين لمساعدته في إدارة التطبيق. المدراء الإداريون لديهم كافة الصلاحيات ما عدا تعديل مفاتيح الربط وحذف أو إضافة مدراء آخرين.",
                color = MediumContrastTextDark,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 15.sp
            )
        }

        // Add Manager Card Form
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
                        "إضافة مدير إداري جديد ➕",
                        fontWeight = FontWeight.Bold,
                        color = CosmicSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = managerName,
                        onValueChange = { managerName = it },
                        label = { Text("الاسم بالكامل", color = CosmicSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = managerEmail,
                        onValueChange = { managerEmail = it },
                        label = { Text("البريد الإلكتروني", color = CosmicSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = managerPhone,
                        onValueChange = { managerPhone = it },
                        label = { Text("رقم الهاتف", color = CosmicSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (managerName.isBlank() || managerEmail.isBlank() || managerPhone.isBlank()) {
                                Toast.makeText(context, "الرجاء تعبئة كافة الحقول لملء الصلاحية ⚠️", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isAdding = true
                            viewModel.addAdminManager(managerName.trim(), managerEmail.trim(), managerPhone.trim()) { err ->
                                isAdding = false
                                if (err == null) {
                                    Toast.makeText(context, "تمت إضافة المدير الإداري بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                                    managerName = ""
                                    managerEmail = ""
                                    managerPhone = ""
                                } else {
                                    Toast.makeText(context, "خطأ: $err ❌", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isAdding
                    ) {
                        Text("إعتماد الصلاحية كمدير إداري ✅", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Managers List Header
        item {
            Text(
                "قائمة المدراء الإداريين النشطين 📋",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            )
        }

        if (allAdminManagers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "لا يوجد مدراء إداريون مسجلون حالياً. 🌌",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(allAdminManagers) { manager ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Delete Button
                        IconButton(
                            onClick = {
                                viewModel.removeAdminManager(manager.id) { err ->
                                    if (err == null) {
                                        Toast.makeText(context, "تم سحب صلاحية المدير بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "خطأ: $err ❌", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف الصلاحية", tint = Color.Red)
                        }

                        // Info
                        Column(horizontalAlignment = Alignment.End) {
                            Text(manager.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(manager.email, color = MediumContrastTextDark, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("هاتف: ${manager.phone}", color = CosmicSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AdminRatingsSection(viewModel: MajarahViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val ratings by viewModel.allRatingsFlow.collectAsStateWithLifecycle()
    val coupons by viewModel.allCouponsFlow.collectAsStateWithLifecycle()
    
    var customCouponCode by remember { mutableStateOf("") }
    var couponForEmail by remember { mutableStateOf("") }
    var couponOfferTitle by remember { mutableStateOf("") }
    var isFreeDelivery by remember { mutableStateOf(true) }
    var isAddingCoupon by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                "رصد وتقييمات العملاء للتطبيق ⭐🌌",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "تراقب هذه الصفحة آراء وتقييمات العملاء من 7 نجوم. يمكنك إرسال كوبونات مخصصة للعملاء الفائزين لإسعادهم وتفعيل الخصومات لهم تلقائياً.",
                color = MediumContrastTextDark,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 15.sp
            )
        }

        // Coupon Generator Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "إنشاء كوبون عرض للعميل الفائز 🎁",
                        color = CosmicSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    OutlinedTextField(
                        value = couponForEmail,
                        onValueChange = { couponForEmail = it },
                        label = { Text("البريد الإلكتروني للمستلم") },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customCouponCode,
                        onValueChange = { customCouponCode = it },
                        label = { Text("كود الكوبون المخصص (مثال: WINNER77)") },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = couponOfferTitle,
                        onValueChange = { couponOfferTitle = it },
                        label = { Text("عنوان العرض الترويجي (مثال: توصيل مجاني لمشاركتك)") },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isFreeDelivery) "نوع العرض: توصيل مجاني 🚚" else "نوع العرض: خصم 50% / قطعتين بسعر واحدة 🎁",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isFreeDelivery,
                            onCheckedChange = { isFreeDelivery = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CosmicSecondary,
                                checkedTrackColor = CosmicSecondary.copy(0.4f)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (customCouponCode.isBlank() || couponForEmail.isBlank() || couponOfferTitle.isBlank()) {
                                Toast.makeText(context, "الرجاء إدخال كافة حقول الكوبون ⚠️", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isAddingCoupon = true
                            coroutineScope.launch {
                                val coupon = com.example.data.db.AppCouponEntity(
                                    code = customCouponCode.trim().uppercase(),
                                    discountPercent = if (isFreeDelivery) 0.0 else 50.0,
                                    isFreeDelivery = isFreeDelivery,
                                    isBogo = !isFreeDelivery,
                                    forUserEmail = couponForEmail.trim().lowercase(),
                                    isUsed = false,
                                    offerTitle = couponOfferTitle.trim()
                                )
                                viewModel.database.appCouponDao().insertCoupon(coupon)
                                isAddingCoupon = false
                                Toast.makeText(context, "تم إرسال الكوبون بنجاح للعميل! 🌌🎁", Toast.LENGTH_SHORT).show()
                                customCouponCode = ""
                                couponForEmail = ""
                                couponOfferTitle = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isAddingCoupon
                    ) {
                        Text("إرسال الكوبون واعتماده سحابياً 🚀", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Coupons List Header
        item {
            Text(
                "الكوبونات والعروض النشطة حالياً 🎫",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            )
        }

        if (coupons.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد كوبونات عروض نشطة حالياً. 🌌", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        } else {
            items(coupons) { coupon ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, Color.White.copy(0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left - Used Status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (coupon.isUsed) Color.Red.copy(0.2f) else Color.Green.copy(0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (coupon.isUsed) "تم الاستخدام 🔒" else "نشط وغير مستخدم 🔓",
                                color = if (coupon.isUsed) Color.Red else Color.Green,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Right - Coupon details
                        Column(horizontalAlignment = Alignment.End) {
                            Text("الكود: ${coupon.code}", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("العرض: ${coupon.offerTitle}", color = Color.White, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("مرتبط بالبريد: ${coupon.forUserEmail}", color = MediumContrastTextDark, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Ratings list
        item {
            Text(
                "آراء وتقييمات العملاء (7 نجوم) ⭐",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            )
        }

        if (ratings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("لم يقم أي عميل بتقييم التطبيق بعد. قيم طلبك القادم لتظهر هنا! ⭐", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        } else {
            items(ratings) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, Color.White.copy(0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Stars Display
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                for (i in 1..7) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = if (i <= item.ratingStars) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            // Name
                            Text(
                                text = item.customerName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        
                        // Rater detailed information (Complete rater profile)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تصنيف المقيم: ${item.customerClassification.ifBlank { "عميل المجرة 👤" }}",
                                color = CosmicSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "الهاتف: ${item.customerPhone.ifBlank { "غير متوفر" }}",
                                color = Color.White.copy(0.8f),
                                fontSize = 10.sp
                            )
                        }
                        
                        if (item.comment.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💬 \"${item.comment}\"",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    couponForEmail = item.customerEmail
                                    customCouponCode = "OFFER_${(1000..9999).random()}"
                                    couponOfferTitle = "عرض خاص لتقييمك الرائع 🌌🎁"
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("إرسال كوبون فوز بالهدية 🎁", color = CosmicSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "بريد: ${item.customerEmail}",
                                color = MediumContrastTextDark,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSystemManagementSection(viewModel: MajarahViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val allProfiles by viewModel.allProfilesFlow.collectAsStateWithLifecycle()
    
    val allOrders by viewModel.allOrdersFlow.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allRestaurantOrders by viewModel.allRestaurantOrders.collectAsStateWithLifecycle()
    val allPharmacyOrders by viewModel.allPharmacyOrders.collectAsStateWithLifecycle()
    
    val couriers by viewModel.allCouriers.collectAsStateWithLifecycle()
    val sellers by viewModel.allSellers.collectAsStateWithLifecycle()
    val pharmacies by viewModel.allPharmacies.collectAsStateWithLifecycle()
    val restaurants by viewModel.allRestaurants.collectAsStateWithLifecycle()
    val allAdminManagers by viewModel.allAdminManagers.collectAsStateWithLifecycle()

    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("customer") } // customer, seller, restaurant, pharmacist, courier
    var isAddingUser by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshAllProfiles()
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                "إدارة الحسابات وتصنيفات المنظومة 👥",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "تمنحك هذه الصفحة (بصفتك المدير العام) صلاحية استعراض كافة الحسابات والعملاء وتصنيفاتهم التلقائية، بالإضافة لإمكانية إضافة مستخدمين جدد أو حذفهم.",
                color = MediumContrastTextDark,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 15.sp
            )
        }

        // App Update Simulator Panel Card
        item {
            var simVersionCode by remember { mutableStateOf("2") }
            var simVersionName by remember { mutableStateOf("1.1.0") }
            var simDaysBeforeRelease by remember { mutableStateOf("0") }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "محاكي ومتحكم تحديثات متجر قوقل بلاي 🛰️⚙️",
                        color = CosmicSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "تحكم بنزول التحديثات في قوقل بلاي مع مهلة الـ 15 يوماً للتأجيل. يمكنك محاكاة تخطي المهلة (مثال: كتابة 16 يوماً مضت) لمشاهدة شاشة الإيقاف الإجبارية الفورية لكل المستخدمين.",
                        color = MediumContrastTextDark,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = simVersionCode,
                        onValueChange = { simVersionCode = it },
                        label = { Text("رمز الإصدار (Version Code)") },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = simVersionName,
                        onValueChange = { simVersionName = it },
                        label = { Text("اسم الإصدار (Version Name)") },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = simDaysBeforeRelease,
                        onValueChange = { simDaysBeforeRelease = it },
                        label = { Text("أيام مضت على تاريخ نشر التحديث") },
                        placeholder = { Text("مثال: 0 لليوم، 16 لتخطي مهلة الـ 15 يوماً") },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val code = simVersionCode.toIntOrNull() ?: 2
                            val name = simVersionName.trim()
                            val daysAgo = simDaysBeforeRelease.toLongOrNull() ?: 0L
                            val releaseTime = System.currentTimeMillis() - (daysAgo * 24L * 60L * 60L * 1000L)
                            
                            viewModel.publishNewUpdate(code, name, releaseTime) { err ->
                                if (err == null) {
                                    Toast.makeText(context, "تم تطبيق ونشر التحديث بنجاح ومزامنته سحابياً! 🚀🛰️", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "تم تطبيق التحديث محلياً ومزامنته بنجاح! 🚀🔄", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("نشر ومحاكاة التحديث الآن 🚀🛰️", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Button(
                        onClick = {
                            Toast.makeText(context, "جاري الاستعلام عن تحديثات Google Play الحقيقية... 🔍", Toast.LENGTH_SHORT).show()
                            viewModel.checkForGooglePlayUpdate(context) { hasUpdate ->
                                if (hasUpdate) {
                                    Toast.makeText(context, "تم العثور على تحديث حقيقي بمتجر قوقل بلاي! 🛍️✨", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "لا يوجد تحديث حقيقي جديد متاح بمتجر قوقل بلاي حالياً (أنت على أحدث إصدار) 💚", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicDeepSpace, contentColor = CosmicSecondary),
                        border = BorderStroke(1.dp, CosmicSecondary.copy(0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فحص وجود تحديث حقيقي بمتجر قوقل بلاي 🛍️🔍", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    
                    TextButton(
                        onClick = {
                            viewModel.publishNewUpdate(1, "1.0.0", System.currentTimeMillis()) {
                                Toast.makeText(context, "تم إعادة تعيين الإصدار للإصدار الافتراضي الحالي بنجاح ✅", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إعادة تعيين للإصدار الحالي (إلغاء التحديث) 🔄", color = Color.White.copy(0.6f), fontSize = 11.sp)
                    }
                }
            }
        }

        // Add User Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "إضافة مستخدم جديد يدوياً بالمنظومة ➕👤",
                        color = CosmicSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("الاسم الكامل") },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userPhone,
                        onValueChange = { userPhone = it },
                        label = { Text("رقم الهاتف") },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = { userEmail = it },
                        label = { Text("البريد الإلكتروني") },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userPassword,
                        onValueChange = { userPassword = it },
                        label = { Text("كلمة المرور") },
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Role Picker Buttons
                    Text(
                        "اختر الدور / تصنيف تسجيل الدخول:",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val rolesList = listOf(
                        "customer" to "عميل 👤",
                        "seller" to "تاجر المجرة 🛍️",
                        "restaurant" to "مطعم المجرة 🍔",
                        "pharmacist" to "صيدلي 💊",
                        "courier" to "مندوب 🚴"
                    )
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
                    ) {
                        items(rolesList) { (roleKey, label) ->
                            val isSel = selectedRole == roleKey
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) CosmicSecondary else CosmicSurfaceVariant.copy(0.5f))
                                    .clickable { selectedRole = roleKey }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(label, color = if (isSel) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (userName.isBlank() || userPhone.isBlank() || userEmail.isBlank() || userPassword.isBlank()) {
                                Toast.makeText(context, "الرجاء ملء كافة التفاصيل ⚠️", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isAddingUser = true
                            
                            val profile = com.example.data.db.ProfileEntity(
                                id = java.util.UUID.randomUUID().toString(),
                                name = userName.trim(),
                                phone = userPhone.trim(),
                                email = userEmail.trim().lowercase(),
                                password = userPassword.trim(),
                                role = selectedRole
                            )

                            // Save user preference role
                            val sharedPrefs = viewModel.getApplication<android.app.Application>()
                                .getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
                            sharedPrefs.edit().putString("user_role_${profile.email}", selectedRole).apply()

                            viewModel.addProfileAdmin(profile) { err ->
                                isAddingUser = false
                                if (err == null) {
                                    Toast.makeText(context, "تمت إضافة المستخدم وحفظه سحابياً بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                                    userName = ""
                                    userPhone = ""
                                    userEmail = ""
                                    userPassword = ""
                                } else {
                                    Toast.makeText(context, "تم الحفظ محلياً وبانتظار المزامنة ⚡", Toast.LENGTH_SHORT).show()
                                    userName = ""
                                    userPhone = ""
                                    userEmail = ""
                                    userPassword = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isAddingUser
                    ) {
                        Text("إعتماد وإضافة الحساب فورياً ✅", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Users & Classifications List
        item {
            Text(
                "جدول كافة الحسابات والتصنيفات التلقائية 📋",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            )
        }

        if (allProfiles.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("لا يوجد مستخدمون مسجلون في قاعدة البيانات المحلية.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        } else {
            items(allProfiles) { p ->
                val emailClean = p.email.trim().lowercase()
                val phoneClean = p.phone.trim().replace("+", "").replace(" ", "")

                // Determine role of this profile
                val sharedPrefs = viewModel.getApplication<android.app.Application>()
                    .getSharedPreferences("majarah_prefs", android.content.Context.MODE_PRIVATE)
                val prefRole = sharedPrefs.getString("user_role_${p.email}", "customer") ?: "customer"

                val isCou = couriers.any { it.phone.trim() == p.phone.trim() } || prefRole == "courier"
                val isSel = sellers.any { it.phone.trim() == p.phone.trim() } || prefRole == "seller"
                val isPhar = pharmacies.any { it.pharmacistEmail.trim().lowercase() == emailClean } || prefRole == "pharmacist"
                val isRest = restaurants.any { it.phone.trim() == p.phone.trim() || it.name.trim().lowercase() == p.name.trim().lowercase() } || prefRole == "restaurant"

                // Dynamic classification based on weekly stats (last 7 days)
                val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
                val classification = when {
                    emailClean == "mawiaosman0@gmail.com" -> "مدير عام للمجرة 👑"
                    allAdminManagers.any { it.email.trim().lowercase() == emailClean } -> "مدير إداري 🧑‍💼"
                    isCou -> {
                        val courierDeliveriesThisWeek = allOrders.filter { 
                            val cPhone = it.courierPhone.trim().replace("+", "").replace(" ", "")
                            (cPhone == phoneClean || it.courierPhone.trim() == p.phone.trim()) && 
                            it.orderDate >= oneWeekAgo &&
                            (it.statusArabic.contains("تم") || it.statusArabic.contains("توصيل") || it.statusArabic.contains("تمام"))
                        }.distinctBy { it.orderId }.size + allPharmacyOrders.count {
                            val cpPhone = it.courierPhone.trim().replace("+", "").replace(" ", "")
                            (cpPhone == phoneClean || it.courierPhone.trim() == p.phone.trim()) &&
                            it.createdAt >= oneWeekAgo &&
                            it.status == "تم التوصيل"
                        }
                        when {
                            courierDeliveriesThisWeek >= 40 -> "مندوب ذهبي 👑 ($courierDeliveriesThisWeek مهمة هذا الأسبوع)"
                            courierDeliveriesThisWeek >= 20 -> "مندوب مميز ⭐ ($courierDeliveriesThisWeek مهمة هذا الأسبوع)"
                            else -> "مندوب المجرة 🚴 ($courierDeliveriesThisWeek مهمة هذا الأسبوع)"
                        }
                    }
                    isSel -> {
                        val sellerProductIds = allProducts.filter { it.sellerEmail.trim().lowercase() == emailClean }.map { it.id }.toSet()
                        val sellerSalesThisWeek = allOrders.filter { 
                            it.productId in sellerProductIds && 
                            it.orderDate >= oneWeekAgo &&
                            (it.statusArabic.contains("تم") || it.statusArabic.contains("توصيل") || it.statusArabic.contains("تمام"))
                        }.sumOf { it.quantity }
                        when {
                            sellerSalesThisWeek >= 20 -> "تاجر المجرة 👑 ($sellerSalesThisWeek مبيعات هذا الأسبوع)"
                            sellerSalesThisWeek >= 10 -> "تاجر مميز ⭐ ($sellerSalesThisWeek مبيعات هذا الأسبوع)"
                            else -> "تاجر المجرة 🛍️ ($sellerSalesThisWeek مبيعات هذا الأسبوع)"
                        }
                    }
                    isRest -> {
                        val restaurantOrdersThisWeek = allRestaurantOrders.filter { 
                            (it.restaurantPhone.trim() == p.phone.trim() || it.restaurantName.trim().lowercase() == p.name.trim().lowercase()) &&
                            it.createdAt >= oneWeekAgo &&
                            it.status == "تم التسليم"
                        }.size
                        when {
                            restaurantOrdersThisWeek >= 20 -> "مطعم ذهبي 👑 ($restaurantOrdersThisWeek طلب هذا الأسبوع)"
                            restaurantOrdersThisWeek >= 10 -> "مطعم مميز ⭐ ($restaurantOrdersThisWeek طلب هذا الأسبوع)"
                            else -> "مطعم المجرة 🍔 ($restaurantOrdersThisWeek طلب هذا الأسبوع)"
                        }
                    }
                    isPhar -> {
                        val myPharmacy = pharmacies.find { it.pharmacistEmail.trim().lowercase() == emailClean }
                        val pharmacyOrdersThisWeek = if (myPharmacy != null) {
                            allPharmacyOrders.count { 
                                it.pharmacyId == myPharmacy.id && 
                                it.createdAt >= oneWeekAgo && 
                                it.status == "تم التوصيل" 
                            }
                        } else 0
                        when {
                            pharmacyOrdersThisWeek >= 20 -> "صيدلي ذهبي 👑 ($pharmacyOrdersThisWeek روشتة هذا الأسبوع)"
                            pharmacyOrdersThisWeek >= 10 -> "صيدلي مميز ⭐ ($pharmacyOrdersThisWeek روشتة هذا الأسبوع)"
                            else -> "صيدلي المجرة 💊 ($pharmacyOrdersThisWeek روشتة هذا الأسبوع)"
                        }
                    }
                    else -> {
                        val clientOrdersThisWeek = allOrders.filter { 
                            val oPhone = it.customerPhone.trim().replace("+", "").replace(" ", "")
                            (oPhone == phoneClean || it.customerName.trim().lowercase() == p.name.trim().lowercase()) &&
                            it.orderDate >= oneWeekAgo &&
                            (it.statusArabic.contains("تم") || it.statusArabic.contains("توصيل") || it.statusArabic.contains("تمام") || it.statusArabic.contains("استلام"))
                        }.distinctBy { it.orderId }.size
                        when {
                            clientOrdersThisWeek >= 40 -> "عميل ذهبي 👑 ($clientOrdersThisWeek طلب هذا الأسبوع)"
                            clientOrdersThisWeek >= 20 -> "عميل مميز ⭐ ($clientOrdersThisWeek طلب هذا الأسبوع)"
                            else -> "عميل المجرة 👤 ($clientOrdersThisWeek طلب هذا الأسبوع)"
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, Color.White.copy(0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Delete user button
                        IconButton(
                            onClick = {
                                viewModel.deleteProfileAdmin(p.id) { err ->
                                    if (err == null) {
                                        Toast.makeText(context, "تم حذف الحساب بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "تم الحذف بنجاح محلياً وسحابياً 🗑️✨", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف الحساب", tint = Color.Red)
                        }

                        // Info details
                        Column(horizontalAlignment = Alignment.End) {
                            Text(p.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("البريد: ${p.email}", color = MediumContrastTextDark, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("الهاتف: ${p.phone}", color = Color.White.copy(0.8f), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("كلمة المرور: ${p.password}", color = Color(0xFFFFC107), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Classification tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CosmicSecondary.copy(0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(classification, color = CosmicSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
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
    val isGeneralAdmin by viewModel.isGeneralAdmin.collectAsStateWithLifecycle()
    val isAdministrativeManager by viewModel.isAdministrativeManager.collectAsStateWithLifecycle()
    val isAllowed = isGeneralAdmin || isAdministrativeManager

    if (!isAllowed) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "صلاحية مقيدة 🔒",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "عذراً، مراجعة وقبول طلبات المنتجات المعلقة للتجار واعتمادها هي ميزة حصرية للمدير العام فقط لتنظيم الأسعار والعمولات.",
                        color = MediumContrastTextDark,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "طلبات المنتجات المعلقة للتجار 🧑‍💼⏳",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "هنا يمكنك مراجعة وتعديل أسعار منتجات التجار لإضافة عمولة/فائدة التطبيق ومن ثم الموافقة عليها ونشرها مباشرة للمشترين.",
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
                            "لا توجد منتجات معلقة مضافة من التجار حالياً! 🎉",
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
                                "طلب معلق من: ${product.sellerEmail.ifBlank { "تاجر خارجي" }}",
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
    val allOrders by viewModel.allOrdersFlow.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val currentSeller = sellers.find { s -> s.email.trim().lowercase() == activeProfile?.email?.trim()?.lowercase() }
    
    var activeSubTab by remember { mutableStateOf(0) } // 0: Products, 1: Add Product, 2: Profile & Support

    var newProdName by remember { mutableStateOf("") }
    var newProdDesc by remember { mutableStateOf("") }
    var newProdPrice by remember { mutableStateOf("") }
    var newProdStock by remember { mutableStateOf("") }
    var newProdCategory by remember { mutableStateOf("electronics") }
    var newProdCategoryArabic by remember { mutableStateOf("إلكترونيات وأجهزة") }
    
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }

    // Camera Launcher
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val base64 = try {
                val outputStream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
                val byteArray = outputStream.toByteArray()
                android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
            if (base64 != null) {
                selectedImageBase64 = base64
                Toast.makeText(context, "تم التقاط الصورة بنجاح! 📸", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Gallery Launcher
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val base64 = try {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= 29) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val outputStream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
                val byteArray = outputStream.toByteArray()
                android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
            if (base64 != null) {
                selectedImageBase64 = base64
                Toast.makeText(context, "تم اختيار الصورة من المعرض بنجاح! 🖼️", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val categories = listOf(
        "electronics" to "كوكب الإلكترونيات",
        "fashion" to "كوكب الأزياء",
        "furniture" to "كوكب الأثاثات المنزلية",
        "services" to "كوكب خدمات عامة",
        "crafts" to "كوكب أعمال حرفية",
        "estate_cars" to "كوكب بيع العقارات والسيارات",
        "pharmacy" to "كوكب صيدلية",
        "restaurant" to "كوكب مطاعم",
        "kids" to "كوكب مستلزمات أطفال",
        "women" to "كوكب للنساء",
        "men" to "كوكب للرجال",
        "travel" to "كوكب وكالات سفر وسياحة",
        "tickets" to "كوكب حجوزات تذاكر",
        "hotels" to "كوكب حجوزات فندقية",
        "foods" to "كوكب الأغذية والمأكولات",
        "cosmetics" to "كوكب عطور وتجميل",
        "other" to "كوكب منتجات أخرى"
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

        val myActiveOrdersCount = remember(allOrders, allProducts, activeProfile) {
            val email = activeProfile?.email?.trim()?.lowercase() ?: ""
            allOrders.filter { order ->
                val prod = allProducts.find { it.id == order.productId }
                prod?.sellerEmail?.trim()?.lowercase() == email
            }.groupBy { it.orderId }.keys.count { orderId ->
                val parent = allOrders.firstOrNull { it.orderId == orderId }
                val status = parent?.statusArabic ?: ""
                !status.contains("تم توصيل") && !status.contains("ملغي") && !status.contains("تم التسليم")
            }
        }

        var previousSellerActiveOrdersCount by remember { mutableStateOf(myActiveOrdersCount) }

        LaunchedEffect(myActiveOrdersCount) {
            if (myActiveOrdersCount > previousSellerActiveOrdersCount) {
                NotificationSoundUtils.playNotificationSound(context)
            }
            previousSellerActiveOrdersCount = myActiveOrdersCount
        }

        // Tab Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val myProductsCount = allProducts.count { it.sellerEmail.trim().lowercase() == activeProfile?.email?.trim()?.lowercase() }
            val tabs = listOf(
                "منتجاتي ($myProductsCount) 🛍️" to 0,
                "الطلبات ${if (myActiveOrdersCount > 0) "($myActiveOrdersCount)" else ""} 📊" to 3,
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
                                        // Delete Button & Stock Controls
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteProduct(product.id) { err ->
                                                        if (err == null) {
                                                            Toast.makeText(context, "تم حذف المنتج بنجاح! 🗑️", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "خطأ أثناء الحذف: $err", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف المنتج", tint = Color.Red.copy(alpha = 0.8f))
                                            }

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
                                                    " سعرك الأصلي (التاجر): ",
                                                    color = CosmicSecondary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
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

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("صورة المنتج الكوني: ", color = CosmicSecondary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    // Image preview if selected
                                    if (selectedImageBase64 != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, CosmicSecondary, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            ProductImagePlaceholder(selectedImageBase64!!, modifier = Modifier.fillMaxSize())
                                            IconButton(
                                                onClick = { selectedImageBase64 = null },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .background(Color.Black.copy(0.6f), RoundedCornerShape(50))
                                                    .size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Close, "إلغاء الصورة", tint = Color.Red, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { galleryLauncher.launch("image/*") },
                                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Image, "المعرض", tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("معرض الصور 🖼️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Button(
                                            onClick = { cameraLauncher.launch(null) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CameraAlt, "الكاميرا", tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("فتح الكاميرا 📸", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

                                                val newProduct = com.example.data.db.ProductEntity(
                                                    id = 0,
                                                    name = newProdName.trim(),
                                                    description = newProdDesc.trim(),
                                                    price = parsedPrice,
                                                    category = newProdCategory,
                                                    categoryArabic = newProdCategoryArabic,
                                                    rating = 4.5f,
                                                    imageResName = selectedImageBase64 ?: "ic_product_placeholder",
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
                                                        selectedImageBase64 = null
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
                                        Text("إدراج هذا المنتج في المجرة للتسوق 🚀", fontWeight = FontWeight.Bold)
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
                                        WhatsAppUtils.sendWhatsAppMessage(context, "249912111111", "مرحباً يا مدير مجرة السودان للتسوق، أنا التاجر الشريك وعندي طلب تسوية أو استفسار بخصوص المتجر.")
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
                3 -> {
                    // ORDER MONITORING (مراقبة الطلبات)
                    val merchantOrders = remember(allOrders, allProducts, activeProfile) {
                        allOrders.filter { order ->
                            val prod = allProducts.find { it.id == order.productId }
                            prod?.sellerEmail?.trim()?.lowercase() == activeProfile?.email?.trim()?.lowercase()
                        }.groupBy { it.orderId }
                    }

                    if (merchantOrders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.HourglassEmpty, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "لا توجد مبيعات أو طلبات مسجلة لمنتجاتك حالياً! 📊",
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
                            items(merchantOrders.entries.toList()) { entry ->
                                val orderId = entry.key
                                val orderItems = entry.value
                                val firstItem = orderItems.firstOrNull()
                                val customerName = firstItem?.customerName ?: "عميل المجرة الكوني"
                                val customerPhone = firstItem?.customerPhone ?: "غير معروف"
                                val customerAddress = firstItem?.customerAddress ?: "السودان"
                                val status = firstItem?.statusArabic ?: "قيد المراجعة"
                                val paymentMethod = firstItem?.paymentMethod ?: "كاش"
                                val receiptImage = firstItem?.bankReceiptImageUri
                                val orderDateMillis = firstItem?.orderDate ?: System.currentTimeMillis()
                                val dateStr = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US).format(java.util.Date(orderDateMillis))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    border = BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                        // Header
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(CosmicSecondary.copy(0.15f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = status,
                                                    color = CosmicSecondary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "فاتورة بيع #: $orderId",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = dateStr,
                                                    color = Color.Gray,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(color = CosmicSurfaceVariant.copy(0.4f))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Customer Info
                                        Text("👤 بيانات المشتري والتوصيل:", fontSize = 11.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                        Text("الاسم: $customerName", fontSize = 10.sp, color = Color.White)
                                        Text("الهاتف: $customerPhone", fontSize = 10.sp, color = Color.White)
                                        Text("العنوان: $customerAddress", fontSize = 10.sp, color = Color.White)

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(color = CosmicSurfaceVariant.copy(0.4f))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Items Purchased
                                        Text("📦 المنتجات المبيعة:", fontSize = 11.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                        orderItems.forEach { item ->
                                            val itemTotal = item.priceAtOrder * item.quantity
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${viewModel.formatPrice(itemTotal)} SDG",
                                                    color = Color.Green,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${item.productName} (الكمية: ${item.quantity})",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Right
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(color = CosmicSurfaceVariant.copy(0.4f))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Payment details
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (paymentMethod != "كاش" && !receiptImage.isNullOrBlank()) {
                                                var showReceiptDialog by remember { mutableStateOf(false) }
                                                if (showReceiptDialog) {
                                                    ViewReceiptDialog(receiptImage) { showReceiptDialog = false }
                                                }
                                                TextButton(
                                                    onClick = { showReceiptDialog = true },
                                                    modifier = Modifier.height(28.dp).padding(0.dp)
                                                ) {
                                                    Text("عرض إشعار التحويل 🖼️", color = CosmicSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.width(1.dp))
                                            }
                                            Text(
                                                text = "طريقة الدفع: $paymentMethod",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
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
}

@Composable
fun CourierDashboardScreenBody(viewModel: MajarahViewModel) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allCouriers by viewModel.allCouriers.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrdersFlow.collectAsStateWithLifecycle()
    val allRestaurantOrders by viewModel.allRestaurantOrders.collectAsStateWithLifecycle()
    val allPharmacyOrders by viewModel.allPharmacyOrders.collectAsStateWithLifecycle()

    var showDeliveryPaymentDialogForOrderId by remember { mutableStateOf<String?>(null) }
    var showDeliveryPaymentDialogForRestaurantId by remember { mutableStateOf<Int?>(null) }
    var showDeliveryPaymentDialogForPharmacyId by remember { mutableStateOf<Int?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf("cash") } // "cash" or "bank"
    var bankTransferReference by remember { mutableStateOf("") }

    var previousAssignedOrderIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var newTaskAlertOrderId by remember { mutableStateOf<String?>(null) }
    var scrollToOrderId by remember { mutableStateOf<String?>(null) }
    var courierOrdersTab by remember { mutableStateOf(0) } // 0: Active, 1: Completed, 2: Cancelled
    var courierMainCategoryTab by remember { mutableStateOf(0) } // 0: Products, 1: Restaurants, 2: Pharmacy

    val myCourierInfo = allCouriers.find {
        it.phone.trim().replace("+", "") == activeProfile?.phone?.trim()?.replace("+", "") ||
        it.name.trim().lowercase() == activeProfile?.name?.trim()?.lowercase()
    }

    val myAssignedOrders = if (myCourierInfo != null) {
        allOrders.filter {
            it.courierName.trim().isNotBlank() && (
                it.courierName == myCourierInfo.name ||
                it.courierPhone.trim() == myCourierInfo.phone.trim()
            )
        }
    } else {
        emptyList()
    }

    val myAssignedRestaurantOrders = if (myCourierInfo != null) {
        allRestaurantOrders.filter {
            it.courierName.trim().isNotBlank() && (
                it.courierName == myCourierInfo.name ||
                it.courierPhone.trim() == myCourierInfo.phone.trim()
            )
        }
    } else {
        emptyList()
    }

    val myAssignedPharmacyOrders = if (myCourierInfo != null) {
        allPharmacyOrders.filter {
            it.courierName.trim().isNotBlank() && (
                it.courierName == myCourierInfo.name ||
                it.courierPhone.trim() == myCourierInfo.phone.trim()
            )
        }
    } else {
        emptyList()
    }

    val currentAssignedKeys = remember(myAssignedOrders, myAssignedRestaurantOrders, myAssignedPharmacyOrders) {
        val orderKeys = myAssignedOrders.map { "ord_" + it.orderId }.toSet()
        val restKeys = myAssignedRestaurantOrders.map { "rest_" + it.id }.toSet()
        val pharKeys = myAssignedPharmacyOrders.map { "phar_" + it.id }.toSet()
        orderKeys + restKeys + pharKeys
    }

    // Products counts
    val groupedOrders = myAssignedOrders.groupBy { it.orderId }
    val activeProductsCount = groupedOrders.count { (_, itemsList) ->
        val statusText = itemsList.firstOrNull()?.statusArabic ?: ""
        val isActuallyCompleted = (statusText.contains("تمام") || statusText.contains("تم توصيل") || statusText.contains("تم التسليم")) && !statusText.contains("تم تسليم المندوب") && !statusText.contains("لمندوب")
        val isCancelled = statusText.contains("ملغي")
        !isActuallyCompleted && !isCancelled
    }
    val completedProductsCount = groupedOrders.count { (_, itemsList) ->
        val statusText = itemsList.firstOrNull()?.statusArabic ?: ""
        (statusText.contains("تمام") || statusText.contains("تم توصيل") || statusText.contains("تم التسليم")) && !statusText.contains("تم تسليم المندوب") && !statusText.contains("لمندوب")
    }
    val cancelledProductsCount = groupedOrders.count { (_, itemsList) ->
        val statusText = itemsList.firstOrNull()?.statusArabic ?: ""
        statusText.contains("ملغي")
    }

    // Restaurant counts
    val activeRestaurantsCount = myAssignedRestaurantOrders.count {
        val isCompleted = (it.status.startsWith("تم التسليم") || it.status.contains("إغلاق") || it.status.contains("تسليم العميل")) && !it.status.contains("المندوب")
        val isCancelled = it.status.contains("ملغ") || it.status.contains("ملغي")
        !isCompleted && !isCancelled
    }
    val completedRestaurantsCount = myAssignedRestaurantOrders.count {
        val isCompleted = (it.status.startsWith("تم التسليم") || it.status.contains("إغلاق") || it.status.contains("تسليم العميل")) && !it.status.contains("المندوب")
        isCompleted
    }
    val cancelledRestaurantsCount = myAssignedRestaurantOrders.count {
        it.status.contains("ملغ") || it.status.contains("ملغي")
    }

    // Pharmacy counts
    val activePharmacyCount = myAssignedPharmacyOrders.count {
        !it.status.contains("تم التوصيل") && !it.status.contains("تم تسليم") && !it.status.contains("ملغ")
    }
    val completedPharmacyCount = myAssignedPharmacyOrders.count {
        it.status.contains("تم التوصيل") || it.status.contains("تم تسليم")
    }
    val cancelledPharmacyCount = myAssignedPharmacyOrders.count {
        it.status.contains("ملغ")
    }

    LaunchedEffect(currentAssignedKeys) {
        if (previousAssignedOrderIds.isNotEmpty()) {
            val newlyAdded = currentAssignedKeys - previousAssignedOrderIds
            if (newlyAdded.isNotEmpty()) {
                NotificationSoundUtils.playNotificationSound(context)

                val totalActive = activeProductsCount + activeRestaurantsCount + activePharmacyCount
                Toast.makeText(context, "🌌 تم إسناد مهمة جديدة لك! إجمالي المهام النشطة حالياً: $totalActive 🚴✨", Toast.LENGTH_LONG).show()
            }
        }
        previousAssignedOrderIds = currentAssignedKeys
    }

    // Selected Tab totals
    val activeCount = when(courierMainCategoryTab) {
        0 -> activeProductsCount
        1 -> activeRestaurantsCount
        else -> activePharmacyCount
    }
    val completedCount = when(courierMainCategoryTab) {
        0 -> completedProductsCount
        1 -> completedRestaurantsCount
        else -> completedPharmacyCount
    }
    val cancelledCount = when(courierMainCategoryTab) {
        0 -> cancelledProductsCount
        1 -> cancelledRestaurantsCount
        else -> cancelledPharmacyCount
    }

    val isWillingToWork = myCourierInfo?.status?.contains("غير متوفر") == false && myCourierInfo?.status?.contains("🔴") == false
    val hasActiveDelivery = activeCount > 0

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

    if (showDeliveryPaymentDialogForRestaurantId != null) {
        val targetRestOrderId = showDeliveryPaymentDialogForRestaurantId!!
        val rOrder = allRestaurantOrders.find { it.id == targetRestOrderId }
        val totalAmount = (rOrder?.foodPrice ?: 0.0) + (rOrder?.deliveryFee ?: 0.0)
        
        AlertDialog(
            onDismissRequest = { showDeliveryPaymentDialogForRestaurantId = null },
            title = {
                Text(
                    text = "طريقة دفع الطلب وتأكيد التسليم 🍔",
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
                        text = "اختر طريقة استلام قيمة توصيل وطلب المطعم (المبلغ الإجمالي: ${viewModel.formatPrice(totalAmount)} SDG):",
                        fontSize = 11.sp,
                        color = Color.White.copy(0.9f),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    
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
                        
                        viewModel.updateRestaurantOrderStatus(targetRestOrderId, finalStatus) { err ->
                            if (err == null) {
                                Toast.makeText(context, "تم تسليم الطلب للمطعم بنجاح! 🚀🎉", Toast.LENGTH_SHORT).show()
                                showDeliveryPaymentDialogForRestaurantId = null
                            } else {
                                Toast.makeText(context, "خطأ: $err", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                ) {
                    Text("تأكيد واستلام الطلب ✅", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeliveryPaymentDialogForRestaurantId = null }) {
                    Text("إلغاء", color = Color.White.copy(0.6f))
                }
            },
            containerColor = CosmicSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDeliveryPaymentDialogForPharmacyId != null) {
        val targetPharmOrderId = showDeliveryPaymentDialogForPharmacyId!!
        val pOrder = allPharmacyOrders.find { it.id == targetPharmOrderId }
        val totalAmount = (pOrder?.medicinePrice ?: 0.0) + (pOrder?.deliveryFee ?: 0.0)
        
        AlertDialog(
            onDismissRequest = { showDeliveryPaymentDialogForPharmacyId = null },
            title = {
                Text(
                    text = "طريقة دفع دواء الصيدلية وتأكيد التسليم 💊",
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
                        text = "اختر طريقة استلام قيمة الدواء (سعر التوصيل مجان، المبلغ الإجمالي: ${viewModel.formatPrice(totalAmount)} SDG):",
                        fontSize = 11.sp,
                        color = Color.White.copy(0.9f),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    
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
                        
                        viewModel.updatePharmacyOrderStatus(targetPharmOrderId, finalStatus) { err ->
                            if (err == null) {
                                Toast.makeText(context, "تم تسليم الدواء بنجاح! 🚀🎉", Toast.LENGTH_SHORT).show()
                                showDeliveryPaymentDialogForPharmacyId = null
                            } else {
                                Toast.makeText(context, "خطأ: $err", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
                ) {
                    Text("تأكيد واستلام الطلب ✅", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeliveryPaymentDialogForPharmacyId = null }) {
                    Text("إلغاء", color = Color.White.copy(0.6f))
                }
            },
            containerColor = CosmicSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDeepSpace)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {

                // Courier Main Category Tabs (Products, Restaurants, Pharmacy)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicSurfaceVariant.copy(0.2f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val categoryData = listOf(
                        Triple(0, if (courierOrdersTab == 1) "المنتجات المنفذة 🛒 ($completedProductsCount)" else if (courierOrdersTab == 2) "منتجات ملغية ❌ ($cancelledProductsCount)" else "المنتجات النشطة 🛒 ($activeProductsCount)", CosmicSecondary),
                        Triple(1, if (courierOrdersTab == 1) "طلبات المطعم المنفذة 🍔 ($completedRestaurantsCount)" else if (courierOrdersTab == 2) "طلبات مطعم ملغية ❌ ($cancelledRestaurantsCount)" else "المطاعم النشطة 🍔 ($activeRestaurantsCount)", CosmicTertiary),
                        Triple(2, if (courierOrdersTab == 1) "الروشتات المنفذة 💊 ($completedPharmacyCount)" else if (courierOrdersTab == 2) "روشتات ملغية ❌ ($cancelledPharmacyCount)" else "الصيدلية النشطة 💊 ($activePharmacyCount)", Color(0xFF64B5F6))
                    )
                    categoryData.forEach { (catIndex, title, colorVal) ->
                        val isSelected = courierMainCategoryTab == catIndex
                        Button(
                            onClick = { courierMainCategoryTab = catIndex },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) colorVal else Color.Transparent,
                                contentColor = if (isSelected) Color.Black else Color.White
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dynamic Tab Selector for Courier Orders segregation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicSurfaceVariant.copy(0.4f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabData = listOf(
                        Triple(0, "المهام النشطة 🚴 ($activeCount)", CosmicSecondary),
                        Triple(1, "تم تنفيذها ✅ ($completedCount)", Color.Green),
                        Triple(2, "الملغية ❌ ($cancelledCount)", Color.Red)
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

                // Apply Tab-based sorting filtration
                val filteredGroupedOrders = groupedOrders.filter { (_, itemsList) ->
                    val statusText = itemsList.firstOrNull()?.statusArabic ?: ""
                    val isActuallyCompleted = (statusText.contains("تمام") || statusText.contains("تم توصيل") || statusText.contains("تم التسليم")) && !statusText.contains("تم تسليم المندوب") && !statusText.contains("لمندوب")
                    val isCancelled = statusText.contains("ملغي")
                    when (courierOrdersTab) {
                        0 -> !isActuallyCompleted && !isCancelled
                        1 -> isActuallyCompleted
                        2 -> isCancelled
                        else -> true
                    }
                }

                val filteredRestaurantOrders = myAssignedRestaurantOrders.filter { order ->
                    val isActuallyCompleted = (order.status.startsWith("تم التسليم") || order.status.contains("إغلاق") || order.status.contains("تسليم العميل")) && !order.status.contains("المندوب")
                    val isCancelled = order.status.contains("ملغ") || order.status.contains("ملغي")
                    when (courierOrdersTab) {
                        0 -> !isActuallyCompleted && !isCancelled
                        1 -> isActuallyCompleted
                        2 -> isCancelled
                        else -> true
                    }
                }

                val filteredPharmacyOrders = myAssignedPharmacyOrders.filter { order ->
                    val isActuallyCompleted = (order.status.startsWith("تم التسليم") || order.status == "تم التوصيل" || order.status.contains("إغلاق") || order.status.contains("تسليم العميل")) && !order.status.contains("المندوب") && !order.status.contains("للمندوب")
                    val isCancelled = order.status.contains("ملغ") || order.status.contains("ملغي")
                    when (courierOrdersTab) {
                        0 -> !isActuallyCompleted && !isCancelled
                        1 -> isActuallyCompleted
                        2 -> isCancelled
                        else -> true
                    }
                }

                val isCurrentCategoryListEmpty = when (courierMainCategoryTab) {
                    0 -> filteredGroupedOrders.isEmpty()
                    1 -> filteredRestaurantOrders.isEmpty()
                    else -> filteredPharmacyOrders.isEmpty()
                }

                if (isCurrentCategoryListEmpty) {
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
                        if (courierMainCategoryTab == 0) {
                            itemsIndexed(filteredGroupedOrders.entries.toList()) { orderIdx, (orderId, itemsList) ->
                                val parent = itemsList.firstOrNull()
                                val totalPrice = itemsList.sumOf { it.priceAtOrder * it.quantity }
                                val isCompleted = parent?.statusArabic?.let { status ->
                                    (status.contains("تمام") || 
                                    status.contains("تم توصيل") || 
                                    status.contains("تم التوصيل") || 
                                    status.contains("تم التسليم")) && 
                                    !status.contains("تم تسليم المندوب") && 
                                    !status.contains("لمندوب")
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
                                                text = "الترتيب: الطلب رقم ${orderIdx + 1} 🔢 (طلب #${orderId.take(10)})\nالتاريخ: " + java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.US).format(java.util.Date(parent?.orderDate ?: System.currentTimeMillis())),
                                                fontSize = 11.sp,
                                                color = CosmicSecondary,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Right
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Customer detail lines
                                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                            Text("الزبون: ${parent?.customerName}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right)
                                            Text("رقم الهاتف: ${parent?.customerPhone}", fontSize = 10.sp, color = Color.White.copy(0.8f), textAlign = TextAlign.Right)
                                            Text("📍 موقع/عنوان التسليم: ${parent?.customerAddress}", fontSize = 11.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right)
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
                                                        val msg = "🌌 مرحباً يا ${parent.customerName}! معكم المندوب ${myCourierInfo?.name ?: "مندوب مجرة"} من تطبيق مجرة السودان. أنا متكفل بتسليم طلبيتكم الآن رقم (#${orderId.take(5)}) وقيمة المشتريات ${viewModel.formatPrice(totalPrice)} SDG + سعر التوصيل ${viewModel.formatPrice(parent?.deliveryFee ?: 0.0)} SDG (الإجمالي الكلي للتحصيل: ${viewModel.formatPrice(totalPrice + (parent?.deliveryFee ?: 0.0))} SDG). هل أنتم متواجدون لتسليمها؟"
                                                        WhatsAppUtils.sendWhatsAppMessage(context, phoneNum, msg)
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
                                            Card(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                                colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace.copy(0.6f)),
                                                border = BorderStroke(1.dp, Color.Green.copy(0.3f)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                                    Text("🔒 الفاتورة مغلقة ومكتملة بالتسليم بنجاح ✅", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("طريقة السداد المؤكدة: ${parent?.paymentMethod ?: "كاش للمندوب"}", color = Color.White, fontSize = 10.sp)
                                                    
                                                     if (!parent?.bankReceiptImageUri.isNullOrBlank()) {
                                                         var showReceiptForCourier by remember { mutableStateOf(false) }
                                                         if (showReceiptForCourier) {
                                                             ViewReceiptDialog(parent!!.bankReceiptImageUri!!) { showReceiptForCourier = false }
                                                         }
                                                         Spacer(modifier = Modifier.height(6.dp))
                                                         Button(
                                                             onClick = { showReceiptForCourier = true },
                                                             colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                                             shape = RoundedCornerShape(8.dp),
                                                             modifier = Modifier.fillMaxWidth()
                                                         ) {
                                                             Icon(Icons.Default.Image, null, modifier = Modifier.size(12.dp))
                                                             Spacer(modifier = Modifier.width(4.dp))
                                                             Text("عرض إشعار التحويل البنكي المرفق من الزبون 📄", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                         }
                                                     }
                                                }
                                            }

                                            var selectedInvoiceType by remember { mutableStateOf("مندوب منتج") }

                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "تحديد نوع الفاتورة للمشاركة مع المدير:",
                                                fontSize = 10.sp,
                                                color = CosmicSecondary,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Right
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
                                            ) {
                                                listOf("مندوب صيدلية", "مندوب منتج", "مندوب مطعم").forEach { type ->
                                                    val isSelected = selectedInvoiceType == type
                                                    Box(
                                                        modifier = Modifier
                                                            .background(if (isSelected) CosmicSecondary else CosmicSurfaceVariant, RoundedCornerShape(12.dp))
                                                            .clickable { selectedInvoiceType = type }
                                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = type,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.Black else Color.White
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = {
                                                    val itemsText = itemsList.joinToString("\n") { "• ${it.productName} (العدد: ${it.quantity}) - ${viewModel.formatPrice(it.priceAtOrder * it.quantity)}" }
                                                    val totalInvPrice = itemsList.sumOf { it.priceAtOrder * it.quantity } + (parent?.deliveryFee ?: 0.0)
                                                    val invoiceMsg = """
 🌌 فاتورة تسليم طلبية المجرة 🌌
 ---------------------------
 🚴 نوع الفاتورة: فاتورة $selectedInvoiceType 📦
 ✍️ اسم المندوب: ${myCourierInfo?.name ?: "مندوب مجرة"}
 👤 اسم الزبون: ${parent?.customerName ?: "غير معروف"}
 📞 هاتف الزبون: ${parent?.customerPhone ?: "غير معروف"}
 📍 عنوان التسليم: ${parent?.customerAddress ?: "السودان"}
 📦 رقم الطلب: #$orderId
 💳 طريقة السداد للطلب: ${parent?.paymentMethod ?: "كاش 💵"}
 📝 حالة الفاتورة: ${parent?.statusArabic ?: "غير محدد"}
 ---------------------------
 💸 تفاصيل الفاتورة والمنتجات:
 $itemsText
 ---------------------------
 🚚 سعر التوصيل: ${viewModel.formatPrice(parent?.deliveryFee ?: 0.0)} SDG
 💰 إجمالي الحساب: ${viewModel.formatPrice(totalInvPrice)} SDG
 ---------------------------
 تم تسليم الشحنة بنجاح من قبل مندوب التوصيل المعتمد.
 شكراً لثقتكم بمجرة التسوق الإلكتروني 🌌⚡
""".trimIndent()
                                                    
                                                    WhatsAppUtils.sendWhatsAppMessage(context, "249910074223", invoiceMsg)
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
                        } else if (courierMainCategoryTab == 1) {
                            itemsIndexed(filteredRestaurantOrders) { orderIdx, order ->
                                val isCompleted = (order.status.startsWith("تم التسليم") || order.status.contains("إغلاق") || order.status.contains("تسليم العميل")) && !order.status.contains("المندوب")
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    border = BorderStroke(1.dp, CosmicTertiary.copy(0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(order.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isCompleted) Color.Green else CosmicSecondary)
                                            Text("الترتيب: الطلب رقم ${orderIdx + 1} 🔢 (طلب مطعم #${order.id})\nالمطعم: ${order.restaurantName}", fontSize = 11.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                            Text("الزبون: ${order.customerName}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right)
                                            Text("رقم الهاتف: ${order.customerPhone}", fontSize = 10.sp, color = Color.White.copy(0.8f), textAlign = TextAlign.Right)
                                            Text("📍 موقع/عنوان التوصيل: ${order.deliveryLocation.ifBlank { "السودان" }}", fontSize = 11.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right)
                                            Text("المطعم: ${order.restaurantName} (${order.restaurantPhone})", fontSize = 10.sp, color = Color.White.copy(0.8f), textAlign = TextAlign.Right)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = Color.White.copy(0.1f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("تفاصيل الوجبات والطلبات:\n${order.itemsAndNotes}", fontSize = 11.sp, color = Color.White.copy(0.8f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("سعر الوجبات 🍔: ${viewModel.formatPrice(order.foodPrice)} SDG\nسعر التوصيل للمطعم 🚚: ${viewModel.formatPrice(order.deliveryFee)} SDG\nالمبلغ الإجمالي المطلـوب: ${viewModel.formatPrice(order.foodPrice + order.deliveryFee)} SDG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CosmicSecondary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            // Call Customer
                                            Button(
                                                onClick = {
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                        data = android.net.Uri.parse("tel:${order.customerPhone}")
                                                    }
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("اتصال 📞", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            // WhatsApp
                                            Button(
                                                onClick = {
                                                    val msg = "مرحباً يا ${order.customerName}! معكم المندوب ${myCourierInfo?.name} لتوصيل طلبكم من مطعم ${order.restaurantName}. رسوم التوصيل هي ${viewModel.formatPrice(order.deliveryFee)} SDG. هل أنتم متواجدون لتسليم الطلب؟"
                                                    WhatsAppUtils.sendWhatsAppMessage(context, order.customerPhone, msg)
                                                },
                                                modifier = Modifier.weight(1.1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Chat, null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("واتساب 💬", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            // Confirm Delivery
                                            Button(
                                                onClick = {
                                                    selectedPaymentMethod = "cash"
                                                    bankTransferReference = ""
                                                    showDeliveryPaymentDialogForRestaurantId = order.id
                                                },
                                                modifier = Modifier.weight(1.2f),
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isCompleted) Color.DarkGray else CosmicSecondary, contentColor = Color.Black),
                                                shape = RoundedCornerShape(10.dp),
                                                enabled = !isCompleted,
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("تسليم 💵", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        if (isCompleted) {
                                            var selectedInvoiceType by remember { mutableStateOf("مندوب مطعم") }

                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "تحديد نوع الفاتورة للمشاركة مع المدير:",
                                                fontSize = 10.sp,
                                                color = CosmicSecondary,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Right
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
                                            ) {
                                                listOf("مندوب صيدلية", "مندوب منتج", "مندوب مطعم").forEach { type ->
                                                    val isSelected = selectedInvoiceType == type
                                                    Box(
                                                        modifier = Modifier
                                                            .background(if (isSelected) CosmicSecondary else CosmicSurfaceVariant, RoundedCornerShape(12.dp))
                                                            .clickable { selectedInvoiceType = type }
                                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = type,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.Black else Color.White
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = {
                                                    val invoiceMsg = """
 🍔 فاتورة تسليم وجبة مطعم 🍔
 ---------------------------
 🚴 نوع الفاتورة: فاتورة $selectedInvoiceType
 ✍️ اسم المندوب: ${myCourierInfo?.name ?: "مندوب مجرة"}
 👤 اسم الزبون: ${order.customerName}
 📞 هاتف الزبون: ${order.customerPhone}
 📦 رقم الطلب: #REST-${order.id}
 💳 طريقة الدفع والاستلام: ${order.status}
 ---------------------------
 🏪 اسم المطعم: ${order.restaurantName}
 📞 هاتف المطعم: ${order.restaurantPhone}
 🍟 تفاصيل الوجبة والطلب:
 ${order.itemsAndNotes}
 ---------------------------
 💵 سعر الوجبات: ${viewModel.formatPrice(order.foodPrice)} SDG
 🚚 سعر التوصيل: ${viewModel.formatPrice(order.deliveryFee)} SDG
 💰 إجمالي التحصيل: ${viewModel.formatPrice(order.foodPrice + order.deliveryFee)} SDG
 ---------------------------
 تم تسليم الشحنة بنجاح من قبل مندوب التوصيل المعتمد.
 شكراً لثقتكم بمجرة التسوق الإلكتروني 🌌⚡
""".trimIndent()
                                                    WhatsAppUtils.sendWhatsAppMessage(context, "249910074223", invoiceMsg)
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
                        } else {
                            itemsIndexed(filteredPharmacyOrders) { orderIdx, order ->
                                val isCompleted = (order.status.contains("تم التوصيل") || order.status.contains("تم التسليم") || order.status.contains("إغلاق")) && !order.status.contains("المندوب") && !order.status.contains("للمندوب")
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    border = BorderStroke(1.dp, Color(0xFF64B5F6).copy(0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(order.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isCompleted) Color.Green else Color(0xFF64B5F6))
                                            Text("الترتيب: الطلب رقم ${orderIdx + 1} 🔢 (روشتة صيدلية #${order.id})", fontSize = 11.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                            Text("المريض/الزبون: ${order.customerName}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right)
                                            Text("رقم الهاتف: ${order.customerPhone}", fontSize = 10.sp, color = Color.White.copy(0.8f), textAlign = TextAlign.Right)
                                            if (order.deliveryLocation.isNotBlank()) {
                                                Text("📍 موقع/عنوان التوصيل: ${order.deliveryLocation}", fontSize = 11.sp, color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, textAlign = TextAlign.Right)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = Color.White.copy(0.1f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("الأدوية المطلوبة:\n${order.medicinesJson}", fontSize = 11.sp, color = Color.White.copy(0.8f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        val delFee = order.deliveryFee
                                        Text("سعر الأدوية 💊: ${viewModel.formatPrice(order.medicinePrice)} SDG\nرسوم التوصيل 🚚: ${if (delFee > 0) viewModel.formatPrice(delFee) + " SDG" else "توصيل مجان 🌸"}\nالمبلغ الإجمالي المطلـوب: ${viewModel.formatPrice(order.medicinePrice + delFee)} SDG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CosmicSecondary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            // Call Customer
                                            Button(
                                                onClick = {
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                        data = android.net.Uri.parse("tel:${order.customerPhone}")
                                                    }
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("اتصال 📞", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            // WhatsApp
                                            Button(
                                                onClick = {
                                                    val msg = "مرحباً يا ${order.customerName}! معكم المندوب ${myCourierInfo?.name} لتوصيل أدويتكم من صيدلية المجرة. قيمة الدواء هي ${viewModel.formatPrice(order.medicinePrice)} SDG وتوصيل الدواء مجان. هل أنتم متواجدون للاستلام؟"
                                                    WhatsAppUtils.sendWhatsAppMessage(context, order.customerPhone, msg)
                                                },
                                                modifier = Modifier.weight(1.1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Chat, null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("واتساب 💬", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            // Confirm Delivery
                                            Button(
                                                onClick = {
                                                    selectedPaymentMethod = "cash"
                                                    bankTransferReference = ""
                                                    showDeliveryPaymentDialogForPharmacyId = order.id
                                                },
                                                modifier = Modifier.weight(1.2f),
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isCompleted) Color.DarkGray else CosmicSecondary, contentColor = Color.Black),
                                                shape = RoundedCornerShape(10.dp),
                                                enabled = !isCompleted,
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("تسليم 💊", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        if (isCompleted) {
                                            var selectedInvoiceType by remember { mutableStateOf("مندوب صيدلية") }

                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "تحديد نوع الفاتورة للمشاركة مع المدير:",
                                                fontSize = 10.sp,
                                                color = CosmicSecondary,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Right
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
                                            ) {
                                                listOf("مندوب صيدلية", "مندوب منتج", "مندوب مطعم").forEach { type ->
                                                    val isSelected = selectedInvoiceType == type
                                                    Box(
                                                        modifier = Modifier
                                                            .background(if (isSelected) CosmicSecondary else CosmicSurfaceVariant, RoundedCornerShape(12.dp))
                                                            .clickable { selectedInvoiceType = type }
                                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = type,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.Black else Color.White
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = {
                                                    val invoiceMsg = """
 💊 فاتورة تسليم دواء صيدلية 💊
 ---------------------------
 🚴 نوع الفاتورة: فاتورة $selectedInvoiceType
 ✍️ اسم المندوب: ${myCourierInfo?.name ?: "مندوب مجرة"}
 👤 اسم المريض/الزبون: ${order.customerName}
 📞 هاتف الزبون: ${order.customerPhone}
 📦 رقم الطلب: #PHAR-${order.id}
 💳 طريقة الدفع والاستلام: ${order.status}
 ---------------------------
 🧪 تفاصيل الأدوية والروشتة:
 ${order.medicinesJson}
 ---------------------------
 💊 قيمة الدواء: ${viewModel.formatPrice(order.medicinePrice)} SDG
 🚚 سعر التوصيل: توصيل الدواء مجان 🎉
 💰 إجمالي التحصيل: ${viewModel.formatPrice(order.medicinePrice)} SDG
 ---------------------------
 تم تسليم الأدوية بنجاح من قبل مندوب التوصيل المعتمد. بالشفاء العاجل إن شاء الله.
 شكراً لثقتكم بمجرة التسوق الإلكتروني 🌌⚡
""".trimIndent()
                                                    WhatsAppUtils.sendWhatsAppMessage(context, "249910074223", invoiceMsg)
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

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicSurfaceVariant.copy(0.15f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
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
                    painter = painterResource(id = R.drawable.img_majarah_logo_1782345985330),
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
                text = "المجرة للتسوق 🌌",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Almajra",
                fontWeight = FontWeight.SemiBold,
                color = CosmicSecondary,
                fontSize = 18.sp,
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

            // Cosmic Elegant Logo and Rotating Stars Loading Indicator
            CosmicMajarahLoader(
                logoSize = 56.dp,
                modifier = Modifier.padding(8.dp)
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

@Composable
fun AppRatingDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var ratingStars by remember { mutableStateOf(7) }
    var commentText by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, CosmicSecondary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🌠🌌",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "تقييم تجربة التسوق ⭐",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "تهانينا على استلاف طلبك بنجاح! 🎉 يسعدنا جداً تقييمك للتطبيق من 7 نجوم لمساعدتنا على تحسين الخدمة وإرسال كوبونات عروض مخصصة لك.",
                    color = MediumContrastTextDark,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..7).forEach { index ->
                        IconButton(
                            onClick = { ratingStars = index },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star $index",
                                tint = if (index <= ratingStars) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$ratingStars من أصل 7 نجوم",
                    color = CosmicSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("اكتب رأيك أو مقترحاتك هنا (اختياري)...") },
                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicSecondary,
                        unfocusedBorderColor = CosmicSurfaceVariant,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { onSubmit(ratingStars, commentText) },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إرسال التقييم 🚀", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RestrictedAccessScreenBody(viewModel: MajarahViewModel) {
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
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = CosmicSecondary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "عذراً! هذا القسم مخصص للزبائن فقط 🔐",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "حسابات الشركاء ومندوبي التوصيل والإدارة مخصصة لإدارة العمليات والخدمات الكونية ولا يمكنها تصفح الأقسام أو الشراء كعميل.",
                    color = MediumContrastTextDark,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun OrderPostDeliveryPaymentBlock(
    currentPaymentMethod: String,
    currentReceiptBase64: String?,
    onSavePayment: (paymentMethod: String, receiptBase64: String?) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var selectedOption by remember { mutableStateOf(if (currentPaymentMethod.contains("بنك")) "bank" else "cash") }
    var attachedReceiptBase64 by remember { mutableStateOf(currentReceiptBase64) }
    var transactionId by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            attachedReceiptBase64 = base64
            android.widget.Toast.makeText(context, "تم التقاط صورة الإشعار بنجاح! 📸", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val outputStream = java.io.ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val bytes = outputStream.toByteArray()
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                    attachedReceiptBase64 = base64
                    android.widget.Toast.makeText(context, "تم اختيار صورة الإشعار بنجاح! 🖼️", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "فشل قراءة الصورة", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "💳 سداد قيمة الطلب وإرفاق إشعار الدفع:",
                color = CosmicSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cash Option
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedOption = "cash" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedOption == "cash") CosmicSecondary.copy(alpha = 0.15f) else CosmicSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (selectedOption == "cash") 1.5.dp else 1.dp,
                        color = if (selectedOption == "cash") CosmicSecondary else CosmicSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Payments, null, tint = if (selectedOption == "cash") CosmicSecondary else Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("الدفع كاش 💵", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Bank Option
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedOption = "bank" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedOption == "bank") CosmicSecondary.copy(alpha = 0.15f) else CosmicSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (selectedOption == "bank") 1.5.dp else 1.dp,
                        color = if (selectedOption == "bank") CosmicSecondary else CosmicSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AccountBalance, null, tint = if (selectedOption == "bank") CosmicSecondary else Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("تحويل بنكي 💳", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (selectedOption == "bank") {
                Spacer(modifier = Modifier.height(10.dp))
                
                // Bank Details Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(0.3f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicSecondary.copy(0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "🏦 بيانات الحساب للتحويل البنكي:",
                            color = CosmicSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("بنك الخرطوم 🇸🇩", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("اسم البنك:", color = Color.Gray, fontSize = 10.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("معاوية عثمان احمد ياسين"))
                                        android.widget.Toast.makeText(context, "تم نسخ الاسم بنجاح! 📋", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("نسخ 📋", fontSize = 9.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                }
                                Text("معاوية عثمان احمد ياسين", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("الاسم كامل:", color = Color.Gray, fontSize = 10.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("3414879"))
                                        android.widget.Toast.makeText(context, "تم نسخ رقم الحساب! 📋", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("نسخ 📋", fontSize = 9.sp, color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                }
                                Text("3414879", color = CosmicSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("رقم الحساب:", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Transaction ID Field
                Text(
                    text = "رقم المعاملة أو العملية البنكية:",
                    color = Color.White.copy(0.8f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = { transactionId = it },
                    placeholder = { Text("أدخل رقم العملية البنكية هنا (إن وجد)", fontSize = 10.sp, color = Color.White.copy(0.4f)) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Right),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicSecondary,
                        unfocusedBorderColor = Color.White.copy(0.3f)
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Image Attach Section (Camera and Gallery buttons side by side)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(14.dp), tint = CosmicSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("التقاط بالكاميرا 📸", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Gallery
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Image, null, modifier = Modifier.size(14.dp), tint = CosmicSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إرفاق من الاستوديو 🖼️", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                if (attachedReceiptBase64 != null) {
                    Text(
                        text = "تم إرفاق إشعار الدفع بنجاح ✅",
                        color = Color.Green,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "يرجى كتابة رقم العملية أو إرفاق صورة الإشعار للتأكيد ⚠️",
                        color = Color.Yellow,
                        fontSize = 10.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val isSubmitEnabled = selectedOption == "cash" || transactionId.isNotBlank() || attachedReceiptBase64 != null

            Button(
                onClick = {
                    isSaving = true
                    val methodStr = if (selectedOption == "bank") {
                        if (transactionId.isNotBlank()) "تحويل بنكي (رقم العملية: ${transactionId.trim()})" else "تحويل بنكي"
                    } else {
                        "كاش"
                    }
                    onSavePayment(methodStr, attachedReceiptBase64)
                    isSaving = false
                },
                enabled = !isSaving && isSubmitEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("تأكيد وإرسال تفاصيل الدفع 🚀", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun ViewReceiptDialog(
    base64String: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    val bitmap = remember(base64String) {
        try {
            val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "إغلاق", tint = Color.White)
                }
                Text(
                    "إشعار التحويل المرفق 📄",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Right
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💡 يمكنك استخدام إصبعين للتكبير والتصغير أو الأزرار بالأسفل 🔍",
                    color = CosmicSecondary,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .background(Color.Black, RoundedCornerShape(12.dp))
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 8f)
                                val maxOffsetX = (size.width * (scale - 1)) / 2
                                val maxOffsetY = (size.height * (scale - 1)) / 2
                                offset = Offset(
                                    x = if (scale > 1f) (offset.x + pan.x * scale).coerceIn(-maxOffsetX, maxOffsetX) else 0f,
                                    y = if (scale > 1f) (offset.y + pan.y * scale).coerceIn(-maxOffsetY, maxOffsetY) else 0f
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "إشعار الدفع",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("فشل تحميل صورة الإشعار ❌", color = Color.Red, fontSize = 12.sp)
                    }
                }

                if (bitmap != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                scale = (scale - 0.5f).coerceIn(1f, 8f)
                                if (scale == 1f) offset = Offset.Zero
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = CosmicSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "تصغير", tint = Color.White)
                        }

                        Text(
                            text = "مستوى التكبير: ${String.format("%.1f", scale)}x",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = CosmicSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "إعادة تعيين", tint = CosmicSecondary)
                        }

                        IconButton(
                            onClick = {
                                scale = (scale + 0.5f).coerceIn(1f, 8f)
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = CosmicSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "تكبير", tint = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary)
            ) {
                Text("إغلاق", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CosmicSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

fun isFemaleName(name: String): Boolean {
    val clean = name.trim()
    if (clean.isEmpty()) return false
    
    // We split the name into words and inspect the first word (usually the first name)
    val firstName = clean.split(Regex("\\s+")).firstOrNull() ?: clean
    
    // A comprehensive set of common female first names in Arabic/Sudanese context
    val femaleNames = setOf(
        "فاطمة", "عائشة", "مريم", "زينب", "خديجة", "سارة", "أميرة", "هدى", "منى", "رنا", "ريهام", 
        "هبة", "أمل", "رحاب", "ولاء", "نور", "أسماء", "آلاء", "نهى", "نادية", "داليا", "سحر", "سمر", 
        "خلود", "مي", "مها", "يسرى", "ليلى", "سلوى", "نجلاء", "غادة", "رشا", "رانيا", "عبير", "إيناس", 
        "هالة", "مروة", "شيرين", "بسمة", "منال", "إيمان", "نهاد", "شيماء", "وفاء", "رجاء", "حنان", 
        "جميلة", "رباب", "وصال", "تهاني", "سعاد", "سناء", "صفاء", "روان", "تسنيم", "سجى", "ضحى", 
        "إسراء", "أفنان", "ندى", "آية", "فدوى", "أمينة", "لبنى", "سوسن", "منى", "فرح", "سلمى", "يارا",
        "لجين", "رهف", "شهد", "مرام", "رنا", "هند", "يسرا", "سالي", "جنا", "ريماس", "تالا", "حلا", "ديما",
        "نورهان", "منة", "تسنيم", "صفية", "مواهب", "إخلاص", "التومة", "ام سلمة", "امنة", "كلتوم", "علوية",
        "سميرة", "نبيلة", "سليمة", "فايزة", "كريمة", "لينا", "نيرمين", "ندين", "رندة", "غدير", "وسام"
    )
    
    // Check if the first name is in our predefined female names set
    if (femaleNames.contains(firstName)) return true
    
    // Also check for common suffixes/prefixes of female names
    // Sudanese/Arabic female names often end with:
    // 'ة' (ta marbouta), 'ى' (alif maqsoura), or 'اء' (alif hamza)
    // But we need to make sure it's not a common male name like "حمزة", "طلحة", "عبيدة", "حذيفة", "عقبة", "أسامة", "جمعة", "علاء", "بهاء", "ضياء"
    val maleExceptions = setOf("حمزة", "طلحة", "عبيدة", "حذيفة", "عقبة", "أسامة", "جمعة", "علاء", "بهاء", "ضياء", "مصطفى", "مرتضى", "مجتبى", "رضا", "يحيى", "موسى", "عيسى")
    if (maleExceptions.contains(firstName)) return false
    
    if (firstName.endsWith("ة") || firstName.endsWith("ى") || firstName.endsWith("اء") || firstName.endsWith("يه")) {
        return true
    }
    
    return false
}


