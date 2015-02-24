package com.hotpads.util.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;


public class PropertiesTool {

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

	public static List<Properties> fromClasspath(final Iterable<String> resourcePaths) {
		List<Properties> multiProperties = new ArrayList<>();
		for (String resourcePath : resourcePaths) {
			try {
				Properties properties = fromClasspath(resourcePath);
				if (null != properties) {
					multiProperties.add(properties);
				}
			} catch (IOException exception) {
				continue;
			}
		}

		return multiProperties;
	}

	public static Properties fromClasspath(final String resourcePath) throws FileNotFoundException, IOException {
		ClassLoader classLoader = PropertiesTool.class.getClassLoader();
		try {
			InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
			Properties properties = new Properties();
			properties.load(inputStream);

			return properties;
		} catch (IOException exception) {
			// Not an issue yet, will try again by reading from a file location
		}

		return fromFile(resourcePath);
	}

	public static List<Properties> fromFiles(Iterable<String> paths){
		List<Properties> multiProperties = ListTool.createArrayList();
		for(String path : IterableTool.nullSafe(paths)) {
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
				in = PropertiesTool.class.getResourceAsStream(pathToFile);
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

	public static boolean isEmpty(Properties p){
		if(p==null){ return true; }
		return p.isEmpty();
	}

	public static SortedMap<String,String> getAsSortedMap(Properties p){
		if(isEmpty(p)){ return null; }
		SortedMap<String,String> map = MapTool.createTreeMap();
		for(Object key : CollectionTool.nullSafe(p.keySet())){
			map.put(key.toString(), p.get(key).toString());
		}
		return map;
	}

	public static String lineByLineString(Properties p){
		if(isEmpty(p)){ return null; }
		SortedMap<String,String> map = getAsSortedMap(p);
		StringBuilder sb = new StringBuilder();
		boolean didFirst = false;
		for(String key : map.keySet()){
			if(didFirst){ sb.append("\n"); }
			sb.append(key+"="+map.get(key));
			didFirst=true;
		}
		return sb.toString();
	}

	public static String getFirstOccurrence(Iterable<Properties> multiProperties, String key) {
		for(Properties properties : IterableTool.nullSafe(multiProperties)) {
			if(properties.containsKey(key)) {
				return properties.getProperty(key);
			}
		}
		return null;
	}
}
