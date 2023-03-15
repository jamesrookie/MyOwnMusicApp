package com.atguigu.myownmusicapp.bean.homefragmentbean

data class PlaylistRecommendData(
    val result:ArrayList<PlaylistRecommendDataResult>
)

data class PlaylistRecommendDataResult(
    val id:Long,
    val picUrl:String,
    val name:String,
    val playCount:Long  //播放数
)
