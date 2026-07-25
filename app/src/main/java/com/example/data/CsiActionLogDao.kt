package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CsiActionLogDao {
    @Query("SELECT * FROM csi_action_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<CsiActionLog>>

    @Query("SELECT * FROM csi_action_logs WHERE isAlert = 1 ORDER BY timestamp DESC")
    fun getAlertLogs(): Flow<List<CsiActionLog>>

    @Query("SELECT * FROM csi_action_logs WHERE actionName = :actionName ORDER BY timestamp DESC")
    fun getLogsByAction(actionName: String): Flow<List<CsiActionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CsiActionLog): Long

    @Delete
    suspend fun deleteLog(log: CsiActionLog)

    @Query("DELETE FROM csi_action_logs")
    suspend fun clearAllLogs()
}
