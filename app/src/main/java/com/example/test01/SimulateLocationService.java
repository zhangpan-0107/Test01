package com.example.test01;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.UUID;

public class SimulateLocationService extends Service {
    private final static String TAG = "SimulateLocationService";
    private LocationManager mLocation;
    private Handler handler;
    private HandlerThread handlerThread;

    public static final int RunCode = 0x01;
    public static final int StopCode = 0x02;

    //经纬度字符串
    private String latLngInfo = "108.91341245460511&34.20710445903613";

    private boolean isStop = true;

    public SimulateLocationService() {
    }

    //uuid random
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        rmNetworkProvider();
        rmGPSTestProvider();
        setNewNetworkProvider();
        setGPSProvider();

        //thread
        handlerThread = new HandlerThread(getUUID(), -2);
        handlerThread.start();

        handler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                try {
                    Thread.sleep(128);
                    if (!isStop) {
                        setNetworkLocation();
                        setGPSLocation();
                        sendEmptyMessage(0);
                        Log.d(TAG, "循环写入位置开始");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(TAG, "handleMessage error");
                    Thread.currentThread().interrupt();
                }
            }
        };

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        String channelId = "channel_01";
        String name = "channel_name";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW);
            Log.i(TAG, mChannel.toString());
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
            notification = new Notification.Builder(this)
                    .setChannelId(channelId)
                    .setContentTitle("位置模拟服务已启动")
                    .setContentText("MockLocation service is running")
                    .setSmallIcon(R.mipmap.ic_launcher).build();
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle("位置模拟服务已启动")
                    .setContentText("MockLocation service is running")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(true)
                    .setChannelId(channelId);//无效
            notification = notificationBuilder.build();
        }
        startForeground(1, notification);
        //

        //get location info from mainActivity
        latLngInfo = intent.getStringExtra("key");
        Log.d(TAG, "DataFromMain is " + latLngInfo);
        isStop = false;
        handler.sendEmptyMessage(0);
        return super.onStartCommand(intent, flags, startId);
    }

    /*移除原有的网络提供者*/
    private void rmNetworkProvider(){
        try {
            String providerStr = mLocation.NETWORK_PROVIDER;
            if (mLocation.isProviderEnabled(providerStr)){
                Log.d(TAG,"now rm networkProvider");
                mLocation.removeTestProvider(providerStr);
            }
        }catch (Exception e){
            e.fillInStackTrace();
            Log.d(TAG,"rm networkProvider error");
        }
    }

    /*移除原有的GPS提供者*/
    private void rmGPSTestProvider() {
        try {
            for (int i = 0; i < 3; i++) {
                String providerStr = LocationManager.GPS_PROVIDER;
                if (mLocation.isProviderEnabled(providerStr)) {
                    Log.d(TAG, "now remove GPSProvider: try_" + i);
                    mLocation.removeTestProvider(providerStr);
                } else {
                    Log.d(TAG, "GPSProvider is not enabled: try_" + i);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "rmGPSProvider error");
        }
    }

    /*设置一个新的网络提供者*/
    private void setNewNetworkProvider(){
        String providerStr = mLocation.NETWORK_PROVIDER;
        try {
            mLocation.addTestProvider(providerStr,false,false,false,
                    false,false,false,false,
                    1, Criteria.ACCURACY_FINE);
        }catch (Exception e){
            e.fillInStackTrace();
            Log.d(TAG,"set networkProvider error");
        }

        if(!mLocation.isProviderEnabled(providerStr)){
            Log.d(TAG, "now  setTestProviderEnabled[network]");
            mLocation.setTestProviderEnabled(providerStr,true);
        }
    }

    private void setGPSProvider() {
        LocationProvider provider = mLocation.getProvider(LocationManager.GPS_PROVIDER);
        try {
            mLocation.addTestProvider(LocationManager.GPS_PROVIDER, false, true, true,
                    false, true, true, true, 0, 5);
            Log.d(TAG, "addTestProvider[GPS_PROVIDER] success");
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "addTestProvider[GPS_PROVIDER] error");
        }

        if (!mLocation.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                mLocation.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            }catch (Exception e){
                e.printStackTrace();
                Log.d(TAG, "setTestProviderEnabled[GPS_PROVIDER] error");
            }

        }

        //新
        mLocation.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null,
                System.currentTimeMillis());
    }

    /*向新的provider写入虚假位置信息*/
    private void setNetworkLocation(){
        String providerStr = LocationManager.NETWORK_PROVIDER;
        try {
            mLocation.setTestProviderLocation(providerStr, generateLocation());
        } catch (Exception e) {
            Log.d(TAG, "setNetworkLocation error");
            e.printStackTrace();
        }
    }

    private void setGPSLocation(){
        Log.d(TAG, "setGPSLocation: " + latLngInfo);

        String providerStr = LocationManager.GPS_PROVIDER;
        try {
            mLocation.setTestProviderLocation(providerStr, generateLocation());
        } catch (Exception e) {
            Log.d(TAG, "setGPSLocation error");
            e.printStackTrace();
        }
    }

    /*
    *longitude : 108.91341245460511
    * latitude : 34.20710445903613
    *生成一个虚拟位置*/
    private Location generateLocation(){
        String latLngStr[] = latLngInfo.split("&");

        Location location = new Location("gps");
        location.setAccuracy(2.0f);//精度
        location.setAltitude(55.0D);//海拔
        location.setBearing(1.0f);//方向

        Bundle bundle = new Bundle();
        bundle.putInt("satellites",7);//设置卫星数
        location.setExtras(bundle);

        location.setLatitude(Double.valueOf(latLngStr[1]));
        location.setLongitude(Double.valueOf(latLngStr[0]));
        location.setTime(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= 17) {
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        return location;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStop = true;
        handler.removeMessages(0);
        rmNetworkProvider();
        Log.d(TAG, "onDestroy isStop ： "+isStop);
    }
}
