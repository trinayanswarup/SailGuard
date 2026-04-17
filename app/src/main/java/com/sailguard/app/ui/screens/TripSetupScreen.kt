package com.sailguard.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sailguard.app.data.model.SailyPlan
import com.sailguard.app.data.model.UsageStyle
import com.sailguard.app.data.repository.PlanRepository
import com.sailguard.app.data.repository.TripHistoryRepository
import com.sailguard.app.ui.theme.AppBackground
import com.sailguard.app.ui.theme.AppSurface
import com.sailguard.app.ui.theme.AppSurface2
import com.sailguard.app.ui.theme.CardBorder
import com.sailguard.app.ui.theme.ErrorRed
import com.sailguard.app.ui.theme.SuccessGreen
import com.sailguard.app.ui.theme.TealPrimary
import com.sailguard.app.ui.theme.TextPrimary
import com.sailguard.app.ui.theme.TextSecondary
import com.sailguard.app.ui.theme.WarningAmber
import com.sailguard.app.viewmodel.TripViewModel
import com.sailguard.app.viewmodel.UsageSliders
import com.sailguard.app.viewmodel.UsageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSetupScreen(
    vm:           TripViewModel,
    usageVm:      UsageViewModel,
    onTripStarted: () -> Unit
) {
    val state   by vm.state.collectAsState()
    val sliders by usageVm.sliders.collectAsState()

    // Persists across recompositions but resets when the screen is recreated
    var step by rememberSaveable { mutableStateOf(1) }

    val canAdvance = when (step) {
        1    -> state.destination.isNotEmpty()
        else -> true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // ── Step indicator ────────────────────────────────────────────────────
        WizardStepBar(
            currentStep = step,
            modifier    = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        )

        // ── Animated step content ─────────────────────────────────────────────
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith
                            (slideOutHorizontally { it } + fadeOut())
                }
            },
            modifier = Modifier.weight(1f),
            label    = "wizard_step"
        ) { targetStep ->
            when (targetStep) {
                1    -> Step1Content(state, vm)
                2    -> Step2Content(sliders, usageVm, state.durationDays)
                else -> Step3Content(state, sliders, vm)
            }
        }

        // ── Navigation buttons ────────────────────────────────────────────────
        Surface(
            color           = AppSurface,
            shadowElevation = 8.dp,
            tonalElevation  = 0.dp
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick  = { step-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        border   = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text("← Back", color = TextPrimary,
                             fontWeight = FontWeight.Medium)
                    }
                }
                Button(
                    onClick  = {
                        if (step < 3) step++
                        else { vm.startTrip(); onTripStarted() }
                    },
                    enabled  = canAdvance,
                    modifier = Modifier
                        .weight(if (step > 1) 2f else 1f)
                        .height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = TealPrimary,
                        disabledContainerColor = AppSurface2
                    )
                ) {
                    Text(
                        text       = if (step < 3) "Next →" else "Start Trip",
                        color      = if (canAdvance) Color.White else TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 1 — Destination, Duration, Usage Style
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1Content(
    state: com.sailguard.app.viewmodel.TripSetupState,
    vm: TripViewModel
) {
    val countries = PlanRepository.countries.map { it.name }
    var queryText        by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val filteredCountries = remember(queryText) {
        if (queryText.isEmpty()) countries
        else countries.filter { it.contains(queryText, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column {
            Text("⛵ SailGuard",
                 style      = MaterialTheme.typography.titleSmall,
                 color      = TealPrimary,
                 fontWeight = FontWeight.Bold,
                 letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            Text("Where are you headed?",
                 style = MaterialTheme.typography.headlineSmall,
                 color = TextPrimary)
            Text("Select your destination, trip length, and usage style.",
                 style = MaterialTheme.typography.bodyMedium,
                 color = TextSecondary)
        }

        // Destination
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Destination")
            ExposedDropdownMenuBox(
                expanded         = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value         = if (state.destination.isNotEmpty() && queryText == state.destination)
                                        "${state.flag}  ${state.destination}"
                                    else queryText,
                    onValueChange = { queryText = it; dropdownExpanded = true },
                    placeholder   = { Text("Search destination…", color = TextSecondary) },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                    modifier      = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                        .fillMaxWidth(),
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedTextColor        = TextPrimary,
                        unfocusedTextColor      = TextPrimary,
                        focusedBorderColor      = TealPrimary,
                        unfocusedBorderColor    = CardBorder,
                        focusedContainerColor   = AppSurface,
                        unfocusedContainerColor = AppSurface
                    )
                )
                ExposedDropdownMenu(
                    expanded         = dropdownExpanded && filteredCountries.isNotEmpty(),
                    onDismissRequest = { dropdownExpanded = false },
                    modifier         = Modifier.background(AppSurface)
                ) {
                    filteredCountries.forEach { country ->
                        val flag = PlanRepository.flagForCountry(country)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(flag, fontSize = 20.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Text(country, color = TextPrimary,
                                         style = MaterialTheme.typography.bodyMedium)
                                }
                            },
                            onClick = {
                                vm.setDestination(country)
                                queryText        = country
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Duration
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Trip Duration")
            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                shape  = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick  = { if (state.durationDays > 1) vm.setDuration(state.durationDays - 1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = TealPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${state.durationDays}",
                             style      = MaterialTheme.typography.displaySmall,
                             color      = TextPrimary,
                             fontWeight = FontWeight.Bold)
                        Text("days", style = MaterialTheme.typography.bodySmall,
                             color = TextSecondary)
                    }
                    IconButton(
                        onClick  = { if (state.durationDays < 60) vm.setDuration(state.durationDays + 1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase", tint = TealPrimary)
                    }
                }
            }
        }

        // Usage Style
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Usage Style")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                UsageStyle.entries.forEach { style ->
                    val selected = state.usageStyle == style
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { vm.setUsageStyle(style) },
                        shape    = RoundedCornerShape(12.dp),
                        color    = if (selected) TealPrimary else AppSurface,
                        border   = if (selected) null else BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier            = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(style.label,
                                 style      = MaterialTheme.typography.titleSmall,
                                 color      = if (selected) Color.White else TextPrimary,
                                 fontWeight = FontWeight.SemiBold)
                            Text("${"%.0f".format(style.dailyGb * 1000)} MB/day",
                                 style = MaterialTheme.typography.labelSmall,
                                 color = if (selected) Color.White.copy(alpha = 0.85f) else TextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 2 — Usage Sliders
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Step2Content(
    sliders:  UsageSliders,
    usageVm:  UsageViewModel,
    tripDays: Int
) {
    val dailyGb = sliders.dailyGb
    val totalGb = dailyGb * tripDays

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column {
            Text("Fine-tune Your Usage",
                 style = MaterialTheme.typography.headlineSmall,
                 color = TextPrimary)
            Text("Drag sliders to reflect your daily habits on the road.",
                 style = MaterialTheme.typography.bodyMedium,
                 color = TextSecondary)
        }

        // Sliders card
        Card(
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            shape  = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                UsageSliderRow(
                    icon     = Icons.Filled.Movie,
                    label    = "Video Streaming",
                    subLabel = "HD, Netflix/YouTube",
                    value    = sliders.videoStreaming,
                    hours    = sliders.videoStreamingHrs(),
                    mbPerHr  = 700,
                    onChange = { usageVm.setVideoStreaming(it) }
                )
                UsageSliderRow(
                    icon     = Icons.Filled.MyLocation,
                    label    = "Maps & Navigation",
                    subLabel = "Google Maps, Waze",
                    value    = sliders.maps,
                    hours    = sliders.mapsHrs(),
                    mbPerHr  = 20,
                    onChange = { usageVm.setMaps(it) }
                )
                UsageSliderRow(
                    icon     = Icons.Filled.VideoCameraFront,
                    label    = "Video Calls",
                    subLabel = "FaceTime, WhatsApp",
                    value    = sliders.videoCalls,
                    hours    = sliders.videoCallsHrs(),
                    mbPerHr  = 300,
                    onChange = { usageVm.setVideoCalls(it) }
                )
                UsageSliderRow(
                    icon     = Icons.Filled.Share,
                    label    = "Social Media",
                    subLabel = "Instagram, TikTok",
                    value    = sliders.socialMedia,
                    hours    = sliders.socialMediaHrs(),
                    mbPerHr  = 100,
                    onChange = { usageVm.setSocialMedia(it) }
                )
                UsageSliderRow(
                    icon     = Icons.Filled.Wifi,
                    label    = "Mobile Hotspot",
                    subLabel = "Sharing data with devices",
                    value    = sliders.hotspot,
                    hours    = sliders.hotspotHrs(),
                    mbPerHr  = 500,
                    onChange = { usageVm.setHotspot(it) }
                )
            }
        }

        // Estimate summary
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EstimateCard(
                label    = "Daily Usage",
                value    = "${"%.2f".format(dailyGb)} GB",
                subValue = "${"%.0f".format(dailyGb * 1024)} MB/day",
                modifier = Modifier.weight(1f)
            )
            EstimateCard(
                label    = "Trip Total",
                value    = "${"%.1f".format(totalGb)} GB",
                subValue = "for $tripDays days",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 3 — Plan Confirmation
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Step3Content(
    state:   com.sailguard.app.viewmodel.TripSetupState,
    sliders: UsageSliders,
    vm:      TripViewModel
) {
    val context = LocalContext.current

    // Load history once when Step 3 is composed.
    val history = remember { TripHistoryRepository.getHistory(context) }

    // Historical average daily GB across past trips (null = no history).
    val historicalDailyGb: Double? = remember(history) {
        if (history.isEmpty()) null
        else history.sumOf { it.actualGb / it.durationDays.coerceAtLeast(1) } / history.size
    }

    // 50/50 blend of slider estimate and historical average.
    val blendedDailyGb: Double? = if (historicalDailyGb != null)
        (sliders.dailyGb + historicalDailyGb) / 2.0
    else null

    // Best plan covering blended estimate × duration × 1.2 buffer.
    val historyRecommendedPlan: SailyPlan? = if (blendedDailyGb != null) {
        val needed = blendedDailyGb * state.durationDays * 1.2
        state.availablePlans
            .filter { it.dataGB >= needed }
            .minByOrNull { it.priceUSD }
            ?: state.availablePlans.maxByOrNull { it.dataGB }
    } else null

    var showPlanOverride by remember { mutableStateOf(false) }
    val plan = state.selectedPlan ?: state.suggestedPlan

    val sliderTotalGb = sliders.dailyGb * state.durationDays

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text("Review Your Plan",
                 style = MaterialTheme.typography.headlineSmall,
                 color = TextPrimary)
            Text("${state.flag}  ${state.destination}  ·  ${state.durationDays} days",
                 style = MaterialTheme.typography.bodyMedium,
                 color = TextSecondary)
        }

        if (plan == null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                shape  = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Text("No plan available for this destination. Go back and try another.",
                     modifier = Modifier.padding(16.dp),
                     style    = MaterialTheme.typography.bodyMedium,
                     color    = TextSecondary)
            }
        } else {
            // ── History-blended recommendation card ───────────────────────────
            if (historyRecommendedPlan != null && historicalDailyGb != null && blendedDailyGb != null) {
                HistoryInsightCard(
                    historyCount       = history.size,
                    historicalDailyGb  = historicalDailyGb,
                    sliderDailyGb      = sliders.dailyGb,
                    blendedDailyGb     = blendedDailyGb,
                    durationDays       = state.durationDays,
                    recommendedGb      = historyRecommendedPlan.dataGB
                )
            }

            // Selected plan card
            SectionLabel("Recommended Plan")
            PlanCard(
                plan        = plan,
                isSuggested = plan.id == state.suggestedPlan?.id,
                needed      = state.usageStyle.dailyGb * state.durationDays
            )

            // Coverage check against sliders
            if (sliderTotalGb > 0.0) {
                val sufficient = plan.dataGB >= sliderTotalGb
                val bgColor    = if (sufficient) SuccessGreen.copy(0.08f) else WarningAmber.copy(0.08f)
                val accent     = if (sufficient) SuccessGreen else WarningAmber

                Card(
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier              = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(if (sufficient) "✓" else "⚠", fontSize = 18.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                if (sufficient) "Covers your estimated usage"
                                else "May not cover your estimated usage",
                                style      = MaterialTheme.typography.titleSmall,
                                color      = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Sliders estimate ${"%.1f".format(sliderTotalGb)} GB total" +
                                " — plan includes ${plan.dataGB.toLong()} GB",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Plan override
            Row(
                modifier          = Modifier
                    .clickable { showPlanOverride = !showPlanOverride }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (showPlanOverride) "Hide all plans" else "Choose a different plan",
                    style = MaterialTheme.typography.bodySmall,
                    color = TealPrimary
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector        = if (showPlanOverride) Icons.Filled.ExpandLess
                                         else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint               = TealPrimary,
                    modifier           = Modifier.size(16.dp)
                )
            }

            if (showPlanOverride) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.availablePlans.forEach { p ->
                        PlanOptionRow(
                            plan       = p,
                            isSelected = state.selectedPlan?.id == p.id,
                            needed     = state.usageStyle.dailyGb * state.durationDays,
                            onClick    = { vm.selectPlan(p) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HistoryInsightCard(
    historyCount:      Int,
    historicalDailyGb: Double,
    sliderDailyGb:     Double,
    blendedDailyGb:    Double,
    durationDays:      Int,
    recommendedGb:     Double
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.07f)),
        shape  = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.35f))
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Based on your usage history + current settings, we recommend " +
                "${recommendedGb.toLong()} GB",
                style      = MaterialTheme.typography.titleSmall,
                color      = TealPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                val tripWord = if (historyCount == 1) "trip" else "trips"
                InsightLine(
                    "Past $historyCount $tripWord averaged ${"%.2f".format(historicalDailyGb)} GB/day"
                )
                InsightLine(
                    "Slider estimate: ${"%.2f".format(sliderDailyGb)} GB/day"
                )
                InsightLine(
                    "50/50 blend: ${"%.2f".format(blendedDailyGb)} GB/day " +
                    "× $durationDays days × 1.2 buffer = " +
                    "${"%.1f".format(blendedDailyGb * durationDays * 1.2)} GB needed"
                )
            }
        }
    }
}

@Composable
private fun InsightLine(text: String) {
    Text(
        text  = "· $text",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Wizard step bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WizardStepBar(currentStep: Int, modifier: Modifier = Modifier) {
    val labels = listOf("Destination", "Usage", "Plan")
    Row(
        modifier          = modifier,
        verticalAlignment = Alignment.Top
    ) {
        labels.forEachIndexed { index, label ->
            val stepNum   = index + 1
            val isDone    = stepNum < currentStep
            val isCurrent = stepNum == currentStep

            // Circle + label stacked
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.width(56.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(28.dp)
                        .background(
                            color = if (isDone || isCurrent) TealPrimary else AppSurface,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Filled.Check, contentDescription = null,
                             tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text(
                            "$stepNum",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = if (isCurrent) Color.White else TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style      = MaterialTheme.typography.labelSmall,
                    color      = if (stepNum <= currentStep) TealPrimary else TextSecondary,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            // Connector line between circles (centered at circle height = 14dp from top)
            if (index < labels.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 13.dp)
                        .height(2.dp)
                        .background(if (currentStep > stepNum) TealPrimary else CardBorder)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelSmall,
        color         = TextSecondary,
        letterSpacing = 1.sp
    )
}

@Composable
private fun PlanCard(plan: SailyPlan, isSuggested: Boolean, needed: Double) {
    val isSufficient = plan.dataGB >= needed
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape  = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, if (isSuggested) TealPrimary else CardBorder)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("${plan.dataGB.toLong()} GB",
                         style      = MaterialTheme.typography.headlineSmall,
                         color      = TextPrimary,
                         fontWeight = FontWeight.Bold)
                    if (isSuggested) {
                        Surface(color = TealPrimary, shape = RoundedCornerShape(6.dp)) {
                            Text("BEST FIT",
                                 modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                 style      = MaterialTheme.typography.labelSmall,
                                 color      = Color.White,
                                 fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text("${plan.network}  ·  Valid ${plan.validDays} days",
                     style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                if (!isSufficient) {
                    Text("⚠ May not cover estimated usage",
                         style = MaterialTheme.typography.bodySmall, color = WarningAmber)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${"%.2f".format(plan.priceUSD)}",
                     style      = MaterialTheme.typography.titleLarge,
                     color      = TealPrimary,
                     fontWeight = FontWeight.Bold)
                Text("USD", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun PlanOptionRow(
    plan: SailyPlan,
    isSelected: Boolean,
    needed: Double,
    onClick: () -> Unit
) {
    val isSufficient = plan.dataGB >= needed
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape    = RoundedCornerShape(10.dp),
        color    = if (isSelected) TealPrimary.copy(alpha = 0.10f) else AppSurface,
        border   = BorderStroke(1.dp, if (isSelected) TealPrimary else CardBorder)
    ) {
        Row(
            modifier              = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = null,
                         tint = TealPrimary, modifier = Modifier.size(18.dp))
                } else {
                    Spacer(Modifier.size(18.dp))
                }
                Column {
                    Text("${plan.dataGB.toLong()} GB  ·  ${plan.validDays}d",
                         style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    if (!isSufficient) {
                        Text("Might run short",
                             style = MaterialTheme.typography.bodySmall, color = ErrorRed)
                    }
                }
            }
            Text("$${"%.2f".format(plan.priceUSD)}",
                 style      = MaterialTheme.typography.titleSmall,
                 color      = if (isSelected) TealPrimary else TextPrimary,
                 fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun UsageSliderRow(
    icon: ImageVector,
    label: String,
    subLabel: String,
    value: Float,
    hours: Float,
    mbPerHr: Int,
    onChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null,
                     tint = TealPrimary, modifier = Modifier.size(20.dp))
                Column {
                    Text(label, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(subLabel, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${"%.1f".format(hours)} hrs/day",
                     style      = MaterialTheme.typography.bodySmall,
                     color      = TealPrimary,
                     fontWeight = FontWeight.SemiBold)
                Text("~${"%.0f".format(hours * mbPerHr)} MB",
                     style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
        Slider(
            value         = value,
            onValueChange = onChange,
            colors        = SliderDefaults.colors(
                thumbColor         = TealPrimary,
                activeTrackColor   = TealPrimary,
                inactiveTrackColor = AppSurface2
            )
        )
    }
}

@Composable
private fun EstimateCard(
    label: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors   = CardDefaults.cardColors(containerColor = AppSurface),
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label.uppercase(),
                 style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value,
                 style      = MaterialTheme.typography.headlineSmall,
                 color      = TextPrimary,
                 fontWeight = FontWeight.Bold)
            Text(subValue,
                 style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
