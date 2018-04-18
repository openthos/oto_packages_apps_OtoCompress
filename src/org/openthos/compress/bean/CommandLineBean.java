package org.openthos.compress.bean;

import android.text.TextUtils;

import java.io.File;
import java.util.List;

/**
 * Created by wm on 17-9-15.
 */

public class CommandLineBean {
    private String mDestPath = "", mSrcPath = "", mFileName = "", mDestFileType = "",
            mPassword = "", mDestTotalName = "", mCommand = "", mOperation = "";
    private String mSpecificFile;
    private List<String> mSrcFilePaths;
    public static final String OPERATION_COMPRESS = "7z a ";
    public static final String OPERATION_DECOMPRESS = "7z t ";
    public static final String OPERATION_SHOW_LIST = "7z l ";

    public CommandLineBean() {
    }

    /**
     * for both compressObject and decompressObject
     *
     * @param mDestPath path of destination
     * @param mFileName name of srcFile or destFile
     */
    public CommandLineBean(String mDestPath, String mFileName) {
        this.mDestPath = mDestPath;
        this.mFileName = mFileName;
    }

    /**
     * for compressObject
     *
     * @param mDestPath     path of destination
     * @param mFileName     name of destFile
     * @param mSrcFilePaths paths of srcFiles
     */
    public CommandLineBean(String mDestPath, String mFileName, List mSrcFilePaths) {
        this.mDestPath = mDestPath;
        this.mFileName = mFileName;
        this.mSrcFilePaths = mSrcFilePaths;
    }

    /**
     * for compressObject
     *
     * @param mDestPath     path of destination
     * @param mFileName     name of destFile
     * @param mDestFileType type of destFile
     * @param mSrcFilePaths paths of srcFiles
     */
    public CommandLineBean(String mDestPath, String mFileName, String mDestFileType, List<String> mSrcFilePaths) {
        this.mDestPath = mDestPath;
        this.mFileName = mFileName;
        this.mDestFileType = mDestFileType;
        this.mSrcFilePaths = mSrcFilePaths;
    }

    public String getOperation() {
        return mOperation;
    }

    public void setOperation(String mOperation) {
        this.mOperation = mOperation;
    }

    public String getCommand() {
        return mCommand;
    }

    public String getDestPath() {

        return mDestPath;
    }

    public void setDestPath(String mDestPath) {
        this.mDestPath = mDestPath;
    }

    public String getSrcPath() {
        return mSrcPath;
    }

    public void setSrcPath(String mSrcPath) {
        this.mSrcPath = mSrcPath;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public String getDestFileType() {
        return mDestFileType;
    }

    public void setDestFileType(String mDestFileType) {
        this.mDestFileType = mDestFileType;
    }

    public String getDestTotalName() {
        return mDestTotalName;
    }

    public void setDestTotalName(String mDestTotalName) {
        this.mDestTotalName = mDestTotalName;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public List<String> getSrcFilePaths() {
        return mSrcFilePaths;
    }

    public void setSrcFilePaths(List<String> mSrcFilePaths) {
        this.mSrcFilePaths = mSrcFilePaths;
    }

    public String getSpecificFile() {
        return mSpecificFile;
    }

    public void setSpecificFile(String mSpecificFile) {
        this.mSpecificFile = mSpecificFile;
    }

    @Override
    public String toString() {
        mCommand = "";
        StringBuilder simpleCmd = new StringBuilder();
        switch (mOperation) {
            case OPERATION_COMPRESS:
                int pathsSize = mSrcFilePaths.size();
                int num = (pathsSize % 20 == 0) ? (pathsSize / 20) : (pathsSize / 20 + 1);
                for (int i = 0; i < num; i++) {
                    simpleCmd.append(mOperation);
                    simpleCmd.append("'" + mDestPath + File.separator + mFileName + mDestFileType);
                    int count = (i == (num - 1)) ? (pathsSize - 20 * i) : 20;
                    for (int j = 0; j < count; j++) {
                        simpleCmd.append("' '" + mSrcFilePaths.get(20 * i + j));
                    }
                    simpleCmd.append("' ");
                    if (!TextUtils.isEmpty(mPassword)) {
                        simpleCmd.append("'-p" + mPassword + "' ");
                    }
                    simpleCmd.append(",");
                }
                mCommand = simpleCmd.toString();
                mCommand = mCommand.substring(0, mCommand.length() - 1);
                break;
            case OPERATION_DECOMPRESS:
                simpleCmd.append(mOperation + "'" + mFileName + "' ");
                if (!TextUtils.isEmpty(mPassword)) {
                    simpleCmd.append("'-p" + mPassword + "' ");
                }
                simpleCmd.append("'-o" + mDestPath + "' ");
                if (!TextUtils.isEmpty(mSpecificFile)) {
                    simpleCmd.append("'" + mSpecificFile + "'");
                }
                mCommand = simpleCmd.toString();
                break;
            case OPERATION_SHOW_LIST:
                simpleCmd.append(mOperation);
                if (!TextUtils.isEmpty(mPassword)) {
                    simpleCmd.append("-p" + mPassword + " " + mSrcPath + mFileName);
                } else {
                    simpleCmd.append(mSrcPath + mFileName);
                }
                mCommand = simpleCmd.toString();
                break;
            default:
                break;
        }
        return mCommand;
    }
}
