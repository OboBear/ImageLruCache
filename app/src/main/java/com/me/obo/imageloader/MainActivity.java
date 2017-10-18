package com.me.obo.imageloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = findViewById(R.id.image_view);

        Opic.with(this)
                .load("http://cn.bing.com/az/hprichbg/rb/LittleAuks_ZH-CN9796184036_1920x1080.jpg")
                .into(imageView);
    }
}
