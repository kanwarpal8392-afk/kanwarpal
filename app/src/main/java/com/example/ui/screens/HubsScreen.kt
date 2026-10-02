package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiAiService
import com.example.data.CalculationHistoryItem
import com.example.data.CustomCalculator
import com.example.data.HistoryRepository
import com.example.engine.*
import kotlin.math.min
import com.example.ui.components.DisclaimerCard
import com.example.ui.components.ResultCard
import com.example.ui.components.StepByStepCard
import com.example.ui.components.triggerHapticFeedback
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class HubCategory(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    FINANCE("Finance", Icons.Filled.AccountBalance),
    HEALTH("Health", Icons.Filled.Favorite),
    GEOMETRY("Geometry", Icons.Filled.SquareFoot),
    PHYSICS_CHEM("Physics & Chem", Icons.Filled.Science),
    CONSTRUCTION("Construction", Icons.Filled.Handyman),
    STATS_MATH("Stats & Solver", Icons.Filled.Equalizer),
    AI_SOLVER("AI Tutor & Solver", Icons.Filled.AutoAwesome),
    CUSTOM("Custom Builder", Icons.Filled.Build)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubsScreen(
    repository: HistoryRepository,
    initialTab: HubCategory = HubCategory.FINANCE,
    initialAiQuery: String = ""
) {
    var selectedCategory by remember { mutableStateOf(initialTab) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("hubs_screen")
    ) {
        // Hub Category Tabs Header
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HubCategory.values()) { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    leadingIcon = {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Active Hub Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedCategory) {
                HubCategory.FINANCE -> FinanceHubContent(repository)
                HubCategory.HEALTH -> HealthHubContent(repository)
                HubCategory.GEOMETRY -> GeometryHubContent(repository)
                HubCategory.PHYSICS_CHEM -> PhysicsChemHubContent(repository)
                HubCategory.CONSTRUCTION -> ConstructionHubContent(repository)
                HubCategory.STATS_MATH -> StatsMathHubContent(repository)
                HubCategory.AI_SOLVER -> AiSolverHubContent(repository, initialAiQuery)
                HubCategory.CUSTOM -> CustomCalculatorHubContent(repository)
            }
        }
    }
}

