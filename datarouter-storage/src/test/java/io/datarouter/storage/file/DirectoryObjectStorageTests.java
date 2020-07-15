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

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.storage.file.DirectoryManager.DirectoryManagerFactory;

@Guice
public class DirectoryObjectStorageTests{
	private static final Logger logger = LoggerFactory.getLogger(DirectoryObjectStorageTests.class);

	@Inject
	private DirectoryManagerFactory directoryManagerFactory;

	private DirectoryManager testDirectory;
	private DirectoryObjectStorage objectStorage;

	@BeforeClass
	public void beforeClass(){
		testDirectory = directoryManagerFactory.create(".");// datarouter-storage root
		objectStorage = new DirectoryObjectStorage(testDirectory);
	}

	@Test
	public void testPathbeanKeyValidation(){
		Assert.assertTrue(PathbeanKey.isValidPath(""));
		Assert.assertTrue(PathbeanKey.isValidPath("a/"));
		Assert.assertTrue(PathbeanKey.isValidPath("a/a/"));
		Assert.assertFalse(PathbeanKey.isValidPath("a//"));
		Assert.assertFalse(PathbeanKey.isValidFile(""));
		Assert.assertFalse(PathbeanKey.isValidFile("/"));
		Assert.assertTrue(PathbeanKey.isValidFile("a"));
		Assert.assertTrue(PathbeanKey.isValidFile("a.txt"));
		Assert.assertTrue(PathbeanKey.isValidFile(".asdf"));
		Assert.assertTrue(PathbeanKey.isValidFile("."));//should it be invalid?
	}

	@Test
	public void testExists(){
		Assert.assertTrue(objectStorage.exists(new PathbeanKey("", "pom.xml")));
		Assert.assertFalse(objectStorage.exists(new PathbeanKey("", "badpom.xml")));
		//TODO reject relative paths
//		Assert.assertFalse(objectStorage.exists(new PathbeanKey("./", "pom.xml")));
	}

	@Test
	public void testScanKeys(){
		objectStorage.scanKeys()
				.each(pathbeanKey -> Assert.assertFalse(pathbeanKey.getFile().isEmpty()))
				.map(PathbeanKey::getPathAndFile)
				.forEach(logger::info);
	}

	@Test
	public void testScan(){
		objectStorage.scan()
				.each(pathbean -> Assert.assertFalse(pathbean.getKey().getFile().isEmpty()))
				.map(pathbean -> String.format("%s[%s]", pathbean.getKey().getPathAndFile(), pathbean.getSize()))
				.forEach(logger::info);
	}

}
