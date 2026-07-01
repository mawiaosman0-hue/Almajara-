package com.example.ui.screens

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.PharmacyEntity
import com.example.data.db.PharmacyOrderEntity
import com.example.data.db.PharmacyProductEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MajarahViewModel
import kotlinx.coroutines.launch

@Composable
fun PharmacyPlanetSection(
    viewModel: MajarahViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val isPharmacist by viewModel.isPharmacist.collectAsStateWithLifecycle()
    val allPharmacies by viewModel.allPharmacies.collectAsStateWithLifecycle()
    val allProducts by viewModel.allPharmacyProducts.collectAsStateWithLifecycle()
    val allOrders by viewModel.allPharmacyOrders.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Starry Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFE91E63), Color(0xFF673AB7))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "💊 كوكب صيدلية المجرة الكونية",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "تصفح مستلزمات طبية، أدوية، ومستحضرات تجميل معتمدة مع إمكانية تصوير ورفع الروشتات.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isPharmacist) {
            // Pharmacist View Panel
            val userEmail = activeProfile?.email?.trim()?.lowercase() ?: ""
            val myPharmacy = allPharmacies.find { it.pharmacistEmail.trim().lowercase() == userEmail }

            if (myPharmacy == null) {
                // Pharmacist has no pharmacy yet -> Ask them to add pharmacy
                PharmacistAddPharmacyForm(viewModel = viewModel, userEmail = userEmail)
            } else {
                // Pharmacist has a pharmacy
                if (!myPharmacy.isApproved) {
                    // Pending approval from manager
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        border = BorderStroke(1.dp, Color(0xFFFFC107)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pending,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "طلب صيدليتك [${myPharmacy.name}] قيد المراجعة ⏳",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "صيدليتك بانتظار موافقة مدير المجرة 🌌 لتتمكن من إضافة أدويتك ومستحضراتك واستقبال روشتات المرضى.",
                                color = MediumContrastTextDark,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    // Approved Pharmacy Panel -> Manage products & incoming prescription orders
                    PharmacistDashboard(
                        viewModel = viewModel,
                        pharmacy = myPharmacy,
                        allProducts = allProducts.filter { it.pharmacyId == myPharmacy.id },
                        allOrders = allOrders.filter { it.pharmacyId == myPharmacy.id }
                    )
                }
            }
        } else {
            // Customer View Panel -> Show approved pharmacies & prescription form
            CustomerPharmacyView(
                viewModel = viewModel,
                approvedPharmacies = allPharmacies.filter { it.isApproved },
                allProducts = allProducts.filter { it.isApproved }
            )
        }
    }
}

