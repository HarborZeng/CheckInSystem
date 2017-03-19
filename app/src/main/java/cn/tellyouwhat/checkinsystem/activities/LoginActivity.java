package cn.tellyouwhat.checkinsystem.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.EncryptUtil;
import cn.tellyouwhat.checkinsystem.utils.SPUtil;

/**
 * A login screen that offers login via number/password.
 */
public class LoginActivity extends BaseActivity {

	private final String TAG = "LoginActivity";

	private EditText mNumberView;
	private EditText mPasswordView;
	private View mProgressView;
	//	private View mLoginFormView;
	private SharedPreferences mSharedPreferences;
	//	private CheckBox mCheckBox_rememberPassword;
//	private CheckBox mCheckbox_auto_login;
	private boolean needToShowTabbedActivity;

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
		Log.i(TAG, "forgetPassword: " + phoneNumberTOBE);
		startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		StatusBarUtil.setTransparent(this);

		needToShowTabbedActivity = getIntent().getBooleanExtra(ConstantValues.FIRST_TIME_AFTER_UPGRADE, true);
		// Set up the login form.
		mNumberView = (EditText) findViewById(R.id.number);
		mPasswordView = (EditText) findViewById(R.id.password);


		Button mNumberSignInButton = (Button) findViewById(R.id.number_sign_in_button);
/*		mCheckBox_rememberPassword = (CheckBox) findViewById(R.id.checkbox_remember_password);
		mCheckbox_auto_login = (CheckBox) findViewById(R.id.checkbox_auto_login);

		mCheckbox_auto_login.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mCheckBox_rememberPassword.setChecked(true);
				}
			}
		});
		mCheckBox_rememberPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					mCheckbox_auto_login.setChecked(false);
				}
			}
		});
		mLoginFormView = findViewById(R.id.login_form);*/
		mProgressView = findViewById(R.id.login_progress);
		mNumberSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});
		//获得sp实例对象
		mSharedPreferences = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		//如果登陆过，直接登录
		mNumberView.setText(mSharedPreferences.getString("USER_NAME", ""));
