package cn.tellyouwhat.checkinsystem.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cn.tellyouwhat.checkinsystem.services.LocationGettingService;


/**
 * Created by Harbor-Laptop on 2017/3/28.
 * 开启屏幕关闭屏幕启动{@link cn.tellyouwhat.checkinsystem.services.LocationGettingService}服务
 */

public class ScreenReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		boolean backGroundServiceEnabled = sharedPref.getBoolean("use_background_service", true);
		if (backGroundServiceEnabled) {
			String action = intent.getAction();
			if (Intent.ACTION_SCREEN_ON.equals(action)) {
				context.startService(new Intent(context, LocationGettingService.class));
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				context.startService(new Intent(context, LocationGettingService.class));
			}
		}

	}
}
