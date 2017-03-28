package cn.tellyouwhat.checkinsystem.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.FlymeUtil;
import cn.tellyouwhat.checkinsystem.utils.MIUIUtil;
import cn.tellyouwhat.checkinsystem.utils.SPUtil;

public class SplashActivity extends BaseActivity {

	private static final String TAG = "SplashActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);

		//把这一句话注释掉之后，API 21以下就不会报错
//		x.view().inject(this);
		askForPermission();
	}

	/*
	  以下逻辑用于判断是否曾经成功登录过app
	  以实现热启动效果
	 */
	private void enter() {
		SharedPreferences SharedPreferences = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		String token = SharedPreferences.getString(ConstantValues.TOKEN, "");
		if (TextUtils.isEmpty(token)) {
			enterLogin();
		} else {
			enterMain();
		}
	}

	private void askForPermission() {
		Log.i(TAG, "askForPermission: 获取中ing");
		if (
				(PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
						||
						(PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE))
						||
						(PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION))
				) {
			Log.i(TAG, "askForPermission: 没权限");
			String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_PHONE_STATE", "android.permission.ACCESS_FINE_LOCATION"};
			ActivityCompat.requestPermissions(this, perms, 1);
		} else {
			initShortCutAndThirdPartPermission();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		Boolean canEnter[] = new Boolean[grantResults.length];
		if (requestCode == 1) {
			for (int i = 0; i < grantResults.length; i++) {
				if (grantResults[i] == -1) {
					canEnter[i] = false;
//					Toast.makeText(this, "您必须要授予权限才能继续", Toast.LENGTH_LONG).show();
					finish();
				} else {
					canEnter[i] = true;
				}
			}
			if (canEnter[0] && canEnter[1] && canEnter[2]) {
				initShortCutAndThirdPartPermission();
			} else {
				Toast.makeText(this, "您必须要授予权限才能继续", Toast.LENGTH_LONG).show();
			}
		}
	}

	private void enterMain() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}


	private void initShortCutAndThirdPartPermission() {
		SPUtil spUtil = new SPUtil(this);
// 判断是否第一次启动应用程序（默认为true）
		boolean firstStart = spUtil.getBoolean(ConstantValues.FIRST_TIME_RUN, true);
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
			spUtil.putBoolean(ConstantValues.FIRST_TIME_RUN, false);
// 提交设置
			Toast.makeText(this, R.string.shortcut_created, Toast.LENGTH_SHORT).show();

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(false);
			final Intent[] intent = {new Intent()};
			intent[0].addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (MIUIUtil.isMIUI()) {
				intent[0].setAction("miui.intent.action.OP_AUTO_START");
				builder.setTitle("检测到您使用的是小米系统");
				builder.setMessage("请前往自启动管理界面打开这个app的自启动权限，以免后台定位失效导致漏签\n本程序后台服务耗电极少，请放心使用")
						.setPositiveButton("设置", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									startActivity(intent[0]);
								} catch (Exception e) {//抛出异常就直接打开设置页面
									intent[0] = new Intent(Settings.ACTION_SETTINGS);
									startActivity(intent[0]);
								}
								dialog.dismiss();
							}
						})
						.show().setCanceledOnTouchOutside(false);
			} else if (FlymeUtil.isFlyme()) {
				//TODO 有了手机在做测试
				intent[0].setAction("");
				builder.setTitle("检测到您使用的是魅族系统");
			} else {
				enter();
			}

		} else {
			enter();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		enter();
	}

	/**
	 * After checking, if there is no newer version exiting, enter the {@link MainActivity} directly.
	 */
	private void enterLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

}
