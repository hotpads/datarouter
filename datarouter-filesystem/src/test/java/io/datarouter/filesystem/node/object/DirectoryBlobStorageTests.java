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
package io.datarouter.filesystem.node.object;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.filesystem.raw.DirectoryManager;
import io.datarouter.filesystem.raw.DirectoryManager.DirectoryManagerFactory;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.util.Subpath;

@Guice
public class DirectoryBlobStorageTests{
	private static final Logger logger = LoggerFactory.getLogger(DirectoryBlobStorageTests.class);

	@Inject
	private DirectoryManagerFactory directoryManagerFactory;

	private DirectoryManager testDirectory;
	private DirectoryBlobStorage blobStorage;

	@BeforeClass
	public void beforeClass(){
		testDirectory = directoryManagerFactory.create(".");// datarouter-storage root
		blobStorage = new DirectoryBlobStorage(testDirectory);
	}

	@Test
	public void testPathbeanKeyValidation(){
		Assert.assertTrue(PathbeanKey.isValidPath(""));
		Assert.assertTrue(PathbeanKey.isValidPath("a/"));
		Assert.assertTrue(PathbeanKey.isValidPath("a/a/"));
		Assert.assertFalse(PathbeanKey.isValidPath("a//"));
		Assert.assertFalse(PathbeanKey.isValidFile("", false));
		Assert.assertTrue(PathbeanKey.isValidFile("", true));
		Assert.assertFalse(PathbeanKey.isValidFile("/", false));
		Assert.assertTrue(PathbeanKey.isValidFile("a", false));
		Assert.assertTrue(PathbeanKey.isValidFile("a.txt", false));
		Assert.assertTrue(PathbeanKey.isValidFile(".asdf", false));
		Assert.assertTrue(PathbeanKey.isValidFile(".", false));//should it be invalid?
	}

	@Test
	public void testExists(){
		Assert.assertTrue(blobStorage.exists(new PathbeanKey("", "pom.xml")));
		Assert.assertFalse(blobStorage.exists(new PathbeanKey("", "badpom.xml")));
		//TODO reject relative paths
//		Assert.assertFalse(objectStorage.exists(new PathbeanKey("./", "pom.xml")));
	}

	@Test
	public void testScanKeys(){
		blobStorage.scanKeysPaged(Subpath.empty())
				.concat(Scanner::of)
				.each(pathbeanKey -> Assert.assertFalse(pathbeanKey.getFile().isEmpty()))
				.map(PathbeanKey::getPathAndFile)
				.forEach(logger::info);
	}

	@Test
	public void testScan(){
		blobStorage.scanPaged(Subpath.empty())
				.concat(Scanner::of)
				.each(pathbean -> Assert.assertFalse(pathbean.getKey().getFile().isEmpty()))
				.map(pathbean -> String.format("%s[%s]", pathbean.getKey().getPathAndFile(), pathbean.getSize()))
				.forEach(logger::info);
	}

}
