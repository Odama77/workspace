package org.span.manager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.span.R;
//import org.span.manager.SendMessageActivity.SendMessageTask;
import org.span.service.ManetObserver;
import org.span.service.core.ManetService.AdhocStateEnum;
import org.span.service.routing.Node;
import org.span.service.system.Encryption;
import org.span.service.system.ManetConfig;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class RTPGenerator extends Activity implements OnItemSelectedListener, ManetObserver{
	
	private static final String PROMPT = "Enter address ...";
	private OnClickListener send = null;
	private OnClickListener stop = null;
	private OnClickListener answer = null;
	public static Button sendRTP = null;
	public static Button stopRTP = null;
	public static Button answerRTP = null;
	AudioStream audioStream;
	AudioGroup audioGroup;
	private static boolean answerFlag = MessageService.answerFlag;
    BroadcastReceiver updateUIReciver;
    private String sourceIP = MessageService.sourceIP;
	
	public static Spinner spnRTP = null;
	private String selection = null;
	private EditText desAddress = null;
	private ManetManagerApp app = null;
	
	private static String address = null;
	
	
	private static final String TAG = "RTPGenerator";
	@Override
	 protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.rtp);
	  StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	  StrictMode.setThreadPolicy(policy);
	  
	  app = (ManetManagerApp)getApplication();
	  app.manet.registerObserver(this);
	  app.manet.sendPeersQuery();
	  
	  spnRTP = (Spinner) findViewById(R.id.spnRTP);
	  spnRTP.setOnItemSelectedListener(this);
	  desAddress = (EditText) findViewById(R.id.desAddress);
	  
	  sendRTP = (Button)findViewById(R.id.startRTP);
	  stopRTP = (Button)findViewById(R.id.stopRTP);
	  answerRTP = (Button)findViewById(R.id.answerRTP);
	  
	  send = new OnClickListener() {
      	@Override
			public void onClick(View v) {
	      		stopRTP.setVisibility(View.VISIBLE);
	    		sendRTP.setVisibility(View.GONE);
	    		answerRTP.setVisibility(View.GONE);
    	      	String destination = selection;
	  			String msg = "<<1>>&" + app.manetcfg.getIpAddress();		//Mensaje de conexion
	  			String error = null, errorMsg = "";
	  			if (selection.equals(PROMPT)) {
	  				address = desAddress.getText().toString();

	  				if (address.contains("(")) {
	  					address = address.split("(")[0];
	  				}
		  			if (!Validation.isValidIpAddress(address)) {
		  				error = "Invalid IP address.";
						errorMsg += error + "\n";
		  			}
	  			} else {
	  				address = destination;
	  			}
	  			if (destination == null) {
	  				error = "Destination is empty.";
					errorMsg += error + "\n";
	  			}
	  			if (errorMsg.isEmpty()) {
	  				msg = app.manetcfg.getIpAddress() + " (" + app.manetcfg.getUserId() + ")\n" + msg;
	  				String retval = null;
	  				try {
	  					SendMessageTask task = new SendMessageTask();
	  					task.execute(new String[] {address, msg});
	  					retval = task.get();
	  				} catch (Exception e) {
	  					retval = "Error: " + e.getMessage();
	  				}
	  			    app.displayToastMessage(retval);
	  			} else {
	  				// show error messages
	  				AlertDialog.Builder builder = new AlertDialog.Builder(RTPGenerator.this);
	  				builder.setTitle("Please Make Corrections")
	  					.setMessage(errorMsg.trim())
	  					.setCancelable(false)
	  					.setPositiveButton("OK", null);
	  				AlertDialog alert = builder.create();
	  				alert.show();
	  			}
//	  			Log.v(TAG, msg);
	  			
      			
        		MessageService.answerFlag = false;
			}
		};
		
		sendRTP.setOnClickListener(this.send);
		
		stop = new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		String msg = "<<0>>";
        		stopRTP.setVisibility(View.GONE);
        		answerRTP.setVisibility(View.GONE);
        		sendRTP.setVisibility(View.VISIBLE);
        		msg = app.manetcfg.getIpAddress() + " (" + app.manetcfg.getUserId() + ")\n" + msg;
  				try {
  					SendMessageTask task = new SendMessageTask();
  					task.execute(new String[] {address, msg});
  				} catch (Exception e) {
  				}
        		
        		MessageService.answerFlag = false;
			}
		};
		stopRTP.setOnClickListener(this.stop);
		
		answer = new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		String msg = "<<3>>";
        		String msg2 = "<<4>>";
        		msg = app.manetcfg.getIpAddress() + " (" + app.manetcfg.getUserId() + ")\n" + msg;
        		msg2 = app.manetcfg.getIpAddress() + " (" + app.manetcfg.getUserId() + ")\n" + msg2;
  				Log.v("SourceIP", MessageService.sourceIP);
  				Log.v("app.manetcfg.getIpNetwork():", app.manetcfg.getIpAddress());
  				try {
  					SendMessageTask task = new SendMessageTask();
  					task.execute(new String[] {MessageService.sourceIP, msg});
  					SendMessageTask2 task2 = new SendMessageTask2();
  					task2.execute(new String[] {app.manetcfg.getIpAddress(), msg2});
  				} catch (Exception e) {
  				}
        		
	  			answerRTP.setVisibility(View.GONE);
	      		sendRTP.setVisibility(View.GONE);
	      		stopRTP.setVisibility(View.VISIBLE);
	      		try {  
	      			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
	      		    StrictMode.setThreadPolicy(policy);
		      	      AudioManager audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
		      	      audio.setMode(AudioManager.MODE_IN_COMMUNICATION);
		      	      audioGroup = new AudioGroup();
		      	      audioGroup.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);        
		      	      audioStream = new AudioStream(InetAddress.getByAddress(getLocalIPAddress ()));
		      	      audioStream.setCodec(AudioCodec.PCMU);
		      	      audioStream.setMode(RtpStream.MODE_NORMAL);
		      	      //set receiver(vlc player) machine ip address(please update with your machine ip)
		      	      StringTokenizer parts = new StringTokenizer(MessageService.destinationIP);
		      	      audioStream.associate(InetAddress.getByAddress(new byte[] {(byte)Integer.parseInt(parts.nextToken(".")),
		      	    		  													 (byte)Integer.parseInt(parts.nextToken(".")), 
		      	    		  													 (byte)Integer.parseInt(parts.nextToken(".")), 
		      	    		  													 (byte)Integer.parseInt(parts.nextToken(".")) }), 55555);
		      	  } catch (Exception e) {
		      	   e.printStackTrace();
		      	  }
	      			audioStream.join(audioGroup);
	      			MessageService.answerFlag = false;
        	}
        	
        	
		};
		answerRTP.setOnClickListener(this.answer);
		
		Log.v(TAG, "HERE");
		if(MessageService.answerFlag){
			Log.v(TAG, "HERE2");
    		answerRTP.setVisibility(View.VISIBLE);
    		sendRTP.setVisibility(View.GONE);
    		stopRTP.setVisibility(View.VISIBLE);
    		MessageService.answerFlag = false;
		}else{
    		answerRTP.setVisibility(View.GONE);
    		sendRTP.setVisibility(View.VISIBLE);
    		stopRTP.setVisibility(View.GONE);
		}
		
		//////////////////////////////////////////////////////////////////////////////
		////////////////////////// Broadcast Recived Messages ////////////////////////
		//////////////////////////////////////////////////////////////////////////////

		//Update UI
		try {  
				IntentFilter filter = new IntentFilter();
			  	filter.addAction("com.start.action"); 
			  	updateUIReciver = new BroadcastReceiver() {
			        @Override
			        public void onReceive(Context context, Intent intent) {
			            //UI update here
			        	Log.v(TAG, "HERE3");
			    		answerRTP.setVisibility(View.VISIBLE);
			    		sendRTP.setVisibility(View.GONE);
			    		stopRTP.setVisibility(View.VISIBLE);
			        }
			  	};
			  	        
			  	registerReceiver(updateUIReciver,filter);
			  	 
			  	 //Finish Call
			  	IntentFilter filter2 = new IntentFilter();
			  	filter2.addAction("com.stop.action"); 
			  	updateUIReciver = new BroadcastReceiver() {
		            @Override
		            public void onReceive(Context context, Intent intent) {
		                //UI update here
		            	Log.v(TAG, "HERE4");
		            	try{
		        			audioGroup.clear();
		        			answerRTP.setVisibility(View.GONE);
		            		sendRTP.setVisibility(View.VISIBLE);
		            		stopRTP.setVisibility(View.GONE);
		        		}catch(Exception e){}
		            }
		        };
		        registerReceiver(updateUIReciver,filter2);
      	  } catch (Exception e) {
      	   e.printStackTrace();
      	  }

	  	  
		//////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////
	  	  
	  	  
	  	try {   
    	    	AudioManager audio =  (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
    	    	audio.setMode(AudioManager.MODE_RINGTONE);
    	    	audioGroup = new AudioGroup();
    	    	audioGroup.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);        
    	    	audioStream = new AudioStream(InetAddress.getByAddress(getLocalIPAddress()));
    	    	audioStream.setCodec(AudioCodec.PCMU);
    	    	audioStream.setMode(RtpStream.MODE_NORMAL);
    	    	StringTokenizer parts2 = new StringTokenizer("127.0.0.1");
    	    	audioStream.associate(InetAddress.getByAddress(new byte[] {(byte)Integer.parseInt(parts2.nextToken(".")),
    	    		  													 (byte)Integer.parseInt(parts2.nextToken(".")), 
    	    		  													 (byte)Integer.parseInt(parts2.nextToken(".")), 
    	    		  													 (byte)Integer.parseInt(parts2.nextToken(".")) }), 55555);
    	    	audioStream.join(audioGroup);
    			audioGroup.clear();
//    			audioStream.release();
	  	} catch (Exception e) {
	      		e.printStackTrace();
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
	  
	public static void open(Activity activity) {
		// TODO Auto-generated method stub
		Log.v(TAG, "open()");
		Intent it = new Intent("android.intent.action.RTP_ACTION");
		activity.startActivity(it);
		
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		Log.v(TAG, "onItemSelected()");
		selection = (String)spnRTP.getItemAtPosition(position);
		if (selection.equals(PROMPT)) {
			desAddress.setVisibility(EditText.VISIBLE);
			desAddress.setText(app.manetcfg.getIpNetwork());
			desAddress.setSelection(desAddress.getText().length()); // move cursor to end
			app.focusAndshowKeyboard(desAddress);
		} else {
			desAddress.setVisibility(EditText.GONE);
		}
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
	public void onConfigUpdated(ManetConfig manetcfg) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPeersUpdated(HashSet<Node> peers) {
		// provide option to enter peer address
		Log.v(TAG, "onPeersUpdated()");
		Set<String> options = new TreeSet<String>();
//		options.add(app.manetcfg.getIpBroadcast() + " (Broadcast)");
		options.add(PROMPT);
		
		String option = null;
		for (Node peer : peers) {
			if (peer.userId != null) {
				option = peer.addr + " (" + peer.userId + ")";
			} else {
				option = peer.addr;	
			}
			options.add(option);
		}
		
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, options.toArray());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnRTP.setAdapter(adapter);
		
	}
	@Override
	public void onRoutingInfoUpdated(String info) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
	public void onDestroy() {
		Log.v(TAG, "onDestroy()");
		super.onDestroy();
		
		try{
			audioGroup.clear();
    		audioStream.release();
		}catch(Exception e){
			
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

	 private class SendMessageTask2 extends AsyncTask<String, Void, String> {
			
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
}
