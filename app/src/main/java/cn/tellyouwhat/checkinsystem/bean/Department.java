package cn.tellyouwhat.checkinsystem.bean;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import cn.tellyouwhat.checkinsystem.adpter.ExpandableItemAdapter;

/**
 * Created by Harbor-Laptop on 2017/4/16.
 */

public class Department extends AbstractExpandableItem<PhoneItem> implements MultiItemEntity {
	public String departmentName;

	public Department(String departmentName) {
		this.departmentName = departmentName;
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public int getItemType() {
		return ExpandableItemAdapter.TYPE_DEPARTMENT;
	}
}
