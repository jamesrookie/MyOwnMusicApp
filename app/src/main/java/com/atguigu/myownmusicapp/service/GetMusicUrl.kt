package com.atguigu.myownmusicapp.service

import android.content.Context
import android.util.Log
import com.atguigu.myownmusicapp.api.Retrofit163Instance
import com.atguigu.myownmusicapp.api.Retrofit163service
import com.atguigu.myownmusicapp.bean.playeractivitybean.Lyric163
import com.atguigu.myownmusicapp.bean.searchActivitybean.kugou.KuGouSongInfo
import com.atguigu.myownmusicapp.bean.searchActivitybean.kuwo.KuWoSongInfoInner
import com.atguigu.myownmusicapp.bean.searchActivitybean.migu.MiGu2SongUrl
import com.atguigu.myownmusicapp.utils.JsUtils
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException

private const val TAG = "myTag-GetMusicUrl"
class GetMusicUrl {
    companion object{
        private var retrofit163service: Retrofit163service =
            Retrofit163Instance.getRetroInstance().create(Retrofit163service::class.java)
        private var retrofit163service2:Retrofit163service =
            Retrofit163Instance.getRetroInstance2().create(Retrofit163service::class.java)
        private var retrofit163service3:Retrofit163service =
            Retrofit163Instance.getRetroInstance3().create(Retrofit163service::class.java)
        private var retrofit163service4:Retrofit163service =
            Retrofit163Instance.getRetroInstance4().create(Retrofit163service::class.java)
        suspend fun getMusicUrl163(id:Long):String?{
            val result=retrofit163service.get163SongUrl(id)
            return if(result.code==200){
                result.data[0].url
            }else{
                null
            }
        }
        suspend fun getMusicUrlMiGu(context: Context, id: String): String? {
            val timeStamp = System.currentTimeMillis()
            val encoded = JsUtils.executeJsMiGuSongUrl(context, id, timeStamp)
            Log.d(TAG, "getMusicUrlMiGu: ${id},${timeStamp},$encoded")
            val result =
                retrofit163service4.getMiGuSongUrl(id = id, _t = timeStamp, token = encoded)
            Log.d(TAG, "getMusicUrlMiGu: ${result.headers()["location"]}")
            return result.headers()["location"]
        }
        suspend fun getMusicLrcMiGu(context: Context,id:String):String?{
            val timeStamp = System.currentTimeMillis()
            val encoded = JsUtils.executeJsMiGuLrc(context, id, timeStamp)
            val jsonObject = JsonObject()
            try {
                //注意这个添加的顺序一定不能颠倒，不然直接出错
                jsonObject.addProperty("id", id)
                jsonObject.addProperty("token", encoded)
                jsonObject.addProperty("_t", timeStamp)
                Log.d(TAG, "search: $jsonObject")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            Log.d(TAG, "getMusicLrcMiGu: ${id},${timeStamp},$encoded")
            val miGuLrc = retrofit163service3.getMiGuLrc(body = jsonObject.toString())
            val lrc=miGuLrc.data?.lrc
            Log.d(TAG, "getMusicLrcMiGu: $lrc")
            return miGuLrc.data?.lrc
            //return ""
        }
        suspend fun getLyric163(id:Long):Lyric163{
            return retrofit163service.get163Lyric(id)
        }

        suspend fun getMusicUrlMiGu2(id: Long): MiGu2SongUrl {
            return retrofit163service.getMiGu2SongUrl(songId = id, toneFlag = "SQ")
        }

        suspend fun getCookieFromKuWo():String{
            val baseUrl="https://www.kuwo.cn/"
            val client= OkHttpClient()
            val request= Request.Builder().url(baseUrl).get().build()
            var csrf=""
            var cookieNeeded =""
            withContext(Dispatchers.IO){
                val resp:Response=client.newCall(request).execute()
                if(resp.isSuccessful){
                    val headers=resp.headers()
                    val cookie=headers.values("Set-Cookie")
                    cookieNeeded= cookie[0].split(";")[0]
                    csrf=cookieNeeded.split("=")[1]
                }
            }
            return csrf
        }

        suspend fun getKuWoSongInfo(id:Long): KuWoSongInfoInner {
            val result= retrofit163service.getKuWoSongInfo("https://m.kuwo.cn/newh5/singles/songinfoandlrc?musicId=$id")
            Log.e("myTag", "getKuWoSongInfo: $result", )
            return result.data
        }
        suspend fun getMusicUrlKuWo(id:Long):String{
            return retrofit163service2.getKuWoSongUrl("https://antiserver.kuwo.cn/anti.s?type=convert_url&format=mp3&response=url&rid=$id")
        }

        suspend fun getKuGouSongInfo(hash:String,album_id:String): KuGouSongInfo {
            val timestamp=System.currentTimeMillis()
            val response=retrofit163service2.getKuGouSongInfo("https://wwwapi.kugou.com/yy/index.php?r=play/getdata&callback=jQuery&mid=1&hash=$hash&platid=4&album_id=$album_id&_=$timestamp")
            val data=response.removePrefix("jQuery(").removeSuffix(");")
            return Gson().fromJson(data,KuGouSongInfo::class.java)
        }

        //获取QQ音乐的播放链接
        suspend fun getQQSongUrlInfo(songMid:String):String?{
            val jsonObject = JsonObject()
            try {
                //1、构造req0
                val req0=JsonObject()
                req0.addProperty("module","vkey.GetVkeyServer")
                req0.addProperty("method","CgiGetVkey")
                val param=JsonObject()
                val fileNameJsonArray=JsonArray()
                fileNameJsonArray.add("M500${songMid}${songMid}.mp3")
                val songMidJsonArray=JsonArray()
                songMidJsonArray.add(songMid)
                val songTypeJsonArray=JsonArray()
                songTypeJsonArray.add(0)
                param.add("filename", fileNameJsonArray)
                param.addProperty("guid","10000")
                param.add("songmid",songMidJsonArray)
                param.add("songtype",songTypeJsonArray)
                param.addProperty("uin","0")
                param.addProperty("loginflag",1)
                param.addProperty("platform","20")
                req0.add("param",param)
                //2、构造comm
                val comm=JsonObject()
                comm.addProperty("uin","0")
                comm.addProperty("format","json")
                comm.addProperty("ct",24)
                comm.addProperty("cv",0)
                //3、构造最终的json格式
                //注意这个添加的顺序一定不能颠倒，不然直接出错
                jsonObject.add("req_0",req0)
                jsonObject.addProperty("loginUin", "0")
                jsonObject.add("comm",comm)
                Log.d(TAG, "search: $jsonObject")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val result= retrofit163service3.getQQSongUrl(data=jsonObject.toString())
            val purl=result?.req_0?.data?.midurlinfo?.get(0)?.purl
            return if(purl.isNullOrBlank())null else "http://ws.stream.qqmusic.qq.com/$purl"
        }

        /**
         * 请求得到QQ音乐的歌词字符串
         * @param songMid String
         * @return String?
         */
        suspend fun getMusicLrcQQ(songMid: String):String? {
            return retrofit163service.getQQSongLyric(songmid= songMid).lyric
        }

        suspend fun getMusicLrcMigu2(lyricUrl: String):String? {
            return retrofit163service2.getMiGu2SongLyric(lyricUrl)
        }
    }

}
