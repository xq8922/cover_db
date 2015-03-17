package com.cover.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.cover.app.AppManager;
import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;
import com.cover.fragment.MapFragment;
import com.wxq.covers.R;

public class SingleMapDetail extends Activity {
	private ImageView back;
	private Entity entity;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private static MapFragment mapFragment;
	int flag_status = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.map_detail);

		back = (ImageView) findViewById(R.id.back);
		entity = (Entity) getIntent().getExtras().getSerializable("entity");
		mMapView = (MapView) findViewById(R.id.map_detail_view);
		mBaiduMap = mMapView.getMap();// get the map
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);// normal view
		// 设定中心点坐标
		LatLng cenpt = new LatLng(entity.getLatitude(), entity.getLongtitude());
		// 构建Marker图标
		BitmapDescriptor bitmap;
		if (entity.getStatus() == null) {
			entity.setStatus(Status.NORMAL);
			flag_status = 1;
		}
		switch (entity.getStatus()) {
		case REPAIR:
			bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.map_yellow_small);
			break;
		case NORMAL:
			bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.map_green_small);
			break;
		default:
			bitmap = BitmapDescriptorFactory.fromResource(R.drawable.red_small);
		}
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(cenpt)
				.icon(bitmap);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);

		TextView location = new TextView(getApplicationContext());
		String status = "";
		if (flag_status == 0) {
			switch (entity.getStatus()) {
			case NORMAL:
				status = "正常状态";
				break;
			case EXCEPTION_1:
				status = "报警状态";
				break;
			case REPAIR:
				status = "维修状态";
				break;
			case EXCEPTION_2:
				status = "欠压状态";
				break;
			case EXCEPTION_3:
				status = "报警欠压状态";
				break;
			case SETTING_FINISH:
				status = "撤防中";
				break;
			case SETTING_PARAM:
				status = "参数设置中";
				break;
			}
		}
		location.setText((entity.getTag().equals("level") ? "水位-" : "井盖-")
				+ entity.getId() + status);
		location.setTextColor(Color.BLACK);
		InfoWindow info = new InfoWindow(location, new LatLng(
				entity.getLatitude(), entity.getLongtitude()), -40);
		mBaiduMap.showInfoWindow(info);

		// 定义地图状态
		MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(12)
				.build();
		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		// 改变地图状态
		mBaiduMap.setMapStatus(mMapStatusUpdate);
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker arg0) {
				TextView location = new TextView(getApplicationContext());
				// location.setBackgroundResource(R.drawable.cover);
				// location.setPadding(30, 20, 30, 50);
				location.setText((entity.getTag().equals("level") ? "水位-"
						: "井盖-") + entity.getId());
				location.setTextColor(Color.BLACK);
				// location.setBackgroundColor(255);
				// location.setBackgroundDrawable(R.drawable.bg_password_normal);
				InfoWindow info = new InfoWindow(location, arg0.getPosition(),
						-16);
				mBaiduMap.showInfoWindow(info);
				return false;
			}
		});

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		AppManager.getAppManager().addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().finishActivity(this);
	}

}
