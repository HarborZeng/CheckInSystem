package cn.tellyouwhat.checkinsystem.bases;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.UserManager;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xdandroid.hellodaemon.DaemonEnv;

import org.xutils.x;

import java.lang.reflect.Method;

import cn.tellyouwhat.checkinsystem.BuildConfig;
import cn.tellyouwhat.checkinsystem.services.LocationGettingService;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;

public class BaseApplication extends Application {
	public static IWXAPI api;

	@Override
	public void onCreate() {
		super.onCreate();
		registerToWeChat();
		if (LeakCanary.isInAnalyzerProcess(this)) {
			// This process is dedicated to LeakCanary for heap analysis.
			// You should not init your app in this process.
			return;
		}
		LeakCanary.install(this);
		// Normal app init code...

		x.Ext.init(this);
//		x.Ext.setDebug(true);
//		Log.d("ChatApplication", "init");
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			//fix android leak fix which is caused by UserManager holding on to a activity ctx
			try {
				final Method m;

				m = UserManager.class.getMethod("get", Context.class);

				m.setAccessible(true);
				m.invoke(null, this);

				//above is reflection for below...
				//UserManager.get();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (Throwable e) {
				if (BuildConfig.DEBUG) {
					throw new RuntimeException(e);
				}
			}
		}
		DaemonEnv.initialize(this, LocationGettingService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
	}

	private void registerToWeChat() {
		api = WXAPIFactory.createWXAPI(super.getApplicationContext(), ConstantValues.WX_APP_ID, true);
		api.registerApp(ConstantValues.WX_APP_ID);
	}

}
