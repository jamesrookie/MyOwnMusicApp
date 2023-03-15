package com.atguigu.myownmusicapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.bean.toplistactivitybean.ListData
import com.atguigu.myownmusicapp.databinding.CellRecommendPlayListDetailBinding
import com.atguigu.myownmusicapp.ui.SongPlaylistActivity
import com.bumptech.glide.Glide

class RecommendPlayListDetailAdapter(arrayList: ArrayList<ListData>):
    RecyclerView.Adapter<RecommendPlayListDetailAdapter.RecommendPlayListDetailViewHolder>() {
    private var recommendPlayListDetail:ArrayList<ListData> =arrayList
    inner class RecommendPlayListDetailViewHolder(val itemBinding: CellRecommendPlayListDetailBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecommendPlayListDetailAdapter.RecommendPlayListDetailViewHolder {
        val itemBinding = CellRecommendPlayListDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecommendPlayListDetailViewHolder(itemBinding)
    }

    override fun onBindViewHolder(
        holder: RecommendPlayListDetailAdapter.RecommendPlayListDetailViewHolder,
        position: Int
    ) {
        val item = recommendPlayListDetail[position]
        with(holder.itemBinding){
            tvPlayListCategory.text=item.name
            tvWhichDayUpdate.text=item.updateFrequency

            Glide.with(root)
                .load(item.coverImgUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivPlayListCategory)

            clPlayListDetail.setOnClickListener {
                val intent= Intent(it.context,SongPlaylistActivity::class.java)
                intent.putExtra("ivUrl",item.coverImgUrl)
                intent.putExtra("tvName",item.name)
                intent.putExtra("tvDesc",item.description)
                intent.putExtra("id",item.id)
                it.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return recommendPlayListDetail.size
    }
}