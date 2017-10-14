package com.me.obo.imageloader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
            diskLruCache =  DiskLruCache.open(file, getAppVersion(context), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public ImageRequest load(String url) {
        this.url = url;
        return this;
    }

    public void into(ImageView imageView) {
        final String hashedKey = hashKeyForDisk(url);
        if (lruCache.get(hashedKey) != null) {
            imageView.setImageBitmap(lruCache.get(hashedKey));
        } else {
            try {
                DiskLruCache.Snapshot snapShot = diskLruCache.get(hashedKey);

                if (snapShot != null) {
                    InputStream inputStream = snapShot.getInputStream(0);
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
                            DiskLruCache.Editor editor = diskLruCache.edit(hashedKey);
                            OutputStream outputStream = editor.newOutputStream(0);
//                            Log.i("","");
//                            while ((len = inputStream.read(bytes, 0, bytes.length)) != -1) {
//                                outputStream.write(bytes, 0, len);
//                            }


                            BufferedInputStream in = new BufferedInputStream(inputStream, 8 * 1024);
                            BufferedOutputStream out = new BufferedOutputStream(outputStream, 8 * 1024);
                            int b;
                            while ( (b = in.read()) != -1) {
                                out.write(b);
                            }


                            out.close();
                            in.close();


                            outputStream.flush();
                            outputStream.close();
                            editor.commit();


                            Bitmap cachedBitmap = readCache(hashedKey);
                            if (cachedBitmap != null) {
                                Log.i("","");
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap readCache(String key) {
        if (lruCache.get(key) != null) {
            return lruCache.get(key);
        } else {
            DiskLruCache.Snapshot snapShot = null;
            try {
                snapShot = diskLruCache.get(key);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (snapShot != null) {
                InputStream inputStream = snapShot.getInputStream(0);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                lruCache.put(url, bitmap);
                return bitmap;
            }
        }
        return null;
    }



    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
