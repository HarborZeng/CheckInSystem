package cn.tellyouwhat.checkinsystem.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.tellyouwhat.checkinsystem.services.LocationGettingService;

/**
 * Created by Harbor-Laptop on 2017/3/22.
 * 开机启动位置监听服务
 */

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(context, LocationGettingService.class));
	}
}
