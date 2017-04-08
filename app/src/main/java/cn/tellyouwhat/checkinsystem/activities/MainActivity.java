package cn.tellyouwhat.checkinsystem.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;

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
import cn.tellyouwhat.checkinsystem.services.LocationGettingService;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.DoubleUtil;
import cn.tellyouwhat.checkinsystem.utils.EncryptUtil;
import cn.tellyouwhat.checkinsystem.utils.NetTypeUtils;
import cn.tellyouwhat.checkinsystem.utils.ReLoginUtil;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends BaseActivity {

	private String TAG = "MainActivity";
	private long time = 0;

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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBackEnable(false);
		setContentView(R.layout.activity_main);
		StatusBarUtil.setColor(this, Color.parseColor("#2D0081"), 0);
		updateSession();
		//解决Fragment可能出现的重叠问题
		if (savedInstanceState == null) {
			// 正常情况下去 加载根Fragment
			ActionBar supportActionBar = getSupportActionBar();
			if (supportActionBar != null) {
				supportActionBar.setDisplayHomeAsUpEnabled(false);
				supportActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2D0081")));
			}
			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.content, CheckInFragment.newInstance(), "CheckIn")
					.commit();
		}

		//开启获取位置的后台服务
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean backGroundServiceEnabled = sharedPref.getBoolean("use_background_service", true);
		if (backGroundServiceEnabled) {
			Intent intent = new Intent(getApplicationContext(), LocationGettingService.class);
			startService(intent);
		}

		BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

		new Thread(new Runnable() {
			@Override
			public void run() {
				checkUpdate();
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}


	@Override
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
	}

	public void checkUpdate() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()) {
//			Log.d(TAG, "checkUpdate: 网络正常");

			RequestParams params = new RequestParams("http://update.checkin.tellyouwhat.cn/update.json");
			x.http().get(params, new Callback.CommonCallback<JSONObject>() {
				@Override
				public void onSuccess(JSONObject object) {
					try {
						String versionName = object.getString("versionName");
						String versionDesc = object.getString("versionDesc");
						String versionCode = object.getString("versionCode");
						String downloadURL = object.getString("downloadURL");
						boolean forceUpgrade = object.getBoolean("forceUpgrade");
						String size = object.getString("size");

						if (getLocalVersionCode() < Integer.parseInt(versionCode)) {
//					Log.d(TAG, "onUpdateAvailable: 有更新版本：" + versionName);
							askToUpgrade(versionName, versionDesc, versionCode, downloadURL, size, forceUpgrade);
						} else if (getLocalVersionCode() > Integer.parseInt(versionCode)) {
							Toast.makeText(MainActivity.this, R.string.you_are_using_an_Alpha_Test_application, Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Log.w(TAG, "onSuccess");
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {
					Log.w(TAG, "run: JSON parser may occurred error or it's an IOException", ex);
					Toast.makeText(MainActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
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
					Toast.makeText(MainActivity.this, R.string.not_connected_to_server, Toast.LENGTH_LONG).show();
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

	private void askToUpgrade(final String versionName, final String versionDesc, String versionCode, final String downloadURL, final String size, final boolean forceUpgrade) {
		if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
			ActivityCompat.requestPermissions(MainActivity.this, perms, 1);
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				boolean showUpgradeDialogOnlyUnderWifi = sharedPref.getBoolean("show_upgrade_dialog_only_under_wifi", true);

				final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				final AlertDialog.Builder innerBuilder = new AlertDialog.Builder(MainActivity.this);
				builder.setIcon(R.mipmap.warning);
				builder.setMessage(getString(R.string.newer_version_detected) + versionName + "\n" + getString(R.string.size) + size + getString(R.string.newer_version_description) + "\n\n" + versionDesc)
						.setPositiveButton(getString(R.string.我要升级), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
									Toast.makeText(MainActivity.this, R.string.cannot_access_external_storage, Toast.LENGTH_LONG).show();
								} else {

									if (NetTypeUtils.isWifiActive(MainActivity.this)) {
										Log.d(TAG, "onClick: 连的是wifi");
										download(downloadURL);
									} else {
										innerBuilder.setIcon(R.mipmap.warning);
										innerBuilder.setCancelable(false)
												.setMessage(R.string.you_are_using_data_now)
												.setTitle(R.string.Are_you_sure)
												.setNegativeButton(R.string.I_am_broken, new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														Toast.makeText(MainActivity.this, R.string.update_after_WiFied, Toast.LENGTH_LONG).show();
														dialog.dismiss();
														MainActivity.this.finish();
													}
												})
												.setPositiveButton(R.string.I_am_rich, new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														dialog.dismiss();
														download(downloadURL);

													}
												}).show();
									}
								}
							}
						});
				if (forceUpgrade) {
					builder.setTitle(R.string.must_upgrade).setCancelable(false);
					builder.show().setCanceledOnTouchOutside(false);
				} else {
					builder.setTitle(R.string.有新版本啦).setCancelable(true);
					builder.setNegativeButton(R.string.不更新, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
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

	private void download(String downloadURL) {
		RequestParams params = new RequestParams(downloadURL);
//		Log.d(TAG, "download link: " + downloadURL);
		params.setAutoRename(true);
		params.setCacheSize(1024 * 1024 * 8);
		params.setConnectTimeout(3000);
		params.setCancelFast(true);

		params.setCacheDirName(Environment.getDownloadCacheDirectory().getPath());
		String newVersionFileName = downloadURL.substring(downloadURL.lastIndexOf("/") + 1, downloadURL.length());
		params.setSaveFilePath(Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + newVersionFileName);
//		Log.d("TAG", "download: " + Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + newVersionFileName);
//			progressBar.setProgress(0);
		final ProgressDialog builder = new ProgressDialog(MainActivity.this, ProgressDialog.STYLE_SPINNER);
//		Log.d(TAG, "download: params：" + params);

		final Callback.Cancelable cancelable = x.http().get(params, new Callback.ProgressCallback<File>() {

			@Override
			public void onWaiting() {
//				Toast.makeText(SplashActivity.this, "正在等待下载开始", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "onWaiting: 正在等待下载开始");
			}

			@Override
			public void onStarted() {
				Toast.makeText(MainActivity.this, R.string.download_begins, Toast.LENGTH_SHORT).show();
				Log.d(TAG, "onStarted: 下载开始");
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
				Log.d(TAG, "onLoading: 下载中");
			}

			@Override
			public void onSuccess(File result) {
				builder.dismiss();
//				Toast.makeText(x.app(), "下载成功", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "onSuccess: The File is: " + result);
				installAPK(result);
				MainActivity.this.finish();
				Log.d(TAG, "onSuccess: 下载成功");
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
//				ex.printStackTrace();
				Toast.makeText(MainActivity.this, R.string.error_in_downloading, Toast.LENGTH_SHORT).show();
//				enterHome();
				Log.d(TAG, "onError: 下载出错啦");
			}

			@Override
			public void onCancelled(CancelledException cex) {
				Log.d(TAG, "onCancelled: 下载已取消");
//				Toast.makeText(x.app(), "cancelled", Toast.LENGTH_SHORT).show();
//				enterLogin();
				builder.dismiss();
			}

			@Override
			public void onFinished() {
//				Toast.makeText(x.app(), "下载完成", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "onFinished: 下载完成");
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
		Log.i(TAG, "installAPK: 刚刚进入安装apk的方法");
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(result), "application/vnd.android.package-archive");
		Log.i(TAG, "installAPK: 准备好了数据，马上开启下一个activity");
		startActivity(intent);
	}

	private void updateSession() {
		SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
		String userName = sharedPreferences.getString("USER_NAME", "");
		String encryptedToken = sharedPreferences.getString(ConstantValues.TOKEN, "");
		String token = EncryptUtil.decryptBase64withSalt(encryptedToken, ConstantValues.SALT);
		RequestParams p = new RequestParams("http://api.checkin.tellyouwhat.cn/User/UpdateSession?username=" + userName + "&deviceid=" + Build.SERIAL + "&token=" + token);
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
						Log.i(TAG, "onSuccess: session 已经更新");
						break;
					case 0:
						ReLoginUtil reLoginUtil = new ReLoginUtil(MainActivity.this);
						try {
							Toast.makeText(MainActivity.this, result.getString("message"), Toast.LENGTH_SHORT).show();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						reLoginUtil.reLoginWithAlertDialog();
						break;
					case -1:
						Toast.makeText(MainActivity.this, "发生了不可描述的错误009", Toast.LENGTH_SHORT).show();
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
