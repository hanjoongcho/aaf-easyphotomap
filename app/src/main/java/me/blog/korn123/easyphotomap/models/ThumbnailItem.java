package me.blog.korn123.easyphotomap.models;

import android.net.Uri;

/**
 * Created by CHO HANJOONG on 2016-08-02.
 */
public class ThumbnailItem {
    public String imageId;
    public String imagePath;
    public String thumbnailPath;
    public Uri thumbnailUri;
    public Uri imageUri;

    public ThumbnailItem(){}

    public ThumbnailItem(String imageId, String imagePath, String thumbnailPath) {
        this.imageId = imageId;
        this.imagePath = imagePath;
        this.thumbnailPath = thumbnailPath;
    }
}
