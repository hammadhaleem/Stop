package hk.ust.stop.adapter;

import hk.ust.stop.activity.R;
import hk.ust.stop.model.GoodsInformation;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class CommonListAdapter extends BaseAdapter {

	// data source
	private List<GoodsInformation> data;
	// context
	private Context context;

	// view-holder model
	private ViewHolder holder;
	
	// if all items are chosen
	private boolean isFullChecked;
	
	public List<GoodsInformation> getData() {
		return data;
	}

	public void setData(List<GoodsInformation> data) {
		this.data = data;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public boolean getFullChecked() {
		return isFullChecked;
	}

	public void setFullChecked(boolean isFullChecked) {
		this.isFullChecked = isFullChecked;
	}

	/**
	 * the number of items shown in listView
	 */
	@Override
	public int getCount() {
		return data.size();
	}

	/**
	 * get data binded to the item
	 */
	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	/**
	 * return itemId
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * the recall times depends on how many items shown in listView
	 * @param position
	 * @param convertView £ºif listView can't show all items, previous view will be used for many times
	 */
	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {

		if (convertView == null) {
			// get context from Activity
			convertView = LayoutInflater.from(context).inflate(R.layout.goodsinfo_list_item, null);
			holder = new ViewHolder();
			holder.goodsNameTextView = (TextView) convertView.findViewById(R.id.goodsName);
			holder.goodsPriceTextView = (TextView) convertView.findViewById(R.id.goodsPrice);
			holder.goodsPlaceTextView = (TextView) convertView.findViewById(R.id.goodsPlace);
			holder.goodsSelectCheckBox = (CheckBox) convertView.findViewById(R.id.goodsSelect);
			
			// set the tag(object) associated with this view
			convertView.setTag(holder);
			
			holder.goodsSelectCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					// get value of position that we have set for checkBox using setTag()
					int position = (Integer) buttonView.getTag();
					// save state of checkBox in data list
					data.get(position).setSelected(isChecked);
					
					// if an item is unchecked, then set header checkBox to false at the same time
					if(isFullChecked && !(data.get(position).getSelected()) ){
						CheckBox checkBox = (CheckBox) parent.findViewById(R.id.fullSelect);
						checkBox.setChecked(false);
					}
					
				}
			});
			
		} else {
			// return this view's tag
			holder = (ViewHolder) convertView.getTag();
		}
		
		// save value of position for checkBox in each item
		holder.goodsSelectCheckBox.setTag(position);
		
		holder.goodsNameTextView.setText(data.get(position).getGoodsName());
		holder.goodsPriceTextView.setText(data.get(position).getPrice() + "");
		// get state of checkBox in data list
		holder.goodsSelectCheckBox.setChecked(data.get(position).getSelected());
		
		return convertView;

	}
	
	/**
	 * observer pattern
	 */
	static class ViewHolder {
		TextView goodsNameTextView;
		TextView goodsPriceTextView;
		TextView goodsPlaceTextView;
		CheckBox goodsSelectCheckBox;
	}

}
