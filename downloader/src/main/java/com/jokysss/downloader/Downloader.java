package com.jokysss.downloader;

/**
 * Created by Joky on 2017/6/13 0013.
 */

public class Downloader {

    public static DownloadTask download(String url){
        DownloadTask task = new DownloadTask(url);
        return task;
    }

}
