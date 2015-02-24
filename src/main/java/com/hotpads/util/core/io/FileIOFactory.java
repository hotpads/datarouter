package com.hotpads.util.core.io;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.hotpads.util.core.FileUtils;
import com.hotpads.util.core.RuntimeTool;

public class FileIOFactory {

	public static FileInputStream makeFileInputStream(String fileLocation) 
	throws FileNotFoundException{
		if(RuntimeTool.isWindows()){
			return new FileInputStream("c:" + fileLocation);
		}
		return new FileInputStream(fileLocation);
	}
	
	public static FileOutputStream makeFileOutputStream(String fileLocation) 
	throws FileNotFoundException {
		if(RuntimeTool.isWindows()){
			return new FileOutputStream("c:" + fileLocation);
		}
		return new FileOutputStream(fileLocation);
	}
	
	public static FileOutputStream makeFileOutputStream(
			String fileLocation, boolean create) 
	throws FileNotFoundException{
		File f = new File((RuntimeTool.isWindows()?"c:":"")+fileLocation);
		if(create) FileUtils.createFileParents(f);
		return new FileOutputStream(f);
	}
	
	public static FileOutputStream makeFileOutputStream(
			String fileLocation, boolean create, boolean append) 
	throws FileNotFoundException{
		File f = new File((RuntimeTool.isWindows()?"c:":"")+fileLocation);
		if(create) FileUtils.createFileParents(f);
		return new FileOutputStream(f,append);
	}
	
}
