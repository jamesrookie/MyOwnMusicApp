package com.atguigu.myownmusicapp.ui

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.base.SlideBackActivity
import com.atguigu.myownmusicapp.bean.playeractivitybean.LyricDataLyricView
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.BUNDLE_PLAY_MODE_KEY
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.CHANGE_PLAY_MODE_ACTION
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.MANUALLY_SKIP_TO_NEXT_ACTION
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.MANUALLY_SKIP_TO_NEXT_KEY
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.METADATA_KEY_LYRIC
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.REQUEST_PROGRESS_ACTION
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.STANDARD_SONG
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.constants.OpenDialogSource.Companion.sourceToOpenDialogSource
import com.atguigu.myownmusicapp.constants.PlayMode
import com.atguigu.myownmusicapp.databinding.ActivityPlayerBinding
import com.atguigu.myownmusicapp.event.QueueModeCurrentSongEvent
import com.atguigu.myownmusicapp.service.GetMusicUrl
import com.atguigu.myownmusicapp.ui.dialog.PlaylistDialog
import com.atguigu.myownmusicapp.ui.dialog.SongMenuDialog
import com.atguigu.myownmusicapp.utils.ClassConvertHelper.metadataToStandardSong
import com.atguigu.myownmusicapp.utils.TimeHelper.milliSecondsToTimer
import com.atguigu.myownmusicapp.viewmodel.PlayerViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ViewTarget
import com.bumptech.glide.request.transition.Transition
import com.dirror.lyricviewx.OnPlayClickListener
import com.dirror.lyricviewx.OnSingleClickListener
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

private const val TAG = "myTag-PlayerActivity"
private const val VOLUME_CHANGE_ACTION = "android.media.VOLUME_CHANGED_ACTION"

class PlayerActivity : SlideBackActivity() {
    private lateinit var binding:ActivityPlayerBinding
    //初始化动画
    private lateinit var startNeedleAnim:Animation
    private lateinit var stopNeedleAnim:Animation
    private lateinit var rotateDiscAnim:Animation
    //定义当前进度条
    private var currentPosition:Long=0
    private var totalDuration:Long=0
    private var bufferedPosition: Long=0
    //定义是否在播放
    private var playbackState=PlaybackStateCompat.STATE_NONE
    //内部类PlayerHandler，消除内存泄露的风险
    private var handler: Handler? = Handler(Looper.getMainLooper())
    private lateinit var receiver:MyVolumeReceiver
    private lateinit var playerViewModel:PlayerViewModel

