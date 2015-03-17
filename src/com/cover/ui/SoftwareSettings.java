package com.cover.ui;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cover.app.AppManager;
import com.cover.bean.Message;
import com.cover.main.MainActivity;
import com.cover.service.InternetService;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

public class SoftwareSettings extends Activity implements OnClickListener {
	private static final String TAG = "cover";
	private final String ACTION = "com.cover.service.IntenetService";
	private RelativeLayout rlIp;
	private ImageView back;
	private Switch swAlarm;
	private ImageView ivSwitch;
	private ImageView exit;
	private TextView tvIP;
	private int setOrNot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.software_settings);

		rlIp = (RelativeLayout) findViewById(R.id.rl_ip);
		back = (ImageView) findViewById(R.id.setting_back);
		swAlarm = (Switch) findViewById(R.id.alarm_switch);
		ivSwitch = (ImageView) findViewById(R.id.iv_switch);
		exit = (ImageView) findViewById(R.id.iv_exit);
		tvIP = (TextView) findViewById(R.id.tv_ip);
		back.setOnClickListener(this);
		swAlarm.setOnClickListener(this);
		rlIp.setOnClickListener(this);
		ivSwitch.setOnClickListener(this);
		exit.setOnClickListener(this);
		setOrNot = CoverUtils.getIntSharedP(getApplicationContext(),
				"setAlarmOrNot");
		if (setOrNot == 0) {

		}
		if (setOrNot == 1)
			swAlarm.setChecked(true);
		else {
			swAlarm.setChecked(false);
		}
		tvIP.setText(CoverUtils.getStringSharedP(getApplicationContext(), "ip")+":"
				+ CoverUtils.getIntSharedP(getApplicationContext(), "port"));

		AppManager.getAppManager().addActivity(this);
	}

	public static class SoftwareSettingsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {

		}

	}

	// public void sendChangeUser(Message msg, String action) {
	//
	// msg = CoverUtils.makeMessageExceptCheck((byte)0x12,
	// CoverUtils.short2ByteArray((short) 18), ("用户名").getBytes());
	// msg.check =
	// CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils.msg2ByteArrayExcepteCheck(msg)));
	// sendMessage( msg, ACTION);
	//
	// }

	// public void sendChangeIp(Message msg, String action) {
	// // Intent intent = new Intent("com.cover.service.InternetService");
	// // stopService(intent);
	// Intent mainActivity = new Intent();
	// mainActivity.putExtra("ip", "");
	// mainActivity.setClass(getApplicationContext(), MainActivity.class);
	// startActivity(mainActivity);
	// msg = CoverUtils.makeMessageExceptCheck((byte)0x12,
	// CoverUtils.short2ByteArray((short) 18), ("用户名").getBytes());
	// msg.check =
	// CRC16M.getSendBuf(CoverUtils.bytes2HexString(CoverUtils.msg2ByteArrayExcepteCheck(msg)));
	// sendMessage( msg, ACTION);
	//
	// }

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
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().finishActivity(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_back:
			// 保存所做的记录
			onBackPressed();
			break;
		case R.id.alarm_switch:
			if (swAlarm.isChecked()) {
				CoverUtils.putInt2SharedP(getApplicationContext(),
						"setAlarmOrNot", 1);
			} else {
				CoverUtils.putInt2SharedP(getApplicationContext(),
						"setAlarmOrNot", 0);
			}
			break;
		case R.id.rl_ip:
			// 弹出对话框
			final EditText et_Ip = new EditText(this);
			new AlertDialog.Builder(this)
					.setTitle("格式IP:PORT")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(et_Ip)
					.setPositiveButton(
							"确定",
							new android.content.DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 保存设置
									// 修改显示
									tvIP.setText(et_Ip.getText().toString()
											.trim());
									String ip_port = et_Ip.getText().toString()
											.trim();
									if (et_Ip.getText().toString().trim()
											.contains(":")) {
										CoverUtils.putString2SharedP(
												getApplicationContext(), "ip",
												tvIP.getText().toString()
														.trim());
										CoverUtils.putInt2SharedP(
												getApplicationContext(),
												"port",
												Integer.valueOf(ip_port.substring(
														ip_port.indexOf(":") + 1,
														ip_port.length())));
										Intent serviceIntent = new Intent();
										serviceIntent.setClass(
												SoftwareSettings.this,
												InternetService.class);
										stopService(serviceIntent);
										try {
											Thread.sleep(1000);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										startService(serviceIntent);
										Intent intent = new Intent();
										intent.setClass(SoftwareSettings.this,
												MainActivity.class);
										startActivity(intent);
									} else {
										Toast.makeText(getApplicationContext(),
												"格式不正确,请重新输入  IP：PORT",
												Toast.LENGTH_SHORT).show();
									}
								}

							}).setNegativeButton("取消", null).show();
			break;
		case R.id.iv_switch:
			Toast.makeText(this, "切换用户", 0).show();
			CoverUtils.putString2SharedP(getApplicationContext(), "username",
					"");
			CoverUtils.putString2SharedP(getApplicationContext(), "password",
					"");
			Intent intent = new Intent();
			intent.setClass(SoftwareSettings.this, MainActivity.class);
			startActivity(intent);
			break;
		case R.id.iv_exit:
			// Toast.makeText(this, "退出", 0).show();
			new AlertDialog.Builder(this)
					.setTitle("确定退出？")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setPositiveButton(
							"确定",
							new android.content.DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Message msg = new Message();
									msg.data = CoverUtils
											.getStringSharedP(
													getApplicationContext(),
													"username").getBytes();
									msg.function = (byte) 0x12;
									msg.length = CoverUtils
											.short2ByteArray((short) (7 + msg.data.length));
									byte[] checkMsg = CoverUtils
											.msg2ByteArrayExcepteCheck(msg);
									byte[] str_ = CRC16M.getSendBuf(CoverUtils
											.bytes2HexString(checkMsg));
									msg.check[0] = str_[str_.length - 1];
									msg.check[1] = str_[str_.length - 2];
									sendMessage(msg, ACTION);
								}

							}).setNegativeButton("取消", null).show();
		}
	}
}
