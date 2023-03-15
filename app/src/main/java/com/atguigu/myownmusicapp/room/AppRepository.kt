package com.atguigu.myownmusicapp.room

import android.content.Context
import androidx.lifecycle.LiveData
import com.atguigu.myownmusicapp.constants.MusicSource

class AppRepository(context:Context) {
    private var appDatabase:AppDatabase= AppDatabase.getDatabase(context)
    private var playQueueDao:PlayQueueDao=appDatabase.getPlayQueueDao()
    private var myFavoriteDao:MyFavoriteDao=appDatabase.getMyFavoriteDao()

    //以下是调用PlayQueueDao中的方法
    suspend fun insertQueueItem(playQueueData: List<PlayQueueData>){
        playQueueDao.insert(playQueueData)
    }
    suspend fun updateQueueItem(playQueueData: PlayQueueData){
        playQueueDao.update(playQueueData)
    }
    suspend fun updateBeforeInsert(insertPosition:Int){
        playQueueDao.updateBeforeInsert(insertPosition)
    }
    suspend fun updateAfterDelete(deletePosition:Int){
        playQueueDao.updateAfterDelete(deletePosition)
    }
    suspend fun updateCurrentPosition(source: MusicSource,name:String,artists:String,newPosition:Int){
        playQueueDao.updateCurrentPosition(source, name, artists, newPosition)
    }
    suspend fun deleteQueueItem(source:MusicSource,name:String,artists:String){
        playQueueDao.delete(source,name,artists)
    }
    suspend fun deleteAllQueueItem(){
        playQueueDao.deleteAll()
    }
    fun getAllQueueItem():LiveData<List<PlayQueueData>>{
        return playQueueDao.getAll()
    }
    fun existsInPlayQueue(source: MusicSource,name:String,artists: String):Boolean{
        return playQueueDao.exists(source,name, artists)
    }

    //以下是调用myFavoriteDao中的方法
    suspend fun insertMyFavorite(myFavoriteList: MutableList<MyFavoriteData>){
        myFavoriteDao.insert(myFavoriteList)
    }
    suspend fun deleteMyFavorite(source:MusicSource,name:String,artists:String){
        myFavoriteDao.delete(source,name,artists)
    }
    fun getMyFavorite():LiveData<List<MyFavoriteData>>{
        return myFavoriteDao.getAll()
    }
    fun existsInFavorite(source: MusicSource,name:String,artists: String):Boolean{
        return myFavoriteDao.exists(source,name, artists)
    }
}