package com.luckyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.luckyweather.android.R
import com.luckyweather.android.databinding.ActivityWeatherBinding
import com.luckyweather.android.logic.model.Weather
import com.luckyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*


class WeatherActivity : AppCompatActivity() {
    lateinit var weatherBinding: ActivityWeatherBinding
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weatherBinding = ActivityWeatherBinding.inflate(layoutInflater)
        /**
         * 将背景图和状态栏融合到一起
         * 更简单的实现方法：
         * 调用decorView.systemUiVisibility来改变系统UI的显示，这里设置为Activity的布局会现实在状态栏上面
         * 最后调用statusBarColor方法，设置成透明色，此时系统状态栏已经成为我们布局的一部分。
         */
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        setContentView(weatherBinding.root)
        //从Intent中获取经度和纬度，并赋值到WeatherViewModel的相应变量中
        if (viewModel.locationLng.isEmpty()){
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()){
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()){
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        //然后对weatherLiveData对象进行观察，
        viewModel.weatherLiveData.observe(this){
            val weather = it.getOrNull()
            if (weather != null){
                showWeatherInfo(weather)
            }else {
                Toast.makeText(this,"无法陈功获取天气信息", Toast.LENGTH_SHORT).show()
                it.exceptionOrNull()?.printStackTrace()
            }
            //下拉刷新请求结束后需要将isRefreshing设置成false
            weatherBinding.swipeRefresh.isRefreshing = false
        }
        //下拉刷新
        weatherBinding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        refreshWeather()
        weatherBinding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
        //当获取到服务器返回的天气数据时，就调用showWeatherInfo()方法进行一次刷新天气的请求
        //viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)

        //滑动菜单的逻辑处理
        weatherBinding.now.navBtn.setOnClickListener {
            weatherBinding.drawerLayout.openDrawer(GravityCompat.START)//打开滑动菜单
        }
        //监听DrawerLayout的状态
        weatherBinding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            //致命错误：An operation is not implemented: Not yet implemented
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            //当滑动菜单被隐藏时，同时也要隐藏输入法
            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }

            override fun onDrawerStateChanged(newState: Int) {}

        })
    }

    /**
     * 将之前刷新天气的代码提取到refreshWeather()函数中
     */
    fun refreshWeather(){
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        weatherBinding.swipeRefresh.isRefreshing = true
    }

    private fun showWeatherInfo(weather: Weather){
        val nowBinding = weatherBinding.now
        val forecastBinding = weatherBinding.forecast
        val lifeIndexBinding = weatherBinding.lifeIndex
        nowBinding.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        //填充给now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()}℃"
        nowBinding.currentTemp.text = currentTempText
        nowBinding.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        nowBinding.currentAQI.text = currentPM25Text
        nowBinding.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        //填充forecast.xml布局中的数据
        forecastBinding.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days){
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastBinding.forecastLayout, false)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateInfo = view.findViewById<TextView>(R.id.dateInfo)
            val skyIcon = view.findViewById<ImageView>(R.id.skyIcon)
            val skyInfo = view.findViewById<TextView>(R.id.skyInfo)
            val temperatureInfo = view.findViewById<TextView>(R.id.temperatureInfo)
            //日期
            dateInfo.text = simpleDateFormat.format(skycon.date)
            //天气图标和文字
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            //温度
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            //添加view
            forecastBinding.forecastLayout.addView(view)
        }
        //填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        //生活指数方面服务器会返回很多天的数据，但是界面你上只需要当天数据就可以，所以对所有生活指数都取了下标为0的那个元素
        lifeIndexBinding.coldRiskText.text = lifeIndex.coldRisk[0].desc
        lifeIndexBinding.dressingText.text = lifeIndex.dressing[0].desc
        lifeIndexBinding.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        lifeIndexBinding.carWashingText.text = lifeIndex.carWashing[0].desc
        //最后要让ScrollView变成可见状态
        weatherBinding.weatherLayout.visibility = View.VISIBLE
    }
}