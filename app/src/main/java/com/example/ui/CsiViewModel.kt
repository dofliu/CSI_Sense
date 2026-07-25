package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CsiActionLog
import com.example.data.CsiRepository
import com.example.model.CsiFrame
import com.example.model.Esp32Config
import com.example.model.InferenceResult
import com.example.model.PyTorchBackendConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CsiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CsiRepository(application)

    val esp32Config: StateFlow<Esp32Config> = repository.esp32Config
    val pyTorchConfig: StateFlow<PyTorchBackendConfig> = repository.pyTorchConfig
    val currentFrame: StateFlow<CsiFrame> = repository.currentFrame
    val recentFrames: StateFlow<List<CsiFrame>> = repository.recentFrames
    val latestInference: StateFlow<InferenceResult> = repository.latestInference
    val isStreaming: StateFlow<Boolean> = repository.isStreaming
    val alertEventFlow: SharedFlow<InferenceResult> = repository.alertEventFlow
    val lastPingStatus: StateFlow<String?> = repository.lastPingStatus

    val allActionLogs: Flow<List<CsiActionLog>> = repository.allActionLogs
    val alertActionLogs: Flow<List<CsiActionLog>> = repository.alertActionLogs

    fun toggleStreaming() {
        repository.toggleStreaming()
    }

    fun setEsp32TriggerEnabled(enabled: Boolean) {
        repository.setEsp32TriggerEnabled(enabled)
    }

    fun updateSamplingRate(hz: Int) {
        repository.updateSamplingRate(hz)
    }

    fun updateSubcarrierCount(count: Int) {
        repository.updateSubcarrierCount(count)
    }

    fun updateTriggerThreshold(thresholdDbm: Float) {
        repository.updateTriggerThreshold(thresholdDbm)
    }

    fun updateEsp32Network(ip: String, port: Int) {
        repository.updateEsp32Network(ip, port)
    }

    fun updateFilterMode(mode: String) {
        repository.updateFilterMode(mode)
    }

    fun updatePyTorchBackend(ip: String, port: Int, protocol: String) {
        repository.updatePyTorchBackend(ip, port, protocol)
    }

    fun updateInferenceThreshold(threshold: Float) {
        repository.updateInferenceThreshold(threshold)
    }

    fun setAlertVibration(enabled: Boolean) {
        repository.setAlertVibration(enabled)
    }

    fun setPopUpAlert(enabled: Boolean) {
        repository.setPopUpAlert(enabled)
    }

    fun sendEsp32TriggerPing() {
        viewModelScope.launch {
            repository.sendEsp32TriggerPing()
        }
    }

    fun testServerConnection() {
        viewModelScope.launch {
            repository.testServerConnection()
        }
    }

    fun simulateBehavior(actionName: String) {
        repository.simulateBehavior(actionName)
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }
}
