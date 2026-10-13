package com.example

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SupabaseSchemaAndRlsTest {

    private val requiredTables = listOf(
        "profiles", "user_roles", "addresses", "driver_profiles", "driver_documents",
        "vehicles", "merchants", "merchant_memberships", "service_categories",
        "service_areas", "products", "product_variants", "inventory", "coupons",
        "orders", "order_items", "order_status_history", "delivery_assignments",
        "driver_locations", "delivery_proofs", "payments", "refunds",
        "platform_commissions", "driver_earnings", "merchant_settlements",
        "financial_transactions", "notifications", "reviews", "support_tickets",
        "audit_logs", "platform_settings"
    )

    private val privateTables = listOf(
        "profiles", "user_roles", "addresses", "orders", "order_items",
        "payments", "refunds", "financial_transactions", "audit_logs",
        "driver_earnings", "merchant_settlements"
    )

    @Test
    fun testAllMandatoryTablesDefinedInMigrations() {
        val migrationDir = File("../supabase/migrations")
        val altMigrationDir = File("supabase/migrations")
        val targetDir = if (migrationDir.exists()) migrationDir else altMigrationDir

        assertTrue("Migration directory should exist", targetDir.exists())

        val combinedSql = targetDir.listFiles()
            ?.filter { it.extension == "sql" }
            ?.joinToString("\n") { it.readText() } ?: ""

        assertTrue("Combined migrations must not be empty", combinedSql.isNotBlank())

        for (table in requiredTables) {
            val containsTable = combinedSql.contains("CREATE TABLE IF NOT EXISTS public.$table", ignoreCase = true) ||
                    combinedSql.contains("CREATE TABLE public.$table", ignoreCase = true)
            assertTrue("Table public.$table must be defined in migrations", containsTable)
        }
    }

    @Test
    fun testRlsEnabledOnAllTables() {
        val migrationDir = File("../supabase/migrations")
        val altMigrationDir = File("supabase/migrations")
        val targetDir = if (migrationDir.exists()) migrationDir else altMigrationDir

        val combinedSql = targetDir.listFiles()
            ?.filter { it.extension == "sql" }
            ?.joinToString("\n") { it.readText() } ?: ""

        for (table in requiredTables) {
            val rlsEnabled = combinedSql.contains("ALTER TABLE public.$table ENABLE ROW LEVEL SECURITY", ignoreCase = true)
            assertTrue("RLS must be enabled on table public.$table", rlsEnabled)
        }
    }

    @Test
    fun testZeroTrustPolicyEnforcementOnPrivateTables() {
        val migrationDir = File("../supabase/migrations")
        val altMigrationDir = File("supabase/migrations")
        val targetDir = if (migrationDir.exists()) migrationDir else altMigrationDir

        val combinedSql = targetDir.listFiles()
            ?.filter { it.extension == "sql" }
            ?.joinToString("\n") { it.readText() } ?: ""

        // Ensure no private table has unrestricted read: 'ON public.<table-name> FOR SELECT USING (true)'
        for (table in privateTables) {
            val hasUnrestrictedSelect = combinedSql.contains("ON public.$table FOR SELECT USING (true)", ignoreCase = true) ||
                    combinedSql.contains("ON public.$table FOR ALL USING (true)", ignoreCase = true)
            assertFalse("Private table $table must not have unrestricted USING (true) policy", hasUnrestrictedSelect)
        }
    }
}
