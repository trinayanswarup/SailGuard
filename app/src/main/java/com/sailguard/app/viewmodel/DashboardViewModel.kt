package com.sailguard.app.viewmodel

import android.app.Application
import android.net.TrafficStats
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sailguard.app.data.model.Alert
import com.sailguard.app.data.model.AlertSeverity
import com.sailguard.app.data.model.DeviceStatus
import com.sailguard.app.data.model.TripConfig
import com.sailguard.app.data.model.TripRecord
import com.sailguard.app.data.repository.DeviceRepository
import com.sailguard.app.data.repository.TripHistoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max

enum class RiskLevel(val label: String) {
    SAFE("Safe"), WARNING("Warning"), CRITICAL("Critical")
}

data class DashboardState(
    val trip:           TripConfig?  = null,
    val deviceStatus:   DeviceStatus = DeviceStatus(),
    val usedGb:         Double       = 0.0,
    /** Epoch millis when the trip started; 0 means no trip active. */
    val tripStartTimeMs: Long        = 0L,
    val riskLevel:      RiskLevel    = RiskLevel.SAFE,
    val dashAlerts:     List<Alert>  = emptyList()
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val deviceRepo = DeviceRepository(app)

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    /** Bytes recorded at trip start; -1 means TrafficStats is unsupported. */
    private var baselineBytes: Long = 0L
    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            deviceRepo.deviceStatusFlow().collect { ds ->
                _state.value = _state.value.copy(deviceStatus = ds)
            }
        }
    }

    // ── Trip binding ──────────────────────────────────────────────────────────

    /**
     * Bind a new trip. Captures a TrafficStats baseline and starts the 30-second
     * polling loop that keeps [DashboardState.usedGb] up to date.
     */
    fun bindTrip(trip: TripConfig) {
        pollingJob?.cancel()
        baselineBytes = readMobileBytes()
        val startTime = System.currentTimeMillis()
        _state.value = DashboardState(
            trip            = trip,
            deviceStatus    = _state.value.deviceStatus,
            usedGb          = 0.0,
            tripStartTimeMs = startTime,
            riskLevel       = RiskLevel.SAFE,
            dashAlerts      = emptyList()
        )
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(30_000L)
                refreshUsage()
            }
        }
    }

    /**
     * Saves the current trip to history then clears the dashboard.
     * Call this when the user explicitly ends a trip via the "End Trip" button.
     */
    fun saveAndEndTrip() {
        val s    = _state.value
        val trip = s.trip ?: run { clearTrip(); return }
        TripHistoryRepository.saveTrip(
            getApplication(),
            TripRecord(
                destination   = trip.destination,
                durationDays  = trip.durationDays,
                planGb        = trip.selectedPlan.dataGB,
                actualGb      = s.usedGb,
                planWasEnough = s.usedGb <= trip.selectedPlan.dataGB
            )
        )
        clearTrip()
    }

    /** Cancels polling and resets the dashboard. Call when the trip is ended. */
    fun clearTrip() {
        pollingJob?.cancel()
        pollingJob = null
        baselineBytes = 0L
        _state.value = DashboardState(deviceStatus = _state.value.deviceStatus)
    }

    // ── Derived helpers ───────────────────────────────────────────────────────

    fun remainingGb(): Double =
        max(0.0, (_state.value.trip?.selectedPlan?.dataGB ?: 0.0) - _state.value.usedGb)

    fun daysLeft(): Double =
        max(0.0, (_state.value.trip?.durationDays?.toDouble() ?: 0.0) - elapsedDays())

    /** Returns null until at least one polling tick has recorded non-zero usage. */
    fun burnRateGbPerDay(): Double? {
        val elapsed = elapsedDays()
        val used    = _state.value.usedGb
        return if (elapsed > 0.0 && used > 0.0) used / elapsed else null
    }

    fun daysUntilEmpty(): Double? {
        val burn = burnRateGbPerDay() ?: return null
        return if (burn > 0.0) remainingGb() / burn else null
    }

    fun usagePercent(): Float {
        val total = _state.value.trip?.selectedPlan?.dataGB ?: 1.0
        return (_state.value.usedGb / total).toFloat().coerceIn(0f, 1f)
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    /** Seconds since trip start expressed as fractional days. */
    private fun elapsedDays(): Double {
        val startMs = _state.value.tripStartTimeMs
        if (startMs == 0L) return 0.0
        return (System.currentTimeMillis() - startMs) / 86_400_000.0
    }

    /**
     * Returns total mobile bytes (RX + TX) from [TrafficStats], or -1 if the
     * device does not support mobile traffic statistics (e.g. Wi-Fi-only or emulator).
     */
    private fun readMobileBytes(): Long {
        val rx = TrafficStats.getMobileRxBytes()
        val tx = TrafficStats.getMobileTxBytes()
        return if (rx == -1L || tx == -1L) -1L else rx + tx
    }

    /** Called every 30 seconds by [pollingJob]. */
    private fun refreshUsage() {
        if (baselineBytes == -1L) return   // TrafficStats unsupported on this device
        val current = readMobileBytes()
        if (current == -1L) return
        val usedBytes = max(0L, current - baselineBytes)
        val usedGb    = usedBytes / 1_073_741_824.0  // exact binary GB
        _state.value = _state.value.copy(usedGb = usedGb)
        recomputeRisk()
        checkAndAddAlerts()
    }

    private fun recomputeRisk() {
        val total = _state.value.trip?.selectedPlan?.dataGB ?: return
        if (total <= 0.0) return
        val remainingFraction = remainingGb() / total
        val risk = when {
            remainingFraction <= 0.25 -> RiskLevel.CRITICAL  // < 25% remaining
            remainingFraction <= 0.50 -> RiskLevel.WARNING   // 25–50% remaining
            else                      -> RiskLevel.SAFE      // > 50% remaining
        }
        _state.value = _state.value.copy(riskLevel = risk)
    }

    private fun checkAndAddAlerts() {
        val s         = _state.value
        val trip      = s.trip ?: return
        if (s.usedGb <= 0.0) return         // no data recorded yet
        val pct       = usagePercent()
        val daysUntil = daysUntilEmpty()
        val existing  = s.dashAlerts.map { it.title }
        val newAlerts = s.dashAlerts.toMutableList()

        if (pct >= 0.8f && "80% Data Used" !in existing) {
            newAlerts += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "80% Data Used",
                message  = "You have used 80% of your ${trip.selectedPlan.dataGB}GB plan. Monitor usage closely.",
                severity = AlertSeverity.WARNING
            )
        }
        if (pct >= 0.5f && pct < 0.8f && "50% Data Used" !in existing) {
            newAlerts += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "50% Data Used",
                message  = "Halfway through your data plan. Burn rate: ${"%.2f".format(burnRateGbPerDay())} GB/day.",
                severity = AlertSeverity.INFO
            )
        }
        if (daysUntil != null && daysUntil < daysLeft() && daysUntil < 3.0
            && "Plan Running Out Soon" !in existing) {
            newAlerts += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "Plan Running Out Soon",
                message  = "At your current pace, your plan may run out in " +
                           "${"%.1f".format(daysUntil)} days but ${daysLeft().toInt()} trip days remain.",
                severity = AlertSeverity.CRITICAL
            )
        }
        _state.value = s.copy(dashAlerts = newAlerts)
    }
}
