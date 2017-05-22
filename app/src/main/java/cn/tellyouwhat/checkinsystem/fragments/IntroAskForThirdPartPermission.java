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
import com.github.paolorotolo.appintro.ISlidePolicy;

import cn.tellyouwhat.checkinsystem.R;

import static com.xdandroid.hellodaemon.IntentWrapper.whiteListMatters;

/**
 * Created by Harbor-Laptop on 2017/3/31.
 */

public class IntroAskForThirdPartPermission extends Fragment implements ISlidePolicy, ISlideBackgroundColorHolder {
	private boolean mButtonClicked = false;
	private View mRootView;

	public static IntroAskForThirdPartPermission newInstance() {
		IntroAskForThirdPartPermission fragment = new IntroAskForThirdPartPermission();
		fragment.setRetainInstance(true);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_ask_for_third_part_permission, container, false);
		return mRootView;
	}

	@Override
	public boolean isPolicyRespected() {
		return mButtonClicked;
	}

	@Override
	public void onUserIllegallyRequestedNextPage() {
		whiteListMatters(getActivity(), "需要权限才能正常运行");
		mButtonClicked = true;
	}

	@Override
	public int getDefaultBackgroundColor() {
		return ContextCompat.getColor(getContext(), R.color.intro_fragment_privilege);
	}

	@Override
	public void setBackgroundColor(@ColorInt int backgroundColor) {
		if (mRootView == null) {
			return;
		}
		mRootView.setBackgroundColor(backgroundColor);
	}
}
