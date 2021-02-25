package com.nlscan.android.luggagetest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class EntryActivity extends AppCompatActivity {



    @BindView(R.id.et_flight)
    TextView et_flight;


    @BindView(R.id.et_st_name)
    TextView et_st_name;



    @BindView(R.id.btn_entry)
    Button btn_entry;


    @BindView(R.id.rg_case)
    RadioGroup rg_case;

    @BindView(R.id.rb_case_1)
    RadioButton rb_case_1;

    @BindView(R.id.rb_case_2)
    RadioButton rb_case_2;

    @BindView(R.id.rb_case_3)
    RadioButton rb_case_3;

    @BindView(R.id.rb_case_3_1)
    RadioButton rb_case_3_1;

    @BindView(R.id.rb_case_4)
    RadioButton rb_case_4;

    @BindView(R.id.rg_st_type)
    RadioGroup rg_st_type;

    @BindView(R.id.rb_start)
    RadioButton rb_start;

    @BindView(R.id.rb_middle)
    RadioButton rb_middle;

    @BindView(R.id.rb_dest)
    RadioButton rb_dest;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        ButterKnife.bind(this);
        initFile();


        rg_case.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_case_1:
                        mCase = ParamValue.CASE_DEFAULT;
                        break;
                    case R.id.rb_case_2:
                        mCase = ParamValue.CASE_CAR_TO_STORE;
                        break;
                    case R.id.rb_case_3:
                        mCase = ParamValue.CASE_STORE_TO_CAR;
                        break;
                    case R.id.rb_case_3_1:
                        mCase = ParamValue.CASE_STORE_SEARCH;
                        break;
                    case R.id.rb_case_4:
                        mCase = ParamValue.CASE_CAR_TO_BAND;
                        break;
                }
            }
        });

        rg_st_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_start:
                        mStationType = ParamValue.STATION_START;
                        break;
                    case R.id.rb_middle:
                        mStationType = ParamValue.STATION_MIDDLE;
                        break;
                    case R.id.rb_dest:
                        mStationType = ParamValue.STATION_DEST;
                        break;
                }
            }
        });


        //获取存储的ip值
        mSp = getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        mFlight = mSp.getString(Constants.SP_KEY_FLIGHT,"");
        et_flight.setText(mFlight);

        mStationName = mSp.getString(Constants.SP_KEY_STATION_NAME,"");
        et_st_name.setText(mStationName);

        mStationType = mSp.getString(Constants.SP_KEY_STATION_TYPE,"");
        mCase = mSp.getString(Constants.SP_KEY_CASE,"");

        switch (mStationType){
            default:
            case ParamValue.STATION_START:
                rb_start.setChecked(true);
                break;
            case ParamValue.STATION_MIDDLE:
                rb_middle.setChecked(true);
                break;
            case ParamValue.STATION_DEST:
                rb_dest.setChecked(true);
                break;
        }

        switch (mCase){
            default:
            case ParamValue.CASE_DEFAULT:
                rb_case_1.setChecked(true);
                break;
            case ParamValue.CASE_CAR_TO_STORE:
                rb_case_2.setChecked(true);
                break;
            case ParamValue.CASE_STORE_TO_CAR:
                rb_case_3.setChecked(true);
                break;
            case ParamValue.CASE_STORE_SEARCH:
                rb_case_3_1.setChecked(true);
                break;
            case ParamValue.CASE_CAR_TO_BAND:
                rb_case_4.setChecked(true);
                break;
        }




        btn_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(EntryActivity.this,MainActivity.class);


                //存储登录信息
                mFlight = et_flight.getText().toString();
                mStationName = et_st_name.getText().toString();

                SharedPreferences.Editor editor = mSp.edit();
                editor.putString(Constants.SP_KEY_FLIGHT, mFlight);
                editor.putString(Constants.SP_KEY_STATION_NAME,mStationName);
                editor.putString(Constants.SP_KEY_CASE,mCase);
                editor.putString(Constants.SP_KEY_STATION_TYPE,mStationType);
                editor.apply();

                intent.putExtra(Constants.SP_KEY_FLIGHT,mFlight);
                intent.putExtra(Constants.SP_KEY_STATION_NAME,mStationName);
                intent.putExtra(Constants.SP_KEY_CASE,mCase);
                intent.putExtra(Constants.SP_KEY_STATION_TYPE,mStationType);



                startActivity(intent);
            }
        });



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
