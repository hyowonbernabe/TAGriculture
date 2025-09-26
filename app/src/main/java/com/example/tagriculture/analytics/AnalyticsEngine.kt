package com.example.tagriculture.analytics

import com.example.tagriculture.data.database.Animal
import com.example.tagriculture.data.database.WeightEntry
import com.github.mikephil.charting.data.Entry
import java.util.concurrent.TimeUnit

data class ChartData(
    val actualHistory: List<Entry>,
    val projectedHistory: List<Entry>,
    val idealCurve: List<Entry>
)

data class AnalyticsReport(
    val feedEfficiencyIndex: Double,
    val conditionScore: String,
    val readinessAlerts: List<String>,
    val chartData: ChartData,
    val ageString: String,
    val lifespanAlert: String?
)

object AnalyticsEngine {

    fun generateReport(animal: Animal, history: List<WeightEntry>): AnalyticsReport {
        val actualHistoryEntries = history.map { Entry(it.date.toFloat(), it.weight.toFloat()) }

        return AnalyticsReport(
            feedEfficiencyIndex = calculateFEI(animal),
            conditionScore = determineConditionScore(animal),
            readinessAlerts = checkReadiness(animal),
            chartData = ChartData(
                actualHistory = actualHistoryEntries,
                projectedHistory = calculateProjectedGain(history),
                idealCurve = generateIdealCurve(animal),
            ),
            ageString = calculateAgeString(animal.birthDate),
            lifespanAlert = checkLifespan(animal)
        )
    }

    private fun calculateAgeString(birthDate: Long): String {
        val ageInMillis = System.currentTimeMillis() - birthDate
        val years = TimeUnit.MILLISECONDS.toDays(ageInMillis) / 365
        val months = (TimeUnit.MILLISECONDS.toDays(ageInMillis) % 365) / 30
        return "$years years, $months months"
    }

    private fun checkLifespan(animal: Animal): String? {
        val ageInMillis = System.currentTimeMillis() - animal.birthDate
        val ageInYears = TimeUnit.MILLISECONDS.toDays(ageInMillis) / 365.0

        val averageLifespan = when (animal.animalType) {
            "Horse" -> 25
            "Buffalo" -> 20
            else -> 0
        }

        return if (averageLifespan > 0 && ageInYears > averageLifespan * 0.8) {
            "Approaching average lifespan ($averageLifespan years)"
        } else {
            null
        }
    }

    private fun calculateProjectedGain(history: List<WeightEntry>): List<Entry> {
        if (history.size < 2) return emptyList()

        val sortedHistory = history.sortedBy { it.date }
        val firstEntry = sortedHistory.first()
        val lastEntry = sortedHistory.last()

        val durationDays = TimeUnit.MILLISECONDS.toDays(lastEntry.date - firstEntry.date)
        if (durationDays < 1) return emptyList()

        val totalGain = lastEntry.weight - firstEntry.weight
        val adg = totalGain / durationDays

        val projection = mutableListOf<Entry>()
        projection.add(Entry(lastEntry.date.toFloat(), lastEntry.weight.toFloat()))

        for (i in 1..3) {
            val futureDate = lastEntry.date + TimeUnit.DAYS.toMillis(30L * i)
            val futureWeight = lastEntry.weight + (adg * 30 * i)
            projection.add(Entry(futureDate.toFloat(), futureWeight.toFloat()))
        }
        return projection
    }

    private fun generateIdealCurve(animal: Animal): List<Entry> {
        val idealAdg = when(animal.animalType) {
            "Cattle" -> 0.7
            "Sheep", "Goat" -> 0.05
            "Pig" -> 0.4
            else -> 0.0
        }
        if (idealAdg == 0.0) return emptyList()

        val curve = mutableListOf<Entry>()
        val ageInMillis = System.currentTimeMillis() - animal.birthDate
        val ageInDays = TimeUnit.MILLISECONDS.toDays(ageInMillis)

        for (i in 0..ageInDays step 30) {
            val date = animal.birthDate + TimeUnit.DAYS.toMillis(i)
            val weight = animal.birthWeight + (idealAdg * i)
            curve.add(Entry(date.toFloat(), weight.toFloat()))
        }
        curve.add(Entry((animal.birthDate + ageInMillis).toFloat(), (animal.birthWeight + (idealAdg * ageInDays)).toFloat()))

        return curve
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