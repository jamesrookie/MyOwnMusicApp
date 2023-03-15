package com.atguigu.myownmusicapp.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.atguigu.myownmusicapp.R

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class ItemLayout(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val typedArray=context.obtainStyledAttributes(attrs, R.styleable.ItemLayout)
    private val text=typedArray.getString(R.styleable.ItemLayout_text)
    private val itemType=typedArray.getInt(R.styleable.ItemLayout_itemType,1)
    init{
        LayoutInflater.from(context).inflate(R.layout.item_layout,this)
        val tvItem=findViewById<TextView>(R.id.tvItem)
        val ivGoto=findViewById<ImageView>(R.id.ivGoto)
        tvItem.text=text
        Log.d("myTag", "tvItem.text:${text}")
        when(itemType){
            //no
            0->ivGoto.visibility= View.INVISIBLE
            //goto
            1->ivGoto.visibility=View.VISIBLE
            //
            2->{
            }
        }
    }
}