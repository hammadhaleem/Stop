package hk.ust.stop.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity
			implements OnClickListener{

	private EditText nameEditText;
	private EditText passwordEditText;
	private Button loginButton;
	private Button registerButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity_layout);
		nameEditText = (EditText)findViewById(R.id.edittext_user_name);
		passwordEditText = (EditText)findViewById(R.id.edittext_user_password);
		loginButton = (Button)findViewById(R.id.button_login);
		registerButton = (Button)findViewById(R.id.button_register);
		loginButton.setOnClickListener(this);
		registerButton.setOnClickListener(this);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("back");
	}

	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		// TODO Auto-generated method stub
		return super.onCreateView(name, context, attrs);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent();
			intent.setClass(this, MainActivity.class);
			startActivity(intent);
			finish();
			return true;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
		
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.button_login:
			Toast.makeText(this, nameEditText.getText().toString()+"***"+
					passwordEditText.getText().toString(), Toast.LENGTH_LONG).show();
			intent.setClass(this, MainActivity.class);
			intent.putExtra("isLogin", true);
			startActivity(intent);
			finish();
			break;
		case R.id.button_register:
			intent.setClass(this, RegisterActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { 
			Intent intent = new Intent();
			intent.setClass(this, MainActivity.class);
			startActivity(intent);
			finish();
			return true;
		} 
		return super.onKeyDown(keyCode, event);
	}

	
}