/*		if (mSharedPreferences.getBoolean("REMEMBER_CHECKBOX_STATUS", false)) {
			mCheckBox_rememberPassword.setChecked(true);
			mPasswordView.setText(EncryptUtil.decryptBase64withSalt(mSharedPreferences.getString("PASSWORD", ""), "saltforcheckinsystemstorepasswordininnerstorage"));
			if (mSharedPreferences.getBoolean("AUTO_LOGIN", false)) {
				mCheckbox_auto_login.setChecked(true);
//				Log.d(TAG, "onCreate: before attemptLogin");
				attemptLogin();
			} else {
				Log.d(TAG, "onCreate: AUTO_LOGIN 失败");
			}

		}*/

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
		final String number = mNumberView.getText().toString();
		final String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;
		if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else {
			// Check for a valid password, if the user entered one.
			if (!isPasswordValid(password)) {
				mPasswordView.setError(getString(R.string.error_invalid_password));
				focusView = mPasswordView;
				cancel = true;
			}
		}
		// Check for a valid Number address.
		if (TextUtils.isEmpty(number)) {
			mNumberView.setError(getString(R.string.error_field_required));
			focusView = mNumberView;
			cancel = true;
		} else if (!isNumberValid(number)) {
			mNumberView.setError(getString(R.string.error_invalid_number));
			focusView = mNumberView;
			cancel = true;
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
					SystemClock.sleep(1000);
					login(number, password);
				}
			}).start();

		}
	}

	private boolean isNumberValid(String number) {
		return number.length() > 5;
	}

	private boolean isPasswordValid(String password) {
		return password.length() >= 6 && password.length() <= 18;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

//		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//		mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//				show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//			@Override
//			public void onAnimationEnd(Animator animation) {
//				mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//			}
//		});

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

		//服务端通过检测phoneNumber和jobNumber是否为0来判断用户传的是手机号还是工号
		String phoneNumber = "0";
		String jobNumber = "0";
		if (number.length() == 11) {
			phoneNumber = number;
		} else if (number.length() == 10) {
			jobNumber = number;
		}

		//通过加盐的MD5算法加密密码，以确保传输过程的安全性
		String encryptedPassword = EncryptUtil.md5WithSalt(password, "saltforcheckinsystemstorepasswordinserverdatabase");

		//将用户名密码封装成json对象，发送出去
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("PhoneNumber", phoneNumber);
			jsonObject.put("JobNumber", jobNumber);
			jsonObject.put("Password", encryptedPassword);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Log.w(TAG, "validate: " + jsonObject);

		//开始准备数据
		RequestParams params = new RequestParams("http://tellyouwhat.cn/Login_validate/login");
//		RequestParams params = new RequestParams("http://127.0.0.1:8080/Login_validate/login");
		params.setAsJsonContent(true);
		params.setBodyContent(jsonObject.toString());

		//利用xUtils3post提交
		final String finalJobNumber = jobNumber;
		final String finalPhoneNumber = phoneNumber;
		x.http().post(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {

				//获取update.json成功的回调
				Log.i(TAG, "onSuccess: " + result);
				if (!"success".equals(result)) {
					if ("no match".equals(result)) {
						showProgress(false);
						Log.d(TAG, "login: Failed");
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mPasswordView.setText("");
							}
						});
						mPasswordView.setError(getString(R.string.error_incorrect_password));
						mPasswordView.requestFocus();
					} else if ("Database Connect failed".equals(result)) {
						Snackbar.make(findViewById(R.id.login_form), R.string.server_busy, Snackbar.LENGTH_LONG).show();
					} else if ("Illegal number".equals(result)) {
						showProgress(false);
						Log.d(TAG, "login: Failed, wrong user name");
						mNumberView.setError(getString(R.string.Invalide_phone_jobnumber));
						mNumberView.requestFocus();
						editor.putString("USER_NAME", "");
						editor.apply();
					}

				} else {
					Log.d(TAG, "onPostExecute: 登录成功");

					editor.putString("USER_NAME", number);
					editor.apply();
//TODO 继续写逻辑
					initGuidancePages();
//					 Activity跳转提交数据
					gotoMain(finalJobNumber, finalPhoneNumber);
					finish();
				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				Log.w(TAG, "onError: " + ex);
				showProgress(false);
				initGuidancePages();
				//TODO 服务器搭好之后注释掉下面这句
				gotoMain("0", "0");

				Snackbar.make(findViewById(R.id.login_form), "网络开小差了~~", Snackbar.LENGTH_LONG).show();
			}

			@Override
			public void onCancelled(CancelledException cex) {
				Log.w(TAG, "onCancelled: cex");
				showProgress(false);
				Snackbar.make(findViewById(R.id.login_form), "服务器正忙，请稍候重试", Snackbar.LENGTH_LONG).show();
			}

			@Override
			public void onFinished() {
				Log.w(TAG, "onFinished: ");
			}
		});
	}

	private void gotoMain(String finalJobNumber, String finalPhoneNumber) {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		if (!"0".equals(finalJobNumber)) {
			intent.putExtra("JOB_NUMBER", finalJobNumber);
		}
		if (!"0".equals(finalPhoneNumber)) {
			intent.putExtra("PHONE_NUMBER", finalPhoneNumber);
		}
		startActivity(intent);
	}

	private void initGuidancePages() {
		SPUtil spUtil = new SPUtil(this);
		boolean isFirstTimeAfterUpgrade = spUtil.getBoolean(ConstantValues.FIRST_TIME_AFTER_UPGRADE, true);
		if (isFirstTimeAfterUpgrade && needToShowTabbedActivity) {
			Intent intent = new Intent(this, TabbedActivity.class);
			startActivity(intent);
			finish();
		}
	}

}

