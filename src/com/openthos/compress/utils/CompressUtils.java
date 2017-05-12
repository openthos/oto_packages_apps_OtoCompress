package com.openthos.compress.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.openthos.compress.CompressActivity;
import com.openthos.compress.DecompressActivity;
import com.openthos.compress.R;
import com.hu.p7zip.ZipUtils;

import java.io.File;
import java.util.regex.Pattern;

public class CompressUtils {

    public static final String EXTRA_DELETE_FILE_HEADER = "OtoDeleteFile:///";
    public static final String COMPRESS_FILE_PATH = "paths";
    private static final int RET_SUCCESS = 0;
    private static final int RET_WARNING = 1;
    private static final int RET_FAULT = 2;
    private static final int RET_COMMAND = 7;
    private static final int RET_MEMORY = 8;
    private static final int RET_USER_STOP = 255;
    public static final int REQUEST_CODE_DST = 1;
    public static final int TAR_GZ_POSITION = 3;
    public static final int TAR_BZ2_POSITION = 4;

    private static final int FILE_NAME_LEGAL = 13;
    private static final int FILE_NAME_NULL = 14;
    private static final int FILE_NAME_ILLEGAL = 15;
    private static final int FILE_NAME_WARNING = 16;

    private Context mContext;
    private Thread mThread;
    private Handler mHandler;
    private String mCommand;
    private String mCommandTar;

    public CompressUtils(Context context, String command, String commandTar) {
        mContext = context;
        mCommand = command;
        mCommandTar = commandTar;

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
                toast(CompressUtils.this.mContext, mContext.getString(retMsgId));
                if (msg.what == RET_SUCCESS || msg.what == RET_WARNING) {
                    if (mContext instanceof CompressActivity) {
                        ((CompressActivity) mContext).finish();
                    } else {
                        ((DecompressActivity) mContext).finish();
                    }
                }
                return false;
            }
        });

        mThread = new Thread() {
            @Override
            public void run() {

                int ret = ZipUtils.executeCommand(CompressUtils.this.mCommand);
                if ((ret == RET_SUCCESS || ret == RET_WARNING) && mCommandTar != null) {
                    ret = ZipUtils.executeCommand(CompressUtils.this.mCommandTar);
                }
                mHandler.sendEmptyMessage(ret);
                super.run();
            }
        };
    }

    public void start() {
        mThread.start();
    }

    public boolean checkPath(Context context, String path) {
        File file = new File(path);
        if (!file.exists()) {
            toast(context, context.getString(R.string.path_not_exist));
            return false;
        }
        if (!file.isDirectory()) {
            toast(context, context.getString(R.string.path_not_directory));
            return false;
        }
        if (!file.canWrite()) {
            toast(context, context.getString(R.string.path_not_permission));
            return false;
        }
        return true;
    }

    public boolean checkFileName(Context context, String name) {
        switch (isValidFileName(name)) {
            case FILE_NAME_NULL:
                toast(context, context.getString(R.string.file_name_not_null));
                return false;
            case FILE_NAME_ILLEGAL:
                toast(context, context.getString(R.string.file_name_illegal));
                return false;
            case FILE_NAME_WARNING:
                toast(context, context.getString(R.string.file_name_warning));
                return true;
        }
        return true;
    }

    public void toast(Context context, String text) {
        Toast.makeText(context, "" + text, Toast.LENGTH_SHORT).show();
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
}
