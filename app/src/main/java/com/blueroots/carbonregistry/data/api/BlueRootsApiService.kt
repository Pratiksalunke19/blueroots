package com.blueroots.carbonregistry.data.api

import com.blueroots.carbonregistry.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface BlueRootsApiService {

    // Project Registration Endpoints
    @POST("projects/register")
    suspend fun registerProject(@Body project: ProjectRegistration): Response<ProjectResponse>

    @GET("projects")
    suspend fun getAllProjects(): Response<List<ProjectRegistration>>

    @GET("projects/{projectId}")
    suspend fun getProject(@Path("projectId") projectId: String): Response<ProjectRegistration>

    @PUT("projects/{projectId}")
    suspend fun updateProject(
        @Path("projectId") projectId: String,
        @Body project: ProjectRegistration
    ): Response<ProjectResponse>

    @DELETE("projects/{projectId}")
    suspend fun deleteProject(@Path("projectId") projectId: String): Response<ProjectResponse>

    // Monitoring Data Endpoints
    @POST("monitoring/upload")
    suspend fun uploadMonitoringData(@Body data: MonitoringData): Response<UploadResponse>

    @GET("monitoring/{projectId}")
    suspend fun getMonitoringData(@Path("projectId") projectId: String): Response<List<MonitoringData>>

    @GET("monitoring/data/{dataId}")
    suspend fun getMonitoringDataById(@Path("dataId") dataId: String): Response<MonitoringData>

    @PUT("monitoring/{dataId}")
    suspend fun updateMonitoringData(
        @Path("dataId") dataId: String,
        @Body data: MonitoringData
    ): Response<UploadResponse>

    @DELETE("monitoring/{dataId}")
    suspend fun deleteMonitoringData(@Path("dataId") dataId: String): Response<UploadResponse>

    // Carbon Credits Endpoints
    @GET("credits")
    suspend fun getAllCredits(): Response<List<CarbonCredit>>

    @GET("credits/{userId}")
    suspend fun getUserCredits(@Path("userId") userId: String): Response<List<CarbonCredit>>

    @GET("credits/project/{projectId}")
    suspend fun getProjectCredits(@Path("projectId") projectId: String): Response<List<CarbonCredit>>

    @POST("credits/issue")
    suspend fun issueCredits(@Body request: CreditIssuanceRequest): Response<CreditIssuanceResponse>

    @POST("credits/transfer")
    suspend fun transferCredits(@Body request: CreditTransferRequest): Response<CreditTransferResponse>

    @POST("credits/retire")
    suspend fun retireCredits(@Body request: CreditRetirementRequest): Response<CreditRetirementResponse>

    // Verification Endpoints
    @POST("verification/request")
    suspend fun requestVerification(@Body request: VerificationRequest): Response<VerificationResponse>

    // UPDATED: Changed return type to VerificationRequestStatus
    @GET("verification/{requestId}")
    suspend fun getVerificationRequestStatus(@Path("requestId") requestId: String): Response<VerificationRequestStatus>

    // Market Data Endpoints
    @GET("market/prices")
    suspend fun getMarketPrices(): Response<MarketPricesResponse>

    @GET("market/transactions")
    suspend fun getMarketTransactions(@Query("limit") limit: Int = 50): Response<List<MarketTransaction>>

    // User Authentication Endpoints
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body refreshRequest: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<LogoutResponse>

    // File Upload Endpoints
    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(
        @Part("file") file: okhttp3.MultipartBody.Part,
        @Part("type") type: okhttp3.RequestBody,
        @Part("projectId") projectId: okhttp3.RequestBody
    ): Response<FileUploadResponse>

    @GET("files/{fileId}")
    suspend fun getFile(@Path("fileId") fileId: String): Response<FileInfo>

    @DELETE("files/{fileId}")
    suspend fun deleteFile(@Path("fileId") fileId: String): Response<DeleteResponse>
}
