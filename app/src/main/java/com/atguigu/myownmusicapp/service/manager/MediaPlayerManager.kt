package com.atguigu.myownmusicapp.service.manager

import android.app.Application
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MediaControllerRelated
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.constants.PlayMode
import com.atguigu.myownmusicapp.service.GetMusicUrl
import com.atguigu.myownmusicapp.utils.MusicUrlCheckUtils
import com.atguigu.myownmusicapp.utils.SharedPreferencesHelper
import kotlinx.coroutines.*

/**
 * 用来管理MediaPlayer
 */
private const val TAG = "myTagMediaPlayerManager"
const val NEXT_PLAY = "NEXT_PLAY"
const val CURRENT_POSITION="CURRENT_POSITION"
private const val MEDIA_ID = "mediaId"
class MediaPlayerManager(private val application: Application,
                         focusChangeListener: AudioManager.OnAudioFocusChangeListener
):MediaPlayer.OnPreparedListener,MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener,
MediaPlayer.OnBufferingUpdateListener{
    private var bufferingProgress: Int=0 //缓存的进度，默认0
    private var lastSongPosition:Int?=null //保存在本地的进度
    private var mediaPlayer:MediaPlayer?=MediaPlayer()
    //初始播放器是否准备就位
    private var isPrepared=false
    //记录删除一首歌时的播放状态,外部可访问
    var stateWhileDelete:Boolean?=null
    //播放队列管理PlayQueueManager初始化，使其私有化，只能在当前类操纵PlayQueueManager
    private var queueManager=PlayQueueManager(application)
    //回调函数，用来更新metadata源数据和playbackState，以及metadata
    private lateinit var onUpdateMetaData:(metadata:MediaMetadataCompat.Builder,bitmapUrl:String,source:MusicSource)->Unit
    private lateinit var onUpdatePlaybackState:(playbackState:PlaybackStateCompat?)->Unit
    private lateinit var onUpdateQueue:(queueList:MutableList<MediaSessionCompat.QueueItem>)->Unit
    private var bitmapUrl=""//存放专辑封面
    private var musicSource=MusicSource.UNKNOWN//默认的音乐来源
    private var myAudioManager:MyAudioManager
    init {
        //初始化的一些工作
        mediaPlayer?.apply {
            //唤醒锁定模式，关闭屏幕时，CPU不休眠
            setWakeMode(application,PowerManager.PARTIAL_WAKE_LOCK)
            setOnPreparedListener(this@MediaPlayerManager)
            setOnCompletionListener(this@MediaPlayerManager)
            setOnErrorListener(this@MediaPlayerManager)
            setOnBufferingUpdateListener(this@MediaPlayerManager)
        }
        //初始化音频管理器
        myAudioManager=MyAudioManager(application,focusChangeListener)
        //设置音频属性
        mediaPlayer?.setAudioAttributes(myAudioManager.playbackAttributes)
    }
    //设置本地音乐播放路径
    //测试网络url转uri和本地uri
    fun setUriPath(uri: Uri){
        stop()//重置状态
        isPrepared=false
        //设置新音乐链接时应重置（以免音乐仍然在播放的时候，报IllegalStateException）
        mediaPlayer?.reset()
        Log.d(TAG, "setPath: $uri")
        mediaPlayer?.apply {
            setDataSource(application,uri)
            prepareAsync()
        }
    }
    //设置网络音乐链接播放路径，因为使用Uri会出现异常，但不影响播放
    fun setStringPath(path:String){
        stop()//重置状态
        isPrepared=false
        mediaPlayer?.reset()
        mediaPlayer?.apply {
            setDataSource(path)
            prepareAsync()
        }
    }
    /**
     * 开始播放
     * (1)如果已经在播放或者还没准备好，就什么也不做
     * (2)如果暂停了，就从暂停的地方继续播放
     */
    fun start(){
        if(mediaPlayer?.isPlaying == true || !isPrepared)return
        //请求音频焦点
        val registerAudioFocus = myAudioManager.registerAudioFocus()
        if(registerAudioFocus==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            mediaPlayer?.start()
            //更新playbackState
            onUpdatePlaybackState(buildPlaybackState())
        }
    }

    /**
     * 注意这里没有使用stop()，因为stop()后再start()就得从头开始播放了
     * Pauses playback. Call start() to resume.
     */
    fun pause(isFocusLossTransient: Boolean=false){
        if(mediaPlayer?.isPlaying == true)mediaPlayer?.pause() else return
        //更新playbackState
        onUpdatePlaybackState(buildPlaybackState())
        //释放音频焦点
        if(!isFocusLossTransient)//不是短暂失去音频焦点就不释放
            myAudioManager.releaseAudioFocus()
    }

    /**
     * 播放器停止
     */
    fun stop(isFocusLossTransient:Boolean=false){
        if(mediaPlayer?.isPlaying==true){
            mediaPlayer?.stop()
            //更新playbackState
            onUpdatePlaybackState(buildPlaybackState())
            //释放音频焦点,不是短暂失去音频焦点就不释放
            if(!isFocusLossTransient)
                myAudioManager.releaseAudioFocus()
        }
        //此时不重置状态，因为再次播放同一首歌曲的时候会调用start方法
        //isPrepared=false
    }
    /**
     * 播放下一曲,playMode这里是给单曲循环的手动下一首留的
     * @param flag Boolean表示在单曲循环模式下是否是人为跳转到下一首
     */
    fun playNext(flag:Boolean?=null):StandardSong?{
        //1、先获得下一首歌曲的standardSong
        val standardSong: StandardSong? = queueManager.getNextSong(flag)
        //2、判断该歌曲来源并设置播放路径
        when (standardSong?.source) {
            MusicSource.LOCAL -> {
                //本地扫描出来的音乐使用uri播放，否则用字符串路径播放
                setUriPath(Uri.parse(standardSong.url_320))
            }
            MusicSource.NETEASE -> {
                GlobalScope.launch(Dispatchers.IO){
                    standardSong.id?.let { standardSong.url_320=GetMusicUrl.getMusicUrl163(it) }
                    standardSong.url_320?.let { setStringPath(it) }
                }
            }
            MusicSource.MIGU2->{
                GlobalScope.launch(Dispatchers.IO) {
                    //获取播放链接和歌词链接
                    val item=standardSong.id?.let { it1 ->
                        GetMusicUrl.getMusicUrlMiGu2(
                            it1
                        ).data
                    }
                    standardSong.url_320 =item?.url
                    standardSong.lrc =item?.songItem?.lrcUrl
                    standardSong.url_320?.let { setStringPath(it) }
                }

            }
            else -> {
                standardSong?.url_320?.let { setStringPath(it) }
            }
        }
        return standardSong
    }

    /**
     * 播放前一曲,这里我就不区分播放模式了，因为随机播放模式下找不回上一首歌曲了，要保存已经播放过的歌曲需要建一个历史播放列表
     * 较为繁琐
     */
    fun playPrevious():StandardSong?{
        val standardSong:StandardSong?=queueManager.getPrevSong()
        when (standardSong?.source) {
            MusicSource.LOCAL -> {
                //本地扫描出来的音乐使用uri播放，否则用字符串路径播放
                setUriPath(Uri.parse(standardSong.url_320))
            }
            MusicSource.NETEASE -> {
                GlobalScope.launch(Dispatchers.IO){
                    standardSong.id?.let { standardSong.url_320=GetMusicUrl.getMusicUrl163(it) }
                    standardSong.url_320?.let { setStringPath(it) }
                }
            }
            else -> {
                standardSong?.url_320?.let { setStringPath(it) }
            }
        }
        return standardSong
    }
    /**
     * 获取当前播放状态
     * @return Boolean
     */
    fun isPlaying():Boolean{
        return mediaPlayer?.isPlaying?:false
    }
    fun getPlayingProgress(){
        if(isPrepared) {
            //异步更新playbackState,然后把进度放在playbackState
            onUpdatePlaybackState(buildPlaybackState())
        }
    }

    /**
     * 拖动播放进度条到指定位置
     * @param pos Long
     */
    fun seekTo(pos: Long) {
        if(isPrepared){
            mediaPlayer?.seekTo(pos.toInt())
        }
    }

    /**
     * 改变播放模式
     * @return PlayMode
     */
    fun changePlayMode(): PlayMode {
        return queueManager.changePlayMode()
    }

    /**
     * MediaPlayer对象会消耗大量的系统资源，因此您应仅使其保留必要的时长，并在操作完成后调用release()。
     * 请务必明确调用此清理方法，而非依赖于系统垃圾回收，因为垃圾回收器要经过一段时间才会回收MediaPlayer，
     * 原因在于它仅对内存需求敏感，而对缺少其他媒体相关资源并不敏感。因此，当您使用Service时，应始终替换
     * onDestroy()方法以确保释放 MediaPlayer
     */
    fun onDestroy(){
        mediaPlayer?.release()
        mediaPlayer=null
        //释放音频管理类资源
        myAudioManager.onDestroy()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        val registerAudioFocus = myAudioManager.registerAudioFocus()
        //todo 假设我这里不写start,那么在focusChangeListener里面会接收到并开始播放吗？答案是不能
        if(registerAudioFocus==AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            mediaPlayer?.start()
        isPrepared=true
        //适用于从本地加载数据后播放
        if(lastSongPosition!=null){
            mediaPlayer?.seekTo(lastSongPosition!!)
            lastSongPosition=null
            mediaPlayer?.pause()
        }
        if(stateWhileDelete==false){
            mediaPlayer?.pause()
        }  //这里表示删除的歌曲没有在播放，那么我们不播放下一首
        //在播放的时候就把"下一首播放"的标签移除
        queueManager.removeNextPlayTag()
        stateWhileDelete=null //用完重置状态
        //这里调用回调函数onUpdateData来更新元数据和播放状态
        //更新playbackState和metadata
        onUpdatePlaybackState(buildPlaybackState())
        buildMetaData()?.let { onUpdateMetaData(it,bitmapUrl,musicSource) }
        //更新播放队列
        onUpdateQueue(queueManager.queueList)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if(mediaPlayer?.isPlaying == true){
            mediaPlayer?.stop()
        }
        Log.d(TAG, "onCompletion: 播放结束")
        //播放完了以后应该是播放下一首
        playNext()
        //EventBus.getDefault().post(PlayMusicEndEvent())//通知Activity切换播放状态图标
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        when(what){
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK->{
                //App.toastSomeThing("MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK")
            }
            MediaPlayer.MEDIA_ERROR_UNKNOWN->{
                //App.toastSomeThing("MEDIA_ERROR_UNKNOWN")
            }
            MediaPlayer.MEDIA_ERROR_SERVER_DIED->{
                //App.toastSomeThing("MEDIA_ERROR_SERVER_DIED")
            }
        }
        return false
    }

    /**
     * 重置相关参数
     */
    private fun resetParam() {
        isPrepared=false
        queueManager.currentPlayId=-1 //重置播放队列中正在播放的歌曲
        bufferingProgress=0
        stateWhileDelete=null
        //更新播放队列
        onUpdateQueue(queueManager.queueList)
    }

    /**
     * 清空播放列表，然后重置所有状态
     */
    fun deleteAllQueueItem(){
        queueManager.deleteAllQueueItem()
        resetParam()
    }

    /**
     * 插入一首歌到下一首播放
     * @param standardSong StandardSong
     * @return Any
     */
    fun insertSpecifyQueueItem(standardSong: StandardSong): String {
        val result=queueManager.insertSpecifyQueueItem(standardSong)
        when(result){
            STATUS_IN_PLAYLIST_NOT_PLAYING,
            STATUS_NOT_IN_NON_EMPTY_PLAYLIST->{
                onUpdateQueue(queueManager.queueList)
                buildMetaData()?.let { onUpdateMetaData(it,bitmapUrl,musicSource) }
            }
            else->{}
        }
        return result
    }
    /**
     * 删除队列中的一首歌
     * @param queueItemToDelete QueueItem
     * @return String?
     */
    fun deleteSpecifyQueueItem(queueItemToDelete: MediaSessionCompat.QueueItem): String? {
        val result=queueManager.deleteSpecifyQueueItem(queueItemToDelete)
        when(result){
            STATUS_IN_PLAYLIST_NOT_PLAYING->{
                //更新播放队列和元数据，确保dialog中正在播放的歌曲能红色显示
                buildMetaData()?.let { onUpdateMetaData(it,bitmapUrl,musicSource) }
                onUpdateQueue(queueManager.queueList)
            }
            else->{}
        }
        //删除后如果播放列表清空了，就重置相关播放参数
        if(queueManager.getQueueListSize()==0) resetParam()
        return result
    }

    /**
     * 添加到播放队列并播放
     * @param standardSong StandardSong
     * @return String
     */
    fun addToQueueAndPlay(standardSong: StandardSong):String{
        if(queueManager.getQueueListSize()==0) resetParam()
        return queueManager.addToQueueAndPlay(standardSong)
    }

    /**
     * 添加歌单中的所有歌曲到播放列表
     * @param songList List<StandardSong>
     */
    fun addPlayListToQueue(songList:List<StandardSong>){
        //todo 1、加入新的播放列表
        //第0首歌直接开始播放，后面的添加进来就行
        queueManager.addToQueueAndPlay(songList[0])
        for(i in 1 until songList.size) queueManager.addToQueue(songList[i])
    }
    /**
     * 获取当前正在播放的歌曲列表
     * @return MutableList<MediaSessionCompat.QueueItem>
     */
    fun getQueueList(): MutableList<MediaSessionCompat.QueueItem> {
        return queueManager.queueList
    }

    /**
     * 将回调函数赋值给成员变量
     * @param function Function2<[@kotlin.ParameterName] MediaMetadataCompat?, [@kotlin.ParameterName] PlaybackStateCompat?, Unit>
     */
    fun setOnUpdateMetaData(function:(metadata:MediaMetadataCompat.Builder?,bitmapUrl:String,source:MusicSource)->Unit){
        this.onUpdateMetaData=function
    }
    fun setOnPlaybackState(function:(playbackState:PlaybackStateCompat?)->Unit){
        this.onUpdatePlaybackState=function
    }
    fun setOnQueueChange(function:(queueList:MutableList<MediaSessionCompat.QueueItem>)->Unit){
        this.onUpdateQueue=function
    }
    /**
     * 构建新的媒体元数据，主要包含歌曲的相关信息（歌名，歌手，时长，专辑封面）
     * @return MediaMetadataCompat?
     */
    private fun buildMetaData():MediaMetadataCompat.Builder?{
        val standardSong = queueManager.getCurrentPlayingSong()?:return null
        //如果为空就给默认值0
        var totalDuration =0L
        if(isPrepared) totalDuration= (mediaPlayer?.duration?:0).toLong()
        bitmapUrl=standardSong.picUrl?:""
        musicSource=standardSong.source?:MusicSource.UNKNOWN
        return MediaMetadataCompat.Builder()
            .putLong(MEDIA_ID,standardSong.id?:-1)//id
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE,standardSong.name)//标题
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,standardSong.artists)//作者
            //按需求可以添加别的内容 比如唱片，媒体时长
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,totalDuration)
            //描述里放歌曲来源
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,standardSong.source.toString())
            //放歌词
            .putString(MediaControllerRelated.METADATA_KEY_LYRIC,standardSong.lrc)
            //放歌曲播放链接
            .putString(MediaControllerRelated.METADATA_KEY_PLAY_URL,standardSong.url_320)
            //放专辑封面地址
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,standardSong.picUrl)
    }

    /**
     * 构建新的播放状态
     * @return PlaybackStateCompat?
     */
    private fun buildPlaybackState(): PlaybackStateCompat? {
        val newState=if(isPlaying())PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        //如果为空，给一个默认值0
        val newPosition:Long= (mediaPlayer?.currentPosition?:0).toLong()
        val newSpeed=1.0f
        return PlaybackStateCompat.Builder()
            .setState(newState,newPosition,newSpeed)
                //todo 这里的actions我一直不明白是干嘛的
            .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
            .setBufferedPosition(bufferingProgress.toLong())
            .build()
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        Log.d(TAG, "onBufferingUpdate: $percent")
        bufferingProgress=percent
    }

    /**
     * 保存音频数据到本地
     */
    fun saveMediaData() {
        Log.d(TAG, "saveMediaData: 没保存进度吗")
        //1、保存进度
        savePosition()
        //2、保存播放队列
        queueManager.saveQueueData()
    }

    /**
     * 保存进度
     */
    private fun savePosition() {
        var position =-1
        if(isPrepared)position=mediaPlayer?.currentPosition?:-1
        SharedPreferencesHelper.savePlayPosition(application,position)
    }

    /**
     * 加载保存在本地的播放进度
     */
    private fun loadPosition(){
        lastSongPosition=SharedPreferencesHelper.readPlayPosition(application)
    }

    /**
     * 加载上一次播放时的数据,采用协程
     */
    suspend fun loadLastData() {
        //todo 初始化歌曲数据，从上次保存的sharedPreferences中读取
        queueManager.loadLastPlayQueue()//先加载队列
        val currentPlayingSong = queueManager.getCurrentPlayingSong()
        Log.d(TAG, "currentPlayingSong是: $currentPlayingSong")
        loadPosition()
        if(currentPlayingSong!=null){
            try {
                when(currentPlayingSong.source){

                    MusicSource.LOCAL->setUriPath(Uri.parse(currentPlayingSong.url_320))
                    MusicSource.UNKNOWN->{}
                    MusicSource.DOWNLOAD_KUGOU,MusicSource.DOWNLOAD_NETEASE,MusicSource.DOWNLOAD_KUWO,
                    MusicSource.DOWNLOAD_MIGU,MusicSource.DOWNLOAD_MIGU2->{
                        val songUrl=currentPlayingSong.url_320
                        if(songUrl!=null)setStringPath(songUrl)
                    }
                    MusicSource.NETEASE-> {
                        MusicUrlCheckUtils.completeStandardSong(currentPlayingSong)
                        val songUrl=currentPlayingSong.url_320
                        //todo 播放链接过时的话可能需要重新获取
                        if(songUrl!=null)setStringPath(songUrl)
                    }
                    MusicSource.MIGU2->{
                        //获取播放链接和歌词链接
                        val item=currentPlayingSong.id?.let { it1 ->
                            GetMusicUrl.getMusicUrlMiGu2(
                                it1
                            ).data
                        }
                        currentPlayingSong.url_320 =item?.url
                        currentPlayingSong.lrc =item?.songItem?.lrcUrl
                        currentPlayingSong.url_320?.let { setStringPath(it) }
                    }
                    MusicSource.KUWO->{
                        val item = currentPlayingSong.id?.let { it1 ->
                            GetMusicUrl.getMusicUrlKuWo(
                                it1
                            )
                        }
                        currentPlayingSong.url_320 = item
                        val results = StringBuilder()
                        val lyricKuWo = currentPlayingSong.id?.let { GetMusicUrl.getKuWoSongInfo(it) }
                        if (lyricKuWo != null) {
                            for (obj in lyricKuWo.lrclist) {
                                val time =
                                    "[0${obj.time.toFloat().toInt() / 60}:${obj.time.toFloat() % 60}]"
                                results.append(time).append(obj.lineLyric).append("\n")
                            }
                        }
                        currentPlayingSong.lrc = results.toString()
                        currentPlayingSong.picUrl = lyricKuWo?.songinfo?.pic
                        currentPlayingSong.url_320?.let { setStringPath(it) }
                    }
                    MusicSource.KUGOU->{
                        val splitList=currentPlayingSong.url_flac?.split("$")
                        val hash=splitList?.get(0)
                        val albumId= splitList?.get(1)
                        if(hash!=null && albumId!=null){
                            val item=GetMusicUrl.getKuGouSongInfo(hash,albumId)
                            currentPlayingSong.picUrl = item.data.img
                            currentPlayingSong.url_320 = item.data.play_url
                            currentPlayingSong.lrc = item.data.lyrics
                            currentPlayingSong.url_320?.let { setStringPath(it) }
                        }
                    }
                    MusicSource.QQ->{
                        currentPlayingSong.url_128?.let { musicId->
                            GetMusicUrl.getQQSongUrlInfo(musicId)
                        }
                        currentPlayingSong.url_320?.let { setStringPath(it) }
                    }
                    MusicSource.MIGU->{
                        val songHashOrId=currentPlayingSong.url_128
                        //1、获取播放连接
                        currentPlayingSong.url_320=songHashOrId?.let{songHash->
                            GetMusicUrl.getMusicUrlMiGu(application,songHash)
                        }

                        currentPlayingSong.url_320?.let {
                            setStringPath(it)
                        }
                    }
                    else->{//todo 这里暂时先不写
                        currentPlayingSong.url_320?.let { setStringPath(it) }
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
    }
}