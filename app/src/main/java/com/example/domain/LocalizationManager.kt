package com.example.domain

import androidx.compose.ui.unit.LayoutDirection
import java.text.NumberFormat
import java.util.Locale

enum class AppLanguage(val code: String, val title: String, val layoutDirection: LayoutDirection) {
    ARABIC("ar", "العربية", LayoutDirection.Rtl),
    FRENCH("fr", "Français", LayoutDirection.Ltr)
}

object LocalizationManager {

    fun formatDzd(amount: Double, language: AppLanguage): String {
        val formattedNumber = NumberFormat.getNumberInstance(Locale.FRANCE).format(amount.toLong())
        return if (language == AppLanguage.ARABIC) {
            "$formattedNumber د.ج"
        } else {
            "$formattedNumber DZD"
        }
    }

    // Comprehensive Bilingual UI String dictionary
    fun get(key: String, language: AppLanguage): String {
        val entry = strings[key] ?: return key
        return if (language == AppLanguage.ARABIC) entry.first else entry.second
    }

    private val strings = mapOf(
        "app_name" to Pair("ديزاد دليفري برو", "DZ Delivery Pro"),
        "tagline" to Pair("منصتكم الأولى للتوصيل السريع في الجزائر", "Plateforme leader de livraison en Algérie"),
        "role_customer" to Pair("زبون", "Client"),
        "role_driver" to Pair("سائق / موصّل", "Livreur"),
        "role_merchant" to Pair("تاجر / مطعم", "Commerçant"),
        "role_admin" to Pair("إدارة المنصة", "Administration"),
        "select_role" to Pair("تبديل الواجهة والمهام", "Changer de rôle / Espace"),
        
        // Navigation & tabs
        "tab_explore" to Pair("استكشاف", "Explorer"),
        "tab_cart" to Pair("السلة", "Panier"),
        "tab_orders" to Pair("طلباتي", "Commandes"),
        "tab_profile" to Pair("حسابي", "Profil"),
        
        // Categories
        "cat_all" to Pair("الكل", "Tous"),
        "cat_food" to Pair("مطاعم ومأكولات", "Restaurants"),
        "cat_pharmacy" to Pair("صيدليات وأدوية", "Pharmacies"),
        "cat_grocery" to Pair("بقالة وسوبرماركت", "Supermarchés"),
        "cat_parcel" to Pair("طرود ووثائق", "Colis & Express"),
        "cat_errand" to Pair("توصيل خاص", "Coursier Express"),
        
        // Search & Wilaya
        "search_hint" to Pair("ابحث عن مطعم، صيدلية، منتج...", "Rechercher un commerce, plat, produit..."),
        "wilaya_label" to Pair("الولاية", "Wilaya"),
        "current_location" to Pair("الجزائر العاصمة، باب الزوار", "Alger, Bab Ezzouar"),
        
        // Statuses
        "status_pending" to Pair("قيد الانتظار", "En attente"),
        "status_confirmed" to Pair("مؤكد من التاجر", "Confirmée"),
        "status_preparing" to Pair("قيد التحضير", "En préparation"),
        "status_ready_for_pickup" to Pair("جاهز للاستلام", "Prêt pour retrait"),
        "status_assigned" to Pair("تم تعيين السائق", "Livreur assigné"),
        "status_picked_up" to Pair("تم الاستلام من التاجر", "Colis récupéré"),
        "status_delivering" to Pair("في طريق التوصيل", "En cours de livraison"),
        "status_delivered" to Pair("تم التوصيل بنجاح", "Livrée avec succès"),
        "status_cancelled" to Pair("ملغاة", "Annulée"),
        
        // Cart & Checkout
        "cart_empty" to Pair("السلة فارغة حالياً", "Votre panier est vide"),
        "cart_empty_sub" to Pair("تصفح المتاجر المفضلة لديك وأضف المنتجات", "Explorez les commerces pour ajouter des articles"),
        "subtotal" to Pair("المجموع الفرعي", "Sous-total"),
        "delivery_fee" to Pair("رسوم التوصيل", "Frais de livraison"),
        "service_fee" to Pair("رسوم الخدمة", "Frais de service"),
        "discount" to Pair("الخصم الترويجي", "Remise promo"),
        "total" to Pair("المجموع الإجمالي", "Total à payer"),
        "apply_coupon" to Pair("تطبيق الكوبون", "Appliquer"),
        "coupon_hint" to Pair("رمز القسيمة (مثال: MARHABA10)", "Code promo (ex: MARHABA10)"),
        "checkout_btn" to Pair("تأكيد الطلب الآن", "Confirmer la commande"),
        "delivery_address" to Pair("عنوان التوصيل", "Adresse de livraison"),
        "delivery_notes" to Pair("ملاحظات للسائق / تعليمات", "Instructions de livraison"),
        "payment_method" to Pair("طريقة الدفع", "Mode de paiement"),
        "pay_cod" to Pair("الدفع نقداً عند الاستلام", "Paiement à la livraison (Espèces)"),
        "pay_edahabia" to Pair("البطاقة الذهبية / بريديموب", "Edahabia / BaridiMob"),
        "pay_cib" to Pair("البطاقة البنكية CIB", "Carte bancaire CIB"),
        
        // Tracking
        "tracking_title" to Pair("تتبع مسار الطلب", "Suivi en temps réel"),
        "otp_code" to Pair("رمز تأكيد التسليم (OTP)", "Code de validation (OTP)"),
        "otp_instruction" to Pair("أعطِ هذا الرمز للسائق عند استلام طلبك", "Donnez ce code au livreur à la réception"),
        "driver_info" to Pair("معلومات السائق", "Informations du livreur"),
        "call_driver" to Pair("اتصال", "Appeler"),
        "estimated_arrival" to Pair("الوقت المقدر للوصول", "Temps estimé"),
        
        // Driver space
        "driver_status_online" to Pair("أنت متصل - متاح للطلبات", "En ligne - Disponible"),
        "driver_status_offline" to Pair("أنت غير متصل", "Hors ligne"),
        "available_requests" to Pair("طلبات التوصيل المتاحة", "Courses disponibles"),
        "accept_delivery" to Pair("قبول التوصيل", "Accepter la course"),
        "decline_delivery" to Pair("رفض", "Refuser"),
        "pickup_from" to Pair("الاستلام من", "Retrait chez"),
        "deliver_to" to Pair("التوصيل إلى", "Livrer à"),
        "confirm_pickup_btn" to Pair("تأكيد استلام الطرد", "Confirmer le retrait"),
        "confirm_delivery_btn" to Pair("تأكيد التسليم للزبون", "Valider la livraison"),
        "enter_otp_prompt" to Pair("أدخل رمز OTP من الزبون (4 أرقام)", "Entrez le code OTP client (4 chiffres)"),
        "today_earnings" to Pair("أرباح اليوم", "Gains du jour"),
        "completed_trips" to Pair("التوصيلات المكتملة", "Courses terminées"),
        
        // Merchant space
        "merchant_orders" to Pair("طلبات المحل", "Commandes reçues"),
        "accept_order" to Pair("قبول وتحضير", "Accepter & Préparer"),
        "mark_ready" to Pair("جاهز للتسليم للسائق", "Prêt pour retrait"),
        "manage_catalog" to Pair("قائمة المنتجات والأسعار", "Gestion du catalogue"),
        "add_product" to Pair("إضافة منتج جديد", "Ajouter un produit"),
        "product_in_stock" to Pair("متوفر", "En stock"),
        "product_out_of_stock" to Pair("نفذ", "Épuisé"),
        
        // Admin
        "admin_dashboard" to Pair("لوحة قيادة العمليات الجزائرية", "Centre d'Opérations DZ"),
        "platform_gmv" to Pair("إجمالي المبيعات", "Volume d'affaires total"),
        "active_orders_count" to Pair("الطلبات النشطة", "Commandes en cours"),
        "active_drivers_count" to Pair("السائقين المتاحين", "Livreurs actifs"),
        "merchants_count" to Pair("المتاجر المعتمدة", "Commerces partenaires"),
        "dispatch_console" to Pair("وحدة المراقبة والتوزيع", "Console de Dispatch")
    )
}
