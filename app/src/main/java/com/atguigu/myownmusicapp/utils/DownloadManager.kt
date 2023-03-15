package com.atguigu.myownmusicapp.utils

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MusicSource
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

const val DOWNLOAD_SUCCESS=1
const val DOWNLOAD_FAIL=0
const val DOWNLOAD_FILE_EXIST=-1
private const val TAG = "myTag-DownloadManager"
object DownloadManager {
    /**
     * 判断一个音乐文件是否存在
     * @param fileName String
     * @return Boolean
     */
    private fun checkMusicExist(fileName:String?):Boolean{
        if (fileName==null) return false
        val file=File(fileName)
        return file.exists()
    }

    /**
     * 下载音乐到本地/storage/emulated/0/Android/packageName/files/Music
     * @param application Application
     * @param standardSong StandardSong
     * @return Int 状态值，是本kt文件上面定义的三种状态之一
     */
    fun downloadMusic(application:Application,standardSong: StandardSong,songUrl:String):Int {
        try {
            val url = URL(songUrl)
            val urlConnection=url.openConnection()
            val inputStream: InputStream =urlConnection.getInputStream()
            //储存的文件夹的名字
            val downloadFolderName= application.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath
            Log.d("myTag", "download: $downloadFolderName")
            downloadFolderName?.let {
                val file =File(downloadFolderName)
                if(!file.exists()){
                    val flag=file.mkdir()
                    Log.d("myTag", "download: 创建了文件夹,$flag")
                }
            }
            //文件名字
            val prefix:String=parseMusicSource(standardSong.source)
            val suffix =".mp3"
            //不然"/"会被当作目录分割符
            val musicFileName=
                handleSpecialSymbol(prefix+"$"+standardSong.name+"$"+standardSong.artists+suffix)
            val fileName:String=downloadFolderName+File.separator+musicFileName
            Log.d(TAG, "downloadMusic: 文件路径:$fileName")
            val musicFile=File(fileName)
            if(musicFile.exists()){
                return DOWNLOAD_FILE_EXIST
            }
            val bytes=ByteArray(1024)
            var length: Int
            val outPutStream: OutputStream = FileOutputStream(fileName)
            while((inputStream.read(bytes).also { length = it })!=-1){
                outPutStream.write(bytes,0,length)
            }
            //关闭资源
            inputStream.close()
            outPutStream.close()
            return DOWNLOAD_SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            return DOWNLOAD_FAIL
        }
    }

    /**
     * 下载歌词
     * @param application Application
     * @param standardSong StandardSong
     * @param lyricString:String
     * @return Int
     */
    fun saveLyric(application: Application, standardSong: StandardSong,lyricString:String):Int{
        try {
            if(lyricString.isEmpty()){
                 return DOWNLOAD_FAIL
            }
            val lyricInputStream=lyricString.byteInputStream(Charsets.UTF_8)
            //储存的文件夹的名字
            val downloadFolderName= application.getExternalFilesDir(null)?.absolutePath+File.separator+"Lyric"
            Log.d("myTag", "saveLyric: $downloadFolderName")
            val file =File(downloadFolderName)
            if(!file.exists()){
                val flag=file.mkdir()
                Log.d("myTag", "saveLyric: 创建了文件夹,$flag")
            }
            //文件名字
            val prefix:String=parseMusicSource(standardSong.source)
            val suffix =".lrc"
            //不然"/"会被当作目录分割符
            val musicFileName= handleSpecialSymbol(prefix+"$"+standardSong.name+"$"+standardSong.artists+suffix)
            val fileName:String=downloadFolderName+File.separator+musicFileName
            Log.d(TAG, "saveLyric: 文件路径:$fileName")
            val musicFile=File(fileName)
            if(musicFile.exists()){
                return DOWNLOAD_FILE_EXIST
            }
            val bytes=ByteArray(1024)
            var length: Int
            val outPutStream: OutputStream = FileOutputStream(fileName)
            while((lyricInputStream.read(bytes).also { length = it })!=-1){
                outPutStream.write(bytes,0,length)
            }
            //关闭资源
            lyricInputStream.close()
            outPutStream.close()
            return DOWNLOAD_SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            return DOWNLOAD_FAIL
        }
    }

    /**
     * 下载专辑封面
     * @param application Application
     * @param standardSong StandardSong
     * @return Int
     */
    fun downloadMusicCover(application: Application,standardSong: StandardSong,songCover:String):Int{
        try {
            val url = URL(songCover)
            val urlConnection=url.openConnection()
            val inputStream: InputStream =urlConnection.getInputStream()
            //储存的文件夹的名字
            val downloadFolderName= application.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
            Log.d("myTag", "download: $downloadFolderName")
            downloadFolderName?.let {
                val file =File(downloadFolderName)
                if(!file.exists()){
                    val flag=file.mkdir()
                    Log.d("myTag", "download: 创建了文件夹,$flag")
                }
            }
            //文件名字
            val prefix:String=parseMusicSource(standardSong.source)
            val suffix =".png"
            //不然"/"会被当作目录分割符
            val musicFileName= handleSpecialSymbol(prefix+"$"+standardSong.name+"$"+standardSong.artists+suffix)
            val fileName:String=downloadFolderName+File.separator+musicFileName
            Log.d(TAG, "downloadMusic: 文件路径:$fileName")
            val musicFile=File(fileName)
            if(musicFile.exists()){
                return DOWNLOAD_FILE_EXIST
            }
            val bytes=ByteArray(1024)
            var length: Int
            val outPutStream: OutputStream = FileOutputStream(fileName)
            while((inputStream.read(bytes).also { length = it })!=-1){
                outPutStream.write(bytes,0,length)
            }
            //关闭资源
            inputStream.close()
            outPutStream.close()
            return DOWNLOAD_SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            return DOWNLOAD_FAIL
        }
    }

