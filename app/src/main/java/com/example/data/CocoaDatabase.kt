package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.CocoaSurvey
import com.example.model.TreeSampleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [CocoaSurvey::class], version = 2, exportSchema = false)
abstract class CocoaDatabase : RoomDatabase() {
    abstract fun cocoaSurveyDao(): CocoaSurveyDao

    companion object {
        @Volatile
        private var INSTANCE: CocoaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CocoaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CocoaDatabase::class.java,
                    "cocoa_yield_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
