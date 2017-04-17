package cn.tellyouwhat.checkinsystem.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import cn.tellyouwhat.checkinsystem.adpter.ExpandableItemAdapter;

/**
 * Created by Harbor-Laptop on 2017/4/16.
 */

public class PhoneItem implements MultiItemEntity {
	private String position;
	private String phone;

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public int getItemType() {
		return ExpandableItemAdapter.TYPE_PHONE;
	}
}
