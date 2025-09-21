package com.blueroots.carbonregistry.data.models

import java.util.Date

data class MonitoringData(
    val id: String = "",
    val projectId: String = "",
    val projectName: String = "",
    val dataType: MonitoringDataType = MonitoringDataType.SOIL_SAMPLE,
    val monitoringDate: Date = Date(),
    val reportingPeriod: String = "", // Q1 2024, etc.
    val location: MonitoringLocation = MonitoringLocation(),

    // Environmental Parameters
    val soilData: SoilData? = null,
    val vegetationData: VegetationData? = null,
    val hydrologyData: HydrologyData? = null,
    val biodiversityData: BiodiversityData? = null,
    val carbonData: CarbonData? = null,
    val climaticData: ClimaticData? = null,

    // Documentation
    val photos: List<MonitoringPhoto> = emptyList(),
    val notes: String = "",
    val dataCollector: String = "",
    val collectorQualifications: String = "",
    val equipmentUsed: List<String> = emptyList(),

    // Quality Assurance
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val qualityControlChecks: List<QualityCheck> = emptyList(),
    val chainOfCustody: String = "",

    // Compliance
    val complianceStandard: String = "", // VCS, Gold Standard, etc.
    val methodologyVersion: String = "",
    val deviations: List<String> = emptyList(),
    val correctiveActions: List<String> = emptyList(),

    // Metadata
    val submissionDeadline: Date? = null,
    val isComplete: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val syncStatus: SyncStatus = SyncStatus.LOCAL
)

data class SoilData(
    val sampleId: String = "",
    val sampleDepth: Double = 0.0, // cm
    val sampleMethod: SamplingMethod = SamplingMethod.CORE_SAMPLING,
    val organicCarbonContent: Double = 0.0, // %
    val totalNitrogen: Double = 0.0, // %
    val totalPhosphorus: Double = 0.0, // mg/kg
    val bulkDensity: Double = 0.0, // g/cm³
    val porosity: Double = 0.0, // %
    val moisture: Double = 0.0, // %
    val pH: Double = 0.0,
    val salinity: Double = 0.0, // ppt
    val electricalConductivity: Double = 0.0, // dS/m
    val carbonStock: Double = 0.0, // tC/ha
    val sampleTemperature: Double = 0.0, // °C
    val laboratoryAnalysis: LabAnalysis? = null
)

data class VegetationData(
    val plotId: String = "",
    val plotSize: Double = 0.0, // m²
    val samplingMethod: String = "",
    val speciesInventory: List<SpeciesData> = emptyList(),
    val totalSpeciesCount: Int = 0,
    val dominantSpecies: List<String> = emptyList(),
    val canopyCover: Double = 0.0, // %
    val averageHeight: Double = 0.0, // m
    val maximumHeight: Double = 0.0, // m
    val stemDensity: Int = 0, // stems/ha
    val basalArea: Double = 0.0, // m²/ha
    val abovegroundBiomass: Double = 0.0, // kg/m²
    val belowgroundBiomass: Double = 0.0, // kg/m²
    val leafAreaIndex: Double = 0.0,
    val seedlingRecruitment: Int = 0, // per m²
    val mortalityRate: Double = 0.0, // %
    val growthRate: Double = 0.0, // cm/year
    val phenologyStage: String = "",
    val healthAssessment: HealthStatus = HealthStatus.HEALTHY
)

data class SpeciesData(
    val scientificName: String = "",
    val commonName: String = "",
    val abundance: Int = 0,
    val dbh: Double = 0.0, // diameter at breast height (cm)
    val height: Double = 0.0, // m
    val conservationStatus: ConservationStatus = ConservationStatus.LEAST_CONCERN
)

