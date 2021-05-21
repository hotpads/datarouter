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
package io.datarouter.filesystem.snapshot.writer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.filesystem.snapshot.compress.CompressedBlock;
import io.datarouter.filesystem.snapshot.compress.CompressedBlocks;
import io.datarouter.util.Require;
import io.datarouter.util.number.NumberFormatter;

public class BlockQueue{
	private static final Logger logger = LoggerFactory.getLogger(BlockQueue.class);

	private final String name;
	private final int targetFileBlocks;
	private final long targetFileBytes;
	private int fileId;
	private final List<CompressedBlock> blocks;
	private int head;
	private int tail;
	private int ending;
	private int numFileBlocks;
	private int numFileBytes;
	private final List<Integer> fileIds;
	private final List<Integer> endings;
	public volatile long numSingleEndingChecks;
	public volatile long numMultiEndingChecks;

	public BlockQueue(String name, long fileByteLimit, int fileBlockLimit){
		this.name = name;
		this.targetFileBlocks = fileBlockLimit;
		this.targetFileBytes = fileByteLimit;
		fileId = 0;
		blocks = new ArrayList<>();
		head = 0;
		tail = 0;
		ending = 0;
		fileIds = new ArrayList<>();
		endings = new ArrayList<>();
		numSingleEndingChecks = 0;
		numMultiEndingChecks = 0;
	}

	public synchronized List<SnapshotFile> submit(int blockId, CompressedBlock block){
		while(blocks.size() <= blockId){
			blocks.add(null);
		}
		blocks.set(blockId, block);
		return advanceHead();
	}

	public synchronized List<SnapshotFile> takeLastFiles(){
		if(head == tail){
			return List.of();
		}
		return List.of(takeFile(head));
	}

	private List<SnapshotFile> advanceHead(){
		List<SnapshotFile> files = new ArrayList<>();
		CompressedBlock block;
		while(head < blocks.size() && (block = blocks.get(head)) != null){
			++head;
			fileIds.add(fileId);
			ending += block.totalLength;
			endings.add(ending);
			++numFileBlocks;
			numFileBytes += block.totalLength;
			if(numFileBlocks >= targetFileBlocks || numFileBytes >= targetFileBytes){
				SnapshotFile snapshotFile = takeFile(head);
				++fileId;
				ending = 0;
				numFileBlocks = 0;
				numFileBytes = 0;
				files.add(snapshotFile);
			}
		}
		return files;
	}

	private SnapshotFile takeFile(int to){
		Require.isTrue(to <= head);
		int length = to - tail;
		List<CompressedBlock> fileBlocks = new ArrayList<>(length);
		for(int i = 0; i < length; ++i){
			CompressedBlock block = blocks.get(tail + i);
			fileBlocks.add(block);
			blocks.set(tail + i, null);
		}
		tail += length;
		var snapshotFile = new SnapshotFile(name, fileId, new CompressedBlocks(fileBlocks));
		logger.info("takeFile {}", snapshotFile.getFlushLog());
		return snapshotFile;
	}

	public synchronized int[] fileIds(int firstBlockId, int numBlocks){
		int[] foundFileIds = new int[numBlocks];
		for(int i = 0; i < numBlocks; ++i){
			foundFileIds[i] = fileIds.get(firstBlockId + i);
		}
		return foundFileIds;
	}

	public synchronized Integer ending(int blockId){
		++numSingleEndingChecks;
		if(blockId >= endings.size()){
			return null;
		}
		return endings.get(blockId);
	}

	public synchronized boolean isReady(int firstBlockId, int numBlocks){
		for(int blockId = firstBlockId + numBlocks - 1; blockId >= firstBlockId; --blockId){
			if(blockId >= head){
				return false;
			}
		}
		return true;
	}

	public synchronized FileIdsAndEndings fileIdsAndEndings(int firstBlockId, int numBlocks){
		int[] foundFileIds = new int[numBlocks];
		int[] foundEndings = new int[numBlocks];
		if(firstBlockId < 0){//expected for the first request
			foundFileIds[0] = 0;// dummy values
			foundEndings[0] = 0;
		}else{
			foundFileIds[0] = fileIds.get(firstBlockId);
			foundEndings[0] = endings.get(firstBlockId);
		}
		for(int i = 1; i < numBlocks; ++i){
			foundFileIds[i] = fileIds.get(firstBlockId + i);
			foundEndings[i] = endings.get(firstBlockId + i);
		}
		return new FileIdsAndEndings(foundFileIds, foundEndings);
	}

	public void assertEmpty(){
		Require.equals(tail, head);
		Require.equals(tail, blocks.size());
		Require.equals(tail, fileIds.size());
		Require.equals(tail, endings.size());
	}

	/*
	 * The first entry in each array is the last value from the previous block.
	 */
	public static class FileIdsAndEndings{

		public final int[] fileIds;
		public final int[] endings;

		public FileIdsAndEndings(int[] fileIds, int[] endings){
			this.fileIds = fileIds;
			this.endings = endings;
		}

	}

	public static class SnapshotFile{

		public final String type;
		public final int id;
		public final CompressedBlocks compressedBlocks;

		public SnapshotFile(String type, int id, CompressedBlocks blocks){
			this.type = type;
			this.id = id;
			this.compressedBlocks = blocks;
		}

		public byte[] concat(){
			return compressedBlocks.concat();
		}

		public String getFlushLog(){
			return String.format("type=%s, id=%s, numBlocks=%s, numBytes=%s",
					type,
					id,
					NumberFormatter.addCommas(compressedBlocks.count),
					NumberFormatter.addCommas(compressedBlocks.totalLength));
		}

	}

}
