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

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.filesystem.raw.DirectoryManager.DirectoryManagerFactory;
import io.datarouter.storage.config.DatarouterFilesystemPaths;
import io.datarouter.storage.util.Subpath;

@Guice
public class BinarySmallFileServiceIntegrationTests{

	private static final String
			DIR = DatarouterFilesystemPaths.BASE_DATA_DIRECTORY + new Subpath("test", "filesystem",
					BinarySmallFileServiceIntegrationTests.class.getSimpleName());

	@Inject
	private DirectoryManagerFactory directoryManagerFactory;
	private DirectoryManager testDirectory;

	@BeforeClass
	public void beforeClass(){
		testDirectory = directoryManagerFactory.create(DIR).deleteDescendants(Subpath.empty());
	}

	@AfterClass
	public void afterClass(){
		testDirectory.selfDestruct();
	}

	@Test
	public void test(){
		String filename = "testfile";
		String content = "hello";
		testDirectory.write(filename, content.getBytes());
		String actualFullFile = new String(testDirectory.read(filename));
		Assert.assertEquals(actualFullFile, content);
		String actualPartialFile = new String(testDirectory.read(filename, 2, 2));
		Assert.assertEquals(actualPartialFile, "ll");
		testDirectory.delete(filename);
	}

}
