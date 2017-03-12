package cn.tellyouwhat.checkinsystem.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.math.RoundingMode;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.utils.DoubleUtil;
import cn.tellyouwhat.checkinsystem.utils.NetTypeUtils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SplashActivity extends BaseActivity {

	private static final String TAG = "SplashActivity";
	private PackageInfo packageInfo;
	private long startTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
			window.setNavigationBarColor(Color.TRANSPARENT);
		}
		setContentView(R.layout.activity_splash);

		//把这一句话注释掉之后，API21一下就不会报错
//		x.view().inject(this);


		checkUpdate();
		initShortCut();
	}

	private void initShortCut() {
		SharedPreferences setting = getSharedPreferences("silent.preferences", 0);
// 判断是否第一次启动应用程序（默认为true）
		boolean firstStart = setting.getBoolean("FIRST_START", true);
// 第一次启动时创建桌面快捷方式
		if (firstStart) {
			Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
// 快捷方式的名称
			shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
// 不允许重复创建
			shortcut.putExtra("duplicate", false);
// 指定快捷方式的启动对象
			ComponentName comp = new ComponentName(this.getPackageName(), "." + this.getLocalClassName());
			shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN).setComponent(comp));
// 快捷方式的图标
			Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher);
			shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
// 发出广播
			sendBroadcast(shortcut);
// 将第一次启动的标识设置为false
			SharedPreferences.Editor editor = setting.edit();
			editor.putBoolean("FIRST_START", false);
// 提交设置
			editor.apply();
			Toast.makeText(this, R.string.shortcut_created, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * check if there is a upgrade exiting
	 */
	private void checkUpdate() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()) {
//			Log.d(TAG, "checkUpdate: 网络正常");
			startTime = SystemClock.elapsedRealtime();

			RequestParams params = new RequestParams("http://tellyouwhat.cn/update/update.json");
			params.setAsJsonContent(true);
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
						long endTime = SystemClock.elapsedRealtime();
						long duration = 900 - (endTime - startTime);
						if (duration > 0) {
							SystemClock.sleep(duration);
						}
						if (getLocalVersionCode() < Integer.parseInt(versionCode)) {
//					Log.d(TAG, "onUpdateAvailable: 有更新版本：" + versionName);
							askToUpgrade(versionName, versionDesc, versionCode, downloadURL, size, forceUpgrade);
						} else if (getLocalVersionCode() == Integer.parseInt(versionCode)) {
//					Log.d(TAG, "onUpdateAvailable: 没有更新版本");
							enterLogin();
						} else {
							Toast.makeText(SplashActivity.this, R.string.you_are_using_an_Alpha_Test_application, Toast.LENGTH_LONG).show();
							enterLogin();
						}
					} catch (JSONException e) {
						e.printStackTrace();
						enterLogin();
					}
//					Log.w(TAG, "onSuccess: "+ s);
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {
					Log.w(TAG, "run: JSON parser may occurred error or it's an IOException", ex);
					Toast.makeText(SplashActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
					enterLogin();
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
			enterLogin();
			Toast.makeText(SplashActivity.this, "您的设备未连接至网络", Toast.LENGTH_LONG).show();
		}
	}

	private int getLocalVersionCode() {
		PackageManager packageManager = getPackageManager();
		try {
			packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
		return packageInfo.versionCode;
	}


	private void askToUpgrade(final String versionName, final String versionDesc, String versionCode, final String downloadURL, final String size, final boolean forceUpgrade) {
		if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
			ActivityCompat.requestPermissions(SplashActivity.this, perms, 1);
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
				final AlertDialog.Builder innerBuilder = new AlertDialog.Builder(SplashActivity.this);

				builder.setIcon(R.mipmap.warning);
				if (forceUpgrade) {
					builder.setTitle("爸爸，此版本包含重大更新，必须要升级！").setCancelable(false);
				} else {
					builder.setTitle(R.string.有新版本啦).setCancelable(true);

					builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							enterLogin();
						}
					});
				}
				builder.setMessage(getString(R.string.newer_version_detected) + versionName + "\n" + getString(R.string.size) + size + getString(R.string.newer_version_description) + "\n\n" + versionDesc)
						.setPositiveButton(getString(R.string.我要升级), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
									Toast.makeText(SplashActivity.this, R.string.cannot_access_external_storage, Toast.LENGTH_LONG).show();
								} else {

									if (NetTypeUtils.isWifiActive(SplashActivity.this)) {
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
														Toast.makeText(SplashActivity.this, R.string.update_after_WiFied, Toast.LENGTH_LONG).show();
														dialog.dismiss();
														SplashActivity.this.finish();
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
				if (!forceUpgrade) {
					builder.setNegativeButton(R.string.不更新, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							enterLogin();
						}
					}).show();
				} else {
					builder.show();
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
		final ProgressDialog builder = new ProgressDialog(SplashActivity.this, ProgressDialog.STYLE_SPINNER);
//		Log.d(TAG, "download: params：" + params);

		final Callback.Cancelable cancelable = x.http().get(params, new Callback.ProgressCallback<File>() {

			@Override
			public void onWaiting() {
//				Toast.makeText(SplashActivity.this, "正在等待下载开始", Toast.LENGTH_SHORT).show();
//				Log.d(TAG, "onWaiting: 正在等待下载开始");
			}

			@Override
			public void onStarted() {
				Toast.makeText(SplashActivity.this, R.string.download_begins, Toast.LENGTH_SHORT).show();
//				Log.d(TAG, "onStarted: 下载开始");
				builder.setIcon(R.mipmap.downloading);
				builder.setTitle(getString(R.string.downloading));
				builder.setCancelable(true);
				builder.setCanceledOnTouchOutside(false);
				builder.show();
			}

			@Override
			public void onLoading(long total, long current, boolean isDownloading) {

				builder.setMessage(getString(R.string.please_wait_a_second) + DoubleUtil.formatDouble2(((double) current) / 1024 / 1024, RoundingMode.DOWN, 2) + "/" + DoubleUtil.formatDouble2(((double) total) / 1024 / 1024, RoundingMode.DOWN, 2) + "M");


//					progressBar.setMax((int) total);
//					progressBar.setProgress((int) current);
//                Toast.makeText(SplashActivity.this, "下载中。。。", Toast.LENGTH_SHORT).show();
//				Log.d(TAG, "onLoading: 下载中");
			}

			@Override
			public void onSuccess(File result) {
				builder.dismiss();
//				Toast.makeText(x.app(), "下载成功", Toast.LENGTH_SHORT).show();
//				Log.d(TAG, "onSuccess: The File is: "+result);
				installAPK(result);
				finish();
//				Log.d(TAG, "onSuccess: 下载成功");
			}


			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
//				ex.printStackTrace();
				Toast.makeText(SplashActivity.this, R.string.error_in_downloading, Toast.LENGTH_SHORT).show();
//				enterHome();
//				enterLogin();
//				Log.d(TAG, "onError: 下载出错啦");
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
				Snackbar.make(findViewById(R.id.first_screen_image), getString(R.string.cancel_updating), Snackbar.LENGTH_SHORT).show();
				dialog.dismiss();
				builder.dismiss();
				cancelable.cancel();
				Toast.makeText(SplashActivity.this, R.string.download_canceled, Toast.LENGTH_LONG).show();
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
		SplashActivity.this.startActivity(intent);
	}

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		enterLogin();
//		super.onActivityResult(requestCode, resultCode, data);
//	}

	/**
	 * After checking, if there is no newer version exiting, enter the {@link MainActivity} directly.
	 */
	private void enterLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

}
