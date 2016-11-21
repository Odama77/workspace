/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.manager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.span.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import org.span.service.system.Encryption;

public class MessageService extends Service {
	
	public static String TAG = "MessageService";
	
	public static final int MESSAGE_PORT = 9000;
	public static final int RTP_PORT = 7777;
	public static final int RTP_PORT2 = 7778;
	public static final int MAX_MESSAGE_LENGTH = 3*1024*1024; // 10;
	
	public static final String MESSAGE_FROM_KEY = "message_from";
	public static final String MESSAGE_CONTENT_KEY = "message_content";
	
	private NotificationManager notifier = null;
	
	private Notification notification = null;
	
	private PendingIntent pendingIntent = null;
	
    // one thread for all activities
    private static Thread msgListenerThread = null;
    private static Thread msgListenerThread2 = null;
    
    //Amado Section
    public static ConcurrentLinkedQueue <String> chatQueue = null;
    public static ArrayList<String> messageList = null;
    
    public static ConcurrentLinkedQueue <String> rtpQueue = null;
    public static ArrayList<String> rtpList = null;
    public String ACK = null;
    public Ringtone ringtone = null;
    public Ringtone ringtone2 = null;
	public AudioStream audioStream;
	public AudioGroup audioGroup;
	public static boolean answerFlag = false;
	public static String sourceIP = null;
    //End of Amado Section
    
    private int notificationId = 0;
    public static String destinationIP = null;
    
    @Override 
    public void onCreate() {
    	Log.v(TAG, "onCreated()");
    	
    }    
    
    @Override    
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.v(TAG, "onStartCommand()");
    	if (msgListenerThread == null) {
    		messageList = new ArrayList<String>();
    		chatQueue = new ConcurrentLinkedQueue<String>();
	    	try {
				msgListenerThread = new MessageListenerThread(chatQueue);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	msgListenerThread.start();
    	}
    	
    	if (msgListenerThread2 == null) {
    		rtpList = new ArrayList<String>();
    		rtpQueue = new ConcurrentLinkedQueue<String>();
	    	try {
				msgListenerThread2 = new MessageListenerThread2(rtpQueue);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		msgListenerThread2.start();
    	}
    	
    	try {
    		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
			
			Uri notification2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			ringtone2 = RingtoneManager.getRingtone(getApplicationContext(), notification2);
		} catch (Exception e) {}
    	
    	return START_STICKY; // run until explicitly stopped  
	}
    
    @Override    
    public void onDestroy() {        
    	// TODO Auto-generated method stub
	}    
    
    
    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    
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
    		
    		if(messageList.size() == 11)
    			messageList.remove(0);
    	}
    	//End of Amado Section
    	
    	// unique notification id
    	notificationId++;
    	
    	if(!MainActivity.onSendFlag){
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
    	
//    	Update UI for Chat
    	Intent local = new Intent();

    	local.setAction("com.hello.action");

    	this.sendBroadcast(local);
//    	XXXXXXXXXXXXXXXXXXXXXXXXXXX
    }
    
