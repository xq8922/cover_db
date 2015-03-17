package com.cover.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.location.LocationClient;
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
import com.cover.adapter.CoverAdapter;
import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;
import com.cover.bean.Message;
import com.cover.ui.CoverList;
import com.cover.ui.Detail;
import com.wxq.covers.R;

public class MapFragment extends Fragment {

	private static final String TAG = "cover";
	Message askMsg = new Message();
	private final String ACTION = "com.cover.service.IntenetService";
	private CoverAdapter adapter;
	private int flag = 0; // 0 全都显示 1显示水位 2显示井盖

	private MapView mMapView;
	private static BaiduMap mBaiduMap;
	private LocationClient mLocationClient;
	private BitmapDescriptor mBitmapDescriptor;
	private MapStatusUpdate myMapStatusUpdate;
	private static ArrayList<Entity> items = new ArrayList<Entity>();
	private static ArrayList<Entity> waterItems = new ArrayList<Entity>();
	private static ArrayList<Entity> coverItems = new ArrayList<Entity>();
	private static ArrayList<Entity> nullItems = new ArrayList<Entity>();
	private static Map<LatLng, Entity> markerEntity1 = new HashMap<LatLng, Entity>();
	private static Map<LatLng, Entity> markerEntity2 = new HashMap<LatLng, Entity>();
	private static Map<LatLng, Entity> markerEntity3 = new HashMap<LatLng, Entity>();

