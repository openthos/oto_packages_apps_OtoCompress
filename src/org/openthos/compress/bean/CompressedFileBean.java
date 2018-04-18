package org.openthos.compress.bean;

/**
 * Created by wm on 17-9-29.
 */

public class CompressedFileBean {
    private String mFileName, mSuffix, mDirectory, mTotalName, mModifiedTime;
    private int mOriginalSize, mCompressedSize;

    public CompressedFileBean() {
    }

    public CompressedFileBean(String mFileName) {
        this.mFileName = mFileName;
    }

    public CompressedFileBean(String mFileName, String mDirectory) {
        this.mFileName = mFileName;
        this.mDirectory = mDirectory;
    }

    public int getCompressedSize() {
        return mCompressedSize;
    }

    public void setCompressedSize(int mCompressedSize) {
        this.mCompressedSize = mCompressedSize;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public String getSuffix() {
        return mSuffix;
    }

    public void setSuffix(String mSuffix) {
        this.mSuffix = mSuffix;
    }

    public String getDirectory() {
        return mDirectory;
    }

    public void setDirectory(String mDirectory) {
        this.mDirectory = mDirectory;
    }

    public String getTotalName() {
        return mTotalName;
    }

    public void setTotalName(String mTotalName) {
        this.mTotalName = mTotalName;
    }

    public String getModifiedTime() {
        return mModifiedTime;
    }

    public void setModifiedTime(String mModifiedTime) {
        this.mModifiedTime = mModifiedTime;
    }

    public int getOriginalSize() {
        return mOriginalSize;
    }

    public void setOriginalSize(int mOriginalSize) {
        this.mOriginalSize = mOriginalSize;
    }
}
