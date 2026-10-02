package com.example.data

data class CalculationHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val expression: String,
    val result: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    var isFavorite: Boolean = false,
    val details: String = ""
)

data class CustomCalculator(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val formulaDisplay: String, // e.g., "Distance * Rate"
    val var1Name: String,
    val var1Unit: String,
    val var2Name: String,
    val var2Unit: String,
    val operation: String, // "multiply", "divide", "add", "subtract"
    val outputName: String,
    val outputUnit: String
)

data class UserPreferences(
    val decimalPrecision: Int = 4, // 2, 3, 4, 6, -1 for Auto
    val angleMode: String = "DEG", // "DEG", "RAD", "GRAD"
    val defaultCurrency: String = "INR", // INR ₹ default
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val darkMode: String = "system" // "system", "dark", "light"
)
