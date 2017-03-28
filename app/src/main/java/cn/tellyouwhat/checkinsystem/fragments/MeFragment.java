package cn.tellyouwhat.checkinsystem.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.tellyouwhat.checkinsystem.R;

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

		Intent intent = getActivity().getIntent();
		String job_number = intent.getStringExtra("JOB_NUMBER");
		String phone_number = intent.getStringExtra("PHONE_NUMBER");
		if (job_number != null) {
			job_number_textView.setText(job_number);
		}


		return view;
	}

	public static MeFragment newInstance() {
		MeFragment fragment = new MeFragment();
		fragment.setRetainInstance(true);
		return fragment;
	}
}
