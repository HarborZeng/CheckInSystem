package cn.tellyouwhat.checkinsystem.fragments;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;

import cn.tellyouwhat.checkinsystem.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Harbor-Laptop on 2017/5/23.
 * 展示头像的fragment
 * 用到了单例设计模式，看起来很“函数式”
 */

public class HeadImageFragment extends Fragment {
	private static HeadImageFragment instance;

	public static HeadImageFragment newInstance() {

		Bundle args = new Bundle();

		if (instance == null) {
			synchronized (HeadImageFragment.class) {
				if (instance == null) {
					instance = new HeadImageFragment();
					instance.setArguments(args);
				}
			}
		}
		return instance;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootLayout = inflater.inflate(R.layout.fragment_head_image, container, false);
		PhotoView photoView = (PhotoView) rootLayout.findViewById(R.id.photo_view_head_image);
		String headImage = getHeadImageBase64ed();
		if (!TextUtils.isEmpty(headImage)) {
			byte[] decodedHeadImage = Base64.decode(headImage, Base64.DEFAULT);
			Bitmap bitmapHeadImage = BitmapFactory.decodeByteArray(decodedHeadImage, 0, decodedHeadImage.length);
			photoView.setImageBitmap(bitmapHeadImage);
		}
		photoView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().getSupportFragmentManager().beginTransaction()
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
						.remove(instance)
						.commit();
			}
		});
		return rootLayout;
	}

	/**
	 * 从userInfo读取头像
	 *
	 * @return 头像的Base64编码
	 */
	private String getHeadImageBase64ed() {
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userInfo", MODE_PRIVATE);
		return sharedPreferences.getString("headImage", "");
	}
}
