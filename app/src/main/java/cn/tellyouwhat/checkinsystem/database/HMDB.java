package cn.tellyouwhat.checkinsystem.database;

public interface HMDB {
	String NAME = "hm.db";
	int VERSION = 1;

	public interface Account {
		String TABLE_NAME = "account";

		String COLUMN_ID = "_id";
		String COLUMN_ACCOUNT = "account";
		String COLUMN_NAME = "name";
		String COLUMN_SEX = "sex";
		String COLUMN_ICON = "icon";
		String COLUMN_SIGN = "sign";
		String COLUMN_AREA = "area";
		String COLUMN_TOKEN = "token";
		String COLUMN_CURRENT = "current";

		String SQL_CREATE_TABLE = "create table " + TABLE_NAME + " ("
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_ACCOUNT + " text," + COLUMN_NAME + " text,"
				+ COLUMN_SEX + " integer," + COLUMN_ICON + " text,"
				+ COLUMN_SIGN + " text," + COLUMN_AREA + " text,"
				+ COLUMN_TOKEN + " text," + COLUMN_CURRENT + " integer" + ")";
	}

	public interface Friend {
		String TABLE_NAME = "friend";
		String COLUMN_ID = "_id";
		String COLUMN_OWNER = "owner";
		String COLUMN_ACCOUNT = "account";
		String COLUMN_NAME = "name";
		String COLUMN_SIGN = "sign";
		String COLUMN_AREA = "area";
		String COLUMN_ICON = "icon";
		String COLUMN_SEX = "sex";
		String COLUMN_NICKNAME = "nick_name";
		String COLUMN_ALPHA = "alpha";
		String COLUMN_SORT = "sort";

		String SQL_CREATE_TABLE = "create table " + TABLE_NAME + " ("
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_OWNER + " text," + COLUMN_ACCOUNT + " text,"
				+ COLUMN_NAME + " text," + COLUMN_SIGN + " text," + COLUMN_AREA
				+ " text," + COLUMN_ICON + " text," + COLUMN_SEX + " integer,"
				+ COLUMN_NICKNAME + " text," + COLUMN_ALPHA + " text,"
				+ COLUMN_SORT + " integer" + ")";
	}

	public interface Invitation {
		String TABLE_NAME = "invitation";
		String COLUMN_ID = "_id";
		String COLUMN_OWNER = "owner";
		String COLUMN_INVITATOR_ACCOUNT = "invitator_account";// 邀请者的黑信号
		String COLUMN_INVITATOR_NAME = "invitator_name";// 邀请者的名字
		String COLUMN_INVITATOR_ICON = "invitator_icon";// 邀请者的图片
		String COLUMN_CONTENT = "content";// 邀请者的图片
		String COLUMN_AGREE = "agree";// 是否已经同意

		String SQL_CREATE_TABLE = "create table " + TABLE_NAME + " ("
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_OWNER + " text," + COLUMN_INVITATOR_ACCOUNT + " text,"
				+ COLUMN_INVITATOR_ICON + " text," + COLUMN_CONTENT + " text,"
				+ COLUMN_INVITATOR_NAME + " text," + COLUMN_AGREE + " integer"
				+ ")";
	}

	public interface Message {
		int TYPE_TEXT = 0;
		int TYPE_IMAGE = 1;

		String TABLE_NAME = "message";
		String COLUMN_ID = "_id";
		String COLUMN_OWNER = "owner";
		String COLUMN_ACCOUNT = "account";// 接收者或发送者
		String COLUMN_DIRECTION = "direct";// 0:发送 1:接收
		String COLUMN_TYPE = "type";
		String COLUMN_CONTENT = "content";
		String COLUMN_URL = "url";
		String COLUMN_STATE = "state";// 发送状态: 1.正在发送 2.已经成功发送 3.发送失败
		String COLUMN_READ = "read";// 0:未读 1:已读
		String COLUMN_CREATE_TIME = "create_time";

		String SQL_CREATE_TABLE = "create table " + TABLE_NAME + " ("
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_OWNER + " text," + COLUMN_ACCOUNT + " text,"
				+ COLUMN_DIRECTION + " integer," + COLUMN_TYPE + " integer,"
				+ COLUMN_CONTENT + " text," + COLUMN_URL + " text,"
				+ COLUMN_STATE + " integer," + COLUMN_READ + " integer,"
				+ COLUMN_CREATE_TIME + " integer" + ")";
	}

	public interface Conversation {
		String TABLE_NAME = "conversation";

		String COLUMN_ID = "_id";
		String COLUMN_OWNER = "owner";
		String COLUMN_ACCOUNT = "account";
		String COLUMN_ICON = "icon";
		String COLUMN_NAME = "name";
		String COLUMN_CONTENT = "content";
		String COLUMN_UNREAD = "unread_count";
		String COLUMN_UPDATE_TIME = "update_time";

		String SQL_CREATE_TABLE = "create table " + TABLE_NAME + " ("
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_OWNER + " text," + COLUMN_ACCOUNT + " text,"
				+ COLUMN_ICON + " text," + COLUMN_NAME + " text,"
				+ COLUMN_CONTENT + " text," + COLUMN_UNREAD + " integer,"
				+ COLUMN_UPDATE_TIME + " integer" + ")";
	}

	public interface BackTask {
		String TABLE_NAME = "back_task";

		String COLUMN_ID = "_id";
		String COLUMN_OWNER = "owner";
		String COLUMN_PATH = "path";
		String COLUMN_STATE = "state";// 0:未执行 1:正在执行 2:执行完成 

		String SQL_CREATE_TABLE = "create table " + TABLE_NAME + " ("
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_OWNER + " text," + COLUMN_PATH + " text,"
				+ COLUMN_STATE + " integer" + ")";
	}
}
