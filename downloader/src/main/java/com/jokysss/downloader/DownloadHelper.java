package com.jokysss.downloader;

/**
 * Created by Joky on 2017/6/13 0013.
 */

public class DownloadHelper {
    private static DownloadAPI api = RetrofitProvider.getInstance().create(DownloadAPI.class);
    public static DownloadAPI getApi(){
        return api;
    }
}
