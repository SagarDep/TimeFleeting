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
import net.simonvt.menudrawer.Position;

import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.capricorn.RayMenu;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;

public class MainActivity extends FragmentActivity {

	public static final String DATEPICKER_TAG = "选择日期";
    public static final String TIMEPICKER_TAG = "选择时间";
	
	private JazzyViewPager mJazzy;
	private LayoutInflater layoutInflater;	
	
	private List<View> mViewList;
	
	private TextView resultTextView;
	
	private TimeFleetingData timeFleetingData;
	
	private Context mContext;
	
	private ListView listView;
	private ListViewAdapter mAdapter;
	
	private PagerAdapter mainAdapter;
	
	private RayMenu rayMenu;
	
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
	
	private View v;
	
	private int statusBarHeight;
	private LinearLayout layout1TitleLinearLayout;
	
	private boolean layout1RayMenuAppeared = false;
	private boolean layout1RayMenuShown = true;
	
	private float pressDownY = -1;
	private int moveDirection = -1;
	private int lastDirection = 1;
	
	private SharedPreferences.Editor editor;
	private SharedPreferences preferences;
	
	private static final int[] ITEM_DRAWABLES_FUTURE = {
		R.drawable.create,
		R.drawable.search,
		R.drawable.over_due,
		};
	
	public static Intent intentService;
	
	private LinearLayout layoutTitleLinearLayout;
	private TextView layoutTitleTextView;
	private ImageView layoutTitleImageView;
	
	private ImageView layoutTitleMenuImageView;
	private ImageView menuLayoutBackImageView;
	
	private String newRemindTimeString;
	private Record setTimeRecord;
	
	private ToggleButton remindEnableButton;
	
	private MenuDrawer mMenuDrawer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.OVERLAY, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
        mMenuDrawer.setContentView(R.layout.activity_main);
        mMenuDrawer.setMenuView(R.layout.menu_layout);
		
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
						intentService = new Intent(mContext, LongRunningService.class);
				        intentService.setAction("TimeFleeting Reminder");
				        initReminds();
						LongRunningService.remindList = GlobalSettings.REMIND_LIST;
						startService(intentService);
					}

				} else {
					Log.d("TimeFleeting", "isEditActivityFinished is false");
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
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
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

			rayMenu = (RayMenu)v1.findViewById(R.id.past_layout_ray_menu);
			
			layout1RayMenuAppeared = true;
			
			for (int i = 0; i < ITEM_DRAWABLES_FUTURE.length; i++) {
				ImageView imageView = new ImageView(mContext);
				imageView.setImageResource(ITEM_DRAWABLES_FUTURE[i]);
				final int menuPosition = i;
				rayMenu.addItem(imageView, new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if (menuPosition == 0) {
							// new
							Intent intent = new Intent(mContext, EditActivity.class);
							intent.putExtra("isOld", false);
							startActivityForResult(intent, 1);
						} else if (menuPosition == 1) {
							// search
						} else if (menuPosition == 2) {
							// overdue
						} 
					}
				});
			}
			
			listView.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_MOVE) {
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
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						pressDownY = -1;
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
								layout1RayMenuShown = false;
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
								layout1RayMenuShown = true;
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
									layout1RayMenuShown = false;
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
									layout1RayMenuShown = true;
									lastDirection = 1;
								} else {
									
								}
								
							}
						}
						lastVisibleItemPosition = firstVisibleItem;
					}
				}
			});

			View v2 = layoutInflater.inflate(R.layout.layout2, null);

			layout1RayMenuAppeared = false;
			View v3 = layoutInflater.inflate(R.layout.layout3, null);
			
			mViewList = new ArrayList<View>();
			
			mViewList.add(v2);
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
        }
		
		super.onBackPressed();
	}
	
	private void setSort() {
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
	
	private void initValues() {
		GlobalSettings.REMIND_ENABLE = preferences.getBoolean("REMIND_ENABLE", true);
		remindEnableButton = (ToggleButton)findViewById(R.id.menu_layout_remind_enable);
		if (GlobalSettings.REMIND_ENABLE) {
			remindEnableButton.setToggleOn();
			intentService = new Intent(mContext, LongRunningService.class);
	        intentService.setAction("TimeFleeting Reminder");
	        initReminds();
			LongRunningService.remindList = GlobalSettings.REMIND_LIST;
			startService(intentService);
		} else {
			remindEnableButton.setToggleOff();
			stopService(intentService);
		}
        remindEnableButton.setOnToggleChanged(new OnToggleChanged() {
			
			@Override
			public void onToggle(boolean on) {
				if (on) {
					// start every service
					intentService = new Intent(mContext, LongRunningService.class);
			        intentService.setAction("TimeFleeting Reminder");
			        initReminds();
					LongRunningService.remindList = GlobalSettings.REMIND_LIST;
					startService(intentService);
					Log.d("TimeFleeting", "on");
				} else {
					stopService(intentService);
					Log.d("TimeFleeting", "off");
				}
			}
		});
	}
	
	public static void initReminds() {
		GlobalSettings.REMIND_LIST = new ArrayList<Remind>();
		for (int i = 0; i < TimeFleetingData.futureRecords.size(); i++) {
			Remind remind = new Remind();
			remind.id = TimeFleetingData.futureRecords.get(i).getId();
			remind.titleString = TimeFleetingData.futureRecords.get(i).getTitle();
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
			if (currentDate.getTime() > remindDate.getTime()) {
				// overdue
				continue;
			}
			remind.triggerAtTime = b;
			GlobalSettings.REMIND_LIST.add(remind);
		}
		Log.d("TimeFleeting", "Finishing loading " + GlobalSettings.REMIND_LIST.size() + " reminds.");
	}
	
}
