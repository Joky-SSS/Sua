package com.jokysss.downloader;


import com.jokysss.downloader.progress.ProgressManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by Joky on 2017/6/13 0013.
 */

public class RetrofitProvider {
    private static String ENDPOINT = "http://example.com/api/";

    private RetrofitProvider() {
    }

    /**
     * 指定endpoint
     *
     * @param endpoint endPoint
     * @return Retrofit
     */
    public static Retrofit getInstance(String endpoint) {
        ENDPOINT = endpoint;
        return SingletonHolder.INSTANCE;
    }

    /**
     * 不指定endPoint
     *
     * @return Retrofit
     */
    public static Retrofit getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final Retrofit INSTANCE = create();

        private static Retrofit create() {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            builder.readTimeout(10, TimeUnit.SECONDS);
            builder.connectTimeout(9, TimeUnit.SECONDS);
            OkHttpClient OkHttpClient = ProgressManager.getInstance().with(builder)
                    .build();

            return new Retrofit.Builder().baseUrl(ENDPOINT)
                    .client(OkHttpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
        }
    }
}
