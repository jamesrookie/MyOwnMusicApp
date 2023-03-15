package com.atguigu.myownmusicapp.service.refactor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.media.MediaBrowserServiceCompat
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.ClickFromSource
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.ALL_SONGS_KEY
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.BUNDLE_PLAY_MODE_KEY
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.CHANGE_PLAY_MODE_ACTION
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.DELETE_ALL_SONGS_ACTION
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.DELETE_SPECIFY_SONG_ACTION
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.EVENT_OPEN_PLAYER_ACTIVITY
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.INSERT_NEXT_PLAY_ACTION
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.MANUALLY_SKIP_TO_NEXT_ACTION
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.MANUALLY_SKIP_TO_NEXT_KEY
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.PLAY_ACTION
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.PLAY_ALL_SONGS
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.QUEUE_ITEM_TO_DELETE
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.QUEUE_ITEM_TO_INSERT
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.REQUEST_PROGRESS_ACTION
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.SAVE_MEDIA_DATA_ACTION
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.STANDARD_SONG
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.service.manager.*
import com.atguigu.myownmusicapp.utils.BitmapUtils
import com.atguigu.myownmusicapp.utils.MusicUrlCheckUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.*
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion as MediaControllerRelated1

//定义常量
private const val MY_MEDIA_ROOT_ID="media_root_id"
private const val MY_EMPTY_ROOT_ID="empty_root_id"
private const val TAG = "myTag-MusicService"
const val MEDIA_CHANNEL_ID=10001 //开启前台服务时候的Id

/**
 * MusicService依赖于manager包下的MediaPlayerManager类，MediaPlayerManager类又依赖于PlayQueueManager
 * @property mediaPlayerManager MediaPlayerManager
 * @property myNotificationManager MyNotificationManager
 * @property broadcastReceiver MyBlueToothBroadcastReceiver
 * @property mediaSession MediaSessionCompat?
 * @property playbackState PlaybackStateCompat
 * @property mediaSessionCallback Callback
 */
class MusicService:MediaBrowserServiceCompat() {
    //mediaPlayerManager
    private lateinit var mediaPlayerManager:MediaPlayerManager
    //NotificationManager
    private lateinit var myNotificationManager:MyNotificationManager
    //通知的广播接收者，以及接收耳机插拔
    private lateinit var broadcastReceiver: MyBlueToothBroadcastReceiver
    //前台服务是否已经启动
    private var isStartForeground=false
    private var isStopped=true
    //是否短暂失去音频焦点
    private var isFocusLossTransient=false
    //协程
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    //mediaSession
    private var mediaSession:MediaSessionCompat?=null
    //playbackState
    private lateinit var playbackState:PlaybackStateCompat
    //mediaSessionCallback
    private var mediaSessionCallback:MediaSessionCompat.Callback=object:
        MediaSessionCompat.Callback() {
        /**
         * onPlay()中需要做的事情：
         * 1、获取音频焦点requestFocus()
         * 2、开启服务startService()
         * 3、媒体会话更新元数据和状态setActive(true)
         * 4、启动播放器
         * 5、发出噪音，注册BroadcastReceiver
         * 6、通知栏显示startForeground(notification)
         */
        override fun onPlay() {
            super.onPlay()
            //todo 获取并请求音频焦点
            startService(Intent(this@MusicService,MusicService::class.java))
            mediaSession?.isActive=true
            //mediaPlayer开始从头播放或者继续播放
            mediaPlayerManager.start()
            isStopped=false
            //更新通知
            updateNotification()
        }
        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            addToQueueAndPlay(true,uri.toString(),extras)
        }

