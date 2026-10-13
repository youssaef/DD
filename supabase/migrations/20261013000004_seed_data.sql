-- ======================================================================================
-- DZ DELIVERY PRO - SUPABASE POSTGRESQL MIGRATION 04: SEED DATA (DEVELOPMENT ONLY)
-- ======================================================================================

-- 1. Service Categories
INSERT INTO public.service_categories (id, code, name_ar, name_fr, icon_name) VALUES
    ('10000000-0000-0000-0000-000000000001', 'food', 'مطاعم ومأكولات', 'Restaurants', 'fastfood'),
    ('10000000-0000-0000-0000-000000000002', 'pharmacy', 'صيدليات وأدوية', 'Pharmacies', 'local_pharmacy'),
    ('10000000-0000-0000-0000-000000000003', 'grocery', 'بقالة وسوبرماركت', 'Supermarchés', 'shopping_basket'),
    ('10000000-0000-0000-0000-000000000004', 'parcel', 'طرود ووثائق سريعة', 'Colis & Express', 'local_shipping'),
    ('10000000-0000-0000-0000-000000000005', 'custom_errand', 'توصيل خاص بين الأفراد', 'Coursier Express', 'motorcycle')
ON CONFLICT (code) DO NOTHING;

-- 2. Algerian Service Areas (Wilayas)
INSERT INTO public.service_areas (id, wilaya_code, name_ar, name_fr, base_delivery_fee) VALUES
    ('20000000-0000-0000-0000-000000000016', '16', 'الجزائر العاصمة', 'Alger', 400.00),
    ('20000000-0000-0000-0000-000000000031', '31', 'وهران', 'Oran', 350.00),
    ('20000000-0000-0000-0000-000000000025', '25', 'قسنطينة', 'Constantine', 350.00),
    ('20000000-0000-0000-0000-000000000019', '19', 'سطيف', 'Sétif', 300.00),
    ('20000000-0000-0000-0000-000000000009', '09', 'البليدة', 'Blida', 350.00),
    ('20000000-0000-0000-0000-000000000015', '15', 'تيزي وزو', 'Tizi Ouzou', 350.00)
ON CONFLICT (wilaya_code) DO NOTHING;

-- 3. Coupons
INSERT INTO public.coupons (id, code, discount_amount, min_order_amount) VALUES
    ('30000000-0000-0000-0000-000000000001', 'MARHABA10', 300.00, 1000.00),
    ('30000000-0000-0000-0000-000000000002', 'DZ500', 500.00, 2500.00),
    ('30000000-0000-0000-0000-000000000003', 'TAWSEEL', 400.00, 1200.00)
ON CONFLICT (code) DO NOTHING;

-- 4. Demo Profiles for Roles
INSERT INTO public.profiles (id, phone, full_name, preferred_language) VALUES
    ('00000000-0000-0000-0000-000000000001', '+213555889900', 'Amina Cherif', 'fr'),
    ('00000000-0000-0000-0000-000000000002', '+213550123456', 'Karim Benali', 'fr'),
    ('00000000-0000-0000-0000-000000000003', '+213661789012', 'Bardo Chef', 'fr'),
    ('00000000-0000-0000-0000-000000000004', '+213770000001', 'Admin Central DZ', 'fr')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.user_roles (user_id, role) VALUES
    ('00000000-0000-0000-0000-000000000001', 'customer'),
    ('00000000-0000-0000-0000-000000000002', 'driver'),
    ('00000000-0000-0000-0000-000000000003', 'merchant'),
    ('00000000-0000-0000-0000-000000000004', 'admin')
ON CONFLICT (user_id, role) DO NOTHING;

