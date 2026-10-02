package com.example.engine

import kotlin.math.*

data class StatisticsResult(
    val count: Int,
    val sum: Double,
    val mean: Double,
    val median: Double,
    val mode: List<Double>,
    val min: Double,
    val max: Double,
    val range: Double,
    val variance: Double,
    val standardDeviation: Double,
    val q1: Double,
    val q3: Double,
    val iqr: Double,
    val sortedData: List<Double>
)

data class QuadraticResult(
    val root1: String,
    val root2: String,
    val discriminant: Double,
    val natureOfRoots: String,
    val vertexX: Double,
    val vertexY: Double,
    val steps: List<String>
)

object StatisticsEquationEngine {

    fun computeStatistics(data: List<Double>): StatisticsResult? {
        if (data.isEmpty()) return null
        val sorted = data.sorted()
        val n = sorted.size
        val sum = sorted.sum()
        val mean = sum / n

        val median = if (n % 2 == 1) {
            sorted[n / 2]
        } else {
            (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
        }

        // Mode
        val freq = mutableMapOf<Double, Int>()
        for (x in sorted) freq[x] = (freq[x] ?: 0) + 1
        val maxFreq = freq.values.maxOrNull() ?: 1
        val mode = if (maxFreq > 1) freq.filter { it.value == maxFreq }.keys.toList() else emptyList()

        val min = sorted.first()
        val max = sorted.last()
        val range = max - min

        // Variance & StdDev
        val sqDiffSum = sorted.sumOf { (it - mean).pow(2) }
        val variance = if (n > 1) sqDiffSum / (n - 1) else 0.0
        val stdDev = sqrt(variance)

        // Quartiles
        val q1 = percentile(sorted, 25.0)
        val q3 = percentile(sorted, 75.0)
        val iqr = q3 - q1

        return StatisticsResult(
            count = n,
            sum = sum,
            mean = mean,
            median = median,
            mode = mode,
            min = min,
            max = max,
            range = range,
            variance = variance,
            standardDeviation = stdDev,
            q1 = q1,
            q3 = q3,
            iqr = iqr,
            sortedData = sorted
        )
    }

    private fun percentile(sorted: List<Double>, p: Double): Double {
        if (sorted.isEmpty()) return 0.0
        val index = (p / 100.0) * (sorted.size - 1)
        val lower = floor(index).toInt()
        val upper = ceil(index).toInt()
        if (lower == upper) return sorted[lower]
        val fraction = index - lower
        return sorted[lower] + fraction * (sorted[upper] - sorted[lower])
    }

    // Quadratic Equation Solver: ax² + bx + c = 0
    fun solveQuadratic(a: Double, b: Double, c: Double): QuadraticResult {
        if (a == 0.0) {
            // Linear degenerate: bx + c = 0
            val r = if (b != 0.0) -c / b else 0.0
            val formatted = MathEngine.formatResult(r, 4)
            return QuadraticResult(
                root1 = formatted,
                root2 = formatted,
                discriminant = 0.0,
                natureOfRoots = "Linear equation (a = 0)",
                vertexX = 0.0,
                vertexY = 0.0,
                steps = listOf(
                    "Since a = 0, equation is $b·x + $c = 0",
                    "x = -($c) / $b = $formatted"
                )
            )
        }

        val d = (b * b) - (4.0 * a * c)
        val vertexX = -b / (2.0 * a)
        val vertexY = c - (b * b) / (4.0 * a)

        val steps = mutableListOf<String>()
        steps.add("Step 1: Standard form ax² + bx + c = 0 with a = $a, b = $b, c = $c")
        steps.add("Step 2: Discriminant D = b² - 4ac = ($b)² - 4·($a)·($c) = ${MathEngine.formatResult(d, 4)}")

        return when {
            d > 1e-12 -> {
                val sqrtD = sqrt(d)
                val r1 = (-b + sqrtD) / (2.0 * a)
                val r2 = (-b - sqrtD) / (2.0 * a)
                steps.add("Step 3: Since D > 0, there are two distinct real roots:")
                steps.add("x₁ = (-b + √D) / (2a) = (${-b} + ${MathEngine.formatResult(sqrtD, 4)}) / ${2.0 * a} = ${MathEngine.formatResult(r1, 4)}")
                steps.add("x₂ = (-b - √D) / (2a) = (${-b} - ${MathEngine.formatResult(sqrtD, 4)}) / ${2.0 * a} = ${MathEngine.formatResult(r2, 4)}")
                QuadraticResult(
                    root1 = MathEngine.formatResult(r1, 4),
                    root2 = MathEngine.formatResult(r2, 4),
                    discriminant = d,
                    natureOfRoots = "Two distinct real roots",
                    vertexX = vertexX,
                    vertexY = vertexY,
                    steps = steps
                )
            }
            abs(d) <= 1e-12 -> {
                val r = -b / (2.0 * a)
                steps.add("Step 3: Since D = 0, there is exactly one repeated real root:")
                steps.add("x = -b / (2a) = ${MathEngine.formatResult(r, 4)}")
                QuadraticResult(
                    root1 = MathEngine.formatResult(r, 4),
                    root2 = MathEngine.formatResult(r, 4),
                    discriminant = 0.0,
                    natureOfRoots = "One repeated real root",
                    vertexX = vertexX,
                    vertexY = vertexY,
                    steps = steps
                )
            }
            else -> {
                val realPart = -b / (2.0 * a)
                val imagPart = sqrt(-d) / (2.0 * abs(a))
                val realStr = MathEngine.formatResult(realPart, 4)
                val imagStr = MathEngine.formatResult(imagPart, 4)
                steps.add("Step 3: Since D < 0, roots are complex conjugates:")
                steps.add("x = $realStr ± ${imagStr}i")
                QuadraticResult(
                    root1 = "$realStr + ${imagStr}i",
                    root2 = "$realStr - ${imagStr}i",
                    discriminant = d,
                    natureOfRoots = "Two complex conjugate roots",
                    vertexX = vertexX,
                    vertexY = vertexY,
                    steps = steps
                )
            }
        }
    }

    // 2-Variable Linear System:
    // a1*x + b1*y = c1
    // a2*x + b2*y = c2
    fun solve2x2System(a1: Double, b1: Double, c1: Double, a2: Double, b2: Double, c2: Double): Pair<String, String>? {
        val det = (a1 * b2) - (a2 * b1)
        if (abs(det) < 1e-12) return null // Parallel or infinite
        val x = ((c1 * b2) - (c2 * b1)) / det
        val y = ((a1 * c2) - (a2 * c1)) / det
        return MathEngine.formatResult(x, 4) to MathEngine.formatResult(y, 4)
    }

    // 2x2 Matrix Determinant & Inverse
    fun matrix2x2Det(a: Double, b: Double, c: Double, d: Double): Double {
        return (a * d) - (b * c)
    }

    fun matrix2x2Inverse(a: Double, b: Double, c: Double, d: Double): List<Double>? {
        val det = matrix2x2Det(a, b, c, d)
        if (abs(det) < 1e-12) return null
        return listOf(d / det, -b / det, -c / det, a / det)
    }
}
