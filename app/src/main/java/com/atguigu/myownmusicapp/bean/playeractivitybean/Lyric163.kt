package com.atguigu.myownmusicapp.bean.playeractivitybean
/*
* 网易云歌词
* */
data class Lyric163(
    val lrc:Lyric163Inner?,
    val tlyric:Lyric163Inner?,
    val code:Int
)
data class Lyric163Inner(
    val lyric:String
)