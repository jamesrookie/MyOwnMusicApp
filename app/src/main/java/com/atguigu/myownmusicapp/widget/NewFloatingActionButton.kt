package com.atguigu.myownmusicapp.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.sqrt

/*Java中是getRawX和getX，kotlin中是rawX和x
getRawX：获取的是真实X坐标，相对于屏幕左上角
getRawY：获取的是真实Y坐标，相对于屏幕左上角
getLeft：获取的是View自身的左边距离父布局的左边的距离
getBottom：获取的是View自身的底部距离父布局的顶部的距离
getRight：获取的是View自身的右边距离父布局的左边的距离
getTop：获取的是View自身的顶部到父布局顶部的距离
getX：获取的是点击事件所在view距离父控件左边的距离
getY：获取的是点击事件所在view距离父控件顶边的距离

* */
class NewFloatingActionButton(context: Context,attributeSet: AttributeSet):FloatingActionButton(context,attributeSet) {
    private var parentHeight=0
    private var parentWidth=0
    private var lastX=0
    private var lastY=0
    private var isDrag=false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val rawX=ev.rawX.toInt()//未移动前的坐标
        val rawY=ev.rawY.toInt()
        when(ev.action and MotionEvent.ACTION_MASK){
            MotionEvent.ACTION_DOWN->{
                isPressed=true
                isDrag=false
                parent.requestDisallowInterceptTouchEvent(true)
                lastX=rawX
                lastY=rawY
                val viewParent:ViewGroup=parent as ViewGroup
                parentHeight=viewParent.height
                parentWidth=viewParent.width
            }
            MotionEvent.ACTION_MOVE->{
                if(parentHeight<=0||parentHeight<=0){
                    //如果不存在父类的宽高则无法拖动，默认直接返回false
                    isDrag=false
                }else{
                    //计算手指移动了多少
                    val dx=rawX-lastX
                    val dy=rawY-lastY
                    //修复一些手机无法触发点击事件的问题
                    val distance= sqrt(((dx*dx+dy*dy).toDouble()))
                    if(distance<3){//给个容错范围，不然有部分手机还是无法点击
                        isDrag=false
                    }else{
                        //程序到这儿一定是在拖动了
                        isDrag=true
                        var currentX=x+dx
                        var currentY=y+dy
                        //检测是否到边缘 左右上下
                        if(currentX<0){
                            currentX=0F
                        }else{
                            if(currentX>parentWidth-width){
                                currentX= (parentWidth-width).toFloat()
                            }
                        }
                        if(y<0){
                            currentY=0F
                        }else{
                            if(currentY>parentHeight-height){
                                currentY= (parentHeight-height).toFloat()
                            }
                        }
                        x=currentX
                        y=currentY
                        lastX=rawX
                        lastY=rawY
                    }
                }
            }
            MotionEvent.ACTION_UP->{
                if (isDrag){
                    //恢复按压效果
                    isPressed=false
                }
            }

        }
        //如果是拖拽则消耗事件，否则正常传递
        return isDrag() || super.onTouchEvent(ev)
    }
    private fun isDrag():Boolean{
        return isDrag
    }
}