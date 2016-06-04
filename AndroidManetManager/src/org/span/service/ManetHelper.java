/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.service;

import java.util.HashSet;
import java.util.Set;

import org.span.service.core.ManetService;
import org.span.service.core.ManetService.AdhocStateEnum;
import org.span.service.routing.Node;
import org.span.service.system.ManetConfig;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class ManetHelper {

	// private members
	
	private final static String TAG = "ManetContext";
	
	private ServiceConnection serviceConn = null;
	
	private Messenger receiveMessenger = null;
	
	private Messenger sendMessenger = null;
	
	private IntentFilter intentFilter = null;
	
	private BroadcastReceiver intentReceiver = null;
	
	private Context context = null;
	
	private Set<ManetObserver> manetObservers = null;
	private Set<LogObserver> logObservers = null;
	
	public SharedPreferences settings = null;
	
	
	// constructor
	public ManetHelper(Context context) {
		Log.v(TAG, "ManetHelper Constructor");
		this.context = context;
		manetObservers = new HashSet<ManetObserver>();
		logObservers = new HashSet<LogObserver>();
		
        // create the intent receiver that will be used to listen to status broadcasts
        intentFilter = new IntentFilter();
        intentFilter.addAction(ManetService.ACTION_SERVICE_STARTED);
        intentFilter.addAction(ManetService.ACTION_SERVICE_STOPPED);
        intentFilter.addAction(ManetService.ACTION_ADHOC_STATE_UPDATED);
        intentFilter.addAction(ManetService.ACTION_CONFIG_UPDATED);
        intentFilter.addAction(ManetService.ACTION_LOG_UPDATED);
        intentFilter.addAction(ManetService.ACTION_PEERS_UPDATED);
        
        intentReceiver = new ManetBroadcastReceiver();
        context.registerReceiver(intentReceiver, intentFilter);
	}
	
	public void registerObserver(ManetObserver observer) {
		Log.v(TAG, "registerOpserver()");
		manetObservers.add(observer);
	}
	
	public void unregisterObserver(ManetObserver observer) {
		Log.v(TAG, "unregisterObserver()");
		manetObservers.remove(observer);
	}
	
	public void registerLogger(LogObserver observer) {
		Log.v(TAG, "registerLogger()");
		logObservers.add(observer);
	}
	
	public void unregisterLogger(LogObserver observer) {
		Log.v(TAG, "unregisterLogger()");
		logObservers.remove(observer);
	}
	
	
	// status methods
	
	public boolean isConnectedToService() {
		// return serviceConn != null;
		Log.v(TAG, "isConenctedToService()");
		return sendMessenger != null;
	}
	
	
	// convenience methods
	
	public void connectToService() {
		// create receive messenger
		Log.v(TAG, "connectToService()");
		receiveMessenger = new Messenger(new IncomingHandler());
		
		// start service (if it is not already started) so that its lifetime is not 
		// bound to any contexts even if contexts bind to it at a later time
		// ComponentName name = context.startService(new Intent(context, ManetService.class)); // DEBUG
        
        // to start a Service or Activity in another package, use setComponent()
        Intent i = new Intent().setComponent(new ComponentName("org.span", "org.span.service.core.ManetService"));
        context.startService(i);
		
		// bind to service        
		serviceConn = new ManetServiceConnection();
		context.bindService(i, serviceConn, Context.BIND_AUTO_CREATE); // DEBUG
	}
	
	public void disconnectFromService() {
		Log.v(TAG, "disconnectFromService()");
		context.unregisterReceiver(intentReceiver);
		intentReceiver = null;
		
		context.unbindService(serviceConn);
		serviceConn = null;
		
		// Setting this ensures that isConnectedToService() returns
		// the correct value.
		sendMessenger = null;
	}
	
	public void sendStartAdhocCommand() {
		Log.v(TAG, "sendStartAdhocCommand()");
		sendMessage(ManetService.COMMAND_START_ADHOC);
	}
	
	public void sendStopAdhocCommand() {
		Log.v(TAG, "sendStopAdhocCommand()");
		sendMessage(ManetService.COMMAND_STOP_ADHOC);
	}
	
	public void sendRestartAdhocCommand() {
		Log.v(TAG, "sendRestartAdhocCommand()");
		sendMessage(ManetService.COMMAND_RESTART_ADHOC);
	}
	
	public void sendManetConfigUpdateCommand(ManetConfig config) {
		Log.v(TAG, "sendManetConfigUpdateCommand()");
		try {
			Bundle data = new Bundle();
			data.putSerializable(ManetService.CONFIG_KEY, config);
			
			Message message = Message.obtain(null, ManetService.COMMAND_MANET_CONFIG_UPDATE);
			message.setData(data);
			message.replyTo = receiveMessenger;
			sendMessenger.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void sendManetConfigLoadCommand(String filename){
		Log.v(TAG, "sendManetConfigLoadCommand()");
		try{
			Bundle data = new Bundle();
			data.putString(ManetService.FILE_KEY, filename);
			
			Message message = Message.obtain(null, ManetService.COMMAND_MANET_CONFIG_LOAD);
			message.setData(data);
			message.replyTo = receiveMessenger;
			sendMessenger.send(message);
		} catch (RemoteException e){
			e.printStackTrace();
		}
	}
	
	public void sendAdhocStatusQuery() {
		Log.v(TAG, "sendAdhocStatusQuery()");
		sendMessage(ManetService.QUERY_ADHOC_STATUS);
	}
	
	public void sendManetConfigQuery() {
		Log.v(TAG, "sendManetConfigQuery()");
		sendMessage(ManetService.QUERY_MANET_CONFIG);
	}
	
	public void sendPeersQuery() {
		Log.v(TAG, "sendPeersQuery()");
		sendMessage(ManetService.QUERY_PEERS);
	}
	
	public void sendRoutingInfoQuery() {
		Log.v(TAG, "sendRoutingInfoQuery()");
		sendMessage(ManetService.QUERY_ROUTING_INFO);
	}
	
	private void sendMessage(int what) {
		Log.v(TAG, "sendMessage()");
		if(sendMessenger == null) {
			Log.e("ManetHelper::sendMessage", "You must connect to the ManetService before sending messages to it!");
			return;
	    	}
		try {
			Message message = Message.obtain(null, what);           
			message.replyTo = receiveMessenger;
			sendMessenger.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	// listen for intent broadcasts
	private class ManetBroadcastReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(TAG, "onReceive()");
			String action = intent.getAction();
			Bundle data = intent.getExtras();
			if(action.equals(ManetService.ACTION_SERVICE_STARTED)) {
				for(ManetObserver observer : manetObservers) {
					observer.onServiceStarted();
				}
		    } else if(action.equals(ManetService.ACTION_SERVICE_STOPPED)) {
		    	for(ManetObserver observer : manetObservers) {
					observer.onServiceStopped();
				}
		    } else if(action.equals(ManetService.ACTION_ADHOC_STATE_UPDATED)) {
		    	AdhocStateEnum state = (AdhocStateEnum)data.getSerializable(ManetService.STATE_KEY);
		    	String info = data.getString(ManetService.INFO_KEY);
		    	for(ManetObserver observer : manetObservers) {
					observer.onAdhocStateUpdated(state, info);
				}
		    } else if(action.equals(ManetService.ACTION_CONFIG_UPDATED)) {
		    	ManetConfig config = (ManetConfig)data.getSerializable(ManetService.CONFIG_KEY);
		    	for(ManetObserver observer : manetObservers) {
					observer.onConfigUpdated(config);
				}
		    }  else if(action.equals(ManetService.ACTION_LOG_UPDATED)) {
		    	String content = data.getString(ManetService.LOG_KEY);
		    	for(LogObserver observer : logObservers) {
					observer.onLogUpdated(content);
				}
		    }  else if(action.equals(ManetService.ACTION_PEERS_UPDATED)) {
		        // Log.i("ManetBroadcastReceiver", "ACTION_PEERS_UPDATED");
		        for(ManetObserver observer : manetObservers) {
		            observer.onPeersUpdated((HashSet<Node>)data.getSerializable(ManetService.PEERS_KEY));
		        }
		    }
		 }
	};
	
	// receive messages from service
	private class IncomingHandler extends Handler {
		
		@Override    
		public void handleMessage(Message rxmessage) {
			Log.v(TAG, "IncomingHandler handleMessage()");
			Bundle data = rxmessage.getData();
			switch (rxmessage.what) {
				case ManetService.QUERY_ADHOC_STATUS:
			    	AdhocStateEnum state = (AdhocStateEnum)data.getSerializable(ManetService.STATE_KEY);
			    	String info = data.getString(ManetService.INFO_KEY);
					for(ManetObserver observer : manetObservers) {
						observer.onAdhocStateUpdated(state, info);
					}                
					break;            
					
				case ManetService.QUERY_MANET_CONFIG:
					ManetConfig config = (ManetConfig)data.getSerializable(ManetService.CONFIG_KEY);
					for(ManetObserver observer : manetObservers) {
						observer.onConfigUpdated(config);
					} 
					break;
					
				case ManetService.QUERY_PEERS:
					HashSet<Node> peers = (HashSet<Node>)data.getSerializable(ManetService.PEERS_KEY);
					for(ManetObserver observer : manetObservers) {
						observer.onPeersUpdated(peers);
					} 
					break;
					
				case ManetService.QUERY_ROUTING_INFO:
					String routingInfo = data.getString(ManetService.INFO_KEY);
					for(ManetObserver observer : manetObservers) {
						observer.onRoutingInfoUpdated(routingInfo);
					} 
					break;
			}
		}
	}

	// service connection
	private class ManetServiceConnection implements ServiceConnection {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v(TAG, "ManetServiceConnected onServiceConnected()");
			// we will communicate with the service through an IDL interface
			sendMessenger = new Messenger(service);
	    	for(ManetObserver observer : manetObservers) {
				observer.onServiceConnected();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {	
			Log.v(TAG, "ManetServiceConnected onServiceDisconnected()");
			sendMessenger = null;
	    	for(ManetObserver observer : manetObservers) {
				observer.onServiceDisconnected();
			}
	    	
	    	// attempt to reconnect
	    	ManetHelper.this.connectToService();
		}
	}
}
