package com.atguigu.myownmusicapp.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.atguigu.myownmusicapp.constants.MusicSource

@Dao
interface MyFavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(myFavoriteList: MutableList<MyFavoriteData>)

    @Query("DELETE FROM MyFavoriteData WHERE source=:source and name=:name and artists=:artists")
    suspend fun delete(source: MusicSource,name:String,artists:String)

    @Update
    suspend fun update(myFavoriteData: MyFavoriteData)

    @Query("DELETE FROM MyFavoriteData")
    suspend fun deleteAll()

    @Query("SELECT * FROM MyFavoriteData")
    fun getAll(): LiveData<List<MyFavoriteData>>

    @Query("SELECT EXISTS (SELECT 1 FROM MyFavoriteData WHERE source=:source AND name=:name AND artists=:artists)")
    fun exists(source: MusicSource,name:String,artists: String):Boolean
}
