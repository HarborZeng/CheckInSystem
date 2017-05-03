package cn.tellyouwhat.checkinsystem.db;

import android.database.Cursor;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

import cn.tellyouwhat.checkinsystem.bean.LocationItem;


/**
 * Created by Harbor-Laptop on 2017/3/23.
 * 用来封装存储读取数据库的方法
 */

public class LocationDB {
	private DbManager db;

	//接收构造方法初始化的DbManager对象
	public LocationDB() {
		db = DatabaseOpenHelper.getInstance();
	}

	/****************************************************************************************/
	//写两个测试方法
	public void saveLocation(LocationItem location) {
		try {
			db.save(location);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	public void deleteLocation(LocationItem location) {
		try {
			db.delete(location);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	public void updateLocation(LocationItem location) {
		try {
			db.update(location);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return 返回
	 */
	public List queryLocation(String year, String month, String day, String userID) {
		List<LocationItem> list = new ArrayList<>();
		try {
			Cursor cursor = db.execQuery("select building_desc, location_type, time, radius from locations where time like '" + year + "-" + month + "-" + day + "%'" + "and building_id != 0 and user_id = '" + userID + "'");
			while (cursor.moveToNext()) {
				LocationItem item = new LocationItem();
				item.setBuildingDesc(cursor.getString(cursor.getColumnIndex("building_desc")));
				item.setTime(cursor.getString(cursor.getColumnIndex("time")));
				item.setLocationType(cursor.getInt(cursor.getColumnIndex("location_type")));
				item.setRadius(cursor.getFloat(cursor.getColumnIndex("radius")));
				list.add(item);
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 查询最后一条记录
	 *
	 * @param username 按照用户名查
	 * @return 如果没找到或者出异常，返回NULL，否则返回带仅有time属性的{@link LocationItem}对象
	 */
	public LocationItem queryLastRecord(String username) {
		LocationItem item = null;
		try {
			Cursor cursor = db.execQuery("select time from locations where user_id = " + username + " and building_id != 0 ORDER BY id DESC LIMIT 1");
			while (cursor.moveToNext()) {
				item = new LocationItem();
				item.setTime(cursor.getString(cursor.getColumnIndex("time")));
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
		return item;
	}

	public LocationItem queryFirstRecordOfDay(String year, String month, String day, String userID) {
		LocationItem item = null;
		try {
			Cursor cursor = db.execQuery("select time from locations where user_id=" + userID + " and building_id != 0 and time like '" + year + "-" + month + "-" + day + "%'" + " LIMIT 1");
			while (cursor.moveToNext()) {
				String time = cursor.getString(cursor.getColumnIndex("time"));
				item = new LocationItem();
				item.setTime(time);
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
		return item;
	}
}
