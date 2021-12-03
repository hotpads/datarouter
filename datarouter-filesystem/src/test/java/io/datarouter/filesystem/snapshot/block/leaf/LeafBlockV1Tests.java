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
package io.datarouter.filesystem.snapshot.block.leaf;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotLeafRecord;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotLeafSearchResult;
import io.datarouter.filesystem.snapshot.writer.BlockQueue.FileIdsAndEndings;
import io.datarouter.util.tuple.Twin;

public class LeafBlockV1Tests{

	private static final List<Twin<String>> INPUTS = List.of(
			new Twin<>("b", "vb0"),//0
			new Twin<>("c", "vc0"),//1
			new Twin<>("c", "vc1"),//2
			new Twin<>("c", "vc2"),//3
			new Twin<>("c", "vc3"),//4
			new Twin<>("c", "vc4"),//5
			new Twin<>("c", "vc5"),//6
			new Twin<>("c", "vc6"),//7
			new Twin<>("c", "vc7"),//8
			new Twin<>("c", "vc8"),//9
			new Twin<>("c", "vc9"),//10
			new Twin<>("d", "vd0"));//11

	private static final LeafBlock BLOCK = makeBlock();

	private static LeafBlock makeBlock(){
		var encoder = new LeafBlockV1Encoder(32 * 1024);
		var keyId = new AtomicLong();
		var blockId = new AtomicInteger();//generate fake blockIds
		INPUTS.forEach(input -> encoder.add(
				blockId.get(),
				keyId.getAndIncrement(),
				new SnapshotEntry(
						input.getLeft().getBytes(),
						input.getRight().getBytes(),
						ByteTool.EMPTY_ARRAY_2),
				new int[]{blockId.getAndIncrement()},
				new int[]{9}));
		var fileIdsAndEndings = new FileIdsAndEndings[]{
				new FileIdsAndEndings(new int[]{0}, new int[]{0})
		};
		byte[] bytes = encoder.encode(fileIdsAndEndings).concat();
		return new LeafBlockV1(bytes);
	}

	@Test
	public void testNumRecords(){
		Assert.assertEquals(BLOCK.numRecords(), INPUTS.size());
		Assert.assertEquals(BLOCK.firstRecordId(), 0);
		Assert.assertEquals(BLOCK.firstValueBlockId(0), 0);
	}

	@Test
	public void testFindRecordId(){
		INPUTS.stream()
				.forEach(input -> Assert.assertTrue(BLOCK.findRecordId(input.getLeft().getBytes()).isPresent()));

		IntStream.range(0, INPUTS.size())
				.forEach(i -> {
					SnapshotLeafRecord actual = BLOCK.snapshotLeafRecord(i);
					Assert.assertEquals(INPUTS.get(i).getLeft(), new String(actual.key));
					Assert.assertEquals(INPUTS.get(i).getRight(), new String(actual.value));
				});
	}

	@Test
	public void testSearch(){
		Assert.assertEquals(insertionIndex("a"), -1);
		Assert.assertEquals(search("a").recordId(true), 0);
		Assert.assertEquals(search("a").recordId(false), 0);

		Assert.assertEquals(insertionIndex("b"), 0);
		Assert.assertEquals(search("b").recordId(true), 0);
		Assert.assertEquals(search("b").recordId(false), 1);

		Assert.assertEquals(insertionIndex("bb"), -2);
		Assert.assertEquals(search("bb").recordId(true), 1);
		Assert.assertEquals(search("bb").recordId(false), 1);

		Assert.assertEquals(insertionIndex("c"), 1);
		Assert.assertEquals(search("c").recordId(true), 1);
		//2 is because it doesn't yet support multiple entries with the same key.  With that support it would be 11.
		Assert.assertEquals(search("c").recordId(false), 2);

		Assert.assertEquals(insertionIndex("cc"), -12);
		Assert.assertEquals(search("cc").recordId(true), 11);
		Assert.assertEquals(search("cc").recordId(false), 11);

		Assert.assertEquals(insertionIndex("d"), 11);
		Assert.assertEquals(search("d").recordId(true), 11);
		Assert.assertEquals(search("d").recordId(false), 12);

		Assert.assertEquals(insertionIndex("dd"), -13);
		Assert.assertEquals(search("dd").recordId(true), 12);
		Assert.assertEquals(search("dd").recordId(false), 12);
	}

	private int insertionIndex(String searchKey){
		return BLOCK.insertionIndex(searchKey.getBytes());
	}

	private SnapshotLeafSearchResult search(String searchKey){
		return BLOCK.search(searchKey.getBytes());
	}

}
