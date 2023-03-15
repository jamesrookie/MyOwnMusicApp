package com.atguigu.myownmusicapp.bean.searchActivitybean

data class SearchKuWoResults(
    val code:Int,
    val data:SearchKuWoInner
)
data class SearchKuWoInner(
    val total:String, //歌曲数量
    val list:ArrayList<SearchKuWoInnerItem>
)
data class SearchKuWoInnerItem(
    val artist:String,
    val rid:Long,
    val pic:String,//封面
    val name:String
)
