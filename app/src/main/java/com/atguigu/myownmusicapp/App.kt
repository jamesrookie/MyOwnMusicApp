package com.atguigu.myownmusicapp

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        //初始化设置的内容
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
        when(sharedPreferences?.getString("dark mode", "2")?.toInt()){
            1->{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            2->{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            3->{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            4->{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }
        }

        //context=applicationContext
        //repository=AppRepository(this)
        //playQueue= repository.playQueueData.asLiveData()
        //playQueue.observeForever {   shuffledPlayQueue.value= playQueue.value?.shuffled()
        //}
        //load(playModeKey)
        //load(searchModeKey)
        //startMusicService()
        //updateMyFavoriteData()
    }
    //开启音乐服务
    private fun startMusicService() {
        // 通过 Service 播放音乐，混合启动
        //val intent = Intent(this, PlayMusicService::class.java)
        //startService(intent)
        //绑定服务
        //bindService(intent,musicServiceConnection, BIND_AUTO_CREATE)
    }
    //companion object{
        /*var searchModeKey="searchMode"
        //var musicController=PlayMusicService.musicController
        @SuppressLint("StaticFieldLeak")
        lateinit var context:Context
        lateinit var repository: AppRepository
        lateinit var playQueue: LiveData<List<PlayQueueData>>
        var shuffledPlayQueue:MutableLiveData<List<PlayQueueData>> = MutableLiveData()
        var playMode: MutableLiveData<PlayMode> = MutableLiveData()
        var searchMode: MutableLiveData<MusicSource> = MutableLiveData()
        val musicServiceConnection by lazy { MusicServiceConnection() } //音乐服务连接
        //lateinit var myFavoriteData:LiveData<List<MyFavoriteData>> //最喜爱的歌曲
        fun toastSomeThing(str:String){
            Toast.makeText(context,str, Toast.LENGTH_SHORT).show()
        }*/
        //保存当前播放信息，下次打开继续放
        /*fun savePlayingSong(){
            //保存播放模式
            val sharedPreferences= context.getSharedPreferences(shpName, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            playMode.value?.let { editor.putInt(playModeKey, it.ordinal) }
            searchMode.value?.let { editor.putInt(searchModeKey,it.ordinal) }
            editor.apply()
            try {
                val objectOutPutStream= ObjectOutputStream(context.openFileOutput("FILE_NAME", MODE_PRIVATE))
                objectOutPutStream.writeObject(PlayMusicService.currentPlayQueue?.standSongData)
                objectOutPutStream.flush()
                objectOutPutStream.close()
            }catch(e:java.lang.Exception){
                Log.e("myTag", "savePlayingSong: $e" )
            }
        }*/
        /*fun updateMyFavoriteData(){
            //myFavoriteData=repository.myFavoriteData.asLiveData()
            //Log.d("boy", "updateMyFavoriteData: ${myFavoriteData.value}")
        }*/

    //}

    /*private fun load(key:String){
        val sharedPreferences=this.getSharedPreferences(shpName, Context.MODE_PRIVATE)
        //第二个参数是default value
        when(key){
            "playMode"->{
                when(sharedPreferences.getInt("playMode",0)){
                    0->{ playMode.value= PlayMode.PLAY_IN_ORDER
                    }
                    1->{playMode.value= PlayMode.PLAY_RANDOM
                    }
                    2->{playMode.value= PlayMode.PLAY_SINGLE_LOOP
                    }
                }
            }
            "searchMode"->{
                when(sharedPreferences.getInt("searchMode",3)){
                    0->{ searchMode.value= MusicSource.MIGU }
                    1->{searchMode.value=MusicSource.NETEASE}
                    2->{ searchMode.value=MusicSource.MIGU2}
                    3->{ searchMode.value=MusicSource.KUWO}
                    4->{searchMode.value=MusicSource.KUGOU}
                }
            }
        }

    }*/


}