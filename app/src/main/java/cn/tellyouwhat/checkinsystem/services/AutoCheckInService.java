package cn.tellyouwhat.checkinsystem.services;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.xdandroid.hellodaemon.AbsWorkService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.http.cookie.DbCookieStore;
import org.xutils.x;

import java.net.HttpCookie;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.activities.MainActivity;
import cn.tellyouwhat.checkinsystem.bean.LocationItem;
import cn.tellyouwhat.checkinsystem.db.LocationDB;
import cn.tellyouwhat.checkinsystem.receivers.BatteryReceiver;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.CookiedRequestParams;
import cn.tellyouwhat.checkinsystem.utils.EncryptUtil;
import cn.tellyouwhat.checkinsystem.utils.NotifyUtil;

/**
 * Created by HarborZeng on 2017/4/14.
 * This is a class for auto check in
 */

public class AutoCheckInService extends AbsWorkService {
	private static final String TAG = "AutoCheckInService";

	static {
		Log.d(TAG, "static initializer: AutoCheckInService, 用于自动签到签出的服务已启动");
	}

	private static final int CHECK_OUT = 1;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	private BatteryReceiver batteryReceiver;
	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case CHECK_OUT:
					//查数据库最后一条在公司的记录
//					Log.d(TAG, "handleMessage: getThisMonthStatusThenCheckOut方法执行之前");
					getThisMonthStatusThenCheckOut();
					return true;
			}
			return false;
		}
	});
	private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//			Log.d(TAG, "onSharedPreferenceChanged: checkInStatus这个设置变化了");
			switch (key) {
				case "has_notified_N_times":
//					Log.d(TAG, "onSharedPreferenceChanged: 是这个东西：执行到has_notified_N_times");
					int hasNotifiedNTimes = sharedPreferences.getInt("has_notified_N_times", 0);
					//5次就是25min的时间
					if (hasNotifiedNTimes == 5) {
//						updateSession();
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
						if (TextUtils.isEmpty(employeeID)) {
							SharedPreferences infoSharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
							employeeID = infoSharedPreferences.getString("employeeID", "");
						}
						LocationItem item = locationDB.queryFirstRecordOfDay(yearString, monthString, dayString, employeeID);
						String time = item.getTime();
						Log.d(TAG, "onSharedPreferenceChanged: 从数据库查来的time:" + time);
						final String hour = time.substring(11, 13);
						final String minute = time.substring(14, 16);
						final String second = time.substring(17, 19);

						//updateSession
						final SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
						String userName = userInfo.getString("USER_NAME", "");
						String encryptedToken = userInfo.getString(ConstantValues.TOKEN, "");
						String token = EncryptUtil.decryptBase64withSalt(encryptedToken, ConstantValues.SALT);
						if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(token)) {
							RequestParams p = new RequestParams("https://api.checkin.tellyouwhat.cn/User/UpdateSession?username=" + userName + "&deviceid=" + Build.SERIAL + "&token=" + token);
							x.http().get(p, new Callback.CommonCallback<JSONObject>() {
								private int resultInt;

								@Override
								public void onSuccess(JSONObject result) {
									try {
										resultInt = result.getInt("result");
									} catch (JSONException e) {
										e.printStackTrace();
									}
									switch (resultInt) {
										case 1:
											DbCookieStore instance = DbCookieStore.INSTANCE;
											List<HttpCookie> cookies = instance.getCookies();
											for (HttpCookie cookie : cookies) {
												String name = cookie.getName();
												String value = cookie.getValue();
												if (ConstantValues.COOKIE_NAME.equals(name)) {
													SharedPreferences preferences = x.app().getSharedPreferences(ConstantValues.COOIKE_SHARED_PREFERENCE_NAME, MODE_PRIVATE);
													SharedPreferences.Editor editor = preferences.edit();
													editor.putString(ConstantValues.cookie, value);
													editor.apply();
													break;
												}
											}
											//开始自动签到
											CookiedRequestParams requestParams = new CookiedRequestParams("https://api.checkin.tellyouwhat.cn/CheckIn/AutoCheckIn?hour=" +
													hour + "&minute=" + minute + "&second=" + second);
											x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {
												@Override
												public void onSuccess(JSONObject result) {
//													Log.d(TAG, "onSuccess: 自动签到返回的数据：" + result.toString());
													try {
														int resultInt = result.getInt("result");
														switch (resultInt) {
															case 1:
																SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
																boolean showNotifications = sharedPref.getBoolean("show_notifications", true);
																boolean notificationsRingEnabled = sharedPref.getBoolean("notifications_ring_enabled", false);
																boolean notificationsVibrateEnabled = sharedPref.getBoolean("notifications_vibrate_enabled", true);
																if (showNotifications) {
																	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
																	intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
																	PendingIntent pIntent = PendingIntent.getActivity(x.app(), 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
																	String ticker = "您有一条新通知";
																	String title = "签到成功";
																	String content = "恭喜您，今日自动签到成功";
																	NotifyUtil notificationSucceededCheckIn = new NotifyUtil(x.app(), 3);
																	notificationSucceededCheckIn.setOnGoing(false);
																	notificationSucceededCheckIn.notify_normal_singline(pIntent, R.drawable.ic_stat_name, ticker, title, content,
																			notificationsRingEnabled, notificationsVibrateEnabled, true);
																}
																Toast.makeText(x.app(), "自动签到成功", Toast.LENGTH_SHORT).show();
																updateTodayStatus();
																break;
															case 0:
																//session失效，重新登录
																updateSession();
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
											//自动签到结束
											break;
										case 0:
											try {
												Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_SHORT).show();
											} catch (JSONException e) {
												e.printStackTrace();
											}
											break;
										case -1:
											Toast.makeText(x.app(), "发生了不可描述的错误009", Toast.LENGTH_SHORT).show();
											break;
										default:
											break;
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
					} else if (hasNotifiedNTimes > 5) {
						sharedPreferences.edit().putInt("has_notified_N_times", 0).apply();
					}
			}
		}
	};

	private void updateTodayStatus() {
		CookiedRequestParams requestParams = new CookiedRequestParams("https://api.checkin.tellyouwhat.cn/CheckIn/GetTodayStatus");
		x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {
			@Override
			public void onSuccess(JSONObject result) {
//				Log.i(TAG, "onSuccess: 今日状态是：" + result.toString());
				int resultInt = -1;
				try {
					resultInt = result.getInt("result");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				switch (resultInt) {
					case 1:
						try {
							Boolean hasCheckIn = result.getBoolean("hascheckin");
							Boolean hasCheckOut = result.getBoolean("hascheckout");
							SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putBoolean("has_check_in", hasCheckIn);
							editor.putBoolean("has_check_out", hasCheckOut);
							editor.apply();
							Log.i(TAG, "onSuccess: 今日状态已更新");
						} catch (JSONException e) {
							e.printStackTrace();
							Log.i(TAG, "onSuccess: 今日状态更新出错，json解析异常");
						}
						break;
					case 0:
						updateSession();
						break;
					case -1:
						try {
							Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_LONG).show();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						break;
					default:
						break;
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				Toast.makeText(x.app(), "获取今日状态出错", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});
	}

	private void updateSession() {
		SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
		String userName = sharedPreferences.getString("USER_NAME", "");
		String encryptedToken = sharedPreferences.getString(ConstantValues.TOKEN, "");
		String token = EncryptUtil.decryptBase64withSalt(encryptedToken, ConstantValues.SALT);
		if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(token)) {
			@SuppressLint("HardwareIds") String deviceID = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
			RequestParams p = new RequestParams("https://api.checkin.tellyouwhat.cn/User/UpdateSession?username=" + userName + "&deviceid=" + deviceID + "&token=" + token);
			x.http().get(p, new Callback.CommonCallback<JSONObject>() {
				private int resultInt;

				@Override
				public void onSuccess(JSONObject result) {
					try {
						resultInt = result.getInt("result");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					switch (resultInt) {
						case 1:
							DbCookieStore instance = DbCookieStore.INSTANCE;
							List<HttpCookie> cookies = instance.getCookies();
							for (HttpCookie cookie : cookies) {
								String name = cookie.getName();
								String value = cookie.getValue();
								if (ConstantValues.COOKIE_NAME.equals(name)) {
									SharedPreferences preferences = x.app().getSharedPreferences(ConstantValues.COOIKE_SHARED_PREFERENCE_NAME, MODE_PRIVATE);
									SharedPreferences.Editor editor = preferences.edit();
									editor.putString(ConstantValues.cookie, value);
									editor.apply();
									break;
								}
							}
							break;
						case 0:
							try {
								Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_SHORT).show();
							} catch (JSONException e) {
								e.printStackTrace();
							}
							break;
						case -1:
							Toast.makeText(x.app(), "发生了不可描述的错误009", Toast.LENGTH_SHORT).show();
							break;
						default:
							break;
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
		IntentFilter batteryFilter = new IntentFilter();
		batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, batteryFilter);

		Date date = new Date();
//		Log.d(TAG, "onCreate: 年份是：" + date.getYear());
		Date time = null;
		try {
			time = formatter.parse((date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + " 23:59:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long DAY_MILLIS = 86400000;
		Log.d(TAG, "onCreate: time is " + time);
		SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
		String token = userInfo.getString(ConstantValues.TOKEN, "");
		if (TextUtils.isEmpty(token)) {
			new Timer(true).schedule(new TimerTask() {
				@Override
				public void run() {
					handler.sendEmptyMessage(CHECK_OUT);
				}
			}, time, DAY_MILLIS);
		}
	}

	@Override
	public Boolean shouldStopService(Intent intent, int flags, int startId) {
		return false;
	}

	@Override
	public void startWork(Intent intent, int flags, int startId) {

	}

	@Override
	public void stopWork(Intent intent, int flags, int startId) {

	}

	@Override
	public Boolean isWorkRunning(Intent intent, int flags, int startId) {
		return null;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent, Void alwaysNull) {
		return null;
	}

	@Override
	public void onServiceKilled(Intent rootIntent) {
		Toast.makeText(getApplicationContext(), "自动签到服务已停止", Toast.LENGTH_LONG).show();
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

	private void getThisMonthStatusThenCheckOut() {
		//先updateSession
		SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
		String userName = sharedPreferences.getString("USER_NAME", "");
		String encryptedToken = sharedPreferences.getString(ConstantValues.TOKEN, "");
		String token = EncryptUtil.decryptBase64withSalt(encryptedToken, ConstantValues.SALT);
		if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(token)) {
			@SuppressLint("HardwareIds") String deviceID = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
			RequestParams p = new RequestParams("https://api.checkin.tellyouwhat.cn/User/UpdateSession?username=" + userName + "&deviceid=" + deviceID + "&token=" + token);
			x.http().get(p, new Callback.CommonCallback<JSONObject>() {
				private int resultInt;

				@Override
				public void onSuccess(JSONObject result) {
					try {
						resultInt = result.getInt("result");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					switch (resultInt) {
						case 1:
							DbCookieStore instance = DbCookieStore.INSTANCE;
							List<HttpCookie> cookies = instance.getCookies();
							for (HttpCookie cookie : cookies) {
								String name = cookie.getName();
								String value = cookie.getValue();
								if (ConstantValues.COOKIE_NAME.equals(name)) {
									SharedPreferences preferences = x.app().getSharedPreferences(ConstantValues.COOIKE_SHARED_PREFERENCE_NAME, MODE_PRIVATE);
									SharedPreferences.Editor editor = preferences.edit();
									editor.putString(ConstantValues.cookie, value);
									editor.apply();
									break;
								}
							}
							//开始签出
							Calendar calendar = Calendar.getInstance();
							int year = calendar.get(Calendar.YEAR);
							int month = calendar.get(Calendar.MONTH);
//							Log.i(TAG, "getThisMonthStatusThenCheckOut: year is " + year + ", and month is " + month);
							int realMonth = month + 1;
							CookiedRequestParams params = new CookiedRequestParams("https://api.checkin.tellyouwhat.cn/CheckIn/GetMonthData?year=" + year + "&month=" + realMonth);
							x.http().get(params, new Callback.CommonCallback<JSONObject>() {
								@Override
								public void onSuccess(JSONObject result) {
//									Log.i(TAG, "onSuccess: 本月的数据是：" + result);
									try {
										int resultInt = result.getInt("result");
										switch (resultInt) {
											case 1:
												LocationDB db = new LocationDB();
												LocationItem item = db.queryLastRecord(employeeID);
												String timeString = item.getTime();
												String timeYear = timeString.substring(0, 4);
												String timeMonth = timeString.substring(5, 7);
												String timeDay = timeString.substring(8, 10);
												String hour = timeString.substring(11, 13);
												String minute = timeString.substring(14, 16);
												String second = timeString.substring(17, 19);
												JSONArray resultJSONArray = result.getJSONArray("data");
												for (int i = 0; i < resultJSONArray.length(); i++) {
													JSONObject jsonObject = resultJSONArray.getJSONObject(i);
													String checkInID = jsonObject.getString("CheckInID");
													String checkInTime = jsonObject.getString("CheckInTime");
													if (checkInTime.startsWith(timeYear + "-" + timeMonth + "-" + timeDay)) {
														x.http().get(new CookiedRequestParams("https://api.checkin.tellyouwhat.cn/CheckIn/AutoCheckOut?checkinid="
																		+ checkInID + "&hour=" + hour + "&minute=" + minute + "&second=" + second),
																new CommonCallback<JSONObject>() {
																	@Override
																	public void onSuccess(JSONObject result) {
//																		Log.d(TAG, "onSuccess: 自动签出返回的数据是：" + result.toString());
																		try {
																			int resultAutoCheckOut = result.getInt("result");
																			switch (resultAutoCheckOut) {
																				case 1:
																					SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
																					boolean showNotifications = sharedPref.getBoolean("show_notifications", true);
																					boolean notificationsRingEnabled = sharedPref.getBoolean("notifications_ring_enabled", false);
																					boolean notificationsVibrateEnabled = sharedPref.getBoolean("notifications_vibrate_enabled", true);
																					if (showNotifications) {
																						Intent intent = new Intent(getApplicationContext(), MainActivity.class);
																						intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
																						PendingIntent pIntent = PendingIntent.getActivity(x.app(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
																						String ticker = "您有一条新通知";
																						String title = "签出成功";
																						String content = "恭喜您，今日自动签出成功";
																						NotifyUtil notificationSucceededCheckIn = new NotifyUtil(x.app(), 1);
																						notificationSucceededCheckIn.setOnGoing(false);
																						notificationSucceededCheckIn.notify_normal_singline(pIntent, R.drawable.ic_stat_name, ticker, title, content, notificationsRingEnabled, notificationsVibrateEnabled, true);
																					}
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
																					Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_LONG).show();
																					break;
																				case -2:
																					Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_LONG).show();
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
												break;
											case 0:
//												updateSession();
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

							break;
						case 0:
							try {
								Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_SHORT).show();
							} catch (JSONException e) {
								e.printStackTrace();
							}
							break;
						case -1:
							try {
								Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_SHORT).show();
							} catch (JSONException e) {
								e.printStackTrace();
							}
							break;
						default:
							break;
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
}