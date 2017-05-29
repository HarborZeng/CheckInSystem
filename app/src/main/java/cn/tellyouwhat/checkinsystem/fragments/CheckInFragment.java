package cn.tellyouwhat.checkinsystem.fragments;

import android.Manifest;
import android.animation.Animator;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.sunfusheng.marqueeview.MarqueeView;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.activities.BoardActivity;
import cn.tellyouwhat.checkinsystem.activities.MainActivity;
import cn.tellyouwhat.checkinsystem.bean.LocationItem;
import cn.tellyouwhat.checkinsystem.bean.Notice;
import cn.tellyouwhat.checkinsystem.db.LocationDB;
import cn.tellyouwhat.checkinsystem.utils.CookiedRequestParams;
import cn.tellyouwhat.checkinsystem.utils.NotifyUtil;
import cn.tellyouwhat.checkinsystem.utils.Polygon;

import static android.content.Context.MODE_PRIVATE;
import static cn.tellyouwhat.checkinsystem.bases.BaseApplication.api;

/**
 * Created by Harbor-Laptop on 2017/3/3.
 *
 * @author HarborZeng
 */

public class CheckInFragment extends BaseFragment {
	public static final int CHECK_IN_STATUS = 60;
	public static final int CHECK_OUT_STATUS = 61;
	public static final int CHECK_STATUS = 62;
	private final String TAG = "CheckInFragment";
	public LocationClient mLocationClient = null;
	public LocationClient mGetLocationClient = null;
	public BDLocationListener mCheckInLocationListener = new CheckInLocationListener();
	public BDLocationListener mGetLocationLocationListener = new GetLocationLocationListener();
	private AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
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
	private FloatingActionsMenu mMenuMultipleActions;
	private boolean isCheckingIn = true;
	private OnClickListener checkInListener = new OnClickListener() {
		@Override
		public void onClick(final View view) {
			isCheckingIn = true;
			if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
				//开始定位
				mLocationClient.start();
				mMenuMultipleActions.collapse();

				snackbar = Snackbar.make(view, R.string.getting_location, Snackbar.LENGTH_INDEFINITE);
				snackbar.setAction("取消", new OnClickListener() {
					@Override
					public void onClick(View v) {
						mLocationClient.stop();
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
	};
	private OnClickListener checkOutListener = new OnClickListener() {
		@Override
		public void onClick(final View view) {
			isCheckingIn = false;
			if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
				//开始定位
				mLocationClient.start();
				mMenuMultipleActions.collapse();

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
	};
	private boolean mHasCheckIn = false;
	private boolean mHasCheckOut = false;
	private SwipeRefreshLayout checkInSwipeRefreshLayout;
	private TextView mCheckStatusTextView;
	private TextView mLocationTextView;
	private CardView mLocationCardView;
	private SharedPreferences sharedPref;
	private MarqueeView marqueeView;
	private ArrayList<Notice> mNotices;
	private ToggleButton mEnableGPSToggleButton;
	private LocationManager mLocationManager;
	private ToggleButton mEnableWiFiToggleButton;
	private WifiManager mWifiManager;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
//		Log.i(TAG, "onCreate: in CheckInFragment");
		super.onCreate(savedInstanceState);
		getLocationGPSDetail();
	}

	@Override
	public void onResume() {
//		Log.i(TAG, "onResume: in CheckInFragment");
		super.onResume();
		mEnableWiFiToggleButton.setChecked(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED);
		mEnableGPSToggleButton.setChecked(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
		getTodayStatus();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mLocationClient != null) {
			mLocationClient.stop();
		}
		if (mGetLocationClient != null) {
			mGetLocationClient.stop();
		}
		if (mLocationCardView == null) {
			return;
		}
		mLocationCardView.setVisibility(View.GONE);
		if (snackbar != null && snackbar.isShownOrQueued()) {
			snackbar.dismiss();
		}
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
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
							mCheckStatusTextView.setText(formatter.format(new Date()) + "\n今日 ");
							mCheckStatusTextView.append(mHasCheckIn ? "已签到" : "未签到");
							mCheckStatusTextView.append(" " + (mHasCheckOut ? "已签出" : "未签出"));
//							Log.i(TAG, "onSuccess: 今日状态已更新");
						} catch (JSONException e) {
							e.printStackTrace();
//							Log.i(TAG, "onSuccess: 今日状态更新出错，json解析异常");
						}
						if (checkInSwipeRefreshLayout.isRefreshing()) {
							checkInSwipeRefreshLayout.setRefreshing(false);
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

	private void makeNotification() {
		NotifyUtil notifyUtil = new NotifyUtil(getContext(), CHECK_IN_STATUS);
		Intent leftIntent = new Intent(getContext(), MainActivity.class);
		leftIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		leftIntent.putExtra("BEGIN_CHECK_IN", true);
		PendingIntent leftPendingIntent = PendingIntent.getActivity(getContext(), CHECK_IN_STATUS, leftIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Intent rightIntent = new Intent(getContext(), MainActivity.class);
		rightIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		rightIntent.putExtra("BEGIN_CHECK_OUT", true);
		PendingIntent rightPendingIntent = PendingIntent.getActivity(getContext(), CHECK_OUT_STATUS, rightIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Intent intent = new Intent(getContext(), MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), CHECK_STATUS, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		notifyUtil.setOnGoing(true);

		notifyUtil.notifyTwoButton(pendingIntent, R.drawable.ic_golf_course,
				R.drawable.locate, "签到", leftPendingIntent,
				R.drawable.check_out, "签出", rightPendingIntent,
				"今日签到",
				"今日签到情况",
				"今日 " + (sharedPref.getBoolean("has_check_in", false) ? "已签到" : "未签到") + "    " + (sharedPref.getBoolean("has_check_out", false) ? "已签出" : "未签出"),
				false, false, false);

	}

	private void saveTodayStatus() {
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean("has_check_in", mHasCheckIn);
		editor.putBoolean("has_check_out", mHasCheckOut);
		editor.apply();
	}

	private void getLocationGPSDetail() {
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

								/*CompanyDB companyDB = new CompanyDB();
								CompanyLocationItem item = new CompanyLocationItem();
								item.setLocationID(locationIDs[i]);
								item.setLocationName(locationNames[i]);
								item.setX1(String.valueOf(x1));
								item.setX2(String.valueOf(x2));
								item.setX3(String.valueOf(x3));
								item.setX4(String.valueOf(x4));
								item.setY1(String.valueOf(y1));
								item.setY2(String.valueOf(y2));
								item.setY3(String.valueOf(y3));
								item.setY4(String.valueOf(y4));
								try {
									companyDB.saveCompany(item);
								} catch (DbException e) {
									e.printStackTrace();
									try {
										companyDB.updateCompany(item);
									} catch (DbException e1) {
										e1.printStackTrace();
									}
								}*/
								SharedPreferences.Editor editor = getActivity().getSharedPreferences("companies_location", MODE_PRIVATE).edit();
								editor.putString("companies_info_all", result.toString());
								editor.apply();
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
				Toast.makeText(x.app(), "获取公司位置出错", Toast.LENGTH_SHORT).show();
				ex.printStackTrace();
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
//		Log.i(TAG, "onCreateView: in CheckInFragment");
		final View view = inflater.inflate(R.layout.fragment_check_in, container, false);

		mLocationCardView = (CardView) view.findViewById(R.id.card_view_location);
		mLocationCardView.setVisibility(View.GONE);

		mLocationTextView = (TextView) view.findViewById(R.id.text_view_location);
		mLocationTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardManager clipboardManager = (ClipboardManager) getActivity().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
				clipboardManager.setPrimaryClip(ClipData.newPlainText(null, ((TextView) v).getText().toString()));  // 将内容set到剪贴板
				if (clipboardManager.hasPrimaryClip()) {
					Toast.makeText(getActivity(), "内容已复制", Toast.LENGTH_SHORT).show();
				}
			}
		});

		Button copyButton = (Button) view.findViewById(R.id.copy_button);
		copyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardManager clipboardManager = (ClipboardManager) getActivity().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
				clipboardManager.setPrimaryClip(ClipData.newPlainText(null, mLocationTextView.getText().toString()));  // 将内容set到剪贴板
				if (clipboardManager.hasPrimaryClip()) {
					Toast.makeText(getActivity(), "内容已复制", Toast.LENGTH_SHORT).show();
				}
			}
		});

		Button shareLocationToFriendsButton = (Button) view.findViewById(R.id.share_location_to_friends_button);
		shareLocationToFriendsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new MaterialDialog.Builder(getContext())
						.title("分享到...")
						.items("微信", "其他...")
						.itemsCallback(new MaterialDialog.ListCallback() {
							@Override
							public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
								String sharedMessage = mLocationTextView.getText().toString();
								switch (position) {
									case 0:
										shareToWeChatFriend(sharedMessage);
										break;
									case 1:
										shareToSystemDialog(sharedMessage);
								}
							}
						}).show();
			}

