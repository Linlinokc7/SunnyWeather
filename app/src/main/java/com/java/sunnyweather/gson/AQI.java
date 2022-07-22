package com.java.sunnyweather.gson;

import com.java.sunnyweather.util.HttpUtil;

public class AQI {
    public AQICity city;
    public class AQICity{
        public String api;
        public String pm25;
    }
}
