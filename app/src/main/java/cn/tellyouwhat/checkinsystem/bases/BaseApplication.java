package cn.tellyouwhat.checkinsystem.bases;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.xutils.x;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import cn.tellyouwhat.checkinsystem.utils.ConstantValues;

public class BaseApplication extends Application {
	List<Activity> activities = new LinkedList<>();
	List<Service> services = new LinkedList<>();

	@Override
	public void onCreate() {
		super.onCreate();
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


	public void addActivity(Activity activity) {
		activities.add(activity);
	}

	public void removeActivity(Activity activity) {
		activities.remove(activity);
	}

	public void addService(Service service) {
		services.add(service);
	}

	public void removeService(Service service) {
		services.remove(service);
	}

	public void closeApplication() {
		closeActivities();
		closeServices();
	}

	private void closeActivities() {
		ListIterator<Activity> iterator = activities.listIterator();
		while (iterator.hasNext()) {
			Activity activity = iterator.next();
			if (activity != null) {
				activity.finish();
			}
		}
	}

	private void closeServices() {
		ListIterator<Service> iterator = services.listIterator();
		while (iterator.hasNext()) {
			Service service = iterator.next();
			if (service != null) {
				stopService(new Intent(this, service.getClass()));
			}
		}
	}

}
