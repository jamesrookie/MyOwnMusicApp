package com.atguigu.myownmusicapp.event

import android.support.v4.media.session.MediaSessionCompat

class CollectAllSongsToFavEvent(val queueItemList:List<MediaSessionCompat.QueueItem>)