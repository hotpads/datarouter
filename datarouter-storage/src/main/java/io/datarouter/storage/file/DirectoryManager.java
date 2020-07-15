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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.small.Utf8SmallFileService;

public class DirectoryManager{
	private static final Logger logger = LoggerFactory.getLogger(DirectoryManager.class);

	@Singleton
	public static class DirectoryManagerFactory{

		@Inject
		private PathService pathService;
		@Inject
		private Utf8SmallFileService utf8SmallFileService;

		public DirectoryManager create(String rootPathString){
			return new DirectoryManager(pathService, utf8SmallFileService, rootPathString);
		}

	}

	private final PathService pathService;
	private final Utf8SmallFileService utf8SmallFileService;
	private final Path root;

	public DirectoryManager(PathService pathService, Utf8SmallFileService utf8SmallFileService, String rootPathString){
		this.pathService = pathService;
		this.utf8SmallFileService = utf8SmallFileService;
		this.root = Paths.get(rootPathString);
		boolean createdAnyParents = root.toFile().mkdirs();
		if(createdAnyParents){
			logger.warn("created missing directories for {}", root);
		}
	}

	/*---------- static ------------*/

	private Path resolve(Path relativePath){
		//TODO reject relative path segments
		return root.resolve(relativePath);
	}

	private Path resolveString(String relativePathString){
		return resolve(Paths.get(relativePathString));
	}

	private Path afterRoot(Path path){
		return path.subpath(root.getNameCount(), path.getNameCount());
	}

	/*---------- paths ------------*/

	public boolean exists(String relativePathString){
		return Files.exists(resolveString(relativePathString));
	}

	public DirectoryManager createSubdirectory(String name){
		return new DirectoryManager(pathService, utf8SmallFileService, root.resolve(name).toString());
	}

	public Scanner<Path> scanChildren(Set<String> excluding, int limit, boolean sorted){
		return pathService.scanChildren(root, excluding, limit, sorted)
				.map(this::afterRoot);
	}

	public Scanner<Path> scanDescendants(boolean includeDirectories, boolean sorted){
		return pathService.scanDescendants(root, includeDirectories, sorted)
				.map(this::afterRoot);
	}

	public Long size(String relativePathString){
		return pathService.size(resolveString(relativePathString));
	}

	/*------------ read/write -----------*/

	public DirectoryManager writeUtf8(String relativePathString, String contents){
		utf8SmallFileService.writeUtf8(resolveString(relativePathString), contents);
		return this;
	}

	public String readUtf8(String relativePathString){
		return utf8SmallFileService.readUtf8(resolveString(relativePathString));
	}

	/*------------ delete -------------*/

	public DirectoryManager delete(String relativePathString){
		pathService.delete(resolveString(relativePathString));
		return this;
	}

	public DirectoryManager deleteDescendants(){
		//Delete files
		scanDescendants(false, false)
				.map(this::resolve)
				.forEach(pathService::delete);
		//Delete directories
		scanDescendants(true, true)
				.sorted(Comparator.reverseOrder())
				.map(this::resolve)
				.forEach(pathService::delete);
		return this;
	}

	/**
	 * This DirectoryManager object will become unusable and need to be recreated
	 */
	public void deleteAll(){
		deleteDescendants();
		pathService.delete(root);
	}

}
