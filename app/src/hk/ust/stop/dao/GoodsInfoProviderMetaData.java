package hk.ust.stop.dao;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Some constants for content provider
 * @author XJR
 *
 */
public class GoodsInfoProviderMetaData {

	public final static String databaseName = "StopDatabase.db";
	public final static String AUTHORITY = "hk.ust.stop.dao.goodsinfoprovider";
	public final static String TABLE_NAME = "GOODS_INFO";
	public final static long DEFAULT_USER = 0;
	public final static int UPLOADED_PRODUCT = 0;
	public final static int PURCHASED_PRODUCT = 1;
	
	public final static class TableMetaData implements BaseColumns {
		public final static Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/myinfo");
		public final static String TABLE_NAME = "GOODS_INFO";
		public final static String UPLOAD_FLAG = "UPLOAD_FLAG";
		public final static String USER_ID = "USER_ID"; 
		public final static String GOODS_ID = "GOODS_ID";
		public final static String PICTURE_NAME = "PICTURE_NAME";
		public final static String LONGITUDE = "LONGITUDE";
		public final static String LATITUDE = "LATITUDE";
		public final static String PRICE = "PRICE";
		public final static String GOODS_NAME = "GOODS_NAME";
		public final static String GOODS_DESCRIPTION = "GOODS_DESCRIPTION";
		public final static String DEFAULT_SORT_ORDER = "_id desc";
	}
}
