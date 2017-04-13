package cn.tellyouwhat.checkinsystem.db;

import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
			Log.d("xyz", "save succeed!");
		} catch (DbException e) {
			Log.d("xyzhahah", e.toString());
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
	public List queryLocation(String year, String month, String day) {
		List<LocationItem> list = new ArrayList<>();
		try {
			Cursor cursor = db.execQuery("select building_desc, location_type, time, radius from locations where time like '" + year + "-" + month + "-" + day + "%'" + "and building_id != 0");
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

	//将Person实例存进数据库
	public List<LocationItem> loadAllLocation() {
		List<LocationItem> list = null;
		try {
			list = db.selector(LocationItem.class).findAll();
		} catch (DbException e) {
			e.printStackTrace();
		}
		return list;
	}
	//读取所有Person信息
}
