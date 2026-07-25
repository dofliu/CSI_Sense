package com.example.model

data class CsiFrame(
    val timestamp: Long = System.currentTimeMillis(),
    val sequenceNumber: Long = 0,
    val subcarrierAmplitudes: FloatArray = FloatArray(64) { 0f },
    val subcarrierPhases: FloatArray = FloatArray(64) { 0f },
    val rssi: Int = -55,
    val snr: Float = 28f,
    val dopplerVelocity: Float = 0f,
    val csiVariance: Float = 0.5f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CsiFrame

        if (timestamp != other.timestamp) return false
        if (sequenceNumber != other.sequenceNumber) return false
        if (!subcarrierAmplitudes.contentEquals(other.subcarrierAmplitudes)) return false
        if (!subcarrierPhases.contentEquals(other.subcarrierPhases)) return false
        if (rssi != other.rssi) return false
        if (snr != other.snr) return false
        if (dopplerVelocity != other.dopplerVelocity) return false
        if (csiVariance != other.csiVariance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + sequenceNumber.hashCode()
        result = 31 * result + subcarrierAmplitudes.contentHashCode()
        result = 31 * result + subcarrierPhases.contentHashCode()
        result = 31 * result + rssi
        result = 31 * result + snr.hashCode()
        result = 31 * result + dopplerVelocity.hashCode()
        result = 31 * result + csiVariance.hashCode()
        return result
    }
}

data class Esp32Config(
    val isTriggerEnabled: Boolean = true,
    val samplingRateHz: Int = 100,
    val subcarrierCount: Int = 64,
    val triggerThresholdDbm: Float = -65f,
    val esp32Ip: String = "192.168.43.101",
    val esp32Port: Int = 8888,
    val activeChannel: Int = 6,
    val noiseFilterLevel: String = "Hampel Filter",
    val hotspotSsid: String = "AndroidHotspot_CSI",
    val isHotspotActive: Boolean = true,
    val connectedClients: Int = 1
)

data class PyTorchBackendConfig(
    val serverIp: String = "192.168.43.100",
    val serverPort: Int = 5000,
    val protocol: String = "UDP / Socket",
    val isConnected: Boolean = true,
    val modelName: String = "CSI-ResNet3D-v2",
    val inferenceThreshold: Float = 0.70f,
    val enableVibrationAlert: Boolean = true,
    val enablePopUpAlert: Boolean = true,
    val avgLatencyMs: Long = 18
)

data class InferenceResult(
    val actionName: String = "Sitting / Calm",
    val confidence: Float = 0.92f,
    val timestamp: Long = System.currentTimeMillis(),
    val isAlert: Boolean = false,
    val alertType: String? = null,
    val csiEnergy: Float = 1.2f,
    val dopplerShift: Float = 0.15f,
    val latencyMs: Long = 16
)
