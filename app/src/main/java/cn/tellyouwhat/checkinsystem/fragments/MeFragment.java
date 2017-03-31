package cn.tellyouwhat.checkinsystem.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.activities.SettingsActivity;
import cn.tellyouwhat.checkinsystem.utils.ConstantValues;

/**
 * Created by Harbor-Laptop on 2017/3/4.
 *
 * @author HarborZeng
 *         Me Fragment for MainActivity
 */

public class MeFragment extends BaseFragment {
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_me, container, false);

		TextView userName_textView = (TextView) view.findViewById(R.id.user_name);
		TextView job_number_textView = (TextView) view.findViewById(R.id.job_number);
		Button settingsButton = (Button) view.findViewById(R.id.button_settings);

		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), SettingsActivity.class));
			}
		});

		Intent intent = getActivity().getIntent();
		String job_number = intent.getStringExtra("JOB_NUMBER");
		String phone_number = intent.getStringExtra("PHONE_NUMBER");
		if (job_number != null) {
			job_number_textView.setText(job_number);
		}

		String token = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString(ConstantValues.TOKEN, "");
		if (!TextUtils.isEmpty(token)) {
			//TODO api地址待修改
			RequestParams params = new RequestParams("http://api.checkin.tellyouwhat.cn/uploadHead");
			params.setMultipart(true);
			params.addBodyParameter("token", token);
			//TODO id
			params.addBodyParameter("id", "");
			//TODO Head Image Placed Here
			File img = new File("");
			params.addBodyParameter("file", img, "multipart/form-data"); // 如果文件没有扩展名, 最好设置contentType参数.
			x.http().post(params, new Callback.CommonCallback<JSONObject>() {
				@Override
				public void onSuccess(JSONObject result) {

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

		return view;
	}

	public void goSettings(View view) {

	}

	public static MeFragment newInstance() {
		MeFragment fragment = new MeFragment();
		fragment.setRetainInstance(true);
		return fragment;
	}
}
