package cn.tellyouwhat.checkinsystem.fragments;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

import cn.tellyouwhat.checkinsystem.R;

/**
 * Created by Harbor-Laptop on 2017/3/3.
 *
 * @author HarborZeng
 */

public class CheckInFragment extends Fragment {
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_check_in, container, false);

		final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) view.findViewById(R.id.multiple_actions);

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
			public void onClick(View view) {
				if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
//开始定位
					mLocationClient.start();
					menuMultipleActions.collapse();
					Snackbar.make(view, R.string.getting_location, Snackbar.LENGTH_INDEFINITE)
							.setAction("取消", new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									mLocationClient.stop();
									//取消定位请求
								}
							}).show();
				} else {
					Snackbar.make(view, "必须要授权位置访问才能正常工作", Snackbar.LENGTH_INDEFINITE)
							.setAction("授权", new View.OnClickListener() {
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

		int span = 1000;
		option.setScanSpan(span);
		//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

		option.setIsNeedAddress(true);
		//可选，设置是否需要地址信息，默认不需要

		option.setOpenGps(true);
		//可选，默认false,设置是否使用gps

		option.setLocationNotify(true);
		//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

		option.setIsNeedLocationDescribe(true);
		//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

		option.setIsNeedLocationPoiList(true);
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

		@Override
		public void onReceiveLocation(BDLocation location) {

			//获取定位结果
			final StringBuffer sb = new StringBuffer(256);
			try {
				jsonCreator.put("TIME", location.getTime());
				jsonCreator.put("LATITUDE", location.getLatitude());
				jsonCreator.put("LONGITUDE", location.getLongitude());
				jsonCreator.put("RADIUS", location.getRadius());
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), "cannot create json of general info successfully, please feedback", Toast.LENGTH_LONG).show();
			}
			sb.append("time : ");
			sb.append(location.getTime());    //获取定位时间

			sb.append("\nerror code : ");
			sb.append(location.getLocType());    //获取类型类型

			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());    //获取纬度信息

			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());    //获取经度信息

			sb.append("\nradius : ");
			sb.append(location.getRadius());    //获取定位精准度

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
			sb.append(location.getLocationDescribe());    //位置语义化信息

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

			uploadLocationInfo(jsonCreator);

		}

		@Override
		public void onConnectHotSpotMessage(String s, int i) {
			Log.w("onConnectHotSpotMessage", "onConnectHotSpotMessage: s: " + s + ", i: " + i);
		}
	}

	/**
	 * 向服务器穿送地理位置坐标信息的json文件的一个方法
	 *
	 * @param jsonCreator 包含地理位置信息的
	 */
	private void uploadLocationInfo(JSONObject jsonCreator) {

		RequestParams entity = new RequestParams("http://tellyouwhat.cn/varify_location");
		entity.setAsJsonContent(true);
		entity.setBodyContent(jsonCreator.toString());

		x.http().post(entity, new Callback.CommonCallback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				switch (result) {
					case 0:
						//不在办公区域范围内
						break;
					case 1:
						//在办公区域范围内
						break;
					default:
						Log.w("Result code", "onSuccess: Wrong result code" + result);
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

	public static CheckInFragment newInstance() {
		CheckInFragment fragment = new CheckInFragment();
		fragment.setRetainInstance(true);
		return fragment;
	}
}
