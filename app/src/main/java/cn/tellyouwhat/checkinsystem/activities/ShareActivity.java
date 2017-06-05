package cn.tellyouwhat.checkinsystem.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;

import com.jaeger.library.StatusBarUtil;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.utils.AppManager;

/**
 * Created by Harbor-Laptop on 2017/6/3.
 */

public class ShareActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_share);
        AppManager.getAppManager().addActivity(this);
        setUpToolBar();
        setStatusBarColor();

    }

    @Override
    public void setStatusBarColor() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("immersed_status_bar_enabled", true)) {
            StatusBarUtil.setColor(this,
                    ContextCompat.getColor(this, R.color.colorPrimary),
                    0);
        } else {
            StatusBarUtil.setColor(this,
                    ContextCompat.getColor(this, R.color.colorPrimary));
        }
    }

    private void setUpToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_share);
        toolbar.setTitle("分享");
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setSupportActionBar(toolbar);
    }
}
