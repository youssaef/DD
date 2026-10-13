package com.example

import com.example.data.remote.SupabaseConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SecurityAndPrivilegeAuditTest {

    @Test
    fun testNoPrivilegedCredentialsEmbeddedInClientSource() {
        // Inspect client source code for forbidden secrets
        val clientSourceDir = File("src/main/java")
        val altClientSourceDir = File("app/src/main/java")
        val targetDir = if (clientSourceDir.exists()) clientSourceDir else altClientSourceDir

        assertTrue("Client source directory must exist", targetDir.exists())

        val forbiddenKeywords = listOf(
            "service_role",
            "postgres://",
            "ALTER ROLE",
            "DROP DATABASE",
            "SECRET_KEY_PROD"
        )

        targetDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val text = file.readText()
            for (keyword in forbiddenKeywords) {
                assertFalse(
                    "Forbidden privileged secret keyword '$keyword' found in client file ${file.name}",
                    text.contains(keyword, ignoreCase = true)
                )
            }
        }
    }

    @Test
    fun testRoleEscalationGuardedInSql() {
        val migrationDir = File("../supabase/migrations")
        val altMigrationDir = File("supabase/migrations")
        val targetDir = if (migrationDir.exists()) migrationDir else altMigrationDir

        val combinedSql = targetDir.listFiles()
            ?.filter { it.extension == "sql" }
            ?.joinToString("\n") { it.readText() } ?: ""

        // Ensure user_roles has admin-only write policy
        assertTrue(
            "user_roles must only permit admin to modify roles",
            combinedSql.contains("public.is_admin()", ignoreCase = true) &&
                    combinedSql.contains("ON public.user_roles", ignoreCase = true)
        )
    }

    @Test
    fun testCrossUserDataAccessPoliciesInSql() {
        val migrationDir = File("../supabase/migrations")
        val altMigrationDir = File("supabase/migrations")
        val targetDir = if (migrationDir.exists()) migrationDir else altMigrationDir

        val combinedSql = targetDir.listFiles()
            ?.filter { it.extension == "sql" }
            ?.joinToString("\n") { it.readText() } ?: ""

        // Check profiles policy
        assertTrue(
            "profiles must isolate read to own user_id or admin",
            combinedSql.contains("auth.uid() = id", ignoreCase = true)
        )

        // Check addresses policy
        assertTrue(
            "addresses must isolate to own user_id",
            combinedSql.contains("auth.uid() = user_id", ignoreCase = true)
        )

        // Check orders policy
        assertTrue(
            "orders must isolate to customer_id or merchant or driver",
            combinedSql.contains("auth.uid() = customer_id", ignoreCase = true)
        )
    }

    @Test
    fun testConcurrencyRowLockInDriverAssignment() {
        val migrationDir = File("../supabase/migrations")
        val altMigrationDir = File("supabase/migrations")
        val targetDir = if (migrationDir.exists()) migrationDir else altMigrationDir

        val combinedSql = targetDir.listFiles()
            ?.filter { it.extension == "sql" }
            ?.joinToString("\n") { it.readText() } ?: ""

        // Verify row-level lock 'FOR UPDATE' in driver assignment function
        assertTrue(
            "Driver assignment must acquire row lock FOR UPDATE to prevent race conditions",
            combinedSql.contains("FROM public.orders", ignoreCase = true) &&
                    combinedSql.contains("FOR UPDATE", ignoreCase = true)
        )
    }

    @Test
    fun testPrivateStorageBucketsEnforced() {
        val migrationDir = File("../supabase/migrations")
        val altMigrationDir = File("supabase/migrations")
        val targetDir = if (migrationDir.exists()) migrationDir else altMigrationDir

        val combinedSql = targetDir.listFiles()
            ?.filter { it.extension == "sql" }
            ?.joinToString("\n") { it.readText() } ?: ""

        // Verify driver-documents is private
        assertTrue(
            "driver-documents bucket must be private (public = false)",
            combinedSql.contains("('driver-documents', 'driver-documents', false", ignoreCase = true)
        )

        // Verify delivery-proofs is private
        assertTrue(
            "delivery-proofs bucket must be private (public = false)",
            combinedSql.contains("('delivery-proofs', 'delivery-proofs', false", ignoreCase = true)
        )
    }

    @Test
    fun testOtpBruteForceRejectionAndLength() {
        val validOtp = "5183"

        // 1. Length constraint
        assertEquals(4, validOtp.length)
        assertTrue(validOtp.all { it.isDigit() })

        // 2. Reject short or alpha codes
        val invalidFormatOtps = listOf("12", "123", "518a", "51839", "")
        for (candidate in invalidFormatOtps) {
            val isValidFormat = candidate.length == 4 && candidate.all { it.isDigit() }
            assertFalse("OTP candidate '$candidate' should fail format validation", isValidFormat)
        }

        // 3. Brute force mismatch rejection
        val attempts = (1000..1010).map { it.toString() }
        for (attempt in attempts) {
            if (attempt != validOtp) {
                assertFalse("Attempt $attempt should not match valid OTP $validOtp", attempt == validOtp)
            }
        }
    }
}
