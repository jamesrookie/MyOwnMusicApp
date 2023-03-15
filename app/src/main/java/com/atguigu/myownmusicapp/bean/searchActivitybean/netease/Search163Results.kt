package com.atguigu.myownmusicapp.bean.searchActivitybean

data class Search163Results(
    val result:Search163Inner1
)
data class Search163Inner1(
    val songs:ArrayList<Search163Inner2>
)
data class Search163Inner2(
    val name:String,
    val id:Long,
    val ar:ArrayList<Search163Inner2Ar>,
    val al:Search163Inner2Al
)
data class Search163Inner2Ar(
    val name:String //艺术家名字
)
data class Search163Inner2Al(
    val name:String, //专辑名字
    val picUrl:String //专辑图片地址
)