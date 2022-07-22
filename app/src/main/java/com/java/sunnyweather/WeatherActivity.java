package com.java.sunnyweather;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.java.sunnyweather.gson.Forecast;
import com.java.sunnyweather.gson.Weather;
import com.java.sunnyweather.util.HttpUtil;
import com.java.sunnyweather.util.Utility;

import java.io.IOException;
import java.util.zip.Inflater;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView sv_weather;
    private TextView tv_title_city;
    private TextView tv_title_update_time;
    private TextView tv_degree;
    private TextView tv_weather_info;
    private LinearLayout ll_forecast;
    private TextView tv_aqi;
    private TextView tv_pm25;
    private TextView tv_comfort;
    private TextView tv_car_wash;
    private TextView tv_sport;
    private ImageView iv_bing_pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        //初始化控件
        sv_weather = findViewById(R.id.sv_weather);
        tv_title_city = findViewById(R.id.tv_title_city);
        tv_title_update_time = findViewById(R.id.tv_title_update_time);
        tv_degree = findViewById(R.id.tv_degree);
        tv_weather_info = findViewById(R.id.tv_weather_info);
        ll_forecast = findViewById(R.id.ll_forecast);
        tv_aqi = findViewById(R.id.tv_aqi);
        tv_pm25 = findViewById(R.id.tv_pm25);
        tv_comfort = findViewById(R.id.tv_comfort);
        tv_car_wash = findViewById(R.id.tv_car_wash);
        tv_sport = findViewById(R.id.tv_sport);
        iv_bing_pic = findViewById(R.id.iv_bing_pic);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if(weatherString != null){
            //有缓存时直接解析
            Weather weather= Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气
            String weatherId=getIntent().getStringExtra("weather_id");
            sv_weather.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(iv_bing_pic);
        }else{
            loadBingPic();
        }
    }

    /*
    *
    * 根据天气id请求城市天气信息*/
    private void requestWeather(final String weatherId) {

        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "服务器获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if( weather != null && "ok".equals(weather.status)){
                            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor=PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Log.d("weather",String.valueOf(weather));
                            Toast.makeText(WeatherActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        loadBingPic();
    }

    /*
    * 加载必应每日一图*/
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                        .edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(iv_bing_pic);
                    }
                });
            }
        });
    }

    /*
    * 处理并展示weather实体类中的数据*/
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split("")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        tv_title_city.setText(cityName);
        tv_title_update_time.setText(updateTime);
        tv_degree.setText(degree);
        tv_weather_info.setText(weatherInfo);
        ll_forecast.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item
            ,ll_forecast,false);
            TextView tv_date = view.findViewById(R.id.tv_date);
            TextView tv_info = view.findViewById(R.id.tv_info);
            TextView tv_max = view.findViewById(R.id.tv_max);
            TextView tv_min = view.findViewById(R.id.tv_min);
            tv_date.setText(forecast.date);
            tv_info.setText(forecast.more.info);
            tv_max.setText(forecast.temperature.max);
            tv_min.setText(forecast.temperature.min);
            ll_forecast.addView(view);
        }
        if(weather.aqi != null){
            tv_aqi.setText(weather.aqi.city.api);
            tv_pm25.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度："+weather.suggestion.comfort.info;
        String carWash = "洗车指数："+weather.suggestion.carwash.info;
        String sport = "运动建议："+weather.suggestion.sport.info;
        tv_comfort.setText(comfort);
        tv_car_wash.setText(carWash);
        tv_sport.setText(sport);
        sv_weather.setVisibility(View.VISIBLE);
    }
}