data class HydrologyData(
    val measurementPoint: String = "",
    val waterLevel: Double = 0.0, // m above datum
    val tidalRange: Double = 0.0, // m
    val highTideTime: String = "",
    val lowTideTime: String = "",
    val salinity: Double = 0.0, // ppt
    val waterTemperature: Double = 0.0, // °C
    val pH: Double = 0.0,
    val dissolvedOxygen: Double = 0.0, // mg/L
    val turbidity: Double = 0.0, // NTU
    val totalSuspendedSolids: Double = 0.0, // mg/L
    val flowVelocity: Double = 0.0, // m/s
    val flowDirection: Double = 0.0, // degrees
    val nutrients: NutrientData = NutrientData(),
    val pollutants: List<PollutantData> = emptyList(),
    val sedimentationRate: Double = 0.0 // mm/year
)

data class BiodiversityData(
    val surveyType: BiodiversitySurveyType = BiodiversitySurveyType.RAPID_ASSESSMENT,
    val surveyDuration: Int = 0, // hours
    val weather: WeatherCondition = WeatherCondition(),
    val fauna: FaunaData = FaunaData(),
    val flora: FloraData = FloraData(),
    val ecosystemServices: List<String> = emptyList(),
    val threatenedSpecies: List<String> = emptyList(),
    val invasiveSpecies: List<InvasiveSpeciesData> = emptyList(),
    val biodiversityIndices: BiodiversityIndices = BiodiversityIndices()
)

data class CarbonData(
    val measurementMethod: CarbonMethod = CarbonMethod.ALLOMETRIC_EQUATIONS,
    val carbonPools: CarbonPools = CarbonPools(),
    val totalCarbonStock: Double = 0.0, // tC/ha
    val totalCO2Equivalent: Double = 0.0, // tCO2e/ha
    val carbonSequestrationRate: Double = 0.0, // tCO2e/ha/year
    val uncertaintyLevel: Double = 0.0, // %
    val qaqcProcedures: List<String> = emptyList(),
    val calculationWorksheet: String = "",
    val verificationNotes: String = ""
)

data class CarbonPools(
    val abovegroundLiving: Double = 0.0, // tC/ha
    val belowgroundLiving: Double = 0.0, // tC/ha
    val deadwood: Double = 0.0, // tC/ha
    val litter: Double = 0.0, // tC/ha
    val soilOrganicCarbon: Double = 0.0, // tC/ha
    val harvested: Double = 0.0 // tC/ha
)

data class ClimaticData(
    val temperature: Double = 0.0, // °C
    val humidity: Double = 0.0, // %
    val precipitation: Double = 0.0, // mm
    val windSpeed: Double = 0.0, // m/s
    val windDirection: Double = 0.0, // degrees
    val barometricPressure: Double = 0.0, // hPa
    val solarRadiation: Double = 0.0, // W/m²
    val evapotranspiration: Double = 0.0 // mm/day
)

data class MonitoringLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0, // m above sea level
    val plotId: String = "",
    val transectId: String = "",
    val siteDescription: String = "",
    val accessibility: String = "",
    val landmarkReference: String = "",
    val gpsAccuracy: Double = 0.0 // m
)

data class MonitoringPhoto(
    val id: String = "",
    val filename: String = "",
    val localPath: String = "",
    val serverUrl: String = "",
    val caption: String = "",
    val gpsCoordinates: String = "",
    val compassDirection: Double = 0.0,
    val timestamp: Date = Date(),
    val photoType: PhotoType = PhotoType.GENERAL,
    val fileSize: Long = 0L,
    val resolution: String = "",
    val isGeotagged: Boolean = false
)

data class QualityCheck(
    val checkType: String = "",
    val checkResult: QualityResult = QualityResult.PASS,
    val notes: String = "",
    val checker: String = "",
    val checkDate: Date = Date(),
    val standardReference: String = ""
)

data class LabAnalysis(
    val labName: String = "",
    val analysisMethod: String = "",
    val certificationNumber: String = "",
    val analysisDate: Date = Date(),
    val reportNumber: String = ""
)

data class NutrientData(
    val nitrate: Double = 0.0, // mg/L
    val phosphate: Double = 0.0, // mg/L
    val ammonia: Double = 0.0, // mg/L
    val silicate: Double = 0.0 // mg/L
)

