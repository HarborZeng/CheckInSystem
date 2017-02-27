package cn.tellyouwhat.checkinsystem.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HMDBOpenHelper extends SQLiteOpenHelper {

	private HMDBOpenHelper(Context context) {
		super(context, HMDB.NAME, null, HMDB.VERSION);
	}

	private static HMDBOpenHelper instance;

	public static HMDBOpenHelper getInstance(Context context) {
		if (instance == null) {
			synchronized (HMDBOpenHelper.class) {
				if (instance == null) {
					instance = new HMDBOpenHelper(context);
				}
			}
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(HMDB.Account.SQL_CREATE_TABLE);
		db.execSQL(HMDB.Friend.SQL_CREATE_TABLE);
		db.execSQL(HMDB.Invitation.SQL_CREATE_TABLE);
		db.execSQL(HMDB.Message.SQL_CREATE_TABLE);
		db.execSQL(HMDB.Conversation.SQL_CREATE_TABLE);
		db.execSQL(HMDB.BackTask.SQL_CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
