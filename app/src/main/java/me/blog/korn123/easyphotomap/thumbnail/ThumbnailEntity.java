package me.blog.korn123.easyphotomap.thumbnail;

import android.net.Uri;

/**
 * Created by CHO HANJOONG on 2016-08-02.
 */
public class ThumbnailEntity {
    String imageId;
    String imagePath;
    String thumbnailPath;
    Uri thumbnailUri;
    Uri imageUri;

    public ThumbnailEntity(){}

    public ThumbnailEntity(String imageId, String imagePath, String thumbnailPath) {
        this.imageId = imageId;
        this.imagePath = imagePath;
        this.thumbnailPath = thumbnailPath;
    }
}
