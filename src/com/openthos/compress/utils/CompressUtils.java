package com.openthos.compress.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.openthos.compress.R;
import com.hu.p7zip.ZipUtils;

public class CompressUtils {

    private static final int RET_SUCCESS = 0;
    private static final int RET_WARNING = 1;
    private static final int RET_FAULT = 2;
    private static final int RET_COMMAND = 7;
    private static final int RET_MEMORY = 8;
    private static final int RET_USER_STOP = 255;
    public static final int REQUEST_CODE_DST = 1;
    public static final int TAR_GZ_POSITION = 3;
    public static final int TAR_BZ2_POSITION = 4;
    public static final String COMPRESS_FILE_PATH = "paths";
    public static final int INDEX_7Z_FILENAME = 53;

    Context mContext = null;
    Thread mThread = null;
    Handler nHandler;
    String mCommand;
    String mCommandTar;
    String mDecompressFile;

    public CompressUtils(Context context, String command, String commandTar, String file) {
        mContext = context;
        mCommand = command;
        mCommandTar = commandTar;
        mDecompressFile = file;

        nHandler = new Handler(new Handler.Callback() {
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
                Toast.makeText(CompressUtils.this.mContext, retMsgId, Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mThread = new Thread() {
            @Override
            public void run() {

                int ret = ZipUtils.executeCommand(CompressUtils.this.mCommand);
                if ((ret == RET_SUCCESS || ret == RET_WARNING) && mCommandTar != null) {
                    ret = ZipUtils.executeCommand(CompressUtils.this.mCommandTar);
                    Log.d("LUNING", "==========success---------" + CompressUtils.this.mCommandTar + "----");
                }
                nHandler.sendEmptyMessage(ret);    //send back return code
                super.run();
            }
        };
    }

    public void start() {
        mThread.start();
    }


}
