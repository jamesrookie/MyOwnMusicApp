package com.atguigu.myownmusicapp.bean.searchActivitybean.qq

data class QQSongUrlInfo(
    val req_0: InnerQQUrlReq0?
) {
    data class InnerQQUrlReq0(
        val data: InnerQQUrlData?
    )
    data class InnerQQUrlData(
        val midurlinfo:ArrayList<InnerQQUrlMidUrlInfo>?
    )
    data class InnerQQUrlMidUrlInfo(
        val purl:String?
    )
}