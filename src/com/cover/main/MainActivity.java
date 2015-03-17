package com.cover.main;

import com.cover.app.AppManager;
import com.cover.bean.Entity;
import com.cover.bean.Message;
import com.cover.bean.Entity.Status;
import com.cover.service.InternetService;
import com.cover.ui.CoverList;
import com.cover.ui.Detail;
import com.cover.ui.SoftwareSettings;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	final static String TAG = "MainActivity";
	private final String ACTION = "com.cover.service.IntenetService";
	String msg = null;
	static byte[] recv;
	Message msgAsk = new Message();
	private EditText et_username;
	private EditText et_password;
	static String userName;
	static String password;
	private ImageView btn_test;
	private SharedPreferences sp;
	private TextView tvChangeIP;
	private MainActivityReceiver receiver;
	private static Editor editor;
	private static CheckBox cbIsRemeber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		receiver = new MainActivityReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.cover.main.mainactivity");
		this.registerReceiver(receiver, filter);

		et_username = (EditText) findViewById(R.id.et_login_user_name);
		et_password = (EditText) findViewById(R.id.et_login_password);
		tvChangeIP = (TextView) findViewById(R.id.tv_changeip);
		cbIsRemeber = (CheckBox) findViewById(R.id.cb_is_remeber);
		sp = getSharedPreferences("douyatech", MODE_PRIVATE);
		editor = sp.edit();
		userName = sp.getString("username", "");
		password = sp.getString("password", "");
		et_username.setText(userName);
		et_password.setText(password);
		cbIsRemeber.setChecked(sp.getBoolean("isremem", false));

		// if (!CoverUtils.isNetworkAvailable(MainActivity.this)) {
		// Toast.makeText(getApplicationContext(), "Network is offline",
		// Toast.LENGTH_SHORT).show();
		// return;
		// }

		tvChangeIP.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final EditText et_Ip = new EditText(MainActivity.this);
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("更改IP和端口")
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
										String ip_port = et_Ip.getText()
												.toString().trim();

										if (et_Ip.getText().toString().trim()
												.contains(":")) {
											editor.putString("ip", ip_port
													.substring(0, ip_port
															.indexOf(":")));
											editor.putInt(
													"port",
													Integer.valueOf(ip_port.substring(
															ip_port.indexOf(":") + 1,
															ip_port.length())));
											// 本地化 下次从文件读取一下 getString
											editor.commit();
											Intent serviceIntent = new Intent();
											serviceIntent.setClass(
													MainActivity.this,
													InternetService.class);
											finish();
											stopService(serviceIntent);
											try {
												Thread.sleep(1500);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											startService(serviceIntent);

										} else {
											Toast.makeText(
													getApplicationContext(),
													"格式不正确,请重新输入",
													Toast.LENGTH_SHORT).show();
										}

									}
								}).setNegativeButton("取消", null).show();
			}
		});

		btn_test = (ImageView) findViewById(R.id.btn_login);
		btn_test.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				userName = et_username.getText().toString().trim();
				password = et_password.getText().toString();
				
				// Intent i1 = new Intent();
				// i1.setClass(MainActivity.this, CoverList.class);
				// startActivity(i1);
				
				// userName = "13468833168";
				// password = "1234";
				if (userName == null || password == null) {
					Toast.makeText(getApplicationContext(), "用户名或密码不能为空",
							Toast.LENGTH_SHORT).show();
				}
				String msg = userName + password;
				int length = 7 + msg.length();
				msgAsk.data = msg.getBytes();
				msgAsk.function = Integer.valueOf("0C", 16).byteValue();
				msgAsk.length = CoverUtils.short2ByteArray((short) length);
				byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(msgAsk);
				byte[] str_ = CRC16M.getSendBuf(CoverUtils
						.bytes2HexString(checkMsg));
				msgAsk.check[0] = str_[str_.length - 1];
				msgAsk.check[1] = str_[str_.length - 2];
				// setNotify();
				sendMessage(msgAsk, ACTION);
			}
		});
		AppManager.getAppManager().addActivity(this);
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

	//
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// super.onCreateOptionsMenu(menu);
	// // 通过MenuInflater将XML 实例化为 Menu Object
	// MenuInflater inflater = getMenuInflater();
	// inflater.inflate(R.layout.menu, menu);
	// return true;
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case R.id.item_exit_settings:
	// // System.exit(0);
	// Message msg = new Message();
	// msg.data = CoverUtils.getStringSharedP(getApplicationContext(),
	// "username").getBytes();
	// msg.function = (byte) 0x12;
	// msg.length = CoverUtils
	// .short2ByteArray((short) (7 + msg.data.length));
	// byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(msg);
	// byte[] str_ = CRC16M.getSendBuf(CoverUtils
	// .bytes2HexString(checkMsg));
	// msg.check[0] = str_[str_.length - 1];
	// msg.check[1] = str_[str_.length - 2];
	// sendMessage(msg, ACTION);
	// }
	// return super.onOptionsItemSelected(item);
	// }

	@Override
	protected void onDestroy() {
		// unbindService(internetServiceConnection);
		// unregisterReceiver(myBroadcast);
		super.onDestroy();
		AppManager.getAppManager().finishActivity(this);
		if (receiver != null)
			this.unregisterReceiver(receiver);

	}

	class MainActivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			recv = intent.getByteArrayExtra("msg");
			if (recv[0] == 0x03) {
				switch (recv[1]) {
				case 0x01:
					Intent i = new Intent();
					i.setClass(context, CoverList.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					if (cbIsRemeber.isChecked()) {
						editor.putString("password", password);
						editor.putBoolean("isremem", true);
					}
					editor.putString("username", userName);
					editor.commit();
					context.startActivity(i);
					finish();
					break;
				case 0x02:
					Toast.makeText(context, "用户名或密码错误", Toast.LENGTH_LONG)
							.show();
					break;
				case 0x03:
					// Toast.makeText(context, "用户已登录",
					// Toast.LENGTH_LONG).show();
					editor.putString("username", userName);
					editor.commit();
					Intent i1 = new Intent();
					i1.setClass(context, CoverList.class);
					startActivity(i1);
					finish();
					break;
				default:
					Log.w(TAG, "wrong code");
					Toast.makeText(context, "错误指令", Toast.LENGTH_LONG).show();
				}
			}
		}

	}

}
