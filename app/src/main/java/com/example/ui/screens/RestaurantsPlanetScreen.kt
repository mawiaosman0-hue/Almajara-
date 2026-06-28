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
fun PharmacyPlanetSection(viewModel: MajarahViewModel) {
    // Placeholder to make sure any imports of PharmacyPlanetSection compile properly if called here.
}

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

    var activeSubTab by remember { mutableStateOf(if (forceAdminPortal && isAdmin) 2 else 0) } // 0: Available Restaurants, 1: My Orders, 2: Manage (Admin only)
    var searchQuery by remember { mutableStateOf("") }

    // Bottom Sheet state for Order Dialog
    var selectedRestaurantForOrder by remember { mutableStateOf<RestaurantEntity?>(null) }
    var showAddRestaurantDialog by remember { mutableStateOf(false) }
    var selectedOrderForInvoice by remember { mutableStateOf<RestaurantOrderEntity?>(null) }

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
            item {
                TabButton(
                    text = "المطاعم المتاحة 🍔",
                    isSelected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 }
                )
            }
            item {
                TabButton(
                    text = "طلباتي السابقة 📑",
                    isSelected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 }
                )
            }
            if (isAdmin) {
                item {
                    TabButton(
                        text = "إدارة المطاعم ⚙️",
                        isSelected = activeSubTab == 2,
                        onClick = { activeSubTab = 2 }
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

                    val filtered = restaurants.filter { it.name.contains(searchQuery, ignoreCase = true) }
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
                                    onOrderClick = { selectedRestaurantForOrder = rest },
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
                // User Orders
                val myEmail = activeProfile?.email ?: ""
                val myOrders = orders.filter { it.customerEmail == myEmail }
                if (myOrders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ليس لديك طلبات سابقة من المطاعم 📝", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myOrders) { ord ->
                            RestaurantOrderCard(
                                order = ord,
                                onShowInvoice = { selectedOrderForInvoice = ord },
                                isAdmin = false,
                                onStatusChange = null
                            )
                        }
                    }
                }
            }
            2 -> {
                // Admin Portal
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
                                }
                            )
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
            onSubmitOrder = { orderText, notesText, paymentMethod, receiptBase64 ->
                val name = activeProfile?.name ?: "عميل المجرة"
                val phone = activeProfile?.phone ?: ""
                val email = activeProfile?.email ?: ""
                val deliveryFee = 1500.0 // Fixed delivery fee for restaurants or calculated

                viewModel.addRestaurantOrder(
                    restaurantId = selectedRestaurantForOrder!!.id,
                    restaurantName = selectedRestaurantForOrder!!.name,
                    restaurantPhone = selectedRestaurantForOrder!!.phone,
                    customerName = name,
                    customerPhone = phone,
                    customerEmail = email,
                    itemsAndNotes = "$orderText\nملاحظات: $notesText",
                    paymentMethod = paymentMethod,
                    deliveryFee = deliveryFee,
                    bankReceiptImageUri = receiptBase64
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

    // Dialog for adding a restaurant (Admin Portal)
    if (showAddRestaurantDialog) {
        AddRestaurantDialog(
            onDismiss = { showAddRestaurantDialog = false },
            onAdd = { name, phone, menuImageBase64 ->
                viewModel.addRestaurant(name, phone, menuImageBase64) { err ->
                    if (err == null) {
                        Toast.makeText(context, "تمت إضافة المطعم بنجاح! 🍔🎉", Toast.LENGTH_LONG).show()
                        showAddRestaurantDialog = false
                    } else {
                        Toast.makeText(context, "فشل إضافة المطعم: $err", Toast.LENGTH_LONG).show()
                    }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOrderClick() },
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CosmicSurfaceVariant)
    ) {
        Column {
            // Menu Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
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

                // Title overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = restaurant.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "📞 ${restaurant.phone}",
                            color = CosmicSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Bottom Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isGeneralAdmin) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, "حذف", tint = Color.Red.copy(0.8f))
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = onOrderClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.RestaurantMenu, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اطلب الآن 🍔", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
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
    onStatusChange: ((String) -> Unit)?
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
                
                val statusColor = when (order.status) {
                    "معلق" -> Color(0xFFFFB300)
                    "قيد التحضير" -> CosmicSecondary
                    else -> Color.Green
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

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = order.itemsAndNotes,
                color = Color.White.copy(0.8f),
                fontSize = 12.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "طريقة الدفع: ${order.paymentMethod}",
                    color = Color.White.copy(0.6f),
                    fontSize = 11.sp
                )
                Text(
                    text = "العميل: ${order.customerName}",
                    color = Color.White.copy(0.6f),
                    fontSize = 11.sp
                )
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
    onSubmitOrder: (order: String, notes: String, payment: String, receiptBase64: String?) -> Unit
) {
    val context = LocalContext.current
    var orderText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("كاش") } // كاش , تحويل بنكي
    var receiptBase64 by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val base64 = try {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val bytes = outputStream.toByteArray()
                Base64.encodeToString(bytes, Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
            if (base64 != null) {
                receiptBase64 = base64
                Toast.makeText(context, "تم التقاط إشعار التحويل! 📸", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
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
                val bytes = outputStream.toByteArray()
                Base64.encodeToString(bytes, Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
            if (base64 != null) {
                receiptBase64 = base64
                Toast.makeText(context, "تم اختيار إشعار التحويل! 🖼️", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                    Text("طريقة الدفع الكونية 💳", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("كاش", "تحويل بنكي").forEach { m ->
                            val isSel = paymentMethod == m
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) CosmicSecondary.copy(0.15f) else CosmicSurface)
                                    .border(1.5.dp, if (isSel) CosmicSecondary else CosmicSurfaceVariant, RoundedCornerShape(8.dp))
                                    .clickable { paymentMethod = m }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(m, color = if (isSel) CosmicSecondary else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (paymentMethod == "تحويل بنكي") {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                            border = BorderStroke(1.dp, CosmicSurfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "حساب بنك الخرطوم للتحويل:\n3414879 باسم معاوية عثمان",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("الرجاء إضافة صورة إشعار التحويل البنكي:", color = CosmicSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { cameraLauncher.launch(null) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("الكاميرا 📸", fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Photo, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("المعرض 🖼️", fontSize = 10.sp)
                                    }
                                }
                                
                                if (receiptBase64 != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("تم إرفاق إشعار الدفع بنجاح! ✅", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (orderText.isBlank()) {
                        Toast.makeText(context, "الرجاء كتابة طلبك أولاً", Toast.LENGTH_SHORT).show()
                    } else if (paymentMethod == "تحويل بنكي" && receiptBase64 == null) {
                        Toast.makeText(context, "الرجاء إرفاق صورة الإشعار البنكي لتأكيد الدفع", Toast.LENGTH_SHORT).show()
                    } else {
                        onSubmitOrder(orderText, notesText, paymentMethod, receiptBase64)
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
                            InvoiceRow(label = "طريقة الدفع", value = order.paymentMethod)
                            InvoiceRow(label = "رسوم التوصيل الكوني", value = "${order.deliveryFee} ج.س")
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
            }
        },
        confirmButton = {
            Button(
                onClick = onShareWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("إرسال للمطعم عبر WhatsApp 💬", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق الفاتورة", color = Color.White)
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
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, menuImageBase64: String?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var menuImageBase64 by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val base64 = try {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val bytes = outputStream.toByteArray()
                Base64.encodeToString(bytes, Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
            if (base64 != null) {
                menuImageBase64 = base64
                Toast.makeText(context, "تم التقاط المنيو! 📸", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
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
                val bytes = outputStream.toByteArray()
                Base64.encodeToString(bytes, Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
            if (base64 != null) {
                menuImageBase64 = base64
                Toast.makeText(context, "تم اختيار المنيو من المعرض! 🖼️", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "إضافة مطعم جديد للمجرة 🌌",
                color = CosmicSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("رقم هاتف الطلبات 📞", color = CosmicSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CosmicSecondary,
                        unfocusedBorderColor = CosmicSurfaceVariant
                    )
                )

                Text("صورة قائمة الطعام والمنيو:", color = Color.White.copy(0.8f), fontSize = 11.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("التقاط صورة 📸", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Photo, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("من المعرض 🖼️", fontSize = 11.sp)
                    }
                }

                if (menuImageBase64 != null) {
                    Text("تم تحميل صورة المنيو بنجاح! ✅", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        Toast.makeText(context, "الرجاء ملء جميع الحقول أولاً", Toast.LENGTH_SHORT).show()
                    } else {
                        onAdd(name, phone, menuImageBase64)
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
