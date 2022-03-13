package com.example.roomtest

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Word::class], version=WordRoomDatabase.DATABASE_VERSION, exportSchema = false)
abstract class WordRoomDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {

        // Any writes to this field are immediately made visible to other threads
        @Volatile
        private var INSTANCE: WordRoomDatabase? = null

        private const val DATABASE_NAME = "word_database"
        const val DATABASE_VERSION = 1

        fun getDatabase(context: Context): WordRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordRoomDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}