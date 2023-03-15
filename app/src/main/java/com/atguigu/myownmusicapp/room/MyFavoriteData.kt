package com.atguigu.myownmusicapp.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.atguigu.myownmusicapp.bean.StandardSong

@Entity
data class MyFavoriteData(
    @Embedded
    var songData:StandardSong,
    @PrimaryKey(autoGenerate = true)
    var myFavoriteId:Int?
)
