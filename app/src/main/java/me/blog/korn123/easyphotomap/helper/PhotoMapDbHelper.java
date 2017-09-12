package me.blog.korn123.easyphotomap.helper;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import me.blog.korn123.easyphotomap.models.PhotoMapItem;

/**
 * Created by CHO HANJOONG on 2017-09-12.
 */

public class PhotoMapDbHelper {

    private volatile static RealmConfiguration diaryConfig;

    private static Realm getRealmInstance() {
        if (diaryConfig == null) {
            diaryConfig = new RealmConfiguration.Builder()
                    .name("easyphotomap.realm")
                    .schemaVersion(1)
                    .migration(new PhotoMapMigration())
                    .modules(Realm.getDefaultModule())
                    .build();

        }
        return Realm.getInstance(diaryConfig);
    }

    public static void insertPhotoMapItem(PhotoMapItem photoMapItem) {
        Realm realm = getRealmInstance();
        realm.beginTransaction();
        int sequence = 1;
        if (realm.where(PhotoMapItem.class).count() > 0) {
            Number number = realm.where(PhotoMapItem.class).max("sequence");
            sequence = number.intValue() + 1;
        }
        photoMapItem.sequence = sequence;
        realm.insert(photoMapItem);
        realm.commitTransaction();
    }

    public static ArrayList<PhotoMapItem> selectPhotoMapItemAll() {
        RealmResults realmResults = getRealmInstance().where(PhotoMapItem.class).findAllSorted("sequence", Sort.DESCENDING);
        ArrayList<PhotoMapItem> list = new ArrayList<>();
        list.addAll(realmResults.subList(0, realmResults.size()));
        return list;
    }

    public static ArrayList<PhotoMapItem> selectPhotoMapItemBy(String targetColumn, String value) {
        RealmResults realmResults = getRealmInstance().where(PhotoMapItem.class).equalTo(targetColumn, value).findAllSorted("sequence", Sort.DESCENDING);
        ArrayList<PhotoMapItem> list = new ArrayList<>();
        list.addAll(realmResults.subList(0, realmResults.size()));
        return list;
    }

}
