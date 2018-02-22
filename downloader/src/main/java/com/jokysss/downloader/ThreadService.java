package com.jokysss.downloader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Joky on 2017/7/14 0014.
 */

public class ThreadService {
    private static final ExecutorService uploadService = Executors.newFixedThreadPool(3);
    private static final ExecutorService downloadService = Executors.newFixedThreadPool(3);

    public static ExecutorService getDownloadService(){
        return downloadService;
    }

    public static ExecutorService getUploadService(){
        return uploadService;
    }
}
