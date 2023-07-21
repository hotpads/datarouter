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
package io.datarouter.filesystem.raw;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import io.datarouter.scanner.ObjectScanner;
import io.datarouter.scanner.Scanner;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class PathService{

	@Inject
	private CheckedPathService checkedPathService;

	public static String pathToString(Path path){
		return Files.isDirectory(path)
				? path.toString() + "/"
				: path.toString();
	}

	public List<Path> listChildren(
			Path fullPath,
			Set<String> excludingFilenames,
			int limit,
			boolean sorted){
		try{
			return checkedPathService.listChildren(fullPath, excludingFilenames, limit, sorted);
		}catch(NoSuchFileException e){
			return List.of();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Recursively scan directories, optionally sorting them in String ordering, since the filesystem may return them
	 * in any order, which is often the order that sub-directories were added to a parent.
	 *
	 * This allows us to mimic the "list" operation provided by S3 or GCS.
	 *
	 * @param fullPath  Path from the root of the filesystem or relative to the execution point.
	 * @param includeDirectories  False for results similar to an object store like S3 or GCS
	 * @param sorted  If true, sort each sub-directory before recursing, so that full paths match String sorting.
	 * @return Lists of Paths grouped by leaf directory, with the leaf directory as the first Path in each List.
	 * 		In the case of a directory with mixed sub-directories and files, group consecutive files into the same List.
	 * 		The number of Lists tries to approximate the number of filesystem operations, but it's probably
	 * 		over-counting by returning each directory as a singleton list.
	 */
	public Scanner<List<Path>> scanDescendantsPaged(
			Path fullPath,
			boolean includeDirectories,
			boolean sorted){
		return Scanner.of(listChildren(fullPath, Set.of(), Integer.MAX_VALUE, sorted))
				.splitBy(Files::isDirectory)
				.map(Scanner::list)
				.concat(directoriesOrFiles -> {
					boolean isDirectories = Files.isDirectory(directoriesOrFiles.get(0));
					if(isDirectories){
						List<Path> directories = directoriesOrFiles;
						if(includeDirectories){
							return Scanner.of(directories)
									.concat(directory -> ObjectScanner.of(List.of(directory))
											.append(scanDescendantsPaged(directory, includeDirectories, sorted)));
						}else{
							return Scanner.of(directories)
									.concat(directory -> scanDescendantsPaged(directory, includeDirectories, sorted));
						}
					}else{
						List<Path> files = directoriesOrFiles;
						return ObjectScanner.of(files);
					}
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

		public List<Path> listChildren(
				Path fullPath,
				Set<String> excludingFilenames,
				int limit,
				boolean sorted)
		throws IOException{
			try(DirectoryStream<Path> iterable = Files.newDirectoryStream(fullPath)){
				Scanner<Path> childPaths = Scanner.of(iterable)
						.include(Files::isReadable)
						.exclude(Files::isSymbolicLink)
						.exclude(path -> excludingFilenames.contains(path.getFileName().toString()));
				if(sorted){
					childPaths = childPaths.sort(PATH_COMPARATOR);
				}
				//must collect the paths in the try block before the DirectoryStream is closed
				return childPaths
						.limit(limit)
						.list();
			}
		}

		public Long size(Path path) throws IOException{
			return Files.size(path);
		}

		public void delete(Path fullPath)
		throws IOException{
			Files.deleteIfExists(fullPath);
		}

	}

}
