package com.cover.ui;

import com.cover.app.AppManager;
import com.cover.main.MainActivity;
import com.cover.service.InternetService;
import com.wxq.covers.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class WelcomeActivity extends Activity implements AnimationListener {
	private SharedPreferences sp;
	private View welcomeView;
	private boolean cbIsRemeber;
	static String userName;
	static String password;

	// InternetService internetService;
	// public ServiceConnection internetServiceConnection = new
	// ServiceConnection() {
	//
	// @Override
	// public void onServiceConnected(ComponentName arg0, IBinder service) {
	// internetService = ((InternetService.InterBinder) service)
	// .getService();
	// }
	//
	// @Override
	// public void onServiceDisconnected(ComponentName arg0) {
	// internetService = null;
	// }
	//
	// };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);

		startService(new Intent(WelcomeActivity.this, InternetService.class));
		Log.i("cover", "start service");
		welcomeView = findViewById(R.id.ll);
		Animation welAnimation = AnimationUtils.loadAnimation(this,
				R.anim.welcome_animation);
		welcomeView.startAnimation(welAnimation);
		welAnimation.setAnimationListener(this);
		AppManager.getAppManager().addActivity(this);
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		sp = getSharedPreferences("douyatech", MODE_PRIVATE);
		userName = sp.getString("username", "");
		password = sp.getString("password", "");
		cbIsRemeber = sp.getBoolean("isremem", false);
		if ((cbIsRemeber == true) && (userName != "") && (password != "")) {
			Intent intent = new Intent();
			intent.setClass(WelcomeActivity.this, CoverList.class);
			startActivity(intent);
			finish();
		} else {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().finishActivity(this);
	}

	@Override
	public void onBackPressed() {
		// 屏蔽返回键
	}
}
