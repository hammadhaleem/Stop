package hk.ust.stop.widget;

import hk.ust.stop.activity.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnTouchListener;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * self-defined refresh widget view
 * @author MIKE
 */
public class RefreshableView extends LinearLayout implements OnTouchListener{

	/**
	 * pull down state
	 */
	public static final int STATUS_PULL_TO_REFRESH = 0;

	/**
	 * release to refresh state
	 */
	public static final int STATUS_RELEASE_TO_REFRESH = 1;

	/**
	 * refreshing state
	 */
	public static final int STATUS_REFRESHING = 2;

	/**
	 * have refreshed or not do refreshing
	 */
	public static final int STATUS_REFRESH_FINISHED = 3;
	
	/**
	 * have loaded or not do loading
	 */
	public static final int STATUS_LOAD_FINISHED = 0;
	
	/**
	 * loading state
	 */
	public static final int STATUS_LOADING = 1;

	/**
	 * speed of rolling back for pull-down header
	 */
	public static final int SCROLL_SPEED = -20;

	/**
	 * millisecond for 1 minute : used to judge the last updating time
	 */
	public static final long ONE_MINUTE = 60 * 1000;

	/**
	 * millisecond for 1 hour : used to judge the last updating time
	 */
	public static final long ONE_HOUR = 60 * ONE_MINUTE;

	/**
	 * millisecond for 1 day : used to judge the last updating time
	 */
	public static final long ONE_DAY = 24 * ONE_HOUR;

	/**
	 * millisecond for 1 month : used to judge the last updating time
	 */
	public static final long ONE_MONTH = 30 * ONE_DAY;

	/**
	 * millisecond for 1 year : used to judge the last updating time
	 */
	public static final long ONE_YEAR = 12 * ONE_MONTH;

	/**
	 * constant String for last updating time, as the key of SharedPreferences
	 */
	private static final String UPDATED_AT = "updated_at";

	/**
	 * recall interface for pulling down refresh
	 */
	private PullToRefreshListener mRefreshListener;
	
	/**
	 * recall interface for pulling up to load
	 */
	private PullToLoadMoreListener mLoadMoreListener;

	/**
	 * save the last updating time
	 */
	private SharedPreferences preferences;

	/**
	 * pull-down View
	 */
	private View header;
	
	/**
	 * pull-up View
	 */
	private View footer;

	/**
	 * ListView object for pulling down operation
	 */
	private ListView listView;

	/**
	 * shown progress bar when updating
	 */
	private ProgressBar progressBar;

	/**
	 * arrow signal for showing pull-down and releasing operation
	 */
	private ImageView arrow;

	/**
	 * words description for showing pull-down and releasing operation
	 */
	private TextView description;

	/**
	 * words description for for last updating time
	 */
	private TextView updateAt;

	/**
	 * layout parameter for pull-down header
	 */
	private MarginLayoutParams headerLayoutParams;

	/**
	 * milliseconds for last updating time
	 */
	private long lastUpdateTime;

	/**
	 * this id is used to distinguish updating operations in different layouts
	 */
	private int mId = -1;

	/**
	 * height of pull-down header
	 */
	private int hideHeaderHeight;

	/**
	 * current operation status to handle, options : STATUS_PULL_TO_REFRESH,
	 *  STATUS_RELEASE_TO_REFRESH, STATUS_REFRESHING and STATUS_REFRESH_FINISHED
	 */
	private int currentStatus = STATUS_REFRESH_FINISHED;

	/**
	 * save last operation status to avoid duplication
	 */
	private int lastStatus = currentStatus;
	
	/**
	 * load operation status to handle, options : STATUS_LOAD_FINISHED, STATUS_LOADING
	 */
	private int loadStatus = STATUS_LOAD_FINISHED;

	/**
	 * y-coordinate while pressing by finger
	 */
	private float yDown;

	/**
	 * slop(max value) to judge a scrolling operation
	 */
	private int touchSlop;

	/**
	 * if have loaded for layout, only need to load once for initialization in onLayout()
	 */
	private boolean loadOnce;

