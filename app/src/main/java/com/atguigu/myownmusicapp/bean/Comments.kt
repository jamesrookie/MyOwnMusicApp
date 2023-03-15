package com.atguigu.myownmusicapp.bean

data class Comments(
    //todo  还有个topComments和Comments暂时不做了
    var hotComments:List<HotCommentItem>?
){
    data class HotCommentItem(
        var user:User?,
        var content:String?,
        var time:Long?,
        var likedCount:Long?,
        var commentId:Long?,//该评论的id，点赞用得上
        var liked:Boolean?//是否已经点赞过

    )
    data class User(
        var avatarUrl:String?,
        var nickname:String?
    )
}
//上面的是所有人的评论，下面这个是我发送评论后收到的评论
//comment里面的也是HotCommentItem
data class MyComments(
    //响应码，一般200为通过
    val code:Int?,
    var comment:Comments.HotCommentItem?
)
//这个是点赞成功或者退出登录发送回来的实体
data class ThumbUpOrLogoutResp(
    //响应码：一般是200
    val code:Int
)
