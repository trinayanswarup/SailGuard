package com.sailguard.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sailguard.app.data.model.Alert
import com.sailguard.app.data.model.AlertSeverity
import com.sailguard.app.data.model.DeviceStatus
import com.sailguard.app.data.model.NetworkStrength
import com.sailguard.app.data.model.SmartRecommendation
import com.sailguard.app.data.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class SmartModeState(
    val deviceStatus:     DeviceStatus        = DeviceStatus(),
    val recommendation:   SmartRecommendation = SmartRecommendation.NORMAL,
    val reasons:          List<String>        = emptyList(),
    val isSmartModeOn:    Boolean             = true,
    val smartAlerts:      List<Alert>         = emptyList()
)

class SmartModeViewModel(app: Application) : AndroidViewModel(app) {

    private val deviceRepo = DeviceRepository(app)

    private val _state = MutableStateFlow(SmartModeState())
    val state: StateFlow<SmartModeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepo.deviceStatusFlow().collect { ds ->
                val (rec, reasons) = analyse(ds)
                val newAlerts      = maybeNewAlerts(ds, _state.value.smartAlerts)
                _state.value = _state.value.copy(
                    deviceStatus   = ds,
                    recommendation = rec,
                    reasons        = reasons,
                    smartAlerts    = newAlerts
                )
            }
        }
    }

    fun setSmartMode(on: Boolean) { _state.value = _state.value.copy(isSmartModeOn = on) }

    // ── Analysis logic ────────────────────────────────────────────────────────

    private fun analyse(ds: DeviceStatus): Pair<SmartRecommendation, List<String>> {
        val reasons = mutableListOf<String>()
        var rec     = SmartRecommendation.NORMAL

        // Battery checks
        when {
            ds.batteryLevel < 10 -> {
                reasons += "Battery critically low (${ds.batteryLevel}%)"
                rec = SmartRecommendation.CRITICAL
            }
            ds.batteryLevel < 20 && !ds.isCharging -> {
                reasons += "Battery below 20% (${ds.batteryLevel}%) — not charging"
                if (rec == SmartRecommendation.NORMAL) rec = SmartRecommendation.LIGHT_MODE
            }
            ds.batteryLevel < 35 && !ds.isCharging -> {
                reasons += "Battery at ${ds.batteryLevel}% — consider charging soon"
                if (rec == SmartRecommendation.NORMAL) rec = SmartRecommendation.REDUCE
            }
        }

        // Network checks
        when (ds.networkStrength) {
            NetworkStrength.NONE -> {
                reasons += "No network connection detected"
                if (rec == SmartRecommendation.NORMAL) rec = SmartRecommendation.REDUCE
            }
            NetworkStrength.WEAK -> {
                reasons += "Weak ${ds.networkType} signal — streaming may consume extra retries"
                if (rec == SmartRecommendation.NORMAL) rec = SmartRecommendation.REDUCE
                if (ds.batteryLevel < 20) rec = SmartRecommendation.LIGHT_MODE
            }
            NetworkStrength.MODERATE -> {
                reasons += "Moderate signal (${ds.networkType}) — avoid HD video"
            }
            NetworkStrength.STRONG -> { /* no issue */ }
        }

        if (ds.isCharging) reasons += "Charging — battery level stabilising"
        if (reasons.isEmpty()) reasons += "All systems nominal — enjoy your trip!"

        return Pair(rec, reasons)
    }

    private fun maybeNewAlerts(ds: DeviceStatus, existing: List<Alert>): List<Alert> {
        val titles = existing.map { it.title }.toSet()
        val acc    = existing.toMutableList()

        if (ds.batteryLevel < 20 && !ds.isCharging && "Low Battery" !in titles) {
            acc += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "Low Battery",
                message  = "Battery at ${ds.batteryLevel}%. Streaming and hotspot drain increases.",
                severity = AlertSeverity.WARNING
            )
        }
        if (ds.networkStrength == NetworkStrength.WEAK && "Weak Signal" !in titles) {
            acc += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "Weak Signal",
                message  = "Poor ${ds.networkType} signal detected. Data use may spike due to retransmissions.",
                severity = AlertSeverity.WARNING
            )
        }
        if (ds.batteryLevel < 15 && ds.networkStrength == NetworkStrength.WEAK
            && "Critical: Low Battery + Weak Signal" !in titles) {
            acc += Alert(
                id       = UUID.randomUUID().toString(),
                title    = "Critical: Low Battery + Weak Signal",
                message  = "Both battery (${ds.batteryLevel}%) and signal are weak. Switch to light profile immediately.",
                severity = AlertSeverity.CRITICAL
            )
        }
        return acc
    }
}
