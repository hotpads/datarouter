package com.hotpads.datarouter.util.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import com.hotpads.util.core.stream.StreamTool;

public class DrPropertiesTool{

	public static Properties parse(String path){
		try{
			return fromFile(path);
		}catch(NullPointerException | IOException e){
			throw new RuntimeException("error reading file " + path, e);
		}
	}

	public static List<Properties> fromFiles(Iterable<String> paths){
		return StreamTool.map(paths, DrPropertiesTool::parse);
	}

	private static Properties fromFile(String pathToFile) throws IOException{
		Objects.requireNonNull(pathToFile);
		Properties properties = new Properties();
		try(InputStream in = getInputStream(pathToFile)){
			properties.load(in);
		}
		return properties;
	}

	private static InputStream getInputStream(String pathToFile) throws IOException{
		Path path = Paths.get(pathToFile);
		if(Files.exists(path)){
			return Files.newInputStream(path);
		}
		// Try the classpath instead
		return DrPropertiesTool.class.getResourceAsStream(pathToFile);
	}

	public static String getFirstOccurrence(Iterable<Properties> multiProperties, String key){
		return StreamTool.stream(multiProperties)
				.map(properties -> properties.getProperty(key))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

}
