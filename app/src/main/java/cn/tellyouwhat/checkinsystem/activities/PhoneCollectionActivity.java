package cn.tellyouwhat.checkinsystem.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.jaeger.library.StatusBarUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.adpter.ExpandableItemAdapter;
import cn.tellyouwhat.checkinsystem.bean.Department;
import cn.tellyouwhat.checkinsystem.bean.PhoneItem;

/**
 * Created by Harbor-Laptop on 2017/4/16.
 * 展示所有电话信息的一个activity
 */

public class PhoneCollectionActivity extends BaseActivity implements ObservableScrollViewCallbacks {
	private ObservableRecyclerView mRecyclerView;
	private ActionBar mActionBar;
	private ArrayList<MultiItemEntity> data;
	private ExpandableItemAdapter adapter;
	private View notDataView;
	private View errorView;
	private SwipeRefreshLayout mPhoneCollectionSwipeRefresh;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_phone_collection);
		setUpActionBar();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPreferences.getBoolean("immersed_status_bar_enabled", true)) {
			StatusBarUtil.setColor(PhoneCollectionActivity.this, getResources().getColor(R.color.colorPrimary), 0);
		}
		mRecyclerView = (ObservableRecyclerView) findViewById(R.id.recycler_view_phone_collection);
		mRecyclerView.setScrollViewCallbacks(this);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

		notDataView = getLayoutInflater().inflate(R.layout.empty, (ViewGroup) mRecyclerView.getParent(), false);
		notDataView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Refresh();
			}
		});
		errorView = getLayoutInflater().inflate(R.layout.error_view, (ViewGroup) mRecyclerView.getParent(), false);
		errorView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Refresh();
			}
		});

		mPhoneCollectionSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.phone_collection_swipe_refresh_layout);
		mPhoneCollectionSwipeRefresh.setColorSchemeResources(R.color.colorAccent, R.color.theme_purple_primary, R.color.theme_yellow_primary, R.color.theme_red_primary, R.color.theme_green_primary, R.color.theme_blue_primary, R.color.pink);
		mPhoneCollectionSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				Refresh();
			}
		});

		initAdapter(new ArrayList<MultiItemEntity>());
		Refresh();
	}

	private void initData() {
		x.http().get(new RequestParams("http://api.checkin.tellyouwhat.cn/Telephone/GetAllTelephone"),
				new Callback.CommonCallback<JSONObject>() {
					@Override
					public void onSuccess(JSONObject result) {
//						Log.d("电话大全", "onSuccess: 电话大全有：" + result.toString());
						try {
							int resultInt = result.getInt("result");
							switch (resultInt) {
								case 1:
									data = new ArrayList<>();
									String itemDepartmentName = "";
									JSONArray jsonArray = result.getJSONArray("data");
									Department department = null;
									for (int i = 0; i < jsonArray.length(); i++) {
										JSONObject jsonObject = jsonArray.getJSONObject(i);
										String departmentName = jsonObject.getString("DepartmentName");
										if (!itemDepartmentName.equals(departmentName)) {
											itemDepartmentName = departmentName;
											if (department != null) {
												data.add(department);
											}
											department = new Department(departmentName);
										}
										String telephoneNumber = jsonObject.getString("TelephoneNumber");
										String telephoneSubordination = jsonObject.getString("TelephoneSubordination");
										PhoneItem phoneItem = new PhoneItem();
										phoneItem.setPhone(telephoneNumber);
										phoneItem.setPosition(telephoneSubordination);
										department.addSubItem(phoneItem);
									}
									adapter.setNewData(data);
									if (mPhoneCollectionSwipeRefresh.isRefreshing()) {
										mPhoneCollectionSwipeRefresh.setRefreshing(false);
									}
									Log.d("list里面的数据", "onSuccess: " + data.toString());
									break;
								case -1:
									Toast.makeText(x.app(), result.getString("message"), Toast.LENGTH_LONG).show();
									break;
								default:
									break;
							}
						} catch (JSONException e) {
							adapter.setEmptyView(notDataView);
							e.printStackTrace();
						}
					}

					@Override
					public void onError(Throwable ex, boolean isOnCallback) {
						Toast.makeText(x.app(), "加载电话失败", Toast.LENGTH_SHORT).show();
						adapter.setEmptyView(errorView);
					}

					@Override
					public void onCancelled(CancelledException cex) {

					}

					@Override
					public void onFinished() {

					}
				});
	}

	private void initAdapter(ArrayList<MultiItemEntity> data) {
		adapter = new ExpandableItemAdapter(data);
		mRecyclerView.setAdapter(adapter);
	}

	private void setUpActionBar() {
		mActionBar = getSupportActionBar();
//		Log.d("actionbar is null ?", "setUpActionBar: "+mActionBar);
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setTitle("电话大全");
		}
	}

	private void Refresh() {
		if (!mPhoneCollectionSwipeRefresh.isRefreshing()) {
			mPhoneCollectionSwipeRefresh.setRefreshing(true);
		}
		adapter.setEmptyView(R.layout.loading, (ViewGroup) mRecyclerView.getParent());
		initData();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case android.R.id.home:
				finish();
				return true;
			default:
				Toast.makeText(PhoneCollectionActivity.this, "点击了其他按钮", Toast.LENGTH_LONG).show();
				return false;
		}
	}

	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

	}

	@Override
	public void onDownMotionEvent() {

	}

	@Override
	public void onUpOrCancelMotionEvent(ScrollState scrollState) {
		if (mActionBar == null) {
			return;
		}
		if (scrollState == ScrollState.UP) {
			if (mActionBar.isShowing()) {
				mActionBar.hide();
			}
		} else if (scrollState == ScrollState.DOWN) {
			if (!mActionBar.isShowing()) {
				mActionBar.show();
			}
		}
	}
}
