package com.atguigu.myownmusicapp.bean.searchActivitybean
//这个是搜索框下面的最热搜索，来源同样是netease
data class SearchHotList(
    val code:Int,
    val data:ArrayList<SearchHotListInner>
)
data class SearchHotListInner(
    val searchWord:String,
    val score:Long,
    val content:String
)
