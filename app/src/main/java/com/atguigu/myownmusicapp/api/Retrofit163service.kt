package com.atguigu.myownmusicapp.api

import com.atguigu.myownmusicapp.bean.Comments
import com.atguigu.myownmusicapp.bean.MyComments
import com.atguigu.myownmusicapp.bean.SongUrl163
import com.atguigu.myownmusicapp.bean.ThumbUpOrLogoutResp
import com.atguigu.myownmusicapp.bean.homefragmentbean.DailySentence
import com.atguigu.myownmusicapp.bean.homefragmentbean.PlaylistRecommendData
import com.atguigu.myownmusicapp.bean.loginbean.*
import com.atguigu.myownmusicapp.bean.playeractivitybean.Lyric163
import com.atguigu.myownmusicapp.bean.searchActivitybean.*
import com.atguigu.myownmusicapp.bean.searchActivitybean.kuwo.KuWoSongInfo
import com.atguigu.myownmusicapp.bean.searchActivitybean.migu.MiGu2SongUrl
import com.atguigu.myownmusicapp.bean.searchActivitybean.myfreemp3.RequestMiGuLrcResp
import com.atguigu.myownmusicapp.bean.searchActivitybean.qq.QQSongLyricInfo
import com.atguigu.myownmusicapp.bean.searchActivitybean.qq.QQSongUrlInfo
import com.atguigu.myownmusicapp.bean.searchActivitybean.qq.SearchQQResults
import com.atguigu.myownmusicapp.bean.songplaylistactivitybean.NewSongs
import com.atguigu.myownmusicapp.bean.songplaylistactivitybean.PlayListAll
import com.atguigu.myownmusicapp.bean.songplaylistactivitybean.PlayListInfo
import com.atguigu.myownmusicapp.bean.toplistactivitybean.TopListData
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.util.*

interface Retrofit163service {

    //获取歌单推荐（默认16个）
    @GET("personalized")
    suspend fun getPlaylistRecommend(
        @Query("limit") limit:Int =16
    ):PlaylistRecommendData

    //获取各大排行榜的封面、名字和更新时间
    @GET("toplist/detail")
    suspend fun getTopListDetail():TopListData

    //获取指定id的播放列表(包含排行榜)的封面、名字和描述
    @GET("playlist/detail")
    suspend fun getPlayListInfo(@Query("id")id:Long):PlayListInfo

    //获取指定id的播放列表(包含排行榜)中所有歌曲名字、歌手和封面
    @GET("playlist/track/all")
    suspend fun getPlayListDetail(@Query("id")id:Long,@Query("limit")limit:Int,@Query("offset")offset:Int): PlayListAll

    //功能同样是获取指定id的播放列表(包含排行榜)中所有歌曲名字、歌手和封面，但是我们这里不使用协程
    @GET("playlist/track/all")
    fun getPlayListDetail(@Query("id")id:Long): Call<PlayListAll>

    //首页的新歌速递
    @GET("personalized/newsong")
    suspend fun getNewSong(): NewSongs

    //获取搜索框热搜的hint
    @GET("search/default")
    suspend fun getHotSearchHint(): SearchDefaultData

    //获取热门搜索列表（20首）
    @GET("search/hot/detail")
    suspend fun getHotSearchList():SearchHotList

    //获取搜索结果
    @GET("cloudsearch")
    suspend fun getSearchResult(@Query("keywords")keywords: String,@Query("limit")limit:Int,@Query("offset")offset:Int): Search163Results

    //获取网易云音乐歌曲链接
    @GET("song/url")
    suspend fun get163SongUrl(@Query("id")id:Long):SongUrl163

    //登录网易云的接口,目前只能二维码登录,先拿到一个二维码的key
    @GET("login/qr/key")
    fun login163QrKey(@Query("timestamp")timestamp:Long= Date().time):Call<QrKey>

