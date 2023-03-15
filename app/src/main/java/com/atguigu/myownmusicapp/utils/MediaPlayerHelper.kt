package com.atguigu.myownmusicapp.utils

import android.content.Context
import android.media.MediaPlayer

class MediaPlayerHelper private constructor(context: Context) {
    private val mediaPlayer:MediaPlayer=MediaPlayer()
    companion object{
        private var instance:MediaPlayerHelper?=null
        fun getInstance(context: Context):MediaPlayerHelper{
            return instance ?: synchronized(this) {
                instance ?: MediaPlayerHelper(context).also { instance = it }
            }
        }
    }
}