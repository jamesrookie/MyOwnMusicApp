package com.atguigu.myownmusicapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.constants.OpenDialogSource
import com.atguigu.myownmusicapp.databinding.CellPlaylistBinding
import com.atguigu.myownmusicapp.event.PlayMusicEvent
import com.atguigu.myownmusicapp.ui.dialog.SongMenuDialog
import com.bumptech.glide.Glide
import org.greenrobot.eventbus.EventBus

class SearchResultAdapter:
    PagingDataAdapter<StandardSong, SearchResultAdapter.SearchResultViewHolder>(DIFFCALLBACK) {
    object DIFFCALLBACK: DiffUtil.ItemCallback<StandardSong>() {
        override fun areItemsTheSame(oldItem: StandardSong, newItem: StandardSong): Boolean {
            return oldItem.name==newItem.name && oldItem.id==oldItem.id
        }

        override fun areContentsTheSame(oldItem: StandardSong, newItem: StandardSong): Boolean {
            return oldItem==newItem
        }
    }
    inner class SearchResultViewHolder(val itemBinding: CellPlaylistBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchResultAdapter.SearchResultViewHolder {
        val itemBinding = CellPlaylistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SearchResultViewHolder(itemBinding)
    }

    override fun onBindViewHolder(
        holder: SearchResultAdapter.SearchResultViewHolder,
        position: Int
    ) {
        val standardSong:StandardSong?=getItem(position = holder.bindingAdapterPosition)
        with(holder.itemBinding){
            tvNewSong.text=standardSong?.name
            tvNewSongAuthor.text= standardSong?.artists
            if(standardSong?.source!= MusicSource.KUGOU){  //酷狗的搜索结果里没有图片
                val picUrl=standardSong?.picUrl
                if(picUrl==null){
                    ivNewSongCover.setImageResource(R.drawable.default_pic_for_song_with_no_pic)
                }else{
                    picUrl.let {
                        Glide.with(root)
                            .load(it)
                            .placeholder(R.drawable.default_pic_for_song_with_no_pic)
                            .into(ivNewSongCover)
                    }
                }
            }else{//酷狗的
                val picUrl=standardSong.picUrl
                if(picUrl.isNullOrBlank()){
                    ivNewSongCover.setImageResource(R.drawable.default_pic_for_song_with_no_pic)
                }else{
                    Glide.with(root)
                        .load(picUrl)
                        .placeholder(R.drawable.default_pic_for_song_with_no_pic)
                        .into(ivNewSongCover)
                }
            }
            root.setOnClickListener {
                EventBus.getDefault().post(standardSong?.let { it1 -> PlayMusicEvent(it1) })
            }
            ivMore.setOnClickListener {
                val supportFragmentManager=(root.context as FragmentActivity).supportFragmentManager
                var openDialogSource=OpenDialogSource.SEARCH_RESULT_NETEASE //默认网易
                when(standardSong?.source){
                    MusicSource.MIGU->openDialogSource=OpenDialogSource.SEARCH_RESULT_MIGU
                    MusicSource.MIGU2->openDialogSource=OpenDialogSource.SEARCH_RESULT_MIGU2
                    MusicSource.KUWO->openDialogSource=OpenDialogSource.SEARCH_RESULT_KUWO
                    MusicSource.KUGOU->openDialogSource=OpenDialogSource.SEARCH_RESULT_KUGOU
                    MusicSource.QQ->openDialogSource=OpenDialogSource.SEARCH_RESULT_QQ
                    else->{}
                }
                if (standardSong != null) {
                    SongMenuDialog(openDialogSource,standardSong).show(supportFragmentManager,null)
                }
            }
        }
    }
}