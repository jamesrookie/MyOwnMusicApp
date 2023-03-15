package com.atguigu.myownmusicapp.utils

import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.service.GetMusicUrl
import java.net.HttpURLConnection
import java.net.URL

/**
 * 校验音乐播放链接是否有效以及更新播放链接
 */
object MusicUrlCheckUtils {
    /**
     * 校验播放链接的时效性
     * @param standardSong StandardSong
     * @return StandardSong
     */
    suspend fun completeStandardSong(standardSong: StandardSong):StandardSong{
        return when(standardSong.source){
            MusicSource.NETEASE->{
                if(standardSong.url_320==null){
                    //获取播放链接
                    standardSong.id?.let { standardSong.url_320=GetMusicUrl.getMusicUrl163(it) }
                }else{
                    //校验播放链接
                    val url = URL(standardSong.url_320)
                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    val code: Int = connection.responseCode
                    if (code == 200) return standardSong
                    //更新播放链接
                    standardSong.id?.let { standardSong.url_320=GetMusicUrl.getMusicUrl163(it) }
                }
                standardSong
            }
            MusicSource.MIGU->{
                standardSong
            }
            MusicSource.MIGU2->{
                standardSong
            }
            MusicSource.KUGOU->{
                standardSong
            }
            MusicSource.KUWO->{
                standardSong
            }
            else->standardSong
        }
    }
}