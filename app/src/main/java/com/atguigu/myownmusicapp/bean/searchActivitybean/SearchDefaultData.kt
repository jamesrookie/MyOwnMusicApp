package com.atguigu.myownmusicapp.bean.searchActivitybean
//这个是搜索框中默认显示的关键词，实际上是netease的
data class SearchDefaultData(
    val code:Int,
    val message:String?,
    val data:SearchDefaultInnerData
)

data class SearchDefaultInnerData(
    val showKeyword:String,
    val realkeyword:String
)