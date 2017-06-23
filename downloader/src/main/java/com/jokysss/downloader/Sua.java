package com.jokysss.downloader;


import com.jokysss.downloader.download.DownloadTask;
import com.jokysss.downloader.upload.UploadTask;

/**
 * Created by Joky on 2017/6/13 0013.
 */

public class Sua {

    public static DownloadTask download(String url){
        DownloadTask task = new DownloadTask(url);
        return task;
    }
    public static UploadTask upload(String url){
        UploadTask task = new UploadTask(url);
        return task;
    }
}
