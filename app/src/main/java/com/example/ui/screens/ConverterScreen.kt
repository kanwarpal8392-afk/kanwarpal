package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalculationHistoryItem
import com.example.data.HistoryRepository
import com.example.engine.MathEngine
import com.example.engine.UnitConversionEngine
import com.example.ui.components.ResultCard
import com.example.ui.components.triggerHapticFeedback
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    repository: HistoryRepository,
    initialCategoryId: String = "length"
) {
    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }
    val currentCategory = UnitConversionEngine.categories.find { it.id == selectedCategoryId }
        ?: UnitConversionEngine.categories.first()

    var fromUnitId by remember { mutableStateOf(currentCategory.units.first().id) }
    var toUnitId by remember {
        mutableStateOf(
            if (currentCategory.units.size > 1) currentCategory.units[1].id else currentCategory.units.first().id
        )
    }

    var inputValueStr by remember { mutableStateOf("1") }
    var isCurrencyMode by remember { mutableStateOf(false) }

    // Currency mode states
    var currencyFrom by remember { mutableStateOf("USD") }
    var currencyTo by remember { mutableStateOf("INR") }
    var currencyAmountStr by remember { mutableStateOf("100") }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Sync unit selections when category changes
    LaunchedEffect(selectedCategoryId) {
        val cat = UnitConversionEngine.categories.find { it.id == selectedCategoryId }
        if (cat != null) {
            fromUnitId = cat.units.first().id
            toUnitId = if (cat.units.size > 1) cat.units[1].id else cat.units.first().id
        }
    }

    val inputValue = inputValueStr.toDoubleOrNull() ?: 0.0
    val convertedValue = UnitConversionEngine.convert(inputValue, selectedCategoryId, fromUnitId, toUnitId)
    val convertedFormatted = MathEngine.formatResult(convertedValue, 6)

    val formulaDesc = UnitConversionEngine.getFormulaDescription(selectedCategoryId, fromUnitId, toUnitId)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("converter_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Switcher: Units vs Currency
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isCurrencyMode,
                    onClick = { isCurrencyMode = false },
                    label = { Text("Standard Units") },
                    leadingIcon = { Icon(Icons.Filled.Straighten, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                FilterChip(
                    selected = isCurrencyMode,
                    onClick = { isCurrencyMode = true },
                    label = { Text("Currency (Forex)") },
                    leadingIcon = { Icon(Icons.Filled.CurrencyRupee, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        if (isCurrencyMode) {
            // Currency Converter Card
            item {
                val amt = currencyAmountStr.toDoubleOrNull() ?: 0.0
                val convertedCurrency = UnitConversionEngine.convertCurrency(amt, currencyFrom, currencyTo)
                val currFormatted = MathEngine.formatResult(convertedCurrency, 2)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Reference Currency Converter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Default region: India (INR ₹). Reference standard forex rates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = currencyAmountStr,
                            onValueChange = { currencyAmountStr = it },
                            label = { Text("Amount ($currencyFrom)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Currency From Selector
                            var expandedFrom by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(onClick = { expandedFrom = true }, shape = RoundedCornerShape(12.dp)) {
                                    Text(currencyFrom, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(expanded = expandedFrom, onDismissRequest = { expandedFrom = false }) {
                                    UnitConversionEngine.referenceExchangeRates.keys.forEach { curr ->
                                        DropdownMenuItem(
                                            text = { Text(curr) },
                                            onClick = {
                                                currencyFrom = curr
                                                expandedFrom = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Swap
                            IconButton(onClick = {
                                val tmp = currencyFrom
                                currencyFrom = currencyTo
                                currencyTo = tmp
                            }) {
                                Icon(Icons.Filled.SwapHoriz, contentDescription = "Swap", tint = MaterialTheme.colorScheme.primary)
                            }

                            // Currency To Selector
                            var expandedTo by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(onClick = { expandedTo = true }, shape = RoundedCornerShape(12.dp)) {
                                    Text(currencyTo, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(expanded = expandedTo, onDismissRequest = { expandedTo = false }) {
                                    UnitConversionEngine.referenceExchangeRates.keys.forEach { curr ->
                                        DropdownMenuItem(
                                            text = { Text(curr) },
                                            onClick = {
                                                currencyTo = curr
                                                expandedTo = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ResultCard(
                            title = "Converted Value",
                            primaryResult = "$currFormatted $currencyTo",
                            formula = "1 $currencyFrom ≈ ${MathEngine.formatResult(UnitConversionEngine.convertCurrency(1.0, currencyFrom, currencyTo), 4)} $currencyTo",
                            notes = "Reference values for estimation. For live commercial transactions, consult banking institutions."
                        )
                    }
                }
            }
        } else {
            // Category Horizontal Scrollable Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UnitConversionEngine.categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = {
                                selectedCategoryId = cat.id
                                triggerHapticFeedback(context)
                            },
                            label = { Text(cat.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Input Value & From/To Conversion Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = inputValueStr,
                            onValueChange = { inputValueStr = it },
                            label = { Text("Input Value") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // From Unit
                            var expFrom by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { expFrom = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val unitObj = currentCategory.units.find { it.id == fromUnitId }
                                    Text(unitObj?.symbol ?: fromUnitId, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                DropdownMenu(expanded = expFrom, onDismissRequest = { expFrom = false }) {
                                    currentCategory.units.forEach { u ->
                                        DropdownMenuItem(
                                            text = { Text("${u.name} (${u.symbol})") },
                                            onClick = {
                                                fromUnitId = u.id
                                                expFrom = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Swap Button
                            IconButton(
                                onClick = {
                                    val tmp = fromUnitId
                                    fromUnitId = toUnitId
                                    toUnitId = tmp
                                    triggerHapticFeedback(context)
                                },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SwapHoriz,
                                    contentDescription = "Swap",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // To Unit
                            var expTo by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { expTo = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val unitObj = currentCategory.units.find { it.id == toUnitId }
                                    Text(unitObj?.symbol ?: toUnitId, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                DropdownMenu(expanded = expTo, onDismissRequest = { expTo = false }) {
                                    currentCategory.units.forEach { u ->
                                        DropdownMenuItem(
                                            text = { Text("${u.name} (${u.symbol})") },
                                            onClick = {
                                                toUnitId = u.id
                                                expTo = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val fromUnitObj = currentCategory.units.find { it.id == fromUnitId }
                        val toUnitObj = currentCategory.units.find { it.id == toUnitId }

                        ResultCard(
                            title = "Converted Result",
                            primaryResult = "$convertedFormatted ${toUnitObj?.symbol ?: ""}",
                            formula = formulaDesc,
                            breakdownItems = listOf(
                                "From" to "$inputValue ${fromUnitObj?.name ?: ""}",
                                "To" to "$convertedFormatted ${toUnitObj?.name ?: ""}"
                            ),
                            onSaveToHistory = {
                                repository.addHistory(
                                    CalculationHistoryItem(
                                        title = "Unit Conversion: ${currentCategory.name}",
                                        expression = "$inputValue ${fromUnitObj?.symbol} to ${toUnitObj?.symbol}",
                                        result = "$convertedFormatted ${toUnitObj?.symbol}",
                                        category = "Conversion"
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // Live Simultaneous Conversion to All Units in this Category
            item {
                Text(
                    text = "Equivalent Values in All ${currentCategory.name} Units",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(currentCategory.units) { unitItem ->
                val eqVal = UnitConversionEngine.convert(inputValue, selectedCategoryId, fromUnitId, unitItem.id)
                val eqFormatted = MathEngine.formatResult(eqVal, 4)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            clipboardManager.setText(AnnotatedString("$eqFormatted ${unitItem.symbol}"))
                            triggerHapticFeedback(context)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = unitItem.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = unitItem.symbol,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = eqFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Download Time Special Tool (if category == data)
            if (selectedCategoryId == "data") {
                item {
                    var fileSizeGb by remember { mutableStateOf("2.0") }
                    var netSpeedMbps by remember { mutableStateOf("50") }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Download Time Estimator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = fileSizeGb,
                                    onValueChange = { fileSizeGb = it },
                                    label = { Text("File Size (GB)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                OutlinedTextField(
                                    value = netSpeedMbps,
                                    onValueChange = { netSpeedMbps = it },
                                    label = { Text("Speed (Mbps)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            val fs = fileSizeGb.toDoubleOrNull() ?: 2.0
                            val sp = netSpeedMbps.toDoubleOrNull() ?: 50.0
                            val dl = UnitConversionEngine.calculateDownloadTime(fs * 1024.0, sp)
                            ResultCard(
                                title = "Estimated Download Duration",
                                primaryResult = dl.second,
                                formula = "Time = (File Size in bits) / (Speed in bits/sec)"
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
