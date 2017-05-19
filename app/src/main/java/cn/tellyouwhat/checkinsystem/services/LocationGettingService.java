package cn.tellyouwhat.checkinsystem.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.xdandroid.hellodaemon.AbsWorkService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.activities.MainActivity;
import cn.tellyouwhat.checkinsystem.bean.LocationItem;
import cn.tellyouwhat.checkinsystem.db.LocationDB;
import cn.tellyouwhat.checkinsystem.receivers.BatteryReceiver;
import cn.tellyouwhat.checkinsystem.receivers.ScreenReceiver;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.NotifyUtil;
import cn.tellyouwhat.checkinsystem.utils.Polygon;

/**
 * Created by Harbor-Laptop on 2017/3/22.
 * 后台获取位置的服务
 */

public class LocationGettingService extends AbsWorkService {

	public static final int PERIOD = 1000 * 5 * 60;
	private static final int IN_RANGE_NOTIFICATION = 200;
	private static final int DELAY = 1000 * 10;

//	public static final int PERIOD = 1000 * 60;

	static {
		Log.d("static", "static initializer: 后台服务已注册");
	}

	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListenerService();
	public final Timer timer = new Timer();
	private TimerTask task;
	private Polygon polygons[];
	private int locationIDs[];
	private BatteryReceiver batteryReceiver;
	private ScreenReceiver screenReceiver;
	private SharedPreferences sharedPref;
	private static final String TAG = "LocationGettingService";
	private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//			Log.d(TAG, "onSharedPreferenceChanged: 设置发生了变化");
			if ("use_background_service".equals(key)) {
//				Log.d(TAG, "onSharedPreferenceChanged: “使用后台服务”发生了变化");
				if (sharedPreferences.getBoolean("use_background_service", true)) {
					task = new TimerTask() {
						@Override
						public void run() {
							Message message = Message.obtain();
							message.what = 1;
							handler.sendMessage(message);
						}
					};
					timer.schedule(task, DELAY, PERIOD);
				} else {
					task.cancel();
				}
			} else if ("use_GPS".equals(key)) {
				task.cancel();
				task = new TimerTask() {
					@Override
					public void run() {
						Message message = Message.obtain();
						message.what = 1;
						handler.sendMessage(message);
					}
				};
				timer.schedule(task, DELAY, PERIOD);
			}
		}
	};
	private Handler handler;
	private String[] locationNames;
	private String userID;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		userID = getSharedPreferences("userInfo", MODE_PRIVATE).getString("employeeID", "");
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		mLocationClient = new LocationClient(getApplicationContext());
		//声明LocationClient类
		mLocationClient.registerLocationListener(myListener);
		//注册监听函数
		initLocation();

		//系统电量过低时
		batteryReceiver = new BatteryReceiver();
		IntentFilter batteryFilter = new IntentFilter();
		batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, batteryFilter);

		screenReceiver = new ScreenReceiver();
		/*<intent-filter>
		        <action android:name="android.intent.action.SCREEN_OFF"/>
                <action android:name="android.intent.action.SCREEN_ON"/>
            </intent-filter>*/
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(screenReceiver, intentFilter);

		RequestParams requestParams = new RequestParams("https://api.checkin.tellyouwhat.cn/location/getalllocation");
		x.http().request(HttpMethod.GET, requestParams, new Callback.CommonCallback<JSONObject>() {
			@Override
			public void onSuccess(JSONObject result) {
				try {
					int resultInt = result.getInt("result");
					switch (resultInt) {
						case 1:
							JSONArray jsonArray = result.getJSONArray("data");
							polygons = new Polygon[jsonArray.length()];
							locationIDs = new int[jsonArray.length()];
							locationNames = new String[jsonArray.length()];
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jsonObject;
								jsonObject = jsonArray.getJSONObject(i);
								locationIDs[i] = jsonObject.getInt("LocationID");
								locationNames[i] = jsonObject.getString("LocationName");
								int x1 = (int) (jsonObject.getDouble("X1") * 1000000);
								int x2 = (int) (jsonObject.getDouble("X2") * 1000000);
								int x3 = (int) (jsonObject.getDouble("X3") * 1000000);
								int x4 = (int) (jsonObject.getDouble("X4") * 1000000);
								int y1 = (int) (jsonObject.getDouble("Y1") * 1000000);
								int y2 = (int) (jsonObject.getDouble("Y2") * 1000000);
								int y3 = (int) (jsonObject.getDouble("Y3") * 1000000);
								int y4 = (int) (jsonObject.getDouble("Y4") * 1000000);
								int xPoints[] = new int[]{x1, x2, x3, x4};
								int yPoints[] = new int[]{y1, y2, y3, y4};
								polygons[i] = new Polygon(xPoints, yPoints, 4);

							}
							break;
						default:
							Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_SHORT).show();
							break;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				Toast.makeText(x.app(), "后台服务获取公司位置出错", Toast.LENGTH_SHORT).show();
				SharedPreferences companiesInfoAll = getSharedPreferences("companies_location", MODE_PRIVATE);
				String companiesInfoAll1 = companiesInfoAll.getString("companies_info_all", "");
				try {
					JSONObject result = new JSONObject(companiesInfoAll1);
					JSONArray jsonArray = result.getJSONArray("data");
					polygons = new Polygon[jsonArray.length()];
					locationIDs = new int[jsonArray.length()];
					locationNames = new String[jsonArray.length()];
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject;
						jsonObject = jsonArray.getJSONObject(i);
						locationIDs[i] = jsonObject.getInt("LocationID");
						locationNames[i] = jsonObject.getString("LocationName");
						int x1 = (int) (jsonObject.getDouble("X1") * 1000000);
						int x2 = (int) (jsonObject.getDouble("X2") * 1000000);
						int x3 = (int) (jsonObject.getDouble("X3") * 1000000);
						int x4 = (int) (jsonObject.getDouble("X4") * 1000000);
						int y1 = (int) (jsonObject.getDouble("Y1") * 1000000);
						int y2 = (int) (jsonObject.getDouble("Y2") * 1000000);
						int y3 = (int) (jsonObject.getDouble("Y3") * 1000000);
						int y4 = (int) (jsonObject.getDouble("Y4") * 1000000);
						int xPoints[] = new int[]{x1, x2, x3, x4};
						int yPoints[] = new int[]{y1, y2, y3, y4};
						polygons[i] = new Polygon(xPoints, yPoints, 4);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});

		//				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//				boolean backGroundServiceEnabled = sharedPref.getBoolean("use_background_service", true);
//				if (backGroundServiceEnabled) {
//				}
// 要做的事情
		//用户已登录的情况下才会获取地理位置
		SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
		final String token = userInfo.getString(ConstantValues.TOKEN, "");
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				if (!TextUtils.isEmpty(token)) {
					mLocationClient.start();
				}
				// 要做的事情
				super.handleMessage(msg);
			}
		};

		task = new TimerTask() {
			@Override
			public void run() {
				Message message = Message.obtain();
				message.what = 1;
				handler.sendMessage(message);
			}
		};

		boolean useBackgroundService = sharedPref.getBoolean("use_background_service", true);
		boolean useBackgroundServiceReceiver = sharedPref.getBoolean("use_background_service_receiver", true);
		sharedPref.registerOnSharedPreferenceChangeListener(listener);
		if (useBackgroundService && useBackgroundServiceReceiver && !TextUtils.isEmpty(token)) {
			timer.schedule(task, DELAY * 50, PERIOD);
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
		Toast.makeText(getApplicationContext(), "后台获取位置服务已停止", Toast.LENGTH_LONG).show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		boolean backGroundServiceEnabled = sharedPref.getBoolean("use_background_service", true);
		boolean useBackgroundServiceReceiver = sharedPref.getBoolean("use_background_service_receiver", true);
		if (backGroundServiceEnabled && useBackgroundServiceReceiver) {
			return START_STICKY_COMPATIBILITY;
		} else {
			return super.onStartCommand(intent, flags, startId);
		}
	}

	@Override
	public void onDestroy() {
		boolean backGroundServiceEnabled = sharedPref.getBoolean("use_background_service", true);
		boolean useBackgroundServiceReceiver = sharedPref.getBoolean("use_background_service_receiver", true);
		if (backGroundServiceEnabled && useBackgroundServiceReceiver) {
			Intent localIntent = new Intent();
			localIntent.setClass(this, LocationGettingService.class); // 销毁时重新启动Service
			this.startService(localIntent);
		}
		unregisterReceiver(batteryReceiver);
		unregisterReceiver(screenReceiver);
		super.onDestroy();
	}

	private void initLocation() {
		LocationClientOption option = new LocationClientOption();
		boolean useGps = sharedPref.getBoolean("use_GPS", true);
		boolean useGpsReceiver = sharedPref.getBoolean("use_GPS_receiver", true);
		if (useGps && useGpsReceiver) {
//			Toast.makeText(this, "using gps", Toast.LENGTH_SHORT).show();
			option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
			option.setOpenGps(true);
			//可选，默认false,设置是否使用gps
		} else {
//			Toast.makeText(this, "not using gps", Toast.LENGTH_SHORT).show();
			option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
		}
		//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

		option.setCoorType("bd09ll");
		//可选，默认gcj02，设置返回的定位结果坐标系

//		int span = 10000;
//		int span = 1000*60*5+1;
		option.setScanSpan(1000 * 3);
		//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

		option.setIsNeedAddress(true);
		//可选，设置是否需要地址信息，默认不需要

		option.setLocationNotify(true);
		//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

		option.setIsNeedLocationDescribe(true);
		//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

		option.setIsNeedLocationPoiList(false);
		//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

		option.setIgnoreKillProcess(false);
		//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

		option.SetIgnoreCacheException(false);
		//可选，默认false，设置是否收集CRASH信息，默认收集

		option.setEnableSimulateGps(false);
		//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
		mLocationClient.setLocOption(option);
	}

	private class MyLocationListenerService implements BDLocationListener {

		private int timesOfGettingInThisTime = 0;
		private SparseArray<BDLocation> locationSparseArray = new SparseArray<>();
		private ArrayList<Float> radiusArray = new ArrayList<>(8);
		private int hasNotifiedNTimes = 0;

		@Override
		public void onReceiveLocation(final BDLocation location) {
			float radius = location.getRadius();
			/*
			  以下内容属于测试代码
			 */
			//获取定位结果
			final StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());    //获取定位时间

			sb.append("\nerror code : ");
			sb.append(location.getLocType());    //获取类型类型

			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());    //获取纬度信息

			sb.append("\nlongitude : ");
			sb.append(location.getLongitude());    //获取经度信息

			sb.append("\nradius : ");
			sb.append(radius);    //获取定位精准度

			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				// GPS定位结果
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());    // 单位：公里每小时

				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());    //获取卫星数

				sb.append("\nheight : ");
				sb.append(location.getAltitude());    //获取海拔高度信息，单位米

				sb.append("\ndirection : ");
				sb.append(location.getDirection());    //获取方向信息，单位度

				sb.append("\naddr : ");
				sb.append(location.getAddrStr());    //获取地址信息

				sb.append("\ndescribe : ");
				sb.append("gps定位成功");

			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {

				// 网络定位结果
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());    //获取地址信息

				sb.append("\noperationers : ");
				sb.append(location.getOperators());    //获取运营商信息

				sb.append("\ndescribe : ");
				sb.append("网络定位成功");

			} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {

				// 离线定位结果
				sb.append("\ndescribe : ");
				sb.append("离线定位成功，离线定位结果也是有效的");

			} else if (location.getLocType() == BDLocation.TypeServerError) {

				sb.append("\ndescribe : ");
				sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");

			} else if (location.getLocType() == BDLocation.TypeNetWorkException) {

				sb.append("\ndescribe : ");
				sb.append("网络不同导致定位失败，请检查网络是否通畅");

			} else if (location.getLocType() == BDLocation.TypeCriteriaException) {

				sb.append("\ndescribe : ");
				sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");

			}
			Log.i("BaiduLocationApiDem", sb.toString());

			if (timesOfGettingInThisTime < 8) {
				locationSparseArray.put(timesOfGettingInThisTime, location);
				radiusArray.add(location.getRadius());
				timesOfGettingInThisTime++;
			} else {
//				mLocationClient.stop();
				float minRadius = Float.MAX_VALUE;
				int index = 0;
				for (int i = 0; i < 8; i++) {
					if (minRadius > radiusArray.get(i)) {
						minRadius = radiusArray.get(i);
						index = i;
					}
				}
//				Toast.makeText(getApplicationContext(), Arrays.toString(radiusArray), Toast.LENGTH_SHORT).show();
				Log.d(TAG, "onReceiveLocation: 半径数组中都有：" + radiusArray);
				if (radiusArray.get(index) < 50 && radiusArray.get(index) != 0) {
//					Toast.makeText(getApplicationContext(), "index=" + index + ", 最小半径是: " + radiusArray[index], Toast.LENGTH_SHORT).show();
					Log.d(TAG, "onReceiveLocation: 最小半径和他的index是: " + radiusArray.get(index) + ", " + index);
					BDLocation bdLocation = locationSparseArray.get(index);

					LocationDB locationDB = new LocationDB();
					LocationItem item = new LocationItem();
					item.setGotFromService(true);
					item.setLocationType(bdLocation.getLocType());
					item.setRadius(bdLocation.getRadius());
					item.setTime(bdLocation.getTime());
					item.setLatitude(Double.toString(bdLocation.getLatitude()));
					item.setLongitude(Double.toString(bdLocation.getLongitude()));
					item.setAddress(bdLocation.getAddrStr());
					item.setLocationDescription(bdLocation.getLocationDescribe());
					item.setUserID(userID);
					if (polygons != null) {
						for (int i = 0; i < polygons.length; i++) {
							if (polygons[i].contains(bdLocation.getLongitude() * 1000000, bdLocation.getLatitude() * 1000000)) {
								item.setBuildingID(locationIDs[i]);
								item.setBuildingDesc(locationNames[i]);
								boolean showNotifications = sharedPref.getBoolean("notifications_check_in_reminder_enabled", true);
								boolean notificationsCheckInReminderRingtoneAndVibrateEnabled = sharedPref.getBoolean("notifications_check_in_reminder_ringtone_and_vibrate_enabled", true);
								boolean hasCheckIn = sharedPref.getBoolean("has_check_in", false);
								if (!hasCheckIn) {
									//用来通知AutoCheckInService开始自动签到
									hasNotifiedNTimes++;
									SharedPreferences checkInStatus = getSharedPreferences("checkInStatus", MODE_PRIVATE);
									SharedPreferences.Editor editor = checkInStatus.edit();
									editor.putInt("has_notified_N_times", hasNotifiedNTimes);
									editor.apply();
								}
								if (showNotifications && !hasCheckIn) {
									Intent intent = new Intent(getApplicationContext(), MainActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.putExtra("BEGIN_CHECK_IN", true);
									PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), IN_RANGE_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

									String ticker = "您已进入签到范围";
									String title = "您已进入签到范围";
									String addrStr = bdLocation.getAddrStr();
									String content = "点击签到\n您在" + locationNames[i] + "\n位于" + (TextUtils.isEmpty(addrStr) ? "未知" : addrStr);

									NotifyUtil notificationSucceededCheckIn = new NotifyUtil(getApplicationContext(), IN_RANGE_NOTIFICATION);
									notificationSucceededCheckIn.setOnGoing(false);
									notificationSucceededCheckIn.notifyNormailMmoreline(pIntent, R.drawable.ic_nature_people, ticker, title, content, notificationsCheckInReminderRingtoneAndVibrateEnabled, notificationsCheckInReminderRingtoneAndVibrateEnabled, false);
								}
								break;
							} else {
								Log.d(TAG, "onReceiveLocation: 不在" + locationNames[i]);
								item.setBuildingID(0);
								SharedPreferences checkInStatus = getSharedPreferences("checkInStatus", MODE_PRIVATE);
								SharedPreferences.Editor editor = checkInStatus.edit();
								editor.putInt("has_notified_N_times", 0);
								editor.apply();
							}
						}
					}
					locationDB.saveLocation(item);
				}
				timesOfGettingInThisTime = 0;
				mLocationClient.stop();
				radiusArray.clear();
			}
		}

		@Override
		public void onConnectHotSpotMessage(String s, int i) {
			Log.w("onConnectHotSpotMessage", "onConnectHotSpotMessage: s: " + s + ", i: " + i);
		}
	}
}