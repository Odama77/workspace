/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.manager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.span.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MessageService extends Service {
	
	public static String TAG = "MessageService";
	
	public static final int MESSAGE_PORT = 9000;
	public static final int MAX_MESSAGE_LENGTH = 3*1024*1024; // 10;
	
	public static final String MESSAGE_FROM_KEY = "message_from";
	public static final String MESSAGE_CONTENT_KEY = "message_content";
	
	private NotificationManager notifier = null;
	
	private Notification notification = null;
	
	private PendingIntent pendingIntent = null;
	
    // one thread for all activities
    private static Thread msgListenerThread = null;
    
    //Amado Section
    public static ConcurrentLinkedQueue <String> chatQueue = null;
    public static ArrayList<String> messageList = null;
    //End of Amado Section
    
    private int notificationId = 0;
    
    @Override 
    public void onCreate() {
    	Log.v(TAG, "onCreated()");
    	// do nothing until prompted by startup activity
    }    
    
    @Override    
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.v(TAG, "onStartCommand()");
    	if (msgListenerThread == null) {
    		messageList = new ArrayList<String>();
    		chatQueue = new ConcurrentLinkedQueue<String>();
	    	msgListenerThread = new MessageListenerThread(chatQueue);
	    	msgListenerThread.start();
    	}
    	
    	return START_STICKY; // run until explicitly stopped    
	}
    
    @Override    
    public void onDestroy() {        
    	// TODO Auto-generated method stub
	}    
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
    
    /**     
     * Show a notification while this service is running.     
     * @throws UnknownHostException 
     */    
    private void showNotification(String tickerStr, Bundle extras) throws UnknownHostException {
    	Log.v(TAG, "showNotification()");
    	if (notifier == null) {
    		// get reference to notifier
    		notifier = (NotificationManager)getSystemService(NOTIFICATION_SERVICE); 
    	}
    	
    	//Amado Section
    	String from = null;
    	if (chatQueue != null){
    		String msg = chatQueue.poll();
    		from = msg.substring(0, msg.indexOf("\n"));
			String content = msg.substring(msg.indexOf("\n")+1);
    		messageList.add(from + ": "+ content);
    		for(String x : messageList)
    			Log.v(TAG, x);
    	}
    	//End of Amado Section
    	
    	
    	
    	
    	// unique notification id
    	notificationId++;
    	
    	// set the icon, ticker text, and timestamp        
    	notification = 
    		new Notification(R.drawable.exclamation, tickerStr, System.currentTimeMillis());
    	  	
    	Intent intent = new Intent(this, ViewMessageActivity.class);
    	if (extras != null) {
    		intent.putExtras(extras);
    	}
    	
    	// NOTE: Use a unique notification id to ensure a new pending intent is created.
    	
    	// pending intent to launch main activity if the user selects notification	  
    	pendingIntent = 
    		PendingIntent.getActivity(this, notificationId, intent, 0);
    	
    	// cancel the notification after it is checked by the user
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	
    	// vibrate
    	// notification.defaults |= Notification.DEFAULT_VIBRATE; // DEBUG

    	// set the info for the views that show in the notification panel    
    	notification.setLatestEventInfo(this, "Wireless AdHoc", tickerStr, pendingIntent);
    	
    	// NOTE: Use a unique notification id, otherwise an existing notification with the same id will be replaced.
    	
    	// send the notification
    	notifier.notify(notificationId, notification);
    }
    
    private class MessageListenerThread extends Thread {
    	
    	public ConcurrentLinkedQueue<String> chatQueue = null;
    	
    	public MessageListenerThread(ConcurrentLinkedQueue<String> chatQueue) {
			// TODO Auto-generated constructor stub
    		this.chatQueue = chatQueue;
		}
    	
    	public void run() {
    		try {
    			// bind to local machine; will receive broadcasts and directed messages
    			// will most likely bind to 127.0.0.1 (localhost)
    			DatagramSocket socket = new DatagramSocket(MESSAGE_PORT);
//    			ServerSocket socket = new ServerSocket(MESSAGE_PORT);
    			
    			byte[] buff = new byte[MAX_MESSAGE_LENGTH];
				DatagramPacket packet = new DatagramPacket(buff, buff.length);
				
				while (true) {
					try {
						// address Android issue where old packet lengths are erroneously 
						// carried over between packet reuse
						packet.setLength(buff.length); 
//						Socket connectionSocket = socket.accept();
						
//						BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
						
//						DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
						socket.receive(packet); // blocking
						
						String msg = new String(packet.getData(), 0, packet.getLength());
//						byte[] receivedData = inFromClient.readLine().getBytes();
//						String msg = new String(receivedData, 0, receivedData.length);
						String from = msg.substring(0, msg.indexOf("\n"));
						String content = msg.substring(msg.indexOf("\n")+1);
						
						chatQueue.add(msg);
						
						String tickerStr = "New message";
						
				    	Bundle extras = new Bundle();
				    	extras.putString(MESSAGE_FROM_KEY, from);
				    	extras.putString(MESSAGE_CONTENT_KEY, content);
						
						showNotification(tickerStr, extras);
//						outToClient.writeBytes("By Amado");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
}