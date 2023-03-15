package com.atguigu.myownmusicapp.bean.searchActivitybean

data class SearchMiGu2Results(
    val code:Long,
    val resultNum:Int,
    val info:String,
    val songResultData:SearchMiGu2Inner
)
data class SearchMiGu2Inner(
    val result:ArrayList<SearchMiGu2Item>
)
data class SearchMiGu2Item(
    val id:Long,
    val singer:String,
    val name:String,
    val imgItems:ArrayList<SearchMiGu2ItemImage>
)
data class SearchMiGu2ItemImage(
    val img:String,
    val imgSizeType:String  //图片大小
)