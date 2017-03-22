package me.blog.korn123.easyphotomap.file;

import org.apache.commons.io.FilenameUtils;

/**
 * Created by CHO HANJOONG on 2016-07-30.
 */
public class FileEntity implements Comparable<FileEntity> {

    public String imagePath;
    public String fileName;
    public boolean isDirectory;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
        this.fileName = FilenameUtils.getName(imagePath);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return fileName;
    }

    @Override
    public int compareTo(FileEntity entity) {
        return fileName.compareTo(entity.fileName);
    }
}
