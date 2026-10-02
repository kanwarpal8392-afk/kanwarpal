package com.example.engine

data class UnitItem(
    val id: String,
    val name: String,
    val symbol: String,
    val toBaseFactor: Double = 1.0 // multiply to get base unit
)

data class UnitCategory(
    val id: String,
    val name: String,
    val iconName: String,
    val baseUnitSymbol: String,
    val units: List<UnitItem>
)

object UnitConversionEngine {

    val categories: List<UnitCategory> = listOf(
        UnitCategory(
            id = "length",
            name = "Length",
            iconName = "straighten",
            baseUnitSymbol = "m",
            units = listOf(
                UnitItem("mm", "Millimeter", "mm", 0.001),
                UnitItem("cm", "Centimeter", "cm", 0.01),
                UnitItem("m", "Meter", "m", 1.0),
                UnitItem("km", "Kilometer", "km", 1000.0),
                UnitItem("in", "Inch", "in", 0.0254),
                UnitItem("ft", "Foot", "ft", 0.3048),
                UnitItem("yd", "Yard", "yd", 0.9144),
                UnitItem("mi", "Mile", "mi", 1609.344),
                UnitItem("nmi", "Nautical Mile", "NM", 1852.0)
            )
        ),
        UnitCategory(
            id = "mass",
            name = "Mass & Weight",
            iconName = "fitness_center",
            baseUnitSymbol = "kg",
            units = listOf(
                UnitItem("mg", "Milligram", "mg", 1e-6),
                UnitItem("g", "Gram", "g", 0.001),
                UnitItem("kg", "Kilogram", "kg", 1.0),
                UnitItem("t", "Metric Ton", "t", 1000.0),
                UnitItem("oz", "Ounce", "oz", 0.02834952),
                UnitItem("lb", "Pound", "lb", 0.45359237),
                UnitItem("st", "Stone", "st", 6.350293),
                UnitItem("uston", "US Short Ton", "US ton", 907.18474)
            )
        ),
        UnitCategory(
            id = "area",
            name = "Area",
            iconName = "crop_square",
            baseUnitSymbol = "m²",
            units = listOf(
                UnitItem("mm2", "Square Millimeter", "mm²", 1e-6),
                UnitItem("cm2", "Square Centimeter", "cm²", 1e-4),
                UnitItem("m2", "Square Meter", "m²", 1.0),
                UnitItem("km2", "Square Kilometer", "km²", 1e6),
                UnitItem("sqin", "Square Inch", "in²", 0.00064516),
                UnitItem("sqft", "Square Foot", "ft²", 0.09290304),
                UnitItem("sqyd", "Square Yard", "yd²", 0.83612736),
                UnitItem("acre", "Acre", "acre", 4046.8564224),
                UnitItem("ha", "Hectare", "ha", 10000.0)
            )
        ),
        UnitCategory(
            id = "volume",
            name = "Volume",
            iconName = "water_drop",
            baseUnitSymbol = "L",
            units = listOf(
                UnitItem("ml", "Milliliter", "mL", 0.001),
                UnitItem("l", "Liter", "L", 1.0),
                UnitItem("m3", "Cubic Meter", "m³", 1000.0),
                UnitItem("cm3", "Cubic Centimeter", "cm³", 0.001),
                UnitItem("floz", "Fluid Ounce (US)", "fl oz", 0.0295735),
                UnitItem("cup", "Cup (US)", "cup", 0.236588),
                UnitItem("pt", "Pint (US)", "pt", 0.473176),
                UnitItem("qt", "Quart (US)", "qt", 0.946353),
                UnitItem("gal", "Gallon (US)", "gal", 3.78541)
            )
        ),
        UnitCategory(
            id = "temperature",
            name = "Temperature",
            iconName = "thermostat",
            baseUnitSymbol = "°C",
            units = listOf(
                UnitItem("c", "Celsius", "°C", 1.0),
                UnitItem("f", "Fahrenheit", "°F", 1.0),
                UnitItem("k", "Kelvin", "K", 1.0),
                UnitItem("r", "Rankine", "°R", 1.0)
            )
        ),
        UnitCategory(
            id = "speed",
            name = "Speed",
            iconName = "speed",
            baseUnitSymbol = "m/s",
            units = listOf(
                UnitItem("mps", "Meters per second", "m/s", 1.0),
                UnitItem("kmh", "Kilometers per hour", "km/h", 0.277778),
                UnitItem("mph", "Miles per hour", "mph", 0.44704),
                UnitItem("knot", "Knot", "kn", 0.514444),
                UnitItem("fps", "Feet per second", "ft/s", 0.3048)
            )
        ),
        UnitCategory(
            id = "data",
            name = "Digital Storage",
            iconName = "dns",
            baseUnitSymbol = "MB",
            units = listOf(
                UnitItem("b", "Bit", "b", 1.0 / (8 * 1024 * 1024)),
                UnitItem("B", "Byte", "B", 1.0 / (1024 * 1024)),
                UnitItem("KB", "Kilobyte", "KB", 1.0 / 1024),
                UnitItem("MB", "Megabyte", "MB", 1.0),
                UnitItem("GB", "Gigabyte", "GB", 1024.0),
                UnitItem("TB", "Terabyte", "TB", 1024.0 * 1024.0),
                UnitItem("PB", "Petabyte", "PB", 1024.0 * 1024.0 * 1024.0)
            )
        ),
        UnitCategory(
            id = "time",
            name = "Time",
            iconName = "schedule",
            baseUnitSymbol = "s",
            units = listOf(
                UnitItem("ms", "Millisecond", "ms", 0.001),
                UnitItem("s", "Second", "s", 1.0),
                UnitItem("min", "Minute", "min", 60.0),
                UnitItem("h", "Hour", "h", 3600.0),
                UnitItem("d", "Day", "d", 86400.0),
                UnitItem("wk", "Week", "wk", 604800.0),
                UnitItem("mo", "Month (Avg)", "mo", 2629800.0),
                UnitItem("yr", "Year (365.25d)", "yr", 31557600.0)
            )
        ),
        UnitCategory(
            id = "pressure",
            name = "Pressure",
            iconName = "compress",
            baseUnitSymbol = "kPa",
            units = listOf(
                UnitItem("pa", "Pascal", "Pa", 0.001),
                UnitItem("kpa", "Kilopascal", "kPa", 1.0),
                UnitItem("bar", "Bar", "bar", 100.0),
                UnitItem("psi", "Pound/sq inch", "psi", 6.89476),
                UnitItem("atm", "Atmosphere", "atm", 101.325),
                UnitItem("mmhg", "Millimeter of mercury", "mmHg", 0.133322),
                UnitItem("torr", "Torr", "Torr", 0.133322)
            )
        ),
        UnitCategory(
            id = "energy",
            name = "Energy",
            iconName = "bolt",
            baseUnitSymbol = "J",
            units = listOf(
                UnitItem("j", "Joule", "J", 1.0),
                UnitItem("kj", "Kilojoule", "kJ", 1000.0),
                UnitItem("cal", "Calorie", "cal", 4.184),
                UnitItem("kcal", "Kilocalorie", "kcal", 4184.0),
                UnitItem("wh", "Watt-hour", "Wh", 3600.0),
                UnitItem("kwh", "Kilowatt-hour", "kWh", 3.6e6),
                UnitItem("btu", "British Thermal Unit", "BTU", 1055.06),
                UnitItem("ev", "Electronvolt", "eV", 1.602176634e-19)
            )
        ),
        UnitCategory(
            id = "power",
            name = "Power",
            iconName = "power",
            baseUnitSymbol = "W",
            units = listOf(
                UnitItem("w", "Watt", "W", 1.0),
                UnitItem("kw", "Kilowatt", "kW", 1000.0),
                UnitItem("mw", "Megawatt", "MW", 1e6),
                UnitItem("hp", "Horsepower (mechanical)", "hp", 745.69987)
            )
        ),
        UnitCategory(
            id = "force",
            name = "Force",
            iconName = "navigation",
            baseUnitSymbol = "N",
            units = listOf(
                UnitItem("n", "Newton", "N", 1.0),
                UnitItem("kn", "Kilonewton", "kN", 1000.0),
                UnitItem("dyn", "Dyne", "dyn", 1e-5),
                UnitItem("lbf", "Pound-force", "lbf", 4.44822)
            )
        )
    )

