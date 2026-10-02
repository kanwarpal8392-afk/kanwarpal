package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class HistoryRepository private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("calcora_prefs", Context.MODE_PRIVATE)

    private val _history = MutableStateFlow<List<CalculationHistoryItem>>(emptyList())
    val history: StateFlow<List<CalculationHistoryItem>> = _history.asStateFlow()

    private val _customCalculators = MutableStateFlow<List<CustomCalculator>>(emptyList())
    val customCalculators: StateFlow<List<CustomCalculator>> = _customCalculators.asStateFlow()

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    init {
        loadHistory()
        loadCustomCalculators()
    }

    private fun loadPreferences(): UserPreferences {
        return UserPreferences(
            decimalPrecision = prefs.getInt("pref_precision", 4),
            angleMode = prefs.getString("pref_angle_mode", "DEG") ?: "DEG",
            defaultCurrency = prefs.getString("pref_currency", "INR") ?: "INR",
            soundEnabled = prefs.getBoolean("pref_sound", true),
            hapticEnabled = prefs.getBoolean("pref_haptic", true),
            darkMode = prefs.getString("pref_dark_mode", "system") ?: "system"
        )
    }

    fun updatePreferences(newPrefs: UserPreferences) {
        prefs.edit()
            .putInt("pref_precision", newPrefs.decimalPrecision)
            .putString("pref_angle_mode", newPrefs.angleMode)
            .putString("pref_currency", newPrefs.defaultCurrency)
            .putBoolean("pref_sound", newPrefs.soundEnabled)
            .putBoolean("pref_haptic", newPrefs.hapticEnabled)
            .putString("pref_dark_mode", newPrefs.darkMode)
            .apply()
        _preferences.value = newPrefs
    }

    private fun loadHistory() {
        val raw = prefs.getString("calc_history", "[]") ?: "[]"
        try {
            val array = JSONArray(raw)
            val list = mutableListOf<CalculationHistoryItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CalculationHistoryItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.optString("title", "Calculation"),
                        expression = obj.optString("expression", ""),
                        result = obj.optString("result", ""),
                        category = obj.optString("category", "General"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        details = obj.optString("details", "")
                    )
                )
            }
            _history.value = list
        } catch (_: Exception) {
            _history.value = emptyList()
        }
    }

    fun addHistory(item: CalculationHistoryItem) {
        val current = _history.value.toMutableList()
        // Prevent exact duplicate consecutive entries
        if (current.isNotEmpty() && current.first().expression == item.expression && current.first().result == item.result) {
            return
        }
        current.add(0, item)
        if (current.size > 200) {
            current.removeAt(current.size - 1)
        }
        _history.value = current
        saveHistory(current)
    }

    fun toggleFavorite(id: String) {
        val updated = _history.value.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
        _history.value = updated
        saveHistory(updated)
    }

    fun deleteHistoryItem(id: String) {
        val updated = _history.value.filter { it.id != id }
        _history.value = updated
        saveHistory(updated)
    }

    fun clearHistory() {
        _history.value = emptyList()
        prefs.edit().remove("calc_history").apply()
    }

    private fun saveHistory(list: List<CalculationHistoryItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("expression", item.expression)
            obj.put("result", item.result)
            obj.put("category", item.category)
            obj.put("timestamp", item.timestamp)
            obj.put("isFavorite", item.isFavorite)
            obj.put("details", item.details)
            array.put(obj)
        }
        prefs.edit().putString("calc_history", array.toString()).apply()
    }

    private fun loadCustomCalculators() {
        val raw = prefs.getString("custom_calcs", null)
        if (raw == null) {
            // Seed a starter custom calculator
            val defaultList = listOf(
                CustomCalculator(
                    name = "Fuel Cost Trip",
                    formulaDisplay = "Distance / Mileage × Fuel Price",
                    var1Name = "Distance (km)",
                    var1Unit = "km",
                    var2Name = "Fuel Price (₹/L)",
                    var2Unit = "₹/L",
                    operation = "multiply",
                    outputName = "Total Fuel Expense",
                    outputUnit = "₹"
                )
            )
            _customCalculators.value = defaultList
            saveCustomCalculators(defaultList)
        } else {
            try {
                val array = JSONArray(raw)
                val list = mutableListOf<CustomCalculator>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CustomCalculator(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            name = obj.optString("name", "Custom"),
                            formulaDisplay = obj.optString("formulaDisplay", ""),
                            var1Name = obj.optString("var1Name", "Var 1"),
                            var1Unit = obj.optString("var1Unit", ""),
                            var2Name = obj.optString("var2Name", "Var 2"),
                            var2Unit = obj.optString("var2Unit", ""),
                            operation = obj.optString("operation", "multiply"),
                            outputName = obj.optString("outputName", "Result"),
                            outputUnit = obj.optString("outputUnit", "")
                        )
                    )
                }
                _customCalculators.value = list
            } catch (_: Exception) {
                _customCalculators.value = emptyList()
            }
        }
    }

    fun addCustomCalculator(calculator: CustomCalculator) {
        val list = _customCalculators.value.toMutableList()
        list.add(0, calculator)
        _customCalculators.value = list
        saveCustomCalculators(list)
    }

    fun deleteCustomCalculator(id: String) {
        val list = _customCalculators.value.filter { it.id != id }
        _customCalculators.value = list
        saveCustomCalculators(list)
    }

    private fun saveCustomCalculators(list: List<CustomCalculator>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("formulaDisplay", item.formulaDisplay)
            obj.put("var1Name", item.var1Name)
            obj.put("var1Unit", item.var1Unit)
            obj.put("var2Name", item.var2Name)
            obj.put("var2Unit", item.var2Unit)
            obj.put("operation", item.operation)
            obj.put("outputName", item.outputName)
            obj.put("outputUnit", item.outputUnit)
            array.put(obj)
        }
        prefs.edit().putString("custom_calcs", array.toString()).apply()
    }

    companion object {
        @Volatile
        private var instance: HistoryRepository? = null

        fun getInstance(context: Context): HistoryRepository {
            return instance ?: synchronized(this) {
                instance ?: HistoryRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
