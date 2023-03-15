package com.atguigu.myownmusicapp.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = 2,entities = [PlayQueueData::class,MyFavoriteData::class],exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun getPlayQueueDao():PlayQueueDao
    abstract fun getMyFavoriteDao():MyFavoriteDao
    companion object{
        //单例模式
        @Volatile
        private var INSTANCE:AppDatabase?=null

        fun getDatabase(context: Context):AppDatabase{
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "play_queue_database"
                ).allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}