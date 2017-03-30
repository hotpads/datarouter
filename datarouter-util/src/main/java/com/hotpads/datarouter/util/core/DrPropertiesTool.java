package com.hotpads.datarouter.util.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class DrPropertiesTool{

	public static Properties parse(String path){
		Properties properties = new Properties();
		try{
			properties = fromFile(path);
		}catch(NullPointerException npe){
			throw new RuntimeException("NPE in file " + path, npe);
		}catch(IOException ioe){
			throw new RuntimeException("error reading file " + path, ioe);
		}
		return properties;
	}

	public static List<Properties> fromFiles(Iterable<String> paths){
		List<Properties> multiProperties = new ArrayList<>();
		for(String path : DrIterableTool.nullSafe(paths)){
			Properties properties = parse(path);
			if(properties != null){
				multiProperties.add(properties);
			}
		}
		return multiProperties;
	}

	private static Properties fromFile(String pathToFile) throws IOException{
		Objects.requireNonNull(pathToFile);
		Properties properties = new Properties();
		InputStream in = null;
		try{
			File file = new File(pathToFile);
			if(file.exists()){
				in = new FileInputStream(file);
			}else{ // Try the classpath instead
				in = DrPropertiesTool.class.getResourceAsStream(pathToFile);
			}
			properties.load(in);
		}finally{
			if(in != null){
				in.close();
			}
		}
		return properties;
	}

	public static String getFirstOccurrence(Iterable<Properties> multiProperties, String key){
		for(Properties properties : DrIterableTool.nullSafe(multiProperties)){
			if(properties.containsKey(key)){
				return properties.getProperty(key);
			}
		}
		return null;
	}
}
