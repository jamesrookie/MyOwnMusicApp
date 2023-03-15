package com.atguigu.myownmusicapp.bean.songplaylistactivitybean
/*
* 新歌速递
* */
data class NewSongs(
    var result:ArrayList<NewSongsInner>
)
data class NewSongsInner(
    var source:Int?,//歌曲来源：网易or咪咕
    var id:Long?,//歌曲id
    var name:String?,//歌曲名称
    var picUrl:String?,//图片url
    var song:NewSongInner2
)
data class NewSongInner2(
    var artists: ArrayList<NewSongsAr>

)
data class NewSongsAr(
    var name:String?,

)