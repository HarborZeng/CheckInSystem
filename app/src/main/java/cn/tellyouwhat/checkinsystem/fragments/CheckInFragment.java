package cn.tellyouwhat.checkinsystem.fragments;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.activities.MainActivity;
import cn.tellyouwhat.checkinsystem.db.LocationDB;
import cn.tellyouwhat.checkinsystem.db.LocationItem;
import cn.tellyouwhat.checkinsystem.utils.NotifyUtil;
import cn.tellyouwhat.checkinsystem.utils.Polygon;
import cn.tellyouwhat.checkinsystem.utils.ReLoginUtil;

/**
 * Created by Harbor-Laptop on 2017/3/3.
 *
 * @author HarborZeng
 */

public class CheckInFragment extends BaseFragment {
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
	private Polygon polygons[];
	private ImageView imageView2_cover_in50;
	private ImageView imageView2_cover_out50;
	private ImageView imageView_cover_in_company;
	private TextView succeed;
	private ImageView imageView_cover_finally_success;
	private TextView out_of_range;
	private ImageView imageView_cover_out_company;
	private TextView in_range;
	private TextView enable_wifi_gps_textView;
	private TextView enough_accuracy_text_view;
	private Snackbar snackbar;
	private int locationIDs[];
	private String locationNames[];
	private View view;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
						locationNames[i] = jsonObject.getString("name");
						locationIDs[i] = jsonObject.getInt("locationID");
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
				Toast.makeText(getActivity(), "获取公司位置出错", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});
	}


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_check_in, container, false);

		imageView2_cover_in50 = (ImageView) view.findViewById(R.id.imageView2_cover_in50);
		imageView2_cover_out50 = (ImageView) view.findViewById(R.id.imageView2_cover_out50);
		imageView_cover_in_company = (ImageView) view.findViewById(R.id.imageView_cover_in_company);
		succeed = (TextView) view.findViewById(R.id.succeed);
		imageView_cover_finally_success = (ImageView) view.findViewById(R.id.imageView_cover_finally_success);
		out_of_range = (TextView) view.findViewById(R.id.out_of_range);
		in_range = (TextView) view.findViewById(R.id.in_range);
		enable_wifi_gps_textView = (TextView) view.findViewById(R.id.enable_wifi_GPS_textView);
		imageView_cover_out_company = (ImageView) view.findViewById(R.id.imageView_cover_out_company);
		enough_accuracy_text_view = (TextView) view.findViewById(R.id.enough_accuracy_text_view);

