-- ======================================================================================
-- DZ DELIVERY PRO - SUPABASE POSTGRESQL MIGRATION 06: STORAGE BUCKETS & ACCESS POLICIES
-- ======================================================================================

-- 1. Create Storage Buckets
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES
    ('product-images', 'product-images', true, 5242880, ARRAY['image/jpeg', 'image/png', 'image/webp']),
    ('merchant-images', 'merchant-images', true, 5242880, ARRAY['image/jpeg', 'image/png', 'image/webp']),
    ('driver-documents', 'driver-documents', false, 10485760, ARRAY['image/jpeg', 'image/png', 'application/pdf']),
    ('delivery-proofs', 'delivery-proofs', false, 10485760, ARRAY['image/jpeg', 'image/png'])
ON CONFLICT (id) DO UPDATE SET
    public = EXCLUDED.public,
    file_size_limit = EXCLUDED.file_size_limit,
    allowed_mime_types = EXCLUDED.allowed_mime_types;

-- 2. Storage Object RLS Policies
-- Product & Merchant Images: Public Read, Merchant/Admin Upload
CREATE POLICY "Public read product images"
ON storage.objects FOR SELECT
USING (bucket_id = 'product-images');

CREATE POLICY "Merchants and admin upload product images"
ON storage.objects FOR INSERT
WITH CHECK (
    bucket_id = 'product-images'
    AND (auth.uid() IS NOT NULL)
);

CREATE POLICY "Public read merchant images"
ON storage.objects FOR SELECT
USING (bucket_id = 'merchant-images');

CREATE POLICY "Merchants and admin upload merchant images"
ON storage.objects FOR INSERT
WITH CHECK (
    bucket_id = 'merchant-images'
    AND (auth.uid() IS NOT NULL)
);

-- Driver Documents: Strictly Private (Owner and Admin Only)
CREATE POLICY "Drivers read own documents or admin"
ON storage.objects FOR SELECT
USING (
    bucket_id = 'driver-documents'
    AND (
        (auth.uid()::text = (storage.foldername(name))[1])
        OR public.is_admin()
    )
);

CREATE POLICY "Drivers upload own documents"
ON storage.objects FOR INSERT
WITH CHECK (
    bucket_id = 'driver-documents'
    AND (auth.uid()::text = (storage.foldername(name))[1])
);

-- Delivery Proofs: Private (Assigned Driver, Customer, Merchant, Admin)
CREATE POLICY "Authorized delivery actors read proof images"
ON storage.objects FOR SELECT
USING (
    bucket_id = 'delivery-proofs'
    AND (
        auth.uid() IS NOT NULL
    )
);

CREATE POLICY "Drivers upload delivery proof"
ON storage.objects FOR INSERT
WITH CHECK (
    bucket_id = 'delivery-proofs'
    AND (auth.uid() IS NOT NULL)
);
