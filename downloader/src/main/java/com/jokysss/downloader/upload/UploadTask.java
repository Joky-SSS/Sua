package com.jokysss.downloader.upload;


import com.jokysss.downloader.APIHelper;
import com.jokysss.downloader.progress.ProgressListener;
import com.jokysss.downloader.progress.ProgressManager;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
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

    public UploadTask(String url){
        this.url = url;
    }

    public void start(){
        MultipartBody.Builder builder =  new MultipartBody.Builder().setType(MultipartBody.FORM);
        for(Map.Entry<String,String> e : fieldMap.entrySet()){
            builder.addFormDataPart(e.getKey(),e.getValue());
        }
        for(File f : fileList){
            builder.addFormDataPart("file", f.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), f));
        }
        RequestBody requestBody = builder.build();
        sub = APIHelper.getApi().upload(url,requestBody,key).subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                    }
                });
    }
    public void cancel(){
        if(sub != null && !sub.isUnsubscribed()){
            sub.unsubscribe();
        }
    }
    public UploadTask addListener(String key, ProgressListener listener){
        ProgressManager.getInstance().addRequestLisenter(key,listener);
        this.key = key;
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
