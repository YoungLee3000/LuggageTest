package com.nlscan.android.luggagetest;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nlscan.luggage.DataKey;
import com.nlscan.luggage.IJudgeCallback;
import com.nlscan.luggage.LuggageManager;
import com.nlscan.luggage.ModelInterface;
import com.nlscan.luggage.ParamValue;
import com.nlscan.luggage.ResultState;

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                    String flight_id = ValueUtil.TextGet(relObj.getString(DataKey.J_FLIGHT_ID));
                    String box_id = ValueUtil.TextGet(relObj.getString(DataKey.J_BOX_ID)) ;
                    String epc_id =  ValueUtil.TextGet(relObj.getString(DataKey.J_EPC_ID)) ;
                    String box_state = ValueUtil.TextGet(relObj.getString(DataKey.J_PREDICT_BOX_STATE)) ;
                    String car_id = ValueUtil.TextGet(relObj.getString(DataKey.J_CAR_ID));

//                    if(box_id.length()>=4) box_id = "**"+box_id.substring(box_id.length()-4);
//                    if(epc_id.length()>=4) epc_id = "**"+epc_id.substring(epc_id.length()-4);
                    map.put(Constants.RV_HEAD_EPC,epc_id);


                    if (gDataMap.containsKey(epc_id)){
                        mCurrentData = (ResultItems) gDataMap.get(epc_id);
                    }
                    else{
                        mCurrentData = new ResultItems();
                    }

                    String hugState = ValueUtil.TextGet(mCurrentData.getHugState()) ;
                    String layState = ValueUtil.TextGet(mCurrentData.getLayState()) ;

                    String stateParse = "";
                    switch (box_state){
                        case ResultState.PREDICT_BOX_CLOSE:
                            stateParse = getResources().getString(R.string.predict_box_close);

                            break;
                        case ResultState.PREDICT_BOX_CARRY_RIGHT:
                            stateParse = getResources().getString(R.string.predict_box_carry_right);
//                            performSound(2);
                            hugState = getResources().getString(R.string.right);

                            if ("AB1000".equals(flight_id)){
                                performSound(4);
                            }
                            else if ("AB2000".equals(flight_id)){
                                performSound(5);
                            }

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showDialog("",  ("AB1000".equals(flight_id) ? "北京     " + flight_id  : "上海    " + flight_id ),"取消 搬运", box_id,ParamValue.CONFIRM_CANCEL,false);
                                }
                            });

                            break;
                        case ResultState.PREDICT_BOX_CARRY_WRONG:
                            stateParse = getResources().getString(R.string.predict_box_carry_wrong);

                            hugState = getResources().getString(R.string.wrong);
                            break;
                        case ResultState.PREDICT_BOX_LAY_RIGHT:
                            if(gAlertDialog!=null) gAlertDialog.dismiss();
                            stateParse = getResources().getString(R.string.predict_box_lay_right);
                            layState = getResources().getString(R.string.right);
                            performSound(3);

                            break;
                        case ResultState.PREDICT_BOX_LAY_WRONG:
                            stateParse = getResources().getString(R.string.predict_box_lay_wrong);
                            layState = getResources().getString(R.string.wrong);
                            wrongNotify();
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showDialog("确认数据","将行李放置此处?","取消",  box_id,ParamValue.CONFIRM_WRONG,true);
                                }
                            });
                            break;
                        case ResultState.PREDICT_BOX_BAN:
                            stateParse = getResources().getString(R.string.predict_box_ban);
                            hugState = getResources().getString(R.string.wrong);
                            wrongNotify();
                            break;
                        case ResultState.PREDICT_BOX_ACCESS:
                            stateParse = getResources().getString(R.string.predict_box_access);
                            hugState = getResources().getString(R.string.right);
                            performSound(2);
                            break;
                        case ResultState.PREDICT_BOX_LACK:
                            stateParse = getResources().getString(R.string.predict_box_lack);
                            hugState = getResources().getString(R.string.predict_box_lack);
                            performSound(2);
                            break;
                    }
                    if ("".equals(stateParse)) continue;



                    mCurrentData = new ResultItems(epc_id,box_id,hugState,layState,car_id);

                    gDataMap.put(epc_id,mCurrentData);




                    map.put(Constants.RV_HEAD_BOX_STATE,stateParse);
                    map.put(Constants.RV_HEAD_CAR,car_id);



                    gDataList.add(map);

                    gHandler.sendEmptyMessage(UPDATE_VIEW);

                }
            }

        }
    };

    //更新当前标签信息
    private void updateResultView(){
        tvBoxAndEpc.setText( mCurrentData.getEpcId() );
        tvInHugTip.setText("");
        tvInHugState.setText("");
        tvInLayTip.setText("");
        tvInLayState.setText("");

        String hugState = mCurrentData.getHugState();
        String layState = mCurrentData.getLayState();

        if (!"".equals(hugState)){
            tvInHugTip.setText(getString(R.string.in_hug));
            tvInHugState.setText(hugState);

            if (getString(R.string.right).equals(hugState)){
                tvInHugState.setTextColor(getResources().getColor(R.color.green));
            }
            else if (getString(R.string.wrong).equals(hugState)){
                tvInHugState.setTextColor(getResources().getColor(R.color.red));
            }
        }
        else{
            tvInHugTip.setText("");
        }



        if (!"".equals(layState)){
            tvInLayTip.setText(getString(R.string.in_lay));
            tvInLayState.setText(layState);

            if (getString(R.string.right).equals(layState)){
                tvInLayState.setTextColor(getResources().getColor(R.color.green));
            }
            else if (getString(R.string.wrong).equals(layState)){
                tvInLayState.setTextColor(getResources().getColor(R.color.red));
            }
        }
        else{
            tvInLayTip.setText("");
        }

        tvCarId.setText("拖车号: \n" + mCurrentData.getCarId());



    }


    private void wrongNotify(){
        final int playId =    performSound(1);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                soundPool.stop(playId);
            }
        },1000);
    }

    private boolean gBindState = false;//服务是否绑定成功


    //初始化服务
    private LuggageManager gLuggageInstance;
    private void initService(){

        //设置扫码配置
        barcodeSet();


        //获取服务实例
        gLuggageInstance = LuggageManager.getInstance(getApplicationContext());


        interfaceServiceInit();









    }






    //上一个界面传来的值
    private String mFlight = "";
    private String mStationName = "";
    private String mStationType = "";
    private String mCase = "";


    private String mFlightStr = "";
    private String mStationNameStr = "";
    private String mStationTypeStr = "";
    private String mCaseStr = "";

    //界面创建时绑定服务
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);





        initData();
        initView();

        showLoadingWindow("服务开启中...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                initService();
            }
        }).start();






    }

    //界面销毁时解除服务
    @Override
    protected void onDestroy() {
        super.onDestroy();
        endService();

    }


    //暂停时销毁广播
    @Override
    protected void onPause() {
        super.onPause();

        unRegister();
    }


    //刷新界面时注册广播
    @Override
    protected void onPostResume() {
        super.onPostResume();
        register();

    }


    //返回时询问是否清除所以数据
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showDialog("清除数据","是否清除数据", Color.RED);
        }

        return false;
    }





