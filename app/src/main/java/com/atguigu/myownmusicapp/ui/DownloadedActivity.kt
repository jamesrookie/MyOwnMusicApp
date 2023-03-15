package com.atguigu.myownmusicapp.ui

import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.adapter.DownloadedAdapter
import com.atguigu.myownmusicapp.base.BaseActivity
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MediaControllerRelated
import com.atguigu.myownmusicapp.databinding.ActivityDownloadedBinding
import com.atguigu.myownmusicapp.event.UpdateDownloadListEvent
import com.atguigu.myownmusicapp.utils.DownloadManager
import com.bumptech.glide.Glide
import org.greenrobot.eventbus.Subscribe

class DownloadedActivity : BaseActivity() {
    private lateinit var binding: ActivityDownloadedBinding
    private var downloadedData: ArrayList<StandardSong> =ArrayList()
    private lateinit var adapter:DownloadedAdapter

    override fun initBinding() {
        super.initBinding()
        binding= ActivityDownloadedBinding.inflate(layoutInflater)
        miniPlayer=binding.miniPlayer
        setContentView(binding.root)
    }

    override fun initView() {
        //添加recyclerview的adapter
        adapter= DownloadedAdapter(downloadedData)
        binding.tvPlayAll.text=this.getString(R.string.playlistNumber,downloadedData.size)
        binding.rvSongPlayList.layoutManager=LinearLayoutManager(this)//没写这句就不显示
        binding.rvSongPlayList.adapter=adapter
        //设置大图的封面
        if(downloadedData.isNotEmpty()){
            Glide.with(this)
                .load(downloadedData[0].picUrl)
                .centerCrop()
                .into(binding.ivListCover)
        }
    }

    override fun initData() {
        super.initData()
        //1、加载本地音乐
        downloadedData=DownloadManager.loadDownloadedMusic(application)
    }

    override fun initListener() {
        super.initListener()
        binding.clPlayAll.setOnClickListener {
            val mediaController= MediaControllerCompat.getMediaController(this)
            val bundle= Bundle()
            bundle.putParcelableArrayList(MediaControllerRelated.ALL_SONGS_KEY,downloadedData)
            mediaController.transportControls.sendCustomAction(MediaControllerRelated.PLAY_ALL_SONGS,bundle)
        }
    }
    @Subscribe
    fun onUpdateDownloadListActivity(updateDownloadListEvent: UpdateDownloadListEvent){
        //更新下载的歌曲的列表
        initData()
        adapter.updateData(downloadedData)
    }
}