	/**
	 * if allowed to pull down to refresh, it's only allowed when ListView scrolls to the top
	 */
	private boolean ableToPull;
	
	/**
	 * constructor for initialization, including adding a pull-down header
	 * @param context
	 * @param attrs
	 */
	@SuppressLint("InflateParams")
	public RefreshableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		header = LayoutInflater.from(context).inflate(R.layout.pull_down_to_refresh, null, true);
		footer = LayoutInflater.from(context).inflate(R.layout.pull_up_to_load, null, true); 
		progressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
		arrow = (ImageView) header.findViewById(R.id.arrow);
		description = (TextView) header.findViewById(R.id.description);
		updateAt = (TextView) header.findViewById(R.id.updated_at);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 3;
		refreshUpdatedAtValue();
		setOrientation(VERTICAL);
		addView(header, 0);
	}

	/**
	 * vital initialization operations,
	 * such as hiding pull-down header by setting negative offset and bind event listener
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed && !loadOnce) {
			hideHeaderHeight = -header.getHeight();
			headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
			headerLayoutParams.topMargin = hideHeaderHeight;
			header.setLayoutParams(headerLayoutParams);
			listView = (ListView) getChildAt(1);
			listView.setOnTouchListener(this);
			loadOnce = true;
		}
	}

	/**
	 * recall when listView is touched, this method handles all kinds of pull-down-refresh logic 
	 * tip1: as for y-axis, the value is positive downward;
	 * tip2: the first pressing motion will trigger DOWN/UP/MOVE at the same time, 
	 *       then recall MOVE motion all the time
	 **/
	public static boolean result = false;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		setIsAbleToPull(event);
		if (ableToPull) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: 
				result = false;
				yDown = event.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				result = true;
				float yMove = event.getRawY();
				int distance = (int) (yMove - yDown);
				
				// if the gesture is pull-up and pull-down header is completely hidden
				if (distance <= 0 && headerLayoutParams.topMargin <= hideHeaderHeight) {
					
					// if distance < slot, it could be sensitive motion
					if( Math.abs(distance) < touchSlop){
						break;
					}
					
					// concrete logic for pull-up-to-load event
					if (listView.getLastVisiblePosition() == listView.getCount()-1) {
						// judge the loadStatus to avoid duplicate loading
						if(loadStatus != STATUS_LOADING && mLoadMoreListener != null ){
							mLoadMoreListener.onLoadMore();
						}
					}
					
					// then forbid pull-down-refresh event
					result = false;
					return false;
				}
				
				// if if the gesture is pull-down and distance < slot, it could be sensitive motion
				if (distance < touchSlop) {
					result = false;
					return false;
				}
				
				if (currentStatus != STATUS_REFRESHING) {
					if (headerLayoutParams.topMargin > 0) {
						currentStatus = STATUS_RELEASE_TO_REFRESH;
					} else {
						currentStatus = STATUS_PULL_TO_REFRESH;
					}
					// realize pull-down effect by changing the topMargin value of pull-down header
					headerLayoutParams.topMargin = (distance / 2) + hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
				}
				
				break;
			case MotionEvent.ACTION_UP: 
				// performs all normal actions associated with clicking
				v.performClick();
			default:
				if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
					// recall refresh-task if the current loosing state is release-to-refresh
					new RefreshingTask().execute();
				} else if (currentStatus == STATUS_PULL_TO_REFRESH) {
					// recall hide-header task if the current loosing state is pull-to-refresh
					new HideHeaderTask().execute();
				}
				break;
			}
			// update the information in pull-down header
			if( currentStatus==STATUS_PULL_TO_REFRESH || currentStatus==STATUS_RELEASE_TO_REFRESH ){
				updateHeaderView();
				// let listView lose focus, or the touched item will always be chosen
				listView.setPressed(false);
				listView.setFocusable(false);
				listView.setFocusableInTouchMode(false);
				lastStatus = currentStatus;
				// intercept the scroll event for listView by returning true
				return true;
			}
		}
		return false;
	}

	/**
	 * bind a listener for pull-down-refresh widget
	 * @param listener : concrete refresh listener
	 * @param id : duplication avoidance for same operations in various layouts
	 */
	public void setOnRefreshListener(PullToRefreshListener listener, int id) {
		mRefreshListener = listener;
		mId = id;
	}

	/**
	 * recall this method when finishing the refreshing logic, or listView is still in updating state
	 */
	public void finishRefreshing() {
		currentStatus = STATUS_REFRESH_FINISHED;
		preferences.edit().putLong(UPDATED_AT + mId, System.currentTimeMillis()).commit();
		new HideHeaderTask().execute();
	}

	/**
	 * bind a listener for pull-up-load widget
	 * @param listener : concrete load listener
	 */
	public void setOnLoadListener(PullToLoadMoreListener listener) {
		mLoadMoreListener = listener;
	}
	
	/**
	 * recall this method when loading, change loadState to STATUS_LOADING
	 */
	public void isLoading() {
		loadStatus = STATUS_LOADING;
	}
	
	/**
	 * recall this method when finishing, change loadState to STATUS_LOAD_FINISHED
	 */
	public void finishLoading() {
		loadStatus = STATUS_LOAD_FINISHED;
	}
	
	/**
	 * set the value of {@link #ableToPull} according to current scrolling state of listView, 
	 * this method should be the first execution in onTouch event,
	 * helping judge the listView is scrolling or pulling down to refresh
	 * @param event
	 */
	private void setIsAbleToPull(MotionEvent event) {
		int largestListCount = listView.getCount()-1;
		View firstChild = listView.getChildAt(0); 
		if (firstChild != null) {
			int firstVisiblePos = listView.getFirstVisiblePosition();
			int lastVisiblePos = listView.getLastVisiblePosition();
			// if the top of the first item reaches the top of parent layout
			if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
				if (!ableToPull) {
					yDown = event.getRawY();
				}
				// pull-down-refresh operation should be allowed
				ableToPull = true;
			// or if the bottom of the last item reaches the bottom of parent layout
			} else if( lastVisiblePos == largestListCount ) {
				ableToPull = true;
			}else {
				if (headerLayoutParams.topMargin != hideHeaderHeight) {
					headerLayoutParams.topMargin = hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
				}
				ableToPull = false;
			}
		} else {
			// if there is no item in ListView, pull-down-refresh operation should also be allowed
			ableToPull = true;
		}
	}

	/**
	 * update the information in pull-down header
	 */
	private void updateHeaderView() {
		if (lastStatus != currentStatus) {
			if (currentStatus == STATUS_PULL_TO_REFRESH) {
				description.setText(getResources().getString(R.string.pull_to_refresh));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
				description.setText(getResources().getString(R.string.release_to_refresh));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_REFRESHING) {
				description.setText(getResources().getString(R.string.refreshing));
				progressBar.setVisibility(View.VISIBLE);
				arrow.clearAnimation();
				arrow.setVisibility(View.GONE);
			}
			refreshUpdatedAtValue();
		}
	}

	/**
	 * rotate the direction of arrow according to the current state
	 */
	private void rotateArrow() {
		float pivotX = arrow.getWidth() / 2f;
		float pivotY = arrow.getHeight() / 2f;
		float fromDegrees = 0f;
		float toDegrees = 0f;
		if (currentStatus == STATUS_PULL_TO_REFRESH) {
			fromDegrees = 180f;
			toDegrees = 360f;
		} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
			fromDegrees = 0f;
			toDegrees = 180f;
		}
		RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
		animation.setDuration(100);
		animation.setFillAfter(true);
		arrow.startAnimation(animation);
	}

	/**
	 * time description for last refreshing operation
	 */
	private void refreshUpdatedAtValue() {
		lastUpdateTime = preferences.getLong(UPDATED_AT + mId, -1);
		long currentTime = System.currentTimeMillis();
		long timePassed = currentTime - lastUpdateTime;
		long timeIntoFormat;
		String updateAtValue;
		if (lastUpdateTime == -1) {
			updateAtValue = getResources().getString(R.string.not_updated_yet);
		} else if (timePassed < 0) {
			updateAtValue = getResources().getString(R.string.time_error);
		} else if (timePassed < ONE_MINUTE) {
			updateAtValue = getResources().getString(R.string.updated_just_now);
		} else if (timePassed < ONE_HOUR) {
			timeIntoFormat = timePassed / ONE_MINUTE;
			String value = (timeIntoFormat>1)?timeIntoFormat+" minutes":timeIntoFormat+" minute";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_DAY) {
			timeIntoFormat = timePassed / ONE_HOUR;
			String value = (timeIntoFormat>1)?timeIntoFormat+ " hours":timeIntoFormat+" hour";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_MONTH) {
			timeIntoFormat = timePassed / ONE_DAY;
			String value = (timeIntoFormat>1)?timeIntoFormat+ " days":timeIntoFormat+" day";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_YEAR) {
			timeIntoFormat = timePassed / ONE_MONTH;
			String value = (timeIntoFormat>1)?timeIntoFormat+ " months":timeIntoFormat+" month";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else {
			timeIntoFormat = timePassed / ONE_YEAR;
			String value = (timeIntoFormat>1)?timeIntoFormat+ " years":timeIntoFormat+" year";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		}
		updateAt.setText(updateAtValue);
	}

	/**
	 * refreshing task, pull-down listener will be recalled in this task
	 */
	class RefreshingTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			int topMargin = headerLayoutParams.topMargin;
			while (true) {
				topMargin = topMargin + SCROLL_SPEED;
				if (topMargin <= 0) {
					topMargin = 0;
					break;
				}
				// this method will trigger the execution of onProgressUpdate on the UI thread
				publishProgress(topMargin);
				sleep(10);
			}
			currentStatus = STATUS_REFRESHING;
			publishProgress(0);
			if (mRefreshListener != null) {
				mRefreshListener.onRefresh();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... topMargin) {
			updateHeaderView();
			headerLayoutParams.topMargin = topMargin[0];
			header.setLayoutParams(headerLayoutParams);
		}

	}

	/**
	 * hide-header task will hide pull-down header again after updating or not updated
	 * P.S. the return value of AsyncTask will be passed to this step as a parameter
	 */
	class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			int topMargin = headerLayoutParams.topMargin;
			while (true) {
				topMargin = topMargin + SCROLL_SPEED;
				if (topMargin <= hideHeaderHeight) {
					topMargin = hideHeaderHeight;
					break;
				}
				publishProgress(topMargin);
				sleep(10);
			}
			return topMargin;
		}

		@Override
		protected void onProgressUpdate(Integer... topMargin) {
			headerLayoutParams.topMargin = topMargin[0];
			header.setLayoutParams(headerLayoutParams);
		}

		@Override
		protected void onPostExecute(Integer topMargin) {
			headerLayoutParams.topMargin = topMargin;
			header.setLayoutParams(headerLayoutParams);
			currentStatus = STATUS_REFRESH_FINISHED;
		}
	}

	/**
	 * set sleep time for the current thread in milliseconds
	 * @param time
	 */
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * add footerView to listView
	 */
	public void addFooterView(){
		
		listView.addFooterView(footer);
		
	}
	
	/**
	 * remove footerView to listView
	 */
	public void removeFooterView(){
		
		listView.removeFooterView(footer);
		
	}

	/**
	 * pull-down-to-refresh listener, it should be implemented and recalled when using updating effect
	 */
	public interface PullToRefreshListener {
		
		/**
		 * this method will be recalled when updating, concrete updating logic should be implemented
		 * P.S. this method is recalled in child Thread, thus no need to create a new Thread
		 */
		void onRefresh();
		
	}
	
	/**
	 * pull-up-to-load listener, it should be implemented and recalled when using loading effect
	 */
	public interface PullToLoadMoreListener {
		
		/**
		 * this method will be recalled when loading, concrete loading logic should be implemented
		 */
		void onLoadMore();
		
	}
	
}
