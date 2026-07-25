package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "csi_action_logs")
data class CsiActionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionName: String,
    val confidence: Float,
    val signalRssi: Int,
    val csiVariance: Float,
    val isAlert: Boolean = false,
    val notes: String = ""
)
