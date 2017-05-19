package cn.tellyouwhat.checkinsystem.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.jaeger.library.StatusBarUtil;
import com.xdandroid.hellodaemon.DaemonEnv;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.math.RoundingMode;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.fragments.CheckInFragment;
import cn.tellyouwhat.checkinsystem.fragments.HistoryFragment;
import cn.tellyouwhat.checkinsystem.fragments.MeFragment;
import cn.tellyouwhat.checkinsystem.services.AutoCheckInService;
import cn.tellyouwhat.checkinsystem.services.LocationGettingService;
import cn.tellyouwhat.checkinsystem.services.UpdateTodayStatusService;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.DoubleUtil;
import cn.tellyouwhat.checkinsystem.utils.NetTypeUtils;
import cn.tellyouwhat.checkinsystem.utils.ReLoginUtil;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends BaseActivity {

	private String TAG = "MainActivity";

	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= new BottomNavigationView.OnNavigationItemSelectedListener() {

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

			Fragment history = getSupportFragmentManager().findFragmentByTag("History");
			Fragment me = getSupportFragmentManager().findFragmentByTag("Me");
			Fragment checkIn = getSupportFragmentManager().findFragmentByTag("CheckIn");

			switch (item.getItemId()) {
				case R.id.navigation_check_in:
					setTitle(item.getTitle());
					if (checkIn != null)
						fragmentTransaction.show(checkIn);
					if (history != null)
						fragmentTransaction.hide(history);
					if (me != null)
						fragmentTransaction.hide(me);
					fragmentTransaction.commit();
					return true;
				case R.id.navigation_history_record:
					setTitle(item.getTitle());
					if (checkIn != null)
						fragmentTransaction.hide(checkIn);
					if (history != null)
						fragmentTransaction.show(history);
					else
						fragmentTransaction.add(R.id.content, HistoryFragment.newInstance(), "History");
					if (me != null)
						fragmentTransaction.hide(me);
					fragmentTransaction.commit();
					return true;
				case R.id.navigation_me:
					setTitle(item.getTitle());
					if (checkIn != null)
						fragmentTransaction.hide(checkIn);
					if (history != null)
						fragmentTransaction.hide(history);

					if (me != null)
						fragmentTransaction.show(me);
					else
						fragmentTransaction.add(R.id.content, MeFragment.newInstance(), "Me");

					fragmentTransaction.commit();
					return true;
			}
			return false;
		}

	};
	private String mVersionName;
	private String mVersionDesc;
	private String mVersionCode;
	private String mDownloadURL;
	private boolean mForceUpgrade;
	private String mSize;

	@Override
	protected void onResume() {
		super.onResume();
		boolean canMockLocation = canMockLocation();
		detectMockLocation(canMockLocation);
	}

	private void detectMockLocation(boolean canMockLocation) {
		if (canMockLocation) {
			new MaterialDialog.Builder(MainActivity.this)
					.title("检测到您开启了“模拟位置”")
					.content("\n您必须前往“开发者选项”\n\n关闭模拟位置相关选项后，才能继续使用CheckIn\n\n或关闭虚拟定位软件")
					.positiveText("设置")
					.icon(getApplication().getResources().getDrawable(R.mipmap.warning))
					.onPositive(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
//							finish();
						}
					})
					.negativeText("重试")
					.onNegative(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							boolean canMockLocation = canMockLocation();
							detectMockLocation(canMockLocation);
						}
					})
					.show()
					.setCancelable(false);
		}
	}

	private boolean canMockLocation() {
		boolean canMockLocation = false;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			canMockLocation = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;
		} else {
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				return false;
			}
			PendingIntent intent = PendingIntent.getActivities(MainActivity.this, 99, new Intent[]{new Intent()}, PendingIntent.FLAG_ONE_SHOT);
			locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, intent);
			locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, intent);
			Location locationNetWork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (locationGPS != null && locationNetWork != null) {
				canMockLocation = locationNetWork.isFromMockProvider() || locationGPS.isFromMockProvider();
			}
			locationManager.removeUpdates(intent);
			Log.i(TAG, "canMockLocation: canMockLocation? " + canMockLocation);
		}
		return canMockLocation;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		Log.i(TAG, "onCreate: in MainActivity");
		super.onCreate(savedInstanceState);
		setBackEnable(false);
		setContentView(R.layout.activity_main);

