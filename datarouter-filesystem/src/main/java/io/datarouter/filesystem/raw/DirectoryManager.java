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
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.filesystem.raw.small.BinaryFileService;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.util.Subpath;
import io.datarouter.util.timer.PhaseTimer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public class DirectoryManager{
	private static final Logger logger = LoggerFactory.getLogger(DirectoryManager.class);

	private static final boolean ALLOW_LOGGING = false;

	@Singleton
	public static class DirectoryManagerFactory{

		@Inject
		private PathService pathService;
		@Inject
		private BinaryFileService binaryFileService;

		public DirectoryManager create(String rootPathString){
			return new DirectoryManager(pathService, binaryFileService, rootPathString);
		}

	}

	private final PathService pathService;
	private final BinaryFileService binaryFileService;
	private final Path root;

	public DirectoryManager(
			PathService pathService,
			BinaryFileService binaryFileService,
			String rootPathString){
		this.pathService = pathService;
		this.binaryFileService = binaryFileService;
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

	private Path resolveSubpath(Subpath subpath){
		return resolveString(subpath.toString());
	}

	private Path resolveString(String relativePathString){
		return resolve(Paths.get(relativePathString));
	}

	private Path afterRoot(Path path){
		return path.subpath(root.getNameCount(), path.getNameCount());
	}

	private Subpath afterRootSubpath(Path path){
		return Scanner.of(afterRoot(path))
				.map(Path::toString)
				.listTo(Subpath::new);
	}

	/*---------- paths ------------*/

	public boolean exists(String relativePathString){
		return Files.exists(resolveString(relativePathString));
	}

	public Optional<Long> length(String relativePathString){
		try{
			return Optional.of(Files.size(resolveString(relativePathString)));
		}catch(NoSuchFileException e){
			return Optional.empty();
		}catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}

	public DirectoryManager createSubdirectory(Subpath subpath){
		return new DirectoryManager(
				pathService,
				binaryFileService,
				resolveSubpath(subpath).toString());
	}

	public Scanner<Path> scanChildren(Subpath subpath, Set<String> excludingFilenames, int limit, boolean sorted){
		return Scanner.of(pathService.listChildren(resolveSubpath(subpath), excludingFilenames, limit, sorted))
				.map(this::afterRoot);
	}

	public Scanner<List<Path>> scanDescendantsPaged(Subpath subpath, boolean includeDirectories, boolean sorted){
		return pathService.scanDescendantsPaged(resolveSubpath(subpath), includeDirectories, sorted)
				.map(page -> Scanner.of(page)
						.map(this::afterRoot)
						.list());
	}

	public Long size(String relativePathString){
		return pathService.size(resolveString(relativePathString));
	}

	/*------------ write -----------*/

	public DirectoryManager write(String relativePathString, byte[] bytes){
		binaryFileService.writeBytes(resolveString(relativePathString), bytes);
		return this;
	}

	public DirectoryManager write(String relativePathString, InputStream inputStream){
		binaryFileService.writeBytes(resolveString(relativePathString), inputStream);
		return this;
	}

	/*------------ read -----------*/

	public Optional<byte[]> read(String relativePathString){
		if(ALLOW_LOGGING){
			var timer = new PhaseTimer(relativePathString);
			Optional<byte[]> optBytes = binaryFileService.readBytes(resolveString(relativePathString));
			optBytes.ifPresent(bytes -> {
				timer.add("read " + bytes.length);
				logger.warn("{}", timer);
			});
			return optBytes;
		}
		return binaryFileService.readBytes(resolveString(relativePathString));
	}

	public Optional<byte[]> read(String relativePathString, long offset, int length){
		return binaryFileService.readBytes(resolveString(relativePathString), offset, length);
	}

	public Optional<byte[]> readEnding(String relativePathString, int length){
		return binaryFileService.readEnding(resolveString(relativePathString), length);
	}

	public InputStream readInputStream(String relativePathString){
		return binaryFileService.readInputStream(resolveString(relativePathString));
	}

	/*------------ delete -------------*/

	public DirectoryManager delete(String relativePathString){
		pathService.delete(resolveString(relativePathString));
		return this;
	}

	public DirectoryManager deleteDescendants(Subpath subpath){
		scanChildren(subpath, Set.of(), Integer.MAX_VALUE, false)
				.map(this::resolve)
				.each(path -> {
					if(Files.isDirectory(path)){
						deleteDescendants(afterRootSubpath(path));
					}
				})
				.forEach(pathService::delete);
		return this;
	}

	/**
	 * This DirectoryManager object will become unusable and need to be recreated
	 */
	public void selfDestruct(){
		deleteDescendants(Subpath.empty());
		pathService.delete(root);
	}

	public Path getRoot(){
		return root;
	}

}
