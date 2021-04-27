package com.nlscan.android.luggagetest;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class CustomChooseDialog extends Dialog {
    Context mContext;


    private Button car1,car2,car3,car4,car5;

    public CustomChooseDialog(@NonNull Context context) {
        super(context, R.style.CustomDialog);
        this.mContext = context;
        initView();
    }

    //初始化
    public void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.choose_dialog, null);


        car1 = (Button) view.findViewById(R.id.btn_car1);
        car2 = (Button) view.findViewById(R.id.btn_car2);
        car3 = (Button) view.findViewById(R.id.btn_car3);
        car4 = (Button) view.findViewById(R.id.btn_car4);
        car5 = (Button) view.findViewById(R.id.btn_car5);




        super.setContentView(view);
    }




    //确定键监听器
    public void setListener(View.OnClickListener listener) {
        car1.setOnClickListener(listener);
        car2.setOnClickListener(listener);
        car3.setOnClickListener(listener);
        car4.setOnClickListener(listener);
        car5.setOnClickListener(listener);

    }


}
