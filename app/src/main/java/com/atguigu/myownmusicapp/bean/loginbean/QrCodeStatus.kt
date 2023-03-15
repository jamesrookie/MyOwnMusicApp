package com.atguigu.myownmusicapp.bean.loginbean

/**
 * 检查扫描二维码的状态，800 为二维码过期,801 为等待扫码,802 为待确认,803 为授权登录成功(803 状态码下会返回 cookies)
 * @property code Int?
 * @property message String?
 * @property cookie String?
 * @constructor
 */
data class QrCodeStatus(
    var code:Int?,
    var message :String?,
    var cookie:String?
)
