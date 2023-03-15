package com.atguigu.myownmusicapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.bean.searchActivitybean.SearchHotListInner
import com.atguigu.myownmusicapp.databinding.CellHotSearchBinding

class SearchHotAdapter(data: ArrayList<SearchHotListInner>) :RecyclerView.Adapter<SearchHotAdapter.SearchHotViewHolder>(),View.OnClickListener {
    private var arr=data
    inner class SearchHotViewHolder(val itemBinding: CellHotSearchBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHotViewHolder {
        val itemBinding = CellHotSearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SearchHotViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: SearchHotViewHolder, position: Int) {
        with(holder.itemBinding){
            root.tag=position   //为后面的OnClick铺路
            tvNumber.apply {
                when(position){
                    0->setTextColor(ContextCompat.getColor(context,R.color.red))
                    1->setTextColor(ContextCompat.getColor(context,R.color.red))
                    2->setTextColor(ContextCompat.getColor(context,R.color.red))
                    else->{}
                }
                text=(position+1).toString()
            }
            tvSongName.text=arr[position].searchWord
            tvScore.text=arr[position].score.toString()
            tvSongDesc.text=arr[position].content
            root.setOnClickListener(this@SearchHotAdapter)
        }
    }

    override fun getItemCount(): Int {
        return arr.size
    }

    interface OnItemClick{
        fun onItemClick(view: View?, position: Int)
    }

    private var onItemClick: OnItemClick? = null

    fun setOnItemClick(onItemClick: OnItemClick?) {
        this.onItemClick = onItemClick
    }

    override fun onClick(v: View?) {
        if (v != null) {
            onItemClick?.onItemClick(v, v.tag as Int)
        }
    }

}