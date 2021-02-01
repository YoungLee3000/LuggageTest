package com.nlscan.android.luggagetest;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class CustomEditTextDialog extends Dialog {
    Context mContext;
    private TextView btnSure;
    private TextView btnCancle;
    private TextView title;
    private EditText editText;

    public CustomEditTextDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
        this.mContext = context;
        initView();
    }

    //初始化
    public void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.edit_dialog, null);
        title = (TextView) view.findViewById(R.id.title);
        editText = (EditText) view.findViewById(R.id.edittext);
        btnSure = (TextView) view.findViewById(R.id.dialog_confirm_sure);
        btnCancle = (TextView) view.findViewById(R.id.dialog_confirm_cancle);
        super.setContentView(view);
    }


    public CustomEditTextDialog setTile(String s) {
        title.setText(s);
        return this;
    }

    //获取当前输入框对象
    public View getEditText() {
        return editText;
    }

    //确定键监听器
    public void setOnSureListener(View.OnClickListener listener) {
        btnSure.setOnClickListener(listener);
    }

    //取消键监听器
    public void setOnCanlceListener(View.OnClickListener listener) {
        btnCancle.setOnClickListener(listener);
    }
}
