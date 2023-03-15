package com.atguigu.myownmusicapp.pagingdata

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.atguigu.myownmusicapp.api.Retrofit163service
import com.atguigu.myownmusicapp.bean.songplaylistactivitybean.SongDetail

class SongListSource(private val id:Long,private val apiService: Retrofit163service): PagingSource<Int, SongDetail>() {
    override fun getRefreshKey(state: PagingState<Int, SongDetail>): Int? {
        Log.d("myTag", "getRefreshKey: "+state.anchorPosition)
        return state.anchorPosition?.let{
                anchorPosition->
            val anchorPage=state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SongDetail> {
        return try {
            val pageNumber:Int=params.key ?: 0
            Log.d("myTag", "load nextPage: $pageNumber")
            val response=apiService.getPlayListDetail(id =id,limit=20,offset=pageNumber).songs
            val nextPage= if(response.isNotEmpty()){
                pageNumber+20
            }else{
                null
            }
            LoadResult.Page(
                data=response,
                prevKey = null, //Only paging forward
                nextKey = nextPage)
        }catch (e:Exception){
            Log.d("myTag", "load: $e")
            LoadResult.Error(e)
        }
    }
}