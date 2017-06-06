package cn.tellyouwhat.checkinsystem.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.anzewei.parallaxbacklayout.ParallaxBackActivityHelper;
import com.github.anzewei.parallaxbacklayout.ParallaxBackLayout;
import com.jaeger.library.StatusBarUtil;
import com.xdandroid.hellodaemon.IntentWrapper;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.util.List;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.fragments.CheckInFragment;
import cn.tellyouwhat.checkinsystem.utils.NotifyUtil;

import static com.xdandroid.hellodaemon.IntentWrapper.whiteListMatters;

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
public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, DirectoryChooserFragment.OnFragmentInteractionListener {
    private static final String BILI_PINK = "0";
    private static final String ZHIHU_BLUE = "1";
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
    private ParallaxBackActivityHelper mHelper;
    private Preference batteryOptimizing;
    private String mDirectory;
    private DirectoryChooserFragment mDialog;
    private Preference useOngoingNotification;

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

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
        setBackEnable(true);
        setupActionBar();
        addPreferencesFromResource(R.xml.preferences);
        bindPreferenceSummaryToValue(findPreference("when_low_battery"));

        //电池优化选项
        batteryOptimizing = findPreference("ignore_battery_optimizing");
        if (batteryOptimizing != null) {
            batteryOptimizing.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new MaterialDialog.Builder(SettingsActivity.this)
                            .title("什么是电池优化？")
                            .content("如果用户离开设备一段时间，没有插上电源，屏幕会关闭，设备会进入Doze（打盹）模式。在这种模式下，系统会限制app的网络和CPU的服务来节省电量。系统也会阻止app访问网络，延迟它要做的任务、同步工作以及标准闹钟。 \n" +
                                    "系统会定期的退出Doze模式一小会儿，让app完成他们的延迟活动。在这个窗口期（Maintenance Window），系统运行所有的同步工作、任务以及闹钟，允许app访问网络。 \n" +
                                    "在每个Maintenance Window结束后，系统会再次进入Doze模式，挂起网络操作等。随着时间的推移，这个窗口期会出现的越来越不频繁，这样帮助设备省电。")
                            .positiveText("加入")
                            .negativeText("设置")
                            .neutralText("了解更多...")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Intent intent = new Intent("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
                                    intent.setData(Uri.parse("package:cn.tellyouwhat.checkinsystem"));
                                    startActivity(intent);
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Intent intent = new Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS");
                                    startActivity(intent);
                                }
                            })
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Uri uri = Uri.parse("http://blog.csdn.net/ada_dengpan/article/details/51108641");
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                }
                            })
                            .show();
                    return true;
                }
            });
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

            boolean hasIgnored = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                hasIgnored = powerManager.isIgnoringBatteryOptimizations(SettingsActivity.this.getPackageName());
            }
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if (hasIgnored) {
                batteryOptimizing.setSummary("已加入，关闭请前往系统电池优化设置");
            } else {
                batteryOptimizing.setSummary("未加入，长时间静止息屏时将暂停后台服务");
            }
        }

        //文件夹选择
        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .newDirectoryName("checkin")
                .allowNewDirectoryNameModification(true)
                .build();
        mDialog = DirectoryChooserFragment.newInstance(config);
        Preference customDownloadDirectory = findPreference("custom_download_directory");
        customDownloadDirectory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mDialog.show(getFragmentManager(), null);
                return true;
            }
        });

        useOngoingNotification = findPreference("use_ongoing_notification");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("immersed_status_bar_enabled", true)) {
            StatusBarUtil.setColor(SettingsActivity.this, ContextCompat.getColor(this, R.color.colorPrimary), 0);
        }

        Preference requestThirdPartPermission = findPreference("request_third_part_permission");
        requestThirdPartPermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                List<IntentWrapper> intentWrappers = whiteListMatters(SettingsActivity.this, "由于系统限制，必须要开启一些权限，您才能正常签到。\n");
                if (intentWrappers.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "所有权限已开放", Toast.LENGTH_SHORT).show();
                    preference.setSummary("所有权限已开放");
                }
                return true;
            }
        });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "use_ongoing_notification":
                if (!sharedPreferences.getBoolean("use_ongoing_notification", true)) {
                    new NotifyUtil(getApplicationContext(), CheckInFragment.CHECK_IN_STATUS).clear();
                    useOngoingNotification.setSummary("已关闭，可能错过签到时间");
                } else {
                    useOngoingNotification.setSummary("已开启，返回即可显示通知");
                }
                break;
            case "immersed_status_bar_enabled":
                if (sharedPreferences.getBoolean("immersed_status_bar_enabled", true)) {
                    StatusBarUtil.setColor(SettingsActivity.this,
                            ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimary)
                            , 0);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        setImmersive(false);
                    }
                }
                Toast.makeText(getApplicationContext(), "可能要重启才能生效", Toast.LENGTH_SHORT).show();
                break;
            case "change_style":
                /*switch (sharedPreferences.getString("change_style", "0")) {
                    case BILI_PINK:
						Themer.INSTANCE.setThemeSoft(this, R.style.AppTheme, null);//带有300毫秒的渐变动画
						break;
					case ZHIHU_BLUE:
						Themer.INSTANCE.setThemeSoft(this, R.style.ZhiHuBlue, null);//带有300毫秒的渐变动画
						break;
					default:
						Themer.INSTANCE.setThemeSoft(this, R.style.AppTheme, null);//带有300毫秒的渐变动画
						break;
				}*/
                break;
            case "g":
                break;
            default:
                break;
        }
    }

    @Override
    public void onSelectDirectory(@NonNull String path) {
        Preference customDownloadDirectory = findPreference("custom_download_directory");
        customDownloadDirectory.setSummary(path);
        customDownloadDirectory.setDefaultValue(path);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("custom_download_directory", path);
        editor.apply();
        mDialog.dismiss();
    }

    @Override
    public void onCancelChooser() {
        mDialog.dismiss();
    }
}
