package com.atguigu.myownmusicapp.ui.dialog

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.atguigu.myownmusicapp.R
import com.atguigu.myownmusicapp.bean.StandardSong
import com.atguigu.myownmusicapp.constants.MediaControllerRelated
import com.atguigu.myownmusicapp.constants.MusicSource
import com.atguigu.myownmusicapp.constants.OpenDialogSource
import com.atguigu.myownmusicapp.databinding.DialogSongMenuBinding
import com.atguigu.myownmusicapp.event.DownloadMusicEvent
import com.atguigu.myownmusicapp.event.InsertNextPlayEvent
import com.atguigu.myownmusicapp.event.UpdateDownloadListEvent
import com.atguigu.myownmusicapp.ui.CommentActivity
import com.atguigu.myownmusicapp.utils.ClassConvertHelper.standardSongToQueueItem
import com.atguigu.myownmusicapp.utils.DownloadManager
import com.atguigu.myownmusicapp.viewmodel.BaseViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.greenrobot.eventbus.EventBus

//source是指从那个activity或fragment打开的，source取值参见constants包下的OpenDialogSource
class SongMenuDialog(private val source:String,private val standardSong: StandardSong): BottomSheetDialogFragment() {
    private lateinit var binding:DialogSongMenuBinding
    private val baseViewModel:BaseViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DialogSongMenuBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when(source){
            OpenDialogSource.FAVORITE->{
                binding.itemAddToFavorite.visibility=GONE
                when(standardSong.source){
                    MusicSource.DOWNLOAD_MIGU2,MusicSource.DOWNLOAD_MIGU,MusicSource.DOWNLOAD_KUWO,
                        MusicSource.DOWNLOAD_NETEASE,MusicSource.DOWNLOAD_KUGOU,MusicSource.DOWNLOAD_QQ,MusicSource.UNKNOWN,
                        MusicSource.LOCAL->binding.itemDownload.visibility= GONE
                    else->{}
                }
            }
            OpenDialogSource.LOCAL->{
                binding.itemSongComment.visibility=GONE
                binding.itemDownload.visibility=GONE
                binding.itemDeleteSong.visibility=GONE
            }
            OpenDialogSource.SEARCH_RESULT_NETEASE,OpenDialogSource.NETEASE_PLAY_LIST->{
                binding.itemDeleteSong.visibility=GONE
            }
            OpenDialogSource.DOWNLOADED->{
                binding.itemSongComment.visibility= GONE
                binding.itemDownload.visibility= GONE
            }
            else->{
                binding.itemDeleteSong.visibility= GONE
                binding.itemSongComment.visibility=GONE
            }
        }
        with(binding){
            itemNextPlay.setOnClickListener {
                //插入到播放列表
                EventBus.getDefault().post(InsertNextPlayEvent(standardSong))
                dismiss()//关闭dialog
            }
            itemAddToFavorite.setOnClickListener {
                baseViewModel.addFavoriteItem(standardSong)
                Toast.makeText(context,"已添加到❤歌单",Toast.LENGTH_SHORT).show()
            }
            itemSongComment.setOnClickListener { context?.startActivity(Intent(context,CommentActivity::class.java)) }
            itemDownload.setOnClickListener {
                //下载音乐
                EventBus.getDefault().post(DownloadMusicEvent(standardSong))
            }
            itemDeleteSong.setOnClickListener {
                //todo 分两种情况，一种是下载了的音乐的删除，一种是最爱歌曲的删除
                when(source){
                    OpenDialogSource.FAVORITE->{
                        baseViewModel.deleteFavoriteItem(standardSong)
                        Toast.makeText(context,"已从❤歌单中移除",Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                    OpenDialogSource.DOWNLOADED->{
                        val dialog=AlertDialog.Builder(context)
                            .setMessage(R.string.alertDialogDelete)
                            ?.setNegativeButton(R.string.cancel) { _, _ -> }
                            ?.setPositiveButton(R.string.delete) { _, _ ->
                                val mediaController= MediaControllerCompat.getMediaController(requireActivity())
                                val bundle=Bundle()
                                val queueItemToDelete=standardSongToQueueItem(standardSong)
                                bundle.putParcelable(MediaControllerRelated.QUEUE_ITEM_TO_DELETE,queueItemToDelete)
                                mediaController.transportControls.sendCustomAction(
                                    MediaControllerRelated.DELETE_SPECIFY_SONG_ACTION,bundle)
                                context?.let { it1 ->
                                    DownloadManager.deleteDownloadedMusic(
                                        it1,standardSong)
                                }
                                //删除完以后应该实时更新列表,让DownloadedActivity更新
                                EventBus.getDefault().post(UpdateDownloadListEvent())
                                baseViewModel.deleteFavoriteItem(standardSong)
                                Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show()
                                //删除完就关闭窗口
                                dismiss()
                            }?.create()
                        //修改按钮的默认颜色
                        dialog?.show()
                        dialog?.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                            Color.RED)
                        dialog?.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                            Color.RED)
                    }
                }
            }
            itemSongInfo.setOnClickListener {
                val supportFragmentManager=(context as FragmentActivity).supportFragmentManager
                SongInfoDialog(standardSong).show(supportFragmentManager,null)
            }
        }
    }
}