    fun convert(value: Double, categoryId: String, fromUnitId: String, toUnitId: String): Double {
        if (categoryId == "temperature") {
            return convertTemperature(value, fromUnitId, toUnitId)
        }
        val category = categories.find { it.id == categoryId } ?: return value
        val fromUnit = category.units.find { it.id == fromUnitId } ?: return value
        val toUnit = category.units.find { it.id == toUnitId } ?: return value

        val baseValue = value * fromUnit.toBaseFactor
        return baseValue / toUnit.toBaseFactor
    }

    private fun convertTemperature(value: Double, from: String, to: String): Double {
        // Convert to Celsius first
        val celsius = when (from.lowercase()) {
            "c" -> value
            "f" -> (value - 32.0) * 5.0 / 9.0
            "k" -> value - 273.15
            "r" -> (value - 491.67) * 5.0 / 9.0
            else -> value
        }
        // Convert from Celsius to target
        return when (to.lowercase()) {
            "c" -> celsius
            "f" -> (celsius * 9.0 / 5.0) + 32.0
            "k" -> celsius + 273.15
            "r" -> (celsius + 273.15) * 9.0 / 5.0
            else -> celsius
        }
    }

    fun getFormulaDescription(categoryId: String, fromUnitId: String, toUnitId: String): String {
        if (categoryId == "temperature") {
            return when {
                fromUnitId == "c" && toUnitId == "f" -> "°F = (°C × 9/5) + 32"
                fromUnitId == "f" && toUnitId == "c" -> "°C = (°F − 32) × 5/9"
                fromUnitId == "c" && toUnitId == "k" -> "K = °C + 273.15"
                fromUnitId == "k" && toUnitId == "c" -> "°C = K − 273.15"
                else -> "Standard Thermodynamic Conversion"
            }
        }
        val category = categories.find { it.id == categoryId } ?: return ""
        val from = category.units.find { it.id == fromUnitId } ?: return ""
        val to = category.units.find { it.id == toUnitId } ?: return ""
        val ratio = from.toBaseFactor / to.toBaseFactor
        return "1 ${from.symbol} = ${MathEngine.formatResult(ratio, 6)} ${to.symbol}"
    }

