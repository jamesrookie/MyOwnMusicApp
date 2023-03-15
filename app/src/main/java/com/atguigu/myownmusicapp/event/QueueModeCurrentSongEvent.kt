package com.atguigu.myownmusicapp.event

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.atguigu.myownmusicapp.constants.PlayMode

//EventBus发布一个事件，包含播放队列，播放模式，当前播放歌曲的元数据，三者都可空
class QueueModeCurrentSongEvent(val currentQueueItem: List<MediaSessionCompat.QueueItem>?,
                                val currentPlayMode:PlayMode?,
                                val currentMetadata:MediaMetadataCompat?
                                )