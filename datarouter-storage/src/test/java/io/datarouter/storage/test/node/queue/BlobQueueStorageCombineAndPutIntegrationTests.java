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
package io.datarouter.storage.test.node.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.PrependLengthByteArrayScanner;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.exception.DataTooLargeException;
import io.datarouter.storage.node.op.raw.BlobQueueStorage;
import io.datarouter.storage.queue.BlobQueueMessage;
import io.datarouter.storage.test.node.queue.BloqQueueStorageTestDto.BloqQueueStorageTestDtoCodec;

public class BlobQueueStorageCombineAndPutIntegrationTests{

	private static final Codec<byte[],byte[]> BYTE_ARRAY_CODEC = Codec.identity();
	private static final Codec<BloqQueueStorageTestDto,byte[]> DTO_CODEC = new BloqQueueStorageTestDtoCodec();

	@Test
	public void testDtoRaw(){
		byte[] data = makeRandomBytes();
		var msg = new BlobQueueMessage<>(null, data, null, BYTE_ARRAY_CODEC);
		Assert.assertEquals(StringCodec.UTF_8.decode(msg.getRawData()), StringCodec.UTF_8.decode(data));
		//cannot assert failure for getDataSplittingScanner, because failure is not guaranteed (but very likely).
	}

	@Test
	public void testDtoMulti(){
		List<byte[]> data = List.of(makeRandomBytes(), makeRandomBytes(), makeRandomBytes());
		var lengthPrependedData = Scanner.of(data)
				.apply(PrependLengthByteArrayScanner::of)
				.listTo(ByteTool::concat);
		var msg = new BlobQueueMessage<>(null, lengthPrependedData, null, BYTE_ARRAY_CODEC);
		assertEqualByteArrayLists(msg.scanSplitDecodedData().list(), data);
	}

	@Test
	public void testMultiEncoded(){

		var dtos = List.of(
				new BloqQueueStorageTestDto('c', 3, true),
				new BloqQueueStorageTestDto('a', 0, true),
				new BloqQueueStorageTestDto('a', 0, false),
				new BloqQueueStorageTestDto('b', 0, false));
		var raw = Scanner.of(dtos)
				.map(DTO_CODEC::encode)
				.apply(PrependLengthByteArrayScanner::of)
				.listTo(ByteTool::concat);

		//one DTO per message
		var singleDao = new MockBlobQueueStorage<>(BloqQueueStorageTestDtoCodec.length() + 1, DTO_CODEC);
		singleDao.combineAndPut(Scanner.of(dtos));
		Assert.assertEquals(singleDao.data.size(), 4);
		verifyRawAndDtos(singleDao, dtos, raw);

		//two DTOs per message
		var twoDao = new MockBlobQueueStorage<>((BloqQueueStorageTestDtoCodec.length() + 1) * 2, DTO_CODEC);
		twoDao.combineAndPut(Scanner.of(dtos));
		Assert.assertEquals(twoDao.data.size(), 2);
		verifyRawAndDtos(twoDao, dtos, raw);

		//all DTOs in one message
		var allDao = new MockBlobQueueStorage<>((BloqQueueStorageTestDtoCodec.length() + 1) * 4, DTO_CODEC);
		allDao.combineAndPut(Scanner.of(dtos));
		Assert.assertEquals(allDao.data.size(), 1);
		verifyRawAndDtos(allDao, dtos, raw);
	}

	private void verifyRawAndDtos(MockBlobQueueStorage<BloqQueueStorageTestDto> dao, List<BloqQueueStorageTestDto> dtos,
			byte[] raw){
		var resultMessages = Scanner.of(dao.data)
				.map(data -> new BlobQueueMessage<>(null, data, null, DTO_CODEC))
				.list();

		var rawBytesResult = Scanner.of(resultMessages)
				.map(BlobQueueMessage::getRawData)
				.listTo(ByteTool::concat);
		var roundTrippedDtos = Scanner.of(resultMessages)
				.concat(BlobQueueMessage::scanSplitDecodedData)
				.list();
		Assert.assertEquals(rawBytesResult, raw);
		Assert.assertEquals(roundTrippedDtos, dtos);
	}

