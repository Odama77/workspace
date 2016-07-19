package org.itt.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.itt.web.NanoHTTPD.Response.Status;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.Environment;

public class MyServer extends NanoHTTPD {
    private final static int PORT = 8080;

    
    public MyServer() throws IOException {
        super(PORT);
        start();
        System.out.println( "\nRunning! Point your browser to http://localhost:8080/ \n" );
    }
    
    @Override
    public Response serve(String uri, Method method,
        Map<String, String> header, Map<String, String> parameters,
        Map<String, String> files) {
    String answer = "";
    String path="";
    FileInputStream fis = null;
    try {
    	System.out.println("AQUI MMG " + uri);
    	path=Environment.getExternalStorageDirectory()+uri;
        if(new File(path).isDirectory()){
        	//return NanoHTTPD.newFixedLengthResponse(Status.OK, , txt)
        	return newFixedLengthResponse(filesIn(new File(path)));
        }
        else{
        	fis = new FileInputStream(path);
        	return new NanoHTTPD.Response(Status.OK, "application/octet-stream", fis,new File(path).length());
        }
        
    } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
    	System.out.println("AQUI MMQ FAIL");
        e.printStackTrace();
    }
    return NanoHTTPD.newFixedLengthResponse("Fail"); 
  }
    
    public String filesIn(File path){
    	List<File> allFiles = Arrays.asList(path.listFiles());
    	String out="";
    	for(File file : allFiles){
    		String temp = file.getPath().toString();
    		if(file.isDirectory())
    			temp+="/";
    		out+=temp;
    		out+="\n";
    	}
    	out = out.replaceAll(Environment.getExternalStorageDirectory().toString(), "");
    	return out;
    	
    }
    
}















   
	