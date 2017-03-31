package cn.tellyouwhat.checkinsystem.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.github.paolorotolo.appintro.AppIntro;

import cn.tellyouwhat.checkinsystem.fragments.GetCheckCodeFragment;
import cn.tellyouwhat.checkinsystem.fragments.GetSMSCodeFragment;

/**
 * Created by Harbor-Laptop on 2017/3/1.
 *
 * @author HarborZeng
 */

public class ResetPasswordActivity extends AppIntro {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		addSlide(new GetCheckCodeFragment());
		addSlide(new GetSMSCodeFragment());

		showSkipButton(false);
		setSwipeLock(true);
		setColorDoneText(Color.parseColor("#000000"));
		setIndicatorColor(Color.parseColor("#000000"), Color.parseColor("#999999"));
		setNextArrowColor(Color.parseColor("#222222"));

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onSkipPressed(Fragment currentFragment) {
		super.onSkipPressed(currentFragment);
		// Do something when users tap on Skip button.
	}

	@Override
	public void onDonePressed(Fragment currentFragment) {
		super.onDonePressed(currentFragment);
		// Do something when users tap on Done button.
	}

	@Override
	public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
		super.onSlideChanged(oldFragment, newFragment);
		// Do something when the slide changes.
	}
}
