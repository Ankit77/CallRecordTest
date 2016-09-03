package com.xiaotuan.autocallrecord;

import java.io.File;

public class FileInfo {
    private int count;
    private String fileName;
    private boolean isDirectory;
    private String path;

    public FileInfo(File file, int count) {
        this.count = count;
        if (file != null) {
            this.path = file.getAbsolutePath();
            this.fileName = file.getName();
            this.isDirectory = file.isDirectory();
            return;
        }
        this.path = null;
        this.fileName = null;
        this.isDirectory = false;
    }

    public FileInfo(String path, int count) {
        this.path = path;
        this.count = count;
        File file = new File(path);
        if (file.exists()) {
            this.fileName = file.getName();
            this.isDirectory = file.isDirectory();
            return;
        }
        this.path = null;
        this.fileName = null;
        this.isDirectory = false;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isDirectory() {
        return this.isDirectory;
    }

    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
