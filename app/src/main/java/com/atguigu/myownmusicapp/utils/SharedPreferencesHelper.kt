package com.atguigu.myownmusicapp.utils

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.atguigu.myownmusicapp.constants.PlayMode
import com.atguigu.myownmusicapp.service.manager.CURRENT_POSITION

object SharedPreferencesHelper {
    private const val USER_COOKIE_KEY="USER_COOKIE"
    private const val SHP_NAME="MyOwnMusicApp"
    private const val SHP_PLAY_MODE_KEY="playMode"
    private const val SHP_CURRENT_PLAY_ID_KEY="currentPlayId"
    private const val USER_SEARCH_ENGINE_KEY="USER_SEARCH_ENGINE"

    /**
     * 读取播放模式
     * @param application Application
     * @return PlayMode
     */
    fun readPlayMode(application:Application):PlayMode{
        val sharedPreferences=application.getSharedPreferences(SHP_NAME, MODE_PRIVATE)
        //1、加载播放模式
        return when(sharedPreferences.getInt(SHP_PLAY_MODE_KEY,0)){
            0-> PlayMode.PLAY_IN_ORDER
            1->PlayMode.PLAY_RANDOM
            2->PlayMode.PLAY_SINGLE_LOOP
            else -> PlayMode.PLAY_IN_ORDER
        }
        //currentPlayId=sharedPreferences.getInt(SHP_CURRENT_PLAY_ID_KEY,-1)
    }

    /**
     * 保存播放模式
     * @param application Application
     * @param playMode PlayMode
     */
    fun savePlayMode(application:Application,playMode:PlayMode){
        val sharedPreferences=application.getSharedPreferences(SHP_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //1、保存播放模式
        editor.putInt(SHP_PLAY_MODE_KEY, playMode.ordinal)
        //2、保存当前播放到哪一首歌曲
        //editor.putInt(SHP_CURRENT_PLAY_ID_KEY,currentPlayId)
        editor.apply()
    }

    /**
     * 保存当前播放位置
     * @param application Application
     * @param position Int
     */
    fun savePlayPosition(application: Application,position:Int){
        val sharedPreferences=application.getSharedPreferences(SHP_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //1、保存当前播放位置
        editor.putInt(CURRENT_POSITION,position)
        editor.apply()
    }

    /**
     * 读取上一次保存的位置
     * @param application Application
     * @return Int?
     */
    fun readPlayPosition(application:Application):Int?{
        val sharedPreferences=application.getSharedPreferences(SHP_NAME, MODE_PRIVATE)
        var lastSongPosition:Int?=sharedPreferences.getInt(CURRENT_POSITION,-1)
        if(lastSongPosition==-1)lastSongPosition=null
        return lastSongPosition
    }

    fun saveCurrentPlayNumber(application: Application,currentPlayId: Int) {
        val sharedPreferences=application.getSharedPreferences(SHP_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //1、保存当前播放位置
        editor.putInt(SHP_CURRENT_PLAY_ID_KEY,currentPlayId)
        editor.apply()
    }

    fun readCurrentPlayNumber(application: Application):Int {
        val sharedPreferences=application.getSharedPreferences(SHP_NAME, MODE_PRIVATE)
        return sharedPreferences.getInt(SHP_CURRENT_PLAY_ID_KEY,-1)
    }

    /**
     * 保存登录的cookie
     * @param context Context
     * @param cookie String?
     */
    fun saveCookie(context: Context,cookie: String?) {
        val sharedPreferences=context.getSharedPreferences(SHP_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //1、保存用户cookie
        if(cookie!=null){
            editor.putString(USER_COOKIE_KEY,cookie)
            editor.apply()
        }
    }

    /**
     * 读取用户cookie
     * @param context Context
     * @return String?
     */
    fun readCookie(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(SHP_NAME, MODE_PRIVATE)
        return sharedPreferences.getString(USER_COOKIE_KEY, "")
    }

    /**
     * 去除登录状态
     * @param context Context
     */
    fun clearCookie(context: Context) {
        val sharedPreferences=context.getSharedPreferences(SHP_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(USER_COOKIE_KEY)
        editor.apply()
    }

    /**
     * 保存当前搜索用的引擎
     * @param engineString String
     */
    fun saveSearchEngine(context:Context,engineString: String) {
        val sharedPreferences=context.getSharedPreferences(SHP_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(USER_SEARCH_ENGINE_KEY,engineString)
        editor.apply()
    }

    /**
     * 读取当前搜索用的引擎
     * @param context Context
     * @return String
     */
    fun readSearchEngine(context: Context):String{
        val sharedPreferences = context.getSharedPreferences(SHP_NAME, MODE_PRIVATE)
        val result=sharedPreferences.getString(USER_SEARCH_ENGINE_KEY, "网易云")
        return result?:"网易云"
    }
}