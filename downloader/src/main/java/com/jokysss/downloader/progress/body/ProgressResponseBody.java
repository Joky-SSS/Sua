package com.jokysss.downloader.progress.body;

import android.os.Handler;
import android.os.SystemClock;


import com.jokysss.downloader.progress.ProgressListener;

import java.io.IOException;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

import static com.jokysss.downloader.progress.ProgressManager.REFRESH_TIME;


/**
 * 继承于{@link ResponseBody},通过此类获取 Okhttp 下载的二进制数据
 */

public class ProgressResponseBody extends ResponseBody {

    protected Handler mHandler;
    protected final ResponseBody mDelegate;
    protected final Set<ProgressListener> mListeners;
    protected final ProgressInfo mProgressInfo;
    private BufferedSource mBufferedSource;

    public ProgressResponseBody(Handler handler, ResponseBody responseBody, String key, Set<ProgressListener> listeners) {
        this.mDelegate = responseBody;
        this.mListeners = listeners;
        this.mHandler = handler;
        this.mProgressInfo = new ProgressInfo(key);
    }

    @Override
    public MediaType contentType() {
        return mDelegate.contentType();
    }

    @Override
    public long contentLength() {
        return mDelegate.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(source(mDelegate.source()));
        }
        return mBufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            private long totalBytesRead = 0L;
            private long lastRefreshTime = 0L;  //最后一次刷新的时间
            private long tempSize = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = 0L;
                try {
                    bytesRead = super.read(sink, byteCount);
                } catch (IOException e) {
                    e.printStackTrace();
                    for (ProgressListener listener : mListeners) {
                        listener.onError(mProgressInfo.getKey(), e);
                    }
                    throw e;
                }
                if (mProgressInfo.getContentLength() == 0) { //避免重复调用 contentLength()
                    mProgressInfo.setContentLength(contentLength());
                }
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                tempSize += bytesRead != -1 ? bytesRead : 0;
                if (mListeners != null) {
                    long curTime = SystemClock.elapsedRealtime();
                    if (curTime - lastRefreshTime >= REFRESH_TIME || bytesRead == -1 || totalBytesRead == mProgressInfo.getContentLength()) {
                        final long finalBytesRead = bytesRead;
                        final long finalTempSize = tempSize;
                        final long finalTotalBytesRead = totalBytesRead;
                        final long finalIntervalTime = curTime - lastRefreshTime;
                        for (final ProgressListener listener : mListeners) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressInfo.setEachBytes(finalBytesRead != -1 ? finalTempSize : -1);
                                    mProgressInfo.setCurrentbytes(finalTotalBytesRead);
                                    mProgressInfo.setIntervalTime(finalIntervalTime);
                                    listener.onProgress(mProgressInfo);
                                }
                            });
                        }
                        lastRefreshTime = curTime;
                        tempSize = 0;
                    }
                }
                return bytesRead;
            }
        };
    }
}
