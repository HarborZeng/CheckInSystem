package cn.tellyouwhat.checkinsystem.activities;

import android.app.Application;

import org.xutils.x;

public class BaseApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		x.Ext.init(this);
//		x.Ext.setDebug(true);
	}
}
