package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalculationHistoryItem
import com.example.data.HistoryRepository
import com.example.engine.MathEngine
import com.example.ui.components.CalcoraButton
import com.example.ui.components.StepByStepCard
import com.example.ui.components.triggerHapticFeedback
import com.example.ui.theme.*

enum class CalculatorMode {
    STANDARD,
    SCIENTIFIC,
    EXPRESSION
}

@Composable
fun CalculatorScreen(
    repository: HistoryRepository,
    onOpenAiSolverWithQuery: (String) -> Unit = {}
) {
    var mode by remember { mutableStateOf(CalculatorMode.STANDARD) }
    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }
    var memoryValue by remember { mutableDoubleStateOf(0.0) }
    var angleMode by remember { mutableStateOf("DEG") } // DEG, RAD, GRAD
    var showSteps by remember { mutableStateOf(false) }
    var calculationSteps by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isRadMode by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Keypad actions
    fun onInput(char: String) {
        expression += char
        // Real-time live evaluation preview if valid
        try {
            if (expression.isNotEmpty()) {
                val eval = MathEngine.evaluate(expression, angleMode)
                resultText = MathEngine.formatResult(eval, 4)
            }
        } catch (_: Exception) {}
    }

    fun onBackspace() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            if (expression.isEmpty()) {
                resultText = "0"
            } else {
                try {
                    val eval = MathEngine.evaluate(expression, angleMode)
                    resultText = MathEngine.formatResult(eval, 4)
                } catch (_: Exception) {}
            }
        }
    }

    fun onClear() {
        expression = ""
        resultText = "0"
        calculationSteps = emptyList()
        showSteps = false
    }

    fun onEquals() {
        if (expression.isBlank()) return
        try {
            val eval = MathEngine.evaluate(expression, angleMode)
            val formatted = MathEngine.formatResult(eval, 6)
            resultText = formatted
            calculationSteps = MathEngine.generateSteps(expression, angleMode)

            // Save to history repository
            repository.addHistory(
                CalculationHistoryItem(
                    title = if (mode == CalculatorMode.SCIENTIFIC) "Scientific Calculation" else "Standard Calculation",
                    expression = expression,
                    result = formatted,
                    category = "Math",
                    details = "Angle Mode: $angleMode"
                )
            )
        } catch (e: Exception) {
            resultText = "Error: ${e.message?.take(25) ?: "Invalid"}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("calculator_screen")
    ) {
        // Mode Selector Tab Row & Angle Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                SegmentedButton(
                    selected = mode == CalculatorMode.STANDARD,
                    onClick = { mode = CalculatorMode.STANDARD },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("Basic", fontSize = 12.sp)
                }
                SegmentedButton(
                    selected = mode == CalculatorMode.SCIENTIFIC,
                    onClick = { mode = CalculatorMode.SCIENTIFIC },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("Scientific", fontSize = 12.sp)
                }
                SegmentedButton(
                    selected = mode == CalculatorMode.EXPRESSION,
                    onClick = { mode = CalculatorMode.EXPRESSION },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("Steps", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Angle Mode Switcher (DEG / RAD / GRAD)
            AssistChip(
                onClick = {
                    angleMode = when (angleMode) {
                        "DEG" -> "RAD"
                        "RAD" -> "GRAD"
                        else -> "DEG"
                    }
                    triggerHapticFeedback(context)
                },
                label = { Text(angleMode, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = null
            )
        }

        // Display Screen Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Top controls inside display (Memory indicators & Copy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (memoryValue != 0.0) {
                            Text(
                                text = "M [${MathEngine.formatResult(memoryValue, 2)}]",
                                style = MaterialTheme.typography.labelSmall,
                                color = CalcoraEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row {
                        if (calculationSteps.isNotEmpty()) {
                            IconButton(
                                onClick = { showSteps = !showSteps },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FormatListNumbered,
                                    contentDescription = "Steps",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(resultText))
                                triggerHapticFeedback(context)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onOpenAiSolverWithQuery(expression.ifBlank { resultText }) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Solve with AI",
                                tint = CalcoraCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Expression text
                Text(
                    text = expression.ifEmpty { "0" },
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState(Int.MAX_VALUE))
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Result text (Big display)
                Text(
                    text = resultText,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Step-by-step collapsible card if requested
        if (showSteps && calculationSteps.isNotEmpty()) {
            StepByStepCard(
                steps = calculationSteps,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        // Keypad Container
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Memory & Secondary Bar (M+, M-, MR, MC, +/-, %)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalcoraButton("MC", { memoryValue = 0.0 }, Modifier.weight(1f), fontSize = 14, backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("MR", { if (memoryValue != 0.0) onInput(MathEngine.formatResult(memoryValue, 4)) }, Modifier.weight(1f), fontSize = 14, backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("M+", {
                    val curr = resultText.toDoubleOrNull() ?: 0.0
                    memoryValue += curr
                }, Modifier.weight(1f), fontSize = 14, backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("M-", {
                    val curr = resultText.toDoubleOrNull() ?: 0.0
                    memoryValue -= curr
                }, Modifier.weight(1f), fontSize = 14, backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("±", {
                    if (expression.startsWith("-")) {
                        expression = expression.drop(1)
                    } else if (expression.isNotEmpty()) {
                        expression = "-$expression"
                    }
                }, Modifier.weight(1f), fontSize = 16, backgroundColor = MaterialTheme.colorScheme.surface)
            }

            // Scientific rows (if mode == SCIENTIFIC)
            if (mode == CalculatorMode.SCIENTIFIC) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcoraButton("sin", { onInput("sin(") }, Modifier.weight(1f), fontSize = 13, backgroundColor = MaterialTheme.colorScheme.surface)
                    CalcoraButton("cos", { onInput("cos(") }, Modifier.weight(1f), fontSize = 13, backgroundColor = MaterialTheme.colorScheme.surface)
                    CalcoraButton("tan", { onInput("tan(") }, Modifier.weight(1f), fontSize = 13, backgroundColor = MaterialTheme.colorScheme.surface)
                    CalcoraButton("ln", { onInput("ln(") }, Modifier.weight(1f), fontSize = 13, backgroundColor = MaterialTheme.colorScheme.surface)
                    CalcoraButton("log", { onInput("log(") }, Modifier.weight(1f), fontSize = 13, backgroundColor = MaterialTheme.colorScheme.surface)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcoraButton("sin⁻¹", { onInput("asin(") }, Modifier.weight(1f), fontSize = 12, backgroundColor = MaterialTheme.colorScheme.surface)
                    CalcoraButton("cos⁻¹", { onInput("acos(") }, Modifier.weight(1f), fontSize = 12, backgroundColor = MaterialTheme.colorScheme.surface)
                    CalcoraButton("tan⁻¹", { onInput("atan(") }, Modifier.weight(1f), fontSize = 12, backgroundColor = MaterialTheme.colorScheme.surface)
                    CalcoraButton("√", { onInput("sqrt(") }, Modifier.weight(1f), fontSize = 14, backgroundColor = MaterialTheme.colorScheme.surface)
                    CalcoraButton("^", { onInput("^") }, Modifier.weight(1f), fontSize = 14, backgroundColor = MaterialTheme.colorScheme.surface)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcoraButton("π", { onInput("pi") }, Modifier.weight(1f), fontSize = 14, backgroundColor = MaterialTheme.colorScheme.surface)
                    CalcoraButton("e", { onInput("e") }, Modifier.weight(1f), fontSize = 14, backgroundColor = MaterialTheme.colorScheme.surface)
                    CalcoraButton("n!", { onInput("!") }, Modifier.weight(1f), fontSize = 13, backgroundColor = MaterialTheme.colorScheme.surface)
                    CalcoraButton("(", { onInput("(") }, Modifier.weight(1f), fontSize = 14, backgroundColor = MaterialTheme.colorScheme.surface)
                    CalcoraButton(")", { onInput(")") }, Modifier.weight(1f), fontSize = 14, backgroundColor = MaterialTheme.colorScheme.surface)
                }
            }

            // Standard Numeric & Operators Keypad
            // Row 1: AC, Del, %, ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalcoraButton("AC", { onClear() }, Modifier.weight(1f), backgroundColor = CalcoraRose.copy(alpha = 0.2f), contentColor = CalcoraRose, fontWeight = FontWeight.Bold)
                CalcoraButton("⌫", { onBackspace() }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                CalcoraButton("%", { onInput("%") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.primary)
                CalcoraButton("÷", { onInput("÷") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalcoraButton("7", { onInput("7") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("8", { onInput("8") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("9", { onInput("9") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("×", { onInput("×") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }

            // Row 3: 4, 5, 6, −
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalcoraButton("4", { onInput("4") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("5", { onInput("5") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("6", { onInput("6") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("−", { onInput("−") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalcoraButton("1", { onInput("1") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("2", { onInput("2") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("3", { onInput("3") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton("+", { onInput("+") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }

            // Row 5: 0, ., =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalcoraButton("0", { onInput("0") }, Modifier.weight(2f), backgroundColor = MaterialTheme.colorScheme.surface)
                CalcoraButton(".", { onInput(".") }, Modifier.weight(1f), backgroundColor = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold)
                CalcoraButton("=", { onEquals() }, Modifier.weight(1f), backgroundColor = CalcoraCyanDark, contentColor = Color.White, fontWeight = FontWeight.Bold, fontSize = 24)
            }
        }
    }
}
