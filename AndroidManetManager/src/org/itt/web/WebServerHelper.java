package org.itt.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

public class WebServerHelper {

	private final static String TAG = "WebServerHelper";

	//public SimpleWebServer server;
	public MyServer server;
	private File[] temp;

	public void sendStartWebServerCommand() throws IOException {
		ArrayList<File> list = new ArrayList<File>();
		list.add(Environment.getExternalStorageDirectory());
		server = new MyServer();
		//server = new SimpleWebServer(8080, list, true);
		Log.v(TAG, "sendStartAdhocCommand()");
	}

	public void sendStopWebServerCommand() throws IOException {
		server = null;
		Log.v(TAG, "sendStopAdhocCommand()");
	}
}