//    LayoutInflater inflater2 = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//    groupPollingAddress = (LinearLayout)inflater2.inflate(R.layout.three_state, null);

		final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) view.findViewById(R.id.multiple_actions);
		x.task().postDelayed(new Runnable() {
			@Override
			public void run() {
				menuMultipleActions.expand();
			}
		}, 1100);

		final FloatingActionButton actionB = (FloatingActionButton) view.findViewById(R.id.action_b);
		actionB.setIcon(R.drawable.check_out);
		actionB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				menuMultipleActions.collapse();
				//签出逻辑
				Toast.makeText(getActivity(), "此功能暂未添加", Toast.LENGTH_LONG).show();
			}
		});

		final FloatingActionButton actionA = (FloatingActionButton) view.findViewById(R.id.action_a);
		actionA.setIcon(R.drawable.locate);
		actionA.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View view) {
				if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
					//开始定位
					mLocationClient.start();
					menuMultipleActions.collapse();

					snackbar = Snackbar.make(view, R.string.getting_location, Snackbar.LENGTH_INDEFINITE);
					snackbar.setAction("取消", new OnClickListener() {
						@Override
						public void onClick(View v) {
							mLocationClient.stop();
							new Thread(new Runnable() {
								@Override
								public void run() {
									//睡100毫秒是为了防止当执行setVisibility INVISIBLE时不至于 服务器对于是否在设定区域内的结果还未返回
									//就已经执行了findViewById(R.id.out_of_range).setVisibility(View.INVISIBLE);
									//类似的代码从而导致取消定位之后有残留的图片
									SystemClock.sleep(100);
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											imageView_cover_out_company.setVisibility(View.INVISIBLE);
											imageView_cover_in_company.setVisibility(View.INVISIBLE);
											succeed.setVisibility(View.INVISIBLE);
											imageView_cover_finally_success.setVisibility(View.INVISIBLE);
											out_of_range.setVisibility(View.INVISIBLE);
											in_range.setVisibility(View.INVISIBLE);
											imageView2_cover_in50.setVisibility(View.INVISIBLE);
											imageView2_cover_out50.setVisibility(View.INVISIBLE);
											enable_wifi_gps_textView.setVisibility(View.INVISIBLE);
											enough_accuracy_text_view.setVisibility(View.INVISIBLE);
										}
									});
								}
							}).start();
						}
					}).show();
				} else {
					Snackbar.make(view, "必须要授权位置访问才能正常工作", Snackbar.LENGTH_INDEFINITE)
							.setAction("授权", new OnClickListener() {
								@Override
								public void onClick(View v) {
									String[] perms = {"android.permission.ACCESS_FINE_LOCATION"};
									ActivityCompat.requestPermissions(getActivity(), perms, 1);
								}
							}).show();
				}
			}
		});

		mLocationClient = new LocationClient(getActivity().getApplicationContext());
		//声明LocationClient类
		mLocationClient.registerLocationListener(myListener);
		//注册监听函数
		initLocation();

		return view;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
		//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

		option.setCoorType("bd09ll");
		//可选，默认gcj02，设置返回的定位结果坐标系

		int span = 3000;
		option.setScanSpan(span);
		//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

		option.setIsNeedAddress(true);
		//可选，设置是否需要地址信息，默认不需要

		option.setOpenGps(true);
		//可选，默认false,设置是否使用gps

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

	private class MyLocationListener implements BDLocationListener {
		private int times = 0;

		@Override
		public void onReceiveLocation(final BDLocation location) {
			alphaAnimation.setDuration(800);
			final float radius = location.getRadius();
			final double latitude = location.getLatitude();
			final double longitude = location.getLongitude();
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
			sb.append(latitude);    //获取纬度信息

			sb.append("\nlongitude : ");
			sb.append(longitude);    //获取经度信息

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

			sb.append("\nlocationdescribe : ");
			sb.append(location.getLocationDescribe() == null || "null".equals(location.getLocationDescribe()) ? "离线定位，位置未知" : location.getLocationDescribe());    //位置语义化信息

			List<Poi> list = location.getPoiList();    // POI数据
			if (list != null) {
				sb.append("\npoilist size = : ");
				sb.append(list.size());
				for (Poi p : list) {
					sb.append("\npoi= : ");
					sb.append(p.getId()).append(" ").append(p.getName()).append(" ").append(p.getRank());
				}
			}

			Log.i("BaiduLocationApiDem", sb.toString());

			if (radius <= 50 && radius != 0) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						times = 0;
						enable_wifi_gps_textView.setVisibility(View.INVISIBLE);
						enough_accuracy_text_view.setText("精度合格\n");
						enough_accuracy_text_view.append(location.getLocationDescribe() == null ? "离线定位" : location.getLocationDescribe());
						enough_accuracy_text_view.setVisibility(View.VISIBLE);
						enough_accuracy_text_view.startAnimation(alphaAnimation);
						imageView2_cover_in50.setVisibility(View.VISIBLE);
						imageView2_cover_out50.setVisibility(View.INVISIBLE);
						imageView2_cover_in50.startAnimation(alphaAnimation);

						imageView_cover_in_company.setVisibility(View.INVISIBLE);
						succeed.setVisibility(View.INVISIBLE);
						imageView_cover_finally_success.setVisibility(View.INVISIBLE);
						out_of_range.setVisibility(View.INVISIBLE);
						imageView_cover_out_company.setVisibility(View.INVISIBLE);
						in_range.setVisibility(View.INVISIBLE);
						YoYo.with(Techniques.Tada)
								.duration(500)
								.repeat(1)
								.playOn(imageView2_cover_in50);
						YoYo.with(Techniques.Tada)
								.duration(500)
								.repeat(1)
								.playOn(enough_accuracy_text_view);
					}
				});

