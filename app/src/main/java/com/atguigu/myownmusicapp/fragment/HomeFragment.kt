package com.atguigu.myownmusicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.atguigu.myownmusicapp.adapter.RecommendNewSongsAdapter
import com.atguigu.myownmusicapp.adapter.RecommendPlayListAdapter
import com.atguigu.myownmusicapp.base.BaseFragment
import com.atguigu.myownmusicapp.bean.homefragmentbean.PlaylistRecommendData
import com.atguigu.myownmusicapp.databinding.FragmentHomeBinding
import com.atguigu.myownmusicapp.ui.TopListActivity
import com.atguigu.myownmusicapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeBinding
    private val mainViewModel:MainViewModel by activityViewModels()
    private var playlistRecommendData:PlaylistRecommendData?=PlaylistRecommendData(ArrayList())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentHomeBinding.inflate(layoutInflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun initView() {
        //歌单推荐板块布局
        val gridLayoutManager = GridLayoutManager(requireContext(), 2,GridLayoutManager.HORIZONTAL,false)
        binding.rvPlaylistRecommend.layoutManager=gridLayoutManager
        val myNewSongAdapter=RecommendPlayListAdapter(PlaylistRecommendData(ArrayList()))
        binding.rvNewSong.adapter=myNewSongAdapter

        lifecycleScope.launch {
            playlistRecommendData=mainViewModel.getPlaylistRecommend()
            if(playlistRecommendData!=null){
                binding.rvPlaylistRecommend.adapter=RecommendPlayListAdapter(playlistRecommendData!!)
            }
        }
        //新歌速递板块布局
        val gridLayoutManager2 = GridLayoutManager(requireContext(), 2)
        binding.rvNewSong.layoutManager=gridLayoutManager2
        lifecycleScope.launch{
            val newSongs=mainViewModel.getNewSongRecommend().value
                if (newSongs != null) {
                    binding.rvNewSong.adapter=RecommendNewSongsAdapter(newSongs.result)
            }
        }
        //每日句子推荐
        lifecycleScope.launch {
            val result=mainViewModel.getDailySentence()
            binding.includeFoyou.apply {
                tvAuthor.text=result.author
                tvSource.text=result.source
                tvText.text=result.text
            }
        }

    }
    override fun initListener() {
        binding.llTopList.setOnClickListener { startActivity(Intent(requireContext(),TopListActivity::class.java)) }
        binding.includeFoyou.root.setOnClickListener {
            lifecycleScope.launch {
                val result=mainViewModel.getDailySentence()
                binding.includeFoyou.apply {
                    tvAuthor.text=result.author
                    tvSource.text=result.source
                    tvText.text=result.text
                }
            }
        }
    }
}