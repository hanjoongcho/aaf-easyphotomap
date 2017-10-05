package me.blog.korn123.easyphotomap.helper

import io.realm.DynamicRealm
import io.realm.RealmMigration

/**
 * Created by CHO HANJOONG on 2017-09-12.
 */

class PhotoMapMigration : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var currentVersion = oldVersion as Int
        val schema = realm.schema
        if (currentVersion == 1) {
            val diarySchema = schema.get("PhotoMapItem")
            currentVersion++
        }
    }

}
