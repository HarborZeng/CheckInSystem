package cn.tellyouwhat.checkinsystem.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import cn.tellyouwhat.checkinsystem.R;

/**
 * Created by Harbor-Laptop on 2017/3/1.
 *
 * @author HarborZeng
 */

public class HistoryFragment extends Fragment {

	private CalendarView calendar_checkin_history;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_history, container, false);

		calendar_checkin_history = (CalendarView) view.findViewById(R.id.calendarView);


		//点击查看当天签到信息
		calendar_checkin_history.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
			@Override
			public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
				Log.i("calendar", "onSelectedDayChange: year-month-dayOfMonth: " + year + "-" + month + "-" + dayOfMonth + "-");
			}
		});


		return view;
	}

	public static HistoryFragment newInstance() {
		HistoryFragment fragment = new HistoryFragment();
		fragment.setRetainInstance(true);
		return fragment;
	}

}
