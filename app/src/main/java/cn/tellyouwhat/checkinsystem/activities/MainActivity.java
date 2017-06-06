package cn.tellyouwhat.checkinsystem.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.jaeger.library.StatusBarUtil;
import com.stephentuso.welcome.WelcomeHelper;
import com.xdandroid.hellodaemon.DaemonEnv;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.math.RoundingMode;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.bases.BaseApplication;
import cn.tellyouwhat.checkinsystem.fragments.CheckInFragment;
import cn.tellyouwhat.checkinsystem.fragments.HistoryFragment;
import cn.tellyouwhat.checkinsystem.fragments.MeFragment;
import cn.tellyouwhat.checkinsystem.services.AutoCheckInService;
import cn.tellyouwhat.checkinsystem.services.LocationGettingService;
import cn.tellyouwhat.checkinsystem.services.UpdateTodayStatusService;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.DoubleUtil;
import cn.tellyouwhat.checkinsystem.utils.ExceptionReporter;
import cn.tellyouwhat.checkinsystem.utils.NetTypeUtils;
import cn.tellyouwhat.checkinsystem.utils.PhoneInfoProvider;
import cn.tellyouwhat.checkinsystem.utils.ReLoginUtil;
import cn.tellyouwhat.checkinsystem.utils.SPUtil;

import static android.content.Intent.ACTION_VIEW;
import static com.xdandroid.hellodaemon.IntentWrapper.whiteListMatters;

public class MainActivity extends BaseActivity {

    private String TAG = "MainActivity";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            Fragment history = getSupportFragmentManager().findFragmentByTag("History");
            Fragment me = getSupportFragmentManager().findFragmentByTag("Me");
            Fragment checkIn = getSupportFragmentManager().findFragmentByTag("CheckIn");

