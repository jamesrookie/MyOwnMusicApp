package com.atguigu.myownmusicapp.ui

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.atguigu.myownmusicapp.adapter.CommentAdapter
import com.atguigu.myownmusicapp.api.Retrofit163Instance
import com.atguigu.myownmusicapp.api.Retrofit163service
import com.atguigu.myownmusicapp.bean.Comments
import com.atguigu.myownmusicapp.bean.MyComments
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.bean.ThumbUpOrLogoutResp
import com.atguigu.myownmusicapp.constants.MediaControllerRelated.Companion.STANDARD_SONG
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.databinding.ActivityCommentBinding
import com.atguigu.myownmusicapp.event.ThumbUpEvent
import com.atguigu.myownmusicapp.utils.SharedPreferencesHelper
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "myTag-CommentActivity"
private const val TOAST_TEXT_PLEASE_LOGIN="请先登录!"
private const val TOAST_TEXT_SHOW_COMMENTS_TRY_AGAIN="请求歌曲评论出错，请稍后重试!"
private const val TOAST_TEXT_PLEASE_INPUT_COMMENTS="请输入评论!"
private const val TOAST_TEXT_SEND_COMMENTS_SUCCESS="发送评论成功!"
private const val TOAST_TEXT_SEND_COMMENTS_FAIL="发送评论失败!"
private const val TOAST_TEXT_ONLY_SUPPORT_NETEASE_COMMENTS="目前仅支持查看网易云音乐的精彩评论!"
class CommentActivity : AppCompatActivity() {
    private lateinit var binding:ActivityCommentBinding
    private lateinit var retrofit163service: Retrofit163service
    private var commentsList=ArrayList<Comments.HotCommentItem>()
    private lateinit var myAdapter:CommentAdapter
    private var standardSong: StandardSong?=null
    private var showCommentCallback=object: Callback<Comments>{
        override fun onResponse(call: Call<Comments>, response: Response<Comments>) {
            val body=response.body()
            val hotCommentsItem = body?.hotComments
            if (hotCommentsItem != null) {
                commentsList.addAll(hotCommentsItem)
            }
            binding.rvComment.layoutManager=LinearLayoutManager(this@CommentActivity)
            myAdapter=CommentAdapter(commentsList)
            binding.rvComment.adapter= myAdapter
        }

        override fun onFailure(call: Call<Comments>, t: Throwable) {
            Log.e(TAG, "onFailure: ${t.printStackTrace()}")
            toastUtil(TOAST_TEXT_SHOW_COMMENTS_TRY_AGAIN)
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initListener()
    }

    private fun initListener() {
        binding.btnSendComments.setOnClickListener {
            val textToSend=binding.etCommentToSend.text.toString()
            if(textToSend.isBlank()){
                toastUtil(TOAST_TEXT_PLEASE_INPUT_COMMENTS)
            }else{
                //1、判断是否已经登录
                val readCookie = SharedPreferencesHelper.readCookie(this)
                if(readCookie.isNullOrBlank()){
                    toastUtil(TOAST_TEXT_PLEASE_LOGIN)
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    val checkLoginStatus = retrofit163service.checkLoginStatus(readCookie)
                    if(checkLoginStatus.data?.account==null || checkLoginStatus.data.profile==null){
                        toastUtil(TOAST_TEXT_PLEASE_LOGIN)
                    }else{
                        sendComments(textToSend,readCookie)
                    }
                    //todo 隐藏软键盘
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(window.peekDecorView().windowToken,0)
                }

            }
        }
    }

    private fun initData() {
        retrofit163service= Retrofit163Instance.getRetroInstance().create(Retrofit163service::class.java)
        standardSong= intent.getParcelableExtra(STANDARD_SONG)
        if(standardSong?.source!=MusicSource.NETEASE){
            toastUtil(TOAST_TEXT_ONLY_SUPPORT_NETEASE_COMMENTS)
            return
        }
        standardSong?.id?.let {
            val readCookie = SharedPreferencesHelper.readCookie(this)
            if(readCookie.isNullOrBlank())
                retrofit163service.get163SongComments(it).enqueue(showCommentCallback)
            else
                retrofit163service.get163SongComments(it,cookie=readCookie).enqueue(showCommentCallback)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
    @Subscribe
    fun onThumbUpEvent(thumbUpEvent: ThumbUpEvent){
        //1、判断是否是登录状态
        val readCookie = SharedPreferencesHelper.readCookie(this)
        if(readCookie.isNullOrBlank()){
            toastUtil(TOAST_TEXT_PLEASE_LOGIN)
            return
        }
        //校验登录状态
        lifecycleScope.launch{
            val checkLoginStatus = retrofit163service.checkLoginStatus(readCookie)
            if(checkLoginStatus.data?.account==null || checkLoginStatus.data.profile==null){
                toastUtil(TOAST_TEXT_PLEASE_LOGIN)
            }else{
                //2、发送点赞或取消点赞功能
                var liked=thumbUpEvent.liked
                val cid=thumbUpEvent.cid
                val id=standardSong?.id
                if(liked==null){
                    liked=false
                }
                //如果liked是false，那么我们要点赞就应该给1
                val likedNumber=if(liked)0 else 1
                sendThumbUpPost(liked,id,cid,likedNumber,readCookie)
            }
        }
    }
    private fun sendThumbUpPost(liked:Boolean, id:Long?, cid:Long?, likedNumber:Int, readCookie:String){
        if(id!=null && cid!=null){
            retrofit163service.send163SongCommentsLike(id,cid,likedNumber,cookie=readCookie).enqueue(
                object:Callback<ThumbUpOrLogoutResp>{
                    override fun onResponse(call: Call<ThumbUpOrLogoutResp>, response: Response<ThumbUpOrLogoutResp>) {
                        if(response.body()==null){
                            toastUtil(TOAST_TEXT_PLEASE_LOGIN)
                        }
                        if(response.body()?.code==200){
                            val toastText=if(liked)"取消点赞成功" else "点赞成功"
                            toastUtil(toastText)
                            //更新点赞数和大拇指
                            myAdapter.changeLikesCount(cid,liked)
                        }
                    }

                    override fun onFailure(call: Call<ThumbUpOrLogoutResp>, t: Throwable) {
                        toastUtil(TOAST_TEXT_SEND_COMMENTS_FAIL)
                    }

                }
            )
        }
    }

    private fun sendComments(textToSend:String,readCookie: String){
        //2、发送评论
        standardSong?.id?.let { it1 ->
            retrofit163service.send163SongComments(it1,content=textToSend,cookie=readCookie)
                .enqueue(object:Callback<MyComments>{
                    override fun onResponse(
                        call: Call<MyComments>,
                        response: Response<MyComments>
                    ) {
                        //3、判断评论的结果
                        val body = response.body()
                        body?.comment?.let {
                            commentsList.add(it)
                            myAdapter.setList(commentsList)
                            toastUtil(TOAST_TEXT_SEND_COMMENTS_SUCCESS)
                            //清空editText
                            binding.etCommentToSend.text.clear()
                        }
                    }
                    override fun onFailure(call: Call<MyComments>, t: Throwable) {
                        toastUtil(TOAST_TEXT_SEND_COMMENTS_FAIL)
                    }
                })
        }
    }

    fun toastUtil(toastText:String){
        Toast.makeText(this@CommentActivity,toastText,Toast.LENGTH_SHORT).show()
    }
}