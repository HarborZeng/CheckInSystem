package cn.tellyouwhat.checkinsystem.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.jaeger.library.StatusBarUtil;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.io.Serializable;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.bean.Notice;

/**
 * Created by Harbor-Laptop on 2017/4/19.
 * 展示通知公告的activity
 */

public class BoardActivity extends BaseActivity implements ObservableScrollViewCallbacks {
	private static final String TAG = "BoardActivity";
	private View mHeaderView;
	private View mToolbarView;
	private ObservableScrollView mScrollView;
	private int mBaseTranslationY;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_board);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPreferences.getBoolean("immersed_status_bar_enabled", true)) {
			StatusBarUtil.setColor(BoardActivity.this, getResources().getColor(R.color.colorPrimary), 0);
		} else {
			StatusBarUtil.setColor(BoardActivity.this, getResources().getColor(R.color.colorPrimary));
		}
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_notice);
		toolbar.setTitle("通知");
		toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
		setSupportActionBar(toolbar);

		mHeaderView = findViewById(R.id.header);
		ViewCompat.setElevation(mHeaderView, 4f);
		mToolbarView = toolbar;

		mScrollView = (ObservableScrollView) findViewById(R.id.scroll_notice);
		mScrollView.setScrollViewCallbacks(this);

		TextView titleTextView = (TextView) findViewById(R.id.title_board);
		TextView contentBoardTextView = (TextView) findViewById(R.id.content_board);
		TextView authorTextView = (TextView) findViewById(R.id.author_board);
		TextView timeTextView = (TextView) findViewById(R.id.time_board);

		Intent intent = getIntent();
		Serializable notice = intent.getSerializableExtra("notice");
		String title = ((Notice) notice).getTitle();
		String content = ((Notice) notice).getContent();
		String author = ((Notice) notice).getAuthor();
		String time = ((Notice) notice).getTime();
		contentBoardTextView.setText(content);
		titleTextView.setText(title);
		authorTextView.setText(author);
		timeTextView.setText(time);
//		Log.d(TAG, "onCreate: 标题："+title+", 内容是："+content);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
		if (dragging) {
			int toolbarHeight = mToolbarView.getHeight();
			if (firstScroll) {
				float currentHeaderTranslationY = ViewHelper.getTranslationY(mHeaderView);
				if (-toolbarHeight < currentHeaderTranslationY) {
					mBaseTranslationY = scrollY;
				}
			}
			float headerTranslationY = ScrollUtils.getFloat(-(scrollY - mBaseTranslationY), -toolbarHeight, 0);
			ViewPropertyAnimator.animate(mHeaderView).cancel();
			ViewHelper.setTranslationY(mHeaderView, headerTranslationY);
		}
	}

	@Override
	public void onDownMotionEvent() {

	}

	@Override
	public void onUpOrCancelMotionEvent(ScrollState scrollState) {
		mBaseTranslationY = 0;
		if (scrollState == ScrollState.DOWN) {
			showToolbar();
		} else if (scrollState == ScrollState.UP) {
			int toolbarHeight = mToolbarView.getHeight();
			int scrollY = mScrollView.getCurrentScrollY();
			if (toolbarHeight <= scrollY) {
				hideToolbar();
			} else {
				showToolbar();
			}
		} else {
			// Even if onScrollChanged occurs without scrollY changing, toolbar should be adjusted
			if (!toolbarIsShown() && !toolbarIsHidden()) {
				// Toolbar is moving but doesn't know which to move:
				// you can change this to hideToolbar()
				showToolbar();
			}
		}
	}

	private boolean toolbarIsShown() {
		return ViewHelper.getTranslationY(mHeaderView) == 0;
	}

	private boolean toolbarIsHidden() {
		return ViewHelper.getTranslationY(mHeaderView) == -mToolbarView.getHeight();
	}

	private void showToolbar() {
		float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
		if (headerTranslationY != 0) {
			ViewPropertyAnimator.animate(mHeaderView).cancel();
			ViewPropertyAnimator.animate(mHeaderView).translationY(0).setDuration(200).start();
		}
	}

	private void hideToolbar() {
		float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
		int toolbarHeight = mToolbarView.getHeight();
		if (headerTranslationY != -toolbarHeight) {
			ViewPropertyAnimator.animate(mHeaderView).cancel();
			ViewPropertyAnimator.animate(mHeaderView).translationY(-toolbarHeight).setDuration(200).start();
		}
	}
}
