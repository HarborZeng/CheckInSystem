package cn.tellyouwhat.checkinsystem.activities;

import android.Manifest;
import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.jaeger.library.StatusBarUtil;

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
import de.psdev.licensesdialog.LicensesDialog;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Harbor-Laptop on 2017/4/4.
 * About Activity
 */

public class AboutActivity extends BaseActivity {
	private static final String TAG = "AboutActivity";

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPreferences.getBoolean("immersed_status_bar_enabled", true)) {
			StatusBarUtil.setColor(AboutActivity.this, getResources().getColor(R.color.colorPrimary), 0);
		}

		ObservableListView listView = (ObservableListView) findViewById(R.id.list);

		listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{"主要开发者", "合作开发者", "艺术设计", "联系我们", "给个赞", "使用的库文件", "版本更新", "功能介绍"}));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:
						Snackbar.make(view, "HarborZeng", Snackbar.LENGTH_LONG).show();
						break;
					case 1:
						Snackbar.make(view, "lsj", Snackbar.LENGTH_LONG).show();
						break;
					case 2:
						Snackbar.make(view, "Kekeemay、杜皓璠", Snackbar.LENGTH_LONG).show();
						break;
					case 3:
						Snackbar.make(view, "QQ757772438\nWeChat: xiaoyao1682c", Snackbar.LENGTH_LONG).show();
						break;
					case 4:
						Uri uri = Uri.parse("market://details?id=" + getPackageName());
						Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
						try {
							startActivity(goToMarket);
						} catch (ActivityNotFoundException e) {
							e.printStackTrace();
							Toast.makeText(AboutActivity.this, "您的手机并没有任何应用市场", Toast.LENGTH_LONG).show();
						}
						break;
					case 5:
						new LicensesDialog.Builder(AboutActivity.this)
								.setNotices(R.raw.notices)
								.build()
								.showAppCompat();
						break;
					case 6:
						checkUpdate();
						break;
					case 7:
						startActivity(new Intent(AboutActivity.this, IntroActivity.class));
						break;
					default:
						Snackbar.make(view, "用户不可能看到这个, 否则开发者压实", Snackbar.LENGTH_LONG).show();
						break;
				}
			}
		});
		ActionBar supportActionBar = getSupportActionBar();
		if (supportActionBar != null) {
			supportActionBar.setDisplayHomeAsUpEnabled(true);
		}

		final View descriptionTextView = findViewById(R.id.text_view_description);
		descriptionTextView.setVisibility(View.INVISIBLE);
		TextView versionTextView = (TextView) findViewById(R.id.text_view_version);
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), MODE_PRIVATE);
			versionTextView.setText(packageInfo.versionName + " (" + packageInfo.versionCode + ")");
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		ImageView iconImageView = (ImageView) findViewById(R.id.image_view_icon);
		YoYo.with(Techniques.Shake)
				.duration(700)
				.repeat(1)
				.delay(200)
				.playOn(iconImageView);
		iconImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				YoYo.with(Techniques.ZoomIn)
						.duration(250)
						.onEnd(new YoYo.AnimatorCallback() {
							@Override
							public void call(Animator animator) {
								descriptionTextView.setVisibility(View.VISIBLE);
								YoYo.with(Techniques.DropOut)
										.duration(600)
										.repeat(1)
										.playOn(descriptionTextView);
							}
						})
						.repeat(1)
						.playOn(v);
			}
		});

		Button feedbackButton = (Button) findViewById(R.id.button_feedback);
		feedbackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(AboutActivity.this, FeedBackActivity.class));
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return super.onOptionsItemSelected(item);
	}

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
							Toast.makeText(AboutActivity.this, R.string.you_are_using_an_Alpha_Test_application, Toast.LENGTH_LONG).show();
						} else {
							Snackbar.make(findViewById(R.id.relative_layout_about_activity), "您当前安装的就是最新版本啦~", Snackbar.LENGTH_LONG).show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
						Snackbar.make(findViewById(R.id.relative_layout_about_activity), "服务器又开小差啦~对不起", Snackbar.LENGTH_LONG).show();
					}
					Log.w(TAG, "onSuccess");
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {
					Log.w(TAG, "run: JSON parser may occurred error or it's an IOException", ex);
					Toast.makeText(AboutActivity.this, R.string.server_error, Toast.LENGTH_SHORT).show();
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
					Toast.makeText(AboutActivity.this, R.string.not_connected_to_server, Toast.LENGTH_LONG).show();
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
		if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(AboutActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
			ActivityCompat.requestPermissions(AboutActivity.this, perms, 1);
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
				final AlertDialog.Builder innerBuilder = new AlertDialog.Builder(AboutActivity.this);

				builder.setIcon(R.mipmap.warning);
				if (forceUpgrade) {
					builder.setTitle(R.string.must_upgrade).setCancelable(false);
				} else {
					builder.setTitle(R.string.有新版本啦).setCancelable(true);
				}
				builder.setMessage(getString(R.string.newer_version_detected) + versionName + "\n" + getString(R.string.size) + size + getString(R.string.newer_version_description) + "\n\n" + versionDesc)
						.setPositiveButton(getString(R.string.我要升级), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
									Toast.makeText(AboutActivity.this, R.string.cannot_access_external_storage, Toast.LENGTH_LONG).show();
								} else {

									if (NetTypeUtils.isWifiActive(AboutActivity.this)) {
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
														Toast.makeText(AboutActivity.this, R.string.update_after_WiFied, Toast.LENGTH_LONG).show();
														dialog.dismiss();
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
							dialog.dismiss();
						}
					}).show().setCanceledOnTouchOutside(false);
				} else {
					builder.show().setCanceledOnTouchOutside(false);
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

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String customDownloadDirectory = sharedPref.getString("custom_download_directory", Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS);

		params.setSaveFilePath(customDownloadDirectory + "/" + newVersionFileName);
//		Log.d("TAG", "download: " + Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + newVersionFileName);
//			progressBar.setProgress(0);
		final ProgressDialog builder = new ProgressDialog(AboutActivity.this, ProgressDialog.STYLE_SPINNER);
//		Log.d(TAG, "download: params：" + params);

		final Callback.Cancelable cancelable = x.http().get(params, new Callback.ProgressCallback<File>() {

			@Override
			public void onWaiting() {
//				Toast.makeText(SplashActivity.this, "正在等待下载开始", Toast.LENGTH_SHORT).show();
//				Log.d(TAG, "onWaiting: 正在等待下载开始");
			}

			@Override
			public void onStarted() {
				Toast.makeText(AboutActivity.this, R.string.download_begins, Toast.LENGTH_SHORT).show();
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
				Log.d(TAG, "onSuccess: The File is: " + result);
				installAPK(result);
				AboutActivity.this.finish();
//				Log.d(TAG, "onSuccess: 下载成功");
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
//				ex.printStackTrace();
				Toast.makeText(AboutActivity.this, R.string.error_in_downloading, Toast.LENGTH_SHORT).show();
//				enterHome();
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
				dialog.dismiss();
				builder.dismiss();
				cancelable.cancel();
				Toast.makeText(AboutActivity.this, R.string.download_canceled, Toast.LENGTH_LONG).show();
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
