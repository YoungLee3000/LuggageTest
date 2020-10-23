package com.nlscan.android.luggagetest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nlscan.luggage.IJudgeCallback;
import com.nlscan.luggage.ModelInterface;
import com.nlscan.luggage.ResultState;


public class MainActivity extends AppCompatActivity {


    //*-------------服务连接相关-------------------------//

    private IJudgeCallback.Stub mCallback = new IJudgeCallback.Stub() {
        @Override
        public void onJudgeResult(String s) throws RemoteException {
            tvRel.setText(s);
        }
    };

    //初始化服务
    private LuggageServiceConnection mConnection;
    private ModelInterface gModelInterface;
    private void initService(){

        Intent service = new Intent("android.nlscan.intent.action.START_LUGGAGE_SERVICE");
        service.setPackage("com.nlscan.luggage");
        mConnection = new LuggageServiceConnection();
        boolean rel = getApplicationContext().bindService(service,mConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "binding service");
        Log.d(TAG,"binding result is " + rel);

    }

    private class  LuggageServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            gModelInterface = ModelInterface.Stub.asInterface(service);
            try {
                int rel = gModelInterface.initService(); //服务初始化
                gModelInterface.setCallback(mCallback);  //设置回调接口
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            gModelInterface = null;
        }
    }

    //界面创建时绑定服务
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initService();
    }

    //界面销毁时解除服务
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }




    //*-------------服务连接相关-------------------------//


    private static final String TAG = "LuggageService";






    //----------------------------控件相关--------------------------//
    //按键
    private Button btnStart,btnStop,btnSend,btnClear;
    private TextView tvRun,tvRel;

    private void initView(){
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rel = 0;
                try {
                    rel = gModelInterface.startService();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                tvRun.setText("开启服务结果: " + rel );
            }
        });


        btnStop = (Button) findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rel = 0;
                try {
                    rel = gModelInterface.stopService();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                tvRun.setText("关闭服务结果: " + rel );
            }
        });

        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    gModelInterface.sendInfo(ResultState.DATA_BOX,
                            "[{\"epc\":\"FF4900\"},{\"epc\":\"FF4922\"}]");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                tvRun.setText("下发数据"  );
            }
        });

        btnClear = (Button) findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    gModelInterface.clearInfo("conditions");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                tvRun.setText("清除数据");
            }
        });


        tvRun = (TextView) findViewById(R.id.tv_run);
        tvRel = (TextView) findViewById(R.id.tv_rel);

    }

    //----------------------------控件相关--------------------------//







}
