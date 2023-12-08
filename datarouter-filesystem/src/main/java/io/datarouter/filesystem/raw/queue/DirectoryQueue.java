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
package io.datarouter.filesystem.raw.queue;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.filesystem.raw.DirectoryManager;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.util.Subpath;
import io.datarouter.types.Ulid;

public class DirectoryQueue{

	private static final String EXTENSION = ".txt";

	private final DirectoryManager directoryManager;
	private final Map<String,Instant> openFilenames;

	public DirectoryQueue(DirectoryManager directoryManager){
		this.directoryManager = directoryManager;
		this.openFilenames = new ConcurrentHashMap<>();
	}

	public String putMessage(byte[] message){
		String id = new Ulid().value();
		directoryManager.write(idToFilename(id), message);
		return id;
	}

	public String putMessage(String message){
		String id = new Ulid().value();
		byte[] messageBytes = StringCodec.UTF_8.encode(message);
		directoryManager.write(idToFilename(id), messageBytes);
		return id;
	}

	public DirectoryQueueMessage getMessage(String id){
		byte[] content = directoryManager.read(idToFilename(id)).orElse(null);//TODO return optional?
		return new DirectoryQueueMessage(id, content);
	}

	public synchronized Optional<DirectoryQueueMessage> peek(){
		timeoutOpenMessages();
		return directoryManager.scanChildren(Subpath.empty(), openFilenames.keySet(), 1, false)
				.map(Path::getFileName)
				.map(Path::toString)
				.each(filename -> openFilenames.put(filename, Instant.now()))
				.findFirst()
				.map(filename -> {
					byte[] content = directoryManager.read(filename).orElse(null);//TODO return optional?
					return new DirectoryQueueMessage(filenameToId(filename), content);
				});
	}

	public void ack(String id){
		String filename = idToFilename(id);
		directoryManager.delete(filename);
		openFilenames.remove(filename);
	}

	/*--------------- counts ----------------------*/

	public long estNumMessages(){
		return directoryManager.scanDescendantsPaged(Subpath.empty(), false, false)
				.concat(Scanner::of)
				.count();
	}

	public long estNumOpenMessages(){
		return openFilenames.size();
	}

	public long estNumWaitingMessages(){
		return directoryManager.scanDescendantsPaged(Subpath.empty(), false, false)
				.concat(Scanner::of)
				.map(Path::getFileName)
				.map(Path::toString)
				.map(DirectoryQueue::filenameToId)
				.exclude(openFilenames::containsKey)
				.count();
	}

	/*--------------- private ----------------------*/

	void timeoutOpenMessages(){
		Instant cutoff = Instant.now().minus(Duration.ofMinutes(10));
		List<String> tooOld = Scanner.of(openFilenames.entrySet())
				.include(entry -> entry.getValue().isBefore(cutoff))
				.map(Entry::getKey)
				.list();
		tooOld.forEach(openFilenames::remove);
	}

	private static String idToFilename(String id){
		return id + EXTENSION;
	}

	private static String filenameToId(String filename){
		return filename.substring(0, filename.length() - EXTENSION.length());
	}

}
