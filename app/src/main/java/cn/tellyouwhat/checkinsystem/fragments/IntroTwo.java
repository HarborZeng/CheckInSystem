package cn.tellyouwhat.checkinsystem.fragments;

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
 */

public class IntroTwo extends Fragment implements ISlideBackgroundColorHolder {

	private View intro2;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_two, container, false);
		intro2 = view.findViewById(R.id.relative_layout_intro2);
		return view;
	}

	public static IntroTwo newInstance() {
		IntroTwo fragment = new IntroTwo();
		fragment.setRetainInstance(true);
		return fragment;
	}

	@Override
	public int getDefaultBackgroundColor() {
		return getResources().getColor(R.color.theme_yellow_primary_dark);
	}

	@Override
	public void setBackgroundColor(@ColorInt int backgroundColor) {

		if (intro2 != null) {
			Log.d("IntroTwo", "setBackgroundColor: intro2不为空");
			intro2.setBackgroundColor(backgroundColor);
		}
	}
}
