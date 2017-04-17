package cn.tellyouwhat.checkinsystem.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cn.tellyouwhat.checkinsystem.services.AutoCheckInService;
import cn.tellyouwhat.checkinsystem.services.LocationGettingService;

/**
 * Created by Harbor-Laptop on 2017/3/22.
 * 开机启动位置监听服务
 */

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		boolean backGroundServiceEnabled = sharedPref.getBoolean("use_background_service", true);
		if (backGroundServiceEnabled) {
			intent = new Intent(context, LocationGettingService.class);
			context.startService(intent);
		}

		context.startService(new Intent(context, AutoCheckInService.class));
	}
}
