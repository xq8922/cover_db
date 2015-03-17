package com.cover.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import com.cover.bean.Entity;
import com.cover.bean.Message;
import com.cover.ui.Detail;
import com.wxq.covers.R;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class CoverUtils {

	/**
	 * 
	 * @param context
	 * @param name
	 * @param value
	 */
	public static void putString2SharedP(Context context, String name,
			String value) {
		// 实例化SharedPreferences对象（第一步）
		SharedPreferences mySharedPreferences = context.getSharedPreferences(
				"douyatech", Activity.MODE_PRIVATE);
		// 实例化SharedPreferences.Editor对象（第二步）
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		// 用putString的方法保存数据
		editor.putString(name, value);
		// 提交当前数据
		editor.commit();
		// 使用toast信息提示框提示成功写入数据
		// Toast.makeText(context, "数据成功写入SharedPreferences！" ,
		// Toast.LENGTH_LONG).show();
	}

	public static void putInt2SharedP(Context context, String name, int value) {
		SharedPreferences sp = context.getSharedPreferences("douyatech",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(name, value);
		editor.commit();
	}

	public static String getStringSharedP(Context context, String name) {
		String value = null;
		// 同样，在读取SharedPreferences数据前要实例化出一个SharedPreferences对象
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"douyatech", Activity.MODE_PRIVATE);
		// 使用getString方法获得value，注意第2个参数是value的默认值
		value = sharedPreferences.getString(name, "");

		return value;
	}

	public static int getIntSharedP(Context context, String name) {
		int value = 0;
		SharedPreferences sp = context.getSharedPreferences("douyatech",
				Activity.MODE_PRIVATE);
		value = sp.getInt(name, 0);
		return value;
	}

	public static boolean getBooleanSharedP(Context context, String name) {
		boolean value = false;
		// 同样，在读取SharedPreferences数据前要实例化出一个SharedPreferences对象
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"douyatech", Activity.MODE_PRIVATE);
		// 使用getString方法获得value，注意第2个参数是value的默认值
		value = sharedPreferences.getBoolean(name, false);

		return value;
	}

	/**
	 * make askMessage
	 */
	public static Message makeMessageExceptCheck(byte function, byte[] length,
			byte[] data) {
		Message askMsg = new Message();
		askMsg.function = function;
		askMsg.length = length;
		askMsg.data = data;
		return askMsg;
	}

	/**
	 * 
	 * @param context
	 * @param msg
	 * @param action
	 */
	public static void sendMessage(Context context, Message msg, String action) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction(action);
		int length = msg.getLength();
		byte[] totalMsg = new byte[length];
		totalMsg = CoverUtils.msg2ByteArray(msg, length);
		serviceIntent.putExtra("msg", totalMsg);
		context.sendBroadcast(serviceIntent);
		Log.i("cover", action + "send broadcast " + action);
	}

	// 浮点到字节转换 
	public static byte[] double2Byte(double d) {
		byte[] b = new byte[8];
		long l = Double.doubleToLongBits(d);
		for (int i = 0; i < b.length; i++) {
			b[i] = new Long(l).byteValue();
			l = l >> 8;
		}
		return b;
	}

	public static double byte2Double(byte[] b) {
		long l;
		l = b[0];
		l &= 0xFF;
		l |= ((long) b[1] << 8);
		l &= 0xFFFF;
		l |= ((long) b[2] << 16);
		l &= 0xFFFFFF;
		l |= ((long) b[3] << 24);
		l &= 0xFFFFFFFFl;
		l |= ((long) b[4] << 32);
		l &= 0xffffffffffl;
		l |= ((long) b[5] << 40);
		l &= 0xffffffffffffl;
		l |= ((long) b[6] << 48);
		l |= ((long) b[7] << 56);
		return Double.longBitsToDouble(l);
	}

	public static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	/**
	 * judge network is available or not
	 * 
	 * @param activity
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return false;
		} else {
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
			return false;
		}

	}

	/**
	 * convert byte to char
	 * 
	 * @param cbuff
	 * @param charBuff
	 * @param size
	 * @return char[]
	 */
	public static char[] convertByteArrToChar(byte[] cbuff) {
		int size = cbuff.length;
		char[] charBuff = new char[size];
		for (int i = 0; i < charBuff.length; i++) {
			if (i < size) {
				charBuff[i] = (char) cbuff[i];
			} else {
				charBuff[i] = ' ';
			}
		}
		return charBuff;
	}

	/**
	 * get local phone ip
	 * 
	 * @return
	 */
	public static String getLocalIpAdress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * is crcRight
	 * 
	 * @return crc is right or not
	 */
	public static boolean isCRCRight(byte[] ucCRC_Buf, byte[] check) {
		short uiX, uiY, uiCRC;
		byte ucStart = 0;
		uiCRC = (short) 0xFFFF; // set all 1
		int ucBufLength = ucCRC_Buf.length;
		ucBufLength += ucStart;
		for (uiX = (short) ucStart; uiX < ucBufLength; uiX++) {
			uiCRC = (short) (uiCRC ^ ucCRC_Buf[uiX]);
			for (uiY = 0; uiY <= 7; uiY++) {
				if ((uiCRC & 1) != 0)
					uiCRC = (short) ((uiCRC >> 1) ^ 0xA001);
				else
					uiCRC = (short) (uiCRC >> 1);
			}
		}
		if (short2ByteArray(uiCRC) == check)
			return true;
		else
			return true;
		// return true;
	}

	/**
	 * ͨ��byte����ȡ��short
	 * 
	 * @param b
	 * 
	 * @return
	 */
	public static short getShort(byte[] b) {
		short sum = 0;
		for (int i = 0, k = 1, j = b.length - 1; i < b.length * 8; i++) {
			if (i == 8 * k) {
				j--;
				k++;
			}
			sum += (0x01 & b[j]) * Math.pow(2, i);
			b[j] >>= 1;
		}
		return sum;
	}

	public static short getShort(byte[] buf, boolean bBigEnding) {
		if (buf == null) {
			throw new IllegalArgumentException("byte array is null!");
		}
		if (buf.length > 2) {
			throw new IllegalArgumentException("byte array size > 2 !");
		}
		short r = 0;
		if (bBigEnding) {
			for (int i = 0; i < buf.length; i++) {
				r <<= 8;
				r |= (buf[i] & 0x00ff);
			}
		} else {
			for (int i = buf.length - 1; i >= 0; i--) {
				r <<= 8;
				r |= (buf[i] & 0x00ff);
			}
		}
		return r;
	}

	/**
	 * short2byte
	 * 
	 * @return return byte[]
	 */
	public static byte[] short2ByteArray(short s) {
		byte[] shortBuf = new byte[2];
		shortBuf[0] = (byte) ((s & 0xFF00) >> 8);
		shortBuf[1] = (byte) (s & 0xFF);
		System.out.println(shortBuf);
		return shortBuf;
	}

	/**
	 * form byte[] from Message
	 */
	public static byte[] msg2ByteArray(Message msg, int length) {
		byte[] totalMsg = new byte[length];
		int j = 0;
		totalMsg[j++] = msg.header1;
		totalMsg[j++] = msg.header2;
		for (int i = 0; i < msg.length.length; i++)
			totalMsg[j++] = msg.length[i];
		totalMsg[j++] = msg.function;
		for (int i = 0; i < (msg.data != null ? msg.data.length : 0); i++)
			totalMsg[j++] = msg.data[i];
		for (int i = 0; i < msg.check.length; i++)
			totalMsg[j++] = msg.check[i];
		return totalMsg;
	}

	/**
	 * form byte[] from Message except check
	 */
	public static byte[] msg2ByteArrayExcepteCheck(Message msg) {
		byte[] totalMsg = new byte[msg.length.length + 1
				+ (msg.data != null ? msg.data.length : 0)];
		int j = 0;
		for (int i = 0; i < msg.length.length; i++) {
			totalMsg[j++] = msg.length[i];
		}
		totalMsg[j++] = msg.function;
		for (int i = 0; i < (msg.data != null ? msg.data.length : 0); i++) {
			totalMsg[j++] = msg.data[i];
		}
		return totalMsg;
	}

	/**
	 * form byte[] from Message except check
	 */
	public static byte[] msg2ByteArrayExceptHeader(Message msg) {
		byte[] totalMsg = new byte[msg.length.length + 1 + msg.data.length
				+ msg.check.length];
		int j = 0;
		for (int i = 0; i < msg.length.length; i++) {
			totalMsg[j++] = msg.length[i];
		}
		totalMsg[j++] = msg.function;
		for (int i = 0; i < msg.data.length; i++) {
			totalMsg[j++] = msg.data[i];
		}
		for (int i = 0; i < msg.check.length; i++) {
			totalMsg[j++] = msg.check[i];
		}
		return totalMsg;
	}
	
	/**
	 * set notify
	 */
	public void setNotify(Entity entity) {
//		// 创建一个NotificationManager的引用
//		String ns = Context.NOTIFICATION_SERVICE;
//		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
//		// 定义Notification的各种属性
//		int icon = R.drawable.icon; // 通知图标
//		CharSequence tickerText = "报警信息"; // 状态栏显示的通知文本提示
//		long when = System.currentTimeMillis(); // 通知产生的时间，会在通知信息里显示
//		// 用上面的属性初始化 Nofification
//		Notification notification = new Notification(icon, tickerText, when);
//		// 添加声音
//		if (CoverUtils.getIntSharedP(getApplicationContext(), "setAlarmOrNot") == 1)
//			notification.defaults |= Notification.DEFAULT_ALL;
//		notification.defaults |= Notification.DEFAULT_LIGHTS;
//		notification.flags |= Notification.FLAG_AUTO_CANCEL;
//
//		/*
//		 * 更多的特征属性 notification.flags |= FLAG_AUTO_CANCEL; //在通知栏上点击此通知后自动清除此通知
//		 * 
//		 * notification.flags |= FLAG_ONGOING_EVENT;
//		 * //将此通知放到通知栏的"Ongoing"即"正在运行"组中 notification.flags |= FLAG_NO_CLEAR;
//		 * //表明在点击了通知栏中的"清除通知"后，此通知不清除， //经常与FLAG_ONGOING_EVENT一起使用
//		 * notification.number = 1; //number字段表示此通知代表的当前事件数量，它将覆盖在状态栏图标的顶部
//		 * //如果要使用此字段，必须从1开始 notification.iconLevel = ; //
//		 */
//		// 设置通知的事件消息
//		Context context = getApplicationContext(); // 上下文
//		CharSequence contentTitle = entity.getTag() + entity.getId(); // 通知栏标题
//		CharSequence contentText = entity.getLatitude() + ","
//				+ entity.getLongtitude(); // 通知栏内容
//
//		Intent notificationIntent = new Intent(this, Detail.class); // 点击该通知后要跳转的Activity
//		notificationIntent.putExtra("entity", entity);
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//		notification.setLatestEventInfo(context, contentTitle, contentText,
//				contentIntent);
//		// 把Notification传递给 NotificationManager
//		mNotificationManager.notify(0, notification);
	}

}
