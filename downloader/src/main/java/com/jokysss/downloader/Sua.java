package com.jokysss.downloader;

import com.jokysss.downloader.download.DownloadTask;
import com.jokysss.downloader.progress.ProgressManager;
import com.jokysss.downloader.upload.UploadTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Joky on 2017/6/13 0013.
 */

public class Sua {

    private static Map<String,DownloadTask> downloadTaskMap = new ConcurrentHashMap<>();

    public static DownloadTask download(String key,String url){
        DownloadTask task = new DownloadTask(key,url);
        return task;
    }

    public static UploadTask upload(String key,String url){
        UploadTask task = new UploadTask(key,url);
        return task;
    }

    public static void removeDownTask(String key){
        DownloadTask task = downloadTaskMap.remove(key);
        if(task != null){
            task.cancel();
        }
        ProgressManager.getInstance().removeResponseListener(key);
    }

    public static boolean isDownloading(String key){
        return downloadTaskMap.containsKey(key);
    }

    public synchronized static boolean checkDownTaskSingle(String key,DownloadTask task){
        if(downloadTaskMap.containsKey(key)){
            return false;
        }else{
            downloadTaskMap.put(key,task);
            return true;
        }
    }
}