//		Themer.INSTANCE.init(getApplication(), R.style.AppTheme);//设置默认主题

		Intent checkIntent = getIntent();
		boolean beginCheckIn = checkIntent.getBooleanExtra("BEGIN_CHECK_IN", false);
		boolean beginCheckOut = checkIntent.getBooleanExtra("BEGIN_CHECK_OUT", false);
		Bundle bundle = new Bundle();
//		Log.d(TAG, "onCreate: BEGIN_CHECK_IN:"+beginCheckIn);
//		Log.d(TAG, "onCreate: BEGIN_CHECK_OUT:"+beginCheckOut);
		bundle.putBoolean("BEGIN_CHECK_IN", beginCheckIn);
		bundle.putBoolean("BEGIN_CHECK_OUT", beginCheckOut);

		//解决Fragment可能出现的重叠问题
		if (savedInstanceState == null) {
			// 正常情况下去 加载根Fragment
			ActionBar supportActionBar = getSupportActionBar();
			if (supportActionBar != null) {
				supportActionBar.setDisplayHomeAsUpEnabled(false);
			}
			CheckInFragment checkInFragment = CheckInFragment.newInstance();
			checkInFragment.setArguments(bundle);
			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.content, checkInFragment, "CheckIn")
					.commit();
		}

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (sharedPref.getBoolean("immersed_status_bar_enabled", true)) {
			StatusBarUtil.setColor(MainActivity.this, getResources().getColor(R.color.colorPrimary), 0);
		}

		startServices(sharedPref);

		BottomNavigationView mNavigation = (BottomNavigationView) findViewById(R.id.navigation);
		mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

		new Thread(new Runnable() {
			@Override
			public void run() {
				checkUpdate();
			}
		}).start();
	}

	private void startServices(SharedPreferences sharedPref) {
		SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
		final String token = userInfo.getString(ConstantValues.TOKEN, "");
		if (!TextUtils.isEmpty(token)) {
			//开启获取位置的后台服务
			boolean backGroundServiceEnabled = sharedPref.getBoolean("use_background_service", true);
			if (backGroundServiceEnabled) {
				DaemonEnv.initialize(getApplicationContext(), LocationGettingService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
				try {
					startService(new Intent(this, LocationGettingService.class));
				} catch (Exception ignored) {
				}
//			Intent intent = new Intent(getApplicationContext(), LocationGettingService.class);
//			startService(intent);
			}

//		Intent intent = new Intent(getApplicationContext(), AutoCheckInService.class);
//		startService(intent);
			DaemonEnv.initialize(getApplicationContext(), AutoCheckInService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
			try {
				startService(new Intent(this, AutoCheckInService.class));
			} catch (Exception ignored) {
			}

			DaemonEnv.initialize(getApplicationContext(), UpdateTodayStatusService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
			try {
				startService(new Intent(this, UpdateTodayStatusService.class));
			} catch (Exception ignored) {
			}
		}
	}

	@Override
	protected void onDestroy() {
//		Log.i(TAG, "onDestroy: in MainActivity");
		super.onDestroy();
		ReLoginUtil.removeAllDialog();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

/*	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - time > 1600)) {
				Toast.makeText(this, R.string.press_one_more_time, Toast.LENGTH_SHORT).show();
				time = System.currentTimeMillis();
			} else {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				startActivity(intent);
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}*/

	//重写这个方法，防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀.
	//重写 MainActivity.onBackPressed(), 只保留对以下 API 的调用.
/*	@Override
	public void onBackPressed() {
//		IntentWrapper.onBackPressed(this);
	}*/

	public void checkUpdate() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()) {
//			Log.d(TAG, "checkUpdate: 网络正常");

			RequestParams params = new RequestParams("https://update.checkin.tellyouwhat.cn/update.json");
			x.http().get(params, new Callback.CommonCallback<JSONObject>() {
				@Override
				public void onSuccess(JSONObject object) {
					try {
						mVersionName = object.getString("versionName");
						mVersionDesc = object.getString("versionDesc");
						mVersionCode = object.getString("versionCode");
						mDownloadURL = object.getString("downloadURL");
						mForceUpgrade = object.getBoolean("forceUpgrade");
						mSize = object.getString("size");

						if (getLocalVersionCode() < Integer.parseInt(mVersionCode)) {
//					Log.d(TAG, "onUpdateAvailable: 有更新版本：" + versionName);
							askToUpgrade();
						} else if (getLocalVersionCode() > Integer.parseInt(mVersionCode)) {
							Toast.makeText(x.app(), R.string.you_are_using_an_Alpha_Test_application, Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Log.w(TAG, "onSuccess");
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {
					Log.w(TAG, "run: JSON parser may occurred error or it's an IOException", ex);
					Toast.makeText(x.app(), R.string.server_error, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onCancelled(CancelledException cex) {

				}

				@Override
				public void onFinished() {

				}
			});
		} else {
//			Log.d(TAG, "checkUpdate: 网络未连接");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), R.string.not_connected_to_server, Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	private int getLocalVersionCode() {
		PackageManager packageManager = getPackageManager();
		PackageInfo packageInfo;
		try {
			packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
		return packageInfo.versionCode;
	}

	private void askToUpgrade() {
		if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
			ActivityCompat.requestPermissions(MainActivity.this, perms, 1);
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				boolean showUpgradeDialogOnlyUnderWifi = sharedPref.getBoolean("show_upgrade_dialog_only_under_wifi", true);

				final MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this);
				final MaterialDialog.Builder innerBuilder = new MaterialDialog.Builder(MainActivity.this);
				builder.iconRes(R.mipmap.warning);
				builder.content(getString(R.string.newer_version_detected) + mVersionName + "\n" + getString(R.string.size) + mSize + getString(R.string.newer_version_description) + "\n\n" + mVersionDesc)
						.positiveText(getString(R.string.我要升级))
						.onPositive(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
									Toast.makeText(x.app(), R.string.cannot_access_external_storage, Toast.LENGTH_LONG).show();
								} else {
									if (NetTypeUtils.isWifiActive(MainActivity.this)) {
										Log.d(TAG, "onClick: 连的是wifi");
										download();
									} else {
										innerBuilder.iconRes(R.mipmap.warning);
										innerBuilder.cancelable(false)
												.content(R.string.you_are_using_data_now)
												.title(R.string.Are_you_sure)
												.negativeText(R.string.I_am_broken)
												.onNegative(new MaterialDialog.SingleButtonCallback() {
													@Override
													public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
														Toast.makeText(x.app(), R.string.update_after_WiFied, Toast.LENGTH_LONG).show();
														dialog.dismiss();
														MainActivity.this.finish();
													}
												})
												.positiveText(R.string.I_am_rich)
												.onPositive(new MaterialDialog.SingleButtonCallback() {
													@Override
													public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
														dialog.dismiss();
														download();
													}
												}).show();
									}
								}
							}
						});
				if (mForceUpgrade) {
					builder.title(R.string.must_upgrade).cancelable(false);
					builder.show().setCanceledOnTouchOutside(false);
				} else {
					builder.title(R.string.有新版本啦).cancelable(true);
					builder.negativeText(R.string.不更新)
							.onNegative(new MaterialDialog.SingleButtonCallback() {
								@Override
								public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
									dialog.dismiss();
								}
							});
					if (!showUpgradeDialogOnlyUnderWifi) {
						builder.show().setCanceledOnTouchOutside(false);
					}
				}
			}
		});
	}

	private void download() {
		RequestParams params = new RequestParams(mDownloadURL);
//		Log.d(TAG, "download link: " + downloadURL);
		params.setAutoRename(true);
		params.setCacheSize(1024 * 1024 * 8);
		params.setCancelFast(true);

		params.setCacheDirName(Environment.getDownloadCacheDirectory().getPath());
		String newVersionFileName = mDownloadURL.substring(mDownloadURL.lastIndexOf("/") + 1, mDownloadURL.length());
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String customDownloadDirectory = sharedPref.getString("custom_download_directory", Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS);

		params.setSaveFilePath(customDownloadDirectory + "/" + newVersionFileName);
//		Log.d("TAG", "download: " + Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + newVersionFileName);
//			progressBar.setProgress(0);
		final ProgressDialog builder = new ProgressDialog(MainActivity.this, ProgressDialog.STYLE_SPINNER);
		if (mForceUpgrade) {
			builder.setCancelable(false);
			builder.setCanceledOnTouchOutside(false);
		}
//		Log.d(TAG, "download: params：" + params);

		final Callback.Cancelable cancelable = x.http().get(params, new Callback.ProgressCallback<File>() {

			@Override
			public void onWaiting() {
//				Toast.makeText(SplashActivity.this, "正在等待下载开始", Toast.LENGTH_SHORT).show();
//				Log.d(TAG, "onWaiting: 正在等待下载开始");
			}

			@Override
			public void onStarted() {
				Toast.makeText(x.app(), R.string.download_begins, Toast.LENGTH_SHORT).show();
//				Log.d(TAG, "onStarted: 下载开始");
				builder.setIcon(R.mipmap.downloading);
				builder.setTitle(getString(R.string.downloading));
				builder.setCancelable(true);
				builder.setCanceledOnTouchOutside(false);
				builder.show();
			}

			@Override
			public void onLoading(long total, long current, boolean isDownloading) {
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append(getString(R.string.please_wait_a_second)).append(DoubleUtil.formatDouble2(((double) current) / 1024 / 1024, RoundingMode.DOWN, 2)).append("/").append(DoubleUtil.formatDouble2(((double) total) / 1024 / 1024, RoundingMode.DOWN, 2)).append("M");
				builder.setMessage(stringBuffer);
//					progressBar.setMax((int) total);
//					progressBar.setProgress((int) current);
//                Toast.makeText(SplashActivity.this, "下载中。。。", Toast.LENGTH_SHORT).show();
//				Log.d(TAG, "onLoading: 下载中");
			}

			@Override
			public void onSuccess(File result) {
				builder.dismiss();
//				Toast.makeText(x.app(), "下载成功", Toast.LENGTH_SHORT).show();
//				Log.d(TAG, "onSuccess: The File is: " + result);
				installAPK(result);
				MainActivity.this.finish();
//				Log.d(TAG, "onSuccess: 下载成功");
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
//				ex.printStackTrace();
				Toast.makeText(x.app(), R.string.error_in_downloading, Toast.LENGTH_SHORT).show();
//				enterHome();
//				Log.d(TAG, "onError: 下载出错啦");
				new MaterialDialog.Builder(MainActivity.this)
						.title("下载遇到问题？")
						.content("注意：调用系统下载器，不能在状态栏暂停或取消下载，且没有“正在使用流量”的警告！")
						.positiveText("去浏览器下载")
						.onPositive(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								Intent intent = new Intent(ACTION_VIEW, Uri.parse(mDownloadURL));
								startActivity(intent);
							}
						})
						.neutralText("调用系统下载器")
						.onNeutral(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
								DownloadManager.Request request = new DownloadManager.Request(
										Uri.parse(mDownloadURL));
								request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
								long enqueue = downloadManager.enqueue(request);
								//TODO 下载逻辑

							}
						})
						.show();
			}

			@Override
			public void onCancelled(CancelledException cex) {
//				Log.d(TAG, "onCancelled: 下载已取消");
//				Toast.makeText(x.app(), "cancelled", Toast.LENGTH_SHORT).show();
//				enterLogin();
				builder.dismiss();
			}

			@Override
			public void onFinished() {
//				Toast.makeText(x.app(), "下载完成", Toast.LENGTH_SHORT).show();
//				Log.d(TAG, "onFinished: 下载完成");
				builder.dismiss();
			}
		});

		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				builder.dismiss();
				cancelable.cancel();
				Toast.makeText(MainActivity.this, R.string.download_canceled, Toast.LENGTH_LONG).show();
			}
		});
	}

	private void installAPK(File result) {
//		Log.i(TAG, "installAPK: 刚刚进入安装apk的方法");
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(result), "application/vnd.android.package-archive");
//		Log.i(TAG, "installAPK: 准备好了数据，马上开启下一个activity");
		startActivity(intent);
	}
}
