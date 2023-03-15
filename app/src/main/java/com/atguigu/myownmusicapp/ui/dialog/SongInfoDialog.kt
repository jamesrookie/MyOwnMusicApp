package com.atguigu.myownmusicapp.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.databinding.DialogSongInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SongInfoDialog(private val standardSong: StandardSong): BottomSheetDialogFragment() {
    private lateinit var binding: DialogSongInfoBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DialogSongInfoBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            this.kvId.setValue(standardSong.id.toString())
            this.kvImageUrl.setValue(standardSong.picUrl.toString())
            this.kvSongUrl.setValue(standardSong.url_320.toString())
            var source ="网易云"
            when(standardSong.source){
                MusicSource.KUGOU->{source="酷狗"}
                MusicSource.KUWO->{source="酷我"}
                MusicSource.QQ->{source="QQ"}
                MusicSource.MIGU,MusicSource.MIGU2->{source="咪咕"}
                MusicSource.LOCAL->{source ="本地"}
                MusicSource.DOWNLOAD_NETEASE,MusicSource.DOWNLOAD_MIGU2,MusicSource.DOWNLOAD_MIGU,
                MusicSource.DOWNLOAD_KUWO,MusicSource.DOWNLOAD_KUGOU->{
                    source="已下载的音乐"
                    kvImageUrl.setTitle("歌曲封面路径")
                    kvSongUrl.setTitle("音乐的本地路径")
                }
                else->{}
            }
            this.kvSource.setValue(source)

        }
    }
}