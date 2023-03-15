package com.atguigu.myownmusicapp.utils

import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.bean.songplaylistactivitybean.SongDetail
import com.atguigu.myownmusicapp.constants.MediaControllerRelated
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.room.PlayQueueData

/**
 * 类与类之间的转换工具类
 */
object ClassConvertHelper {
    /**
     * metaData元数据转为StandardSong
     * @param currentMetadata MediaMetadataCompat?
     * @return StandardSong
     */
    fun metadataToStandardSong(currentMetadata: MediaMetadataCompat?): StandardSong {
        val sourceStr = currentMetadata?.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION).toString()
        val source = MusicSource.strToMusicSource(sourceStr)
        val id = currentMetadata?.getLong("mediaId")
        val name=currentMetadata?.description?.title.toString()
        val picUrl=currentMetadata?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
        val artists=currentMetadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        val lrc = currentMetadata?.getString(MediaControllerRelated.METADATA_KEY_LYRIC)
        val playUrl=currentMetadata?.getString(MediaControllerRelated.METADATA_KEY_PLAY_URL)
        return StandardSong(source,id,name,picUrl,artists,lrc,null,null,playUrl,null)
    }

    /**
     * standardSong转QueueItem
     * @param standardSong StandardSong
     * @return MediaSessionCompat.QueueItem
     */
    fun standardSongToQueueItem(standardSong: StandardSong): MediaSessionCompat.QueueItem {
        val bundle = Bundle()
        bundle.putParcelable(MediaControllerRelated.STANDARD_SONG, standardSong)
        val description = MediaDescriptionCompat.Builder()
            .setTitle(standardSong.name)
            .setDescription("${standardSong.source}")
            .setMediaId("id")
            .setSubtitle(standardSong.artists)
            .setExtras(bundle)//额外的自定义的数据
            .build()
        val id: Long = 0
        return MediaSessionCompat.QueueItem(description, id)
    }

    /**
     * MutableList<QueueItem>转MutableList<PlayQueueData>
     * @param queueItemList MutableList<QueueItem>
     * @return MutableList<PlayQueueData>
     */
    fun queueItemListToPlayQueueDataList(queueItemList:List<MediaSessionCompat.QueueItem>):List<PlayQueueData>{
        val newPlayQueue=ArrayList<PlayQueueData>()
        queueItemList.forEachIndexed { index, queueItem ->
            val standardSong:StandardSong?=queueItem.description.extras?.getParcelable(
                MediaControllerRelated.STANDARD_SONG
            )
            val playQueueData= standardSong?.let {
                PlayQueueData(it,null,index)
            }
            if(playQueueData!=null)newPlayQueue.add(playQueueData)
        }
        return newPlayQueue
    }
    fun playQueueDataListToQueueItemList(playQueueDataList:List<PlayQueueData>):List<MediaSessionCompat.QueueItem>{
        val newQueueItemList=ArrayList<MediaSessionCompat.QueueItem>()
        playQueueDataList.forEachIndexed { index, playQueueData ->
            val queueItem= standardSongToQueueItem(playQueueData.standSongData)
            newQueueItemList.add(queueItem)
        }
        return newQueueItemList
    }

    fun queueItemListToStandardSongList(queueItemList:List<MediaSessionCompat.QueueItem>):List<StandardSong>{
        val songList=ArrayList<StandardSong>()
        queueItemList.forEachIndexed { _, queueItem ->
            val standardSong:StandardSong?=queueItem.description.extras?.getParcelable(
                MediaControllerRelated.STANDARD_SONG
            )
            if(standardSong!=null)songList.add(standardSong)
        }
        return songList
    }

    /**
     * SongDetail转换为StandardSong
     * @param songDetail SongDetail?
     * @return StandardSong
     */
    fun songDetailToStandardSong(songDetail: SongDetail?):StandardSong{
        var songAuthorJoint =""
        songDetail?.ar?.forEach {
            songAuthorJoint += if(it==songDetail.ar.last()){
                "${it.name}"
            }else {
                "${it.name}/"
            }
        }
        return StandardSong(MusicSource.NETEASE,songDetail?.id,songDetail?.name,songDetail?.al?.picUrl,songAuthorJoint,null,null,null,null,null)
    }
}