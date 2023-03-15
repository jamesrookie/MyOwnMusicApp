package com.atguigu.myownmusicapp.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.adapter.MyFavoriteAdapter
import com.atguigu.myownmusicapp.base.BaseActivity
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MediaControllerRelated
import com.atguigu.myownmusicapp.databinding.ActivityMyFavoriteBinding
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

class MyFavoriteActivity : BaseActivity() {
    private lateinit var binding:ActivityMyFavoriteBinding
    private var favoriteList= ArrayList<StandardSong>()
    private lateinit var myAdapter:MyFavoriteAdapter
    override fun initBinding() {
        super.initBinding()
        binding= ActivityMyFavoriteBinding.inflate(layoutInflater)
        miniPlayer=binding.miniPlayer
        setContentView(binding.root)
    }

    override fun initData() {
        super.initData()
        myAdapter= MyFavoriteAdapter()
    }
    override fun initView() {
        //处理滑动删除的操作
        with(binding.rvSongPlayList){
            layoutManager=LinearLayoutManager(this@MyFavoriteActivity)
            this.adapter=myAdapter
            //public SimpleCallback(int dragDirs, int swipeDirs)
            //dragDirs表示拖动方向(0表示不支持)，swipeDirs表示滑动方向
            /*ItemTouchHelper.UP | ItemTouchHelper.DOWN*/
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                        return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val songToDelete: StandardSong = favoriteList[viewHolder.absoluteAdapterPosition]
                    baseViewModel.deleteFavoriteItem(songToDelete)
                    Snackbar.make(this@with, "从❤歌单删除了一首歌曲", Snackbar.LENGTH_SHORT)
                        .setAction("撤销") {
                            baseViewModel.addFavoriteItem(songToDelete)
                        }.show()
                }

                var icon = ContextCompat.getDrawable(this@MyFavoriteActivity, R.drawable.ic_delete)
                var background: Drawable = ColorDrawable(Color.LTGRAY)
                override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    val itemView = viewHolder.itemView
                    val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
                    val iconLeft: Int
                    val iconRight: Int
                    val iconBottom: Int
                    val backLeft: Int
                    val backRight: Int
                    val backTop: Int = itemView.top
                    val backBottom: Int = itemView.bottom
                    val iconTop: Int = itemView.top + (itemView.height - icon!!.intrinsicHeight) / 2
                    iconBottom = iconTop + icon!!.intrinsicHeight
                    when {
                        dX > 0 -> {
                            backLeft = itemView.left
                            backRight = itemView.left + dX.toInt()
                            background.setBounds(backLeft, backTop, backRight, backBottom)
                            iconLeft = itemView.left + iconMargin
                            iconRight = iconLeft + icon!!.intrinsicWidth
                            icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        }
                        dX < 0 -> {
                            backRight = itemView.right
                            backLeft = itemView.right + dX.toInt()
                            background.setBounds(backLeft, backTop, backRight, backBottom)
                            iconRight = itemView.right - iconMargin
                            iconLeft = iconRight - icon!!.intrinsicWidth
                            icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        }
                        else -> {
                            background.setBounds(0, 0, 0, 0)
                            icon!!.setBounds(0, 0, 0, 0)
                        }
                    }
                    background.draw(c)
                    icon!!.draw(c)
                }
            }).attachToRecyclerView(this) //依附到recycleView
        }
    }

    override fun initObserver() {
        super.initObserver()
        baseViewModel.myFavoriteData.observe(this,{
            favoriteList.clear()//先清空列表
            if(it!=null){
                for(item in it){
                    favoriteList.add(item.songData)
                }
            }
            myAdapter.submitList(favoriteList)
            myAdapter.notifyDataSetChanged()
            binding.tvPlayAll.text = this.getString(R.string.playlistNumber,favoriteList.size)
            //设置封面的大图
            if(favoriteList.isNotEmpty()){
                Glide.with(this)
                    .load(favoriteList[0].picUrl)
                    .centerCrop()
                    .into(binding.ivListCover)
            }
        })
    }

    override fun initListener() {
        super.initListener()
        binding.clPlayAll.setOnClickListener {
            val mediaController= MediaControllerCompat.getMediaController(this)
            val bundle= Bundle()
            bundle.putParcelableArrayList(MediaControllerRelated.ALL_SONGS_KEY,favoriteList)
            mediaController.transportControls.sendCustomAction(MediaControllerRelated.PLAY_ALL_SONGS,bundle)
        }
    }
}