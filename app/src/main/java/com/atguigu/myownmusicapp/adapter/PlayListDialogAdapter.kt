package com.atguigu.myownmusicapp.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.ClickFromSource
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.STANDARD_SONG
import com.atguigu.myownmusicapp.databinding.PlaylistDialogBinding
import com.atguigu.myownmusicapp.event.DeleteMusicEvent
import com.atguigu.myownmusicapp.event.PlayMusicEvent
import org.greenrobot.eventbus.EventBus

private const val TAG = "myTagPlayListDialogAda"
class PlayListDialogAdapter : RecyclerView.Adapter<PlayListDialogAdapter.ViewHolder>() {
    private var arr:List<MediaSessionCompat.QueueItem> = emptyList()
    private var currentPlayingPosition=-1//记录当前正在播放的位置
    inner class ViewHolder(val itemBinding: PlaylistDialogBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        PlaylistDialogBinding.inflate(
        LayoutInflater.from(parent.context), parent, false).apply {
            return ViewHolder(this)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val queueItem:MediaSessionCompat.QueueItem=arr[position]
        val standardSong: StandardSong? =queueItem.description.extras?.getParcelable(STANDARD_SONG)
        with(holder.itemBinding){
            tvSongName.text=standardSong?.name
            tvSongAuthor.text=standardSong?.artists
            ivDelete.setOnClickListener {//todo 删除操作
                EventBus.getDefault().post(DeleteMusicEvent(queueItem))
            }
            //第二个参数表示从PlayListDialog播放
            root.setOnClickListener {
                EventBus.getDefault().post(standardSong?.let { it1 -> PlayMusicEvent(it1,ClickFromSource.CLICK_FROM_PLAYLIST_DIALOG) })
            }
            //todo 正在播放的音乐显示红色
            if(position==currentPlayingPosition){
                tvSongName.setTextColor(Color.RED)//自定义颜色使用Color.rgb(255,0,0)
                tvSongAuthor.setTextColor(Color.RED)
                ivVolume.visibility=VISIBLE
                ivVolume.imageTintList= ColorStateList.valueOf(Color.RED)
            }else{
                tvSongName.setTextColor(ContextCompat.getColor(root.context, R.color.textviewColor))
                tvSongAuthor.setTextColor(ContextCompat.getColor(root.context, R.color.textviewColor))
                ivVolume.visibility= GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return arr.size
    }
    fun setData(newData: List<MediaSessionCompat.QueueItem>){
        arr=newData
        notifyDataSetChanged()
    }

    /**
     * 设置当前播放的歌曲显示为红色
     * @param metadata MediaMetadataCompat
     */
    fun setPlayingRed(metadata: MediaMetadataCompat):Int? {
        val title = metadata.description.title
        val artists = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        val source=metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)
        Log.d(TAG, "setPlayingRed: ${arr.size}")
        arr.forEachIndexed { index, queueItem ->
            val item: StandardSong? =queueItem.description.extras?.getParcelable(STANDARD_SONG)
            val itemTitle=item?.name
            val itemArtists=item?.artists
            val itemSource=item?.source.toString()
            if(title==itemTitle && artists==itemArtists && source==itemSource){
                //就认为是当前播放的歌曲
                currentPlayingPosition=index
                //todo 通知数据集的改变
                notifyDataSetChanged()
                //todo 如果歌曲播放列表很长，我们要让当前播放的歌曲显示在可见范围内
                return index //找到就结束
            }
        }
        return null
    }
}