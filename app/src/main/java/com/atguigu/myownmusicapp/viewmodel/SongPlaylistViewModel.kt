package com.atguigu.myownmusicapp.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.atguigu.myownmusicapp.api.Retrofit163Instance
import com.atguigu.myownmusicapp.api.Retrofit163service
import com.atguigu.myownmusicapp.bean.songplaylistactivitybean.PlayListInfoInner
import com.atguigu.myownmusicapp.bean.songplaylistactivitybean.SongDetail
import com.atguigu.myownmusicapp.pagingdata.SongListSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SongPlaylistViewModel(application: Application) : BaseViewModel(application) {
    private var currentId= MutableLiveData(0L)
    var songs:LiveData<PagingData<SongDetail>> =MutableLiveData()
    var listInfo=MutableLiveData<PlayListInfoInner>()
    var retrofit163service: Retrofit163service =
        Retrofit163Instance.getRetroInstance().create(Retrofit163service::class.java)
    private fun getListData(id:Long): Flow<PagingData<SongDetail>> {
        return Pager(
            PagingConfig(pageSize = 20)
        ) {
            SongListSource(id,retrofit163service).also {
            }
        }.flow
            .cachedIn(viewModelScope)
    }
    fun saveQuery(id:Long){
        currentId.value=id
        //这里采用的分页加载，先不用
        /*songs=currentId.switchMap { searchId->
            getListData(searchId).asLiveData()
        }*/
    }
    fun getPlayListInfo(id:Long){
        viewModelScope.launch {
            listInfo.value=retrofit163service.getPlayListInfo(id).playlist
        }
    }
}