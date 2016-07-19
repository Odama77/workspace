package org.itt.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class OutputFileFormat extends File {
	
	
	

	public OutputFileFormat(String path) {
		super(path);
		// TODO Auto-generated constructor stub
	}


	@Override
	public String toString(){
		String out = null;
		try {
			out = this.getCanonicalPath();
			if(this.isDirectory())
				out +='/';
			
			return this.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}
	
	
}
