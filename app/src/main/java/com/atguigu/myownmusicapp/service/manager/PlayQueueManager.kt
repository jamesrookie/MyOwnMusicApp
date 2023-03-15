package com.atguigu.myownmusicapp.service.manager

import android.app.Application
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.STANDARD_SONG
import com.atguigu.myownmusicapp.constants.PlayMode
import com.atguigu.myownmusicapp.utils.SharedPreferencesHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * 播放队列管理，负责队列的增删改查，并暴露api给外部
 */
//定义四种播放状态，1、不在非空的播放列表 2、在播放列表但不是当前播放的这一首 3、在播放列表且正在播放
//4、不在空的播放列表
const val STATUS_NOT_IN_NON_EMPTY_PLAYLIST="NOT_IN_NON_EMPTY_PLAYLIST"
const val STATUS_IN_PLAYLIST_NOT_PLAYING="IN_PLAYLIST_NOT_PLAYING"
const val STATUS_IN_PLAYLIST_PLAYING="IN_PLAYLIST_PLAYING"
const val STATUS_NOT_IN_EMPTY_PLAYLIST="NOT_IN_EMPTY_PLAYLIST"


private const val TAG = "myTag-PlayQueueManager"
class PlayQueueManager(private val application: Application) {
    //播放队列
    var queueList:ArrayList<MediaSessionCompat.QueueItem> =ArrayList()
    //记录当前播放的歌曲id
    var currentPlayId:Int=-1
    //todo 播放模式后面会从sharedPreference中读取
    var playMode= PlayMode.PLAY_IN_ORDER

    //初始化加载保存的数据
    fun loadLastPlayQueue(){
        loadPlayQueue()
        loadPlayRelatedData()
    }
    /**
     * 返回播放队列的长度
     * @return Int
     */
    fun getQueueListSize():Int{
        return queueList.size
    }
    /**
     * 返回当前正在播放的歌曲
     * @return StandardSong?
     */
    fun getCurrentPlayingSong(): StandardSong? {
        if (queueList.isEmpty() || currentPlayId < 0) return null
        val queueItem = queueList[currentPlayId]
        return queueItem.description.extras?.getParcelable(STANDARD_SONG)
    }
    fun getPrevSong():StandardSong?{
        if (queueList.isEmpty()) return null
        var prevPlayId:Int
        prevPlayId = currentPlayId - 1
        //如果是最后一首，就从0开始
        if (prevPlayId < 0) prevPlayId = queueList.size-1
        currentPlayId=prevPlayId//修改当前播放id
        val description: MediaDescriptionCompat = queueList[prevPlayId].description
        return description.extras?.getParcelable(STANDARD_SONG)
    }
    /**
     * 根据不同的播放模式返回下一首歌曲
     * @param flag 表示在单曲循环模式下是否是人为跳转到下一首
     * @return StandardSong?
     */
    fun getNextSong(flag: Boolean?=null): StandardSong? {
        if(queueList.isEmpty()) return null
        var nextPlayId:Int
        val originalPlayId=currentPlayId
        // 单曲循环的优先考虑
        if(playMode==PlayMode.PLAY_SINGLE_LOOP && flag!=true){
            val description: MediaDescriptionCompat = queueList[currentPlayId].description
            return description.extras?.getParcelable(STANDARD_SONG)
        }
        //先不管播放模式，先看有没有NEXT_PLAY的标志的
        nextPlayId = currentPlayId + 1
        //如果是最后一首，就从0开始
        if (nextPlayId >= queueList.size) nextPlayId = 0
        currentPlayId=nextPlayId//修改当前播放id
        var description: MediaDescriptionCompat = queueList[nextPlayId].description
        if(description.extras?.getBoolean(NEXT_PLAY)==true || playMode==PlayMode.PLAY_IN_ORDER){//是否有设置下一个播放
            return description.extras?.getParcelable(STANDARD_SONG)
        }else{
            currentPlayId=originalPlayId //如果没有设置下一个播放就回滚
        }
        return when(playMode){
            PlayMode.PLAY_SINGLE_LOOP->{//函数走到这里表示我们是在单曲循环模式下人为跳转到下一首
                currentPlayId=nextPlayId //修改播放id
                description.extras?.getParcelable(STANDARD_SONG)
            }
            PlayMode.PLAY_RANDOM->{
                /**随机播放需要考虑的几种情况
                 *  1、只有一首歌曲，那么就还是单曲循环
                 *  2、随机数加减也要随机
                 * */
                return if(queueList.size==1){//一首的话还是单曲循环
                    description= queueList[currentPlayId].description
                    description.extras?.getParcelable(STANDARD_SONG)
                }else{
                    nextPlayId=(1 until queueList.size).random()+currentPlayId
                    if(nextPlayId>=queueList.size) nextPlayId-=queueList.size
                    currentPlayId=nextPlayId
                    description= queueList[nextPlayId].description
                    description.extras?.getParcelable(STANDARD_SONG)
                }
            }
            else->{return null}
        }
    }
    /**
     * 清空播放列表，然后重置所有状态
     */
    fun deleteAllQueueItem(){
        queueList.clear()
    }

