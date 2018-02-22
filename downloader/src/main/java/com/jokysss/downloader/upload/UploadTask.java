package com.jokysss.downloader.upload;

import android.os.Handler;
import android.os.Looper;

import com.jokysss.downloader.APIHelper;
import com.jokysss.downloader.ThreadService;
import com.jokysss.downloader.progress.ProgressListener;
import com.jokysss.downloader.progress.ProgressManager;
import com.jokysss.downloader.progress.body.ProgressInfo;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by Joky on 2017/6/15 0015.
 */

public class UploadTask {
    private String url;
    private Map<String,String> fieldMap = new HashMap<>();
    private List<File> fileList = new LinkedList<>();
    private Subscription sub;
    private String key = "";
    private static Scheduler scheduler = Schedulers.from(ThreadService.getUploadService());

    public UploadTask(String key,String url){
        this.url = url;
        this.key = key;
    }

    public Subscription start(){
        MultipartBody.Builder builder =  new MultipartBody.Builder().setType(MultipartBody.FORM);
        for(Map.Entry<String,String> e : fieldMap.entrySet()){
            builder.addFormDataPart(e.getKey(),e.getValue());
        }
        for(File f : fileList){
            builder.addFormDataPart("file", f.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), f));
        }
        RequestBody requestBody = builder.build();
        sub = APIHelper.getApi().upload(url,requestBody,key).subscribeOn(scheduler)
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                        Set<ProgressListener> listenerSet = ProgressManager.getInstance().removeRequestLisenter(key);
                        if(listenerSet == null) return;
                        final ProgressInfo info = new ProgressInfo(key);
                        info.setFinish(true);
                        long l = 0L;
                        for (File f : fileList) {
                            l += f.length();
                        }
                        info.setContentLength(l);
                        info.setCurrentbytes(l);
                        Handler h = new Handler(Looper.getMainLooper());
                        for (final ProgressListener listener : listenerSet) {
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onProgress(info);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                    }
                });
        return sub;
    }
    public void cancel(){
        if(sub != null && !sub.isUnsubscribed()){
            sub.unsubscribe();
        }
    }
    public UploadTask addListener(ProgressListener listener){
        if(listener != null){
            ProgressManager.getInstance().addRequestLisenter(key,listener);
        }
        return this;
    }
    public UploadTask setUrl(String url){
        this.url = url;
        return this;
    }
    public UploadTask addField(String key,String value){
        fieldMap.put(key,value);
        return this;
    }
    public UploadTask addFile(File file){
        fileList.add(file);
        return this;
    }
}
