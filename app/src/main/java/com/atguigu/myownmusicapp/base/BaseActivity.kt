package com.atguigu.myownmusicapp.base

import android.content.ComponentName
import android.content.Intent
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.constants.MediaControllerRelated
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.BUNDLE_PLAY_MODE_KEY
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.SAVE_MEDIA_DATA_ACTION
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.constants.PlayMode
import com.atguigu.myownmusicapp.databinding.MiniPlayerBinding
import com.atguigu.myownmusicapp.event.*
import com.atguigu.myownmusicapp.service.GetMusicUrl
import com.atguigu.myownmusicapp.service.refactor.MusicService
import com.atguigu.myownmusicapp.ui.PlayerActivity
import com.atguigu.myownmusicapp.ui.dialog.PlaylistDialog
import com.atguigu.myownmusicapp.utils.*
import com.atguigu.myownmusicapp.viewmodel.BaseViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.ref.WeakReference
import java.util.*

/**
 * 基类Activity,在该类中创建MediaBrowser和MediaController对象作为客户端
 */
//常量
private const val TAG = "myTag-BaseActivity"
private const val DOWNLOAD_MSG_WHAT=10001//下载信息的渠道
private const val NO_MUSIC_URL_MSG_WHAT=1002//没有歌曲下载url的渠道
abstract class BaseActivity : AppCompatActivity() {
    //region 成员变量，包括播放器、handler、音频浏览器(及回调)，音频控制器回调
    var miniPlayer:MiniPlayerBinding? =null
    //baseViewModel，用来加载和存放❤的歌曲
    open lateinit var baseViewModel:BaseViewModel
    //handler用来处理弹出下载消息
    private lateinit var handler:BaseHandler
    //音频浏览器
    lateinit var mediaBrowser: MediaBrowserCompat
    //记录当前音乐播放模式
    var currentPlayMode:PlayMode?=null
    //记录当前播放队列
    var currentQueueItem:MutableList<MediaSessionCompat.QueueItem>? =null
    //记录当前元数据
    var currentMetadata: MediaMetadataCompat?=null
    //音频浏览器连接回调
    open val mediaBrowserConnectionCallback: MediaBrowserCompat.ConnectionCallback =object:
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            //必须在确保连接成功的前提下执行订阅的操作
            if(mediaBrowser.isConnected){
                //mediaId即为MediaBrowserService.onGetRoot的返回值
                //若Service允许客户端连接，则返回结果不为null，其值为数据内容层级结构的根ID
                //若拒绝连接，则返回null
                val mediaId:String = mediaBrowser.root
                //Browser通过订阅的方式向Service请求数据，发起订阅请求需要两个参数，其一为mediaId
                //如果该mediaId已经被其他Browser实例订阅，则需要在订阅之前取消mediaId的订阅者
                //虽然订阅一个 已被订阅的mediaId时会取代原Browser的订阅回调，但却无法触发onChildrenLoaded回调
                mediaBrowser.unsubscribe(mediaId)
                //订阅的方法还需要一个参数，即订阅回调SubscriptionCallback
                //当Service获取数据后会将数据发送回来，此时会触发SubscriptionCallback.onChildrenLoaded回调
                mediaBrowser.subscribe(mediaId,browserSubscriptionCallback)
                //获取mediaController
                val mediaController= MediaControllerCompat(this@BaseActivity,mediaBrowser.sessionToken)
                MediaControllerCompat.setMediaController(this@BaseActivity,mediaController)
                mediaController.registerCallback(mediaControllerCallback)
                //此时就需要更新我们的miniPlayer,包括元数据和播放状态
                initMiniPlayerView(mediaController.metadata,mediaController.queue)
                changePauseOrStart(mediaController.playbackState)
            }
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            Log.d(TAG, "onConnectionSuspended: 连接终止！")
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            Log.d(TAG, "onConnectionFailed: 连接失败！")
        }
    }

    //音频浏览器订阅回调
    val browserSubscriptionCallback: MediaBrowserCompat.SubscriptionCallback=object:
        MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            super.onChildrenLoaded(parentId, children)
            Log.d(TAG, "onChildrenLoaded: ")
            //children即为Service发送回来的媒体数据集合
            //拿到数据集合后填充布局
        }
    }

    //音频控制器回调,音频控制器我们不再放入成员变量，而是随取随用
    open var mediaControllerCallback: MediaControllerCompat.Callback=object:
        MediaControllerCompat.Callback() {
        //当服务端运行mediaSession.setPlaybackState就会到达此处
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            Log.d(TAG, "onPlaybackStateChanged: $state")
            changePauseOrStart(state)
        }
        //播放音乐改变的回调，当服务端运行mediaSession.setMetadata就会到达此处
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            Log.d(TAG, "onMetadataChanged: ")
            //媒体数据更改后反映在播放器界面上,更新UI
            val mediaController=MediaControllerCompat.getMediaController(this@BaseActivity)
            initMiniPlayerView(metadata,mediaController.queue)
            //此时需要发布事件，给PlaylistDialog，让它给当前正在播放的歌曲加红色
            currentMetadata=metadata
            EventBus.getDefault().postSticky(QueueModeCurrentSongEvent(
                currentQueueItem,currentPlayMode,currentMetadata
            ))
        }
        //播放模式改变的回调，当服务端运行mediaSession.setExtras就会到达这里
        override fun onExtrasChanged(extras: Bundle?) {
            super.onExtrasChanged(extras)
            currentPlayMode= extras?.getSerializable(BUNDLE_PLAY_MODE_KEY) as PlayMode?
            //修改图标 此时需要发布事件，接收方为PlaylistDialog
            EventBus.getDefault().postSticky(QueueModeCurrentSongEvent(
                currentQueueItem,currentPlayMode,currentMetadata
            ))

        }
        //当mediaSession调用setQueue方法后，就会收到这个
        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            super.onQueueChanged(queue)
            //修改播放队列
            Log.d(TAG, "onQueueChanged:")
            currentQueueItem=queue
            //如果播放列表为空，那么直接隐藏miniPlayer
            if(currentQueueItem?.size==0) {
                miniPlayer?.root?.visibility= GONE
            } else
                miniPlayer?.root?.visibility= VISIBLE
            //发布事件，接收方为PlaylistDialog，使当前正在播放的音乐显示红色
            //注意这里发布的也应该是粘性事件
            EventBus.getDefault().postSticky(QueueModeCurrentSongEvent(
                currentQueueItem,currentPlayMode,currentMetadata
            ))
        }
        //当mediaSession调用sendSessionEvent方法后，就会收到这个
        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            if(event==MediaControllerRelated.EVENT_OPEN_PLAYER_ACTIVITY){
                //如果我们有一首歌曲在播放，然后我在页面上又点了一次，那么就打开PlayerActivity
                startActivity(Intent(this@BaseActivity,PlayerActivity::class.java))
            }
        }
    }
    //endregion

    //region 生命周期回调方法
    /**
     * 初始化MediaBrowser对象
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initData()
        initView()
        initListener()
        initObserver()
        initBroadcastReceiver()
        initMediaBrowser()
        initMiniPlayer()
        handler= BaseHandler(this)
    }

    /**
     * 1、在onStart()中连接到MediaBrowserService。如果连接成功，onConnect()回调会创建媒体控制器，将其链接到媒体会话，将您的界面控件链接到MediaController，
     *    并注册控制器以接收来自媒体会话的回调
     * 2、生命周期回调函数，进行EventBus订阅的注册和解注册
     */
    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
        EventBus.getDefault().register(this)
    }

    /**
     * 保存相关数据到本地
     */
    override fun onPause() {
        Log.d(TAG, "onPause: ")
        super.onPause()
        //todo 在后台保存当前进度
        lifecycleScope.launchWhenCreated {
            mediaController?.transportControls?.sendCustomAction(SAVE_MEDIA_DATA_ACTION,null)
        }
    }

    /**
     * 断开MediaBrowser的连接，并在Activity停止时取消注册MediaController.Callback
     */
    override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(mediaControllerCallback)
        mediaBrowser.disconnect()
        EventBus.getDefault().unregister(this)
        Log.d(TAG, "onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        miniPlayer=null
    }
    //endregion

    //region 初始化的一些方法
    /**
     * 初始化MediaBrowser，设置响应的连接回调
     */
    private fun initMediaBrowser() {
        mediaBrowser= MediaBrowserCompat(this,
            ComponentName(this, MusicService::class.java), //绑定服务端
            mediaBrowserConnectionCallback,null)//设置连接回调
    }

    open fun initBinding(){}

    open fun initData(){
        baseViewModel= ViewModelProvider(this).get(BaseViewModel::class.java)
    }

    open fun initView(){}

    open fun initListener(){}

    open fun initObserver(){
    }

    open fun initBroadcastReceiver(){}

    open fun initMiniPlayer(){
        miniPlayer?.apply {
            root.setOnClickListener {
                val intent=Intent(this@BaseActivity,PlayerActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.anim_slide_enter_bottom,
                    R.anim.anim_no_anim)
            }
            ivPlayQueue.setOnClickListener {
                PlaylistDialog().show(supportFragmentManager,null)
            }
            ivStartOrPause.setOnClickListener {
                playOrPause()
            }
        }
    }

    /**
     * 这个方法抽取出来是为了后面PlayerActivity的播放暂停大按钮也能使用
     */
    fun playOrPause() {
        val mediaController = MediaControllerCompat.getMediaController(this@BaseActivity)
        //根据播放器的状态进行修改
        when (mediaController.playbackState.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                mediaController.transportControls.pause()
            }
            PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> {
                mediaController.transportControls.play()
            }
            else -> { }
        }
    }

    private fun initMiniPlayerView(
        metadata: MediaMetadataCompat?,
        queue: MutableList<MediaSessionCompat.QueueItem>?
    ) {
        //初始化视图的时候如果没有传来歌曲元数据，就不显示miniPlayer
        Log.d(TAG, "initMiniPlayerView: 当前Activity:${this},当前metadata:$metadata")
        if(queue==null || queue.size ==0){
            miniPlayer?.root?.visibility=GONE
            return
        }else{//这里else一定要写
            miniPlayer?.root?.visibility= VISIBLE
        }
        val description:MediaDescriptionCompat?= metadata?.description
        //获取标题
        val title:String=description?.title.toString()
        //专辑封面的bitmap
        val albumBitmap=metadata?.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
        miniPlayer?.tvTitle?.text=title//歌曲名
        if(albumBitmap!=null){
            miniPlayer?.ivCover?.let {//专辑封面
                Glide.with(this@BaseActivity)
                    .load(albumBitmap)
                    .placeholder(R.drawable.default_pic_for_song_with_no_pic)
                    .into(it)
            }
        }
    }

    /**
     * 修改播放器状态
     * @param state PlaybackStateCompat?
     */
    open fun changePauseOrStart(state: PlaybackStateCompat?) {
        when (state?.state) {
            PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.STATE_PAUSED,
            PlaybackStateCompat.STATE_STOPPED -> {//无任何状态
                //播放器的初始状态设置
                miniPlayer?.ivStartOrPause?.setImageResource(R.drawable.ic_play)
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                //当播放器播放音乐
                miniPlayer?.ivStartOrPause?.setImageResource(R.drawable.ic_pause)
            }
            else -> { }
        }
    }
    //endregion

    //region EventBus处理事件的一些方法，主要是完成播放音乐和下载
    @Subscribe(threadMode = ThreadMode.ASYNC)//异步模式，可以放心在里面进行耗时操作
    fun onPlayMusic(playMusicEvent: PlayMusicEvent) {
        //1、针对不同来源的歌曲进行处理
        val standardSong = playMusicEvent.standardSong
        val clickFromSource=playMusicEvent.clickFromSource
        lifecycleScope.launch(Dispatchers.IO) {
            when (standardSong.source) {
                MusicSource.NETEASE -> {
                    //网易的歌曲需要额外获取播放url
                    standardSong.url_320 = standardSong.id?.let { it1 ->
                        GetMusicUrl.getMusicUrl163(it1)
                    }
                }
                MusicSource.MIGU->{
                    val songHashOrId=standardSong.url_128
                    //1、获取播放连接
                    standardSong.url_320=songHashOrId?.let{songHash->
                        GetMusicUrl.getMusicUrlMiGu(this@BaseActivity,songHash)
                    }
                }
                MusicSource.LOCAL -> {
                    //本地音乐播放时需要通过Uri来播放
                }
                MusicSource.MIGU2->{
                    //获取播放链接和歌词链接
                    val item=standardSong.id?.let { it1 ->
                        GetMusicUrl.getMusicUrlMiGu2(
                            it1
                        ).data
                    }
                    standardSong.url_320 =item?.url
                    standardSong.lrc =item?.songItem?.lrcUrl
                }
                MusicSource.KUWO-> {
                    val item = standardSong.id?.let { it1 ->
                        GetMusicUrl.getMusicUrlKuWo(
                            it1
                        )
                    }
                    standardSong.url_320 = item
                    val results = StringBuilder()
                    val lyricKuWo = standardSong.id?.let { GetMusicUrl.getKuWoSongInfo(it) }
                    if (lyricKuWo != null) {
                        for (obj in lyricKuWo.lrclist) {
                            val time =
                                "[0${obj.time.toFloat().toInt() / 60}:${obj.time.toFloat() % 60}]"
                            results.append(time).append(obj.lineLyric).append("\n")
                        }
                    }
                    standardSong.lrc = results.toString()
                    standardSong.picUrl = lyricKuWo?.songinfo?.pic
                }
                MusicSource.KUGOU->{
                    val splitList=standardSong.url_flac?.split("$")
                    val hash=splitList?.get(0)
                    val albumId= splitList?.get(1)
                    if(hash!=null && albumId!=null){
                        val item=GetMusicUrl.getKuGouSongInfo(hash,albumId)
                        standardSong.picUrl = item.data.img
                        standardSong.url_320 = item.data.play_url
                        standardSong.lrc = item.data.lyrics
                    }
                }
                MusicSource.QQ->{
                    val songId=standardSong.url_128
                    standardSong.url_320= songId?.let { GetMusicUrl.getQQSongUrlInfo(it) }
                }
                else -> { }
            }
            val bundle = Bundle()
            //存放当前要播放的歌曲
            bundle.putParcelable(MediaControllerRelated.STANDARD_SONG, standardSong)
            //存放用户的点击事件是由哪里发出的，这里是由Activity发出的，后续MediaSession客户端接收到并返回响应
            //进行操作，如果用户点击的歌曲是正在播放的那一首，就打开PlayerActivity
            bundle.putSerializable(MediaControllerRelated.PLAY_FROM_KEY,clickFromSource)
            mediaController.transportControls.sendCustomAction(
                MediaControllerRelated.PLAY_ACTION,
                bundle
            )
        }
    }

    @Subscribe
    fun onDownloadMusic(downloadMusicEvent: DownloadMusicEvent){
        val standardSong=downloadMusicEvent.standardSong
        val msg:Message=Message.obtain()
        Toast.makeText(this,"正在准备下载音乐",Toast.LENGTH_SHORT).show()
        lifecycleScope.launch(Dispatchers.IO){
            var musicUrl:String?=null
            var musicCover:String?=null
            var musicLyric:String?=null//以上两个都是播放链接，这个直接就是歌词
            when(standardSong.source){
                MusicSource.NETEASE->{
                    //先获取Url
                    standardSong.url_320=standardSong.id?.let { GetMusicUrl.getMusicUrl163(it) }
                    //再获取歌词
                    standardSong.lrc=standardSong.id?.let {
                        val lyric163=GetMusicUrl.getLyric163(it)
                        //返回的是第一语言的歌词和第二语言的歌词
                        lyric163.lrc?.lyric+"$$$$$"+lyric163.tlyric?.lyric
                    }
                    musicUrl=standardSong.url_320
                    musicCover=standardSong.picUrl
                    musicLyric=standardSong.lrc
                }
                MusicSource.QQ->{
                    val songId=standardSong.url_128
                    standardSong.url_320= songId?.let { GetMusicUrl.getQQSongUrlInfo(it) }
                    musicUrl=standardSong.url_320
                    musicCover=standardSong.picUrl
                    musicLyric=standardSong.lrc?.let { GetMusicUrl.getMusicLrcQQ(it)+"$$$$$" }
                }
                MusicSource.KUWO->{
                    val item = standardSong.id?.let { it1 ->
                        GetMusicUrl.getMusicUrlKuWo(it1)
                    }
                    standardSong.url_320 = item
                    val results = StringBuilder()
                    val lyricKuWo = standardSong.id?.let { GetMusicUrl.getKuWoSongInfo(it) }
                    if (lyricKuWo != null) {
                        for (obj in lyricKuWo.lrclist) {
                            val time =
                                "[0${obj.time.toFloat().toInt() / 60}:${obj.time.toFloat() % 60}]"
                            results.append(time).append(obj.lineLyric).append("\n")
                        }
                    }
                    standardSong.lrc = results.toString()
                    standardSong.picUrl = lyricKuWo?.songinfo?.pic
                    musicUrl=standardSong.url_320
                    musicCover=standardSong.picUrl
                    musicLyric=standardSong.lrc+"$$$$$"
                }
                MusicSource.KUGOU->{
                    val splitList=standardSong.url_flac?.split("$")
                    val hash=splitList?.get(0)
                    val albumId= splitList?.get(1)
                    if(hash!=null && albumId!=null){
                        val item=GetMusicUrl.getKuGouSongInfo(hash,albumId)
                        standardSong.picUrl = item.data.img
                        standardSong.url_320 = item.data.play_url
                        standardSong.lrc = item.data.lyrics
                    }
                    musicUrl=standardSong.url_320
                    musicCover=standardSong.picUrl
                    musicLyric=standardSong.lrc+"$$$$$"
                }
                MusicSource.MIGU2->{
                    //获取播放链接和歌词链接
                    val item=standardSong.id?.let { it1 ->
                        GetMusicUrl.getMusicUrlMiGu2(it1).data
                    }
                    standardSong.url_320 =item?.url
                    standardSong.lrc =item?.songItem?.lrcUrl
                    musicUrl=standardSong.url_320
                    musicCover=standardSong.picUrl
                    val lyricMiGu=standardSong.lrc?.let{GetMusicUrl.getMusicLrcMigu2(it)}
                    musicLyric= "$lyricMiGu$$$$$"
                }
                MusicSource.MIGU->{
                    val songHashOrId=standardSong.url_128
                    //1、获取播放连接
                    standardSong.url_320=songHashOrId?.let{songHash->
                        GetMusicUrl.getMusicUrlMiGu(this@BaseActivity,songHash)
                    }
                    musicUrl=standardSong.url_320
                    musicCover=standardSong.picUrl
                    val lyricMiGu=standardSong.lrc?.let{GetMusicUrl.getMusicLrcMiGu(this@BaseActivity,it)}
                    musicLyric= "$lyricMiGu$$$$$"
                }
                else->{

                }
            }
            //TODO() lyric另说
            if(musicUrl!=null){
                Log.d(TAG, "onDownloadMusic: 音乐链接${standardSong.url_320}")
                //会返回一个状态值
                val status:Int =
                    DownloadManager.downloadMusic(application,standardSong,musicUrl)
                Log.d(TAG, "onDownloadMusic: 下载状态:$status")
                when(status){
                    DOWNLOAD_SUCCESS->{
                        msg.what= DOWNLOAD_MSG_WHAT
                        msg.arg1= DOWNLOAD_SUCCESS
                    }
                    DOWNLOAD_FAIL->{
                        msg.what= DOWNLOAD_MSG_WHAT
                        msg.arg1= DOWNLOAD_FAIL
                    }
                    DOWNLOAD_FILE_EXIST->{
                        msg.what= DOWNLOAD_MSG_WHAT
                        msg.arg1= DOWNLOAD_FILE_EXIST
                    }
                }
                //下载歌词和封面，但是我并不关心歌词和封面是否下载成功，因为不是必须的
                if(musicCover!=null){
                    DownloadManager.downloadMusicCover(application,standardSong,musicCover)
                }
                if(musicLyric!=null){
                    DownloadManager.saveLyric(application,standardSong,musicLyric)
                }
            }else{
                //无法获取歌曲链接
                Log.d(TAG, "onDownloadMusic: 无法获取歌曲链接")
                msg.what= NO_MUSIC_URL_MSG_WHAT
            }
            //发送消息
            handler.sendMessage(msg)
        }
    }

    @Subscribe//删除一首歌的时候调用，也就是在播放列表中移除它
    fun onDeleteMusic(deleteMusicEvent: DeleteMusicEvent){
        val bundle=Bundle()
        val queueItem = deleteMusicEvent.queueItem
        bundle.putParcelable(MediaControllerRelated.QUEUE_ITEM_TO_DELETE,queueItem)
        mediaController.transportControls.sendCustomAction(MediaControllerRelated.DELETE_SPECIFY_SONG_ACTION,bundle)
    }

    @Subscribe//插入到下一首播放
    fun onInsertNextPlay(insertNextPlayEvent: InsertNextPlayEvent){
        val bundle=Bundle()
        val standardSong = insertNextPlayEvent.standardSong
        bundle.putParcelable(MediaControllerRelated.QUEUE_ITEM_TO_INSERT,standardSong)
        mediaController.transportControls.sendCustomAction(MediaControllerRelated.INSERT_NEXT_PLAY_ACTION,bundle)
        Toast.makeText(this,"已添加到下一首播放",Toast.LENGTH_SHORT).show()
    }

    @Subscribe//添加所有歌曲到收藏❤
    fun onCollectAllSongsToFav(collectAllSongsToFavEvent: CollectAllSongsToFavEvent){
        val queItemList=collectAllSongsToFavEvent.queueItemList
        val songList = ClassConvertHelper.queueItemListToStandardSongList(queItemList)
        songList.forEach {
            baseViewModel.addFavoriteItem(it)
        }
    }
    //endregion

    //region 继承自Handler的静态内部类
    //实现静态内部类（防止内存泄露）,定义一个handler用来处理歌曲下载的Toast消息传递
    class BaseHandler(activity: BaseActivity): Handler(Looper.getMainLooper()){
        private var weakReference: WeakReference<BaseActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val currentActivity=weakReference.get()
            when(msg.what){
                DOWNLOAD_MSG_WHAT->{
                    when(msg.arg1){
                        DOWNLOAD_SUCCESS->{
                            Toast.makeText(currentActivity,"下载成功",Toast.LENGTH_SHORT).show()
                        }
                        DOWNLOAD_FILE_EXIST->{
                            Toast.makeText(currentActivity,"歌曲已存在",Toast.LENGTH_SHORT).show()
                        }
                        DOWNLOAD_FAIL->{
                            Toast.makeText(currentActivity,"下载失败，请稍后重试",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                NO_MUSIC_URL_MSG_WHAT->{
                    Toast.makeText(currentActivity,"没有歌曲下载链接",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    //endregion
}