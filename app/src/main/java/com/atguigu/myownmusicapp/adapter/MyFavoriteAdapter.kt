package com.atguigu.myownmusicapp.adapter

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

private const val TAG = "myTagMyFavoriteAdapter"
class MyFavoriteAdapter: androidx.recyclerview.widget.ListAdapter<StandardSong,MyFavoriteAdapter.MyFavoriteHolder>(DIFFCALLBACK) {
    object DIFFCALLBACK: DiffUtil.ItemCallback<StandardSong>() {
        override fun areItemsTheSame(oldItem: StandardSong, newItem: StandardSong): Boolean {
            return oldItem.name==newItem.name && oldItem.id==oldItem.id && oldItem.url_320==newItem.url_320
        }

        override fun areContentsTheSame(oldItem: StandardSong, newItem: StandardSong): Boolean {
            return oldItem==newItem
        }
    }
    inner class MyFavoriteHolder(val itemBinding: CellPlaylistBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyFavoriteHolder {
        val itemBinding =
            CellPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyFavoriteHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MyFavoriteHolder, position: Int) {
        val standardSong=getItem(position)
        with(holder.itemBinding){
            tvNewSong.text=standardSong.name
            tvNewSongAuthor.text= standardSong.artists
            Glide.with(root)
                .load(standardSong.picUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivNewSongCover)
            root.setOnClickListener {
                EventBus.getDefault().post(PlayMusicEvent(standardSong))
            }
            ivMore.setOnClickListener {
                val supportFragmentManager=(root.context as FragmentActivity).supportFragmentManager
                SongMenuDialog(OpenDialogSource.FAVORITE,standardSong).show(supportFragmentManager,null)
            }
        }
    }

}
