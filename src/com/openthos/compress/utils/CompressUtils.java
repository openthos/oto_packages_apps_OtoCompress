package com.openthos.compress.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;
import android.app.Activity;

import com.openthos.compress.CompressActivity;
import com.openthos.compress.DecompressActivity;
import com.openthos.compress.R;
import com.hu.p7zip.ZipUtils;

import java.io.File;
import java.util.regex.Pattern;
import java.util.ArrayList;

public class CompressUtils {

    public static final String EXTRA_DELETE_FILE_HEADER = "OtoDeleteFile:///";
    public static final String COMPRESS_FILE_PATH = "paths";
    public static final String SUFFIX_TAR = ".tar";
    public static final String SUFFIX_GZ = ".tar.gz";
    public static final String SUFFIX_BZ2 = ".tar.bz2";
    private static final int RET_SUCCESS = 0;
    private static final int RET_WARNING = 1;
    private static final int RET_FAULT = 2;
    private static final int RET_COMMAND = 7;
    private static final int RET_MEMORY = 8;
    private static final int RET_USER_STOP = 255;
    public static final int REQUEST_CODE_DST = 1;

    private static final int FILE_NAME_LEGAL = 11;
    private static final int FILE_NAME_NULL = 12;
    private static final int FILE_NAME_ILLEGAL = 13;
    private static final int FILE_NAME_WARNING = 14;

    private static final int CONTENT_START_INDEX = 41;
    private static final int CONTENT_END_INDEX = 15;

    private Context mContext;
    private Thread mThread;
    private Handler mHandler;
    private String mCommand;
    private String mSourceName;
    private String mDestinationPath;
    private ProgressInfoDialog mDialog;

