package hk.ust.stop.dao;

import hk.ust.stop.dao.GoodsInfoProviderMetaData.TableMetaData;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * 
 * @author XJR
 *
 */
public class GoodsInfoProvider extends ContentProvider{

	
	private SQLiteDatabase db;
	
	private static final UriMatcher uriMatcher;
	
	private static final int MY_INFO = 1;
	private static final int MY_INFO_SINGLE = 2;
	private static HashMap<String,String> userProjectionMap;
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(GoodsInfoProviderMetaData.AUTHORITY, "myinfo", MY_INFO);
		uriMatcher.addURI(GoodsInfoProviderMetaData.AUTHORITY, "myinfo/#", MY_INFO_SINGLE);
	
		userProjectionMap = new HashMap<String, String>();
		userProjectionMap.put(TableMetaData._ID, TableMetaData._ID);
		userProjectionMap.put(TableMetaData.USER_ID, TableMetaData.USER_ID);
		userProjectionMap.put(TableMetaData.UPLOAD_FLAG, TableMetaData.UPLOAD_FLAG);
		userProjectionMap.put(TableMetaData.GOODS_ID, TableMetaData.GOODS_ID);
		userProjectionMap.put(TableMetaData.PICTURE_NAME, TableMetaData.PICTURE_NAME);
		userProjectionMap.put(TableMetaData.LONGITUDE, TableMetaData.LONGITUDE);
		userProjectionMap.put(TableMetaData.LATITUDE, TableMetaData.LATITUDE);
		userProjectionMap.put(TableMetaData.PRICE, TableMetaData.PRICE);
		userProjectionMap.put(TableMetaData.GOODS_NAME, TableMetaData.GOODS_NAME);
		userProjectionMap.put(TableMetaData.GOODS_DESCRIPTION, TableMetaData.GOODS_DESCRIPTION);
		
	}
	
	@Override
	public boolean onCreate() {
		//get database object to operate database
		Context context = getContext();
		StopDatabaseHelper dbHelper = new StopDatabaseHelper(context, GoodsInfoProviderMetaData.databaseName);
		db = dbHelper.getReadableDatabase();
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(TableMetaData.TABLE_NAME);
		builder.setProjectionMap(userProjectionMap);
		
		//decide the order in the result
		String orderBy;
		if(TextUtils.isEmpty(sortOrder)) {
			orderBy = TableMetaData.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}
		
		//decide to query a single record or a record set
		Cursor cursor;
		switch (uriMatcher.match(uri)) {
		case MY_INFO:
			// query database use specific selection, 
			// and create the result set cursor
			cursor = builder.query(db, projection, 
					selection, selectionArgs, null, null, orderBy);
			break;
		case MY_INFO_SINGLE:
			// return a single record
			String id = uri.getPathSegments().get(1);
			cursor = builder.query(db, projection,
					"_id = ? ", new String[]{id}, null, null, orderBy);
			break;
		default:
			cursor = builder.query(db, projection, 
					selection, selectionArgs, null, null, orderBy);
			break;
		}
		
		if(null != cursor) {
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = db.insert(TableMetaData.TABLE_NAME, null, values);
		if(rowId > 0) {
			//发出通知给监听器，说明数据已经改变
            //ContentUris为工具类
            Uri insertedUserUri = ContentUris.withAppendedId(TableMetaData.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(insertedUserUri, null);
            
            return insertedUserUri;
		}
		throw new SQLException("Failed to insert row into" + uri);	
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		/*String id = uri.getPathSegments().get(1);

		//if the id is not empty, then we delete the record according to id
		if(!TextUtils.isEmpty(id)) {
			String []whereArgs = new String[1 + selectionArgs.length];
			for(int i = 0; i < selectionArgs.length; i++) {
				whereArgs[i] = selectionArgs[i];
			}
			
			whereArgs[whereArgs.length-1] = id;
			String whereClause = (TextUtils.isEmpty(selection)?" id = ?":"and id = ?");
			
			return db.delete(GoodsInfoProviderMetaData.TABLE_NAME, 
					whereClause, whereArgs);
		} else {
			return db.delete(GoodsInfoProviderMetaData.TABLE_NAME, 
					selection, selectionArgs);
		}*/
		
		// Just simply delete the record with the selection arguments 
		return db.delete(GoodsInfoProviderMetaData.TABLE_NAME, 
				selection, selectionArgs);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int result = db.update(GoodsInfoProviderMetaData.TABLE_NAME, 
				values, selection, selectionArgs);
		return result;
	}

}
