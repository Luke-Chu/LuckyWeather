package com.luckyweather.android.logic

import androidx.lifecycle.liveData
import com.luckyweather.android.logic.dao.PlaceDao
import com.luckyweather.android.logic.model.Place
import com.luckyweather.android.logic.model.Weather
import com.luckyweather.android.logic.network.LuckyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

object Repository {
    /**
     * liveData()函数是lifecycle-livedata-ktx库中提供的一个非常强大且好用的功能，
     * 它可以自动构建并返回一个LiveData对象，然后在它的代码块中提供一个挂起函数的上下文，这样就可以在liveData()函数自动代码块中调用任意的挂起函数了
     * 线程参数Dispatchers.IO：执行的代码大多数时间是在阻塞和等待中，比如执行网络请求，为了能够支持更高的并发量
     * emit()函数就是给设置LiveData的value值
     */
    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = LuckyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    /**
     * coroutineScope是一个挂起函数，并且创建一个子协程
     * async函数必须在协程作用域中才能调用，会创建一个新的子协程并返回一个Deferred对象，调用Deferred对象的await()方法即可获取结果
     */
    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        coroutineScope {
            val deferredRealtime = async {
                LuckyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                LuckyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather =
                    Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }

    /**
     * 对try catch进行封装
     */
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure(e)
            }
            emit(result)
        }

    /**
     * 记录选中的城市
     */
    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()
}