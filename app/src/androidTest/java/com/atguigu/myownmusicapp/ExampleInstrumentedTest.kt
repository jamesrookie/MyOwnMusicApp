package com.atguigu.myownmusicapp

import android.os.Environment
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.room.PlayQueueData
import com.atguigu.myownmusicapp.utils.BitmapUtils
import com.eclipsesource.v8.V8
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
private const val TAG = "myTag"
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.atguigu.myownmusicapp", appContext.packageName)
    }
    @Test
    fun test01(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val dir= appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.path
        val file= File("$dir${File.separator}myOwnMusic${File.separator}downloadMusic")
        Log.d("girl", "test01: ${file.absolutePath}")
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.d("girl", "getExternalStorageDirectory() $dir")
            /*if(!file.exists()){
                if(!file.mkdirs()){
                    Log.e("girl", "test01: create directory failed.")
                }
            }*/
        }
    }

    @Test
    fun test02(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val filePath=appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val picPath="https://img-prod-cms-rt-microsoft-com.akamaized.net/cms/api/am/imageFileData/RE4wB7c?ver=7fac"
        println("绝对路径"+filePath?.absoluteFile)
        val bitmap = BitmapUtils.getBitmap(appContext, MusicSource.NETEASE, picPath)
        Log.d(TAG, "test02: ${bitmap.height},${bitmap.width}")

    }
    @Test
    fun test03(){
        val standardSong1= StandardSong(MusicSource.LOCAL,null,"song1","hdafd","singer1","dfdd",null,"dgadgf","dfafdfad",null)
        val standardSong2= StandardSong(MusicSource.DOWNLOAD_KUGOU,null,"song2","hdafd","singer1","dfdd",null,"dgadgf","dfafdfad",null)
        val standardSong3= StandardSong(MusicSource.NETEASE,null,"song3","hdafd","singer1","dfdd",null,"dgadgf","dfafdfad",null)
        val standardSong4= StandardSong(MusicSource.LOCAL,null,"song4","hdafd","singer1","dfdd",null,"dgadgf","dfafdfad",null)
        val standardSong5= StandardSong(MusicSource.MIGU,null,"song5","hdafd","singer1","dfdd",null,"dgadgf","dfafdfad",null)
        val standardSong6= StandardSong(MusicSource.LOCAL,null,"song6","hdafd","singer1","dfdd",null,"dgadgf","dfafdfad",null)
        val standardSong7= StandardSong(MusicSource.DOWNLOAD_MIGU,null,"song7","hdafd","singer1","dfdd",null,"dgadgf","dfafdfad",null)
        val standardSong8= StandardSong(MusicSource.MIGU2,null,"song8","hdafd","singer1","dfdd",null,"dgadgf","dfafdfad",null)

        val queue1= PlayQueueData(standardSong1,null,0)
        val queue2= PlayQueueData(standardSong2,null,1)
        val queue2_= PlayQueueData(standardSong2,null,100)

        val queue3= PlayQueueData(standardSong3,null,2)
        val queue4= PlayQueueData(standardSong4,null,3)
        val queue5= PlayQueueData(standardSong5,null,4)
        val queue6= PlayQueueData(standardSong6,null,5)
        val queue7= PlayQueueData(standardSong7,null,6)
        val queue8= PlayQueueData(standardSong8,null,7)
        val list01= mutableListOf(queue1,queue2,queue3,queue4,queue5)
        val list02= mutableListOf(queue2_,queue4,queue6,queue7,queue8)
        val chaji=list01-list02
        val chaji2=list02-list01
        val jiaoji=list01 intersect list02
        println("差集：$chaji")
        println("差集2：$chaji2")
        println("交集:$jiaoji")
    }
    @Test
    fun test04(){
        val result=executeJs("爱如火",1678200303649)
        println(result)
    }
    fun executeJs(text:String,timeStamp:Long):String{
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val inputStream: InputStream =appContext.assets.open("testMyFreeMp3.js")
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
    fun test05(){

    }
}
