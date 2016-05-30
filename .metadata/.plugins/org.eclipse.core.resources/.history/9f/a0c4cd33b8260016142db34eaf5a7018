package org.span.manager;

import java.util.HashSet;
import java.util.TreeSet;

import org.span.service.ManetHelper;
import org.span.service.ManetObserver;
import org.span.service.core.ManetService.AdhocStateEnum;
import org.span.service.routing.Node;
import org.span.service.system.ManetConfig;


import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.content.SharedPreferences;

public class ManetManagerAdapter extends Application implements ManetObserver {
	
	// Configuring the manet helper
	public ManetHelper manetHelper = null;
	
	// preferences
	public SharedPreferences sharedPreferences = null;
	public SharedPreferences.Editor prefEditor = null;
	
	// Magnet Configurator
	public ManetConfig manetConfig = null;
	
	// Ad-hoc State
	public AdhocStateEnum adhocStateEnum = null;
	
	// Single
	private static ManetManagerAdapter manetManagerAdapter = null;
	
	public static final String TAG = "This is the manetManagerAdapter";
	
	public static ManetManagerAdapter getInstance(){
		return manetManagerAdapter;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		Log.d(TAG, "onCreate()");
		
		manetManagerAdapter = this;
		sharedPreferences =  PreferenceManager.getDefaultSharedPreferences(this);
		prefEditor = sharedPreferences.edit();
		
		//initialize manet helper
		manetHelper = new ManetHelper(this);
		manetHelper.registerObserver(this);
		
		//initialize activity helpers to start observing
		ViewLogActivityHelper.setApplication(this);
	}

	public void displayToastMessage(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
	public void focusAndshowKeyboard(final View view) {
		view.requestFocus();
		view.postDelayed(new Runnable() {
              @Override
              public void run() {
                  InputMethodManager keyboard = (InputMethodManager)
                  getSystemService(getBaseContext().INPUT_METHOD_SERVICE);
                  keyboard.showSoftInput(view, 0);
              }
          },100);
	}
	
	public int getVersionNumber() {
    	int version = -1;
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pi.versionCode;
        } catch (Exception e) {
            Log.e(TAG, "Package name not found", e);
        }
        return version;
    }
    
    public String getVersionName() {
    	String version = "?";
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pi.versionName;
        } catch (Exception e) {
            Log.e(TAG, "Package name not found", e);
        }
        return version;
    }
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		Log.d(TAG, "onTerminate()");
		
    	// manet.stopAdhoc();
    	manetHelper.disconnectFromService();
	}
	
	@Override
	public void onServiceConnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServiceDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServiceStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServiceStopped() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAdhocStateUpdated(AdhocStateEnum state, String info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConfigUpdated(ManetConfig manetConfig) {
		// TODO Auto-generated method stub
		// Log.d(TAG, "onConfigUpdated()"); // DEBUG
		this.manetConfig = manetConfig;
		
		String device = manetConfig.getDeviceType();
		Log.d(TAG, "device: " + device); // DEBUG
		
	}

	@Override
	public void onPeersUpdated(HashSet<Node> peers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRoutingInfoUpdated(String info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub
		
	}
}
