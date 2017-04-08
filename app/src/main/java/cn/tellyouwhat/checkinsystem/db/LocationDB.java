package cn.tellyouwhat.checkinsystem.db;

import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

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
	 * @param location 传入location
	 * @return 如果SQL语句执行有误，返回null
	 */
	public Map queryLocation(LocationItem location) {
		Map<String, String> map = null;
		try {
			//TODO aaaaa
			Cursor cursor = db.execQuery("select * from locations where date=");
			while (cursor.moveToNext()) {
				int in = cursor.getInt(cursor.getColumnIndex("in"));
				String date = cursor.getString(cursor.getColumnIndex("date"));
				map = new HashMap<>();
				map.put("in", String.valueOf(in));
				map.put("date", date);
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
		return map;
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
