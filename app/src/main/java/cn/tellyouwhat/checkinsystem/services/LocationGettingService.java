package cn.tellyouwhat.checkinsystem.services;

import android.app.PendingIntent;
import android.app.Service;
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

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.activities.MainActivity;
import cn.tellyouwhat.checkinsystem.db.LocationDB;
import cn.tellyouwhat.checkinsystem.db.LocationItem;
import cn.tellyouwhat.checkinsystem.receivers.BatteryReceiver;
import cn.tellyouwhat.checkinsystem.receivers.ScreenReceiver;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.NotifyUtil;
import cn.tellyouwhat.checkinsystem.utils.Polygon;

/**
 * Created by Harbor-Laptop on 2017/3/22.
 * 后台获取位置的服务
 */

public class LocationGettingService extends Service {

	public static final int PERIOD = 1000 * 5 * 60;
	private static final int IN_RANGE_NOTIFICATION = 200;
	private static final int DELAY = 200;

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
			Log.d(TAG, "onSharedPreferenceChanged: 设置发生了变化");
			if ("use_background_service".equals(key)) {
				Log.d(TAG, "onSharedPreferenceChanged: “使用后台服务”发生了变化");
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

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		mLocationClient = new LocationClient(getApplicationContext());
		//声明LocationClient类
		mLocationClient.registerLocationListener(myListener);
		//注册监听函数
		initLocation();

		//系统电量过低时
		batteryReceiver = new BatteryReceiver();
		IntentFilter batteryfilter = new IntentFilter();
		batteryfilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, batteryfilter);

		screenReceiver = new ScreenReceiver();
		/*<intent-filter>
		        <action android:name="android.intent.action.SCREEN_OFF"/>
                <action android:name="android.intent.action.SCREEN_ON"/>
            </intent-filter>*/
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(screenReceiver, intentFilter);

