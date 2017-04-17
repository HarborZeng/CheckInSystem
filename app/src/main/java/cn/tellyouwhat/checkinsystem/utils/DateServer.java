package cn.tellyouwhat.checkinsystem.utils;

import java.util.List;

import cn.tellyouwhat.checkinsystem.db.LocationDB;
import cn.tellyouwhat.checkinsystem.db.LocationItem;

/**
 * Created by HarborZeng on 2017/4/12.
 * This is a class for
 */

public class DateServer {

	public static List<LocationItem> getData(String year, String month, String day, String employeeID) {
		LocationDB locationDB = new LocationDB();
		List list = locationDB.queryLocation(year, month, day, employeeID);
		return list;
	}
}
