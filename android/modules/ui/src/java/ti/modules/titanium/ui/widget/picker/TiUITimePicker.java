/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.ui.widget.picker;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiUIView;

import java.util.Calendar;
import java.util.Date;

public class TiUITimePicker extends TiUIView implements OnTimeChangedListener {
	private static final String TAG = "TiUITimePicker";
	
	protected Date minDate, maxDate;
	protected int minuteInterval;
	
	private int lastSelectedHour = 0;
	private int lastSelectMin = 0;
	
	private Handler timeChangeHandler;
	private Runnable timeChangeRunnable;
	
	public TiUITimePicker(TiViewProxy proxy)
	{
		super(proxy);
	}
	public TiUITimePicker(final TiViewProxy proxy, Activity activity)
	{
		this(proxy);
		Log.d(TAG, "Creating a time picker", Log.DEBUG_MODE);
		
		TimePicker picker;
		// If it is not API Level 21 (Android 5.0), create picker normally.
		// If not, it will inflate a spinner picker to address a bug.
		if (Build.VERSION.SDK_INT != Build.VERSION_CODES.LOLLIPOP) {
			picker = new TimePicker(activity)
			{
				@Override
				protected void onLayout(boolean changed, int left, int top, int right, int bottom)
				{
					super.onLayout(changed, left, top, right, bottom);
					TiUIHelper.firePostLayoutEvent(proxy);
				}
			};
		} else {
			// A bug where PickerCalendarDelegate does not send events to the
			// listener on API Level 21 (Android 5.0) for TIMOB-19192
			// https://code.google.com/p/android/issues/detail?id=147657
			// Work around is to use spinner view instead of calendar view in
			// in Android 5.0
			int timePickerSpinner;
			try {
				timePickerSpinner = TiRHelper.getResource("layout.titanium_ui_time_picker_spinner");
			} catch (ResourceNotFoundException e) {
				if (Log.isDebugModeEnabled()) {
					Log.e(TAG, "XML resources could not be found!!!");
				}
				return;
			}
			picker = (TimePicker) activity.getLayoutInflater().inflate(timePickerSpinner, null);
		}
		picker.setIs24HourView(false);
		picker.setOnTimeChangedListener(this);
		picker.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
			@Override
			public void onViewAttachedToWindow(View v) {
				timeChangeHandler = new Handler();
				timeChangeRunnable = new Runnable() {
					@Override
					public void run() {
						detectTimeChange();
						if (timeChangeRunnable != null) {
							timeChangeHandler.postDelayed(this, 500);
						}
					}
				};
				timeChangeHandler.postDelayed(timeChangeRunnable, 500);
			}

			@Override
			public void onViewDetachedFromWindow(View v) {
				if (timeChangeHandler != null) {
					timeChangeHandler.removeCallbacks(timeChangeRunnable);
				}
			}
		});
		setNativeView(picker);
	}
	
	@Override
	public void processProperties(KrollDict d) {
		super.processProperties(d);
		
		boolean valueExistsInProxy = false;
		Calendar calendar = Calendar.getInstance();
	    
        TimePicker picker = (TimePicker) getNativeView();
        if (d.containsKey(TiC.PROPERTY_VALUE)) {
            calendar.setTime((Date) d.get(TiC.PROPERTY_VALUE));
            valueExistsInProxy = true;
        }   
        if (d.containsKey(TiC.PROPERTY_MIN_DATE)) {
            this.minDate = (Date) d.get(TiC.PROPERTY_MIN_DATE);
        }   
        if (d.containsKey(TiC.PROPERTY_MAX_DATE)) {
            this.maxDate = (Date) d.get(TiC.PROPERTY_MAX_DATE);
        }   
        if (d.containsKey(TiC.PROPERTY_MINUTE_INTERVAL)) {
            int mi = d.getInt(TiC.PROPERTY_MINUTE_INTERVAL);
            if (mi >= 1 && mi <= 30 && mi % 60 == 0) {
                this.minuteInterval = mi; 
            }   
        }   
        
        // Undocumented but maybe useful for Android
        boolean is24HourFormat = false;
        if (d.containsKey(TiC.PROPERTY_FORMAT_24)) {
        	is24HourFormat = d.getBoolean(TiC.PROPERTY_FORMAT_24);
        }
    	picker.setIs24HourView(is24HourFormat);
        
        setValue(calendar.getTimeInMillis());
        
        if (!valueExistsInProxy) {
        	proxy.setProperty(TiC.PROPERTY_VALUE, calendar.getTime());
        }
        
        //iPhone ignores both values if max <= min
        if (minDate != null && maxDate != null) {
            if (maxDate.compareTo(minDate) <= 0) {
                Log.w(TAG, "maxDate is less or equal minDate, ignoring both settings.");
                minDate = null;
                maxDate = null;
            }   
        }
	}
	
	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue,
			KrollProxy proxy)
	{
		if (key.equals(TiC.PROPERTY_VALUE)) {
			Date date = (Date) newValue;
			setValue(date.getTime());
		} else if (key.equals(TiC.PROPERTY_FORMAT_24)) {
			((TimePicker) getNativeView()).setIs24HourView(TiConvert.toBoolean(newValue));
		}
		super.propertyChanged(key, oldValue, newValue, proxy);
	}
	
	public void setValue(long value)
	{
		TimePicker picker = (TimePicker) getNativeView();

		int currentSelectedHour, currentSelectedMin;
		if (Build.VERSION.SDK_INT >= 23) {
			currentSelectedHour = ApiLevel23.getHourFrom(picker);
			currentSelectedMin = ApiLevel23.getMinuteFrom(picker);
		} else {
			currentSelectedHour = picker.getCurrentHour();
			currentSelectedMin = picker.getCurrentMinute();
		}
		lastSelectedHour = currentSelectedHour;
		lastSelectMin = currentSelectedMin;
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(value);
		if (Build.VERSION.SDK_INT >= 23) {
			ApiLevel23.setHourFor(picker, calendar.get(Calendar.HOUR_OF_DAY));
			ApiLevel23.setMinuteFor(picker, calendar.get(Calendar.MINUTE));
		} else {
			picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
			picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
		}
	}

	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
	{
		detectTimeChange();
	}
	
	private void detectTimeChange() {
		Calendar calendar = Calendar.getInstance();
		TimePicker picker = (TimePicker) getNativeView();
		if (picker == null) {
			return;
		}
		
		int currentSelectedHour, currentSelectedMin;
		if (Build.VERSION.SDK_INT >= 23) {
			currentSelectedHour = ApiLevel23.getHourFrom(picker);
			currentSelectedMin = ApiLevel23.getMinuteFrom(picker);
		} else {
			currentSelectedHour = picker.getCurrentHour();
			currentSelectedMin = picker.getCurrentMinute();
		}
		
		boolean hasChanged = lastSelectedHour != currentSelectedHour || lastSelectMin != currentSelectedMin;
		if (hasChanged) {
			calendar.set(calendar.HOUR_OF_DAY, currentSelectedHour);
			calendar.set(Calendar.MINUTE, currentSelectedMin);
			lastSelectedHour = currentSelectedHour;
			lastSelectMin = currentSelectedMin;
			KrollDict data = new KrollDict();
			data.put(TiC.PROPERTY_VALUE, calendar.getTime());
			proxy.setPropertyAndFire(TiC.PROPERTY_VALUE, calendar.getTime());
			fireEvent(TiC.EVENT_CHANGE, data);
		}
	}

	private static class ApiLevel23
	{
		private ApiLevel23() {}

		public static int getHourFrom(TimePicker picker)
		{
			return picker.getHour();
		}
		
		public static void setHourFor(TimePicker picker, int hour) {
			picker.setHour(hour);
		}

		public static int getMinuteFrom(TimePicker picker)
		{
			return picker.getMinute();
		}

		public static void setMinuteFor(TimePicker picker, int minute) {
			picker.setMinute(minute);
		}
	}
}
