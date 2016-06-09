package com.example.administrator.repo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.example.administrator.ble.BluetoothLeService;
import com.example.administrator.ble.DeviceControlService;
import com.example.administrator.ble.SampleGattAttributes;
import com.example.administrator.set.SetActivity;
import com.example.administrator.ustc_health.MainActivity;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by CaoRuijuan on 3/19/16.
 */
public class SleepMode {
    private Context context;

    private Handler mHandler = new Handler();
    Runnable runnable;
    byte[] p = new byte[4];//这个用来int转换byte的暂存数组
    final byte[] test = new byte[2];
    boolean istestxinlv = false, istestxinlv_open = false, istestxinlv_close = false;
    boolean checkflag2=false;
    boolean checkflag3=false;
    public static boolean openSleepModeSuccess = false;
    public static boolean closeSleepModeSuccess = false;

    public SleepMode(Context context){
        this.context = context;
    }

    //测试设备连接状况
    public boolean testConnect(){
        if (!DeviceControlService.mBluetoothLeService.initialize()) {//引用服务类的初始化
            Toast.makeText(context, "未初始化", Toast.LENGTH_LONG).show();
            return false;
        }
        if(!SampleGattAttributes.connectedState) {
            if (!DeviceControlService.mBluetoothLeService.connect(MainActivity.mDeviceAddress)) {
                Toast.makeText(context, "未连接蓝牙，不能开启", Toast.LENGTH_LONG).show();
                return false;
            }
            else {
                Toast.makeText(context, "已经连接", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(context, "已经连接", Toast.LENGTH_LONG).show();
        }

        //注册广播接收器
        context.registerReceiver(mGattUpdateReceiver01, makeGattUpdateIntentFilter());
        runnable = new Runnable() {
            @Override
            public void run() {
                //要做的事情
                System.out.println("判断中");
                if(SampleGattAttributes.lastrecall.compareTo(SampleGattAttributes.thisrecall)==0
                        &&SampleGattAttributes.connectedState){
                    DeviceControlService.mBluetoothLeService.disconnect();
                }//
                else
                    SampleGattAttributes.lastrecall=SampleGattAttributes.thisrecall;
                mHandler.postDelayed(this, 30000);
            }
        };

        return true;
    }

    //打开睡眠模式
    public void openSleepMode(){
        //心率设置为3分/次（20次/时)
        int measuretimes = 480;
        //写入蓝牙
        SampleGattAttributes.trim(p, measuretimes);
        byte[] data = {p[3], p[2]};
        DeviceControlService.mBluetoothLeService.writeMessage(UUID.fromString(SampleGattAttributes.HEARTRATEPERIODRW), data);////写入
        DeviceControlService.mBluetoothLeService.readMessage(UUID.fromString(SampleGattAttributes.HEARTRATEPERIODRW));//

        //设置为非连续测心率
        istestxinlv_close = true;
        istestxinlv_open = false;
        test[1] = 0x55;
        test[0] = 0x02;
        DeviceControlService.mBluetoothLeService.writeMessage(UUID.fromString(SampleGattAttributes.CHARACTERISTIC_NOTIFY), test);//
    }

    //关闭睡眠模式
    public void closeSleepMode(){
        //心率恢复为2小时/次
        int measuretimes = 12;
        //写入蓝牙
        SampleGattAttributes.trim(p, measuretimes);
        byte[] data = {p[3], p[2]};
        DeviceControlService.mBluetoothLeService.writeMessage(UUID.fromString(SampleGattAttributes.HEARTRATEPERIODRW), data);////写入
        DeviceControlService.mBluetoothLeService.readMessage(UUID.fromString(SampleGattAttributes.HEARTRATEPERIODRW));//

        //设置为默认值（非连续测心率)
        test[1] = 0x55;
        test[0] = 0x02;
        DeviceControlService.mBluetoothLeService.writeMessage(UUID.fromString(SampleGattAttributes.CHARACTERISTIC_NOTIFY), test);//
    }

    //自定义广播
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        // intentFilter.addAction(BluetoothLeService.UpData);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_WIRTE_SUCCESS);
        return intentFilter;
    }

    //定义一个广播接收器
    private final BroadcastReceiver mGattUpdateReceiver01 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //已经连接上BLE

            SampleGattAttributes.thisrecall= Calendar.getInstance();//记录回掉时间

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                System.out.println("到ACTION_GATT_CONNECTED");
                SampleGattAttributes.connectedState=true;//连接成功
                if(SampleGattAttributes.isinsetactivity)
                    mHandler.postDelayed(runnable, 30000);
            } else if (BluetoothLeService.ACTION_GATT_WIRTE_SUCCESS.equals(action)) {
                if (istestxinlv) {
                    if (istestxinlv_open) {
                        Toast.makeText(context, "关闭睡眠模式成功！", Toast.LENGTH_LONG).show();
                        SampleGattAttributes.checkflag=true;

                        checkflag2=true;//告诉listener不是人操作
                    }

                    if (istestxinlv_close) {
                        Toast.makeText(context, "开启睡眠模式成功！", Toast.LENGTH_LONG).show();
                        SampleGattAttributes.checkflag=false;
                        checkflag2=true;
                    }
                } else {
                    Toast.makeText(context, "修改成功！", Toast.LENGTH_LONG).show();
                }
            }
            //没有连接上BLE
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mHandler.removeCallbacks(runnable);//关掉自动关闭连接的线程
                System.out.println("断线重连显示，并关闭判断中");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                Toast.makeText(context, "服务获取", Toast.LENGTH_LONG).show();
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                if (intent.getStringExtra("DataType").equals("SevenDayData")) {

                } else if (intent.getStringExtra("DataType").equals("CollectPeriod")) {
                    int mode = SampleGattAttributes.dataGetter(intent.getByteArrayExtra("data"), 0, 2);
                    if(mode == 480){
                        openSleepModeSuccess = true;
                    }
                    if(mode == 12){
                        closeSleepModeSuccess = true;
                    }
                }
            }
        }
    };

    public void close(){
        SampleGattAttributes.isinsetactivity=false;//出去了
        DeviceControlService.mBluetoothLeService.disconnect();
        SampleGattAttributes.connectedState=false;
        mHandler.removeCallbacks(runnable);

//        context.unregisterReceiver(mGattUpdateReceiver01);
        istestxinlv = false;
        istestxinlv_close = false;
        istestxinlv_open = false;
    }
}
