package cn.tellyouwhat.checkinsystem.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;

import cn.tellyouwhat.checkinsystem.R;

/**
 * Created by Harbor-Laptop on 2017/3/29.
 * 第一个介绍界面
 */

public class IntroOne extends Fragment implements ISlideBackgroundColorHolder {

	private View intro1;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_one, container, false);
		intro1 = view.findViewById(R.id.relative_layout_intro1);
		return view;
	}

	public static IntroOne newInstance() {
		IntroOne fragment = new IntroOne();
		fragment.setRetainInstance(true);
		return fragment;
	}

	@Override
	public int getDefaultBackgroundColor() {
		return Color.parseColor("#0277BD");
	}

	@Override
	public void setBackgroundColor(@ColorInt int backgroundColor) {
		if (intro1 != null) {
			Log.d("IntroOne", "setBackgroundColor: intro1不为空");
			intro1.setBackgroundColor(backgroundColor);
		}
	}
}
