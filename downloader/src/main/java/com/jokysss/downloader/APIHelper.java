package com.jokysss.downloader;

/**
 * Created by Joky on 2017/6/13 0013.
 */

public class APIHelper {
    private static SuaAPI api = RetrofitProvider.getInstance().create(SuaAPI.class);
    public static SuaAPI getApi(){
        return api;
    }
}
