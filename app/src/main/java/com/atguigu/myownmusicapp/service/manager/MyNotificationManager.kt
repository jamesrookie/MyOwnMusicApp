package com.atguigu.myownmusicapp.service.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.service.refactor.MEDIA_CHANNEL_ID
import com.atguigu.myownmusicapp.ui.MainActivity
import com.atguigu.myownmusicapp.ui.PlayerActivity

private const val TAG = "myTag-MyNotificationMan"
const val playIntentActionStr="PLAY_INTENT"
const val pauseIntentActionStr="PAUSE_INTENT"
const val prevIntentActionStr="PREV_INTENT"
const val nextIntentActionStr="NEXT_INTENT"
const val closeIntentActionStr="CLOSE_INTENT"  //关闭通知的intent
class MyNotificationManager(private val context: Context) {
    private val channelId="myOwnMusicAppChannel"//通知渠道id,是该通知渠道唯一标识
    private val description="MyOwnMusicApp音乐播放控制通知"
    private val notificationManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val clickPendingIntent:PendingIntent by lazy {
        val intentMain=Intent(context,MainActivity::class.java)
        val intentPlayer= Intent(context,PlayerActivity::class.java)
        PendingIntent.getActivities(context,0, arrayOf(intentMain,intentPlayer),PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private val closePendingIntent:PendingIntent= getPendingIntent(closeIntentActionStr)

    private val playAction:NotificationCompat.Action
    =NotificationCompat.Action(R.drawable.ic_play,context.getString(R.string.notificationPlay),getPendingIntent(playIntentActionStr))

    private val pauseAction=NotificationCompat.Action(R.drawable.ic_pause,context.getString(R.string.notificationPause),getPendingIntent(
        pauseIntentActionStr))
    private val nextAction=NotificationCompat.Action(R.drawable.ic_next,context.getString(R.string.notificationNext),getPendingIntent(
        nextIntentActionStr
    ))
    private val prevAction=NotificationCompat.Action(R.drawable.ic_prev,context.getString(R.string.notificationPrev),getPendingIntent(
        prevIntentActionStr
    ))
    private fun getPendingIntent(intentStr: String): PendingIntent {
        val intent=Intent(intentStr)
        return PendingIntent.getBroadcast(context,0,intent,0)
    }
    fun onDestroy(){
        //todo 取消所有通知
        notificationManager.cancelAll()
    }
    fun getNotification(metadata:MediaMetadataCompat,isPlaying:Boolean,sessionToken:MediaSessionCompat.Token):Notification?{
        //todo 1、创建通知渠道，如果有了就复用
        createNotificationChannel()
        val notification=NotificationCompat.Builder(context,channelId)
            //添加metadata
            .setContentTitle(metadata.description.title)//歌名
            .setContentText(metadata.getString(METADATA_KEY_ARTIST))//歌手
            //点击事件
            .setContentIntent(clickPendingIntent)
            //stop the service when the notification is swiped away
            //适用于api21 安卓5.0及以后的
            .setDeleteIntent(closePendingIntent)
            //make the transport controls visible on the lockscreen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                //add an app icon and set its accent color
                //be careful about the color
            .setSmallIcon(R.drawable.ic_music_small_icon)
            .setColor(ContextCompat.getColor(context,R.color.design_default_color_primary_dark))
                //添加上一首按键
            .addAction(prevAction)
                //添加播放暂停按键
            .addAction(if (isPlaying) pauseAction else playAction)
                //添加下一首按键
            .addAction(nextAction)
            .setStyle(getMediaStyle(sessionToken))
            //设置大图
            .setLargeIcon(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
            .build()
        Log.d(TAG, "onPlay: 当前这个通知:${notification.hashCode()}")
        return notification
    }

    private fun getMediaStyle(sessionToken: MediaSessionCompat.Token): androidx.media.app.NotificationCompat.MediaStyle {
        return androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(sessionToken)
            //0，1，2分别对应上一首，播放暂停，下一首这三个按键
            .setShowActionsInCompactView(0,1,2)
            //下面两行代码是兼容安卓5.0以前的版本，api21
            .setShowCancelButton(true)
            .setCancelButtonIntent(closePendingIntent)
    }

    /**
     * 创建通知渠道，安卓8.0以上才有通知渠道
     */
    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            //如果已经存在，就复用，不存在就创建
            if(notificationManager.getNotificationChannel(channelId)!=null) return
            //id是该通知渠道唯一标识，name是用户可见的渠道的名字（用户在设置里可以看到该应用的通知渠道名字）,最后一个是通知的初始优先级，用户可以在设置里修改
            val notificationChannel= NotificationChannel(channelId,"普通的通知",IMPORTANCE_DEFAULT)
            //配置通知的描述
            notificationChannel.description=description
            notificationChannel.setSound(null,null)//禁止铃声
            notificationManager.createNotificationChannel(notificationChannel)
            //不同优先级的通知方式不同，高优先级的会在屏幕上方显示，就类似短信验证码那样
        }
    }

    /**
     * 更新通知
     * @param metadata MediaMetadataCompat
     * @param isPlaying Boolean
     * @param sessionToken Token
     */
    fun updateNotification(metadata:MediaMetadataCompat,isPlaying:Boolean,sessionToken:MediaSessionCompat.Token) {
        val notification = getNotification(metadata, isPlaying, sessionToken)
        if(notification!=null)
            notificationManager.notify(MEDIA_CHANNEL_ID, notification)
    }
}