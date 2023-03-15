package com.atguigu.myownmusicapp.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.atguigu.myownmusicapp.constants.MusicSource

@Dao
interface PlayQueueDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(playQueueData: List<PlayQueueData>)

    @Query("DELETE FROM playQueueData WHERE source=:source and name=:name and artists=:artists")
    suspend fun delete(source: MusicSource,name:String,artists:String)

    @Update
    suspend fun update(playQueueData: PlayQueueData)

    @Query("UPDATE playQueueData SET playOrder=playOrder-1 WHERE playOrder>=:deletePosition")
    suspend fun updateAfterDelete(deletePosition: Int)

    @Query("UPDATE playQueueData SET playOrder=playOrder+1 WHERE playOrder>=:insertPosition")
    suspend fun updateBeforeInsert(insertPosition:Int)

    /**
     * 更新某一个元素的位置
     * @param source MusicSource
     * @param name String
     * @param artists String
     * @param newPosition Int
     */
    @Query("UPDATE playQueueData SET playOrder=:newPosition WHERE source=:source and name=:name and artists=:artists ")
    suspend fun updateCurrentPosition(source: MusicSource,name:String,artists:String,newPosition:Int)

    @Query("DELETE FROM playQueueData")
    suspend fun deleteAll()

    //查询的时候按照播放顺序查找，升序排列
    @Query("SELECT * FROM playQueueData ORDER BY playOrder ASC")
    fun getAll(): LiveData<List<PlayQueueData>>

    @Query("SELECT EXISTS (SELECT 1 FROM playQueueData WHERE source=:source AND name=:name AND artists=:artists)")
    fun exists(source: MusicSource,name:String,artists: String):Boolean
}