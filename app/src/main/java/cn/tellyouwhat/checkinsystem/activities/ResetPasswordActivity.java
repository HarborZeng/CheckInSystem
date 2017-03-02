package cn.tellyouwhat.checkinsystem.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cn.tellyouwhat.checkinsystem.R;

/**
 * Created by Harbor-Laptop on 2017/3/1.
 *
 * @author HarborZeng
 */

public class ResetPasswordActivity extends BaseActivity {

	private static final String TAG = "ResetPasswordActivity";
	private EditText mPhoneNumber;
	private EditText mVerificationCode;
	private Button mSendVarificationCode;
	private Button mFindPSW;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_psw);

		mPhoneNumber = (EditText) findViewById(R.id.editText_phoneNumber);
		mVerificationCode = (EditText) findViewById(R.id.editText_verification_code);
		mSendVarificationCode = (Button) findViewById(R.id.button_send_verification_code);
		mFindPSW = (Button) findViewById(R.id.button_findPSW);

		Intent intent = getIntent();
		String phoneNumber = intent.getStringExtra("PhoneNumber");
		if (phoneNumber != null) {
			mPhoneNumber.setText(phoneNumber);
		}

	}

	public void sendVerificationCode(View view) {
		mSendVarificationCode.setClickable(false);
		String phoneNumber = mPhoneNumber.getText().toString();
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 60; i > 0; i--) {
//					Log.i(TAG, "sendVerificationCode: "+i);
					final int finalI = i;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mSendVarificationCode.setText(finalI + "");
						}
					});
					SystemClock.sleep(1000);
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mSendVarificationCode.setText("获取验证码");
						mSendVarificationCode.setClickable(true);
					}
				});
			}
		}).start();

		//向短信验证码服务器提交手机号码
		//。。。。。。。。。。


	}

	public void findPSW(View view) {
		String phoneNumber = mPhoneNumber.getText().toString();
		String verificatiuonCode = mVerificationCode.getText().toString();

	}
}
