package com.atguigu.myownmusicapp.bean.loginbean

data class QrCode(
    var data:QrCodeInner
){
    data class QrCodeInner(
        var qrimg:String?
    )
}
