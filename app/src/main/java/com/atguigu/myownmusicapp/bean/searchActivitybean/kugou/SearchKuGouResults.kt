package com.atguigu.myownmusicapp.bean.searchActivitybean

data class SearchKuGouResults(
    val status:Int,
    val data:SearchKuGouInner
)
data class SearchKuGouInner(
    val pagesize:Int,  //当前页歌曲数量
    val total:Int, //全部搜索结果
    val lists:ArrayList<SearchKuGouInnerItem>
)
data class SearchKuGouInnerItem(
    val SingerName:String,
    val SongName:String,
    val FileHash:String,
    val AlbumID:String

)
