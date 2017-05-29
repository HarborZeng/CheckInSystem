package cn.tellyouwhat.checkinsystem.services;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.xdandroid.hellodaemon.AbsWorkService;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.http.cookie.DbCookieStore;
import org.xutils.x;

import java.net.HttpCookie;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.activities.MainActivity;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.CookiedRequestParams;
import cn.tellyouwhat.checkinsystem.utils.EncryptUtil;
import cn.tellyouwhat.checkinsystem.utils.NotifyUtil;

import static cn.tellyouwhat.checkinsystem.fragments.CheckInFragment.CHECK_IN_STATUS;
import static cn.tellyouwhat.checkinsystem.fragments.CheckInFragment.CHECK_OUT_STATUS;
import static cn.tellyouwhat.checkinsystem.fragments.CheckInFragment.CHECK_STATUS;

/**
 * Created by Harbor-Laptop on 2017/5/12.
 * 用来更新状态栏的签到未签到签出未签出提示
 */

public class UpdateTodayStatusService extends AbsWorkService {
	SharedPreferences sharedPref;
	public boolean mHasCheckOut;
	public boolean mHasCheckIn;

	@Override
	public void onCreate() {
		super.onCreate();
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
		String token = userInfo.getString(ConstantValues.TOKEN, "");
		if (TextUtils.isEmpty(token)) {
			return;
		}
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				getTodayStatus();
			}
		}, 10000, 5 * 1000 * 60);
	}

	private void getTodayStatus() {
//		Log.i(TAG, "getTodayStatus: 获取今日状态中ing");
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
							mHasCheckIn = result.getBoolean("hascheckin");
							mHasCheckOut = result.getBoolean("hascheckout");
							saveTodayStatus();
							boolean useOngoingNotification = sharedPref.getBoolean("use_ongoing_notification", true);
							if (useOngoingNotification) {
								makeNotification();
							}
						} catch (JSONException e) {
							e.printStackTrace();
//							Log.i(TAG, "onSuccess: 今日状态更新出错，json解析异常");
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

	public void updateSession() {
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
//									Log.i("在BaseFragment里面", "onSuccess: session 已经更新");
									break;
								}
							}
							getTodayStatus();
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

	private void saveTodayStatus() {
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean("has_check_in", mHasCheckIn);
		editor.putBoolean("has_check_out", mHasCheckOut);
		editor.apply();
	}

	private void makeNotification() {
		NotifyUtil notifyUtil = new NotifyUtil(getApplicationContext(), CHECK_IN_STATUS);
		Intent leftIntent = new Intent(getApplicationContext(), MainActivity.class);
		leftIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		leftIntent.putExtra("BEGIN_CHECK_IN", true);
		PendingIntent leftPendingIntent = PendingIntent.getActivity(getApplicationContext(), CHECK_IN_STATUS, leftIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Intent rightIntent = new Intent(getApplicationContext(), MainActivity.class);
		rightIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		rightIntent.putExtra("BEGIN_CHECK_OUT", true);
		PendingIntent rightPendingIntent = PendingIntent.getActivity(getApplicationContext(), CHECK_OUT_STATUS, rightIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), CHECK_STATUS, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		notifyUtil.setOnGoing(true);

		notifyUtil.notifyTwoButton(pendingIntent, R.drawable.ic_golf_course,
				R.drawable.locate, "签到", leftPendingIntent,
				R.drawable.check_out, "签出", rightPendingIntent,
				"今日签到",
				"今日签到情况",
				"今日 " + (sharedPref.getBoolean("has_check_in", false) ? "已签到" : "未签到") + "    " + (sharedPref.getBoolean("has_check_out", false) ? "已签出" : "未签出"),
				false, false, false);

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
		stopService(intent);
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

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY_COMPATIBILITY;
	}

	@Override
	public void onDestroy() {
		Intent localIntent = new Intent();
		localIntent.setClass(this, UpdateTodayStatusService.class); // 销毁时重新启动Service
		this.startService(localIntent);
		super.onDestroy();
	}
}