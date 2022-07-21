package com.luckyweather.android.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.luckyweather.android.databinding.ActivityWeatherBinding
import com.luckyweather.android.logic.model.Location
import com.luckyweather.android.ui.place.PagerAdapter


class WeatherActivity : AppCompatActivity() {
    lateinit var weatherBinding: ActivityWeatherBinding
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    private lateinit var pagerAdapter: PagerAdapter
    private var pagePos: Int = 0

    @SuppressLint("NotifyDataSetChanged")
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
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }

        //然后对weatherLiveData对象进行观察，
        viewModel.weatherLiveData.observe(this) {
            val weather = it.getOrNull()
            if (weather != null) {
                if (!viewModel.placeNameList.contains(viewModel.placeName)) {
                    viewModel.placeNameList.add(viewModel.placeName)
                    viewModel.locationList.add( Location(viewModel.locationLng, viewModel.locationLat))
                    viewModel.weatherList.add(weather)
                    val viewPager2 = weatherBinding.viewPager
                    pagerAdapter = PagerAdapter(this, viewModel.weatherList)
                    viewPager2.adapter = pagerAdapter
                }
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                it.exceptionOrNull()?.printStackTrace()
            }
        }

        //然后对weatherLiveData对象进行观察，刷新观察
        viewModel.pageWeatherLiveData.observe(this) {
            val weather = it.getOrNull()
            if (weather != null) {
                viewModel.weatherList[pagePos] = weather
                pagerAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                it.exceptionOrNull()?.printStackTrace()
            }
        }

        refreshWeather()

        weatherBinding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pagePos = position
                Log.d("WeatherActivity", position.toString())
            }
        })
        //滑动菜单的逻辑处理
        weatherBinding.navBtn.setOnClickListener {
            weatherBinding.drawerLayout.openDrawer(GravityCompat.START)//打开滑动菜单
        }
        //监听DrawerLayout的状态
        weatherBinding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            //致命错误：An operation is not implemented: Not yet implemented
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            //当滑动菜单被隐藏时，同时也要隐藏输入法
            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(
                    drawerView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }

            override fun onDrawerStateChanged(newState: Int) {}

        })
    }

    /**
     * 将之前刷新天气的代码提取到refreshWeather()函数中
     */
    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
    }
}