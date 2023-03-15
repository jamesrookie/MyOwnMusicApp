package com.atguigu.myownmusicapp.bean

data class SongUrl163(
    val code:Int,
    val data:ArrayList<SongUrl163Inner>
)
data class SongUrl163Inner(
    val url:String?
)