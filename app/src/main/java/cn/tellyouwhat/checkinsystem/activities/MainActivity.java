package cn.tellyouwhat.checkinsystem.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

			Fragment history = getFragmentManager().findFragmentByTag("History");
			Fragment me = getFragmentManager().findFragmentByTag("Me");
			Fragment checkIn = getFragmentManager().findFragmentByTag("CheckIn");

			switch (item.getItemId()) {
				case R.id.navigation_check_in:
					setTitle(item.getTitle());
					if (checkIn != null)
						fragmentTransaction.show(checkIn);
					if (history != null)
						fragmentTransaction.hide(history);
					if (me != null)
						fragmentTransaction.hide(me);
					fragmentTransaction.commit();
					return true;
				case R.id.navigation_history_record:
					setTitle(item.getTitle());
					if (checkIn != null)
						fragmentTransaction.hide(checkIn);
					if (history != null)
						fragmentTransaction.show(history);
					else
						fragmentTransaction.add(R.id.content, HistoryFragment.newInstance(), "History");
					if (me != null)
						fragmentTransaction.hide(me);
					fragmentTransaction.commit();
					return true;
				case R.id.navigation_me:
					setTitle(item.getTitle());
					if (checkIn != null)
						fragmentTransaction.hide(checkIn);
					if (history != null)
						fragmentTransaction.hide(history);

					if (me != null)
						fragmentTransaction.show(me);
					else
						fragmentTransaction.add(R.id.content, MeFragment.newInstance(), "Me");

					fragmentTransaction.commit();
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
				.add(R.id.content, CheckInFragment.newInstance(), "CheckIn")
				.commit();

		BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
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
