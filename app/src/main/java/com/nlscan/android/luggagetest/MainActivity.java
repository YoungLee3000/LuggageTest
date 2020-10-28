package com.nlscan.android.luggagetest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nlscan.luggage.IJudgeCallback;
import com.nlscan.luggage.ModelInterface;
import com.nlscan.luggage.ResultState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {


    //*-------------服务连接相关-------------------------//

    private IJudgeCallback.Stub mCallback = new IJudgeCallback.Stub() {
        @Override
        public void onJudgeResult(String s) throws RemoteException {
            JSONArray resultArray = JSON.parseArray(s);

            if (resultArray != null){


                for(Object object : resultArray){
                    JSONObject relObj = (JSONObject) object;
                    Map<String,String> map = new HashMap<>();
                    String box_id = relObj.getString(Constants.J_TARGET_BOX_ID);
                    String epc_id = relObj.getString(Constants.J_TARGET_EPC_ID);
                    String box_state = relObj.getString(Constants.J_TARGET_BOX_STATE);
                    String epcArrayStr = relObj.getString(Constants.JA_EPC_ARRAY);

                    if (box_id!=null && epc_id!=null){
                        if(box_id.length()>=4) box_id = "**"+box_id.substring(box_id.length()-4);
                        if(epc_id.length()>=4) epc_id = "**"+epc_id.substring(epc_id.length()-4);
                        map.put(Constants.RV_HEAD_EPC,epc_id+","+box_id);
                    }
                    if (box_state != null){
                        box_state = (box_state.equals(ResultState.PREDICT_BOX_LAY)?
                                "已放置" : "正在靠近");
                        map.put(Constants.RV_HEAD_BOX_STATE,box_state);
                    }
                    if(epcArrayStr != null){
                        JSONArray epc_array = JSON.parseArray(epcArrayStr);
                        if (epc_array != null){
                            StringBuilder sb = new StringBuilder();

                            for(Object object2 : epc_array){
                                JSONObject epcObject = (JSONObject) object2;
                                sb.append(epcObject.getString(Constants.J_EPC_ID));
                                sb.append(",");
                                sb.append(epcObject.getString(Constants.J_RSSI));
                                sb.append("\n");
                            }
                            map.put(Constants.RV_HEAD_AROUND,sb.toString());
                        }

                    }

                   gDataList.add(map);
                   myRVAdapter.notifyDataSetChanged();
                   rvEpc.scrollToPosition(gDataList.size()-1);
                }
            }

        }
    };

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

    }

    private class  LuggageServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            gModelInterface = ModelInterface.Stub.asInterface(service);
            gBindState = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            gBindState = false;
            gModelInterface = null;
        }
    }

    //界面创建时绑定服务
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
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

    @BindView(R.id.tv_run_result)
    private TextView tvRunResult;

    @BindView(R.id.btn_init)
    private Button btnInit;

    @BindView(R.id.btn_send_flight)
    private Button btnSendFlight;

    @BindView(R.id.btn_clear_flight)
    private Button btnClearFlight;

    @BindView(R.id.btn_start_service)
    private Button btnStartService;

    @BindView(R.id.btn_new_car_ready)
    private Button btnNewCarReady;

    @BindView(R.id.btn_new_car_done)
    private Button btnNewCarDone;

    @BindView(R.id.btn_end_service)
    private Button btnEndService;

    @BindView(R.id.rv_epc)
    private RecyclerView rvEpc;
    private List<Map<String,String>> gDataList = new ArrayList<>();
    private MyRVAdapter myRVAdapter;

    @BindView(R.id.sp_clear_flight)
    private Spinner spClearFlight;

    @BindView(R.id.sp_send_action)
    private Spinner spSendAction;

    private ArrayAdapter<String> aaFlightSet;
    private String[] strFlightSet;


    //测试用例
    private JSONArray testDataJA;

    //按键事件
    @OnClick({R.id.btn_init, R.id.btn_send_flight,R.id.btn_clear_flight,
                R.id.btn_start_service,R.id.btn_new_car_ready,R.id.btn_new_car_done,
                R.id.btn_end_service})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_init:
                serviceInit();//初始化服务
                break;
            case R.id.btn_send_flight:
                sendFlight();//下发所有航班的行李信息
                break;
            case R.id.btn_clear_flight:
                clearFlight();//清除指定航班的行李信息
                break;
            case R.id.btn_start_service:
                startService();//开启检测服务
                break;
            case R.id.btn_new_car_ready:
                newCarReady();//开始搬第一个行李至拖车
                break;
            case R.id.btn_new_car_done:
                newCarDone();//结束第一个行李搬运
                break;
            case R.id.btn_end_service:
                endService();//结束服务
                break;
        }
    }

    //***按键方法***//
    private void serviceInit(){

        int rel = ResultState.FAIL;
        try {
            rel = gModelInterface.initService(); //服务初始化
            gModelInterface.setCallback(mCallback);  //设置回调接口
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        tvRunResult.setText("初始化结果" + ( rel == ResultState.SUCCESS ? "成功" : "失败"));

    }

    private void sendFlight(){

        String strData = testDataJA.toString();
        Log.d(TAG,"the json data is " + strData);
        try {
            gModelInterface.sendInfo(ResultState.DATA_BOX, strData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        tvRunResult.setText("下发所有行李数据");

    }

    private void clearFlight(){

        int index = spClearFlight.getSelectedItemPosition();

        JSONObject jsonObject = testDataJA.getJSONObject(index);

        JSONObject clearObject = new JSONObject();
        clearObject.put(Constants.J_FLIGHT_ID,jsonObject.getString(Constants.J_FLIGHT_ID));

        JSONArray clearArray = new JSONArray();
        clearArray.add(clearObject);
        String strData = clearArray.toString();
        try {
            gModelInterface.clearInfo(strData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        tvRunResult.setText("清除"+ (index+1) +  "组数据");

    }

    private void startService(){

        int rel = 0;
        try {
            rel = gModelInterface.startService();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        tvRunResult.setText("开启服务结果: " +  ( rel == ResultState.SUCCESS ? "成功" : "失败") );


    }

    private void newCarReady(){

        int index = spClearFlight.getSelectedItemPosition();

        JSONObject jsonObject = testDataJA.getJSONObject(index);

        JSONObject actionObject = new JSONObject();
        actionObject.put(Constants.J_FLIGHT_ID,jsonObject.getString(Constants.J_FLIGHT_ID));
        actionObject.put(Constants.J_FLIGHT_TIME,jsonObject.getString(Constants.J_FLIGHT_TIME));
        actionObject.put(Constants.J_FLIGHT_DEST,jsonObject.getString(Constants.J_FLIGHT_DEST));
        actionObject.put(Constants.J_ACTION,ResultState.ACTION_NEW_CAR_READY);

        JSONArray actionArray = new JSONArray();
        actionArray.add(actionObject);


        String strData = actionArray.toString();
        try {
            gModelInterface.sendInfo(ResultState.DATA_ACTION,strData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        tvRunResult.setText("开始为"+ (index+1) +  "组行李选拖车");

    }

    private void newCarDone(){

        int index = spClearFlight.getSelectedItemPosition();

        JSONObject jsonObject = testDataJA.getJSONObject(index);

        JSONObject actionObject = new JSONObject();
        actionObject.put(Constants.J_FLIGHT_ID,jsonObject.getString(Constants.J_FLIGHT_ID));
        actionObject.put(Constants.J_FLIGHT_TIME,jsonObject.getString(Constants.J_FLIGHT_TIME));
        actionObject.put(Constants.J_FLIGHT_DEST,jsonObject.getString(Constants.J_FLIGHT_DEST));
        actionObject.put(Constants.J_ACTION,ResultState.ACTION_NEW_CAR_DONE);

        JSONArray actionArray = new JSONArray();
        actionArray.add(actionObject);


        String strData = actionArray.toString();
        try {
            gModelInterface.sendInfo(ResultState.DATA_ACTION,strData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        tvRunResult.setText("结束"+ (index+1) +  "组行李拖车选择");



    }

    private void endService(){

        int rel = 0;
        try {
            rel = gModelInterface.stopService();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        tvRunResult.setText("结束服务结果: " +  ( rel == ResultState.SUCCESS ? "成功" : "失败") );


    }

    //***按键方法***//




    //初始化测试用例
    private void initData(){
        String dataStr = FileUtil.readJsonFile(this,R.raw.flight_data);
        testDataJA = JSON.parseArray(dataStr);
        if (testDataJA != null){
            strFlightSet = new String[testDataJA.size()];
            for (int i=0; i<testDataJA.size(); i++){
                strFlightSet[i] =  (i+1) + "组数据" ;
            }
        }
    }

    //初始化spinner与recycleView
    private void initView(){

        //初始化清除数据选项
        aaFlightSet= new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, strFlightSet);
        aaFlightSet.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spClearFlight.setAdapter(aaFlightSet);
        spSendAction.setAdapter(aaFlightSet);

        //初始化recycleView
        myRVAdapter = new MyRVAdapter(this,gDataList);
        rvEpc.setAdapter(myRVAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rvEpc.setLayoutManager(linearLayoutManager);


    }










    //----------------------------控件相关--------------------------//







}
