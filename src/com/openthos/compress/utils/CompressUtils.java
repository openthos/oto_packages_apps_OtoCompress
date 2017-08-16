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
    public static final String SDCARD_PATH = "/storage/emulated/0";
    public static final String SUFFIX_TAR = ".tar";
    public static final String SUFFIX_GZ = ".tar.gz";
    public static final String SUFFIX_BZ2 = ".tar.bz2";
    /**
     * String: Part of detailed running result of command from jni code,
     * which means command running infomation.
     */
    private static final String STRING_RESULT_SUCCESS = "Everything is Ok";
    private static final String STRING_RESULT_REPETITION = "already exists. Overwrite with";
    private static final String STRING_RESULT_PASSWORD = "Enter password:";
    private static final String STRING_RESULT_WRONG_PASSWORD =
                                "Data Error in encrypted file. Wrong password?";
    private static final String STRING_RESULT_COMMAND = "CommandError";
    private static final String STRING_RESULT_MEMORY = "MemoryError";
    private static final String STRING_RESULT_USER_BREAK = "UserBreak";
    /**
     * int: Simple running result of command from jni code.
     */
    private static final int INT_RESULT_SUCCESS = 0;
    private static final int INT_RESULT_WARNING = 1;
    private static final int INT_RESULT_FAULT = 2;
    private static final int INT_RESULT_COMMAND = 7;
    private static final int INT_RESULT_MEMORY = 8;
    private static final int INT_RESULT_REPETITION = 9;
    private static final int INT_RESULT_PASSWORD = 10;
    private static final int INT_RESULT_USER_BREAK = 255;
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
    private ProgressInfoDialog mDialog;
    private boolean mHasTemp;

    public CompressUtils(Context context) {
        mContext = context;
    }

    public void initUtils(String command) {
        mCommand = mHasTemp ? command + "-aoa" : command;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int retMsgId = -1;
                switch (msg.what) {
                    case INT_RESULT_SUCCESS:
                        retMsgId = R.string.msg_ret_success;
                        break;
                    case INT_RESULT_WARNING:
                        retMsgId = R.string.msg_ret_warning;
                        break;
                    case INT_RESULT_FAULT:
                        retMsgId = R.string.msg_ret_fault;
                        break;
                    case INT_RESULT_COMMAND:
                        retMsgId = R.string.msg_ret_command;
                        break;
                    case INT_RESULT_MEMORY:
                        retMsgId = R.string.msg_ret_memmory;
                        break;
                    case INT_RESULT_USER_BREAK:
                        retMsgId = R.string.msg_ret_user_stop;
                        break;
                    case INT_RESULT_REPETITION:
                        DialogInterface.OnClickListener ok =
                                new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                initThread();
                                mCommand += "-aoa";
                                mThread.start();
                                mDialog.showDialog(R.raw.decompress);
                                mDialog.changeTitle(mContext.getResources().
                                    getString(R.string.compress_info));
                            }
                        };
                        showChooseAlertDialog(String.format(mContext.getResources().
                           getString(R.string.dialog_decompress_text), (String) msg.obj), ok, null);
                        break;
                    case INT_RESULT_PASSWORD:
                        ((DecompressActivity) mContext).inputPassword();
                        mHasTemp = true;
                        break;
                }
                if (retMsgId != -1) {
                    toast(mContext.getString(retMsgId));
                }
                if (msg.what == INT_RESULT_SUCCESS || msg.what == INT_RESULT_WARNING) {
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).finish();
                    }
                }
                mThread = null;
                super.handleMessage(msg);
            }
        };
        initThread();
    }

    private void initThread() {
        mThread = new Thread() {
            @Override
            public void run() {
                if (mCommand.startsWith("7z x")) {
                    String result = ZipUtils.executeCommandGetStream(mCommand);
                    checkResult(result);
                    mHasTemp = false;
                } else {
                    int ret = ZipUtils.executeCommand(mCommand);
                    mDialog.cancel();
                    mHandler.sendEmptyMessage(ret);
                }
                super.run();
            }
        };
        mDialog = ProgressInfoDialog.getInstance(mContext);
    }

    private void checkResult(String result) {
        int hintResult = -1;
        if (!result.contains(" ")) {
            switch (result) {
                case STRING_RESULT_COMMAND:
                    hintResult = INT_RESULT_COMMAND;
                    break;
                case STRING_RESULT_MEMORY:
                    hintResult = INT_RESULT_MEMORY;
                    break;
                case STRING_RESULT_USER_BREAK:
                    hintResult = INT_RESULT_USER_BREAK;
                    break;
                default:
                    hintResult = INT_RESULT_FAULT;
                    break;
            }
        } else {
            String[] allResult = result.split("\n");
            for (int i = allResult.length - 1; i >= 0; i--) {
                switch (allResult[i]) {
                    case STRING_RESULT_SUCCESS:
                        hintResult = INT_RESULT_SUCCESS;
                        break;
                    case STRING_RESULT_REPETITION:
                        hintResult = INT_RESULT_REPETITION;
                        mHandler.sendMessage(Message.obtain(
                                mHandler, hintResult, allResult[i + 1]));
                        break;
                    case STRING_RESULT_PASSWORD:
                        hintResult = INT_RESULT_PASSWORD;
                        break;
                    case STRING_RESULT_WRONG_PASSWORD:
                        hintResult = INT_RESULT_PASSWORD;
                        break;
                }
                if (hintResult != -1) {
                    break;
                }
            }
        }
        mDialog.cancel();
        if (hintResult == -1) {
            hintResult = INT_RESULT_SUCCESS;
        }
        if (hintResult != INT_RESULT_REPETITION) {
            mHandler.sendEmptyMessage(hintResult);
        }
    }

    public void start() {
        mThread.start();
        mDialog.showDialog(R.raw.compress);
        mDialog.changeTitle(mContext.getResources().getString(R.string.compress_info));
    }

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
                .setCancelable(true)
                .create();
        dialog.show();
    }

    public void showSimpleAlertDialog(String  message) {
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setMessage(message)
                .setPositiveButton(mContext.getResources().getString(R.string.confirm), null)
                .setCancelable(true)
                .create();
        dialog.show();
    }
}
