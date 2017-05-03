package cn.tellyouwhat.checkinsystem.db;

import android.database.Cursor;

import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

import cn.tellyouwhat.checkinsystem.bean.CompanyLocationItem;

/**
 * Created by Harbor-Laptop on 2017/5/1.
 * 用来做公司信息的存储
 */

public class CompanyDB {
	private DbManager db;

	//接收构造方法初始化的DbManager对象
	public CompanyDB() {
		db = DatabaseOpenHelper.getInstance();
	}

	/****************************************************************************************/
	public void saveCompany(CompanyLocationItem company) throws DbException {
		db.save(company);
	}

	public void updateCompany(CompanyLocationItem company) throws DbException{
		db.update(company);
	}

	public void deleteCompany(CompanyLocationItem company) throws DbException {
		db.delete(company);
	}

	public List<CompanyLocationItem> queryAllCompany() throws DbException {
		Cursor cursor = db.execQuery("select * from companies_info");
		CompanyLocationItem item;
		List<CompanyLocationItem> list = new ArrayList<>();
		while (cursor.moveToNext()){
			int buildingId = cursor.getInt(cursor.getColumnIndex("building_id"));
			String buildingName = cursor.getString(cursor.getColumnIndex("building_name"));
			String x1 = cursor.getString(cursor.getColumnIndex("x1"));
			String x2 = cursor.getString(cursor.getColumnIndex("x2"));
			String x3 = cursor.getString(cursor.getColumnIndex("x3"));
			String x4 = cursor.getString(cursor.getColumnIndex("x4"));
			String y1 = cursor.getString(cursor.getColumnIndex("y1"));
			String y2 = cursor.getString(cursor.getColumnIndex("y2"));
			String y3 = cursor.getString(cursor.getColumnIndex("y3"));
			String y4 = cursor.getString(cursor.getColumnIndex("y4"));
			item = new CompanyLocationItem();
			item.setLocationID(buildingId);
			item.setLocationName(buildingName);
			item.setX1(x1);
			item.setX2(x2);
			item.setX3(x3);
			item.setX4(x4);
			item.setY1(y1);
			item.setY2(y2);
			item.setY3(y3);
			item.setY4(y4);
			list.add(item);
		}
		return list;
	}
}
