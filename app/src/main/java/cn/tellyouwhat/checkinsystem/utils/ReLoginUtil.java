package cn.tellyouwhat.checkinsystem.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;

import cn.tellyouwhat.checkinsystem.activities.LoginActivity;

/**
 * Created by Harbor-Laptop on 2017/4/6.
 * 此工具用来注销账户的登录状态
 */

public class ReLoginUtil {
	private Activity activity;
	private static ArrayList<AlertDialog> list = new ArrayList<>();

	public ReLoginUtil(Activity activity) {
		this.activity = activity;
	}

	public void reLoginWithAlertDialog() {
		Log.w("弹出了", "reLoginWithAlertDialog");
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		AlertDialog alertDialog = builder.setMessage("\n您的登录状态异常")
				.setCancelable(false)
				.setTitle("需要重新登陆")
				.setPositiveButton("重新登录", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						swipe_token();
						activity.startActivity(new Intent(activity, LoginActivity.class));
						stopService();
						activity.finish();
						dialog.dismiss();
					}
				})
				.show();
		alertDialog.setCanceledOnTouchOutside(false);
		list.add(alertDialog);
	}

	public void reLoginWithAreYouSureDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		AlertDialog alertDialog = builder.setMessage("\n确定注销当前账号？\n")
				.setCancelable(true)
				.setTitle("确定注销")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						swipe_token();
						activity.startActivity(new Intent(activity, LoginActivity.class));
						stopService();
						activity.finish();
						dialog.dismiss();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.show();
		alertDialog.setCanceledOnTouchOutside(false);
		list.add(alertDialog);
	}

	public void reLogin() {
		swipe_token();
		activity.startActivity(new Intent(activity, LoginActivity.class));
		activity.finish();
	}

	private void swipe_token() {
		SharedPreferences preferences = activity.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = preferences.edit();
		edit.putString(ConstantValues.TOKEN, "");
		edit.apply();
	}

	private void stopService() {
//		AbsWorkService.cancelJobAlarmSub();
	}

	//
	public static void removeAllDialog() {
		for (AlertDialog dialog :
				list) {
			if (dialog != null) {
				Log.d("清理了dialog", "removeAllDialog");
				dialog.dismiss();
			}
		}
		list.clear();
	}
}
