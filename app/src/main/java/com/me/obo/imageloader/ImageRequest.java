package com.me.obo.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.bumptech.glide.disklrucache.DiskLruCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by obo on 2017/10/12.
 * Email:obo1993@gmail.com
 */

public class ImageRequest {
    private WeakReference<Context> contextWeakReference;
    private String url;
    private static LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(4 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    };

    private static DiskLruCache diskLruCache;

    public ImageRequest(Context context) {
        contextWeakReference = new WeakReference<Context>(context);
        File sdkFile = Environment.getExternalStorageDirectory();
        File file = new File(sdkFile + "/opic");
        try {
            diskLruCache =  DiskLruCache.open(file, 1, 1, 100 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ImageRequest load(String url) {
        this.url = url;
        return this;
    }

    public void into(ImageView imageView) {
        if (lruCache.get(url) != null) {
            imageView.setImageBitmap(lruCache.get(url));
        } else {
            try {
                DiskLruCache.Value snapShot = diskLruCache.get(url);

                if (snapShot != null) {
                    File cacheFile = snapShot.getFile(0);
                    InputStream inputStream = new FileInputStream(cacheFile);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    lruCache.put(url, bitmap);
                    imageView.setImageBitmap(bitmap);
                } else {
                    Request request = new Request.Builder().url(url).build();
                    OkHttpClient okHttpClient = new OkHttpClient();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            InputStream inputStream = response.body().byteStream();
                            int len ;
                            byte[] bytes = new byte[4096];
                            DiskLruCache.Editor editor = diskLruCache.edit(url);
                            File fff = editor.getFile(0);
                            if (!fff.exists()) {
                                fff.createNewFile();
                            }
                            OutputStream outputStream = new FileOutputStream(fff);
                            Log.i("","");
                            while ((len = inputStream.read(bytes, 0, bytes.length)) != -1) {
                                outputStream.write(bytes, 0, len);
                            }
                            outputStream.flush();
                            outputStream.close();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
