package com.atguigu.myownmusicapp.bean.searchActivitybean.kugou

data class KuGouSongInfo(
    val status:Int,
    val data:KuGouSongInfoInner
)
data class KuGouSongInfoInner(
    val img:String,
    val lyrics:String,
    val play_url:String,
    val bitrate:Int,   //比特率
    val play_backup_url:String //歌曲备用链接
)

