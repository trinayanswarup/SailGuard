package com.sailguard.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UsageSliders(
    val videoStreaming: Float = 0f,  // 0-1 → 0-4 hrs/day  @ 0.70 GB/hr
    val maps:          Float = 0f,  // 0-1 → 0-4 hrs/day  @ 0.02 GB/hr
    val videoCalls:    Float = 0f,  // 0-1 → 0-2 hrs/day  @ 0.30 GB/hr
    val socialMedia:   Float = 0f,  // 0-1 → 0-4 hrs/day  @ 0.10 GB/hr
    val hotspot:       Float = 0f   // 0-1 → 0-4 hrs/day  @ 0.50 GB/hr
) {
    val dailyGb: Double get() =
        videoStreaming * 4 * 0.70 +
        maps          * 4 * 0.02 +
        videoCalls    * 2 * 0.30 +
        socialMedia   * 4 * 0.10 +
        hotspot       * 4 * 0.50

    /** Hours per day for each activity label. */
    fun videoStreamingHrs() = videoStreaming * 4f
    fun mapsHrs()           = maps          * 4f
    fun videoCallsHrs()     = videoCalls    * 2f
    fun socialMediaHrs()    = socialMedia   * 4f
    fun hotspotHrs()        = hotspot       * 4f
}

class UsageViewModel : ViewModel() {

    private val _sliders = MutableStateFlow(UsageSliders())
    val sliders: StateFlow<UsageSliders> = _sliders.asStateFlow()

    fun setVideoStreaming(v: Float) { _sliders.value = _sliders.value.copy(videoStreaming = v) }
    fun setMaps(v: Float)          { _sliders.value = _sliders.value.copy(maps = v) }
    fun setVideoCalls(v: Float)    { _sliders.value = _sliders.value.copy(videoCalls = v) }
    fun setSocialMedia(v: Float)   { _sliders.value = _sliders.value.copy(socialMedia = v) }
    fun setHotspot(v: Float)       { _sliders.value = _sliders.value.copy(hotspot = v) }
}
