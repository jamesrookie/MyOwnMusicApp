package com.atguigu.myownmusicapp

import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MusicSource
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val url="https://www.kuwo.cn/"
        val client=OkHttpClient()
        val request=Request.Builder().url(url).get().build()
        val response=client.newCall(request).execute()
        if(response.isSuccessful){
            val headers=response.headers()
            val cookie=headers.values("Set-Cookie")
            println(cookie[0].split(";")[0])
        }
    }

    @Test
    fun test01(){
        val file= File("D:\\test.db")
        println(file.length())
    }
    @Test
    fun test02(){
        val list= listOf(1,2,3,4,5,6,7,8,9,10)

        for(i in 0..100){
            //println(Random.nextInt(0,list.size))
            println((1 until 10).random())
        }
    }
    @Test
    fun test03(){
        val s1=StandardSong(MusicSource.DOWNLOAD_MIGU2,1,"五十","https://ds",
        "艺术家","hlrddd","dfaga",null,null,null)
        val s2=StandardSong(MusicSource.DOWNLOAD_MIGU2,2,"五十","https:/s",
            "艺术家","hlrdddddd","da",null,null,null)
        println(s1 == s2)
    }
    @Test
    fun test04(){
        val action=Action2()
        action.check('a')
    }
    @Test
    fun test05(){
        val artists = arrayListOf(1,2,3).forEach {
            it
        }
        println(artists)
    }
}
open class Action1{
    private var a:String
    init{
        a="abc"
    }
    fun check(ch:Char){
        if(ch in a) println("在")else println("不在")
    }
}
class Action2:Action1(){

}