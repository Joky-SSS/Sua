package com.jokysss.downloader.progress;


import com.jokysss.downloader.progress.body.ProgressInfo;

public interface ProgressListener {
    /**
     * 进度监听
     *
     * @param progressInfo
     */
    void onProgress(ProgressInfo progressInfo);

    void onError(String key, Exception e);
}
