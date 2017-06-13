package com.jokysss.downloader.progress.body;

import android.os.Handler;

import com.jokysss.downloader.progress.ProgressInfo;
import com.jokysss.downloader.progress.ProgressListener;
import com.jokysss.downloader.progress.ProgressManager;

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
 * Created by jess on 02/06/2017 18:05
 * Contact with jess.yan.effort@gmail.com
 */

public class ProgressRequestBody extends RequestBody {

    protected Handler mHandler;
    protected final RequestBody mDelegate;
    protected final ProgressListener[] mListeners;
    protected final ProgressInfo mProgressInfo;
    private BufferedSink mBufferedSink;

    public ProgressRequestBody(Handler handler, RequestBody delegate, String key, Set<ProgressListener> listeners) {
        this.mDelegate = delegate;
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
            for (int i = 0; i < mListeners.length; i++) {
                mListeners[i].onError(mProgressInfo.getKey(), e);
            }
            ProgressManager.getInstance().removeRequestLisenter(mProgressInfo.getKey());
            throw e;
        }
    }

    protected final class CountingSink extends ForwardingSink {
        private long totalBytesRead = 0L;
        private long lastRefreshTime = 0L;  //最后一次刷新的时间

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            try {
                super.write(source, byteCount);
            } catch (IOException e) {
                e.printStackTrace();
                for (int i = 0; i < mListeners.length; i++) {
                    mListeners[i].onError(mProgressInfo.getKey(), e);
                }
                ProgressManager.getInstance().removeRequestLisenter(mProgressInfo.getKey());
                throw e;
            }
            if (mProgressInfo.getContentLength() == 0) { //避免重复调用 contentLength()
                mProgressInfo.setContentLength(contentLength());
            }
            totalBytesRead += byteCount;
            if (mListeners != null) {
                long curTime = System.currentTimeMillis();
                if (curTime - lastRefreshTime >= REFRESH_TIME || totalBytesRead == mProgressInfo.getContentLength()) {
                    mProgressInfo.setCurrentbytes(totalBytesRead);
                    for (int i = 0; i < mListeners.length; i++) {
                        final int finalI = i;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mListeners[finalI].onProgress(mProgressInfo);
                            }
                        });
                    }
                    lastRefreshTime = System.currentTimeMillis();
                    if(totalBytesRead == mProgressInfo.getContentLength()){
                        ProgressManager.getInstance().removeRequestLisenter(mProgressInfo.getKey());
                    }
                }
            }
        }
    }
}
