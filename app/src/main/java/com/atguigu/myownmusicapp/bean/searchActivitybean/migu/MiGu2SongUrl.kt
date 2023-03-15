package com.atguigu.myownmusicapp.bean.searchActivitybean.migu

data class MiGu2SongUrl(
    val code:String,
    val data:MiGu2SongUrlInner
)
data class MiGu2SongUrlInner(
    val songItem:MiGu2SongUrlInnerLyric,
    val url:String, //歌曲链接
    val formatType:String //音乐品质
)
data class MiGu2SongUrlInnerLyric(
    val lrcUrl:String //歌词url
)
