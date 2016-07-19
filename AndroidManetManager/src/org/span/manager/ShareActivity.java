/**
 *  SPAN - Smart Phone Ad-Hoc Networking project
 *  Copyright (c) 2012 The MITRE Corporation.
 */
/**
 *  Portions of this code are copyright (c) 2009 Harald Mueller and Sofia Lemons.
 * 
 *  This program is free software; you can redistribute it and/or modify it under 
 *  the terms of the GNU General Public License as published by the Free Software 
 *  Foundation; either version 3 of the License, or (at your option) any later 
 *  version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along with 
 *  this program; if not, see <http://www.gnu.org/licenses/>. 
 *  Use this application at your own risk.
 */
package org.span.manager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.servalproject.SimpleWebServer;
import org.servalproject.system.WiFiRadio;
import org.span.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

public class ShareActivity extends Activity {
	
	private static final String TAG = "ShareActivity";
	public boolean ask=true;
	public static ArrayList<String> shareList = null; 
    public static ListView shareView = null;
    public static ArrayAdapter<String> shareAdapter = null;
    public static String currentDir="/";
    
	 private ProgressDialog pDialog;
	 public static final int progress_bar_type = 4;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Log.d(TAG, "onCreate()"); // DEBUG
        
        setContentView(R.layout.share);
        
        shareList = new ArrayList<String>();
        fetchFileNames(currentDir);
        
        
        shareView = (ListView)findViewById(R.id.shareList_id);
        shareAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.share_text,shareList);
        shareView.setAdapter(shareAdapter);
        shareView.setClickable(true);
        shareView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> arg0, View view, int position, long value) {
	        	Log.d(TAG, "Position " + position + " long " + value); 
	        	Object o = shareView.getItemAtPosition(position);
	        	System.out.println( "La data " + o.toString());
	        	
	        	
	        	String selected = shareList.get(position);
	        	
	        	if(selected.charAt(selected.length()-1)=='/'){	        	
		        	fetchFileNames(selected);	        	
		        	shareAdapter.notifyDataSetChanged();
		        }
	        	else{
	        		new DownloadFileFromURL().execute("http://192.168.1.162:8080"+selected);
	        	}
	        	
	        }
        });

    }
    @Override
    public void onBackPressed(){
    	if(currentDir.equals("/")){
    		this.finish();
    		return;
    	}
    	
    	
    	String parentDir;
    	int lastIndex= currentDir.length()-1;
    	if(currentDir.lastIndexOf("/")==lastIndex ){
    		currentDir = currentDir.substring(0,lastIndex-1);
    	}
    	parentDir=currentDir.substring(0, currentDir.lastIndexOf("/")+1);
    	fetchFileNames(parentDir);
    	shareAdapter.notifyDataSetChanged();
    }
    public void fetchFileNames(String query){
    
    	
    	HttpClient client = new DefaultHttpClient();
    	
    	HttpGet request = new HttpGet("http://192.168.1.162:8080"+query);
    	currentDir = query;
    	
    	HttpResponse response=null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String responseStr=null;
    	try {
    		
			responseStr = EntityUtils.toString(response.getEntity());
			String lines[] = responseStr.split("\\r?\\n");
			for(String line : lines){
				line = line.replaceFirst(currentDir, "");
				
			}
			shareList.clear();
			shareList.add(currentDir); 
			shareList.addAll(Arrays.asList(lines));
			
			ask=false;
		
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void onPause() {
    	Log.v(TAG, "onPause()");
    	super.onPause();
    }
    
	public static void open(Activity parentActivity) {
		Log.v(TAG, "open()");
		Intent it = new Intent("android.intent.action.SHARE_ACTION");
		parentActivity.startActivity(it);
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		Log.v(TAG, "onCreateDialog2()");		
		if (id == progress_bar_type){
			 pDialog = new ProgressDialog(this);
	            pDialog.setMessage("Downloading file. Please wait...");
	            pDialog.setIndeterminate(false);
	            pDialog.setMax(100);
	            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	            pDialog.setCancelable(true);
	            pDialog.show();
	            return pDialog;
		}
		return null;
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Log.d(TAG, "onCreateDialog()"); // DEBUG
		if (id == progress_bar_type){
			return onCreateDialog(id);
		} 
		return null;
	}
	/*
	 * Background Async Task to download file
    * */
   class DownloadFileFromURL extends AsyncTask<String, String, String> {

       /**
        * Before starting background thread Show Progress Bar Dialog
        * */
       @Override
       protected void onPreExecute() {
           super.onPreExecute();
           showDialog(progress_bar_type);
       }

       /**
        * Downloading file in background thread
        * */
       @Override
       protected String doInBackground(String... f_url) {
           int count;
           try {
               URL url = new URL(f_url[0]);
               URLConnection conection = url.openConnection();
               conection.connect();

               // this will be useful so that you can show a tipical 0-100%
               // progress bar
               int lenghtOfFile = conection.getContentLength();

               // download the file
               InputStream input = new BufferedInputStream(url.openStream(),
                       8192);

               // Output stream
               File directory = new File(Environment
                       .getExternalStorageDirectory().toString()
                       + "/Download/");
               directory.mkdirs();
               File outputFile = new File(directory, f_url[0].substring(f_url[0].lastIndexOf('/') + 1));
               OutputStream output = new FileOutputStream(outputFile);

               byte data[] = new byte[1024];

               long total = 0;

               while ((count = input.read(data)) != -1) {
                   total += count;
                   // publishing the progress....
                   // After this onProgressUpdate will be called
                   publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                   // writing data to file
                   output.write(data, 0, count);
               }

               // flushing output
               output.flush();

               // closing streams
               output.close();
               input.close();

           } catch (Exception e) {
               Log.e("Error: ", e.getMessage());
           }

           return null;
       }
       protected void onProgressUpdate(String... progress) {
           // setting progress percentage
           pDialog.setProgress(Integer.parseInt(progress[0]));
       }

       /**
        * After completing background task Dismiss the progress dialog
        * **/
       @Override
       protected void onPostExecute(String file_url) {
           // dismiss the dialog after the file was downloaded
           dismissDialog(progress_bar_type);

       }

   }
  	
}

