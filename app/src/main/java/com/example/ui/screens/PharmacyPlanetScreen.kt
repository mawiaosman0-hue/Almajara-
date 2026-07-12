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
import androidx.compose.foundation.lazy.itemsIndexed
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
                        text = "💊 كوكب صيدلية المجرة للتسوق",
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

            var showEditPharmacyDialog by remember { mutableStateOf(false) }

            if (showEditPharmacyDialog && myPharmacy != null) {
                EditPharmacyDialog(
                    pharmacy = myPharmacy,
                    onDismiss = { showEditPharmacyDialog = false },
                    onSave = { name, doctorName, phone, location, imageBase64, hasCosmetics ->
                        val updated = myPharmacy.copy(
                            name = name,
                            doctorName = doctorName,
                            phone = phone,
                            location = location,
                            imageBase64 = imageBase64,
                            hasCosmetics = hasCosmetics
                        )
                        viewModel.updatePharmacy(updated) { err ->
                            if (err == null) {
                                Toast.makeText(context, "تم تحديث بيانات الصيدلية بنجاح! ✅", Toast.LENGTH_SHORT).show()
                                showEditPharmacyDialog = false
                            } else {
                                Toast.makeText(context, "حدث خطأ: $err", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            if (myPharmacy == null) {
                // Pharmacist has no pharmacy yet -> Ask them to add pharmacy
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PharmacistAddPharmacyForm(viewModel = viewModel, userEmail = userEmail)
                }
            } else {
                // Pharmacist has a pharmacy
                if (!myPharmacy.isApproved) {
                    var editName by remember(myPharmacy.id) { mutableStateOf(myPharmacy.name) }
                    var editDoctorName by remember(myPharmacy.id) { mutableStateOf(myPharmacy.doctorName) }
                    var editPhone by remember(myPharmacy.id) { mutableStateOf(myPharmacy.phone) }
                    var editLocation by remember(myPharmacy.id) { mutableStateOf(myPharmacy.location) }
                    var editHasCosmetics by remember(myPharmacy.id) { mutableStateOf(myPharmacy.hasCosmetics) }
                    var editImageBase64 by remember(myPharmacy.id) { mutableStateOf(myPharmacy.imageBase64) }

                    val cameraLauncher = rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
                    ) { bitmap ->
                        if (bitmap != null) {
                            try {
                                val outputStream = java.io.ByteArrayOutputStream()
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                                val bytes = outputStream.toByteArray()
                                editImageBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                                Toast.makeText(context, "تم التقاط صورة اللوقو بنجاح! 📸", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "حدث خطأ أثناء معالجة الصورة", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    val galleryLauncher = rememberLauncherForActivityResult(
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
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                                val bytes = outputStream.toByteArray()
                                editImageBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                                Toast.makeText(context, "تم اختيار صورة اللوقو بنجاح! 🖼️", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "حدث خطأ أثناء معالجة الصورة", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
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
                                        text = "حالة الصيدلية: في انتظار موافقة المدير ⏳",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "طلب صيدليتك [${myPharmacy.name}] قيد المراجعة حالياً من قبل إدارة منظومة المجرة الذكية لضمان سلامة المرضى وتوثيق الرخص الطبية الخاصة بك.",
                                        color = MediumContrastTextDark,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                border = BorderStroke(1.2.dp, CosmicSecondary.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        "صيدلية تحتاج تعديل وإرسال الطلب للمدير للموافقة والنشر 🛠️💊",
                                        color = CosmicSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Form fields
                                    Text("اسم الصيدلية:", color = Color.White.copy(0.8f), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = CosmicSurfaceVariant
                                        ),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("اسم الدكتور الصيدلي المسؤول:", color = Color.White.copy(0.8f), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = editDoctorName,
                                        onValueChange = { editDoctorName = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = CosmicSurfaceVariant
                                        ),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("رقم هاتف التواصل والواتساب:", color = Color.White.copy(0.8f), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = editPhone,
                                        onValueChange = { editPhone = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = CosmicSurfaceVariant
                                        ),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("موقع وعنوان الصيدلية بالتفصيل:", color = Color.White.copy(0.8f), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = editLocation,
                                        onValueChange = { editLocation = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = CosmicSecondary,
                                            unfocusedBorderColor = CosmicSurfaceVariant
                                        ),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Cosmetics checkbox
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { editHasCosmetics = !editHasCosmetics }
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "صيدليتي توفر مستحضرات التجميل والعناية بالبشرة 🧴",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Checkbox(
                                            checked = editHasCosmetics,
                                            onCheckedChange = { editHasCosmetics = it },
                                            colors = CheckboxDefaults.colors(checkedColor = CosmicSecondary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Logo & image selection
                                    Text("شعار / لوقو الصيدلية الحالي:", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CosmicDeepSpace),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (editImageBase64.isNotBlank()) {
                                            val bitmap = remember(editImageBase64) {
                                                try {
                                                    val clean = if (editImageBase64.contains(",")) editImageBase64.substringAfter(",") else editImageBase64
                                                    val bytes = android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
                                                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                } catch (e: Exception) {
                                                    null
                                                }
                                            }
                                            if (bitmap != null) {
                                                Image(
                                                    bitmap = bitmap.asImageBitmap(),
                                                    contentDescription = "شعار الصيدلية",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Fit
                                                )
                                            } else {
                                                Text("شعار غير صالح ❌", color = Color.Gray, fontSize = 11.sp)
                                            }
                                        } else {
                                            Text("لا يوجد شعار محدد 🏪", color = Color.Gray, fontSize = 11.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { cameraLauncher.launch(null) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("كاميرا 📸", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { galleryLauncher.launch("image/*") },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("المعرض 🖼️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = {
                                            val updated = myPharmacy.copy(
                                                name = editName,
                                                doctorName = editDoctorName,
                                                phone = editPhone,
                                                location = editLocation,
                                                imageBase64 = editImageBase64,
                                                hasCosmetics = editHasCosmetics
                                            )
                                            viewModel.updatePharmacy(updated) { err ->
                                                if (err == null) {
                                                    Toast.makeText(context, "تم تعديل وإرسال طلب الصيدلية للمدير للموافقة بنجاح! 🚀✅", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "حدث خطأ: $err", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تعديل وإرسال الطلب للمدير للموافقة والنشر 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Approved Pharmacy Panel -> Manage products & incoming prescription orders
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PharmacistDashboard(
                            viewModel = viewModel,
                            pharmacy = myPharmacy,
                            allProducts = allProducts.filter { it.pharmacyId == myPharmacy.id },
                            allOrders = allOrders.filter { it.pharmacyId == myPharmacy.id }
                        )
                    }
                }
            }
        } else {
            // Customer View Panel -> Show approved pharmacies & prescription form
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                CustomerPharmacyView(
                    viewModel = viewModel,
                    approvedPharmacies = allPharmacies.filter { it.isApproved },
                    allProducts = allProducts.filter { it.isApproved }
                )
            }
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
                text = "🆕 إنشاء وتوثيق صيدليتك بالمجرة للتسوق",
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
            // Trigger audial alarm notification natively using RingtoneManager!
            try {
                val alertUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                val r = android.media.RingtoneManager.getRingtone(context, alertUri)
                r?.play()
                Toast.makeText(context, "🔔 تنبيه عاجل: تم إرسال روشتة جديدة لصيدليتك بالمجرة!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        previousPendingCount = pendingCount
    }

    var showEditPharmacyDialog by remember { mutableStateOf(false) }

    if (showEditPharmacyDialog) {
        EditPharmacyDialog(
            pharmacy = pharmacy,
            onDismiss = { showEditPharmacyDialog = false },
            onSave = { name, doctorName, phone, location, imageBase64, hasCosmetics ->
                val updated = pharmacy.copy(
                    name = name,
                    doctorName = doctorName,
                    phone = phone,
                    location = location,
                    imageBase64 = imageBase64,
                    hasCosmetics = hasCosmetics
                )
                viewModel.updatePharmacy(updated) { err ->
                    if (err == null) {
                        Toast.makeText(context, "تم تحديث بيانات الصيدلية بنجاح! ✅", Toast.LENGTH_SHORT).show()
                        showEditPharmacyDialog = false
                    } else {
                        Toast.makeText(context, "حدث خطأ: $err", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Green.copy(0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("نشط وموثق 🟢", color = Color.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = { showEditPharmacyDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                    }
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
    var showEnlargeForImage by remember { mutableStateOf<String?>(null) }
    
    if (showEnlargeForImage != null) {
        EnlargeImageDialog(showEnlargeForImage!!) { showEnlargeForImage = null }
    }
    
    // Filter orders specifically for this pharmacist's pharmacy
    val myPharmacyOrders = remember(orders, pharmacyId) {
        orders.filter { it.pharmacyId == pharmacyId }
    }

    var subTabState by remember { mutableStateOf(0) } // 0: Active, 1: Previous Prescriptions
    
    val activeOrders = remember(myPharmacyOrders) {
        myPharmacyOrders.filter { !it.status.contains("تم التسليم") && !it.status.contains("تم تسليم") && !it.status.contains("إغلاق") && !it.status.contains("تم التوصيل") && !it.status.contains("كاش") && !it.status.contains("بنكي") }
    }
    val previousOrders = remember(myPharmacyOrders) {
        myPharmacyOrders.filter { it.status.contains("تم التسليم") || it.status.contains("تم تسليم") || it.status.contains("إغلاق") || it.status.contains("تم التوصيل") || it.status.contains("كاش") || it.status.contains("بنكي") }
    }

    val displayOrders = if (subTabState == 0) activeOrders else previousOrders

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        // Sub-tabs row for Active vs Previous
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf(
                "روشتات سابقة 📜 (${previousOrders.size})" to 1,
                "روشتات نشطة ⏳ (${activeOrders.size})" to 0
            )
            tabs.forEach { (label, index) ->
                val isSelected = subTabState == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CosmicSecondary else CosmicSurface)
                        .clickable { subTabState = index }
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

        if (displayOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (subTabState == 0) "لا توجد روشتات أو طلبات نشطة حالياً صيدليتك 📭" else "لا توجد روشتات سابقة منفذة صيدليتك 📜",
                    color = MediumContrastTextDark,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                displayOrders.sortedByDescending { it.createdAt }.forEach { order ->
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
                            if (order.deliveryLocation.isNotBlank()) {
                                Text("📍 موقع التوصيل: ${order.deliveryLocation}", color = Color.White.copy(0.9f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            
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
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(0.2f))
                                            .clickable {
                                                showEnlargeForImage = order.prescriptionImageBase64
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "صورة الروشتة المرفوعة",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Inside
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(0.25f)),
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            Text(
                                                "اضغط لتكبير الروشتة 🔍",
                                                color = CosmicSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                        }
                                    }
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

    val rxActive = remember(myPharmacyOrders) {
        myPharmacyOrders.filter { it.status != "تم التوصيل" && !it.status.startsWith("تم التسليم") }
    }
    val rxCompleted = remember(myPharmacyOrders) {
        myPharmacyOrders.filter { it.status == "تم التوصيل" || it.status.startsWith("تم التسليم") }
    }

    var activeWellWishesOrder by remember { mutableStateOf<PharmacyOrderEntity?>(null) }
    var showLocalRatingDialog by remember { mutableStateOf(false) }
    var orderForRatingByCustomer by remember { mutableStateOf<PharmacyOrderEntity?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val sharedPref = remember(context) { context.getSharedPreferences("pharmacy_ratings_pref", android.content.Context.MODE_PRIVATE) }
    LaunchedEffect(myPharmacyOrders) {
        val newlyCompletedOrder = myPharmacyOrders.firstOrNull {
            it.status.startsWith("تم التسليم") && !sharedPref.getBoolean("well_wishes_shown_${it.id}", false)
        }
        if (newlyCompletedOrder != null) {
            sharedPref.edit().putBoolean("well_wishes_shown_${newlyCompletedOrder.id}", true).apply()
            orderForRatingByCustomer = newlyCompletedOrder
            showLocalRatingDialog = true
        }
    }

    if (activeWellWishesOrder != null) {
        PharmacyWellWishesOverlay()
    }
    if (showLocalRatingDialog) {
        PharmacyAppRatingDialog(
            viewModel = viewModel,
            onRateDismiss = { stars, comment ->
                showLocalRatingDialog = false
                if (stars > 0) {
                    viewModel.submitAppRating(stars, comment ?: "تقييم تطبيق الصيدليات بالمجرة")
                }
                val ord = orderForRatingByCustomer
                if (ord != null) {
                    activeWellWishesOrder = ord
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(5000)
                        activeWellWishesOrder = null
                        orderForRatingByCustomer = null
                    }
                }
            }
        )
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
                        if (rxActive.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(rxActive.size.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("طلباتي السابقة (الروشتات) 📑", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        text = "🏥 الصيدليات الطبية المعتمدة في المجرة للتسوق:",
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
                                    // Left Side: Doctor Name and Contact at the top
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = "الطبيب المسؤول المسؤول 🧑‍⚕️",
                                            color = CosmicSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "د. ${pharmacy.doctorName}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "📞 ${pharmacy.phone}",
                                            color = CosmicSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Right Side: Logo & Name
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(pharmacy.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        val pharmLogo = remember(pharmacy.imageBase64) {
                                            if (pharmacy.imageBase64.isBlank()) null
                                            else {
                                                try {
                                                    val clean = if (pharmacy.imageBase64.contains(",")) pharmacy.imageBase64.substringAfter(",") else pharmacy.imageBase64
                                                    val decodedBytes = android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
                                                    android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                                } catch (e: Exception) { null }
                                            }
                                        }
                                        if (pharmLogo != null) {
                                            Image(
                                                bitmap = pharmLogo.asImageBitmap(),
                                                contentDescription = "شعار الصيدلية",
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .border(1.5.dp, CosmicSecondary, CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(CosmicSurfaceVariant)
                                                    .border(1.5.dp, CosmicSecondary.copy(0.4f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.LocalPharmacy, null, tint = CosmicSecondary, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
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
            var rxSubFilter by remember { mutableStateOf(0) } // 0: Active, 1: Previous (Completed)
            val displayedRx = if (rxSubFilter == 0) rxActive else rxCompleted

            Column(modifier = Modifier.fillMaxWidth()) {
                // Sub-tabs row for Patient Prescriptions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { rxSubFilter = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (rxSubFilter == 1) CosmicSecondary else CosmicSurface,
                            contentColor = if (rxSubFilter == 1) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("روشتات سابقة 📜 (${rxCompleted.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { rxSubFilter = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (rxSubFilter == 0) CosmicSecondary else CosmicSurface,
                            contentColor = if (rxSubFilter == 0) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("روشتات قيد التنفيذ ⏳ (${rxActive.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (displayedRx.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (rxSubFilter == 0) "لا توجد روشتات قيد التنفيذ حالياً ⏳" else "لا توجد روشتات سابقة منفذة 📜",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(displayedRx) { order ->
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

                                    Spacer(modifier = Modifier.height(6.dp))
                                    PharmacyOrderTracker(order.status)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (order.prescriptionImageBase64.isNotBlank()) {
                                        var showEnlarge by remember { mutableStateOf(false) }
                                        if (showEnlarge) {
                                            EnlargeImageDialog(order.prescriptionImageBase64) { showEnlarge = false }
                                        }
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
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(110.dp)
                                                    .padding(vertical = 4.dp)
                                                    .clickable { showEnlarge = true },
                                                colors = CardDefaults.cardColors(containerColor = CosmicDeepSpace),
                                                border = BorderStroke(1.dp, CosmicSurfaceVariant),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Image(
                                                        bitmap = bitmap.asImageBitmap(),
                                                        contentDescription = "الروشتة المرفقة",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Inside
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Black.copy(0.3f)),
                                                        contentAlignment = Alignment.BottomCenter
                                                    ) {
                                                        Text(
                                                            "اضغط لتكبير الروشتة 🔍",
                                                            color = CosmicSecondary,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(bottom = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

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
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = {
                                                val phone = order.courierPhone ?: ""
                                                val cleanPhone = phone.replace("+", "").replace(" ", "")
                                                val shareMsg = """
                                                    🌌 فاتورة صيدلية كوني 🌌
                                                    ------------------------------
                                                    👤 الزبون: ${order.customerName}
                                                    📞 هاتف: ${order.customerPhone}
                                                    📍 العنوان: ${order.deliveryLocation}
                                                    ------------------------------
                                                    📦 الأدوية المسعرة:
                                                    ${order.medicinesJson}
                                                    ------------------------------
                                                    💰 المجموع الكلي للدواء: ${viewModel.formatPrice(order.medicinePrice)} SDG
                                                    حالة الفاتورة: ${order.status}
                                                    بالشفاء العاجل والشفاء التام إن شاء الله 🤲🩺
                                                """.trimIndent()
                                                
                                                try {
                                                    val intent = android.content.Intent(
                                                        android.content.Intent.ACTION_VIEW,
                                                        android.net.Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${android.net.Uri.encode(shareMsg)}")
                                                     )
                                                     context.startActivity(intent)
                                                } catch (e: Exception) {
                                                     val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                     val clip = android.content.ClipData.newPlainText("Majarah Pharmacy Invoice", shareMsg)
                                                     clipboard.setPrimaryClip(clip)
                                                     Toast.makeText(context, "تم نسخ تفاصيل الفاتورة! شاركها مع المندوب يدوياً 📋", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary.copy(0.12f), contentColor = CosmicSecondary),
                                            border = BorderStroke(1.dp, CosmicSecondary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Share, null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("مشاركة تفاصيل الفاتورة والروشتة مع المندوب 🚴💬", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                     // Display "بالشفاء العاجل لك إن شاء الله 🤲✨" when order.status == "تم التوصيل"
                                    if (order.status == "تم تسليم المندوب" || order.status == "تم التوصيل" || order.status.startsWith("تم التسليم")) {
                                        if (order.status == "تم التوصيل" || order.status.startsWith("تم التسليم")) {
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

                                        Spacer(modifier = Modifier.height(10.dp))

                                        val savedPaymentMethod = order.paymentMethod ?: ""
                                        val savedReceiptBase64 = order.bankReceiptImageUri
                                        val localContext = androidx.compose.ui.platform.LocalContext.current

                                        var receiptToShow by remember { mutableStateOf<String?>(null) }
                                        if (receiptToShow != null) {
                                            ViewReceiptDialog(receiptToShow!!) { receiptToShow = null }
                                        }

                                        val isPaymentSubmitted = (savedPaymentMethod.isNotBlank() && savedPaymentMethod != "كاش") || sharedPref.getBoolean("payment_submitted_${order.id}", false)

                                        if (!isPaymentSubmitted) {
                                            OrderPostDeliveryPaymentBlock(
                                                currentPaymentMethod = "",
                                                currentReceiptBase64 = null,
                                                onSavePayment = { method, base64 ->
                                                    viewModel.updatePharmacyOrderPayment(order.id, method, base64) { err ->
                                                        if (err == null) {
                                                            sharedPref.edit().putBoolean("payment_submitted_${order.id}", true).apply()
                                                            android.widget.Toast.makeText(localContext, "تم تأكيد الدفع وإكمال الفاتورة بنجاح! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            android.widget.Toast.makeText(localContext, "فشل حفظ الدفع: $err", android.widget.Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                            )
                                        } else {
                                            Card(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                                border = BorderStroke(1.dp, Color.Green.copy(0.3f)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    Text(
                                                        text = "✅ تم تأكيد طريقة الدفع للصيدلية",
                                                        color = Color.Green,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        modifier = Modifier.padding(bottom = 4.dp)
                                                    )
                                                    Text(
                                                        text = "طريقة السداد: $savedPaymentMethod",
                                                        color = Color.White,
                                                        fontSize = 11.sp
                                                    )
                                                    if (!savedReceiptBase64.isNullOrBlank()) {
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Button(
                                                            onClick = { receiptToShow = savedReceiptBase64 },
                                                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Icon(Icons.Default.Image, null, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("عرض إشعار التحويل المرفق 📄", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Button(
                                                        onClick = {
                                                            val phone = order.courierPhone ?: ""
                                                            val cleanPhone = phone.replace("+", "").replace(" ", "")
                                                            val shareMsg = """
                                                                🌌 فاتورة صيدلية كوني 🌌
                                                                ------------------------------
                                                                👤 الزبون: ${order.customerName}
                                                                📞 هاتف: ${order.customerPhone}
                                                                📍 العنوان: ${order.deliveryLocation}
                                                                ------------------------------
                                                                📦 الأدوية:
                                                                ${order.medicinesJson}
                                                                ------------------------------
                                                                💰 المجموع الكلي: ${viewModel.formatPrice(order.medicinePrice)} SDG
                                                                💳 طريقة السداد: $savedPaymentMethod
                                                                حالة الفاتورة: تم تأكيد الدفع ومطابقتها من العميل بنجاح مغلق 🔒✅
                                                            """.trimIndent()
                                                            
                                                            try {
                                                                val intent = android.content.Intent(
                                                                    android.content.Intent.ACTION_VIEW,
                                                                    android.net.Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${android.net.Uri.encode(shareMsg)}")
                                                                )
                                                                localContext.startActivity(intent)
                                                            } catch (e: Exception) {
                                                                val clipboard = localContext.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                                val clip = android.content.ClipData.newPlainText("Majarah Pharmacy Invoice", shareMsg)
                                                                clipboard.setPrimaryClip(clip)
                                                                android.widget.Toast.makeText(localContext, "تم نسخ تفاصيل الفاتورة! شاركها مع المندوب يدوياً 📋", android.widget.Toast.LENGTH_LONG).show()
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Icon(Icons.Default.Share, null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("مشاركة الفاتورة المؤكدة مع المندوب 🚴💬", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
        var deliveryLoc by remember { mutableStateOf("") }
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

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = deliveryLoc,
                        onValueChange = { deliveryLoc = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("موقع التوصيل المحدد بالتفصيل 📍", color = CosmicSecondary, fontSize = 11.sp) },
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
                        if (custName.isBlank() || custPhone.isBlank() || prescriptionImageBase64.isBlank() || deliveryLoc.isBlank()) {
                            Toast.makeText(context, "الرجاء إدخال اسمك ورقم هاتفك وموقع التوصيل وتصوير الروشتة الطبية ⚠️", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addPharmacyOrder(
                                pharmacyId = pharm.id,
                                customerName = custName.trim(),
                                customerPhone = custPhone.trim(),
                                customerEmail = activeProfile?.email ?: "",
                                prescriptionBase64 = prescriptionImageBase64,
                                deliveryLocation = deliveryLoc.trim()
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
    
    var showEnlargeForImage by remember { mutableStateOf<String?>(null) }
    if (showEnlargeForImage != null) {
        EnlargeImageDialog(showEnlargeForImage!!) { showEnlargeForImage = null }
    }

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
                        itemsIndexed(pharmacies) { index, pharmacy ->
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("صيدلية #${index + 1}", color = CosmicSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(pharmacy.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
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
                        itemsIndexed(orders.sortedByDescending { it.createdAt }) { index, order ->
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("طلب #${orders.size - index}", color = CosmicSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("روشتة من العميل: ${order.customerName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("هاتف العميل: ${order.customerPhone} 📞", color = MediumContrastTextDark, fontSize = 10.sp)
                                    if (order.deliveryLocation.isNotBlank()) {
                                        Text("موقع التوصيل المحدد: ${order.deliveryLocation} 📍", color = Color.White.copy(0.9f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text("الصيدلية المصدر: ${pharm?.name ?: "مجهولة"}", color = CosmicSecondary, fontSize = 10.sp)
                                    
                                    if (pharm != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Call button
                                            Button(
                                                onClick = {
                                                    try {
                                                        val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                            data = android.net.Uri.parse("tel:${pharm.phone.trim()}")
                                                        }
                                                        context.startActivity(dialIntent)
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(0.2f), contentColor = Color.Green),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.padding(end = 6.dp)
                                            ) {
                                                Icon(Icons.Default.Call, null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("اتصال بالصيدلي 📞", fontSize = 10.sp)
                                            }

                                            // WhatsApp button
                                            Button(
                                                onClick = {
                                                    try {
                                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                            data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=${pharm.phone.trim()}")
                                                        }
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366).copy(0.2f), contentColor = Color(0xFF25D366)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Message, null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("واتساب الصيدلي 💬", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                    
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
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(130.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.Black.copy(0.2f))
                                                    .clickable {
                                                        showEnlargeForImage = order.prescriptionImageBase64
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    bitmap = bitmap.asImageBitmap(),
                                                    contentDescription = "الروشتة الطبية مكبرة",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Inside
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(0.25f)),
                                                    contentAlignment = Alignment.BottomCenter
                                                ) {
                                                    Text(
                                                        "اضغط لتكبير الروشتة 🔍",
                                                        color = CosmicSecondary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(bottom = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (order.status == "بانتظار المدير" || order.status == "بانتظار الصيدلي") {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { selectedOrderForApprove = order },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (order.status == "بانتظار الصيدلي") CosmicSecondary else Color.Green,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (order.status == "بانتظار الصيدلي") "تسعير وتأكيد الفاتورة وتعيين مندوب مباشرة 🚴✅" else "تعيين المندوب وتأكيد السعر النهائي والرسوم 🚴✅",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
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
                        var directMedicinesInput by remember { mutableStateOf("") }
                        var directPriceInput by remember { mutableStateOf("") }
                        var courierSelection by remember { mutableStateOf("") } // Name
                        var courierPhoneSelection by remember { mutableStateOf("") } // Phone
                        var expandedCouriersDropdown by remember { mutableStateOf(false) }

                        AlertDialog(
                            onDismissRequest = { selectedOrderForApprove = null },
                            containerColor = CosmicSurface,
                            title = { Text(if (ord.status == "بانتظار الصيدلي") "تسعير الروشتة وتعيين المندوب مباشرة 🚴🏆" else "تعيين المندوب ورسوم التوصيل 🚴🏆", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                            text = {
                                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                                    if (ord.status == "بانتظار الصيدلي") {
                                        Text("الروشتة بانتظار الصيدلي ولكن يمكنك تسعيرها مباشرة لإراحة العميل وعدم التأخير 🌟", color = CosmicSecondary, fontSize = 10.sp, textAlign = TextAlign.Right, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                                        OutlinedTextField(
                                            value = directMedicinesInput,
                                            onValueChange = { directMedicinesInput = it },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                            label = { Text("أسماء الأدوية والعلاجات المسعرة 🧪", color = CosmicSecondary, fontSize = 11.sp) },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                        )
                                        OutlinedTextField(
                                            value = directPriceInput,
                                            onValueChange = { directPriceInput = it },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                            label = { Text("سعر الأدوية الإجمالي بالجنيه (SDG) 💰", color = CosmicSecondary, fontSize = 11.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                        )
                                    } else {
                                        Text("أدوية العميل: ${ord.medicinesJson}", color = MediumContrastTextDark, fontSize = 11.sp, textAlign = TextAlign.Right)
                                        Text("قيمة الدواء: ${viewModel.formatPrice(ord.medicinePrice)}", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    if (ord.deliveryLocation.isNotBlank()) {
                                        Text("📍 موقع التوصيل المطلوب: ${ord.deliveryLocation}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.Green.copy(0.12f)),
                                        border = BorderStroke(1.dp, Color.Green.copy(0.3f)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.LocalShipping, null, tint = Color.Green, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "رسوم التوصيل: التوصيل مجاني 🚚🆓",
                                                color = Color.Green,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

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
                                         val fee = 0.0
                                         val isDirect = ord.status == "بانتظار الصيدلي"
                                         val directPrice = directPriceInput.toDoubleOrNull() ?: 0.0
                                         if (isDirect && (directMedicinesInput.isBlank() || directPrice <= 0.0)) {
                                             Toast.makeText(context, "الرجاء كتابة أسماء الأدوية وسعر صحيح لها أولاً ⚠️", Toast.LENGTH_SHORT).show()
                                         } else if (courierSelection.isBlank()) {
                                             Toast.makeText(context, "الرجاء اختيار المندوب لتسليم الطلب 🚴", Toast.LENGTH_SHORT).show()
                                         } else {
                                            viewModel.adminApprovePharmacyOrder(
                                                orderId = ord.id, 
                                                courierName = courierSelection, 
                                                courierPhone = courierPhoneSelection, 
                                                deliveryFee = fee,
                                                medicinesJson = if (isDirect) directMedicinesInput else "",
                                                medicinePrice = if (isDirect) directPrice else 0.0
                                            ) { err ->
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

@Composable
fun EditPharmacyDialog(
    pharmacy: com.example.data.db.PharmacyEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, doctorName: String, phone: String, location: String, imageBase64: String, hasCosmetics: Boolean) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var name by remember { mutableStateOf(pharmacy.name) }
    var doctorName by remember { mutableStateOf(pharmacy.doctorName) }
    var phone by remember { mutableStateOf(pharmacy.phone) }
    var location by remember { mutableStateOf(pharmacy.location) }
    var hasCosmetics by remember { mutableStateOf(pharmacy.hasCosmetics) }
    var imageBase64 by remember { mutableStateOf(pharmacy.imageBase64) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val outputStream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                val bytes = outputStream.toByteArray()
                imageBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                Toast.makeText(context, "تم التقاط صورة اللوقو! 📸", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "حدث خطأ أثناء معالجة الصورة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
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
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                val bytes = outputStream.toByteArray()
                imageBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                Toast.makeText(context, "تم اختيار صورة اللوقو! 🖼️", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "حدث خطأ أثناء معالجة الصورة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "تعديل بيانات الصيدلية واللوقو 💊",
                color = CosmicSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("اسم الصيدلية:", color = Color.White.copy(0.8f), fontSize = 11.sp)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant
                        )
                    )
                }

                item {
                    Text("اسم الدكتور الصيدلي المسؤول:", color = Color.White.copy(0.8f), fontSize = 11.sp)
                    OutlinedTextField(
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant
                        )
                    )
                }

                item {
                    Text("رقم التواصل (واتساب):", color = Color.White.copy(0.8f), fontSize = 11.sp)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant
                        )
                    )
                }

                item {
                    Text("العنوان والموقع بالتفصيل:", color = Color.White.copy(0.8f), fontSize = 11.sp)
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicSecondary,
                            unfocusedBorderColor = CosmicSurfaceVariant
                        )
                    )
                }

                item {
                    Text("تعديل لوقو / صورة الصيدلية:", color = CosmicSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (imageBase64.isNotBlank()) {
                        val bitmap = remember(imageBase64) {
                            try {
                                val clean = if (imageBase64.contains(",")) imageBase64.substringAfter(",") else imageBase64
                                val bytes = android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
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
                            Text("المعرض 🖼️", fontSize = 10.sp)
                        }

                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("الكاميرا 📸", fontSize = 10.sp)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { hasCosmetics = !hasCosmetics }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("توفر مستحضرات التجميل والعناية بالبشرة 🧴", color = Color.White, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Checkbox(
                            checked = hasCosmetics,
                            onCheckedChange = { hasCosmetics = it },
                            colors = CheckboxDefaults.colors(checkedColor = CosmicSecondary)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || doctorName.isBlank() || phone.isBlank() || location.isBlank()) {
                        Toast.makeText(context, "الرجاء تعبئة كافة الحقول المطلوبة ⚠️", Toast.LENGTH_SHORT).show()
                    } else {
                        onSave(name, doctorName, phone, location, imageBase64, hasCosmetics)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
            ) {
                Text("حفظ التعديلات 💾", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.White)
            }
        }
    )
}

@Composable
fun PharmacyOrderTracker(status: String) {
    val steps = listOf("تجهيز الدواء 💊", "تم تسليم المندوب 🚴", "تم التسليم ✅")
    val currentStepIndex = when {
        status.startsWith("تم التسليم") -> 2
        status == "تم تسليم المندوب" -> 1
        else -> 0
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, title ->
            val isCompleted = index <= currentStepIndex
            val color = if (isCompleted) Color.Green else Color.Gray
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) Color.Green.copy(0.12f) else Color.DarkGray)
                        .border(1.5.dp, color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < currentStepIndex) {
                        Icon(Icons.Default.Check, null, tint = Color.Green, modifier = Modifier.size(14.dp))
                    } else {
                        Text((index + 1).toString(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    color = if (isCompleted) Color.White else Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(0.5f)
                        .background(if (index < currentStepIndex) Color.Green else Color.DarkGray)
                )
            }
        }
    }
}

@Composable
fun PharmacyWellWishesOverlay() {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = {},
        properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CosmicDeepSpace)
                .border(2.dp, Color.Green, RoundedCornerShape(20.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = Color.Green,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "بالشفاء العاجل لك إن شاء الله 🤲✨",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "تم تسليم أدويتك الطبية بنجاح.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PharmacyAppRatingDialog(
    viewModel: MajarahViewModel,
    onRateDismiss: (stars: Int, comment: String?) -> Unit
) {
    var rating by remember { mutableStateOf(7) }
    var comment by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { onRateDismiss(0, null) },
        title = {
            Text(
                text = "تقييم تطبيق مجرة الصيدليات 🌌⭐",
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "نتمنى أن تكون تجربتك رائعة! يرجى تقييم جودة الخدمة والمندوب وصيدلية المجرة (تقييم من 7 نجوم):",
                    fontSize = 11.sp,
                    color = Color.White.copy(0.8f),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..7).forEach { star ->
                        IconButton(onClick = { rating = star }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (star <= rating) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("اكتب رأيك هنا أو أي ملاحظات للمدير... ✍️", color = Color.Gray, fontSize = 11.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Right),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicSecondary,
                        unfocusedBorderColor = CosmicSurfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onRateDismiss(rating, comment.trim().ifBlank { null })
                },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black)
            ) {
                Text("إرسال التقييم ⭐", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = { onRateDismiss(0, null) }) {
                Text("تخطي", color = Color.White.copy(0.6f))
            }
        },
        containerColor = CosmicSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EnlargeImageDialog(imageBase64: String, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            border = BorderStroke(1.5.dp, CosmicSecondary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "عرض الروشتة الطبية بوضوح 📸🔬",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                val bitmap = remember(imageBase64) {
                    try {
                        val clean = if (imageBase64.contains(",")) imageBase64.substringAfter(",") else imageBase64
                        val bytes = android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
                        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "الروشتة الطبية مكبرة",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("تعذر تحميل الصورة ❌", color = Color.Red, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق العرض ❌", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

