package org.appcelerator.titanium.proxy;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.frankify.f;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.R;
import org.appcelerator.titanium.TiC;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@Kroll.proxy(propertyAccessors = {
		TiC.DRAWER_ITEMS,
		TiC.PROPERTY_TITLE,
		TiC.PROPERTY_ICON
})

/*
 frankie after todo list:
  + remove f.logs
  + rename resource files and remove unused files/codes
  + custom props pass in
  + why is addDrawerListener invalid?
 */
public class DrawerProxy extends KrollProxy
{
	private WeakReference<AppCompatActivity> activityWeakReference;
	private ActionBarDrawerToggle actionbarDrawerToggle;
	private DrawerLayout drawerlayout;
	private ListView drawerListView;
	private ArrayList<DrawerItemProxy> drawerItemProxies;
	
	public ActionBarDrawerToggle getActionbarDrawerToggle() {
		return actionbarDrawerToggle;
	}
	
	
	public DrawerProxy(AppCompatActivity activity) {
		try {
			init(activity);
		} catch (Exception e) {
			f.log("threw exception " + e);
		}
	}
	
	private void init(AppCompatActivity activity) {
		f.log();
		if (activity == null) {
			f.log("activity is null");
			return;
		}
		try {
			activityWeakReference = new WeakReference<AppCompatActivity>(activity);
			drawerlayout = (DrawerLayout) activity.findViewById(R.layout.drawer);
			drawerListView = (ListView) activity.findViewById(R.id.left_drawer);
			drawerItemProxies = new ArrayList<DrawerItemProxy>();
			// todo pass in user-custom props and use them rather than this
			drawerItemProxies.add(new DrawerItemProxy("hardcoded_title#2", "hardcoded_icon#1"));
			drawerItemProxies.add(new DrawerItemProxy("hardcoded_title#2", "hardcoded_icon#2"));
			TiArrayAdapter arrayAdapter = new TiArrayAdapter(activity, R.layout.drawer_item, drawerItemProxies);
			drawerListView.setAdapter(arrayAdapter);
			drawerListView.setOnItemClickListener(new DrawerItemClickListener());
			if (activity.getSupportActionBar() != null) {
				f.log("actionBar is not null");
				activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				activity.getSupportActionBar().setHomeButtonEnabled(true);
			} else {
				f.log("actionBar is null");
			}
			actionbarDrawerToggle = new ActionBarDrawerToggle(activity, drawerlayout, R.string.drawer_open_title, R.string.drawer_close_title) {
				public void onDrawerClosed(View view) {
					super.onDrawerClosed(view);
					activity.getSupportActionBar().setTitle(R.string.drawer_close_title);
					activity.invalidateOptionsMenu();
				}
				public void onDrawerOpened(View drawerView) {
					super.onDrawerOpened(drawerView);
					activity.getSupportActionBar().setTitle(R.string.drawer_open_title);
					activity.invalidateOptionsMenu();
				}
			};
			drawerlayout.setDrawerListener(actionbarDrawerToggle);
			// if (Build.VERSION.SDK_INT >= 21) {
			// 	// addDrawerListener is not recognised for some reason
			// 	// drawerlayout.addDrawerListener(actionbarDrawerToggle);
			// } else {
			// 	drawerlayout.setDrawerListener(actionbarDrawerToggle);
			// }
			f.log("no errors have occurred in init");
		} catch (Exception e) {
			f.log("threw exception " + e);
		}
	}
	 // suppressed inspection "static inner fragment"
	private static class TiDrawItemFragment extends Fragment {
		ImageView iconView;
		TextView itemTextView;
		
		private static final String IMAGE_RESOURCE_ID = "iconResourceId";
		private static final String ITEM_NAME = "item";
		
		public TiDrawItemFragment() {
			
		}
		
