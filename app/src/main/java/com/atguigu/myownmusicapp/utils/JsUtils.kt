package com.atguigu.myownmusicapp.utils

import android.content.Context
import com.eclipsesource.v8.V8
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object JsUtils {
    //搜素咪咕音乐时用
    fun executeJsMiGuSearch(context: Context,text:String,timeStamp:Long):String{
        val inputStream: InputStream =context.assets.open("testMyFreeMp3.js")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val runtime: V8 = V8.createV8Runtime()
        val results= StringBuilder()
        while(true){
            val line: String = reader.readLine() ?: break
            results.append(line)
        }
        val inputAsString=results.toString()
        reader.close()
        val encodedName=runtime.executeStringScript(inputAsString+"generateSearchParam(\"$text\",$timeStamp);")
        runtime.release()
        return encodedName
    }
    //请求咪咕音乐的播放链接时使用
    fun executeJsMiGuSongUrl(context:Context,id:String,timeStamp: Long):String{
        val inputStream: InputStream =context.assets.open("testMyFreeMp3.js")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val runtime: V8 = V8.createV8Runtime()
        val results= StringBuilder()
        while(true){
            val line: String = reader.readLine() ?: break
            results.append(line)
        }
        val inputAsString=results.toString()
        reader.close()
        val encodedName=runtime.executeStringScript(inputAsString+"generateMp3Param(\"$id\",128,$timeStamp);")
        runtime.release()
        return encodedName
    }
    //请求咪咕音乐的歌词字符串时使用
    fun executeJsMiGuLrc(context:Context,id:String,timeStamp: Long):String{
        val inputStream: InputStream =context.assets.open("testMyFreeMp3.js")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val runtime: V8 = V8.createV8Runtime()
        val results= StringBuilder()
        while(true){
            val line: String = reader.readLine() ?: break
            results.append(line)
        }
        val inputAsString=results.toString()
        reader.close()
        val encodedName=runtime.executeStringScript(inputAsString+"generateLrcParam(\"$id\",$timeStamp);")
        runtime.release()
        return encodedName
    }
    //搜索咪咕音乐2时用
    fun executeJs2(context: Context,text:String):String{
        val inputStream: InputStream =context.assets.open("testMiGu.js")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val runtime: V8 = V8.createV8Runtime()
        val results= StringBuilder()
        while(true){
            val line: String = reader.readLine() ?: break
            results.append(line)
        }
        val inputAsString=results.toString()
        reader.close()
        val encodedName=runtime.executeStringScript(inputAsString+"generateMiGu2Param('$text');")
        runtime.release()
        return encodedName
    }
}