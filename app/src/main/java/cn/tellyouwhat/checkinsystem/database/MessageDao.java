package cn.tellyouwhat.checkinsystem.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import cn.tellyouwhat.checkinsystem.domain.Conversation;
import cn.tellyouwhat.checkinsystem.domain.Message;

public class MessageDao {
	private HMDBOpenHelper helper;

	public MessageDao(Context context) {
		helper = HMDBOpenHelper.getInstance(context);
	}

	public void addMessage(Message message) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(HMDB.Message.COLUMN_ACCOUNT, message.getAccount());
		values.put(HMDB.Message.COLUMN_CONTENT, message.getContent());
		values.put(HMDB.Message.COLUMN_CREATE_TIME, message.getCreateTime());
		values.put(HMDB.Message.COLUMN_DIRECTION, message.getDirection());
		values.put(HMDB.Message.COLUMN_OWNER, message.getOwner());
		values.put(HMDB.Message.COLUMN_STATE, message.getState());
		values.put(HMDB.Message.COLUMN_TYPE, message.getType());
		values.put(HMDB.Message.COLUMN_URL, message.getUrl());
		values.put(HMDB.Message.COLUMN_READ, message.isRead() ? 1 : 0);

		message.setId(db.insert(HMDB.Message.TABLE_NAME, null, values));

