package com.example.test01;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.test01.util.AMapUtil;
import com.example.test01.util.ToastUtil;

public class MarkerActivity extends AppCompatActivity implements AMap.OnMapClickListener,
        AMap.OnMapTouchListener, GeocodeSearch.OnGeocodeSearchListener, AMap.OnMarkerClickListener, View.OnClickListener {

    private AMap aMap;
    private MapView mapView;
    private TextView mTapTextView;
    private TextView mTouchTextView;
    private Marker marker;
    private GeocodeSearch geocoderSearch;
    private String addressName;
    private LatLonPoint latLonPoint;
    private Button okBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        okBtn = findViewById(R.id.ok);
        okBtn.setOnClickListener(this);
        okBtn.setEnabled(false);
        init();
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
        mTapTextView = (TextView) findViewById(R.id.tap_text);
        mTouchTextView = (TextView) findViewById(R.id.touch_text);
    }

    /**
     * amap添加一些事件监听器和初始化中心位置
     */
    private void setUpMap() {
        aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器
        aMap.setOnMapTouchListener(this);// 对amap添加触摸地图事件监听器
        //aMap.setOnMarkerClickListener(this);//给标记点添加点击事件
        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(Constants.XIAN,11)));//移动中心点到西安
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 对单击地图事件回调
     */
    @Override
    public void onMapClick(LatLng point) {
        mTapTextView.setText("tapped, point=" + point);
        //marker.destroy();
        if (marker != null){
            marker.remove();
        }
        addMarkersToMap(point);
        latLonPoint = new LatLonPoint(point.latitude,point.longitude);
        getAddress(latLonPoint);
        if(!okBtn.isEnabled()){
            okBtn.setEnabled(true);
        }
    }


    /**
     * 对触摸地图事件回调
     */
    @Override
    public void onTouch(MotionEvent event) {
        mTouchTextView.setText("触摸事件：屏幕位置" + event.getX() + " " + event.getY());
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap(LatLng latlng) {
        MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher_foreground))
                .position(latlng)
                .draggable(true);
        marker = aMap.addMarker(markerOption);
    }

    /**
     * 响应逆地理编码
     */
    public void getAddress(final LatLonPoint latLonPoint) {
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                addressName = result.getRegeocodeAddress().getFormatAddress()
                        + "附近";
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        AMapUtil.convertToLatLng(latLonPoint), 13));
                marker.setPosition(AMapUtil.convertToLatLng(latLonPoint));
                ToastUtil.show(MarkerActivity.this, addressName);
            } else {
                ToastUtil.show(MarkerActivity.this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }
    }

    /**
     * 地理编码查询回调
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onClick(View view) {
        Intent intent =getIntent();
        Bundle bundle =new Bundle();
        bundle.putString("address",addressName);
        bundle.putDouble("latitude",latLonPoint.getLatitude());
        bundle.putDouble("longitude",latLonPoint.getLongitude());
        intent.putExtras(bundle);
        setResult(1,intent);
        finish();
    }
}
