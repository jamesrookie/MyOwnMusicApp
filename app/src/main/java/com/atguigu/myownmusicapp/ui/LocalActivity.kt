package com.atguigu.myownmusicapp.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.adapter.LocalAdapter
import com.atguigu.myownmusicapp.base.BaseActivity
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.ALL_SONGS_KEY
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.PLAY_ALL_SONGS
import com.atguigu.myownmusicapp.databinding.ActivityLocalBinding
import com.atguigu.myownmusicapp.utils.LocalMusic
import com.bumptech.glide.Glide

class LocalActivity : BaseActivity(){
    private lateinit var binding: ActivityLocalBinding
    private var localList= ArrayList<StandardSong>()
    override fun initBinding() {
        super.initBinding()
        binding= ActivityLocalBinding.inflate(layoutInflater)
        miniPlayer=binding.miniPlayer
        setContentView(binding.root)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun initView() {
        val adapter= LocalAdapter()
        scanLocalMusic(this)
        adapter.submitList(localList)
        binding.tvPlayAll.text = this.getString(R.string.playlistNumber,localList.size)
        with(binding.rvSongPlayList) {
            layoutManager = LinearLayoutManager(this@LocalActivity)
            this.adapter = adapter
        }
        //设置大图的封面
        if(localList.isNotEmpty()){
            Glide.with(this)
                .load(localList[0].picUrl)
                .centerCrop()
                .into(binding.ivListCover)
        }
    }

    override fun initListener() {
        super.initListener()
        binding.clPlayAll.setOnClickListener {
            val mediaController=MediaControllerCompat.getMediaController(this)
            val bundle=Bundle()
            bundle.putParcelableArrayList(ALL_SONGS_KEY,localList)
            mediaController.transportControls.sendCustomAction(PLAY_ALL_SONGS,bundle)
        }
    }
    @RequiresApi(Build.VERSION_CODES.R)
    fun scanLocalMusic(context:Context){
        LocalMusic.scanLocalMusic(context,{
            runOnUiThread{
                localList=it
            }
        },{})
    }
}