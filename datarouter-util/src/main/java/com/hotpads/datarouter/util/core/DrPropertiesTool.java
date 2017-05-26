package com.hotpads.datarouter.util.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.stream.StreamTool;

public class DrPropertiesTool{

	public static Properties parse(String path){
		return parseAndGetLocation(path).getLeft();
	}

	public static Pair<Properties,URL> parseAndGetLocation(String path){
		try{
			return fromFile(path);
		}catch(NullPointerException | IOException e){
			throw new RuntimeException("error reading file " + path, e);
		}
	}

	public static List<Properties> fromFiles(Iterable<String> paths){
		return StreamTool.map(paths, DrPropertiesTool::parse);
	}

	private static Pair<Properties,URL> fromFile(String pathToFile) throws IOException{
		Objects.requireNonNull(pathToFile);
		Properties properties = new Properties();
		URL url = getUrl(pathToFile);
		try(InputStream in = url.openStream()){
			properties.load(in);
		}
		return new Pair<>(properties, url);
	}

	private static URL getUrl(String pathToFile) throws IOException{
		Path path = Paths.get(pathToFile);
		if(Files.exists(path)){
			return path.toUri().toURL();
		}
		// Try the classpath instead
		return DrPropertiesTool.class.getResource(pathToFile);
	}

	public static String getFirstOccurrence(Iterable<Properties> multiProperties, String key){
		return StreamTool.stream(multiProperties)
				.map(properties -> properties.getProperty(key))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

}
