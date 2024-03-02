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
package io.datarouter.web.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.test.TestableService;
import io.datarouter.pathnode.FilesRoot;
import io.datarouter.pathnode.FilesRoot.NoOpFilesRoot;
import io.datarouter.pathnode.PathNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.Require;
import io.datarouter.util.io.FileTool;
import io.datarouter.util.lang.ClassTool;

public abstract class BaseFilesTests implements TestableService{
	private static final Logger logger = LoggerFactory.getLogger(BaseFilesTests.class);

	private static final List<String> OMITTED_WORDS = List.of(
			"src/main/webapp/META-INF",
			"web.xml");

	protected abstract FilesRoot getNode();

	protected abstract String getRootDirectory();

	public void testPathNodesFilesExist(){
		if(skipTests()){
			return;
		}
		getNodeFiles().stream()
				.peek(file -> logger.info("{}", file))
				.forEach(FileTool::requireIsFileAndExists);
	}

	public void testSystemFilesExistAsPathNodes(){
		if(skipTests()){
			return;
		}
		Set<String> nodeFileStrings = getNodeFiles().stream()
				.map(File::toString)
				.collect(Collectors.toSet());
		for(String file : getDirectoryFiles()){
			if(!filterOmitted(file)){
				continue;
			}
			String message = "add file to PathNodes - " + file;
			Require.isTrue(nodeFileStrings.contains(file), message);
		}
	}

	@Override
	public void testAll(){
		testPathNodesFilesExist();
		testSystemFilesExistAsPathNodes();
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
				.filter(this::filterOmitted)
				.map(getRootDirectory()::concat)
				.map(Paths::get)
				.map(Path::toFile)
				.toList();
	}

	private Set<String> getFilesToOmit(){
		return Scanner.concat(getNode().filesToOmit(), OMITTED_WORDS).collect(HashSet::new);
	}

	private boolean filterOmitted(String path){
		return getFilesToOmit().stream()
				.noneMatch(path::contains);
	}

	private boolean skipTests(){
		return ClassTool.sameClass(getNode().getClass(), NoOpFilesRoot.class);
	}

}