    /**
     * 把StandardSong对象转为QueueItem
     * @param standardSong StandardSong
     * @return MediaSessionCompat.QueueItem
     */
    private fun standardSongToQueueItem(standardSong: StandardSong): MediaSessionCompat.QueueItem {
        val bundle = Bundle()
        bundle.putParcelable(STANDARD_SONG, standardSong)
        val description = MediaDescriptionCompat.Builder()
            .setTitle(standardSong.name)
            .setDescription("${standardSong.source}")
            .setMediaId("id")
            .setSubtitle(standardSong.artists)
            .setExtras(bundle)//额外的自定义的数据
            .build()
        val id: Long = queueList.size.toLong()
        return MediaSessionCompat.QueueItem(description, id)
    }
    //todo 添加很多首歌到播放列表，比如一整个列表的歌曲

    /**
     * 返回这首歌在播放列表里的序号，如果不存在返回-1
     * @param standardSong StandardSong
     * @return Int
     */
    private fun checkQueueNumber(standardSong: StandardSong):Int{
        queueList.forEachIndexed { index, queueItem ->
            val description: MediaDescriptionCompat =queueItem.description
            if(standardSong.source.toString()==description.description &&
                standardSong.name==description.title &&
                standardSong.artists==description.subtitle){
                //1、如果已经在播放列表
                return index
            }
        }
        //2、如果不在播放列表
        return -1
    }

    /**
     * 切换播放模式
     */
    fun changePlayMode():PlayMode {
        playMode=when(playMode){
            PlayMode.PLAY_IN_ORDER ->{
                PlayMode.PLAY_RANDOM
            }
            PlayMode.PLAY_RANDOM ->{
                PlayMode.PLAY_SINGLE_LOOP
            }
            PlayMode.PLAY_SINGLE_LOOP ->{
                PlayMode.PLAY_IN_ORDER
            }
        }
        return playMode
    }

    //在播放的时候就把"下一首播放"的标签移除
    fun removeNextPlayTag(){
        if(queueList.isEmpty() || currentPlayId==-1)return
        queueList[currentPlayId].description.extras?.remove(NEXT_PLAY)
    }

    /**
     * 添加到播放队列并播放
     * @param standardSong StandardSong
     * @param ifInQueue Int 是否在播放队列，默认值null
     * @return String 取值只能是下列四种情况之一：
     *      STATUS_NOT_IN_NON_EMPTY_PLAYLIST表示播放列表非空，且不在其中
     *      STATUS_IN_PLAYLIST_NOT_PLAYING表示在播放列表，但不是正在播放的歌曲
     *      STATUS_IN_PLAYLIST_PLAYING表示正在播放的歌曲
     *      STATUS_NOT_IN_EMPTY_PLAYLIST表示播放列表空，且不在其中
     */
    fun addToQueueAndPlay(standardSong: StandardSong,ifInQueue:Int?=null):String{
        var checkIfInQueue:Int?=ifInQueue//因为kotlin函数中的参数是val，所以无法修改
        if(checkIfInQueue==null)checkIfInQueue = checkQueueNumber(standardSong)
        //分成几种情况
        when (checkIfInQueue) {
            -1 -> {
                //1、不在播放队列
                val mediaQueueItem = standardSongToQueueItem(standardSong)
                val flag= queueList.isEmpty() //记录播放队列是否为空
                queueList.add(mediaQueueItem)
                currentPlayId=queueList.size-1
                return if(flag) STATUS_NOT_IN_EMPTY_PLAYLIST else STATUS_NOT_IN_NON_EMPTY_PLAYLIST
            }
            currentPlayId -> {
                //2、在播放队列且在播放
                //需要在MusicService判断指令是否是由Dialog发出的
                return STATUS_IN_PLAYLIST_PLAYING
            }
            else -> {
                //3、在播放队列但不在播放
                currentPlayId=checkIfInQueue
                return STATUS_IN_PLAYLIST_NOT_PLAYING
            }
        }
    }

    /**
     * 把指定歌曲插入播放列表，放在正在播放的歌曲的后面
     * @param standardSong StandardSong
     * @return String
     *      STATUS_NOT_IN_NON_EMPTY_PLAYLIST表示播放列表非空，且不在其中
     *      STATUS_IN_PLAYLIST_NOT_PLAYING表示在播放列表，但不是正在播放的歌曲
     *      STATUS_IN_PLAYLIST_PLAYING表示正在播放的歌曲
     *      STATUS_NOT_IN_EMPTY_PLAYLIST表示播放列表空，且不在其中
     */
    fun insertSpecifyQueueItem(standardSong: StandardSong): String {
        //分成几种情况，一种是插入正在播放的歌曲，一种是插入在列表但不在播放的歌曲，一种是不在列表的歌曲
        val checkQueueNumber = checkQueueNumber(standardSong)
        val queueItem=standardSongToQueueItem(standardSong)
        return when (checkQueueNumber) {
            -1 -> {//不在播放列表，就先给歌曲添加一个标签
                queueItem.description.extras?.putBoolean(NEXT_PLAY,true)
                val flag = queueList.isEmpty()
                if(flag){//不在空的播放列表
                    addToQueueAndPlay(standardSong,-1)
                }else{//不在非空的播放列表
                    queueList.add(currentPlayId+1,queueItem)
                    STATUS_NOT_IN_NON_EMPTY_PLAYLIST
                }
            }
            currentPlayId -> {
                //在播放列表且正在播放，什么也不做
                STATUS_IN_PLAYLIST_PLAYING
            }
            else -> {
                //在播放列表，且不在播放，需要先删除原有位置的item，再插入
                deleteSpecifyQueueItem(queueItem)
                queueItem.description.extras?.putBoolean(NEXT_PLAY,true)
                queueList.add(currentPlayId+1,queueItem)
                STATUS_IN_PLAYLIST_NOT_PLAYING
            }
        }
    }

