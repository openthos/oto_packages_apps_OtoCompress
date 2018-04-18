package org.openthos.compress.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.openthos.compress.R;

/**
 * Created by MingWang on 2017/10/12.
 */

public class SingleLineInputDialog extends Dialog {
    private Activity mContext;
    private EditText mEditText;
    private TextView mTvMessage;
    private Button mBtConfirm, mBtCancel;
    private View.OnClickListener mCancelListener, mConfirmListener;
    private View mView;

    public SingleLineInputDialog(Activity context) {
        super(context, R.style.AppTheme);
        this.mContext = context;
        initView();
    }

    private void initView() {
        if (mView == null) {
            mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_single_input, null, false);
            mTvMessage = (TextView) mView.findViewById(R.id.tv_title);
            mEditText = (EditText) mView.findViewById(R.id.et_content);
            mBtConfirm = (Button) mView.findViewById(R.id.bt_confirm);
            mBtCancel = (Button) mView.findViewById(R.id.bt_cancel);
        }
        if (mConfirmListener != null) {
            mBtConfirm.setOnClickListener(mConfirmListener);
        }
        if (mCancelListener != null) {
            mBtCancel.setOnClickListener(mCancelListener);
        }
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        mBtConfirm.callOnClick();
                        return true;
                    }
                }
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
                    mContext.finish();
                    return false;
                }
                return false;
            }
        });
        this.setContentView(mView);
    }

    @Override
    public void show() {
        super.show();
        mEditText.setText(mContext.getResources().getString(R.string.empty_str));
        mEditText.requestFocus();
    }

    public void setCancelListener(View.OnClickListener listener) {
        mCancelListener = listener;
    }

    public void setConfirmListener(View.OnClickListener listener) {
        mConfirmListener = listener;
    }

    public String getContent() {
        return mEditText.getText().toString();
    }

    public void setMessage(String message) {
        mTvMessage.setText(message);
        initView();
    }
}
