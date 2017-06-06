package cn.tellyouwhat.checkinsystem.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
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
import cn.tellyouwhat.checkinsystem.services.AutoCheckInService;
import cn.tellyouwhat.checkinsystem.services.LocationGettingService;
import cn.tellyouwhat.checkinsystem.services.UpdateTodayStatusService;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.CookiedRequestParams;
import cn.tellyouwhat.checkinsystem.utils.EncryptUtil;
import cn.tellyouwhat.checkinsystem.utils.ReLoginUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A login screen that offers login via number/password.
 */
public class LoginActivity extends BaseActivity {

    private final String TAG = "LoginActivity";

    private EditText mNumberView;
    private EditText mPasswordView;
    private View mProgressView;
    private SharedPreferences mSharedPreferences;
    //只有特定版本才可以把这个bool设为true，用来告诉app需要显示引导页
    private View mLoginBG;
    private Button mNumberSignInButton;
    private Button mForgetPasswordButton;

    /**
     * 此方法为对登陆界面的“忘记密码”点击事件的响应，即携带面板上已输入手机号（仅限），前往{@link ResetPasswordActivity}重置密码
     *
     * @param view 按钮所在的父view
     */
    public void forgetPassword(View view) {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        String phoneNumberTOBE = mNumberView.getText().toString();
        if (!TextUtils.isEmpty(phoneNumberTOBE) && phoneNumberTOBE.length() == 11)
            intent.putExtra("PhoneNumber", phoneNumberTOBE);
//		Log.i(TAG, "forgetPassword: " + phoneNumberTOBE);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackEnable(false);
        setContentView(R.layout.activity_login);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        stopService(new Intent(this, UpdateTodayStatusService.class));
        stopService(new Intent(this, LocationGettingService.class));
        stopService(new Intent(this, AutoCheckInService.class));

        mLoginBG = findViewById(R.id.login_bg);
        mLoginBG.setVisibility(View.GONE);

        StatusBarUtil.setTransparent(this);

        // Set up the login form.
        mNumberView = (EditText) findViewById(R.id.number);
        mPasswordView = (EditText) findViewById(R.id.password);

        mForgetPasswordButton = (Button) findViewById(R.id.button_forgetPassword);
        mForgetPasswordButton.setClickable(true);

        mNumberSignInButton = (Button) findViewById(R.id.number_sign_in_button);
        mNumberSignInButton.setClickable(true);

        mProgressView = findViewById(R.id.login_progress);
        mNumberSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        //获得sp实例对象
        mSharedPreferences = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        mNumberView.setText(mSharedPreferences.getString("USER_NAME", ""));

        //输入好账号后显示头像
        mNumberView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String number = mNumberView.getText().toString().trim();
                    RequestParams requestParams = new RequestParams("https://api.checkin.tellyouwhat.cn/User/getHeadImage?username=" + number);
                    x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {
                        @Override
                        public void onSuccess(JSONObject result) {
//							Log.d(TAG, "onSuccess: 获取头像的结果是：" + result);
                            try {
                                int resultInt = result.getInt("result");
                                switch (resultInt) {
                                    case 1:
                                        String headImageBase64ed = result.getString("image");
                                        byte[] decodedHeadImage = Base64.decode(headImageBase64ed, Base64.DEFAULT);
                                        Bitmap bitmapHeadImage = BitmapFactory.decodeByteArray(decodedHeadImage, 0, decodedHeadImage.length);
                                        CircleImageView headImage = (CircleImageView) findViewById(R.id.profile_image);
                                        headImage.setImageBitmap(bitmapHeadImage);
                                        YoYo.with(Techniques.FadeIn)
                                                .duration(300)
                                                .repeat(1)
                                                .playOn(headImage);
                                        break;
                                    case -1:
                                        break;
                                    case -2:
                                        Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_LONG).show();
                                        break;
                                    default:
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable ex, boolean isOnCallback) {
                            Snackbar.make(findViewById(R.id.login_form), "获取头像出错", Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCancelled(CancelledException cex) {

                        }

                        @Override
                        public void onFinished() {

                        }
                    });
                }
            }
        });

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid Number, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */

    private void attemptLogin() {

        mNumberView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String number = mNumberView.getText().toString().trim();
        final String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
            YoYo.with(Techniques.Tada)
                    .duration(500)
                    .repeat(1)
                    .playOn(findViewById(R.id.card_view_password));
            findViewById(R.id.card_view_password).setAlpha(0.5f);
        } else {
            // Check for a valid password, if the user entered one.
            if (!isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
                YoYo.with(Techniques.Tada)
                        .duration(500)
                        .repeat(1)
                        .playOn(findViewById(R.id.card_view_password));
                findViewById(R.id.card_view_password).setAlpha(0.5f);
            }
        }
        // Check for a valid Number address.
        if (TextUtils.isEmpty(number)) {
            mNumberView.setError(getString(R.string.error_field_required));
            focusView = mNumberView;
            cancel = true;
            YoYo.with(Techniques.Tada)
                    .duration(500)
                    .repeat(1)
                    .playOn(findViewById(R.id.card_view_username));
            findViewById(R.id.card_view_username).setAlpha(0.5f);
        } else if (!isNumberValid(number)) {
            mNumberView.setError(getString(R.string.error_invalid_number));
            focusView = mNumberView;
            cancel = true;
            YoYo.with(Techniques.Tada)
                    .duration(500)
                    .repeat(1)
                    .playOn(findViewById(R.id.card_view_username));
            findViewById(R.id.card_view_username).setAlpha(0.5f);
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    login(number, password);
                }
            }).start();

            AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
            alphaAnimation.setDuration(500);
            mLoginBG.setVisibility(View.VISIBLE);
            mLoginBG.startAnimation(alphaAnimation);
        }
    }

    private boolean isNumberValid(String number) {
        return number.length() > 5;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        mNumberSignInButton.setClickable(!show);
        mForgetPasswordButton.setClickable(!show);
        mLoginBG.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

    }

    /**
     * 通过servlet小程序与服务器数据库进行验证操作，并进行后续处理工作
     *
     * @param number   用户输入的手机号或工号
     * @param password 用户输入的密码
     */
    private void login(final String number, final String password) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();

        //通过加盐的MD5算法加密密码，以确保传输过程的安全性
        String encryptedPassword = EncryptUtil.md5WithSalt(password, ConstantValues.SALT);

        //开始准备数据
        @SuppressLint("HardwareIds") String deviceID = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        RequestParams params = new RequestParams("https://api.checkin.tellyouwhat.cn/User/Login?username=" + number + "&password=" + encryptedPassword + "&deviceid=" + deviceID);
        params.setConnectTimeout(5000);
        //利用xUtils3get提交
        x.http().get(params, new Callback.CommonCallback<JSONObject>() {

            private int loginResponseCode;
            private String token;

            @Override
            public void onSuccess(JSONObject result) {
                try {
                    loginResponseCode = result.getInt("result");
                    if (loginResponseCode == 1) {
                        token = result.getString("token");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//				Log.i(TAG, "onSuccess: " + result);
                if (loginResponseCode == -2) {
                    showProgress(false);
                    mPasswordView.setText("");
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    YoYo.with(Techniques.Tada)
                            .duration(500)
                            .repeat(1)
                            .playOn(findViewById(R.id.card_view_password));
                    findViewById(R.id.card_view_password).setAlpha(0.5f);
                } else if (loginResponseCode == -1) {
                    try {
                        Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            mLoginBG.setVisibility(View.INVISIBLE);
                            mNumberSignInButton.setClickable(true);
                            mForgetPasswordButton.setClickable(true);
                        }
                    });
                } else {
                    Log.d(TAG, "onPostExecute: 登录成功");
                    //这里到时候服务器搭好以后传入自己的token
                    if (!TextUtils.isEmpty(token)) {
                        editor.putString(ConstantValues.TOKEN, EncryptUtil.encryptBase64withSalt(token, ConstantValues.SALT));
                    }
                    editor.putString("USER_NAME", number);
                    editor.apply();
                    updateSession();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.w(TAG, "onError: " + ex);
                showProgress(false);
                Snackbar.make(findViewById(R.id.login_form), "网络开小差了~~", Snackbar.LENGTH_LONG).show();
                mLoginBG.setVisibility(View.INVISIBLE);
                mNumberSignInButton.setClickable(true);
                mForgetPasswordButton.setClickable(true);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                showProgress(false);
                Snackbar.make(findViewById(R.id.login_form), "服务器正忙，请稍候重试", Snackbar.LENGTH_LONG).show();
                mLoginBG.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onFinished() {
            }
        });
    }

    private void storeUserInfo() {
//		Log.i(TAG, "store user information: setting up...");
        CookiedRequestParams requestParams = new CookiedRequestParams("https://api.checkin.tellyouwhat.cn/User/GetUserInfo");
        x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {
            private int resultInt;

            @Override
            public void onSuccess(JSONObject result) {
                try {
                    resultInt = result.getInt("result");
//					Log.d(TAG, "onSuccess: resultInt=" + resultInt + "and result is " + result.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (resultInt) {
                    case 1:
                        try {
                            String employeeID = result.getString("employeeid");
                            String name = result.getString("name");
                            String departmentName = result.getString("departmentname");
                            String phoneNumber = result.getString("phonenumber");
                            String email = result.getString("email");
                            String headImage = result.getString("headimage");
                            SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                            editor.putString("employeeID", employeeID);
                            editor.putString("name", name);
                            editor.putString("departmentName", departmentName);
                            editor.putString("phoneNumber", phoneNumber);
                            editor.putString("email", email);
                            editor.putString("headImage", headImage);
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 0:
                        updateSession();
                        break;
                    case -1:
                        try {
                            Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_SHORT).show();
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
//							Log.i(TAG, "onSuccess: session 已经更新");
                            DbCookieStore instance = DbCookieStore.INSTANCE;
                            List<HttpCookie> cookies = instance.getCookies();
                            for (HttpCookie cookie : cookies) {
                                String name = cookie.getName();
                                String value = cookie.getValue();
                                if (ConstantValues.COOKIE_NAME.equals(name)) {
                                    SharedPreferences preferences = getSharedPreferences(ConstantValues.COOIKE_SHARED_PREFERENCE_NAME, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString(ConstantValues.cookie, value);
                                    editor.apply();
                                    break;
                                }
                            }
                            storeUserInfo();
                            enterMain();
                            break;
                        case 0:
                            ReLoginUtil reLoginUtil = new ReLoginUtil(LoginActivity.this);
                            try {
                                Toast.makeText(LoginActivity.this, result.getString("message"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            reLoginUtil.reLoginWithAlertDialog();
                            break;
                        case -1:
                            try {
                                Toast.makeText(LoginActivity.this, result.getString("message"), Toast.LENGTH_SHORT).show();
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
            ReLoginUtil reLoginUtil = new ReLoginUtil(LoginActivity.this);
            reLoginUtil.reLoginWithAlertDialog();
        }
    }

    private void enterMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        x.task().postDelayed(new Runnable() {
            @Override
            public void run() {
                LoginActivity.this.finish();
            }
        }, 3300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class LoginTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            attemptLogin();
            return 0;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
    }
}

