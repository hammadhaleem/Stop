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

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public abstract class BaseListActivity extends ListActivity implements OnItemClickListener{

	public static String SERIALIZABLE_KEY;

	private View header;
	// checkBox in header
	private CheckBox checkBox;
	
	private Handler handler;
	private Thread currentThread;
	
	// record the first cursor of listView for data, currentNum X times the number of batchSize
	private int currentNum;
	// max records shown on one page
	private int batchSize;
	
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
		// initial value for listView
		initValue();
		// bind events
		initEvent();
		// initial Handler and ListView
		initHandler();
		// get data
		initData();
		
		serverData = new ArrayList<GoodsInformation>();
		adapterData = new ArrayList<GoodsInformation>();
		selectedData = new ArrayList<GoodsInformation>();
		
	}
	
	/**
	 * initial GUI
	 */
	public abstract void initView();
	
	/**
	 *  initial value for listView
	 */
	public abstract void initValue();
	
	/**
	 * bind events (scroll event and itemClick event)
	 */
	private void initEvent(){
		
		listView.setOnItemClickListener(this);
		// set pull-down-refresh listener in self-defined widget
		refreshableView.setOnRefreshListener(new MyPullToRefreshListener(), 0);
		// set pull-up-load listener in self-defined widget
		refreshableView.setOnLoadListener(new MyPullToLoadMoreListener());
		
	}
	
	/**
	 * initial Handler, set data, and initial adapter
	 */
	public abstract void initHandler();
	
	/**
	 *  get data for initialization
	 */
	public abstract void initData();
	
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
		bundle.putSerializable(SERIALIZABLE_KEY, (Serializable) itemObject);
		Intent intent = new Intent();
		intent.setClass(this, GoodsInfoActivity.class);
		intent.putExtras(bundle);
		intent.putExtra("SerializableKey", SERIALIZABLE_KEY);
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
						initData();
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

	public View getHeader() {
		return header;
	}

	public void setHeader(View header) {
		this.header = header;
	}

	public int getCurrentNum() {
		return currentNum;
	}

	public void setCurrentNum(int currentNum) {
		this.currentNum = currentNum;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
	
	
}
