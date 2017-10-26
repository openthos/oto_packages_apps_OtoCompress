package com.openthos.compress;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ListView;

import com.openthos.compress.bean.CompressedFileBean;
import com.openthos.compress.common.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by wm on 17-9-29.
 */

class ArchiveBrowserPresenter implements ArchiveBrowserContract.IPresenter {
    private Context mContext;
    private ArchiveBrowserContract.IView mIView;
    private ArchiveBrowserListAdapter mListAdapter;
    private Map<String, Set<String>> mDirsInfoMap;
    private Map<String, List<CompressedFileBean>> mFilesInfoMap;
    private String mTempFolderPath;

    ArchiveBrowserPresenter(Context context, ArchiveBrowserContract.IView iView) {
        mContext = context;
        mIView = iView;
        mFilesInfoMap = new HashMap<>();
        mDirsInfoMap = new HashMap<>();
        initData();
    }

    private void initData() {
        mDirsInfoMap.put("/", new TreeSet<String>());
    }

    @Override
    public void handleResult(String result) {
        int testIndex = result.indexOf("-----");
        if (testIndex == -1) {
            if (result.contains(CompressUtils.TEXT_INPUT_PASSWORD)) {
                mIView.toInputPassword(false);
            } else if (result.contains(CompressUtils.TEXT_WRONG_PASSWORD)) {
                mIView.toInputPassword(true);
            }
            return;
        }
        mIView.cancelInputDialog();
        handleFilesInfo(result);
        mIView.cancelProgressDialog();
        setAdapter(mIView.getListView());
        mIView.clearFocus();
    }

    /**
     * Handle the str from execution of command line
     * (The sample parameter str is at the bottom)
     *
     * @param filesInfo The str from execution of command line
     */
    @Override
    public void handleFilesInfo(String filesInfo) {
        // get the strings useful only
        int subIndex = filesInfo.indexOf("----\n") + 5;
        String filesInfoStr = filesInfo.substring(subIndex);
        subIndex = filesInfoStr.indexOf("-----");
        filesInfoStr = filesInfoStr.substring(0, subIndex - 1);

        // foreach to resolve and save data
        String[] filesInfoArray = filesInfoStr.split("\n");
        boolean needInitDirs = true;
        for (int i = 1; i < filesInfoArray.length; i += 2) {
            // second line : isDirectory,totalName,directory,fileName,[suffix]
            String tempStr = filesInfoArray[i];
            String fileName;
            boolean isDirectory, hasParentDir;
            int lastIndex = tempStr.lastIndexOf("/");
            hasParentDir = (lastIndex != -1);
            if (hasParentDir) {
                fileName = tempStr.substring(lastIndex + 1);
            } else {
                fileName = tempStr;
            }
            isDirectory = !(filesInfoArray[i - 1].contains("A") ||
                    (fileName.contains(".") && !tempStr.startsWith(".")));
            if (isDirectory) {
                // this two line is directoryInfo ,save to mDirsInfoMap
                if (needInitDirs) {
                    checkForDirs(tempStr);
                } else {
                    String tempKey, tempName;
                    if (hasParentDir) {
                        tempKey = "/" + tempStr.substring(0, lastIndex);
                        tempName = tempStr.substring(lastIndex + 1);
                    } else {
                        tempKey = "/";
                        tempName = fileName;
                    }
                    if (mDirsInfoMap.containsKey(tempKey)) {
                        mDirsInfoMap.get(tempKey).add(tempName);
                    } else {
                        Set<String> newSet = new TreeSet<>();
                        newSet.add(tempName);
                        mDirsInfoMap.put(tempKey, newSet);
                    }
                }
            } else {
                // this two line is fileInfo,save bean to mFilesInfoMap
                CompressedFileBean bean = new CompressedFileBean();
                bean.setTotalName(tempStr);
                int suffixIndex = fileName.indexOf(".");
                boolean hasSuffix = (suffixIndex > 0);
                if (hasSuffix) {
                    bean.setFileName(fileName.substring(0, suffixIndex));
                    bean.setSuffix(fileName.substring(suffixIndex + 1));
                } else {
                    bean.setSuffix(mContext.getString(R.string.empty_str));
                    bean.setFileName(fileName);
                }
                if (hasParentDir) {
                    String tempDirs = "/" + tempStr.substring(0, lastIndex);
                    bean.setDirectory(tempDirs);
                } else {
                    bean.setDirectory("/");
                }
                if (needInitDirs && hasParentDir) {
                    String tempDirs = tempStr.substring(0, lastIndex);
                    checkForDirs(tempDirs);
                }
                // first line : date time size compressedSize
                tempStr = filesInfoArray[i - 1];
                if (tempStr.contains("D")) {
                    needInitDirs = false;
                }
                int tempIndex = tempStr.indexOf(".");
                if (tempIndex > 0 && !TextUtils.isEmpty(tempStr.charAt(0) + "")) {
                    bean.setModifiedTime(tempStr.substring(0, tempIndex - 1).trim());
                } else {
                    bean.setModifiedTime("");
                }
                tempStr = tempStr.substring(tempIndex + 5).trim() + " ";
                String originalSize = tempStr.substring(0, tempStr.indexOf(" "));
                if (!TextUtils.isEmpty(originalSize)) {
                    bean.setOriginalSize(Integer.parseInt(originalSize));
                }
                String compressedSize = tempStr.substring(tempStr.indexOf(" ")).trim();
                if (!TextUtils.isEmpty(compressedSize)) {
                    bean.setCompressedSize(Integer.parseInt(compressedSize));
                }
                // put the fileBean to map
                String tempDir = bean.getDirectory();
                if (mFilesInfoMap.containsKey(tempDir)) {
                    mFilesInfoMap.get(tempDir).add(bean);
                } else {
                    List<CompressedFileBean> list = new ArrayList<>();
                    list.add(bean);
                    mFilesInfoMap.put(tempDir, list);
                }
            }
        }
    }

