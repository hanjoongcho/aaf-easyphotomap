package me.blog.korn123.easyphotomap.helper;

import android.support.multidex.MultiDexApplication;

import io.realm.Realm;

/**
 * Created by CHO HANJOONG on 2017-09-12.
 */

public class EasyPhotoMapApplication extends MultiDexApplication{
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