    private void callNotification() throws UnknownHostException {
    	
    	String from = null;
    	if (rtpQueue != null){
    		String msg = rtpQueue.poll();
    		from = msg.substring(0, msg.indexOf("\n"));
			String content = msg.substring(msg.indexOf("\n")+1);
    		
			StringTokenizer parts = new StringTokenizer(from);
			sourceIP = parts.nextToken(" (");
    		Log.v("Call message:", content);
    		Log.v("Call sourceIP:", sourceIP);
    		
    		
    		//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    		//XXXXXXXXXXXXXXXX TYPE OF MESSAGES XXXXXXXXXXXXXXXXXXXXXX
    		//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    		
    		if(content.toLowerCase().indexOf("<<1>>".toLowerCase()) != -1){
    			StringTokenizer first_message = new StringTokenizer(content,"&");
    			first_message.nextToken();
    			destinationIP = first_message.nextToken();
    		}
    		
    		
    		if(content.toLowerCase().indexOf("<<1>>".toLowerCase()) != -1){					//CONNECT MESSAGE
    			Log.v("Connect <<1>>:", content);
    			AudioManager audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
    	    	audio.setMode(AudioManager.MODE_RINGTONE);
    			msg = sourceIP + " (" + "Source" + ")\n" + "<<2>>";
  				try {
  					SendMessageTask task = new SendMessageTask();
  					task.execute(new String[] {sourceIP, msg});
  				} catch (Exception e) {
  				}
  				
  				try{ringtone.play();
  				} catch (Exception e) {}
  				
  				answerFlag = true;
  				
  				//Update UI for RTP
  		    	Intent local = new Intent();
  		    	local.setAction("com.start.action");
  		    	this.sendBroadcast(local);
    		}else if(content.toLowerCase().indexOf("<<2>>".toLowerCase()) != -1){		//ACK MESSAGE
    			Log.v("ACK <<2>>:", content);
    			AudioManager audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
    	    	audio.setMode(AudioManager.MODE_RINGTONE);
    			
    			try{ringtone2.play();
    			} catch (Exception e) {}
    			
    		}else if(content.toLowerCase().indexOf("<<3>>".toLowerCase()) != -1){		//ACK OK MESSAGE
    			Log.v("ACK OK <<3>>:", content);
    			
    			try {   
    				ringtone.stop();
    				ringtone2.stop();
    				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
    			    StrictMode.setThreadPolicy(policy);
  	      	    	AudioManager audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
  	      	    	audio.setMode(AudioManager.MODE_IN_COMMUNICATION);
  	      	    	audioGroup = new AudioGroup();
  	      	    	audioGroup.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);        
  	      	    	audioStream = new AudioStream(InetAddress.getByAddress(getLocalIPAddress()));
  	      	    	audioStream.setCodec(AudioCodec.PCMU);
  	      	    	audioStream.setMode(RtpStream.MODE_NORMAL);
  	      	    	StringTokenizer parts2 = new StringTokenizer(sourceIP);
  	      	    	audioStream.associate(InetAddress.getByAddress(new byte[] {(byte)Integer.parseInt(parts2.nextToken(".")),
  	      	    		  													 (byte)Integer.parseInt(parts2.nextToken(".")), 
  	      	    		  													 (byte)Integer.parseInt(parts2.nextToken(".")), 
  	      	    		  													 (byte)Integer.parseInt(parts2.nextToken(".")) }), 55555);
	  	      	} catch (Exception e) {
	  	      		e.printStackTrace();
	  	      	}
    			audioStream.join(audioGroup);
    		
    		}else if(content.toLowerCase().indexOf("<<4>>".toLowerCase()) != -1){		//ACK OK MESSAGE
    			Log.v("Stop ring <<4>>:", content);
    			try{
    				ringtone.stop();
					ringtone2.stop();
				}catch(Exception e){}
    			
    		}else if(content.toLowerCase().indexOf("<<0>>".toLowerCase()) != -1){		//DISCONNECT MESSAGE
    			Log.v("Disconnect <<0>>:", content);

    			msg = sourceIP + " (" + "Source" + ")\n" + "<<00>>";
  				try {
  					ringtone.stop();
    				ringtone2.stop();
  					SendMessageTask task = new SendMessageTask();
  					task.execute(new String[] {sourceIP, msg});
  					
  					audioGroup.clear();
  				} catch (Exception e) {}
  				
  				//Stop Call
  		    	Intent local = new Intent();
  		    	local.setAction("com.stop.action");
  		    	this.sendBroadcast(local);
  		    	
    		}else if(content.toLowerCase().indexOf("<<00>>".toLowerCase()) != -1){		//DISCONNECT REPLAYMESSAGE
    			Log.v("Disconenct Replay <<00>>:", content);
    			
    			try{
    				ringtone.stop();
    				ringtone2.stop();
    				audioGroup.clear();
    			}catch(Exception e){}
    			
    			//Stop Call
  		    	Intent local = new Intent();
  		    	local.setAction("com.stop.action");
  		    	this.sendBroadcast(local);
    		}
    		
    		//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    		//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    	}
    }
    

    	
    
