package com.atguigu.myownmusicapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.atguigu.myownmusicapp.api.Retrofit163Instance
import com.atguigu.myownmusicapp.api.Retrofit163service
import com.atguigu.myownmusicapp.bean.homefragmentbean.DailySentence
import com.atguigu.myownmusicapp.bean.homefragmentbean.PlaylistRecommendData
import com.atguigu.myownmusicapp.bean.songplaylistactivitybean.NewSongs

/*
* 共享MainActivity和HomeFragment、MyFragment的数据
* */
private const val TAG = "myTag-MainViewModel"
class MainViewModel(application: Application) :AndroidViewModel(application) {
    var playlistRecommendData:MutableLiveData<PlaylistRecommendData> = MutableLiveData()
    private var newSongRecommendData:MutableLiveData<NewSongs> = MutableLiveData()

    private var retrofit163service:Retrofit163service=Retrofit163Instance.getRetroInstance().create(Retrofit163service::class.java)

    suspend fun getPlaylistRecommend(): PlaylistRecommendData {
        val data=retrofit163service.getPlaylistRecommend()
        Log.d(TAG, "getPlaylistRecommend第一次: ${data.result.size}")
        playlistRecommendData.value=data
        Log.d(TAG, "getPlaylistRecommend第二次: ${playlistRecommendData.value?.result?.size}")
        return data
    }

    suspend fun getNewSongRecommend():MutableLiveData<NewSongs>{
        //子线程中赋值使用postValue
        newSongRecommendData.value=retrofit163service.getNewSong()
        return newSongRecommendData
    }

    suspend fun getDailySentence(): DailySentence {
        return retrofit163service.getDailySentence()
    }

}