data class PollutantData(
    val pollutantType: String = "",
    val concentration: Double = 0.0,
    val unit: String = "",
    val detectionLimit: Double = 0.0
)

data class FaunaData(
    val birdSpecies: Int = 0,
    val fishSpecies: Int = 0,
    val crabSpecies: Int = 0,
    val molluskSpecies: Int = 0,
    val mammalSpecies: Int = 0,
    val reptileSpecies: Int = 0,
    val amphibianSpecies: Int = 0,
    val insectSpecies: Int = 0,
    val totalAbundance: Int = 0
)

data class FloraData(
    val treeSpecies: Int = 0,
    val shrubSpecies: Int = 0,
    val herbSpecies: Int = 0,
    val grassSpecies: Int = 0,
    val fernSpecies: Int = 0,
    val algaeSpecies: Int = 0,
    val totalCover: Double = 0.0 // %
)

data class InvasiveSpeciesData(
    val species: String = "",
    val coverageArea: Double = 0.0, // m²
    val invasionLevel: InvasionLevel = InvasionLevel.LOW,
    val controlMeasures: String = ""
)

data class BiodiversityIndices(
    val shannonIndex: Double = 0.0,
    val simpsonIndex: Double = 0.0,
    val evenness: Double = 0.0,
    val richness: Int = 0
)

data class WeatherCondition(
    val condition: String = "",
    val visibility: Double = 0.0, // km
    val cloudCover: Double = 0.0 // %
)

// Enums
enum class MonitoringDataType(val displayName: String) {
    SOIL_SAMPLE("Soil Sample"),
    VEGETATION_SURVEY("Vegetation Survey"),
    HYDROLOGY_MEASUREMENT("Hydrology Measurement"),
    BIODIVERSITY_ASSESSMENT("Biodiversity Assessment"),
    CARBON_MEASUREMENT("Carbon Measurement"),
    DRONE_SURVEY("Drone Survey"),
    SATELLITE_IMAGERY("Satellite Imagery"),
    COMMUNITY_REPORT("Community Report"),
    RESTORATION_PROGRESS("Restoration Progress"),
    MAINTENANCE_LOG("Maintenance Log"),
    CLIMATE_DATA("Climate Data"),
    WATER_QUALITY("Water Quality"),
    SPECIES_MONITORING("Species Monitoring"),
    THREAT_ASSESSMENT("Threat Assessment"),
    SOCIOECONOMIC_SURVEY("Socioeconomic Survey")
}

enum class VerificationStatus {
    PENDING, VERIFIED, REJECTED, REQUIRES_CLARIFICATION, IN_REVIEW
}

enum class PhotoType {
    GENERAL, SOIL_SAMPLE, VEGETATION, RESTORATION_SITE, EQUIPMENT,
    SPECIES_IDENTIFICATION, PROGRESS_COMPARISON, COMMUNITY_ACTIVITY,
    AERIAL_VIEW, REFERENCE_POINT, DAMAGE_ASSESSMENT, BEFORE_AFTER
}

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class SyncStatus {
    LOCAL, SYNCING, SYNCED, ERROR
}

enum class SamplingMethod {
    CORE_SAMPLING, AUGER_SAMPLING, PIT_SAMPLING, COMPOSITE_SAMPLING
}

enum class HealthStatus {
    EXCELLENT, HEALTHY, MODERATE, POOR, CRITICAL
}

enum class ConservationStatus {
    LEAST_CONCERN, NEAR_THREATENED, VULNERABLE, ENDANGERED, CRITICALLY_ENDANGERED
}

enum class BiodiversitySurveyType {
    RAPID_ASSESSMENT, DETAILED_SURVEY, LONG_TERM_MONITORING, SPECIES_SPECIFIC
}

enum class CarbonMethod {
    ALLOMETRIC_EQUATIONS, DIRECT_MEASUREMENT, REMOTE_SENSING, MODELING
}

enum class InvasionLevel {
    LOW, MODERATE, HIGH, SEVERE
}

enum class QualityResult {
    PASS, FAIL, WARNING, NEEDS_REVIEW
}
