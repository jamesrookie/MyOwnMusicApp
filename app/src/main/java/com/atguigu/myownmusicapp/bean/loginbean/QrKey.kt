package com.atguigu.myownmusicapp.bean.loginbean

data class QrKey(
    var data:QrKeyInner
){
    data class QrKeyInner(
        val unikey:String?
    )
}