		@SuppressWarnings("deprecation") @Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.drawer_item_fragment, container, false);
			itemTextView = (TextView) view.findViewById(R.id.frag1_text);
			iconView = (ImageView) view.findViewById(R.id.drawer_item_fragment_icon);
			itemTextView.setText(getArguments().getString(ITEM_NAME));
			itemTextView.setTextColor(Color.parseColor("#00ff00"));
			if (Build.VERSION.SDK_INT >= 21) {
				iconView.setImageDrawable(view.getResources().getDrawable(getArguments().getInt(IMAGE_RESOURCE_ID), null));
			} else {
				iconView.setImageDrawable(view.getResources().getDrawable(getArguments().getInt(IMAGE_RESOURCE_ID)));
			}
			return view;
		}
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{
		@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			try {
				f.log("item clicked " + position);
				selectItem(position);
			} catch (Exception e) {
				f.log("threw exception " + e);
			}
		}
	}
	
	private void selectItem(int position) throws Exception {
		Fragment fragment = null;
		Bundle args = new Bundle();
		f.log("case/position " + position);
		switch (position)  {
			case 0:
				fragment = new TiDrawItemFragment();
				args.putString(TiDrawItemFragment.ITEM_NAME, drawerItemProxies.get(position).drawerItem.getItem());
				args.putInt(TiDrawItemFragment.IMAGE_RESOURCE_ID, drawerItemProxies.get(position).drawerItem.getImgResId());
				break;
			default:
				break;
		}
		if (fragment != null) {
			fragment.setArguments(args);
		} else {
			f.log("fragment is null");
		}
		FragmentManager fragmentManager = activityWeakReference.get().getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
		drawerListView.setItemChecked(position, true);
		activityWeakReference.get().setTitle(drawerItemProxies.get(position).drawerItem.getItem());
		drawerlayout.closeDrawer(drawerListView);
	}
	
	@Override
	public void handleCreationDict(KrollDict dict)
	{
		super.handleCreationDict(dict);
		// "drawerItems" property holds drawerItem objects
		if (dict.containsKey(TiC.DRAWER_ITEMS)) {
			try {
				f.log("DrawerProxy has drawerItems property");
				if (dict.get(TiC.DRAWER_ITEMS) instanceof Object[]) {
					f.log("instanceof Object[]");
				} else {
					f.log("not instanceof Object[]");
				}
				if (dict.containsKeyAndNotNull(TiC.DRAWER_ITEMS)) f.log("has drawerItems and NOT NULL"); else f.log("has NULL drawerItems");
				Object[] items = (Object[]) dict.get(TiC.DRAWER_ITEMS);
				if (items.length > 0) f.log("items length > 0"); else f.log("items length <= 0");
				for (int i = 0; i < items.length; ++i) {
					drawerItemProxies.add((DrawerItemProxy) items[i]);
				}
			} catch (Exception e) {
				f.log("threw exception " + e);
			}
		} else {
			f.log("does not contain drawerItems property");
		}
	}
	
	@Override public boolean handleMessage(Message msg)
	{
		f.log();
		// property change logic
		return super.handleMessage(msg);
	}
	
	@Kroll.method @Kroll.setProperty
	public void setDrawerItems(Object[] drawerItems) {
		try {
			f.log();
		} catch (Exception e) {
			f.log("threw exception " + e);
		}
	}
	
	public class DrawerItemProxy extends KrollProxy
	{
		private DrawerItem drawerItem;
		public DrawerItemProxy() {
			f.log();
			drawerItem = new DrawerItem();
		}
		
		public DrawerItemProxy(String title, String icon) {
			setProperty(TiC.PROPERTY_TITLE, title);
			setProperty(TiC.PROPERTY_ICON, icon);
		} 
		
		@Override public void handleCreationDict(KrollDict dict)
		{
			super.handleCreationDict(dict);
			if (dict.containsKey(TiC.PROPERTY_TITLE)) {
				f.log("has title " + dict.get(TiC.PROPERTY_TITLE));
				setProperty(TiC.PROPERTY_TITLE, dict.get(TiC.PROPERTY_TITLE));
				drawerItem.setItem((String) dict.get(TiC.PROPERTY_TITLE));
			} else {
				f.log("has not contain title ");
			}
			if (dict.containsKey(TiC.PROPERTY_ICON)) {
				f.log("has icon " + dict.get(TiC.PROPERTY_ICON));
				setProperty(TiC.PROPERTY_ICON, dict.get(TiC.PROPERTY_ICON));
				//frankie TODO need resolve url into resource id or something
				// drawerItem.setImgResId(TiConvert.toInt(dict.get(TiC.PROPERTY_ICON)));
			} else {
				f.log("has not icon");
			}
		}
		
		@Kroll.method @Kroll.getProperty
		public String getTitle() {
			return (String) getProperty(TiC.PROPERTY_TITLE);
		}
		
		@Kroll.method @Kroll.setProperty
		public void setTitle(String title) {
			setPropertyAndFire(TiC.PROPERTY_TITLE, title);
		}
		
		@Kroll.method @Kroll.getProperty
		public String getIcon() {
			return (String) getProperty(TiC.PROPERTY_ICON);
		}
		
		@Kroll.method @Kroll.setProperty
		public void setIcon(String icon) {
			setPropertyAndFire(TiC.PROPERTY_ICON, icon);
		}
		
		public class DrawerItem
		{
			private String item;
			private int imgResId;
			
			public DrawerItem() {
				this(null, -1);
			}
			public DrawerItem(String itemName, int imgResID) {
				super();
				f.log();
				this.item = itemName;
				this.imgResId = imgResID;
			}
			
			public String getItem() {
				return item;
			}
			public void setItem(String item) {
				this.item = item;
			}
			public int getImgResId() {
				return imgResId;
			}
			public void setImgResId(int imgResId) {
				this.imgResId = imgResId;
			}
		}
	}
	
	/**
	 Created to use for customising how each row in drawerListView should look like
	 */
	private class TiArrayAdapter extends ArrayAdapter<DrawerItemProxy>
	{
		private Context context;
		private List<DrawerItemProxy> drawerItemList;
		private int layoutResId;
		
		// should allow user to set color
		private Color color;
		
		public TiArrayAdapter(Context context, int resourceId,
				List<DrawerItemProxy> items) {
			super(context, resourceId, items);
			this.context = context;
			layoutResId = resourceId;
			drawerItemList = items;
		}
		
		@SuppressWarnings("deprecation") @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			DrawerItemHolder drawerHolder;
			View view = convertView;
			
			if (view == null) {
				LayoutInflater inflater = ((Activity) context).getLayoutInflater();
				drawerHolder = new DrawerItemHolder();
				
				view = inflater.inflate(layoutResId, parent, false);
				drawerHolder.itemName = (TextView) view.findViewById(R.id.drawer_itemName);
				drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon);
				
				view.setTag(drawerHolder);
				
			} else {
				drawerHolder = (DrawerItemHolder) view.getTag();
				
			}
			
			DrawerItemProxy dItem = drawerItemList.get(position);
			
			drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(
					dItem.drawerItem.getImgResId()));
			drawerHolder.itemName.setText(dItem.drawerItem.getItem());
			
			return view;
		}
		
		private class DrawerItemHolder {
			TextView itemName;
			ImageView icon;
		}
		
		// @Override
		// public View getView(int position, View convertView, ViewGroup parent) {
		// 	TextView textView = (TextView) super.getView(position, convertView, parent);
		// 	int textColor = Color.BLUE;
		// 	textView.setTextColor(textColor);
		// 	return textView;
		// }
	}
}
