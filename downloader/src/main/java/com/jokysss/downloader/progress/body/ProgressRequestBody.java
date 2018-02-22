package com.jokysss.downloader.progress.body;

import android.os.Handler;
import android.os.SystemClock;

import com.jokysss.downloader.progress.ProgressListener;

import java.io.IOException;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

import static com.jokysss.downloader.progress.ProgressManager.REFRESH_TIME;


/**
 * 继承于{@link RequestBody},通过此类获取 Okhttp 上传的二进制数据
 */

public class ProgressRequestBody extends RequestBody {

    protected Handler mHandler;
    protected final RequestBody mDelegate;
    protected final Set<ProgressListener> mListeners;
    protected final ProgressInfo mProgressInfo;
    private BufferedSink mBufferedSink;


    public ProgressRequestBody(Handler handler, RequestBody delegate,String key, Set<ProgressListener> listeners) {
        this.mDelegate = delegate;
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
        try {
            return mDelegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (mBufferedSink == null) {
            mBufferedSink = Okio.buffer(new CountingSink(sink));
        }
        try {
            mDelegate.writeTo(mBufferedSink);
            mBufferedSink.flush();
        } catch (IOException e) {
            e.printStackTrace();
            for (ProgressListener listener : mListeners) {
                listener.onError(mProgressInfo.getKey(), e);
            }
            throw e;
        }
    }

    protected final class CountingSink extends ForwardingSink {
        private long totalBytesRead = 0L;
        private long lastRefreshTime = 0L;  //最后一次刷新的时间
        private long tempSize = 0L;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            try {
                super.write(source, byteCount);
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
            totalBytesRead += byteCount;
            tempSize += byteCount;
            if (mListeners != null) {
                long curTime = SystemClock.elapsedRealtime();
                if (curTime - lastRefreshTime >= REFRESH_TIME || totalBytesRead == mProgressInfo.getContentLength()) {
                    final long finalTempSize = tempSize;
                    final long finalTotalBytesRead = totalBytesRead;
                    final long finalIntervalTime = curTime - lastRefreshTime;
                    for (final ProgressListener listener : mListeners) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressInfo.setEachBytes(finalTempSize);
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
        }
    }
}
