package hk.ust.stop.util;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

	/**
	 * @param userName
	 * @param password
	 * @return
	 * @throws JSONException
	 */
	public static String userInfo2Json(String userName, String password) {

		JSONObject sendJo = new JSONObject();
		try {
			sendJo.put("userName", userName);
			sendJo.put("password", password);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String content = sendJo.toString();
		return content;

	}

	/**
	 * @param responseData
	 * @param key
	 * @return
	 * @throws JSONException
	 */
	public static String jsonObjectTransfer(String responseData, String key) {

		JSONObject jo;
		String reply = null;
		try {
			jo = new JSONObject(responseData);
			reply = jo.getString(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return reply;
	}
	
}
