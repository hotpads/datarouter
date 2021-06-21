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
package io.datarouter.filesystem.raw;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.scanner.RetainingGroup;
import io.datarouter.scanner.Scanner;

@Guice
public class PathServiceIntegrationTests{
	private static final Logger logger = LoggerFactory.getLogger(PathServiceIntegrationTests.class);

	@Inject
	private PathService pathService;

	@Test
	public void testScanDescendents(){
		Path datarouterPath = Paths.get("../datarouter-filesystem/src/");
		pathService.scanDescendantsPaged(datarouterPath, true, true)
				.each(page -> logger.info("{}", pathsToString(page)))
				.concat(Scanner::of)
				.map(PathService::pathToString)
				.retain(1)
				.forEach(this::assertSorted);
	}

	private void assertSorted(RetainingGroup<String> retained){
		if(retained.previous() == null){
			return;
		}
		int diff = retained.previous().compareTo(retained.current());
		if(diff >= 0){
			String message = String.format("%s arrived after %s", retained.current(), retained.previous());
			throw new RuntimeException(message);
		}

	}

	public static String pathsToString(List<Path> paths){
		return Scanner.of(paths)
				.map(path -> Files.isDirectory(path)
						? path.toString() + "/"
						: path.getFileName().toString())
				.collect(Collectors.joining(", "));
	}

}
