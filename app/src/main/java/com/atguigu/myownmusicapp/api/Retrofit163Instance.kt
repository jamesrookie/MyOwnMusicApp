package com.atguigu.myownmusicapp.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.net.ssl.HostnameVerifier


class Retrofit163Instance {
    companion object{
        //private const val baseURL="http://140.238.34.252:3000/"
        //var baseURL="https://autumnfish.cn/"
        var originalBaseURL="http://81.70.199.89:3000/"
        var baseURL="http://81.70.199.89:3000/"
        var client: OkHttpClient = OkHttpClient.Builder().hostnameVerifier(HostnameVerifier { hostname, session ->
            true
        }).build()
        var gson: Gson = GsonBuilder()
            .setLenient()
            .create()
        fun getRetroInstance(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()

        }
        fun getRetroInstance2():Retrofit{
            return Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(client)
                .build()
        }
        fun getRetroInstance3():Retrofit{
            return Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
        }
        fun getRetroInstance4():Retrofit{
            val newClient = OkHttpClient.Builder()
                .followRedirects(false)
                .build()
            return Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(newClient)
                .build()
        }
        fun changeBaseUrl(newUrl:String){
            baseURL=newUrl
        }
    }
}
