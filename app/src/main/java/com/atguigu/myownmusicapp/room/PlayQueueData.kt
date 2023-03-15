package com.atguigu.myownmusicapp.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.atguigu.myownmusicapp.bean.StandardSong

@Entity(tableName = "playQueueData",indices = [Index(value = ["url_320"],unique = true)])
data class PlayQueueData(
    @Embedded
    val standSongData:StandardSong,
    @PrimaryKey(autoGenerate = true)
    var playQueueId: Long?,
    //记录播放顺序
    val playOrder:Int?
){
    override fun equals(other: Any?): Boolean {
        if(this === other)return true
        if(other==null)return false
        if(javaClass!=other.javaClass)return false
        val otherPlayQueue:PlayQueueData= other as PlayQueueData
        return otherPlayQueue.standSongData==this.standSongData
    }

    override fun hashCode(): Int {
        var result = standSongData.hashCode()
        result = 31 * result + (playQueueId?.hashCode() ?: 0)
        result = 31 * result + (playOrder ?: 0)
        return result
    }
}
