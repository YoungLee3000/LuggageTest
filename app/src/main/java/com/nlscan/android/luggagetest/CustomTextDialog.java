package com.nlscan.android.luggagetest;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class CustomTextDialog extends Dialog {
    Context mContext;
    private TextView btnSure;
    private TextView btnCancle;
    private TextView title;
    private TextView message;

    public CustomTextDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
        this.mContext = context;
        initView();
    }

    //初始化
    public void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.text_dialog, null);
        title = (TextView) view.findViewById(R.id.title2);
        message = (TextView) view.findViewById(R.id.message);
        btnSure = (TextView) view.findViewById(R.id.dialog_confirm_sure);
        btnCancle = (TextView) view.findViewById(R.id.dialog_confirm_cancle);
        super.setContentView(view);
    }


    public CustomTextDialog setTile(String s) {
        title.setText(s);
        return this;
    }

    public void setButtonVisible(boolean show){
        if (show){
            btnSure.setVisibility(View.VISIBLE);
        }
        else{
            btnSure.setVisibility(View.INVISIBLE);
        }
    }

    public void setButtonText(String str){
        btnCancle.setText(str);
    }


    public CustomTextDialog setMessage(String s) {
        message.setText(s);
        return this;
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
