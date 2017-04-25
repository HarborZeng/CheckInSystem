package cn.tellyouwhat.checkinsystem.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.http.cookie.DbCookieStore;
import org.xutils.x;

import java.net.HttpCookie;
import java.util.List;

import cn.tellyouwhat.checkinsystem.utils.ConstantValues;
import cn.tellyouwhat.checkinsystem.utils.EncryptUtil;
import cn.tellyouwhat.checkinsystem.utils.ReLoginUtil;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Harbor-Laptop on 2017/3/28.
 * Base
 */

public class BaseFragment extends Fragment {
	private Activity mActivity;
	private AppCompatActivity mAppCompatActivity;
	private Context context;

	//解决Fragment可能出现的重叠问题
	private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);

			FragmentTransaction ft = getFragmentManager().beginTransaction();
			if (isSupportHidden) {
				ft.hide(this);
			} else {
				ft.show(this);
			}
			ft.commit();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		this.context = context;
	}

	public void updateSession() {
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userInfo", MODE_PRIVATE);
		String userName = sharedPreferences.getString("USER_NAME", "");
		String encryptedToken = sharedPreferences.getString(ConstantValues.TOKEN, "");
		String token = EncryptUtil.decryptBase64withSalt(encryptedToken, ConstantValues.SALT);
		if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(token)) {
			RequestParams p = new RequestParams("http://api.checkin.tellyouwhat.cn/User/UpdateSession?username=" + userName + "&deviceid=" + Build.SERIAL + "&token=" + token);
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
									Log.i("在BaseFragment里面", "onSuccess: session 已经更新");
									break;
								}
							}
							break;
						case 0:
							ReLoginUtil reLoginUtil = new ReLoginUtil(getActivity());
							try {
								Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_SHORT).show();
							} catch (JSONException e) {
								e.printStackTrace();
							}
							reLoginUtil.reLoginWithAlertDialog();
							break;
						case -1:
							Toast.makeText(x.app(), "发生了不可描述的错误009", Toast.LENGTH_SHORT).show();
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
			ReLoginUtil reLoginUtil = new ReLoginUtil(getActivity());
			reLoginUtil.reLoginWithAlertDialog();
		}
	}
}