    public void initUtils(Context context, String command) {
        mContext = context;
        mCommand = command;
        mDialog = ProgressInfoDialog.getInstance(context);
        mDialog.setCancelable(false);

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                int retMsgId = R.string.msg_ret_success;
                switch (msg.what) {
                    case RET_SUCCESS:
                        retMsgId = R.string.msg_ret_success;
                        break;
                    case RET_WARNING:
                        retMsgId = R.string.msg_ret_warning;
                        break;
                    case RET_FAULT:
                        retMsgId = R.string.msg_ret_fault;
                        break;
                    case RET_COMMAND:
                        retMsgId = R.string.msg_ret_command;
                        break;
                    case RET_MEMORY:
                        retMsgId = R.string.msg_ret_memmory;
                        break;
                    case RET_USER_STOP:
                        retMsgId = R.string.msg_ret_user_stop;
                        break;
                    default:
                        break;
                }
                toast(mContext.getString(retMsgId));
                if (msg.what == RET_SUCCESS || msg.what == RET_WARNING) {
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).finish();
                    }
                } else if (msg.what == RET_FAULT && mContext instanceof DecompressActivity) {
                    ((DecompressActivity) mContext).inputPassword();
                }
                return false;
            }
        });

        mThread = new Thread() {
            @Override
            public void run() {
                int ret = ZipUtils.executeCommand(CompressUtils.this.mCommand);
                mDialog.cancel();
                mHandler.sendEmptyMessage(ret);
                super.run();
            }
        };
    }

    public void start() {
        if (mCommand.startsWith("7z x")) {
            new Thread() {

                @Override
                public void run() {
                    if (!checkExist()) {
                        ((Activity) mContext).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mThread.start();
                                mDialog.showDialog(R.raw.decompress);
                                mDialog.changeTitle(mContext.getResources().
                                    getString(R.string.compress_info));
                            }
                        });
                    }
                }
            }.start();
        } else {
            mThread.start();
            mDialog.showDialog(R.raw.compress);
            mDialog.changeTitle(mContext.getResources().getString(R.string.compress_info));
        }
    }

    private boolean checkExist() {
        File[] files = new File(mDestinationPath).listFiles();
        if (files == null) {
            return false;
        }
        ArrayList<String> archieveList = new ArrayList<>();
        StringBuilder builder = new StringBuilder("7z l ");
        builder.append("'" + mSourceName + "'");
        String result = ZipUtils.executeCommandGetStream(builder.toString());
        /**
         * result eg:
         * index   Date      Time    Attr         Size   Compressed  Name
         * 39      ------------------- ----- ------------ ------------  ------------------------
         * 40      2017-06-22 10:48:00 D....            0            0
         * 41      Screenshots
         * 42      2017-06-22 10:48:00 .....       360139       359663
         * 43      Screenshots/Screenshot_2017-06-22-10-47-59.png
         * 44      ------------------- ----- ------------ ------------  ------------------------
         * 45      2017-06-22 10:48:00             360139       359663  1 files, 1 folders
         */
        String[] allFiles = result.split("\n");
        for (int i = CONTENT_START_INDEX; i <= allFiles.length - CONTENT_END_INDEX; i += 2) {
            if (allFiles[i].contains("/")) {
                allFiles[i] = allFiles[i].replace(allFiles[i].substring(allFiles[i].indexOf("/")), "");
                if (!archieveList.contains(allFiles[i])) {
                    archieveList.add(allFiles[i]);
                }
            } else {
                archieveList.add(allFiles[i]);
            }
        }

        for (final String s : archieveList) {
            for (File file : files) {
                if (file.getName().equals(s)) {
                    ((Activity) mContext).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            DialogInterface.OnClickListener ok =
                                    new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    mThread.start();
                                    mDialog.showDialog(R.raw.decompress);
                                    mDialog.changeTitle(mContext.getResources().
                                        getString(R.string.compress_info));
                                }
                            };
                            showChooseAlertDialog(String.format(mContext.getResources().
                                getString(R.string.dialog_decompress_text), s), ok, null);
                        }
                    });
                    return true;
                }
            }
        }
        return false;
    }

    public void setDecompressInfo(String name, String path) {
        mSourceName = name;
        mDestinationPath = path;
    }

    /*public boolean checkPath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            toast(mContext.getString(R.string.path_not_exist));
            return false;
        }
        if (!file.isDirectory()) {
            toast(mContext.getString(R.string.path_not_directory));
            return false;
        }
        if (!file.canWrite()) {
            toast(mContext.getString(R.string.path_not_permission));
            return false;
        }
        return true;
    }*/

    public void checkFileName(String path, String name) {
        if (new File(path).exists()) {
            toast(mContext.getString(R.string.filename_aready_exists));
            return;
        }
        boolean isNameLegal = true;
        switch (isValidFileName(name)) {
            case FILE_NAME_NULL:
                toast(mContext.getString(R.string.file_name_not_null));
                return;
            case FILE_NAME_ILLEGAL:
                toast(mContext.getString(R.string.file_name_illegal));
                return;
            case FILE_NAME_WARNING:
                isNameLegal = false;
                DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        start();
                    }
                };
                showChooseAlertDialog(
                    mContext.getResources().getString(R.string.file_name_warning), ok, null);
        }
        if (isNameLegal) {
            start();
        }
    }

    public void toast(String text) {
        Toast.makeText(mContext, "" + text, Toast.LENGTH_SHORT).show();
    }

    public int isValidFileName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return FILE_NAME_NULL;
        } else {
            if (fileName.indexOf("/") != -1) {
                return FILE_NAME_ILLEGAL;
            }
            if (!Pattern.compile("[^@#\\$\\^&*\\(\\)\\[\\]]*").matcher(fileName).matches()) {
                return FILE_NAME_WARNING;
            }
            return FILE_NAME_LEGAL;
        }
    }

    public void showChooseAlertDialog(String message,
                       DialogInterface.OnClickListener ok, DialogInterface.OnClickListener cancel) {
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setMessage(message)
                .setPositiveButton(mContext.getResources().getString(R.string.confirm), ok)
                .setNegativeButton(mContext.getResources().getString(R.string.cancel), cancel)
                .setCancelable(false)
                .create();
        dialog.show();
    }

    public void showSimpleAlertDialog(String  message) {
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setMessage(message)
                .setPositiveButton(mContext.getResources().getString(R.string.confirm), null)
                .setCancelable(false)
                .create();
        dialog.show();
    }
}
