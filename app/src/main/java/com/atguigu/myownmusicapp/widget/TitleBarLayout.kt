package com.atguigu.myownmusicapp.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.atguigu.myownmusicapp.R

@SuppressLint("ResourceAsColor")
class TitleBarLayout(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private var tvTitleBar:TextView
    private val typedArray=context.obtainStyledAttributes(attrs,R.styleable.TitleBarLayout)
    val text=typedArray.getString(R.styleable.TitleBarLayout_text)
    init {
        LayoutInflater.from(context).inflate(R.layout.titlebar_layout,this)
        tvTitleBar=findViewById(R.id.tvTitleBar)
        val btnBack=findViewById<ImageView>(R.id.btnBack)
        tvTitleBar.text=text
        btnBack.setOnClickListener {
            val activity=context as Activity
            activity.finish()
        }
    }
    fun setTitleBarText(text:String){
        tvTitleBar.text=text
    }

}