package hk.ust.stop.activity;

import hk.ust.stop.adapter.CommonListAdapter;
import hk.ust.stop.model.GoodsInformation;
import hk.ust.stop.util.ToastUtil;
import hk.ust.stop.widget.RefreshableView;
import hk.ust.stop.widget.RefreshableView.PullToLoadMoreListener;
import hk.ust.stop.widget.RefreshableView.PullToRefreshListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchListActivity extends ListActivity implements OnItemClickListener{

	public final static String GOODSINFO_KEY = "hk.ust.stop.activity.SearchListActivity";

	private View header;
	private CheckBox checkBox;
	
	private Handler handler;
	private Thread currentThread;
	
	// record the first cursor of listView for data, currentNum X times the number of batchSize
	private int currentNum = 0;
	// max records shown on one page
	private int batchSize = 10;
	
	private List<GoodsInformation> selectedData;
	private List<GoodsInformation> serverData;
	private List<GoodsInformation> adapterData; // model
	private RefreshableView refreshableView; // widget view
	private ListView listView; // sub view 
	private CommonListAdapter adapter; // controller
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		// initial GUI
		initView();
		// bind events
		initEvent();
		// initial Handler and ListView
		initHandler();
		// get data from server
		getDataFromServer();
		
		serverData = new ArrayList<GoodsInformation>();
		adapterData = new ArrayList<GoodsInformation>();
		selectedData = new ArrayList<GoodsInformation>();
		
	}
	
	/**
	 * initial GUI
	 */
	private void initView(){
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_searchlist);
		refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
		listView = getListView(); // the way getting listView in ListActivity
		
		LayoutInflater inflater = getLayoutInflater();
		header = (View)inflater.inflate(R.layout.common_list_header, listView, false);
		checkBox = (CheckBox) header.findViewById(R.id.fullSelect);
		
	}
	
	/**
	 * bind events (scroll event and itemClick event)
	 */
	private void initEvent(){
		
		listView.setOnItemClickListener(this);
		// set pull-down-refresh listener in self-defined widget
		refreshableView.setOnRefreshListener(new MyPullToRefreshListener(), 1);
		// set pull-up-load listener in self-defined widget
		refreshableView.setOnLoadListener(new MyPullToLoadMoreListener());
		
	}
	
	/**
	 * initial Handler, set data, and initial adapter
	 */
	@SuppressLint("HandlerLeak")
	private void initHandler(){
		handler = new Handler() {
			@SuppressWarnings("unchecked")
			public void handleMessage(Message msg) {
				Bundle bundle = msg.getData();
				switch (msg.what) {
				case 1:
					// get data successfully
					List<GoodsInformation> goodsItems = (List<GoodsInformation>) bundle.getSerializable(GOODSINFO_KEY);
					serverData = goodsItems;
					initAdapter();
					ToastUtil.showToast(getApplicationContext(), "get data successfully");
					break;
				case 2:
					// get data unsuccessfully
					ToastUtil.showToast(getApplicationContext(), "fail to get data");
					break;
				default:
					ToastUtil.showToast(getApplicationContext(), "logic error");
					break;
				}
			}
		};
	}
	
	/**
	 *  get data from server
	 */
	private void getDataFromServer(){
		
		 new Thread(new Runnable() {
				@Override
				public void run() {
					
					
					/*String staticUrl = UrlConstant.DISHINFO_URL;
					String responseData = ConnectionUtil.getFromServer(staticUrl);
					List<GoodsItem> dishInfos = Transfer2JsonUtil.dishInfoJsonTransfer(responseData);*/
					
					List<GoodsInformation> goodsItems = new ArrayList<GoodsInformation>();
					
					for (int i = 0; i < 40; i++) {
						goodsItems.add(new GoodsInformation("name"+i,i));
					}
					
						

					Message message=new Message();
					Bundle bundle = new Bundle();
					bundle.putSerializable(GOODSINFO_KEY, (Serializable) goodsItems);
					message.setData(bundle);
					message.what = 1;
					handler.sendMessage(message);
					
				}
		}).start();
		
	}
	
	/**
	 *  initial adapter
	 *  first step: get part of data (model)
	 *  second step: build a new adapter and initial it (controller)
	 *  third step: set adapter for ListView (view)
	 */
	private void initAdapter() {
	
		if (listView == null)
			return;
		
		batchServerData();
		
		adapter = new CommonListAdapter();
		adapter.setContext(this);
		adapter.setData(adapterData);
		adapter.setFullChecked(false);
		// restore checkBox to default state
		checkBox.setChecked(false);

		// addHeaderView or addFooterView has to be called before setAdapter
		listView.addHeaderView(header, "header", false);
		listView.setAdapter(adapter);
		
		
	}
	
	/**
	 *  handle ServerData, set adapterData with serverData of batchSize each time
	 */
 	private void batchServerData() {
		
		if (serverData == null)
			return;

		int totalSize = serverData.size();

		// stop condition
		if (currentNum == totalSize)
			return;

		int showSize;
		int result = currentNum + batchSize - totalSize;
		// result<=0 indicates the number of dishes next time is batchSize
		if(result<=0){
			showSize = batchSize;
		}else{
			// result>0 indicates the number of dishes next time is less than batchSize
			showSize = totalSize - currentNum;
		}
		
		for(int i=0;i<showSize;i++){
			adapterData.add( serverData.get(currentNum+i) );
		}
		
		currentNum += showSize;
		
	}
 	
	/**
	 * handle loading event : update data in adapter and UI,
	 */
	class DataLoadThread extends Thread {
		@Override
		public void run() {
			try {
				// wait for 2000ms : reserve showing time for loading
				Thread.sleep(2000);
				
				// handle server data (cut into batches)
				batchServerData();
				
				// post request to UI thread to update UI
				handler.post(new Runnable() {
					@Override
					public void run() {
						// remove footer view after finishing loading;
						refreshableView.removeFooterView();
						// change loadStatus after finishing loading
						refreshableView.finishLoading();
						// inform listView update data when data is changed
						adapter.notifyDataSetChanged();
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {

		Object itemObject = listView.getItemAtPosition(position);
		Bundle bundle = new Bundle();
		bundle.putSerializable(GOODSINFO_KEY, (Serializable) itemObject);
		Intent intent = new Intent();
		intent.setClass(this, GoodsInfoActivity.class);
		intent.putExtras(bundle);
		intent.putExtra("SerializableKey", GOODSINFO_KEY);
		startActivity(intent);
		
	}
	
	/**
	 * Interface of pull-down-refresh listener
	 * accomplish concrete refresh logic here 
	 */
	class MyPullToRefreshListener implements PullToRefreshListener {

		@Override
		public void onRefresh() {

			try {
				Thread.sleep(2000);

				handler.post(new Runnable() {
					@Override
					public void run() {
						
						adapterData.clear();
						adapterData = new ArrayList<GoodsInformation>();
						serverData.clear();
						// clear signalNum and adaterData
						currentNum = 0;
						// notice to remove previous header before initial adapter again
						listView.removeHeaderView(header);
						// get data from server again for updating
						getDataFromServer();
						adapter.notifyDataSetChanged();
					    
					}
				});

				// change loadStatus after finishing loading
				refreshableView.finishRefreshing();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};
	
	/**
	 * Interface of pull-up-load listener
	 * accomplish concrete loading logic here 
	 */
	class MyPullToLoadMoreListener implements PullToLoadMoreListener{

		@Override
		public void onLoadMore() {
			// if there is more data to load
			if(serverData.size() != currentNum){
				// change loadStatus when loading
				refreshableView.isLoading();
				// add footer view to the tail of listView
				refreshableView.addFooterView();
				// start Thread to load data in batch
				currentThread = new DataLoadThread();
				currentThread.start();
			}else{
				// change loadStatus when not loading
				refreshableView.finishLoading();
			}
		}
		
	}
	
	/**
	 * set selectedData according to selected items in adapterData 
	 * @return number of chosen items
	 */
	private String setSelectedData(){
		
		String chosenNum = "";
		for(int i=0; i<adapterData.size() ;i++){
			GoodsInformation singleData = adapterData.get(i);
			if(singleData.getSelected()){
				String temp = i+" ";
				chosenNum += temp;
				selectedData.add(singleData);
			}
		}
		return chosenNum;
		
	}
	
	/**
	 * bind onClickListener for saveList Button 
	 * @param view
	 */
	public void saveListOnClickListener(View view){
		
		// save selected items to local database like contentProvider
		String nums = setSelectedData();
		
		// test
		ToastUtil.showToast(this, nums);
		
	}
	
	/**
	 * bind onClickListener for showOnMap Button
	 * @param view
	 */
	public void showOnMapOnClickListener(View view){
		
		// upload selected items to server and get optimized route
		String nums = setSelectedData();
		
		// test
		ToastUtil.showToast(this, nums);
		

		// jump to MainActivity with optimized route
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		
	}
	
	/**
	 * bind onClickListener for checkBox
	 * set full-checked state
	 */
	public void onCheckboxClicked(View view){

		if(checkBox.isChecked()){
			ToastUtil.showToast(this, checkBox.isChecked()+"");
			for(GoodsInformation singleData : adapterData){
				singleData.setSelected(true);
			}
			adapter.setFullChecked(true);
			adapter.notifyDataSetChanged();
		}else{
			ToastUtil.showToast(this, checkBox.isChecked()+"");
			for(GoodsInformation singleData : adapterData){
				singleData.setSelected(false);
			}
			adapter.setFullChecked(false);
			adapter.notifyDataSetChanged();
		}
		
	}
	
}