package com.atguigu.myownmusicapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.OpenDialogSource
import com.atguigu.myownmusicapp.databinding.CellPlaylistBinding
import com.atguigu.myownmusicapp.event.PlayMusicEvent
import com.atguigu.myownmusicapp.ui.dialog.SongMenuDialog
import com.bumptech.glide.Glide
import org.greenrobot.eventbus.EventBus

private const val TAG = "myTagDownloadedAdapter"
class DownloadedAdapter(private var arr:ArrayList<StandardSong>): RecyclerView.Adapter<DownloadedAdapter.DownloadedViewHolder>() {
    inner class DownloadedViewHolder(val itemBinding:CellPlaylistBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadedViewHolder {
        val itemBinding=CellPlaylistBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return DownloadedViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: DownloadedViewHolder, position: Int) {
        val standardSong=arr[position]
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
                SongMenuDialog(OpenDialogSource.DOWNLOADED,standardSong).show(supportFragmentManager,null)
            }
        }
    }

    override fun getItemCount(): Int {
        return arr.size
    }

    /**
     * 更新数据
     * @param arrayList ArrayList<StandardSong>
     */
    fun updateData(arrayList:ArrayList<StandardSong>){
        arr=arrayList
        notifyDataSetChanged()
    }
}