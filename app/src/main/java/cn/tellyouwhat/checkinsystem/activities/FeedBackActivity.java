package cn.tellyouwhat.checkinsystem.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.jaeger.library.StatusBarUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.utils.PhoneInfoProvider;

/**
 * Created by Harbor-Laptop on 2017/4/9.
 * FeedBack Page based on fangtang <a href="http://sc.ftqq.com/3.version">Server酱</a>
 * which will push a WeChat message about what users had inputted,
 *  note that you must use {@link URLEncoder} to encode the text user inputted, otherwise
 *  you will get the message partly, like space will be looked as an end signal.
 *  user's message will terminally transformed like "E%89%E5%B8%82%E9%95%B"
 * @author HarborZeng
 */

public class FeedBackActivity extends BaseActivity {
	private static final String TAG = "FeedBackActivity";
	private EditText mContactInformationEditText;
	private EditText mFeedBackEditText;
	private ProgressBar mFeedBackProgressBar;
	private CardView mFeedBackCardView;
	private AlphaAnimation mAnimationIn = new AlphaAnimation(0f, 1f);
	private AlphaAnimation mAnimationOut = new AlphaAnimation(1f, 0f);
	private CheckBox mUploadPhoneInfoCheckBox;
	private String mAllPhoneInfo = "无设备信息";

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_feedback);
		setUpActionBar();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPreferences.getBoolean("immersed_status_bar_enabled", true)) {
			StatusBarUtil.setColor(FeedBackActivity.this, ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
		}
		mContactInformationEditText = (EditText) findViewById(R.id.contact_information_edit_text);
		mFeedBackEditText = (EditText) findViewById(R.id.feedback_edit_text);
		mFeedBackProgressBar = (ProgressBar) findViewById(R.id.feedback_summit_progress);
		mFeedBackCardView = (CardView) findViewById(R.id.feedback_summit_bg);
		mUploadPhoneInfoCheckBox = (CheckBox) findViewById(R.id.checkbox_upload_phone_info);
		mFeedBackProgressBar.setVisibility(View.INVISIBLE);
		mFeedBackCardView.setVisibility(View.INVISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.feedback_actionbar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.item_submit:
				showProgress(true);
				synchronized (FeedBackActivity.class) {
					if (mUploadPhoneInfoCheckBox.isChecked()) {
						mAllPhoneInfo = PhoneInfoProvider.getInstance().getAllInfo(getApplicationContext());
					}
				}
				sendFeedBack();
				return true;
			default:
				return false;
		}
	}

	private void sendFeedBack() {
		String encodedContactInfo = "";
		String encodedFeedbackText = "";
		try {
			encodedContactInfo = URLEncoder.encode(mContactInformationEditText.getText().toString(), "UTF-8");
			encodedFeedbackText = URLEncoder.encode(mFeedBackEditText.getText().toString() + "\n", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
		String userName = sharedPreferences.getString("USER_NAME", "");
		if (!TextUtils.isEmpty(encodedFeedbackText)) {
			RequestParams requestParams = null;
			try {
				String userInputContact = (TextUtils.isEmpty(encodedContactInfo) ? URLEncoder.encode("匿名", "UTF-8") : encodedContactInfo);
				requestParams = new RequestParams("http://sc.ftqq.com/SCU6693Tdfc142ce95a8a9fcfbbb14f587cbdf4258c9c7a088af6.send?text=" + userInputContact + URLEncoder.encode(", 真实信息: " + userName, "UTF-8") + "&desp=" + encodedFeedbackText + URLEncoder.encode(mAllPhoneInfo, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {
				@Override
				public void onSuccess(JSONObject result) {
					int errno = 0;
					Log.i(TAG, "onSuccess: result=" + result);
					try {
						errno = result.getInt("errno");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					switch (errno) {
						case 0:
							Toast.makeText(FeedBackActivity.this, "反馈成功，谢谢", Toast.LENGTH_LONG).show();
							break;
						case 1024:
							try {
								Toast.makeText(FeedBackActivity.this, result.getString("errmsg"), Toast.LENGTH_LONG).show();
							} catch (JSONException e) {
								e.printStackTrace();
							}
							break;
						default:
							Toast.makeText(FeedBackActivity.this, "其他情况", Toast.LENGTH_LONG).show();
					}
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {
					Toast.makeText(FeedBackActivity.this, "反馈出错，请稍候再试", Toast.LENGTH_LONG).show();
					ex.printStackTrace();
				}

				@Override
				public void onCancelled(CancelledException cex) {

				}

				@Override
				public void onFinished() {
					showProgress(false);
				}
			});
		} else {
			showProgress(false);
			mFeedBackEditText.requestFocus();
			mFeedBackEditText.setError("内容不能为空");
			YoYo.with(Techniques.Tada)
					.duration(700)
					.repeat(1)
					.playOn(mFeedBackEditText);
		}
	}

	private void showProgress(boolean show) {
		mAnimationIn.setDuration(500);
		mAnimationOut.setDuration(500);
		mFeedBackProgressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mFeedBackCardView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mFeedBackProgressBar.startAnimation(show ? mAnimationIn : mAnimationOut);
		mFeedBackCardView.startAnimation(show ? mAnimationIn : mAnimationOut);
	}

	private void setUpActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle("意见反馈");
		}
	}
}
