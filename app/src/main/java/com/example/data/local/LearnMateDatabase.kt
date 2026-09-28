package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        StudentMemoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LearnMateDatabase : RoomDatabase() {
    abstract fun dao(): LearnMateDao

    companion object {
        @Volatile
        private var INSTANCE: LearnMateDatabase? = null

        fun getDatabase(context: Context): LearnMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LearnMateDatabase::class.java,
                    "learnmate_database"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
