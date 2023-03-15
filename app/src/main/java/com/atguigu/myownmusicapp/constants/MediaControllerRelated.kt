package com.atguigu.myownmusicapp.constants

/**
 * 存放一些关于MediaController相关的常量
 */
class MediaControllerRelated {
    companion object{
        //MediaController发送customAction时的bundle的serializable键
        const val PLAY_FROM_KEY="PLAY_FROM_KEY"

        //MediaController发送customAction时的Action，但是是通过Activity发送的
        const val PLAY_ACTION= "PLAY_ACTION"
        //MediaController发送customAction时的Action
        const val CHANGE_PLAY_MODE_ACTION="CHANGE_PLAY_MODE"
        //MediaController发送customAction时的Action
        const val SAVE_MEDIA_DATA_ACTION="SAVE_MEDIA_DATA_ACTION"
        //删除所有歌曲的Action
        const val DELETE_ALL_SONGS_ACTION="DELETE_ALL_SONGS"
        //删除指定歌曲的Action
        const val DELETE_SPECIFY_SONG_ACTION="DELETE_SPECIFY_SONG"
        //添加到下一首播放的Action
        const val INSERT_NEXT_PLAY_ACTION="INSERT_NEXT_PLAY"
        //手动播放下一首的情况Action
        const val MANUALLY_SKIP_TO_NEXT_ACTION="MANUALLY_SKIP_TO_NEXT"
        //请求歌曲播放进度时的Action
        const val REQUEST_PROGRESS_ACTION="REQUEST_PROGRESS"
        //添加当前列表中的所有歌曲到播放队列的Action
        const val PLAY_ALL_SONGS="PLAY_ALL_SONGS"
        //MediaController发送customAction时的bundle的parcelable键，表示把歌单中所有歌曲放进播放列表
        const val ALL_SONGS_KEY="ALL_SONGS"
        //MediaController发送customAction时的bundle的parcelable键
        const val STANDARD_SONG = "STANDARD_SONG"
        //MediaController发送customAction时的bundle的parcelable键
        const val QUEUE_ITEM_TO_DELETE="QUEUE_ITEM_TO_DELETE"
        //MediaController发送customAction时的bundle的parcelable键
        const val QUEUE_ITEM_TO_INSERT="QUEUE_ITEM_TO_INSERT"
        const val MANUALLY_SKIP_TO_NEXT_KEY="MANUALLY_SKIP_TO_NEXT_KEY"
        //由MediaSession发送setExtras方法时bundle的键，由MediaController接收到
        const val BUNDLE_PLAY_MODE_KEY="PLAY_MODE_KEY"

        //定义MediaSession调用sendSessionEvent(event:String,extras:Bundle)方法时event的值
        const val EVENT_OPEN_PLAYER_ACTIVITY="OPEN_PLAYER_ACTIVITY"

        //定义歌词的常量
        const val METADATA_KEY_LYRIC="LYRIC"
        //定义歌曲播放链接的常量
        const val METADATA_KEY_PLAY_URL="PLAY_URL"
    }
}