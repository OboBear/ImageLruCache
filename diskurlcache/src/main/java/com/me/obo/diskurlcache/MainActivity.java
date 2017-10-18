package com.me.obo.diskurlcache;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;

    DiskLruCache diskLruCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.btn_load);
        imageView = (ImageView) findViewById(R.id.iv_view);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    long size = 1024L * 1024 * 1024;
                    diskLruCache = DiskLruCache.open(getDiskCacheDir(MainActivity.this, "bitmap"), 2, 1, size);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                loadImage();
            }
        });
    }

    private void loadImage() {

        try {

            DiskLruCache.Snapshot snapshot = diskLruCache.get("1");

            if (snapshot != null) {
                InputStream inputStream = snapshot.getInputStream(0);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            } else {
                AssetManager assetManager = getAssets();
                InputStream inputStream = assetManager.open("a.jpg");

                DiskLruCache.Editor editor = diskLruCache.edit("1");
                OutputStream outputStream = editor.newOutputStream(0);
                byte [] inputData = new byte[4096];
                while (inputStream.read(inputData, 0, 4096) != -1) {
                    outputStream.write(inputData);
                }
                outputStream.flush();
                editor.commit();
                loadImage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }
}
