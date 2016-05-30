/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.manager;

import org.span.service.CircularStringBuffer;
import org.span.service.LogObserver;
import org.span.manager.ManetManagerAdapter;

public class ViewLogActivityHelper implements LogObserver{
	private ViewLogActivity activity = null;
	
	public boolean messageScrollLock = true;
	public CircularStringBuffer buff = new CircularStringBuffer();
	
	// singleton
	private static ViewLogActivityHelper instance = null;
	
	private ViewLogActivityHelper() {}
	
	public static void setApplication(ManetManagerAdapter manetManagerAdapter) {
		if (instance == null) {
			instance = new ViewLogActivityHelper();
		}
		manetManagerAdapter.manetHelper.registerLogger(instance);
	}
	
	public static ViewLogActivityHelper getInstance(ViewLogActivity activity) {
		if (instance == null) {
			instance = new ViewLogActivityHelper();
		}
		
		// can only be associated with one activity at a time
		instance.activity = activity;
		
		return instance;
	}
	
	// callback method
	@Override
	public void onLogUpdated(String content) {
		if (activity != null) {
			activity.appendMessage(content);
		}
	}
}