	public void update(int flag) {
		switch (flag) {
		case 0:
			// 在这里 修改地图上 全部显示 items
			if (mBaiduMap != null)
				mBaiduMap.clear();
			Iterator<Entity> it = items.iterator();
			while (it.hasNext()) {
				Entity tempEntity = it.next();
				LatLng point = new LatLng(tempEntity.getLatitude(),
						tempEntity.getLongtitude());
				// 构建Marker图标
				BitmapDescriptor bitmap;
				switch (tempEntity.getStatus()) {
				case REPAIR:
					bitmap = BitmapDescriptorFactory
							.fromResource(R.drawable.red_small);
					break;
				case NORMAL:
					bitmap = BitmapDescriptorFactory
							.fromResource(R.drawable.map_green_small);
					break;
				default:
					bitmap = BitmapDescriptorFactory
							.fromResource(R.drawable.map_yellow_small);
				}
				// 构建MarkerOption，用于在地图上添加Marker
				OverlayOptions option = new MarkerOptions().position(point)
						.icon(bitmap);
				// 在地图上添加Marker，并显示
				mBaiduMap.addOverlay(option);
				markerEntity1.put(point, tempEntity);
				mBaiduMap.setMyLocationEnabled(true);
			}
			this.flag = 0;
			break;
		case 1:
			if (mBaiduMap != null)
				mBaiduMap.clear();
			Iterator<Entity> it2 = waterItems.iterator();
			while (it2.hasNext()) {
				// LatLng point = new LatLng(it2.next().getLatitude(),
				// it2.next().getLongtitude());
				Entity tempEntity2 = it2.next();
				LatLng point = new LatLng(tempEntity2.getLatitude(),
						tempEntity2.getLongtitude());
				// 构建Marker图标
				BitmapDescriptor bitmap;
				switch (tempEntity2.getStatus()) {
				case REPAIR:
					bitmap = BitmapDescriptorFactory
							.fromResource(R.drawable.red_small);
					break;
				case NORMAL:
					bitmap = BitmapDescriptorFactory
							.fromResource(R.drawable.map_green_small);
					break;
				default:
					bitmap = BitmapDescriptorFactory
							.fromResource(R.drawable.map_yellow_small);
				}
				OverlayOptions option = new MarkerOptions().position(point)
						.icon(bitmap);
				mBaiduMap.addOverlay(option);
				markerEntity2.put(point, tempEntity2);
			}
			// 只显示水位
			this.flag = 1;
			break;
		case 2:
			if (mBaiduMap != null)
				mBaiduMap.clear();
			Iterator<Entity> it3 = coverItems.iterator();
			while (it3.hasNext()) {
				Entity tempEntity3 = it3.next();
				LatLng point = new LatLng(tempEntity3.getLatitude(),
						tempEntity3.getLongtitude());
				// 构建Marker图标
				BitmapDescriptor bitmap;
				switch (tempEntity3.getStatus()) {
				case REPAIR:
					bitmap = BitmapDescriptorFactory
							.fromResource(R.drawable.red_small);
					break;
				case NORMAL:
					bitmap = BitmapDescriptorFactory
							.fromResource(R.drawable.map_green_small);
					break;
				default:
					bitmap = BitmapDescriptorFactory
							.fromResource(R.drawable.map_yellow_small);
				}
				// 构建MarkerOption，用于在地图上添加Marker
				OverlayOptions option = new MarkerOptions().position(point)
						.icon(bitmap).title("cover");
				// 在地图上添加Marker，并显示
				mBaiduMap.addOverlay(option);
				markerEntity3.put(point, tempEntity3);
			}
			// adapter.update(coverItems);
			this.flag = 2;
			break;
		case 3:
			// 什么都不显示
			if (mBaiduMap != null)
				mBaiduMap.clear();
			// adapter.update(nullItems);
			this.flag = 3;
			break;
		case 4:
			Entity entity = (Entity) CoverList.entity;
			// getArguments().getSerializable("entity");
			// System.out.println("test Map"+entity);
			// 设定中心点坐标
			LatLng cenpt = new LatLng(entity.getLongtitude(),
					entity.getLatitude());
			// 构建Marker图标
			BitmapDescriptor bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.red_small);
			// 构建MarkerOption，用于在地图上添加Marker
			OverlayOptions option = new MarkerOptions().position(cenpt)
					.icon(bitmap).title("cover");
			// 定义地图状态
			MapStatus mMapStatus = new MapStatus.Builder().target(cenpt)
					.zoom(12).build();
			// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
			MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
					.newMapStatus(mMapStatus);
			// 在地图上添加Marker，并显示
			mBaiduMap.addOverlay(option);
			// 改变地图状态
			mBaiduMap.setMapStatus(mMapStatusUpdate);

		}
	}

	private void getDatas() {
		for (int i = 0; i < (16 - 1) / 5; i++) {
			if (i <= 1) {
				Entity entity = new Entity((short) 1, Status.REPAIR,
						"水位", 34.26667, 108.95000);
				waterItems.add(entity);
				items.add(entity);
			} else {
				Entity entity = new Entity((short) 2, Status.NORMAL,
						"井盖", 34.26667 + 0.1 * new Random().nextFloat(),
						108.95000 + 0.1 * new Random().nextFloat());
				coverItems.add(entity);
				items.add(entity);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SDKInitializer.initialize(getActivity().getApplicationContext());
		// getDatas();
		View view = inflater.inflate(R.layout.cover_map_list, null);

		mMapView = (MapView) view.findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();// get the map
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);// normal view

		// 设定中心点坐标
		LatLng cenpt = new LatLng(34.26667, 108.95000);
		// 定义地图状态
		MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(12)
				.build();
		// 定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		// 改变地图状态
		mBaiduMap.setMapStatus(mMapStatusUpdate);
		Log.i(TAG, "test entity is null");
		return view;
	}

	public void firstData() {
		// items.clear();
		// waterItems.clear();
		// coverItems.clear();
		items = CoverList.items;
		waterItems = CoverList.waterItems;
		coverItems = CoverList.coverItems;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((CoverList) getActivity()).setAllChecked();
		// 全部显示
		Entity e = CoverList.entity;
		if (e == null) {
			Log.i(TAG, "test entity is null");
			Iterator<Entity> it = items.iterator();
			while (it.hasNext()) {
				Entity tempEntity = it.next();
				LatLng point = new LatLng(tempEntity.getLatitude(),
						tempEntity.getLongtitude());
				// 构建Marker图标
				BitmapDescriptor bitmap = BitmapDescriptorFactory
						.fromResource(R.drawable.red_small);
				try {
					switch (tempEntity.getStatus()) {
					case REPAIR:
						bitmap = BitmapDescriptorFactory
								.fromResource(R.drawable.map_yellow_small);
						break;
					case NORMAL:
						bitmap = BitmapDescriptorFactory
								.fromResource(R.drawable.map_green_small);
						break;
					default:
						bitmap = BitmapDescriptorFactory
								.fromResource(R.drawable.red_small);
					}
				} catch (NullPointerException e1) {
					e1.printStackTrace();
				}
				// 构建MarkerOption，用于在地图上添加Marker
				OverlayOptions option = new MarkerOptions().position(point)
						.icon(bitmap);
				// 在地图上添加Marker，并显示
				mBaiduMap.addOverlay(option);

				// TextView location = new TextView(getActivity());
				// location.setText(tempEntity.getTag() + "，" +
				// tempEntity.getId());
				// location.setTextColor(Color.BLACK);
				// InfoWindow info = new InfoWindow(location, new LatLng(
				// tempEntity.getLatitude(), tempEntity.getLongtitude()), -40);
				// mBaiduMap.showInfoWindow(info);
				// Marker m = Marker.
				markerEntity1.put(point, tempEntity);
				mBaiduMap.setMyLocationEnabled(true);
				// for (Entity element : items) {
				// TextView location = new TextView(getActivity());
				// location.setText(element.getTag() + "，" + element.getId());
				// location.setTextColor(Color.BLACK);
				// InfoWindow info = new InfoWindow(location, new LatLng(
				// element.getLatitude(), element.getLongtitude()), -40);
				// mBaiduMap.showInfoWindow(info);
				// }
			}
		} else {
			Log.i(TAG, "test entity is not null" + e);
			LatLng point = new LatLng(e.getLatitude(), e.getLongtitude());
			// 构建Marker图标
			BitmapDescriptor bitmap = BitmapDescriptorFactory
					.fromResource(R.drawable.red_small);
			// 构建MarkerOption，用于在地图上添加Marker
			OverlayOptions option = new MarkerOptions().position(point).icon(
					bitmap);
			// 在地图上添加Marker，并显示
			mBaiduMap.addOverlay(option);
			CoverList.entity = null;
		}
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker arg0) {
				// 进入详情界面 传进对象
				Entity entity = null;
				switch (flag) {
				case 0:
					entity = markerEntity1.get(arg0.getPosition());
					break;
				case 1:
					entity = markerEntity2.get(arg0.getPosition());
					break;
				case 2:
					entity = markerEntity3.get(arg0.getPosition());
					break;
				}
				Intent intent = new Intent(getActivity(), Detail.class);
				intent.putExtra("entity", entity);
				startActivity(intent);
				return false;
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		// mLocationClient.start();
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		// if (!mLocationClient.isStarted()) mLocationClient.start();
		// mLocationClient.requestLocation();

	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		mBaiduMap.setMyLocationEnabled(false);
		// mLocationClient.stop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();

	}

}
