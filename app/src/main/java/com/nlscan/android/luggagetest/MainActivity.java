package com.nlscan.android.luggagetest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nlscan.luggage.DataKey;
import com.nlscan.luggage.IJudgeCallback;
import com.nlscan.luggage.ModelInterface;
import com.nlscan.luggage.ParamValue;
import com.nlscan.luggage.ResultState;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {


    //*-------------服务连接相关-------------------------//

    private IJudgeCallback.Stub mCallback = new IJudgeCallback.Stub() {
        @Override
        public void onJudgeResult(String s) throws RemoteException {

            Log.d(TAG,"the receive data is " + s);
            JSONArray resultArray = JSON.parseArray(s);

            if (resultArray != null){


                for(Object object : resultArray){
                    JSONObject relObj = (JSONObject) object;
                    Log.d(TAG,"the json obj is " + relObj.toJSONString());
                    Map<String,String> map = new HashMap<>();
                    String current_case = ValueUtil.TextGet(relObj.getString(DataKey.J_CASE));
                    if (!current_case.equals(mCase)) continue;
                    String box_id = ValueUtil.TextGet(relObj.getString(DataKey.J_BOX_ID)) ;
                    String epc_id =  ValueUtil.TextGet(relObj.getString(DataKey.J_EPC_ID)) ;
                    String box_state = ValueUtil.TextGet(relObj.getString(DataKey.J_PREDICT_BOX_STATE)) ;
                    String car_id = ValueUtil.TextGet(relObj.getString(DataKey.J_CAR_ID));

                    if(box_id.length()>=4) box_id = "**"+box_id.substring(box_id.length()-4);
                    if(epc_id.length()>=4) epc_id = "**"+epc_id.substring(epc_id.length()-4);
                    map.put(Constants.RV_HEAD_EPC,epc_id+","+box_id);

                    String stateParse = "";
                    switch (box_state){
                        case ResultState.PREDICT_BOX_CLOSE:
                            stateParse = "行李靠近";
                            break;
                        case ResultState.PREDICT_BOX_CARRY_RIGHT:
                            stateParse = "携带至正确拖车";
                            performSound(false);

                            break;
                        case ResultState.PREDICT_BOX_CARRY_WRONG:
                            stateParse = "携带至错误拖车";
                            wrongNotify();
                            break;
                        case ResultState.PREDICT_BOX_LAY_RIGHT:
                            stateParse = "放置正确拖车";
                            break;
                        case ResultState.PREDICT_BOX_LAY_WRONG:
                            stateParse = "放置错误拖车";
                            break;
                        case ResultState.PREDICT_BOX_BAN:
                            stateParse = "非法搬运";
                            wrongNotify();
                            break;
                        case ResultState.PREDICT_BOX_LACK:
                            stateParse = "缺失的行李";
                            break;
                    }
                    if ("".equals(stateParse)) continue;




                    map.put(Constants.RV_HEAD_BOX_STATE,stateParse);
                    map.put(Constants.RV_HEAD_CAR,car_id);



                    gDataList.add(map);

                    gHandler.sendEmptyMessage(UPDATE_VIEW);

                }
            }

        }
    };


    private void wrongNotify(){
        final int playId =    performSound(true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                soundPool.stop(playId);
            }
        },1000);
    }

    private boolean gBindState = false;//服务是否绑定成功


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

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "interface service init");
                interfaceServiceInit();
            }
        },600);

    }

    private class  LuggageServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"luggage service connected ");
            gModelInterface = ModelInterface.Stub.asInterface(service);
            gBindState = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"luggage service disconnected ... ");
            gBindState = false;
            gModelInterface = null;
        }
    }




    //上一个界面传来的值
    private String mFlight = "";
    private String mStationName = "";
    private String mStationType = "";
    private String mCase = "";

    //界面创建时绑定服务
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);
        initData();
        initService();
        initView();

    }

    //界面销毁时解除服务
    @Override
    protected void onDestroy() {
        super.onDestroy();
        endService();

        //------------删除所有数据，慎用！调试时需要用到--------------------//
        clearAll();
        //-------------删除所有数据，慎用！调试时需要用到--------------------//
        try {
            unbindService(mConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }




    //*-------------服务连接相关-------------------------//


    private static final String TAG = "LuggageService";






    //----------------------------控件相关--------------------------//

    @BindView(R.id.tv_run_result)
    TextView tvRunResult;

    @BindView(R.id.tv_current_case)
    TextView tvCurrentCase;

    @BindView(R.id.ed_car_id)
    EditText edCarId;


    @BindView(R.id.btn_start_service)
    Button btnStartService;

    @BindView(R.id.btn_new_car_ready)
    Button btnNewCarReady;

    @BindView(R.id.btn_new_car_done)
    Button btnNewCarDone;

    @BindView(R.id.btn_end_service)
    Button btnEndService;

    @BindView(R.id.rv_epc)
    RecyclerView rvEpc;
    private List<Map<String,String>> gDataList = new ArrayList<>();
    private MyRVAdapter myRVAdapter;



    //测试用例
    private JSONArray testDataJA;

    //按键事件
    @OnClick({R.id.btn_start_service,R.id.btn_new_car_ready,R.id.btn_new_car_done,
                R.id.btn_end_service})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_start_service:
                startService();//开启检测服务
                break;
            case R.id.btn_new_car_ready:

                newCar(ParamValue.NEW_CAR_READY);//开始搬第一个行李至拖车
                break;
            case R.id.btn_new_car_done:

                newCar(ParamValue.NEW_CAR_DONE);//结束第一个行李搬运
                break;
            case R.id.btn_end_service:
                endService();//结束服务
                break;
        }
    }

    //********************按键方法********************************//

    //服务初始化
    private void interfaceServiceInit(){

        int rel = ResultState.FAIL;
        try {
            Log.d(TAG, "remote service init");
            rel = gModelInterface.initService(); //服务初始化
            gModelInterface.setCallback(mCallback);  //设置回调接口
        } catch (Exception e) {
            e.printStackTrace();
        }


        tvRunResult.setText("初始化结果" + ( rel == ResultState.SUCCESS ? "成功" : "失败"));

        Message toastMeg = Message.obtain();
        toastMeg.obj = "初始化结果" + ( rel == ResultState.SUCCESS ? "成功" : "失败");
        toastMeg.what = LOAD_SUCCESS;
        gHandler.sendMessage(toastMeg);

        //下发初始数据
        if (rel == ResultState.SUCCESS){
            sendCase();
            sendFlight();
        }



    }


    //下发场景数据
    private void sendCase(){

        JSONArray caseArray = new JSONArray();
        JSONObject caseObject = new JSONObject();
        caseObject.put(DataKey.J_CASE,mCase);
        caseObject.put(DataKey.J_FLIGHT_ID,mFlight);
        caseObject.put(DataKey.J_CURRENT_STATION,mStationName);
        caseObject.put(DataKey.J_STATION_TYPE,mStationType);
        caseArray.add(caseObject);

        String strData = caseArray.toString();
        Log.d(TAG,"the json data is " + strData);
        boolean result = false;
        disConnectToast();
        try {
            gModelInterface.sendInfo(ParamValue.DATA_CASE, strData);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvRunResult.setText("下发场景数据 " + (result ? "成功" : "失败"));

    }


    //下发所有航班数据
    private void sendFlight(){

        String strData = testDataJA.toString();
        Log.d(TAG,"the json data is " + strData);
        boolean result = false;
        disConnectToast();
        try {
            gModelInterface.sendInfo(ParamValue.DATA_BOX, strData);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvRunResult.setText("下发所有行李数据 " + (result ? "成功" : "失败"));

    }


    //清除第一组航班的数据
    private void clearFlight(){

        JSONObject jsonObject = testDataJA.getJSONObject(0);

        JSONObject clearObject = new JSONObject();
        clearObject.put(DataKey.J_FLIGHT_ID,jsonObject.getString(DataKey.J_FLIGHT_ID));

        JSONArray clearArray = new JSONArray();
        clearArray.add(clearObject);
        String strData = clearArray.toString();
        boolean result = false;

        disConnectToast();
        try {
            gModelInterface.clearInfo(strData);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvRunResult.setText("清除"+  "1组数据 " + (result ? "成功" : "失败"));

    }

    //清除所有数据
    private void clearAll(){

        boolean result = false;
        disConnectToast();
        try {
            gModelInterface.clearInfo(ParamValue.CLEAR_ALL);
            result =true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvRunResult.setText("清除所有数据 " + (result ? "成功" : "失败"));
    }




    //开启检测
    private void startService(){

        int rel = 0;
        disConnectToast();
        try {
            rel = gModelInterface.startService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvRunResult.setText("开启服务结果: " +  ( rel == ResultState.SUCCESS ? "成功" : "失败") );


    }


    //发送行为数据
    private void newCar(String state){
        JSONObject actionObject = new JSONObject();

        actionObject.put(DataKey.J_CAR_ID,edCarId.getText().toString());
        actionObject.put(DataKey.J_NEW_CAR,state);

        JSONArray actionArray = new JSONArray();
        actionArray.add(actionObject);


        String strData = actionArray.toString();
        boolean result = false;
        disConnectToast();
        try {
            gModelInterface.sendInfo(ParamValue.DATA_NEW_CAR,strData);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvRunResult.setText("发送行为数据 "  + state + (result ? "成功" : "失败"));
    }



    //结束检测
    private void endService(){

        int rel = 0;
        disConnectToast();
        try {
            rel = gModelInterface.stopService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvRunResult.setText("结束服务结果: " +  ( rel == ResultState.SUCCESS ? "成功" : "失败") );


    }

    //服务断开提醒
    private void disConnectToast(){
        if (gModelInterface == null){
            try {
                Toast.makeText(this,"服务已断开！",Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
    }



    //********************按键方法********************************//




    private static final String CSV_FLIE = "/sdcard/myLuggage/flight_data.json";
    private SoundPool soundPool;
    private SoundPool soundPool2;
    //初始化测试用例
    private void initData(){
//        String dataStr = FileUtil.readJsonFile(this,R.raw.flight_data);
        String dataStr = FileUtil.readJsonFile(CSV_FLIE);
        testDataJA = JSON.parseArray(dataStr);


        mFlight = getIntent().getStringExtra(Constants.SP_KEY_FLIGHT);
        mCase = getIntent().getStringExtra(Constants.SP_KEY_CASE);
        mStationName = getIntent().getStringExtra(Constants.SP_KEY_STATION_NAME);
        mStationType = getIntent().getStringExtra(Constants.SP_KEY_STATION_TYPE);


        //初始化蜂鸣器
        soundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);
        soundPool.load(this, R.raw.beep51, 1);

        //初始化蜂鸣器2
        soundPool2 = new SoundPool(10, AudioManager.STREAM_RING, 5);
        soundPool2.load(this, R.raw.beep, 1);




    }


    /**
     * 发出警报
     */
    private int performSound(boolean ifLoop){
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 获取最大音量值
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_RING);
        // 不断获取当前的音量值
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_RING);
        //最终影响音量
//        float volumnRatio = audioCurrentVolumn/audioMaxVolumn;
        float volumnRatio = 1.0f;
        if (ifLoop){
            return soundPool.play(1, volumnRatio, volumnRatio, 0, -1, 1);
        }
        else{
            return soundPool2.play(1, volumnRatio, volumnRatio, 0, 0, 1);
        }

    }

    //初始化spinner与recycleView
    private void initView(){

        tvCurrentCase.setText(mCase+"," + mFlight + ","  + mStationName + "," + mStationType);

        //初始化recycleView
        myRVAdapter = new MyRVAdapter(this,gDataList);
        rvEpc.setAdapter(myRVAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rvEpc.setLayoutManager(linearLayoutManager);


    }










    //----------------------------控件相关--------------------------//


    private static final int UPDATE_VIEW = 1;
    private static final int LOAD_SUCCESS = 2;

    private MyHandler gHandler = new MyHandler(this);

    /**
     * 静态Handler
     */
    static class MyHandler extends Handler {

        private SoftReference<MainActivity> mySoftReference;

        public MyHandler(MainActivity mainActivity) {
            this.mySoftReference = new SoftReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg){
            final MainActivity mainActivity = mySoftReference.get();
            String str = (String) msg.obj;
            switch (msg.what) {

                case UPDATE_VIEW:
                    mainActivity.myRVAdapter.notifyDataSetChanged();
                    mainActivity.rvEpc.scrollToPosition(mainActivity.gDataList.size()-1);

                    break;
                case LOAD_SUCCESS:

                    Toast.makeText(mainActivity,str,Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }




}
