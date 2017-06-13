package com.jokysss.downloader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Joky on 2017/6/13 0013.
 */

public class DownloadManager {
    private static ConcurrentMap<String,DownloadTask> taskMap = new ConcurrentHashMap<>();
    public static void addTask(DownloadTask task){
        taskMap.put(task.getUrl(),task);
    }
    public static void removeTask(String url){
        taskMap.remove(url);
    }
    public static boolean isRun(String url){
        return taskMap.containsKey(url);
    }
}