    override var mediaBrowserConnectionCallback=object:
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            if(mediaBrowser.isConnected){
                //获取mediaController
                val mediaController= MediaControllerCompat(this@PlayerActivity,mediaBrowser.sessionToken)
                MediaControllerCompat.setMediaController(this@PlayerActivity,mediaController)
                mediaController.registerCallback(mediaControllerCallback)
                //此时就需要更新我们的miniPlayer,包括元数据和播放状态,以及播放模式
                currentMetadata=mediaController.metadata
                baseViewModel.updateIsFavorite(metadataToStandardSong(currentMetadata))
                currentQueueItem=mediaController.queue
                currentPlayMode= mediaController.extras?.getSerializable(BUNDLE_PLAY_MODE_KEY) as PlayMode?
                playbackState=mediaController.playbackState.state
                if(currentMetadata!=null)
                    initPlayerView(currentMetadata)
                changePauseOrStart(mediaController.playbackState)
                updatePLayMode(currentPlayMode)
                //发送指定，更新seekbar
                mediaController.transportControls.sendCustomAction(REQUEST_PROGRESS_ACTION,null)
                //更新歌词
                updateLyric(currentMetadata)
            }
        }
    }

    /**
     * 更新播放模式
     * @param playMode PlayMode?
     */
    private fun updatePLayMode(playMode: PlayMode?) {
        if (playMode != null) {
            updatePlayModeImage(playMode)
        }
    }

    override var mediaControllerCallback:MediaControllerCompat.Callback=object:
    MediaControllerCompat.Callback(){
        //当服务端运行mediaSession.setPlaybackState就会到达此处
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            Log.d(TAG, "onPlaybackStateChanged: $state")
            if(playbackState!=state?.state) {
                playbackState=state?.state?:PlaybackStateCompat.STATE_NONE
                Log.w(TAG, "onPlaybackStateChanged: 新的播放状态是${playbackState}")
                changePauseOrStart(state)
            }
            currentPosition= state?.position?:0//如果为空就给默认值0
            bufferedPosition=state?.bufferedPosition?:0
            updateSeekBar(currentPosition,bufferedPosition,totalDuration)
            //更新后完再发送,判断是在播放的就发送，不是就不发送
            if(playbackState==PlaybackStateCompat.STATE_PLAYING)
                handler?.postDelayed(updater,500)
        }

        //播放音乐改变的回调，当服务端运行mediaSession.setMetadata就会到达此处
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            //媒体数据更改后反映在播放器界面上,更新UI
            val mediaController=MediaControllerCompat.getMediaController(this@PlayerActivity)
            initPlayerView(metadata)
            //此时需要发布事件，给PlaylistDialog，让它给当前正在播放的歌曲加红色
            currentMetadata=metadata
            playerViewModel.updateIsFavorite(metadataToStandardSong(currentMetadata))
            EventBus.getDefault().postSticky(
                QueueModeCurrentSongEvent(currentQueueItem,currentPlayMode,metadata))
            //歌曲变换后，这里最好也请求更新一下进度条
            mediaController.transportControls.sendCustomAction(REQUEST_PROGRESS_ACTION,null)
            updateLyric(metadata)
            //如果为空，就置零
            totalDuration= metadata?.getLong(METADATA_KEY_DURATION)?:0
            Log.d(TAG, "onMetadataChanged: $totalDuration")
            updateSeekBar(currentPosition,bufferedPosition,totalDuration)
        }

        //播放模式改变的回调，当服务端运行mediaSession.setExtras就会到达这里
        override fun onExtrasChanged(extras: Bundle?) {
            super.onExtrasChanged(extras)
            currentPlayMode= extras?.getSerializable(BUNDLE_PLAY_MODE_KEY) as PlayMode?
            //修改图标 此时需要发布事件，接收方为PlaylistDialog
            EventBus.getDefault().postSticky(
                QueueModeCurrentSongEvent(currentQueueItem,currentPlayMode,currentMetadata))
            currentPlayMode?.let { updatePlayModeImage(it) }
        }

        //当mediaSession调用setQueue方法后，就会收到这个
        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            super.onQueueChanged(queue)
            //修改播放队列
            Log.d(TAG, "onQueueChanged:")
            currentQueueItem=queue
            //如果播放队列为空，直接关闭PlayerActivity
            if(currentQueueItem?.isEmpty() == true)finish()
            //发布事件，接收方为PlaylistDialog，使当前正在播放的音乐显示红色
            //注意这里发布的也应该是粘性事件
            EventBus.getDefault().postSticky(
                QueueModeCurrentSongEvent(currentQueueItem,currentPlayMode,currentMetadata))
        }
    }

    private fun updateLyric(metadata: MediaMetadataCompat?) {
        //更新歌词
        val id = metadata?.getLong("mediaId")
        val lrc = metadata?.getString(METADATA_KEY_LYRIC)
        val source = metadata?.getString(METADATA_KEY_DISPLAY_DESCRIPTION).toString()
        loadLyc(id, source, lrc)
    }

    private fun loadLyc(id: Long?, source:String,lrc: String?) {
        when(MusicSource.strToMusicSource(source)){
            MusicSource.NETEASE->{
                lifecycleScope.launch(Dispatchers.IO) {
                    val lyric163=id?.let { GetMusicUrl.getLyric163(it) }
                    val lyricViewData= LyricDataLyricView(lyric163?.lrc?.lyric?:"",lyric163?.tlyric?.lyric?:"")
                    binding.lyricView.loadLyric(lyricViewData.lyric,lyricViewData.secondLyric)
                }
            }
            MusicSource.MIGU ->{
                lifecycleScope.launch(Dispatchers.IO) {
                    val lyricMiGu=lrc?.let{GetMusicUrl.getMusicLrcMiGu(this@PlayerActivity,it)}
                    binding.lyricView.loadLyric(lyricMiGu?:"","")
                }
            }
            MusicSource.MIGU2 ->{
                val lyricMiGuUrl=lrc?:""
                binding.lyricView.loadLyricByUrl(lyricMiGuUrl)
            }
            MusicSource.KUWO->{
                binding.lyricView.loadLyric(lrc.toString(),"")
            }
            MusicSource.KUGOU->{
                binding.lyricView.loadLyric(lrc.toString(),"")
            }
            MusicSource.QQ->{
                lifecycleScope.launch(Dispatchers.IO) {
                    val lyricQQ=lrc?.let { GetMusicUrl.getMusicLrcQQ(it) }
                    binding.lyricView.loadLyric(lyricQQ?:"","")
                }
            }
            MusicSource.DOWNLOAD_NETEASE,MusicSource.DOWNLOAD_MIGU2,MusicSource.DOWNLOAD_MIGU,
            MusicSource.DOWNLOAD_KUWO,MusicSource.DOWNLOAD_KUGOU,MusicSource.DOWNLOAD_QQ->{
                //读取本地文件
                if(lrc!=null){
                    val lyricList = lrc.split("$$$$$")
                    val lyricViewData = LyricDataLyricView(lyricList[0], lyricList[1])
                    binding.lyricView.loadLyric(lyricViewData.lyric,lyricViewData.secondLyric)
                }
            }
            MusicSource.LOCAL->{}
            else->{}
        }
    }

    override fun initBinding() {
        binding= ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * 初始化动画
     */
    override fun initData() {
        super.initData()
        startNeedleAnim=AnimationUtils.loadAnimation(this,R.anim.anim_needle_leave_disc)
        stopNeedleAnim=AnimationUtils.loadAnimation(this,R.anim.anim_needle_come_to_disc)
        rotateDiscAnim=AnimationUtils.loadAnimation(this,R.anim.anim_disc_rotate)
        playerViewModel=ViewModelProvider(this).get(PlayerViewModel::class.java)
    }
    override fun initView() {
        //四句话实现状态栏透明和布局背景一致
        val decorView=window.decorView
        val option= SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility=option
        window.statusBarColor= Color.TRANSPARENT
        binding.sbPlay.max=100
        with(binding.lyricView){
            //歌词布局默认的文字
            setLabel("暂无歌词")
            //设置拖动歌词时间线下歌词颜色
            //setTimelineTextColor(R.color.white)
        }
        //初始化音量调节
        with(binding.sbVolume){
            max=playerViewModel.maxVolume
            progress=playerViewModel.getCurrentVolume()
        }
    }

    override fun initListener() {
        with(binding){
            //手动前一首
            ivPrev.setOnClickListener {
                mediaController.transportControls.skipToPrevious()
            }
            //手动后一首
            ivNext.setOnClickListener {
                val bundle= Bundle()
                bundle.putBoolean(MANUALLY_SKIP_TO_NEXT_KEY,true)
                mediaController.transportControls.sendCustomAction(MANUALLY_SKIP_TO_NEXT_ACTION,bundle)
            }
            //开始/播放/暂停
            ivPlay.setOnClickListener {
                playOrPause()
            }
            //切换播放模式：单曲循环、列表循环、随机循环
            ivMode.setOnClickListener {
                mediaController.transportControls.sendCustomAction(CHANGE_PLAY_MODE_ACTION,null)
            }
            //添加到收藏歌单
            ivFavorite.setOnClickListener {
                val standardSong=metadataToStandardSong(currentMetadata)
                //已经在收藏列表里
                if(baseViewModel.isFavorite.value==true){
                    playerViewModel.deleteFavoriteItem(standardSong)
                    baseViewModel.changeIsFavorite()
                    ivFavorite.setImageResource(R.drawable.ic_favorite_outline)
                    Toast.makeText(this@PlayerActivity,"已从❤歌单移除",Toast.LENGTH_SHORT).show()
                }else{
                    playerViewModel.addFavoriteItem(standardSong)
                    baseViewModel.changeIsFavorite()
                    ivFavorite.setImageResource(R.drawable.ic_favorite)
                    Toast.makeText(this@PlayerActivity,"已添加到收藏歌单",Toast.LENGTH_SHORT).show()
                }
            }
            //查看精彩评论
            ivComment.setOnClickListener {
                val standardSong:Parcelable=metadataToStandardSong(currentMetadata)
                val intent = Intent(this@PlayerActivity, CommentActivity::class.java)
                with(intent) {
                    putExtra(STANDARD_SONG,standardSong)
                    startActivity(this)
                }
            }
            //弹出相关歌曲信息
            ivMore.setOnClickListener {
                val standardSong=metadataToStandardSong(currentMetadata)
                val supportFragmentManager=(root.context as FragmentActivity).supportFragmentManager
                val source=sourceToOpenDialogSource(standardSong.source)
                SongMenuDialog(source,standardSong).show(supportFragmentManager,null)
            }
            //弹出播放列表
            ivPlayList.setOnClickListener {
                PlaylistDialog().show(supportFragmentManager,null)
            }
            //设置拖动进度条
            sbPlay.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val newPosition: Long = (totalDuration.div(100)) * (seekBar?.progress?:0)
                    mediaController.transportControls.seekTo(newPosition)
                    tvCurrentTime.text = milliSecondsToTimer(currentPosition.toInt())
                }

            })
            //显示歌词
            clUpper.setOnClickListener {
                it.visibility=GONE
                clLyric.visibility= VISIBLE
            }
            //隐藏歌词
            lyricView.setOnSingerClickListener(object:OnSingleClickListener{
                override fun onClick() {
                    binding.clLyric.visibility= GONE
                    binding.clUpper.visibility= VISIBLE
                }
            })
            clLyric.setOnClickListener {
                it.visibility= GONE
                clUpper.visibility= VISIBLE
            }

            //拖动歌词并播放的监听
            lyricView.setDraggable(true,onPlayClickListener = object :OnPlayClickListener{
                override fun onPlayClick(time: Long): Boolean {
                    //playerViewModel.setProgress(time.toInt())
                    //更新进度
                    mediaController.transportControls.seekTo(time)
                    return true
                }
            })

            //音量调节
            sbVolume.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) { playerViewModel.setStreamVolume(progress) }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            })
        }
        //注册广播接收者用来接收广播
        val intentFilter=IntentFilter()
        intentFilter.addAction(VOLUME_CHANGE_ACTION)
        receiver=MyVolumeReceiver()
        registerReceiver(receiver,intentFilter)
    }

    override fun initObserver() {
        baseViewModel.isFavorite.observe(this,{flag->
            binding.ivFavorite.setImageResource(if(flag) R.drawable.ic_favorite else R.drawable.ic_favorite_outline)
        })
    }

    private fun initPlayerView(metadata: MediaMetadataCompat?) {
        //初始化歌曲名字和艺术家，以及封面
        val mediaController=MediaControllerCompat.getMediaController(this)
        val description=mediaController.metadata.description
        //获取标题
        val title:String=description?.title.toString()
        //获取作者
        val author:String=description?.subtitle.toString()
        //获取音乐总时间
        totalDuration=metadata?.getLong(METADATA_KEY_DURATION)?:0
        //专辑封面
        val albumBitmap=metadata?.getBitmap(METADATA_KEY_ALBUM_ART)
        binding.tvName.text=title
        binding.tvDuration.text=milliSecondsToTimer(totalDuration.toInt())
        binding.tvArtist.text=author
        Glide.with(binding.root)
            .load(albumBitmap)
            .into(binding.includePlayerCover.ivCover)
        //对背景进行一个高斯模糊的设置
        Glide.with(binding.root)
            .load(albumBitmap)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(25,35)))
            .into(object: ViewTarget<ImageView, Drawable>(binding.ivLyricsBackground) {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    //拿到glide的drawable
                    var drawable=resource.current
                    //使用适配器进行包装
                    drawable= DrawableCompat.wrap(drawable)
                    //设置着色的效果和颜色，蒙版模式
                    drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
                    binding.ivLyricsBackground.background=drawable//设置成背景，而不是src
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        handler=null
    }

    /**
     * 设置结束时的动画
     */
    override fun finish() {
        super.finish()
        overridePendingTransition(
            R.anim.anim_no_anim,
            R.anim.anim_slide_exit_bottom
        )
    }

    private fun updateSeekBar(currentPosition:Long,bufferingPosition:Long,totalDuration:Long){
        if(currentPosition==0L || totalDuration==0L) return
        with(binding){
            sbPlay.progress=(currentPosition.toFloat()/ totalDuration*100).toInt()
            sbPlay.secondaryProgress=bufferingPosition.toInt()
            tvCurrentTime.text=milliSecondsToTimer(currentPosition.toInt())
            tvDuration.text=milliSecondsToTimer(totalDuration.toInt())
            lyricView.updateTime(currentPosition)//更新歌词进度
        }
    }

    private var updater:Runnable= Runnable {
        mediaController.transportControls.sendCustomAction(REQUEST_PROGRESS_ACTION,null)
    }

    //旋转动画
    private val objectAnimator: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(binding.includePlayerCover.root,"rotation",0f,360f)
            .apply {
                interpolator= LinearInterpolator()
                duration=32_000L
                repeatCount=-1
                start()
            }
    }

    override fun changePauseOrStart(state: PlaybackStateCompat?) {
        when (state?.state) {
            PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.STATE_PAUSED,
            PlaybackStateCompat.STATE_STOPPED -> {//无任何状态
                //播放器的初始状态设置
                binding.ivPlay.setImageResource(R.drawable.ic_play)
                handler?.removeCallbacks(updater)
                //结束旋转动画
                //binding.includePlayerCover.flCover.clearAnimation()
                objectAnimator.pause()
                binding.ivNeedle.startAnimation(startNeedleAnim)
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                //当播放器播放音乐
                binding.ivPlay.setImageResource(R.drawable.ic_pause)
                handler?.post(updater)
                //开始旋转动画
                //binding.includePlayerCover.flCover.startAnimation(rotateDiscAnim)
                objectAnimator.resume()
                binding.ivNeedle.startAnimation(stopNeedleAnim)
            }
            else -> {}
        }
    }

    /**
     * 修改左下角播放模式的贴图
     * @param playMode PlayMode
     */
    fun updatePlayModeImage(playMode:PlayMode){
        when(playMode){
            PlayMode.PLAY_SINGLE_LOOP->binding.ivMode.setImageResource(R.drawable.ic_bq_player_mode_repeat_one)
            PlayMode.PLAY_RANDOM->binding.ivMode.setImageResource(R.drawable.ic_bq_player_mode_random)
            PlayMode.PLAY_IN_ORDER->binding.ivMode.setImageResource(R.drawable.ic_bq_player_mode_circle)
        }
    }

    inner class MyVolumeReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action== VOLUME_CHANGE_ACTION){
                val audioManager:AudioManager= context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val currentVolume=audioManager.getStreamVolume(STREAM_MUSIC)
                binding.sbVolume.progress=currentVolume
            }
        }
    }
}