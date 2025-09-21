package com.blueroots.carbonregistry.data.models

import java.util.Date

data class ProjectRegistration(
    val id: String = "",
    val projectName: String = "",
    val projectDescription: String = "",
    val projectType: EcosystemType = EcosystemType.MANGROVE,
    val projectStatus: ProjectStatus = ProjectStatus.PLANNING,

    // Location Details
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "",
    val state: String = "",
    val district: String = "",
    val nearestCity: String = "",
    val projectArea: Double = 0.0, // in hectares

    // Project Timeline
    val startDate: Date = Date(),
    val projectDuration: Int = 0, // in years
    val credatingPeriod: Int = 0, // in years

    // Financial Details
    val estimatedInvestment: Double = 0.0,
    val fundingSource: FundingSource = FundingSource.PRIVATE,
    val expectedCreditGeneration: Double = 0.0, // tCO2e per year

    // Stakeholder Information
    val projectDeveloper: ProjectDeveloper = ProjectDeveloper(),
    val localCommunityPartner: String = "",
    val technicalPartner: String = "",
    val verificationBody: String = "",

    // Environmental Details
    val existingVegetation: String = "",
    val soilType: String = "",
    val hydrologyDetails: String = "",
    val biodiversityBaseline: String = "",
    val threatsAndRisks: List<String> = emptyList(),

    // Compliance
    val methodology: CarbonMethodology = CarbonMethodology.VCS_VM0007,
    val standardsCompliance: List<String> = emptyList(),
    val socialSafeguards: List<String> = emptyList(),

    // Documents
    val projectDocuments: List<ProjectDocument> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class ProjectDeveloper(
    val organizationName: String = "",
    val contactPerson: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val organizationType: OrganizationType = OrganizationType.PRIVATE_COMPANY,
    val experience: String = ""
)

data class ProjectDocument(
    val id: String = "",
    val name: String = "",
    val type: DocumentType = DocumentType.PROJECT_DESIGN,
    val url: String = "",
    val uploadDate: Date = Date()
)

enum class EcosystemType {
    MANGROVE,
    SEAGRASS,
    SALT_MARSH,
    COASTAL_WETLAND
}

enum class ProjectStatus {
    PLANNING,
    REGISTERED,
    UNDER_VALIDATION,
    VALIDATED,
    IMPLEMENTATION,
    MONITORING,
    VERIFICATION,
    COMPLETED,
    SUSPENDED
}

enum class FundingSource {
    PRIVATE,
    PUBLIC,
    BLENDED,
    DEVELOPMENT_FINANCE,
    CARBON_FINANCE,
    COMMUNITY_BASED
}

enum class CarbonMethodology {
    VCS_VM0007, // Restoration of degraded coastal wetlands
    CDM_AMS_III_BF, // Small-scale wetland restoration
    GOLD_STANDARD_WETLAND,
    BLUE_CARBON_ACCELERATOR,
    CUSTOM
}

enum class OrganizationType {
    PRIVATE_COMPANY,
    NGO,
    GOVERNMENT_AGENCY,
    RESEARCH_INSTITUTION,
    COMMUNITY_ORGANIZATION,
    INTERNATIONAL_ORGANIZATION
}

enum class DocumentType {
    PROJECT_DESIGN,
    ENVIRONMENTAL_IMPACT,
    SOCIAL_IMPACT,
    MONITORING_PLAN,
    VALIDATION_REPORT,
    VERIFICATION_REPORT,
    COMMUNITY_AGREEMENT,
    LEGAL_PERMITS,
    BASELINE_STUDY,
    SAFEGUARDS_PLAN
}
