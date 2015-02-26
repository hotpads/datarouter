package com.hotpads.datarouter.util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.hotpads.datarouter.util.core.FileUtils;
import com.hotpads.datarouter.util.core.RuntimeTool;

public class FileIOFactory {

	public static FileOutputStream makeFileOutputStream(
			String fileLocation, boolean create, boolean append) 
	throws FileNotFoundException{
		File f = new File((RuntimeTool.isWindows()?"c:":"")+fileLocation);
		if(create) FileUtils.createFileParents(f);
		return new FileOutputStream(f,append);
	}
	
}
