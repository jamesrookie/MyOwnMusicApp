package com.atguigu.myownmusicapp.event

import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.ClickFromSource

/**
 * EventBus库的使用，用于Android的事件发布-订阅总线。他简化了应用程序内各个组件之间进行通信的复杂度。
 */
/**
 * 处理在adapter上点击itemView时播放音乐的操作
 * @property standardSong StandardSong
 * @property clickFromSource ClickFromSource 默认值表示是从playListDialog以外的其他地方点击播放的
 * @constructor
 */
class PlayMusicEvent(val standardSong: StandardSong,val clickFromSource: ClickFromSource=ClickFromSource.CLICK_FROM_OTHERS)