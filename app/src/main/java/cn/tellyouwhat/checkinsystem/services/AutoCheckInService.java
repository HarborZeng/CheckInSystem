package cn.tellyouwhat.checkinsystem.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cn.tellyouwhat.checkinsystem.activities.MainActivity;
import cn.tellyouwhat.checkinsystem.db.LocationDB;
import cn.tellyouwhat.checkinsystem.db.LocationItem;
import cn.tellyouwhat.checkinsystem.receivers.BatteryReceiver;
import cn.tellyouwhat.checkinsystem.utils.CookiedRequestParams;

/**
 * Created by HarborZeng on 2017/4/14.
 * This is a class for
 */

public class AutoCheckInService extends Service {
	private static final String TAG = "AutoCheckInService";

	static {
		Log.d(TAG, "static initializer: AutoCheckInService, 用于自动签到签出的服务已启动");
	}

	private static final int CHECK_OUT = 1;
	private static final int NOTHING_TODO = 0;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	private BatteryReceiver batteryReceiver;
	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case CHECK_OUT:
					//查数据库最后一条在公司的记录
					getThisMonthStatusTnenCheckOut();
					return true;
			}
			return false;
		}
	});
	private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			Log.d(TAG, "onSharedPreferenceChanged: checkInStatus这个设置变化了");
			switch (key) {
				case "has_notified_N_times":
					Log.d(TAG, "onSharedPreferenceChanged: 是这个东西：执行到has_notified_N_times");
					int hasNotifiedNTimes = sharedPreferences.getInt("has_notified_N_times", 0);
					//5次就是25min的时间
					if (hasNotifiedNTimes == 5) {
						LocationDB locationDB = new LocationDB();
						Calendar calendar = Calendar.getInstance();
						int year = calendar.get(Calendar.YEAR);
						int month = calendar.get(Calendar.MONTH) + 1;
						int day = calendar.get(Calendar.DAY_OF_MONTH);
						String yearString = String.valueOf(year);
						String monthString;
						if (month < 10) {
							monthString = "0" + month;
						} else {
							monthString = String.valueOf(month);
						}
						String dayString;
						if (day < 10) {
							dayString = "0" + day;
						} else {
							dayString = String.valueOf(day);
						}
						LocationItem item = locationDB.queryFirstRecordOfDay(yearString, monthString, dayString, employeeID);
						String time = item.getTime();
						Log.d(TAG, "onSharedPreferenceChanged: 从数据库查来的time:" + time);
						String hour = time.substring(11, 13);
						String minute = time.substring(14, 16);
						String second = time.substring(17, 19);
						CookiedRequestParams requestParams = new CookiedRequestParams("http://api.checkin.tellyouwhat.cn/CheckIn/AutoCheckIn?hour=" + hour + "&minute=" + minute + "&second=" + second);
						x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {
							@Override
							public void onSuccess(JSONObject result) {
								Log.d(TAG, "onSuccess: 自动签到返回的数据：" + result.toString());
								try {
									int resultInt = result.getInt("result");
									switch (resultInt) {
										case 1:
											//TODO 自动签到成功，发送一条通知
											Toast.makeText(x.app(), "自动签到成功", Toast.LENGTH_SHORT).show();
											break;
										case 0:
											//session失效，重新登录
											Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_LONG).show();
											Intent intent = new Intent(getApplicationContext(), MainActivity.class);
											intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											startActivity(intent);
											break;
										case -1:
											Toast.makeText(x.app(), "内部错误, 代码120", Toast.LENGTH_SHORT).show();
											break;
										case -2:
											Toast.makeText(x.app(), "自动签到失败，已签到", Toast.LENGTH_SHORT).show();
											break;
										default:
											break;
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}

							}

							@Override
							public void onError(Throwable ex, boolean isOnCallback) {

							}

							@Override
							public void onCancelled(CancelledException cex) {

							}

							@Override
							public void onFinished() {
							}
						});
					} else if (hasNotifiedNTimes > 5) {
						sharedPreferences.edit().putInt("has_notified_N_times", 0).apply();
					}
			}
		}
	};
	private String employeeID;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences infoSharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
		employeeID = infoSharedPreferences.getString("employeeID", "");

		SharedPreferences checkSharedPreferences = getSharedPreferences("checkInStatus", MODE_PRIVATE);
		checkSharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

		//系统电量过低时
		batteryReceiver = new BatteryReceiver();
		IntentFilter batteryfilter = new IntentFilter();
		batteryfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, batteryfilter);

		Calendar calendar = Calendar.getInstance();
		Date time = null;
		try {
			time = formatter.parse(calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + 1 + "-" + calendar.get(Calendar.DAY_OF_MONTH) + " 23:59:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long DAY_MILLIS = 86400000;
		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(CHECK_OUT);
			}
		}, time, DAY_MILLIS);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY_COMPATIBILITY;
	}

	@Override
	public void onDestroy() {
		Intent localIntent = new Intent();
		localIntent.setClass(this, AutoCheckInService.class); // 销毁时重新启动Service
		this.startService(localIntent);
		unregisterReceiver(batteryReceiver);
		super.onDestroy();
	}

	private void getThisMonthStatusTnenCheckOut() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		Log.i(TAG, "getThisMonthStatusTnenCheckOut: year is " + year + ", and month is " + month);
		int realMonth = month + 1;
		CookiedRequestParams params = new CookiedRequestParams("http://api.checkin.tellyouwhat.cn/CheckIn/GetMonthData?year=" + year + "&month=" + realMonth);
		x.http().get(params, new Callback.CommonCallback<JSONObject>() {
			@Override
			public void onSuccess(JSONObject result) {
				Log.i(TAG, "onSuccess: 本月的数据是：" + result);
				try {
					int resultInt = result.getInt("result");
					switch (resultInt) {
						case 1:
							LocationDB db = new LocationDB();
							LocationItem item = db.queryLastRecord(employeeID);
							String timeString = item.getTime();
							Date time = formatter.parse(timeString, new ParsePosition(0));
							int timeYear = time.getYear();
							int timeMonth = time.getMonth();
							int timeDay = time.getDate();
							int hour = time.getHours();
							int minute = time.getMinutes();
							int second = time.getSeconds();
							JSONArray resultJSONArray = result.getJSONArray("data");
							for (int i = 0; i < resultJSONArray.length(); i++) {
								JSONObject jsonObject = resultJSONArray.getJSONObject(i);
								String checkInID = jsonObject.getString("CheckInID");
								String checkInTime = jsonObject.getString("CheckInTime");
								if (checkInTime.startsWith(timeYear + "-" + timeMonth + "-" + timeDay)) {
									x.http().get(new CookiedRequestParams("http://api.checkin.tellyouwhat.cn/CheckIn/AutoCheckOut?id="
													+ checkInID + "&hour=" + hour + "&minute=" + minute + "&second=" + second),
											new CommonCallback<JSONObject>() {
												@Override
												public void onSuccess(JSONObject result) {
													Log.d(TAG, "onSuccess: 自动签出返回的数据是：" + result.toString());
													try {
														int resultAutoCheckOut = result.getInt("result");
														switch (resultAutoCheckOut) {
															case 1:
																//TODO 自动签出成功的通知
																Toast.makeText(x.app(), "自动签出成功", Toast.LENGTH_LONG).show();
																break;
															case 0:
																Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_LONG).show();
																//session失效，重新登录
																Intent intent = new Intent(getApplicationContext(), MainActivity.class);
																intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
																startActivity(intent);
																break;
															case -1:
																Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_LONG).show();
																break;
															case -2:
																Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_LONG).show();
																break;
															default:
																break;
														}
													} catch (JSONException e) {
														e.printStackTrace();
													}
												}

												@Override
												public void onError(Throwable ex, boolean isOnCallback) {

												}

												@Override
												public void onCancelled(CancelledException cex) {

												}

												@Override
												public void onFinished() {

												}
											});
								} else {
									Toast.makeText(getApplicationContext(), "今日还未签到\n自动签出失败", Toast.LENGTH_LONG).show();
								}
							}
							break;
						case 0:
//							updateSession();
							break;
						case -1:
							Toast.makeText(getApplicationContext(), "内部错误, 重启app再试试", Toast.LENGTH_LONG).show();
							break;
						default:
							break;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});
	}
}
