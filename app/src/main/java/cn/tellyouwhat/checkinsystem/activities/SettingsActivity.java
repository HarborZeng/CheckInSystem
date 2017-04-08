package cn.tellyouwhat.checkinsystem.activities;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.github.anzewei.parallaxbacklayout.ParallaxBackActivityHelper;
import com.github.anzewei.parallaxbacklayout.ParallaxBackLayout;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.services.LocationGettingService;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private ParallaxBackActivityHelper mHelper;
	private Preference batteryOptimizing;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mHelper.onPostCreate();
	}

	@Override
	public View findViewById(int id) {
		View v = super.findViewById(id);
		if (v == null && mHelper != null)
			return mHelper.findViewById(id);
		return v;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHelper.onActivityDestroy();
	}

	public ParallaxBackLayout getBackLayout() {
		return mHelper.getBackLayout();
	}

	public void setBackEnable(boolean enable) {
		getBackLayout().setEnableGesture(enable);
	}

	public void scrollToFinishActivity() {
		mHelper.scrollToFinishActivity();
	}

	@Override
	public void onBackPressed() {
		scrollToFinishActivity();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new ParallaxBackActivityHelper(this);
		setupActionBar();
		addPreferencesFromResource(R.xml.preferences);
		bindPreferenceSummaryToValue(findPreference("when_low_battery"));
		batteryOptimizing = findPreference("ignore_battery_optimizing");
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			// Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle("设置");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (batteryOptimizing != null) {
			batteryOptimizing.setSummary("已优化");
		}
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	}


	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference.setSummary(
						index >= 0
								? listPreference.getEntries()[index]
								: null);

			} else if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);

				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone.getTitle(preference.getContext());
						preference.setSummary(name);
					}
				}
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 *
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
				PreferenceManager
						.getDefaultSharedPreferences(preference.getContext())
						.getString(preference.getKey(), ""));
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Intent intent = new Intent(getApplicationContext(), LocationGettingService.class);
		switch (key) {
			/*case "use_background_service":
				boolean useBackgroundService = sharedPreferences.getBoolean("use_background_service", true);
				Log.d("useBackgroundService", "onSharedPreferenceChanged: " + useBackgroundService);
				*//*if(useBackgroundService){
					startService(intent);
				}else {
					stopService(intent);
				}*//*
				Snackbar.make(getCurrentFocus(), "需要重启应用程序生效", Snackbar.LENGTH_INDEFINITE).setAction("重启", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						restartApplication();
					}
				}).show();
				break;*/
			//TODO more settings changed should be listened here.
			case "use_GPS":
				/*	stopService(intent);
					startService(intent);*/
				break;
			case "b":
				break;
			case "n":
				break;
			case "m":
				break;
			case "g":
				break;
		}
	}

	private void restartApplication() {
		AlarmManager alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		alm.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()), 0));
	}

	public static void restart(Context context, int delay) {
		if (delay == 0) {
			delay = 1;
		}
		Log.e("", "restarting app");
		Intent restartIntent = context.getPackageManager()
				.getLaunchIntentForPackage(context.getPackageName());
		PendingIntent intent = PendingIntent.getActivity(
				context, 0,
				restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		manager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, intent);
		System.exit(2);
	}
}
