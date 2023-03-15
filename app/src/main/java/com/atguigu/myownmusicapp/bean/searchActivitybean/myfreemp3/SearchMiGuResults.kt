package com.atguigu.myownmusicapp.bean.searchActivitybean


data class SearchMiGuResults(
    val data:SearchMiGuResult?
)

data class SearchMiGuResult(
    val list:ArrayList<MiGuSongResult>?
)

data class MiGuSongResult(
    val id:String?,
    val name:String?,
    val time:Int?,//歌曲时长
    //代表这首歌有哪些音乐品质，一般是这样[96,128,320,2000]分别代表流畅m4a，标准mp3，高品mp3，无损flac
    //原本想用来标记，但是因为格式不同，有些是Int,有些是键值对类型的
    //val quality:ArrayList<Int>?,
    val album:MiGuAlbumInfo?,
    val pic:String?,
    val artist:ArrayList<MiGuArtistsInfo>?,
    val hash:String?,//一串hash值
    val lyric:String?//有些有，有些没有（没有的话请求歌词就用id或hash就行），这个是请求歌词时的id
){
    data class MiGuAlbumInfo(
        val id:String?,//专辑id
        val name:String?,//专辑名字
        val pic:String?//专辑封面的图片
    )
    data class MiGuArtistsInfo(
        val id:String?,
        val name:String?
    )
}