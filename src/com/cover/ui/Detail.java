package com.cover.ui;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cover.app.AppManager;
import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;
import com.cover.bean.Message;
import com.cover.dbhelper.Douyatech;
import com.cover.fragment.MapFragment;
import com.cover.ui.ParamSettingActivity.Timer;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

public class Detail extends Activity implements OnClickListener {
	private static final String TAG = "cover";
	public static final int MINITE = 60 * 1000 * 5;
	private Entity entity;
	private TextView tvId;
	private ImageView ivState;
	private TextView tvLocation;
	private TextView tvName;
	private ImageView ivType;
	private ImageView back;
	static ImageView ivReparing;
	static ImageView ivFinish;
	// private ImageView ivLeave;
	private ImageView ivParam;
	private ImageView ivEnterMap;
	private static MapFragment mapFragment;
	private final String ACTION = "com.cover.service.IntenetService";
	Message msg = new Message();
	private boolean flagThreadIsStart = false;
	public static boolean flagIsSetSuccess = false;
	Douyatech douyadb = null;
	int flag_notify = 0;
	private Handler hander = new Handler() {
		public void handleMessage(android.os.Message msg) {
			setNotify(entity);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);

		douyadb = new Douyatech(this);
		entity = (Entity) getIntent().getExtras().getSerializable("entity");
		back = (ImageView) findViewById(R.id.back);
		ivType = (ImageView) findViewById(R.id.iv_type_detail);
		tvName = (TextView) findViewById(R.id.tv_name_detail);
		tvId = (TextView) findViewById(R.id.tv_id_detail);
		ivState = (ImageView) findViewById(R.id.iv_state_detail);
		tvLocation = (TextView) findViewById(R.id.tv_location_detail);

		ivReparing = (ImageView) findViewById(R.id.iv_reparing);
		ivFinish = (ImageView) findViewById(R.id.iv_finish);
		ivParam = (ImageView) findViewById(R.id.iv_param);
		ivEnterMap = (ImageView) findViewById(R.id.iv_entermap_detail);

		ivReparing.setOnClickListener(this);
		ivFinish.setOnClickListener(this);
		ivParam.setOnClickListener(this);
		ivEnterMap.setOnClickListener(this);
		if (entity.getTag().equals("level")) {
			ivType.setImageResource(R.drawable.water);
		} else {
			ivType.setImageResource(R.drawable.cover);
		}
		tvName.setText(entity.getTag().equals("level") ? "水位" : "井盖");
		tvId.setText(entity.getId() + "");

		switch (entity.getStatus()) {
		case NORMAL:
			ivState.setImageResource(R.drawable.state_normal);
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
		case REPAIR:
			ivState.setImageResource(R.drawable.state_reparing);
			break;
		}
		// if (Status.NORMAL == entity.getStatus()) {
		// ivState.setImageResource(R.drawable.state_normal);
		// } else if (Status.REPAIR == entity.getStatus()) {
		// ivState.setImageResource(R.drawable.state_reparing);
		// } else if (Status.EXCEPTION_1 == entity.getStatus()) {
		// ivState.setImageResource(R.drawable.state_alarm);
		// } else if (Status.EXCEPTION_2 == entity.getStatus()) {
		// ivState.setImageResource(R.drawable.state_less_pressure);
		// } else if (Status.EXCEPTION_3 == entity.getStatus()) {
		// // if(Status.EXCEPTION_3 == entity.getStatus())
		// ivState.setImageResource(R.drawable.state_alarm_less_pressure);
		// } else if ((Status.SETTING_FINISH == entity.getStatus())) {
		// ivState.setImageResource(R.drawable.state_leaving);
		// } else if ((Status.SETTING_PARAM == entity.getStatus())) {
		// ivState.setImageResource(R.drawable.state_setting);
		// }
		tvLocation.setText(new java.text.DecimalFormat("#.000000")
				.format(entity.getLatitude())
				+ " "
				+ new java.text.DecimalFormat("#.000000").format(entity
						.getLongtitude()));

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		// sendFailUnAlarm(entity);

		AppManager.getAppManager().addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().finishActivity(this);
	}

	class Timer implements Runnable {

		@Override
		public void run() {
			if (!flagThreadIsStart) {
				try {
					Thread.sleep(MINITE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (!flagIsSetSuccess) {
					sendFailUnAlarm(entity);
					Looper.prepare();
					hander.sendEmptyMessage(11);
					if (douyadb.isExist("leave",
							entity.getTag() + "_" + entity.getId())) {
						douyadb.delete("leave",
								entity.getTag() + "_" + entity.getId());
					}
				}
			}
		}
	}

	public static class DetailReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			byte[] recv = intent.getByteArrayExtra("msg");
			// final int length = 1;
			switch (recv[0]) {
			case 0x06:// 报警解除
				Toast.makeText(context, "报警解除设置命令发送成功", Toast.LENGTH_LONG)
						.show();
				break;
			case 0x07:// begin repair
				Toast.makeText(context, "开始维修命令发送成功", Toast.LENGTH_LONG).show();
				break;
			}
		}

	}

