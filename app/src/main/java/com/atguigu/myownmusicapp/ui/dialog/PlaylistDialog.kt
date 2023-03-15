package com.atguigu.myownmusicapp.ui.dialog

import android.graphics.Color
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.adapter.PlayListDialogAdapter
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.CHANGE_PLAY_MODE_ACTION
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.DELETE_ALL_SONGS_ACTION
import com.atguigu.myownmusicapp.constants.PlayMode
import com.atguigu.myownmusicapp.databinding.DialogPlaylistBinding
import com.atguigu.myownmusicapp.event.CollectAllSongsToFavEvent
import com.atguigu.myownmusicapp.event.QueueModeCurrentSongEvent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

private const val TAG = "myTag-PlaylistDialog"
class PlaylistDialog: BottomSheetDialogFragment() {
    private var binding: DialogPlaylistBinding?=null
    private var adapter:PlayListDialogAdapter?=null
    private var currentQueueItem:List<MediaSessionCompat.QueueItem>?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DialogPlaylistBinding.inflate(layoutInflater,container,false)
        return binding!!.root
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter= PlayListDialogAdapter()
        binding?.rvPlaylist?.layoutManager = LinearLayoutManager(context)
        binding?.rvPlaylist?.adapter=adapter
        initListener()
    }

    /**
     * 初始化视图监听
     */
    private fun initListener() {
        //切换播放模式
        binding?.llMode?.setOnClickListener {
            val mediaController=MediaControllerCompat.getMediaController(requireActivity())
            Log.d(TAG, "initListener: ${mediaController == null}")//false说明能获取到mediaController
            //调用mediaController来切换播放模式，会回调到服务端mediaSession的onCustomAction中
            mediaController.transportControls.sendCustomAction(CHANGE_PLAY_MODE_ACTION,null)
        }
        //收藏播放列表
        binding?.llCollect?.setOnClickListener {
            //todo 收藏到我的最爱歌曲，如果列表中没有歌曲，就什么也不做
            EventBus.getDefault().post(currentQueueItem?.let { it1 -> CollectAllSongsToFavEvent(it1) })
            Toast.makeText(context,"已全部收藏到❤歌单",Toast.LENGTH_SHORT).show()
        }
        //删除列表中所有歌曲
        binding?.ivDeleteAll?.setOnClickListener {
            //todo 如果列表中没有歌曲，就什么也不做
            val builder = context?.let { it1 -> AlertDialog.Builder(it1) }
            val dialog=builder?.setMessage(R.string.alertDialogDeleteAll)
                ?.setNegativeButton(R.string.cancel) { _, _ -> }
                ?.setPositiveButton(R.string.clearAll) { _, _ ->
                    val mediaController=MediaControllerCompat.getMediaController(requireActivity())
                    mediaController.transportControls.sendCustomAction(DELETE_ALL_SONGS_ACTION,null)
                }?.create()
            //修改按钮的默认颜色
            dialog?.show()
            dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.RED)
            dialog?.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.RED)
        }
    }

    /**
     * 粘性事件，即使这个PlaylistDialog还没创建，也可以发送过来
     * 作为EventBus的订阅方，接收来自Activity的QueueModeCurrentSongEvent事件
     * 接收到事件后：1、修改当前队列 2、修改正在播放的歌曲为红色 3、修改当前播放模式
     * @param queueModeCurrentSongEvent QueueModeCurrentSongEvent
     */
    @Subscribe(sticky = true)
    fun onQueueModeCurrentSong(queueModeCurrentSongEvent: QueueModeCurrentSongEvent){
        currentQueueItem = queueModeCurrentSongEvent.currentQueueItem
        val currentMetadata = queueModeCurrentSongEvent.currentMetadata
        val currentPlayMode = queueModeCurrentSongEvent.currentPlayMode
        //设置播放列表
        if (currentQueueItem != null) {
            if(currentQueueItem!!.isEmpty()) dismiss() //如果播放队列为空，直接关闭
            adapter?.setData(currentQueueItem!!)
            binding?.playlistNumber?.text=context?.getString(R.string.playlistNumber,
                currentQueueItem!!.size)
        }
        if(currentMetadata!=null){
            val position = adapter?.setPlayingRed(currentMetadata)
            if (position != null) {
                binding?.rvPlaylist?.smoothScrollToPosition(position)
            }
        }
        if(currentPlayMode!=null){
            updatePlayMode(currentPlayMode)
        }
    }
    /**
     * 根据不同的播放模式设置不同的图片资源和文字资源
     * @param playMode PlayMode
     */
    private fun updatePlayMode(playMode: PlayMode) {
        when(playMode){
            PlayMode.PLAY_IN_ORDER->{
                binding?.ivMode?.setImageResource(R.drawable.ic_bq_player_mode_circle)
                binding?.tvMode?.text=context?.getString(R.string.playModeNormal)
            }
            PlayMode.PLAY_RANDOM->{
                binding?.ivMode?.setImageResource(R.drawable.ic_bq_player_mode_random)
                binding?.tvMode?.text=context?.getString(R.string.playModeRandom)
            }
            PlayMode.PLAY_SINGLE_LOOP->{
                binding?.ivMode?.setImageResource(R.drawable.ic_bq_player_mode_repeat_one)
                binding?.tvMode?.text=context?.getString(R.string.playModeSingle)
            }
        }
    }
}