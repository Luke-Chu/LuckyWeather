package com.luckyweather.android.ui.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.luckyweather.android.logic.Repository
import com.luckyweather.android.logic.model.Location
import com.luckyweather.android.logic.model.Weather

class WeatherViewModel: ViewModel() {

    private val locationLiveData = MutableLiveData<Location>()

    private val pageLocationLiveData = MutableLiveData<Location>()

    val placeNameList = ArrayList<String>()

    val locationList = ArrayList<Location>()

    val weatherList = ArrayList<Weather>()

    var locationLng = ""

    var locationLat = ""

    var placeName = ""

    val weatherLiveData = Transformations.switchMap(locationLiveData){ location ->
        Repository.refreshWeather(location.lng, location.lat)
    }

    val pageWeatherLiveData = Transformations.switchMap(pageLocationLiveData){ location ->
        Repository.refreshWeather(location.lng, location.lat)
    }

    fun refreshWeather(lng: String, lat: String){
        locationLiveData.value = Location(lng, lat)
    }

    fun refreshPageWeather(lng: String, lat: String){
        pageLocationLiveData.value = Location(lng, lat)
    }
}