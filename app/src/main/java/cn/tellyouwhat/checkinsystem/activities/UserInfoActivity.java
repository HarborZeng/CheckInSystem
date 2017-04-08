package cn.tellyouwhat.checkinsystem.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.utils.InputDialog;
import cn.tellyouwhat.checkinsystem.utils.ReLoginUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Harbor-Laptop on 2017/4/7.
 * 用户个人信息编辑于此
 */

public class UserInfoActivity extends BaseActivity {
	private static final int REQUEST_SELECT_PICTURE = 0x01;
	private static final String SAMPLE_CROPPED_IMAGE_NAME = "head";
	protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
	protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;
	private static final String TAG = "UserInfoActivity";
	private CardView mEditPageHeadCardView;
	private Uri mHeadImage;
	private TextView nameEditPageTextView;
	private TextView jobNumberEditPageTextView;
	private TextView phoneNumberEditPageTextView;
	private TextView emailEditPageTextView;
	private TextView departmentEditPageTextView;
	private AlertDialog mAlertDialog;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_user_info);
		setUpActionBar();
		setUpUI();
	}

	protected void showAlertDialog(@Nullable String title,
	                               @NonNull String positiveText,
	                               @NonNull final TextView parentView) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.dialog, null);
		dialog.setView(layout);
		final EditText fill = (EditText) findViewById(R.id.editText_fill_in_blank);
		dialog.setTitle(title)
				.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						parentView.setText(fill.getText().toString());
						dialog.dismiss();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.show();
	}

	private void setUpUI() {
		Intent intent = getIntent();
		Bundle userInfo = intent.getBundleExtra("userInfo");
		String jobNumber = userInfo.getString("jobNumber");
		String name = userInfo.getString("name");
		String departmentName = userInfo.getString("departmentName");
		String phoneNumber = userInfo.getString("phoneNumber");
		String email = userInfo.getString("email");
		String headImage = userInfo.getString("headImage");

		nameEditPageTextView = (TextView) findViewById(R.id.edit_page_name);
		if (!TextUtils.isEmpty(name)) {
			nameEditPageTextView.setText(name);
		}

		jobNumberEditPageTextView = (TextView) findViewById(R.id.edit_page_job_number);
		if (!TextUtils.isEmpty(jobNumber)) {
			jobNumberEditPageTextView.setText(jobNumber);
		}

		phoneNumberEditPageTextView = (TextView) findViewById(R.id.edit_page_phone_number);
		if (!TextUtils.isEmpty(phoneNumber)) {
			phoneNumberEditPageTextView.setText(phoneNumber);
		}

		emailEditPageTextView = (TextView) findViewById(R.id.edit_page_email);
		if (!TextUtils.isEmpty(email)) {
			emailEditPageTextView.setText(email);
		}

		departmentEditPageTextView = (TextView) findViewById(R.id.edit_page_department);
		if (!TextUtils.isEmpty(departmentName)) {
			departmentEditPageTextView.setText(departmentName);
		}

		CircleImageView headEditPageCircleImageView = (CircleImageView) findViewById(R.id.edit_page_head);
		if (!TextUtils.isEmpty(headImage)) {
			byte[] decodedHeadImage = Base64.decode(headImage, Base64.DEFAULT);
			Bitmap bitmapHeadImage = BitmapFactory.decodeByteArray(decodedHeadImage, 0, decodedHeadImage.length);
			headEditPageCircleImageView.setImageBitmap(bitmapHeadImage);
		}
		mEditPageHeadCardView = (CardView) findViewById(R.id.card_view_edit_page_head);
		mEditPageHeadCardView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickFromGallery();
			}
		});


		nameEditPageTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new InputDialog.Builder(getApplicationContext())
						.setTitle("请输入新名字")
						.setPositiveButton("确定", new InputDialog.ButtonActionListener() {
							@Override
							public void onClick(CharSequence inputText) {
								nameEditPageTextView.setText(inputText);
							}
						})
						.setNegativeButton("取消", new InputDialog.ButtonActionListener() {
							@Override
							public void onClick(CharSequence inputText) {

							}
						}).show();
			}
		});
