/*
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
package io.datarouter.util.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.string.StringTool;

public final class FileTool{

	private static final List<String> STATIC_FILE_EXTENSIONS = Stream.of("ttf", "css", "js", "html", "pdf", "png",
			"jpg", "jpeg", "gif", "swf", "woff", "woff2", "map", "jsx")
			.map("."::concat)
			.toList();

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

	public static void requireExists(File file){
		if(!file.exists()){
			throw new IllegalArgumentException("expected file not found: " + file);
		}
	}

	public static void requireIsFileAndExists(File file){
		if(file.isFile() && !file.exists()){
			throw new IllegalArgumentException("expected file not found: " + file);
		}
	}

	public static void delete(String path){
		if(StringTool.isEmpty(path) || "/".equals(path)){
			throw new IllegalArgumentException("cannot delete empty or root path");
		}
		File file = new File(path);
		file.delete();
	}

	public static void deleteDirectory(File directory){
		deleteDirectoryContent(directory);
		directory.delete();
	}

	public static void deleteDirectoryContent(File directory){
		if(!directory.isDirectory()){
			throw new IllegalArgumentException(directory + " is not a directory");
		}
		Optional.ofNullable(directory.listFiles())
				.map(Scanner::of)
				.orElseGet(Scanner::empty)
				.forEach(file -> {
					if(file.isDirectory()){
						deleteDirectoryContent(file);
					}
					file.delete();
				});
	}

	public static boolean hasAStaticFileExtension(String path){
		return STATIC_FILE_EXTENSIONS.stream()
				.anyMatch(path::endsWith);
	}

	public static String readFile(File file){
		byte[] bytes = FilesTool.readAllBytes(file.toPath());
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public static void cacheRemoteFile(String remoteUrl, String localPath){
		Path path = Path.of(localPath);
		path.getParent().toFile().mkdirs();
		if(Files.exists(path)){
			return;
		}
		try{
			URL url = new URL(remoteUrl);
			InputStream inputStream = url.openStream();
			Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

}
