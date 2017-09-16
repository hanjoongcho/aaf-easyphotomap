package me.blog.korn123.easyphotomap.models;

import org.apache.commons.io.FilenameUtils;

/**
 * Created by CHO HANJOONG on 2016-07-30.
 */
public class FileItem implements Comparable<FileItem> {

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
    public int compareTo(FileItem entity) {
        return fileName.compareTo(entity.fileName);
    }
}
