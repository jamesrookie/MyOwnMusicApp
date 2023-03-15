package com.atguigu.myownmusicapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.api.Retrofit163Instance
import com.atguigu.myownmusicapp.api.Retrofit163service
import com.atguigu.myownmusicapp.base.BaseActivity
import com.atguigu.myownmusicapp.bean.loginbean.UserProfile
import com.atguigu.myownmusicapp.databinding.ActivityMainBinding
import com.atguigu.myownmusicapp.event.UpdateProfileEvent
import com.atguigu.myownmusicapp.fragment.HomeFragment
import com.atguigu.myownmusicapp.fragment.MyFragment
import com.atguigu.myownmusicapp.utils.SharedPreferencesHelper
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

private const val TAG = "myTag-MainActivity"
private const val PERMISSION_REQUEST_READ_AND_WRITE = 1
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var retrofit163service: Retrofit163service
    private lateinit var imageProfile:ImageView
    private lateinit var tvUsername:TextView

    override fun initBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        miniPlayer = binding.miniPlayer
        setContentView(binding.root)
        //请求本地读写权限
        requestPermission()
    }

    override fun initView() {
        val navView: NavigationView = binding.navigationView
        val headerView = navView.getHeaderView(0)
        imageProfile=headerView.findViewById(R.id.ivProfile)
        tvUsername=headerView.findViewById(R.id.tvUsername)
        //自己给自己发布事件，更新用户信息
        EventBus.getDefault().postSticky(UpdateProfileEvent())
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menuAbout -> {
                    val intent = Intent(this@MainActivity, AboutActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuSetting -> {
                    val intent = Intent(this@MainActivity, SettingActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuChangeAccount -> {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuProfile -> {
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.menuExit -> {
                    finish()
                }
            }
            true
        }
        //初始化viewpager2
        binding.viewpager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> MyFragment()
                    else -> HomeFragment()
                }
            }
        }
        //将TabLayout和viewpager2关联起来
        TabLayoutMediator(binding.tabLayout, binding.viewpager2) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.my)
                else -> getString(R.string.home)
            }
        }.attach()
        //val navController = findNavController(R.id.nav_host_fragment_content_main)
        //navView.setupWithNavController(navController)
    }

    override fun initData() {
        super.initData()
        retrofit163service= Retrofit163Instance.getRetroInstance().create(Retrofit163service::class.java)
    }
    override fun initListener() {
        with(binding) {
            imageMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
            ivSearch.setOnClickListener {
                startActivity(
                    Intent(
                        this@MainActivity,
                        SearchActivity::class.java
                    )
                )
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_READ_AND_WRITE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_REQUEST_READ_AND_WRITE->{
                if(grantResults.isNotEmpty()&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    //Toast.makeText(this,"请求权限成功", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this,"请求权限失败，无法访问本地音乐", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //更新个人图片和用户名
    @Subscribe(sticky = true)
    fun onUpdateProfile(updateProfileEvent: UpdateProfileEvent){
        //todo 1、查看用户登录的cookie是否有效
        val userCookie = SharedPreferencesHelper.readCookie(this)
        Log.d(TAG, "onUpdateProfile: $userCookie")
        if(userCookie.isNullOrBlank()){
            tvUsername.text="尚未登录"
            imageProfile.setImageResource(R.drawable.avatar)
            return
        }
        //todo 2、请求数据
        retrofit163service.get163UserProfile(URLEncoder.encode(userCookie,"utf-8")).enqueue(
            object: Callback<UserProfile> {
                override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                    val body = response.body()
                    Log.d(TAG, "onResponse: $body")
                    //todo 3、填充视图
                    body?.profile?.avatarUrl?.let {
                        Glide.with(binding.root)
                            .load(it)
                            .into(imageProfile)
                    }
                    body?.profile?.nickname?.let{
                        tvUsername.text=it
                    }
                }

                override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                    Toast.makeText(this@MainActivity,"请求用户数据出错，请稍后重试!",Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}