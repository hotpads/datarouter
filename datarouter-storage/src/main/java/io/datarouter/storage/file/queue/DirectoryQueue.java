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
package io.datarouter.storage.file.queue;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.datarouter.storage.file.DirectoryManager;
import io.datarouter.util.UlidTool;

public class DirectoryQueue{

	private static final String EXTENSION = ".txt";

	private final DirectoryManager directoryManager;
	private final Map<String,Instant> openMessages;

	public DirectoryQueue(DirectoryManager directoryManager){
		this.directoryManager = directoryManager;
		this.openMessages = new ConcurrentHashMap<>();
	}

	public String putMessage(String message){
		String id = UlidTool.nextUlid();
		directoryManager.writeUtf8(idToFilename(id), message);
		return id;
	}

	public DirectoryQueueMessage getMessage(String id){
		String content = directoryManager.readUtf8(idToFilename(id));
		return new DirectoryQueueMessage(id, content);
	}

	public synchronized Optional<DirectoryQueueMessage> peek(){
		return directoryManager.scanChildren(openMessages.keySet(), 1, false)
				.map(Path::getFileName)
				.map(Path::toString)
				.map(DirectoryQueue::filenameToId)
				.exclude(openMessages::containsKey)
				.each(id -> openMessages.put(id, Instant.now()))
				.findFirst()
				.map(id -> {
					String content = directoryManager.readUtf8(idToFilename(id));
					return new DirectoryQueueMessage(id, content);
				});
	}

	public void ack(String id){
		directoryManager.delete(idToFilename(id));
		openMessages.remove(id);
	}

	/*--------------- counts ----------------------*/

	public long estNumMessages(){
		return directoryManager.scanDescendants(false, false).count();
	}

	public long estNumOpenMessages(){
		return openMessages.size();
	}

	public long estNumWaitingMessages(){
		return directoryManager.scanDescendants(false, false)
				.map(Path::getFileName)
				.map(Path::toString)
				.map(DirectoryQueue::filenameToId)
				.exclude(openMessages::containsKey)
				.count();
	}

	/*--------------- private ----------------------*/

	private static String idToFilename(String id){
		return id + EXTENSION;
	}

	private static String filenameToId(String filename){
		return filename.substring(0, filename.length() - EXTENSION.length());
	}

}
