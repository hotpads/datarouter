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
package io.datarouter.web.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.util.collection.SetTool;
import io.datarouter.util.io.FileTool;
import io.datarouter.util.iterable.IterableTool;

public abstract class BaseFilesTests{
	private static final Logger logger = LoggerFactory.getLogger(BaseFilesTests.class);

	private static final List<String> OMITTED_WORDS = Arrays.asList(
			"src/main/webapp/META-INF",
			"web.xml");

	protected abstract FilesRoot getNode();

	protected abstract String getRootDirectory();

	@Test
	public void testPathNodesFilesExist(){
		getNodeFiles().stream()
				.peek(file -> logger.info("{}", file))
				.forEach(FileTool::requireExists);
	}

	@Test
	public void testSystemFilesExistAsPathNodes(){
		Set<String> nodeFileStrings = IterableTool.mapToSet(getNodeFiles(), File::toString);
		for(String file : getDirectoryFiles()){
			if(!filterOmited(file)){
				continue;
			}
			String message = "add file to PathNodes - " + file;
			Assert.assertTrue(nodeFileStrings.contains(file), message);
		}
	}

	private List<String> getDirectoryFiles(){
		List<String> directoryFiles = new ArrayList<>();
		try{
			Files.walk(Paths.get(getRootDirectory()))
					.filter(Files::isRegularFile)
					.map(Path::toString)
					.forEach(directoryFiles::add);
		}catch(IOException e){
			logger.warn("{}", e.getMessage());
		}
		return directoryFiles;
	}

	private List<File> getNodeFiles(){
		return getNode().paths().stream()
				.filter(PathNode::isLeaf)
				.map(PathNode::toSlashedString)
				.filter(this::filterOmited)
				.map(getRootDirectory()::concat)
				.map(Paths::get)
				.map(Path::toFile)
				.collect(Collectors.toList());
	}

	private Set<String> getFilesToOmit(){
		return SetTool.union(getNode().filesToOmit(), OMITTED_WORDS);
	}

	private boolean filterOmited(String path){
		return !getFilesToOmit().stream().anyMatch(path::contains);
	}

}