        /**
         * 这个我们就可以认为是从网络链接进行播放
         * @param query String  这个就是播放链接
         * @param extras Bundle
         */
        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            super.onPlayFromSearch(query, extras)
            addToQueueAndPlay(false,query,extras)
        }

        /**
         * 添加到播放列表并开始播放
         * @param isUri Boolean
         * @param str String
         * @param extras Bundle?
         */
        fun addToQueueAndPlay(isUri:Boolean,str:String?,extras: Bundle?){
            if(str==null){
                Log.d(TAG, "onPlayFromUri: 音乐Uri或Url为空")
                Toast.makeText(this@MusicService,"音乐无法播放，请尝试其他歌曲",Toast.LENGTH_SHORT).show()
            }else{
                //从extras中取出播放来源
                val clickFromSource=extras?.getSerializable(MediaControllerRelated1.PLAY_FROM_KEY)
                //添加到播放队列
                extras?.getParcelable<StandardSong>(STANDARD_SONG)?.let {
                    when(mediaPlayerManager.addToQueueAndPlay(it)){
                        STATUS_IN_PLAYLIST_PLAYING->{
                            //已经在播放而且不是在playlistDialog就打开PlayerActivity
                            if(clickFromSource== ClickFromSource.CLICK_FROM_OTHERS){
                                mediaSession?.sendSessionEvent(EVENT_OPEN_PLAYER_ACTIVITY,null)
                            }
                        }
                        else->{
                            if(isUri) mediaPlayerManager.setUriPath(Uri.parse(str))
                            else mediaPlayerManager.setStringPath(str)
                        }
                    }
                }
                //这一步调用onPlay方法进行相应的初始化操作
                onPlay()
            }
        }
        /**
         * onPause()中应该做的事情：
         * 1、更新元数据和状态
         * 2、暂停播放器
         * 3、取消注册BroadcastReceiver
         * 4、通知stopForeground(false)
         */
        override fun onPause() {
            super.onPause()
            //暂停播放器
            mediaPlayerManager.pause(isFocusLossTransient)
            isFocusLossTransient=false
            //Take the service out of the foreground,retain the notification(服务仍然运行)
            //更新通知
            updateNotification()
            stopNotification()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            mediaPlayerManager.playNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            mediaPlayerManager.playPrevious()
        }

        /**
         * onStop()中应该做的事情
         * 1、abandonAudioFocus
         * 2、停止服务stopSelf()
         * 3、媒体会话setActive(false)并更新元数据和状态
         * 4、停止播放器
         * 5、通知stopForeground(false)
         */
        override fun onStop() {
            super.onStop()
            Log.d(TAG, "onStop:MusicService")
            //todo 放弃音频焦点
            stopSelf()//停止服务
            mediaSession?.isActive=false
            //停止mediaPlayer播放器
            mediaPlayerManager.stop(isFocusLossTransient)
            isFocusLossTransient=false
            isStopped=true
            stopNotification()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            mediaPlayerManager.seekTo(pos)
        }
        //接收mediaController.transportControls.sendCustomAction命令
        override fun onCustomAction(action: String?, extras: Bundle?) {
            super.onCustomAction(action, extras)
            when(action){
                //接收到客户端发送的来自网易云音乐的播放请求
                PLAY_ACTION->{
                    //从extra中拿到内容
                    val standardSong:StandardSong?=extras?.getParcelable(STANDARD_SONG)
                    //判断是否是本地音乐
                    if(standardSong?.source==MusicSource.LOCAL){
                        val musicUri=Uri.parse(standardSong.url_320)
                        onPlayFromUri(musicUri,extras)
                    }else{
                        val songUrl=standardSong?.url_320
                        onPlayFromSearch(songUrl,extras)
                    }
                }
                //修改播放模式，从PlayerActivity或miniPlayer的播放列表里调用
                CHANGE_PLAY_MODE_ACTION->{
                    val newPlayMode = mediaPlayerManager.changePlayMode()
                    val extra=Bundle()
                    //注意枚举类也是可序列化对象
                    extra.putSerializable(BUNDLE_PLAY_MODE_KEY,newPlayMode)
                    updateExtras(extra)
                }
                //删除所有歌曲
                DELETE_ALL_SONGS_ACTION->{
                    mediaPlayerManager.deleteAllQueueItem()
                    //直接关闭
                    onStop()
                }
                //删除指定歌曲
                DELETE_SPECIFY_SONG_ACTION->{
                    val queueItemToDelete:MediaSessionCompat.QueueItem?=extras?.getParcelable(QUEUE_ITEM_TO_DELETE)
                    if (queueItemToDelete != null) {
                        //判断是不是当前正在播放的歌曲以及是否正在播放
                        when(mediaPlayerManager.deleteSpecifyQueueItem(queueItemToDelete)){//是就跳到下一首
                            STATUS_IN_PLAYLIST_PLAYING->{
                                //先停止播放
                                mediaPlayerManager.stateWhileDelete = mediaPlayerManager.isPlaying()
                                onStop()
                                onSkipToNext()
                            }
                            else->{}
                        }
                    }
                }
                //添加到下一首播放
                INSERT_NEXT_PLAY_ACTION->{
                    val standardSong: StandardSong = extras?.getParcelable(QUEUE_ITEM_TO_INSERT)
                        ?: return
                    val insertSpecifyQueueItem =
                            mediaPlayerManager.insertSpecifyQueueItem(standardSong)
                        //播放列表为空，而我们新加进来一首歌曲
                        if(insertSpecifyQueueItem== STATUS_NOT_IN_EMPTY_PLAYLIST){
                            if(standardSong.source ==MusicSource.LOCAL){
                                val musicUri=Uri.parse(standardSong.url_320)
                                mediaPlayerManager.setUriPath(musicUri)
                            }else{
                                val songUrl=standardSong.url_320
                                if (songUrl != null) {
                                    mediaPlayerManager.setStringPath(songUrl)
                                }
                            }
                            onPlay()
                        }
                }
                //手动播放下一首，此时我们分几种情况:1、单曲循环模式下 2、非单曲循环模式下
                MANUALLY_SKIP_TO_NEXT_ACTION->{
                    val flag=extras?.getBoolean(MANUALLY_SKIP_TO_NEXT_KEY)
                    mediaPlayerManager.playNext(flag)
                }
                REQUEST_PROGRESS_ACTION->{
                    //告知当前进度条，更改playbackState，返回给客户端
                    mediaPlayerManager.getPlayingProgress()
                }
                SAVE_MEDIA_DATA_ACTION->{//保存相关数据到本地
                    scope.launch {
                        //由于保存相关数据是耗时操作，所以在后台进行
                        mediaPlayerManager.saveMediaData()
                    }
                }
                PLAY_ALL_SONGS->{//把歌单中所有歌曲添加到播放列表
                    val songList:ArrayList<StandardSong>? =extras?.getParcelableArrayList(ALL_SONGS_KEY)
                    mediaPlayerManager.deleteAllQueueItem()//先删除原有的
                    //直接开始播放第一首
                    val standardSong = songList?.get(0)
                    if(standardSong?.source ==MusicSource.LOCAL){
                        val musicUri=Uri.parse(standardSong.url_320)
                        mediaPlayerManager.setUriPath(musicUri)
                    }else{
                        scope.launch {
                            if (standardSong != null) {
                                MusicUrlCheckUtils.completeStandardSong(standardSong)
                                Log.d(TAG, "onCustomAction: 新的url:${standardSong.url_320}")
                                if (standardSong.url_320 != null) {
                                    mediaPlayerManager.setStringPath(standardSong.url_320!!)
                                }
                            }
                        }
                    }
                    onPlay()
                    //后面的是耗时操作
                    scope.launch {
                        songList?.let { mediaPlayerManager.addPlayListToQueue(it) }
                    }
                }
            }
        }
    }

    //需要重写父类MediaBrowserServiceCompat的两个方法：onGetRoot和onLoadChildren
    /**
     * 此方法用来控制对对服务的访问
     * onGetRoot()方法返回内容层次结构的根节点。如果该方法返回null，则会拒绝连接。
    要允许客户端连接到您的服务并浏览其媒体内容，onGetRoot()必须返回非null的BrowserRoot这是代表您的内容层次结构的根ID。
    要允许客户端连接到您的 MediaSession 而不进行浏览，onGetRoot()仍然必须返回非null的 BrowserRoot但此根ID应代表一个空的内容层次结构
     * @param clientPackageName String
     * @param clientUid Int
     * @param rootHints Bundle?
     * @return BrowserRoot?
     */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MY_MEDIA_ROOT_ID,null)
    }

    /**
     * 此方法使客户端能够构建和显示内容层次结构菜单。客户端连接后，可以通过重复调用MediaBrowserCompat.subscribe()来遍历内容层次结构，以构建界面的本地表示方式。
     * subscribe()方法将回调onLoadChildren()发送给服务，该服务会返回MediaBrowser.MediaItem对象的列表。每个MediaItem都有一个唯一的ID字符串，
     * 这是一个不透明令牌。当客户端想要打开子菜单或播放某项内容时，它就会传递此ID。您的服务负责将此ID与相应的菜单节点或内容项关联起来。
     * @param parentId String
     * @param result Result<MutableList<MediaItem>>
     */
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        //将信息从当前线程中移除，允许后续调用sendResult方法
        result.detach()
        //不允许浏览
        when (parentId) {
            MY_EMPTY_ROOT_ID -> {
                result.sendResult(null)
                return
            }
            MY_MEDIA_ROOT_ID -> {
                //异步加载数据
                //填充数据
                val mediaItems:ArrayList<MediaBrowserCompat.MediaItem> = ArrayList()
                result.sendResult(mediaItems)
            }
            else -> {
                Log.d(TAG, "onLoadChildren: parentId错误")
            }
        }
    }

    /**
     * 1、初始化playbackState
     * 2、初始化mediaSession
     */
    override fun onCreate() {
        super.onCreate()
        initPlaybackState()
        initMediaSession()
        initManager()
        Log.d(TAG, "onCreate: ")
        //注册广播接收器
        registerReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: $this")
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 销毁MediaPlayerManager中的mediaPlayer对象
     */
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerManager.onDestroy()
        stopSelf()
        Log.d(TAG, "onDestroy: 调用了服务终止？")
        myNotificationManager.onDestroy()
        //解绑广播接收者
        unregisterReceiver(broadcastReceiver)
    }

    /**
     * 初始化音频管理器、音频焦点管理器、通知管理器，还有广播接收者
     */
    private fun initManager() {
        mediaPlayerManager= MediaPlayerManager(application) { focusChange: Int ->
            Log.d(TAG, "focusChange: $focusChange")
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> mediaSession?.controller?.transportControls?.play()
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    isFocusLossTransient=true
                    mediaSession?.controller?.transportControls?.pause()
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    isFocusLossTransient=true
                    mediaSession?.controller?.transportControls?.pause()
                }
            }
        }
        //加载上一次的数据
        scope.launch { mediaPlayerManager.loadLastData() }
        myNotificationManager= MyNotificationManager(application)
        broadcastReceiver=MyBlueToothBroadcastReceiver()
        mediaPlayerManager.setOnUpdateMetaData { metadata,bitmapUrl,musicSource ->
            if (metadata==null) return@setOnUpdateMetaData
            var bitmap:Bitmap
            when(musicSource){
                MusicSource.KUWO,
                MusicSource.MIGU,
                MusicSource.NETEASE,
                MusicSource.MIGU2,
                MusicSource.QQ,
                MusicSource.KUGOU->{
                    scope.launch{
                        //异步获取bitmap
                        Log.d(TAG, "initManager: 当前线程:${Thread.currentThread()},时间:${SystemClock.uptimeMillis()}")
                        //todo 判断bitmap的链接是否是空的或null
                        if(bitmapUrl.isBlank()){
                            bitmap=BitmapUtils.getBitmap(application,MusicSource.UNKNOWN,"")
                            metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,bitmap)
                            mediaSession?.setMetadata(metadata.build())
                            //更新通知
                            updateNotification()
                        }else{
                            Glide.with(this@MusicService)
                                .asBitmap()
                                .load(bitmapUrl)
                                .listener(object:RequestListener<Bitmap>{
                                    override fun onResourceReady(
                                        resource: Bitmap?,
                                        model: Any?,
                                        target: Target<Bitmap>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        bitmap = resource ?: BitmapUtils.getBitmap(application,MusicSource.UNKNOWN,"")
                                        metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,bitmap)
                                        mediaSession?.setMetadata(metadata.build())
                                        //更新通知
                                        updateNotification()
                                        Log.d(TAG, "initManager: 当前线程:${Thread.currentThread()},时间:${SystemClock.uptimeMillis()}")
                                        return false
                                    }

                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Bitmap>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        return false
                                    }
                                })
                                .submit(256,256)
                        }
                    }
                }
                else->{
                    bitmap=BitmapUtils.getBitmap(application,musicSource,bitmapUrl)
                    metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,bitmap)
                    mediaSession?.setMetadata(metadata.build())
                    //更新通知
                    updateNotification()
                }
            }

        }
        mediaPlayerManager.setOnPlaybackState { playbackState ->
            if (playbackState != null) {
                this.playbackState=playbackState
            }
            if(playbackState!=null) mediaSession?.setPlaybackState(playbackState)
        }
        mediaPlayerManager.setOnQueueChange{queueList->
            mediaSession?.setQueue(queueList)
        }
    }

    /**
     * 1、创建并初始化媒体会话MediaSession
     * 2、设置媒体会话回调MediaSessionCallback
     * 3、设置媒体会话令牌
     */
    private fun initMediaSession() {
        mediaSession= MediaSessionCompat(this, TAG).apply {
            //允许MediaButtons和TransportControls的回调
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                    or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            //设置playbackState
            setPlaybackState(playbackState)
            //设置回调来响应控制器指定的回调
            setCallback(mediaSessionCallback)
            //设置令牌，让客户端能够连接
            setSessionToken(sessionToken)
        }
    }

    /**
     * 初始化playbackState
     */
    private fun initPlaybackState() {
        playbackState=PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE,0,1.0f)
            //Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            //应该和MediaSession设置的flags有关
            .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE)
            .build()
    }

    /**
     * 注册广播接收者
     */
    private fun registerReceiver(){
        val intentFilter=IntentFilter()
        intentFilter.addAction(playIntentActionStr)
        intentFilter.addAction(pauseIntentActionStr)
        intentFilter.addAction(nextIntentActionStr)
        intentFilter.addAction(prevIntentActionStr)
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)//耳机插拔
        registerReceiver(broadcastReceiver,intentFilter)
    }

    /**
     * 更新通知
     */
    fun updateNotification(){
        val token=mediaSession?.sessionToken
        val metadata=mediaSession?.controller?.metadata
        val playbackState=mediaSession?.controller?.playbackState
        val isPlaying=playbackState?.state==PlaybackStateCompat.STATE_PLAYING
        if(metadata!=null && token!=null){
            if(isStartForeground)
                myNotificationManager.updateNotification(metadata,isPlaying,token)
            else{
                val notification = myNotificationManager.getNotification(metadata, isPlaying, token)
                startForeground(MEDIA_CHANNEL_ID,notification)
                isStartForeground=true
            }
        }
    }

    /**
     * 停止通知
     */
    fun stopNotification(){
        Log.d(TAG, "stopNotification: 停止前台显示，isStopped:$isStopped")
        stopForeground(isStopped)
        isStartForeground=false
    }

    /**
     * 更新extras中的数据，这里我用来存放播放模式，mediaSession.setExtras(Bundle extras)会由服务端接收到onExtrasChanged回调
     * @param extras Bundle
     */
    private fun updateExtras(extras:Bundle){
        mediaSession?.setExtras(extras)
    }

    //内部类：广播接收者，用来接收notification发送的广播
    inner class MyBlueToothBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: 没接收到广播")
            val controller= mediaSession?.controller ?: return
            val transportControls=controller.transportControls
            when(intent?.action){
                playIntentActionStr->{transportControls.play()}
                pauseIntentActionStr->{transportControls.pause()}
                nextIntentActionStr->{transportControls.skipToNext()}
                prevIntentActionStr->{transportControls.skipToPrevious()}
                closeIntentActionStr->{
                    Log.d(TAG, "onReceive: 关闭前台服务通知")
                    transportControls.pause()
                }
                AudioManager.ACTION_AUDIO_BECOMING_NOISY->{
                    transportControls.pause()
                }
            }
        }
    }
}