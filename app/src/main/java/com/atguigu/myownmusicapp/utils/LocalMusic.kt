package com.atguigu.myownmusicapp.utils

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MusicSource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/*
* 扫描本地音乐
* */
private const val TAG = "myTag-LocalMusic"
object LocalMusic {
    //未知错误query failed,handle error
    private const val ERROR_UNKNOWN=0
    //无音乐
    private const val ERROR_NO_MUSIC=1

    private lateinit var  downloadManager:DownloadManager
    private var standardSong:StandardSong?=null
    private var downloadId:Long?=null
    /*
    * 扫描本地音乐
    * @param success 成功返回歌单集合
    * */
    @RequiresApi(Build.VERSION_CODES.R)
    fun scanLocalMusic(context: Context, success:(ArrayList<StandardSong>)->Unit, failure:(Int)->Unit){
        val songList=ArrayList<StandardSong>()
        val resolver:ContentResolver=context.contentResolver
        val externalContentUri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //过滤规则
        val sortOrder=MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        val cursor: Cursor?=resolver.query(externalContentUri,null,null,null,sortOrder)
        when{
            cursor==null->{
                MainScope().launch {
                    Toast.makeText(context,"cursor=null",Toast.LENGTH_SHORT).show()
                }
                failure.invoke(ERROR_UNKNOWN)
            }
            !cursor.moveToFirst()->{
                MainScope().launch {
                    Toast.makeText(context,"没有音乐",Toast.LENGTH_SHORT).show()
                }
                failure.invoke(ERROR_NO_MUSIC)
            }
            else->{
                MainScope().launch {
                    Toast.makeText(context,"有音乐",Toast.LENGTH_SHORT).show()
                }
                //要读取的数据表列
                val titleColumn=cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)//标题，音乐名称
                val songIdColumn=cursor.getColumnIndex(MediaStore.Audio.Media._ID) //音乐id
                val artistColumn=cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST) //艺术家
                val dataColumn=cursor.getColumnIndex(MediaStore.Audio.Media.DATA) //路径
                val albumIdColumn=cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)//专辑id
                val sizeColumn=cursor.getColumnIndex(MediaStore.Audio.Media.SIZE) //大小
                do{
                    val id=cursor.getLong(songIdColumn)
                    val data=cursor.getString(dataColumn)
                    val albumId=cursor.getLong(albumIdColumn)
                    val title=cursor.getString(titleColumn)
                    var artist=cursor.getString(artistColumn)
                    val size=cursor.getLong(sizeColumn)

                    val coverUri=getAlbumCover(albumId)
                    //歌曲的Uri
                    //通过_id字段获取音乐Uri
                    val musicUri: Uri = ContentUris.withAppendedId(externalContentUri,id)
                    //过滤无法播放的歌曲
                    if(title=="" && artist=="<unknown>"){
                        continue
                    }
                    if(artist=="<unknown>"){
                        artist="未知歌手"
                    }
                    //大小为0过滤
                    if(size==0L || size<=500_000){
                        continue
                    }
                    val song=StandardSong(
                        MusicSource.LOCAL,
                        id,
                        title,
                        coverUri.toString(),
                        artist,
                        null,
                        null,
                        null,
                        musicUri.toString(),
                        null
                    )
                    songList.add(song)
                }while (cursor.moveToNext())
                success.invoke(songList)
            }
        }
        cursor?.close()
    }

    private fun getAlbumCover(albumId: Long): Uri {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
    }
}