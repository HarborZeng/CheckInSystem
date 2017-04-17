package cn.tellyouwhat.checkinsystem.adpter;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

import cn.tellyouwhat.checkinsystem.R;
import cn.tellyouwhat.checkinsystem.bean.Department;
import cn.tellyouwhat.checkinsystem.bean.PhoneItem;


/**
 * Created by HarborZeng on 2016/8/9.
 * This is a class for
 */
public class ExpandableItemAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
	private static final String TAG = ExpandableItemAdapter.class.getSimpleName();

	public static final int TYPE_DEPARTMENT = 0;
	public static final int TYPE_PHONE = 1;

	/**
	 * Same as QuickAdapter#QuickAdapter(Context,int) but with
	 * some initialization data.
	 *
	 * @param data A new list is created out of this one to avoid mutable list
	 */
	public ExpandableItemAdapter(List<MultiItemEntity> data) {
		super(data);
		addItemType(TYPE_DEPARTMENT, R.layout.item_department);
		addItemType(TYPE_PHONE, R.layout.item_phone);
	}


	@Override
	protected void convert(final BaseViewHolder holder, final MultiItemEntity item) {
		switch (holder.getItemViewType()) {
			case TYPE_DEPARTMENT:
				final Department department = (Department) item;
				holder.setText(R.id.department_name_text_view, department.departmentName)
						.setImageResource(R.id.expand_image_view, department.isExpanded() ? R.drawable.ic_arrow_forward_black_24dp : R.drawable.ic_arrow_downward_black_24dp);
//	            Log.d(TAG, "convert: department.departmentName是："+department.departmentName);
				holder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = holder.getAdapterPosition();
						Log.d(TAG, "Level 0 item pos: " + pos);
						if (department.isExpanded()) {
							collapse(pos);
						} else {
//                            if (pos % 3 == 0) {
//                                expandAll(pos, false);
//                            } else {
							expand(pos);
//                            }
						}
					}
				});
				break;
			case TYPE_PHONE:
				final PhoneItem phoneItem = (PhoneItem) item;
				holder.setText(R.id.position_text_view, phoneItem.getPosition())
						.setText(R.id.phone_number_text_view, phoneItem.getPhone());
				holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						ClipboardManager clipboardManager = (ClipboardManager) v.getContext().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
						clipboardManager.setPrimaryClip(ClipData.newPlainText(null,
								(
										(TextView)
												v.findViewById(R.id.phone_number_text_view))
										.getText().toString()
								)
						);  // 将内容set到剪贴板
						if (clipboardManager.hasPrimaryClip()) {
							Toast.makeText(v.getContext(), "内容已复制", Toast.LENGTH_SHORT).show();
						}
						return true;
					}
				});
				holder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						CharSequence phoneNumber = ((TextView) v.findViewById(R.id.phone_number_text_view))
								.getText();
						Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
						v.getContext().startActivity(intent);
					}
				});
				break;
		}
	}
}
