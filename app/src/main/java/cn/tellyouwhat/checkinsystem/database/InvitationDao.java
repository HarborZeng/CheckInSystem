package cn.tellyouwhat.checkinsystem.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import cn.tellyouwhat.checkinsystem.domain.Invitation;


public class InvitationDao {
	private HMDBOpenHelper helper;

	public InvitationDao(Context context) {
		helper = HMDBOpenHelper.getInstance(context);
	}

	public Cursor queryCursor(String owner) {
		SQLiteDatabase db = helper.getReadableDatabase();

		String sql = "select * from " + HMDB.Invitation.TABLE_NAME + " where "
				+ HMDB.Invitation.COLUMN_OWNER + "=?";
		return db.rawQuery(sql, new String[]{owner});
	}

	public void addInvitation(Invitation invitation) {
		SQLiteDatabase db = helper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(HMDB.Invitation.COLUMN_OWNER, invitation.getOwner());
		values.put(HMDB.Invitation.COLUMN_INVITATOR_ACCOUNT,
				invitation.getAccount());
		values.put(HMDB.Invitation.COLUMN_INVITATOR_NAME, invitation.getName());
		values.put(HMDB.Invitation.COLUMN_INVITATOR_ICON, invitation.getIcon());
		values.put(HMDB.Invitation.COLUMN_CONTENT, invitation.getContent());
		values.put(HMDB.Invitation.COLUMN_AGREE, invitation.isAgree() ? 1 : 0);
		db.insert(HMDB.Invitation.TABLE_NAME, null, values);
	}

	public void updateInvitation(Invitation invitation) {
		SQLiteDatabase db = helper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(HMDB.Invitation.COLUMN_INVITATOR_NAME, invitation.getName());
		values.put(HMDB.Invitation.COLUMN_INVITATOR_ICON, invitation.getIcon());
		values.put(HMDB.Invitation.COLUMN_CONTENT, invitation.getContent());
		values.put(HMDB.Invitation.COLUMN_AGREE, invitation.isAgree() ? 1 : 0);

		String whereClause = HMDB.Invitation.COLUMN_OWNER + "=? and "
				+ HMDB.Invitation.COLUMN_INVITATOR_ACCOUNT + "=?";
		String[] whereArgs = new String[]{invitation.getOwner(),
				invitation.getAccount()};

		db.update(HMDB.Invitation.TABLE_NAME, values, whereClause, whereArgs);
	}

	public Invitation queryInvitation(String owner, String account) {
		SQLiteDatabase db = helper.getWritableDatabase();
		String sql = "select * from " + HMDB.Invitation.TABLE_NAME + " where "
				+ HMDB.Invitation.COLUMN_OWNER + "=? and "
				+ HMDB.Invitation.COLUMN_INVITATOR_ACCOUNT + "=?";

		Cursor cursor = db.rawQuery(sql, new String[]{owner, account});
		Invitation invitation = null;
		if (cursor != null) {
			if (cursor.moveToNext()) {
				// String account = cursor
				// .getString(cursor
				// .getColumnIndex(HMDB.Invitation.COLUMN_INVITATOR_ACCOUNT));
				String name = cursor.getString(cursor
						.getColumnIndex(HMDB.Invitation.COLUMN_INVITATOR_NAME));
				String icon = cursor.getString(cursor
						.getColumnIndex(HMDB.Invitation.COLUMN_INVITATOR_ICON));
				boolean agree = cursor.getInt(cursor
						.getColumnIndex(HMDB.Invitation.COLUMN_AGREE)) == 1;
				String content = cursor.getString(cursor
						.getColumnIndex(HMDB.Invitation.COLUMN_CONTENT));
				// String owner = cursor.getString(cursor
				// .getColumnIndex(HMDB.Invitation.COLUMN_OWNER));
				long id = cursor.getLong(cursor
						.getColumnIndex(HMDB.Invitation.COLUMN_ID));

				invitation = new Invitation();
				invitation.setAccount(account);
				invitation.setAgree(agree);
				invitation.setContent(content);
				invitation.setIcon(icon);
				invitation.setName(name);
				invitation.setOwner(owner);
				invitation.setId(id);
			}
			cursor.close();
		}
		return invitation;
	}

	public boolean hasUnagree(String owner) {
		SQLiteDatabase db = helper.getWritableDatabase();
		String sql = "select count(" + HMDB.Invitation.COLUMN_ID + ") from "
				+ HMDB.Invitation.TABLE_NAME + " where "
				+ HMDB.Invitation.COLUMN_AGREE + "=0";
		Cursor cursor = db.rawQuery(sql, null);

		int count = 0;
		if (cursor != null) {
			if (cursor.moveToNext()) {
				count = cursor.getInt(0);
			}
			cursor.close();
		}
		return count != 0;
	}
}
