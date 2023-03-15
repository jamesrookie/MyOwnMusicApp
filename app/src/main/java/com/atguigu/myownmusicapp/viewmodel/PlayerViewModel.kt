package com.atguigu.myownmusicapp.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.bean.playeractivitybean.LyricDataLyricView
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.service.GetMusicUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLEncoder
//todo 网易云的本地歌词需要额外进行解析，因为第一语言和第二语言中间有"$$$$$"符号
class PlayerViewModel(application: Application) : BaseViewModel(application) {
    var lyricViewData:MutableLiveData<LyricDataLyricView> = MutableLiveData(LyricDataLyricView("",""))
    var lyricMiGuUrl=""
    //音频管理器
    private val audioManager=application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    //获取最大媒体音
    val maxVolume=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    fun loadLyric(standardSong:StandardSong?){
        when(standardSong?.source){
            MusicSource.NETEASE->{
                viewModelScope.launch(Dispatchers.IO) {
                    val lyric163=standardSong.id?.let { GetMusicUrl.getLyric163(it) }
                    lyricViewData.value= LyricDataLyricView(lyric163?.lrc?.lyric?:"",lyric163?.tlyric?.lyric?:"")
                }
            }
            MusicSource.MIGU ->{
                var encode=standardSong.name+"-"+standardSong.artists
                encode = URLEncoder.encode(encode, "utf-8")
                lyricMiGuUrl=standardSong.lrc+"/name/"+encode+".lrc"
            }
            MusicSource.MIGU2 ->{
                lyricMiGuUrl=standardSong.lrc?:""
            }
            MusicSource.KUWO->{
                lyricViewData.value= LyricDataLyricView(standardSong.lrc.toString(),"")
            }
            MusicSource.KUGOU->{
                lyricViewData.value= LyricDataLyricView(standardSong.lrc.toString(),"")
            }
            MusicSource.DOWNLOAD_NETEASE,MusicSource.DOWNLOAD_MIGU2,MusicSource.DOWNLOAD_MIGU,
            MusicSource.DOWNLOAD_KUWO,MusicSource.DOWNLOAD_KUGOU->{
                //读取本地文件
                val lrcPath=standardSong.lrc
                if(lrcPath!=null){
                    val lyricContent= File(lrcPath).readText(Charsets.UTF_8)
                    val lyricList=lyricContent.split("$$$$$")
                    lyricViewData.value=LyricDataLyricView(lyricList[0],lyricList[1])
                }
            }
            MusicSource.LOCAL->{}
        }
    }

    //传入volume设置音量
    fun setStreamVolume(volume:Int){
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volume,AudioManager.FLAG_SHOW_UI)
    }

    //获取当前媒体音
    fun getCurrentVolume():Int{
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }
}