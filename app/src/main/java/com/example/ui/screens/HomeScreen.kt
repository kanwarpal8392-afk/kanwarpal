package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalculationHistoryItem
import com.example.data.HistoryRepository
import com.example.engine.MathEngine
import com.example.ui.components.NavDestination
import com.example.ui.theme.*

data class QuickToolItem(
    val title: String,
    val subtitle: String,
    val category: String,
    val icon: ImageVector,
    val destination: NavDestination,
    val hubCategory: HubCategory? = null,
    val converterCategory: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: HistoryRepository,
    onNavigateDestination: (NavDestination) -> Unit,
    onNavigateHubCategory: (HubCategory) -> Unit,
    onNavigateConverter: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val historyList by repository.history.collectAsState()

    // Mini quick calculation state on Home
    var quickInput by remember { mutableStateOf("") }
    var quickResult by remember { mutableStateOf("0") }

    val allTools = remember {
        listOf(
            QuickToolItem("Standard Calculator", "Basic 0-9, memory, & arithmetic", "Everyday", Icons.Filled.Calculate, NavDestination.CALCULATOR),
            QuickToolItem("Scientific Calculator", "Trig, logs, powers & factorial", "Mathematics", Icons.Filled.Functions, NavDestination.CALCULATOR),
            QuickToolItem("Graphing Calculator", "Interactive 2D multi-function plotting", "Advanced", Icons.Filled.ShowChart, NavDestination.GRAPHING),
            QuickToolItem("Loan & EMI Calculator", "Monthly EMI, amortization & interest", "Finance", Icons.Filled.AccountBalance, NavDestination.HUBS, hubCategory = HubCategory.FINANCE),
            QuickToolItem("Indian GST Calculator", "5%, 12%, 18%, 28% Inc & Exclusive", "Finance", Icons.Filled.ReceiptLong, NavDestination.HUBS, hubCategory = HubCategory.FINANCE),
            QuickToolItem("BMI & Health Metrics", "Body Mass Index, BMR & calorie intake", "Health", Icons.Filled.Favorite, NavDestination.HUBS, hubCategory = HubCategory.HEALTH),
            QuickToolItem("Universal Unit Converter", "Length, Mass, Volume, Temperature, Speed", "Conversion", Icons.Filled.SwapHoriz, NavDestination.CONVERT),
            QuickToolItem("Digital Storage & Download", "File size MB/GB & download time", "Tech", Icons.Filled.Dns, NavDestination.CONVERT, converterCategory = "data"),
            QuickToolItem("2D & 3D Geometry", "Interactive dynamic Canvas shape diagrams", "Geometry", Icons.Filled.SquareFoot, NavDestination.HUBS, hubCategory = HubCategory.GEOMETRY),
            QuickToolItem("Ohm's Law & Circuit Solver", "V = IR, Electrical Power & Resistance", "Physics", Icons.Filled.Bolt, NavDestination.HUBS, hubCategory = HubCategory.PHYSICS_CHEM),
            QuickToolItem("Chemistry Molar Mass", "Empirical formula mass & elements %", "Chemistry", Icons.Filled.Science, NavDestination.HUBS, hubCategory = HubCategory.PHYSICS_CHEM),
            QuickToolItem("Concrete & Cement Estimator", "Slab wet/dry volume, cement bags & sand", "Construction", Icons.Filled.Handyman, NavDestination.HUBS, hubCategory = HubCategory.CONSTRUCTION),
            QuickToolItem("Descriptive Statistics", "Mean, median, mode, std dev & IQR", "Statistics", Icons.Filled.Equalizer, NavDestination.HUBS, hubCategory = HubCategory.STATS_MATH),
            QuickToolItem("Quadratic Equation Solver", "ax² + bx + c = 0 step-by-step roots", "Algebra", Icons.Filled.LinearScale, NavDestination.HUBS, hubCategory = HubCategory.STATS_MATH),
            QuickToolItem("AI STEM & Math Tutor", "Deep step-by-step thinking solver", "Artificial Intelligence", Icons.Filled.AutoAwesome, NavDestination.HUBS, hubCategory = HubCategory.AI_SOLVER),
            QuickToolItem("Custom Calculator Builder", "Create and save your personalized formulas", "Tools", Icons.Filled.Build, NavDestination.HUBS, hubCategory = HubCategory.CUSTOM)
        )
    }

    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else allTools.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subtitle.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CalcoraCyan.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CalcoraCyan.copy(alpha = 0.12f),
                                    Color.Transparent
                                ),
                                radius = 600f
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "CALCORA PLATFORM",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Every Calculator You Need, In One Place",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            lineHeight = 28.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Simple calculations, scientific mathematics, graphing, conversions, finance, health, construction, education and more.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search a calculator (e.g. BMI, EMI, Scientific, GST)...") },
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = CalcoraCyan) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }

        // Search Results List (if actively searching)
        if (searchResults.isNotEmpty()) {
            item {
                Text(
                    text = "Matching Calculators (${searchResults.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(searchResults) { tool ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (tool.hubCategory != null) {
                                onNavigateHubCategory(tool.hubCategory)
                            } else if (tool.converterCategory != null) {
                                onNavigateConverter(tool.converterCategory)
                            } else {
                                onNavigateDestination(tool.destination)
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(tool.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tool.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(tool.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Quick Inline Calculator Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quick Math Express", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { onNavigateDestination(NavDestination.CALCULATOR) }) {
                            Text("Full Keypad", fontSize = 12.sp)
                            Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = quickInput,
                            onValueChange = {
                                quickInput = it
                                try {
                                    if (it.isNotBlank()) {
                                        val eval = MathEngine.evaluate(it, "DEG")
                                        quickResult = MathEngine.formatResult(eval, 4)
                                    } else {
                                        quickResult = "0"
                                    }
                                } catch (_: Exception) {}
                            },
                            placeholder = { Text("e.g. 25 * (15 + 8) or sqrt(144) + 35") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                try {
                                    val eval = MathEngine.evaluate(quickInput, "DEG")
                                    val formatted = MathEngine.formatResult(eval, 4)
                                    quickResult = formatted
                                    repository.addHistory(
                                        CalculationHistoryItem(
                                            title = "Quick Express",
                                            expression = quickInput,
                                            result = formatted,
                                            category = "General"
                                        )
                                    )
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("=")
                        }
                    }

                    if (quickInput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "= $quickResult",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        // Popular Tools Shortcuts (Horizontal Scroll)
        item {
            Text(
                text = "Popular Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(
                    listOf(
                        Triple("EMI Calculator", Icons.Filled.AccountBalance, HubCategory.FINANCE),
                        Triple("BMI & Health", Icons.Filled.Favorite, HubCategory.HEALTH),
                        Triple("2D Graphing", Icons.Filled.ShowChart, null),
                        Triple("GST Tax India", Icons.Filled.ReceiptLong, HubCategory.FINANCE),
                        Triple("Unit Converter", Icons.Filled.SwapHoriz, null),
                        Triple("Concrete Mix", Icons.Filled.Handyman, HubCategory.CONSTRUCTION),
                        Triple("AI Math Solver", Icons.Filled.AutoAwesome, HubCategory.AI_SOLVER)
                    )
                ) { (name, icon, hubCat) ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .width(130.dp)
                            .clickable {
                                if (hubCat != null) onNavigateHubCategory(hubCat)
                                else if (name.contains("Graphing")) onNavigateDestination(NavDestination.GRAPHING)
                                else onNavigateDestination(NavDestination.CONVERT)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Category Exploration Cards
        item {
            Text(
                text = "Category Hubs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryHubCard(
                        title = "Finance Suite",
                        desc = "EMI, GST, CI, Discount, Inflation",
                        icon = Icons.Filled.AccountBalance,
                        color = CalcoraEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateHubCategory(HubCategory.FINANCE) }
                    )
                    CategoryHubCard(
                        title = "Health Suite",
                        desc = "BMI, BMR, Calories, Water",
                        icon = Icons.Filled.Favorite,
                        color = CalcoraRose,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateHubCategory(HubCategory.HEALTH) }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryHubCard(
                        title = "Geometry Suite",
                        desc = "2D & 3D shapes with dynamic Canvas",
                        icon = Icons.Filled.SquareFoot,
                        color = CalcoraAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateHubCategory(HubCategory.GEOMETRY) }
                    )
                    CategoryHubCard(
                        title = "Physics & Chem",
                        desc = "Ohm's Law, Energy, Molar Mass",
                        icon = Icons.Filled.Science,
                        color = CalcoraPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateHubCategory(HubCategory.PHYSICS_CHEM) }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryHubCard(
                        title = "Construction",
                        desc = "Concrete, Tiles, Paint Estimates",
                        icon = Icons.Filled.Handyman,
                        color = CalcoraCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateHubCategory(HubCategory.CONSTRUCTION) }
                    )
                    CategoryHubCard(
                        title = "AI Solver",
                        desc = "High Thinking Math Reasoning",
                        icon = Icons.Filled.AutoAwesome,
                        color = CalcoraCyanDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateHubCategory(HubCategory.AI_SOLVER) }
                    )
                }
            }
        }

        // FAQs Section
        item {
            Text(
                text = "Frequently Asked Questions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FaqItem(
                    question = "How is loan EMI calculated?",
                    answer = "EMI (Equated Monthly Installment) is computed using the formula: EMI = [P × r × (1+r)ⁿ] / [(1+r)ⁿ - 1], where P is Principal loan amount, r is monthly interest rate (annual rate / 12 / 100), and n is total tenure in months."
                )
                FaqItem(
                    question = "What are the standard Indian GST tax rates?",
                    answer = "Calcora supports standard slabs prescribed by the GST Council of India: 5%, 12%, 18%, and 28%. In inclusive mode, base price = Total / (1 + Rate/100). In exclusive mode, GST is added to the base price."
                )
                FaqItem(
                    question = "How does the AI Step-by-Step Solver work?",
                    answer = "The AI Solver uses Google's state-of-the-art gemini-3.1-pro-preview model configured with ThinkingLevel.HIGH to deeply reason, verify algebra, and format complete multi-step mathematical solutions."
                )
            }
        }

        item {
            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CALCORA",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Calculate Anything. Convert Everything.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryHubCard(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(question, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(answer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                }
            }
        }
    }
}
