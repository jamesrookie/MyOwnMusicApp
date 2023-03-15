package com.atguigu.myownmusicapp.bean

import android.os.Parcelable
import com.atguigu.myownmusicapp.constants.MusicSource
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/*
* 标准歌曲信息
* */
@Parcelize
data class StandardSong(
    var source:MusicSource?,//歌曲来源：网易or咪咕
    var id:Long?,//歌曲id
    var name:String?,//歌曲名称
    var picUrl:String?,//图片url
    var artists: String?, //所有艺术家，之间用"/"分割

    //以下为咪咕中直接可以获得的信息
    var lrc:String?,
    var url_m4a:String?, //LQ
    var url_128:String?, //PQ
    var url_320:String?, //HQ
    val url_flac:String?, //无损 SQ

):Parcelable,Serializable{
    //比较两个对象是否相等
    override fun equals(other: Any?): Boolean {
        if(this === other)return true
        if(other==null)return false
        if(javaClass!=other.javaClass)return false
        val otherSong:StandardSong= other as StandardSong
        return otherSong.artists==artists && otherSong.source==source && otherSong.name==name
        //return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = source?.hashCode() ?: 0
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (picUrl?.hashCode() ?: 0)
        result = 31 * result + (artists?.hashCode() ?: 0)
        result = 31 * result + (lrc?.hashCode() ?: 0)
        result = 31 * result + (url_m4a?.hashCode() ?: 0)
        result = 31 * result + (url_128?.hashCode() ?: 0)
        result = 31 * result + (url_320?.hashCode() ?: 0)
        result = 31 * result + (url_flac?.hashCode() ?: 0)
        return result
    }
}

