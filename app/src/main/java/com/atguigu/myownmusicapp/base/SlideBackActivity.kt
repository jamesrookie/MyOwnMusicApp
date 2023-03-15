package com.atguigu.myownmusicapp.base

import android.content.Context
import android.view.View
import com.atguigu.myownmusicapp.widget.SlideBackLayout

/*
* 拖拽关闭的Activity
* */
abstract class SlideBackActivity : BaseActivity() {
    //SlideBackLayout拖拽关闭Activity
    private lateinit var slideBackLayout:SlideBackLayout

    var slideBackEnabled:Boolean=true
        set(value){
            slideBackLayout.viewEnabled=value
            field=value
        }

    fun bindSlide(context: Context, view: View){
        slideBackLayout= SlideBackLayout(context,view)
        slideBackLayout.bind()
    }
}