package com.atguigu.myownmusicapp.constants

enum class MusicSource {
    MIGU,NETEASE,MIGU2,KUWO,KUGOU,QQ,LOCAL,DOWNLOAD_NETEASE,DOWNLOAD_MIGU,DOWNLOAD_MIGU2,DOWNLOAD_KUWO,
    DOWNLOAD_KUGOU,DOWNLOAD_QQ,UNKNOWN;
    companion object{
        fun strToMusicSource(str:String):MusicSource{
            return when(str){
                "MIGU","咪咕"-> MIGU
                "NETEASE","网易云"->NETEASE
                "MIGU2","咪咕2号"->MIGU2
                "KUWO","酷我"->KUWO
                "KUGOU","酷狗"->KUGOU
                "QQ"->QQ
                "LOCAL"->LOCAL
                "DOWNLOAD_NETEASE"->DOWNLOAD_NETEASE
                "DOWNLOAD_MIGU"->DOWNLOAD_MIGU
                "DOWNLOAD_MIGU2"->DOWNLOAD_MIGU2
                "DOWNLOAD_KUWO"->DOWNLOAD_KUWO
                "DOWNLOAD_KUGOU"->DOWNLOAD_KUGOU
                "DOWNLOAD_QQ"->DOWNLOAD_QQ
                else -> UNKNOWN
            }
        }
    }

}