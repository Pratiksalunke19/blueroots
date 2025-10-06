package com.blueroots.carbonregistry.data.repository

import android.util.Log
import com.blueroots.carbonregistry.data.models.MonitoringData
import com.blueroots.carbonregistry.data.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.text.SimpleDateFormat
import java.util.*

class MonitoringRepository {

    private val supabase = SupabaseClient.client
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    /**
     * Get current authenticated user ID from Supabase Auth
     */
    private suspend fun getCurrentUserId(): String? {
        return try {
            SupabaseClient.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            Log.e("MonitoringRepository", "❌ Failed to get current user: ${e.message}")
            null
        }
    }

    /**
     * Insert monitoring data to Supabase database
     * Returns monitoring data UUID from Supabase if successful, null otherwise
     */
    suspend fun insertMonitoringData(monitoringData: MonitoringData): String? {
        return try {
            // Get authenticated user ID
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e("MonitoringRepository", "❌ No authenticated user found")
                return null
            }

            Log.d("MonitoringRepository", "📤 Inserting monitoring data for user: $userId")

            // Build JSON object for data_payload (stores all monitoring details as JSONB)
            val dataPayload = buildJsonObject {
                // Location information
                putJsonObject("location") {
                    put("latitude", monitoringData.location.latitude)
                    put("longitude", monitoringData.location.longitude)
                    put("altitude", monitoringData.location.altitude)
                    put("plot_id", monitoringData.location.plotId)
                    put("transect_id", monitoringData.location.transectId)
                    put("site_description", monitoringData.location.siteDescription)
                    put("gps_accuracy", monitoringData.location.gpsAccuracy)
                }

                // Soil data (if applicable)
                monitoringData.soilData?.let { soil ->
                    putJsonObject("soil_data") {
                        put("sample_id", soil.sampleId)
                        put("sample_depth", soil.sampleDepth)
                        put("organic_carbon_content", soil.organicCarbonContent)
                        put("bulk_density", soil.bulkDensity)
                        put("moisture", soil.moisture)
                        put("ph", soil.pH)
                        put("salinity", soil.salinity)
                        put("sample_temperature", soil.sampleTemperature)
                    }
                }

                // Photos
                if (monitoringData.photos.isNotEmpty()) {
                    putJsonArray("photos") {
                        monitoringData.photos.forEach { photo ->
                            addJsonObject {
                                put("id", photo.id)
                                put("filename", photo.filename)
                                put("local_path", photo.localPath)
                                put("caption", photo.caption)
                                put("gps_coordinates", photo.gpsCoordinates)
                                put("timestamp", dateTimeFormat.format(photo.timestamp))
                                put("photo_type", photo.photoType.name)
                            }
                        }
                    }
                }

                // Additional metadata
                put("notes", monitoringData.notes)
                put("data_collector", monitoringData.dataCollector)
                put("collector_qualifications", monitoringData.collectorQualifications)
                put("reporting_period", monitoringData.reportingPeriod)
                put("compliance_standard", monitoringData.complianceStandard)
                put("methodology_version", monitoringData.methodologyVersion)
                put("priority", monitoringData.priority.name)
                put("is_complete", monitoringData.isComplete)
                put("sync_status", monitoringData.syncStatus.name)

                // Equipment used
                if (monitoringData.equipmentUsed.isNotEmpty()) {
                    putJsonArray("equipment_used") {
                        monitoringData.equipmentUsed.forEach { equipment ->
                            add(kotlinx.serialization.json.JsonPrimitive(equipment))
                        }
                    }
                }


                // Quality control checks
                if (monitoringData.qualityControlChecks.isNotEmpty()) {
                    putJsonArray("quality_control_checks") {
                        monitoringData.qualityControlChecks.forEach { check ->
                            addJsonObject {
                                put("check_type", check.checkType)
                                put("check_result", check.checkResult.name)
                                put("checker", check.checker)
                                put("check_date", dateTimeFormat.format(check.checkDate))
                                check.notes?.let { put("notes", it) }
                            }
                        }
                    }
                }
            }

            // Build main monitoring data JSON
            val monitoringJson = buildJsonObject {
                put("user_id", userId)
//                put("project_id", monitoringData.projectId)
                put("data_type", monitoringData.dataType.name)
                put("monitoring_date", dateFormat.format(monitoringData.monitoringDate))
                put("location_lat", monitoringData.location.latitude)
                put("location_lng", monitoringData.location.longitude)
                put("data_payload", dataPayload.toString())
                put("verification_status", monitoringData.verificationStatus.name)
            }

            // Insert into Supabase
            val response = supabase.from("monitoring_data")
                .insert(monitoringJson) {
                    select()
                }

            // Parse response to get monitoring data ID
            val responseData = response.data
            Log.d("MonitoringRepository", "✅ Monitoring data inserted successfully: $responseData")

            // Extract UUID from response
            val monitoringId = extractMonitoringId(responseData)

            if (monitoringId != null) {
                Log.d("MonitoringRepository", "✅ Monitoring ID: $monitoringId")
            } else {
                Log.w("MonitoringRepository", "⚠️ Could not extract monitoring ID from response")
            }

            monitoringId

        } catch (e: Exception) {
            Log.e("MonitoringRepository", "❌ Failed to insert monitoring data: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    /**
     * Fetch all monitoring data for current user from Supabase
     */
    suspend fun fetchUserMonitoringData(): List<Map<String, Any>> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e("MonitoringRepository", "❌ No authenticated user found")
                return emptyList()
            }

            val response = supabase.from("monitoring_data")
                .select() {
                    filter {
                        eq("user_id", userId)
                    }
                    order("monitoring_date", Order.DESCENDING)
                }

            Log.d("MonitoringRepository", "✅ Fetched monitoring data: ${response.data}")

            // Return parsed list (you can convert to MonitoringData objects if needed)
            emptyList() // TODO: Parse response.data JSON to list of maps

        } catch (e: Exception) {
            Log.e("MonitoringRepository", "❌ Failed to fetch monitoring data: ${e.message}")
            emptyList()
        }
    }

    /**
     * Check if monitoring data exists in Supabase
     */
    suspend fun monitoringDataExists(monitoringId: String): Boolean {
        return try {
            val response = supabase.from("monitoring_data")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("id", monitoringId)
                    }
                    limit(1)
                }

            val dataString = response.data
            val exists = dataString != "[]" && dataString.isNotEmpty()

            Log.d("MonitoringRepository", "🔍 Monitoring data exists: $exists")
            exists

        } catch (e: Exception) {
            Log.e("MonitoringRepository", "❌ Failed to check monitoring data existence: ${e.message}")
            false
        }
    }

    /**
     * Extract monitoring UUID from Supabase response
     */
    private fun extractMonitoringId(responseData: String): String? {
        return try {
            // Response format: [{"id":"550e8400-e29b-41d4-a716-446655440000",...}]
            val idPattern = """"id"\s*:\s*"([^"]+)"""".toRegex()
            val matchResult = idPattern.find(responseData)
            matchResult?.groupValues?.getOrNull(1)
        } catch (e: Exception) {
            Log.e("MonitoringRepository", "Failed to parse monitoring ID: ${e.message}")
            null
        }
    }
}
