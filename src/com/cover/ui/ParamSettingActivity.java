package com.cover.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cover.app.AppManager;
import com.cover.bean.Entity;
import com.cover.bean.Entity.Status;
import com.cover.bean.Message;
import com.cover.dbhelper.Douyatech;
import com.cover.ui.Detail.Timer;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

public class ParamSettingActivity extends Activity implements OnClickListener {
	private static final String TAG = "cover";
	private final static String ACTION = "com.cover.service.IntenetService";
	Message askMsg = new Message();
	private ImageView back;
	private Entity entity;

	private ImageView type;
	private ImageView ivType;
	private TextView tvName;

	private RelativeLayout rlAlarmFreq;
	private RelativeLayout rlAlarmTime;
	private RelativeLayout rlAlarmAngle;

	private TextView tvAlarmAngle;
	private TextView tvAlarmTime;
	private TextView tvAlarmFreq;
	private ImageView update;
	private short angle = 10;
	private short time = 20;
	private short alarmFrequency = 10;
	private short seconfAlarm = 100;
	final static int MINITE = 1000 * 60 * 5;
	public static boolean flagIsSetSuccess = false;
	private boolean flagThreadIsStart = false;
	Douyatech douyadb = null;
	private TextView tvFrequencyAlarm;
	private Handler hander = new Handler() {
		public void handleMessage(android.os.Message msg) {
			setNotify(entity);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.paramsetting);

		douyadb = new Douyatech(this);
		entity = (Entity) getIntent().getExtras().getSerializable("entity");
		tvFrequencyAlarm = (TextView) findViewById(R.id.tv_frequency_alarm);
		back = (ImageView) findViewById(R.id.setting_param_back);
		ivType = (ImageView) findViewById(R.id.iv_type_param);
		tvName = (TextView) findViewById(R.id.tv_name_param);
		tvAlarmAngle = (TextView) findViewById(R.id.tv_angle_param);
		tvAlarmTime = (TextView) findViewById(R.id.tv_time_param);
		tvAlarmFreq = (TextView) findViewById(R.id.tv_freq_param);
		rlAlarmAngle = (RelativeLayout) findViewById(R.id.rl_alarm_angle);
		rlAlarmTime = (RelativeLayout) findViewById(R.id.rl_alarm_time);
		rlAlarmFreq = (RelativeLayout) findViewById(R.id.rl_alarm_freq);
		update = (ImageView) findViewById(R.id.update);
		update.setOnClickListener(this);
		rlAlarmTime.setOnClickListener(this);
		rlAlarmAngle.setOnClickListener(this);
		rlAlarmFreq.setOnClickListener(this);
		if (entity.getTag().equals("level")) {
			ivType.setImageResource(R.drawable.water);
		} else {// if (entity.getTag().equals("井盖"))
			ivType.setImageResource(R.drawable.cover);
		}
		String status = "";
		if (entity.getStatus() == Status.SETTING_FINISH)
			status = "_撤防中";
		else if (entity.getStatus() == Status.SETTING_PARAM)
			status = "_参数设置中";
		tvName.setText((entity.getTag().equals("level") ? "水位:" : "井盖:")
				+ entity.getId() + status);

		back.setOnClickListener(this);

		if (entity.getTag().equals("level")) {
			rlAlarmAngle.setVisibility(View.GONE);
			tvFrequencyAlarm.setText("二次报警");
		}
		// ask for result of settings
		// int le+ngth = 7 + msg.length();
		// askMsg.length
		// msgAsk.data = msg.getBytes();
		// msgAsk.function = 0x0c;
		// msgAsk.length = Integer.toHexString(length).getBytes();
		// byte[] checkMsg = new byte[3 + msg.length()];
		// msgAsk.check = CoverUtils.genCRC(checkMsg, checkMsg.length);
		// sendMessage()

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
					sendFailSetting(entity);
					hander.sendEmptyMessage(11);
					if (douyadb.isExist("setting", entity.getTag() + "_"
							+ entity.getId())) {
						douyadb.delete("setting", entity.getTag() + "_"
								+ entity.getId());
					}
				}
			}
		}
	}

	public void sendFailSetting(Entity entity) {
		// 终端报警解除失败 0x13 App->Server ID 、设备类型
		Message msg = new Message();
		byte[] b = CoverUtils.short2ByteArray(entity.getId());
		byte[] t = new byte[3];
		t[0] = b[0];
		t[1] = b[1];
		t[2] = entity.getTag().equals("level") ? (byte) 0x2C : (byte) 0x10;
		msg = CoverUtils.makeMessageExceptCheck((byte) 0x14,
				CoverUtils.short2ByteArray((short) (7 + 3)), t);
		byte[] check = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils
				.msg2ByteArrayExcepteCheck(msg)));
		msg.check[0] = check[check.length - 1];
		msg.check[1] = check[check.length - 2];
		sendMessage(msg, ACTION);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_param_back:
			// 1.保存所做的修改
			onBackPressed();
			break;
		case R.id.rl_alarm_angle:
			final EditText et_Ip1 = new EditText(this);
			new AlertDialog.Builder(this)
					.setTitle("报警角度")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(et_Ip1)
					.setPositiveButton(
							"确定",
							new android.content.DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 保存设置
									// 修改显示
									tvAlarmAngle.setText(et_Ip1.getText()
											.toString().trim()
											+ "度");
									angle = Short.valueOf(et_Ip1.getText()
											.toString().trim());
								}

							}).setNegativeButton("取消", null).show();
			break;
		case R.id.rl_alarm_time:
			final EditText et_Ip2 = new EditText(this);
			new AlertDialog.Builder(this)
					.setTitle("定时上报")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(et_Ip2)
					.setPositiveButton(
							"确定",
							new android.content.DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 保存设置
									// 修改显示
									tvAlarmTime.setText(et_Ip2.getText()
											.toString().trim()
											+ "分钟");
									time = Short.valueOf(et_Ip2.getText()
											.toString().trim());
								}

							}).setNegativeButton("取消", null).show();
			break;
		case R.id.rl_alarm_freq:
			final EditText et_Ip3 = new EditText(this);
			new AlertDialog.Builder(this)
					.setTitle("报警频率")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(et_Ip3)
					.setPositiveButton(
							"确定",
							new android.content.DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 保存设置
									// 修改显示
									tvAlarmFreq.setText(et_Ip3.getText()
											.toString().trim()
											+ "分钟");
									alarmFrequency = Short.valueOf(et_Ip3
											.getText().toString().trim());
								}
							}).setNegativeButton("取消", null).show();
			break;
		case R.id.update:
			if (entity.getStatus() == Status.NORMAL) {
				String nameID = entity.getTag() + "_" + entity.getId();
				if (!douyadb.isExist("setting", nameID)) {
					sendArgSettings(entity);
					new Thread(new Timer()).start();
					douyadb.add("setting", nameID);
				} else {
					Toast.makeText(getApplicationContext(), "已上传，请勿重复点击",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getApplicationContext(), "当前状态下不可点击设置",
						Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	public void sendArgSettings(Entity entity) {
		// 是否有小数
		Message msg = new Message();
		int j = 0;
		short id = entity.getId();
		byte[] tmp = CoverUtils.short2ByteArray(id);
		byte[] data = new byte[11];
		data[j++] = tmp[0];
		data[j++] = tmp[1];

		data[j++] = entity.getTag().equals("level") ? (byte) 0x2C : (byte) 0x10;
		short jiaodu = angle;
		tmp = (entity.getTag().equals("cover")) ? CoverUtils
				.short2ByteArray(jiaodu) : new byte[] { 0, 0 };
		data[j++] = tmp[0];
		data[j++] = tmp[1];
		tmp = CoverUtils.short2ByteArray(time);
		data[j++] = tmp[0];
		data[j++] = tmp[1];
		short alarmFrequency = 10;
		tmp = (entity.getTag().equals("cover")) ? CoverUtils
				.short2ByteArray(alarmFrequency) : new byte[] { 0, 0 };
		data[j++] = tmp[0];
		data[j++] = tmp[1];
		tmp = (entity.getTag().equals("cover")) ? new byte[] { 0, 0 }
				: CoverUtils.short2ByteArray(seconfAlarm);
		data[j++] = tmp[0];
		data[j++] = tmp[1];

		msg = CoverUtils.makeMessageExceptCheck((byte) 0x11,
				CoverUtils.short2ByteArray((short) 18), data);
		byte[] tmp1 = CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils
				.msg2ByteArrayExcepteCheck(msg)));
		msg.check[0] = tmp1[tmp1.length - 1];
		msg.check[1] = tmp1[tmp1.length - 2];
		if (!entity.getTag().equals("level")) {
			if (alarmFrequency == 0 || seconfAlarm == 0) {
				Toast.makeText(getApplicationContext(), "输入不正确！",
						Toast.LENGTH_LONG).show();
				return;
			}
		} else {
			if (alarmFrequency == 0 || seconfAlarm == 0 || angle == 0) {
				Toast.makeText(getApplicationContext(), "输入不正确！",
						Toast.LENGTH_LONG).show();
				return;
			}
		}
		sendMessage(msg, ACTION);
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

	public static class SettingsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			byte[] recv = intent.getByteArrayExtra("msg");
			if (recv[0] == 0x05) {
				int length = 4;
				if (recv[3] == 0x01) {
					Toast.makeText(context, "命令设置成功", Toast.LENGTH_LONG).show();
					Message msg = null;
					msg.data = null;
					msg.function = (byte) 0x0B;
					msg.length = CoverUtils.short2ByteArray((short) 7);
					byte[] check = CRC16M.getSendBuf(CoverUtils
							.bytes2HexString(CoverUtils
									.msg2ByteArrayExcepteCheck(msg)));
					msg.check[0] = check[check.length - 2];
					msg.check[1] = check[check.length - 1];
					((ParamSettingActivity) context).sendMessage(msg, ACTION);
				} else if (recv[3] == 0x02) {
					Toast.makeText(context, "set failed", Toast.LENGTH_LONG)
							.show();
				}
				// 需要在刷新列表的时候检测是否超限
			} else if (recv[0] == 0x09) {
				Toast.makeText(context, "终端参数设置命令发送成功", Toast.LENGTH_SHORT)
						.show();
			}
		}

	}

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
		CharSequence contentTitle = (entity.getTag().equals("level") ? "水位"
				: "井盖") + entity.getId(); // 通知栏标题
		CharSequence contentText = "参数设置失败"; // 通知栏内容
		Intent notificationIntent = new Intent(this, Detail.class); // 点击该通知后要跳转的Activity
		notificationIntent.putExtra("entity", entity);
		// startActivity(notificationIntent);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		// 把Notification传递给 NotificationManager
		mNotificationManager.notify(0, notification);
	}

}
