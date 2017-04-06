package cn.tellyouwhat.checkinsystem.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.EncryptUtil;

/**
 * Created by Harbor-Laptop on 2017/3/30.
 * 重置密码的Fragment
 */

public class UpdatePasswordFragment extends Fragment implements ISlidePolicy {

	private EditText mEditTextNewPassword;
	private EditText mEditTextOneMoreTimePassword;
	private EditText mVerificationCode;
	private boolean isUpdatePasswordSucceed = false;
	private View view;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_update_password, container, false);
		mEditTextNewPassword = (EditText) view.findViewById(R.id.editText_new_password);
		mEditTextOneMoreTimePassword = (EditText) view.findViewById(R.id.editText_one_more_time_new_password);
		mVerificationCode = (EditText) view.findViewById(R.id.editText_SMS_verification_code);
		YoYo.with(Techniques.BounceInDown)
				.duration(1400)
				.repeat(1)
				.playOn(view.findViewById(R.id.textView_SMS_has_been_send));
		return view;
	}

	@Override
	public boolean isPolicyRespected() {
		return isUpdatePasswordSucceed;
	}

	@Override
	public void onUserIllegallyRequestedNextPage() {
		String againPass = mEditTextOneMoreTimePassword.getText().toString().trim();
		String newPass = mEditTextNewPassword.getText().toString().trim();
		String verificationCode = mVerificationCode.getText().toString().trim();
		if (TextUtils.isEmpty(verificationCode)) {
			mVerificationCode.requestFocus();
			mVerificationCode.setError("请先输入短信验证码");
			YoYo.with(Techniques.Tada)
					.duration(700)
					.repeat(1)
					.playOn(mVerificationCode);
		} else if (TextUtils.isEmpty(newPass)) {
			mEditTextNewPassword.requestFocus();
			mEditTextNewPassword.setError("您还没有输入密码");
			YoYo.with(Techniques.Tada)
					.duration(700)
					.repeat(1)
					.playOn(view.findViewById(R.id.editText_new_password));
		} else if (newPass.length() < 6) {
			mEditTextNewPassword.requestFocus();
			mEditTextNewPassword.setError("密码必须大于等于6位");
			mEditTextNewPassword.setText("");
			mEditTextOneMoreTimePassword.setText("");
			YoYo.with(Techniques.Tada)
					.duration(700)
					.repeat(1)
					.playOn(view.findViewById(R.id.editText_new_password));
		} else if (!TextUtils.isEmpty(newPass) && newPass.equals(againPass)) {
			String encryptedPassword = EncryptUtil.md5WithSalt(newPass, ConstantValues.SALT);
			String url = "http://api.checkin.tellyouwhat.cn/user/UpdatePassword?code=" + verificationCode + "&newpass=" + encryptedPassword;
			RequestParams params = new RequestParams(url);
			x.http().get(params, new Callback.CommonCallback<JSONObject>() {
				@Override
				public void onSuccess(JSONObject result) {
					try {
						int success = result.getInt("result");
						if (success == 1) {
							isUpdatePasswordSucceed = true;
							Toast.makeText(getActivity(), "重置密码成功，再次点击前进按钮继续", Toast.LENGTH_SHORT).show();
						} else if (success == -1) {
							isUpdatePasswordSucceed = false;
							Toast.makeText(getActivity(), "发生了可怕的错误，代码：004，我们正在抢修", Toast.LENGTH_SHORT).show();
						} else if (success == -2) {
							isUpdatePasswordSucceed = false;
							Toast.makeText(getActivity(), "验证码错误", Toast.LENGTH_LONG).show();
							mVerificationCode.setText("");
							mVerificationCode.requestFocus();
							mVerificationCode.setError("请重新输入");
							YoYo.with(Techniques.Tada)
									.duration(700)
									.repeat(1)
									.playOn(mVerificationCode);
						} else {
							isUpdatePasswordSucceed = false;
							Toast.makeText(getActivity(), "验证码超时，请返回重试", Toast.LENGTH_SHORT).show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {
					isUpdatePasswordSucceed = false;
					Snackbar.make(view.findViewById(R.id.reset_password_step_two_framelayout), "重置密码出错", Snackbar.LENGTH_SHORT).show();
				}

				@Override
				public void onCancelled(CancelledException cex) {

				}

				@Override
				public void onFinished() {

				}
			});
		} else {
			mEditTextOneMoreTimePassword.requestFocus();
			mEditTextOneMoreTimePassword.setError("密码输入不一致");
			YoYo.with(Techniques.Tada)
					.duration(700)
					.repeat(1)
					.playOn(view.findViewById(R.id.editText_one_more_time_new_password));
		}
	}
}
