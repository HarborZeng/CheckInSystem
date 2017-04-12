package cn.tellyouwhat.checkinsystem.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.activities.SettingsActivity;
import cn.tellyouwhat.checkinsystem.activities.UserInfoActivity;
import cn.tellyouwhat.checkinsystem.utils.CookiedRequestParams;
import cn.tellyouwhat.checkinsystem.utils.ReLoginUtil;

/**
 * Created by Harbor-Laptop on 2017/3/4.
 *
 * @author HarborZeng
 *         Me Fragment for MainActivity
 */

public class MeFragment extends BaseFragment {

	private final String TAG = "MeFragment";
	private String mName;
	private String mEmployeeID;
	private String mDepartmentName;
	private String mPhoneNumber;
	private String mEmail;
	private String mHeadImage;
	private ProgressBar mGetUserInfoProgressBar;
	private CardView mGetUserInfoBGCharView;
	AlphaAnimation mAlphaAnimationOut = new AlphaAnimation(1f, 0f);
	AlphaAnimation mAlphaAnimationIn = new AlphaAnimation(0f, 1f);

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		Log.i(TAG, "onCreate: in MeFragment");
		super.onCreate(savedInstanceState);
		mAlphaAnimationOut.setDuration(500);
		mAlphaAnimationIn.setDuration(1000);
	}

	private void setUpUserInfo() {
		Log.i(TAG, "setUpUserInfo: setting up...");
		final View view = getView();
		showProgress(true);
		CookiedRequestParams requestParams = new CookiedRequestParams("http://api.checkin.tellyouwhat.cn/User/GetUserInfo");
		x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {

			private int resultInt;

			@Override
			public void onSuccess(JSONObject result) {
//				Toast.makeText(getActivity(), "hoquuserinfosucceed", Toast.LENGTH_SHORT).show();
				try {
					resultInt = result.getInt("result");
					Log.d(TAG, "onSuccess: resultInt=" + resultInt + "and result is " + result.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				switch (resultInt) {
					case 1:
						mGetUserInfoBGCharView.setVisibility(View.INVISIBLE);
						mGetUserInfoBGCharView.startAnimation(mAlphaAnimationOut);
						showProgress(false);
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
						updateSession();
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

	@Override
	public void onResume() {
		Log.i(TAG, "onResume: in MeFragment");
		super.onResume();
		setUpUserInfo();
	}

	@Override
	public void onPause() {
		Log.i(TAG, "onPause: in MeFragment");
		super.onPause();
		mGetUserInfoBGCharView.setVisibility(View.VISIBLE);
		mGetUserInfoBGCharView.startAnimation(mAlphaAnimationIn);
		showProgress(true);
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

		mGetUserInfoProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
		mGetUserInfoProgressBar.animate().setDuration(shortAnimTime).alpha(
				show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mGetUserInfoProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		});
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView: in MeFragment");
		final View view = inflater.inflate(R.layout.fragment_me, container, false);
		mGetUserInfoBGCharView = (CardView) view.findViewById(R.id.get_userinfo_bg);
		mGetUserInfoProgressBar = (ProgressBar) view.findViewById(R.id.get_userinfo_progress);
		mGetUserInfoBGCharView.setVisibility(View.VISIBLE);

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
