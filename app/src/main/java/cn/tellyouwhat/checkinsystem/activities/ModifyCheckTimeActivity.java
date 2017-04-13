package cn.tellyouwhat.checkinsystem.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.util.List;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.adpter.AllBackGroundLocationAdapter;
import cn.tellyouwhat.checkinsystem.db.LocationItem;
import cn.tellyouwhat.checkinsystem.utils.CookiedRequestParams;

/**
 * Created by HarborZeng on 2017/4/12.
 * This is a class for modify the check in/out time
 */

public class ModifyCheckTimeActivity extends BaseActivity {
	private RecyclerView mRecyclerView;
	private AllBackGroundLocationAdapter adapter;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_modify_check_time);

		/*Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_modify);
		toolbar.setTitle("修改记录");
		toolbar.setBackgroundColor(getResources().getColor(R.color.blue_semi_transparent));
		setSupportActionBar(toolbar);*/
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle("修改记录");
		}

		mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_modify_check_time);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

		Intent intent = getIntent();
		String year = intent.getStringExtra("year");
		String month = intent.getStringExtra("month");
		String day = intent.getStringExtra("day");
		String id = intent.getStringExtra("id");
		initAdapter(year, month, day, id);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return false;
		}
	}

	private void initAdapter(String year, String month, String day, final String id) {
		adapter = new AllBackGroundLocationAdapter(year, month, day);
		adapter.openLoadAnimation();
		adapter.setNotDoAnimationCount(3);
		adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
		adapter.isFirstOnly(false);

		adapter.setEmptyView(getLayoutInflater().inflate(R.layout.error_view, (ViewGroup) mRecyclerView.getParent(), false));

		adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(final BaseQuickAdapter adapter, final View view, final int position) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ModifyCheckTimeActivity.this);
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
								List data = adapter.getData();
								LocationItem item = (LocationItem) data.get(position);
								String time = item.getTime();
//								Toast.makeText(ModifyCheckTimeActivity.this, time, Toast.LENGTH_SHORT).show();
								String hour = time.substring(11, 13);
								String minute = time.substring(14, 16);
								String second = time.substring(17, 19);
//								Toast.makeText(ModifyCheckTimeActivity.this, hour+minute+second, Toast.LENGTH_SHORT).show();

								CookiedRequestParams params = new CookiedRequestParams("http://api.checkin.tellyouwhat.cn/checkin/ModifyCheckIn?id=" + id + "&hour=" + hour + "&minute=" + minute + "&second=" + second);
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
													Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_SHORT).show();
													break;
												case -2:
													Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_SHORT).show();
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

									}

									@Override
									public void onCancelled(CancelledException cex) {

									}

									@Override
									public void onFinished() {

									}
								});
								dialog.dismiss();
							}
						}).show();
			}
		});

		mRecyclerView.setAdapter(adapter);
	}
}
