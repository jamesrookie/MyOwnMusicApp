package com.atguigu.myownmusicapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.res.ResourcesCompat
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.constants.MusicSource
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URL

object BitmapUtils {
    /**
     * 暴露给外界这个方法，其他方法私有，建议异步实现
     * @param context Context
     * @param source MusicSource
     * @param str String
     * @return Bitmap
     */
    fun getBitmap(context:Context,source: MusicSource,str:String):Bitmap{
        if(str.isEmpty()) return getDefaultBitmap(context)
        return when(source){
            MusicSource.LOCAL-> getBitmapFromLocalUri(context,Uri.parse(str))
            MusicSource.DOWNLOAD_NETEASE,
            MusicSource.DOWNLOAD_MIGU2,
            MusicSource.DOWNLOAD_MIGU,
            MusicSource.DOWNLOAD_KUWO,
            MusicSource.DOWNLOAD_KUGOU,
            MusicSource.DOWNLOAD_QQ-> getBitmapFromFile(context,str)
            MusicSource.KUWO,
            MusicSource.MIGU,
            MusicSource.NETEASE,
            MusicSource.MIGU2,
            MusicSource.KUGOU-> getBitmapFromUrl(context,str)
            else-> getDefaultBitmap(context)
        }
    }
    /**
     * 从网络链接中获取图片
     * @param context Context
     * @param imgUrl String
     * @return Bitmap
     */
    private fun getBitmapFromUrl(context: Context,imgUrl:String): Bitmap {
        val url = URL(imgUrl)
        return try {
            val input = url.openStream()
            //这里有可能为空
            BitmapFactory.decodeStream(input)?: getDefaultBitmap(context)
        } catch (e: IOException) {
            e.printStackTrace()
            getDefaultBitmap(context)
        }
    }

    /**
     * 从本地文件获取图片
     * @param context Context
     * @param filePath String
     * @return Bitmap
     */
    private fun getBitmapFromFile(context: Context,filePath:String):Bitmap{
        val file= File(filePath)
        if(!file.exists()) return getDefaultBitmap(context)
        return try {
            val inputStream=FileInputStream(file)
            //这里有可能为空
            BitmapFactory.decodeStream(inputStream)?: getDefaultBitmap(context)
        }catch (e:IOException){
            e.printStackTrace()
            getDefaultBitmap(context)
        }
    }

    /**
     * 从本地uri地址获取图片对象
     * @param context Context
     * @param uri Uri
     * @return Bitmap
     */
    private fun getBitmapFromLocalUri(context: Context,uri: Uri):Bitmap{
        var bmp: Bitmap?
        val contentResolver=context.contentResolver
        bmp = try {
            if(Build.VERSION.SDK_INT<28){
                MediaStore.Images.Media.getBitmap(contentResolver,uri)
            }else{
                val source= ImageDecoder.createSource(contentResolver,uri)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getDefaultBitmap(context)
        }
        if (bmp == null) {
            bmp = getDefaultBitmap(context)
        }
        return bmp
    }

    /**
     * 在其他方法获取Bitmap对象失效的时候调用该方法，获得一个默认的Bitmap
     * @return Bitmap
     */
    private fun getDefaultBitmap(context: Context):Bitmap{
        //如果是非xml文件可以这么用
        val substitute=BitmapFactory.decodeResource(context.resources, R.drawable.disc)
        //对于xml文件，需要如下步骤：
        val drawable=ResourcesCompat.getDrawable(context.resources,R.drawable.default_pic_for_song_with_no_pic,context.theme)
        val canvas=Canvas()
        val bitmap=
            drawable?.intrinsicHeight?.let {
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    it,Bitmap.Config.ARGB_8888)
            }
        canvas.setBitmap(bitmap)
        drawable?.setBounds(0,0,drawable.intrinsicWidth,drawable.intrinsicHeight)
        drawable?.draw(canvas)
        return bitmap?:substitute
    }
}