		String sql = "select * from " + HMDB.Conversation.TABLE_NAME
				+ " where " + HMDB.Conversation.COLUMN_ACCOUNT + "=? and "
				+ HMDB.Conversation.COLUMN_OWNER + "=?";
		Cursor cursor = db.rawQuery(sql, new String[]{message.getAccount(),
				message.getOwner()});
		if (cursor != null && cursor.moveToNext()) {
			// String account = cursor.getString(cursor
			// .getColumnIndex(HMDB.Conversation.COLUMN_ACCOUNT));
			// String content = cursor.getString(cursor
			// .getColumnIndex(HMDB.Conversation.COLUMN_CONTENT));
			// String icon = cursor.getString(cursor
			// .getColumnIndex(HMDB.Conversation.COLUMN_ICON));
			// String name = cursor.getString(cursor
			// .getColumnIndex(HMDB.Conversation.COLUMN_NAME));
			// String owner = cursor.getString(cursor
			// .getColumnIndex(HMDB.Conversation.COLUMN_OWNER));
			// int unread = cursor.getInt(cursor
			// .getColumnIndex(HMDB.Conversation.COLUMN_UNREAD));
			// long updateTime = cursor.getLong(cursor
			// .getColumnIndex(HMDB.Conversation.COLUMN_UPDATE_TIME));
			//

			// 关闭cursor
			cursor.close();
			cursor = null;

			int unread = 0;

			sql = "select count(_id) from " + HMDB.Message.TABLE_NAME
					+ " where " + HMDB.Message.COLUMN_READ + "=0 and "
					+ HMDB.Message.COLUMN_ACCOUNT + "=? and "
					+ HMDB.Message.COLUMN_OWNER + "=?";
			cursor = db.rawQuery(sql, new String[]{message.getAccount(),
					message.getOwner()});
			if (cursor != null && cursor.moveToNext()) {
				unread = cursor.getInt(0);
			}

			values = new ContentValues();
			values.put(HMDB.Conversation.COLUMN_ACCOUNT, message.getAccount());

			int type = message.getType();
			if (type == 0) {
				values.put(HMDB.Conversation.COLUMN_CONTENT,
						message.getContent());
			} else if (type == 1) {
				values.put(HMDB.Conversation.COLUMN_CONTENT, "图片");
			}
			// values.put(HMDB.Conversation.COLUMN_ICON,
			// conversation.getIcon());
			// values.put(HMDB.Conversation.COLUMN_NAME,
			// conversation.getName());
			values.put(HMDB.Conversation.COLUMN_OWNER, message.getOwner());
			values.put(HMDB.Conversation.COLUMN_UNREAD, unread);
			values.put(HMDB.Conversation.COLUMN_UPDATE_TIME,
					System.currentTimeMillis());

			String whereClause = HMDB.Conversation.COLUMN_OWNER + "=? and "
					+ HMDB.Conversation.COLUMN_ACCOUNT + "=?";
			String[] whereArgs = new String[]{message.getOwner(),
					message.getAccount()};

			db.update(HMDB.Conversation.TABLE_NAME, values, whereClause,
					whereArgs);

		} else {
			Conversation conversation = new Conversation();
			conversation.setAccount(message.getAccount());
			int type = message.getType();
			if (type == 0) {
				conversation.setContent(message.getContent());
			} else if (type == 1) {
				conversation.setContent("图片");
			}
			// conversation.setIcon(message.get);
			// conversation.setName(message.get);
			conversation.setOwner(message.getOwner());
			conversation.setUnread(message.isRead() ? 0 : 1);
			conversation.setUpdateTime(System.currentTimeMillis());

			values = new ContentValues();
			values.put(HMDB.Conversation.COLUMN_ACCOUNT,
					conversation.getAccount());
			values.put(HMDB.Conversation.COLUMN_CONTENT,
					conversation.getContent());
			values.put(HMDB.Conversation.COLUMN_ICON, conversation.getIcon());
			values.put(HMDB.Conversation.COLUMN_NAME, conversation.getName());
			values.put(HMDB.Conversation.COLUMN_OWNER, conversation.getOwner());
			values.put(HMDB.Conversation.COLUMN_UNREAD,
					conversation.getUnread());
			values.put(HMDB.Conversation.COLUMN_UPDATE_TIME,
					conversation.getUpdateTime());

			db.insert(HMDB.Conversation.TABLE_NAME, null, values);
		}
	}

	public void updateMessage(Message message) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(HMDB.Message.COLUMN_CONTENT, message.getContent());
		values.put(HMDB.Message.COLUMN_CREATE_TIME, message.getCreateTime());
		values.put(HMDB.Message.COLUMN_DIRECTION, message.getDirection());
		values.put(HMDB.Message.COLUMN_STATE, message.getState());
		values.put(HMDB.Message.COLUMN_TYPE, message.getType());
		values.put(HMDB.Message.COLUMN_URL, message.getUrl());
		values.put(HMDB.Message.COLUMN_READ, message.isRead() ? 1 : 0);

		String whereClause = HMDB.Message.COLUMN_ID + "=?";
		String[] whereArgs = new String[]{message.getId() + ""};
		db.update(HMDB.Message.TABLE_NAME, values, whereClause, whereArgs);
	}

	public Cursor queryMessage(String owner, String account) {
		String sql = "select * from " + HMDB.Message.TABLE_NAME + " where "
				+ HMDB.Message.COLUMN_OWNER + "=? and "
				+ HMDB.Message.COLUMN_ACCOUNT + "=? order by "
				+ HMDB.Message.COLUMN_CREATE_TIME + " asc";
		SQLiteDatabase db = helper.getReadableDatabase();
		return db.rawQuery(sql, new String[]{owner, account});
	}

	public Cursor queryConversation(String owner) {
		String sql = "select * from " + HMDB.Conversation.TABLE_NAME
				+ " where " + HMDB.Conversation.COLUMN_OWNER + "=? order by "
				+ HMDB.Conversation.COLUMN_UPDATE_TIME + " desc";
		SQLiteDatabase db = helper.getReadableDatabase();
		return db.rawQuery(sql, new String[]{owner});
	}

	public void clearUnread(String owner, String account) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(HMDB.Message.COLUMN_READ, 1);
		String whereClause = HMDB.Message.COLUMN_OWNER + "=? and "
				+ HMDB.Message.COLUMN_ACCOUNT + "=?";
		String[] whereArgs = new String[]{owner, account};
		db.update(HMDB.Message.TABLE_NAME, values, whereClause, whereArgs);

		values = new ContentValues();
		values.put(HMDB.Conversation.COLUMN_UNREAD, 0);
		whereClause = HMDB.Conversation.COLUMN_OWNER + "=? and "
				+ HMDB.Conversation.COLUMN_ACCOUNT + "=?";
		db.update(HMDB.Conversation.TABLE_NAME, values, whereClause, whereArgs);
	}

	public int getAllUnread(String owner) {
		String sql = "select sum(" + HMDB.Conversation.COLUMN_UNREAD
				+ ") from " + HMDB.Conversation.TABLE_NAME + " where "
				+ HMDB.Conversation.COLUMN_OWNER + "=?";

		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, new String[]{owner});
		int sum = 0;
		if (cursor != null) {
			if (cursor.moveToNext()) {
				sum = cursor.getInt(0);
			}
			cursor.close();
		}
		return sum;
	}
}
