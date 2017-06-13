package com.jokysss.downloader.progress.body;

import android.os.Handler;
import android.util.Log;


import com.jokysss.downloader.progress.ProgressInfo;
import com.jokysss.downloader.progress.ProgressListener;
import com.jokysss.downloader.progress.ProgressManager;

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
 * Created by jess on 02/06/2017 18:25
 * Contact with jess.yan.effort@gmail.com
 */

public class ProgressResponseBody extends ResponseBody {

    protected Handler mHandler;
    protected final ResponseBody mDelegate;
    protected final ProgressListener[] mListeners;
    protected final ProgressInfo mProgressInfo;
    private BufferedSource mBufferedSource;

    public ProgressResponseBody(Handler handler, ResponseBody responseBody, String key, Set<ProgressListener> listeners) {
        this.mDelegate = responseBody;
        this.mListeners = listeners.toArray(new ProgressListener[listeners.size()]);
        this.mHandler = handler;
        this.mProgressInfo = new ProgressInfo(System.currentTimeMillis(),key);
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

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = 0L;
                try {
                    bytesRead = super.read(sink, byteCount);
                } catch (IOException e) {
                    e.printStackTrace();
                    for (int i = 0; i < mListeners.length; i++) {
                        mListeners[i].onError(mProgressInfo.getKey(), e);
                    }
                    ProgressManager.getInstance().removeResponseListener(mProgressInfo.getKey());
                    throw e;
                }
                if (mProgressInfo.getContentLength() == 0) { //避免重复调用 contentLength()
                    mProgressInfo.setContentLength(contentLength());
                }
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                if (mListeners != null) {
                    long curTime = System.currentTimeMillis();
                    if (curTime - lastRefreshTime >= REFRESH_TIME || totalBytesRead == mProgressInfo.getContentLength()) {
                        mProgressInfo.setCurrentbytes(totalBytesRead);
                        for (int i = 0; i < mListeners.length; i++) {
                            final int finalI = i;
                            Log.e("Xup","mListeners["+finalI + "],totalBytesRead:"+totalBytesRead+",ContentLength"+mProgressInfo.getContentLength());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mListeners[finalI].onProgress(mProgressInfo);
                                }
                            });
                        }
                        lastRefreshTime = System.currentTimeMillis();
                        if(totalBytesRead == mProgressInfo.getContentLength()){
                            ProgressManager.getInstance().removeResponseListener(mProgressInfo.getKey());
                        }
                    }
                }
                return bytesRead;
            }
        };
    }
}
