package cn.tellyouwhat.checkinsystem.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.xutils.http.RequestParams;
import org.xutils.http.app.ParamsBuilder;
import org.xutils.x;


/**
 * Created by Harbor-Laptop on 2017/4/10
 *
 * @author HarborZeng inspired <a href="https://github.com/wyouflf/xUtils3/issues/125">这里</a>
 *         带有从{@link SharedPreferences}的请求参数类
 */

public class CookiedRequestParams extends RequestParams {
	public CookiedRequestParams() {
	}

	public CookiedRequestParams(String url) {
		super(url);
		setUseCookie(false);
		SharedPreferences sharedPreferences = x.app().getSharedPreferences(ConstantValues.COOIKE_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
		String cookie = sharedPreferences.getString(ConstantValues.cookie, "");
		if (!TextUtils.isEmpty(cookie)) {
			Log.i("cookie", "CookiedRequestParams: cookie is " + cookie);
			addHeader("Cookie", ConstantValues.COOKIE_NAME + "=" + cookie);
		}
	}

	public CookiedRequestParams(String uri, ParamsBuilder builder, String[] signs, String[] cacheKeys) {
		super(uri, builder, signs, cacheKeys);
	}
}
