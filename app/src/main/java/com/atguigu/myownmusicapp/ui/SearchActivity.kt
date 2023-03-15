package com.atguigu.myownmusicapp.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.View.*
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.atguigu.myownmusicapp.adapter.SearchHotAdapter
import com.atguigu.myownmusicapp.adapter.SearchResultAdapter
import com.atguigu.myownmusicapp.base.BaseActivity
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.databinding.ActivitySearchBinding
import com.atguigu.myownmusicapp.utils.SharedPreferencesHelper
import com.atguigu.myownmusicapp.viewmodel.SearchViewModel

class SearchActivity : BaseActivity() {
    private lateinit var binding:ActivitySearchBinding
    private val searchViewModel:SearchViewModel by viewModels()
    private var searchResultAdapter:SearchResultAdapter=SearchResultAdapter()
    override fun initBinding() {
        binding= ActivitySearchBinding.inflate(layoutInflater)
        miniPlayer=binding.miniPlayer
        window.setSoftInputMode(SOFT_INPUT_ADJUST_PAN)//设置软键盘的显示方式，不会把整体布局向上顶
        setContentView(binding.root)
    }

    override fun initView() {
        //获取焦点
        binding.etSearch.apply {
            isFocusable=true
            isFocusableInTouchMode=true
            requestFocus()
        }
        binding.rgSearchEngine.check(SharedPreferencesHelper.readSearchEngine(this))
        //获取默认热搜关键词和热搜列表
        lifecycleScope.launchWhenCreated {
            binding.etSearch.hint= searchViewModel.getHotKeyWord().value?.data?.showKeyword ?: "请输入关键词"
            //禁止recyclerview在scrollView内部滑动
            binding.rvHotSearch.layoutManager =LinearLayoutManager(this@SearchActivity)
            val adapter= searchViewModel.getSearchHotList().value?.data?.let {
                SearchHotAdapter(it).apply {
                    setOnItemClick(object :SearchHotAdapter.OnItemClick{
                        override fun onItemClick(view: View?, position: Int) {
                            val searchWord=it[position].searchWord
                            binding.etSearch.setText(searchWord)
                            search()
                        }
                    })
                }
            }
            binding.rvHotSearch.adapter=adapter
        }
        binding.rvSearchResultList.layoutManager=LinearLayoutManager(this)
        binding.rvSearchResultList.adapter=searchResultAdapter
    }

    override fun onPause() {
        super.onPause()
        //保存搜索引擎的选项
        SharedPreferencesHelper.saveSearchEngine(this,binding.rgSearchEngine.checkedRadioButtonText.toString())
    }
    override fun initListener() {
        with(binding){
            ivBack.setOnClickListener {
                this@SearchActivity.finish()
            }
            ilMyGithub.setOnClickListener{
                val uri: Uri = Uri.parse("https://github.com/jamesrookie/myownmusic") //设置跳转的网站
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            btnSearch.setOnClickListener {
                search()
            }
            etSearch.addTextChangedListener {
                ivDelete.visibility= VISIBLE
            }
            ivDelete.setOnClickListener {
                //清空输入框
                etSearch.text.clear()
                ivDelete.visibility= INVISIBLE
            }
            fab.setOnClickListener {
                //滑动到顶部
                rvSearchResultList.smoothScrollToPosition(0)
            }
        }
    }

    private fun search(){
        // 关闭软键盘
        val inputMethodManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.window?.decorView?.windowToken, 0)
        val etSearch=binding.etSearch
        val nsvSearch=binding.nsvSearch
        val rvSearchResultList=binding.rvSearchResultList
        val searchType: MusicSource?
        var searchWords=etSearch.hint.toString()
        if(etSearch.text.isNotEmpty()){
            searchWords=etSearch.text.toString()
        }
        val searchTypeString=binding.rgSearchEngine.checkedRadioButtonText.toString()
        searchType=MusicSource.strToMusicSource(searchTypeString)
        searchViewModel.saveQuery(searchType,searchWords)
        nsvSearch.visibility=GONE
        rvSearchResultList.visibility= VISIBLE
        binding.fab.visibility=VISIBLE
        searchViewModel.searchResults.removeObservers(this)
        searchViewModel.searchResults.observe(this@SearchActivity){
            searchResultAdapter.submitData(lifecycle,it)
        }
        rvSearchResultList.smoothScrollToPosition(0)
    }

    override fun onBackPressed() {
        if(binding.nsvSearch.visibility== GONE){
            binding.nsvSearch.visibility= VISIBLE
            binding.rvSearchResultList.visibility= GONE
            binding.fab.visibility= GONE
        }else{
            super.onBackPressed()
        }
    }

}