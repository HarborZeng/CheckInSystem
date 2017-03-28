package cn.tellyouwhat.checkinsystem.db;

import org.xutils.DbManager;
import org.xutils.x;

/**
 * Created by Harbor-Laptop on 2017/3/23.
 * Database Open Helper 创建数据库checkindb
 */

public class DatabaseOpenHelper {
	private static DbManager db;

	private DatabaseOpenHelper() {
		String DB_NAME = "checkindb.db";
		int VERSION = 1;
		DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
				.setDbName(DB_NAME)
				.setDbVersion(VERSION)
				.setDbOpenListener(new DbManager.DbOpenListener() {
					@Override
					public void onDbOpened(DbManager db) {
						db.getDatabase().enableWriteAheadLogging();
						//开启WAL, 对写入加速提升巨大(作者原话)
					}
				})
				.setDbUpgradeListener(new DbManager.DbUpgradeListener() {
					@Override
					public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
						//数据库升级操作
					}
				});
		db = x.getDb(daoConfig);
	}

	public static DbManager getInstance() {
		if (db == null) {
			new DatabaseOpenHelper();
		}
		return db;
	}
}