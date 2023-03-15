package com.atguigu.myownmusicapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.bean.songplaylistactivitybean.SongDetail
import com.atguigu.myownmusicapp.constants.OpenDialogSource
import com.atguigu.myownmusicapp.databinding.CellPlaylistBinding
import com.atguigu.myownmusicapp.event.PlayMusicEvent
import com.atguigu.myownmusicapp.ui.dialog.SongMenuDialog
import com.atguigu.myownmusicapp.utils.ClassConvertHelper.songDetailToStandardSong
import com.bumptech.glide.Glide
import org.greenrobot.eventbus.EventBus

class SongPlaylistAdapter : ListAdapter<SongDetail,SongPlaylistAdapter.SongPlaylistViewHolder>(DIFFCALLBACK) {
    object DIFFCALLBACK: DiffUtil.ItemCallback<SongDetail>() {
        override fun areItemsTheSame(oldItem: SongDetail, newItem: SongDetail): Boolean {
            return oldItem.name==newItem.name && oldItem.id==oldItem.id
        }

        override fun areContentsTheSame(oldItem: SongDetail, newItem: SongDetail): Boolean {
            return oldItem==newItem
        }
    }
    inner class SongPlaylistViewHolder(val itemBinding: CellPlaylistBinding) :RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SongPlaylistAdapter.SongPlaylistViewHolder {
        val itemBinding = CellPlaylistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SongPlaylistViewHolder(itemBinding)
    }

    override fun onBindViewHolder(
        holder: SongPlaylistAdapter.SongPlaylistViewHolder,
        position: Int
    ) {
        val songDetail:SongDetail?=getItem(holder.bindingAdapterPosition)

        val standardSong= songDetailToStandardSong(songDetail)
        with(holder.itemBinding){
            tvNewSong.text=songDetail?.name
            tvNewSongAuthor.text= standardSong.artists
            Glide.with(root)
                .load(songDetail?.al?.picUrl)
                .override(128,128)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivNewSongCover)
            root.setOnClickListener {
                EventBus.getDefault().post(PlayMusicEvent(standardSong))
            }
            ivMore.setOnClickListener {
                val supportFragmentManager=(root.context as FragmentActivity).supportFragmentManager
                SongMenuDialog(OpenDialogSource.NETEASE_PLAY_LIST,standardSong).show(supportFragmentManager,null)
            }
        }
    }

    fun playFirst() {
        val item = getItem(0)
        val standardSong = songDetailToStandardSong(item)
        EventBus.getDefault().post(PlayMusicEvent(standardSong))
    }


}