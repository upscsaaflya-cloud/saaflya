package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.ExamSubject
import com.example.data.model.LearningDashboardUiState
import com.example.data.model.SubjectProgressMetric

enum class DashboardChartType(
    val id: String,
    val labelEn: String,
    val labelGu: String
) {
    BAR_CHART("BAR", "BarChart", "સ્તંભ આલેખ"),
    RADIAL_CHART("RADIAL", "RadialChart", "વર્તુળ આલેખ"),
    AREA_CHART("AREA", "AreaChart", "પ્રાવીણ્ય આલેખ")
}

private val SubjectChartPalette = listOf(
    Color(0xFF0A235C), // Indian Polity — Royal Peacock Navy
    Color(0xFF9E6F1D), // Gujarat History — Feather Gold
    Color(0xFF007075), // Geography — Peacock Emerald Teal
    Color(0xFF1565C0), // Indian Economy — Cobalt Blue
    Color(0xFF2E7D32)  // General Science — Forest Green
)

/**
 * Interactive Learning Dashboard section visualizing student completion and accuracy percentages
 * across subjects (Indian Polity, Geography, Gujarat History & Culture, Indian Economy, General Science)
 * using Recharts-inspired Responsive BarChart, RadialBarChart, and AreaChart visualizations.
 */
@Composable
fun LearningDashboardSection(
    language: AppLanguage,
    dashboardState: LearningDashboardUiState,
    selectedSubject: ExamSubject,
    onSelectSubject: (ExamSubject) -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = true
) {
    var isExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    var activeChartType by rememberSaveable { mutableStateOf(DashboardChartType.BAR_CHART) }
    val metrics = dashboardState.subjectMetrics

    var inspectedSubjectIndex by rememberSaveable {
        mutableIntStateOf(
            metrics.indexOfFirst { it.subject == selectedSubject }.coerceAtLeast(0)
        )
    }
    val safeInspectedIdx = if (metrics.isEmpty()) 0 else inspectedSubjectIndex.coerceIn(0, metrics.lastIndex)
    val inspectedMetric = metrics.getOrNull(safeInspectedIdx)

    val animProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "chartAnimationProgress"
    )

    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("learning_dashboard_section")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 1. Header Row with Overall Completion Pill & Expand/Collapse Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (language == AppLanguage.GUJARATI) {
                                "લર્નિંગ ડેશબોર્ડ • વિષયવાર પ્રગતિ આલેખ"
                            } else {
                                "Learning Dashboard • Subject Progress Charts"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (language == AppLanguage.GUJARATI) {
                                "કુલ પૂર્ણતા: ${dashboardState.overallCompletionPercent}% • ચોકસાઈ: ${dashboardState.overallAccuracyPercent}% • સોલ્વ કરેલા MCQ: ${dashboardState.totalQuestionsAttempted}/${dashboardState.totalQuestionsAvailable}"
                            } else {
                                "Overall Completion: ${dashboardState.overallCompletionPercent}% • Accuracy: ${dashboardState.overallAccuracyPercent}% • MCQs: ${dashboardState.totalQuestionsAttempted}/${dashboardState.totalQuestionsAvailable}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("toggle_learning_dashboard_button")
                ) {
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            if (isExpanded) "સંક્ષિપ્ત" else "આલેખ જુઓ"
                        } else {
                            if (isExpanded) "Collapse" else "Expand"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            // 2. Summary KPI Strip (Always visible)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardKpiChip(
                    label = if (language == AppLanguage.GUJARATI) "કુલ પૂર્ણતા" else "Completion",
                    value = "${dashboardState.overallCompletionPercent}%",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                DashboardKpiChip(
                    label = if (language == AppLanguage.GUJARATI) "પ્રશ્ન ચોકસાઈ" else "Accuracy",
                    value = "${dashboardState.overallAccuracyPercent}%",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                DashboardKpiChip(
                    label = if (language == AppLanguage.GUJARATI) "અભ્યાસક્રમ મુદ્દા" else "Syllabus Done",
                    value = "${dashboardState.totalSyllabusCompleted}/${dashboardState.totalSyllabusTopics}",
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            AnimatedVisibility(visible = isExpanded && metrics.isNotEmpty()) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Recharts-Style Chart Switcher (BarChart / RadialChart / AreaChart)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DashboardChartType.entries.forEach { chartType ->
                            val selected = activeChartType == chartType
                            val icon = when (chartType) {
                                DashboardChartType.BAR_CHART -> Icons.Default.BarChart
                                DashboardChartType.RADIAL_CHART -> Icons.Default.DonutLarge
                                DashboardChartType.AREA_CHART -> Icons.Default.ShowChart
                            }
                            FilterChip(
                                selected = selected,
                                onClick = { activeChartType = chartType },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (language == AppLanguage.GUJARATI) chartType.labelGu else chartType.labelEn,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("chart_type_${chartType.id}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Chart Legend Row (Completion % vs Accuracy %)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChartLegendItem(
                            color = MaterialTheme.colorScheme.primary,
                            label = if (language == AppLanguage.GUJARATI) {
                                "પૂર્ણતા ટકાવારી (Completion %)"
                            } else {
                                "Subject Completion (%)"
                            }
                        )
                        ChartLegendItem(
                            color = MaterialTheme.colorScheme.secondary,
                            label = if (language == AppLanguage.GUJARATI) {
                                "પ્રશ્ન ચોકસાઈ (Quiz Accuracy %)"
                            } else {
                                "Quiz Accuracy (%)"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Active Interactive Visualization Container
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recharts_visualization_container")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            when (activeChartType) {
                                DashboardChartType.BAR_CHART -> {
                                    RechartsResponsiveBarChart(
                                        metrics = metrics,
                                        language = language,
                                        selectedIndex = safeInspectedIdx,
                                        animProgress = animProgress,
                                        onSelectBarIndex = { idx ->
                                            inspectedSubjectIndex = idx
                                        }
                                    )
                                }
                                DashboardChartType.RADIAL_CHART -> {
                                    RechartsResponsiveRadialChart(
                                        metrics = metrics,
                                        overallCompletion = dashboardState.overallCompletionPercent,
                                        language = language,
                                        selectedIndex = safeInspectedIdx,
                                        animProgress = animProgress,
                                        onSelectSubjectIndex = { idx ->
                                            inspectedSubjectIndex = idx
                                        }
                                    )
                                }
                                DashboardChartType.AREA_CHART -> {
                                    RechartsResponsiveAreaChart(
                                        metrics = metrics,
                                        language = language,
                                        selectedIndex = safeInspectedIdx,
                                        animProgress = animProgress,
                                        onSelectPointIndex = { idx ->
                                            inspectedSubjectIndex = idx
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 6. Recharts-Style Interactive Tooltip / Active Subject Card
                    if (inspectedMetric != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        InspectedSubjectTooltipCard(
                            metric = inspectedMetric,
                            language = language,
                            accentColor = SubjectChartPalette[safeInspectedIdx % SubjectChartPalette.size],
                            onPracticeSubject = {
                                onSelectSubject(inspectedMetric.subject)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // 7. Subject-by-Subject Completion Breakdown List (Polity, Geography, History, Economy, Science)
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "વિષયવાર પૂર્ણતા ટકાવારી (ભારતીય બંધારણ, ભૂગોળ, ઇતિહાસ, અર્થતંત્ર, વિજ્ઞાન)"
                        } else {
                            "Subject Completion Breakdown (Indian Polity, Geography, History, Economy, Science)"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        metrics.forEachIndexed { idx, metric ->
                            val isSelected = idx == safeInspectedIdx
                            val subjectColor = SubjectChartPalette[idx % SubjectChartPalette.size]
                            SubjectCompletionRow(
                                metric = metric,
                                language = language,
                                accentColor = subjectColor,
                                isSelected = isSelected,
                                onClick = {
                                    inspectedSubjectIndex = idx
                                    onSelectSubject(metric.subject)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardKpiChip(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChartLegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Recharts-style Cartesian Grouped BarChart with CartesianGrid, YAxis (0%..100%), XAxis subject labels,
 * dual bars per subject (Completion % and Accuracy %), and tap-to-inspect interaction.
 */
@Composable
private fun RechartsResponsiveBarChart(
    metrics: List<SubjectProgressMetric>,
    language: AppLanguage,
    selectedIndex: Int,
    animProgress: Float,
    onSelectBarIndex: (Int) -> Unit
) {
    val primaryBarColor = MaterialTheme.colorScheme.primary
    val secondaryBarColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val highlightFill = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // YAxis Labels Column (100% down to 0%)
            Column(
                modifier = Modifier
                    .height(190.dp)
                    .width(36.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                listOf("100%", "75%", "50%", "25%", "0%").forEach { tick ->
                    Text(
                        text = tick,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // CartesianGrid + Grouped Bars Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(190.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .testTag("recharts_bar_canvas")
                        .pointerInput(metrics.size) {
                            detectTapGestures { tapOffset ->
                                if (metrics.isNotEmpty() && size.width > 0) {
                                    val slotWidth = size.width.toFloat() / metrics.size
                                    val tappedIdx = (tapOffset.x / slotWidth).toInt()
                                        .coerceIn(0, metrics.lastIndex)
                                    onSelectBarIndex(tappedIdx)
                                }
                            }
                        }
                ) {
                    val chartWidth = size.width
                    val chartHeight = size.height
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                    // Draw 5 horizontal CartesianGrid lines (0%, 25%, 50%, 75%, 100%)
                    for (step in 0..4) {
                        val y = chartHeight * (step / 4f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(chartWidth, y),
                            strokeWidth = if (step == 4) 2f else 1.dp.toPx(),
                            pathEffect = if (step == 4) null else dashEffect
                        )
                    }

                    if (metrics.isEmpty()) return@Canvas
                    val slotWidth = chartWidth / metrics.size
                    val barWidth = (slotWidth * 0.28f).coerceAtMost(24.dp.toPx())
                    val gapBetweenBars = (slotWidth * 0.06f).coerceAtLeast(3.dp.toPx())

                    metrics.forEachIndexed { idx, item ->
                        val slotLeft = idx * slotWidth
                        val slotCenter = slotLeft + slotWidth / 2f

                        // Highlight active Tooltip band like Recharts cursor
                        if (idx == selectedIndex) {
                            drawRoundRect(
                                color = highlightFill,
                                topLeft = Offset(slotLeft + 4f, 0f),
                                size = Size(slotWidth - 8f, chartHeight),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                        }

                        // Bar 1: Completion Percentage
                        val completionFrac = (item.completionPercentage / 100f).coerceIn(0.03f, 1f) * animProgress
                        val compBarHeight = chartHeight * completionFrac
                        val compBarLeft = slotCenter - barWidth - (gapBetweenBars / 2f)
                        drawRoundRect(
                            color = primaryBarColor,
                            topLeft = Offset(compBarLeft, chartHeight - compBarHeight),
                            size = Size(barWidth, compBarHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )

                        // Bar 2: Accuracy Percentage
                        val accuracyFrac = (item.accuracyPercentage / 100f).coerceIn(0.03f, 1f) * animProgress
                        val accBarHeight = chartHeight * accuracyFrac
                        val accBarLeft = slotCenter + (gapBetweenBars / 2f)
                        drawRoundRect(
                            color = secondaryBarColor,
                            topLeft = Offset(accBarLeft, chartHeight - accBarHeight),
                            size = Size(barWidth, accBarHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // XAxis Subject Labels + Completion Values Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 44.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            metrics.forEachIndexed { idx, item ->
                val isSelected = idx == selectedIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectBarIndex(idx) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${item.completionPercentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = if (language == AppLanguage.GUJARATI) item.shortLabelGu else item.shortLabelEn,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Recharts-style Concentric RadialBarChart visualizing subject completion rings
 * for Indian Polity, Geography, Gujarat History & Culture, Economy, and Science.
 */
@Composable
private fun RechartsResponsiveRadialChart(
    metrics: List<SubjectProgressMetric>,
    overallCompletion: Int,
    language: AppLanguage,
    selectedIndex: Int,
    animProgress: Float,
    onSelectSubjectIndex: (Int) -> Unit
) {
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(185.dp)
                .testTag("recharts_radial_canvas"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(185.dp)) {
                val strokeWidth = 10.dp.toPx()
                val ringSpacing = 4.dp.toPx()
                val maxRadius = (minOf(size.width, size.height) / 2f) - strokeWidth

                metrics.forEachIndexed { idx, metric ->
                    val ringRadius = maxRadius - idx * (strokeWidth + ringSpacing)
                    if (ringRadius > 16.dp.toPx()) {
                        val topLeft = Offset(
                            (size.width / 2f) - ringRadius,
                            (size.height / 2f) - ringRadius
                        )
                        val arcSize = Size(ringRadius * 2f, ringRadius * 2f)
                        val color = SubjectChartPalette[idx % SubjectChartPalette.size]

                        // Background ring track
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Foreground progress arc
                        val sweep = (metric.completionPercentage / 100f).coerceIn(0.04f, 1f) * 360f * animProgress
                        drawArc(
                            color = color,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = if (idx == selectedIndex) strokeWidth + 2.dp.toPx() else strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$overallCompletion%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (language == AppLanguage.GUJARATI) "કુલ પ્રગતિ" else "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Interactive Radial Legend Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            metrics.forEachIndexed { idx, metric ->
                val ringColor = SubjectChartPalette[idx % SubjectChartPalette.size]
                val isSelected = idx == selectedIndex
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectSubjectIndex(idx) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) {
                        ringColor.copy(alpha = 0.14f)
                    } else {
                        Color.Transparent
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ringColor)
                            )
                            Text(
                                text = if (language == AppLanguage.GUJARATI) metric.shortLabelGu else metric.shortLabelEn,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${metric.completionPercentage}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ringColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Recharts-style AreaChart plotting Completion % and Accuracy % curves with shaded gradient areas
 * across all 5 exam subjects.
 */
@Composable
private fun RechartsResponsiveAreaChart(
    metrics: List<SubjectProgressMetric>,
    language: AppLanguage,
    selectedIndex: Int,
    animProgress: Float,
    onSelectPointIndex: (Int) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("recharts_area_canvas")
                .pointerInput(metrics.size) {
                    detectTapGestures { tapOffset ->
                        if (metrics.isNotEmpty() && size.width > 0) {
                            val slotWidth = size.width.toFloat() / metrics.size
                            val idx = (tapOffset.x / slotWidth).toInt().coerceIn(0, metrics.lastIndex)
                            onSelectPointIndex(idx)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

            for (step in 0..4) {
                val y = height * (step / 4f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = if (step == 4) null else dashEffect
                )
            }

            if (metrics.size < 2) return@Canvas
            val stepX = width / metrics.size
            val halfSlot = stepX / 2f

            val completionPoints = metrics.mapIndexed { idx, m ->
                val x = idx * stepX + halfSlot
                val y = height - (height * (m.completionPercentage / 100f).coerceIn(0.04f, 0.96f) * animProgress)
                Offset(x, y)
            }

            val accuracyPoints = metrics.mapIndexed { idx, m ->
                val x = idx * stepX + halfSlot
                val y = height - (height * (m.accuracyPercentage / 100f).coerceIn(0.04f, 0.96f) * animProgress)
                Offset(x, y)
            }

            // Completion Area Path
            val compAreaPath = Path().apply {
                moveTo(completionPoints.first().x, height)
                completionPoints.forEach { pt -> lineTo(pt.x, pt.y) }
                lineTo(completionPoints.last().x, height)
                close()
            }
            drawPath(
                path = compAreaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.35f), primaryColor.copy(alpha = 0.02f))
                )
            )

            // Completion Line Path
            val compLinePath = Path().apply {
                moveTo(completionPoints.first().x, completionPoints.first().y)
                completionPoints.drop(1).forEach { pt -> lineTo(pt.x, pt.y) }
            }
            drawPath(
                path = compLinePath,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Accuracy Line Path
            val accLinePath = Path().apply {
                moveTo(accuracyPoints.first().x, accuracyPoints.first().y)
                accuracyPoints.drop(1).forEach { pt -> lineTo(pt.x, pt.y) }
            }
            drawPath(
                path = accLinePath,
                color = secondaryColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Data Points & Selected Cursor Line
            completionPoints.forEachIndexed { idx, pt ->
                if (idx == selectedIndex) {
                    drawLine(
                        color = primaryColor.copy(alpha = 0.4f),
                        start = Offset(pt.x, 0f),
                        end = Offset(pt.x, height),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }
                drawCircle(
                    color = primaryColor,
                    radius = if (idx == selectedIndex) 6.dp.toPx() else 4.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = secondaryColor,
                    radius = if (idx == selectedIndex) 5.5.dp.toPx() else 3.5.dp.toPx(),
                    center = accuracyPoints[idx]
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            metrics.forEachIndexed { idx, item ->
                val isSelected = idx == selectedIndex
                Text(
                    text = if (language == AppLanguage.GUJARATI) item.shortLabelGu else item.shortLabelEn,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectPointIndex(idx) },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun InspectedSubjectTooltipCard(
    metric: SubjectProgressMetric,
    language: AppLanguage,
    accentColor: Color,
    onPracticeSubject: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recharts_active_tooltip_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (language == AppLanguage.GUJARATI) {
                        "${metric.subject.titleGu} (${metric.subject.code})"
                    } else {
                        "${metric.subject.titleEn} (${metric.subject.code})"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.GUJARATI) {
                        "પૂર્ણતા: ${metric.completionPercentage}% • ચોકસાઈ: ${metric.accuracyPercentage}% • MCQ: ${metric.attemptedQuestions}/${metric.totalQuestions} • અભ્યાસક્રમ: ${metric.syllabusTopicsCompleted}/${metric.syllabusTopicsTotal}"
                    } else {
                        "Completion: ${metric.completionPercentage}% • Accuracy: ${metric.accuracyPercentage}% • MCQs: ${metric.attemptedQuestions}/${metric.totalQuestions} • Syllabus: ${metric.syllabusTopicsCompleted}/${metric.syllabusTopicsTotal}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onPracticeSubject,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier.testTag("tooltip_practice_${metric.subject.name}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (language == AppLanguage.GUJARATI) "પ્રેક્ટિસ" else "Practice",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun SubjectCompletionRow(
    metric: SubjectProgressMetric,
    language: AppLanguage,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("subject_progress_row_${metric.subject.name}"),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) {
            accentColor.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = if (isSelected) BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        text = if (language == AppLanguage.GUJARATI) metric.subject.titleGu else metric.subject.titleEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "ચોકસાઈ: ${metric.accuracyPercentage}%"
                        } else {
                            "Acc: ${metric.accuracyPercentage}%"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (metric.completionPercentage >= 100) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = "${metric.completionPercentage}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (metric.completionPercentage / 100f).coerceIn(0f, 1f) },
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (language == AppLanguage.GUJARATI) {
                        "MCQ પ્રેક્ટિસ: ${metric.attemptedQuestions}/${metric.totalQuestions} (સાચા: ${metric.correctQuestions})"
                    } else {
                        "MCQs Practiced: ${metric.attemptedQuestions}/${metric.totalQuestions} (Correct: ${metric.correctQuestions})"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (language == AppLanguage.GUJARATI) {
                        "અભ્યાસક્રમ મુદ્દા: ${metric.syllabusTopicsCompleted}/${metric.syllabusTopicsTotal}"
                    } else {
                        "Syllabus Topics: ${metric.syllabusTopicsCompleted}/${metric.syllabusTopicsTotal}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
