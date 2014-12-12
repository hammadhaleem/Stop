package hk.ust.stop.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class StopDatabaseHelper extends SQLiteOpenHelper{

	private static final int VERSION = 1;
	private static final String CREATE_GOODS_INFO 
				= " create table GOODS_INFO (" +
				  " _id integer primary key autoincrement" +
				  ",UPLOAD_FLAG integer not null" +
				  ",USER_ID long not null" +
				  ",GOODS_ID long not null" +
				  ",PICTURE_NAME varchar(100) not null" +
				  ",LONGITUDE long not null" +
				  ",LATITUDE long not null" +
				  ",PRICE double not null" +
				  ",GOODS_NAME varchar(100)" +
				  ",GOODS_DESCRIPTION varchar(300) )";
	
	
	public StopDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	
	public StopDatabaseHelper(Context context, String name){
		this(context, name, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_GOODS_INFO);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
