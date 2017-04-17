package cn.tellyouwhat.checkinsystem.bases;

import android.app.Application;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.xutils.x;

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
		Log.d("ChatApplication", "init");
	}

	private void registerToWeChat() {
		api = WXAPIFactory.createWXAPI(super.getApplicationContext(), ConstantValues.WX_APP_ID, true);
		api.registerApp(ConstantValues.WX_APP_ID);
	}

}