//          uploadLocationInfo();
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (polygons == null) {
							Toast.makeText(getActivity(), "请重试", Toast.LENGTH_LONG).show();
						} else {

							for (int i = 0; i < polygons.length; i++) {
								if (polygons[i].contains(longitude * 1000000, latitude * 1000000)) {
									in_range.setText(locationNames[i] + "范围内");
									LocationDB locationDB = new LocationDB();
									LocationItem item = new LocationItem();
									String time = location.getTime();
									item.setGotFromService(false);
									item.setLocationType(location.getLocType());
									item.setRadius(radius);
									item.setTime(time);
									item.setBuildingID(locationIDs[i]);
									item.setLatitude(Double.toString(location.getLatitude()));
									item.setLongitide(Double.toString(location.getLongitude()));
									item.setAddress(location.getAddrStr());
									item.setLocationDescription(location.getLocationDescribe());
									locationDB.saveLocation(item);

									Log.i("zdhobuzd", "run: 在");
									mLocationClient.stop();
									snackbar.dismiss();
									new Thread(new Runnable() {
										@Override
										public void run() {
											SystemClock.sleep(1000);
											getActivity().runOnUiThread(new Runnable() {
												@Override
												public void run() {
													imageView_cover_out_company.setVisibility(View.INVISIBLE);
													out_of_range.setVisibility(View.INVISIBLE);

													imageView_cover_in_company.setVisibility(View.VISIBLE);
													imageView_cover_in_company.setAnimation(alphaAnimation);
													in_range.startAnimation(alphaAnimation);
													in_range.setVisibility(View.VISIBLE);

													imageView2_cover_out50.setVisibility(View.INVISIBLE);
													enable_wifi_gps_textView.setVisibility(View.INVISIBLE);
													YoYo.with(Techniques.Tada)
															.duration(700)
															.repeat(1)
															.playOn(in_range);
													YoYo.with(Techniques.Tada)
															.duration(700)
															.repeat(1)
															.playOn(imageView_cover_in_company);
												}
											});
										}
									}).start();
									beginCheckingIn(radius, longitude, latitude, time);
									break;
								} else {
									Log.i("zdhobuzd", "run: 不在");
									new Thread(new Runnable() {
										@Override
										public void run() {
											SystemClock.sleep(1000);
											getActivity().runOnUiThread(new Runnable() {
												@Override
												public void run() {
													imageView_cover_out_company.setVisibility(View.VISIBLE);
													imageView_cover_out_company.startAnimation(alphaAnimation);
													out_of_range.startAnimation(alphaAnimation);
													out_of_range.setVisibility(View.VISIBLE);

													imageView2_cover_out50.setVisibility(View.INVISIBLE);
													imageView_cover_in_company.setVisibility(View.INVISIBLE);
													succeed.setVisibility(View.INVISIBLE);
													imageView_cover_finally_success.setVisibility(View.INVISIBLE);
													enable_wifi_gps_textView.setVisibility(View.INVISIBLE);
													YoYo.with(Techniques.Tada)
															.duration(500)
															.repeat(1)
															.playOn(imageView_cover_out_company);
//													imageView_cover_out_company.setAlpha(0.5f);
													YoYo.with(Techniques.Tada)
															.duration(500)
															.repeat(1)
															.playOn(out_of_range);
//													out_of_range.setAlpha(0.5f);
												}
											});
										}
									}).start();
								}
							}
						}
					}
				});

			} else {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (times > 2) {
							enable_wifi_gps_textView.setVisibility(View.VISIBLE);
							enable_wifi_gps_textView.startAnimation(alphaAnimation);
						}
						times++;
						imageView2_cover_in50.setVisibility(View.INVISIBLE);
						imageView2_cover_out50.setVisibility(View.VISIBLE);
						imageView2_cover_out50.startAnimation(alphaAnimation);

						imageView_cover_in_company.setVisibility(View.INVISIBLE);
						succeed.setVisibility(View.INVISIBLE);
						imageView_cover_finally_success.setVisibility(View.INVISIBLE);
						out_of_range.setVisibility(View.INVISIBLE);
						imageView_cover_out_company.setVisibility(View.INVISIBLE);
						in_range.setVisibility(View.INVISIBLE);
						enough_accuracy_text_view.setVisibility(View.INVISIBLE);
						YoYo.with(Techniques.Tada)
								.duration(500)
								.repeat(1)
								.playOn(enable_wifi_gps_textView);
						YoYo.with(Techniques.Tada)
								.duration(500)
								.repeat(1)
								.playOn(imageView2_cover_out50);
					}
				});
			}
		}

		@Override
		public void onConnectHotSpotMessage(String s, int i) {
			Log.w("onConnectHotSpotMessage", "onConnectHotSpotMessage: s: " + s + ", i: " + i);
		}
	}


	private void beginCheckingIn(float radius, double longitude, double latitude, String time) {
		RequestParams requestParams = new RequestParams("http://api.checkin.tellyouwhat.cn/checkin/checkin");
		requestParams.setUseCookie(true);
		requestParams.setConnectTimeout(5000);
		requestParams.setMultipart(true);
		x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {
			@Override
			public void onSuccess(JSONObject result) {
				try {
					int resultInt = result.getInt("result");
					switch (resultInt) {
						case 1:
							SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
							boolean showNotifications = sharedPref.getBoolean("show_notifications", true);
							boolean notificationsRingEnabled = sharedPref.getBoolean("notifications_ring_enabled", false);
							boolean notificationsVibrateEnabled = sharedPref.getBoolean("notifications_vibrate_enabled", true);
							if (showNotifications) {
								Intent intent = new Intent(getActivity(), MainActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
								PendingIntent pIntent = PendingIntent.getActivity(getActivity(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
								String ticker = "您有一条新通知";
								String title = "签到成功";
								String content = "恭喜您，今日签到成功";
								NotifyUtil notificationSucceededCheckIn = new NotifyUtil(getActivity().getApplicationContext(), 1);
								notificationSucceededCheckIn.setOnGoing(false);
								notificationSucceededCheckIn.notify_normal_singline(pIntent, R.mipmap.ic_launcher, ticker, title, content, notificationsRingEnabled, notificationsVibrateEnabled, true);
							}
							break;
						case 0:
							ReLoginUtil util = new ReLoginUtil(getActivity());
							util.reLoginWithAlertDialog();
							break;
						case -1:
							Toast.makeText(getActivity(), "发生了可怕的错误，代码：008，我们正在抢修", Toast.LENGTH_SHORT).show();
							break;
						case -2:
							Toast.makeText(getActivity(), "然鹅，你已经签过到了", Toast.LENGTH_SHORT).show();
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
				view = getView();
				if (view != null) {
					Snackbar.make(view, "网络开小差了~~", Snackbar.LENGTH_LONG).show();
				}
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});
	}

	public static CheckInFragment newInstance() {
		CheckInFragment fragment = new CheckInFragment();
		fragment.setRetainInstance(true);
		return fragment;
	}
}

