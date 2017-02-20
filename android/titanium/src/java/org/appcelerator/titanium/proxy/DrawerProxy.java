package org.appcelerator.titanium.proxy;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
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
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;
import org.appcelerator.titanium.util.TiResponseCache;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@Kroll.proxy(propertyAccessors = {
		TiC.DRAWER_ITEMS,
		TiC.PROPERTY_TITLE,
		TiC.PROPERTY_ICON
})

public class DrawerProxy extends KrollProxy
{
	private WeakReference<AppCompatActivity> activityWeakReference;
	private ActionBarDrawerToggle actionbarDrawerToggle;
	private DrawerLayout drawerlayout;
	private ListView drawerListView;
	private ArrayList<DrawerItemProxy> drawerItemProxies;
	
	private ActionBarDrawerToggle initActionBarToggle(AppCompatActivity activity) {
		if (activity == null) {
			f.log("activity is null");
			return null;
		}
		int drawer_open_title_id = getResourceId(activity, "string.drawer_open_title"), drawer_close_title_id = getResourceId(activity, "string.drawer_close_title");
		if (drawer_open_title_id != -1 && drawer_close_title_id != -1) {
			return null;
		}
		actionbarDrawerToggle = new ActionBarDrawerToggle(activity, drawerlayout, drawer_open_title_id, drawer_close_title_id) {
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				activity.getSupportActionBar().setTitle(drawer_close_title_id);
				activity.invalidateOptionsMenu();
			}
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				activity.getSupportActionBar().setTitle(drawer_close_title_id);
				activity.invalidateOptionsMenu();
			}
		};
		return actionbarDrawerToggle;
	}
	
	private static int getResourceId(final AppCompatActivity context, final String uri) {
		try {
			if (context == null) {
				f.log("context is null");
			}
			return TiRHelper.getResource(uri);
		} catch (ResourceNotFoundException resourceNotFound) {
			f.log("threw exception " + resourceNotFound);
		}
		return -1;
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
			activityWeakReference = new WeakReference<AppCompatActivity>(activity);
			drawerlayout = (DrawerLayout) activity.getLayoutInflater().inflate(getResourceId(activity, "layout.drawer"), null);
			drawerListView = (ListView) activity.getLayoutInflater().inflate(getResourceId(activity, "id.left_drawer"), null);
			drawerItemProxies = new ArrayList<DrawerItemProxy>();
			// todo pass in user-custom props and use them rather than this
			drawerItemProxies.add(new DrawerItemProxy("hardcoded_title#2", -1));
			drawerItemProxies.add(new DrawerItemProxy("hardcoded_title#2", -2));
			TiArrayAdapter arrayAdapter = new TiArrayAdapter(activity, activity.getLayoutInflater().inflate(getResourceId(activity, "layout.drawer_item"), null).getId(), drawerItemProxies);
			drawerListView.setAdapter(arrayAdapter);
			drawerListView.setOnItemClickListener(new DrawerItemClickListener());
			if (activity.getSupportActionBar() != null) {
				f.log("actionBar is not null");
				activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				activity.getSupportActionBar().setHomeButtonEnabled(true);
			} else {
				f.log("actionBar is null");
			}
			drawerlayout.setDrawerListener(initActionBarToggle(activity));
	}
	 // suppressed inspection "static inner fragment"
	private static class TiDrawItemFragment extends Fragment {
		ImageView iconView;
		TextView itemTextView;
		
		private static final String IMAGE_RESOURCE_ID = "iconResourceId";
		private static final String ITEM_NAME = "item";
		
		@SuppressWarnings("deprecation") @Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view;
				view = inflater.inflate(getResourceId(activityWeakReference.get(), "layout.drawer_item_fragment"), container, false);
				itemTextView = (TextView) view.findViewById(getResourceId(activityWeakReference.get(), "id.frag_text"));
				iconView = (ImageView) view.findViewById(getResourceId(activityWeakReference.get(), "id.frag_icon"));
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
				args.putString(TiDrawItemFragment.ITEM_NAME, drawerItemProxies.get(position).drawerItem.getItemName());
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
		activityWeakReference.get().setTitle(drawerItemProxies.get(position).drawerItem.getItemName());
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
		
		public DrawerItemProxy(String itemName, int iconResId) {
			drawerItem.setItemName(itemName);
			try {
				// drawerItem.setImgResId(activityWeakReference.get().findViewById(R.drawable.ninja).getId());
				drawerItem.setImgResId(activityWeakReference.get().findViewById(TiRHelper.getResource("drawable.ninja")).getId());
			} catch (TiRHelper.ResourceNotFoundException e) {
				f.log("threw resource not found exception" + e);
			}
		}
		
		@Override public void handleCreationDict(KrollDict dict)
		{
			super.handleCreationDict(dict);
			if (dict.containsKey(TiC.PROPERTY_TITLE)) {
				f.log("has title " + dict.get(TiC.PROPERTY_TITLE));
				setProperty(TiC.PROPERTY_TITLE, dict.get(TiC.PROPERTY_TITLE));
				drawerItem.setItemName((String) dict.get(TiC.PROPERTY_TITLE));
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
		
		private class DrawerItem
		{
			private String itemName;
			private int imgResId;
			
			DrawerItem() {
				this(null, -1);
			}
			DrawerItem(String itemName, int imgResID) {
				super();
				f.log();
				this.itemName = itemName;
				this.imgResId = imgResID;
			}
			
			String getItemName() {
				return itemName;
			}
			void setItemName(String itemName) {
				this.itemName = itemName;
			}
			int getImgResId() {
				return imgResId;
			}
			void setImgResId(int imgResId) {
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
		
		TiArrayAdapter(Context context, int resourceId,
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
					drawerHolder.item = (TextView) view.findViewById(getResourceId(activityWeakReference.get(),"id.drawer_itemView"));
					drawerHolder.icon = (ImageView) view.findViewById(getResourceId(activityWeakReference.get(),"id.drawer_imgView"));
				view.setTag(drawerHolder);
			} else {
				drawerHolder = (DrawerItemHolder) view.getTag();
				
			}
			
			DrawerItemProxy dItem = drawerItemList.get(position);
			drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(
					dItem.drawerItem.getImgResId()));
			drawerHolder.item.setText(dItem.drawerItem.getItemName());
			return view;
		}
		
		private class DrawerItemHolder {
			TextView item;
			ImageView icon;
		} // end of DrawerItemHolder
	} // end of TiArrayAdapter
} // end of DrawerProxy
