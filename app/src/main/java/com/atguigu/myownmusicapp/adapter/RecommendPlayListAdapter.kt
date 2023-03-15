package com.atguigu.myownmusicapp.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.bean.homefragmentbean.PlaylistRecommendData
import com.atguigu.myownmusicapp.bean.homefragmentbean.PlaylistRecommendDataResult
import com.atguigu.myownmusicapp.databinding.CellRecommendPlayListBinding
import com.atguigu.myownmusicapp.ui.SongPlaylistActivity
import com.bumptech.glide.Glide

private const val TAG = "myTagRecPlayListAdapter"
class RecommendPlayListAdapter(arraylist:PlaylistRecommendData):RecyclerView.Adapter<RecommendPlayListAdapter.RecommendPlayListViewHolder>() {
    private var arr = arraylist.result

    inner class RecommendPlayListViewHolder(val itemBinding: CellRecommendPlayListBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecommendPlayListAdapter.RecommendPlayListViewHolder {
        val itemBinding =
            CellRecommendPlayListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecommendPlayListViewHolder(itemBinding)
    }


    override fun onBindViewHolder(
        holder: RecommendPlayListAdapter.RecommendPlayListViewHolder,
        position: Int
    ) {
        val item = arr[position]
        with(holder.itemBinding) {
            tvPlayListName.text = item.name
            "${item.playCount / 10000}万次播放".also { tvTrackCount.text = it }
            Glide.with(root)
                .load(item.picUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivPlayListCover)
            clRecommendPlayList.setOnClickListener {
                val intent= Intent(it.context, SongPlaylistActivity::class.java)
                intent.putExtra("ivUrl",item.picUrl)
                intent.putExtra("tvName",item.name)
                intent.putExtra("id",item.id)
                it.context.startActivity(intent)
            }
        }
    }
    override fun getItemCount(): Int {
        return arr.size
    }
    //todo 先用这个方法，后续可采用livedata 和diffUtil相结合
    //更新数据
    fun submitList(newData:ArrayList<PlaylistRecommendDataResult>){
        Log.d(TAG, "submitList: ")
        arr.clear()
        arr.addAll(newData)
        notifyDataSetChanged()
    }
}