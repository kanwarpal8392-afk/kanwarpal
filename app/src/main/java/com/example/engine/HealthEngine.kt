package com.example.engine

data class BmiResult(
    val bmi: Double,
    val category: String,
    val minHealthyWeight: Double,
    val maxHealthyWeight: Double,
    val colorHex: Long
)

object HealthEngine {
    const val DISCLAIMER = "Results are estimates for informational purposes and are not a medical diagnosis or medical advice. Consult a qualified healthcare professional for health-related decisions."

    fun calculateBmi(heightCm: Double, weightKg: Double): BmiResult {
        if (heightCm <= 0 || weightKg <= 0) {
            return BmiResult(0.0, "Invalid Input", 0.0, 0.0, 0xFF9E9E9E)
        }
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)

        val category = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal Weight"
            bmi < 30.0 -> "Overweight"
            bmi < 35.0 -> "Obese Class I"
            bmi < 40.0 -> "Obese Class II"
            else -> "Obese Class III"
        }

        val color = when {
            bmi < 18.5 -> 0xFF38BDF8 // Sky blue
            bmi < 25.0 -> 0xFF10B981 // Emerald green
            bmi < 30.0 -> 0xFFF59E0B // Amber
            else -> 0xFFEF4444 // Red
        }

        val minHealthy = 18.5 * (heightM * heightM)
        val maxHealthy = 24.9 * (heightM * heightM)

        return BmiResult(bmi, category, minHealthy, maxHealthy, color)
    }

    fun calculateBmr(
        heightCm: Double,
        weightKg: Double,
        ageYears: Int,
        isMale: Boolean
    ): Double {
        if (heightCm <= 0 || weightKg <= 0 || ageYears <= 0) return 0.0
        // Mifflin-St Jeor
        val base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * ageYears)
        return if (isMale) base + 5 else base - 161
    }

    fun calculateTdee(bmr: Double, activityLevel: String): Double {
        val multiplier = when (activityLevel.lowercase()) {
            "sedentary" -> 1.2
            "light" -> 1.375
            "moderate" -> 1.55
            "active" -> 1.725
            "extreme" -> 1.9
            else -> 1.2
        }
        return bmr * multiplier
    }

    fun calculateWaterIntakeLiters(weightKg: Double, activeMinutes: Int = 30): Double {
        val baseMl = weightKg * 35.0
        val exerciseBonusMl = (activeMinutes / 30.0) * 350.0
        return (baseMl + exerciseBonusMl) / 1000.0
    }
}
