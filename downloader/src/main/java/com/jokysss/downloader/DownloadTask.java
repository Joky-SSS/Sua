package com.jokysss.downloader;

import com.jokysss.downloader.progress.ProgressListener;
import com.jokysss.downloader.progress.ProgressManager;

import java.io.File;

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
    private Subscription sub;
    public DownloadTask(String url){
        this.url = url;
    }
    public void start(){
        if(!DownloadManager.isRun(url)){
            DownloadManager.addTask(this);
            sub = DownloadHelper.getApi().download(url).subscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<ResponseBody>() {
                        @Override
                        public void onCompleted() {
                            DownloadManager.removeTask(url);
                        }
                        @Override
                        public void onError(Throwable e) {
                            DownloadManager.removeTask(url);
                        }
                        @Override
                        public void onNext(ResponseBody responseBody) {
                            Filehelper.writeResponseBodyToDisk(responseBody, path+ File.separator+fileName);
                        }
                    });
        }
    }
    public void stop(){
        if(sub != null && !sub.isUnsubscribed()){
            sub.unsubscribe();
        }
    }
    public DownloadTask addListener(ProgressListener listener){
        ProgressManager.getInstance().addResponseListener(url,listener);
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
}
