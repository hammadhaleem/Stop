package hk.ust.stop.dao;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import hk.ust.stop.dao.GoodsInfoProviderMetaData.TableMetaData;
import hk.ust.stop.idao.BaseDaoInterface;
import hk.ust.stop.model.GoodsInformation;
import hk.ust.stop.model.UserInformation;

public class BaseDaoImpl implements BaseDaoInterface{

	private ContentResolver resolver;
	
	public BaseDaoImpl(ContentResolver resolver) {
		this.resolver = resolver;
	}
	
	@Override
	public boolean insert(UserInformation user, GoodsInformation info, int flag) {
		ContentValues values = new ContentValues();
		long userId = 0;
		if(null != user) {
			userId = user.getUserId();
		}
		
		// set attribute value
		values.put(TableMetaData.USER_ID, userId);
		values.put(TableMetaData.UPLOAD_FLAG, flag);
		values.put(TableMetaData.GOODS_ID, info.getGoodsId());
		values.put(TableMetaData.PICTURE_NAME, info.getPictureName());
		values.put(TableMetaData.LONGITUDE, info.getLongitude());
		values.put(TableMetaData.LATITUDE, info.getLatitude());
		values.put(TableMetaData.PRICE, info.getPrice());
		values.put(TableMetaData.GOODS_NAME, info.getGoodsName());
		values.put(TableMetaData.GOODS_DESCRIPTION, info.getGoodsDescription());
		
		Uri uri = resolver.insert(TableMetaData.CONTENT_URI, values);
		if(null != uri)
			return true;
		else
			return false;
	}

	@Override
	public ArrayList<GoodsInformation> queryAllRecord(
						UserInformation user, int flag) {
		
		String selection = TableMetaData.USER_ID + "=? and " +
							TableMetaData.UPLOAD_FLAG + "=? ";
		String selectionArgs[] = new String[2];
		
		if(null != user) {
			// If the user is not null, use the userId to query
			selectionArgs[0] = user.getUserId()+"";
		} else {
			// If the user is null, query the default user
			selectionArgs[0] = GoodsInfoProviderMetaData.DEFAULT_USER+"";
		}
		selectionArgs[1] = flag+"";
		
		Cursor cursor = resolver.query(TableMetaData.CONTENT_URI, 
						null, selection, selectionArgs, null);
		ArrayList<GoodsInformation> list = convertCursorToArrayList(cursor);
		
		return list;
	}

	@Override
	public boolean deleteByUserAndGoodsId(UserInformation user,
			GoodsInformation goods) {
		String whereClause = TableMetaData.USER_ID + "=? and " +
							TableMetaData.GOODS_ID + "=?";
		String selectionArgs[] = new String[2];
		
		if(null != user) {
			// If the user is not null, use the userId to query
			selectionArgs[0] = user.getUserId()+"";
		} else {
			// If the user is null, query the default user
			selectionArgs[0] = GoodsInfoProviderMetaData.DEFAULT_USER + "";
		}
		
		selectionArgs[1] = goods.getGoodsId()+"";
		int rowsDeleted = resolver.delete(TableMetaData.CONTENT_URI, 
								whereClause, selectionArgs);
		
		if(rowsDeleted <= 0) {
			//If there are no rows are deleted, that means fail
			return false;
		} else {
			return true;
		}
	}

	
	/**
	 * Traverse the cursor and put the result into a ArrayList
	 * @param cursor
	 * @return
	 */
	private ArrayList<GoodsInformation> convertCursorToArrayList(Cursor cursor) {
		ArrayList<GoodsInformation> list = new ArrayList<GoodsInformation>();
		
		// Traverse the cursor, convert the cursor to ArrayList
		while(cursor.moveToNext()) {
			GoodsInformation goods = new GoodsInformation();
			goods.setGoodsId(cursor.getLong(cursor.getColumnIndex(TableMetaData.GOODS_ID)));
			goods.setGoodsName(cursor.getString(cursor.getColumnIndex(TableMetaData.GOODS_NAME)));
			goods.setPictureName(cursor.getString(cursor.getColumnIndex(TableMetaData.PICTURE_NAME)));
			goods.setLongitude(cursor.getDouble(cursor.getColumnIndex(TableMetaData.LONGITUDE)));
			goods.setLatitude(cursor.getDouble(cursor.getColumnIndex(TableMetaData.LATITUDE)));
			goods.setPrice(cursor.getDouble(cursor.getColumnIndex(TableMetaData.PRICE)));
			goods.setGoodsDescription(cursor.getString(cursor.getColumnIndex(TableMetaData.GOODS_DESCRIPTION)));
			list.add(goods);
		}
		
		return list;
	}
	
	private GoodsInformation convertCursorToSingleObject(Cursor cursor) {
		ArrayList<GoodsInformation> list = convertCursorToArrayList(cursor);
		if( null != list && 0 != list.size() ) {
			GoodsInformation goods= list.get(0);
			return goods;
		}
		
		return null;
	}
}