//    AlertDialog gAlertDialog;
    CustomTextDialog gAlertDialog;
    /**
     * 显示弹出窗
     * @param meg
     */
    private void showDialog(String title,String meg,int colorID){

        if (gAlertDialog != null) gAlertDialog.dismiss();



        gAlertDialog = new CustomTextDialog(this);
        gAlertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        gAlertDialog.setTile(title);
        gAlertDialog.setMessage(meg);
        gAlertDialog.setOnSureListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
                gAlertDialog.dismiss();
                MainActivity.this.finish();
            }
        });
        gAlertDialog.setOnCanlceListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finish();
            }
        });
//        gAlertDialog.setTile("请输入拖车号");
        gAlertDialog.show();


    }







    /**
     * 搬运错误或者携带正确的时候弹窗,
     * @param meg
     */
    private void showDialog(String title,String meg,String cancelText,String boxId, String negValue,boolean ifShowPos){
        if (gAlertDialog != null) gAlertDialog.dismiss();


        JSONObject actionObject = new JSONObject();
        actionObject.put(DataKey.J_BOX_ID,boxId);

        gAlertDialog = new CustomTextDialog(this);
        gAlertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        gAlertDialog.setTile(title);
        gAlertDialog.setMessage(meg);
        gAlertDialog.setButtonText(cancelText);

        if (ifShowPos){
            gAlertDialog.setOnSureListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionObject.put(DataKey.J_CONFIRM,ParamValue.CONFIRM_RIGHT);
                    sendConfirmData(actionObject,boxId);
                    gAlertDialog.dismiss();
                }
            });
        }
        else{
            gAlertDialog.setButtonVisible(false);
        }

        gAlertDialog.setOnCanlceListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionObject.put(DataKey.J_CONFIRM,negValue);
                sendConfirmData(actionObject,boxId);
                gAlertDialog.dismiss();
            }
        });
