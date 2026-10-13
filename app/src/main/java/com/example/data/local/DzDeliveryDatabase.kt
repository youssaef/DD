package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CouponEntity
import com.example.data.model.DriverEntity
import com.example.data.model.MerchantEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.ProductEntity
import com.example.data.model.WilayaEntity
import com.example.domain.DeliveryCategory
import com.example.domain.OrderStatus
import com.example.domain.PaymentMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MerchantEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        DriverEntity::class,
        WilayaEntity::class,
        CouponEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DzDeliveryDatabase : RoomDatabase() {
    abstract fun merchantDao(): MerchantDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun driverDao(): DriverDao
    abstract fun wilayaDao(): WilayaDao
    abstract fun couponDao(): CouponDao

    companion object {
        @Volatile
        private var INSTANCE: DzDeliveryDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DzDeliveryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DzDeliveryDatabase::class.java,
                    "dz_delivery_pro.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    seedDatabase(database)
                }
            }
        }
    }
}

suspend fun seedDatabase(db: DzDeliveryDatabase) {
    // 1. Seed Wilayas
    val wilayas = listOf(
        WilayaEntity("16", "الجزائر العاصمة", "Alger", 400.0),
        WilayaEntity("31", "وهران", "Oran", 350.0),
        WilayaEntity("25", "قسنطينة", "Constantine", 350.0),
        WilayaEntity("19", "سطيف", "Sétif", 300.0),
        WilayaEntity("09", "البليدة", "Blida", 350.0),
        WilayaEntity("15", "تيزي وزو", "Tizi Ouzou", 350.0),
        WilayaEntity("23", "عنابة", "Annaba", 350.0),
        WilayaEntity("13", "تلمسان", "Tlemcen", 350.0),
        WilayaEntity("06", "بجاية", "Béjaïa", 350.0),
        WilayaEntity("05", "باتنة", "Batna", 300.0)
    )
    db.wilayaDao().insertWilayas(wilayas)

    // 2. Seed Coupons
    val coupons = listOf(
        CouponEntity("MARHABA10", 300.0, 1000.0),
        CouponEntity("DZ500", 500.0, 2500.0),
        CouponEntity("TAWSEEL", 400.0, 1200.0)
    )
    db.couponDao().insertCoupons(coupons)

    // 3. Seed Merchants
    val merchants = listOf(
        MerchantEntity(
            id = "m_bardo",
            nameAr = "بيسترو الباردو ذواقة",
            nameFr = "Le Bardo Bistro Gourmet",
            category = DeliveryCategory.FOOD,
            wilayaCode = "16",
            wilayaName = "Alger - Hydra",
            address = "Rue Didouche / Hydra, Alger",
            rating = 4.8,
            prepTimeMinutes = 25,
            deliveryFeeDzd = 400.0,
            minOrderDzd = 800.0,
            tagAr = "برجر، طاكوس، مأكولات تقليدية",
            tagFr = "Burgers, Tacos, Cuisine Algérienne"
        ),
        MerchantEntity(
            id = "m_didouche",
            nameAr = "صيدلية ديدوش مراد",
            nameFr = "Pharmacie Didouche Mourad",
            category = DeliveryCategory.PHARMACY,
            wilayaCode = "16",
            wilayaName = "Alger Centre",
            address = "Boulevard Didouche Mourad, Alger",
            rating = 4.9,
            prepTimeMinutes = 10,
            deliveryFeeDzd = 350.0,
            minOrderDzd = 400.0,
            tagAr = "أدوية، فيتامينات، مستلزمات طبية",
            tagFr = "Médicaments, Vitamines, Parapharmacie"
        ),
        MerchantEntity(
            id = "m_bahia",
            nameAr = "سوبرماركت الباهية إكسبرس",
            nameFr = "Supermarché El Bahia Express",
            category = DeliveryCategory.GROCERY,
            wilayaCode = "31",
            wilayaName = "Oran - Akid Lotfi",
            address = "Boulevard Akid Lotfi, Oran",
            rating = 4.7,
            prepTimeMinutes = 15,
            deliveryFeeDzd = 350.0,
            minOrderDzd = 1000.0,
            tagAr = "أجبان، ألبان، فواكه، مواد تنظيف",
            tagFr = "Produits frais, Épicerie, Boissons"
        ),
        MerchantEntity(
            id = "m_kasbah",
            nameAr = "حلويات ومخبزة القصبة التقليدية",
            nameFr = "Pâtisserie El Kasbah",
            category = DeliveryCategory.FOOD,
            wilayaCode = "16",
            wilayaName = "Alger - Bab El Oued",
            address = "Bab El Oued, Alger",
            rating = 4.9,
            prepTimeMinutes = 20,
            deliveryFeeDzd = 400.0,
            minOrderDzd = 600.0,
            tagAr = "مقروط، قلب اللوز، بقلاوة، كرواسون",
            tagFr = "Pâtisseries traditionnelles, Makroud, Thé"
        ),
        MerchantEntity(
            id = "m_express",
            nameAr = "ديزاد إكسبرس للطرود والمراسلات",
            nameFr = "DZ Colis & Documents Express",
            category = DeliveryCategory.PARCEL,
            wilayaCode = "16",
            wilayaName = "Alger & National",
            address = "Hub Central Bab Ezzouar, Alger",
            rating = 4.9,
            prepTimeMinutes = 5,
            deliveryFeeDzd = 500.0,
            minOrderDzd = 500.0,
            tagAr = "توصيل طرود، عقود رسمية، مستندات",
            tagFr = "Livraison plis urgents, Contrats, Colis"
        ),
        MerchantEntity(
            id = "m_cirta",
            nameAr = "شواية ومطعم سيرتا",
            nameFr = "Rôtisserie Cirta Constantine",
            category = DeliveryCategory.FOOD,
            wilayaCode = "25",
            wilayaName = "Constantine",
            address = "Cité Zouaghi, Constantine",
            rating = 4.6,
            prepTimeMinutes = 30,
            deliveryFeeDzd = 350.0,
            minOrderDzd = 1200.0,
            tagAr = "مشويات، دجاج محمر، شخشوخة قسنطينية",
            tagFr = "Grillades, Chakhchoukha, Poulet braisé"
        )
    )
    db.merchantDao().insertMerchants(merchants)

    // 4. Seed Products
    val products = listOf(
        // Le Bardo
        ProductEntity("p_bardo_1", "m_bardo", "كسكسي جزائري ملكي باللحم", "Couscous Royal Algérien", "كسكسي قمح صلب تقليدي مع لحم غنم وخضار طازجة ومرق أحمر", "Couscous traditionnel à la viande d'agneau et légumes frais", 1450.0, "Plats", isAvailable = true, isPopular = true),
        ProductEntity("p_bardo_2", "m_bardo", "طاكوس ديزاد دوبل فروماج", "Tacos DZ Double Fromage", "دجاج متبل، صلصة جزائرية حارة، جبن غرويير، بطاطا مقلية", "Poulet mariné, sauce algérienne, frites et fromage fondu", 850.0, "Fast Food", isAvailable = true, isPopular = true),
        ProductEntity("p_bardo_3", "m_bardo", "برجر غورميه تلمساني", "Burger Gourmet Tlemcen", "شريحة لحم عجل محلي 180غ، جبن شيدر، بصل مكرمل وصوص خاص", "Steak pur bœuf 180g, cheddar affiné et oignons caramélisés", 950.0, "Burgers", isAvailable = true, isPopular = false),
        ProductEntity("p_bardo_4", "m_bardo", "بيتزا فواكه البحر المتوسطية", "Pizza Fruits de Mer Méditerranée", "عجينة إيطالية مخمرة، جمبري، حبار، جبن موزاريلا وزيتون سيق", "Crevettes, calamars, mozzarella fraîche et olives de Sig", 1300.0, "Pizzas", isAvailable = true, isPopular = true),
        ProductEntity("p_bardo_5", "m_bardo", "مشروب حمود بوعلام سيليكتو 1ل", "Hamoud Boualem Selecto 1L", "المشروب الجزائري الأصيل المنعش", "Boisson gazeuse Selecto mythique algérienne 1L", 150.0, "Boissons", isAvailable = true, isPopular = false),

        // Pharmacie Didouche
        ProductEntity("p_pharma_1", "m_didouche", "دولبران 1000 ملغ (علبة 8 أقراص)", "Doliprane 1000mg (8 comprimés)", "باراسيتامول لتسكين الآلام وخفض الحرارة", "Paracétamol pour douleurs et fièvre", 220.0, "Médicaments", isAvailable = true, isPopular = true),
        ProductEntity("p_pharma_2", "m_didouche", "فيتامين سي فوار 1000 ملغ", "Vitamine C Effervescente 1000mg", "مكمل غذائي لتعزيز المناعة ومقاومة التعب", "Complément tonus et vitalité immunitaire", 650.0, "Vitamines", isAvailable = true, isPopular = true),
        ProductEntity("p_pharma_3", "m_didouche", "علبة ضمادات طبية معقمة متنوعة", "Boîte Pansements Stériles Assortis", "ضمادات مقاومة للماء لحماية الجروح", "Protection hypoallergénique et résistante à l'eau", 380.0, "Premiers Soins", isAvailable = true, isPopular = false),
        ProductEntity("p_pharma_4", "m_didouche", "محلول فيزيولوجي معقم 500 مل", "Sérum Physiologique Stérile 500ml", "لتنظيف الأنف والعيون للأطفال والكبار", "Solution pour lavage nasal et ophtalmique", 190.0, "Soins", isAvailable = true, isPopular = false),

        // Supermarché El Bahia
        ProductEntity("p_gro_1", "m_bahia", "زيت زيتون معسكر بكر ممتاز 1ل", "Huile d'Olive Vierge Mascara 1L", "زيت زيتون جزائري طبيعي معصور على البارد", "Huile d'olive pure pression à froid", 1200.0, "Épicerie", isAvailable = true, isPopular = true),
        ProductEntity("p_gro_2", "m_bahia", "حليب صومام معقم 1ل (حزمة 6)", "Pack 6x Lait Soummam UHT 1L", "حليب كامل الدسم مبستر عالي الجودة", "Lait entier de qualité supérieure pasteurisé", 720.0, "Crèmerie", isAvailable = true, isPopular = true),
        ProductEntity("p_gro_3", "m_bahia", "بن فاميكو مطحون 250غ", "Café Famico Moulu 250g", "قهوة جزائرية غنية بالرائحة والنكهة القوية", "Café torréfié traditionnel goût intense", 320.0, "Petit Déjeuner", isAvailable = true, isPopular = false),
        ProductEntity("p_gro_4", "m_bahia", "مياه معدنية لالة خديجة 1.5ل (حزمة 6)", "Pack Eau Lalla Khedidja 6x1.5L", "مياه ينابيع طبيعية نقية من جبال جرجرة", "Eau minérale naturelle des montagnes du Djurdjura", 270.0, "Boissons", isAvailable = true, isPopular = false),

        // Pâtisserie El Kasbah
        ProductEntity("p_kas_1", "m_kasbah", "علبة مقروط عسل جزائري بالتمر (1كغ)", "Boîte Makroud au Miel & Dattes (1kg)", "مقروط عاصمي تقليدي معسل بالتمر واللوز وماء الزهر", "Makroud algérois fondant au miel pur et fleur d'oranger", 1100.0, "Gâteaux", isAvailable = true, isPopular = true),
        ProductEntity("p_kas_2", "m_kasbah", "قلب اللوز الجزائري بالسمن البلدي (8 قطع)", "Kalb El Louz Traditionnel (8 pcs)", "معسل أصيل محشو باللوز ومحضر بأجود المكونات", "Pâtisserie moelleuse aux amandes imbibée de sirop parfumé", 750.0, "Gâteaux", isAvailable = true, isPopular = true),
        ProductEntity("p_kas_3", "m_kasbah", "علبة بقلاوة جزائرية بالجوز واللوز", "Baklawa Algérienne Noix & Amandes", "طبقات مقرمشة مورقة معسلة بالعسل الحر", "Feuilletage croustillant aux fruits secs et miel", 1600.0, "Gâteaux", isAvailable = true, isPopular = false),

        // DZ Colis Express
        ProductEntity("p_exp_1", "m_express", "توصيل وثائق وعقود عاجلة (نفس اليوم)", "Livraison Document / Pli Urgent", "توصيل فوري آمن للوثائق الإدارية والمستندات", "Transport express sécurisé sous pli scellé", 600.0, "Express", isAvailable = true, isPopular = true),
        ProductEntity("p_exp_2", "m_express", "شحن طرد حتى 5 كغ داخل الولاية", "Colis Standard jusqu'à 5kg", "توصيل طرد مع تتبع مباشر وإشعار بالاستلام", "Livraison avec suivi et preuve de remise", 800.0, "Colis", isAvailable = true, isPopular = true)
    )
    db.productDao().insertProducts(products)

    // 5. Seed Drivers
    val drivers = listOf(
        DriverEntity(
            id = "d_karim",
            name = "Karim Benali",
            phone = "+213 550 12 34 56",
            vehicleType = "Moto Honda CG 125",
            wilayaCode = "16",
            isOnline = true,
            rating = 4.9,
            totalDeliveries = 342,
            todayEarningsDzd = 4800.0,
            activeOrderId = "DZ-8021"
        ),
        DriverEntity(
            id = "d_sofiane",
            name = "Sofiane Meftah",
            phone = "+213 661 78 90 12",
            vehicleType = "Scooter Peugeot Tweet 150",
            wilayaCode = "31",
            isOnline = true,
            rating = 4.8,
            totalDeliveries = 219,
            todayEarningsDzd = 3200.0,
            activeOrderId = null
        ),
        DriverEntity(
            id = "d_amine",
            name = "Amine Djoudi",
            phone = "+213 770 45 67 89",
            vehicleType = "Renault Clio 4 Express",
            wilayaCode = "16",
            isOnline = true,
            rating = 4.7,
            totalDeliveries = 158,
            todayEarningsDzd = 2600.0,
            activeOrderId = null
        )
    )
    db.driverDao().insertDrivers(drivers)

    // 6. Seed Realistic Orders
    val orders = listOf(
        OrderEntity(
            id = "DZ-8021",
            customerPhone = "+213 555 88 99 00",
            customerName = "Amina Cherif",
            customerAddress = "Résidence Sahraoui, Bab Ezzouar, Alger",
            wilayaCode = "16",
            merchantId = "m_didouche",
            merchantName = "Pharmacie Didouche Mourad",
            driverId = "d_karim",
            driverName = "Karim Benali",
            driverPhone = "+213 550 12 34 56",
            driverVehicle = "Moto Honda CG 125",
            status = OrderStatus.DELIVERING,
            subtotalDzd = 870.0,
            deliveryFeeDzd = 350.0,
            discountDzd = 0.0,
            totalDzd = 1220.0,
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            paymentStatus = "PENDING",
            notes = "Appelez à l'interphone B3 svp",
            otpCode = "5183",
            createdAtMillis = System.currentTimeMillis() - 25 * 60 * 1000,
            updatedAtMillis = System.currentTimeMillis() - 5 * 60 * 1000
        ),
        OrderEntity(
            id = "DZ-8022",
            customerPhone = "+213 662 33 44 55",
            customerName = "Reda Belkacem",
            customerAddress = "Cité 1000 Logements, Kouba, Alger",
            wilayaCode = "16",
            merchantId = "m_bardo",
            merchantName = "Le Bardo Bistro Gourmet",
            driverId = null,
            driverName = null,
            status = OrderStatus.READY_FOR_PICKUP,
            subtotalDzd = 2300.0,
            deliveryFeeDzd = 400.0,
            discountDzd = 300.0,
            totalDzd = 2400.0,
            paymentMethod = PaymentMethod.EDAHABIA_BARIDIMOB,
            paymentStatus = "PAID",
            notes = "Sauce algérienne supplémentaire svp",
            otpCode = "7942",
            createdAtMillis = System.currentTimeMillis() - 40 * 60 * 1000,
            updatedAtMillis = System.currentTimeMillis() - 8 * 60 * 1000
        ),
        OrderEntity(
            id = "DZ-8023",
            customerPhone = "+213 771 99 88 77",
            customerName = "Yassine Mansouri",
            customerAddress = "Rue Larbi Ben M'hidi, Alger Centre",
            wilayaCode = "16",
            merchantId = "m_kasbah",
            merchantName = "Pâtisserie El Kasbah",
            driverId = null,
            driverName = null,
            status = OrderStatus.PREPARING,
            subtotalDzd = 1850.0,
            deliveryFeeDzd = 400.0,
            discountDzd = 0.0,
            totalDzd = 2250.0,
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            paymentStatus = "PENDING",
            notes = "Livrer avant 18h si possible",
            otpCode = "3461",
            createdAtMillis = System.currentTimeMillis() - 15 * 60 * 1000,
            updatedAtMillis = System.currentTimeMillis() - 12 * 60 * 1000
        ),
        OrderEntity(
            id = "DZ-8019",
            customerPhone = "+213 555 88 99 00",
            customerName = "Amina Cherif",
            customerAddress = "Résidence Sahraoui, Bab Ezzouar, Alger",
            wilayaCode = "16",
            merchantId = "m_bardo",
            merchantName = "Le Bardo Bistro Gourmet",
            driverId = "d_amine",
            driverName = "Amine Djoudi",
            driverPhone = "+213 770 45 67 89",
            driverVehicle = "Renault Clio 4 Express",
            status = OrderStatus.DELIVERED,
            subtotalDzd = 1700.0,
            deliveryFeeDzd = 400.0,
            discountDzd = 0.0,
            totalDzd = 2100.0,
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            paymentStatus = "PAID",
            notes = "",
            otpCode = "1284",
            createdAtMillis = System.currentTimeMillis() - 24 * 60 * 60 * 1000,
            updatedAtMillis = System.currentTimeMillis() - 23 * 60 * 60 * 1000
        )
    )

    orders.forEach { order ->
        db.orderDao().insertOrder(order)
    }

    // Seed Order Items
    val orderItems = listOf(
        OrderItemEntity(orderId = "DZ-8021", productId = "p_pharma_1", productNameAr = "دولبران 1000 ملغ", productNameFr = "Doliprane 1000mg", unitPriceDzd = 220.0, quantity = 1),
        OrderItemEntity(orderId = "DZ-8021", productId = "p_pharma_2", productNameAr = "فيتامين سي فوار", productNameFr = "Vitamine C Effervescente", unitPriceDzd = 650.0, quantity = 1),
        OrderItemEntity(orderId = "DZ-8022", productId = "p_bardo_1", productNameAr = "كسكسي جزائري ملكي", productNameFr = "Couscous Royal Algérien", unitPriceDzd = 1450.0, quantity = 1),
        OrderItemEntity(orderId = "DZ-8022", productId = "p_bardo_2", productNameAr = "طاكوس ديزاد دوبل فروماج", productNameFr = "Tacos DZ Double Fromage", unitPriceDzd = 850.0, quantity = 1),
        OrderItemEntity(orderId = "DZ-8023", productId = "p_kas_1", productNameAr = "علبة مقروط عسل بالتمر", productNameFr = "Boîte Makroud au Miel (1kg)", unitPriceDzd = 1100.0, quantity = 1),
        OrderItemEntity(orderId = "DZ-8023", productId = "p_kas_2", productNameAr = "قلب اللوز الجزائري", productNameFr = "Kalb El Louz Traditionnel", unitPriceDzd = 750.0, quantity = 1)
    )
    db.orderDao().insertOrderItems(orderItems)
}
