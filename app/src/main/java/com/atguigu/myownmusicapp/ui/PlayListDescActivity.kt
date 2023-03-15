package com.atguigu.myownmusicapp.ui

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.base.BaseActivity
import com.atguigu.myownmusicapp.databinding.ActivityPlayListDescBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayListDescActivity : BaseActivity() {
    private lateinit var binding:ActivityPlayListDescBinding
    private lateinit var tvName:String //这个后面保存封面有用
    private lateinit var ivCover:String
    private lateinit var tvDesc:String
    override fun initBinding() {
        binding= ActivityPlayListDescBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView() {
        //全屏
        val decorView=window.decorView
        val option= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility=option
        window.statusBarColor= Color.TRANSPARENT
        val intent=this.intent
        ivCover = intent.getStringExtra("ivCover").toString()
        tvName= intent.getStringExtra("tvName").toString()
        tvDesc= intent.getStringExtra("tvDesc").toString()
        binding.tvName.text=tvName
        binding.tvDesc.text=tvDesc
        Glide.with(binding.root)
            .load(ivCover)
            .placeholder(R.drawable.ic_launcher_background)
            .into(binding.ivCover)

    }

    override fun initListener() {
        binding.btnSaveCover.setOnClickListener {
            //存在pictures到系统相册
            //请求权限这一步我们已经做过了
            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q){
                saveImage()
            }else{
                lifecycleScope.launch { saveImage29() }
            }

        }
        binding.ivClose.setOnClickListener { this.finish() }
    }
    /**
     * 保存图片到相册的方法
     *  API29 后此方法已废弃
     */
    private fun saveImage() {
        val toBitmap = binding.ivCover.drawable.toBitmap()
        val insertImage = MediaStore.Images.Media.insertImage(
            contentResolver, toBitmap, tvName, tvDesc
        )
        if (insertImage.isNotEmpty()) {
            Toast.makeText(this, "图片保存成功！-${insertImage}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "图片保存失败！}", Toast.LENGTH_SHORT).show()
        }
    }
    /**
     * API29 中的最新保存图片到相册的方法
     */
    private suspend fun saveImage29() {
        //开始一个新的进程执行保存图片的操作
        withContext(Dispatchers.IO) {
            val toBitmap = binding.ivCover.drawable.toBitmap()
            val insertUri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues()
            ) ?: kotlin.run {
                showSaveToast("保存失败！")
                return@withContext
            }
            //使用use可以自动关闭流
            contentResolver.openOutputStream(insertUri).use {
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
            Toast.makeText(this@PlayListDescActivity, showMsg, Toast.LENGTH_SHORT).show()
        }
}