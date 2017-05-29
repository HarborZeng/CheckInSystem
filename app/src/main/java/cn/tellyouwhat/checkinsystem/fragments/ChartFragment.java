package cn.tellyouwhat.checkinsystem.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.tellyouwhat.checkinsystem.R;

/**
 * Created by Harbor-Laptop on 2017/5/23.
 * 图表以后都展示在这里
 */

public class ChartFragment extends Fragment {
	private static ChartFragment instance;

	public static ChartFragment newInstance() {

		Bundle args = new Bundle();

		if (instance == null) {
			synchronized (ChartFragment.class) {
				if (instance == null) {
					instance = new ChartFragment();
					instance.setArguments(args);
				}
			}
		}
		return instance;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_chart, container, false);

		return rootView;
	}

}
