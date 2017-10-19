/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.util.properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import io.datarouter.util.StreamTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.tuple.Pair;

public class PropertiesTool{

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
		return IterableTool.map(paths, PropertiesTool::parse);
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
		return PropertiesTool.class.getResource(pathToFile);
	}

	public static String getFirstOccurrence(Iterable<Properties> multiProperties, String key){
		return StreamTool.stream(multiProperties)
				.map(properties -> properties.getProperty(key))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

}
