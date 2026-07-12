package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.RestaurantEntity
import com.example.data.db.RestaurantOrderEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MajarahViewModel
import java.io.ByteArrayOutputStream
import java.net.URLEncoder




@Composable
fun RestaurantsPlanetSection(
    viewModel: MajarahViewModel,
    forceAdminPortal: Boolean = false
) {
    val context = LocalContext.current
    val restaurants by viewModel.allRestaurants.collectAsStateWithLifecycle()
    val orders by viewModel.allRestaurantOrders.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val isGeneralAdmin by viewModel.isGeneralAdmin.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isRestaurant by viewModel.isRestaurant.collectAsStateWithLifecycle()
    val isCourier by viewModel.isCourier.collectAsStateWithLifecycle()
    val isSeller by viewModel.isSeller.collectAsStateWithLifecycle()
    val isPharmacist by viewModel.isPharmacist.collectAsStateWithLifecycle()

    // Find our restaurant if we are registered as a restaurant owner
    val profile = activeProfile
    val myRestaurant = remember(restaurants, profile, isRestaurant) {
        if (!isRestaurant || profile == null) null
        else {
            restaurants.find { r ->
                r.phone.trim() == profile.phone.trim() ||
                r.name.trim().lowercase() == profile.name.trim().lowercase()
            }
        }
    }

    // Filter orders specifically for our restaurant
    val myRestaurantOrders = remember(orders, myRestaurant) {
        if (myRestaurant == null) emptyList()
        else orders.filter { it.restaurantId == myRestaurant.id }
    }

    // Play sound on new order received
    var lastSeenOrderCount by remember { mutableStateOf(-1) }
    LaunchedEffect(myRestaurantOrders) {
        if (lastSeenOrderCount != -1 && myRestaurantOrders.size > lastSeenOrderCount) {
            try {
                val alertUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                val r = android.media.RingtoneManager.getRingtone(context, alertUri)
                r?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (myRestaurantOrders.isNotEmpty()) {
            lastSeenOrderCount = myRestaurantOrders.size
        } else {
            lastSeenOrderCount = 0
        }
    }

    // Filter my past orders as a customer
    val myCustomerOrders = remember(orders, profile) {
        if (profile == null) emptyList()
        else orders.filter { it.customerPhone == profile.phone }
    }

    val myCustomerActiveOrders = remember(myCustomerOrders) {
        myCustomerOrders.filter {
            val isClosedForCustomer = it.status == "تم تسليم العميل وإغلاق الفاتورة ✅" ||
                    it.status.contains("مغلقة") ||
                    it.status == "بانتظار تأكيد التسليم من المندوب" ||
                    it.status.startsWith("تم التسليم") ||
                    it.paymentMethod.isNotBlank()
            !isClosedForCustomer
        }
    }

    val myCustomerPastOrders = remember(myCustomerOrders) {
        myCustomerOrders.filter {
            val isClosedForCustomer = it.status == "تم تسليم العميل وإغلاق الفاتورة ✅" ||
                    it.status.contains("مغلقة") ||
                    it.status == "بانتظار تأكيد التسليم من المندوب" ||
                    it.status.startsWith("تم التسليم") ||
                    it.paymentMethod.isNotBlank()
            isClosedForCustomer
        }
    }

    // Determine tabs based on role
    val tabTitles = remember(isAdmin, isRestaurant, myRestaurantOrders.size, myCustomerActiveOrders.size, restaurants.size) {
        val customerTabLabel = if (myCustomerActiveOrders.isNotEmpty()) {
            "طلباتي الحالية (${myCustomerActiveOrders.size}) 📑"
        } else {
            "طلباتي السابقة 📑"
        }

        if (isRestaurant) {
            listOf("المطاعم المتاحة 🍔", "طلبات مطعمي (${myRestaurantOrders.size}) 📋", "إدارة مطعمي 🏪")
        } else if (isAdmin) {
            listOf("المطاعم المتاحة 🍔", customerTabLabel, "إدارة المطاعم (${restaurants.size}) ⚙️")
        } else {
            listOf("المطاعم المتاحة 🍔", customerTabLabel)
        }
    }

    var activeSubTab by remember { mutableStateOf(if (forceAdminPortal && isAdmin) 2 else 0) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog & UI states
    var selectedRestaurantForOrder by remember { mutableStateOf<RestaurantEntity?>(null) }
    var showAddRestaurantDialog by remember { mutableStateOf(false) }
    var selectedOrderForInvoice by remember { mutableStateOf<RestaurantOrderEntity?>(null) }
    var selectedOrderForApprove by remember { mutableStateOf<RestaurantOrderEntity?>(null) }
    val couriers by viewModel.allCouriers.collectAsStateWithLifecycle()

    var activeDeliveredOverlayOrderId by remember { mutableStateOf<Int?>(null) }
    var activeRatingDialogOrderId by remember { mutableStateOf<Int?>(null) }
    val shownDeliveredNotifications = remember { mutableStateListOf<Int>() }
    var pricingOrderTarget by remember { mutableStateOf<RestaurantOrderEntity?>(null) }

    LaunchedEffect(orders) {
        val customerPhone = activeProfile?.phone ?: ""
        if (customerPhone.isNotBlank()) {
            val deliveredCustomerOrder = orders.find { ord ->
                ord.customerPhone.trim() == customerPhone.trim() &&
                (ord.status.contains("تم التوصيل") || ord.status.contains("تم التسليم")) &&
                ord.status != "تم تسليم العميل وإغلاق الفاتورة ✅" &&
                ord.id !in shownDeliveredNotifications
            }
            if (deliveredCustomerOrder != null) {
                shownDeliveredNotifications.add(deliveredCustomerOrder.id)
                activeRatingDialogOrderId = deliveredCustomerOrder.id
            }
        }
    }

    LaunchedEffect(activeDeliveredOverlayOrderId) {
        if (activeDeliveredOverlayOrderId != null) {
            val orderId = activeDeliveredOverlayOrderId!!
            kotlinx.coroutines.delay(5000)
            viewModel.updateRestaurantOrderStatus(orderId, "تم تسليم العميل وإغلاق الفاتورة ✅") { err ->
                activeDeliveredOverlayOrderId = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDeepSpace)
    ) {
        // Horizontal Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            reverseLayout = true
        ) {
            tabTitles.forEachIndexed { index, title ->
                item {
                    TabButton(
                        text = title,
                        isSelected = activeSubTab == index,
                        onClick = { activeSubTab = index }
                    )
                }
            }
        }

        // SubTab Contents
        when (activeSubTab) {
            0 -> {
                // Available Restaurants
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("بحث عن مطعم...", color = Color.White.copy(0.5f), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = CosmicSecondary) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val filtered = restaurants.filter { 
                        (it.isApproved || isAdmin || isRestaurant) && 
                        it.name.contains(searchQuery, ignoreCase = true) 
                    }
                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد مطاعم متاحة حالياً بالمجرة 🪐", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            items(filtered) { rest ->
                                RestaurantCard(
                                    restaurant = rest,
                                    onOrderClick = {
                                        if (isRestaurant) {
                                            Toast.makeText(context, "عذراً! لا يمكن لأصحاب المطاعم طلب وجبات كزبائن ❌", Toast.LENGTH_LONG).show()
                                        } else {
                                            selectedRestaurantForOrder = rest
                                        }
                                    },
                                    isAdmin = isAdmin,
                                    isGeneralAdmin = isGeneralAdmin,
                                    onDeleteClick = {
                                        viewModel.deleteRestaurant(rest.id) { err ->
                                            if (err == null) {
                                                Toast.makeText(context, "تم حذف المطعم بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                if (isRestaurant) {
                    // My Restaurant Orders tab
                    if (myRestaurant == null) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Restaurant, null, tint = CosmicSecondary, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "يرجى إنشاء وتفعيل صفحة مطعمك أولاً لتلقي طلبات الزبائن الكونية 🪐",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { activeSubTab = 2 },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("إنشاء صفحة مطعمي الآن 🏪", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        var restaurantOwnerOrdersSubTab by remember { mutableStateOf(0) } // 0: Active, 1: Completed
                        val activeRestaurantOrders = myRestaurantOrders.filter {
                            val status = it.status
                            !status.contains("تم تسليم العميل") && !status.contains("إغلاق") && !status.contains("تم التسليم") && !status.contains("كاش") && !status.contains("بنكي")
                        }
                        val completedRestaurantOrders = myRestaurantOrders.filter {
                            val status = it.status
                            status.contains("تم تسليم العميل") || status.contains("إغلاق") || status.contains("تم التسليم") || status.contains("كاش") || status.contains("بنكي")
                        }
                        val displayedOrders = if (restaurantOwnerOrdersSubTab == 0) activeRestaurantOrders else completedRestaurantOrders

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Owner Orders Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .background(CosmicSurfaceVariant.copy(0.3f), RoundedCornerShape(10.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Button(
                                    onClick = { restaurantOwnerOrdersSubTab = 0 },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (restaurantOwnerOrdersSubTab == 0) CosmicSecondary else Color.Transparent,
                                        contentColor = if (restaurantOwnerOrdersSubTab == 0) Color.Black else Color.White
                                    )
                                ) {
                                    Text("الطلبات النشطة 🍳 (${activeRestaurantOrders.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { restaurantOwnerOrdersSubTab = 1 },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (restaurantOwnerOrdersSubTab == 1) Color.Green.copy(0.8f) else Color.Transparent,
                                        contentColor = if (restaurantOwnerOrdersSubTab == 1) Color.Black else Color.White
                                    )
                                ) {
                                    Text("المكتملة والمنفذة ✅ (${completedRestaurantOrders.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (displayedOrders.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (restaurantOwnerOrdersSubTab == 0) "لا توجد طلبات جارية أو نشطة حالياً لمطعمك 🪐🍔" else "سجل الفواتير والطلبات المكتملة فارغ حالياً 📜💼",
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item {
                                        Text(
                                            text = if (restaurantOwnerOrdersSubTab == 0) "طلبات مطعمك النشطة والمطروحة للتحضير 🍳" else "أرشيف فواتير طلبات مطعمك المكتملة كلياً 📜✅",
                                            color = CosmicSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                    items(displayedOrders) { ord ->
                                    val orderIndex = myRestaurantOrders.indexOf(ord) + 1
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, CosmicSurfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "الترتيب: الطلب رقم $orderIndex 🔢 (طلب #${ord.id})",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                val statusColor = when (ord.status) {
                                                    "معلق" -> Color(0xFFFFB300)
                                                    "قيد التحضير بالمطعم 🍳" -> CosmicSecondary
                                                    "جاهز للتوصيل (بانتظار المدير) 🛵" -> Color.Cyan
                                                    else -> Color.Green
                                                }
                                                Text(
                                                    text = ord.status,
                                                    color = statusColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier
                                                        .background(statusColor.copy(0.12f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(text = ord.itemsAndNotes, color = Color.White.copy(0.8f), fontSize = 12.sp)

                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "طريقة الدفع: ${ord.paymentMethod}", color = Color.White.copy(0.6f), fontSize = 11.sp)
                                                Text(text = "العميل: ${ord.customerName}", color = Color.White.copy(0.6f), fontSize = 11.sp)
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Contact Buttons
                                                IconButton(
                                                    onClick = {
                                                        try {
                                                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ord.customerPhone}")))
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "فشل تشغيل تطبيق الاتصال", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier.background(Color.White.copy(0.08f), CircleShape).size(36.dp)
                                                ) {
                                                    Icon(Icons.Default.Phone, "اتصال بالعميل", tint = Color.White, modifier = Modifier.size(16.dp))
                                                }

                                                if (ord.status == "معلق" || ord.status == "قيد التحضير بالمطعم 🍳") {
                                                    Button(
                                                        onClick = {
                                                            pricingOrderTarget = ord
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("قبول الطلب وتحديد السعر الكلي 🍳💰", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Text(
                                                        text = "تم التسليم بنجاح للمدير للتوصيل 🌌🚀",
                                                        color = Color.Green,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
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
                } else {
                    // Regular customer orders separated into active and past
                    var customerSubTab by remember { mutableStateOf(0) } // 0: Active, 1: Past
                    
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { customerSubTab = 0 },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (customerSubTab == 0) CosmicSecondary else CosmicSurfaceVariant,
                                    contentColor = if (customerSubTab == 0) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("الطلبات الجارية (${myCustomerActiveOrders.size}) 🛰️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { customerSubTab = 1 },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (customerSubTab == 1) CosmicSecondary else CosmicSurfaceVariant,
                                    contentColor = if (customerSubTab == 1) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("الطلبات المغلقة (${myCustomerPastOrders.size}) 📑", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        val targetOrders = if (customerSubTab == 0) myCustomerActiveOrders else myCustomerPastOrders

                        if (targetOrders.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (customerSubTab == 0) "ليس لديك طلبات جارية حالياً 🍔" else "لا توجد طلبات منفذة سابقة 📝",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(targetOrders) { ord ->
                                    RestaurantOrderCard(
                                        order = ord,
                                        onShowInvoice = { selectedOrderForInvoice = ord },
                                        isAdmin = false,
                                        onStatusChange = null,
                                        onUpdatePayment = { method, base64 ->
                                            viewModel.updateRestaurantOrderPayment(ord.id, method, base64) { err ->
                                                if (err == null) {
                                                    viewModel.updateRestaurantOrderStatus(ord.id, "تم تسليم العميل وإغلاق الفاتورة ✅") { errStatus ->
                                                        if (errStatus == null) {
                                                            Toast.makeText(context, "تم سداد الفاتورة وإغلاقها بنجاح! 🎉🛰️", Toast.LENGTH_LONG).show()
                                                        } else {
                                                            Toast.makeText(context, "تم تأكيد السداد بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(context, "فشل تأكيد الدفع: $err", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                if (isRestaurant) {
                    // Restaurant Settings/Dashboard for Owner
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        if (myRestaurant == null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(1.dp, CosmicSurfaceVariant),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.AddBusiness, null, tint = CosmicSecondary, modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "مرحباً بك كشريك ومزود خدمة بالمجرة! 🪐",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "يرجى الضغط على الزر أدناه لإدخال اسم مطعمك الكوني، رقم هاتف تلقي الطلبات، ورفع صورة المنيو وصورة الشعار (اللوقو) لبدء تفعيل الخدمة واستقبال طلبات زبائن تطبيق المجرة فوراً.",
                                        color = MediumContrastTextDark,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = { showAddRestaurantDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("إنشاء صفحة مطعمي الكوني 🏪✨", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        } else {
                            // Display my restaurant details
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(1.2.dp, CosmicSecondary.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Logo render
                                        val logoBmp = remember(myRestaurant.logoImageUri) {
                                            if (myRestaurant.logoImageUri == null) null
                                            else {
                                                try {
                                                    val decodedBytes = Base64.decode(myRestaurant.logoImageUri, Base64.DEFAULT)
                                                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                                } catch (e: Exception) { null }
                                            }
                                        }
                                        if (logoBmp != null) {
                                            Image(
                                                bitmap = logoBmp.asImageBitmap(),
                                                contentDescription = "شعار مطعمي",
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                                    .border(1.5.dp, CosmicSecondary, CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                                    .background(CosmicDeepSpace)
                                                    .border(1.5.dp, CosmicSecondary, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Store, null, tint = CosmicSecondary, modifier = Modifier.size(28.dp))
                                            }
                                        }

                                        Column {
                                            Text(text = myRestaurant.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            Text(text = "هاتف الطلبات الكونية: ${myRestaurant.phone}", color = CosmicSecondary, fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = CosmicSurfaceVariant)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text("قائمة الطعام (المنيو) الحالية:", color = Color.White.copy(0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CosmicDeepSpace),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val menuBmp = remember(myRestaurant.menuImageUri) {
                                            if (myRestaurant.menuImageUri == null) null
                                            else {
                                                try {
                                                    val decodedBytes = Base64.decode(myRestaurant.menuImageUri, Base64.DEFAULT)
                                                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                                } catch (e: Exception) { null }
                                            }
                                        }
                                        if (menuBmp != null) {
                                            Image(
                                                bitmap = menuBmp.asImageBitmap(),
                                                contentDescription = "قائمة الطعام والمنيو",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text("لم تقم برفع صورة المنيو حتى الآن 📋", color = Color.Gray, fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { showAddRestaurantDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Edit, "تعديل", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تعديل بيانات وصور مطعمي 🏪✏️", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Admin Portal
                    var adminSubSection by remember { mutableStateOf(0) } // 0: Orders, 1: Restaurant Verification
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val subSections = listOf(
                                "توثيق المطاعم الجديدة 🏪" to 1,
                                "طلبات زبائن المطاعم 🍔" to 0
                            )
                            subSections.forEach { (label, index) ->
                                val isSelected = adminSubSection == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) CosmicSecondary else CosmicSurface)
                                        .clickable { adminSubSection = index }
                                        .padding(vertical = 10.dp),
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
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (adminSubSection == 0) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Button(
                                        onClick = { showAddRestaurantDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Add, "إضافة مطعم", modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("إضافة مطعم جديد 🏪", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }

                                item {
                                    Text(
                                        "طلبات المطاعم الواردة للمجرة 🌌",
                                        color = CosmicSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                if (orders.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("لا توجد طلبات مطاعم حالياً 🍔", color = Color.Gray, fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    items(orders) { ord ->
                                        RestaurantOrderCard(
                                            order = ord,
                                            onShowInvoice = { selectedOrderForInvoice = ord },
                                            isAdmin = true,
                                            onStatusChange = { newStatus ->
                                                viewModel.updateRestaurantOrderStatus(ord.id, newStatus) { err ->
                                                    if (err == null) {
                                                        Toast.makeText(context, "تم تحديث حالة الطلب بنجاح! 🟢", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            onAssignCourier = { selectedOrderForApprove = ord }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Verification / Approvals of restaurants
                            val pendingRestaurants = restaurants.filter { !it.isApproved }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Text(
                                        "طلبات توثيق المطاعم الجديدة 🏪📥",
                                        color = CosmicSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "هنا تظهر كافة طلبات التسجيل الجديدة للمطاعم. بمجرد قبولك وتوثيقك لها، سيتم نشرها وظهورها لكافة عملاء المجرة.",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                                
                                if (pendingRestaurants.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("لا توجد طلبات توثيق معلقة حالياً لشركاء مطاعم 🏪💤", color = Color.Gray, fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    items(pendingRestaurants) { rest ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                            border = BorderStroke(1.dp, CosmicSurfaceVariant),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color.Red.copy(0.15f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("بانتظار الموافقة ⏳", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(rest.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                }
                                                
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("رقم هاتف تلقي الطلبات: ${rest.phone} 💬", color = Color.White.copy(0.7f), fontSize = 11.sp)
                                                Text("تاريخ التسجيل: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(rest.createdAt))}", color = Color.Gray, fontSize = 10.sp)
                                                
                                                Spacer(modifier = Modifier.height(12.dp))
                                                
                                                // Menu preview if available
                                                val menuBmp = remember(rest.menuImageUri) {
                                                    if (rest.menuImageUri == null) null
                                                    else {
                                                        try {
                                                            val decodedBytes = Base64.decode(rest.menuImageUri, Base64.DEFAULT)
                                                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                                        } catch (e: Exception) { null }
                                                    }
                                                }
                                                if (menuBmp != null) {
                                                    Text("صورة المنيو وقائمة الطعام:", color = Color.White.copy(0.8f), fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(120.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(8.dp))
                                                    ) {
                                                        Image(
                                                            bitmap = menuBmp.asImageBitmap(),
                                                            contentDescription = "المنيو",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                }
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.deleteRestaurant(rest.id) { err ->
                                                                if (err == null) {
                                                                    Toast.makeText(context, "تم رفض وحذف المطعم بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1.5f),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.2f), contentColor = Color.Red),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("رفض وحذف 🗑️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    
                                                    Button(
                                                        onClick = {
                                                            viewModel.approveRestaurant(rest.id) { err ->
                                                                if (err == null) {
                                                                    Toast.makeText(context, "تم قبول واعتماد المطعم بنجاح ونشره بالمنظومة! 🎉🍔", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1.5f),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("قبول ونشر المطعم ✅", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
    }

    // Dialog for ordering from a specific restaurant
    if (selectedRestaurantForOrder != null) {
        OrderFromRestaurantDialog(
            restaurant = selectedRestaurantForOrder!!,
            activeProfile = activeProfile,
            onDismiss = { selectedRestaurantForOrder = null },
            onSubmitOrder = { orderText, notesText, deliveryLoc ->
                val name = activeProfile?.name ?: "عميل المجرة"
                val phone = activeProfile?.phone ?: ""
                val email = activeProfile?.email ?: ""
                val deliveryFee = 0.0 // Initially 0, Manager/Admin will specify it!

                viewModel.addRestaurantOrder(
                    restaurantId = selectedRestaurantForOrder!!.id,
                    restaurantName = selectedRestaurantForOrder!!.name,
                    restaurantPhone = selectedRestaurantForOrder!!.phone,
                    customerName = name,
                    customerPhone = phone,
                    customerEmail = email,
                    itemsAndNotes = "$orderText\nملاحظات: $notesText\nموقع التوصيل: $deliveryLoc",
                    paymentMethod = "", // Initially empty, chosen after delivery
                    deliveryFee = deliveryFee,
                    bankReceiptImageUri = null // Initially null, attached after delivery
                ) { err, savedOrder ->
                    if (err == null && savedOrder != null) {
                        Toast.makeText(context, "تم تسجيل طلبك بنجاح! 🎉", Toast.LENGTH_LONG).show()
                        selectedRestaurantForOrder = null
                        selectedOrderForInvoice = savedOrder
                    } else {
                        Toast.makeText(context, "خطأ أثناء تسجيل الطلب: $err", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Dialog for displaying the invoice
    if (selectedOrderForInvoice != null) {
        InvoiceDialog(
            order = selectedOrderForInvoice!!,
            onDismiss = { selectedOrderForInvoice = null },
            onShareWhatsApp = {
                val message = """
                    🌌 *فاتورة طلب وجبة من كوكب المطاعم - تطبيق المجرة* 🌌
                    
                    📌 *رقم الفاتورة:* #${selectedOrderForInvoice!!.id}
                    🏪 *المطعم:* ${selectedOrderForInvoice!!.restaurantName}
                    👤 *العميل:* ${selectedOrderForInvoice!!.customerName}
                    📞 *هاتف العميل:* ${selectedOrderForInvoice!!.customerPhone}
                    
                    📋 *الطلبات والوجبات:*
                    ${selectedOrderForInvoice!!.itemsAndNotes}
                    
                    💳 *طريقة الدفع:* ${selectedOrderForInvoice!!.paymentMethod}
                    🛵 *رسوم التوصيل الكونية:* ${selectedOrderForInvoice!!.deliveryFee} ج.س
                    
                    ⏱️ *الحالة:* ${selectedOrderForInvoice!!.status}
                    💡 *تم الترتيب وتأكيد الفاتورة بواسطة تطبيق المجرة الكوني.*
                """.trimIndent()
                
                try {
                    val urlEncoded = URLEncoder.encode(message, "UTF-8")
                    // Format phone number to international Sudan +249
                    var targetPhone = selectedOrderForInvoice!!.restaurantPhone.trim()
                    if (targetPhone.startsWith("0")) {
                        targetPhone = "249" + targetPhone.substring(1)
                    } else if (!targetPhone.startsWith("249")) {
                        targetPhone = "249" + targetPhone
                    }
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=$urlEncoded")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "الرجاء تثبيت واتساب لإرسال الفاتورة 💬", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog for adding a restaurant (Admin Portal / Restaurant Owner Portal)
    if (showAddRestaurantDialog) {
        AddRestaurantDialog(
            initialRestaurant = myRestaurant,
            onDismiss = { showAddRestaurantDialog = false },
            onAdd = { name, phone, menuImageBase64, logoImageBase64 ->
                viewModel.addRestaurant(
                    id = myRestaurant?.id ?: 0,
                    name = name,
                    phone = phone,
                    menuImageUri = menuImageBase64,
                    logoImageUri = logoImageBase64
                ) { err ->
                    if (err == null) {
                        try {
                            val alertUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                            val r = android.media.RingtoneManager.getRingtone(context, alertUri)
                            r?.play()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        Toast.makeText(context, "تم تسجيل المطعم بنجاح! المطعم حالياً بانتظار موافقة المدير العام لتأكيد التوثيق ⏳🏪", Toast.LENGTH_LONG).show()
                        showAddRestaurantDialog = false
                    } else {
                        Toast.makeText(context, "فشل حفظ بيانات المطعم: $err", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Dialog for assigning courier and delivery fee to a restaurant order
    if (selectedOrderForApprove != null) {
        val ord = selectedOrderForApprove!!
        var feeInput by remember { mutableStateOf("") }
        var courierSelection by remember { mutableStateOf("") } // Name
        var courierPhoneSelection by remember { mutableStateOf("") } // Phone
        var expandedCouriersDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { selectedOrderForApprove = null },
            containerColor = CosmicSurface,
            title = { Text("تعيين المندوب ورسوم التوصيل 🚴🏆", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Text("وجبات العميل: ${ord.itemsAndNotes}", color = MediumContrastTextDark, fontSize = 11.sp, textAlign = TextAlign.Right)
                    Text("العميل: ${ord.customerName}", color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Right)
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = feeInput,
                        onValueChange = { feeInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("رسوم التوصيل بالجنيه السوداني (ج.س) 💰", color = CosmicSecondary, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Courier Dropdown selection
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedCouriersDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CosmicSecondary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(
                                text = if (courierSelection.isBlank()) "اختر مندوب التوصيل الكوني 🚴" else "$courierSelection ($courierPhoneSelection)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        DropdownMenu(
                            expanded = expandedCouriersDropdown,
                            onDismissRequest = { expandedCouriersDropdown = false },
                            modifier = Modifier.background(CosmicSurface)
                        ) {
                            if (couriers.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("لا يوجد مناديب مسجلين", color = Color.White) },
                                    onClick = { expandedCouriersDropdown = false }
                                )
                            } else {
                                couriers.forEach { courier ->
                                    DropdownMenuItem(
                                        text = { Text("${courier.name} (${courier.stateInfo}) [${courier.status}]", color = Color.White) },
                                        onClick = {
                                            courierSelection = courier.name
                                            courierPhoneSelection = courier.phone
                                            expandedCouriersDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fee = feeInput.toDoubleOrNull() ?: 0.0
                        if (courierSelection.isBlank() || fee <= 0.0) {
                            Toast.makeText(context, "الرجاء اختيار المندوب وتحديد رسوم توصيل صالحة ⚠️", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.assignCourierToRestaurantOrder(ord.id, "تم تعيين المندوب وقيد التوصيل 🚴", courierSelection, courierPhoneSelection, fee) { err ->
                                if (err == null) {
                                    Toast.makeText(context, "تم تعيين المندوب ونشر الفاتورة بنجاح! 🎉🚴", Toast.LENGTH_LONG).show()
                                    selectedOrderForApprove = null
                                } else {
                                    Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary)
                ) {
                    Text("اعتماد ونشر الفاتورة 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOrderForApprove = null }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    // 1. "بالهناء والشفاء" 5-second full-screen overlay dialog
    if (activeDeliveredOverlayOrderId != null) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = CosmicDeepSpace,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 10.dp,
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(CosmicSecondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("❤️", fontSize = 42.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "بالهناء والشفاء 🪐❤️",
                        color = CosmicSecondary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "تم تسليم وجبتك اللذيذة بنجاح.\nنتمنى لك وجبة شهية وممتعة في كوكبنا! 🍕🍔",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {}
        )
    }

    // 2. "تقييم التطبيق" dialog (App Rating)
    if (activeRatingDialogOrderId != null) {
        var ratingStars by remember { mutableStateOf(5) }
        var reviewText by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { activeRatingDialogOrderId = null },
            containerColor = CosmicSurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "تقييم تطبيق المجرة الكوني 🌟🪐",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "يسعدنا كثيراً تقييم تجربتك معنا لتحسين خدمات التوصيل والطلب المستمر بالمجرة!",
                        color = Color.White.copy(0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Star rating row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        for (i in 1..5) {
                            val isSelected = i <= ratingStars
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$i نجوم",
                                tint = if (isSelected) CosmicSecondary else Color.White.copy(0.4f),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { ratingStars = i }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        placeholder = { Text("اكتب رأيك أو اقتراحاتك هنا (اختياري)...", fontSize = 11.sp, color = Color.White.copy(0.4f)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitAppRating(stars = ratingStars, comment = reviewText)
                        Toast.makeText(context, "شكراً جزيلاً لتقييمك الكوني المميز! ❤️🌌", Toast.LENGTH_LONG).show()
                        activeDeliveredOverlayOrderId = activeRatingDialogOrderId
                        activeRatingDialogOrderId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("إرسال التقييم 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        activeDeliveredOverlayOrderId = activeRatingDialogOrderId
                        activeRatingDialogOrderId = null
                    }
                ) {
                    Text("تخطي", color = Color.White.copy(0.6f))
                }
            }
        )
    }

    // 3. Restaurant Owner Pricing Dialog
    if (pricingOrderTarget != null) {
        val ord = pricingOrderTarget!!
        var priceInput by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { pricingOrderTarget = null },
            containerColor = CosmicSurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "تسعير طلب الوجبة وإكمال التحضير 🍳💵",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "الرجاء تحديد السعر الإجمالي لقائمة الطعام والطلبات لتسليم الفاتورة للمدير:",
                        color = Color.White.copy(0.8f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("السعر الإجمالي للوجبات (ج.س) 💰", color = CosmicSecondary, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = priceInput.toDoubleOrNull() ?: 0.0
                        if (price <= 0.0) {
                            Toast.makeText(context, "الرجاء تحديد سعر صحيح للمأكولات ⚠️", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateRestaurantOrderPriceAndStatus(ord.id, "جاهز للتوصيل (بانتظار المدير) 🛵", price) { err ->
                                if (err == null) {
                                    Toast.makeText(context, "تم تحديد السعر الكلي للوجبات وإرسال الطلب للمدير! 🛵🚀", Toast.LENGTH_SHORT).show()
                                    pricingOrderTarget = null
                                } else {
                                    Toast.makeText(context, "فشل الحفظ: $err", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("إرسال للمدير الكوني 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pricingOrderTarget = null }) {
                    Text("إلغاء", color = Color.White.copy(0.6f))
                }
            }
        )
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) CosmicSecondary else CosmicSurface)
            .border(
                width = 1.dp,
                color = if (isSelected) CosmicSecondary else CosmicSurfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
fun RestaurantCard(
    restaurant: RestaurantEntity,
    onOrderClick: () -> Unit,
    isAdmin: Boolean,
    isGeneralAdmin: Boolean,
    onDeleteClick: () -> Unit
) {
    var showFullMenuDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CosmicSurfaceVariant)
    ) {
        Column {
            // 1. Logo and Phone/Order number at the very top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Side: Phone/Order number
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "📞 رقم الطلبات:",
                        color = CosmicSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = restaurant.phone,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Right Side: Logo & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = restaurant.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Right
                    )

                    val logoBitmap = remember(restaurant.logoImageUri) {
                        if (restaurant.logoImageUri == null) null
                        else {
                            try {
                                val decodedBytes = Base64.decode(restaurant.logoImageUri, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            } catch (e: Exception) { null }
                        }
                    }
                    if (logoBitmap != null) {
                        Image(
                            bitmap = logoBitmap.asImageBitmap(),
                            contentDescription = "شعار المطعم",
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, CosmicSecondary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                                .background(CosmicDeepSpace)
                                .border(1.5.dp, CosmicSecondary.copy(0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, null, tint = CosmicSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Divider(color = CosmicSurfaceVariant, modifier = Modifier.padding(horizontal = 14.dp))

            // 2. Menu Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF0F1524))
            ) {
                if (restaurant.menuImageUri != null) {
                    val bitmap = remember(restaurant.menuImageUri) {
                        try {
                            val decodedBytes = Base64.decode(restaurant.menuImageUri, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "قائمة الطعام والمنيو",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        ImagePlaceholder()
                    }
                } else {
                    ImagePlaceholder()
                }
            }

            // 3. Bottom Actions with "عرض المنيو كامل" and "اطلب الآن"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isGeneralAdmin) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, "حذف", tint = Color.Red.copy(0.8f))
                    }
                }

                // Button to browse full menu image
                Button(
                    onClick = { showFullMenuDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CosmicSecondary.copy(0.3f))
                ) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(16.dp), tint = CosmicSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عرض المنيو كامل 📋🔍", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onOrderClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.RestaurantMenu, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اطلب الآن 🍔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Full Menu Image Viewer Dialog
    if (showFullMenuDialog) {
        AlertDialog(
            onDismissRequest = { showFullMenuDialog = false },
            containerColor = CosmicDeepSpace,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showFullMenuDialog = false }) {
                        Icon(Icons.Default.Close, "إغلاق", tint = Color.White)
                    }
                    Text(
                        text = "منيو مطعم ${restaurant.name} الكوني 🌌🍟",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                        .background(Color.Black, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (restaurant.menuImageUri != null) {
                        val bitmap = remember(restaurant.menuImageUri) {
                            try {
                                val decodedBytes = Base64.decode(restaurant.menuImageUri, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            } catch (e: Exception) { null }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "منيو كامل",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit // Fit is crucial to see the entire menu without crop
                            )
                        } else {
                            Text("حدث خطأ في تحميل صورة المنيو ❌", color = Color.White)
                        }
                    } else {
                        Text("لا تتوفر صورة منيو لهذا المطعم 📭", color = Color.White)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFullMenuDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary)
                ) {
                    Text("حسناً", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun ImagePlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Restaurant, null, tint = CosmicSecondary.copy(0.4f), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("منيو وقائمة طعام المطعم الكوزموس", color = Color.White.copy(0.4f), fontSize = 11.sp)
        }
    }
}

@Composable
fun RestaurantOrderCard(
    order: RestaurantOrderEntity,
    onShowInvoice: () -> Unit,
    isAdmin: Boolean,
    onStatusChange: ((String) -> Unit)?,
    onAssignCourier: (() -> Unit)? = null,
    onUpdatePayment: ((String, String?) -> Unit)? = null
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CosmicSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "طلب #${order.id} - ${order.restaurantName}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                val statusColor = when {
                    order.status == "معلق" -> Color(0xFFFFB300)
                    order.status == "قيد التحضير" -> CosmicSecondary
                    order.status.contains("تم") || order.status.contains("تسليم") -> Color.Green
                    else -> CosmicSecondary
                }
                Text(
                    text = order.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(statusColor.copy(0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            if (order.courierName.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "المندوب المعين: ${order.courierName} (${order.courierPhone}) 🚴",
                    color = CosmicSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                if (order.deliveryFee > 0) {
                    Text(
                        text = "رسوم التوصيل المقدرة: ${order.deliveryFee} SDG 🚚",
                        color = CosmicTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = order.itemsAndNotes,
                color = Color.White.copy(0.8f),
                fontSize = 12.sp,
                maxLines = 2
            )

            if (!isAdmin) {
                Spacer(modifier = Modifier.height(12.dp))
                RestaurantOrderTrackingPipeline(order.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (order.paymentMethod.isNotBlank()) "طريقة الدفع: ${order.paymentMethod}" else "طريقة الدفع: لم تحدد بعد",
                    color = Color.White.copy(0.6f),
                    fontSize = 11.sp
                )
                Text(
                    text = "العميل: ${order.customerName}",
                    color = Color.White.copy(0.6f),
                    fontSize = 11.sp
                )
            }

            var receiptToShow by remember { mutableStateOf<String?>(null) }
            if (receiptToShow != null) {
                ViewReceiptDialog(receiptToShow!!) { receiptToShow = null }
            }

            val isDelivered = order.status.contains("تم") || order.status.contains("تسليم") || order.status.contains("المندوب") || order.status.contains("التوصيل")
            if (!isAdmin && isDelivered) {
                if (order.paymentMethod.isBlank()) {
                    OrderPostDeliveryPaymentBlock(
                        currentPaymentMethod = "",
                        currentReceiptBase64 = null,
                        onSavePayment = { method, base64 ->
                            onUpdatePayment?.invoke(method, base64)
                        }
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(0.3f)),
                        border = BorderStroke(1.dp, Color.Green.copy(0.3f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "✅ تم تأكيد طريقة الدفع للمطعم",
                                color = Color.Green,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            if (!order.bankReceiptImageUri.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { receiptToShow = order.bankReceiptImageUri },
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
            } else if (isAdmin && !order.bankReceiptImageUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { receiptToShow = order.bankReceiptImageUri },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عرض إشعار التحويل المرفق بالطلب 📄", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAdmin) {
                    // Contact buttons
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${order.restaurantPhone}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "لا يمكن تشغيل تطبيق الاتصال", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .background(Color.White.copy(0.08f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Phone, "اتصال بالمطعم", tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = {
                            try {
                                var wp = order.restaurantPhone.trim()
                                if (wp.startsWith("0")) wp = "249" + wp.substring(1)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://api.whatsapp.com/send?phone=$wp&text=مرحباً، بخصوص طلب المجرة #${order.id}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "الرجاء تثبيت واتساب", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .background(Color.White.copy(0.08f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Message, "واتساب المطعم", tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                    }

                    // Status transitions
                    if (order.status == "معلق" && onStatusChange != null) {
                        Button(
                            onClick = { onStatusChange("قيد التحضير") },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("تحضير 🧑‍🍳", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (order.status == "قيد التحضير" && onStatusChange != null) {
                        Button(
                            onClick = { onStatusChange("تم التسليم") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("تم التسليم ✅", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (onAssignCourier != null && order.courierName.isBlank()) {
                        Button(
                            onClick = onAssignCourier,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("تعيين مندوب 🚴", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = onShowInvoice,
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الفاتورة 🧾", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun OrderFromRestaurantDialog(
    restaurant: RestaurantEntity,
    activeProfile: com.example.data.db.ProfileEntity?,
    onDismiss: () -> Unit,
    onSubmitOrder: (order: String, notes: String, deliveryLoc: String) -> Unit
) {
    val context = LocalContext.current
    var orderText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var deliveryLocation by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "طلب طعام من ${restaurant.name} 🍔",
                color = CosmicSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("اكتب طلباتك ووجباتك التي ترغب بها بالتفصيل:", color = Color.White.copy(0.8f), fontSize = 11.sp)
                    OutlinedTextField(
                        value = orderText,
                        onValueChange = { orderText = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text("مثال: 1 شاورما دبل لحم، 1 عصير برتقال كبير", color = Color.White.copy(0.4f), fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant
                        )
                    )
                }

                item {
                    Text("أي ملاحظات للمطعم (اختياري):", color = Color.White.copy(0.8f), fontSize = 11.sp)
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("مثال: بدون مايونيز، التوصيل للطابق الثاني", color = Color.White.copy(0.4f), fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant
                        )
                    )
                }

                item {
                    Text("عنوان وموقع التوصيل بالتفصيل *", color = Color.White.copy(0.8f), fontSize = 11.sp)
                    OutlinedTextField(
                        value = deliveryLocation,
                        onValueChange = { deliveryLocation = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("مثال: الخرطوم، حي الرياض، شارع 11 بجانب صيدلية النخبة", color = Color.White.copy(0.4f), fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (orderText.isBlank()) {
                        Toast.makeText(context, "الرجاء كتابة طلبك أولاً", Toast.LENGTH_SHORT).show()
                    } else if (deliveryLocation.isBlank()) {
                        Toast.makeText(context, "الرجاء كتابة عنوان التوصيل بالتفصيل", Toast.LENGTH_SHORT).show()
                    } else {
                        onSubmitOrder(orderText, notesText, deliveryLocation)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("تأكيد وإصدار الفاتورة 🧾", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.White)
            }
        },
        containerColor = CosmicSurface
    )
}

@Composable
fun InvoiceDialog(
    order: RestaurantOrderEntity,
    onDismiss: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val fullInvoiceMsg = """
🌌 *فاتورة وجبة مطعم كوكب المطاعم - تطبيق المجرة* 🌌
---------------------------
📌 *رقم الفاتورة:* #${order.id}
🏪 *المطعم:* ${order.restaurantName}
📞 *هاتف المطعم:* ${order.restaurantPhone}
👤 *العميل:* ${order.customerName}
📞 *هاتف العميل:* ${order.customerPhone}
📍 *العنوان:* ${order.deliveryLocation.ifBlank { "السودان" }}
---------------------------
🍟 *الوجبات والطلبات:*
${order.itemsAndNotes}
---------------------------
💵 *سعر الوجبات:* ${order.foodPrice} SDG
🚚 *رسوم التوصيل الكونية:* ${order.deliveryFee} SDG
💰 *الإجمالي الكلي للتحصيل:* ${order.foodPrice + order.deliveryFee} SDG
💳 *طريقة الدفع:* ${if (order.paymentMethod.isNotBlank()) order.paymentMethod else "لم تحدد بعد"}
⏱️ *حالة الطلب:* ${order.status}
🚴 *المندوب المعين:* ${if (order.courierName.isNotBlank()) "${order.courierName} (${order.courierPhone})" else "بانتظار تعيين مندوب"}
---------------------------
شكراً لتعاملكم مع تطبيق المجرة للتسوق 🪐✨
""".trimIndent()

    val orderNumberMsg = """
🌌 *تطبيق المجرة - رقم طلب المطعم الكوني* 🌌
📦 *رقم الطلب:* #REST-${order.id}
🏪 *المطعم:* ${order.restaurantName}
👤 *العميل:* ${order.customerName}
📞 *هاتف العميل:* ${order.customerPhone}
💰 *الإجمالي للتحصيل:* ${order.foodPrice + order.deliveryFee} SDG
""".trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "فاتورة طلب الوجبة الكونية 🪐",
                color = CosmicSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                        border = BorderStroke(1.dp, CosmicSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            InvoiceRow(label = "رقم الفاتورة", value = "#${order.id}")
                            InvoiceRow(label = "اسم المطعم", value = order.restaurantName)
                            InvoiceRow(label = "هاتف الطلبات", value = order.restaurantPhone)
                            Divider(color = CosmicSurfaceVariant)
                            InvoiceRow(label = "اسم العميل", value = order.customerName)
                            InvoiceRow(label = "هاتف العميل", value = order.customerPhone)
                        }
                    }
                }

                item {
                    Text("تفاصيل ومحتوى الفاتورة:", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                        border = BorderStroke(1.dp, CosmicSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = order.itemsAndNotes,
                            color = Color.White.copy(0.9f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                        border = BorderStroke(1.dp, CosmicSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            InvoiceRow(label = "سعر الوجبات", value = "${order.foodPrice} SDG")
                            InvoiceRow(label = "رسوم التوصيل الكوني", value = "${order.deliveryFee} SDG")
                            InvoiceRow(label = "المجموع الإجمالي", value = "${order.foodPrice + order.deliveryFee} SDG")
                            InvoiceRow(label = "طريقة الدفع", value = if (order.paymentMethod.isNotBlank()) order.paymentMethod else "لم تحدد بعد")
                            InvoiceRow(label = "الحالة الحالية", value = order.status)
                        }
                    }
                }

                if (order.bankReceiptImageUri != null) {
                    item {
                        Text("إشعار التحويل البنكي المرفق:", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        val bitmap = remember(order.bankReceiptImageUri) {
                            try {
                                val decodedBytes = Base64.decode(order.bankReceiptImageUri, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "إشعار الدفع البنكي",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "خيارات مشاركة الفاتورة 📲:",
                            color = CosmicSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )
                        
                        // Share with Restaurant (WhatsApp)
                        Button(
                            onClick = onShareWhatsApp,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Send, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مشاركة الفاتورة مع المطعم 🏪💬", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Share with Courier (WhatsApp) - Only if assigned
                        if (order.courierPhone.isNotBlank()) {
                            Button(
                                onClick = {
                                    try {
                                        var phone = order.courierPhone.trim().replace("+", "").replace(" ", "")
                                        if (phone.startsWith("0")) {
                                            phone = "249" + phone.substring(1)
                                        } else if (!phone.startsWith("249")) {
                                            phone = "249" + phone
                                        }
                                        val url = "https://api.whatsapp.com/send?phone=$phone&text=${java.net.URLEncoder.encode(fullInvoiceMsg, "UTF-8")}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "الرجاء تثبيت واتساب لمشاركة الفاتورة", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2), contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مشاركة الفاتورة مع المندوب 🚴💬", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Share order number (General chooser)
                        Button(
                            onClick = {
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, orderNumberMsg)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة رقم الطلب"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "حدث خطأ أثناء المشاركة", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Numbers, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مشاركة رقم طلب المطعم والمندوب 🔢💬", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق الفاتورة ❌", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CosmicSurface
    )
}

@Composable
fun InvoiceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Text(label, color = Color.White.copy(0.6f), fontSize = 11.sp)
    }
}

@Composable
fun AddRestaurantDialog(
    initialRestaurant: RestaurantEntity? = null,
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, menuImageBase64: String?, logoImageBase64: String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialRestaurant?.name ?: "") }
    var phone by remember { mutableStateOf(initialRestaurant?.phone ?: "") }
    var menuImageBase64 by remember { mutableStateOf<String?>(initialRestaurant?.menuImageUri) }
    var logoImageBase64 by remember { mutableStateOf<String?>(initialRestaurant?.logoImageUri) }

    var pendingCameraTarget by remember { mutableStateOf<String?>(null) } // "logo" or "menu"

    // Launchers for menu image
    val menuCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val base64 = try {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            } catch (e: Exception) { null }
            if (base64 != null) {
                menuImageBase64 = base64
                Toast.makeText(context, "تم التقاط المنيو! 📸", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val menuGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
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
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            } catch (e: Exception) { null }
            if (base64 != null) {
                menuImageBase64 = base64
                Toast.makeText(context, "تم اختيار المنيو! 🖼️", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launchers for logo image
    val logoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val base64 = try {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            } catch (e: Exception) { null }
            if (base64 != null) {
                logoImageBase64 = base64
                Toast.makeText(context, "تم التقاط اللوقو! 📸", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val logoGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
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
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            } catch (e: Exception) { null }
            if (base64 != null) {
                logoImageBase64 = base64
                Toast.makeText(context, "تم اختيار اللوقو! 🖼️", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                if (pendingCameraTarget == "logo") {
                    logoCameraLauncher.launch(null)
                } else if (pendingCameraTarget == "menu") {
                    menuCameraLauncher.launch(null)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "تعذر تشغيل الكاميرا: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "يجب منح إذن الكاميرا لالتقاط صورة المطعم! ⚠️", Toast.LENGTH_SHORT).show()
        }
        pendingCameraTarget = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "بيانات مطعمك في المجرة 🏪🪐",
                color = CosmicSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("اسم المطعم 🏪", color = CosmicSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("رقم هاتف طلبات المطعم 📞", color = CosmicSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant
                        )
                    )
                }

                item {
                    Text("1. صورة شعار (لوقو) المطعم الكوزموس 🎨", color = Color.White.copy(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Button(
                            onClick = {
                                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    try {
                                        logoCameraLauncher.launch(null)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "تعذر فتح الكاميرا: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    pendingCameraTarget = "logo"
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("كاميرا اللوقو 📸", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { logoGalleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Photo, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("معرض اللوقو 🖼️", fontSize = 10.sp)
                        }
                    }
                    if (logoImageBase64 != null) {
                        Text("تم اختيار شعار المطعم بنجاح! ✅🎨", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                item {
                    Text("2. صورة قائمة الطعام والمنيو 📋", color = Color.White.copy(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Button(
                            onClick = {
                                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    try {
                                        menuCameraLauncher.launch(null)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "تعذر فتح الكاميرا: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    pendingCameraTarget = "menu"
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("كاميرا المنيو 📸", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { menuGalleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Photo, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("معرض المنيو 🖼️", fontSize = 10.sp)
                        }
                    }
                    if (menuImageBase64 != null) {
                        Text("تم اختيار المنيو بنجاح! ✅📋", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        Toast.makeText(context, "الرجاء ملء اسم المطعم ورقم الهاتف أولاً", Toast.LENGTH_SHORT).show()
                    } else {
                        onAdd(name, phone, menuImageBase64, logoImageBase64)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("إضافة وحفظ 💾", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.White)
            }
        },
        containerColor = CosmicSurface
    )
}

@Composable
fun RestaurantOrderTrackingPipeline(status: String, modifier: Modifier = Modifier) {
    // Steps:
    // 1. قيد التحضير بالمطعم 🍳 (Active: "معلق", "قيد التحضير". Done: "جاهز للتوصيل" or higher)
    // 2. تم تسليم المندوب 🚴 (Active: contains "المندوب" or "التوصيل" or "تأكيد". Done: "تم التسليم" or higher)
    // 3. تم التسليم ✅ (Active/Done: contains "تم التسليم" or is "تم تسليم العميل وإغلاق الفاتورة ✅")
    
    val step1State = when {
        status == "معلق" || status.contains("التحضير") -> StepState.ACTIVE
        status.contains("جاهز") || status.contains("المندوب") || status.contains("التسليم") || status.contains("تأكيد") || status.contains("إغلاق") -> StepState.DONE
        else -> StepState.AWAITING
    }
    
    val step2State = when {
        status.contains("المندوب") || status.contains("التوصيل") || status.contains("تأكيد") -> {
            if (status.contains("تم التسليم") || status.contains("إغلاق") || status.contains("تسليم العميل")) StepState.DONE else StepState.ACTIVE
        }
        status.contains("تم التسليم") || status.contains("إغلاق") || status.contains("تسليم العميل") -> StepState.DONE
        else -> StepState.AWAITING
    }
    
    val step3State = when {
        status.contains("تم التسليم") || status.contains("إغلاق") || status.contains("تسليم العميل") -> StepState.DONE
        else -> StepState.AWAITING
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(0.2f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CosmicSurfaceVariant.copy(0.5f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step 1: تجهيز الطلب
            TrackingStepItem(
                title = "تجهيز الطلب 🍳",
                state = step1State
            )
            
            // Connection line 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(if (step2State != StepState.AWAITING) CosmicSecondary else Color.DarkGray)
            )
            
            // Step 2: تم تسليم المندوب
            TrackingStepItem(
                title = "تم تسليم المندوب 🚴",
                state = step2State
            )
            
            // Connection line 2
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(if (step3State == StepState.DONE) Color.Green else Color.DarkGray)
            )
            
            // Step 3: تم التسليم
            TrackingStepItem(
                title = "تم التسليم ✅",
                state = step3State
            )
        }
    }
}

enum class StepState {
    AWAITING, ACTIVE, DONE
}

@Composable
fun TrackingStepItem(title: String, state: StepState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = when (state) {
                        StepState.DONE -> Color.Green
                        StepState.ACTIVE -> CosmicSecondary
                        StepState.AWAITING -> Color.DarkGray
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (state) {
                    StepState.DONE -> Icons.Default.Check
                    StepState.ACTIVE -> Icons.Default.DirectionsRun
                    StepState.AWAITING -> Icons.Default.HourglassEmpty
                },
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = when (state) {
                StepState.DONE -> Color.Green
                StepState.ACTIVE -> CosmicSecondary
                StepState.AWAITING -> Color.White.copy(0.5f)
            }
        )
    }
}
