package cn.tellyouwhat.checkinsystem.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.activities.PhoneCollectionActivity;
import cn.tellyouwhat.checkinsystem.activities.SettingsActivity;
import cn.tellyouwhat.checkinsystem.activities.UserInfoActivity;
import cn.tellyouwhat.checkinsystem.utils.ReLoginUtil;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Harbor-Laptop on 2017/3/4.
 *
 * @author HarborZeng
 *         Me Fragment for MainActivity
 */

public class MeFragment extends BaseFragment {

	private static final int OPEN_USER_INFO_ACTIVITY = 13;
	private final String TAG = "MeFragment";
	private String mName;
	private String mEmployeeID;
	private String mDepartmentName;
	private String mPhoneNumber;
	private String mEmail;
	private String mHeadImage;

	AlphaAnimation mAlphaAnimationOut = new AlphaAnimation(1f, 0f);
	AlphaAnimation mAlphaAnimationIn = new AlphaAnimation(0f, 1f);

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
//		Log.i(TAG, "onCreate: in MeFragment");
		super.onCreate(savedInstanceState);
		mAlphaAnimationOut.setDuration(500);
		mAlphaAnimationIn.setDuration(1000);
	}

	@Override
	public void onResume() {
//		Log.i(TAG, "onResume: in MeFragment");
		super.onResume();
	}

	@Override
	public void onPause() {
//		Log.i(TAG, "onPause: in MeFragment");
		super.onPause();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView: in MeFragment");
		final View view = inflater.inflate(R.layout.fragment_me, container, false);
		//set up UserInfo
		setUpUserInfo(view);

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
				startActivityForResult(intent, OPEN_USER_INFO_ACTIVITY);
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

		CardView checkInGraphicCardView = (CardView) view.findViewById(R.id.card_view_check_in_graphic);
		checkInGraphicCardView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().getSupportFragmentManager().beginTransaction()
						.add(R.id.container_me_fragment, ChartFragment.newInstance())
						.commit();
			}
		});

		CardView phoneCollectionCardView = (CardView) view.findViewById(R.id.card_view_phone_collection);
		phoneCollectionCardView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), PhoneCollectionActivity.class);
				startActivity(intent);
			}
		});

		return view;
	}

	private void setUpUserInfo(View view) {
		if (view != null) {
			SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userInfo", MODE_PRIVATE);
			mName = sharedPreferences.getString("name", "");
			mDepartmentName = sharedPreferences.getString("departmentName", "");
			mPhoneNumber = sharedPreferences.getString("phoneNumber", "");
			mEmployeeID = sharedPreferences.getString("employeeID", "");
			mEmail = sharedPreferences.getString("email", "");
			mHeadImage = sharedPreferences.getString("headImage", "");
			TextView userNameTextView = (TextView) view.findViewById(R.id.user_name);
			userNameTextView.setText(mName);
			TextView jobNumberTextView = (TextView) view.findViewById(R.id.job_number);
			jobNumberTextView.setText(mEmployeeID);
			ImageView profileImageView = (ImageView) view.findViewById(R.id.profile_image);
			byte[] decodedHeadImage = Base64.decode(mHeadImage, Base64.DEFAULT);
			Bitmap bitmapHeadImage = BitmapFactory.decodeByteArray(decodedHeadImage, 0, decodedHeadImage.length);
			profileImageView.setImageBitmap(bitmapHeadImage);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case OPEN_USER_INFO_ACTIVITY:
				switch (resultCode) {
					case RESULT_OK:
						setUpUserInfo(getView());
						break;
					default:
						break;
				}
				break;
			default:
				break;
		}
	}

	public static MeFragment newInstance() {
		MeFragment fragment = new MeFragment();
		fragment.setRetainInstance(true);
		return fragment;
	}
}
