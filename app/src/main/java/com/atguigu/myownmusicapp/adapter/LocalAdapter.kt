package com.atguigu.myownmusicapp.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.OpenDialogSource
import com.atguigu.myownmusicapp.databinding.CellPlaylistBinding
import com.atguigu.myownmusicapp.event.PlayMusicEvent
import com.atguigu.myownmusicapp.ui.dialog.SongMenuDialog
import com.bumptech.glide.Glide
import org.greenrobot.eventbus.EventBus

private const val TAG = "myTag-LocalAdapter"
class LocalAdapter:androidx.recyclerview.widget.ListAdapter<StandardSong,LocalAdapter.LocalHolder>(DIFFCALLBACK) {
    object DIFFCALLBACK: DiffUtil.ItemCallback<StandardSong>() {
        override fun areItemsTheSame(oldItem: StandardSong, newItem: StandardSong): Boolean {
            return oldItem.name==newItem.name && oldItem.id==oldItem.id && oldItem.url_320==newItem.url_320
        }

        override fun areContentsTheSame(oldItem: StandardSong, newItem: StandardSong): Boolean {
            return oldItem==newItem
        }
    }
    inner class LocalHolder(val itemBinding: CellPlaylistBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalAdapter.LocalHolder {
        val itemBinding =
            CellPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocalHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: LocalAdapter.LocalHolder, position: Int) {
        val standardSong=getItem(position)
        with(holder.itemBinding){
            tvNewSong.text=standardSong.name
            tvNewSongAuthor.text= standardSong.artists
            Glide.with(this.root)
                    .load(Uri.parse(standardSong.picUrl))
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivNewSongCover)

            root.setOnClickListener {
                 //播放音乐，使用EventBus发布事件
                 EventBus.getDefault().post(PlayMusicEvent(standardSong))
             }
            ivMore.setOnClickListener {
                //todo
                val supportFragmentManager=(root.context as FragmentActivity).supportFragmentManager
                SongMenuDialog(OpenDialogSource.LOCAL,standardSong).show(supportFragmentManager,null)
            }

        }
    }

}
