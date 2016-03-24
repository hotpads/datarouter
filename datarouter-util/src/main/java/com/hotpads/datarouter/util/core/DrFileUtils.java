package com.hotpads.datarouter.util.core;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class DrFileUtils{

	private static final List<String> staticFilesExtensions = Arrays.asList("ttf", "css", "js", "html", "pdf", "png",
			"jpg", "jpeg", "swf", "woff", "woff2");

	public static boolean createFileParents(String path){
		return createFileParents(new File(path));
	}

	public static boolean createFileParents(File file){
		if(file.exists()){
			return true;
		}
		File parent = new File(file.getParent());
		if(parent.exists()){
			return true;
		}
		try{
			parent.mkdirs();
		}catch(Exception e){
			return false;
		}
		return true;
	}

	public static void delete(String path){
		if(DrStringTool.isEmpty(path) || "/".equals(path)){
			throw new IllegalArgumentException("cannot delete empty or root path");
		}
		File file = new File(path);
		file.delete();
	}

	public static boolean hasAStaticFileExtension(String path){
		for(String extension : staticFilesExtensions){
			if(path.endsWith(extension)){
				return true;
			}
		}
		return false;
	}

}