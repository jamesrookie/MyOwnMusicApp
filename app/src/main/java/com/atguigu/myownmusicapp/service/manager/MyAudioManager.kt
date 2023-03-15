package com.atguigu.myownmusicapp.service.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

/**
 * 管理音频焦点
 */
private const val TAG = "myTag-MyAudioManager"
class MyAudioManager(
    private var context: Context?,
    private var focusChangeListener: AudioManager.OnAudioFocusChangeListener?
){
    //音频焦点管理初始化
    private var audioManager:AudioManager =
        context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    var playbackAttributes: AudioAttributes=AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private var focusRequest: AudioFocusRequest? =
        //适配安卓8以上，sdk版本26及以上
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            focusChangeListener?.let {
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    // 可让您的应用异步处理焦点请求。设置此标记后，
                    // 在焦点锁定时发出的请求会返回 AUDIOFOCUS_REQUEST_DELAYED。
                    // 当锁定音频焦点的情况不再存在时（例如当通话结束时），
                    // 系统会批准待处理的焦点请求，并调用 onAudioFocusChange() 来通知您的应用。
                    .setAcceptsDelayedFocusGain(true)
                    //播放通知铃声时自动降低音量，true则回调音频焦点更改回调，可在回调里暂停音乐
                    .setWillPauseWhenDucked(false)
                    .setOnAudioFocusChangeListener(it)
                    .build()
            }
        //安卓8以下不需要focusRequest
        }else null

    /**
     * 请求音频焦点并返回音频焦点的状态
     * @return Int
     */
    fun registerAudioFocus():Int{
        return if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            audioManager.requestAudioFocus(focusRequest!!)
        }else{
            audioManager.requestAudioFocus(focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    /**
     * 释放音频焦点
     */
    fun releaseAudioFocus(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            audioManager.abandonAudioFocusRequest(focusRequest!!)
        }else{
            audioManager.abandonAudioFocus(focusChangeListener)
        }
    }

    fun onDestroy(){
        releaseAudioFocus()
        context=null
        focusChangeListener=null
    }

}