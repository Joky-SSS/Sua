package com.jokysss.downloader;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by Joky on 2017/6/13 0013.
 */

public interface DownloadAPI {
    @GET
    @Streaming
    Observable<ResponseBody> download(@Url String url);
}
