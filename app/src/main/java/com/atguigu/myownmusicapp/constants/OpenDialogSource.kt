package com.atguigu.myownmusicapp.constants

class OpenDialogSource {
    companion object{
        const val FAVORITE="from my favorite"
        const val LOCAL="from local"
        const val NETEASE_PLAY_LIST="from netEase playlist"
        const val SEARCH_RESULT_NETEASE="from search result netease"
        const val SEARCH_RESULT_MIGU="from search result migu"
        const val SEARCH_RESULT_QQ="from search result qq"
        const val SEARCH_RESULT_MIGU2="from search result migu2"
        const val SEARCH_RESULT_KUWO="from search result kuwo"
        const val SEARCH_RESULT_KUGOU="from search result kugou"
        const val DOWNLOADED="from downloaded"
        private const val UNKNOWN="unknown source" //这个一般不会用到，所以使用private
        fun sourceToOpenDialogSource(source:MusicSource?):String{
            return when(source){
                MusicSource.DOWNLOAD_KUGOU,MusicSource.DOWNLOAD_KUWO,
                MusicSource.DOWNLOAD_MIGU,MusicSource.DOWNLOAD_MIGU2,
                MusicSource.DOWNLOAD_NETEASE-> DOWNLOADED
                MusicSource.LOCAL-> LOCAL
                MusicSource.KUGOU-> SEARCH_RESULT_KUGOU
                MusicSource.KUWO-> SEARCH_RESULT_KUWO
                MusicSource.MIGU2-> SEARCH_RESULT_MIGU2
                MusicSource.NETEASE-> NETEASE_PLAY_LIST
                MusicSource.MIGU-> SEARCH_RESULT_MIGU
                MusicSource.QQ-> SEARCH_RESULT_QQ
                else -> UNKNOWN
            }
        }
    }
}