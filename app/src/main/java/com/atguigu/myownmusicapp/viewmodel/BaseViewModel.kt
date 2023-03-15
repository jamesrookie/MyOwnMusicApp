package com.atguigu.myownmusicapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.room.AppRepository
import com.atguigu.myownmusicapp.room.MyFavoriteData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 用来作为所有ViewModel的基类。
 * 1、用来保存读取♥的歌曲列表
 */
open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    //❤歌单
    private var _myFavoriteData:LiveData<List<MyFavoriteData>>
    val myFavoriteData:LiveData<List<MyFavoriteData>>
    //是否在❤歌单里
    private val _isFavorite: MutableLiveData<Boolean> = MutableLiveData()
    val isFavorite:LiveData<Boolean> =_isFavorite
    private val repository:AppRepository= AppRepository(application)
    init {
        _myFavoriteData=repository.getMyFavorite()
        myFavoriteData=_myFavoriteData
    }

    /**
     * 添加歌曲到❤
     * @param standardSong StandardSong
     */
    fun addFavoriteItem(standardSong: StandardSong){
        val ifInData=checkIfInFavoriteData(standardSong)
        if (!ifInData) {
            val myFavoriteData=MyFavoriteData(standardSong,null)
            //添加到数据库中
            viewModelScope.launch(Dispatchers.IO){
                repository.insertMyFavorite(arrayListOf(myFavoriteData))
            }
        }
    }

    /**
     * 将歌曲从❤删除
     * @param standardSong StandardSong
     */
    fun deleteFavoriteItem(standardSong: StandardSong){
        val deleteIndex=checkIfInFavoriteData(standardSong)
        if(deleteIndex){
            //从数据库中删除
            viewModelScope.launch(Dispatchers.IO) {
                val source=standardSong.source
                val name=standardSong.name
                val artists=standardSong.artists
                if(source!=null && name!=null && artists!=null)
                repository.deleteMyFavorite(source,name,artists)
            }
        }
    }

    /**
     * 修改❤中的歌曲，主要用来修改播放链接，因为有时效性
     * @param standardSong StandardSong
     */
    fun updateItem(standardSong: StandardSong){
        //todo 修改
    }

    /**
     * 根据传过来的standardSong确定当前歌曲是否是❤
     * @param standardSong StandardSong?
     */
    fun updateIsFavorite(standardSong: StandardSong?){
        if(standardSong==null){
            _isFavorite.value=false
            return
        }
        val flag=checkIfInFavoriteData(standardSong)
        _isFavorite.value = flag
    }

    /**
     * 手动修改isFavorite的值
     */
    fun changeIsFavorite(){
        if(_isFavorite.value!=null){
            _isFavorite.value=!_isFavorite.value!!
        }
    }

    /**
     * 检查是否已经在❤队列
     * @param standardSong StandardSong
     * @return Boolean 返回是否在队列的布尔值
     */
    private fun checkIfInFavoriteData(standardSong: StandardSong):Boolean{
        val source=standardSong.source
        val name = standardSong.name
        val artists=standardSong.artists
        return if(source!=null && name!=null && artists!=null) {
            repository.existsInFavorite(source, name, artists)
        }else false
    }
}