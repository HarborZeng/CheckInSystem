package cn.tellyouwhat.checkinsystem.fragments;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;

import cn.tellyouwhat.checkinsystem.R;

/**
 * Created by Harbor-Laptop on 2017/3/29.
 */

public class IntroThree extends Fragment implements ISlideBackgroundColorHolder {

	private View mRootView;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_three, container, false);

		return mRootView;
	}

	public static IntroThree newInstance() {
		IntroThree fragment = new IntroThree();
		fragment.setRetainInstance(true);
		return fragment;
	}

	@Override
	public int getDefaultBackgroundColor() {
		return ContextCompat.getColor(getContext(), R.color.intro_fragment_three);
	}

	@Override
	public void setBackgroundColor(@ColorInt int backgroundColor) {
		if (mRootView == null) {
			return;
		}
		mRootView.setBackgroundColor(backgroundColor);
	}
}
