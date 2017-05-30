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

public class IntroAskForThirdPartPermission extends Fragment {
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

}