            switch (item.getItemId()) {
                case R.id.navigation_check_in:
                    setTitle(item.getTitle());
                    if (checkIn != null)
                        fragmentTransaction.show(checkIn);
                    if (history != null)
                        fragmentTransaction.hide(history);
                    if (me != null)
                        fragmentTransaction.hide(me);
                    fragmentTransaction.commitAllowingStateLoss();
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
                    fragmentTransaction.commitAllowingStateLoss();
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

                    fragmentTransaction.commitAllowingStateLoss();
                    return true;
            }
            return false;
        }

    };
    private String mVersionName;
    private String mVersionDesc;
    private String mVersionCode;
    private String mDownloadURL;
    private boolean mForceUpgrade;
    private String mSize;
    private Bundle mInstanceState;
    private WelcomeHelper mWelcomeHelper;

    @Override
    protected void onResume() {
        super.onResume();
        boolean canMockLocation = canMockLocation();
        detectMockLocation(canMockLocation);
    }

    private void detectMockLocation(boolean canMockLocation) {
        if (canMockLocation) {
            new MaterialDialog.Builder(MainActivity.this)
                    .title("检测到您开启了“模拟位置”")
                    .content("\n您必须前往“开发者选项”\n\n关闭模拟位置相关选项后，才能继续使用CheckIn\n\n或关闭虚拟定位软件")
                    .positiveText("设置")
                    .icon(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.warning))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
//                   finish();
                        }
                    })
                    .negativeText("重试")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            boolean canMockLocation = canMockLocation();
                            detectMockLocation(canMockLocation);
                        }
                    })
                    .show()
                    .setCancelable(false);
        }
    }

    private boolean canMockLocation() {
        boolean canMockLocation = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            canMockLocation = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false;
            }
            PendingIntent intent = PendingIntent.getActivities(MainActivity.this, 99, new Intent[]{new Intent()}, PendingIntent.FLAG_ONE_SHOT);
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, intent);
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, intent);
            Location locationNetWork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null && locationNetWork != null) {
                canMockLocation = locationNetWork.isFromMockProvider() || locationGPS.isFromMockProvider();
            }
            locationManager.removeUpdates(intent);
            Log.i(TAG, "canMockLocation: canMockLocation? " + canMockLocation);
        }
        return canMockLocation;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInstanceState = savedInstanceState;
        setBackEnable(false);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        if (
                (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                        ||
                        (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE))
                        ||
                        (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION))
                ) {
            Log.i(TAG, "askForPermission: 没权限");
            askForPermission();

        } else {
            //有权限直接进逻辑
            AfterPermissionGranted();
        }
        initShortCut();
    }

    private void askForPermission() {
        new MaterialDialog.Builder(MainActivity.this)
                .title("请授权！")
                .contentColor(Color.parseColor("#B9887D"))
                .content("CheckIn正常运行需要三个权限\n" +
                        "\n1.下载安装更新，需要使用内置存储的读写权限\n" +
                        "\n2.获取手机设备序列号、机型和安卓版本等信息，需要电话权限\n" +
                        "\n3.基于GPS、基站和WiFi定位，需要定位的权限。")
                .iconRes(R.drawable.ic_verified_user_black_24dp)
                .negativeText("拒绝")
                .negativeColor(Color.parseColor("#88222222"))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "拒绝了合理请求的权限等同于不同意使用此软件", Toast.LENGTH_LONG).show();
                    }
                })
                .positiveText("授权")
                .positiveColor(Color.parseColor("#FF0000"))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE",
                                "android.permission.READ_PHONE_STATE",
                                "android.permission.ACCESS_FINE_LOCATION"};
                        //申请权限
                        ActivityCompat.requestPermissions(MainActivity.this, perms, 1);
                    }
                })
                .cancelable(false)
                .show().setCanceledOnTouchOutside(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Boolean canEnter[] = new Boolean[grantResults.length];
        if (requestCode == 1) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    canEnter[i] = false;
                    finish();
                } else {
                    canEnter[i] = true;
                }
            }
            try {
                if (canEnter[0] && canEnter[1] && canEnter[2]) {
                    AfterPermissionGranted();
                } else {
                    Toast.makeText(this, "您必须要授予权限才能继续", Toast.LENGTH_LONG).show();
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                finish();
            }
        }
    }

    private void AfterPermissionGranted() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkUpdate();
            }
        }).start();

        SystemClock.sleep(1000);

        SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
        String token = userInfo.getString(ConstantValues.TOKEN, "");
        if (TextUtils.isEmpty(token)) {
            enterLogin();
        } else {
            synchronized (MainActivity.class) {
                mWelcomeHelper = new WelcomeHelper(this, IntroActivity.class);
                mWelcomeHelper.show(mInstanceState);
            }
            setTheme(R.style.AppTheme);
            setContentView(R.layout.activity_main);
            setTitle("签到");
            Intent checkIntent = getIntent();
            boolean beginCheckIn = checkIntent.getBooleanExtra("BEGIN_CHECK_IN", false);
            boolean beginCheckOut = checkIntent.getBooleanExtra("BEGIN_CHECK_OUT", false);
            Bundle bundle = new Bundle();
            bundle.putBoolean("BEGIN_CHECK_IN", beginCheckIn);
            bundle.putBoolean("BEGIN_CHECK_OUT", beginCheckOut);

            ActionBar supportActionBar = getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.setDisplayHomeAsUpEnabled(false);
            }
            CheckInFragment checkInFragment = CheckInFragment.newInstance();
            checkInFragment.setArguments(bundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content, checkInFragment, "CheckIn")
                    .commitAllowingStateLoss();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (sharedPref.getBoolean("immersed_status_bar_enabled", true)) {
                StatusBarUtil.setColor(MainActivity.this,
                        ContextCompat.getColor(MainActivity.this, R.color.colorPrimary),
                        0);
            }

            startServices(sharedPref);

            BottomNavigationView mNavigation = (BottomNavigationView) findViewById(R.id.navigation);
            mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        }
    }

    private void initShortCut() {
        SPUtil spUtil = new SPUtil(this);
// 判断是否第一次启动应用程序（默认为true）
        boolean firstStart = spUtil.getBoolean(ConstantValues.FIRST_TIME_RUN, true);
// 第一次启动时创建桌面快捷方式
        if (firstStart) {
            Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
// 快捷方式的名称
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
// 不允许重复创建
            shortcut.putExtra("duplicate", false);
// 指定快捷方式的启动对象
            ComponentName comp = new ComponentName(this.getPackageName(), "." + this.getLocalClassName());
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN).setComponent(comp));
// 快捷方式的图标
            Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher);
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
// 发出广播
            sendBroadcast(shortcut);
