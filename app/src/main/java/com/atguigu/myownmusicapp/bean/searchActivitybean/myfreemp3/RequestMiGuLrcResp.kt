package com.atguigu.myownmusicapp.bean.searchActivitybean.myfreemp3
//请求咪咕音乐（myFreeMp3）歌词时的返回
data class RequestMiGuLrcResp(
    val data: MiGuLrcInner?
){
    data class MiGuLrcInner(
        val lrc:String?,
        val tlyric:String?,
    )
}
