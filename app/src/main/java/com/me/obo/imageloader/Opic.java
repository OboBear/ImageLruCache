package com.me.obo.imageloader;

import android.content.Context;
import android.util.LruCache;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by obo on 2017/10/12.
 * Email:obo1993@gmail.com
 */

public class Opic {

    public static ImageRequest with(Context context) {
        return new ImageRequest(context);
    }
}
