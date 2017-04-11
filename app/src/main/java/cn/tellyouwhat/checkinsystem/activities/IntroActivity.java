package cn.tellyouwhat.checkinsystem.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.jaeger.library.StatusBarUtil;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.fragments.IntroAskForThirdPartPermission;
import cn.tellyouwhat.checkinsystem.fragments.IntroOne;
import cn.tellyouwhat.checkinsystem.fragments.IntroThree;
import cn.tellyouwhat.checkinsystem.fragments.IntroTwo;

public class IntroActivity extends AppIntro2 {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StatusBarUtil.setColor(this, Color.parseColor("#0277BD"), 0);

		addSlide(IntroOne.newInstance());
		addSlide(IntroTwo.newInstance());
		addSlide(IntroThree.newInstance());
		addSlide(IntroAskForThirdPartPermission.newInstance());
		addSlide(AppIntroFragment.newInstance("", "内容", R.drawable.funny, Color.parseColor("#124874")));
//		setFadeAnimation();
//		setDepthAnimation();
//		setFlowAnimation();
//		setZoomAnimation();
//		setSlideOverAnimation();
		showSkipButton(false);
	}

	@Override
	public void onSkipPressed(Fragment currentFragment) {
		super.onSkipPressed(currentFragment);
		// Do something when users tap on Skip button.
		enterMain();
	}

	@Override
	public void onDonePressed(Fragment currentFragment) {
		super.onDonePressed(currentFragment);
		// Do something when users tap on Done button.
		enterMain();
	}

	@Override
	public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
		super.onSlideChanged(oldFragment, newFragment);
		// Do something when the slide changes.
	}

	private void enterMain() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}
