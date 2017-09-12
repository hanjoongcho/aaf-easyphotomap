package me.blog.korn123.easyphotomap.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by CHO HANJOONG on 2017-09-12.
 */

public class PhotoMapItem extends RealmObject implements Comparable<PhotoMapItem> {

    @PrimaryKey
    public int sequence = 0;
    public double latitude = 0;
    public double longitude = 0;
    public String info = null;
    public String imagePath = null;
    public String date = null;
    public String originDate = null;
    public int sortFlag = 0;

    @Override
    public String toString() {
        if (sortFlag == 1) {
            info = date;
        }
        return info;
    }

    @Override
    public int compareTo(PhotoMapItem item) {
        int result = -1;
        switch (sortFlag) {
            case 0:
                info.compareTo(item.info);
                break;
            case 1:
                originDate.compareTo(item.originDate);
                break;
        }
        return result;
    }

}