    /**
     * 删除指定的queueItem
     * @param queueItem QueueItem
     * @param ifInQueue Int? 默认空
     * @return Boolean 返回5种状态之一
     *      STATUS_NOT_IN_NON_EMPTY_PLAYLIST表示播放列表非空，且不在其中
     *      STATUS_IN_PLAYLIST_NOT_PLAYING表示在播放列表，但不是正在播放的歌曲
     *      STATUS_IN_PLAYLIST_PLAYING表示正在播放的歌曲
     *      STATUS_NOT_IN_EMPTY_PLAYLIST表示播放列表空，且不在其中
     *      null
     */
    fun deleteSpecifyQueueItem(queueItem: MediaSessionCompat.QueueItem,ifInQueue: Int?=null):String?{
        //分情况讨论，一种是删除的正在播放的歌曲，另一种是删除其他歌曲
        val standardSong = queueItem.description.extras?.getParcelable<StandardSong>(
            STANDARD_SONG
        )
            ?: return null //如果standardSong为空，直接返回空
        var idToDelete=ifInQueue
        if(idToDelete==null) idToDelete=checkQueueNumber(standardSong)
        if(idToDelete==-1 && queueList.isEmpty()) return STATUS_NOT_IN_EMPTY_PLAYLIST
        if(idToDelete==-1 && queueList.isNotEmpty())return STATUS_NOT_IN_EMPTY_PLAYLIST
        val flag=(idToDelete==currentPlayId)
        if(idToDelete<=currentPlayId) currentPlayId-- //后续跳到下一首时实际上还是这个序号，所以这里要-1
        queueList.removeAt(idToDelete)
        return if(flag) STATUS_IN_PLAYLIST_PLAYING else STATUS_IN_PLAYLIST_NOT_PLAYING
    }

    /**
     * 加载播放队列到内存中
     */
    private fun loadPlayQueue(){
        val gson=Gson()
        //1、把json字符串从json文件中读取出来
        val filePath=application.getExternalFilesDir(null)?.absolutePath+File.separator+"playQueueList.json"
        val file=File(filePath)
        if(file.exists()){
            val readText = file.readText(Charsets.UTF_8)
            val type=object: TypeToken<ArrayList<StandardSong>>() {}.type
            val fromJson = gson.fromJson<ArrayList<StandardSong>>(readText, type)
            fromJson.forEach {
                queueList.add(standardSongToQueueItem(it))
            }
        }
    }

    /**
     * 加载上次播放的数据到内存中
     */
    private fun loadPlayRelatedData(){
        playMode=SharedPreferencesHelper.readPlayMode(application)
        currentPlayId=SharedPreferencesHelper.readCurrentPlayNumber(application)
    }
    /**
     * 保存播放队列到数据库或者txt文档
     */
    private fun savePlayQueue() {
        //1、先把对象转为json字符串
        val gson=Gson()
        val standardSongList=ArrayList<StandardSong>()
        for (queueItem in queueList) {
            val song=queueItem.description.extras?.getParcelable<StandardSong>(STANDARD_SONG)
            if (song != null) {
                standardSongList.add(song)
            }
        }
        val toJson = gson.toJson(standardSongList)
        //2、把json字符串写入到json文件中
        val filePath=application.getExternalFilesDir(null)?.absolutePath+File.separator+"playQueueList.json"
        val file=File(filePath)
        file.writeText(toJson,Charsets.UTF_8)
    }

    /**
     * 保存播放模式和当前播放的歌曲，以及进度等数据（播放进度在MediaPlayerManager中保存）到sharedPreferences
     */
    private fun savePlayRelatedData() {
        SharedPreferencesHelper.savePlayMode(application,playMode)
        SharedPreferencesHelper.saveCurrentPlayNumber(application,currentPlayId)
    }

    fun saveQueueData() {
        savePlayQueue()
        savePlayRelatedData()
    }

    /**
     * 只添加到播放列表中，但不播放
     * @param standardSong StandardSong
     */
    fun addToQueue(standardSong: StandardSong) {
        queueList.add(standardSongToQueueItem(standardSong))
    }
}