//        gAlertDialog.setTile("请输入拖车号");
        gAlertDialog.show();










    }


    //设置标题与消息内容
//    private void setTitle(AlertDialog.Builder builder, String title,String meg, int color){
//        gAlertDialog = builder.create();
//
//        gAlertDialog.setTitle(title);
//
//        gAlertDialog.setMessage(meg);
//        gAlertDialog.show();
//        gAlertDialog.getWindow().setGravity(Gravity.CENTER);
//        gAlertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
//        try {
//            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
//            mAlert.setAccessible(true);
//            Object mAlertController = mAlert.get(gAlertDialog);
//
//
//            //获取mButton并设置大小颜色----------------
//            Field mPos = mAlertController.getClass().getDeclaredField("mButtonPositive");
//            mPos.setAccessible(true);
//            Button mButtonPos = (Button) mPos.get(mAlertController);
//            mButtonPos.setTextSize(25);
//
//            LinearLayout.LayoutParams posBtnPara = (LinearLayout.LayoutParams) mButtonPos.getLayoutParams();
////            posBtnPara.height = LinearLayout.LayoutParams.WRAP_CONTENT;
////            posBtnPara.width = LinearLayout.LayoutParams.MATCH_PARENT;
////            posBtnPara.gravity = Gravity.CENTER;
//            posBtnPara.setMargins(0, 0, 50, 20);
//            mButtonPos.setLayoutParams(posBtnPara);
//
//
//            //获取mButton并设置大小颜色-------------
//            Field mNeg = mAlertController.getClass().getDeclaredField("mButtonNegative");
//            mNeg.setAccessible(true);
//            Button mButtonNeg = (Button) mNeg.get(mAlertController);
//            mButtonNeg.setTextSize(25);
//
//            LinearLayout.LayoutParams cancelBtnPara = (LinearLayout.LayoutParams) mButtonNeg.getLayoutParams();
////            cancelBtnPara.height = LinearLayout.LayoutParams.WRAP_CONTENT;
////            cancelBtnPara.width = LinearLayout.LayoutParams.MATCH_PARENT;
////            cancelBtnPara.gravity = Gravity.CENTER;
//            cancelBtnPara.setMargins(0, 0, 200, 20);
//            mButtonNeg.setLayoutParams(cancelBtnPara);
//
//
//
//
//            //设置消息的字体大小
//            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
//            mMessage.setAccessible(true);
//            TextView mMessageView = (TextView) mMessage.get(mAlertController);
//            mMessageView.setTextColor(color);
//            mMessageView.setTextSize(25);
//            mMessageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        }
//    }


    //发送行为数据
    private void sendConfirmData(JSONObject actionObject,String epcId){
        JSONArray actionArray = new JSONArray();
        actionArray.add(actionObject);
        String strData = actionArray.toString();
        int rel = gLuggageInstance.sendInfo(ParamValue.DATA_CONFIRM,strData);
        tvRunResult.setText("发送确认数据 "  + epcId + (rel==ResultState.SUCCESS ? "成功" : "失败"));
    }







    //*-------------服务连接相关-------------------------//


    private static final String TAG = "LuggageService";






    //----------------------------控件相关--------------------------//

    @BindView(R.id.tv_run_result)
    TextView tvRunResult;

    @BindView(R.id.tv_current_case)
    TextView tvCurrentCase;


    @BindView(R.id.btn_start_service)
    Button btnStartService;

    @BindView(R.id.btn_show_record)
    Button btnShowRecord;

    @BindView(R.id.btn_new_car_done)
    Button btnNewCarDone;

    @BindView(R.id.btn_end_service)
    Button btnEndService;

    @BindView(R.id.ll_history)
    LinearLayout layoutHistory;

    @BindView(R.id.btn_close_record)
    Button btnCloseRecord;

    @BindView(R.id.btn_add_lack)
    Button btnAddLack;


    @BindView(R.id.tv_car_id_set)
    TextView tvCarIdSet;

    @BindView(R.id.tv_box_and_epc)
    TextView tvBoxAndEpc;

    @BindView(R.id.tv_car_id)
    TextView tvCarId;

    @BindView(R.id.tv_in_hug_tip)
    TextView tvInHugTip;

    @BindView(R.id.tv_in_lay_tip)
    TextView tvInLayTip;

    @BindView(R.id.tv_in_hug_state)
    TextView tvInHugState;

    @BindView(R.id.tv_in_lay_state)
    TextView tvInLayState;



    @BindView(R.id.rv_epc_data)
    RecyclerView rvEpc;
    private List<Map<String,String>> gDataList = new ArrayList<>();
    private MyRVAdapter myRVAdapter;
    private ResultItems mCurrentData = new ResultItems();
    private Map<String,ResultItems> gDataMap = new HashMap<>();
    private Set<String> mCarIdSet = new HashSet<>();



    //测试用例
    private JSONArray testDataJA;

    //按键事件
    @OnClick({R.id.btn_start_service,R.id.btn_new_car_done,
                R.id.btn_end_service,R.id.btn_show_record,R.id.btn_close_record,
                R.id.btn_add_lack})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_start_service:
                startService();//开启检测服务
                btnStartService.setEnabled(false);
                btnEndService.setEnabled(true);
                break;

            case R.id.btn_new_car_done:

                showDialog();
                break;
            case R.id.btn_end_service:
                endService();//结束服务
                btnStartService.setEnabled(true);
                btnEndService.setEnabled(false);
                break;
            case R.id.btn_show_record:

                layoutHistory.setVisibility(View.VISIBLE);

                break;
            case R.id.btn_close_record:
                layoutHistory.setVisibility(View.GONE);
                break;
            case R.id.btn_add_lack:
                addLack();
