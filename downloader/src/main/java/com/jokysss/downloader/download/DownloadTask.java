package com.jokysss.downloader.download;


import com.jokysss.downloader.APIHelper;
import com.jokysss.downloader.FileHelper;
import com.jokysss.downloader.progress.ProgressListener;
import com.jokysss.downloader.progress.ProgressManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by Joky on 2017/6/13 0013.
 */

public class DownloadTask {
    private String url;
    private String fileName;
    private String path;
    private Map<String,String> queryMap = new HashMap<>();
    private Subscription sub;
    private String key = "";

    public DownloadTask(String url){
        this.url = url;
    }
    public Subscription start(){
        sub = APIHelper.getApi().download(url,queryMap,key).subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {}
                    @Override
                    public void onError(Throwable e) {
                    }
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        FileHelper.writeResponseBodyToDisk(responseBody, path+ File.separator+fileName);
                    }
                });
        return sub;
    }
    public void cancel(){
        if(sub != null && !sub.isUnsubscribed()){
            sub.unsubscribe();
        }
    }
    public DownloadTask addListener(String key,ProgressListener listener){
        ProgressManager.getInstance().addResponseListener(key,listener);
        this.key = key;
        return this;
    }
    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public DownloadTask setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getPath() {
        return path;
    }

    public DownloadTask setPath(String path) {
        this.path = path;
        return this;
    }
    public DownloadTask addQuery(String key,String value){
        queryMap.put(key,value);
        return this;
    }
}
