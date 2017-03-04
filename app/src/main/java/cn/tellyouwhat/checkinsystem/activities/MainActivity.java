package cn.tellyouwhat.checkinsystem.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.fragments.CheckInFragment;
import cn.tellyouwhat.checkinsystem.fragments.HistoryFragment;
import cn.tellyouwhat.checkinsystem.fragments.MeFragment;

public class MainActivity extends BaseActivity {

	private long time = 0;

	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= new BottomNavigationView.OnNavigationItemSelectedListener() {

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			switch (item.getItemId()) {

				case R.id.navigation_check_in:
					setTitle(item.getTitle());
//					Toolbar toolbar_check_in= (Toolbar) findViewById(R.id.toolbar_check_in);
//					setSupportActionBar(toolbar_check_in);
//					getSupportActionBar().setTitle("aaa");
					getFragmentManager()
							.beginTransaction()
							.replace(R.id.content, CheckInFragment.newInstance())
							.commit();
					return true;
				case R.id.navigation_history_record:
					setTitle(item.getTitle());
					getFragmentManager()
							.beginTransaction()
							.replace(R.id.content, HistoryFragment.newInstance())
							.commit();
					return true;
				case R.id.navigation_me:
					setTitle(item.getTitle());
					getFragmentManager()
							.beginTransaction()
							.replace(R.id.content, MeFragment.newInstance())
							.commit();
					return true;
			}
			return false;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.content, new CheckInFragment())
				.commit();

		BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
	}

	/**
	 * 双击返回桌面
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - time > 1600)) {
				Toast.makeText(this, "再按一次返回桌面", Toast.LENGTH_SHORT).show();
				time = System.currentTimeMillis();
			} else {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				startActivity(intent);
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

}
