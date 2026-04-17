package com.sailguard.app.viewmodel

import androidx.lifecycle.ViewModel
import com.sailguard.app.data.model.Alert
import com.sailguard.app.data.model.AlertSeverity
import com.sailguard.app.data.model.SailyPlan
import com.sailguard.app.data.model.TripConfig
import com.sailguard.app.data.model.UsageStyle
import com.sailguard.app.data.repository.PlanRepository
import com.sailguard.app.data.repository.Region
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class TripSetupState(
    val destination: String   = "",
    val flag: String          = "",
    val durationDays: Int     = 7,
    val usageStyle: UsageStyle = UsageStyle.MEDIUM,
    val suggestedPlan: SailyPlan? = null,
    val selectedPlan: SailyPlan?  = null,
    val availablePlans: List<SailyPlan> = emptyList(),
    val tripStarted: Boolean   = false,
    val activeTrip: TripConfig? = null,
    val selectedRegion: Region? = null
)

class TripViewModel : ViewModel() {

    private val _state = MutableStateFlow(TripSetupState())
    val state: StateFlow<TripSetupState> = _state.asStateFlow()

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    // ── Setup mutations ───────────────────────────────────────────────────────

    fun setDestination(country: String) {
        val plans     = PlanRepository.getPlansForCountry(country)
        val suggested = PlanRepository.suggestPlan(country, _state.value.durationDays, _state.value.usageStyle)
        val flag      = PlanRepository.flagForCountry(country)
        _state.value  = _state.value.copy(
            destination    = country,
            flag           = flag,
            selectedRegion = null,
            availablePlans = plans,
            suggestedPlan  = suggested,
            selectedPlan   = suggested
        )
    }

    fun setRegion(region: Region) {
        val plans     = PlanRepository.getRegionalPlans(region)
        val suggested = plans.filter { !it.isUnlimited }.minByOrNull { it.priceUSD }
        _state.value  = _state.value.copy(
            destination    = PlanRepository.regionDisplayName(region),
            flag           = region.emoji,
            selectedRegion = region,
            availablePlans = plans,
            suggestedPlan  = suggested,
            selectedPlan   = suggested
        )
    }

    fun setDuration(days: Int) {
        val s         = _state.value
        val suggested = if (s.selectedRegion != null)
            s.availablePlans.filter { !it.isUnlimited }.minByOrNull { it.priceUSD }
        else
            PlanRepository.suggestPlan(s.destination, days, s.usageStyle)
        _state.value  = s.copy(
            durationDays  = days,
            suggestedPlan = suggested,
            selectedPlan  = suggested
        )
    }

    fun setUsageStyle(style: UsageStyle) {
        val s         = _state.value
        val suggested = if (s.selectedRegion != null)
            s.availablePlans.filter { !it.isUnlimited }.minByOrNull { it.priceUSD }
        else
            PlanRepository.suggestPlan(s.destination, s.durationDays, style)
        _state.value  = s.copy(
            usageStyle    = style,
            suggestedPlan = suggested,
            selectedPlan  = suggested
        )
    }

    fun selectPlan(plan: SailyPlan) {
        _state.value = _state.value.copy(selectedPlan = plan)
    }

    // ── Trip lifecycle ────────────────────────────────────────────────────────

    fun startTrip() {
        val s    = _state.value
        val plan = s.selectedPlan ?: return
        val trip = TripConfig(
            destination  = s.destination,
            countryCode  = plan.countryCode,
            flag         = s.flag,
            durationDays = s.durationDays,
            usageStyle   = s.usageStyle,
            selectedPlan = plan
        )
        _state.value = _state.value.copy(tripStarted = true, activeTrip = trip)
        generateInitialAlerts(trip)
    }

    fun resetTrip() {
        _state.value  = TripSetupState()
        _alerts.value = emptyList()
    }

    // ── Alert helpers ─────────────────────────────────────────────────────────

    fun addAlert(alert: Alert) {
        _alerts.value = listOf(alert) + _alerts.value
    }

    private fun generateInitialAlerts(trip: TripConfig) {
        val needed   = trip.usageStyle.dailyGb * trip.durationDays
        val planGb   = trip.selectedPlan.dataGB
        val newAlerts = mutableListOf<Alert>()

        if (planGb < needed) {
            newAlerts += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "Plan May Be Insufficient",
                message  = "Your ${planGb}GB plan covers less than the " +
                           "${"%.1f".format(needed)}GB estimated for ${trip.durationDays} days " +
                           "of ${trip.usageStyle.label.lowercase()} usage.",
                severity = AlertSeverity.WARNING
            )
        } else {
            newAlerts += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "Trip Started",
                message  = "${trip.flag} ${trip.destination} · ${planGb}GB Saily plan active for ${trip.durationDays} days.",
                severity = AlertSeverity.INFO
            )
        }
        _alerts.value = newAlerts
    }
}