    /**
     * 解析音乐来源
     * @param source MusicSource?
     * @return String
     */
    private fun parseMusicSource(source: MusicSource?): String {
        return "DOWNLOAD_"+when(source){
            MusicSource.NETEASE,MusicSource.DOWNLOAD_NETEASE->"NETEASE"
            MusicSource.MIGU2,MusicSource.DOWNLOAD_MIGU2->"MIGU2"
            MusicSource.KUWO,MusicSource.DOWNLOAD_KUWO->"KUWO"
            MusicSource.KUGOU,MusicSource.DOWNLOAD_KUGOU->"KUGOU"
            MusicSource.MIGU,MusicSource.DOWNLOAD_MIGU->"MIGU"
            MusicSource.QQ,MusicSource.DOWNLOAD_QQ->"QQ"
            else->""
        }
    }

    /**
     * 加载下载了的音乐
     * @param application Application
     * @return ArrayList<StandardSong>
     */
    fun loadDownloadedMusic(application: Application):ArrayList<StandardSong>{
        //todo 1、新建一个ArrayList
        val arr:ArrayList<StandardSong> = ArrayList()
        //todo 1、读取指定目录
        val musicFolder=application.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath
        val lyricFolder=application.getExternalFilesDir(null)?.absolutePath+File.separator+"Lyric"
        val pictureFolder=application.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        if(!checkMusicExist(musicFolder)){
            return arr
        }
        val musicFolderFile=File(musicFolder!!)
        val listFiles = musicFolderFile.listFiles()
        listFiles?.forEach {
            val standardSong:StandardSong
            //此处需要移除掉.mp3后缀
            val filename=it.name.removeSuffix(".mp3").split("$")
            val source=MusicSource.strToMusicSource(filename[0])
            val name= handleSpecialSymbol(filename[1],reverse = true)//将原有名字恢复
            val artists = handleSpecialSymbol(filename[2],reverse = true)
            var picUrl:String?=null//存放专辑封面地址
            var lrc:String?=null//存放歌词
            val picPath = pictureFolder + File.separator + it.name.removeSuffix(".mp3") + ".png"
            val lrcPath=lyricFolder+File.separator + it.name.removeSuffix(".mp3") + ".lrc"
            if(checkMusicExist(picPath)){
                picUrl=picPath
            }
            if(checkMusicExist(lrcPath)){
                lrc=File(lrcPath).readText(Charsets.UTF_8)
            }
            standardSong=StandardSong(source,null,name,picUrl,artists,lrc,null,null,it.absolutePath,null)
            arr.add(standardSong)
        }
        return arr
    }

    /**
     * 处理一些特殊符号，比如?、:、/、\等，他们无法再在文件名字中出现
     * @param str String 输入的要处理的字符串
     * @param reverse Boolean 是否往回处理，也就是还原为原来的字符串，默认false
     * @return String
     */
    private fun handleSpecialSymbol(str:String,reverse:Boolean=false):String{
        return when(reverse){
            true->{
                str.replace("#%%%#","?")
                    .replace("#%%%%#",":")
                    .replace("#%%%%%#","/")
                    .replace("#%%%%%%#","\\")
            }
            false->{
                str.replace("?","#%%%#")
                    .replace(":","#%%%%#")
                    .replace("/","#%%%%%#")
                    .replace("\\","#%%%%%%#")
            }
        }
    }
    //删除已下载的音乐
    fun deleteDownloadedMusic(context: Context, standardSong: StandardSong) {
        //todo 1、解析standardSong拿到文件名
        val name= standardSong.name?.let { handleSpecialSymbol(it) }
        val artists=standardSong.artists?.let { handleSpecialSymbol(it) }
        val source=parseMusicSource(standardSong.source)
        val fileNameWithoutSuffix = "$source$$name$$artists"//无后缀的文件名
        //todo 2、删除对应的音频文件、图片文件、歌词文件
        val musicFolder=context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath
        val lyricFolder=context.getExternalFilesDir(null)?.absolutePath+File.separator+"Lyric"
        val pictureFolder=context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        val mp3File=File(musicFolder+File.separator+fileNameWithoutSuffix+".mp3")
        val picFile=File(pictureFolder+File.separator+fileNameWithoutSuffix+".png")
        val lyricFile=File(lyricFolder+File.separator+fileNameWithoutSuffix+".lrc")
        if(mp3File.exists()) mp3File.delete()
        if(picFile.exists()) picFile.delete()
        if(lyricFile.exists()) lyricFile.delete()
    }

}