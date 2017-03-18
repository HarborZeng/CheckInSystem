package cn.tellyouwhat.checkinsystem.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.transition.Scene;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
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
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import cn.tellyouwhat.checkinsystem.R;

/**
 * Created by Harbor-Laptop on 2017/3/3.
 *
 * @author HarborZeng
 */

public class CheckInFragment extends Fragment {
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	private ImageView imageView2_cover_in50;
	private ImageView imageView2_cover_out50;
	AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);




	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_check_in, container, false);
		imageView2_cover_in50 = (ImageView) view.findViewById(R.id.imageView2_cover_in50);
		imageView2_cover_out50 = (ImageView) view.findViewById(R.id.imageView2_cover_out50);

//		LayoutInflater inflater2 = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		groupPollingAddress = (LinearLayout)inflater2.inflate(R.layout.three_state, null);

		final ViewGroup sceneRoot = (ViewGroup) view.findViewById(R.id.scene_root);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			TransitionManager.go(Scene.getSceneForLayout(sceneRoot, R.layout.three_state, getActivity()), TransitionInflater.from(getActivity()).inflateTransition(R.transition.slide_and_changebounds_sequential_with_interpolators));

		}

		final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) view.findViewById(R.id.multiple_actions);
		menuMultipleActions.expand();

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

					Snackbar.make(view, R.string.getting_location, Snackbar.LENGTH_INDEFINITE)
							.setAction("取消", new OnClickListener() {
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
													getActivity().findViewById(R.id.imageView_cover_out_company).setVisibility(View.INVISIBLE);
													getActivity().findViewById(R.id.imageView_cover_in_company).setVisibility(View.INVISIBLE);
													getActivity().findViewById(R.id.succeed).setVisibility(View.INVISIBLE);
													getActivity().findViewById(R.id.imageView_cover_finally_success).setVisibility(View.INVISIBLE);
													getActivity().findViewById(R.id.out_of_range).setVisibility(View.INVISIBLE);
													getActivity().findViewById(R.id.in_range).setVisibility(View.INVISIBLE);
													imageView2_cover_in50.setVisibility(View.INVISIBLE);
													imageView2_cover_out50.setVisibility(View.INVISIBLE);
													getActivity().findViewById(R.id.enable_wifi_GPS_textView).setVisibility(View.INVISIBLE);
													getActivity().findViewById(R.id.enough_accuracy_text_view).setVisibility(View.INVISIBLE);
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

		int span = 2100;
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


		JSONObject jsonCreator = new JSONObject();
		private int times = 0;

		@Override
		public void onReceiveLocation(final BDLocation location) {
			alphaAnimation.setDuration(800);
			float radius = location.getRadius();

			try {
				jsonCreator.put("TIME", location.getTime());
				jsonCreator.put("LATITUDE", location.getLatitude());
				jsonCreator.put("LONGITUDE", location.getLongitude());
				jsonCreator.put("RADIUS", radius);
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), "cannot create json of general info successfully, please feedback", Toast.LENGTH_LONG).show();
			}




			/*
			  以下内容属于测试代码
			 */
			//获取定位结果
/*			final StringBuffer sb = new StringBuffer(256);
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

			Log.i("BaiduLocationApiDem", sb.toString());*/

			if (radius < 50 && radius != 0) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						times = 0;
						TextView enough_accuracy_text_view = (TextView) getActivity().findViewById(R.id.enough_accuracy_text_view);
						getActivity().findViewById(R.id.enable_wifi_GPS_textView).setVisibility(View.INVISIBLE);
						enough_accuracy_text_view.setText("精度合格\n");
						enough_accuracy_text_view.append(location.getLocationDescribe() == null ? "离线定位" : location.getLocationDescribe());
						enough_accuracy_text_view.setVisibility(View.VISIBLE);
						enough_accuracy_text_view.startAnimation(alphaAnimation);
						imageView2_cover_in50.setVisibility(View.VISIBLE);
						imageView2_cover_out50.setVisibility(View.INVISIBLE);
						imageView2_cover_in50.startAnimation(alphaAnimation);

					}
				});

				uploadLocationInfo(jsonCreator);
			} else {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (times > 2) {
							TextView enable_wifi_GPS_textView = (TextView) getActivity().findViewById(R.id.enable_wifi_GPS_textView);
							enable_wifi_GPS_textView.setVisibility(View.VISIBLE);
							enable_wifi_GPS_textView.startAnimation(alphaAnimation);
						}
						times++;
						imageView2_cover_in50.setVisibility(View.INVISIBLE);
						getActivity().findViewById(R.id.enough_accuracy_text_view).setVisibility(View.INVISIBLE);
						imageView2_cover_out50.setVisibility(View.VISIBLE);
						imageView2_cover_out50.startAnimation(alphaAnimation);
					}
				});
			}

		}

		@Override
		public void onConnectHotSpotMessage(String s, int i) {
			Log.w("onConnectHotSpotMessage", "onConnectHotSpotMessage: s: " + s + ", i: " + i);
		}
	}

	/**
	 * 向服务器传送地理位置坐标信息的json文件的一个方法
	 *
	 * @param jsonCreator 包含地理位置信息的json对象
	 */
	private void uploadLocationInfo(final JSONObject jsonCreator) {
		alphaAnimation.setDuration(800);

		final ImageView imageView_cover_out_company = (ImageView) getActivity().findViewById(R.id.imageView_cover_out_company);
		final TextView out_of_range = (TextView) getActivity().findViewById(R.id.out_of_range);

		RequestParams entity = new RequestParams("http://tellyouwhat.cn/location_verify/location_verify");
		entity.setAsJsonContent(true);
		entity.setBodyContent(jsonCreator.toString());

//		Log.i("发送过去的数据", "uploadLocationInfo: " + jsonCreator.toString());

		x.http().post(entity, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {

				switch (result) {
					case "out":
						//不在办公区域范围内
//						Log.i("uploadLocationInfo", "onSuccess: 不在办公区域范围内");
						imageView_cover_out_company.setVisibility(View.VISIBLE);
						imageView_cover_out_company.startAnimation(alphaAnimation);
						out_of_range.startAnimation(alphaAnimation);
						out_of_range.setVisibility(View.VISIBLE);
						break;
					case "in":
						//在办公区域范围内
//						Log.i("uploadLocationInfo", "onSuccess: 在办公区域范围内");
						mLocationClient.stop();
						imageView_cover_out_company.setVisibility(View.INVISIBLE);
						out_of_range.setVisibility(View.INVISIBLE);

						ImageView imageView_cover_in_company = (ImageView) getActivity().findViewById(R.id.imageView_cover_in_company);
						imageView_cover_in_company.setVisibility(View.VISIBLE);
						imageView_cover_in_company.setAnimation(alphaAnimation);
						TextView in_range = (TextView) getActivity().findViewById(R.id.in_range);
						in_range.startAnimation(alphaAnimation);
						in_range.setVisibility(View.VISIBLE);

						//时间格式大概是这样的：2017-03-06 09:39:59
						try {
							beginCheckingIn(jsonCreator.getString("TIME"));
						} catch (JSONException e) {
							e.printStackTrace();
						}

						break;
					case "Database Connect failed":
						//打开数据库失败
						Log.d("uploadLocationInfo", "Database Connect failed");
						Toast.makeText(getActivity(), R.string.server_busy, Toast.LENGTH_SHORT).show();

					default:
						Log.w("Result code", "onSuccess: Wrong result code: " + result);
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				Log.w("发送完坐标数据的返回", "onError: " + ex);
			}

			@Override
			public void onCancelled(CancelledException cex) {
				Log.w("发送完坐标数据的返回", "onCanceled: " + cex);
			}

			@Override
			public void onFinished() {
//				Log.w("发送完坐标数据的返回", "onFinished");
			}
		});

	}

	private void beginCheckingIn(String time) {

	}

	public static CheckInFragment newInstance() {
		CheckInFragment fragment = new CheckInFragment();
		fragment.setRetainInstance(true);
		return fragment;
	}
}
