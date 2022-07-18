package com.luckyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.luckyweather.android.LuckyWeatherApplication
import com.luckyweather.android.logic.model.Place

/**
 * 为记录选中的城市做准备
 */
object PlaceDao {
    fun savePlace(place: Place) {
        sharedPreferences().edit {
            putString("place", Gson().toJson(place))
        }
    }

    fun getSavedPlace(): Place {
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    fun isPlaceSaved() = sharedPreferences().contains("place")

    private fun sharedPreferences() =
        LuckyWeatherApplication.context.getSharedPreferences("lucky_weather", Context.MODE_PRIVATE)
}