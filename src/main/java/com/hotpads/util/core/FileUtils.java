package com.hotpads.util.core;

import java.io.File;

public final class FileUtils{

	public static boolean createFileParents(String path){
		return createFileParents(new File(path));
	}

	public static boolean createFileParents(File aFile){
		if(aFile.exists()) return true;
		File parent = new File(aFile.getParent());
		if(parent.exists()) return true;
		try{
			parent.mkdirs();
		}catch(Exception e){
			return false;
		}
		return true;
	}

	public static void delete(String path){
		if(StringTool.isEmpty(path) || "/".equals(path)){ throw new IllegalArgumentException(
				"cannot delete empty or root path"); }
		File file = new File(path);
		file.delete();
	}
	
	public static boolean hasAStaticFileExtension(String path){
		if(path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".html") || path.endsWith(".pdf")
				|| path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".swf")){ return true; }
		return false;
	}

}