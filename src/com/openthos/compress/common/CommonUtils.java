package com.openthos.compress.common;

import android.content.Context;
import com.openthos.compress.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

/**
 * Created by MingWang on 2017/10/11.
 */

public class CommonUtils {

    private final static int GB = 1024 * 1024 * 1024;
    private final static int MB = 1024 * 1024;
    private final static int KB = 1024;

    public static String sizeUnitFormat(Context context, int size) {
        DecimalFormat df = new DecimalFormat("0.00");
        if (size / GB > 0) {
            return df.format((float) size / GB) + " GB";
        } else if (size / MB > 0) {
            return df.format((float) size / MB) + " MB";
        } else if (size / KB > 0) {
            return df.format((float) size / KB) + " KB";
        } else {
            return size + " " + context.getResources().getString(R.string.str_byte);
        }
    }

    public static void copyFile(String srcPath, String destPath) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(new File(srcPath));
            output = new FileOutputStream(new File(destPath));
            byte[] buf = new byte[2048];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        }
    }

    public static void removeFile(String filePath) {
        String cmd = "/bin/rm -rv " + filePath;
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
