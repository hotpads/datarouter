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
package io.datarouter.util.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.collection.CollectionTool;


public class FileToolTests{

	@Test
	public void testFindFiles() throws IOException{
		String thisJavaFile = "glob:**/" + FileToolTests.class.getSimpleName() + ".java";
		List<File> files = FileTool.findFiles(thisJavaFile);

		File file = CollectionTool.getFirst(files);
		Assert.assertNotNull(file);

		String content = FileTool.readFile(file);
		Assert.assertTrue(content.contains(FileToolTests.class.getSimpleName()));
		Assert.assertTrue(content.contains(FileToolTests.class.getPackage().getName()));
	}

}
