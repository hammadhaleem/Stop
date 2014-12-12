package hk.ust.stop.activity;

import hk.ust.stop.dao.PictureDaoImpl;
import hk.ust.stop.util.ConnectionUtil;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class AddGoodsActivity extends Activity implements OnClickListener{

	private Button captureButton;
	private Button deleteButton;
	private Button resetButton;
	private Button saveButton;
	private ImageView imageView;
	private EditText productName;
	private EditText productDescription;
	private EditText productPrice;
	
	private String tempPicPath;
	private Bitmap currentBitmap;
	private String currentFileName;
	private PictureDaoImpl dao;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addgoods);
		handler = new Handler();
		
		dao = PictureDaoImpl.getInstance();
		
		// Initialize these views
		captureButton = (Button) findViewById(R.id.captureButton);
		deleteButton = (Button) findViewById(R.id.deleteButton);
		resetButton = (Button) findViewById(R.id.resetButton);
		saveButton = (Button) findViewById(R.id.saveButton);
		imageView = (ImageView) findViewById(R.id.imageView);
		productName = (EditText) findViewById(R.id.productName);
		productDescription = (EditText) findViewById(R.id.productDescription);
		productPrice = (EditText) findViewById(R.id.productPrice);
		
		tempPicPath = dao.getDirectory() + "temp.jpg";

		captureButton.setOnClickListener(this);
		deleteButton.setOnClickListener(this);
		resetButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_goods, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	

	/**
	 *  When the user finish taking picture, then enter this method.
	 *  This method would change the size of the picture, so that it
	 *  can fit the ImageView
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Activity.RESULT_FIRST_USER == requestCode) {
			String sdStatus = Environment.getExternalStorageState();
			if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // ¼ì²âsdÊÇ·ñ¿ÉÓÃ
				return;
			}

			File file = new File(tempPicPath);
			
			if (file.exists()) {
				Toast.makeText(this, file.getName(), Toast.LENGTH_LONG).show();
				BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
				bmpFactoryOptions.inJustDecodeBounds = true;

				int dw = imageView.getWidth();
				int dh = imageView.getHeight();
				currentBitmap = BitmapFactory.decodeFile(tempPicPath,
						bmpFactoryOptions);
				int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight
						/ (float) dh);
				int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth
						/ (float) dw);

				if (heightRatio > 1 && widthRatio > 1) {

					if (heightRatio > widthRatio) {

						bmpFactoryOptions.inSampleSize = heightRatio;
					} else {
						bmpFactoryOptions.inSampleSize = widthRatio;
					}

				}
				
				bmpFactoryOptions.inJustDecodeBounds = false;
				currentBitmap = BitmapFactory.decodeFile(tempPicPath, bmpFactoryOptions);
				imageView.setImageBitmap(currentBitmap);
				
				// Delete the previous picture
				if(null != currentFileName)
					dao.deleteBitmap(currentFileName);
				
				currentFileName = ""+DateFormat.format("yyyyMMdd_hhmmss",
						Calendar.getInstance(Locale.CHINA));
				
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.captureButton:
			// Call system camera to take a picture, and save it in a temp file
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File tempPicture = new File(tempPicPath);
			if(tempPicture.exists())
				tempPicture.delete();
			Uri uri = Uri.fromFile(tempPicture);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			startActivityForResult(intent, 1);
			break;
		case R.id.deleteButton:
			// Delete the picture 
			currentBitmap = null;
			imageView.setImageResource(R.drawable.ic_launcher);
			break;
		case R.id.resetButton:
			// Reset the ImageView and EditText
			resetActivity();
			break;
		case R.id.saveButton:
			/**
			 * Save the picture in SD card, and also save it in the server
			 */
			if(isReadyToSave()) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						ConnectionUtil.uploadFile(currentBitmap, currentFileName);
					}
				}).start();
				/** Do some real saving work here **/
				dao.saveImageToSdcard(currentBitmap, currentFileName);
				resetActivity();
			} else {
				Toast.makeText(this, productPrice.getText().toString(), 
						Toast.LENGTH_SHORT).show();
			}
			
			break;

		default:
			break;
		}
	}
	
	
	/**
	 * Reset this activity, cleat the EditText and the ImageView
	 */
	private void resetActivity() {
		productName.setText("");
		productDescription.setText("");
		productPrice.setText("");
		currentBitmap = null;
		currentFileName = null;
		imageView.setImageResource(R.drawable.ic_launcher);
	}

	/**
	 * To check whether the user has taken a picture and 
	 * filled in all the EditText
	 * @return
	 */
	private boolean isReadyToSave() {
		if(null == currentBitmap || 
				null == currentFileName ||
				TextUtils.isEmpty(productName.getText().toString()) ||
				TextUtils.isEmpty(productDescription.getText().toString()) ||
				TextUtils.isEmpty(productPrice.getText().toString())) {
			return false;
		} else {
			return true;
		}
	}
	
}
