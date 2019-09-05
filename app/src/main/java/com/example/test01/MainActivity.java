package com.example.test01;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.example.test01.util.CoordinateUtil;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private static LocationManager locationManager;
    private Switch mSwitch;
    private Button mapSelectPoint;
    private TextView pointDescription;
    private RadioButton homeBtn, workBtn;
    private Intent intent;

    private String homeLatLngInfo = "108.91341245460511&34.20710445903613";
    private String workLatLngInfo = "108.830848&34.208666";
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
        }

        mSwitch = findViewById(R.id.switch1);
        homeBtn = findViewById(R.id.radioButton);
        workBtn = findViewById(R.id.radioButton2);
        mapSelectPoint = findViewById(R.id.custom_button);
        pointDescription = findViewById(R.id.location_description);

        mSwitch.setOnCheckedChangeListener(this);
        homeBtn.setOnCheckedChangeListener(this);
        workBtn.setOnCheckedChangeListener(this);
        mapSelectPoint.setOnClickListener(this);

        intent = new Intent(this, SimulateLocationService.class);

        workBtn.setChecked(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("zhang","resultCode : "+resultCode+"   data : "+data);
        if (resultCode == 0 && isSelectedMoniLocal(this)) {
            // TODO: 19-9-5
        }else if (resultCode == 1 && data != null){
            Bundle content = data.getExtras();
            String address = content.getString("address","0");
            double latitude = content.getDouble("latitude", 0);
            double longitude = content.getDouble("longitude", 0);

            LatLonPoint point = CoordinateUtil.toGPSPoint(latitude, longitude);//高德地图经纬度转GPS经纬度

            pointDescription.append(address);
            pointDescription.append("\n"+" 纬度 : "+point.getLatitude());
            pointDescription.append("\n"+" 精度 : "+point.getLongitude());
            workBtn.setEnabled(false);
            homeBtn.setEnabled(false);
            key = point.getLongitude()+"&"+point.getLatitude();
        }
    }

    /**
     * 根据类型 转换 坐标
     */
    private LatLng convert(LatLng sourceLatLng, CoordinateConverter.CoordType coord ) {
        CoordinateConverter converter  = new CoordinateConverter(this);
        // CoordType.GPS 待转换坐标类型
        converter.from(coord);
        // sourceLatLng待转换坐标点
        converter.coord(sourceLatLng);
        // 执行转换操作
        LatLng desLatLng = converter.convert();
        return desLatLng;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int id = compoundButton.getId();
        switch (id) {
            case R.id.switch1:
                //判定是否使用了模拟定位
                if (isSelectedMoniLocal(this)) {
                    intent.putExtra("key", key);
                    if (b) {
                        startService(intent);
                        workBtn.setEnabled(false);
                        homeBtn.setEnabled(false);
                        Toast.makeText(this, "模拟定位已开启", Toast.LENGTH_SHORT).show();
                    } else {
                        stopService(intent);
                        workBtn.setEnabled(true);
                        homeBtn.setEnabled(true);
                        Toast.makeText(this, "模拟定位已关闭", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(
                            Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                    startActivityForResult(intent, 0);
                    mSwitch.setChecked(false);
                }
                break;
            case R.id.radioButton:
                if (b) {
                    workBtn.setChecked(false);
                    key = homeLatLngInfo;
                } else {
                    workBtn.setChecked(true);
                    key = workLatLngInfo;
                }
                break;
            case R.id.radioButton2:
                if (b) {
                    homeBtn.setChecked(false);
                    key = workLatLngInfo;
                } else {
                    homeBtn.setChecked(true);
                    key = homeLatLngInfo;
                }
                break;
        }
    }

    /**
     * 6.0以上系统判定是否允许当前应用使用模拟定位
     * true--允许
     * false--禁止
     *
     * @param mContext
     * @return
     */
    public static boolean isSelectedCurApp(Context mContext) {
        boolean statue = true;
        String serviceName = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) mContext.getSystemService(serviceName);
        try {
            String providerStr = LocationManager.GPS_PROVIDER;
            LocationProvider provider = locationManager.getProvider(providerStr);
            if (provider != null) {
                locationManager.addTestProvider(
                        provider.getName()
                        , provider.requiresNetwork()
                        , provider.requiresSatellite()
                        , provider.requiresCell()
                        , provider.hasMonetaryCost()
                        , provider.supportsAltitude()
                        , provider.supportsSpeed()
                        , provider.supportsBearing()
                        , provider.getPowerRequirement()
                        , provider.getAccuracy());
            } else {
                locationManager.addTestProvider(
                        providerStr
                        , true, true, false, false, true, true, true
                        , Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            //如果没有选择当前的应用，则addTestProvider会抛出异常
            statue = false;
        }
        return statue;
    }

    public static boolean isSelectedMoniLocal(Context mContext) {
        boolean status = false;
        if (Build.VERSION.SDK_INT > 22) {
            //6.0以上版本
            if (isSelectedCurApp(mContext)) {
                //Toast.makeText(mContext, "模拟定位已开启", Toast.LENGTH_SHORT).show();
                status = true;
            } else {
                Toast.makeText(mContext, "没开虚拟定位权限", Toast.LENGTH_SHORT).show();
                status = false;
            }
        } else {
            Toast.makeText(mContext, "当前系统版本低于6.0", Toast.LENGTH_SHORT).show();
        }
        return status;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.custom_button:
                //Intent mapIntent = new Intent(this,ReGeocoderActivity.class);
                Intent mapIntent = new Intent(this,MarkerActivity.class);
                startActivityForResult(mapIntent,1);
                break;
        }
    }
}