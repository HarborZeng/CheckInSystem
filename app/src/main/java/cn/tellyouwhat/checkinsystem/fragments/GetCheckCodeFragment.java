package cn.tellyouwhat.checkinsystem.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.paolorotolo.appintro.ISlidePolicy;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import cn.tellyouwhat.checkinsystem.R;

/**
 * Created by Harbor-Laptop on 2017/3/30.
 * 找回密码第一屏
 */

public class GetCheckCodeFragment extends Fragment implements ISlidePolicy {

	private static final String TAG = "ResetPasswordActivity";
	private EditText mPhoneNumber;
	private EditText mCheckCodeEditText;
	private ImageView mCheckCodeImageView;
	private View view;
	private String phoneNumber;
	private String userInput;
	private boolean isCorrectCode = false;


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_get_checkcode, container, false);
		requestCheckCode();
		mPhoneNumber = (EditText) view.findViewById(R.id.editText_phoneNumber);
		mCheckCodeImageView = (ImageView) view.findViewById(R.id.imageview_check_code);
		mCheckCodeEditText = (EditText) view.findViewById(R.id.editText_check_code);

		mPhoneNumber.requestFocus();
		Intent intent = getActivity().getIntent();
		phoneNumber = intent.getStringExtra("PhoneNumber");
		if (!TextUtils.isEmpty(phoneNumber)) {
			mPhoneNumber.setText(phoneNumber);
			mCheckCodeEditText.requestFocus();
		}

/*					new Thread(new Runnable() {
						@Override
						public void run() {
							for (int i = 60; i > 0; i--) {
//					Log.i(TAG, "sendVerificationCode: "+i);
								final int finalI = i;
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										mNextStepOfGetCheckCode.setText(finalI + "");
										mNextStepOfGetCheckCode.setBackgroundColor(getResources().getColor(R.color.caldroid_333));
									}
								});
								SystemClock.sleep(1000);
							}
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mNextStepOfGetCheckCode.setText("获取验证码");
									mNextStepOfGetCheckCode.setClickable(true);
								}
							});
						}
					}).start();*/

		//向短信验证码服务器提交手机号码
		//。。。。。。。。。。

		mCheckCodeImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestCheckCode();
			}
		});

		return view;
	}

	private void requestCheckCode() {
		RequestParams params = new RequestParams("http://api.checkin.tellyouwhat.cn/user/GetCheckCode");
		params.setUseCookie(true);
		params.setMultipart(true);
		x.http().get(params, new Callback.CommonCallback<JSONObject>() {

			private int resultInt = 0;

			@Override
			public void onSuccess(JSONObject result) {
				try {
					resultInt = result.getInt("result");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (resultInt == 1) {
					String imageString = null;
					try {
						imageString = result.getString("image");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Log.d(TAG, "onSuccess: " + resultInt + ", image:　" + imageString);
					byte[] imageByte = imageString != null ? imageString.getBytes() : new byte[0];
					byte[] decodeImage = Base64.decode(imageByte, Base64.DEFAULT);
					Bitmap bitmap = BitmapFactory.decodeByteArray(decodeImage, 0, decodeImage.length);
					mCheckCodeImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
					mCheckCodeImageView.setScaleX(3f);
					mCheckCodeImageView.setScaleY(3f);
					mCheckCodeImageView.setImageBitmap(bitmap);
				} else if (resultInt == -1) {
					Toast.makeText(getActivity(), "发生了可怕的错误，代码：002，我们正在抢修", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				Snackbar.make(view.findViewById(R.id.reset_password_step_one_framelayout), "获取验证码出错", Snackbar.LENGTH_SHORT).show();
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}
		});
	}


	@Override
	public boolean isPolicyRespected() {
		return isCorrectCode;
	}

	@Override
	public void onUserIllegallyRequestedNextPage() {
		if (TextUtils.isEmpty(mPhoneNumber.getText().toString().trim())) {
			mPhoneNumber.setError(getString(R.string.input_phonenumber));
			mPhoneNumber.requestFocus();
			YoYo.with(Techniques.Tada)
					.duration(700)
					.repeat(1)
					.playOn(view.findViewById(R.id.linearLayout_input_phone_nuber));
		} else if (TextUtils.isEmpty(mCheckCodeEditText.getText().toString().trim())) {
			mCheckCodeEditText.setError("请输入图片上的字母！");
			mCheckCodeEditText.requestFocus();
			YoYo.with(Techniques.Tada)
					.duration(700)
					.repeat(1)
					.playOn(view.findViewById(R.id.textInputLayoutPassword));
		} else {
			phoneNumber = mPhoneNumber.getText().toString().trim();
			userInput = mCheckCodeEditText.getText().toString().trim();
			String requestURL = "http://api.checkin.tellyouwhat.cn/User/SendSMS?checkcode=" + userInput + "&phonenumber=" + phoneNumber;

			RequestParams requestParams = new RequestParams(requestURL);
			Log.d(TAG, "onUserIllegallyRequestedNextPage: 请求的链接：" + requestURL);
			x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {
				@Override
				public void onSuccess(JSONObject result) {
					try {
						int successCode = result.getInt("result");
						if (successCode == 1) {
							isCorrectCode = true;
							Toast.makeText(getActivity(), "验证码正确，再次点击前进按钮继续", Toast.LENGTH_SHORT).show();
						} else if (successCode == -1) {
							Toast.makeText(getActivity(), "发生了可怕的错误，代码：003，我们正在抢修", Toast.LENGTH_SHORT).show();
						} else {
							isCorrectCode = false;
							Toast.makeText(getActivity(), result.getString("message"), Toast.LENGTH_SHORT).show();
							mCheckCodeEditText.setText("");
							YoYo.with(Techniques.Tada)
									.duration(700)
									.repeat(1)
									.playOn(view.findViewById(R.id.textInputLayoutPassword));
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {
					isCorrectCode = false;
					Snackbar.make(view.findViewById(R.id.reset_password_step_one_framelayout), "验证验证码出错", Snackbar.LENGTH_SHORT).show();
					mCheckCodeEditText.setText("");
				}

				@Override
				public void onCancelled(CancelledException cex) {

				}

				@Override
				public void onFinished() {
//					isCorrectCode = true;
					requestCheckCode();
				}
			});
		}
	}

}
