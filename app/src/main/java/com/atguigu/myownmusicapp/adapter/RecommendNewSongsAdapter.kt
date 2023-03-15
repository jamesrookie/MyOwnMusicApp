package com.atguigu.myownmusicapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.bean.songplaylistactivitybean.NewSongsInner
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.databinding.CellRecommendNewSongsBinding
import com.atguigu.myownmusicapp.event.PlayMusicEvent
import com.bumptech.glide.Glide
import org.greenrobot.eventbus.EventBus

class RecommendNewSongsAdapter(private val arrList:ArrayList<NewSongsInner>):RecyclerView.Adapter<RecommendNewSongsAdapter.RecommendNewSongsViewHolder>() {
    inner class RecommendNewSongsViewHolder(val itemBinding:CellRecommendNewSongsBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendNewSongsViewHolder {
        val item=CellRecommendNewSongsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RecommendNewSongsViewHolder(item)
    }

    override fun onBindViewHolder(holder: RecommendNewSongsViewHolder, position: Int) {
        with(holder.itemBinding){
            Glide.with(this.root)
                .load(arrList[position].picUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivNewSongCover)
            tvNewSong.text=arrList[position].name
            val item=arrList[position]
            var songAuthorJoint =""
            item.song.artists.forEach {
                songAuthorJoint += if(it== item.song.artists.last()){
                    "${it.name}"
                }else {
                    "${it.name}/"
                }
            }
            tvNewSongAuthor.text= songAuthorJoint
            root.setOnClickListener {
                //将歌曲内容转换为标准歌曲的格式，通过EventBus发布事件，MainActivity接收到事件后做相应处理
                //如异步请求歌曲url，调用MusicService播放音乐等
                val standardSong= StandardSong(MusicSource.NETEASE,item.id,item.name,item.picUrl,songAuthorJoint,null,null,null,null,null)
                EventBus.getDefault().post(PlayMusicEvent(standardSong))
            }
        }
    }

    override fun getItemCount(): Int {
        return arrList.size
    }
}