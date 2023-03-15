package com.atguigu.myownmusicapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.atguigu.myownmusicapp.api.Retrofit163Instance
import com.atguigu.myownmusicapp.api.Retrofit163service
import com.atguigu.myownmusicapp.bean.toplistactivitybean.TopListData

class TopListViewModel(application: Application) : AndroidViewModel(application) {
    private var topListData: MutableLiveData<TopListData> = MutableLiveData()
    private var retrofit163service: Retrofit163service =
        Retrofit163Instance.getRetroInstance().create(Retrofit163service::class.java)
    suspend fun getTopListData():MutableLiveData<TopListData>{
        topListData.value=retrofit163service.getTopListDetail()
        return topListData
    }
}