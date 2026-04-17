package com.sailguard.app.data.repository

import android.content.Context
import com.sailguard.app.data.model.TripRecord
import org.json.JSONArray
import org.json.JSONObject

object TripHistoryRepository {

    private const val PREFS_NAME  = "sailguard_trip_history"
    private const val KEY_HISTORY = "history"

    fun saveTrip(context: Context, record: TripRecord) {
        val prefs   = prefs(context)
        val arr     = loadArray(prefs.getString(KEY_HISTORY, "[]"))
        val obj     = JSONObject().apply {
            put("destination",   record.destination)
            put("durationDays",  record.durationDays)
            put("planGb",        record.planGb)
            put("actualGb",      record.actualGb)
            put("planWasEnough", record.planWasEnough)
        }
        arr.put(obj)
        prefs.edit().putString(KEY_HISTORY, arr.toString()).apply()
    }

    fun getHistory(context: Context): List<TripRecord> {
        val arr = loadArray(prefs(context).getString(KEY_HISTORY, "[]"))
        return (0 until arr.length()).mapNotNull { i ->
            runCatching {
                val o = arr.getJSONObject(i)
                TripRecord(
                    destination   = o.getString("destination"),
                    durationDays  = o.getInt("durationDays"),
                    planGb        = o.getDouble("planGb"),
                    actualGb      = o.getDouble("actualGb"),
                    planWasEnough = o.getBoolean("planWasEnough")
                )
            }.getOrNull()
        }
    }

    /**
     * Average actual GB used per day across all recorded trips.
     * Returns null when there is no history yet.
     */
    fun averageDailyGb(context: Context): Double? {
        val history = getHistory(context)
        if (history.isEmpty()) return null
        return history.sumOf { it.actualGb / it.durationDays.coerceAtLeast(1) } / history.size
    }

    // ─────────────────────────────────────────────────────────────────────────

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun loadArray(json: String?): JSONArray =
        runCatching { JSONArray(json ?: "[]") }.getOrDefault(JSONArray())
}