//                performSound(false);
                break;
        }
    }

    //********************按键方法********************************//

    //服务初始化
    private void interfaceServiceInit(){

        int rel = gLuggageInstance.initService();

//        if (rel == ResultState.FAIL){
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            rel = gLuggageInstance.initService();
//        }


//        tvRunResult.setText("初始化结果" + ( rel == ResultState.SUCCESS ? "成功" : "失败"));

        Message toastMeg = Message.obtain();
        toastMeg.obj = "4次初始化结果" + ( rel == ResultState.SUCCESS ? "成功" : "失败");
        toastMeg.what = LOAD_SUCCESS;
        gHandler.sendMessage(toastMeg);

        //初始化成功后开始设置回调接口、下发场景数据、航班数据
        if (rel == ResultState.SUCCESS){
            gLuggageInstance.setCallback(mCallback);
            sendCase();
            sendFlight();

            gHandler.sendEmptyMessage(CLICK_START);
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
        Log.d(TAG,"the case json data is " + strData);
        int rel = gLuggageInstance.sendInfo(ParamValue.DATA_CASE,strData);

        tvRunResult.setText("下发场景数据 " + (rel==ResultState.SUCCESS ? "成功" : "失败"));

    }


    //下发所有航班数据
    private void sendFlight(){

        String strData = testDataJA.toString();
        Log.d(TAG,"the json data is " + strData);
        int rel = gLuggageInstance.sendInfo(ParamValue.DATA_BOX,strData);


        tvRunResult.setText("下发所有行李数据 " + (rel==ResultState.SUCCESS ? "成功" : "失败"));

    }


    //清除第一组航班的数据
    private void clearFlight(){

        JSONObject jsonObject = testDataJA.getJSONObject(0);

        JSONObject clearObject = new JSONObject();
        clearObject.put(DataKey.J_FLIGHT_ID,jsonObject.getString(DataKey.J_FLIGHT_ID));

        JSONArray clearArray = new JSONArray();
        clearArray.add(clearObject);
        String strData = clearArray.toString();

        int rel = gLuggageInstance.clearInfo(strData);

        tvRunResult.setText("清除"+  "1组数据 " + (rel==ResultState.SUCCESS ? "成功" : "失败"));

    }

    //清除所有数据
    private void clearAll(){



        int rel = gLuggageInstance.clearInfo(ParamValue.CLEAR_ALL);



        tvRunResult.setText("清除所有数据 " + (rel==ResultState.SUCCESS ? "成功" : "失败"));
    }




    //开启检测
    private void startService(){

        int rel = gLuggageInstance.startService();

        tvRunResult.setText("开启服务结果: " +  ( rel == ResultState.SUCCESS ? "成功" : "失败") );


    }


    //发送行为数据
    private void newCar(String carId,String state){
        JSONObject actionObject = new JSONObject();

        actionObject.put(DataKey.J_CAR_ID,carId);
        actionObject.put(DataKey.J_NEW_CAR,state);

        JSONArray actionArray = new JSONArray();
        actionArray.add(actionObject);


        String strData = actionArray.toString();

        int rel = gLuggageInstance.sendInfo(ParamValue.DATA_NEW_CAR,strData);

        tvRunResult.setText("发送行为数据 "  + state + (rel==ResultState.SUCCESS ? "成功" : "失败"));
    }


    //显示设置拖车号的对话框
    public void showDialog() {
        CustomEditTextDialog customDialog = new CustomEditTextDialog(this);
        EditText editText = (EditText) customDialog.getEditText();//方法在CustomDialog中实现
        customDialog.setOnSureListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String carID = editText.getText().toString();
                mCarIdSet.add(carID);
                String carIDSets = "";
                for (String item: mCarIdSet){
                    carIDSets += item;
                    carIDSets += " ";
                }
                tvCarIdSet.setText(carIDSets);
                newCar(carID,ParamValue.NEW_CAR_DONE);
                Log.d(TAG,"the car id is " + carID);
                customDialog.dismiss();
            }
        });
        customDialog.setOnCanlceListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });
        customDialog.setTile("请输入拖车号");
        customDialog.show();
    }


    //结束检测
    private void endService(){


        int rel = gLuggageInstance.stopService();

        tvRunResult.setText("结束服务结果: " +  ( rel == ResultState.SUCCESS ? "成功" : "失败") );


    }


    //发送扫码广播
    private void addLack(){
        //普通广播
        Intent intent1 = new Intent("nlscan.action.SCANNER_TRIG");
        intent1.putExtra("SCAN_TIMEOUT", 6);//单位为秒，值为int类型，且不超过9秒
        sendBroadcast(intent1);//content.
    }


    //扫码接收广播
    private BroadcastReceiver mScanReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            final String scanResult_1=intent.getStringExtra("SCAN_BARCODE1");
            final String scanStatus=intent.getStringExtra("SCAN_STATE");

            if("ok".equals(scanStatus)){

                sendAddLackAction(scanResult_1);
            }

        }
    };


    //发送补签行为数据
    private void sendAddLackAction(String boxId){
        JSONObject actionObject = new JSONObject();

        actionObject.put(DataKey.J_BOX_ID,boxId);

        JSONArray actionArray = new JSONArray();
        actionArray.add(actionObject);

        String strData = actionArray.toString();

        int rel = gLuggageInstance.sendInfo(ParamValue.DATA_ADD_LACK,strData);

        tvRunResult.setText("发送补签数据 "  + boxId + (rel==ResultState.SUCCESS ? "成功" : "失败"));
    }


    //注册广播
    private void register(){

        //扫码广播
        IntentFilter scanFilter = new IntentFilter("nlscan.action.SCANNER_RESULT");
        registerReceiver(mScanReceiver,scanFilter);

    }


    //取消注册
    private void unRegister(){
        try {

            unregisterReceiver(mScanReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //设置扫码配置
    private void barcodeSet(){
        Intent intentConfig = new Intent("ACTION_BAR_SCANCFG");
        intentConfig.putExtra("EXTRA_SCAN_MODE", 3);//广播输出
        intentConfig.putExtra("EXTRA_OUTPUT_EDITOR_ACTION_ENABLE", 0);//不输出软键盘
        sendBroadcast(intentConfig);
    }





    //********************按键方法********************************//




    private static final String CSV_FILE_1 = "/sdcard/myLuggage/flight_data_1.json";
    private static final String CSV_FILE_2 = "/sdcard/myLuggage/flight_data_2.json";
    private static final String CSV_FILE_3 = "/sdcard/myLuggage/flight_data_3.json";
    private static final String CSV_FILE_3_1 = "/sdcard/myLuggage/flight_data_3_1.json";
    private static final String CSV_FILE_4 = "/sdcard/myLuggage/flight_data_4.json";

    private SoundPool soundPool;
    private SoundPool soundPool2;
    private SoundPool soundPool3;
    private SoundPool soundPool4;
    private SoundPool soundPool5;
    private Vibrator mVibrator;
    private long[] mPattern = {0,500};
    private long[] mPatternLong = {0,500,50,500,50,500};

    //初始化测试用例
    private void initData(){




        mFlight = getIntent().getStringExtra(Constants.SP_KEY_FLIGHT);
        mCase = getIntent().getStringExtra(Constants.SP_KEY_CASE);
        mStationName = getIntent().getStringExtra(Constants.SP_KEY_STATION_NAME);
        mStationType = getIntent().getStringExtra(Constants.SP_KEY_STATION_TYPE);


        mFlightStr = "航班号: " + mFlight;
        mStationNameStr = "站点: " + mStationName;

        String dataStr = "";
        switch (mCase){
            default:
            case ParamValue.CASE_DEFAULT:
                dataStr = FileUtil.readJsonFile(CSV_FILE_1);
                mCaseStr = "当前场景: "  +  getString(R.string.case_1);
                break;
            case ParamValue.CASE_CAR_TO_STORE:
                dataStr = FileUtil.readJsonFile(CSV_FILE_2);
                mCaseStr = "当前场景: "  +  getString(R.string.case_2);
                break;
            case ParamValue.CASE_STORE_TO_CAR:
                dataStr = FileUtil.readJsonFile(CSV_FILE_3);
                mCaseStr = "当前场景: "  +  getString(R.string.case_3);
                break;
            case ParamValue.CASE_STORE_SEARCH:
                dataStr = FileUtil.readJsonFile(CSV_FILE_3_1);
                mCaseStr = "当前场景: "  +  getString(R.string.case_3_1);
                break;
            case ParamValue.CASE_CAR_TO_BAND :
                dataStr = FileUtil.readJsonFile(CSV_FILE_4);
                mCaseStr = "当前场景: "  +  getString(R.string.case_4);
                break;
        }
        switch (mStationType){
            default:
            case ParamValue.STATION_START:
                mStationTypeStr ="站点类型: "  + getString(R.string.station_start);
                break;
            case ParamValue.STATION_MIDDLE:
                mStationTypeStr ="站点类型: "  + getString(R.string.station_middle);
                break;
            case ParamValue.STATION_DEST:
                mStationTypeStr = "站点类型: "  + getString(R.string.station_dest);
                break;
        }
        testDataJA = JSON.parseArray(dataStr);


        if (!ParamValue.CASE_DEFAULT.equals(mCase)){
            btnNewCarDone.setVisibility(View.GONE);
        }

        //初始化蜂鸣器
        soundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);
        soundPool.load(this, R.raw.beep51, 1);

        //初始化蜂鸣器2
        soundPool2 = new SoundPool(10, AudioManager.STREAM_RING, 5);
        soundPool2.load(this, R.raw.beep91, 1);


        //初始化蜂鸣器3
        soundPool3 = new SoundPool(10, AudioManager.STREAM_RING, 5);
        soundPool3.load(this, R.raw.beep, 1);


        //初始化蜂鸣器4
        soundPool4 = new SoundPool(10, AudioManager.STREAM_RING, 5);
        soundPool4.load(this, R.raw.beijing, 1);

        //初始化蜂鸣器5
        soundPool5 = new SoundPool(10, AudioManager.STREAM_RING, 5);
        soundPool5.load(this, R.raw.shanghai, 1);


        //初始化振动
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


    }


    /**
     * 发出警报
     */
    private int performSound(int type){
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 获取最大音量值
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_RING);
        // 不断获取当前的音量值
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_RING);
        //最终影响音量
        float volumnRatio = audioCurrentVolumn/audioMaxVolumn;

        if (type == 1){
            mVibrator.vibrate(mPatternLong,-1);
            return soundPool.play(1, volumnRatio, volumnRatio, 0, -1, 1);
        }
        else if (type == 2){
            mVibrator.vibrate(mPattern,-1);
            return soundPool2.play(1, 1.0f, 1.0f, 0, 0, 1.3f);
        }
        else if (type == 3){
            mVibrator.vibrate(mPattern,-1);
            return soundPool3.play(1, 1.0f, 1.0f, 0, 0, 1);
        }
        else if (type == 4){
            mVibrator.vibrate(mPattern,-1);
            return soundPool4.play(1, 1.0f, 1.0f, 0, 0, 1);
        }
        else if (type == 5){
            mVibrator.vibrate(mPattern,-1);
            return soundPool5.play(1, 1.0f, 1.0f, 0, 0, 1);
        }
        else{
            return 0;
        }


    }

    //初始化spinner与recycleView
    private void initView(){

        tvCurrentCase.setText(mCaseStr);

        //初始化recycleView
        myRVAdapter = new MyRVAdapter(this,gDataList);
        rvEpc.setAdapter(myRVAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rvEpc.setLayoutManager(linearLayoutManager);


    }


    private ProgressDialog mDialog;
    /**
     * 显示进度条
     * @param message
     */
    protected void showLoadingWindow(String message)
    {


        if(isDialogShow())
            return ;

        mDialog = new ProgressDialog(this) ;
        mDialog.setProgressStyle(ProgressDialog.BUTTON_NEUTRAL);// 设置进度条的形式为圆形转动的进度条
        mDialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        mDialog.setCanceledOnTouchOutside(true);// 设置在点击Dialog外是否取消Dialog进度条
        // 设置提示的title的图标，默认是没有的，如果没有设置title的话只设置Icon是不会显示图标的

        mDialog.setMessage(message);
        mDialog.show();

        View v = mDialog.getWindow().getDecorView();
        setDialogText(v);
    }

    private boolean isDialogShow(){
        return mDialog != null && mDialog.isShowing();
    }

    //设置其字体大小
    private void setDialogText(View v){
        if(v instanceof ViewGroup){
            ViewGroup parent=(ViewGroup)v;
            int count=parent.getChildCount();
            for(int i=0;i<count;i++){
                View child=parent.getChildAt(i);
                setDialogText(child);
            }
        }else if(v instanceof TextView){
            ((TextView)v).setTextSize(22);
        }
    }

    protected void  cancelDialog(){
        if (mDialog != null){
            mDialog.dismiss();
        }
    }










    //----------------------------控件相关--------------------------//


    private static final int UPDATE_VIEW = 1;
    private static final int LOAD_SUCCESS = 2;
    private static final int CLICK_START = 3;

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
                    mainActivity.updateResultView();

                    break;
                case LOAD_SUCCESS:
                    mainActivity.cancelDialog();
                    Toast.makeText(mainActivity,str,Toast.LENGTH_SHORT).show();
                    break;
                case CLICK_START:
                    mainActivity.btnNewCarDone.setEnabled(true);
                    mainActivity.btnAddLack.setEnabled(true);
                    mainActivity.btnStartService.performClick();
                    break;
            }

        }
    }




}
