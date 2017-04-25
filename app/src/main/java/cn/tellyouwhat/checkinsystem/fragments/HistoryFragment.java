package cn.tellyouwhat.checkinsystem.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.adpter.AllBackGroundLocationAdapter;
import cn.tellyouwhat.checkinsystem.bean.CheckInRecord;
import cn.tellyouwhat.checkinsystem.db.LocationItem;
import cn.tellyouwhat.checkinsystem.decorators.HighlightWeekendsDecorator;
import cn.tellyouwhat.checkinsystem.decorators.OneDayDecorator;
import cn.tellyouwhat.checkinsystem.decorators.TextDecorator;
import cn.tellyouwhat.checkinsystem.utils.CookiedRequestParams;
import cn.tellyouwhat.checkinsystem.utils.DateUtils;
import cn.tellyouwhat.checkinsystem.utils.TextSpan;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Harbor-Laptop on 2017/3/1.
 *
 * @author HarborZeng
 */

public class HistoryFragment extends BaseFragment {

	public static final int REQUEST_START_MODIFY_CHECK_TIME_CODE = 11;
	private final String TAG = "HistoryFragment";
	private Context mContext;
	private MaterialCalendarView mCalendarView;
	private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
	private AlphaAnimation mAnimationIn = new AlphaAnimation(0f, 1f);
	private AlphaAnimation mAnimationOut = new AlphaAnimation(1f, 0f);
	private ProgressBar mHistoryProgressBar;
	private CardView mHistoryCardView;
	private Map<CalendarDay, CheckInRecord> mRecordMap = new HashMap<>();
	private TextView mOriginalCheckInTime;
	private TextView mOriginalCheckOutTime;
	private TextView mCheckOutTimeTextView;
	private TextView mCheckInTimeTextView;
	private List<CalendarDay> normalDates = new ArrayList<>();
	private List<CalendarDay> overTimeDates = new ArrayList<>();
	private List<CalendarDay> leaveEarilyDates = new ArrayList<>();
	private List<CalendarDay> notCheckOutDates = new ArrayList<>();

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_history, container, false);

		mHistoryProgressBar = (ProgressBar) view.findViewById(R.id.get_month_record_progress);
		mHistoryCardView = (CardView) view.findViewById(R.id.get_month_record_bg);

		Calendar calendar = Calendar.getInstance();
		getThisMonthStatus(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

		mCheckInTimeTextView = (TextView) view.findViewById(R.id.check_in_time_text_view);
		mCheckOutTimeTextView = (TextView) view.findViewById(R.id.check_out_time_text_view);
		mOriginalCheckInTime = (TextView) view.findViewById(R.id.text_view_original_check_in_time);
		mOriginalCheckOutTime = (TextView) view.findViewById(R.id.text_view_original_check_out_time);

		mCalendarView = (MaterialCalendarView) view.findViewById(R.id.calendar_view);
		mCalendarView.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
		Calendar instance = Calendar.getInstance();
		mCalendarView.setSelectedDate(instance.getTime());

		Calendar instance1 = Calendar.getInstance();
		instance1.set(instance1.get(Calendar.YEAR), Calendar.JANUARY, 1);

		Calendar instance2 = Calendar.getInstance();
		instance2.set(instance2.get(Calendar.YEAR), Calendar.DECEMBER, 31);

		mCalendarView.state().edit()
				.setMinimumDate(instance1.getTime())
				.setMaximumDate(instance2.getTime())
				.commit();
		mCalendarView.addDecorators(
				new HighlightWeekendsDecorator(),
				oneDayDecorator
		);

		mCalendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
			@Override
			public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
				getThisMonthStatus(date.getYear(), date.getMonth());
			}
		});
		mCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
			@Override
			public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
				showCheckTime(date);
			}
		});

		//点击按钮更新数据的逻辑
		FloatingActionButton reFreshFloatingActionBar = (FloatingActionButton) view.findViewById(R.id.floatingActionButton_refresh_calendar);
		reFreshFloatingActionBar.setBackgroundColor(Color.WHITE);
		reFreshFloatingActionBar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCalendarView.removeDecorators();
				CalendarDay currentDate = mCalendarView.getCurrentDate();
				getThisMonthStatus(currentDate.getYear(), currentDate.getMonth());
			}
		});

		//点击签到时间文本即可修正签到记录
		mCheckInTimeTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startModifyCheckTime(true);
			}
		});
		mCheckOutTimeTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//只有签到了的，才能更改签出时间
				if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("has_check_in", false) ||
						mCalendarView.getSelectedDate().isBefore(CalendarDay.today())) {
					startModifyCheckTime(false);
				} else {
					Snackbar.make(v, "还未签出", Snackbar.LENGTH_SHORT).show();
				}
			}
		});

		return view;
	}

	private void startModifyCheckTime(final boolean isCheckIn) {
		CalendarDay currentDate = mCalendarView.getSelectedDate();
		String year = String.valueOf(currentDate.getYear());
		int month = currentDate.getMonth();
		int realMonth = month + 1;
		String realMonthString;
		if (realMonth < 10) {
			realMonthString = "0" + realMonth;
		} else {
			realMonthString = String.valueOf(realMonth);
		}
		int day = currentDate.getDay();
		String dayString;
		if (day < 10) {
			dayString = "0" + day;
		} else {
			dayString = String.valueOf(day);
		}
		CheckInRecord checkInRecord = mRecordMap.get(currentDate);
		final String checkInID;
		if (checkInRecord == null) {
			Snackbar.make(getView(), "不是工作日或尚未签到", Snackbar.LENGTH_SHORT).show();
		} else {
			checkInID = checkInRecord.getCheckInID();
//			Log.d(TAG, "startModifyCheckTime: " + checkInID);
			SharedPreferences sharedPreferences = getContext().getSharedPreferences("userInfo", MODE_PRIVATE);
			final String employeeID = sharedPreferences.getString("employeeID", "");
//			Log.d("用户名debug", "initAdapter: " + employeeID);
			AllBackGroundLocationAdapter adapter = new AllBackGroundLocationAdapter(year, realMonthString, dayString, employeeID);
			adapter.openLoadAnimation();
			adapter.setNotDoAnimationCount(3);
			adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
			adapter.isFirstOnly(true);

			adapter.setEmptyView(getLayoutInflater(null).inflate(R.layout.empty, (ViewGroup) getView().getParent(), false));

			adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
				@Override
				public void onItemClick(final BaseQuickAdapter adapter, final View view, final int position) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setTitle("确定修改为这条记录吗？")
							.setMessage("\n修改后原记录将予以保留\n")
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							})
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									new MaterialDialog.Builder(getContext())
											.title("输入备注")
											.inputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT)
											.input("请输入备注", null, false, new MaterialDialog.InputCallback() {
												@Override
												public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
													if (input != null) {
														String comment = input.toString();
														String encodedComment = "";
														try {
															encodedComment = URLEncoder.encode(comment, "UTF-8");
														} catch (UnsupportedEncodingException e) {
															e.printStackTrace();
														}
														dialog.dismiss();
														List data = adapter.getData();
														LocationItem item = (LocationItem) data.get(position);
														String time = item.getTime();
//								Toast.makeText(ModifyCheckTimeActivity.this, time, Toast.LENGTH_SHORT).show();
														String hour = time.substring(11, 13);
														String minute = time.substring(14, 16);
														String second = time.substring(17, 19);
//								Toast.makeText(ModifyCheckTimeActivity.this, hour+minute+second, Toast.LENGTH_SHORT).show();
														Log.d("传的api网址", "onClick: 传的api网址：" +
																"http://api.checkin.tellyouwhat.cn/checkin/ModifyCheckIn?checkinid=" +
																checkInID +
																"&hour=" + hour +
																"&minute=" + minute +
																"&second=" + second +
																"&reason=" + encodedComment);
														CookiedRequestParams params;
														if (isCheckIn) {
															params = new CookiedRequestParams("http://api.checkin.tellyouwhat.cn/checkin/ModifyCheckIn?checkinid=" + checkInID +
																	"&hour=" + hour +
																	"&minute=" + minute +
																	"&second=" + second +
																	"&reason=" + encodedComment);
														} else {
															params = new CookiedRequestParams("http://api.checkin.tellyouwhat.cn/checkin/ModifyCheckOut?checkinid=" + checkInID +
																	"&hour=" + hour +
																	"&minute=" + minute +
																	"&second=" + second +
																	"&reason=" + encodedComment);
														}
														x.http().get(params, new Callback.CommonCallback<JSONObject>() {
															@Override
															public void onSuccess(JSONObject result) {
																Log.d("修改记录结果", "onSuccess: result: " + result.toString());
																try {
																	int resultInt = result.getInt("result");
																	switch (resultInt) {
																		case 1:
																			Snackbar.make(view, "修改记录成功", Snackbar.LENGTH_LONG).show();
																			break;
																		case 0:
																			updateSession();
																			break;
																		case -1:
																			Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_SHORT).show();
																			break;
																		case -2:
																			Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_SHORT).show();
																			break;
																		default:
																			break;
																	}
																} catch (JSONException e) {
																	e.printStackTrace();
																}
															}

															@Override
															public void onError(Throwable ex, boolean isOnCallback) {
																Snackbar.make(view, "修改失败，请重试", Snackbar.LENGTH_SHORT).show();
																ex.printStackTrace();
															}

															@Override
															public void onCancelled(CancelledException cex) {

															}

															@Override
															public void onFinished() {
																Calendar calendar = Calendar.getInstance();
																getThisMonthStatus(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
															}
														});
													}
												}
											})
											.inputRange(3, 55, getResources().getColor(R.color.theme_red_primary))
											.show();
									dialog.dismiss();
								}
							}).show();
				}
			});

			new MaterialDialog.Builder(getActivity())
					.adapter(adapter, new LinearLayoutManager(getContext()))
					.show();
		}
	}

	private void showCheckTime(CalendarDay date) {
		mOriginalCheckInTime.setText("");
		mOriginalCheckOutTime.setText("");
		Calendar calendar = date.getCalendar();
		CheckInRecord record = mRecordMap.get(CalendarDay.from(calendar));
		if (record != null) {
			String checkInTime = record.getCheckInTime();
			String checkOutTime = record.getCheckOutTime();
			String oriCheckInTime = record.getOriCheckInTime();
			String oriCheckOutTime = record.getOriCheckOutTime();
			mCheckInTimeTextView.setText(checkInTime.replace("T", " "));
			mCheckOutTimeTextView.setText(checkOutTime.replace("T", " ").replace("0001-01-01 00:00:00", "未签出"));
			if (!checkInTime.equals(oriCheckInTime)) {
				mOriginalCheckInTime.setText("原始签入时间：" + oriCheckInTime.substring(11));
			}
			if (!checkOutTime.equals(oriCheckOutTime)) {
				mOriginalCheckOutTime.setText("原始签出时间：" + oriCheckOutTime.substring(11));
			}
		} else {
			mCheckInTimeTextView.setText("未签到");
			mCheckOutTimeTextView.setText("未签出");
		}
	}

	private void getThisMonthStatus(int year, int month) {
		showProgress(true);
		Log.i(TAG, "getThisMonthStatus: year is " + year + ", and month is " + month);
		int realMonth = month + 1;
		CookiedRequestParams params = new CookiedRequestParams("http://api.checkin.tellyouwhat.cn/CheckIn/GetMonthData?year=" + year + "&month=" + realMonth);
		x.http().get(params, new Callback.CommonCallback<JSONObject>() {
			@Override
			public void onSuccess(JSONObject result) {
				Log.i(TAG, "onSuccess: 本月的数据是：" + result);
				try {
					int resultInt = result.getInt("result");
					switch (resultInt) {
						case 1:
							mRecordMap.clear();
							JSONArray resultJSONArray = result.getJSONArray("data");
							for (int i = 0; i < resultJSONArray.length(); i++) {
								JSONObject jsonObject = resultJSONArray.getJSONObject(i);
								String checkInID = jsonObject.getString("CheckInID");
								String checkInTime = jsonObject.getString("CheckInTime");
								String checkOutTime = jsonObject.getString("CheckOutTime");
								String oriCheckInTime = jsonObject.getString("OriCheckInTime");
								String oriCheckOutTime = jsonObject.getString("OriCheckOutTime");
								boolean hasCheckOut = jsonObject.getBoolean("HasCheckOut");
								CheckInRecord record = new CheckInRecord();
								record.setCheckInID(checkInID);
								record.setCheckInTime(checkInTime);
								record.setCheckOutTime(checkOutTime);
								record.setHasCheckOut(hasCheckOut);
								record.setOriCheckInTime(oriCheckInTime);
								record.setOriCheckOutTime(oriCheckOutTime);

								String date = checkInTime.substring(0, checkInTime.indexOf("T"));
								String[] dateFlags = date.split("-");
								int year = Integer.valueOf(dateFlags[0]);
								int month = Integer.valueOf(dateFlags[1]);
								int day = Integer.valueOf(dateFlags[2]);
								CalendarDay calendarDay = CalendarDay.from(new Date(year - 1900, month - 1, day));
								mRecordMap.put(calendarDay, record);
							}
							showCheckInStatusOnCalendar(mRecordMap);
							showCheckTime(mCalendarView.getSelectedDate());
							break;
						case 0:
							updateSession();
							break;
						case -1:
							Toast.makeText(x.app(), "内部错误, 重启app再试试", Toast.LENGTH_LONG).show();
							break;
						default:
							break;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				View view = getView();
				if (view != null) {
					Snackbar.make(getView(), "获取历史记录出错", Snackbar.LENGTH_LONG).setAction("重试", new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Calendar instance = Calendar.getInstance();
							getThisMonthStatus(instance.get(Calendar.YEAR), instance.get(Calendar.MONTH));
						}
					}).show();
				}
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {
				showProgress(false);
			}
		});
	}

	private void showProgress(boolean show) {
		mAnimationIn.setDuration(500);
		mAnimationOut.setDuration(500);
		mHistoryProgressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mHistoryCardView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mHistoryProgressBar.startAnimation(show ? mAnimationIn : mAnimationOut);
		mHistoryCardView.startAnimation(show ? mAnimationIn : mAnimationOut);
	}

	private void showCheckInStatusOnCalendar(Map<CalendarDay, CheckInRecord> recordMap) {
		normalDates.clear();
		overTimeDates.clear();
		leaveEarilyDates.clear();
		notCheckOutDates.clear();
		Collection<CheckInRecord> values = recordMap.values();
		for (CheckInRecord record :
				values) {
			String checkInTime = record.getCheckInTime();
			String checkOutTime = record.getCheckOutTime();
			boolean hasCheckOut = record.isHasCheckOut();

			Date checkInDateAndTime = null;
			Date checkOutDateAndTime = null;
			try {
				checkInDateAndTime = DateUtils.parse(checkInTime.replace("T", " "), "yy-MM-dd HH:mm:ss");
				checkOutDateAndTime = DateUtils.parse(checkOutTime.replace("T", " "), "yy-MM-dd HH:mm:ss");
			} catch (ParseException e) {
				e.printStackTrace();
			}

			String checkInDate = checkInTime.substring(0, checkInTime.indexOf("T"));
			String[] dateFlags = checkInDate.split("-");
			int year = Integer.valueOf(dateFlags[0]);
			int month = Integer.valueOf(dateFlags[1]);
			int day = Integer.valueOf(dateFlags[2]);

			long timeDifference = checkOutDateAndTime.getTime() - checkInDateAndTime.getTime();
			int workHour = (int) (timeDifference % (1000 * 24 * 60 * 60) / (1000 * 60 * 60));

			if (hasCheckOut) {
				if (workHour == 8) {
					normalDates.add(CalendarDay.from(new Date(year - 1900, month - 1, day)));
				} else if (workHour > 8) {
					overTimeDates.add(CalendarDay.from(new Date(year - 1900, month - 1, day)));
				} else {
					leaveEarilyDates.add(CalendarDay.from(new Date(year - 1900, month - 1, day)));
				}
			} else {
				notCheckOutDates.add(CalendarDay.from(new Date(year - 1900, month - 1, day)));
			}
		}
//		mCalendarView.removeDecorators();
		mCalendarView.addDecorators(new TextDecorator(normalDates, TextSpan.NORMAL),
				new TextDecorator(overTimeDates, TextSpan.OVER_TIME),
				new TextDecorator(leaveEarilyDates, TextSpan.LEAVE_EARILY),
				new TextDecorator(notCheckOutDates, TextSpan.NOT_CHECK_OUT));
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
