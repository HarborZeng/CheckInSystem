package cn.tellyouwhat.checkinsystem.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import cn.tellyouwhat.checkinsystem.domain.BackTask;


public class BackTaskDao {
	private HMDBOpenHelper helper;

	public BackTaskDao(Context context) {
		helper = HMDBOpenHelper.getInstance(context);
	}

	public void addTask(BackTask task) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(HMDB.BackTask.COLUMN_OWNER, task.getOwner());
		values.put(HMDB.BackTask.COLUMN_PATH, task.getPath());
		values.put(HMDB.BackTask.COLUMN_STATE, task.getState());
		task.setId(db.insert(HMDB.BackTask.TABLE_NAME, null, values));
	}

	public void updateTask(BackTask task) {

		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(HMDB.BackTask.COLUMN_OWNER, task.getOwner());
		values.put(HMDB.BackTask.COLUMN_PATH, task.getPath());
		values.put(HMDB.BackTask.COLUMN_STATE, task.getState());

		String whereClause = HMDB.BackTask.COLUMN_ID + "=?";
		String[] whereArgs = new String[]{task.getId() + ""};
		db.update(HMDB.BackTask.TABLE_NAME, values, whereClause, whereArgs);
	}

	public void updateState(long id, int state) {
		SQLiteDatabase db = helper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(HMDB.BackTask.COLUMN_STATE, state);
		String whereClause = HMDB.BackTask.COLUMN_ID + "=?";
		String[] whereArgs = new String[]{id + ""};
		db.update(HMDB.BackTask.TABLE_NAME, values, whereClause, whereArgs);
	}

	public Cursor query(String owner) {
		SQLiteDatabase db = helper.getReadableDatabase();
		String sql = "select * from " + HMDB.BackTask.TABLE_NAME + " where "
				+ HMDB.BackTask.COLUMN_OWNER + "=?";
		return db.rawQuery(sql, new String[]{owner});
	}

	public Cursor query(String owner, int state) {
		SQLiteDatabase db = helper.getReadableDatabase();
		String sql = "select * from " + HMDB.BackTask.TABLE_NAME + " where "
				+ HMDB.BackTask.COLUMN_OWNER + "=? and "
				+ HMDB.BackTask.COLUMN_STATE + "=?";
		return db.rawQuery(sql, new String[]{owner, "0"});
	}
}