// -------------------------------------------------------------
// PHARMACIST ADD PHARMACY FORM
// -------------------------------------------------------------
@Composable
fun PharmacistAddPharmacyForm(
    viewModel: MajarahViewModel,
    userEmail: String
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }
    var hasCosmetics by remember { mutableStateOf(false) }

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
                Toast.makeText(context, "تم التقاط صورة الصيدلية بنجاح! 📸", Toast.LENGTH_SHORT).show()
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
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                val byteArray = outputStream.toByteArray()
                android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
            if (base64 != null) {
                selectedImageBase64 = base64
                Toast.makeText(context, "تم اختيار صورة الصيدلية بنجاح! 🖼️", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        border = BorderStroke(1.dp, CosmicSurfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "🆕 إنشاء وتوثيق صيدليتك بالمجرة الكونية",
                color = CosmicSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Right
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "يرجى ملء البيانات لتوثيق صيدليتك سحابياً لتظهر لمرضى ومشتري المجرة فوراً بعد موافقة الإدارة.",
                color = MediumContrastTextDark,
                fontSize = 10.sp,
                textAlign = TextAlign.Right
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("اسم الصيدلية 🏥", color = CosmicSecondary, fontSize = 11.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicSecondary,
                    unfocusedBorderColor = CosmicSurfaceVariant,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = doctorName,
                onValueChange = { doctorName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("اسم الدكتور المسؤول 🧑‍⚕️", color = CosmicSecondary, fontSize = 11.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicSecondary,
                    unfocusedBorderColor = CosmicSurfaceVariant,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("رقم هاتف واتساب الصيدلية 💬", color = CosmicSecondary, fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicSecondary,
                    unfocusedBorderColor = CosmicSurfaceVariant,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("موقع وعنوان الصيدلية بالتفصيل 📍", color = CosmicSecondary, fontSize = 11.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicSecondary,
                    unfocusedBorderColor = CosmicSurfaceVariant,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pharmacy Image Picker Section
            Text(
                text = "🖼️ صورة لشكل الصيدلية من الخارج أو الداخل:",
                color = CosmicSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Right
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (selectedImageBase64 != null) {
                val bytes = try {
                    android.util.Base64.decode(selectedImageBase64, android.util.Base64.DEFAULT)
                } catch (e: Exception) {
                    null
                }
                if (bytes != null) {
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "صورة الصيدلية المحددة",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
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
                    Text("المعرض 🖼️", fontSize = 11.sp)
                }

                Button(
                    onClick = { cameraLauncher.launch(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("الكاميرا 📸", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cosmetics Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { hasCosmetics = !hasCosmetics }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تفعيل خيار بيع الكوزمتك ومستحضرات التجميل بالصيدلية 💄🌸",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.Checkbox(
                    checked = hasCosmetics,
                    onCheckedChange = { hasCosmetics = it },
                    colors = androidx.compose.material3.CheckboxDefaults.colors(
                        checkedColor = CosmicSecondary,
                        uncheckedColor = CosmicSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    if (name.isBlank() || doctorName.isBlank() || phone.isBlank() || location.isBlank()) {
                        Toast.makeText(context, "الرجاء ملء كافة البيانات المطلوبة ⚠️", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addPharmacy(
                            name = name.trim(),
                            doctorName = doctorName.trim(),
                            phone = phone.trim(),
                            location = location.trim(),
                            pharmacistEmail = userEmail,
                            imageBase64 = selectedImageBase64 ?: "",
                            hasCosmetics = hasCosmetics
                        ) { err ->
                            if (err == null) {
                                Toast.makeText(context, "تم حفظ الصيدلية بنجاح وبانتظار موافقة مدير المجرة للنشر والعمل الفوري! 🎉🏥", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "حدث خطأ: $err", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("توثيق وحفظ صيدليتك سحابياً وبانتظار موافقة المدير 🌌", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// PHARMACIST DASHBOARD
// -------------------------------------------------------------
@Composable
fun PharmacistDashboard(
    viewModel: MajarahViewModel,
    pharmacy: PharmacyEntity,
    allProducts: List<PharmacyProductEntity>,
    allOrders: List<PharmacyOrderEntity>
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Orders, 1: Products
    
    // Play Audible Notification Alarm when a new order with state "بانتظار الصيدلي" is assigned!
    val pendingCount = remember(allOrders) { allOrders.count { it.status == "بانتظار الصيدلي" } }
    var previousPendingCount by remember { mutableStateOf(0) }

    LaunchedEffect(pendingCount) {
        if (pendingCount > previousPendingCount) {
            // Trigger audial alarm notification natively using ToneGenerator!
            try {
                val tg = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                tg.startTone(ToneGenerator.TONE_PROP_BEEP, 1200)
                Toast.makeText(context, "🔔 تنبيه عاجل: تم إرسال روشتة جديدة لصيدليتك بالمجرة!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        previousPendingCount = pendingCount
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        border = BorderStroke(1.dp, CosmicSurfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Green.copy(0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("نشط وموثق 🟢", color = Color.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(pharmacy.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("الدكتور المسؤول: ${pharmacy.doctorName} 🧑‍⚕️", color = MediumContrastTextDark, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs Bar
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = CosmicDeepSpace,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = CosmicSecondary
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (pendingCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(pendingCount.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("الروشتات والطلبات 📥", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("أدوية ومستحضرات الصيدلية 🧪", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (activeTab) {
                0 -> PharmacistOrdersTab(viewModel = viewModel, pharmacyId = pharmacy.id, orders = allOrders)
                1 -> PharmacistProductsTab(viewModel = viewModel, pharmacyId = pharmacy.id, products = allProducts)
            }
        }
    }
}

// -------------------------------------------------------------
// PHARMACIST ORDERS TAB
// -------------------------------------------------------------
@Composable
fun PharmacistOrdersTab(
    viewModel: MajarahViewModel,
    pharmacyId: Int,
    orders: List<PharmacyOrderEntity>
) {
    val context = LocalContext.current
    var selectedOrderForExecution by remember { mutableStateOf<PharmacyOrderEntity?>(null) }

    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("لا توجد روشتات أو طلبات مضافة حالياً لصيدليتك 📭", color = MediumContrastTextDark, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            orders.sortedByDescending { it.createdAt }.forEach { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                    border = BorderStroke(1.dp, CosmicSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (order.status) {
                                            "بانتظار الصيدلي" -> Color.Red.copy(0.15f)
                                            "بانتظار المدير" -> Color(0xFFFF9800).copy(0.15f)
                                            else -> Color.Green.copy(0.15f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = order.status,
                                    color = when (order.status) {
                                        "بانتظار الصيدلي" -> Color.Red
                                        "بانتظار المدير" -> Color(0xFFFF9800)
                                        else -> Color.Green
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text("روشتة من: ${order.customerName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("رقم الهاتف: ${order.customerPhone} 📞", color = MediumContrastTextDark, fontSize = 10.sp)
                        
                        if (order.medicinesJson.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("الفاتورة المقترحة: ${order.medicinesJson}", color = CosmicSecondary, fontSize = 11.sp, textAlign = TextAlign.Right)
                            Text("السعر الإجمالي للأدوية: ${viewModel.formatPrice(order.medicinePrice)}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        if (order.prescriptionImageBase64.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val bitmap = remember(order.prescriptionImageBase64) {
                                try {
                                    val cleanBase64 = if (order.prescriptionImageBase64.contains(",")) order.prescriptionImageBase64.substringAfter(",") else order.prescriptionImageBase64
                                    val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "صورة الروشتة المرفوعة",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            // Optional fullscreen pop
                                        },
                                    contentScale = ContentScale.Inside
                                )
                            }
                        }

                        if (order.status == "بانتظار الصيدلي") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { selectedOrderForExecution = order },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("تنفيذ الروشتة وإضافة أسعار الأدوية 🛠️💊", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Execute Order Dialog popup
    if (selectedOrderForExecution != null) {
        val ord = selectedOrderForExecution!!
        var medsText by remember { mutableStateOf("") }
        var totalAmountInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedOrderForExecution = null },
            containerColor = CosmicSurface,
            title = { Text("تسعير وتنفيذ الروشتة الطبية 📝", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Text("قم بكتابة الأدوية المتوفرة وأسعارها (مثال: بندول: 1500، مضاد حيوي: 4000):", color = MediumContrastTextDark, fontSize = 11.sp, textAlign = TextAlign.Right)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = medsText,
                        onValueChange = { medsText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("قائمة الأدوية المتوفرة وتسعيرها بالتفصيل...", color = Color.Gray, fontSize = 11.sp) },
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = totalAmountInput,
                        onValueChange = { totalAmountInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("السعر الإجمالي للأدوية فقط (ج.س) 💰", color = CosmicSecondary, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedPrice = totalAmountInput.toDoubleOrNull() ?: 0.0
                        if (medsText.isBlank() || parsedPrice <= 0.0) {
                            Toast.makeText(context, "الرجاء كتابة الفاتورة والسعر الإجمالي بصيغة صحيحة ⚠️", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.pharmacistExecuteOrder(ord.id, medsText.trim(), parsedPrice) { err ->
                                if (err == null) {
                                    Toast.makeText(context, "تم رفع الطلب بنجاح إلى الإدارة لتحديد المندوب والرسوم! 🌌🚀", Toast.LENGTH_LONG).show()
                                    selectedOrderForExecution = null
                                } else {
                                    Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary)
                ) {
                    Text("رفع الفاتورة للمدير 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOrderForExecution = null }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// PHARMACIST PRODUCTS TAB
// -------------------------------------------------------------
@Composable
fun PharmacistProductsTab(
    viewModel: MajarahViewModel,
    pharmacyId: Int,
    products: List<PharmacyProductEntity>
) {
    val context = LocalContext.current
    var showAddProductDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Button(
            onClick = { showAddProductDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة دواء أو كوزمتك جديد للعيادة ➕🏥", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("لم تقم بإضافة أي منتجات أو أدوية في صيدليتك حالياً 📭", color = MediumContrastTextDark, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                products.forEach { prod ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                        border = BorderStroke(1.dp, CosmicSurfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                viewModel.deletePharmacyProduct(prod.id) { err ->
                                    if (err == null) {
                                        Toast.makeText(context, "تم الحذف بنجاح! 🗑️", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Delete, "حذف", tint = Color.Red.copy(0.7f))
                            }

                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                Text(prod.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("الشركة: ${prod.company} | تصنيف: ${prod.type}", color = MediumContrastTextDark, fontSize = 9.sp)
                                Text("السعر: ${viewModel.formatPrice(prod.price)}", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                if (!prod.isApproved) {
                                    Text("بانتظار موافقة المدير ⏳", color = Color(0xFFFF9800), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("معتمد ومعروض للجميع ✨", color = Color.Green, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (prod.imageBase64.isNotBlank()) {
                                val bitmap = remember(prod.imageBase64) {
                                    try {
                                        val cleanBase64 = if (prod.imageBase64.contains(",")) prod.imageBase64.substringAfter(",") else prod.imageBase64
                                        val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                                        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "صورة الدواء",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CosmicSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Medication, null, tint = CosmicSecondary, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Product Dialog Popup
    if (showAddProductDialog) {
        var pName by remember { mutableStateOf("") }
        var pCompany by remember { mutableStateOf("") }
        var pPrice by remember { mutableStateOf("") }
        var pType by remember { mutableStateOf("دواء") } // "دواء" or "كوزمتك"
        var pImageBase64 by remember { mutableStateOf("") }

        val imagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                try {
                    val stream = context.contentResolver.openInputStream(it)
                    val bytes = stream?.readBytes()
                    if (bytes != null) {
                        pImageBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                        Toast.makeText(context, "📸 تم اختيار الصورة بنجاح!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            containerColor = CosmicSurface,
            title = { Text("إضافة مستحضر طبي أو تجميلي 🧪🏥", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("اسم الدواء أو المنتج 🧪", color = CosmicSecondary, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pCompany,
                        onValueChange = { pCompany = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("الشركة المصنعة 🏢", color = CosmicSecondary, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pPrice,
                        onValueChange = { pPrice = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("السعر بالجنيه السوداني (ج.س) 💰", color = CosmicSecondary, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("تصنيف ونوع المنتج الكوني:", color = MediumContrastTextDark, fontSize = 10.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = pType == "كوزمتك", onClick = { pType = "كوزمتك" }, colors = RadioButtonDefaults.colors(selectedColor = CosmicSecondary))
                        Text("مستحضر تجميلي / كوزمتك 💄", color = Color.White, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = pType == "دواء", onClick = { pType = "دواء" }, colors = RadioButtonDefaults.colors(selectedColor = CosmicSecondary))
                        Text("دواء علاجي 💊", color = Color.White, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CosmicSecondary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicSecondary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (pImageBase64.isNotBlank()) "تغيير الصورة المرفقة 📸" else "إرفاق صورة الدواء/المنتج 📸", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = pPrice.toDoubleOrNull() ?: 0.0
                        if (pName.isBlank() || pCompany.isBlank() || parsed <= 0.0) {
                            Toast.makeText(context, "الرجاء إدخال كافة الحقول بصيغة صحيحة ⚠️", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addPharmacyProduct(pharmacyId, pType, pName.trim(), pCompany.trim(), parsed, pImageBase64) { err ->
                                if (err == null) {
                                    Toast.makeText(context, "تم حفظ المنتج بنجاح وبانتظار موافقة المدير! ✨🏥", Toast.LENGTH_LONG).show()
                                    showAddProductDialog = false
                                } else {
                                    Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary)
                ) {
                    Text("إضافة ونشر 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// CUSTOMER PHARMACY VIEW
// -------------------------------------------------------------
@Composable
fun CustomerPharmacyView(
    viewModel: MajarahViewModel,
    approvedPharmacies: List<PharmacyEntity>,
    allProducts: List<PharmacyProductEntity>
) {
    val context = LocalContext.current
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allOrders by viewModel.allPharmacyOrders.collectAsStateWithLifecycle()
    var selectedPharmacyForDetails by remember { mutableStateOf<PharmacyEntity?>(null) }
    var showPrescriptionFormForPharmacy by remember { mutableStateOf<PharmacyEntity?>(null) }
    var activeSubTab by remember { mutableStateOf(0) } // 0: Pharmacies, 1: My Prescriptions

    val myPharmacyOrders = remember(allOrders, activeProfile) {
        val email = activeProfile?.email?.trim()?.lowercase() ?: ""
        val phone = activeProfile?.phone?.trim() ?: ""
        allOrders.filter {
            (email.isNotBlank() && it.customerEmail.trim().lowercase() == email) ||
            (phone.isNotBlank() && it.customerPhone.trim() == phone)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Tab row for Customer Pharmacy View
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = CosmicDeepSpace,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                    color = CosmicSecondary
                )
            },
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("الصيدليات الطبية 🏥", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (myPharmacyOrders.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(myPharmacyOrders.size.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("روشتاتي وطلباتي 📋", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (activeSubTab == 0) {
            if (approvedPharmacies.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, CosmicSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MedicalServices, null, tint = CosmicSecondary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("لا توجد صيدليات نشطة حالياً في كوكب الصيدلية بالمجرة 📭", color = MediumContrastTextDark, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "🏥 الصيدليات الطبية المعتمدة في المجرة الكونية:",
                        color = CosmicSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    approvedPharmacies.forEach { pharmacy ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPharmacyForDetails = pharmacy },
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
                                    Icon(Icons.Default.ArrowBackIosNew, null, tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                                    Text(pharmacy.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("الدكتور المسؤول: د. ${pharmacy.doctorName} 🧑‍⚕️", color = MediumContrastTextDark, fontSize = 10.sp)
                                Text("الموقع والفرع: ${pharmacy.location} 📍", color = Color.White.copy(0.7f), fontSize = 10.sp)
                                
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showPrescriptionFormForPharmacy = pharmacy },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CameraAlt, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تصوير وإضافة روشتة 📸✍️", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val cleanNum = pharmacy.phone.trim().replace(" ", "").replace("+", "")
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                    data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$cleanNum")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "الرقم غير صالح لـ WhatsApp: ${pharmacy.phone}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFF25D366)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366)),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Chat, null, tint = Color(0xFF25D366), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تواصل عبر واتساب 💬", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // My Prescriptions / Submitted Orders
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (myPharmacyOrders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد روشتات أو طلبات طبية سابقة لك 📭", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(myPharmacyOrders) { order ->
                        val pharm = approvedPharmacies.find { it.id == order.pharmacyId }
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
                                    Text(
                                        text = order.status,
                                        color = when (order.status) {
                                            "بانتظار الصيدلي" -> Color.Red
                                            "بانتظار المدير" -> Color(0xFFFF9800)
                                            "تم التوصيل" -> Color.Green
                                            else -> CosmicSecondary
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(
                                                when (order.status) {
                                                    "بانتظار الصيدلي" -> Color.Red.copy(0.12f)
                                                    "بانتظار المدير" -> Color(0xFFFF9800).copy(0.12f)
                                                    "تم التوصيل" -> Color.Green.copy(0.12f)
                                                    else -> CosmicSecondary.copy(0.12f)
                                                },
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                    Text(
                                        text = "طلب روشتة #${order.id}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("الصيدلية: ${pharm?.name ?: "صيدلية معتمدة بالمجرة"}", color = CosmicSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                
                                if (order.medicinesJson.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("الأدوية المسعرة: ${order.medicinesJson}", color = Color.White.copy(0.8f), fontSize = 11.sp, textAlign = TextAlign.Right)
                                    Text("قيمة العلاج: ${viewModel.formatPrice(order.medicinePrice)} SDG", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("توصيل الدواء مجان 🌸", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("رسوم التوصيل 🚚:", color = Color.White.copy(0.6f), fontSize = 10.sp)
                                }

                                if (order.courierName.isNotBlank()) {
                                    Text("المندوب المعين: ${order.courierName} (${order.courierPhone}) 🚴", color = Color.White, fontSize = 10.sp)
                                }

                                // Display "بالشفاء العاجل لك إن شاء الله 🤲✨" when order.status == "تم التوصيل"
                                if (order.status == "تم التوصيل") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.Green.copy(0.12f)),
                                        border = BorderStroke(1.dp, Color.Green.copy(0.3f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "بالشفاء العاجل لك ان شاء الله",
                                                color = Color.Green,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center
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

    // Pharmacy Details Dialog/Overlay
    if (selectedPharmacyForDetails != null) {
        val pharm = selectedPharmacyForDetails!!
        val pharmacyProducts = allProducts.filter { it.pharmacyId == pharm.id }

        AlertDialog(
            onDismissRequest = { selectedPharmacyForDetails = null },
            containerColor = CosmicSurface,
            title = {
                Text(
                    text = pharm.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("تفاصيل الصيدلية الكونية 🌌:", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🏥 الصيدلية: ${pharm.name}", color = Color.White, fontSize = 11.sp)
                    Text("🧑‍⚕️ الدكتور المسؤول: د. ${pharm.doctorName}", color = Color.White, fontSize = 11.sp)
                    Text("📍 الموقع: ${pharm.location}", color = Color.White, fontSize = 11.sp)
                    Text("💬 واتساب: ${pharm.phone}", color = Color.White, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("🧪 المنتجات الطبية المتوفرة بصيدليتنا:", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (pharmacyProducts.isEmpty()) {
                        Text("لا توجد منتجات معروضة حالياً لهذه الصيدلية 📭", color = MediumContrastTextDark, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    } else {
                        pharmacyProducts.forEach { p ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                                border = BorderStroke(1.dp, CosmicSurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                                        Text(p.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("الشركة: ${p.company}", color = MediumContrastTextDark, fontSize = 9.sp)
                                        Text("السعر: ${viewModel.formatPrice(p.price)}", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }

                                    if (p.imageBase64.isNotBlank()) {
                                        val bitmap = remember(p.imageBase64) {
                                            try {
                                                val cleanBase64 = if (p.imageBase64.contains(",")) p.imageBase64.substringAfter(",") else p.imageBase64
                                                val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(45.dp)
                                                    .clip(RoundedCornerShape(6.dp)),
                                                contentScale = ContentScale.Crop
                                            )
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
                    onClick = {
                        showPrescriptionFormForPharmacy = pharm
                        selectedPharmacyForDetails = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary)
                ) {
                    Text("طلب دواء / روشتة 📸", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPharmacyForDetails = null }) {
                    Text("إغلاق", color = Color.White)
                }
            }
        )
    }

    // Prescription Upload Form Popup
    if (showPrescriptionFormForPharmacy != null) {
        val pharm = showPrescriptionFormForPharmacy!!
        var custName by remember { mutableStateOf(activeProfile?.name ?: "") }
        var custPhone by remember { mutableStateOf(activeProfile?.phone ?: "") }
        var prescriptionImageBase64 by remember { mutableStateOf("") }

        val imagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                try {
                    val stream = context.contentResolver.openInputStream(it)
                    val bytes = stream?.readBytes()
                    if (bytes != null) {
                        prescriptionImageBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                        Toast.makeText(context, "📸 تم تصوير وإرفاق الروشتة بنجاح!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showPrescriptionFormForPharmacy = null },
            containerColor = CosmicSurface,
            title = { Text("تقديم روشتة طبية أو طلب دواء 📸💊", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    // Warning Notice Message to Customer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE91E63).copy(0.12f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 تنبيه: سيتم إرجاع فاتورة إليك بالأدوية المطلوبة والمتوفرة فقط شاملة السعر الإجمالي ورسوم التوصيل والجامع النهائي.",
                            color = Color(0xFFFF4081),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Right,
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = custName,
                        onValueChange = { custName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("اسم المريض بالكامل 👤", color = CosmicSecondary, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = custPhone,
                        onValueChange = { custPhone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("رقم هاتفك للتواصل والتوصيل 📞", color = CosmicSecondary, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CosmicSecondary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicSecondary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (prescriptionImageBase64.isNotBlank()) "تم تصوير الروشتة بنجاح ✅" else "تصوير أو إرفاق الروشتة 📸", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (custName.isBlank() || custPhone.isBlank() || prescriptionImageBase64.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال اسمك ورقم هاتفك وتصوير الروشتة الطبية ⚠️", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addPharmacyOrder(
                                pharmacyId = pharm.id,
                                customerName = custName.trim(),
                                customerPhone = custPhone.trim(),
                                customerEmail = activeProfile?.email ?: "",
                                prescriptionBase64 = prescriptionImageBase64
                            ) { err ->
                                if (err == null) {
                                    Toast.makeText(context, "تم إرسال روشتتك بنجاح! سيتم إرجاع الفاتورة والتسعيرة إليك للتأكيد الفوري! 🌌💊", Toast.LENGTH_LONG).show()
                                    showPrescriptionFormForPharmacy = null
                                } else {
                                    Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary)
                ) {
                    Text("تقديم الطلب للصيدلية 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrescriptionFormForPharmacy = null }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// ADMIN PHARMACY PORTAL
// -------------------------------------------------------------
@Composable
fun AdminPharmacyPortal(
    viewModel: MajarahViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pharmacies by viewModel.allPharmacies.collectAsStateWithLifecycle()
    val products by viewModel.allPharmacyProducts.collectAsStateWithLifecycle()
    val orders by viewModel.allPharmacyOrders.collectAsStateWithLifecycle()
    val couriers by viewModel.allCouriers.collectAsStateWithLifecycle()
    val isGeneralAdmin by viewModel.isGeneralAdmin.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Pharmacies, 1: Products, 2: Orders

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Sub tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val subTabs = listOf(
                "طلبات الروشتات من الصيدلي للتوصيل 📥" to 2,
                "الأدوية والمنتجات 🧪" to 1,
                "توثيق الصيدليات 🏥" to 0
            )
            subTabs.forEach { (label, index) ->
                val isSelected = activeSubTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) CosmicSecondary else CosmicSurface)
                        .clickable { activeSubTab = index }
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

        Spacer(modifier = Modifier.height(16.dp))

        when (activeSubTab) {
            0 -> {
                // Pharmacies management
                if (pharmacies.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد صيدليات مسجلة بالمجرة بعد 🏥", color = MediumContrastTextDark, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)) {
                        items(pharmacies) { pharmacy ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(1.dp, CosmicSurfaceVariant),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (pharmacy.isApproved) Color.Green.copy(0.15f) else Color.Red.copy(0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (pharmacy.isApproved) "معتمدة وموثقة ✅" else "بانتظار الموافقة ⏳",
                                                color = if (pharmacy.isApproved) Color.Green else Color.Red,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(pharmacy.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("الدكتور المسؤول: د. ${pharmacy.doctorName} 🧑‍⚕️", color = MediumContrastTextDark, fontSize = 10.sp)
                                    Text("البريد الإلكتروني للصيدلي: ${pharmacy.pharmacistEmail}", color = MediumContrastTextDark, fontSize = 10.sp)
                                    Text("رقم الهاتف: ${pharmacy.phone} 💬", color = Color.White.copy(0.7f), fontSize = 10.sp)
                                    Text("الموقع: ${pharmacy.location} 📍", color = Color.White.copy(0.7f), fontSize = 10.sp)

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (!isGeneralAdmin) {
                                                    Toast.makeText(context, "عذراً، حذف الصيدليات حصرية للمدير العام فقط 🔒", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    viewModel.deletePharmacy(pharmacy.id) { err ->
                                                        if (err == null) {
                                                            Toast.makeText(context, "تم حذف الصيدلية بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isGeneralAdmin) Color.Red.copy(0.2f) else Color.Gray.copy(0.15f),
                                                contentColor = if (isGeneralAdmin) Color.Red else Color.Gray
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("حذف 🗑️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        if (!pharmacy.isApproved) {
                                            Button(
                                                onClick = {
                                                    if (!isGeneralAdmin) {
                                                        Toast.makeText(context, "عذراً، اعتماد وقبول الصيدليات ميزة حصرية للمدير العام فقط 🔒", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        viewModel.approvePharmacy(pharmacy.id) { err ->
                                                            if (err == null) {
                                                                Toast.makeText(context, "تم اعتماد وتوثيق الصيدلية بنجاح! 🎉🏥", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1.5f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isGeneralAdmin) Color.Green else Color.Gray.copy(0.2f),
                                                    contentColor = if (isGeneralAdmin) Color.Black else Color.Gray
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("قبول وتوثيق ✅", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Products management
                if (products.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد منتجات صيدلية معروضة بعد 🧪", color = MediumContrastTextDark, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)) {
                        items(products) { prod ->
                            val pharm = pharmacies.find { it.id == prod.pharmacyId }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(1.dp, CosmicSurfaceVariant),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.Start) {
                                        IconButton(onClick = {
                                            viewModel.deletePharmacyProduct(prod.id) { err ->
                                                if (err == null) {
                                                    Toast.makeText(context, "تم حذف المنتج 🗑️", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Default.Delete, "حذف", tint = Color.Red.copy(0.7f))
                                        }

                                        if (!prod.isApproved) {
                                            IconButton(onClick = {
                                                viewModel.approvePharmacyProduct(prod.id) { err ->
                                                    if (err == null) {
                                                        Toast.makeText(context, "تم اعتماد ونشر المنتج بنجاح! ✅🧪", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }) {
                                                Icon(Icons.Default.Check, "موافقة", tint = Color.Green)
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                        Text(prod.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("الشركة: ${prod.company} | نوع: ${prod.type}", color = MediumContrastTextDark, fontSize = 9.sp)
                                        Text("الصيدلية المصدر: ${pharm?.name ?: "مجهولة"}", color = CosmicSecondary, fontSize = 9.sp)
                                        Text("السعر الكوني: ${viewModel.formatPrice(prod.price)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        if (!prod.isApproved) {
                                            Text("بانتظار موافقة الإدارة ⏳", color = Color(0xFFFF9800), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("معتمد للجميع 🟢", color = Color.Green, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (prod.imageBase64.isNotBlank()) {
                                        val bitmap = remember(prod.imageBase64) {
                                            try {
                                                val clean = if (prod.imageBase64.contains(",")) prod.imageBase64.substringAfter(",") else prod.imageBase64
                                                val bytes = android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
                                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Orders (prescription) management
                if (orders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد طلبيات أو روشتات مضافة حالياً 📥", color = MediumContrastTextDark, fontSize = 12.sp)
                    }
                } else {
                    var selectedOrderForApprove by remember { mutableStateOf<PharmacyOrderEntity?>(null) }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)) {
                        items(orders.sortedByDescending { it.createdAt }) { order ->
                            val pharm = pharmacies.find { it.id == order.pharmacyId }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(1.dp, CosmicSurfaceVariant),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    when (order.status) {
                                                        "بانتظار الصيدلي" -> Color.Red.copy(0.12f)
                                                        "بانتظار المدير" -> Color(0xFFFF9800).copy(0.12f)
                                                        else -> Color.Green.copy(0.12f)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = order.status,
                                                color = when (order.status) {
                                                    "بانتظار الصيدلي" -> Color.Red
                                                    "بانتظار المدير" -> Color(0xFFFF9800)
                                                    else -> Color.Green
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text("روشتة من العميل: ${order.customerName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("هاتف العميل: ${order.customerPhone} 📞", color = MediumContrastTextDark, fontSize = 10.sp)
                                    Text("الصيدلية المصدر: ${pharm?.name ?: "مجهولة"}", color = CosmicSecondary, fontSize = 10.sp)
                                    
                                    if (order.medicinesJson.isNotBlank()) {
                                        Text("قائمة الأدوية والأسعار: ${order.medicinesJson}", color = Color.White.copy(0.8f), fontSize = 11.sp, textAlign = TextAlign.Right)
                                        Text("قيمة العلاج الإجمالية: ${viewModel.formatPrice(order.medicinePrice)}", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }

                                    if (order.deliveryFee > 0) {
                                        Text("رسوم التوصيل المحددة: ${viewModel.formatPrice(order.deliveryFee)}", color = Color.White, fontSize = 10.sp)
                                        Text("المندوب المعين: ${order.courierName} (${order.courierPhone}) 🚴", color = Color.White, fontSize = 10.sp)
                                    }

                                    if (order.prescriptionImageBase64.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val bitmap = remember(order.prescriptionImageBase64) {
                                            try {
                                                val clean = if (order.prescriptionImageBase64.contains(",")) order.prescriptionImageBase64.substringAfter(",") else order.prescriptionImageBase64
                                                val bytes = android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
                                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(130.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Inside
                                            )
                                        }
                                    }

                                    if (order.status == "بانتظار المدير") {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { selectedOrderForApprove = order },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("تعيين المندوب وتأكيد السعر النهائي والرسوم 🚴✅", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Assign Courier and Delivery Fee Dialog
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
                                    Text("أدوية العميل: ${ord.medicinesJson}", color = MediumContrastTextDark, fontSize = 11.sp, textAlign = TextAlign.Right)
                                    Text("قيمة الدواء: ${viewModel.formatPrice(ord.medicinePrice)}", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    
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
                                            viewModel.adminApprovePharmacyOrder(ord.id, courierSelection, courierPhoneSelection, fee) { err ->
                                                if (err == null) {
                                                    Toast.makeText(context, "تم تأكيد الفاتورة ونشر الطلب للمريض بنجاح! 🎉🚴", Toast.LENGTH_LONG).show()
                                                    selectedOrderForApprove = null
                                                } else {
                                                    Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary)
                                ) {
                                    Text("اعتماد الفاتورة للمريض 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { selectedOrderForApprove = null }) {
                                    Text("إلغاء", color = Color.White)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
