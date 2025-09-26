package com.example.tagriculture.analytics

import com.example.tagriculture.data.database.Animal
import com.example.tagriculture.data.database.WeightEntry
import java.util.concurrent.TimeUnit

data class AnalyticsReport(
    val feedEfficiencyIndex: Double,
    val conditionScore: String,
    val readinessAlerts: List<String>
)

object AnalyticsEngine {

    fun generateReport(animal: Animal, history: List<WeightEntry>): AnalyticsReport {
        return AnalyticsReport(
            feedEfficiencyIndex = calculateFEI(animal),
            conditionScore = determineConditionScore(animal),
            readinessAlerts = checkReadiness(animal)
        )
    }

    private fun calculateFEI(animal: Animal): Double {
        val ageInMillis = System.currentTimeMillis() - animal.birthDate
        val ageInDays = TimeUnit.MILLISECONDS.toDays(ageInMillis)
        if (ageInDays == 0L) return 0.0
        return animal.currentWeight / ageInDays
    }

    private fun determineConditionScore(animal: Animal): String {
        val fei = calculateFEI(animal)
        return when (animal.animalType) {
            "Cattle" -> when {
                fei < 0.35 -> "Underweight"
                fei > 0.65 -> "Overweight"
                else -> "Normal"
            }
            "Sheep", "Goat" -> when {
                fei < 0.04 -> "Underweight"
                fei > 0.08 -> "Overweight"
                else -> "Normal"
            }
            else -> "N/A"
        }
    }

    private fun checkReadiness(animal: Animal): List<String> {
        val alerts = mutableListOf<String>()
        val ageInMillis = System.currentTimeMillis() - animal.birthDate
        val ageInYears = TimeUnit.MILLISECONDS.toDays(ageInMillis) / 365.0

        if (animal.animalType == "Cattle") {
            if (ageInYears > 1.8 && animal.currentWeight > 350) {
                alerts.add("Ready for Market")
            }
            if (ageInYears > 1.5) {
                alerts.add("Ready for Breeding")
            }
        }
        return alerts
    }
}