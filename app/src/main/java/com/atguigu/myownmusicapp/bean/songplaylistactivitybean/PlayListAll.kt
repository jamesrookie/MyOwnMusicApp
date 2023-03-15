package com.atguigu.myownmusicapp.bean.songplaylistactivitybean
/*
* 获取各歌单的所有歌曲信息
* */
data class PlayListAll(
    val code:Int,
    val songs:ArrayList<SongDetail>
)

data class SongDetail(
    val id:Long?,
    val name:String?,
    val ar:ArrayList<ArtistDetail>,
    val al:AlbumDetail
)
data class ArtistDetail(
    val id:Long?,
    val name:String?
)
data class AlbumDetail(
    val picUrl:String?
)