	  public static byte[] getLocalIPAddress () {
		    byte ip[]=null;
		       try {
		           for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
		               NetworkInterface intf = (NetworkInterface) en.nextElement();
		               for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
		                   InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
		                   if (!inetAddress.isLoopbackAddress()) {
		                    ip= inetAddress.getAddress();
		                   }
		               }
		           }
		       } catch (SocketException ex) {
		           Log.i("SocketException ", ex.toString());
		       }
		       return ip;
		 }
	  
    private class MessageListenerThread extends Thread {
    	
    	public ConcurrentLinkedQueue<String> chatQueue = null;
    	public Encryption encrypt = null;
    	public MessageListenerThread(ConcurrentLinkedQueue<String> chatQueue) throws Exception{
			// TODO Auto-generated constructor stub
    		this.chatQueue = chatQueue;
    		encrypt = new Encryption();
		}
    	
    	public void run() {
    		try {
    			// bind to local machine; will receive broadcasts and directed messages
    			// will most likely bind to 127.0.0.1 (localhost)
    			DatagramSocket socket = new DatagramSocket(MESSAGE_PORT);
    			
    			byte[] buff = new byte[MAX_MESSAGE_LENGTH];
				DatagramPacket packet = new DatagramPacket(buff, buff.length);
				
				while (true) {
					try {
						// address Android issue where old packet lengths are erroneously 
						// carried over between packet reuse
						packet.setLength(buff.length); 
						socket.receive(packet); // blocking
						
						String msg = new String(packet.getData(), 0, packet.getLength());
						msg = encrypt.decrypt(msg);
						System.out.println("Mensaje -> " + msg);
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
    
    private class MessageListenerThread2 extends Thread {
    	public ConcurrentLinkedQueue<String> rtpQueue = null;
    	public Encryption encrypt = null;
    	public MessageListenerThread2(ConcurrentLinkedQueue<String> rtpQueue) throws Exception{
			// TODO Auto-generated constructor stub
    		this.rtpQueue = rtpQueue;
    		encrypt = new Encryption();
		}
    	
    	public void run() {
    		try {
    			// bind to local machine; will receive broadcasts and directed messages
    			// will most likely bind to 127.0.0.1 (localhost)
    			DatagramSocket socket = new DatagramSocket(RTP_PORT);
    			
    			byte[] buff = new byte[MAX_MESSAGE_LENGTH];
				DatagramPacket packet = new DatagramPacket(buff, buff.length);
				
				while (true) {
					try {
						// address Android issue where old packet lengths are erroneously 
						// carried over between packet reuse
						packet.setLength(buff.length); 
						socket.receive(packet); // blocking
						
						String msg = new String(packet.getData(), 0, packet.getLength());
						msg = encrypt.decrypt(msg);
						System.out.println("Mensaje -> " + msg);
						String from = msg.substring(0, msg.indexOf("\n"));
						String content = msg.substring(msg.indexOf("\n")+1);
						
						rtpQueue.add(msg);
						callNotification();
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
    
    private class SendMessageTask extends AsyncTask<String, Void, String> {
		
		 @Override
		 protected String doInBackground(String... params) {
			Log.v(TAG, "doInBackground()");
			String address = params[0]; 
			String msg = params[1]; 
			try{
				String retval = sendMessage(address, msg);
				return retval;
			}catch(Exception e){
				return "";
			}
		 }
		 
		 private String sendMessage(String address, String msg) throws Exception {
			 Encryption encrypt = new Encryption();
			 Log.v(TAG, "sendMessage()");
			 	String retval = null;
				DatagramSocket socket = null;
				try {
					/*Amado section*/
					socket = new DatagramSocket();
					
					msg = encrypt.encrypt(msg);

					byte buff[] = msg.getBytes();
					int msgLen = buff.length;
					boolean truncated = false;
					if (msgLen > MessageService.MAX_MESSAGE_LENGTH) {
						msgLen = MessageService.MAX_MESSAGE_LENGTH;
						truncated = true;
					}
				
					DatagramPacket packet = 
							new DatagramPacket(buff, msgLen, InetAddress.getByName(address), MessageService.RTP_PORT);
					socket.send(packet);
					/*Amado section End*/
					if (truncated) {
						retval = "Message truncated and sent.";
					} else {
						retval = "Message sent.";
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					retval = "Error: " + e.getMessage();
				} finally {
					if (socket != null) {
						socket.close();
					}
				}
				
				return retval;
			}
	 };

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
    
}