package com.atguigu.myownmusicapp.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.customview.widget.ViewDragHelper

class SlideBackLayout(context: Context,private val scrollableView:View): FrameLayout(context) {
    private lateinit var decorView:ViewGroup
    private lateinit var myRootView:View
    private lateinit var activity:Activity
    private lateinit var viewDragHelper:ViewDragHelper
    private var slideHeight:Float=0F
    private var screenHeight:Int=0
    private val cancelHideHeightPercent=0.15f
    var viewEnabled=true

    init{
        init(context)
    }
    private fun init(context: Context){
        activity=context as Activity
        viewDragHelper= ViewDragHelper.create(this,DragCallback())
    }
    fun bind(){
        decorView=activity.window.decorView as ViewGroup
        myRootView=decorView.getChildAt(0)
        decorView.removeView(myRootView)
        this.addView(myRootView)
        decorView.addView(this)

        val displayMetrics=DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenHeight=(displayMetrics.heightPixels*1.1).toInt()
        slideHeight=displayMetrics.heightPixels*cancelHideHeightPercent
    }
    inner class DragCallback:ViewDragHelper.Callback(){
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val top=releasedChild.top
            if(top<=slideHeight){
                viewDragHelper.settleCapturedViewAt(0,0)
            }else{
                viewDragHelper.settleCapturedViewAt(0,screenHeight)
            }
            invalidate()
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return screenHeight
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            if(changedView===myRootView && top>=screenHeight){
                activity.finish()
            }
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return 0
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return if(top>=0){
                top
            }else{
                0
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if(viewEnabled){
            if(scrollableView.scrollY==0){
                viewDragHelper.shouldInterceptTouchEvent(event)

            }else{
                false
            }
        }else{
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            viewDragHelper.processTouchEvent(event)
        }
        return true
    }
    override fun computeScroll(){
        if(viewDragHelper.continueSettling(true)){
            invalidate()
        }
    }
}