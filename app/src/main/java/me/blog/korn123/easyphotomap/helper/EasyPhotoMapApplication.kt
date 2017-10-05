package me.blog.korn123.easyphotomap.helper

import android.support.multidex.MultiDexApplication

import io.realm.Realm

/**
 * Created by CHO HANJOONG on 2017-09-12.
 */

class EasyPhotoMapApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}
