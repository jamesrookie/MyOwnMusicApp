package com.atguigu.myownmusicapp.bean.searchActivitybean.kuwo

data class KuWoSongInfo(
    val status:Int, //状态码
    val data:KuWoSongInfoInner
)
data class KuWoSongInfoInner(
    val lrclist:ArrayList<KuWoSongLyric>,
    val songinfo:KuWoSongLyricInner
)
data class KuWoSongLyric(
    val lineLyric:String, //每一句歌词
    val time:String, //每一句歌词对应的时间
)
data class KuWoSongLyricInner(
    val pic:String //这张图应该清晰一点，相比搜索结果里的预览图
)