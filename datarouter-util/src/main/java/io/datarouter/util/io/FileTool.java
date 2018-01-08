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
package io.datarouter.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.string.StringTool;

public final class FileTool{

	private static final List<String> staticFilesExtensions = Stream.of("ttf", "css", "js", "html", "pdf", "png",
			"jpg", "jpeg", "swf", "woff", "woff2", "map")
			.map("."::concat)
			.collect(Collectors.toList());

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

	public static void delete(String path){
		if(StringTool.isEmpty(path) || "/".equals(path)){
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

	public static String readFile(File file) throws IOException{
		byte[] bytes = Files.readAllBytes(file.toPath());
		return StringByteTool.fromUtf8Bytes(bytes);
	}

	/**
	 * Find files matching the globOrRegex pattern.
	 * @param globOrRegex see FileSystems.getDefault().getPathMatcher(globOrRegex)
	 */
	public static List<File> findFiles(String globOrRegex, File startingDir) throws IOException{
		FileFinder fileFinder = new FileFinder(globOrRegex);
		Files.walkFileTree(startingDir.toPath(), fileFinder);
		return fileFinder.results;
	}

	public static List<File> findFiles(String globOrRegex) throws IOException{
		File pwd = new File(".").getCanonicalFile();
		return findFiles(globOrRegex, pwd);
	}

	private static class FileFinder extends SimpleFileVisitor<Path>{
		private final PathMatcher pathMatcher;
		private final List<File> results = new ArrayList<>();

		public FileFinder(String globOrRegex){
			this.pathMatcher = FileSystems.getDefault().getPathMatcher(globOrRegex);
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
			if(pathMatcher.matches(file)){
				results.add(file.toFile());
			}
			return FileVisitResult.CONTINUE;
		}
	}
}