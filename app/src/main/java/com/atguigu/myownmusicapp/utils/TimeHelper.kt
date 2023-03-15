package com.atguigu.myownmusicapp.utils

object TimeHelper {
    /**
     * 把毫秒转换为时间的字符串
     * @param milliSeconds Int
     * @return String
     */
    fun milliSecondsToTimer(milliSeconds:Int):String{
        var timerStringBuilder=""
        val secondsString: String
        val hours:Int= (milliSeconds/(1000*60*60))
        val minutes= (milliSeconds%(1000*60*60)/(1000*60))
        val seconds=(milliSeconds%(1000*60*60)%(1000*60)/1000)
        if(hours>0){
            timerStringBuilder="$hours:"
        }
        secondsString = if(seconds<10){
            "0$seconds"
        }else{
            ""+seconds
        }
        timerStringBuilder= "$timerStringBuilder$minutes:$secondsString"
        return timerStringBuilder
    }
}