-- 5. Driver Profiles
INSERT INTO public.driver_profiles (id, user_id, vehicle_type, wilaya_code, is_online, rating, total_deliveries, today_earnings) VALUES
    ('40000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'Moto Honda CG 125', '16', true, 4.90, 342, 4800.00)
ON CONFLICT (user_id) DO NOTHING;

-- 6. Merchants
INSERT INTO public.merchants (id, owner_id, category_id, service_area_id, name_ar, name_fr, address, rating, prep_time_minutes, delivery_fee, min_order_amount, tag_ar, tag_fr) VALUES
    ('50000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000016', 'بيسترو الباردو ذواقة', 'Le Bardo Bistro Gourmet', 'Rue Didouche / Hydra, Alger', 4.85, 25, 400.00, 800.00, 'برجر، طاكوس، مأكولات تقليدية', 'Burgers, Tacos, Cuisine Algérienne'),
    ('50000000-0000-0000-0000-000000000002', NULL, '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000016', 'صيدلية ديدوش مراد', 'Pharmacie Didouche Mourad', 'Boulevard Didouche Mourad, Alger', 4.95, 10, 350.00, 400.00, 'أدوية، فيتامينات، مستلزمات طبية', 'Médicaments, Parapharmacie'),
    ('50000000-0000-0000-0000-000000000003', NULL, '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000031', 'سوبرماركت الباهية إكسبرس', 'Supermarché El Bahia Express', 'Boulevard Akid Lotfi, Oran', 4.70, 15, 350.00, 1000.00, 'أجبان، ألبان، فواكه، مواد تنظيف', 'Produits frais, Épicerie, Boissons')
ON CONFLICT (id) DO NOTHING;

-- 7. Products
INSERT INTO public.products (id, merchant_id, name_ar, name_fr, desc_ar, desc_fr, price, category_name, is_available, is_popular) VALUES
    ('60000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 'كسكسي جزائري ملكي باللحم', 'Couscous Royal Algérien', 'كسكسي قمح صلب تقليدي مع لحم غنم وخضار طازجة', 'Couscous traditionnel à la viande d''agneau', 1450.00, 'Plats', true, true),
    ('60000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000001', 'طاكوس ديزاد دوبل فروماج', 'Tacos DZ Double Fromage', 'دجاج متبل، صلصة جزائرية حارة، جبن غرويير', 'Poulet mariné, sauce algérienne, frites', 850.00, 'Fast Food', true, true),
    ('60000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000002', 'دولبران 1000 ملغ (علبة 8 أقراص)', 'Doliprane 1000mg (8 comprimés)', 'باراسيتامول لتسكين الآلام وخفض الحرارة', 'Paracétamol pour douleurs et fièvre', 220.00, 'Médicaments', true, true),
    ('60000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000002', 'فيتامين سي فوار 1000 ملغ', 'Vitamine C Effervescente 1000mg', 'مكمل غذائي لتعزيز المناعة ومقاومة التعب', 'Complément tonus et vitalité', 650.00, 'Vitamines', true, true),
    ('60000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000003', 'زيت زيتون معسكر بكر ممتاز 1ل', 'Huile d''Olive Vierge Mascara 1L', 'زيت زيتون طبيعي معصور على البارد', 'Huile d''olive pure pression à froid', 1200.00, 'Épicerie', true, true)
ON CONFLICT (id) DO NOTHING;

-- 8. Seed Orders
INSERT INTO public.orders (id, order_number, customer_id, customer_phone, customer_name, customer_address, wilaya_code, merchant_id, merchant_name, driver_id, driver_name, driver_phone, driver_vehicle, status, subtotal, delivery_fee, discount, total, payment_method, payment_status, otp_code) VALUES
    ('70000000-0000-0000-0000-000000000001', 'DZ-8021', '00000000-0000-0000-0000-000000000001', '+213555889900', 'Amina Cherif', 'Résidence Sahraoui, Bab Ezzouar, Alger', '16', '50000000-0000-0000-0000-000000000002', 'Pharmacie Didouche Mourad', '40000000-0000-0000-0000-000000000001', 'Karim Benali', '+213550123456', 'Moto Honda CG 125', 'delivering', 870.00, 350.00, 0.00, 1220.00, 'cash_on_delivery', 'pending', '5183'),
    ('70000000-0000-0000-0000-000000000002', 'DZ-8022', '00000000-0000-0000-0000-000000000001', '+213555889900', 'Amina Cherif', 'Résidence Sahraoui, Bab Ezzouar, Alger', '16', '50000000-0000-0000-0000-000000000001', 'Le Bardo Bistro Gourmet', NULL, NULL, NULL, NULL, 'ready_for_pickup', 2300.00, 400.00, 300.00, 2400.00, 'edahabia_baridimob', 'completed', '7942')
ON CONFLICT (id) DO NOTHING;