		RequestParams requestParams = new RequestParams("http://update.checkin.tellyouwhat.cn/company_location.json");
		x.http().request(HttpMethod.GET, requestParams, new Callback.CommonCallback<JSONArray>() {
			@Override
			public void onSuccess(JSONArray jsonArray) {
				polygons = new Polygon[jsonArray.length()];
				locationIDs = new int[jsonArray.length()];
				locationNames = new String[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject;
					try {
						jsonObject = jsonArray.getJSONObject(i);
						locationIDs[i] = jsonObject.getInt("locationID");
						locationNames[i] = jsonObject.getString("name");
						int x1 = (int) (jsonObject.getDouble("x1") * 1000000);
						int x2 = (int) (jsonObject.getDouble("x2") * 1000000);
						int x3 = (int) (jsonObject.getDouble("x3") * 1000000);
						int x4 = (int) (jsonObject.getDouble("x4") * 1000000);
						int y1 = (int) (jsonObject.getDouble("y1") * 1000000);
						int y2 = (int) (jsonObject.getDouble("y2") * 1000000);
						int y3 = (int) (jsonObject.getDouble("y3") * 1000000);
						int y4 = (int) (jsonObject.getDouble("y4") * 1000000);
						int xPoints[] = new int[]{x1, x2, x3, x4};
						int yPoints[] = new int[]{y1, y2, y3, y4};
						polygons[i] = new Polygon(xPoints, yPoints, 4);
					} catch (JSONException e) {
						e.printStackTrace();
					}
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

		//				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//				boolean backGroundServiceEnabled = sharedPref.getBoolean("use_background_service", true);
//				if (backGroundServiceEnabled) {
//				}
// 要做的事情
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				//用户已登录的情况下才会获取地理位置
				SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
				String token = userInfo.getString(ConstantValues.TOKEN, "");
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
		sharedPref.registerOnSharedPreferenceChangeListener(listener);
		if (useBackgroundService) {
			timer.schedule(task, DELAY * 50, PERIOD);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		boolean backGroundServiceEnabled = sharedPref.getBoolean("use_background_service", true);
		if (backGroundServiceEnabled) {
			return START_STICKY_COMPATIBILITY;
		} else {
			return super.onStartCommand(intent, flags, startId);
		}
	}

	@Override
	public void onDestroy() {
		boolean backGroundServiceEnabled = sharedPref.getBoolean("use_background_service", true);
		if (backGroundServiceEnabled) {
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
		if (useGps) {
//			Toast.makeText(this, "using GPS", Toast.LENGTH_SHORT).show();
			option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
			option.setOpenGps(true);
			//可选，默认false,设置是否使用gps
		} else {
//			Toast.makeText(this, "not using GPS", Toast.LENGTH_SHORT).show();
			option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
		}
		//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

		option.setCoorType("bd09ll");
		//可选，默认gcj02，设置返回的定位结果坐标系

		int span = 10000;
//		int span = 1000*60*5+1;
		option.setScanSpan(1000 * 5);
		//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

		option.setIsNeedAddress(true);
		//可选，设置是否需要地址信息，默认不需要

		option.setLocationNotify(false);
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
		private Float radiusArray[] = new Float[8];

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
				radiusArray[timesOfGettingInThisTime] = location.getRadius();
				timesOfGettingInThisTime++;
			} else {
				mLocationClient.stop();
				float minRadius = Float.MAX_VALUE;
				int index = 0;
				for (int i = 0; i < 8; i++) {
					if (minRadius > radiusArray[i]) {
						minRadius = radiusArray[i];
						index = i;
					}
				}
//				Toast.makeText(getApplicationContext(), Arrays.toString(radiusArray), Toast.LENGTH_SHORT).show();
				Log.d(TAG, "onReceiveLocation: 半径数组中都有：" + Arrays.toString(radiusArray));
				if (radiusArray[index] < 50 && radiusArray[index] != 0) {
//					Toast.makeText(getApplicationContext(), "index=" + index + ", 最小半径是: " + radiusArray[index], Toast.LENGTH_SHORT).show();
					Log.d(TAG, "onReceiveLocation: 最小半径和他的index是: " + radiusArray[index] + ", " + index);
					BDLocation bdLocation = locationSparseArray.get(index);

					LocationDB locationDB = new LocationDB();
					LocationItem item = new LocationItem();
					item.setGotFromService(true);
					item.setLocationType(bdLocation.getLocType());
					item.setRadius(bdLocation.getRadius());
					item.setTime(bdLocation.getTime());
					item.setLatitude(Double.toString(bdLocation.getLatitude()));
					item.setLongitide(Double.toString(bdLocation.getLongitude()));
					item.setAddress(bdLocation.getAddrStr());
					item.setLocationDescription(bdLocation.getLocationDescribe());
					if (polygons != null) {
						for (int i = 0; i < polygons.length; i++) {
							if (polygons[i].contains(bdLocation.getLongitude() * 1000000, bdLocation.getLatitude() * 1000000)) {
								item.setBuildingID(locationIDs[i]);
								item.setBuildingDesc(locationNames[i]);
								boolean showNotifications = sharedPref.getBoolean("notifications_check_in_reminder_enabled", true);
								boolean notificationsCheckInReminderRingtoneAndVibrateEnabled = sharedPref.getBoolean("notifications_check_in_reminder_ringtone_and_vibrate_enabled", true);
								boolean hasCheckIn = sharedPref.getBoolean("has_check_in", false);
								if (showNotifications && !hasCheckIn) {
									Intent intent = new Intent(getApplicationContext(), MainActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.putExtra("BEGIN_CHECK_IN", true);
									PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), IN_RANGE_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

									String ticker = "您已进入签到范围";
									String title = "您已进入签到范围";
									String content = "点击签到\n您在" + locationNames[i] + "，位于" + bdLocation.getAddrStr();

									NotifyUtil notificationSucceededCheckIn = new NotifyUtil(getApplicationContext(), IN_RANGE_NOTIFICATION);
									notificationSucceededCheckIn.setOnGoing(false);
									notificationSucceededCheckIn.notifyNormailMmoreline(pIntent, R.mipmap.ic_launcher, ticker, title, content, notificationsCheckInReminderRingtoneAndVibrateEnabled, notificationsCheckInReminderRingtoneAndVibrateEnabled, true);
								}
								break;
							} else {
								item.setBuildingID(0);
							}
						}
					}
					locationDB.saveLocation(item);
					timesOfGettingInThisTime = 0;
				}
				mLocationClient.stop();
			}
		}

		@Override
		public void onConnectHotSpotMessage(String s, int i) {
			Log.w("onConnectHotSpotMessage", "onConnectHotSpotMessage: s: " + s + ", i: " + i);
		}
	}

}
