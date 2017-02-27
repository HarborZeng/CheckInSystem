package cn.tellyouwhat.checkinsystem.bases;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.util.Log;

import org.xutils.x;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import cn.tellyouwhat.checkinsystem.database.AccountDao;
import cn.tellyouwhat.checkinsystem.domain.Account;

public class BaseApplication extends Application {
	List<Activity> activities = new LinkedList<>();
	List<Service> services = new LinkedList<>();
	private Account account;

	@Override
	public void onCreate() {
		super.onCreate();
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

	public Account getCurrentAccount() {
		if (account == null) {
			//TODO 添加GREENDAO代码
			AccountDao dao = new AccountDao(this);
			account = dao.getCurrentAccount();
		}
		return account;
	}

}
