package cn.tellyouwhat.checkinsystem.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.fragments.HeadImageFragment;
import cn.tellyouwhat.checkinsystem.utils.CookiedRequestParams;
import cn.tellyouwhat.checkinsystem.utils.ReLoginUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Harbor-Laptop on 2017/4/7.
 * 用户个人信息编辑于此
 */

public class UserInfoActivity extends BaseActivity {
    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;
    private static final int REQUEST_SELECT_PICTURE = 0x01;
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "head";
    private static final String TAG = "UserInfoActivity";
    private CardView mEditPageHeadCardView;
    private TextView nameEditPageTextView;
    private TextView jobNumberEditPageTextView;
    private TextView phoneNumberEditPageTextView;
    private TextView emailEditPageTextView;
    private TextView departmentEditPageTextView;
    private AlphaAnimation mAnimationIn = new AlphaAnimation(0f, 1f);
    private AlphaAnimation mAnimationOut = new AlphaAnimation(1f, 0f);
    private View.OnClickListener nameListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MaterialDialog.Builder(UserInfoActivity.this)
                    .title("输入新的名字")
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input("请输入名字", nameEditPageTextView.getText(), new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            if (input != null) {
                                nameEditPageTextView.setText(input);
                            }
                        }
                    })
                    .inputRange(2, 15,
                            ContextCompat.getColor(UserInfoActivity.this,
                                    R.color.theme_red_primary))
                    .show();
        }
    };
    private View.OnClickListener jobNumberListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MaterialDialog.Builder(UserInfoActivity.this)
                    .title("输入新的工号")
                    .inputType(InputType.TYPE_CLASS_NUMBER)
                    .input("请输入10位数字", jobNumberEditPageTextView.getText(), new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            if (input != null) {
                                jobNumberEditPageTextView.setText(input);
                            }
                        }
                    })
                    .inputRange(10, 10,
                            ContextCompat.getColor(UserInfoActivity.this,
                                    R.color.theme_red_primary))
                    .show();
        }
    };
    private View.OnClickListener phoneNumberListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MaterialDialog.Builder(UserInfoActivity.this)
                    .title("输入新的手机号")
                    .inputType(InputType.TYPE_CLASS_PHONE)
                    .input("请输入11位手机号码", phoneNumberEditPageTextView.getText(), new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            if (input != null) {
                                phoneNumberEditPageTextView.setText(input);
                            }
                        }
                    })
                    .inputRange(11, 11,
                            ContextCompat.getColor(UserInfoActivity.this,
                                    R.color.theme_red_primary))
                    .show();
        }
    };
    private View.OnClickListener emailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new MaterialDialog.Builder(UserInfoActivity.this)
                    .title("输入新的邮箱")
                    .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                    .input("请输邮箱", emailEditPageTextView.getText(), new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            if (input != null) {
                                emailEditPageTextView.setText(input);
                            }
                        }
                    })
                    .inputRange(5, 30,
                            ContextCompat.getColor(UserInfoActivity.this,
                                    R.color.theme_red_primary))
                    .show();
        }
    };
    private Map<Integer, String> department = new LinkedHashMap<>();
    private int mDepartmentID = 0;
    private int selectedDepartmentID;
    private ProgressBar mUserInfoSummitProgressBar;
    private CardView mUserInfoSummitGB;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_user_info);

        setStatusBarColor();
        setUpActionBar();
        setUpUI();
        getAllDepartment();
    }

    private void getAllDepartment() {
        x.http().get(new RequestParams("https://api.checkin.tellyouwhat.cn/Department/GetAllDepartments"), new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                Log.d(TAG, "onSuccess: 公司职位部门: " + result.toString());
                try {
                    int resultInt = result.getInt("result");
                    switch (resultInt) {
                        case 1:
                            JSONArray jsonArray = result.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                int departmentID = object.getInt("DepartmentID");
                                String departmentName = object.getString("DepartmentName");
                                department.put(departmentID, departmentName);
                                if (departmentName.equals(getSharedPreferences("userInfo", MODE_PRIVATE).getString("departmentName", ""))) {
                                    mDepartmentID = departmentID;
                                }
                            }
                            break;
                        case -1:
                            Toast.makeText(UserInfoActivity.this, result.getString("message"), Toast.LENGTH_LONG).show();
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
                Toast.makeText(x.app(), "获取部门信息出错", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                Log.d(TAG, "onFinished: department里面都有：" + department);
            }
        });
    }

    private void setUpUI() {
        Intent intent = getIntent();
        Bundle userInfo = intent.getBundleExtra("userInfo");
        String jobNumber = userInfo.getString("jobNumber");
        String name = userInfo.getString("name");
        final String departmentName = userInfo.getString("departmentName");
        String phoneNumber = userInfo.getString("phoneNumber");
        String email = userInfo.getString("email");
        String headImage = userInfo.getString("headImage");
        selectedDepartmentID = 0;

        mUserInfoSummitGB = (CardView) findViewById(R.id.userinfo_summit_bg);
        mUserInfoSummitProgressBar = (ProgressBar) findViewById(R.id.userinfo_summit_progress);

        nameEditPageTextView = (TextView) findViewById(R.id.edit_page_name);
        if (!TextUtils.isEmpty(name)) {
            nameEditPageTextView.setText(name);
        } else {
            nameEditPageTextView.setText("");
        }

        jobNumberEditPageTextView = (TextView) findViewById(R.id.edit_page_job_number);
        if (!TextUtils.isEmpty(jobNumber)) {
            jobNumberEditPageTextView.setText(jobNumber);
        } else {
            jobNumberEditPageTextView.setText("");
        }

        phoneNumberEditPageTextView = (TextView) findViewById(R.id.edit_page_phone_number);
        if (!TextUtils.isEmpty(phoneNumber)) {
            phoneNumberEditPageTextView.setText(phoneNumber);
        } else {
            phoneNumberEditPageTextView.setText("");
        }

        emailEditPageTextView = (TextView) findViewById(R.id.edit_page_email);
        if (!TextUtils.isEmpty(email)) {
            emailEditPageTextView.setText(email);
        } else {
            emailEditPageTextView.setText("");
        }

        departmentEditPageTextView = (TextView) findViewById(R.id.edit_page_department);
        if (!TextUtils.isEmpty(departmentName)) {
            departmentEditPageTextView.setText(departmentName);
        } else {
            departmentEditPageTextView.setText("");
        }

        CircleImageView headEditPageCircleImageView = (CircleImageView) findViewById(R.id.edit_page_head);
        if (!TextUtils.isEmpty(headImage)) {
            byte[] decodedHeadImage = Base64.decode(headImage, Base64.DEFAULT);
            Bitmap bitmapHeadImage = BitmapFactory.decodeByteArray(decodedHeadImage, 0, decodedHeadImage.length);
            headEditPageCircleImageView.setImageBitmap(bitmapHeadImage);
        }

        headEditPageCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .add(R.id.relative_layout_user_info_activity, HeadImageFragment.newInstance())
                        .commit();
            }
        });

        mEditPageHeadCardView = (CardView) findViewById(R.id.card_view_edit_page_head);
        mEditPageHeadCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
            }
        });

        findViewById(R.id.card_view_edit_page_name).setOnClickListener(nameListener);
        findViewById(R.id.card_view_edit_page_job_number).setOnClickListener(jobNumberListener);
        findViewById(R.id.card_view_edit_page_phone_number).setOnClickListener(phoneNumberListener);
        findViewById(R.id.card_view_edit_page_email).setOnClickListener(emailListener);

        findViewById(R.id.card_view_edit_page_department).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] d = new String[department.size()];
                int i = 0;
                for (String value : department.values()) {
                    d[i] = value;
                    if (value.equals(departmentName)) {
                        selectedDepartmentID = i;
                    }
                    i++;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this);
                builder.setTitle("选择新部门")
                        .setSingleChoiceItems(d, selectedDepartmentID, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                departmentEditPageTextView.setText(d[i]);
                                mDepartmentID = i + 1;
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showProgress(boolean show) {
        mAnimationIn.setDuration(500);
        mAnimationOut.setDuration(500);
        mUserInfoSummitProgressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mUserInfoSummitGB.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mUserInfoSummitProgressBar.startAnimation(show ? mAnimationIn : mAnimationOut);
        mUserInfoSummitGB.startAnimation(show ? mAnimationIn : mAnimationOut);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.item_finish) {
            showProgress(true);
            final String jobNumber = jobNumberEditPageTextView.getText().toString().trim();
//			final String department = departmentEditPageTextView.getText().toString().trim();
            final String name = nameEditPageTextView.getText().toString().trim();
            final String phone = phoneNumberEditPageTextView.getText().toString().trim();
            final String email = emailEditPageTextView.getText().toString().trim();
//			Log.i(TAG, "onOptionsItemSelected: jobNumber: " + jobNumber + ", department: " + department + ", name:" + URLEncoder.encode(name, "UTF-8") + ", phone: " + phone + ", email: " + email);
            CookiedRequestParams requestParams = new CookiedRequestParams("https://api.checkin.tellyouwhat.cn/User/UpdateUserInfo");
            requestParams.setMultipart(true);
            final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/head.jpg");
            if (file.exists()) {
                requestParams.addBodyParameter("headimage", file, null);
            }
            requestParams.addBodyParameter("employeeid", jobNumber);
            requestParams.addBodyParameter("departmentid", String.valueOf(mDepartmentID));
            requestParams.addBodyParameter("name", name);
            requestParams.addBodyParameter("phonenumber", phone);
            requestParams.addBodyParameter("email", email);

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
                            setResult(RESULT_OK);
                            updateUserInfoAfterModified();
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
                    showProgress(false);
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
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateUserInfoAfterModified() {
        CookiedRequestParams requestParams = new CookiedRequestParams("https://api.checkin.tellyouwhat.cn/User/GetUserInfo");
        x.http().get(requestParams, new Callback.CommonCallback<JSONObject>() {
            private int resultInt;

            @Override
            public void onSuccess(JSONObject result) {
                try {
                    resultInt = result.getInt("result");
                    Log.d(TAG, "onSuccess: resultInt=" + resultInt + "and result is " + result.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (resultInt) {
                    case 1:
                        try {
                            String employeeID = result.getString("employeeid");
                            String name = result.getString("name");
                            String departmentName = result.getString("departmentname");
                            String phoneNumber = result.getString("phonenumber");
                            String email = result.getString("email");
                            String headImage = result.getString("headimage");
                            SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                            editor.putString("employeeID", employeeID);
                            editor.putString("name", name);
                            editor.putString("departmentName", departmentName);
                            editor.putString("phoneNumber", phoneNumber);
                            editor.putString("email", email);
                            editor.putString("headImage", headImage);
                            editor.apply();
                            showProgress(false);
                            Snackbar.make(findViewById(R.id.scroll_view_user_info), "保存成功", Snackbar.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 0:
                        updateSession();
                        break;
                    case -1:
                        Toast.makeText(x.app(), "发生了不可描述的错误010", Toast.LENGTH_SHORT).show();
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
        destinationFileName += ".jpg";

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

        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);

        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(false);

        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);

        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

//		options.setMaxBitmapSize(16000);
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
        String filename = "head.jpg";

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
