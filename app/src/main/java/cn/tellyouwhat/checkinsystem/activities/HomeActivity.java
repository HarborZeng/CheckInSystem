package cn.tellyouwhat.checkinsystem.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.utils.DoubleUtil;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;


public class HomeActivity extends BaseActivity
		implements NavigationView.OnNavigationItemSelectedListener {
	long time = 0;
	private DrawerLayout drawer;
	private TextView mCoordinate;
	private LocationManager locationManager;
	private int timesPressed = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		mCoordinate = (TextView) findViewById(R.id.coordinate);
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


		mayRequestLocation();

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		final Snackbar snackbar = Snackbar.make(findViewById(R.id.drawer_layout), "必须要授予权限，程序才能正常运行", Snackbar.LENGTH_INDEFINITE);
		snackbar.setAction("我知道啦", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				snackbar.dismiss();
			}
		}).show();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

			fab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (timesPressed != 0) {
						Toast.makeText(HomeActivity.this, "不要着急，不要猛戳", Toast.LENGTH_SHORT).show();
					} else {
						if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
							final MyLocation myLocation = new MyLocation();
							locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, myLocation);
							Snackbar.make(view, R.string.getting_location, Snackbar.LENGTH_INDEFINITE)
									.setAction("取消", new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											locationManager.removeUpdates(myLocation);
											timesPressed = 0;
										}
									}).show();
							timesPressed++;
						}
					}
				}
			});
		} else {
			fab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Snackbar.make(v, "必须要授权位置访问才能正常工作", Snackbar.LENGTH_INDEFINITE)
							.setAction("授权", new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									String[] perms = {"android.permission.ACCESS_FINE_LOCATION"};
									ActivityCompat.requestPermissions(HomeActivity.this, perms, 1);
								}
							}).show();
				}
			});
		}

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

		if (id == R.id.nav_check_in) {
			setTitle(item.getTitle());

		} else if (id == R.id.nav_gallery) {

		} else if (id == R.id.nav_slideshow) {

		} else if (id == R.id.nav_manage) {

		} else if (id == R.id.nav_share) {

		} else if (id == R.id.nav_send) {

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

	class MyLocation implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			long time = location.getTime();
			double longitude = location.getLongitude();
			double latitude = location.getLatitude();
			mCoordinate.setText("time: " + time + "; longitude: " + DoubleUtil.formatDouble2(longitude, RoundingMode.DOWN, 4) + "; latitude: " + DoubleUtil.formatDouble2(latitude, RoundingMode.DOWN, 4));
			Log.w("onLocationChanged", "onLocationChanged: " + "longitude: " + DoubleUtil.formatDouble2(longitude, RoundingMode.DOWN, 4) + "; latitude: " + DoubleUtil.formatDouble2(latitude, RoundingMode.DOWN, 4));
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			System.out.println("onStatusChanged");
		}

		@Override
		public void onProviderEnabled(String provider) {
			System.out.println("onProviderEnabled");
		}

		@Override
		public void onProviderDisabled(String provider) {
			System.out.println("onProviderDisabled");

		}
	}

	private boolean mayRequestReadPhoneState() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return true;
		}
		if (checkSelfPermission(READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
			return true;
		}
		if (shouldShowRequestPermissionRationale(READ_PHONE_STATE)) {
			Snackbar.make(findViewById(R.id.first_screen_image), R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
					.setAction(android.R.string.ok, new View.OnClickListener() {
						@Override
						@TargetApi(Build.VERSION_CODES.M)
						public void onClick(View v) {
							requestPermissions(new String[]{READ_PHONE_STATE}, 0);
						}
					});
		} else {
			requestPermissions(new String[]{READ_PHONE_STATE}, 0);
		}
		return false;
	}

	private boolean mayRequestLocation() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return true;
		}
		if (checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			return true;
		}
		if (shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {
			Snackbar.make(findViewById(R.id.first_screen_image), R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
					.setAction(android.R.string.ok, new View.OnClickListener() {
						@Override
						@TargetApi(Build.VERSION_CODES.M)
						public void onClick(View v) {
							requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, 1);
						}
					});
		} else {
			requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, 1);
		}
		return false;
	}
}
