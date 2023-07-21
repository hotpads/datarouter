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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.filesystem.raw.DirectoryManager.DirectoryManagerFactory;
import io.datarouter.filesystem.raw.queue.DirectoryQueue;
import io.datarouter.filesystem.raw.queue.DirectoryQueueMessage;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.DatarouterFilesystemPaths;
import io.datarouter.storage.util.Subpath;
import jakarta.inject.Inject;

@Guice
public class DirectoryQueueIntegrationTests{

	private static final String
			DIR = DatarouterFilesystemPaths.BASE_DATA_DIRECTORY + new Subpath("test", "filesystem", "directoryQueue");

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
	public void testSingleFile(){
		var singleFileQueue = new DirectoryQueue(testDirectory.createSubdirectory(new Subpath("singleFile")));
		String content = "The time is " + Instant.now().toString();
		String id = singleFileQueue.putMessage(content);
		String content2 = singleFileQueue.getMessage(id).getContentUtf8();
		Assert.assertEquals(content2, content);
		singleFileQueue.ack(id);
		assertEmpty(singleFileQueue);
	}

	@Test
	public void testMultiFile(){
		var multiFileQueue = new DirectoryQueue(testDirectory.createSubdirectory(new Subpath("multiFile")));
		int numMessages = 1_000;
		List<Integer> inputContents = Scanner.iterate(0, i -> i + 1)
				.limit(numMessages)
				.list();
		List<String> ids = Scanner.of(inputContents)
				.map(i -> i + "")
				.map(multiFileQueue::putMessage)
				.list();
		Assert.assertEquals(ids.size(), numMessages);
		List<Integer> outputContents = Scanner.generate(multiFileQueue::peek)
				.advanceUntil(Optional::isEmpty)
				.map(Optional::get)
				.each(message -> multiFileQueue.ack(message.id))
				.map(DirectoryQueueMessage::getContentUtf8)
				.map(Integer::valueOf)
				.sort()
				.list();
		Assert.assertEquals(outputContents, inputContents);
		assertEmpty(multiFileQueue);
	}

	private void assertEmpty(DirectoryQueue queue){
		Assert.assertTrue(queue.peek().isEmpty());
		Assert.assertEquals(queue.estNumMessages(), 0);
		Assert.assertEquals(queue.estNumOpenMessages(), 0);
		Assert.assertEquals(queue.estNumWaitingMessages(), 0);
	}

}