	public void sendMessage(Message msg, String action) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction(action);
		int length = msg.getLength();
		byte[] totalMsg = new byte[length];
		totalMsg = CoverUtils.msg2ByteArray(msg, length);
		serviceIntent.putExtra("msg", totalMsg);
		sendBroadcast(serviceIntent);
		Log.i(TAG, action + "send broadcast " + action);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_reparing:
			if (entity.getStatus() != Status.NORMAL
					&& entity.getStatus() != Status.REPAIR
					&& entity.getStatus() != Status.SETTING_FINISH
					&& entity.getStatus() != Status.SETTING_PARAM
					&& entity.getTag() != "level")
				setRepairBegin(entity);
			else
				Toast.makeText(getApplicationContext(), "当前状态下不可点击维修",
						Toast.LENGTH_LONG).show();
			break;
		case R.id.iv_finish:
			if (entity.getStatus() == Status.NORMAL
					|| entity.getStatus() == Status.SETTING_FINISH
					|| entity.getStatus() == Status.SETTING_PARAM
					|| entity.getTag().equals("level"))
				Toast.makeText(getApplicationContext(), "当前状态下不可点击撤防",
						Toast.LENGTH_LONG).show();
			else {
				String nameID = entity.getTag() + "_" + entity.getId();
				if (!douyadb.isExist("leave", nameID)) {
					setUnAlarm(entity);
					new Thread(new Timer()).start();
					douyadb.add("leave", nameID);
				} else {
					Toast.makeText(getApplicationContext(), "已上传，请勿重复点击",
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.iv_param:
			// 参数按钮的点击事件
			Intent intent = new Intent(Detail.this, ParamSettingActivity.class);
			intent.putExtra("entity", entity);
			startActivity(intent);
			break;
		case R.id.iv_entermap_detail:
			Intent i = new Intent(Detail.this, SingleMapDetail.class);
			i.putExtra("entity", entity);
			startActivity(i);
			break;
		}
	}

	public void setRepairBegin(Entity entity) {
		byte[] b = CoverUtils.short2ByteArray(entity.getId());
		byte[] t = new byte[3];
		t[0] = b[0];
		t[1] = b[1];
		t[2] = entity.getTag().equals("level") ? (byte) 0x2C : (byte) 0x10;
		msg = CoverUtils.makeMessageExceptCheck((byte) 0x0E,
				CoverUtils.short2ByteArray((short) (7 + 3)), t);
		byte[] check = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils
				.msg2ByteArrayExcepteCheck(msg)));
		msg.check[0] = check[check.length - 1];
		msg.check[1] = check[check.length - 2];
		sendMessage(msg, ACTION);
	}

	public void sendFailUnAlarm(Entity entity) {
		// 终端报警解除失败 0x13 App->Server ID 、设备类型
		byte[] b = CoverUtils.short2ByteArray(entity.getId());
		byte[] t = new byte[3];
		t[0] = b[0];
		t[1] = b[1];
		t[2] = entity.getTag().equals("level") ? (byte) 0x2C : (byte) 0x10;
		msg = CoverUtils.makeMessageExceptCheck((byte) 0x13,
				CoverUtils.short2ByteArray((short) (7 + 3)), t);
		byte[] check = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils
				.msg2ByteArrayExcepteCheck(msg)));
		msg.check[0] = check[check.length - 1];
		msg.check[1] = check[check.length - 2];
		sendMessage(msg, ACTION);
	}

	public void setUnAlarm(Entity entity) {
		byte[] b = CoverUtils.short2ByteArray(entity.getId());
		byte[] t = new byte[3];
		t[0] = b[0];
		t[1] = b[1];
		t[2] = entity.getTag().equals("level") ? (byte) 0x2C : (byte) 0x10;
		msg = CoverUtils.makeMessageExceptCheck((byte) 0x10,
				CoverUtils.short2ByteArray((short) (7 + 3)), t);
		byte[] check = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils
				.msg2ByteArrayExcepteCheck(msg)));
		msg.check[0] = check[check.length - 1];
		msg.check[1] = check[check.length - 2];
		sendMessage(msg, ACTION);
	}

	@SuppressWarnings("deprecation")
	public void setNotify(Entity entity) {
		// 创建一个NotificationManager的引用
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		// 定义Notification的各种属性
		int icon = R.drawable.icon; // 通知图标
		CharSequence tickerText = "信息"; // 状态栏显示的通知文本提示
		long when = System.currentTimeMillis(); // 通知产生的时间，会在通知信息里显示
		// 用上面的属性初始化 Nofification
		Notification notification = new Notification(icon, tickerText, when);
		// 添加声音
		if (CoverUtils.getIntSharedP(getApplicationContext(), "setAlarmOrNot") == 1)
			notification.defaults |= Notification.DEFAULT_ALL;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// 设置通知的事件消息
		Context context = getApplicationContext(); // 上下文
		CharSequence contentTitle = entity.getTag() + entity.getId(); // 通知栏标题
		CharSequence contentText = "撤防失败"; // 通知栏内容
		Intent notificationIntent = new Intent(this, Detail.class); // 点击该通知后要跳转的Activity
		notificationIntent.putExtra("entity", entity);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		// 把Notification传递给 NotificationManager
		mNotificationManager.notify(flag_notify++, notification);
	}

}
