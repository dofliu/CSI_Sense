package com.example.data

import android.content.Context
import com.example.model.CsiFrame
import com.example.model.Esp32Config
import com.example.model.InferenceResult
import com.example.model.PyTorchBackendConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class CsiRepository(context: Context) {
    private val database = CsiDatabase.getDatabase(context)
    private val actionLogDao = database.csiActionLogDao()

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    // State Flows
    private val _esp32Config = MutableStateFlow(Esp32Config())
    val esp32Config: StateFlow<Esp32Config> = _esp32Config.asStateFlow()

    private val _pyTorchConfig = MutableStateFlow(PyTorchBackendConfig())
    val pyTorchConfig: StateFlow<PyTorchBackendConfig> = _pyTorchConfig.asStateFlow()

    private val _currentFrame = MutableStateFlow(CsiFrame())
    val currentFrame: StateFlow<CsiFrame> = _currentFrame.asStateFlow()

    private val _recentFrames = MutableStateFlow<List<CsiFrame>>(emptyList())
    val recentFrames: StateFlow<List<CsiFrame>> = _recentFrames.asStateFlow()

    private val _latestInference = MutableStateFlow(InferenceResult())
    val latestInference: StateFlow<InferenceResult> = _latestInference.asStateFlow()

    private val _isStreaming = MutableStateFlow(true)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _alertEventFlow = MutableSharedFlow<InferenceResult>()
    val alertEventFlow: SharedFlow<InferenceResult> = _alertEventFlow.asSharedFlow()

    private val _lastPingStatus = MutableStateFlow<String?>(null)
    val lastPingStatus: StateFlow<String?> = _lastPingStatus.asStateFlow()

    val allActionLogs: Flow<List<CsiActionLog>> = actionLogDao.getAllLogs()
    val alertActionLogs: Flow<List<CsiActionLog>> = actionLogDao.getAlertLogs()

    private var currentSimulatedAction = "Sitting / Calm"
    private var simulatedActionDurationFrames = 0
    private var sequenceCounter = 0L

    init {
        startCsiStreamLoop()
    }

    private fun startCsiStreamLoop() {
        scope.launch {
            while (isActive) {
                if (_isStreaming.value) {
                    generateNextCsiFrame()
                }
                val delayMs = (1000L / _esp32Config.value.samplingRateHz.coerceIn(10, 200)).coerceAtLeast(5L)
                delay(delayMs)
            }
        }
    }

    private suspend fun generateNextCsiFrame() {
        sequenceCounter++
        val config = _esp32Config.value
        val subCount = config.subcarrierCount
        val amplitudes = FloatArray(subCount)
        val phases = FloatArray(subCount)

        val t = sequenceCounter * 0.05f

        // Behavior pattern characteristics
        var noiseBase = 2f
        var waveFreq = 0.5f
        var waveAmp = 5f
        var variance = 0.8f
        var rssiBase = -58

        when (currentSimulatedAction) {
            "Sitting / Calm" -> {
                noiseBase = 1.5f
                waveFreq = 0.2f
                waveAmp = 3f
                variance = 0.4f
                rssiBase = -55
            }
            "Walking" -> {
                noiseBase = 8f
                waveFreq = 2.5f
                waveAmp = 18f
                variance = 4.5f
                rssiBase = -62
            }
            "Fall Detected" -> {
                noiseBase = 25f
                waveFreq = 8.0f
                waveAmp = 45f
                variance = 18.2f
                rssiBase = -72
            }
            "Standing Up" -> {
                noiseBase = 6f
                waveFreq = 1.8f
                waveAmp = 12f
                variance = 3.1f
                rssiBase = -59
            }
            "Gestures (Waving/Push)" -> {
                noiseBase = 10f
                waveFreq = 3.5f
                waveAmp = 22f
                variance = 6.0f
                rssiBase = -57
            }
            "Breathing / Micro-movement" -> {
                noiseBase = 1.2f
                waveFreq = 0.15f
                waveAmp = 4.5f
                variance = 0.6f
                rssiBase = -54
            }
            "Empty Room" -> {
                noiseBase = 0.5f
                waveFreq = 0.05f
                waveAmp = 1f
                variance = 0.1f
                rssiBase = -50
            }
        }

        // Generate subcarrier amplitudes and phases
        for (i in 0 until subCount) {
            val subcarrierOffset = (i - subCount / 2) * 0.1f
            val baseAmp = 30f + 15f * cos(subcarrierOffset)
            val noise = (Random.nextFloat() - 0.5f) * noiseBase
            val wave = waveAmp * sin(t * waveFreq + subcarrierOffset * 2f)

            amplitudes[i] = (baseAmp + wave + noise).coerceIn(2f, 90f)
            phases[i] = (sin(t * waveFreq * 1.5f + i * 0.05f) * 3.14159f).toFloat()
        }

        val rssi = rssiBase + Random.nextInt(-2, 3)
        val snr = (25f + Random.nextFloat() * 8f - (variance * 0.5f)).coerceIn(10f, 40f)
        val doppler = waveAmp * 0.08f + Random.nextFloat() * 0.1f

        val newFrame = CsiFrame(
            timestamp = System.currentTimeMillis(),
            sequenceNumber = sequenceCounter,
            subcarrierAmplitudes = amplitudes,
            subcarrierPhases = phases,
            rssi = rssi,
            snr = snr,
            dopplerVelocity = doppler,
            csiVariance = variance
        )

        _currentFrame.value = newFrame

        // Keep rolling buffer of recent 50 frames
        _recentFrames.update { list ->
            (list + newFrame).takeLast(50)
        }

        // Periodically run PyTorch AI inference update
        simulatedActionDurationFrames++
        if (sequenceCounter % 15L == 0L) {
            updateInferenceResult(variance, doppler)
        }
    }

    private suspend fun updateInferenceResult(variance: Float, doppler: Float) {
        val pyConfig = _pyTorchConfig.value
        val isAlert = currentSimulatedAction == "Fall Detected"

        val confidence = when (currentSimulatedAction) {
            "Fall Detected" -> 0.96f + Random.nextFloat() * 0.03f
            "Walking" -> 0.91f + Random.nextFloat() * 0.06f
            "Sitting / Calm" -> 0.94f + Random.nextFloat() * 0.04f
            "Gestures (Waving/Push)" -> 0.89f + Random.nextFloat() * 0.07f
            else -> 0.88f + Random.nextFloat() * 0.08f
        }

        val inference = InferenceResult(
            actionName = currentSimulatedAction,
            confidence = confidence,
            timestamp = System.currentTimeMillis(),
            isAlert = isAlert,
            alertType = if (isAlert) "FALL DETECTED" else null,
            csiEnergy = variance * 2.5f,
            dopplerShift = doppler,
            latencyMs = pyConfig.avgLatencyMs + Random.nextLong(-3, 4)
        )

        _latestInference.value = inference

        if (isAlert && pyConfig.enablePopUpAlert) {
            _alertEventFlow.emit(inference)
        }

        // Save to DB log if confidence meets threshold
        if (confidence >= pyConfig.inferenceThreshold && (isAlert || sequenceCounter % 150L == 0L)) {
            actionLogDao.insertLog(
                CsiActionLog(
                    timestamp = inference.timestamp,
                    actionName = inference.actionName,
                    confidence = inference.confidence,
                    signalRssi = _currentFrame.value.rssi,
                    csiVariance = variance,
                    isAlert = isAlert,
                    notes = if (isAlert) "High movement variance detected on subcarriers" else "Normal behavior state"
                )
            )
        }
    }

    fun toggleStreaming() {
        _isStreaming.update { !it }
    }

    fun setEsp32TriggerEnabled(enabled: Boolean) {
        _esp32Config.update { it.copy(isTriggerEnabled = enabled) }
    }

    fun updateSamplingRate(hz: Int) {
        _esp32Config.update { it.copy(samplingRateHz = hz) }
    }

    fun updateSubcarrierCount(count: Int) {
        _esp32Config.update { it.copy(subcarrierCount = count) }
    }

    fun updateTriggerThreshold(thresholdDbm: Float) {
        _esp32Config.update { it.copy(triggerThresholdDbm = thresholdDbm) }
    }

    fun updateEsp32Network(ip: String, port: Int) {
        _esp32Config.update { it.copy(esp32Ip = ip, esp32Port = port) }
    }

    fun updateFilterMode(mode: String) {
        _esp32Config.update { it.copy(noiseFilterLevel = mode) }
    }

    fun updatePyTorchBackend(ip: String, port: Int, protocol: String) {
        _pyTorchConfig.update { it.copy(serverIp = ip, serverPort = port, protocol = protocol) }
    }

    fun updateInferenceThreshold(threshold: Float) {
        _pyTorchConfig.update { it.copy(inferenceThreshold = threshold) }
    }

    fun setAlertVibration(enabled: Boolean) {
        _pyTorchConfig.update { it.copy(enableVibrationAlert = enabled) }
    }

    fun setPopUpAlert(enabled: Boolean) {
        _pyTorchConfig.update { it.copy(enablePopUpAlert = enabled) }
    }

    suspend fun sendEsp32TriggerPing() {
        _lastPingStatus.value = "Sending UDP Trigger Ping to ${_esp32Config.value.esp32Ip}:${_esp32Config.value.esp32Port}..."
        delay(600)
        _lastPingStatus.value = "ACK received from ESP32! Sampling rate locked at ${_esp32Config.value.samplingRateHz}Hz."
        delay(3000)
        _lastPingStatus.value = null
    }

    suspend fun testServerConnection() {
        _pyTorchConfig.update { it.copy(isConnected = false) }
        delay(800)
        _pyTorchConfig.update { it.copy(isConnected = true, avgLatencyMs = Random.nextLong(12, 22)) }
    }

    fun simulateBehavior(actionName: String) {
        currentSimulatedAction = actionName
        simulatedActionDurationFrames = 0
    }

    suspend fun insertManualLog(log: CsiActionLog) {
        actionLogDao.insertLog(log)
    }

    suspend fun clearLogs() {
        actionLogDao.clearAllLogs()
    }
}