    //获取二维码的base64编码
    @GET("login/qr/create")
    fun login163QrCode(@Query("key")key:String,
                       @Query("qrimg")qrimg:Int=1,
                       @Query("timestamp")timestamp:Long= Date().time):Call<QrCode>
    //查询二维码扫描的状态
    @GET("/login/qr/check")
    fun login163CheckStatus(@Query("key")key:String):Call<QrCodeStatus>

    //获取用户信息
    @GET("user/account")
    fun get163UserProfile(@Query("cookie")cookie:String):Call<UserProfile>

    //获取歌曲评论信息
    @GET("/comment/music")
    fun get163SongComments(@Query("id")id:Long,
                           @Query("limit")limit: Int?=20,
                           @Query("cookie")cookie:String="",
                           @Query("timestamp")timestamp:Long= Date().time):Call<Comments>

    //发送评论
    /**
     * @param id Long    对应的资源id，比如某一首歌曲的id
     * @param t Int      1 发送, 2 回复
     * @param type Int   资源类型,对应歌曲,mv,专辑,歌单,电台,视频对应以下类型
     *                    0: 歌曲 1: mv 2: 歌单 3: 专辑 4: 电台 5: 视频 6: 动态
     * @param content String 要发送的内容
     * @param cookie String
     * @return Call<MyComments>
     */
    @GET("comment")
    fun send163SongComments(@Query("id")id:Long,
                            @Query("t")t:Int=1,
                            @Query("type")type:Int=0,
                            @Query("content")content:String,
                            @Query("cookie")cookie:String):Call<MyComments>

    //点赞/取消点赞的功能，无论点赞还是取消点赞，都会返回{"code":200}
    @GET("comment/like")
    fun send163SongCommentsLike(@Query("id")id:Long, //歌曲id
                                @Query("cid")cid:Long,//评论id
                                @Query("t")t:Int,//是否点赞：1是 0否
                                @Query("type")type:Int=0,//0: 歌曲 1: mv 2: 歌单 3: 专辑 4: 电台节目 5: 视频 6: 动态 7: 电台
                                @Query("cookie")cookie:String,
                                @Query("timestamp")timestamp:Long= Date().time):Call<ThumbUpOrLogoutResp>

    //调用此接口,可获取登录状态
    @GET("login/status")
    suspend fun checkLoginStatus(
        @Query("cookie")cookie:String,
        @Query("timestamp")timestamp:Long= Date().time
    ):CheckLoginStatus

    //退出登录
    @GET("logout")
    suspend fun logout(
        @Query("cookie")cookie:String,
        @Query("timestamp")timestamp:Long= Date().time
    ):ThumbUpOrLogoutResp

    //获取网易云音乐歌词
    @GET("lyric")
    suspend fun get163Lyric(@Query("id")id:Long):Lyric163

    //获取myFreeMusic搜索结果
    //@FormUrlEncoded
    @Headers(
        "content-type: application/json",
        "origin: https://tool.liumingye.cn",
        "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.0.0"
    )
    @POST
    suspend fun searchFromMiGu(
        @Url url:String="https://test.quanjian.com.cn/m/api/search",
        @Body body: String,//json格式
        /*@Field("page")page:Int=1,
        @Field("text") text:String,
        @Field("token")token:String,
        @Field("type")type:String="YQM",
        @Field("v") v:String="beta",
        @Field("_t")_t:Long*/
    ): SearchMiGuResults

    @GET
    @Headers(
        "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.0.0"
    )
    suspend fun getMiGuSongUrl(
        @Url url:String="https://test.quanjian.com.cn/m/api/link",
        @Query("id")id:String,
        @Query("quality")quality:Int=128,
        @Query("_t")_t:Long,
        @Query("token")token:String
    ):Response<String>

