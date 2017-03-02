package cn.tellyouwhat.checkinsystem.activities;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.fragments.HistoryFragment;


public class HomeActivity extends BaseActivity
		implements NavigationView.OnNavigationItemSelectedListener {

	private final String TAG = "HomeActivity";
	long time = 0;
	private DrawerLayout drawer;
	private TextView mCoordinate;
	private LocationManager locationManager;
	private int timesPressed = 0;
	private CalendarView mCalendarView;
	private MyLocationListener myLocationListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home);

		mCalendarView = (CalendarView) findViewById(R.id.calendarView);

		mCoordinate = (TextView) findViewById(R.id.coordinate);
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

//		final Snackbar snackbar = Snackbar.make(findViewById(R.id.drawer_layout), "必须要授予权限，程序才能正常运行", Snackbar.LENGTH_INDEFINITE);
//		snackbar.setAction("我知道啦", new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				snackbar.dismiss();
//			}
//		}).show();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		myLocationListener = new MyLocationListener();
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (timesPressed != 0) {
					Toast.makeText(HomeActivity.this, R.string.dont_rush, Toast.LENGTH_SHORT).show();
				} else {
					if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//开始定位
						List<String> allProviders = locationManager.getAllProviders();
						for (String allProvider : allProviders) {
							Log.i(TAG, "onClick: " + allProvider);
						}
						locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, myLocationListener);

						Snackbar.make(view, R.string.getting_location, Snackbar.LENGTH_INDEFINITE)
								.setAction("取消", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										if (locationManager != null) {
											locationManager.removeUpdates(myLocationListener);
										}
										//取消定位请求
										timesPressed = 0;
									}
								}).show();
						timesPressed++;
					} else {
						Snackbar.make(view, "必须要授权位置访问才能正常工作", Snackbar.LENGTH_INDEFINITE)
								.setAction("授权", new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										String[] perms = {"android.permission.ACCESS_FINE_LOCATION"};
										ActivityCompat.requestPermissions(HomeActivity.this, perms, 1);
									}
								}).show();
					}
				}
			}
		});


		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		navigationView.setCheckedItem(R.id.nav_check_in);
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.

		int id = item.getItemId();


		switch (id) {
			case R.id.nav_check_in:
				setTitle(item.getTitle());
				item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {

						return false;
					}
				});
				mCoordinate.setVisibility(View.VISIBLE);

				break;
			case R.id.nav_history:
				setTitle(item.getTitle());
				getFragmentManager()
						.beginTransaction()
						.replace(R.id.calendar_layout, new HistoryFragment())
						.commit();
				mCoordinate.setVisibility(View.GONE);
				mCalendarView.setVisibility(View.VISIBLE);
				break;

		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
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

	@Override
	protected void onDestroy() {
		if (locationManager != null) {
			locationManager.removeUpdates(myLocationListener);
		}
		super.onDestroy();
	}

	/**
	 * 实现{@link LocationListener}
	 */
	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			float accuracy = location.getAccuracy();
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			Log.i(TAG, "onLocationChanged: accuracy: " + accuracy + ", latitude: " + latitude + ", longitude: " + longitude);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}
	}
}
