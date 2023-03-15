package com.atguigu.myownmusicapp.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.atguigu.myownmusicapp.R

class KeyValueView(context: Context, attrs:AttributeSet): ConstraintLayout(context,attrs) {
    private val typedArray=context.obtainStyledAttributes(attrs, R.styleable.KeyValueView)
    private val title=typedArray.getString(R.styleable.KeyValueView_text)
    private val value=typedArray.getString(R.styleable.KeyValueView_value)
    private var tvTitle:TextView
    private var tvValue:TextView

    init{
        LayoutInflater.from(context).inflate(R.layout.key_value_view_layout,this)
        tvTitle=findViewById(R.id.tvTitle)
        tvValue=findViewById(R.id.tvValue)
        tvTitle.text=title
        tvValue.text=value
        //设置颜色，这样可以在夜间模式适配
        tvTitle.setTextColor(ContextCompat.getColor(context,R.color.textviewColor))
        tvValue.setTextColor(ContextCompat.getColor(context,R.color.textviewColor))
    }
    fun setTitle(string:String){
        tvTitle.text=string
    }
    fun setValue(string:String){
        tvValue.text=string
    }
}