package com.blueroots.carbonregistry.data.repository

import android.util.Log
import com.blueroots.carbonregistry.data.models.ProjectRegistration
import com.blueroots.carbonregistry.data.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.*

class ProjectRepository {

    private val supabase = SupabaseClient.client
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Get current authenticated user ID from Supabase Auth
     */
    private suspend fun getCurrentUserId(): String? {
        return try {
            SupabaseClient.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            Log.e("ProjectRepository", "❌ Failed to get current user: ${e.message}")
            null
        }
    }

    /**
     * Insert project to Supabase database
     * Returns project UUID from Supabase if successful, null otherwise
     */
    suspend fun insertProject(project: ProjectRegistration): String? {
        return try {
            // Get authenticated user ID
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e("ProjectRepository", "❌ No authenticated user found")
                return null
            }

            Log.d("ProjectRepository", "📤 Inserting project for user: $userId")

            // Build JSON object (avoids serialization issues)
            val projectJson = buildJsonObject {
                put("user_id", userId)
                put("project_name", project.projectName)
                put("project_description", project.projectDescription)
                put("project_type", project.projectType.name)
                put("latitude", project.latitude)
                put("longitude", project.longitude)
                put("country", project.country)
                put("state", project.state)
                put("district", project.district)
                put("nearest_city", project.nearestCity)
                put("project_area", project.projectArea)
                put("start_date", dateFormat.format(project.startDate))
                put("project_duration", project.projectDuration)
                put("crediting_period", project.credatingPeriod)
                put("estimated_investment", project.estimatedInvestment)
                put("funding_source", project.fundingSource.name)
                put("expected_credit_generation", project.expectedCreditGeneration)
                put("methodology", project.methodology.name)
                put("status", project.projectStatus.name)

                // Store project developer as JSONB
                put("project_developer", buildJsonObject {
                    put("organization_name", project.projectDeveloper.organizationName)
                    put("contact_person", project.projectDeveloper.contactPerson)
                    put("email", project.projectDeveloper.email)
                    put("phone", project.projectDeveloper.phone)
                    put("address", project.projectDeveloper.address)
                    put("organization_type", project.projectDeveloper.organizationType.name)
                    put("experience", project.projectDeveloper.experience)
                }.toString())
            }

            // Insert into Supabase and get the created project ID
            val response = supabase.from("projects")
                .insert(projectJson) {
                    select()
                }

            // Parse response to get project ID
            val responseData = response.data
            Log.d("ProjectRepository", "✅ Project inserted successfully: $responseData")

            // Extract UUID from response (format: [{"id":"uuid-here",...}])
            val projectId = extractProjectId(responseData)

            if (projectId != null) {
                Log.d("ProjectRepository", "✅ Project ID: $projectId")
            } else {
                Log.w("ProjectRepository", "⚠️ Could not extract project ID from response")
            }

            projectId

        } catch (e: Exception) {
            Log.e("ProjectRepository", "❌ Failed to insert project: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    /**
     * Check if project exists in Supabase by project name and user ID
     */
    suspend fun projectExists(projectName: String): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false

            val response = supabase.from("projects")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("user_id", userId)
                        eq("project_name", projectName)
                    }
                    limit(1)
                }

            val dataString = response.data
            val exists = dataString != "[]" && dataString.isNotEmpty()

            Log.d("ProjectRepository", "🔍 Project '$projectName' exists: $exists")
            exists

        } catch (e: Exception) {
            Log.e("ProjectRepository", "❌ Failed to check project existence: ${e.message}")
            false
        }
    }

    /**
     * Fetch all projects for current user from Supabase
     */
    suspend fun fetchUserProjects(): List<Map<String, Any>> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e("ProjectRepository", "❌ No authenticated user found")
                return emptyList()
            }

            val response = supabase.from("projects")
                .select() {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                }

            Log.d("ProjectRepository", "✅ Fetched projects: ${response.data}")

            // Return parsed list (you can convert to ProjectRegistration objects if needed)
            emptyList() // TODO: Parse response.data JSON to list of maps

        } catch (e: Exception) {
            Log.e("ProjectRepository", "❌ Failed to fetch projects: ${e.message}")
            emptyList()
        }
    }

    /**
     * Extract project UUID from Supabase response
     */
    private fun extractProjectId(responseData: String): String? {
        return try {
            // Response format: [{"id":"550e8400-e29b-41d4-a716-446655440000",...}]
            val idPattern = """"id"\s*:\s*"([^"]+)"""".toRegex()
            val matchResult = idPattern.find(responseData)
            matchResult?.groupValues?.getOrNull(1)
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Failed to parse project ID: ${e.message}")
            null
        }
    }
}