    // Currency Conversion (Reference standard rates based on 1 USD)
    val referenceExchangeRates = mapOf(
        "USD" to 1.0,
        "INR" to 86.85,
        "EUR" to 0.92,
        "GBP" to 0.79,
        "JPY" to 153.20,
        "AUD" to 1.55,
        "CAD" to 1.40,
        "AED" to 3.67,
        "SGD" to 1.34,
        "CHF" to 0.88
    )

    fun convertCurrency(amount: Double, from: String, to: String): Double {
        val rateFrom = referenceExchangeRates[from] ?: 1.0
        val rateTo = referenceExchangeRates[to] ?: 1.0
        val inUsd = amount / rateFrom
        return inUsd * rateTo
    }

    // Download Time Estimator
    fun calculateDownloadTime(fileSizeMb: Double, speedMbps: Double): Pair<Double, String> {
        if (speedMbps <= 0) return 0.0 to "Invalid speed"
        val totalBits = fileSizeMb * 8.0 // MB to Mb
        val seconds = totalBits / speedMbps
        val durationStr = when {
            seconds < 60 -> "${String.format(java.util.Locale.US, "%.1f", seconds)} seconds"
            seconds < 3600 -> {
                val mins = (seconds / 60).toInt()
                val secs = (seconds % 60).toInt()
                "${mins}m ${secs}s"
            }
            else -> {
                val hrs = (seconds / 3600).toInt()
                val mins = ((seconds % 3600) / 60).toInt()
                "${hrs}h ${mins}m"
            }
        }
        return seconds to durationStr
    }
}
