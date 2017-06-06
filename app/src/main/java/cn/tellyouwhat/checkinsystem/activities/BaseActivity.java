package cn.tellyouwhat.checkinsystem.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.github.anzewei.parallaxbacklayout.ParallaxActivityBase;
import com.jaeger.library.StatusBarUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.http.cookie.DbCookieStore;
import org.xutils.x;

import java.net.HttpCookie;
import java.util.List;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.EncryptUtil;
import cn.tellyouwhat.checkinsystem.utils.ReLoginUtil;

/**
 * Created by Harbor-Laptop on 2017/2/24.
 *
 * @author HarborZeng
 *         It's all activities' root
 */

public class BaseActivity extends ParallaxActivityBase {
    private AlertDialog mAlertDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    /**
     * Requests given permission.
     * If the permission has been denied previously, a Dialog will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    protected void requestPermission(final String permission, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showAlertDialog("需要权限", rationale,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(BaseActivity.this,
                                    new String[]{permission}, requestCode);
                        }
                    }, "授权", null, "取消");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    public void setStatusBarColor() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("immersed_status_bar_enabled", true)) {
            StatusBarUtil.setColor(this,
                    ContextCompat.getColor(this, R.color.colorPrimary),
                    0);
        }
    }

    /**
     * This method shows dialog with given title & message.
     * Also there is an option to pass onClickListener for positive & negative button.
     *
     * @param title                         - dialog title
     * @param message                       - dialog message
     * @param onPositiveButtonClickListener - listener for positive button
     * @param positiveText                  - positive button text
     * @param onNegativeButtonClickListener - listener for negative button
     * @param negativeText                  - negative button text
     */
    protected void showAlertDialog(@Nullable String title, @Nullable String message,
                                   @Nullable DialogInterface.OnClickListener onPositiveButtonClickListener,
                                   @NonNull String positiveText,
                                   @Nullable DialogInterface.OnClickListener onNegativeButtonClickListener,
                                   @NonNull String negativeText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, onPositiveButtonClickListener);
        builder.setNegativeButton(negativeText, onNegativeButtonClickListener);
        mAlertDialog = builder.show();
    }

    /**
     * Hide alert dialog if any.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    public void updateSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        String userName = sharedPreferences.getString("USER_NAME", "");
        String encryptedToken = sharedPreferences.getString(ConstantValues.TOKEN, "");
        String token = EncryptUtil.decryptBase64withSalt(encryptedToken, ConstantValues.SALT);
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(token)) {
            @SuppressLint("HardwareIds") String deviceID = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
            RequestParams p = new RequestParams("https://api.checkin.tellyouwhat.cn/User/UpdateSession?username=" + userName + "&deviceid=" + deviceID + "&token=" + token);
            x.http().get(p, new Callback.CommonCallback<JSONObject>() {

                private int resultInt;

                @Override
                public void onSuccess(JSONObject result) {
                    try {
                        resultInt = result.getInt("result");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    switch (resultInt) {
                        case 1:
                            DbCookieStore instance = DbCookieStore.INSTANCE;
                            List<HttpCookie> cookies = instance.getCookies();
                            for (HttpCookie cookie : cookies) {
                                String name = cookie.getName();
                                String value = cookie.getValue();
                                if (ConstantValues.COOKIE_NAME.equals(name)) {
                                    SharedPreferences preferences = x.app().getSharedPreferences(ConstantValues.COOIKE_SHARED_PREFERENCE_NAME, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString(ConstantValues.cookie, value);
                                    editor.apply();
//									Log.i("在BaseFragment里面", "onSuccess: session 已经更新");
                                    break;
                                }
                            }
                            break;
                        case 0:
                            ReLoginUtil reLoginUtil = new ReLoginUtil(BaseActivity.this);
                            try {
                                Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            reLoginUtil.reLoginWithAlertDialog();
                            break;
                        case -1:
                            try {
                                Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {

                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onFinished() {

                }
            });
        } else {
            //存在sharedPreference里面的username或token是空的
            ReLoginUtil reLoginUtil = new ReLoginUtil(BaseActivity.this);
            reLoginUtil.reLoginWithAlertDialog();
        }
    }

}
