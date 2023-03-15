package com.atguigu.myownmusicapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.atguigu.myownmusicapp.api.Retrofit163Instance
import com.atguigu.myownmusicapp.api.Retrofit163service
import com.atguigu.myownmusicapp.bean.loginbean.UserProfile
import com.atguigu.myownmusicapp.databinding.ActivityProfileBinding
import com.atguigu.myownmusicapp.utils.SharedPreferencesHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "myTag-ProfileActivity"
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding:ActivityProfileBinding
    private lateinit var retrofit163service: Retrofit163service
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
    }

    private fun initData() {
        retrofit163service= Retrofit163Instance.getRetroInstance().create(Retrofit163service::class.java)
    }

    private fun initView() {
        //todo 1、查看用户登录的cookie是否有效
        val userCookie = SharedPreferencesHelper.readCookie(this)
        Log.d(TAG, "onUpdateProfile: $userCookie")
        if(userCookie.isNullOrBlank()){
            openLoginActivity()
            return
        }
        //校验登录状态
        lifecycleScope.launch {
            val checkLoginStatus = retrofit163service.checkLoginStatus(userCookie)
            if(checkLoginStatus.data?.profile==null || checkLoginStatus.data.account==null){
                openLoginActivity()
            }else{
                //todo 2、请求数据
                requestProfileData(userCookie)
            }
        }


    }

    /**
     * 请求用户数据
     * @param userCookie String?
     */
    private fun requestProfileData(userCookie: String?) {
        retrofit163service.get163UserProfile(URLEncoder.encode(userCookie, "utf-8")).enqueue(
            object : Callback<UserProfile> {
                @SuppressLint("SimpleDateFormat")
                override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                    val body = response.body()
                    //todo 3、填充视图
                    //设置背景
                    body?.profile?.backgroundUrl?.let {
                        Glide.with(binding.root)
                            .load(it)
                            .into(object : CustomTarget<Drawable>() {
                                override fun onResourceReady(
                                    resource: Drawable,
                                    transition: Transition<in Drawable>?
                                ) {
                                    binding.llProfile.background = resource
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    Log.e(TAG, "onLoadCleared: 用户背景图片加载失败")
                                }
                            })
                    }
                    body?.profile?.avatarUrl?.let {
                        Glide.with(binding.root)
                            .load(it)
                            .into(binding.ivAvatar)
                    }
                    body?.profile?.nickname?.let {
                        binding.tvUsername.text = it
                    }
                    body?.account?.id?.let {
                        binding.tvUserId.text = it.toString()
                    }
                    body?.account?.createTime?.let {
                        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val date = Date(it)
                        binding.tvCreateTime.text = simpleDateFormat.format(date)
                    }
                    body?.account?.vipType?.let {
                        binding.tvUserType.text = it.toString()
                    }
                }

                override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "请求用户数据出错，请稍后重试!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )
    }

    private fun openLoginActivity(){
        Toast.makeText(this@ProfileActivity,"请登录",Toast.LENGTH_SHORT).show()
        //打开登录页面
        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()//能否直接结束当前activity?
    }
}