package com.nlscan.android.luggagetest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.nlscan.luggage.IJudgeCallback;
import com.nlscan.luggage.ModelInterface;
import com.nlscan.luggage.ParamValue;

public class EntryActivity extends AppCompatActivity {

    private EditText et_flight,et_st_name;

    private Button btn_entry;

    private RadioGroup rg_case,rg_st_type;



    //登录信息保存
    private static final String TAG = "LuggageTest";
    private SharedPreferences mSp;
    private String mFlight = "";
    private String mStationName = "";
    private String mStationType = "";
    private String mCase = "";







    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);



        et_flight = (EditText) findViewById(R.id.et_flight);
        et_st_name = (EditText) findViewById(R.id.et_st_name);
        btn_entry = (Button) findViewById(R.id.btn_entry);


        rg_case = (RadioGroup) findViewById(R.id.rg_case);
        rg_st_type = (RadioGroup) findViewById(R.id.rg_st_type);


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
}
