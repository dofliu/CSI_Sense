package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CsiActionLog::class], version = 1, exportSchema = false)
abstract class CsiDatabase : RoomDatabase() {
    abstract fun csiActionLogDao(): CsiActionLogDao

    companion object {
        @Volatile
        private var INSTANCE: CsiDatabase? = null

        fun getDatabase(context: Context): CsiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CsiDatabase::class.java,
                    "csi_sense_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
