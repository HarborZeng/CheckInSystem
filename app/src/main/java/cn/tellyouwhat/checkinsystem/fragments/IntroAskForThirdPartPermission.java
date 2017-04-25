package cn.tellyouwhat.checkinsystem.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.github.paolorotolo.appintro.ISlidePolicy;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.utils.FlymeUtil;
import cn.tellyouwhat.checkinsystem.utils.MIUIUtil;

/**
 * Created by Harbor-Laptop on 2017/3/31.
 */

public class IntroAskForThirdPartPermission extends Fragment implements ISlidePolicy, ISlideBackgroundColorHolder {
	private boolean buttonClicked = false;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_ask_for_third_part_permission, container, false);

		return view;
	}

	@Override
	public boolean isPolicyRespected() {
		return buttonClicked;
	}

	@Override
	public void onUserIllegallyRequestedNextPage() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (MIUIUtil.isMIUI()) {
			intent.setAction(MIUIUtil.MIUI_AUTO_START_MANAGEMENT);
			try {
				startActivity(intent);
				buttonClicked = true;
			} catch (Exception e) {//抛出异常就直接打开设置页面
				intent = new Intent(Settings.ACTION_SETTINGS);
				buttonClicked = true;
				startActivity(intent);
			}
		} else if (FlymeUtil.isFlyme()) {
			//TODO 有了手机在做测试
			intent.setAction("");
		} else {
			buttonClicked = true;
			Toast.makeText(getActivity(), "您的手机不需要配置启动管理", Toast.LENGTH_SHORT).show();
		}
	}

	public static IntroAskForThirdPartPermission newInstance() {
		IntroAskForThirdPartPermission fragment = new IntroAskForThirdPartPermission();
		fragment.setRetainInstance(true);
		return fragment;
	}

	@Override
	public int getDefaultBackgroundColor() {
		return 0;
	}

	@Override
	public void setBackgroundColor(@ColorInt int backgroundColor) {

	}
}
