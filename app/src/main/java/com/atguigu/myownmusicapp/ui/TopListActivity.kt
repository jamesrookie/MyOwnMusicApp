package com.atguigu.myownmusicapp.ui

import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.atguigu.myownmusicapp.adapter.RecommendPlayListDetailAdapter
import com.atguigu.myownmusicapp.base.BaseActivity
import com.atguigu.myownmusicapp.databinding.ActivityTopListBinding
import com.atguigu.myownmusicapp.viewmodel.TopListViewModel
import kotlinx.coroutines.launch

class TopListActivity : BaseActivity() {
    private lateinit var binding:ActivityTopListBinding
    private val topListViewModel:TopListViewModel by viewModels()
    override fun initBinding() {
        binding= ActivityTopListBinding.inflate(layoutInflater)
        miniPlayer=binding.miniPlayer
        setContentView(binding.root)
    }

    override fun initView() {
        lifecycleScope.launch {
            binding.rvPlayListDetail.layoutManager=LinearLayoutManager(this@TopListActivity)
            binding.rvPlayListDetail.adapter= topListViewModel.getTopListData().value?.let {
                RecommendPlayListDetailAdapter(it.list)
            }
        }
    }

    override fun initListener() {
    }
}