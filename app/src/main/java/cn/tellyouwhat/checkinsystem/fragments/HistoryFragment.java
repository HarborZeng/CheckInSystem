package cn.tellyouwhat.checkinsystem.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.roomorama.caldroid.CaldroidFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import cn.tellyouwhat.checkinsystem.R;

/**
 * Created by Harbor-Laptop on 2017/3/1.
 *
 * @author HarborZeng
 */

public class HistoryFragment extends BaseFragment {

	private CalendarView calendar_checkin_history;
	private Context mContext;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_history, container, false);

		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		CaldroidFragment caldroidFragment = new CaldroidFragment();
		Bundle args = new Bundle();
		Calendar cal = Calendar.getInstance();
		args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
		args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
		args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
		args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);

		// Uncomment this to customize startDayOfWeek
		args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
				CaldroidFragment.MONDAY);

		// Uncomment this line to use Caldroid in compact mode
		// args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);

		// Uncomment this line to use dark theme
//            args.putInt(CaldroidFragment.THEME_RESOURCE, com.caldroid.R.style.CaldroidDefaultDark);

		caldroidFragment.setArguments(args);

//		FragmentTransaction t = mContext.getSupportFragmentManager().beginTransaction();
//		t.replace(R.id.calendar, caldroidFragment);
//		t.commit();


		//点击查看当天签到信息



		return view;
	}

	public static HistoryFragment newInstance() {
		HistoryFragment fragment = new HistoryFragment();
		fragment.setRetainInstance(true);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		this.mContext = context;
	}
}
