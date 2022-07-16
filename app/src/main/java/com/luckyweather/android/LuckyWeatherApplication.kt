package com.luckyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * 由于从ViewModel层开始就不再持有Activity的引用了，因此经常会出现“缺Context”的情况
 * 可以用如下技术，给LuckyWeather提供一种全局获取Context的方式
 */
class LuckyWeatherApplication: Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        //在彩云天气获取到的令牌
        const val TOKEN = "bv7oPkU410u9RSwZ"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}