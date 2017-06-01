package me.blog.korn123.easyphotomap.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class FontUtils {

    private static Typeface mTypeface;

    public static void setTypefaceDefault(TextView view) {
        view.setTypeface(Typeface.DEFAULT);
    }

    public static void setTypeface(AssetManager assetManager, TextView view) {
        view.setTypeface(getTypeface(assetManager));
    }

    public static Typeface getTypeface(AssetManager assetManager) {
        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(assetManager, "fonts/NanumPen.ttf");
        }
        return  mTypeface;
    }

    public static void setToolbarTypeface(Toolbar toolbar, AssetManager assetManager) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView) {
                FontUtils.setTypeface(assetManager, (TextView)view);
//                ((TextView) view).setTypeface(Typeface.DEFAULT);
            }
        }
    }

    public static void setToolbarTypeface(Toolbar toolbar, Typeface typeface) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTypeface(typeface);
            }
        }
    }

    public static void setChildViewTypeface(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                setChildViewTypeface((ViewGroup)viewGroup.getChildAt(i));
            } else {
                if (viewGroup.getChildAt(i) instanceof TextView) {
                    TextView tv = (TextView) viewGroup.getChildAt(i);
                    tv.setTypeface(Typeface.DEFAULT);
                    Log.i("view info", String.format("%s", tv.getText()));
                }
            }
        }
    }
}
