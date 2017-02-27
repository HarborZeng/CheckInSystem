package cn.tellyouwhat.checkinsystem.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.util.MD5;
import org.xutils.http.RequestParams;
import org.xutils.x;

import cn.tellyouwhat.checkinsystem.R;

/**
 * A login screen that offers login via number/password.
 */
public class LoginActivity extends BaseActivity {

	private final String TAG = "LoginActivity";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */

	// UI references.
	private EditText mNumberView;
	private EditText mPasswordView;
	private View mProgressView;
	private View mLoginFormView;
	private SharedPreferences mSharedPreferences;
	private CheckBox mCheckBox_rememberPassword;
	private CheckBox mCheckbox_auto_login;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// Set up the login form.
		mNumberView = (EditText) findViewById(R.id.number);
		mPasswordView = (EditText) findViewById(R.id.password);
		Button mNumberSignInButton = (Button) findViewById(R.id.number_sign_in_button);
		mCheckBox_rememberPassword = (CheckBox) findViewById(R.id.checkbox_remember_password);
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
		mLoginFormView = findViewById(R.id.login_form);
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
		if (mSharedPreferences.getBoolean("REMEMBER_CHECKBOX_STATUS", false)) {
			mCheckBox_rememberPassword.setChecked(true);
			mPasswordView.setText(new String(Base64.decode(mSharedPreferences.getString("PASSWORD", "").getBytes(), Base64.DEFAULT)));
			if (mSharedPreferences.getBoolean("AUTO_LOGIN", false)) {
				mCheckbox_auto_login.setChecked(true);
//				Log.d(TAG, "onCreate: before attemptLogin");
				attemptLogin();
			} else {
				Log.d(TAG, "onCreate: AUTO_LOGIN 失败");
			}

		}

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
		String number = mNumberView.getText().toString();
		String password = mPasswordView.getText().toString();

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
			login(number, password);
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

		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		mLoginFormView.animate().setDuration(shortAnimTime).alpha(
				show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			}
		});

		mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
		mProgressView.animate().setDuration(shortAnimTime).alpha(
				show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		});

	}


	private void login(final String number, final String password) {
		final SharedPreferences.Editor editor = mSharedPreferences.edit();

		String encryptedPassword = MD5.md5(password);
		Log.i(TAG, "doInBackground: password is encrypted: " + encryptedPassword);

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("Username", number);
			jsonObject.put("Password", encryptedPassword);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Log.d(TAG, "validate: " + jsonObject);

		RequestParams params = new RequestParams("http://tellyouwhat.cn/Login_validate/login");
//		RequestParams params = new RequestParams("http://127.0.0.1:8080/Login_validate/login");
		params.setAsJsonContent(true);
		params.setBodyContent(jsonObject.toString());


		x.http().post(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				Log.i(TAG, "onSuccess: " + result);
				if ("success".equals(result)) {
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
					editor.putBoolean("REMEMBER_CHECKBOX_STATUS", false);
					editor.putString("PASSWORD", "");
					editor.apply();
				} else {
					Log.d(TAG, "onPostExecute: 登录成功");

					editor.putString("USER_NAME", number);
					if (mCheckbox_auto_login.isChecked()) {
						editor.putBoolean("AUTO_LOGIN", true);
					} else {
						editor.putBoolean("AUTO_LOGIN", false);
					}
					//记住用户名、密码、
					if (mCheckBox_rememberPassword.isChecked()) {
						String encryptPassword = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
						editor.putString("PASSWORD", encryptPassword);
						editor.putBoolean("REMEMBER_CHECKBOX_STATUS", true);
					} else {
						editor.putString("PASSWORD", "");
						editor.putBoolean("REMEMBER_CHECKBOX_STATUS", false);
					}
					editor.apply();

					// Activity跳转
					Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
					intent.putExtra("job_number", number);
					startActivity(intent);
					finish();
				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				Log.w(TAG, "onError: " + ex);
				showProgress(false);
			}

			@Override
			public void onCancelled(CancelledException cex) {
				Log.w(TAG, "onCancelled: cex");
				showProgress(false);
			}

			@Override
			public void onFinished() {
				Log.w(TAG, "onFinished: ");
			}
		});
	}
}

