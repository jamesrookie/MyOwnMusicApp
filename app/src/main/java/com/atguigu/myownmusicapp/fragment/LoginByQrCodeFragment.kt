package com.atguigu.myownmusicapp.fragment

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.atguigu.myownmusicapp.api.Retrofit163Instance
import com.atguigu.myownmusicapp.api.Retrofit163service
import com.atguigu.myownmusicapp.bean.loginbean.QrCode
import com.atguigu.myownmusicapp.bean.loginbean.QrCodeStatus
import com.atguigu.myownmusicapp.bean.loginbean.QrKey
import com.atguigu.myownmusicapp.databinding.FragmentLoginByQrCodeBinding
import com.atguigu.myownmusicapp.event.UpdateProfileEvent
import com.atguigu.myownmusicapp.utils.SharedPreferencesHelper
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "myTagLoginByQrCodeFrag"
class LoginByQrCodeFragment : Fragment() {
    private lateinit var binding:FragmentLoginByQrCodeBinding
    private lateinit var retrofit163service: Retrofit163service
    private var base64QrCode:String?=null
    private var qrCodeKey:String?=null
    private var qrCodeCallback=object:Callback<QrCode>{
        override fun onResponse(call: Call<QrCode>, response: Response<QrCode>) {
            val body = response.body()
            base64QrCode = body?.data?.qrimg
            Glide.with(binding.root)
                .load(base64QrCode)
                .into(binding.ivQrCode)
        }

        override fun onFailure(call: Call<QrCode>, t: Throwable) {
            Toast.makeText(context,"获取二维码失败，请稍后重试",Toast.LENGTH_SHORT).show()
        }
    }
    private var qrStatusCallback=object:Callback<QrCodeStatus>{
        override fun onResponse(call: Call<QrCodeStatus>, response: Response<QrCodeStatus>) {
            val body = response.body()
            Log.e(TAG, "onResponse: body ${body?.code}")
            when(body?.code){
                800->{
                    Toast.makeText(context,"二维码过期，请关闭登录面后重新打开",Toast.LENGTH_SHORT).show()
                }
                801->{
                    Toast.makeText(context,"正在等待您扫码，请尽快完成",Toast.LENGTH_SHORT).show()
                }
                802->{
                    Toast.makeText(context,"正在等待系统确认，请稍安勿躁",Toast.LENGTH_SHORT).show()
                }
                803->{
                    Toast.makeText(context,"授权登录成功!",Toast.LENGTH_SHORT).show()
                    //todo 保存数据到shp
                    context?.let { SharedPreferencesHelper.saveCookie(it,body.cookie) }
                    activity?.finish()//关闭当前fragment所在activity
                }
            }
        }

        override fun onFailure(call: Call<QrCodeStatus>, t: Throwable) {
            Toast.makeText(context,"校验当前二维码状态失败",Toast.LENGTH_SHORT).show()
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding= FragmentLoginByQrCodeBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: $qrCodeKey")
        //todo 检验是否登录成功并保存cookie
        qrCodeKey?.let {
            retrofit163service.login163CheckStatus(it).enqueue(qrStatusCallback)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //todo 判断是否处于登录状态，如果是就显示退出登录，如果不是就生成二维码供用户扫码登录
        initData()
        initView()
        initListener()
    }

    private fun initData() {
        retrofit163service=Retrofit163Instance.getRetroInstance().create(Retrofit163service::class.java)
    }

    private fun initView() {
        val cookie= context?.let { SharedPreferencesHelper.readCookie(it) }
        //todo 1、校验登录状态
        lifecycleScope.launch {
            if (!cookie.isNullOrBlank()) {
                val checkLoginStatus = retrofit163service.checkLoginStatus(cookie)
                if(checkLoginStatus.data?.account==null || checkLoginStatus.data.profile==null){
                    //说明cookie过期
                    Log.d(TAG, "initView: cookie已经过期，请重新登录")
                    //todo 2、请求二维码的key
                    requestQrCode()
                    //清空sharedPreferences中的数据
                    context?.let { SharedPreferencesHelper.clearCookie(it) }
                    //去除主页面上的封面图片
                    EventBus.getDefault().post(UpdateProfileEvent())
                }else{
                    //说明登录状态是已经登录，我们就隐藏部分组件的可见性
                    with(binding){
                        tvInfo.visibility=View.GONE
                        ivQrCode.visibility=View.GONE
                        btnSaveToGallery.visibility=View.GONE
                        btnLogout.visibility=View.VISIBLE
                    }
                }
            }else{
                requestQrCode()
            }
        }
    }

    private fun requestQrCode() {
        retrofit163service.login163QrKey().enqueue(object : Callback<QrKey> {
            override fun onResponse(call: Call<QrKey>, response: Response<QrKey>) {
                val qrKey = response.body()
                qrCodeKey = qrKey?.data?.unikey
                if (qrCodeKey != null) {
                    retrofit163service.login163QrCode(qrCodeKey!!).enqueue(qrCodeCallback)
                }
            }
            override fun onFailure(call: Call<QrKey>, t: Throwable) {
                Toast.makeText(context, "生成二维码的key失败，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initListener() {
        //todo 1、设置保存的监听
        binding.btnSaveToGallery.setOnClickListener {
            if(base64QrCode.isNullOrBlank()) Toast.makeText(context,"二维码图片加载失败，请稍后重试",Toast.LENGTH_SHORT).show()
            //存在pictures到系统相册
            //请求权限这一步我们已经做过了
            if(Build.VERSION.SDK_INT< Build.VERSION_CODES.Q){
                saveImage()
            }else{
                lifecycleScope.launch { saveImage29() }
            }
        }
        //todo 设置退出登录的监听
        binding.btnLogout.setOnClickListener {
            //todo 1、拿到cookie
            val cookie= context?.let { SharedPreferencesHelper.readCookie(it) }
            //todo 2、退出登录
            lifecycleScope.launch {
                if (cookie != null) {
                    val logout = retrofit163service.logout(cookie)
                    //todo 3、查看状态码是成功还是失败
                    if(logout.code==200){
                        Toast.makeText(context,"退出登录成功!",Toast.LENGTH_SHORT).show()
                        //隐藏退出登录按钮，显示其他按钮
                        with(binding){
                            tvInfo.visibility=View.VISIBLE
                            ivQrCode.visibility=View.VISIBLE
                            btnSaveToGallery.visibility=View.VISIBLE
                            btnLogout.visibility=View.GONE
                        }
                    }else{
                        Toast.makeText(context,"退出登录失败，请稍后重试!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    /**
     * 保存图片到相册的方法
     *  API29 后此方法已废弃
     */
    private fun saveImage() {
        val toBitmap = binding.ivQrCode.drawable.toBitmap()
        val insertImage = MediaStore.Images.Media.insertImage(
            context?.contentResolver, toBitmap, "qrCode", ""
        )
        if (insertImage.isNotEmpty()) {
            Toast.makeText(context, "图片保存成功！-${insertImage}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "图片保存失败！}", Toast.LENGTH_SHORT).show()
        }
    }
    /**
     * API29 中的最新保存图片到相册的方法
     */
    private suspend fun saveImage29() {
        //开始一个新的进程执行保存图片的操作
        withContext(Dispatchers.IO) {
            val toBitmap = binding.ivQrCode.drawable.toBitmap()
            val insertUri = context?.contentResolver?.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues()
            ) ?: kotlin.run {
                showSaveToast("保存失败！")
                return@withContext
            }
            //使用use可以自动关闭流
            context?.contentResolver?.openOutputStream(insertUri).use {
                if (toBitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) {
                    showSaveToast("保存成功！")
                } else {
                    showSaveToast("保存失败！")
                }
            }
        }
    }
    /**
     * 显示保存图片结果的方法
     */
    private fun showSaveToast(showMsg: String) =
        MainScope().launch {
            Toast.makeText(context, showMsg, Toast.LENGTH_SHORT).show()
        }
}