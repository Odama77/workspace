/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
package org.span.manager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.span.R;
import org.span.service.ManetObserver;
import org.span.service.core.ManetService.AdhocStateEnum;
import org.span.service.routing.Node;
import org.span.service.system.ManetConfig;
import org.span.service.system.Encryption;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

public class SendMessageActivity extends Activity implements OnItemSelectedListener, ManetObserver {
	
	public static String TAG = "SendMessageActivity";
	
	private static final String PROMPT = "Enter address ...";
	
	private ManetManagerApp app = null;
    
    public static Spinner spnDestination = null;
    private EditText etAddress = null;
    private EditText etMessage = null;
    private Button btnSend = null;
    private Button btnCancel = null;
    
    
    private String selection = null;
    
    //Amado Section
    public static ArrayList<String> messageList = MessageService.messageList; 
    public static ListView chatView = null;
    public static ArrayAdapter<String> messageAdapter = null;
    public static String address2 = null;
    BroadcastReceiver updateUIReciver;
    
    //End Amado Section
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);	
		
		MainActivity.onSendFlag = true;
		
		Log.v(TAG, "onCreate()");
		
        // init application
        app = (ManetManagerApp)getApplication();
		
		setContentView(R.layout.sendmessageview);
		
	    app = (ManetManagerApp)getApplication();
	    
	    //Amado Section
	    chatView = (ListView)findViewById(R.id.chat_id);
	    messageAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.item_view,messageList);
	    chatView.setAdapter(messageAdapter);
	    
	    String macAddress = MainActivity.getBluetoothMacAddress();
	    
	    if(macAddress != null)
	    	Log.v("Mac Address", macAddress);
	    else
	    	Log.v("Mac Address", "null");
	    
	    //End Amado Section
	    
	    etAddress = (EditText) findViewById(R.id.etAddress);
	    etMessage = (EditText) findViewById(R.id.etMessage);
	    
	    app.manet.registerObserver(this);
	    app.manet.sendPeersQuery();
	    
	    spnDestination = (Spinner) findViewById(R.id.spnDestination);
	    spnDestination.setOnItemSelectedListener(this);
		
	    btnSend = (Button) findViewById(R.id.btnSend);
	    btnSend.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
	  			String destination = (String) spnDestination.getSelectedItem();
	  			String msg = etMessage.getText().toString();
	  			String address = null;
	  			String error = null, errorMsg = "";
	  			if (selection.equals(PROMPT)) {
	  				address = etAddress.getText().toString();
  					// remove user id
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
	  			if (msg.isEmpty()) {
	  				error = "Message is empty.";
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
	  				AlertDialog.Builder builder = new AlertDialog.Builder(SendMessageActivity.this);
	  				builder.setTitle("Please Make Corrections")
	  					.setMessage(errorMsg.trim())
	  					.setCancelable(false)
	  					.setPositiveButton("OK", null);
	  				AlertDialog alert = builder.create();
	  				alert.show();
	  			}
	  			Log.v(TAG, msg);
	  			etMessage.setText("");
	  			messageAdapter.notifyDataSetChanged();
	  		}
		});
	    
	    btnCancel = (Button) findViewById(R.id.btnCancel);
	  	btnCancel.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
				finish();
	  		}
		});
	  	
//	  	Updating chat
	  	
	  	IntentFilter filter = new IntentFilter();

	  	 filter.addAction("com.hello.action"); 

	  	updateUIReciver = new BroadcastReceiver() {

	  	            @Override
	  	            public void onReceive(Context context, Intent intent) {
	  	                //UI update here
	  	            	messageAdapter.notifyDataSetChanged();
	  	            }
	  	        };
	  	 registerReceiver(updateUIReciver,filter);
	  	
//	  	XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    }
	
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy()");
		super.onDestroy();
		app.manet.unregisterObserver(this);
		unregisterReceiver(updateUIReciver);
		
		MainActivity.onSendFlag = false;
	}
	
	public static void open(Activity parentActivity) {
		Log.v(TAG, "open()");
		Intent it = new Intent("android.intent.action.SEND_MESSAGE_ACTION");
		parentActivity.startActivity(it);
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		Log.v(TAG, "onItemSelected()");
		selection = (String)spnDestination.getItemAtPosition(position);
		if (selection.equals(PROMPT)) {
			etAddress.setVisibility(EditText.VISIBLE);
			etAddress.setText(app.manetcfg.getIpNetwork());
			etAddress.setSelection(etAddress.getText().length()); // move cursor to end
			app.focusAndshowKeyboard(etAddress);
		} else {
			etAddress.setVisibility(EditText.GONE);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}

	private class SendMessageTask extends AsyncTask<String, Void, String> {
		
		 @Override
		 protected String doInBackground(String... params) {
			Log.v(TAG, "doInBackground()");
			String address = params[0]; 
			address2 = params[0];
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
							new DatagramPacket(buff, msgLen, InetAddress.getByName(address), MessageService.MESSAGE_PORT);
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
	public void onRoutingInfoUpdated(String info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPeersUpdated(HashSet<Node> peers) {
		// provide option to enter peer address
		Log.v(TAG, "onPeersUpdated()");
		Set<String> options = new TreeSet<String>();
//		options.add(app.manetcfg.getIpBroadcast() + " (Broadcast)");
		options.add("10.0.0.0" + " (Broadcast)");
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
		spnDestination.setAdapter(adapter);
	}
	
	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub
		
	}
}