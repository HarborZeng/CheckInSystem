package cn.tellyouwhat.checkinsystem.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.github.anzewei.parallaxbacklayout.ParallaxActivityBase;

/**
 * Created by Harbor-Laptop on 2017/2/24.
 * @author HarborZeng
 * It's all activities' root
 */

public class BaseActivity extends ParallaxActivityBase {
	private AlertDialog mAlertDialog;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
	}

	/**
	 * Requests given permission.
	 * If the permission has been denied previously, a Dialog will prompt the user to grant the
	 * permission, otherwise it is requested directly.
	 */
	protected void requestPermission(final String permission, String rationale, final int requestCode) {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
			showAlertDialog("需要权限", rationale,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ActivityCompat.requestPermissions(BaseActivity.this,
									new String[]{permission}, requestCode);
						}
					}, "授权", null, "取消");
		} else {
			ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
		}
	}


	/**
	 * This method shows dialog with given title & message.
	 * Also there is an option to pass onClickListener for positive & negative button.
	 *
	 * @param title                         - dialog title
	 * @param message                       - dialog message
	 * @param onPositiveButtonClickListener - listener for positive button
	 * @param positiveText                  - positive button text
	 * @param onNegativeButtonClickListener - listener for negative button
	 * @param negativeText                  - negative button text
	 */
	protected void showAlertDialog(@Nullable String title, @Nullable String message,
	                               @Nullable DialogInterface.OnClickListener onPositiveButtonClickListener,
	                               @NonNull String positiveText,
	                               @Nullable DialogInterface.OnClickListener onNegativeButtonClickListener,
	                               @NonNull String negativeText) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton(positiveText, onPositiveButtonClickListener);
		builder.setNegativeButton(negativeText, onNegativeButtonClickListener);
		mAlertDialog = builder.show();
	}

	/**
	 * Hide alert dialog if any.
	 */
	@Override
	protected void onStop() {
		super.onStop();
		if (mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.dismiss();
		}
	}
}