    //获取migu音乐的歌词字符串
    @POST
    @Headers(
        "content-type: application/json",
        "origin: https://tool.liumingye.cn",
        "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.0.0"
    )
    suspend fun getMiGuLrc(
        @Url url:String="https://test.quanjian.com.cn/m/api/lyric",
        @Body body:String,//json格式
    ): RequestMiGuLrcResp


    //获取咪咕2音乐搜索结果(k1插件)
    @GET
    suspend fun searchFromMiGu2(
        @Url url:String="https://112.25.126.158/music_search/v2/search/searchAll",
        @Query("sid")sid:String,
        @Query("isCorrect")isCorrect:Int=1,
        @Query("isCopyright")isCopyright:Int=1,
        @Query("searchSwitch")searchSwitch:String="{\"song\":1}",
        @Query("pageSize")pageSize:Int=20,
        @Query("text")text:String,
        @Query("pageNo")pageNo:Int,
        @Query("feature")feature:Long=1000000000,
        @Query("sort")sort:Int=1,
        @Header("appId")appId:String="yyapp2",
        @Header("deviceId")deviceId:String,
        @Header("sign")sign:String,
        @Header("timestamp")timestamp:Long,
        @Header("uiVersion")uiVersion:String="A_music_3.3.0",
        @Header("version")version:String="7.0.4"
    ): SearchMiGu2Results

    @Headers("channel: '0146951'","uid: 1234")
    @GET
    suspend fun getMiGu2SongUrl(
        @Url url:String="https://app.c.nf.migu.cn/MIGUM2.0/strategy/listen-url/v2.2",
        @Query("netType")netType:String="01",
        @Query("resourceType")resourceType:String="E",
        @Query("songId")songId:Long,
        @Query("toneFlag")toneFlag:String
    ): MiGu2SongUrl

    @GET
    suspend fun getMiGu2SongLyric(
        @Url url:String
    ):String?

    //获取酷我音乐搜索结果(k1插件)
    @GET
    suspend fun searchFromKuWo(@Url url:String,@Header("csrf")csrf:String,@Header("Cookie")cookie:String,@Header("referer")referer:String="https://www.kuwo.cn/"):SearchKuWoResults

    //获取酷我音乐单首歌曲信息，主要是歌词
    @GET
    suspend fun getKuWoSongInfo(@Url url:String): KuWoSongInfo

    //获取酷我音乐播放链接
    @GET
    suspend fun getKuWoSongUrl(@Url url:String):String

    //获取酷狗音乐搜素结果(k1插件)
    @GET
    suspend fun searchFromKuGou(@Url url:String):SearchKuGouResults

    //获取酷狗音乐信息，包括歌词和播放链接
    @GET
    suspend fun getKuGouSongInfo(@Url url:String):String

    //获取QQ音乐的搜索信息
    @Headers(
        "content-type: application/json;charset=UTF-8",
        "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.63",
    )
    @POST
    suspend fun searchFromQQ(
        @Url url:String="https://u.y.qq.com/cgi-bin/musicu.fcg",
        @Body body:String//json格式的
    ):SearchQQResults?

    @Headers(
        "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.69"
    )
    @GET
    suspend fun getQQSongUrl(
        @Url url:String="https://u.y.qq.com/cgi-bin/musicu.fcg",
        @Query("format")format:String="json",
        @Query("data")data:String,
    ): QQSongUrlInfo?

    @Headers(
        "origin: https://y.qq.com/",
        "referer: https://y.qq.com/",
        "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.69"
    )
    @GET
    suspend fun getQQSongLyric(
     @Url url:String="https://i.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg",
     @Query("songmid")songmid:String,
     @Query("g_tk")g_tk:Int=5381,
     @Query("format")format:String="json",
     @Query("inCharset")inCharset:String="utf8",
     @Query("outCharset")outCharset:String="utf8",
     @Query("nobase64")nobase64:Int=1,
    ):QQSongLyricInfo

    @GET
    suspend fun getDailySentence(
        @Url url:String="https://hitoapi.cc/sp"
    ): DailySentence
}