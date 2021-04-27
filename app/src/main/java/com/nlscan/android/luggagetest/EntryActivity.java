package com.nlscan.android.luggagetest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.nlscan.luggage.ParamValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EntryActivity extends AppCompatActivity {



    @BindView(R.id.btn_case1)
    Button btnCase1;

    @BindView(R.id.btn_case2)
    Button btnCase2;

    @BindView(R.id.btn_case3)
    Button btnCase3;

    @BindView(R.id.btn_case3_1)
    Button btnCase3_1;

    @BindView(R.id.btn_case4)
    Button btnCase4;




    //登录信息保存
    private static final String TAG = "LuggageTest";
    private SharedPreferences mSp;
    private String mFlight = "";
    private String mStationName = "";
    private String mStationType = "";
    private String mCase = "";



    //测试文件目录
    private String filePath = "/sdcard/myLuggage/";
    private String fileName_1 = "flight_data_1.json";
    private String fileName_2 = "flight_data_2.json";
    private String fileName_3 = "flight_data_3.json";
    private String fileName_3_1 = "flight_data_3_1.json";
    private String fileName_4 = "flight_data_4.json";


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }






    //按键事件
    @OnClick({R.id.btn_case1,R.id.btn_case2,R.id.btn_case3,R.id.btn_case3_1,R.id.btn_case4})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_case1:
                mCase = ParamValue.CASE_DEFAULT;
                break;

            case R.id.btn_case2:
                mCase = ParamValue.CASE_CAR_TO_STORE;
                break;
            case R.id.btn_case3:
                mCase = ParamValue.CASE_STORE_TO_CAR;

                break;
            case R.id.btn_case3_1:
                mCase = ParamValue.CASE_STORE_SEARCH;

                break;
            case R.id.btn_case4:
                mCase = ParamValue.CASE_CAR_TO_BAND;
                break;

        }
        entry();
    }


    private void entry(){
        Intent intent = new Intent(EntryActivity.this,MainActivity.class);

        intent.putExtra(Constants.SP_KEY_FLIGHT,mFlight);
        intent.putExtra(Constants.SP_KEY_STATION_NAME,mStationName);
        intent.putExtra(Constants.SP_KEY_CASE,mCase);
        intent.putExtra(Constants.SP_KEY_STATION_TYPE,mStationType);



        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        ButterKnife.bind(this);
        initFile();


    }


    /**
     * 初始文件
     */
    private void initFile(){
        FileUtil.createDir(filePath);
        copyfile(filePath,fileName_1,R.raw.flight_data_1);
        copyfile(filePath,fileName_2,R.raw.flight_data_2);
        copyfile(filePath,fileName_3,R.raw.flight_data_3);
        copyfile(filePath,fileName_3_1,R.raw.flight_data_3_1);
        copyfile(filePath,fileName_4,R.raw.flight_data_4);

    }




    /**
     * 复制文件至手机文件夹
     * @param fileDirPath
     * @param fileName
     * @param id
     */
    private void copyfile(String fileDirPath,String fileName,int id) {
        String filePath = fileDirPath  + fileName;// 文件路径
        try {
            File dir = new File(fileDirPath);// 目录路径
            if (!dir.exists()) {// 如果不存在，则创建路径名
                dir.mkdirs();
            }
            // 目录存在，则将apk中raw文件夹中的需要的文档复制到该目录下
            File file = new File(filePath);
            if (!file.exists()) {
                InputStream is = getResources().openRawResource(
                        id);
                FileOutputStream fs = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fs.write(buffer, 0, count);
                }
                try {
                    fs.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
