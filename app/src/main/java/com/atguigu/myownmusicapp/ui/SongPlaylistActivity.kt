package com.atguigu.myownmusicapp.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.adapter.SongPlaylistAdapter
import com.atguigu.myownmusicapp.base.BaseActivity
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.bean.songplaylistactivitybean.PlayListAll
import com.atguigu.myownmusicapp.constants.MediaControllerRelated
import com.atguigu.myownmusicapp.databinding.ActivitySongPlaylistBinding
import com.atguigu.myownmusicapp.utils.ClassConvertHelper
import com.atguigu.myownmusicapp.viewmodel.SongPlaylistViewModel
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SongPlaylistActivity : BaseActivity() {
    private lateinit var binding:ActivitySongPlaylistBinding
    private lateinit var adapter:SongPlaylistAdapter
    private var playlistId:Long=0
    private var playlist=ArrayList<StandardSong>()
    private val playlistViewModel:SongPlaylistViewModel by lazy {
        ViewModelProvider(this).get(SongPlaylistViewModel::class.java)
    }

    override fun initBinding() {
        binding=ActivitySongPlaylistBinding.inflate(layoutInflater)
        miniPlayer=binding.miniPlayer
        setContentView(binding.root)
    }

    override fun initView() {
        binding.rvSongPlayList.layoutManager=LinearLayoutManager(this)
        adapter=SongPlaylistAdapter()
        binding.rvSongPlayList.adapter=adapter
        val intent = this.intent
        val url = intent.getStringExtra("ivUrl")
        val tvName=intent.getStringExtra("tvName")
        val tvDesc=intent.getStringExtra("tvDesc")
        Glide.with(binding.root)
            .load(url)
            .placeholder(R.drawable.ic_launcher_background)
            .into(binding.ivListCover)
        binding.tvPlayListName.text=tvName
        binding.tvPlayListDesc.text=tvDesc
    }

    override fun initData() {
        val intent = this.intent
        playlistId=intent.getLongExtra("id",0)
        /**
         * 原有方法采用viewModel+pagingData分页加载，现在我们不需要分页
         */
        playlistViewModel.saveQuery(playlistId)
        playlistViewModel.getPlayListInfo(playlistId)
        /*playlistViewModel.songs.observe(this){
            adapter.submitData(lifecycle,it)
        }
        */
        playlistViewModel.listInfo.observe(this){ it ->
            if(binding.tvPlayListDesc.text.isEmpty()){
                binding.tvPlayListDesc.text=it.description
            }
            "播放全部(共${it.trackCount}首)".also { binding.tvPlayAll.text = it }
        }
        /**
         * 新方法
         */
        playlistViewModel.retrofit163service.getPlayListDetail(playlistId)
            .enqueue(object:Callback<PlayListAll>{
                override fun onResponse(call: Call<PlayListAll>, response: Response<PlayListAll>) {
                    val songs = response.body()?.songs
                    songs?.forEachIndexed { _, songDetail ->
                        playlist.add(ClassConvertHelper.songDetailToStandardSong(songDetail))
                    }
                    adapter.submitList(songs)
                    //"播放全部(共${playlist.size}首)".also { binding.tvPlayAll.text = it }
                }

                override fun onFailure(call: Call<PlayListAll>, t: Throwable) {
                    Toast.makeText(this@SongPlaylistActivity,"加载失败，请稍后重试!",Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun initListener() {
        binding.tvPlayListDesc.setOnClickListener {
            val intent=Intent(this,PlayListDescActivity::class.java)
                .putExtra("ivCover",playlistViewModel.listInfo.value?.coverImgUrl)
                .putExtra("tvName",playlistViewModel.listInfo.value?.name)
                .putExtra("tvDesc",playlistViewModel.listInfo.value?.description)
            startActivity(intent)
        }
        binding.clPlayAll.setOnClickListener {
            val mediaController= MediaControllerCompat.getMediaController(this)
            val bundle= Bundle()
            bundle.putParcelableArrayList(MediaControllerRelated.ALL_SONGS_KEY,playlist)
            mediaController.transportControls.sendCustomAction(MediaControllerRelated.PLAY_ALL_SONGS,bundle)
        }
    }
}