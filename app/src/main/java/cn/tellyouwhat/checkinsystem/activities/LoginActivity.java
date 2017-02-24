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
import android.support.v7.app.AppCompatActivity;
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

import cn.tellyouwhat.checkinsystem.R;

/**
 * A login screen that offers login via number/password.
 */
public class LoginActivity extends AppCompatActivity {

	private final String TAG = "LoginActivity";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// UI references.
	private EditText mNumberView;
	private EditText mPasswordView;
	private View mProgressView;
	private View mLoginFormView;
	private SharedPreferences sp;
	private CheckBox checkBox_rememberPassword;
	private CheckBox checkbox_auto_login;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// Set up the login form.
		mNumberView = (EditText) findViewById(R.id.number);
		mPasswordView = (EditText) findViewById(R.id.password);
		Button mNumberSignInButton = (Button) findViewById(R.id.number_sign_in_button);
		checkBox_rememberPassword = (CheckBox) findViewById(R.id.checkbox_remember_password);
		checkbox_auto_login = (CheckBox) findViewById(R.id.checkbox_auto_login);

		checkbox_auto_login.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					checkBox_rememberPassword.setChecked(true);
				}
			}
		});
		checkBox_rememberPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(!isChecked){
					checkbox_auto_login.setChecked(false);
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
		sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		//如果登陆过，直接登录
		mNumberView.setText(sp.getString("USER_NAME", ""));
		if (sp.getBoolean("REMEMBER_CHECKBOX_STATUS", false)) {
			checkBox_rememberPassword.setChecked(true);
			mPasswordView.setText(new String(Base64.decode(sp.getString("PASSWORD", "").getBytes(), Base64.DEFAULT)));
			if (sp.getBoolean("AUTO_LOGIN", false)) {
				checkbox_auto_login.setChecked(true);
				Log.d(TAG, "onCreate: before attemptLogin");
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
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mNumberView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String number = mNumberView.getText().toString();
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;
		if(TextUtils.isEmpty(password)){
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}else {
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
			mAuthTask = new UserLoginTask(number, password);
			mAuthTask.execute((Void) null);
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


	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

		private final String mNumber;
		private final String mPassword;
		SharedPreferences.Editor editor = sp.edit();

		UserLoginTask(String number, String password) {
			mNumber = number;
			mPassword = password;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			SystemClock.sleep(1500);
			if ("2015111123".equals(mNumber) && "w499759".equals(mPassword)) {
				return true;
			} else {
				Log.d(TAG, "login: Failed");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mPasswordView.setText("");
					}
				});
				editor.putBoolean("REMEMBER_CHECKBOX_STATUS", false);
				editor.putString("PASSWORD", "");
				editor.apply();
				return false;
			}
			// TODO: register the new account here.
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				Log.d(TAG, "onPostExecute: 登录成功");
				Message msg = Message.obtain();
				uiHandler.sendMessage(msg);
				finish();

			} else {

				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		private Handler uiHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				editor.putString("USER_NAME", mNumber);
				if (checkbox_auto_login.isChecked()) {
					editor.putBoolean("AUTO_LOGIN", true);
				} else {
					editor.putBoolean("AUTO_LOGIN", false);
				}
				//记住用户名、密码、
				if (checkBox_rememberPassword.isChecked()) {
					String encryptPassword = Base64.encodeToString(mPassword.getBytes(), Base64.DEFAULT);
					editor.putString("PASSWORD", encryptPassword);
					editor.putBoolean("REMEMBER_CHECKBOX_STATUS", true);
				} else {
					editor.putString("PASSWORD", "");
					editor.putBoolean("REMEMBER_CHECKBOX_STATUS", false);
				}
				editor.apply();

				// Activity跳转
				Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
				startActivity(intent);
			}
		};

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}

