package com.hotpads.datarouter.util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.hotpads.datarouter.util.core.DrFileUtils;
import com.hotpads.datarouter.util.core.DrRuntimeTool;

public class DrFileIoFactory{

	public static FileOutputStream makeFileOutputStream(
			String fileLocation, boolean create, boolean append)
	throws FileNotFoundException{
		File file = new File((DrRuntimeTool.isWindows() ? "c:" : "") + fileLocation);
		if(create){
			DrFileUtils.createFileParents(file);
		}
		return new FileOutputStream(file,append);
	}

}
