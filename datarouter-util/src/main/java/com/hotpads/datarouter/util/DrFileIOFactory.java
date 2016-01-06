package com.hotpads.datarouter.util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.hotpads.datarouter.util.core.DrFileUtils;
import com.hotpads.datarouter.util.core.DrRuntimeTool;

public class DrFileIOFactory {

	public static FileOutputStream makeFileOutputStream(
			String fileLocation, boolean create, boolean append) 
	throws FileNotFoundException{
		File f = new File((DrRuntimeTool.isWindows()?"c:":"")+fileLocation);
		if(create) DrFileUtils.createFileParents(f);
		return new FileOutputStream(f,append);
	}
	
}
