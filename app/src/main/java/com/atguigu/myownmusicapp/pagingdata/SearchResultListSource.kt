package com.atguigu.myownmusicapp.pagingdata

import android.util.Log
import android.widget.Toast
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.atguigu.myownmusicapp.api.Retrofit163service
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.service.GetMusicUrl
import com.atguigu.myownmusicapp.utils.JsUtils
import com.atguigu.myownmusicapp.viewmodel.SearchViewModel
import com.google.gson.JsonObject
import org.json.JSONException

private const val TAG = "myTagSearchResultListS"
class SearchResultListSource(private val songSource: MusicSource, private val searchViewModel: SearchViewModel, private val apiService: Retrofit163service): PagingSource<Int, StandardSong>() {
    override fun getRefreshKey(state: PagingState<Int, StandardSong>): Int? {
        return state.anchorPosition?.let{
                anchorPosition->
            val anchorPage=state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StandardSong> {
        val pageNumber: Int
        return try {
            pageNumber = if(songSource== MusicSource.NETEASE){
                params.key ?: 0
            }else{
                params.key?:1
            }
            val response= ArrayList<StandardSong>()
            var nextPage:Int?=0
            val keywords=searchViewModel.currentQuery.value
            when(songSource){
                MusicSource.MIGU ->{
                    val timeStamp=System.currentTimeMillis()
                    val encoded=keywords?.let {
                        JsUtils.executeJsMiGuSearch(searchViewModel.getApplication(),it,timeStamp)
                    }
                    val responseOrigin=if(encoded!=null){
                        val jsonObject = JsonObject()
                        try {
                            //注意这个添加的顺序一定不能颠倒，不然直接出错
                            jsonObject.addProperty("type", "YQM")
                            jsonObject.addProperty("text", keywords)
                            jsonObject.addProperty("page", 1)
                            jsonObject.addProperty("v", "beta")
                            jsonObject.addProperty("_t", timeStamp)
                            jsonObject.addProperty("token", encoded)
                            Log.d(TAG, "search: $jsonObject")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        Log.d(TAG, "load: $apiService")
                        apiService.searchFromMiGu(body=jsonObject.toString())
                    }else null
                    Log.d(TAG, "load: $responseOrigin")
                    nextPage=null
                    if (responseOrigin != null) {
                        for(item in responseOrigin.data?.list!!){
                            val source= MusicSource.MIGU
                            //有些地方需要指定尺寸
                            val picUrl=(item.pic?:(item.album?.pic))?.replace("{size}","100")
                            val name=item.name
                            var artists=""
                            for (miGuArtistsInfo in item.artist!!) {
                                artists+="/"+miGuArtistsInfo.name
                            }
                            artists=artists.removePrefix("/")
                            //todo 后续再处理其他链接的事情,我这里将hash(实际上是歌曲id存入url_320，可能有种情况是没有hash，此时我们就用id来代替)
                            val standSong = StandardSong(source,null,name,picUrl,artists,item.lyric?:(item.hash?:item.id),null,item.hash?:item.id,null,null)
                            response.add(standSong)
                        }
                    }
                }
                MusicSource.NETEASE ->{
                    val responseOrigin=
                        keywords?.let { apiService.getSearchResult(keywords = it,limit=30,offset=pageNumber).result.songs }
                    if (responseOrigin != null) {
                        for(item in responseOrigin){
                            val source= MusicSource.NETEASE
                            val id=item.id
                            val picUrl=item.al.picUrl
                            val name=item.name
                            var artists =""
                            item.ar.forEach {
                                artists += if(it==item.ar.last()){
                                    it.name
                                }else {
                                    "${it.name}/"
                                }
                            }
                            val standSong = StandardSong(source,id,name,picUrl,artists,null,null,null,null,null)
                            response.add(standSong)

                        }
                    }
                    if (responseOrigin != null) {
                        nextPage= if(responseOrigin.isNotEmpty()){
                            pageNumber+30
                        }else{
                            null
                        }
                    }
                }
                MusicSource.MIGU2->{
                    val encoded= keywords?.let { JsUtils.executeJs2(searchViewModel.getApplication(),keywords) }
                    val dataNeeded=encoded?.split("$")
                    val sid= dataNeeded?.get(0)
                    val deviceId=dataNeeded?.get(1)
                    val sign=dataNeeded?.get(2)
                    val timeStamp= dataNeeded?.get(3)?.toLong()
                    val responseOrigin=
                        if (sid!=null && deviceId != null && sign!=null && timeStamp !=null) {
                            searchViewModel.currentQuery.value?.let { apiService.searchFromMiGu2(sid= sid,deviceId = deviceId,sign = sign,timestamp =timeStamp,pageNo =pageNumber,text = it) }
                        }else null
                    if (responseOrigin != null) {
                        if (nextPage != null) {
                            nextPage = if(nextPage>responseOrigin.resultNum/20+1){
                                null
                            }else{
                                pageNumber+1
                            }
                        }
                    }
                    if (responseOrigin != null) {
                        for(item in responseOrigin.songResultData.result){
                            val id=item.id
                            val source= MusicSource.MIGU2
                            val picUrl=item.imgItems[0].img
                            val name=item.name
                            val artists =item.singer
                            val standSong = StandardSong(source,id,name,picUrl,artists,null,null,null,null,null)
                            response.add(standSong)
                        }
                    }

                }
                MusicSource.KUWO->{
                    val csrf=GetMusicUrl.getCookieFromKuWo()
                    val url ="https://www.kuwo.cn/api/www/search/searchMusicBykeyWord?key=${keywords}&pn=${pageNumber}&rn=20"
                    val responseOrigin=searchViewModel.currentQuery.value?.let { apiService.searchFromKuWo(url=url,csrf=csrf,cookie ="kw_token=$csrf") }
                    nextPage=if(nextPage!! > responseOrigin?.data?.total?.toInt()!!/20+1){
                        null
                    }else{
                        pageNumber+1
                    }
                    for(item in responseOrigin.data.list){
                        val id=item.rid
                        val source= MusicSource.KUWO
                        val picUrl=item.pic
                        val name=item.name.replace("&nbsp;"," ")
                        val artists =item.artist.replace("&nbsp;"," ")
                        val standSong = StandardSong(source,id,name,picUrl,artists,null,null,null,null,null)
                        response.add(standSong)
                    }
                }
                MusicSource.KUGOU->{
                    val url ="https://songsearch.kugou.com/song_search_v2?keyword=$keywords&page=$pageNumber"
                    val responseOrigin=url.let { apiService.searchFromKuGou(url=it) }
                    nextPage=if(nextPage!! > responseOrigin.data.total /20+1){
                        null
                    }else{
                        pageNumber+1
                    }
                    for(item in responseOrigin.data.lists){
                        val source= MusicSource.KUGOU
                        val name=item.SongName
                        val artists =item.SingerName
                        val urlFlac=item.FileHash+"$"+item.AlbumID
                        val standSong = StandardSong(source,null,name,null,artists,null,null,null,null,urlFlac)
                        response.add(standSong)
                    }
                }
                MusicSource.QQ->{
                    val jsonObject = JsonObject()
                    try {
                        val innerComm=JsonObject()
                        innerComm.addProperty("ct","19")
                        innerComm.addProperty("cv","1859")
                        innerComm.addProperty("uin","0")
                        val innerReq=JsonObject()
                        val innerInnerParam=JsonObject()
                        innerInnerParam.addProperty("grp",1)
                        innerInnerParam.addProperty("num_per_page",50)
                        innerInnerParam.addProperty("page_num",pageNumber)
                        innerInnerParam.addProperty("query",keywords)
                        innerInnerParam.addProperty("search_type",0)
                        innerReq.addProperty("method","DoSearchForQQMusicDesktop")
                        innerReq.addProperty("module","music.search.SearchCgiService")
                        innerReq.add("param",innerInnerParam)
                        //注意这个添加的顺序一定不能颠倒，不然直接出错
                        jsonObject.add("comm", innerComm)
                        jsonObject.add("req", innerReq)
                        Log.d(TAG, "searchQQ: $jsonObject")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    val responseOrigin= apiService.searchFromQQ(body=jsonObject.toString())

                    Log.d(TAG, "load: $responseOrigin")
                    val songList=responseOrigin?.req?.data?.body?.song?.list
                    if (songList != null) {
                        for(item in songList){
                            val source= MusicSource.QQ
                            val picMid=item.album?.mid
                            val picUrl=if (!picMid.isNullOrBlank())"https://y.gtimg.cn/music/photo_new/T002R300x300M000${picMid}.jpg" else null
                            val name=item.name
                            var artists=""
                            if(item.singer!=null){
                                for (miGuArtistsInfo in item.singer) {
                                    artists+="/"+miGuArtistsInfo.name
                                }
                            }
                            artists=artists.removePrefix("/")
                            val standSong = StandardSong(source,null,name,picUrl,artists,item.mid,null,
                                item.mid,null,null)
                            response.add(standSong)
                        }
                    }
                    nextPage= responseOrigin?.req?.data?.meta?.nextpage
                    if(nextPage==-1){
                        nextPage=null
                    }
                }
                else -> {}
            }
            LoadResult.Page(
                data=response,
                prevKey = null, //Only paging forward
                nextKey = nextPage)
        }catch (e:Exception){
            Toast.makeText(searchViewModel.getApplication(),"未知错误，请稍后重试!",Toast.LENGTH_SHORT).show()
            LoadResult.Error(e)
        }
        finally {

        }
    }
}