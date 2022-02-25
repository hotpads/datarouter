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

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteUnitType;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Count.Counts;
import io.datarouter.util.number.NumberFormatter;

@Guice
public class FilesystemScanTester{
	private static final Logger logger = LoggerFactory.getLogger(FilesystemScanTester.class);

	private static final Path ROOT = Path.of("/");
	private static final List<Path> EXCLUDED_TOP_LEVEL_DIRECTORIES = List.of(
			// https://www.linux.com/news/discover-possibilities-proc-directory/
			Path.of("/proc"));

	private static final long FAKE_FILE_SIZE_THRESHOLD = ByteUnitType.TiB.toBytes(10);
	private static final int LOG_EVERY_N = 10_000;
	private static final int DISPLAY_MAX_N_SIZES = 20;

	@Inject
	private PathService pathService;

	@Test
	public void testScanDescendents(){
		var counts = new Counts();
		var files = counts.add("files");
		var bytes = counts.add("bytes");
		Scanner.of(pathService.listChildren(ROOT, Set.of(), Integer.MAX_VALUE, true))
				.exclude(EXCLUDED_TOP_LEVEL_DIRECTORIES::contains)
				.concat(topLevelDirectory -> pathService.scanDescendantsPaged(topLevelDirectory, true, true)
						.concat(Scanner::of)
						.map(Path::toFile)
						.each(file -> {
							if(file.length() < FAKE_FILE_SIZE_THRESHOLD){
								files.increment();
								bytes.incrementBy(file.length());
							}else{
								logger.warn("excluding huge file {} {}", file.getAbsolutePath(), file.length());
							}
						}))
						.each(file -> {
							if(files.value() > 0 && files.value() % LOG_EVERY_N == 0){
								logger.warn("scanned files={} bytes={} through {}", files, bytes, file);
							}
						})
				.maxN(Comparator.comparing(File::length), DISPLAY_MAX_N_SIZES)
				.forEach(file -> logger.warn("size={} file={}",
						NumberFormatter.addCommas(file.length()),
						file.getAbsolutePath()));
		logger.warn("total {}", counts);
	}

}