// ---------------------------------------------------------
// 1. FINANCE HUB
// ---------------------------------------------------------
@Composable
fun FinanceHubContent(repository: HistoryRepository) {
    var subTool by remember { mutableStateOf("EMI") } // EMI, GST, CI, Inflation, Discount

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("EMI", "GST", "Compound Interest", "Discount", "Inflation").forEach { tool ->
                    ElevatedFilterChip(
                        selected = subTool == tool,
                        onClick = { subTool = tool },
                        label = { Text(tool) }
                    )
                }
            }
        }

        item {
            DisclaimerCard(FinanceEngine.DISCLAIMER)
        }

        when (subTool) {
            "EMI" -> {
                item {
                    var principalStr by remember { mutableStateOf("1000000") } // 10 Lakhs INR
                    var rateStr by remember { mutableStateOf("8.5") } // 8.5%
                    var tenureYearsStr by remember { mutableStateOf("15") } // 15 years
                    var showAmortization by remember { mutableStateOf(false) }

                    val p = principalStr.toDoubleOrNull() ?: 0.0
                    val r = rateStr.toDoubleOrNull() ?: 0.0
                    val t = tenureYearsStr.toIntOrNull() ?: 1
                    val emiResult = FinanceEngine.calculateEmi(p, r, t * 12)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("EMI & Loan Payment Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = principalStr,
                                onValueChange = { principalStr = it },
                                label = { Text("Loan Amount (₹ INR)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = rateStr,
                                    onValueChange = { rateStr = it },
                                    label = { Text("Interest Rate (% p.a.)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )
                                OutlinedTextField(
                                    value = tenureYearsStr,
                                    onValueChange = { tenureYearsStr = it },
                                    label = { Text("Tenure (Years)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))

                            // Principal vs Interest Visual Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(emiResult.principalRatio.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(CalcoraCyan)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(emiResult.interestRatio.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(CalcoraAmber)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Principal (${(emiResult.principalRatio * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, color = CalcoraCyan)
                                Text("Total Interest (${(emiResult.interestRatio * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, color = CalcoraAmber)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            ResultCard(
                                title = "Monthly EMI",
                                primaryResult = "₹ ${MathEngine.formatResult(emiResult.monthlyEmi, 2)}",
                                formula = "EMI = [P × r × (1+r)ⁿ] / [(1+r)ⁿ - 1]",
                                breakdownItems = listOf(
                                    "Principal Loan" to "₹ ${MathEngine.formatResult(p, 2)}",
                                    "Total Interest Payable" to "₹ ${MathEngine.formatResult(emiResult.totalInterest, 2)}",
                                    "Total Payment (Principal + Interest)" to "₹ ${MathEngine.formatResult(emiResult.totalPayment, 2)}"
                                ),
                                onSaveToHistory = {
                                    repository.addHistory(
                                        CalculationHistoryItem(
                                            title = "Loan EMI: ₹${MathEngine.formatResult(p, 0)}",
                                            expression = "₹$p at $r% for $t yrs",
                                            result = "₹${MathEngine.formatResult(emiResult.monthlyEmi, 2)}/mo",
                                            category = "Finance"
                                        )
                                    )
                                }
                            )

                            // Amortization Table Toggle
                            Spacer(modifier = Modifier.height(10.dp))
                            TextButton(
                                onClick = { showAmortization = !showAmortization },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(if (showAmortization) "Hide Amortization Schedule" else "View Year-by-Year Schedule")
                                Icon(
                                    imageVector = if (showAmortization) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }

                            AnimatedVisibility(visible = showAmortization) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    emiResult.yearlySchedule.take(10).forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Year ${item.year}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                            Text("Principal: ₹${MathEngine.formatResult(item.principalPaid, 0)}", style = MaterialTheme.typography.bodySmall)
                                            Text("Balance: ₹${MathEngine.formatResult(item.remainingBalance, 0)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "GST" -> {
                item {
                    var amountStr by remember { mutableStateOf("10000") }
                    var selectedRate by remember { mutableDoubleStateOf(18.0) }
                    var isInclusive by remember { mutableStateOf(false) }

                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    val gstRes = FinanceEngine.calculateGst(amt, selectedRate, isInclusive)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Indian GST Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = amountStr,
                                onValueChange = { amountStr = it },
                                label = { Text("Amount (₹ INR)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Select GST Slab Rate:", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(5.0, 12.0, 18.0, 28.0).forEach { r ->
                                    FilterChip(
                                        selected = selectedRate == r,
                                        onClick = { selectedRate = r },
                                        label = { Text("${r.toInt()}%") }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Mode: ${if (isInclusive) "GST Inclusive" else "GST Exclusive"}")
                                Switch(checked = isInclusive, onCheckedChange = { isInclusive = it })
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            ResultCard(
                                title = "Total Amount",
                                primaryResult = "₹ ${MathEngine.formatResult(gstRes.totalAmount, 2)}",
                                formula = if (isInclusive) "Base = Total / (1 + Rate/100)" else "Total = Base + (Base × Rate/100)",
                                breakdownItems = listOf(
                                    "Base Net Amount" to "₹ ${MathEngine.formatResult(gstRes.baseAmount, 2)}",
                                    "Total GST (${selectedRate.toInt()}%)" to "₹ ${MathEngine.formatResult(gstRes.gstAmount, 2)}",
                                    "CGST (${selectedRate / 2}%)" to "₹ ${MathEngine.formatResult(gstRes.cgst, 2)}",
                                    "SGST (${selectedRate / 2}%)" to "₹ ${MathEngine.formatResult(gstRes.sgst, 2)}"
                                )
                            )
                        }
                    }
                }
            }

            "Compound Interest" -> {
                item {
                    var pStr by remember { mutableStateOf("50000") }
                    var rStr by remember { mutableStateOf("7.5") }
                    var tStr by remember { mutableStateOf("5") }

                    val p = pStr.toDoubleOrNull() ?: 0.0
                    val r = rStr.toDoubleOrNull() ?: 0.0
                    val t = tStr.toDoubleOrNull() ?: 0.0
                    val (totalCI, interestCI) = FinanceEngine.calculateCompoundInterest(p, r, t)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Compound Interest Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(value = pStr, onValueChange = { pStr = it }, label = { Text("Principal (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = rStr, onValueChange = { rStr = it }, label = { Text("Annual Rate (%)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = tStr, onValueChange = { tStr = it }, label = { Text("Time (Years)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            ResultCard(
                                title = "Future Total Value",
                                primaryResult = "₹ ${MathEngine.formatResult(totalCI, 2)}",
                                formula = "A = P × (1 + r/n)ⁿᵗ",
                                breakdownItems = listOf(
                                    "Principal Investment" to "₹ ${MathEngine.formatResult(p, 2)}",
                                    "Total Interest Earned" to "₹ ${MathEngine.formatResult(interestCI, 2)}"
                                )
                            )
                        }
                    }
                }
            }

            "Discount" -> {
                item {
                    var priceStr by remember { mutableStateOf("2500") }
                    var discStr by remember { mutableStateOf("20") }
                    var extraDiscStr by remember { mutableStateOf("5") }

                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val d1 = discStr.toDoubleOrNull() ?: 0.0
                    val d2 = extraDiscStr.toDoubleOrNull() ?: 0.0
                    val (finalPrice, savings, effectiveRate) = FinanceEngine.calculateDiscount(price, d1, d2)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Discount & Savings Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Original Price (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = discStr, onValueChange = { discStr = it }, label = { Text("Discount (%)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = extraDiscStr, onValueChange = { extraDiscStr = it }, label = { Text("Extra Coupon (%)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            ResultCard(
                                title = "Final Discounted Price",
                                primaryResult = "₹ ${MathEngine.formatResult(finalPrice, 2)}",
                                breakdownItems = listOf(
                                    "Total Amount Saved" to "₹ ${MathEngine.formatResult(savings, 2)}",
                                    "Effective Total Discount" to "${MathEngine.formatResult(effectiveRate, 1)}%"
                                )
                            )
                        }
                    }
                }
            }

            "Inflation" -> {
                item {
                    var amountStr by remember { mutableStateOf("100000") }
                    var inflRateStr by remember { mutableStateOf("6.0") }
                    var yearsStr by remember { mutableStateOf("10") }

                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    val ir = inflRateStr.toDoubleOrNull() ?: 0.0
                    val yr = yearsStr.toDoubleOrNull() ?: 0.0
                    val (futureCost, loss) = FinanceEngine.calculateInflation(amt, ir, yr)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Inflation Scenario Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Current Expense (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = inflRateStr, onValueChange = { inflRateStr = it }, label = { Text("Avg Inflation (%)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = yearsStr, onValueChange = { yearsStr = it }, label = { Text("Time (Years)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            ResultCard(
                                title = "Equivalent Cost in $yearsStr Years",
                                primaryResult = "₹ ${MathEngine.formatResult(futureCost, 2)}",
                                breakdownItems = listOf(
                                    "Purchasing Power Decline" to "${MathEngine.formatResult(loss, 1)}%"
                                ),
                                notes = "A mathematical scenario illustrating the compounding effect of general price inflation over time."
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ---------------------------------------------------------
// 2. HEALTH HUB
// ---------------------------------------------------------
@Composable
fun HealthHubContent(repository: HistoryRepository) {
    var heightCmStr by remember { mutableStateOf("175") }
    var weightKgStr by remember { mutableStateOf("70") }
    var ageStr by remember { mutableStateOf("28") }
    var isMale by remember { mutableStateOf(true) }

    val h = heightCmStr.toDoubleOrNull() ?: 175.0
    val w = weightKgStr.toDoubleOrNull() ?: 70.0
    val a = ageStr.toIntOrNull() ?: 28

    val bmiRes = HealthEngine.calculateBmi(h, w)
    val bmr = HealthEngine.calculateBmr(h, w, a, isMale)
    val tdeeModerate = HealthEngine.calculateTdee(bmr, "moderate")
    val waterLiters = HealthEngine.calculateWaterIntakeLiters(w, 30)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            DisclaimerCard(HealthEngine.DISCLAIMER)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Health & Vital Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = heightCmStr,
                            onValueChange = { heightCmStr = it },
                            label = { Text("Height (cm)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = weightKgStr,
                            onValueChange = { weightKgStr = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it },
                            label = { Text("Age") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(selected = isMale, onClick = { isMale = true }, label = { Text("Male") })
                            Spacer(modifier = Modifier.width(6.dp))
                            FilterChip(selected = !isMale, onClick = { isMale = false }, label = { Text("Female") })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BMI Visual Gauge Bar
                    Text("BMI: ${MathEngine.formatResult(bmiRes.bmi, 1)} — ${bmiRes.category}", fontWeight = FontWeight.Bold, color = Color(bmiRes.colorHex))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        Box(modifier = Modifier.weight(18.5f).fillMaxHeight().background(CalcoraCyan))
                        Box(modifier = Modifier.weight(6.4f).fillMaxHeight().background(CalcoraEmerald))
                        Box(modifier = Modifier.weight(5.0f).fillMaxHeight().background(CalcoraAmber))
                        Box(modifier = Modifier.weight(10.0f).fillMaxHeight().background(CalcoraRose))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ResultCard(
                        title = "Body Mass Index (BMI)",
                        primaryResult = "${MathEngine.formatResult(bmiRes.bmi, 1)} (${bmiRes.category})",
                        formula = "BMI = weight (kg) / [height (m)]²",
                        breakdownItems = listOf(
                            "Healthy Weight Range for $h cm" to "${MathEngine.formatResult(bmiRes.minHealthyWeight, 1)} kg – ${MathEngine.formatResult(bmiRes.maxHealthyWeight, 1)} kg",
                            "Basal Metabolic Rate (BMR)" to "${MathEngine.formatResult(bmr, 0)} kcal/day",
                            "Daily Calorie Needs (Moderate Active)" to "${MathEngine.formatResult(tdeeModerate, 0)} kcal/day",
                            "Recommended Daily Water" to "${MathEngine.formatResult(waterLiters, 1)} Liters"
                        )
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ---------------------------------------------------------
// 3. GEOMETRY HUB (with Dynamic Canvas Diagrams!)
// ---------------------------------------------------------
@Composable
fun GeometryHubContent(repository: HistoryRepository) {
    var selectedShape by remember { mutableStateOf("Circle") } // Circle, Rectangle, Triangle, Trapezium, Sphere, Cylinder, Cube

    var param1 by remember { mutableStateOf("10") }
    var param2 by remember { mutableStateOf("6") }

    val p1 = param1.toDoubleOrNull() ?: 10.0
    val p2 = param2.toDoubleOrNull() ?: 6.0

    val calcResult = when (selectedShape) {
        "Circle" -> GeometryEngine.circle(p1)
        "Rectangle" -> GeometryEngine.rectangle(p1, p2)
        "Triangle" -> GeometryEngine.triangle(p1, p2)
        "Trapezium" -> GeometryEngine.trapezium(p1, p2, 5.0)
        "Sphere" -> GeometryEngine.sphere(p1)
        "Cylinder" -> GeometryEngine.cylinder(p1, p2)
        "Cube" -> GeometryEngine.cube(p1)
        else -> GeometryEngine.circle(p1)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Circle", "Rectangle", "Triangle", "Trapezium", "Sphere", "Cylinder", "Cube").forEach { shape ->
                    FilterChip(
                        selected = selectedShape == shape,
                        onClick = { selectedShape = shape },
                        label = { Text(shape) }
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("$selectedShape Calculator & Diagram", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Interactive Canvas Diagram
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val cx = w / 2f
                            val cy = h / 2f

                            when (selectedShape) {
                                "Circle", "Sphere" -> {
                                    val r = min(cx, cy) * 0.65f
                                    drawCircle(
                                        color = CalcoraCyan.copy(alpha = 0.3f),
                                        radius = r,
                                        center = Offset(cx, cy)
                                    )
                                    drawCircle(
                                        color = CalcoraCyan,
                                        radius = r,
                                        center = Offset(cx, cy),
                                        style = Stroke(width = 3f)
                                    )
                                    // Radius line
                                    drawLine(Color.White, Offset(cx, cy), Offset(cx + r, cy), strokeWidth = 2f)
                                }
                                "Rectangle" -> {
                                    val rw = min(w * 0.7f, 220f)
                                    val rh = min(h * 0.6f, 100f)
                                    drawRect(
                                        color = CalcoraEmerald.copy(alpha = 0.3f),
                                        topLeft = Offset(cx - rw / 2f, cy - rh / 2f),
                                        size = Size(rw, rh)
                                    )
                                    drawRect(
                                        color = CalcoraEmerald,
                                        topLeft = Offset(cx - rw / 2f, cy - rh / 2f),
                                        size = Size(rw, rh),
                                        style = Stroke(width = 3f)
                                    )
                                }
                                "Triangle" -> {
                                    val path = Path().apply {
                                        moveTo(cx, cy - 50f)
                                        lineTo(cx + 80f, cy + 50f)
                                        lineTo(cx - 80f, cy + 50f)
                                        close()
                                    }
                                    drawPath(path, CalcoraAmber.copy(alpha = 0.3f))
                                    drawPath(path, CalcoraAmber, style = Stroke(width = 3f))
                                }
                                "Cylinder" -> {
                                    drawOval(
                                        color = CalcoraPurple.copy(alpha = 0.4f),
                                        topLeft = Offset(cx - 50f, cy - 60f),
                                        size = Size(100f, 30f)
                                    )
                                    drawOval(
                                        color = CalcoraPurple,
                                        topLeft = Offset(cx - 50f, cy - 60f),
                                        size = Size(100f, 30f),
                                        style = Stroke(width = 2f)
                                    )
                                    drawLine(CalcoraPurple, Offset(cx - 50f, cy - 45f), Offset(cx - 50f, cy + 45f), strokeWidth = 2f)
                                    drawLine(CalcoraPurple, Offset(cx + 50f, cy - 45f), Offset(cx + 50f, cy + 45f), strokeWidth = 2f)
                                    drawOval(
                                        color = CalcoraPurple.copy(alpha = 0.4f),
                                        topLeft = Offset(cx - 50f, cy + 30f),
                                        size = Size(100f, 30f)
                                    )
                                    drawOval(
                                        color = CalcoraPurple,
                                        topLeft = Offset(cx - 50f, cy + 30f),
                                        size = Size(100f, 30f),
                                        style = Stroke(width = 2f)
                                    )
                                }
                                "Cube" -> {
                                    val s = 70f
                                    drawRect(CalcoraRose.copy(alpha = 0.3f), Offset(cx - s / 2f, cy - s / 2f), Size(s, s))
                                    drawRect(CalcoraRose, Offset(cx - s / 2f, cy - s / 2f), Size(s, s), style = Stroke(width = 3f))
                                }
                                else -> {
                                    drawCircle(CalcoraCyan, 40f, Offset(cx, cy))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Input Fields
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = param1,
                            onValueChange = { param1 = it },
                            label = { Text(if (selectedShape in listOf("Circle", "Sphere")) "Radius (r)" else "Length / Base") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        if (selectedShape !in listOf("Circle", "Sphere", "Cube")) {
                            OutlinedTextField(
                                value = param2,
                                onValueChange = { param2 = it },
                                label = { Text("Width / Height") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ResultCard(
                        title = calcResult.primaryValueLabel,
                        primaryResult = MathEngine.formatResult(calcResult.primaryValue, 4),
                        formula = calcResult.formulaUsed,
                        breakdownItems = listOf(
                            calcResult.secondaryValueLabel to MathEngine.formatResult(calcResult.secondaryValue, 4)
                        ) + calcResult.additionalMetrics.map { it.first to MathEngine.formatResult(it.second, 4) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ---------------------------------------------------------
// 4. PHYSICS & CHEMISTRY HUB
// ---------------------------------------------------------
@Composable
fun PhysicsChemHubContent(repository: HistoryRepository) {
    var subTab by remember { mutableStateOf("Ohm's Law") } // Ohm's Law, Kinematics, Chemistry Molar Mass

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Ohm's Law", "Kinematics", "Chemistry Molar Mass").forEach { tab ->
                    FilterChip(selected = subTab == tab, onClick = { subTab = tab }, label = { Text(tab) })
                }
            }
        }

        when (subTab) {
            "Ohm's Law" -> {
                item {
                    var voltStr by remember { mutableStateOf("12") }
                    var currStr by remember { mutableStateOf("2") }
                    var resStr by remember { mutableStateOf("") }
                    var powStr by remember { mutableStateOf("") }

                    val v = voltStr.toDoubleOrNull()
                    val i = currStr.toDoubleOrNull()
                    val r = resStr.toDoubleOrNull()
                    val p = powStr.toDoubleOrNull()

                    val ohmsRes = PhysicsChemistryEngine.solveOhmsLaw(v, i, r, p)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Ohm's Law & Electrical Power", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Enter any 2 parameters to calculate the rest:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = voltStr, onValueChange = { voltStr = it }, label = { Text("Voltage (V)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = currStr, onValueChange = { currStr = it }, label = { Text("Current (A)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = resStr, onValueChange = { resStr = it }, label = { Text("Resistance (Ω)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = powStr, onValueChange = { powStr = it }, label = { Text("Power (W)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            ResultCard(
                                title = "Circuit Solution",
                                primaryResult = "${MathEngine.formatResult(ohmsRes.voltage, 2)} V, ${MathEngine.formatResult(ohmsRes.current, 2)} A",
                                formula = "V = I × R, P = V × I",
                                breakdownItems = listOf(
                                    "Voltage (V)" to "${MathEngine.formatResult(ohmsRes.voltage, 2)} Volts",
                                    "Current (I)" to "${MathEngine.formatResult(ohmsRes.current, 2)} Amperes",
                                    "Resistance (R)" to "${MathEngine.formatResult(ohmsRes.resistance, 2)} Ohms (Ω)",
                                    "Power Dissipation (P)" to "${MathEngine.formatResult(ohmsRes.power, 2)} Watts"
                                )
                            )
                        }
                    }
                }
            }

            "Kinematics" -> {
                item {
                    var massStr by remember { mutableStateOf("1500") } // kg
                    var velStr by remember { mutableStateOf("25") } // m/s (90 km/h)
                    var heightStr by remember { mutableStateOf("10") } // m

                    val mass = massStr.toDoubleOrNull() ?: 1500.0
                    val vel = velStr.toDoubleOrNull() ?: 25.0
                    val hgt = heightStr.toDoubleOrNull() ?: 10.0

                    val ke = PhysicsChemistryEngine.kineticEnergy(mass, vel)
                    val pe = PhysicsChemistryEngine.potentialEnergy(mass, hgt)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Kinetic & Potential Energy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(value = massStr, onValueChange = { massStr = it }, label = { Text("Mass (kg)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = velStr, onValueChange = { velStr = it }, label = { Text("Velocity (m/s)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = heightStr, onValueChange = { heightStr = it }, label = { Text("Height (m)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            ResultCard(
                                title = "Kinetic Energy (E_k)",
                                primaryResult = "${MathEngine.formatResult(ke / 1000.0, 2)} kJ",
                                formula = "E_k = ½·m·v², E_p = m·g·h",
                                breakdownItems = listOf(
                                    "Kinetic Energy (Joules)" to "${MathEngine.formatResult(ke, 2)} J",
                                    "Potential Energy (Joules)" to "${MathEngine.formatResult(pe, 2)} J",
                                    "Total Mechanical Energy" to "${MathEngine.formatResult((ke + pe) / 1000.0, 2)} kJ"
                                )
                            )
                        }
                    }
                }
            }

            "Chemistry Molar Mass" -> {
                item {
                    var formulaInput by remember { mutableStateOf("H2SO4") }
                    val (molarMass, percentages) = PhysicsChemistryEngine.calculateMolarMass(formulaInput)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Chemical Molar Mass Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = formulaInput,
                                onValueChange = { formulaInput = it },
                                label = { Text("Chemical Formula (e.g. H2O, C6H12O6, NaCl)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick sample formulas
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("H2O", "CO2", "NaCl", "C6H12O6", "CaCO3").forEach { s ->
                                    AssistChip(onClick = { formulaInput = s }, label = { Text(s) })
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            ResultCard(
                                title = "Molar Mass",
                                primaryResult = "${MathEngine.formatResult(molarMass, 3)} g/mol",
                                breakdownItems = percentages.map { "${it.key} % by Mass" to "${MathEngine.formatResult(it.value, 2)}%" }
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ---------------------------------------------------------
// 5. CONSTRUCTION HUB
// ---------------------------------------------------------
@Composable
fun ConstructionHubContent(repository: HistoryRepository) {
    var subTool by remember { mutableStateOf("Concrete") } // Concrete, Tiles, Paint

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            DisclaimerCard(ConstructionEngine.DISCLAIMER)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Concrete", "Tiles", "Paint").forEach { tool ->
                    FilterChip(selected = subTool == tool, onClick = { subTool = tool }, label = { Text(tool) })
                }
            }
        }

        when (subTool) {
            "Concrete" -> {
                item {
                    var lenStr by remember { mutableStateOf("5.0") }
                    var widStr by remember { mutableStateOf("4.0") }
                    var depStr by remember { mutableStateOf("0.15") } // 15 cm slab
                    var wasteStr by remember { mutableStateOf("5") }

                    val l = lenStr.toDoubleOrNull() ?: 5.0
                    val w = widStr.toDoubleOrNull() ?: 4.0
                    val d = depStr.toDoubleOrNull() ?: 0.15
                    val wst = wasteStr.toDoubleOrNull() ?: 5.0

                    val cResult = ConstructionEngine.calculateConcrete(l, w, d, 1.0, 2.0, 4.0, wst)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Concrete / Cement / Sand Estimator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Standard mix ratio 1:2:4 (Cement : Sand : Gravel)", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = lenStr, onValueChange = { lenStr = it }, label = { Text("Length (m)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = widStr, onValueChange = { widStr = it }, label = { Text("Width (m)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = depStr, onValueChange = { depStr = it }, label = { Text("Depth/Thickness (m)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = wasteStr, onValueChange = { wasteStr = it }, label = { Text("Waste (%)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            ResultCard(
                                title = "Estimated Cement Bags",
                                primaryResult = "${MathEngine.formatResult(cResult.cementBags50kg, 1)} Bags (50kg)",
                                formula = "Dry Vol = Wet Vol × 1.54 × (1 + waste%)",
                                breakdownItems = listOf(
                                    "Wet Concrete Volume" to "${MathEngine.formatResult(cResult.wetVolumeM3, 2)} m³",
                                    "Dry Volume (with waste)" to "${MathEngine.formatResult(cResult.dryVolumeM3, 2)} m³",
                                    "Sand Volume Required" to "${MathEngine.formatResult(cResult.sandVolumeM3, 2)} m³",
                                    "Aggregate / Gravel" to "${MathEngine.formatResult(cResult.aggregateVolumeM3, 2)} m³"
                                )
                            )
                        }
                    }
                }
            }

            "Tiles" -> {
                item {
                    var roomLStr by remember { mutableStateOf("6.0") }
                    var roomWStr by remember { mutableStateOf("4.5") }
                    var tileLStr by remember { mutableStateOf("60") } // 60 cm
                    var tileWStr by remember { mutableStateOf("60") }

                    val rl = roomLStr.toDoubleOrNull() ?: 6.0
                    val rw = roomWStr.toDoubleOrNull() ?: 4.5
                    val tl = tileLStr.toDoubleOrNull() ?: 60.0
                    val tw = tileWStr.toDoubleOrNull() ?: 60.0

                    val tileRes = ConstructionEngine.calculateTiles(rl, rw, tl, tw, 10.0, 8)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Floor & Wall Tile Estimator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = roomLStr, onValueChange = { roomLStr = it }, label = { Text("Room L (m)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = roomWStr, onValueChange = { roomWStr = it }, label = { Text("Room W (m)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = tileLStr, onValueChange = { tileLStr = it }, label = { Text("Tile L (cm)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = tileWStr, onValueChange = { tileWStr = it }, label = { Text("Tile W (cm)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            ResultCard(
                                title = "Total Tiles Required (with 10% cuts)",
                                primaryResult = "${tileRes.totalTilesWithWaste} Tiles",
                                breakdownItems = listOf(
                                    "Total Room Area" to "${MathEngine.formatResult(tileRes.roomAreaM2, 2)} m²",
                                    "Exact Net Tiles" to "${tileRes.netTiles}",
                                    "Estimated Tile Boxes (8/box)" to "${tileRes.estimatedBoxes} Boxes"
                                )
                            )
                        }
                    }
                }
            }

            "Paint" -> {
                item {
                    var wallL by remember { mutableStateOf("5") }
                    var wallH by remember { mutableStateOf("3") }
                    var coats by remember { mutableStateOf("2") }

                    val wl = wallL.toDoubleOrNull() ?: 5.0
                    val wh = wallH.toDoubleOrNull() ?: 3.0
                    val c = coats.toIntOrNull() ?: 2

                    val pRes = ConstructionEngine.calculatePaint(wl, wh, 4, 3.5, 10.0, c)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Paint Coverage Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = wallL, onValueChange = { wallL = it }, label = { Text("Wall Length (m)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = wallH, onValueChange = { wallH = it }, label = { Text("Wall Height (m)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            ResultCard(
                                title = "Recommended Paint Purchase",
                                primaryResult = "${pRes.recommendedPurchaseLitres.toInt()} Litres",
                                breakdownItems = listOf(
                                    "Net Paintable Area" to "${MathEngine.formatResult(pRes.netPaintableAreaM2, 1)} m²",
                                    "Exact Litres for $c Coats" to "${MathEngine.formatResult(pRes.totalLitres, 1)} L"
                                )
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ---------------------------------------------------------
// 6. STATISTICS & EQUATION SOLVER HUB
// ---------------------------------------------------------
@Composable
fun StatsMathHubContent(repository: HistoryRepository) {
    var subTab by remember { mutableStateOf("Stats") } // Stats, Quadratic, 2x2 Linear

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Stats", "Quadratic Solver", "2-Var Linear Solver").forEach { tab ->
                    FilterChip(selected = subTab == tab, onClick = { subTab = tab }, label = { Text(tab) })
                }
            }
        }

        when (subTab) {
            "Stats" -> {
                item {
                    var dataInput by remember { mutableStateOf("12, 15, 18, 22, 22, 25, 29, 35, 40, 42") }
                    val parsed = dataInput.split(",", " ", "\n")
                        .mapNotNull { it.trim().toDoubleOrNull() }
                    val stats = StatisticsEquationEngine.computeStatistics(parsed)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Descriptive Statistics Suite", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = dataInput,
                                onValueChange = { dataInput = it },
                                label = { Text("Enter numbers (separated by comma or space)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )

                            if (stats != null) {
                                Spacer(modifier = Modifier.height(14.dp))
                                ResultCard(
                                    title = "Mean & Central Tendency",
                                    primaryResult = "Mean = ${MathEngine.formatResult(stats.mean, 2)}",
                                    breakdownItems = listOf(
                                        "Count (N)" to "${stats.count}",
                                        "Sum (∑x)" to MathEngine.formatResult(stats.sum, 2),
                                        "Median" to MathEngine.formatResult(stats.median, 2),
                                        "Mode" to if (stats.mode.isEmpty()) "No Mode" else stats.mode.joinToString(),
                                        "Standard Deviation (σ)" to MathEngine.formatResult(stats.standardDeviation, 3),
                                        "Variance (s²)" to MathEngine.formatResult(stats.variance, 3),
                                        "Range (Max - Min)" to "${MathEngine.formatResult(stats.min, 1)} – ${MathEngine.formatResult(stats.max, 1)} (${MathEngine.formatResult(stats.range, 1)})",
                                        "Interquartile Range (IQR)" to MathEngine.formatResult(stats.iqr, 2)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            "Quadratic Solver" -> {
                item {
                    var aStr by remember { mutableStateOf("1") }
                    var bStr by remember { mutableStateOf("-5") }
                    var cStr by remember { mutableStateOf("6") }

                    val a = aStr.toDoubleOrNull() ?: 1.0
                    val b = bStr.toDoubleOrNull() ?: -5.0
                    val c = cStr.toDoubleOrNull() ?: 6.0
                    val quadRes = StatisticsEquationEngine.solveQuadratic(a, b, c)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Quadratic Equation Solver", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("ax² + bx + c = 0", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = aStr, onValueChange = { aStr = it }, label = { Text("a") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = bStr, onValueChange = { bStr = it }, label = { Text("b") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = cStr, onValueChange = { cStr = it }, label = { Text("c") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            ResultCard(
                                title = quadRes.natureOfRoots,
                                primaryResult = "x₁ = ${quadRes.root1},  x₂ = ${quadRes.root2}",
                                formula = "x = [-b ± √(b² - 4ac)] / (2a)",
                                breakdownItems = listOf(
                                    "Discriminant (D = b² - 4ac)" to MathEngine.formatResult(quadRes.discriminant, 2),
                                    "Parabola Vertex (x, y)" to "(${MathEngine.formatResult(quadRes.vertexX, 2)}, ${MathEngine.formatResult(quadRes.vertexY, 2)})"
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            StepByStepCard(steps = quadRes.steps.mapIndexed { idx, s -> "Step ${idx + 1}" to s })
                        }
                    }
                }
            }

            "2-Var Linear Solver" -> {
                item {
                    var a1 by remember { mutableStateOf("2") }
                    var b1 by remember { mutableStateOf("3") }
                    var c1 by remember { mutableStateOf("13") }

                    var a2 by remember { mutableStateOf("4") }
                    var b2 by remember { mutableStateOf("-1") }
                    var c2 by remember { mutableStateOf("5") }

                    val res = StatisticsEquationEngine.solve2x2System(
                        a1.toDoubleOrNull() ?: 2.0,
                        b1.toDoubleOrNull() ?: 3.0,
                        c1.toDoubleOrNull() ?: 13.0,
                        a2.toDoubleOrNull() ?: 4.0,
                        b2.toDoubleOrNull() ?: -1.0,
                        c2.toDoubleOrNull() ?: 5.0
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Simultaneous Linear Equations (2 Variables)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Equation 1: a₁·x + b₁·y = c₁", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(value = a1, onValueChange = { a1 = it }, label = { Text("a₁") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = b1, onValueChange = { b1 = it }, label = { Text("b₁") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = c1, onValueChange = { c1 = it }, label = { Text("c₁") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Equation 2: a₂·x + b₂·y = c₂", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(value = a2, onValueChange = { a2 = it }, label = { Text("a₂") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = b2, onValueChange = { b2 = it }, label = { Text("b₂") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                                OutlinedTextField(value = c2, onValueChange = { c2 = it }, label = { Text("c₂") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            if (res != null) {
                                ResultCard(
                                    title = "Unique Solution",
                                    primaryResult = "x = ${res.first},  y = ${res.second}",
                                    formula = "Solved via Cramer's Rule (Determinant Method)"
                                )
                            } else {
                                Text("No unique solution (Lines are parallel or coincident).", color = CalcoraRose)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ---------------------------------------------------------
// 7. AI TUTOR & SOLVER HUB (Gemini 3.1 Pro Preview with HIGH Thinking)
// ---------------------------------------------------------
@Composable
fun AiSolverHubContent(repository: HistoryRepository, initialQuery: String = "") {
    var queryText by remember { mutableStateOf(initialQuery.ifEmpty { "Find the roots of 3x^3 - 5x^2 + 2x - 1 = 0 and explain step-by-step" }) }
    var domainContext by remember { mutableStateOf("Advanced Mathematics") }
    var isLoading by remember { mutableStateOf(false) }
    var solutionText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, CalcoraCyan.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CalcoraCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = CalcoraCyan, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("AI Step-by-Step STEM & Math Solver", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Powered by gemini-3.1-pro-preview with High Thinking Mode", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        label = { Text("Ask any calculation, physics, calculus, or finance question...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Integrate x*exp(x)",
                            "Derive Black-Scholes formula",
                            "Explain Truss Bridge stress",
                            "Calculate pH of 0.05M HCl"
                        ).forEach { sample ->
                            AssistChip(onClick = { queryText = sample }, label = { Text(sample, fontSize = 11.sp) })
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (queryText.isNotBlank()) {
                                isLoading = true
                                errorMessage = null
                                solutionText = null
                                scope.launch {
                                    val result = GeminiAiService.solveAndExplain(queryText, domainContext)
                                    isLoading = false
                                    result.fold(
                                        onSuccess = { sol ->
                                            solutionText = sol
                                            repository.addHistory(
                                                CalculationHistoryItem(
                                                    title = "AI Math Solution",
                                                    expression = queryText.take(50),
                                                    result = "Step-by-step solution resolved",
                                                    category = "AI Solver"
                                                )
                                            )
                                        },
                                        onFailure = { err ->
                                            errorMessage = err.message ?: "Failed to generate solution."
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Deeply reasoning with High Thinking...")
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Solve & Explain Step-by-Step", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CalcoraRose.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, CalcoraRose)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = CalcoraRose)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(errorMessage ?: "", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        if (solutionText != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CalcoraCyan.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Step-by-Step AI Solution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = CalcoraEmerald)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = solutionText ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ---------------------------------------------------------
// 8. CUSTOM CALCULATOR BUILDER HUB
// ---------------------------------------------------------
@Composable
fun CustomCalculatorHubContent(repository: HistoryRepository) {
    val customCalcs by repository.customCalculators.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var calcName by remember { mutableStateOf("") }
    var var1Name by remember { mutableStateOf("Quantity") }
    var var1Unit by remember { mutableStateOf("items") }
    var var2Name by remember { mutableStateOf("Unit Price") }
    var var2Unit by remember { mutableStateOf("₹") }
    var operation by remember { mutableStateOf("multiply") } // multiply, divide, add, subtract
    var outputName by remember { mutableStateOf("Total Cost") }
    var outputUnit by remember { mutableStateOf("₹") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Custom Calculator Builder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Create and save your own personalized calculation formulas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Build New Custom Calculator")
                    }
                }
            }
        }

        items(customCalcs) { calc ->
            var val1Str by remember { mutableStateOf("10") }
            var val2Str by remember { mutableStateOf("5") }

            val v1 = val1Str.toDoubleOrNull() ?: 0.0
            val v2 = val2Str.toDoubleOrNull() ?: 0.0
            val res = when (calc.operation) {
                "multiply" -> v1 * v2
                "divide" -> if (v2 != 0.0) v1 / v2 else 0.0
                "add" -> v1 + v2
                "subtract" -> v1 - v2
                else -> v1 * v2
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(calc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { repository.deleteCustomCalculator(calc.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = CalcoraRose)
                        }
                    }
                    Text(calc.formulaDisplay, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = val1Str,
                            onValueChange = { val1Str = it },
                            label = { Text("${calc.var1Name} (${calc.var1Unit})") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = val2Str,
                            onValueChange = { val2Str = it },
                            label = { Text("${calc.var2Name} (${calc.var2Unit})") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    ResultCard(
                        title = calc.outputName,
                        primaryResult = "${MathEngine.formatResult(res, 2)} ${calc.outputUnit}",
                        formula = "${calc.var1Name} ${when(calc.operation) { "multiply" -> "×"; "divide" -> "÷"; "add" -> "+"; else -> "−" }} ${calc.var2Name} = ${calc.outputName}"
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Build Your Custom Calculator") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = calcName, onValueChange = { calcName = it }, label = { Text("Calculator Name") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = var1Name, onValueChange = { var1Name = it }, label = { Text("Input 1 Name") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = var1Unit, onValueChange = { var1Unit = it }, label = { Text("Unit") }, modifier = Modifier.weight(0.6f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = var2Name, onValueChange = { var2Name = it }, label = { Text("Input 2 Name") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = var2Unit, onValueChange = { var2Unit = it }, label = { Text("Unit") }, modifier = Modifier.weight(0.6f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = outputName, onValueChange = { outputName = it }, label = { Text("Output Result Name") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = outputUnit, onValueChange = { outputUnit = it }, label = { Text("Unit") }, modifier = Modifier.weight(0.6f))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (calcName.isNotBlank()) {
                            repository.addCustomCalculator(
                                CustomCalculator(
                                    name = calcName.trim(),
                                    formulaDisplay = "$var1Name × $var2Name = $outputName",
                                    var1Name = var1Name,
                                    var1Unit = var1Unit,
                                    var2Name = var2Name,
                                    var2Unit = var2Unit,
                                    operation = operation,
                                    outputName = outputName,
                                    outputUnit = outputUnit
                                )
                            )
                            showCreateDialog = false
                            calcName = ""
                        }
                    }
                ) {
                    Text("Save Calculator")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}
