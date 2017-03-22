package me.blog.korn123.easyphotomap.search;

/**
 * Created by CHO HANJOONG on 2016-07-16.
 */
public class PhotoEntity implements Comparable<PhotoEntity> {
    public double latitude;
    public double longitude;
    public String info;
    public String imagePath;
    public String date;
    public String originDate;
    public int sortFlag = 0;

    @Override
    public String toString() {
        String info = null;
        if (sortFlag == 0) {
            info = this.info;
        } else if (sortFlag == 1) {
            info = date;
        }
        return info;
    }

    @Override
    public int compareTo(PhotoEntity imageEntity) {
        int result = 0;
        if (sortFlag == 0) {
            result = info.compareTo(imageEntity.info);
        } else if (sortFlag == 1) {
            result = originDate.compareTo(imageEntity.originDate);
        }
        return result;
    }
}
