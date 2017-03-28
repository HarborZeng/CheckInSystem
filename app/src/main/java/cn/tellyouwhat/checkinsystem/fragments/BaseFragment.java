package cn.tellyouwhat.checkinsystem.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by Harbor-Laptop on 2017/3/28.
 * Base
 */

public class BaseFragment extends Fragment {

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
}