			private void shareToWeChatFriend(String text) {
				//使用Application全局静态变量，有违迪米特法则
				WXTextObject textObject = new WXTextObject();
				textObject.text = text;

				WXMediaMessage wxMediaMessage = new WXMediaMessage();
				wxMediaMessage.mediaObject = textObject;
				wxMediaMessage.description = text;

				SendMessageToWX.Req req = new SendMessageToWX.Req();
				req.transaction = String.valueOf(System.currentTimeMillis());
				req.message = wxMediaMessage;

				boolean sendSuccessfully = api.sendReq(req);
				if (!sendSuccessfully) {
					Toast.makeText(getActivity(), "分享失败，可能没有安装微信", Toast.LENGTH_SHORT).show();
				}
			}

			private void shareToSystemDialog(String text) {
				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_TEXT, text);
				shareIntent.setType("text/plain");
				//设置分享列表的标题，并且每次都显示分享列表
				startActivity(Intent.createChooser(shareIntent, "分享到"));
			}
		});

		mCheckStatusTextView = (TextView) view.findViewById(R.id.check_status_text_view);
		mCheckStatusTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getTodayStatus();
			}
		});

		ImageView checkInBGImageView = (ImageView) view.findViewById(R.id.image_view_check_in_bg);
		final int[] i = {0};

		checkInBGImageView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mLocationCardView.setVisibility(View.VISIBLE);
				if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
					if (i[0] == 0) {
						mGetLocationClient = new LocationClient(getActivity().getApplicationContext());
						//声明LocationClient类
						mGetLocationClient.registerLocationListener(mGetLocationLocationListener);
						initGetLocationLocation();
						mGetLocationClient.start();
						i[0]++;
					} else {
						Toast.makeText(getActivity(), "不要着急", Toast.LENGTH_SHORT).show();
					}
				} else {
					Snackbar.make(v, "必须要授权位置访问才能正常工作", Snackbar.LENGTH_INDEFINITE)
							.setAction("授权", new OnClickListener() {
								@Override
								public void onClick(View v) {
									String[] perms = {"android.permission.ACCESS_FINE_LOCATION"};
									ActivityCompat.requestPermissions(getActivity(), perms, 1);
								}
							}).show();
				}
				return true;
			}
		});
		checkInBGImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGetLocationClient != null) {
					mGetLocationClient.stop();
				}
				i[0] = 0;
				mLocationCardView.setVisibility(View.GONE);
			}
		});

		checkInSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.check_in_swipe_refresh_layout);
		checkInSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.theme_purple_primary, R.color.theme_yellow_primary, R.color.theme_red_primary, R.color.theme_green_primary, R.color.theme_blue_primary, R.color.pink);
		checkInSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				getTodayStatus();
				prepareBoard();
				getLocationGPSDetail();
				imageView2_cover_in50.setVisibility(View.INVISIBLE);
				imageView2_cover_out50.setVisibility(View.INVISIBLE);
				imageView_cover_in_company.setVisibility(View.INVISIBLE);
				succeed.setVisibility(View.INVISIBLE);
				imageView_cover_finally_success.setVisibility(View.INVISIBLE);
				out_of_range.setVisibility(View.INVISIBLE);
				imageView_cover_out_company.setVisibility(View.INVISIBLE);
				in_range.setVisibility(View.INVISIBLE);
				enable_wifi_gps_textView.setVisibility(View.INVISIBLE);
				enough_accuracy_text_view.setVisibility(View.INVISIBLE);
			}
		});

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

		mMenuMultipleActions = (FloatingActionsMenu) view.findViewById(R.id.multiple_actions);
		x.task().postDelayed(new Runnable() {
			@Override
			public void run() {
				mMenuMultipleActions.expand();
			}
		}, 200);

		final FloatingActionButton actionB = (FloatingActionButton) view.findViewById(R.id.action_b);
		actionB.setIcon(R.drawable.check_out);
		actionB.setOnClickListener(checkOutListener);

		final FloatingActionButton actionA = (FloatingActionButton) view.findViewById(R.id.action_a);
		actionA.setIcon(R.drawable.locate);
		actionA.setOnClickListener(checkInListener);

		mLocationClient = new LocationClient(getActivity().getApplicationContext());
		//声明LocationClient类
		mLocationClient.registerLocationListener(mCheckInLocationListener);
		//注册监听函数
		initCheckInLocation();

		Bundle arguments = getArguments();
		final boolean beginCheckIn = arguments.getBoolean("BEGIN_CHECK_IN", false);
		final boolean beginCheckOut = arguments.getBoolean("BEGIN_CHECK_OUT", false);
		x.task().post(new Runnable() {
			@Override
			public void run() {
				if (beginCheckIn || beginCheckOut) {
					isCheckingIn = beginCheckIn;
//					Log.d(TAG, "run: isCheckIn????" + isCheckingIn);
					if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
						//开始定位
						mLocationClient.start();
						mMenuMultipleActions.collapse();

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
										SystemClock.sleep(300);
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
			}
		});

		marqueeView = (MarqueeView) view.findViewById(R.id.marqueeView);
		prepareBoard();
		marqueeView.setOnItemClickListener(new MarqueeView.OnItemClickListener() {
			@Override
			public void onItemClick(int position, TextView textView) {
				Intent intent = new Intent(getContext(), BoardActivity.class);
				Notice notice = mNotices.get(position);
				intent.putExtra("notice", notice);
				startActivity(intent);
			}
		});

		mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		mEnableWiFiToggleButton = (ToggleButton) view.findViewById(R.id.toggle_button_enable_wifi);
		mEnableWiFiToggleButton.setChecked(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED);
		mEnableWiFiToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					boolean wifiEnabled = mWifiManager.setWifiEnabled(true);
					if (wifiEnabled) {
						Toast.makeText(getContext(), "Wi-Fi已启用", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getContext(), "启用Wi-Fi出错，是否没有权限？", Toast.LENGTH_SHORT).show();
					}
				} else {
					boolean wifiEnabled = mWifiManager.setWifiEnabled(false);
					if (wifiEnabled) {
						Toast.makeText(getContext(), "Wi-Fi已关闭", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getContext(), "关闭Wi-Fi出错，是否没有权限？", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		mLocationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		mEnableGPSToggleButton = (ToggleButton) view.findViewById(R.id.toggle_button_enable_GPS);
		mEnableGPSToggleButton.setChecked(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
		mEnableGPSToggleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		});

		return view;
	}

	private void prepareBoard() {
		x.http().get(new RequestParams("https://api.checkin.tellyouwhat.cn/Notice/GetAllNotices"), new Callback.CommonCallback<JSONObject>() {
			@Override
			public void onSuccess(JSONObject result) {
//				Log.d(TAG, "onSuccess: 板子上要写的东西有："+result);
				int resultInt = 0;
				try {
					resultInt = result.getInt("result");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (resultInt == 1) {
					mNotices = new ArrayList<>();
					JSONArray data = null;
					try {
						data = result.getJSONArray("data");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					for (int i = 0; i < (data != null ? data.length() : 0); i++) {
						try {
							JSONObject jsonObject = data.getJSONObject(i);
							String title = jsonObject.getString("Title");
							String content = jsonObject.getString("Content");
							String time = jsonObject.getString("Time").replace("T", " ").substring(0, 16);
							String author = jsonObject.getString("Author");
							Notice notice = new Notice();
							notice.setContent(content);
							notice.setTitle(title);
							notice.setTime(time);
							notice.setAuthor(author);
							mNotices.add(notice);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					List<String> noticeTitles = new ArrayList<>();
					for (Notice noticeTitle :
							mNotices) {
						noticeTitles.add(noticeTitle.getTitle());
					}
					marqueeView.startWithList(noticeTitles);
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				Toast.makeText(x.app(), "加载通知出错", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy: in CheckInFragment");
		super.onDestroy();
	}

	@Override
	public void onStart() {
		super.onStart();
		marqueeView.startFlipping();
	}

	@Override
	public void onStop() {
		super.onStop();
		marqueeView.stopFlipping();
	}

	private void initCheckInLocation() {
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

	private void initGetLocationLocation() {
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
		mGetLocationClient.setLocOption(option);
	}

	private class CheckInLocationListener implements BDLocationListener {
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
/*			final StringBuffer sb = new StringBuffer(256);
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

			Log.i("BaiduLocationApiDem", sb.toString());*/

			if (radius <= 50 && radius != 0) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						times = 0;
						enable_wifi_gps_textView.setVisibility(View.INVISIBLE);
						enough_accuracy_text_view.setText("精度合格\n");
						enough_accuracy_text_view.append(location.getLocationDescribe() == null ? "未知" : location.getLocationDescribe());
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
									final String time = location.getTime();
									item.setGotFromService(false);
									item.setLocationType(location.getLocType());
									item.setRadius(radius);
									item.setTime(time);
									item.setBuildingID(locationIDs[i]);
									item.setLatitude(Double.toString(location.getLatitude()));
									item.setLongitude(Double.toString(location.getLongitude()));
									item.setAddress(location.getAddrStr());
									item.setLocationDescription(location.getLocationDescribe());
									item.setBuildingDesc(locationNames[i]);
									item.setUserID(getActivity().getSharedPreferences("userInfo", MODE_PRIVATE).getString("employeeID", ""));
									locationDB.saveLocation(item);

//									Log.i("zdhobuzd", "run: 在");
									mLocationClient.stop();
									snackbar.dismiss();
									new Thread(new Runnable() {
										@Override
										public void run() {
											SystemClock.sleep(1100);
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
															.onEnd(new YoYo.AnimatorCallback() {
																@Override
																public void call(Animator animator) {
																	enough_accuracy_text_view.setVisibility(View.INVISIBLE);
																	enable_wifi_gps_textView.setVisibility(View.INVISIBLE);
																}
															})
															.playOn(in_range);
													YoYo.with(Techniques.Tada)
															.duration(700)
															.repeat(1)
															.playOn(imageView_cover_in_company);
												}
											});
											SystemClock.sleep(1100);
											if (isCheckingIn) {
												beginCheckingIn();
											} else {
												beginCheckingOut();
											}
										}
									}).start();
									break;
								} else {
//									Log.i("zdhobuzd", "run: 不在");
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
//			Log.w("onConnectHotSpotMessage", "onConnectHotSpotMessage: s: " + s + ", i: " + i);
		}
	}

	private class GetLocationLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(final BDLocation location) {
			alphaAnimation.setDuration(800);
			final float radius = location.getRadius();
			final double latitude = location.getLatitude();
			final double longitude = location.getLongitude();

			final StringBuilder builder = new StringBuilder(256);
			builder.append("时间: ");
			builder.append(location.getTime());    //获取定位时间

			builder.append("\n纬度: ");
			builder.append(latitude);    //获取纬度信息

			builder.append("\n经度: ");
			builder.append(longitude);    //获取经度信息

			builder.append("\n精度: ");
			builder.append(radius).append("米");    //获取定位精准度

			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				// GPS定位结果
				builder.append("\n速度: ");
				builder.append(location.getSpeed()).append("km/h");    // 单位：公里每小时

				builder.append("\n卫星数: ");
				builder.append(location.getSatelliteNumber()).append("颗");    //获取卫星数

				builder.append("\n海拔: ");
				builder.append(location.getAltitude()).append("米");    //获取海拔高度信息，单位米

				builder.append("\n方向: ");
				builder.append(location.getDirection()).append("度");    //获取方向信息，单位度

				builder.append("\n地址: ");
				builder.append(location.getAddrStr());    //获取地址信息

				builder.append("\n描述: ");
				builder.append("gps定位成功");

			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {

				// 网络定位结果
				builder.append("\n地址: ");
				builder.append(location.getAddrStr());    //获取地址信息

				builder.append("\n描述: ");
				builder.append("网络定位成功");

			} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {

				// 离线定位结果
				builder.append("\n描述: ");
				builder.append("离线定位成功，离线定位结果也是有效的");

			} else if (location.getLocType() == BDLocation.TypeServerError) {

				builder.append("\n描述: ");
				builder.append("服务端网络定位失败");

			} else if (location.getLocType() == BDLocation.TypeNetWorkException) {

				builder.append("\n描述: ");
				builder.append("网络不同导致定位失败，请检查网络是否通畅");

			} else if (location.getLocType() == BDLocation.TypeCriteriaException) {

				builder.append("\n描述: ");
				builder.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");

			}

			builder.append("\n位置信息描述: ");
			builder.append(location.getLocationDescribe() == null || "null".equals(location.getLocationDescribe()) ? "离线定位，位置未知" : location.getLocationDescribe());    //位置语义化信息
			if (polygons == null) {
				Toast.makeText(x.app(), "还在初始化数据", Toast.LENGTH_SHORT).show();
			} else {
				for (int i = 0; i < polygons.length; i++) {
					Log.d(TAG, "onReceiveLocation: 测验: " + locationNames[i]);
					if (polygons[i].contains(longitude * 1000000, latitude * 1000000)) {
						Log.d(TAG, "onReceiveLocation: 在" + locationNames[i]);
						builder.append("\n身处: ").append(TextUtils.isEmpty(locationNames[i]) ? "不在公司范围" : locationNames[i]);
						break;
					} else {
						Log.d(TAG, "onReceiveLocation: 不在" + locationNames[i]);
					}
				}
				Log.i("BaiduLocationApiDem", builder.toString());
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mLocationTextView.setText(builder.toString());
						mLocationCardView.setVisibility(View.VISIBLE);
//					mLocationCardView.startAnimation(alphaAnimation);
					}
				});
			}
		}

		@Override
		public void onConnectHotSpotMessage(String s, int i) {
//			Log.w("onConnectHotSpotMessage", "onConnectHotSpotMessage: s: " + s + ", i: " + i);
		}
	}

	private void beginCheckingOut() {
		CookiedRequestParams requestParams = new CookiedRequestParams("https://api.checkin.tellyouwhat.cn/checkin/checkout");
		x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {
			@Override
			public void onSuccess(JSONObject result) {
				Log.i(TAG, "onSuccess: 签出结果：" + result.toString());
				try {
					int resultInt = result.getInt("result");
					switch (resultInt) {
						case 1:
							getTodayStatus();
							imageView_cover_finally_success.setVisibility(View.VISIBLE);
							succeed.setVisibility(View.VISIBLE);
							succeed.setText("签出成功");
							YoYo.with(Techniques.Tada)
									.duration(700)
									.repeat(1)
									.onEnd(new YoYo.AnimatorCallback() {
										@Override
										public void call(Animator animator) {
											enable_wifi_gps_textView.setVisibility(View.INVISIBLE);
											enough_accuracy_text_view.setVisibility(View.INVISIBLE);
											in_range.setVisibility(View.INVISIBLE);
											out_of_range.setVisibility(View.INVISIBLE);
										}
									})
									.playOn(imageView_cover_finally_success);
							YoYo.with(Techniques.Tada)
									.duration(700)
									.repeat(1)
									.playOn(succeed);
							SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
							boolean showNotifications = sharedPref.getBoolean("show_notifications", true);
							boolean notificationsRingEnabled = sharedPref.getBoolean("notifications_ring_enabled", false);
							boolean notificationsVibrateEnabled = sharedPref.getBoolean("notifications_vibrate_enabled", true);
							if (showNotifications) {
								Intent intent = new Intent(getActivity(), MainActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
								PendingIntent pIntent = PendingIntent.getActivity(x.app(), 16, intent, PendingIntent.FLAG_UPDATE_CURRENT);
								String ticker = "您有一条新通知";
								String title = "签出成功";
								String content = "恭喜您，今日签出成功";
								NotifyUtil notificationSucceededCheckIn = new NotifyUtil(x.app(), 16);
								notificationSucceededCheckIn.setOnGoing(false);
								notificationSucceededCheckIn.notify_normal_singline(pIntent, R.drawable.ic_stat_name, ticker, title, content, notificationsRingEnabled, notificationsVibrateEnabled, false);
							}
							break;
						case 0:
							updateSession();
							break;
						case -1:
							Toast.makeText(x.app(), "发生了可怕的错误，代码：008，我们正在抢修", Toast.LENGTH_SHORT).show();
							break;
						case -2:
							imageView_cover_finally_success.setVisibility(View.VISIBLE);
							succeed.setText(result.getString("message"));
							succeed.setVisibility(View.VISIBLE);
							YoYo.with(Techniques.Tada)
									.duration(700)
									.repeat(1)
									.onEnd(new YoYo.AnimatorCallback() {
										@Override
										public void call(Animator animator) {
											enable_wifi_gps_textView.setVisibility(View.INVISIBLE);
											enough_accuracy_text_view.setVisibility(View.INVISIBLE);
											in_range.setVisibility(View.INVISIBLE);
											out_of_range.setVisibility(View.INVISIBLE);
										}
									})
									.playOn(imageView_cover_finally_success);
							YoYo.with(Techniques.Tada)
									.duration(700)
									.repeat(1)
									.playOn(succeed);
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

	private void beginCheckingIn() {
		CookiedRequestParams requestParams = new CookiedRequestParams("https://api.checkin.tellyouwhat.cn/checkin/checkin");
		x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {
			@Override
			public void onSuccess(JSONObject result) {
//				Log.i(TAG, "onSuccess: 签到结果：" + result.toString());
				try {
					getTodayStatus();
					int resultInt = result.getInt("result");
					switch (resultInt) {
						case 1:
							imageView_cover_finally_success.setVisibility(View.VISIBLE);
							succeed.setText("签到成功");
							succeed.setVisibility(View.VISIBLE);
							YoYo.with(Techniques.Tada)
									.duration(700)
									.repeat(1)
									.onEnd(new YoYo.AnimatorCallback() {
										@Override
										public void call(Animator animator) {
											enable_wifi_gps_textView.setVisibility(View.INVISIBLE);
											enough_accuracy_text_view.setVisibility(View.INVISIBLE);
											in_range.setVisibility(View.INVISIBLE);
											out_of_range.setVisibility(View.INVISIBLE);
										}
									})
									.playOn(imageView_cover_finally_success);
							YoYo.with(Techniques.Tada)
									.duration(700)
									.repeat(1)
									.playOn(succeed);
							SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
							boolean showNotifications = sharedPref.getBoolean("show_notifications", true);
							boolean notificationsRingEnabled = sharedPref.getBoolean("notifications_ring_enabled", false);
							boolean notificationsVibrateEnabled = sharedPref.getBoolean("notifications_vibrate_enabled", true);
							if (showNotifications) {
								Intent intent = new Intent(getActivity(), MainActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
								PendingIntent pIntent = PendingIntent.getActivity(x.app(), 15, intent, PendingIntent.FLAG_UPDATE_CURRENT);
								String ticker = "您有一条新通知";
								String title = "签到成功";
								String content = "恭喜您，今日签到成功";
								NotifyUtil notificationSucceededCheckIn = new NotifyUtil(x.app(), 15);
								notificationSucceededCheckIn.setOnGoing(false);
								notificationSucceededCheckIn.notify_normal_singline(pIntent, R.drawable.ic_stat_name, ticker, title, content, notificationsRingEnabled, notificationsVibrateEnabled, true);
							}
							break;
						case 0:
							updateSession();
							break;
						case -1:
							Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_SHORT).show();
							break;
						case -2:
							imageView_cover_finally_success.setVisibility(View.VISIBLE);
							succeed.setText(result.getString("message"));
							succeed.setVisibility(View.VISIBLE);
							YoYo.with(Techniques.Tada)
									.duration(700)
									.repeat(1)
									.onEnd(new YoYo.AnimatorCallback() {
										@Override
										public void call(Animator animator) {
											enable_wifi_gps_textView.setVisibility(View.INVISIBLE);
											enough_accuracy_text_view.setVisibility(View.INVISIBLE);
											in_range.setVisibility(View.INVISIBLE);
											out_of_range.setVisibility(View.INVISIBLE);
										}
									})
									.playOn(imageView_cover_finally_success);
							YoYo.with(Techniques.Tada)
									.duration(700)
									.repeat(1)
									.playOn(succeed);
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
				ex.printStackTrace();
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