// 将第一次启动的标识设置为false
            spUtil.putBoolean(ConstantValues.FIRST_TIME_RUN, false);
// 提交设置
            Toast.makeText(this, R.string.shortcut_created, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * After checking, if there is no newer version exiting, enter the {@link MainActivity} directly.
     */
    private void enterLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
//		finish();
    }

    private void startServices(SharedPreferences sharedPref) {
        SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
        final String token = userInfo.getString(ConstantValues.TOKEN, "");
        if (!TextUtils.isEmpty(token)) {
            //开启获取位置的后台服务
            boolean backGroundServiceEnabled = sharedPref.getBoolean("use_background_service", true);
            if (backGroundServiceEnabled) {
                DaemonEnv.initialize(getApplicationContext(), LocationGettingService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
                try {
                    startService(new Intent(this, LocationGettingService.class));
                } catch (Exception ignored) {
                }
            }
            //开启自动签入签出的服务
            DaemonEnv.initialize(getApplicationContext(), AutoCheckInService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
            try {
                startService(new Intent(this, AutoCheckInService.class));
            } catch (Exception ignored) {
            }
            //开启自动更新今日状态的服务
            DaemonEnv.initialize(getApplicationContext(), UpdateTodayStatusService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
            try {
                startService(new Intent(this, UpdateTodayStatusService.class));
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReLoginUtil.removeAllDialog();
        BaseApplication.appExit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == WelcomeHelper.DEFAULT_WELCOME_SCREEN_REQUEST) {
            // The key of the welcome screen is in the Intent
            //这条语句可能会被报空指针
//			String welcomeKey = data.getStringExtra(IntroActivity.WELCOME_SCREEN_KEY);
            whiteListMatters(MainActivity.this, "由于系统限制，必须要开启一些权限，您才能正常签到。\n");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mWelcomeHelper == null) {
            return;
        }
        mWelcomeHelper.onSaveInstanceState(outState);
    }

    public void checkUpdate() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            Log.d(TAG, "checkUpdate: 网络正常");

            RequestParams params = new RequestParams("https://update.checkin.tellyouwhat.cn/update.json");
            x.http().get(params, new Callback.CommonCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject object) {
                    try {
                        mVersionName = object.getString("versionName");
                        mVersionDesc = object.getString("versionDesc");
                        mVersionCode = object.getString("versionCode");
                        mDownloadURL = object.getString("downloadURL");
                        mForceUpgrade = object.getBoolean("forceUpgrade");
                        mSize = object.getString("size");
                        int localVersionCode = PhoneInfoProvider.getLocalVersionCode(getApplicationContext());
                        if (localVersionCode < Integer.parseInt(mVersionCode)) {
                            Log.d(TAG, "onUpdateAvailable: 有更新版本：" + mVersionName);
                            askToUpgrade();
                        } else if (localVersionCode > Integer.parseInt(mVersionCode)) {
                            Toast.makeText(x.app(), R.string.you_are_using_an_Alpha_Test_application, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ExceptionReporter.reportException(e.getMessage(), PhoneInfoProvider.getInstance().getAllInfo(getApplicationContext()));
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    Log.w(TAG, "run: JSON parser may occurred error or it's an IOException", ex);
                    Toast.makeText(x.app(), R.string.server_error, Toast.LENGTH_SHORT).show();
                    ExceptionReporter.reportException("JSON parser may occurred error or it's an IOException",
                            ex.getMessage(),
                            PhoneInfoProvider.getInstance().getAllInfo(getApplicationContext()));
                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onFinished() {

                }
            });
        } else {
//       Log.d(TAG, "checkUpdate: 网络未连接");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.not_connected_to_server, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void askToUpgrade() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
            ActivityCompat.requestPermissions(MainActivity.this, perms, 1);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                boolean showUpgradeDialogOnlyUnderWifi = sharedPref.getBoolean("show_upgrade_dialog_only_under_wifi", true);

                final MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this);
                final MaterialDialog.Builder innerBuilder = new MaterialDialog.Builder(MainActivity.this);
                builder.iconRes(R.mipmap.warning);
                builder.content(getString(R.string.newer_version_detected) + mVersionName + "\n" + getString(R.string.size) + mSize + getString(R.string.newer_version_description) + "\n\n" + mVersionDesc)
                        .positiveText(getString(R.string.我要升级))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                    Toast.makeText(x.app(), R.string.cannot_access_external_storage, Toast.LENGTH_LONG).show();
                                } else {
                                    if (NetTypeUtils.isWifiActive(MainActivity.this)) {
                                        Log.d(TAG, "onClick: 连的是wifi");
                                        download();
                                    } else {
                                        innerBuilder.iconRes(R.mipmap.warning);
                                        innerBuilder.cancelable(false)
                                                .content(R.string.you_are_using_data_now)
                                                .title(R.string.Are_you_sure)
                                                .negativeText(R.string.I_am_broken)
                                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        Toast.makeText(x.app(), R.string.update_after_WiFied, Toast.LENGTH_LONG).show();
                                                        dialog.dismiss();
                                                        MainActivity.this.finish();
                                                    }
                                                })
                                                .positiveText(R.string.I_am_rich)
                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                        dialog.dismiss();
                                                        download();
                                                    }
                                                }).show();
                                    }
                                }
                            }
                        })
                        .neutralText("去酷安更新")
                        .neutralColor(ContextCompat.getColor(getApplicationContext(), R.color.sample_red))
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                gotoCoolAPK();
                            }
                        });
                if (mForceUpgrade) {
                    builder.title(R.string.must_upgrade).cancelable(false);
                    builder.show().setCanceledOnTouchOutside(false);
                } else {
                    builder.title(R.string.有新版本啦).cancelable(true);
                    builder.negativeText(R.string.不更新)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            });
                    if (!showUpgradeDialogOnlyUnderWifi) {
                        builder.show().setCanceledOnTouchOutside(false);
                    }
                }
            }
        });
    }

    private void download() {
        RequestParams params = new RequestParams(mDownloadURL);
//    Log.d(TAG, "download link: " + downloadURL);
        params.setAutoRename(true);
        params.setCacheSize(1024 * 1024 * 8);
        params.setCancelFast(true);

        params.setCacheDirName(Environment.getDownloadCacheDirectory().getPath());
        String newVersionFileName = mDownloadURL.substring(mDownloadURL.lastIndexOf("/") + 1, mDownloadURL.length());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String customDownloadDirectory = sharedPref.getString("custom_download_directory", Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS);

        params.setSaveFilePath(customDownloadDirectory + "/" + newVersionFileName);
//    Log.d("TAG", "download: " + Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + newVersionFileName);
//       progressBar.setProgress(0);
        final ProgressDialog builder = new ProgressDialog(MainActivity.this, ProgressDialog.STYLE_SPINNER);
        if (mForceUpgrade) {
            builder.setCancelable(false);
            builder.setCanceledOnTouchOutside(false);
        }
//    Log.d(TAG, "download: params：" + params);

        final Callback.Cancelable cancelable = x.http().get(params, new Callback.ProgressCallback<File>() {

            @Override
            public void onWaiting() {
//          Toast.makeText(SplashActivity.this, "正在等待下载开始", Toast.LENGTH_SHORT).show();
//          Log.d(TAG, "onWaiting: 正在等待下载开始");
            }

            @Override
            public void onStarted() {
                Toast.makeText(x.app(), R.string.download_begins, Toast.LENGTH_SHORT).show();
//          Log.d(TAG, "onStarted: 下载开始");
                builder.setIcon(R.mipmap.downloading);
                builder.setTitle(getString(R.string.downloading));
                builder.setCancelable(true);
                builder.setCanceledOnTouchOutside(false);
                builder.show();
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(getString(R.string.please_wait_a_second)).append(DoubleUtil.formatDouble2(((double) current) / 1024 / 1024, RoundingMode.DOWN, 2)).append("/").append(DoubleUtil.formatDouble2(((double) total) / 1024 / 1024, RoundingMode.DOWN, 2)).append("M");
                builder.setMessage(stringBuffer);
            }

            @Override
            public void onSuccess(File result) {
                builder.dismiss();
//          Toast.makeText(x.app(), "下载成功", Toast.LENGTH_SHORT).show();
//          Log.d(TAG, "onSuccess: The File is: " + result);
                installAPK(result);
//          Log.d(TAG, "onSuccess: 下载成功");
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
//          ex.printStackTrace();
                Toast.makeText(x.app(), R.string.error_in_downloading, Toast.LENGTH_SHORT).show();
                ExceptionReporter.reportException("下载出错",
                        ex.getMessage(),
                        PhoneInfoProvider.getInstance().getAllInfo(getApplicationContext()));
//          enterHome();
//          Log.d(TAG, "onError: 下载出错啦");
                new MaterialDialog.Builder(MainActivity.this)
                        .title("下载遇到问题？")
                        .content("请注意通知栏上面的下载进度")
                        .positiveText("去浏览器下载")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Intent intent = new Intent(ACTION_VIEW, Uri.parse(mDownloadURL));
                                startActivity(intent);
                                MainActivity.this.finish();
                            }
                        })
                        .neutralText("调用系统下载器")
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                DownloadManager.Request request = new DownloadManager.Request(
                                        Uri.parse(mDownloadURL));
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                                long enqueue = downloadManager.enqueue(request);
                                //TODO 下载逻辑

                            }
                        })
                        .negativeText("去酷安更新")
                        .negativeColor(ContextCompat.getColor(getApplicationContext(), R.color.sample_red))
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                gotoCoolAPK();
                            }
                        })
                        .show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
