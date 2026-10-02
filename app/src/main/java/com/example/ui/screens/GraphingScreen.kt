package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MathEngine
import com.example.ui.theme.*
import kotlin.math.*

data class GraphFunction(
    val id: String = java.util.UUID.randomUUID().toString(),
    var expression: String,
    var color: Color,
    var isVisible: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphingScreen() {
    var xMin by remember { mutableFloatStateOf(-10f) }
    var xMax by remember { mutableFloatStateOf(10f) }
    var yMin by remember { mutableFloatStateOf(-10f) }
    var yMax by remember { mutableFloatStateOf(10f) }

    val functions = remember {
        mutableStateListOf(
            GraphFunction(expression = "x^2 - 4", color = CalcoraCyan),
            GraphFunction(expression = "3*sin(x)", color = CalcoraAmber, isVisible = true)
        )
    }

    var newFuncText by remember { mutableStateOf("") }
    var tracePoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var showWindowSettings by remember { mutableStateOf(false) }

    // Evaluator helper for f(x)
    fun evalFunction(expr: String, xVal: Float): Float? {
        val replaced = expr
            .replace("x", "($xVal)")
            .replace("X", "($xVal)")
        return try {
            val res = MathEngine.evaluate(replaced, "RAD")
            if (res.isNaN() || res.isInfinite()) null else res.toFloat()
        } catch (_: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("graphing_screen")
    ) {
        // Header Controls: Zoom, Pan & Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Interactive Grapher",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Zoom in
                IconButton(
                    onClick = {
                        val xSpan = (xMax - xMin) * 0.2f
                        val ySpan = (yMax - yMin) * 0.2f
                        xMin += xSpan
                        xMax -= xSpan
                        yMin += ySpan
                        yMax -= ySpan
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom In")
                }

                // Zoom out
                IconButton(
                    onClick = {
                        val xSpan = (xMax - xMin) * 0.25f
                        val ySpan = (yMax - yMin) * 0.25f
                        xMin -= xSpan
                        xMax += xSpan
                        yMin -= ySpan
                        yMax += ySpan
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.ZoomOut, contentDescription = "Zoom Out")
                }

                // Reset
                IconButton(
                    onClick = {
                        xMin = -10f
                        xMax = 10f
                        yMin = -10f
                        yMax = 10f
                        tracePoint = null
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = "Reset View")
                }

                // Settings modal toggle
                IconButton(
                    onClick = { showWindowSettings = !showWindowSettings },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Outlined.Tune, contentDescription = "Window Settings")
                }
            }
        }

        // Trace Tooltip if active
        if (tracePoint != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Cursor: x = ${String.format(java.util.Locale.US, "%.2f", tracePoint!!.first)}, y = ${String.format(java.util.Locale.US, "%.2f", tracePoint!!.second)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // The Graph Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkBackground)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val xRange = xMax - xMin
                            val yRange = yMax - yMin
                            val dx = (dragAmount.x / size.width) * xRange
                            val dy = (dragAmount.y / size.height) * yRange
                            xMin -= dx
                            xMax -= dx
                            yMin += dy
                            yMax += dy
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val x = xMin + (offset.x / size.width) * (xMax - xMin)
                            val y = yMax - (offset.y / size.height) * (yMax - yMin)
                            tracePoint = x to y
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height

                fun toScreenX(x: Float): Float = ((x - xMin) / (xMax - xMin)) * canvasW
                fun toScreenY(y: Float): Float = ((yMax - y) / (yMax - yMin)) * canvasH

                // Draw Grid
                val gridColor = Color.White.copy(alpha = 0.08f)
                val axisColor = Color.White.copy(alpha = 0.45f)

                // Vertical grid lines
                var gx = floor(xMin).toInt()
                while (gx <= ceil(xMax).toInt()) {
                    val sx = toScreenX(gx.toFloat())
                    drawLine(gridColor, Offset(sx, 0f), Offset(sx, canvasH), strokeWidth = 1f)
                    gx += if ((xMax - xMin) > 40) 5 else if ((xMax - xMin) > 20) 2 else 1
                }

                // Horizontal grid lines
                var gy = floor(yMin).toInt()
                while (gy <= ceil(yMax).toInt()) {
                    val sy = toScreenY(gy.toFloat())
                    drawLine(gridColor, Offset(0f, sy), Offset(canvasW, sy), strokeWidth = 1f)
                    gy += if ((yMax - yMin) > 40) 5 else if ((yMax - yMin) > 20) 2 else 1
                }

                // Origin Axes
                val originX = toScreenX(0f)
                val originY = toScreenY(0f)
                if (originX in 0f..canvasW) {
                    drawLine(axisColor, Offset(originX, 0f), Offset(originX, canvasH), strokeWidth = 2.5f)
                }
                if (originY in 0f..canvasH) {
                    drawLine(axisColor, Offset(0f, originY), Offset(canvasW, originY), strokeWidth = 2.5f)
                }

                // Plot Each Visible Function
                val stepPixels = 3
                for (func in functions) {
                    if (!func.isVisible || func.expression.isBlank()) continue

                    val path = Path()
                    var isFirst = true

                    for (px in 0..canvasW.toInt() step stepPixels) {
                        val mathX = xMin + (px.toFloat() / canvasW) * (xMax - xMin)
                        val mathY = evalFunction(func.expression, mathX)

                        if (mathY != null && !mathY.isNaN() && abs(mathY) < 1e5) {
                            val py = toScreenY(mathY)
                            if (py in -canvasH..canvasH * 2) {
                                if (isFirst) {
                                    path.moveTo(px.toFloat(), py)
                                    isFirst = false
                                } else {
                                    path.lineTo(px.toFloat(), py)
                                }
                            } else {
                                isFirst = true
                            }
                        } else {
                            isFirst = true
                        }
                    }

                    drawPath(
                        path = path,
                        color = func.color,
                        style = Stroke(width = 3.5f)
                    )
                }

                // Draw Trace Point if selected
                tracePoint?.let { (tx, ty) ->
                    val sx = toScreenX(tx)
                    val sy = toScreenY(ty)
                    drawCircle(Color.White, radius = 6f, center = Offset(sx, sy))
                    drawCircle(CalcoraCyan, radius = 4f, center = Offset(sx, sy))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Functions List & Input Row
        Text(
            text = "Equations & Functions",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Function list items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            functions.forEachIndexed { index, func ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(func.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "y₁ = ${func.expression}",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    // Visibility toggle
                    IconButton(
                        onClick = { func.isVisible = !func.isVisible },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (func.isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle visibility",
                            tint = if (func.isVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Delete
                    if (functions.size > 1) {
                        IconButton(
                            onClick = { functions.removeAt(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = CalcoraRose
                            )
                        }
                    }
                }
            }

            // Add new function row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newFuncText,
                    onValueChange = { newFuncText = it },
                    placeholder = { Text("e.g. cos(x), x^3 - 3*x") },
                    leadingIcon = { Text("y =", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newFuncText.isNotBlank()) {
                            val colors = listOf(CalcoraEmerald, CalcoraRose, CalcoraPurple, CalcoraAmber, CalcoraCyan)
                            val nextColor = colors[functions.size % colors.size]
                            functions.add(GraphFunction(expression = newFuncText.trim(), color = nextColor))
                            newFuncText = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Plot")
                }
            }

            // Quick function presets chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = { functions.add(GraphFunction(expression = "x^3 - 4*x", color = CalcoraPurple)) },
                    label = { Text("x³ - 4x", fontSize = 11.sp) }
                )
                AssistChip(
                    onClick = { functions.add(GraphFunction(expression = "2*cos(x)", color = CalcoraEmerald)) },
                    label = { Text("2 cos(x)", fontSize = 11.sp) }
                )
                AssistChip(
                    onClick = { functions.add(GraphFunction(expression = "1/(x^2 + 1)", color = CalcoraRose)) },
                    label = { Text("Lorentzian", fontSize = 11.sp) }
                )
            }
        }
    }
}
