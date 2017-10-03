package me.blog.korn123.easyphotomap.helper;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by CHO HANJOONG on 2017-09-12.
 */

public class PhotoMapMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        if (oldVersion == 1) {
            RealmObjectSchema diarySchema = schema.get("PhotoMapItem");
            oldVersion++;
        }
    }

}
