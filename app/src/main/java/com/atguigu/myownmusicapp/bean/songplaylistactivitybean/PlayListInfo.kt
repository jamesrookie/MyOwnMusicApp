package com.atguigu.myownmusicapp.bean.songplaylistactivitybean

/*
* 获取各个歌单的信息描述
* */
data class PlayListInfo(
    val code:Int?,
    val playlist:PlayListInfoInner?
)
data class PlayListInfoInner(
    val id:Long?,
    val name:String?,
    val coverImgUrl:String?,
    val description: String?,
    val trackCount:Int? //该歌单的歌曲数量
)
