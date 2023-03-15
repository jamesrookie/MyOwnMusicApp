package com.atguigu.myownmusicapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.atguigu.myownmusicapp.api.Retrofit163Instance
import com.atguigu.myownmusicapp.api.Retrofit163service
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.bean.searchActivitybean.SearchDefaultData
import com.atguigu.myownmusicapp.bean.searchActivitybean.SearchHotList
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.pagingdata.SearchResultListSource
import kotlinx.coroutines.flow.Flow

private const val TAG = "myTag-SearchViewModel"
class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private var searchHotKeyword: MutableLiveData<SearchDefaultData> = MutableLiveData()
    private var searchHotList: MutableLiveData<SearchHotList> = MutableLiveData()

    var currentQuery=MutableLiveData("")
    var searchResults:LiveData<PagingData<StandardSong>> = MutableLiveData()
    var retrofit163service: Retrofit163service=
        Retrofit163Instance.getRetroInstance().create(Retrofit163service::class.java)
    var retrofit163service3:Retrofit163service=
        Retrofit163Instance.getRetroInstance3().create(Retrofit163service::class.java)
    suspend fun getHotKeyWord():MutableLiveData<SearchDefaultData>{
        searchHotKeyword.value= retrofit163service.getHotSearchHint()
        return searchHotKeyword
    }
    suspend fun getSearchHotList():MutableLiveData<SearchHotList>{
        searchHotList.value=retrofit163service.getHotSearchList()
        return searchHotList
    }

    private fun getSearchResultList(searchSource: MusicSource): Flow<PagingData<StandardSong>>{
        return Pager(
            PagingConfig(pageSize = 30)
        ){
            if(searchSource==MusicSource.MIGU || searchSource==MusicSource.QQ){
                SearchResultListSource(searchSource,this,retrofit163service3)
            }else {
                SearchResultListSource(searchSource,this,retrofit163service)
            }
        }.flow
            .cachedIn(viewModelScope)
    }
    fun saveQuery(searchSource:MusicSource,keywords:String){
        Log.d(TAG, "saveQuery: ${searchSource},${keywords}")
        currentQuery.value=keywords
        searchResults=currentQuery.switchMap {
            getSearchResultList(searchSource).asLiveData()
        }
    }



}