//          Log.d(TAG, "onCancelled: 下载已取消");
//          Toast.makeText(x.app(), "cancelled", Toast.LENGTH_SHORT).show();
//          enterLogin();
                builder.dismiss();
                MainActivity.this.finish();
            }

            @Override
            public void onFinished() {
//          Toast.makeText(x.app(), "下载完成", Toast.LENGTH_SHORT).show();
//          Log.d(TAG, "onFinished: 下载完成");
                builder.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                builder.dismiss();
                cancelable.cancel();
                Toast.makeText(MainActivity.this, R.string.download_canceled, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void installAPK(File result) {
//    Log.i(TAG, "installAPK: 刚刚进入安装apk的方法");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            uri = FileProvider.getUriForFile(MainActivity.this, "cn.tellyouwhat.checkinsystem.provider", result);
        } else {
            uri = Uri.fromFile(result);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
//    Log.i(TAG, "installAPK: 准备好了数据，马上开启下一个activity");
        startActivity(intent);
    }

    private void gotoCoolAPK() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri)
                .setPackage("com.coolapk.market")  //指定应用市场
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "您的手机并没有安装酷安", Toast.LENGTH_LONG).show();
            uri = Uri.parse("http://www.coolapk.com/apk/cn.tellyouwhat.checkinsystem");
            Intent gotoWebsite = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(gotoWebsite);
        }
    }
}