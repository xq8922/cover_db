package com.cover.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import com.cover.app.AppManager;
import com.cover.bean.Entity;
import com.cover.bean.Message;
import com.cover.bean.Entity.Status;
import com.cover.dbhelper.DouYaSqliteHelper;
import com.cover.dbhelper.Douyatech;
import com.cover.main.MainActivity;
import com.cover.ui.CoverList;
import com.cover.ui.Detail;
import com.cover.ui.ParamSettingActivity;
import com.cover.ui.SingleMapDetail;
import com.cover.util.CRC16M;
import com.cover.util.CoverUtils;
import com.wxq.covers.R;

import android.R.integer;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class InternetService extends Service implements Runnable {
	private final static String TAG = "COVER";
	private final int INTERVAL = 2 * 60 * 1000;

	private static final String ACTION_MainActivity = "com.cover.main.mainactivity";
	private static final String ACTION_CoverList = "com.cover.coverlist";
	private static final String ACTION_Detail = "com.cover.detail";
	private static final String ACTION_Settings = "com.cover.settings";
	// private static final String ACTION_Paramsetting = "com.cover.settings";
	private SharedPreferences sp;
	public String ip;
	public int port;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private Binder binder;
	private Thread thread;
	private String workStatus = "test";
	private String currAction;
	static boolean flag_send = false;
	static byte[] msg = null;
	ServiceReceiver myReceiver;
	boolean flagReaderThread = false;
	Message message = new Message();
	Douyatech douyadb;
	int count = 0;
	int flag_notify = 0;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0x01:
				Toast.makeText(getApplicationContext(), "连接中断请重新登陆",
						Toast.LENGTH_LONG).show();
				break;
			case 0x02:
				// Toast.makeText(getApplicationContext(), "连接中断",
				// Toast.LENGTH_LONG).show();
				break;
			case 0x03:
				// Toast.makeText(getApplicationContext(),
				// "Thread is not connected", Toast.LENGTH_LONG).show();
				break;
			case 0x04:
				// Toast.makeText(getApplicationContext(),
				// "network is not connected", Toast.LENGTH_LONG).show();
				break;
			case 0x05:
				// Toast.makeText(getApplicationContext(),
				// "连接中断", Toast.LENGTH_LONG).show();
				break;
			case 0x06:
				// Toast.makeText(getApplicationContext(), "连接中断",
				// Toast.LENGTH_LONG).show();
				break;
			case 0x07:
				Toast.makeText(getApplicationContext(), "服务器连接超时",
						Toast.LENGTH_SHORT).show();
				break;
			case 0x08:
				// Toast.makeText(getApplicationContext(), "连接中断请重新登陆",
				// Toast.LENGTH_LONG).show();
				break;
			case 0x10:
				Toast.makeText(getApplicationContext(), "未知功能码",
						Toast.LENGTH_LONG).show();
				break;
			case 0x11:
				Toast.makeText(getApplicationContext(), "撤防失败命令发送成功",
						Toast.LENGTH_LONG).show();
				break;
			case 0x12:
				Toast.makeText(getApplicationContext(), "参数设置失败命令发送成功",
						Toast.LENGTH_LONG).show();
				break;
			}
		}

	};
	private boolean flagConnectionOutOnce = true;

	public static class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			msg = intent.getByteArrayExtra("msg");
			Log.i(TAG, msg.toString());
			// Toast.makeText(context, "正在向服务器发送请求", Toast.LENGTH_SHORT).show();
			flag_send = true;
		}

	}

	public void sendRequest(byte[] action) {
		try {
			workStatus = null;
			currAction = action.toString();
			if (flag_send && msg != null) {
				sendMessage(action);
				flag_send = false;
				Log.i(TAG, "msg sent");
			}
		} catch (Exception e) {
			workStatus = "sendmessage fail";
		}
	}

	private void sendMessage(byte[] bs) {
		if (!CoverUtils.isNetworkAvailable(this)) {
			Log.v(TAG, "workStatus is not connectted");
			workStatus = "connect failed";
			handler.sendEmptyMessage(0x04);
			return;
		}
		if (socket == null) {
			handler.sendEmptyMessage(0x05);
			connectService();
		} else {
			if (bs != null) {
				try {
					writer = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(socket.getOutputStream())),
							true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (!InternetService.this.thread.isAlive()) {
			handler.sendEmptyMessage(0x03);
			(thread = new Thread(InternetService.this)).start();
		}
		if (!socket.isConnected() || socket.isClosed()) {
			handler.sendEmptyMessage(0x02);
			workStatus = "socket not connect";
			Log.v(TAG, "not connect");
			return;
		}
		if (!socket.isOutputShutdown()) {
			try {
				if (msg != null) {
					OutputStream socketWriter = socket.getOutputStream();
					socketWriter.write(msg);
					socketWriter.flush();
					Log.i(TAG, "msg sent " + msg.toString());
					if (msg[4] == (byte) 0x12) {
						AppManager.getAppManager().finishAllActivity();
						this.stopSelf();
					}
					msg = null;
				}
			} catch (Exception e) {
				handler.sendEmptyMessage(0x06);
				Log.v(TAG, "is not connect");
				e.printStackTrace();
				workStatus = "Output err";
			}
		} else {
			workStatus = "OutputShutdown";
		}
	}

	public String getWorkStatus() {
		return workStatus;
	}

	public void connectService() {
		try {
			socket = new Socket();
			SocketAddress socAddress = new InetSocketAddress(ip, port);
			socket.connect(socAddress, 3000);
			Log.i(TAG, "socket is connectted");

		} catch (SocketException e) {
			if (flagConnectionOutOnce)
				handler.sendEmptyMessage(0x07);
			flagConnectionOutOnce = false;
			Log.v(TAG, "time out");
			e.printStackTrace();
			workStatus = e.toString();
			return;
		} catch (IOException e) {
			Log.v(TAG, "time out");
			e.printStackTrace();
		}
	}

	public void getMessage(byte[] msg, String action) {
		try {
			Intent serviceIntent = new Intent();
			serviceIntent.setAction(action);
			serviceIntent.putExtra("msg", msg);
			sendBroadcast(serviceIntent);
			Log.i(TAG, "send Broadcast in InternetService && action is "
					+ action);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	public class InterBinder extends Binder {
		public InternetService getService() {
			return InternetService.this;
		}

		/**
		 * this is for Activity send messages to service
		 * 
		 * @data object when send to service
		 * @reply object that service returns
		 */
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}

	}

	@Override
	public void run() {
		connectService();
		int interval = 0;
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (socket.isConnected()) {
				interval++;
				if (interval > INTERVAL) {
					sendMessage("\n".getBytes());
					interval = 0;
				}
				if (!socket.isInputShutdown() && socket.isConnected()
						&& !socket.isOutputShutdown()) {
					if (!flagReaderThread) {
						new Thread(new Reader()).start();
						flagReaderThread = true;
					}
				}
				if (msg != null && flag_send) {
					sendMessage(msg);
					flag_send = false;
					Log.i(TAG, "message send to server .");
				}
				count = 0;
			} else {
				count++;
				connectService();
				handler.sendEmptyMessage(0x08);
				if (count == 10) {
					handler.sendEmptyMessage(0x01);
					Intent i = new Intent();
					i.setClass(this, MainActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i);

				}
			}
		}
	}

	private class Reader implements Runnable {

		@Override
		public void run() {
			// flagReaderThread = true;
			DataInputStream bufferedReader = null;
			try {
				bufferedReader = new DataInputStream(socket.getInputStream());
				Message msg = new Message();
				int size = 0;
				byte[] headerBuff = new byte[2];
				size = bufferedReader.read(headerBuff);

				if (!((headerBuff[0] == (byte) 0xFA) && (headerBuff[1] == (byte) 0xF5))) {
					flagReaderThread = false;
					return;
				}
				if (!flagReaderThread) {

				}
				byte[] length = new byte[2];
				size = bufferedReader.read(length);
				byte[] length_temp = new byte[2];
				for (int i = 0; i < length.length; i++) {
					length_temp[i] = length[i];
				}
				int msgLength = CoverUtils.getShort(length, true);
				System.out.println(msgLength - 6);
				byte[] msgBuff = new byte[msgLength - 6];
				size = bufferedReader.read(msgBuff);

				byte[] checkBuf = new byte[2];
				size = bufferedReader.read(checkBuf);

				msg.check[0] = checkBuf[0];
				msg.check[1] = checkBuf[1];
				byte[] totalMsg = new byte[msgLength];
				int j = 0;
				totalMsg[j++] = (byte) 0xFA;
				totalMsg[j++] = (byte) 0xF5;
				for (int i = 0; i < length.length; i++) {
					totalMsg[j++] = length_temp[i];
					msg.length[i] = length_temp[i];
				}
				msg.data = new byte[msgBuff.length - 1];
				for (int i = 0, k = 0; i < msgBuff.length; i++) {
					totalMsg[j++] = msgBuff[i];
					if (i == 0)
						msg.function = msgBuff[i];
					else {
						msg.data[k++] = msgBuff[i];
					}
				}
				totalMsg[j++] = checkBuf[0];
				totalMsg[j++] = checkBuf[1];
				byte[] checkMsg = CoverUtils.msg2ByteArrayExcepteCheck(msg);
				byte[] str_ = CRC16M.getSendBuf(CoverUtils
						.bytes2HexString(checkMsg));
				byte[] check_temp = new byte[2];
				check_temp[0] = str_[str_.length - 1];
				check_temp[1] = str_[str_.length - 2];
				if ((check_temp[0] == msg.check[0])
						&& (check_temp[1] == msg.check[1])) {
					Log.i(TAG, "check right");
					// if
					// (CRC16M.checkBuf(CoverUtils.msg2ByteArrayExceptHeader(msg)))
					// {
					switch (msgBuff[0]) {
					case 0x01: {// 需要处理报警信息ack
						//
						sendAskList();
						flag_notify++;
						byte[] idByte = new byte[2];
						Entity entity = new Entity();
						int i = 0;
						idByte[1] = msgBuff[i++ + 1];
						idByte[0] = msgBuff[i++ + 1];
						entity.setId(CoverUtils.getShort(idByte));
						entity.setTag(msgBuff[i++ + 1] == (byte) 0x10 ? "cover"
								: "level");
						byte[] longTi = new byte[8];
						for (int k = 0, t = i; i < t + 8; i++) {
							longTi[k++] = msgBuff[i + 1];
						}
						byte[] laTi = new byte[8];
						for (int k = 0, t = i; i < t + 8; i++) {
							laTi[k++] = msgBuff[i + 1];
						}
						entity.setLongtitude(CoverUtils.byte2Double(longTi));
						entity.setLatitude(CoverUtils.byte2Double(laTi));
						int tempStatus = -1;
						switch (msgBuff[i++ + 1]) {
						case 0x01:
							entity.setStatus(Status.NORMAL);
							tempStatus = 0;
							break;
						case 0x02:
							entity.setStatus(Status.EXCEPTION_1);
							tempStatus = 1;
							break;
						case 0x03:
							tempStatus = 2;
							entity.setStatus(Status.REPAIR);
							break;
						case 0x04:
							tempStatus = 3;
							entity.setStatus(Status.EXCEPTION_2);
							break;
						case 0x05:
							tempStatus = 4;
							entity.setStatus(Status.EXCEPTION_3);
							break;
						case 0x06:// 处理接收到撤防或者0x07的设置中时，删除数据路相应条目
							tempStatus = 5;
							entity.setStatus(Status.SETTING_FINISH);
							break;
						case 0x07:
							tempStatus = 6;
							entity.setStatus(Status.SETTING_PARAM);
							break;
						}

						// 处理若有从撤防中状态改变成正常状态
						if (douyadb.isExist("leave",
								entity.getTag().equals("level") ? "水位" : "井盖"
										+ "_" + entity.getId())
								&& (entity.getStatus() == Status.NORMAL)) {
							Detail.flagIsSetSuccess = true;
							if (douyadb.isExist("leave", entity.getTag() + "_"
									+ entity.getId()))
								douyadb.delete("leave", entity.getTag() + "_"
										+ entity.getId());
						}
						if (douyadb.isExist("setting", entity.getTag() + "_"
								+ entity.getId())
								&& (entity.getStatus() == Status.NORMAL)) {
							ParamSettingActivity.flagIsSetSuccess = true;
							if (douyadb.isExist("setting", entity.getTag()
									+ "_" + entity.getId()))
								douyadb.delete("setting", entity.getTag() + "_"
										+ entity.getId());
						}
						// 修改数据库
						if (douyadb.isExistInEntity(entity.getTag(), ""
								+ entity.getId())) {
							douyadb.updateColumn("entity", entity.getTag(),
									entity.getId() + "",
									entity.getLongtitude(),
									entity.getLatitude(), tempStatus, true);
						}
						setNotify(entity, "报警信息");
						byte[] ackAlert = new byte[] { (byte) 0xFA,
								(byte) 0xF5, (byte) 0x00, (byte) 0x07,
								(byte) 0x0A };
						byte[] checkAck = CRC16M.getSendBuf(CoverUtils
								.bytes2HexString(ackAlert));
						sendMessage(checkAck);
						break;
					}
					case 0x02: {
						sendAskList();
						flag_notify++;
						Entity entity = new Entity();
						byte[] b = new byte[2];
						b[1] = msgBuff[1];
						b[0] = msgBuff[2];
						String title = (msgBuff[3] == (byte) 0x1C ? "水位" : "井盖");
						entity.setId(CoverUtils.getShort(b));
						entity.setTag(title);
						byte[] lati = new byte[8];
						byte[] lonti = new byte[8];
						int j1 = 4;
						for (int i = 0; i < 8; i++) {
							lonti[i] = msgBuff[j1++];
						}
						for (int i = 0; i < 8; i++) {
							lati[i] = msgBuff[j1++];
						}
						entity.setLatitude(CoverUtils.byte2Double(lati));
						entity.setLongtitude(CoverUtils.byte2Double(lonti));
						// 修改数据库
						if (douyadb.isExistInEntity(entity.getTag(), ""
								+ entity.getId())) {
							douyadb.updateLonLa("entity", entity.getTag(),
									entity.getId() + "",
									entity.getLongtitude(),
									entity.getLatitude());
						}
						setNotify(entity, "终端信息改变");
						break;
					}
					case 0x03: {
						getMessage(msgBuff, ACTION_MainActivity);
						getMessage(msgBuff, ACTION_CoverList);
						break;
					}
					case 0x04: {
						getMessage(msgBuff, ACTION_CoverList);
						{
							final int dataLength = 20;
							int tempStatus = -1;
							int numOfEntity = (msgBuff.length - 1) / dataLength;
							byte[] idByte = new byte[2];
							int i = 0;
							for (int j1 = 0; j1 < numOfEntity; j1++) {
								Entity entity = new Entity();
								idByte[1] = msgBuff[i++ + 1];
								idByte[0] = msgBuff[i++ + 1];
								entity.setId(CoverUtils.getShort(idByte));
								entity.setTag(msgBuff[i++ + 1] == (byte) 0x10 ? "cover"
										: "level");
								byte[] longTi = new byte[8];
								for (int k = 0, t = i; i < t + 8; i++) {
									longTi[k++] = msgBuff[i + 1];
								}
								byte[] laTi = new byte[8];
								for (int k = 0, t = i; i < t + 8; i++) {
									laTi[k++] = msgBuff[i + 1];
								}
								entity.setLongtitude(CoverUtils
										.byte2Double(longTi));
								entity.setLatitude(CoverUtils.byte2Double(laTi));
								switch (msgBuff[i++ + 1]) {
								case 0x01:
									tempStatus = 0;
									entity.setStatus(Status.NORMAL);
									break;
								case 0x02:
									tempStatus = 1;
									entity.setStatus(Status.EXCEPTION_1);
									break;
								case 0x03:
									tempStatus = 2;
									entity.setStatus(Status.REPAIR);
									break;
								case 0x04:
									tempStatus = 3;
									entity.setStatus(Status.EXCEPTION_2);
									break;
								case 0x05:
									tempStatus = 4;
									entity.setStatus(Status.EXCEPTION_3);
									break;
								case 0x06:
									tempStatus = 5;
									entity.setStatus(Status.SETTING_FINISH);
									break;
								case 0x07:
									tempStatus = 6;
									entity.setStatus(Status.SETTING_PARAM);
									break;
								}
								if (!douyadb.isExistInEntity(entity.getTag(),
										entity.getId() + "")) {
									douyadb.addEntity(entity.getId() + "",
											entity.getStatus(), entity.getTag(),
											entity.getLongtitude(),
											entity.getLatitude());
								}
							}
						}
						break;
					}
					case 0x05: {
						// 终端参数设置回复，应该显示到通知栏
						// getMessage(msgBuff, ACTION_Paramsetting);
						sendAskList();
						ParamSettingActivity.flagIsSetSuccess = true;
						Entity entity = new Entity();
						byte[] b = new byte[2];
						b[0] = msgBuff[1];
						b[1] = msgBuff[2];
						String title = (msgBuff[3] == (byte) 0x10 ? "井盖 "
								: "水位 ");
						entity.setId(CoverUtils.getShort(b));
						entity.setTag(title);
						if (msgBuff[3] == (byte) 0x02) {
							setNotify(entity.getTag() + "_" + entity.getId(),
									"设置失败");
						} else {
							setNotify(entity.getTag() + "_" + entity.getId(),
									"设置成功");
						}
						if (douyadb.isExist("setting", entity.getTag() + "_"
								+ entity.getId())
								&& (entity.getStatus() == Status.NORMAL)) {
							ParamSettingActivity.flagIsSetSuccess = true;
							if (douyadb.isExist("setting", entity.getTag()
									+ "_" + entity.getId()))
								douyadb.delete("setting", entity.getTag() + "_"
										+ entity.getId());
						}
						break;
					}
					case 0x06:
					case 0x07:
						getMessage(msgBuff, ACTION_Detail);
						break;
					case 0x09:
						getMessage(msgBuff, ACTION_Settings);
						break;
					case 0x0A:// 报警解除失败命令接收成功的ACK信息
						handler.sendEmptyMessage(0x11);
						break;
					case 0x0B:// 终端参数设置失败命令接收成功的ACK信息
						handler.sendEmptyMessage(0x12);
						break;
					default:
						handler.sendEmptyMessage(0x10);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			flagReaderThread = false;
		}
	}

	@SuppressWarnings("deprecation")
	public void setNotify(Entity entity, String title) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		// 定义Notification的各种属性
		int icon = R.drawable.icon; // 通知图标
		CharSequence tickerText = title; // 状态栏显示的通知文本提示
		long when = System.currentTimeMillis(); // 通知产生的时间，会在通知信息里显示
		Notification notification = new Notification(icon, tickerText, when);
		if (CoverUtils.getIntSharedP(getApplicationContext(), "setAlarmOrNot") == 1)
			notification.defaults |= Notification.DEFAULT_ALL;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Context context = getApplicationContext(); // 上下文
		CharSequence contentTitle = (entity.getTag().equals("level") ? "水位"
				: "井盖") + entity.getId(); // 通知栏标题
		CharSequence contentText = entity.getLatitude() + ","
				+ entity.getLongtitude(); // 通知栏内容
		Intent notificationIntent = new Intent(this, SingleMapDetail.class); // 点击该通知后要跳转的Activity
		notificationIntent.putExtra("entity", entity);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		// 把Notification传递给 NotificationManager
		mNotificationManager.notify(flag_notify, notification);
	}

	public void sendAskList() {
		Message askMsg = new Message();
		askMsg.function = (byte) 0x0D;
		askMsg.data = null;
		askMsg.length = CoverUtils.short2ByteArray((short) 7);
		byte[] checkAsk = CoverUtils.msg2ByteArrayExcepteCheck(askMsg);
		byte[] tmp_str = CRC16M
				.getSendBuf(CoverUtils.bytes2HexString(checkAsk));
		askMsg.check[0] = tmp_str[tmp_str.length - 1];
		askMsg.check[1] = tmp_str[tmp_str.length - 2];
		int lengthAsk = askMsg.getLength();
		byte[] totalAsk = new byte[lengthAsk];
		totalAsk = CoverUtils.msg2ByteArray(askMsg, lengthAsk);
		sendMessage(totalAsk);
	}

	public void setNotify(String tagID, String arg) {
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
		CharSequence contentTitle = tagID; // 通知栏标题
		CharSequence contentText = arg; // 通知栏内容

		notification.setLatestEventInfo(context, contentTitle, contentText,
				null);
		// 把Notification传递给 NotificationManager
		mNotificationManager.notify(0, notification);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		binder = new InterBinder();
		// thread = new Thread(InternetService.this);
		// thread.start();
		return binder;
	}

	@Override
	public void onCreate() {
		sp = getSharedPreferences("douyatech", MODE_PRIVATE);
		if (CoverUtils.getStringSharedP(getApplicationContext(), "ip") == "")
			CoverUtils.putString2SharedP(getApplicationContext(), "ip",
					"124.115.169.98");
		if (CoverUtils.getIntSharedP(getApplicationContext(), "port") == 0)
			CoverUtils.putInt2SharedP(getApplicationContext(), "port", 6221);
		ip = sp.getString("ip", "");
		port = sp.getInt("port", 0);
		douyadb = new Douyatech(getApplicationContext());
		myReceiver = new ServiceReceiver();
		thread = new Thread(InternetService.this);
		thread.start();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 保证退出程序时候不再开启
		flagReaderThread = true;
		super.onDestroy();
		Log.v("QLQ", "service is on destroy");
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {

		// String ip = intent.getStringExtra("ip");
		// if (ip != null) {
		// this.ip = ip;
		// }
		super.onStart(intent, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

}
