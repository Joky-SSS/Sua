package com.jokysss.downloader;

import com.jokysss.downloader.progress.ProgressManager;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by Joky on 2017/6/13 0013.
 */

public interface SuaAPI {
    @GET
    @Streaming
    Observable<ResponseBody> download(@Url String url, @QueryMap Map<String, String> querys, @Header(ProgressManager.LISTENKEY) String key);

    @POST
    Observable<ResponseBody> upload(@Url String url, @Body RequestBody Body, @Header(ProgressManager.LISTENKEY) String key);
}
