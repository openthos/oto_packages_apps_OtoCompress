package org.openthos.compress;

import android.net.Uri;
import android.widget.ListView;

import org.openthos.compress.bean.CompressedFileBean;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wm on 17-9-29.
 */

class ArchiveBrowserContract {

    interface IView {
        void updateUIData(String path, int dirsCount, int filesCount);

        ListView getListView();

        void initPwDialog();

        void cancelInputDialog();

        void toInputPassword(boolean hasInputPassword);

        void clearFocus();

        void doExtract(Uri srcUri, String specificFile);

        void cancelProgressDialog();
    }

    interface IPresenter {
        void handleFilesInfo(String filesInfo);

        void setAdapter(ListView mListView);

        boolean onEscClick();

        void onBackClick();

        void onForwardClick();

        void updateUIData(String path, int dirsCount, int filesCount);

        void searchByDir(String dirPath);

        void goHome();

        void handleResult(String result);

        void clearFocus();

        String getSelectedFileName();

        String createTempFile(String filePath, String tempFolderPath, String targetDirs);

        void removeTempFile();

        void clearData();
    }
}