//		jobNumberEditPageTextView;
//		phoneNumberEditPageTextView;
//		emailEditPageTextView;
//		departmentEditPageTextView;
	}

	private void setUpActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		} else if (item.getItemId() == R.id.item_finish) {
			item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					Log.d(TAG, "onMenuItemClick: 点击了完成");
					RequestParams requestParams = new RequestParams("http://api.checkin.tellyouwhat.cn/User/UpdateUserInfo");
					requestParams.setMultipart(true);

					File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/head.png");
					try {
						requestParams.addBodyParameter("headimage", file);
					} catch (NullPointerException e) {
						e.printStackTrace();
						requestParams.addBodyParameter("headimage", "null");
					}
					requestParams.addBodyParameter("employeeid", jobNumberEditPageTextView.getText().toString().trim());
					requestParams.addBodyParameter("departmentid", departmentEditPageTextView.getText().toString().trim());
					requestParams.addBodyParameter("name", nameEditPageTextView.getText().toString().trim());
					requestParams.addBodyParameter("phonenumber", phoneNumberEditPageTextView.getText().toString().trim());
					requestParams.addBodyParameter("email", emailEditPageTextView.getText().toString().trim());

					x.http().post(requestParams, new Callback.CommonCallback<JSONObject>() {
						@Override
						public void onSuccess(JSONObject result) {
							int resultInt = -1;
							Log.d(TAG, "onSuccess: result: " + result.toString());
							try {
								resultInt = result.getInt("result");
							} catch (JSONException e) {
								e.printStackTrace();
							}
							switch (resultInt) {
								case 1:
									Snackbar.make(findViewById(R.id.scroll_view_user_info), "保存成功", Snackbar.LENGTH_LONG).show();
									break;
								case 0:
									ReLoginUtil reLoginUtil = new ReLoginUtil(UserInfoActivity.this);
									reLoginUtil.reLoginWithAlertDialog();
									break;
								case -1:
									Snackbar.make(findViewById(R.id.scroll_view_user_info), "发生了不可描述的错误011", Snackbar.LENGTH_LONG).show();
									break;
								default:
									Snackbar.make(findViewById(R.id.scroll_view_user_info), "出错0110", Snackbar.LENGTH_LONG).show();
									break;
							}
						}

						@Override
						public void onError(Throwable ex, boolean isOnCallback) {
							ex.printStackTrace();
						}

						@Override
						public void onCancelled(CancelledException cex) {
							cex.printStackTrace();
						}

						@Override
						public void onFinished() {
							Log.d(TAG, "onFinished");
						}
					});
					return true;
				}
			});
		}
		return super.onOptionsItemSelected(item);
	}

	private void pickFromGallery() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
				&& ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
					"需要读取内置存储的权限才可以选择图片",
					REQUEST_STORAGE_READ_ACCESS_PERMISSION);
		} else {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_SELECT_PICTURE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.personal_info, menu);
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_STORAGE_READ_ACCESS_PERMISSION:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					pickFromGallery();
				}
				break;
			case REQUEST_STORAGE_WRITE_ACCESS_PERMISSION:

				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_SELECT_PICTURE) {
				final Uri selectedUri = data.getData();
				if (selectedUri != null) {
					startCropActivity(data.getData());
				} else {
					Toast.makeText(UserInfoActivity.this, "选中的图片不能处理", Toast.LENGTH_SHORT).show();
				}
			} else if (requestCode == UCrop.REQUEST_CROP) {
				handleCropResult(data);
			}
		}
		if (resultCode == UCrop.RESULT_ERROR) {
			handleCropError(data);
		}
	}

	private void startCropActivity(@NonNull Uri uri) {
		String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME;
		destinationFileName += ".png";

		UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));

		uCrop = basisConfig(uCrop);
		uCrop = advancedConfig(uCrop);

		uCrop.start(UserInfoActivity.this);
	}

	/**
	 * In most cases you need only to set crop aspect ration and max size for resulting image.
	 *
	 * @param uCrop - ucrop builder instance
	 * @return - ucrop builder instance
	 */
	private UCrop basisConfig(@NonNull UCrop uCrop) {
		uCrop = uCrop.withAspectRatio(1, 1);
		uCrop = uCrop.withMaxResultSize(400, 400);
		return uCrop;
	}

	/**
	 * Sometimes you want to adjust more options, it's done via {@link UCrop.Options} class.
	 *
	 * @param uCrop - ucrop builder instance
	 * @return - ucrop builder instance
	 */
	private UCrop advancedConfig(@NonNull UCrop uCrop) {
		UCrop.Options options = new UCrop.Options();

		options.setCompressionFormat(Bitmap.CompressFormat.PNG);
		options.setCompressionQuality(80);

		options.setHideBottomControls(false);
		options.setFreeStyleCropEnabled(false);

		options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
		/*
		If you want to configure how gestures work for all UCropActivity tabs

        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        * */

        /*
        This sets max size for bitmap that will be decoded from source Uri.
        More size - more memory allocation, default implementation uses screen diagonal.

        options.setMaxBitmapSize(640);
        * */


       /*

        Tune everything (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧

        options.setMaxScaleMultiplier(5);
        options.setImageToCropBoundsAnimDuration(666);
        options.setDimmedLayerColor(Color.CYAN);
        options.setCircleDimmedLayer(true);
        options.setShowCropFrame(false);
        options.setCropGridStrokeWidth(20);
        options.setCropGridColor(Color.GREEN);
        options.setCropGridColumnCount(2);
        options.setCropGridRowCount(1);
        options.setToolbarCropDrawable(R.drawable.your_crop_icon);
        options.setToolbarCancelDrawable(R.drawable.your_cancel_icon);

        // Color palette
        options.setToolbarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setRootViewBackgroundColor(ContextCompat.getColor(this, R.color.your_color_res));

        // Aspect ratio options
        options.setAspectRatioOptions(1,
            new AspectRatio("WOW", 1, 2),
            new AspectRatio("MUCH", 3, 4),
            new AspectRatio("RATIO", CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO),
            new AspectRatio("SO", 16, 9),
            new AspectRatio("ASPECT", 1, 1));

       */

		return uCrop.withOptions(options);
	}

	private void handleCropResult(@NonNull Intent result) {
		final Uri resultUri = UCrop.getOutput(result);
		if (resultUri != null) {
//			mHeadImage = resultUri;
			saveCroppedImage(resultUri);
			CircleImageView profileImage = (CircleImageView) findViewById(R.id.edit_page_head);
			profileImage.setImageURI(resultUri);
		} else {
			Toast.makeText(UserInfoActivity.this, "剪裁图片出错", Toast.LENGTH_SHORT).show();
		}
	}

	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	private void handleCropError(@NonNull Intent result) {
		final Throwable cropError = UCrop.getError(result);
		if (cropError != null) {
			Log.e(TAG, "handleCropError: ", cropError);
			Toast.makeText(UserInfoActivity.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(UserInfoActivity.this, "未知意外", Toast.LENGTH_SHORT).show();
		}
	}

	private void saveCroppedImage(Uri resultUri) {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
					"需要写入内置存储的权限",
					REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
		} else {
			if (resultUri != null && resultUri.getScheme().equals("file")) {
				try {
					copyFileToDownloads(resultUri);
				} catch (Exception e) {
					Toast.makeText(UserInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
					Log.e(TAG, resultUri.toString(), e);
				}
			} else {
				Toast.makeText(UserInfoActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void copyFileToDownloads(Uri croppedFileUri) throws Exception {
		String downloadsDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
		String filename = "head.png";

		File saveFile = new File(downloadsDirectoryPath, filename);

		FileInputStream inStream = new FileInputStream(new File(croppedFileUri.getPath()));
		FileOutputStream outStream = new FileOutputStream(saveFile);
		FileChannel inChannel = inStream.getChannel();
		FileChannel outChannel = outStream.getChannel();
		inChannel.transferTo(0, inChannel.size(), outChannel);
		inStream.close();
		outStream.close();
	}


}
