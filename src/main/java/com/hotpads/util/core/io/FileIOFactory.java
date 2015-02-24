package com.hotpads.util.core.io;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.hotpads.util.core.FileUtils;
import com.hotpads.util.core.RuntimeTool;

public class FileIOFactory {

	public static FileOutputStream makeFileOutputStream(
			String fileLocation, boolean create, boolean append) 
	throws FileNotFoundException{
		File f = new File((RuntimeTool.isWindows()?"c:":"")+fileLocation);
		if(create) FileUtils.createFileParents(f);
		return new FileOutputStream(f,append);
	}
	
}
