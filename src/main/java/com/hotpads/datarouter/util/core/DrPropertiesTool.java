package com.hotpads.datarouter.util.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;


public class DrPropertiesTool {

	public static Properties parse(String path){
		Properties properties = new Properties();
		try{
			properties = fromFile(path);
		}catch(FileNotFoundException fnfe){
			throw new RuntimeException(fnfe);
		}catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
		return properties;
	}

	public static List<Properties> fromFiles(Iterable<String> paths){
		List<Properties> multiProperties = DrListTool.createArrayList();
		for(String path : DrIterableTool.nullSafe(paths)) {
			Properties properties = parse(path);
			if(properties!=null) { multiProperties.add(properties); }
		}
		return multiProperties;
	}

	protected static Properties fromFile(String pathToFile)
	throws FileNotFoundException, IOException{
		if(pathToFile==null){ throw new FileNotFoundException(); }
		Properties properties = new Properties();
		InputStream in = null;
		try{
			File f = new File( pathToFile );
			if ( f.exists() ) {
				in = new FileInputStream( f );
			} else { // Try the classpath instead
				in = DrPropertiesTool.class.getResourceAsStream(pathToFile);
			}
			properties.load(in);
		}
		finally{
			if(in!= null){
				in.close();
			}
		}
		return properties;
	}

	public static String getFirstOccurrence(Iterable<Properties> multiProperties, String key) {
		for(Properties properties : DrIterableTool.nullSafe(multiProperties)) {
			if(properties.containsKey(key)) {
				return properties.getProperty(key);
			}
		}
		return null;
	}
}
