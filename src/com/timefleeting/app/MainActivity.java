package com.timefleeting.app;

import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.MenuDrawer.OnDrawerStateChangeListener;
import net.simonvt.menudrawer.MenuDrawer.OnInterceptMoveEventListener;
import net.simonvt.menudrawer.Position;

import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.sleepbot.datetimepicker.time.TimePickerDialog.OnTimeSetListener;
import com.timefleeting.app.JazzyViewPager;
import com.timefleeting.app.JazzyViewPager.TransitionEffect;
import com.zcw.togglebutton.ToggleButton;
import com.zcw.togglebutton.ToggleButton.OnToggleChanged;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TimingLogger;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;

import com.capricorn.RayMenu;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;

public class MainActivity extends FragmentActivity implements OnTimeSetListener {

	public static final String DATEPICKER_TAG = "选择日期";
    public static final String TIMEPICKER_TAG = "选择时间";
	
	private JazzyViewPager mJazzy;
	private LayoutInflater layoutInflater;	
	
	private List<View> mViewList;
	
	private TextView resultTextView;
	
	private TimeFleetingData timeFleetingData;
	
	private Context mContext;
	
	private ListView listView;
	private ListView pastListView;
	private ListViewAdapter mAdapter;
	private PastListViewAdapter pastAdapter;
	
	private PagerAdapter mainAdapter;
	
	private RayMenu rayMenu;
	private RayMenu pastRayMenu;
	
	// according which to sort
	private boolean isSortByCreateTime = true;
	private boolean isSortedByRemindTime = false;
	private boolean isSortedByTitle = false;
	private boolean isSortedByStar = false;
	
	// the direction of the sort
	private boolean isSortedByCreateTimeReversely = false;
	private boolean isSortedByRemindTimeReversely = false;
	private boolean isSortedByTitleReversely = false;
	private boolean isSortedByStarReversely = false;
	
	// record whether the listview is scrolling
	private boolean scrollFlag = false;
	private int lastVisibleItemPosition = 0;
	private boolean lastIsScrollDown = false;
	
	private boolean pastScrollFlag = false;
	private int pastLastVisibleItemPosition = 0;
	private boolean pastLastIsScrollDown = false;
	
	private View v;
	
	private int statusBarHeight;
	private LinearLayout layout1TitleLinearLayout;
	
	private boolean rayMenuAppeared = false;
	private boolean rayMenuShown = true;
	
	private float pressDownY = -1;
	private int moveDirection = -1;
	private int lastDirection = 1;
	
	private float pastPressDownY = -1;
	private int pastMoveDirection = -1;
	private int pastLastDirection = 1;
	
	private SharedPreferences.Editor editor;
	private SharedPreferences preferences;
	
	private static final int[] ITEM_DRAWABLES_FUTURE = {
		R.drawable.create,
		R.drawable.search,
		R.drawable.over_due,
		};
	
	public static Intent intentService;
	public static Intent intentPastService;
	
	private LinearLayout layoutTitleLinearLayout;
	private TextView layoutTitleTextView;
	private ImageView layoutTitleImageView;
	
	private ImageView layoutTitleMenuImageView;
	private ImageView menuLayoutBackImageView;
	
	private String newRemindTimeString;
	private Record setTimeRecord;
	
	private ToggleButton remindEnableButton;
	private ToggleButton remindPastEnableButton;
	private ToggleButton vibrateEnableButton;
	private ToggleButton soundEnableButton;
	
	private LinearLayout menuAdvancedTimeLinearLayout;
	private Spinner spinner;
	private SpinnerArrayAdapter spinnerArrayAdapter;
	
	private String[] spinnerStrings = {"1 day",
									   "3 days",
									   "1 week",
									   "2 weeks",
									   "1 month"};
	
	private LinearLayout menuRemindTimeLinearLayout;
	private TextView menuRemindTimeTextView;
	
	private MenuDrawer mMenuDrawer;
	
	private int mPagerPosition;
    private float mPagerOffsetPixels;
    
    static final int MIN_DISTANCE = 100;
    private float downX, downY, upX, upY;
    private float pastDownX, pastDownY, pastUpX, pastUpY;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		setContentView(R.layout.activity_main);
		
		mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.BEHIND, Position.LEFT, MenuDrawer.MENU_DRAG_CONTENT);
        mMenuDrawer.setContentView(R.layout.activity_main);
        mMenuDrawer.setMenuView(R.layout.menu_layout);
        mMenuDrawer.setDrawerIndicatorEnabled(true);
		mMenuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_FULLSCREEN);
		mMenuDrawer.setOnInterceptMoveEventListener(new OnInterceptMoveEventListener() {
			
			@Override
			public boolean isViewDraggable(View v, int dx, int x, int y) {
				Log.d("TimeFleeting", "DRAWINGING");
				rayMenu.closeMenu();
//				Log.d("TimeFleeting", "dx: " + dx);
//				Log.d("TimeFleeting", "x: " + x);
//				Log.d("TimeFleeting", "y: " + y);
//				Log.d("TimeFleeting", "Jazzy");
				return mJazzy.getCurrentItem() != 0;
//				if (v == mJazzy) {
//					Log.d("TimeFleeting", "Jazzy");
//					return mJazzy.getCurrentItem() != 0;
//                }
//				return false;
			}
		});
		
        layoutTitleMenuImageView = (ImageView)findViewById(R.id.layout_title_menu_logo);
        layoutTitleMenuImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMenuDrawer.toggleMenu(true);
			}
		});
        

        editor = getSharedPreferences("Values", MODE_PRIVATE).edit();
        preferences = getSharedPreferences("Values", MODE_PRIVATE);

        menuLayoutBackImageView = (ImageView)findViewById(R.id.menu_layout_back);
        menuLayoutBackImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMenuDrawer.toggleMenu(true);
			}
		});
        
		mContext = this;
		statusBarHeight = getStatusBarHeight();
		
		try {
			timeFleetingData = TimeFleetingData.getInstance(this);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		initValues();
		
		mAdapter = new ListViewAdapter(TimeFleetingData.futureRecords, mContext);
		mAdapter.setMode(Attributes.Mode.Single);
		
		pastAdapter = new PastListViewAdapter(TimeFleetingData.pastRecords, mContext);
		pastAdapter.setMode(Attributes.Mode.Single);
		
		layoutInflater = getLayoutInflater().from(this);
		setupJazziness(TransitionEffect.Tablet);
		
		layoutTitleLinearLayout = (LinearLayout)findViewById(R.id.layout_title);
		layoutTitleTextView = (TextView)findViewById(R.id.layout_title_text);
		layoutTitleTextView.setText(GlobalSettings.FUTURE_TITLE);
		layoutTitleImageView = (ImageView)findViewById(R.id.layout_title_imageview);
		
		layoutTitleTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mJazzy.setCurrentItem(1 - mJazzy.getCurrentItem(), true);
			}
		});
		
		layoutTitleImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setSort();
			}
		});
		
		boolean whetherShownSplash = preferences.getBoolean("SHOWN_SPLASH", false);
		
		Bundle bundle = getIntent().getBundleExtra("BUNDLE");
        if(bundle != null){
        	whetherShownSplash = true;
        	Intent intent = new Intent(mContext, EditPastActivity.class);
        	intent.putExtra("isOld", true);
        	intent.putExtra("Title", bundle.getString("TITLE"));
        	intent.putExtra("Content", bundle.getString("CONTENT"));
        	intent.putExtra("RemindTime", bundle.getString("REMINDTIME"));
        	intent.putExtra("Type", bundle.getString("Type"));
			startActivityForResult(intent, 2);
        }
        
        
        if (!whetherShownSplash) {
        	whetherShownSplash = true;
        	editor.putBoolean("SHOWN_SPLASH", true);
        	editor.commit();
        	Intent intent = new Intent(this, Splash.class);
        	startActivity(intent);
        } else {
        	Log.d("TimeFleeting", "SHOWN_SPLAST_TRUE");
        }
	}
	
	// listener to listen whether the EditActivity is finished
	// if finished, notifyDataSetChanged
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK) {
				boolean isEditActivityFinished = data.getBooleanExtra("isEditActivityFinished", false);
				if (isEditActivityFinished) {
					TimeFleetingData.sortFutureRecordByCreateTimeReversely();
					mAdapter.notifyDataSetChanged();
					if (data.getBooleanExtra("isOld", false)) {
						// is old record
						// don't scroll
					} else {
						listView.smoothScrollToPosition(0);
					}
					
					if (GlobalSettings.REMIND_ENABLE) {
				        initReminds();
						LongRunningService.remindList = GlobalSettings.REMIND_LIST;
						stopService(intentService);
						startService(intentService);
					}

				} else {
					Log.d("TimeFleeting", "Future isEditActivityFinished is false");
				}
			}
			break;
		case 2:
			if (resultCode == RESULT_OK) {
				boolean isEditActivityFinished = data.getBooleanExtra("isEditActivityFinished", false);
				if (isEditActivityFinished) {
					TimeFleetingData.sortPastRecordsByLastSort();
					pastAdapter.notifyDataSetChanged();
					
					if (GlobalSettings.REMIND_PAST_ENABLE) {
				        initPastReminds();
						LongRunningPastService.remindList = GlobalSettings.REMIND_PAST_LIST;
						stopService(intentPastService);
						startService(intentPastService);
					}

				} else {
					Log.d("TimeFleeting", "Past isEditActivityFinished is false");
				}
			}
			break;
		default:
			break;
		}
	}

	private void setupJazziness(TransitionEffect effect) {

		initView();
		
		mJazzy = (JazzyViewPager) findViewById(R.id.jazzy_pager);

		mJazzy.setTransitionEffect(TransitionEffect.valueOf("ZoomIn".toString()));
		mainAdapter = new MainAdapter();
		mJazzy.setAdapter(mainAdapter);
		mJazzy.setCurrentItem(1);
		mJazzy.setPageMargin(0);
		mJazzy.setOutlineColor(Color.parseColor("#f4f4f4"));
		
		mJazzy.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
		
		mJazzy.addOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				if (position == 0) {
					layoutTitleTextView.setText(GlobalSettings.PAST_TITLE);
				} else {
					layoutTitleTextView.setText(GlobalSettings.FUTURE_TITLE);
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				rayMenu.closeMenu();
				mAdapter.closeAllItems();
				pastAdapter.closeAllItems();
				mPagerPosition = position;
                mPagerOffsetPixels = positionOffsetPixels;
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
	}

	private class MainAdapter extends PagerAdapter {
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			
//			container.addView(mViewList.get(position));
//			return mViewList.get(position);
//			
			mJazzy.setObjectForPosition(mViewList.get(position), position);
			container.addView(mViewList.get(position), LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			
			return mViewList.get(position);
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object obj) {
			container.removeView(mJazzy.findViewFromObject(position));
//			container.removeView(mViewList.get(position));
		}
		
		@Override
		public int getCount() {
			return mViewList.size();
		}
		
		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
//			if (view instanceof OutlineContainer) {
//				return ((OutlineContainer) view).getChildAt(0) == obj;
//			} else {
//				return view == obj;
//			}
		}		
	}
	
	private void initView() {
		mViewList = new ArrayList<View>();
		
		initView2();
		initView1();

		View v3 = layoutInflater.inflate(R.layout.layout3, null);
		
	}
	
	private void initView2() {
		View v2 = layoutInflater.inflate(R.layout.layout2, null);
		pastListView = (ListView)v2.findViewById(R.id.listview_past);
		pastListView.setAdapter(pastAdapter);

		// sort by the remain time default
		TimeFleetingData.sortPastRecordsByRemainTime();
		
		if (TimeFleetingData.pastRecords.size() > 0) {
			TextView tipsTextView = (TextView)v2.findViewById(R.id.layout_2_tips);
			tipsTextView.setVisibility(View.INVISIBLE);
		} else {
			TextView tipsTextView = (TextView)v2.findViewById(R.id.layout_2_tips);
			tipsTextView.setVisibility(View.VISIBLE);
		}
		
		// on list item click listener
		pastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	if (pastAdapter.isOpen(position)) {
                    return;
            	} else {
            		pastAdapter.closeAllItems();
            		Intent intent = new Intent(mContext, EditPastActivity.class);
	            	intent.putExtra("isOld", true);
	            	intent.putExtra("Title", TimeFleetingData.pastRecords.get(position).getTitle());
	            	intent.putExtra("Content", TimeFleetingData.pastRecords.get(position).getText());
	            	intent.putExtra("CreateTime", TimeFleetingData.pastRecords.get(position).getCreateTime());
	            	intent.putExtra("RemindTime", TimeFleetingData.pastRecords.get(position).getRemindTime());
	            	intent.putExtra("Star", TimeFleetingData.pastRecords.get(position).getStar());
	            	intent.putExtra("Type", TimeFleetingData.pastRecords.get(position).getType());
	            	intent.putExtra("ID", TimeFleetingData.pastRecords.get(position).getId());
	            	intent.putExtra("Status", TimeFleetingData.pastRecords.get(position).getStatus());
	            	intent.putExtra("Top", TimeFleetingData.pastRecords.get(position).getBeTop());
					startActivityForResult(intent, 2);
            	}
            }
        });			

		pastListView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				    case MotionEvent.ACTION_DOWN: {
				    	pastDownX = event.getX();
				    	pastDownY = event.getY();
				    	//   return true;
				    }
				    case MotionEvent.ACTION_UP: {
				    	pastPressDownY = -1;
				    	
				    	pastUpX = event.getX();
				    	pastUpY = event.getY();

				    	float deltaX = pastDownX - pastUpX;
				    	float deltaY = pastDownY - pastUpY;
				    	
				    	// swipe horizontal?
				    	if(Math.abs(deltaX) > MIN_DISTANCE) {
				    		// left or right
				    		if(deltaX > 0) {
				    			boolean isOpen = false;
				    			for (int i = pastListView.getFirstVisiblePosition(); i <= pastListView.getLastVisiblePosition(); i++) {
				    				if (pastAdapter.isOpen(i)) {
				    					isOpen = true;
				    					break;
				    				}
				    			}
				    			if (isOpen) {
				    				pastAdapter.closeAllItems();
				    				return true;
				    			}
				    			return false; 
				    		}
				    		if(deltaX < 0) { 
				    			
				    			return false; 
				    		}
				    	} 
				    }
				    case MotionEvent.ACTION_MOVE: {
				    	if (pastPressDownY == -1) {
							// don't calculate
				    		pastPressDownY = event.getY();
							return false;
						}
						if (event.getY() > pastPressDownY) {
							pastMoveDirection = 1;
						} else if (event.getY() < pastPressDownY) {
							pastMoveDirection = -1;
						}
						pastPressDownY = event.getY();
						return false;
				    }
				}
				return false;
			}
		});
		
		pastListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					pastScrollFlag = false;
					if (pastListView.getLastVisiblePosition() == (pastListView.getCount() - 1)) {
						// to bottom 
						
					}
					if (pastListView.getFirstVisiblePosition() == 0) {
						// to top

					}
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					pastAdapter.closeAllItems();
					pastScrollFlag = true;
					break;
				case OnScrollListener.SCROLL_STATE_FLING:
					pastScrollFlag = false;
					break;
				default:
					break;
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				rayMenu.closeMenu();
				if (pastScrollFlag && 
						ScreenUtil.getScreenViewBottomHeight(pastListView) 
						>= ScreenUtil.getScreenHeight(MainActivity.this) 
						- statusBarHeight 
						- layoutTitleLinearLayout.getHeight()) {
					if (firstVisibleItem > pastLastVisibleItemPosition) {
						// scroll down
						// should disappear
						if (!pastLastIsScrollDown) {
							AnimationSet animationSet = new AnimationSet(true);
							TranslateAnimation translateAnimation = 
									new TranslateAnimation(
											Animation.RELATIVE_TO_SELF, 0f,
											Animation.RELATIVE_TO_SELF, 0f,
											Animation.RELATIVE_TO_SELF, 0f,
											Animation.RELATIVE_TO_SELF, 1f);
							translateAnimation.setDuration(1000);
							animationSet.addAnimation(translateAnimation);
							animationSet.setFillAfter(true);
							pastRayMenu.startAnimation(animationSet);
							rayMenuShown = false;
						} else {
							
						}
						pastLastIsScrollDown = true;
					} else if (firstVisibleItem < pastLastVisibleItemPosition) {
						// scroll up
						// should appear
						if (pastLastIsScrollDown) {
							AnimationSet animationSet = new AnimationSet(true);
							TranslateAnimation translateAnimation = 
									new TranslateAnimation(
											Animation.RELATIVE_TO_SELF, 0f,
											Animation.RELATIVE_TO_SELF, 0f,
											Animation.RELATIVE_TO_SELF, 1f,
											Animation.RELATIVE_TO_SELF, 0f);
							translateAnimation.setDuration(1000);
							animationSet.addAnimation(translateAnimation);
							animationSet.setFillAfter(true);
							pastRayMenu.startAnimation(animationSet);
							rayMenuShown = true;
						} else {
							
						}
						pastLastIsScrollDown = false;
					} else {
						if (pastMoveDirection == -1) {
							// down
							if (pastLastDirection == 1) {
								AnimationSet animationSet = new AnimationSet(true);
								TranslateAnimation translateAnimation = 
										new TranslateAnimation(
												Animation.RELATIVE_TO_SELF, 0f,
												Animation.RELATIVE_TO_SELF, 0f,
												Animation.RELATIVE_TO_SELF, 0f,
												Animation.RELATIVE_TO_SELF, 1f);
								translateAnimation.setDuration(1000);
								animationSet.addAnimation(translateAnimation);
								animationSet.setFillAfter(true);
								pastRayMenu.startAnimation(animationSet);
								rayMenuShown = false;
								pastLastDirection = -1;
							} else {
								
							}
							
						} else if (pastMoveDirection == 1) {
							// scroll up
							// should appear
							if (pastLastDirection == -1) {
								AnimationSet animationSet = new AnimationSet(true);
								TranslateAnimation translateAnimation = 
										new TranslateAnimation(
												Animation.RELATIVE_TO_SELF, 0f,
												Animation.RELATIVE_TO_SELF, 0f,
												Animation.RELATIVE_TO_SELF, 1f,
												Animation.RELATIVE_TO_SELF, 0f);
								translateAnimation.setDuration(1000);
								animationSet.addAnimation(translateAnimation);
								animationSet.setFillAfter(true);
								pastRayMenu.startAnimation(animationSet);
								rayMenuShown = true;
								pastLastDirection = 1;
							} else {
								
							}
							
						}
					}
					pastLastVisibleItemPosition = firstVisibleItem;
				}
			}
		});
		mViewList.add(v2);
	}
	
	private void initView1() {
		View v1 = layoutInflater.inflate(R.layout.layout1, null);
		listView = (ListView)v1.findViewById(R.id.listview);
		listView.setAdapter(mAdapter);

		// sort by the create time default
		TimeFleetingData.sortFutureRecordByCreateTimeReversely();
		
		if (TimeFleetingData.futureRecords.size() > 0) {
			TextView tipsTextView = (TextView)v1.findViewById(R.id.layout_1_tips);
			tipsTextView.setVisibility(View.INVISIBLE);
		} else {
			TextView tipsTextView = (TextView)v1.findViewById(R.id.layout_1_tips);
			tipsTextView.setVisibility(View.VISIBLE);
		}
		
		// on list item click listener
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	if (mAdapter.isOpen(position)) {
                    return;
            	} else {
            		mAdapter.closeAllItems();
            		Intent intent = new Intent(mContext, EditActivity.class);
	            	intent.putExtra("isOld", true);
	            	intent.putExtra("Title", TimeFleetingData.futureRecords.get(position).getTitle());
	            	intent.putExtra("Content", TimeFleetingData.futureRecords.get(position).getText());
	            	intent.putExtra("CreateTime", TimeFleetingData.futureRecords.get(position).getCreateTime());
	            	intent.putExtra("RemindTime", TimeFleetingData.futureRecords.get(position).getRemindTime());
	            	intent.putExtra("Star", TimeFleetingData.futureRecords.get(position).getStar());
	            	intent.putExtra("ID", TimeFleetingData.futureRecords.get(position).getId());
	            	intent.putExtra("Status", TimeFleetingData.futureRecords.get(position).getStatus());
	            	intent.putExtra("Top", TimeFleetingData.futureRecords.get(position).getBeTop());
					startActivityForResult(intent, 1);
            	}
            }
        });			

		
		
		listView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				    case MotionEvent.ACTION_DOWN: {
				    	downX = event.getX();
				    	downY = event.getY();
				    	//   return true;
				    }
				    case MotionEvent.ACTION_UP: {
				    	pressDownY = -1;
				    	
				    	upX = event.getX();
				    	upY = event.getY();

				    	float deltaX = downX - upX;
				    	float deltaY = downY - upY;
				    	
				    	// swipe horizontal?
				    	if(Math.abs(deltaX) > MIN_DISTANCE) {
				    		// left or right
				    		if(deltaX > 0) {
				    			boolean isOpen = false;
				    			for (int i = listView.getFirstVisiblePosition(); i <= listView.getLastVisiblePosition(); i++) {
				    				if (mAdapter.isOpen(i)) {
				    					isOpen = true;
				    					break;
				    				}
				    			}
				    			if (isOpen) {
				    				mAdapter.closeAllItems();
				    				return true;
				    			}
				    			return false; 
				    		}
				    		if(deltaX < 0) { 
				    			
				    			return false; 
				    		}
				    	} 
				    }
				    case MotionEvent.ACTION_MOVE: {
				    	if (pressDownY == -1) {
							// don't calculate
							pressDownY = event.getY();
							return false;
						}
						if (event.getY() > pressDownY) {
							moveDirection = 1;
						} else if (event.getY() < pressDownY) {
							moveDirection = -1;
						}
						pressDownY = event.getY();
						return false;
				    }
				}
				return false;
			}
		});
		
		listView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					scrollFlag = false;
					if (listView.getLastVisiblePosition() == (listView.getCount() - 1)) {
						// to bottom 
						
					}
					if (listView.getFirstVisiblePosition() == 0) {
						// to top

					}
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					mAdapter.closeAllItems();
					scrollFlag = true;
					break;
				case OnScrollListener.SCROLL_STATE_FLING:
					scrollFlag = false;
					break;
				default:
					break;
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				rayMenu.closeMenu();
				if (scrollFlag && 
						ScreenUtil.getScreenViewBottomHeight(listView) 
						>= ScreenUtil.getScreenHeight(MainActivity.this) 
						- statusBarHeight 
						- layoutTitleLinearLayout.getHeight()) {
					if (firstVisibleItem > lastVisibleItemPosition) {
						// scroll down
						// should disappear
						if (!lastIsScrollDown) {
							AnimationSet animationSet = new AnimationSet(true);
							TranslateAnimation translateAnimation = 
									new TranslateAnimation(
											Animation.RELATIVE_TO_SELF, 0f,
											Animation.RELATIVE_TO_SELF, 0f,
											Animation.RELATIVE_TO_SELF, 0f,
											Animation.RELATIVE_TO_SELF, 1f);
							translateAnimation.setDuration(1000);
							animationSet.addAnimation(translateAnimation);
							animationSet.setFillAfter(true);
							rayMenu.startAnimation(animationSet);
							rayMenuShown = false;
						} else {
							
						}
						lastIsScrollDown = true;
					} else if (firstVisibleItem < lastVisibleItemPosition) {
						// scroll up
						// should appear
						if (lastIsScrollDown) {
							AnimationSet animationSet = new AnimationSet(true);
							TranslateAnimation translateAnimation = 
									new TranslateAnimation(
											Animation.RELATIVE_TO_SELF, 0f,
											Animation.RELATIVE_TO_SELF, 0f,
											Animation.RELATIVE_TO_SELF, 1f,
											Animation.RELATIVE_TO_SELF, 0f);
							translateAnimation.setDuration(1000);
							animationSet.addAnimation(translateAnimation);
							animationSet.setFillAfter(true);
							rayMenu.startAnimation(animationSet);
							rayMenuShown = true;
						} else {
							
						}
						lastIsScrollDown = false;
					} else {
						if (moveDirection == -1) {
							// down
							if (lastDirection == 1) {
								AnimationSet animationSet = new AnimationSet(true);
								TranslateAnimation translateAnimation = 
										new TranslateAnimation(
												Animation.RELATIVE_TO_SELF, 0f,
												Animation.RELATIVE_TO_SELF, 0f,
												Animation.RELATIVE_TO_SELF, 0f,
												Animation.RELATIVE_TO_SELF, 1f);
								translateAnimation.setDuration(1000);
								animationSet.addAnimation(translateAnimation);
								animationSet.setFillAfter(true);
								rayMenu.startAnimation(animationSet);
								rayMenuShown = false;
								lastDirection = -1;
							} else {
								
							}
							
						} else if (moveDirection == 1) {
							// scroll up
							// should appear
							if (lastDirection == -1) {
								AnimationSet animationSet = new AnimationSet(true);
								TranslateAnimation translateAnimation = 
										new TranslateAnimation(
												Animation.RELATIVE_TO_SELF, 0f,
												Animation.RELATIVE_TO_SELF, 0f,
												Animation.RELATIVE_TO_SELF, 1f,
												Animation.RELATIVE_TO_SELF, 0f);
								translateAnimation.setDuration(1000);
								animationSet.addAnimation(translateAnimation);
								animationSet.setFillAfter(true);
								rayMenu.startAnimation(animationSet);
								rayMenuShown = true;
								lastDirection = 1;
							} else {
								
							}
							
						}
					}
					lastVisibleItemPosition = firstVisibleItem;
				}
			}
		});
		rayMenuAppeared = false;
		
		mViewList.add(v1);
	}
	
	public int getStatusBarHeight() {
	  int result = 0;
	  int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	  if (resourceId > 0) {
	      result = getResources().getDimensionPixelSize(resourceId);
	  }
	  return result;
	}
	
	@Override
	public void onBackPressed() {
		final int drawerState = mMenuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mMenuDrawer.closeMenu();
            return;
        } else {
        	if (isTaskRoot()) {
        		Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                return;
        	}
        }
		
		super.onBackPressed();
		
	}
	
	@Override
	public void onDestroy() {
		editor.putBoolean("SHOWN_SPLASH", false);
    	editor.commit();
    	Log.d("TimeFleeting", "SHOWN_SPLAST_FALSE");
		super.onDestroy();
	}
	
	private void setSort() {
		if (mJazzy.getCurrentItem() == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			View view = layoutInflater.inflate(R.layout.set_sort_past, null);
			builder.setView(view);
			builder.setCancelable(true);
			final AlertDialog dialog = builder.show();
			dialog.setCanceledOnTouchOutside(true);
			YoYo.with(Techniques.Tada).duration(1000).delay(500).playOn(view.findViewById(R.id.sort_past_by_remind_time_logo));
			YoYo.with(Techniques.Tada).duration(1000).delay(500).playOn(view.findViewById(R.id.sort_past_by_remain_time_logo));
			LinearLayout sortByTitleLinearLayout = (LinearLayout)view.findViewById(R.id.sort_past_by_remind_time_ly);
			sortByTitleLinearLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (TimeFleetingData.pastIsSortByRemindTimeReversely) {
						TimeFleetingData.sortPastRecordsByRemindTime();
					} else {
						TimeFleetingData.sortPastRecordsByRemindTimeReversely();
					}
					pastAdapter.notifyDataSetChanged();
					dialog.dismiss();
				}
			});
			LinearLayout sortByCreateTimeLinearLayout = (LinearLayout)view.findViewById(R.id.sort_past_by_remain_time_ly);
			sortByCreateTimeLinearLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (TimeFleetingData.pastIsSortByRemainTimeReversely) {
						TimeFleetingData.sortPastRecordsByRemainTime();
					} else {
						TimeFleetingData.sortPastRecordsByRemainTimeReversely();
					}
					pastAdapter.notifyDataSetChanged();
					dialog.dismiss();
				}
			});
		} else if (mJazzy.getCurrentItem() == 1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			View view = layoutInflater.inflate(R.layout.set_sort, null);
			builder.setView(view);
			builder.setCancelable(true);
			final AlertDialog dialog = builder.show();
			dialog.setCanceledOnTouchOutside(true);
			YoYo.with(Techniques.Tada).duration(1000).delay(500).playOn(view.findViewById(R.id.sort_by_title_logo));
			YoYo.with(Techniques.Tada).duration(1000).delay(500).playOn(view.findViewById(R.id.sort_by_create_time_logo));
			YoYo.with(Techniques.Tada).duration(1000).delay(500).playOn(view.findViewById(R.id.sort_by_remind_time_logo));
			YoYo.with(Techniques.Tada).duration(1000).delay(500).playOn(view.findViewById(R.id.sort_by_star_logo));
			LinearLayout sortByTitleLinearLayout = (LinearLayout)view.findViewById(R.id.sort_by_title_ly);
			sortByTitleLinearLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!TimeFleetingData.isSortedByTitleReversely) {
						TimeFleetingData.sortFutureRecordByTitleReversely();
					} else {
						TimeFleetingData.sortFutureRecordByTitle();
					}
					mAdapter.notifyDataSetChanged();
					dialog.dismiss();
				}
			});
			LinearLayout sortByCreateTimeLinearLayout = (LinearLayout)view.findViewById(R.id.sort_by_create_time_ly);
			sortByCreateTimeLinearLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (TimeFleetingData.isSortedByCreateTimeReversely) {
						TimeFleetingData.sortFutureRecordByCreateTime();
					} else {
						TimeFleetingData.sortFutureRecordByCreateTimeReversely();
					}
					mAdapter.notifyDataSetChanged();
					dialog.dismiss();
				}
			});
			LinearLayout sortByRemindTimeLinearLayout = (LinearLayout)view.findViewById(R.id.sort_by_remind_time_ly);
			sortByRemindTimeLinearLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!TimeFleetingData.isSortedByRemindTimeReversely) {
						TimeFleetingData.sortFutureRecordByRemindTimeReversely();
					} else {
						TimeFleetingData.sortFutureRecordByRemindTime();
					}
					mAdapter.notifyDataSetChanged();
					dialog.dismiss();
				}
			});
			LinearLayout sortByStarLinearLayout = (LinearLayout)view.findViewById(R.id.sort_by_star_ly);
			sortByStarLinearLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!TimeFleetingData.isSortedByStarReversely) {
						TimeFleetingData.sortFutureRecordByStarReversely();
					} else {
						TimeFleetingData.sortFutureRecordByStar();
					}
					mAdapter.notifyDataSetChanged();
					dialog.dismiss();
				}
			});
		}
	}
	
	private void initValues() {
		
		rayMenu = (RayMenu)findViewById(R.id.ray_menu);
		
		rayMenuAppeared = true;
		
		for (int i = 0; i < ITEM_DRAWABLES_FUTURE.length; i++) {
			ImageView imageView = new ImageView(mContext);
			imageView.setImageResource(ITEM_DRAWABLES_FUTURE[i]);
			final int menuPosition = i;
			rayMenu.addItem(imageView, new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (menuPosition == 0) {
						// new
						if (mJazzy.getCurrentItem() == 0) {
							Intent intent = new Intent(mContext, EditPastActivity.class);
							intent.putExtra("isOld", false);
							startActivityForResult(intent, 2);
						} else if (mJazzy.getCurrentItem() == 1) {
							Intent intent = new Intent(mContext, EditActivity.class);
							intent.putExtra("isOld", false);
							startActivityForResult(intent, 1);
						}
					} else if (menuPosition == 1) {
						// search
					} else if (menuPosition == 2) {
						// overdue
					} 
				}
			});
		}
		
		intentService = new Intent(mContext, LongRunningService.class);
        intentService.setAction("TimeFleeting Reminder");
        
        intentPastService = new Intent(mContext, LongRunningPastService.class);
        intentPastService.setAction("TimeFleeting Memory Reminder");
		
		GlobalSettings.REMIND_ENABLE = preferences.getBoolean("REMIND_ENABLE", true);
		GlobalSettings.REMIND_PAST_ENABLE = preferences.getBoolean("REMIND_PAST_ENABLE", true);
		remindEnableButton = (ToggleButton)findViewById(R.id.menu_layout_remind_enable);
		remindPastEnableButton = (ToggleButton)findViewById(R.id.menu_layout_remind_past_enable);
		
		if (GlobalSettings.REMIND_ENABLE) {
			remindEnableButton.setToggleOff();
	        initReminds();
			LongRunningService.remindList = GlobalSettings.REMIND_LIST;
			startService(intentService);
		} else {
			remindEnableButton.setToggleOn();
			stopService(intentService);
		}
		
		if (GlobalSettings.REMIND_PAST_ENABLE) {
			remindPastEnableButton.setToggleOff();
			initPastReminds();
			LongRunningPastService.remindList = GlobalSettings.REMIND_PAST_LIST;
			startService(intentPastService);
		} else {
			remindPastEnableButton.setToggleOn();
			stopService(intentPastService);
		}
		
        remindEnableButton.setOnToggleChanged(new OnToggleChanged() {
			
			@Override
			public void onToggle(boolean on) {
				if (!on) {
					// start every service
			        initReminds();
					LongRunningService.remindList = GlobalSettings.REMIND_LIST;
					startService(intentService);
					GlobalSettings.REMIND_ENABLE = true;
					Log.d("TimeFleeting remind todo", "on");
				} else {
					stopService(intentService);
					GlobalSettings.REMIND_ENABLE = false;
					Log.d("TimeFleeting remind todo", "off");
				}
				editor.putBoolean("REMIND_ENABLE", GlobalSettings.REMIND_ENABLE);
				editor.commit();
			}
		});
        
        remindPastEnableButton.setOnToggleChanged(new OnToggleChanged() {
			
			@Override
			public void onToggle(boolean on) {
				if (!on) {
					// start every service
			        initPastReminds();
					LongRunningPastService.remindList = GlobalSettings.REMIND_PAST_LIST;
					startService(intentPastService);
					GlobalSettings.REMIND_PAST_ENABLE = true;
					Log.d("TimeFleeting remind memory", "on");
				} else {
					stopService(intentPastService);
					GlobalSettings.REMIND_PAST_ENABLE = false;
					Log.d("TimeFleeting remind memory", "off");
				}
				editor.putBoolean("REMIND_PAST_ENABLE", GlobalSettings.REMIND_PAST_ENABLE);
				editor.commit();
			}
		});
        
        GlobalSettings.VIBRATE_ENABLE = preferences.getBoolean("VIBRATE_ENABLE", true);
        vibrateEnableButton = (ToggleButton)findViewById(R.id.menu_layout_vibrate_enable);
        if (GlobalSettings.VIBRATE_ENABLE) {
        	vibrateEnableButton.setToggleOff();
        } else {
        	vibrateEnableButton.setToggleOn();
        }
        vibrateEnableButton.setOnToggleChanged(new OnToggleChanged() {
			
			@Override
			public void onToggle(boolean on) {
				if (!on) {
					GlobalSettings.VIBRATE_ENABLE = true;
				} else {
					GlobalSettings.VIBRATE_ENABLE = false;
				}
				editor.putBoolean("VIBRATE_ENABLE", GlobalSettings.VIBRATE_ENABLE);
				editor.commit();
			}
		});
        
        GlobalSettings.SOUND_ENABLE = preferences.getBoolean("SOUND_ENABLE", true);
        soundEnableButton = (ToggleButton)findViewById(R.id.menu_layout_sound_enable);
        if (GlobalSettings.SOUND_ENABLE) {
        	soundEnableButton.setToggleOff();
        } else {
        	soundEnableButton.setToggleOn();
        }
        soundEnableButton.setOnToggleChanged(new OnToggleChanged() {
			
			@Override
			public void onToggle(boolean on) {
				if (!on) {
					GlobalSettings.SOUND_ENABLE = true;
				} else {
					GlobalSettings.SOUND_ENABLE = false;
				}
				editor.putBoolean("SOUND_ENABLE", GlobalSettings.SOUND_ENABLE);
				editor.commit();
			}
		});
        GlobalSettings.REMIND_HOUR = preferences.getInt("REMIND_HOUR", 9);
        GlobalSettings.REMIND_MINUTE = preferences.getInt("REMIND_MINUTE", 0);
        menuRemindTimeTextView = (TextView)findViewById(R.id.menu_layout_remind_time_text);
        menuRemindTimeTextView.setText(
        		(GlobalSettings.REMIND_HOUR < 10 ? 
        				"0" + String.valueOf(GlobalSettings.REMIND_HOUR) : 
        					String.valueOf(GlobalSettings.REMIND_HOUR)) + ":" + 
        	    (GlobalSettings.REMIND_MINUTE < 10 ? 
        				"0" + String.valueOf(GlobalSettings.REMIND_MINUTE) : 
        					String.valueOf(GlobalSettings.REMIND_MINUTE)));
        menuRemindTimeLinearLayout = (LinearLayout)findViewById(R.id.menu_layout_remind_time_ly);
        menuRemindTimeLinearLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setRemindTime();
			}
		});
        
        spinner = (Spinner)findViewById(R.id.past_advanced_spinner);
        spinnerArrayAdapter = new SpinnerArrayAdapter(mContext, spinnerStrings, 13);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        
        spinner.setSelection(preferences.getInt("AHEAD_DAYS_POSITION", 2));
        
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					GlobalSettings.AHEAD_DAYS = 1;
				} else if (position == 1) {
					GlobalSettings.AHEAD_DAYS = 3;
				} else if (position == 2) {
					GlobalSettings.AHEAD_DAYS = 7;
				} else if (position == 3) {
					GlobalSettings.AHEAD_DAYS = 14;
				} else if (position == 4) {
					GlobalSettings.AHEAD_DAYS = 30;
				}
				editor.putInt("AHEAD_DAYS_POSITION", position);
				editor.commit();
				initPastReminds();
				LongRunningPastService.remindList = GlobalSettings.REMIND_PAST_LIST;
				stopService(intentPastService);
				startService(intentPastService);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
        
	}
	
	private void setRemindTime() {
		final Calendar calendar = Calendar.getInstance();
		
	    final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);
		
		timePickerDialog.setVibrate(false);
        timePickerDialog.setCloseOnSingleTapMinute(false);
        timePickerDialog.setCancelable(true);
        timePickerDialog.show(getSupportFragmentManager(), GlobalSettings.TIMEPICKER_TAG);
	}
	
	public static void initReminds() {
		GlobalSettings.REMIND_LIST = new ArrayList<Remind>();
		for (int i = 0; i < TimeFleetingData.futureRecords.size(); i++) {
			Remind remind = new Remind();
			remind.id = TimeFleetingData.futureRecords.get(i).getId();
			remind.title = TimeFleetingData.futureRecords.get(i).getTitle();
			remind.createTime = TimeFleetingData.futureRecords.get(i).getCreateTime();
			remind.remindTime = TimeFleetingData.futureRecords.get(i).getRemindTime();
			remind.star = TimeFleetingData.futureRecords.get(i).getStar();
			remind.type = TimeFleetingData.futureRecords.get(i).getType();
			remind.status = TimeFleetingData.futureRecords.get(i).getStatus();
			remind.Top = TimeFleetingData.futureRecords.get(i).getBeTop();
			remind.content = TimeFleetingData.futureRecords.get(i).getText();
			SimpleDateFormat formatter = new SimpleDateFormat (GlobalSettings.FULL_DATE_FORMAT); 
			Date remindDate = new Date(System.currentTimeMillis());
			Date currentDate = new Date(System.currentTimeMillis());
			try {
				remindDate = formatter.parse(TimeFleetingData.futureRecords.get(i).getRemindTime());
			} catch (ParseException p) {
				p.printStackTrace();
			}
			
			// a-----------b-----c
			long c = remindDate.getTime();
			long b = c - GlobalSettings.ALARM_TIME;
			if (currentDate.getTime() >= remindDate.getTime()) {
				// overdue
				continue;
			}
			remind.triggerAtTime = b;
			GlobalSettings.REMIND_LIST.add(remind);
		}
		Log.d("TimeFleeting", "Finishing loading " + GlobalSettings.REMIND_LIST.size() + " reminds.");
	}
	
	public static void initPastReminds() {
		GlobalSettings.REMIND_PAST_LIST = new ArrayList<Remind>();
		for (int i = 0; i < TimeFleetingData.pastRecords.size(); i++) {
			if (TimeFleetingData.pastRecords.get(i).getType().equals("PAST_N")) {
				continue;
			}
			Remind remind = new Remind();
			remind.id = TimeFleetingData.pastRecords.get(i).getId();
			remind.title = TimeFleetingData.pastRecords.get(i).getTitle();
			remind.createTime = TimeFleetingData.pastRecords.get(i).getCreateTime();
			remind.remindTime = TimeFleetingData.pastRecords.get(i).getRemindTime();
			remind.star = TimeFleetingData.pastRecords.get(i).getStar();
			remind.type = TimeFleetingData.pastRecords.get(i).getType();
			remind.status = TimeFleetingData.pastRecords.get(i).getStatus();
			remind.Top = TimeFleetingData.pastRecords.get(i).getBeTop();
			remind.content = TimeFleetingData.pastRecords.get(i).getText();
			Date remindDate = new Date(System.currentTimeMillis());
			Date currentDate = new Date(System.currentTimeMillis());
			currentDate.setSeconds(0);
			int days = TimeFleetingData.calculateRemainDays(TimeFleetingData.pastRecords.get(i));
			if (days <= GlobalSettings.AHEAD_DAYS) {
				days = 0;
			} else {
				days -= GlobalSettings.AHEAD_DAYS;
			}
			remindDate.setTime(remindDate.getTime() + days * GlobalSettings.A_DAY);
			remindDate.setHours(GlobalSettings.REMIND_HOUR);
			remindDate.setMinutes(GlobalSettings.REMIND_MINUTE);
			remindDate.setSeconds(0);
			
			if (currentDate.getTime() > remindDate.getTime()) {
				// overdue
				continue;
			}
			remind.triggerAtTime = remindDate.getTime();
			GlobalSettings.REMIND_PAST_LIST.add(remind);
		}
		Log.d("TimeFleeting", "Finishing loading " + GlobalSettings.REMIND_PAST_LIST.size() + " memory.");
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		GlobalSettings.REMIND_HOUR = hourOfDay;
		GlobalSettings.REMIND_MINUTE = minute;
		menuRemindTimeTextView.setText(
        		(GlobalSettings.REMIND_HOUR < 10 ? 
        				"0" + String.valueOf(GlobalSettings.REMIND_HOUR) : 
        					String.valueOf(GlobalSettings.REMIND_HOUR)) + ":" + 
        	    (GlobalSettings.REMIND_MINUTE < 10 ? 
        				"0" + String.valueOf(GlobalSettings.REMIND_MINUTE) : 
        					String.valueOf(GlobalSettings.REMIND_MINUTE)));
		editor.putInt("REMIND_HOUR", GlobalSettings.REMIND_HOUR);
		editor.putInt("REMIND_MINUTE", GlobalSettings.REMIND_MINUTE);
		editor.commit();
		initPastReminds();
		LongRunningPastService.remindList = GlobalSettings.REMIND_PAST_LIST;
		stopService(intentPastService);
		startService(intentPastService);
	}
	
}
