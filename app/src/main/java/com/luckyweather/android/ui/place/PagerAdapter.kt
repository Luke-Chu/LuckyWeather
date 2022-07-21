package com.luckyweather.android.ui.place

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luckyweather.android.R
import com.luckyweather.android.databinding.ItemPagerBinding
import com.luckyweather.android.logic.model.Weather
import com.luckyweather.android.logic.model.getSky
import com.luckyweather.android.ui.weather.WeatherActivity
import java.text.SimpleDateFormat
import java.util.*

class PagerAdapter(private val activity: WeatherActivity, private val weatherList: MutableList<Weather>):
    RecyclerView.Adapter<PagerAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ItemPagerBinding): RecyclerView.ViewHolder(binding.root){
        private val nowBinding = binding.now
        private val forecastBinding = binding.forecast
        private val lifeIndexBinding = binding.lifeIndex
        //下拉刷新
        val swipeRefresh = binding.swipeRefresh
        //now.xml
        val placeName = nowBinding.placeName
        val currentTemp = nowBinding.currentTemp
        val currentSky = nowBinding.currentSky
        val currentAQI = nowBinding.currentAQI
        val nowLayout = nowBinding.nowLayout
        //forecast.xml
        val forecastLayout = forecastBinding.forecastLayout
        //life_index.xml
        val coldRiskText = lifeIndexBinding.coldRiskText
        val dressingText = lifeIndexBinding.dressingText
        val ultravioletText = lifeIndexBinding.ultravioletText
        val carWashingText = lifeIndexBinding.carWashingText
        //ScrollView
        val weatherLayout = binding.weatherLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPagerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        val holder = ViewHolder(binding)
        //给每页添加刷新事件
        holder.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        holder.swipeRefresh.setOnRefreshListener {
            val position = holder.bindingAdapterPosition
            val location = activity.viewModel.locationList[position]
            activity.viewModel.refreshPageWeather(location.lng, location.lat)
            holder.swipeRefresh.isRefreshing = true
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.placeName.text = activity.viewModel.placeNameList[position]
        showWeatherInfo(holder, position)
    }

    private fun showWeatherInfo(holder: ViewHolder, position: Int){
        val weather = weatherList[position]
        val realtime = weather.realtime
        val daily = weather.daily
        //设置now.xml
        val currentTempText = "${realtime.temperature.toInt()}℃"
        holder.currentTemp.text = currentTempText
        holder.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        holder.currentAQI.text = currentPM25Text
        holder.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        //设置forecast.xml
        holder.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days){
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(activity).inflate(R.layout.forecast_item, holder.forecastLayout, false)
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
            holder.forecastLayout.addView(view)
        }
        //填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        //生活指数方面服务器会返回很多天的数据，但是界面你上只需要当天数据就可以，所以对所有生活指数都取了下标为0的那个元素
        holder.coldRiskText.text = lifeIndex.coldRisk[0].desc
        holder.dressingText.text = lifeIndex.dressing[0].desc
        holder.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        holder.carWashingText.text = lifeIndex.carWashing[0].desc
        //最后还需将weatherLayout设置为可见
        holder.weatherLayout.visibility = View.VISIBLE
        //下拉刷新请求结束后需要将isRefreshing设置成false
        holder.swipeRefresh.isRefreshing = false
    }

    override fun getItemCount(): Int {
        return weatherList.size
    }
}