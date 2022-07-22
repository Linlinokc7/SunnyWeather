package com.java.sunnyweather.util;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;

//发起Http请求
public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
//        Log.d("http",callback.toString());
    }
}
