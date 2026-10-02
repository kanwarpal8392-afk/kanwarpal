package com.example.engine

import kotlin.math.*

data class GeometryCalculationResult(
    val title: String,
    val primaryValueLabel: String,
    val primaryValue: Double,
    val secondaryValueLabel: String,
    val secondaryValue: Double,
    val additionalMetrics: List<Pair<String, Double>>,
    val formulaUsed: String,
    val shapeType: String
)

object GeometryEngine {

    fun circle(radius: Double): GeometryCalculationResult {
        val area = PI * radius * radius
        val circumference = 2.0 * PI * radius
        val diameter = 2.0 * radius
        return GeometryCalculationResult(
            title = "Circle",
            primaryValueLabel = "Area (A)",
            primaryValue = area,
            secondaryValueLabel = "Circumference (C)",
            secondaryValue = circumference,
            additionalMetrics = listOf("Diameter (d)" to diameter),
            formulaUsed = "A = π·r², C = 2·π·r",
            shapeType = "2D"
        )
    }

    fun rectangle(length: Double, width: Double): GeometryCalculationResult {
        val area = length * width
        val perimeter = 2.0 * (length + width)
        val diagonal = sqrt(length * length + width * width)
        return GeometryCalculationResult(
            title = "Rectangle",
            primaryValueLabel = "Area (A)",
            primaryValue = area,
            secondaryValueLabel = "Perimeter (P)",
            secondaryValue = perimeter,
            additionalMetrics = listOf("Diagonal (d)" to diagonal),
            formulaUsed = "A = l·w, P = 2·(l + w), d = √(l² + w²)",
            shapeType = "2D"
        )
    }

    fun triangle(base: Double, height: Double, sideA: Double = 0.0, sideC: Double = 0.0): GeometryCalculationResult {
        val area = 0.5 * base * height
        val effectiveSideA = if (sideA > 0) sideA else sqrt((base / 2.0).pow(2) + height.pow(2))
        val effectiveSideC = if (sideC > 0) sideC else effectiveSideA
        val perimeter = base + effectiveSideA + effectiveSideC
        return GeometryCalculationResult(
            title = "Triangle",
            primaryValueLabel = "Area (A)",
            primaryValue = area,
            secondaryValueLabel = "Perimeter (P)",
            secondaryValue = perimeter,
            additionalMetrics = listOf("Base" to base, "Height" to height),
            formulaUsed = "A = ½·b·h, P = a + b + c",
            shapeType = "2D"
        )
    }

    fun trapezium(baseA: Double, baseB: Double, height: Double): GeometryCalculationResult {
        val area = 0.5 * (baseA + baseB) * height
        val slant = sqrt(height * height + ((baseA - baseB) / 2.0).pow(2))
        val perimeter = baseA + baseB + (2.0 * slant)
        return GeometryCalculationResult(
            title = "Trapezium",
            primaryValueLabel = "Area (A)",
            primaryValue = area,
            secondaryValueLabel = "Perimeter (P)",
            secondaryValue = perimeter,
            additionalMetrics = listOf("Slant Leg" to slant),
            formulaUsed = "A = ½·(a + b)·h",
            shapeType = "2D"
        )
    }

    fun sphere(radius: Double): GeometryCalculationResult {
        val volume = (4.0 / 3.0) * PI * radius.pow(3)
        val surfaceArea = 4.0 * PI * radius.pow(2)
        val diameter = 2.0 * radius
        return GeometryCalculationResult(
            title = "Sphere",
            primaryValueLabel = "Volume (V)",
            primaryValue = volume,
            secondaryValueLabel = "Surface Area (A)",
            secondaryValue = surfaceArea,
            additionalMetrics = listOf("Diameter" to diameter),
            formulaUsed = "V = ⁴⁄₃·π·r³, A = 4·π·r²",
            shapeType = "3D"
        )
    }

    fun cylinder(radius: Double, height: Double): GeometryCalculationResult {
        val volume = PI * radius.pow(2) * height
        val lateralArea = 2.0 * PI * radius * height
        val totalArea = lateralArea + (2.0 * PI * radius.pow(2))
        return GeometryCalculationResult(
            title = "Cylinder",
            primaryValueLabel = "Volume (V)",
            primaryValue = volume,
            secondaryValueLabel = "Total Surface Area (A)",
            secondaryValue = totalArea,
            additionalMetrics = listOf("Curved Lateral Area" to lateralArea),
            formulaUsed = "V = π·r²·h, A = 2·π·r·h + 2·π·r²",
            shapeType = "3D"
        )
    }

    fun cone(radius: Double, height: Double): GeometryCalculationResult {
        val slantHeight = sqrt(radius.pow(2) + height.pow(2))
        val volume = (1.0 / 3.0) * PI * radius.pow(2) * height
        val lateralArea = PI * radius * slantHeight
        val totalArea = lateralArea + (PI * radius.pow(2))
        return GeometryCalculationResult(
            title = "Cone",
            primaryValueLabel = "Volume (V)",
            primaryValue = volume,
            secondaryValueLabel = "Total Surface Area (A)",
            secondaryValue = totalArea,
            additionalMetrics = listOf("Slant Height (l)" to slantHeight, "Curved Area" to lateralArea),
            formulaUsed = "V = ⅓·π·r²·h, l = √(r² + h²)",
            shapeType = "3D"
        )
    }

    fun cube(side: Double): GeometryCalculationResult {
        val volume = side.pow(3)
        val surfaceArea = 6.0 * side.pow(2)
        val spaceDiagonal = side * sqrt(3.0)
        return GeometryCalculationResult(
            title = "Cube",
            primaryValueLabel = "Volume (V)",
            primaryValue = volume,
            secondaryValueLabel = "Total Surface Area (A)",
            secondaryValue = surfaceArea,
            additionalMetrics = listOf("Space Diagonal" to spaceDiagonal),
            formulaUsed = "V = s³, A = 6·s², d = s·√3",
            shapeType = "3D"
        )
    }
}
