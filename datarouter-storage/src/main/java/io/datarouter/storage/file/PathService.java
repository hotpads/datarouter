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
package io.datarouter.storage.file;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.ObjectScanner;
import io.datarouter.scanner.Scanner;

@Singleton
public class PathService{

	@Inject
	private CheckedPathService checkedPathService;

	public static String pathToString(Path path){
		return Files.isDirectory(path)
				? path.toString() + "/"
				: path.toString();
	}

	public Scanner<Path> scanChildren(Path fullPath, Set<String> excluding, int limit, boolean sorted){
		try{
			return checkedPathService.scanChildren(fullPath, excluding, limit, sorted);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Recursively scan directories, optionally sorting them in String ordering, since the filesystem may return them
	 * in any order, which is often the order that subdirectories were added to a parent.
	 *
	 * @param fullPath  Path from the root of the filesystem or relative to the execution point.
	 * @param includeDirectories  False for results similar to an object store like S3 or GCS
	 * @param sorted  If true, sort each subdirectory before recursing, so that full paths match String sorting.
	 * @return
	 */
	public Scanner<Path> scanDescendants(Path fullPath, boolean includeDirectories, boolean sorted){
		return scanChildren(fullPath, Set.of(), Integer.MAX_VALUE, sorted)
				.concat(child -> {
					Scanner<Path> childScanner = ObjectScanner.of(child);//Path is Iterable
					if(Files.isDirectory(child)){
						return includeDirectories
								? Scanner.concat(childScanner, scanDescendants(child, includeDirectories, sorted))
								: scanDescendants(child, includeDirectories, sorted);
					}
					return childScanner;
				});
	}

	public Long size(Path path){
		try{
			return checkedPathService.size(path);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public void delete(Path fullPath){
		try{
			checkedPathService.delete(fullPath);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Singleton
	public static class CheckedPathService{

		public static final Comparator<Path> PATH_COMPARATOR = Comparator.comparing(PathService::pathToString);

		public Scanner<Path> scanChildren(Path fullPath, Set<String> excluding, int limit, boolean sorted)
		throws IOException{
			try(DirectoryStream<Path> iterable = Files.newDirectoryStream(fullPath)){
				Scanner<Path> childPaths = Scanner.of(iterable)
						.exclude(path -> excluding.contains(path.toString()));
				if(sorted){
					childPaths = childPaths.sorted(PATH_COMPARATOR);
				}
				//must collect the paths in the try block before the Scanner is closed
				List<Path> childPathsList = childPaths
						.limit(limit)
						.list();
				return Scanner.of(childPathsList);
			}
		}

		public Long size(Path path) throws IOException{
			return Files.size(path);
		}

		public void delete(Path fullPath)
		throws IOException{
			Files.delete(fullPath);
		}

	}

}
