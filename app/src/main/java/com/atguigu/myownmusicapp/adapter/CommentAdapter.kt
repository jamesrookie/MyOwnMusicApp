package com.atguigu.myownmusicapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.bean.Comments
import com.atguigu.myownmusicapp.databinding.CellCommentsBinding
import com.atguigu.myownmusicapp.event.ThumbUpEvent
import com.bumptech.glide.Glide
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(private var commentsList: List<Comments.HotCommentItem>) : RecyclerView.Adapter<CommentAdapter.CommentHolder>() {
    class CommentHolder(val itemBinding: CellCommentsBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val inflate =
            CellCommentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentHolder(inflate)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        val item=commentsList[position]
        val liked = item.liked
        val simpleDateFormat= SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        holder.itemBinding.apply {
            Glide.with(this.root)
                .load(item.user?.avatarUrl)
                .into(ivUserCover)
            tvContent.text=item.content
            tvThumbUpNumber.text=(item.likedCount?:0).toString()
            //评论日期
            val date = item.time?.let { Date(it) }
            date?.let {  tvTime.text=simpleDateFormat.format(it)}
            tvUsername.text=item.user?.nickname
            //点赞功能
            if(liked==true)ivThumbUp.setColorFilter(R.color.red)
            else ivThumbUp.setColorFilter(R.color.colorTransparent)
            ivThumbUp.setOnClickListener {
                EventBus.getDefault().post(ThumbUpEvent(item.commentId,liked))
            }
        }
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    fun setList(commentsList: ArrayList<Comments.HotCommentItem>) {
        this.commentsList=commentsList
        notifyDataSetChanged()
    }

    /**
     * 修改点赞数
     * @param cid Long
     * @param liked Boolean
     */
    fun changeLikesCount(cid: Long, liked: Boolean) {
        commentsList.forEach {
            if(it.commentId==cid){
                //有可能是自己新写的评论，这时候是没有点赞数的
                if(it.likedCount==null)it.likedCount=0
                if(liked) {
                    it.likedCount = it.likedCount?.minus(1)
                    it.liked=false
                }
                else{
                    it.likedCount= it.likedCount?.plus(1)
                    it.liked=true
                }
                notifyDataSetChanged()
                return
            }
        }
    }
}