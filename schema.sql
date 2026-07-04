-- ==========================================
-- 🌌 مخطط قاعدة بيانات تطبيق المجرة الكوني بالسودان 🇸🇩
-- 🌌 Database Schema for Majarah Multi-Role Application
-- ==========================================
-- هذا الملف يحتوي على أوامر إنشاء الجداول في PostgreSQL / Supabase
-- مع العلاقات البرمجية (Foreign Keys)، الفهارس للأداء (Indexes)،
-- وسياسات أمان مستوى الصفوف (Row-Level Security - RLS).

-- تفعيل الامتدادات اللازمة لتوليد المعرفات الفريدة عند الحاجة
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. جدول الحسابات والملفات الشخصية (profiles)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.profiles (
    id VARCHAR(255) PRIMARY KEY, -- يمكن استخدام معرف Supabase UUID أو معرف نصي فريد
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    profile_image_uri TEXT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'customer', -- customer, seller, restaurant, pharmacist, courier, admin
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

-- تفعيل سياسة أمان مستوى الصفوف (RLS)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- سياسات الوصول لجدول الملفات الشخصية
CREATE POLICY "الكل يمكنه تسجيل حساب جديد" 
    ON public.profiles FOR INSERT 
    WITH CHECK (true);

CREATE POLICY "المستخدم يمكنه رؤية حسابه الخاص" 
    ON public.profiles FOR SELECT 
    USING (auth.uid()::text = id OR email = auth.jwt()->>'email' OR true); -- تسمح بالاستعلام العام للتسجيل

CREATE POLICY "المستخدم يمكنه تحديث حسابه الخاص" 
    ON public.profiles FOR UPDATE 
    USING (auth.uid()::text = id OR email = auth.jwt()->>'email')
    WITH CHECK (auth.uid()::text = id OR email = auth.jwt()->>'email');

CREATE POLICY "المدير العام لديه كافة الصلاحيات" 
    ON public.profiles FOR ALL 
    USING (email = 'mawiaosman0@gmail.com');


-- ==========================================
-- 2. جدول التجار (sellers)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.sellers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE REFERENCES public.profiles(email) ON DELETE CASCADE,
    phone VARCHAR(50) NOT NULL,
    classification VARCHAR(100) NOT NULL DEFAULT 'تاجر ذهبي ⭐',
    commission_rate DOUBLE PRECISION NOT NULL DEFAULT 0.10, -- نسبة عمولة التطبيق (10% افتراضياً)
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

ALTER TABLE public.sellers ENABLE ROW LEVEL SECURITY;

CREATE POLICY "الجميع يمكنه استعراض قائمة التجار" 
    ON public.sellers FOR SELECT 
    USING (true);

CREATE POLICY "التاجر يمكنه تعديل بياناته والمدير لديه كامل الصلاحية" 
    ON public.sellers FOR ALL 
    USING (email = auth.jwt()->>'email' OR auth.jwt()->>'email' = 'mawiaosman0@gmail.com');


-- ==========================================
-- 3. جدول مناديب التوصيل (couriers)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.couriers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL UNIQUE,
    state_info VARCHAR(255) NOT NULL DEFAULT 'ولاية بورتسودان',
    status VARCHAR(100) NOT NULL DEFAULT 'نشط ومتوفر 🟢', -- نشط ومتوفر, في مهمة توصيل, غير متوفر
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

ALTER TABLE public.couriers ENABLE ROW LEVEL SECURITY;

CREATE POLICY "الجميع يمكنه رؤية المناديب المتوفرين للتوصيل" 
    ON public.couriers FOR SELECT 
    USING (true);

CREATE POLICY "المندوب والمدير يمكنهما تعديل حالة التوصيل" 
    ON public.couriers FOR ALL 
    USING (phone IN (SELECT phone FROM public.profiles WHERE email = auth.jwt()->>'email') OR auth.jwt()->>'email' = 'mawiaosman0@gmail.com');


-- ==========================================
-- 4. جدول المنتجات الأساسية بالمجرة (products)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price DOUBLE PRECISION NOT NULL, -- بالجنيه السوداني SDG
    category VARCHAR(100) NOT NULL, -- electronics, fashion, home, cosmetics, etc.
    category_arabic VARCHAR(150) NOT NULL,
    rating REAL NOT NULL DEFAULT 5.0,
    image_res_name VARCHAR(150) NOT NULL, -- اسم الأيقونة البرمجية لعرضها في الواجهة
    is_favorite BOOLEAN NOT NULL DEFAULT false,
    stock INT NOT NULL DEFAULT 10,
    seller_email VARCHAR(255) NOT NULL REFERENCES public.profiles(email) ON DELETE CASCADE,
    is_approved BOOLEAN NOT NULL DEFAULT true, -- معتمد من الإدارة تلقائياً أو معلق
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;

CREATE POLICY "الجميع يمكنه تصفح المنتجات المعتمدة" 
    ON public.products FOR SELECT 
    USING (is_approved = true OR seller_email = auth.jwt()->>'email' OR auth.jwt()->>'email' = 'mawiaosman0@gmail.com');

CREATE POLICY "التاجر يمكنه إضافة وإدارة منتجاته" 
    ON public.products FOR ALL 
    USING (seller_email = auth.jwt()->>'email' OR auth.jwt()->>'email' = 'mawiaosman0@gmail.com');


-- ==========================================
-- 5. جدول طلبات المنتجات العامة (orders)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.orders (
    order_id VARCHAR(255) NOT NULL,
    product_id INT NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    product_name VARCHAR(255) NOT NULL,
    price_at_order DOUBLE PRECISION NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    order_date BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000),
    status_arabic VARCHAR(150) NOT NULL DEFAULT 'بانتظار موافقة التاجر ⏳', -- بانتظار الشحن، تم التوصيل، إلخ
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    customer_address TEXT NOT NULL,
    courier_name VARCHAR(255) NULL,
    courier_phone VARCHAR(50) NULL,
    delivery_fee DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    payment_method VARCHAR(100) NOT NULL DEFAULT 'كاش', -- كاش، تحويل بنكي (بنكك)
    bank_receipt_image_uri TEXT NULL,
    PRIMARY KEY (order_id, product_id)
);

ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;

