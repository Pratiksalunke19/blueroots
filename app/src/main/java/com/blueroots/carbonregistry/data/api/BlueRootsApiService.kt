package com.blueroots.carbonregistry.data.api

import com.blueroots.carbonregistry.data.models.ProjectRegistration
import com.blueroots.carbonregistry.data.models.ProjectResponse
import com.blueroots.carbonregistry.data.models.MonitoringData
import com.blueroots.carbonregistry.data.models.UploadResponse
import com.blueroots.carbonregistry.data.models.CarbonCredit
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.Path

interface BlueRootsApiService {
    @POST("projects/register")
    suspend fun registerProject(@Body project: ProjectRegistration): Response<ProjectResponse>

    @POST("monitoring/upload")
    suspend fun uploadMonitoringData(@Body data: MonitoringData): Response<UploadResponse>

    @GET("credits/{userId}")
    suspend fun getUserCredits(@Path("userId") userId: String): Response<List<CarbonCredit>>
}
