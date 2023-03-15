package com.atguigu.myownmusicapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.atguigu.myownmusicapp.base.BaseFragment
import com.atguigu.myownmusicapp.databinding.FragmentMyBinding
import com.atguigu.myownmusicapp.ui.DownloadedActivity
import com.atguigu.myownmusicapp.ui.LocalActivity
import com.atguigu.myownmusicapp.ui.MyFavoriteActivity

class MyFragment : BaseFragment() {
    private lateinit var binding: FragmentMyBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentMyBinding.inflate(layoutInflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun initListener() {
        with(binding){
            this.ivLocal.setOnClickListener {
                val intent=Intent(context,LocalActivity::class.java)
                startActivity(intent)
            }
            this.ivLike.setOnClickListener {
                val intent= Intent(context, MyFavoriteActivity::class.java)
                startActivity(intent)
            }
            this.ivHistory.setOnClickListener {
                startActivity(Intent(context,DownloadedActivity::class.java))
            }
        }
    }
}