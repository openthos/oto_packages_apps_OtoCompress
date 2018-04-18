package org.openthos.compress;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.hu.p7zip.ZipUtils;

import java.io.File;
import java.util.regex.Pattern;

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
    private static final String TEXT_SUCCESS = "Everything is Ok";
    private static final String TEXT_REPETITION = "already exists. Overwrite with";
    public static final String TEXT_INPUT_PASSWORD = "Enter password";
    public static final String TEXT_WRONG_PASSWORD = "Wrong password";
    private static final String TEXT_COMMAND_ERROR = "CommandError";
    private static final String TEXT_MEMORY_ERROR = "MemoryError";
    private static final String TEXT_USER_BREAK = "UserBreak";
    /**
     * int: Simple running result of command from jni code.
     */
    private static final int SUCCESS = 0;
    private static final int WARNING = 1;
    private static final int FAULT = 2;
    private static final int INPUT_PASSWORD = 3;
    private static final int WRONG_PASSWORD = 4;
    private static final int CORRECT_PASSWORD = 5;
    private static final int COMMAND_ERROR = 7;
    private static final int MEMORY_ERROR = 8;
    private static final int REPETITION = 9;
    private static final int USER_BREAK = 255;
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
    private String[] mCommands;
    private boolean mIsValidity;

    public CompressUtils(Context context) {
        mContext = context;
    }

    public void initUtils(String[] command) {
        mIsValidity = false;
        mCommands = command;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (mContext instanceof BaseActivity) {
                    ((BaseActivity) mContext).cancelDialog();
                }
                int retMsgId = -1;
                switch (msg.what) {
                    case SUCCESS:
                        retMsgId = R.string.msg_ret_success;
                        break;
                    case WARNING:
                        retMsgId = R.string.msg_ret_warning;
                        break;
                    case FAULT:
                        retMsgId = R.string.msg_ret_fault;
                        break;
                    case COMMAND_ERROR:
                        retMsgId = R.string.msg_ret_command;
                        break;
                    case MEMORY_ERROR:
                        retMsgId = R.string.msg_ret_memmory;
                        break;
                    case USER_BREAK:
                        retMsgId = R.string.msg_ret_user_stop;
                        break;
                    case REPETITION:
                        DialogInterface.OnClickListener ok =
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        initThread();
                                        mCommands[0] += " -aoa";
                                        start();
                                    }
                                };
                        showChooseAlertDialog(String.format(mContext.getResources().
                                getString(R.string.dialog_decompress_text), (String) msg.obj), ok, null);
                        break;
                    case INPUT_PASSWORD:
                        ((DecompressActivity) mContext).inputPassword(true);
                        break;
                    case WRONG_PASSWORD:
                        ((DecompressActivity) mContext).inputPassword(false);
                        break;
                    case CORRECT_PASSWORD:
                        mIsValidity = true;
                        initThread();
                        start();
                        break;
                }
                if (retMsgId != -1) {
                    toast(mContext.getString(retMsgId));
                }
                if (msg.what == SUCCESS || msg.what == WARNING) {
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
        mThread = null;
        mThread = new Thread() {
            @Override
            public void run() {
                if (mCommands[0].startsWith("7z t")) {
                    if (mIsValidity) {
                        mCommands[0] = mCommands[0].replaceFirst("t", "x");
                        String result = ZipUtils.executeCommandGetStream(mCommands[0]);
                        checkRepetition(result);
                    } else {
                        String results = ZipUtils.executeCommandGetStream(mCommands[0]);
                        checkPassword(results);
                    }
                } else {
                    int ret = SUCCESS;
                    for (int i = 0; i < mCommands.length; i++) {
                        ret = ZipUtils.executeCommand(mCommands[i]);
                        if (ret != SUCCESS) {
                            break;
                        }
                    }
                    mHandler.sendEmptyMessage(ret);
                }
                super.run();
            }
        };
    }

    private void checkPassword(String result) {
        if (result.contains(TEXT_INPUT_PASSWORD)) {
            mHandler.sendEmptyMessage(INPUT_PASSWORD);
            return;
        }
        if (result.contains(TEXT_WRONG_PASSWORD)) {
            mHandler.sendEmptyMessage(WRONG_PASSWORD);
            return;
        }
        mHandler.sendEmptyMessage(CORRECT_PASSWORD);
    }

    private void checkRepetition(String result) {
        if (!result.contains(" ")) {
            switch (result) {
                case TEXT_COMMAND_ERROR:
                    mHandler.sendEmptyMessage(COMMAND_ERROR);
                    return;
                case TEXT_MEMORY_ERROR:
                    mHandler.sendEmptyMessage(MEMORY_ERROR);
                    return;
                case TEXT_USER_BREAK:
                    mHandler.sendEmptyMessage(USER_BREAK);
                    return;
                default:
                    mHandler.sendEmptyMessage(FAULT);
                    return;
            }
        } else {
            String[] allResult = result.split("\n");
            for (int i = allResult.length - 1; i >= 0; i--) {
                switch (allResult[i]) {
                    case TEXT_SUCCESS:
                        mHandler.sendEmptyMessage(SUCCESS);
                        return;
                    case TEXT_REPETITION:
                        mHandler.sendMessage(Message.obtain(
                                mHandler, REPETITION, allResult[i + 1]));
                        return;
                }
            }
        }
        mHandler.sendEmptyMessage(SUCCESS);
    }

    public void start() {
        mThread.start();
        if (mContext instanceof BaseActivity) {
            if (mCommands[0].startsWith("7z a")) {
                ((BaseActivity) mContext).showCompressingDialog();
            } else {
                ((BaseActivity) mContext).showDeCompressingDialog();
            }
        }
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
                                      DialogInterface.OnClickListener ok,
                                      DialogInterface.OnClickListener cancel) {
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setMessage(message)
                .setPositiveButton(mContext.getResources().getString(R.string.confirm), ok)
                .setNegativeButton(mContext.getResources().getString(R.string.cancel), cancel)
                .setCancelable(true)
                .create();
        dialog.show();
    }

    public void showSimpleAlertDialog(String message) {
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setMessage(message)
                .setPositiveButton(mContext.getResources().getString(R.string.confirm), null)
                .setCancelable(true)
                .create();
        dialog.show();
    }
}
