package com.jokysss.downloader.download;

import android.os.Handler;
import android.os.Looper;

import com.jokysss.downloader.APIHelper;
import com.jokysss.downloader.FileHelper;
import com.jokysss.downloader.Sua;
import com.jokysss.downloader.ThreadService;
import com.jokysss.downloader.progress.ProgressListener;
import com.jokysss.downloader.progress.ProgressManager;
import com.jokysss.downloader.progress.body.ProgressInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by Joky on 2017/6/13 0013.
 */

public class DownloadTask {
    private String url;
    private String fileName;
    private String path;
    private Map<String,String> queryMap = new HashMap<>();
    private Disposable sub;
    private String key = "";
    private static Scheduler scheduler = Schedulers.from(ThreadService.getDownloadService());
    public DownloadTask(String key, String url){
        this.key = key;
        this.url = url;
    }

    public Disposable start() {
        if(!Sua.checkDownTaskSingle(key,this)){
            return null;
        }
        sub = APIHelper.getApi().download(url,queryMap,key).subscribeOn(scheduler).subscribeWith(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onComplete() {
                        Set<ProgressListener> listenerSet = ProgressManager.getInstance().removeResponseListener(key);
                        if (listenerSet == null) return;
                        final ProgressInfo info = new ProgressInfo(key);
                        info.setFinish(true);
                        Handler h = new Handler(Looper.getMainLooper());
                        for (final ProgressListener listener : listenerSet) {
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onProgress(info);
                                }
                            });
                        }
                        Sua.removeDownTask(key);
                    }
                    @Override
                    public void onError(Throwable e) {
                    }
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        FileHelper.writeResponseBodyToDisk(responseBody, path + fileName);
                    }
                });
        return sub;
    }
    public void cancel(){
        if (sub != null && !sub.isDisposed()) {
            sub.dispose();
        }
    }
    public DownloadTask addListener(ProgressListener listener){
        if(listener != null){
            ProgressManager.getInstance().addResponseListener(key,listener);
        }
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
    public DownloadTask addQuery(String key, String value){
        queryMap.put(key,value);
        return this;
    }
}
