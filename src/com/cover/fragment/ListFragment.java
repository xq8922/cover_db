package com.cover.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.cover.adapter.CoverAdapter;
import com.cover.bean.Entity;
import com.cover.ui.CoverList;
import com.cover.ui.Detail;
import com.wxq.covers.R;

public class ListFragment extends Fragment {

	private CoverAdapter adapter;
	private int flag = 0; // 0 全都显示 1显示水位 2显示井盖
	private ListView lv;
	private ArrayList<Entity> items = new ArrayList<Entity>();
	private ArrayList<Entity> waterItems = new ArrayList<Entity>();
	private ArrayList<Entity> coverItems = new ArrayList<Entity>();
	private ArrayList<Entity> nullItems = new ArrayList<Entity>();
	public boolean flagWhitchIsCurrent;

	public void update(int flag) {
		switch (flag) {
		case 0:
			adapter.update(items);
			this.flag = 0;
			break;
		case 1:
			adapter.update(waterItems);
			this.flag = 1;
			break;
		case 2:
			adapter.update(coverItems);
			this.flag = 2;
			break;
		case 3:
			// 什么都不显示
			adapter.update(nullItems);
			this.flag = 3;
			break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		this.items = CoverList.items;
		this.waterItems = CoverList.waterItems;
		this.coverItems = CoverList.coverItems;
		// this.coverItems = ((CoverList) getActivity()).coverItems;

		View view = inflater.inflate(R.layout.list_fragment, null);
		lv = (ListView) view.findViewById(R.id.lv_coverlist_cover);
		return view;
	}

	public void firstData() {
		this.items = CoverList.items;
		this.waterItems = CoverList.waterItems;
		this.coverItems = CoverList.coverItems;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		((CoverList) getActivity()).setAllChecked();
		// 获取到activity里面的数据并进行显示
		this.items = CoverList.items;
		this.waterItems = CoverList.waterItems;
		this.coverItems = CoverList.coverItems;

		adapter = new CoverAdapter(getActivity(), items);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(itemClickListener);
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			// 进入详情界面 传进对象
			Entity entity = null;
			switch (flag) {
			case 0:
				entity = items.get(position);
				break;
			case 1:
				entity = waterItems.get(position);
				break;
			case 2:
				entity = coverItems.get(position);
				break;
			}
			// Toast.makeText(getActivity(), entity.toString(), 1).show();
			Intent intent = new Intent(getActivity(), Detail.class);
			intent.putExtra("entity", entity);
			startActivity(intent);
		}
	};

}
