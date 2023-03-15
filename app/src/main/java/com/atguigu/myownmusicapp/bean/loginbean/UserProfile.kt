package com.atguigu.myownmusicapp.bean.loginbean

data class UserProfile(
    var account: UserAccount?,
    var profile:InnerProfile?
) {
    data class UserAccount(
        var id:Long?,
        var createTime:Long?,
        var vipType:Int?,
    )
    data class InnerProfile(
        var nickname:String?,
        var avatarUrl:String?,
        var backgroundUrl:String?,
    )
}