package com.atguigu.myownmusicapp.bean.searchActivitybean.qq

data class SearchQQResults(
    val req:InnerQQReq?
){
    data class InnerQQReq(
        val data: InnerQQData?
    )
    data class InnerQQData(
        val body:InnerQQBody?,
        val meta:InnerQQMeta?//用来存放元数据，比如总共的歌曲数和下一页
    )
    data class InnerQQBody(
        val song:InnerQQSongAll?
    )
    data class InnerQQSongAll(
        val list:ArrayList<InnerQQSong>?
    )
    data class InnerQQSong(
        val album:InnerQQAlbum?,
        val id:Long?,
        val mid:String?,
        val name:String?,
        val singer:ArrayList<InnerQQSinger>?
    )
    data class InnerQQAlbum(
        val mid:String?,
    )
    data class InnerQQSinger(
        val name:String?
    )
    data class InnerQQMeta(
        val curpage:Int?,//当前页数
        val nextpage:Int? //如果是-1就说明没有下一页了
    )
}
