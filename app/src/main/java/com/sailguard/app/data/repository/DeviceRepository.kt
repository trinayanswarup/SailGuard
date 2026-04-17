package com.sailguard.app.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import com.sailguard.app.data.model.DeviceStatus
import com.sailguard.app.data.model.NetworkStrength
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine

class DeviceRepository(private val context: Context) {

    fun batteryFlow(): Flow<Pair<Int, Boolean>> = callbackFlow {
        fun extract(intent: Intent): Pair<Int, Boolean> {
            val level  = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale  = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val pct    = if (level >= 0 && scale > 0) level * 100 / scale else 50
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                           status == BatteryManager.BATTERY_STATUS_FULL
            return Pair(pct, charging)
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) { trySend(extract(intent)) }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val sticky = context.registerReceiver(receiver, filter)
        sticky?.let { trySend(extract(it)) }
        awaitClose { context.unregisterReceiver(receiver) }
    }

    fun networkFlow(): Flow<Pair<NetworkStrength, String>> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun currentStatus(): Pair<NetworkStrength, String> {
            val net  = cm.activeNetwork ?: return Pair(NetworkStrength.NONE, "No Connection")
            val caps = cm.getNetworkCapabilities(net) ?: return Pair(NetworkStrength.NONE, "No Connection")
            return when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                    Pair(NetworkStrength.STRONG, "WiFi")
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    val bw = caps.linkDownstreamBandwidthKbps
                    when {
                        bw >= 10_000 -> Pair(NetworkStrength.STRONG,   "4G/LTE")
                        bw >= 2_000  -> Pair(NetworkStrength.MODERATE, "3G")
                        bw > 0       -> Pair(NetworkStrength.WEAK,     "2G")
                        else         -> Pair(NetworkStrength.MODERATE, "Cellular")
                    }
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                    Pair(NetworkStrength.STRONG, "Ethernet")
                else -> Pair(NetworkStrength.MODERATE, "Connected")
            }
        }

        trySend(currentStatus())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network)                               { trySend(currentStatus()) }
            override fun onLost(network: Network)                                    { trySend(Pair(NetworkStrength.NONE, "No Connection")) }
            override fun onCapabilitiesChanged(n: Network, c: NetworkCapabilities)  { trySend(currentStatus()) }
        }
        val request = NetworkRequest.Builder().build()
        cm.registerNetworkCallback(request, callback)
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }

    fun deviceStatusFlow(): Flow<DeviceStatus> =
        combine(batteryFlow(), networkFlow()) { (battery, charging), (strength, type) ->
            DeviceStatus(
                batteryLevel    = battery,
                isCharging      = charging,
                networkStrength = strength,
                networkType     = type
            )
        }
}
