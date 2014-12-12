package hk.ust.stop.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.os.Environment;

/**
 * This class is implemented with Singleton Pattern
 * @author XJR
 *
 */
public class PictureDaoImpl {
	
	private String appBaseDir;
	private static PictureDaoImpl dao = null;
	
	/**
	 * Use this method to get an instance of this class
	 * @return
	 */
	public static PictureDaoImpl getInstance() {
		if(null == dao) {
			synchronized (PictureDaoImpl.class) {
				if(null == dao) {
					dao = new PictureDaoImpl();
				}
			}
		}
		
		return dao;
	}
	
	/**
	 * Set the constructor to private, so that the user can only 
	 * create object via getInstance() function.
	 */
	private PictureDaoImpl() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if(sdCardExist){
			// If SD card exist, then get root directory
			sdDir = Environment.getExternalStorageDirectory();
		}
		else{
			// If SD card don't exist, then get system directory
			sdDir = Environment.getDownloadCacheDirectory();
		}
		
		appBaseDir = null;
		if(sdDir != null){
			appBaseDir =  sdDir.getPath()+"/StopForAndroid/Pictures/";
			File dirFile = new File(appBaseDir);
			
			// If the folder doesn't exist in SD card, we will create it
			if(!dirFile.exists())
				dirFile.mkdirs();
		}
	}
	
	
	public String getDirectory() {
		
		return appBaseDir;
	}
	
	
	public void saveImageToSdcard(Bitmap bmp, String fileName) {
		if(bmp == null)
			return;

		File file = new File(getDirectory()+fileName+".jpg");
		
		try {
			file.createNewFile();
			OutputStream outputStream = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean deleteBitmap(String fileName) {
		File file = new File(getDirectory()+fileName+".jpg");
		return file.delete();
	}
	
}
