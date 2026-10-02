package com.example.engine

import kotlin.math.log10
import kotlin.math.pow

object PhysicsChemistryEngine {
    const val GRAVITY_G = 9.80665

    // Ohm's Law Solver
    data class OhmsLawResult(
        val voltage: Double,
        val current: Double,
        val resistance: Double,
        val power: Double,
        val solvedFor: String
    )

    fun solveOhmsLaw(
        voltage: Double?,
        current: Double?,
        resistance: Double?,
        power: Double?
    ): OhmsLawResult {
        var v = voltage ?: 0.0
        var i = current ?: 0.0
        var r = resistance ?: 0.0
        var p = power ?: 0.0
        var solved = ""

        when {
            // Given I & R
            current != null && resistance != null && current > 0 && resistance > 0 -> {
                v = i * r
                p = i * i * r
                solved = "Voltage (V = I·R) and Power (P = I²·R)"
            }
            // Given V & I
            voltage != null && current != null && voltage > 0 && current > 0 -> {
                r = v / i
                p = v * i
                solved = "Resistance (R = V/I) and Power (P = V·I)"
            }
            // Given V & R
            voltage != null && resistance != null && voltage > 0 && resistance > 0 -> {
                i = v / r
                p = (v * v) / r
                solved = "Current (I = V/R) and Power (P = V²/R)"
            }
            // Given P & V
            power != null && voltage != null && power > 0 && voltage > 0 -> {
                i = p / v
                r = (v * v) / p
                solved = "Current (I = P/V) and Resistance (R = V²/P)"
            }
            // Given P & I
            power != null && current != null && power > 0 && current > 0 -> {
                v = p / i
                r = p / (i * i)
                solved = "Voltage (V = P/I) and Resistance (R = P/I²)"
            }
            // Given P & R
            power != null && resistance != null && power > 0 && resistance > 0 -> {
                i = kotlin.math.sqrt(p / r)
                v = i * r
                solved = "Current (I = √(P/R)) and Voltage (V = I·R)"
            }
            else -> {
                solved = "Please provide at least 2 non-zero parameters"
            }
        }

        return OhmsLawResult(v, i, r, p, solved)
    }

    // Kinematics & Work
    fun kineticEnergy(massKg: Double, velocityMps: Double): Double {
        return 0.5 * massKg * (velocityMps * velocityMps)
    }

    fun potentialEnergy(massKg: Double, heightM: Double): Double {
        return massKg * GRAVITY_G * heightM
    }

    fun force(massKg: Double, accelerationMps2: Double): Double {
        return massKg * accelerationMps2
    }

    fun work(forceN: Double, distanceM: Double): Double {
        return forceN * distanceM
    }

    // Chemistry Molar Mass & Dilution
    private val atomicWeights = mapOf(
        "H" to 1.008, "He" to 4.0026, "Li" to 6.94, "Be" to 9.0122,
        "B" to 10.81, "C" to 12.011, "N" to 14.007, "O" to 15.999,
        "F" to 18.998, "Ne" to 20.180, "Na" to 22.990, "Mg" to 24.305,
        "Al" to 26.982, "Si" to 28.085, "P" to 30.974, "S" to 32.06,
        "Cl" to 35.45, "K" to 39.098, "Ar" to 39.948, "Ca" to 40.078,
        "Fe" to 55.845, "Cu" to 63.546, "Zn" to 65.38, "Ag" to 107.87,
        "Au" to 196.97, "Hg" to 200.59, "Pb" to 207.2, "Br" to 79.904,
        "I" to 126.90
    )

    fun calculateMolarMass(formula: String): Pair<Double, Map<String, Double>> {
        val clean = formula.trim()
        val regex = Regex("([A-Z][a-z]?)(\\d*)")
        val matches = regex.findAll(clean)
        var totalMass = 0.0
        val elementCounts = mutableMapOf<String, Int>()

        for (match in matches) {
            val element = match.groupValues[1]
            val countStr = match.groupValues[2]
            val count = if (countStr.isEmpty()) 1 else countStr.toIntOrNull() ?: 1
            if (atomicWeights.containsKey(element)) {
                elementCounts[element] = (elementCounts[element] ?: 0) + count
                totalMass += (atomicWeights[element] ?: 0.0) * count
            }
        }

        val percentages = elementCounts.mapValues { (elem, count) ->
            val mass = (atomicWeights[elem] ?: 0.0) * count
            if (totalMass > 0) (mass / totalMass) * 100.0 else 0.0
        }

        return totalMass to percentages
    }

    // Dilution M1 * V1 = M2 * V2
    fun calculateDilution(m1: Double, v1: Double, m2: Double?, v2: Double?): Pair<String, Double> {
        return when {
            m2 == null && v2 != null && v2 > 0 -> "Target Concentration (M2)" to (m1 * v1 / v2)
            v2 == null && m2 != null && m2 > 0 -> "Target Volume (V2)" to (m1 * v1 / m2)
            else -> "Missing parameter" to 0.0
        }
    }

    // pH & pOH
    fun calculatePhFromHydrogen(hConcentration: Double): Pair<Double, Double> {
        if (hConcentration <= 0) return 7.0 to 7.0
        val ph = -log10(hConcentration)
        val poh = (14.0 - ph).coerceAtLeast(0.0)
        return ph to poh
    }
}
