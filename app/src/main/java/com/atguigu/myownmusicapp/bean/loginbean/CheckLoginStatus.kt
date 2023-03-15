package com.atguigu.myownmusicapp.bean.loginbean

/**
 * 这个是cookie过期返回的数据{"data":{"code":200,"account":null,"profile":null}}
 * 这个是未过期的数据：
 * {"data":{"code":200,"account":{"id":xxx,"userName":"xxx","type":1,"status":0,
 * "whitelistAuthority":0,"createTime":1457091056402,"tokenVersion":0,"ban":0,"baoyueVersion":0,
 * "donateVersion":0,"vipType":0,"anonimousUser":false,"paidFee":false},"profile":{"userId":xxx,
 * "userType":0,"nickname":"Iamsiu","avatarImgId":3142404232208625,"
 * avatarUrl":"xxx","backgroundImgId":2002210674180198,"backgroundUrl":"xxx","signature":"",
 * "createTime":1457091108248,"userName":"xxx,"accountType":1,"shortUserName":"xxx",
 * "birthday":-2209017600000,"authority":0,"gender":1,"accountStatus":0,"province":xxx,"city":xxx,
 * "authStatus":0,"description":null,"detailDescription":null,"defaultAvatar":false,"expertTags":null,
 * "experts":null,"djStatus":0,"locationStatus":10,"vipType":0,"followed":false,"mutual":false,
 * "authenticated":false,"lastLoginTime":xxx,"lastLoginIP":"xxx","remarkName":null,
 * "viptypeVersion":1652286442739,"authenticationTypes":0,"avatarDetail":null,"anchor":false}}}
 * @constructor
 */
data class CheckLoginStatus(
    val data:InnerData?
) {
    data class InnerData(
        val code:Int?,//响应码，不论cookie是否过期，响应码都是200
        val account:UserProfile.UserAccount?,
        val profile: UserProfile.InnerProfile?
    )
}