package hk.ust.stop.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtil {

	// pop up Toast 
	public static void showToast(Context applicationContext, String msg) {
		Toast toast = Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
}
