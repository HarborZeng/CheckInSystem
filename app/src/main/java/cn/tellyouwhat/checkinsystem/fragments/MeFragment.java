package cn.tellyouwhat.checkinsystem.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.activities.SettingsActivity;
import cn.tellyouwhat.checkinsystem.activities.UserInfoActivity;
import cn.tellyouwhat.checkinsystem.utils.ReLoginUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Harbor-Laptop on 2017/3/4.
 *
 * @author HarborZeng
 *         Me Fragment for MainActivity
 */

public class MeFragment extends BaseFragment {

	private static final String TAG = "MeFragment";
	private String mName;
	private String mEmployeeID;
	private String mDepartmentName;
	private String mPhoneNumber;
	private String mEmail;
	private String mHeadImage;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setUpUserInfo();
	}


	private void setUpUserInfo() {

	}

	@Override
	public void onResume() {
		final View view = getView();
		super.onResume();
		RequestParams requestParams = new RequestParams("http://api.checkin.tellyouwhat.cn/User/GetUserInfo");
		x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {

			private int resultInt;

			@Override
			public void onSuccess(JSONObject result) {
				Toast.makeText(getActivity(), "hoquuserinfosucceed", Toast.LENGTH_SHORT).show();
				try {
					resultInt = result.getInt("result");
					Log.d(TAG, "onSuccess: resultInt=" + resultInt + "and result is " + result.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				switch (resultInt) {
					case 1:
						try {
							mEmployeeID = result.getString("employeeid");
							mName = result.getString("name");
							mDepartmentName = result.getString("departmentname");
							mPhoneNumber = result.getString("phonenumber");
							mEmail = result.getString("email");
							mHeadImage = result.getString("headimage");
							TextView userNameTextView = (TextView) view.findViewById(R.id.user_name);
							if (!TextUtils.isEmpty(mName)) {
								userNameTextView.setText(mName);
							} else {
								Toast.makeText(getActivity(), "name is null", Toast.LENGTH_SHORT).show();
							}
							TextView jobNumberTextView = (TextView) view.findViewById(R.id.job_number);
							if (!TextUtils.isEmpty(mEmployeeID)) {
								jobNumberTextView.setText(mEmployeeID);
							}
							ImageView profileImageView = (ImageView) view.findViewById(R.id.profile_image);
							if (!TextUtils.isEmpty(mHeadImage)) {
								byte[] decodedHeadImage = Base64.decode(mHeadImage, Base64.DEFAULT);
								Bitmap bitmapHeadImage = BitmapFactory.decodeByteArray(decodedHeadImage, 0, decodedHeadImage.length);
								profileImageView.setImageBitmap(bitmapHeadImage);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						break;
					case 0:
						ReLoginUtil reLoginUtil = new ReLoginUtil(getActivity());
						reLoginUtil.reLoginWithAlertDialog();
						break;
					case -1:
						Toast.makeText(getActivity(), "发生了不可描述的错误010", Toast.LENGTH_SHORT).show();
						break;
					default:
						break;
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
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_me, container, false);
		RequestParams requestParams = new RequestParams("http://api.checkin.tellyouwhat.cn/User/GetUserInfo");
		x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {

			private int resultInt;

			@Override
			public void onSuccess(JSONObject result) {
				Toast.makeText(getActivity(), "hoquuserinfosucceed", Toast.LENGTH_SHORT).show();
				try {
					resultInt = result.getInt("result");
					Log.d(TAG, "onSuccess: resultInt=" + resultInt + "and result is " + result.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				switch (resultInt) {
					case 1:
						try {
							mEmployeeID = result.getString("employeeid");
							mName = result.getString("name");
							mDepartmentName = result.getString("departmentname");
							mPhoneNumber = result.getString("phonenumber");
							mEmail = result.getString("email");
							mHeadImage = result.getString("headimage");
							TextView userNameTextView = (TextView) view.findViewById(R.id.user_name);
							if (!TextUtils.isEmpty(mName)) {
								userNameTextView.setText(mName);
							} else {
								Toast.makeText(getActivity(), "name is null", Toast.LENGTH_SHORT).show();
							}
							TextView jobNumberTextView = (TextView) view.findViewById(R.id.job_number);
							if (!TextUtils.isEmpty(mEmployeeID)) {
								jobNumberTextView.setText(mEmployeeID);
							}
							CircleImageView profileImageView = (CircleImageView) view.findViewById(R.id.profile_image);
							if (!TextUtils.isEmpty(mHeadImage)) {
								byte[] decodedHeadImage = Base64.decode(mHeadImage, Base64.DEFAULT);
								Bitmap bitmapHeadImage = BitmapFactory.decodeByteArray(decodedHeadImage, 0, decodedHeadImage.length);
								profileImageView.setImageBitmap(bitmapHeadImage);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						break;
					case 0:
						ReLoginUtil reLoginUtil = new ReLoginUtil(getActivity());
						reLoginUtil.reLoginWithAlertDialog();
						break;
					case -1:
						Toast.makeText(getActivity(), "发生了不可描述的错误010", Toast.LENGTH_SHORT).show();
						break;
					default:
						break;
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

		view.findViewById(R.id.card_view_person_profile).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), UserInfoActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("jobNumber", mEmployeeID);
				bundle.putString("name", mName);
				bundle.putString("departmentName", mDepartmentName);
				bundle.putString("phoneNumber", mPhoneNumber);
				bundle.putString("email", mEmail);
				bundle.putString("headImage", mHeadImage);
				intent.putExtra("userInfo", bundle);
				startActivity(intent);
			}
		});

		view.findViewById(R.id.card_view_settings).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), SettingsActivity.class));
			}
		});

		Button logoffButton = (Button) view.findViewById(R.id.button_logoff);
		logoffButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new ReLoginUtil(getActivity()).reLoginWithAreYouSureDialog();
			}
		});
		return view;
	}

	public static MeFragment newInstance() {
		MeFragment fragment = new MeFragment();
		fragment.setRetainInstance(true);
		return fragment;
	}
}
