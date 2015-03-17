package com.cover.adapter;

import java.util.List;

import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;
import com.wxq.covers.R;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.UpdateAppearance;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CoverAdapter extends BaseAdapter {

	private Context context;
	private List<Entity> items;

	public CoverAdapter(Context context, List<Entity> items) {
		this.context = context;
		this.items = items;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void update(List<Entity> items) {
		this.items = items;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = View.inflate(context, R.layout.item_listview, null);
		ImageView ivType = (ImageView) view.findViewById(R.id.iv_type);
		TextView ivName = (TextView) view.findViewById(R.id.tv_name);
		ImageView ivState = (ImageView) view.findViewById(R.id.iv_state);
		Entity entity = items.get(position);
		if (entity.getTag().equals("level")) {
			ivType.setImageResource(R.drawable.water);
		} else {
			ivType.setImageResource(R.drawable.cover);
		}
		ivName.setText((entity.getTag().equals("level")?"水位":"井盖") + "-" + entity.getId());
//		if (Status.SETTING_FINISH == entity.getStatus()) {
//			String string = entity.getTag() + "-" + entity.getId();
//			SpannableString builder = new SpannableString(string);
//			builder.setSpan(new AbsoluteSizeSpan(14), string.length() - 4,
//					string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			ivName.setText(builder);
//			ivName.setText(string);
//		} else if (Status.SETTING_PARAM == entity.getStatus()) {
//			String string = entity.getTag() + "-" + entity.getId();
//			SpannableString builder = new SpannableString(string);
//			builder.setSpan(new AbsoluteSizeSpan(14), string.length() - 4,
//					string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			ivName.setText(builder);
//			ivName.setText(string);
//		}
//if(entity)
		switch (entity.getStatus()) {
		case NORMAL:
			ivState.setImageResource(R.drawable.state_normal);
			break;
		case REPAIR:
			ivState.setImageResource(R.drawable.state_reparing);
			break;
		case EXCEPTION_1:
			ivState.setImageResource(R.drawable.state_alarm);
			break;
		case EXCEPTION_2:
			ivState.setImageResource(R.drawable.state_less_pressure);
			break;
		case EXCEPTION_3:
			ivState.setImageResource(R.drawable.state_alarm_less_pressure);
			break;
		case SETTING_FINISH:
			ivState.setImageResource(R.drawable.state_leaving);
			break;
		case SETTING_PARAM:
			ivState.setImageResource(R.drawable.state_setting);
			break;
		default:
			break;
		}

		return view;
	}

}
