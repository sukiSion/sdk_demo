package cn.dabby.demo.sdk

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * @author Sion
 * @date 2023/11/24 15:40
 * @version 1.0.0
 * @description
 **/
object OkHttpClient {

    fun getOkHttpClient(): OkHttpClient{
        return OkHttpClient().newBuilder()
            .addInterceptor(HttpLoggingInterceptor())
            .writeTimeout(10 , TimeUnit.SECONDS)
            .readTimeout(10 , TimeUnit.SECONDS)
            .connectTimeout(10 , TimeUnit.SECONDS)
            .build()
    }



}