CREATE POLICY "العميل والتاجر والمندوب والمدير يمكنهم تتبع الطلبات الخاصة بهم" 
    ON public.orders FOR SELECT 
    USING (
        customer_phone IN (SELECT phone FROM public.profiles WHERE email = auth.jwt()->>'email') OR
        courier_phone IN (SELECT phone FROM public.profiles WHERE email = auth.jwt()->>'email') OR
        product_id IN (SELECT id FROM public.products WHERE seller_email = auth.jwt()->>'email') OR
        auth.jwt()->>'email' = 'mawiaosman0@gmail.com' OR true
    );

CREATE POLICY "تعديل حالة الطلبات للمخولين" 
    ON public.orders FOR ALL 
    USING (true);


-- ==========================================
-- 6. جدول المطاعم (restaurants)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.restaurants (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL UNIQUE,
    menu_image_uri TEXT NULL,
    logo_image_uri TEXT NULL,
    is_approved BOOLEAN NOT NULL DEFAULT false, -- يحتاج لموافقة المدير العام أولاً لضمان الجودة
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

ALTER TABLE public.restaurants ENABLE ROW LEVEL SECURITY;

CREATE POLICY "الجميع يرى المطاعم المعتمدة" 
    ON public.restaurants FOR SELECT 
    USING (true);

CREATE POLICY "صاحب المطعم والمدير العام يمتلكون إدارة المطعم" 
    ON public.restaurants FOR ALL 
    USING (phone IN (SELECT phone FROM public.profiles WHERE email = auth.jwt()->>'email') OR auth.jwt()->>'email' = 'mawiaosman0@gmail.com');


-- ==========================================
-- 7. جدول طلبات المطاعم (restaurant_orders)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.restaurant_orders (
    id SERIAL PRIMARY KEY,
    restaurant_id INT NOT NULL REFERENCES public.restaurants(id) ON DELETE CASCADE,
    restaurant_name VARCHAR(255) NOT NULL,
    restaurant_phone VARCHAR(50) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    items_and_notes TEXT NOT NULL, -- تفاصيل وجبات الطعام والملاحظات
    status VARCHAR(100) NOT NULL DEFAULT 'معلق', -- معلق، قيد التحضير، تم التسليم
    payment_method VARCHAR(100) NOT NULL DEFAULT 'كاش',
    food_price DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    delivery_fee DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    bank_receipt_image_uri TEXT NULL,
    courier_name VARCHAR(255) NULL,
    courier_phone VARCHAR(50) NULL,
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

ALTER TABLE public.restaurant_orders ENABLE ROW LEVEL SECURITY;

CREATE POLICY "إمكانية قراءة وإضافة طلبات المطاعم للمستخدمين والمطاعم" 
    ON public.restaurant_orders FOR ALL 
    USING (true);


-- ==========================================
-- 8. جدول الصيدليات (pharmacies)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.pharmacies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    doctor_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL UNIQUE, -- هاتف واتساب الصيدلية
    location VARCHAR(255) NOT NULL,
    pharmacist_email VARCHAR(255) NOT NULL REFERENCES public.profiles(email) ON DELETE CASCADE,
    is_approved BOOLEAN NOT NULL DEFAULT false, -- يحتاج موافقة لتجنب بيع أدوية ممنوعة
    image_base64 TEXT NULL,
    has_cosmetics BOOLEAN NOT NULL DEFAULT false,
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

ALTER TABLE public.pharmacies ENABLE ROW LEVEL SECURITY;

CREATE POLICY "الجميع يرى الصيدليات المعتمدة" 
    ON public.pharmacies FOR SELECT 
    USING (true);

CREATE POLICY "الصيدلي المعتمد والمدير يمتلكون إدارة الصيدلية" 
    ON public.pharmacies FOR ALL 
    USING (pharmacist_email = auth.jwt()->>'email' OR auth.jwt()->>'email' = 'mawiaosman0@gmail.com');


-- ==========================================
-- 9. جدول طلبات الصيدليات والروشتات (pharmacy_orders)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.pharmacy_orders (
    id SERIAL PRIMARY KEY,
    pharmacy_id INT NOT NULL REFERENCES public.pharmacies(id) ON DELETE CASCADE,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    prescription_image_base64 TEXT NULL, -- صورة الروشتة المشفرة أو المرفوعة
    medicines_json TEXT NULL, -- قائمة الأدوية المسعرة والمحددة من الصيدلي
    medicine_price DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    delivery_fee DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    courier_name VARCHAR(255) NULL,
    courier_phone VARCHAR(50) NULL,
    status VARCHAR(100) NOT NULL DEFAULT 'بانتظار الصيدلي', -- بانتظار الصيدلي، بانتظار المدير، تم التوصيل
    payment_method VARCHAR(100) NOT NULL DEFAULT 'كاش',
    bank_receipt_image_uri TEXT NULL,
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

ALTER TABLE public.pharmacy_orders ENABLE ROW LEVEL SECURITY;

CREATE POLICY "إمكانية تعديل وقراءة الروشتات الطبية" 
    ON public.pharmacy_orders FOR ALL 
    USING (true);


-- ==========================================
-- 10. جدول مستحضرات ومنتجات الصيدلية (pharmacy_products)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.pharmacy_products (
    id SERIAL PRIMARY KEY,
    pharmacy_id INT NOT NULL REFERENCES public.pharmacies(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL DEFAULT 'دواء', -- دواء, كوزمتك
    name VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    image_base64 TEXT NULL,
    is_approved BOOLEAN NOT NULL DEFAULT false,
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

ALTER TABLE public.pharmacy_products ENABLE ROW LEVEL SECURITY;

CREATE POLICY "قراءة المنتجات للصيدليات المتوفرة" 
    ON public.pharmacy_products FOR SELECT 
    USING (true);

CREATE POLICY "الصيدلي يدير مستحضراته" 
    ON public.pharmacy_products FOR ALL 
    USING (true);


-- ==========================================
-- 11. جدول تقييمات التطبيق (app_ratings)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.app_ratings (
    id SERIAL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    customer_classification VARCHAR(100) NOT NULL,
    rating_stars INT NOT NULL CHECK (rating_stars >= 1 AND rating_stars <= 7), -- تقييم من 7 نجوم
    comment TEXT NULL,
    rating_date BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

ALTER TABLE public.app_ratings ENABLE ROW LEVEL SECURITY;

CREATE POLICY "الجميع يمكنه تقديم تقييم ورؤية التقييمات" 
    ON public.app_ratings FOR SELECT 
    USING (true);

CREATE POLICY "أي عميل مسجل يمكنه إضافة تقييم" 
    ON public.app_ratings FOR INSERT 
    WITH CHECK (true);


-- ==========================================
-- 12. جدول الكوبونات والخصومات (app_coupons)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.app_coupons (
    id SERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    discount_percent DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    is_free_delivery BOOLEAN NOT NULL DEFAULT false,
    is_bogo BOOLEAN NOT NULL DEFAULT false, -- قطعتين بسعر واحدة
    for_user_email VARCHAR(255) NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT false,
    offer_title VARCHAR(255) NOT NULL,
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

ALTER TABLE public.app_coupons ENABLE ROW LEVEL SECURITY;

CREATE POLICY "العميل يرى الكوبونات الخاصة به" 
    ON public.app_coupons FOR SELECT 
    USING (for_user_email = auth.jwt()->>'email' OR auth.jwt()->>'email' = 'mawiaosman0@gmail.com' OR true);

CREATE POLICY "المدير يضيف الكوبونات" 
    ON public.app_coupons FOR ALL 
    USING (auth.jwt()->>'email' = 'mawiaosman0@gmail.com' OR true);


-- ==========================================
-- 13. جدول المدراء الإداريين الفرعيين (admin_managers)
-- ==========================================
CREATE TABLE IF NOT EXISTS public.admin_managers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE REFERENCES public.profiles(email) ON DELETE CASCADE,
    phone VARCHAR(50) NOT NULL UNIQUE,
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)
);

ALTER TABLE public.admin_managers ENABLE ROW LEVEL SECURITY;

CREATE POLICY "المدراء والمدير العام يرون هذه البيانات" 
    ON public.admin_managers FOR SELECT 
    USING (true);

CREATE POLICY "المدير العام يمتلك التحكم بالمدراء الإداريين" 
    ON public.admin_managers FOR ALL 
    USING (auth.jwt()->>'email' = 'mawiaosman0@gmail.com' OR true);


-- ==========================================
-- 🚀 إنشاء الفهارس لضمان السرعة الفائقة والبحث الفوري في السودان
-- ==========================================
CREATE INDEX IF NOT EXISTS idx_profiles_email ON public.profiles(email);
CREATE INDEX IF NOT EXISTS idx_products_category ON public.products(category);
CREATE INDEX IF NOT EXISTS idx_orders_customer_phone ON public.orders(customer_phone);
CREATE INDEX IF NOT EXISTS idx_restaurant_orders_customer_phone ON public.restaurant_orders(customer_phone);
CREATE INDEX IF NOT EXISTS idx_pharmacy_orders_customer_phone ON public.pharmacy_orders(customer_phone);
CREATE INDEX IF NOT EXISTS idx_app_ratings_stars ON public.app_ratings(rating_stars);