	@Test
	public void testDaoMultiDefaultMethodsBig(){
		List<byte[]> data = List.of(makeRandomBytes(), makeRandomBytes(), makeRandomBytes());
		var storage = getByteArrayStorage(Integer.MAX_VALUE);
		storage.combineAndPut(Scanner.of(data));
		storage.putRaw(Scanner.of(data).listTo(ByteTool::concat));
		var msgWithLength = new BlobQueueMessage<>(null, storage.data.get(0), null, BYTE_ARRAY_CODEC);
		assertEqualByteArrayLists(msgWithLength.scanSplitDecodedData().list(), data);
		var msgWithoutLength = new BlobQueueMessage<>(null, storage.data.get(1), null, BYTE_ARRAY_CODEC);
		assertEqualByteArrayLists(List.of(msgWithoutLength.getRawData()), List.of(ByteTool.concat(data)));
	}

	@Test
	public void testDaoMultiDefaultMethodsSmall(){
		List<byte[]> data = List.of(makeRandomBytes(), makeRandomBytes(), makeRandomBytes());

		var tooSmallWithLength = getByteArrayStorage(data.get(0).length);
		tooSmallWithLength.putRaw(data.get(0));//this works for raw
		assertEqualByteArrayLists(tooSmallWithLength.data, List.of(data.get(0)));
		//but not when length is added
		Assert.expectThrows(
				DataTooLargeException.class, () -> tooSmallWithLength.combineAndPut(Scanner.of(data)));

		var storage = getByteArrayStorage(data.get(0).length + 1);//item + length will just fit in a message
		storage.combineAndPut(Scanner.of(data));

		var withAndWithoutLengthLists = Scanner.of(storage.data)
				.map(messageData -> new BlobQueueMessage<>(null, messageData, null, BYTE_ARRAY_CODEC))
				.batch(data.size())
				.list();
		//test with length
		for(int i = 0; i < data.size(); i++){
			assertEqualByteArrayLists(
					withAndWithoutLengthLists.get(0).get(i).scanSplitDecodedData().list(),
					List.of(data.get(i)));
		}
	}

	//this is just to test the functionality of BlobQueueStorage's default combining methods
	private static class MockBlobQueueStorage<T> implements BlobQueueStorage<T>{

		private final int size;
		private final Codec<T,byte[]> codec;

		public List<byte[]> data = new ArrayList<>();

		public MockBlobQueueStorage(int size, Codec<T,byte[]> codec){
			this.size = size;
			this.codec = codec;
		}

		@Override
		public Codec<T,byte[]> getCodec(){
			return codec;
		}

		@Override
		public int getMaxRawDataSize(){
			return size;
		}

		@Override
		public void putRaw(byte[] data, Config config){
			this.data.add(data);
		}

		@Override
		public Optional<BlobQueueMessage<T>> peek(Config config){
			return null;
		}

		@Override
		public void ack(byte[] handle, Config config){
		}

	}

	private static MockBlobQueueStorage<byte[]> getByteArrayStorage(int size){
		return new MockBlobQueueStorage<>(size, BYTE_ARRAY_CODEC);
	}

	private static byte[] makeRandomBytes(){
		return StringCodec.UTF_8.encode(UUID.randomUUID().toString());
	}

	//byte[] comparison doesn't work
	private static void assertEqualByteArrayLists(List<byte[]> actual, List<byte[]> expected){
		Assert.assertEquals(toString(actual), toString(expected));
	}

	private static List<String> toString(List<byte[]> byteList){
		return Scanner.of(byteList)
				.map(StringCodec.UTF_8::decode)
				.list();
	}

}
