package hk.ust.stop.activity;

import hk.ust.stop.model.GoodsInformation;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GoodsInfoActivity extends Activity {

	private TextView goodsName;
	private TextView goodsPrice;
	private TextView goodsPlace;
	private Button returnToSearchList;
	
	private GoodsInformation goodsItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		String serializableKey = extras.getString("SerializableKey");
		goodsItem = (GoodsInformation)extras.getSerializable(serializableKey);

		initView();
		initEvent();

	}

	private void initView() {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_goodsinfo);
		goodsName = (TextView) findViewById(R.id.goodsNameItem);
		goodsPrice = (TextView) findViewById(R.id.goodsPriceItem);
		goodsPlace = (TextView) findViewById(R.id.goodsPlaceItem);
		returnToSearchList = (Button) findViewById(R.id.returnToSearchList);
		
		goodsName.setText(goodsItem.getGoodsName());
		goodsPrice.setText(goodsItem.getPrice()+"HKD");
		goodsPlace.setText("place");

	}

	private void initEvent() {

		returnToSearchList.setOnClickListener(new returnToSearchListListener());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.first, menu);
		return true;
	}

	class returnToSearchListListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			finish();
		}

	}

}