    /**
     * Check if mDirsInfoMap already contains every part of the file's package directory as a key
     *
     * @param fileDirs .
     */
    private void checkForDirs(String fileDirs) {
        String dirsLeft = fileDirs;
        String[] dirsArray = fileDirs.split("/");
        for (int i = dirsArray.length - 1; i >= 0; i--) {
            if (i == 0) {
                mDirsInfoMap.get("/").add(dirsArray[i]);
                return;
            }
            dirsLeft = dirsLeft.substring(0, dirsLeft.lastIndexOf("/"));
            String dirsKey = "/" + dirsLeft;
            if (mDirsInfoMap.containsKey(dirsKey)) {
                mDirsInfoMap.get(dirsKey).add(dirsArray[i]);
            } else {
                TreeSet<String> newSet = new TreeSet<>();
                newSet.add(dirsArray[i]);
                mDirsInfoMap.put(dirsKey, newSet);
            }
        }
    }

    @Override
    public void setAdapter(ListView listView) {
        if (mListAdapter == null) {
            mListAdapter = new ArchiveBrowserListAdapter(this, mContext, mDirsInfoMap, mFilesInfoMap);
            listView.setAdapter(mListAdapter);
        } else {
            mListAdapter.refreshDataList();
        }
    }

    /**
     * To add file to any specific directory in the archive file,the same parentFolders is necessary
     *
     * @param selectedFilePath the path of file which is selected to add
     * @param tempFolderPath   the path where to create the tempFile and its parentFolders
     * @param targetDirs       the relative path in the archive that to add file to,
     *                         also is the parentFolders
     * @return the path of tempFolder contains the tempFile
     */
    @Override
    public String createTempFile(String selectedFilePath, String tempFolderPath, String targetDirs) {
        targetDirs = targetDirs.substring(1);
        if (!tempFolderPath.endsWith("/")) {
            tempFolderPath += "/";
        }
        File parentFolder = new File(tempFolderPath + targetDirs);
        parentFolder.mkdirs();
        File tempFile = new File(parentFolder, new File(selectedFilePath).getName());
        try {
            CommonUtils.copyFile(selectedFilePath, tempFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (parentFolder.listFiles().length > 0) {
            File tempFolder;
            if (targetDirs.contains("/")) {
                targetDirs = targetDirs.substring(0, targetDirs.indexOf("/"));
                tempFolder = new File(tempFolderPath + targetDirs);
            } else {
                tempFolder = new File(tempFolderPath + targetDirs);
            }
            mTempFolderPath = tempFolder.getParentFile().getAbsolutePath();
            return tempFolder.getAbsolutePath();
        }
        return "";
    }

    @Override
    public void removeTempFile() {
        if (!TextUtils.isEmpty(mTempFolderPath)) {
            CommonUtils.removeFile(mTempFolderPath);
            mTempFolderPath = "";
        }
    }

    @Override
    public void clearData() {
        mDirsInfoMap.clear();
        mFilesInfoMap.clear();
        initData();
    }

    @Override
    public void updateUIData(String path, int dirsCount, int filesCount) {
        mIView.updateUIData(path, dirsCount, filesCount);
    }

    @Override
    public String getSelectedFileName() {
        return mListAdapter.getSelectedFileName();
    }

    @Override
    public void searchByDir(String dirPath) {
        mListAdapter.searchByDir(dirPath);
    }

    @Override
    public void goHome() {
        mListAdapter.goHome();
    }

    @Override
    public boolean onEscClick() {
        return mListAdapter.toParentDir();
    }

    @Override
    public void onBackClick() {
        mListAdapter.toLastDir();
    }

    @Override
    public void onForwardClick() {
        mListAdapter.toNextDir();
    }

    @Override
    public void clearFocus() {
        mIView.clearFocus();
    }
}


/* Sample str Type 1:

 Date      Time    Attr         Size   Compressed  Name
 ------------------- ----- ------------ ------------  ------------------------
 2016-09-18 16:46:10 .....        28216         5257  AndroidManifest.xml
 2016-09-18 16:46:10 .....         1780          892  assets/help/en/ack_gladman_aes.html
 2016-09-18 16:46:10 .....         4663         2035  assets/help/en/ack_infozip.html
 2016-09-18 16:46:10 .....         2022         1006  assets/help/en/ack_intel_crc.html
 2016-09-18 16:45:22 .....          166          166  res/drawable-xhdpi-v4/ic_filecheck_pressed.png
 2016-09-18 16:45:22 .....          184          184  res/drawable-xhdpi-v4/ic_folder.png
 2016-09-18 16:46:10 .....       768324       441064  lib/armeabi-v7a/librarlab_rar.so
 2016-09-18 16:46:10 .....      1701144       789808  lib/mips/librarlab_rar.so
 2016-09-18 16:46:10 .....      1469300       656691  lib/x86/librarlab_rar.so
 2016-09-18 16:46:12 .....        73995        22552  META-INF/MANIFEST.MF
 2016-09-18 16:46:12 .....        74024        22735  META-INF/CERT.SF
 2016-09-18 16:46:12 .....          772          611  META-INF/CERT.RSA
 ------------------- ----- ------------ ------------  ------------------------
 9200621      4655802
 776 files, 0 folders

 */

/* Sample str Type 2:

 Date      Time    Attr         Size   Compressed  Name
------------------- ----- ------------ ------------  ------------------------
2017-09-29 09:32:43 D....            0            0
dir1
2017-09-29 09:32:25 D....            0            0
dir1/dir22
2017-09-29 09:32:48 D....            0            0
dir1/dir22/dir33
2017-09-29 09:32:43 ....A      3459336     19972541
dir1/asd.2.zip
2017-09-29 09:32:48 ....A      3459336
dir1/dir22/dir33/asd.2.zip
2017-09-29 09:32:25 ....A      3459336
dir1/wallpaper (8).jpg
2017-09-29 09:32:25 ....A      1211552
dir1/dir22/dir33/wallpaper (9).2.jpg
2017-09-29 09:32:25 ....A      1211552
dir1/dir22/dir33/wallpaper (9).jpg
2017-09-29 09:32:25 ....A      1211552
dir1/dir22/wallpaper (9).jpg
2017-09-29 09:32:25 ....A      1211552
dir1/wallpaper (9).jpg
------------------- ----- ------------ ------------  ------------------------
2017-09-29 09:32:48

41736235    19972541
30 files, 3 folders

 */