package com.openthos.compress.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.openthos.compress.R;

/**
 * Created by xu on 2016/12/06.
 */
public class ProgressInfoDialog extends Dialog {
    private Context mContext;
    private TextView mTextMessage;
    private TextView mTextTitle;
    private static ProgressInfoDialog dialog = null;
    private GifView mGif;
    private int mRawId;

    public ProgressInfoDialog(Context context) {
        super(context);
        mContext = context;
    }

    public static ProgressInfoDialog getInstance(Context context) {
        if (dialog == null) {
            return new ProgressInfoDialog(context);
        } else {
            return dialog;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View v = View.inflate(mContext, R.layout.dialog_progress, null);
        setContentView(v);
        mTextMessage = (TextView) v.findViewById(R.id.text_message);
        mTextTitle = (TextView) v.findViewById(R.id.text_title);
        mGif = (GifView) v.findViewById(R.id.gif);
        mTextTitle.setText(mContext.getString(R.string.compress_info));
    }

    public void showDialog(int rawId) {
        mRawId = rawId;
        if (mGif == null) {
            mGif = (GifView) View.inflate(mContext, R.layout.dialog_progress, null)
                                     .findViewById(R.id.gif);
        }
        mGif.setMovieResource(mRawId);
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        show();
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
    }

    public void changeTitle(final String s) {
        if (mGif == null) {
            mGif = (GifView) View.inflate(mContext, R.layout.dialog_progress, null)
                                     .findViewById(R.id.gif);
        }
        mGif.setMovieResource(mRawId);
        mTextTitle.setText(s);
    }
}
