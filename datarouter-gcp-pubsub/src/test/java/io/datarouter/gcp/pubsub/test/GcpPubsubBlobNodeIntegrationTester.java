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
package io.datarouter.gcp.pubsub.test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.bytes.Codec;
import io.datarouter.bytes.VarIntByteArraysTool;
import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.client.gcp.pubsub.GcpPubsubDataTooLargeException;
import io.datarouter.client.gcp.pubsub.node.GcpPubsubBlobNode;
import io.datarouter.gcp.pubsub.DatarouterPubsubTestNgModuleFactory;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.test.node.queue.BaseBlobQueueStorageTestDao;
import io.datarouter.storage.test.node.queue.BloqQueueStorageTestDto;
import io.datarouter.storage.test.node.queue.BloqQueueStorageTestDto.BloqQueueStorageTestDtoCodec;

@Guice(moduleFactory = DatarouterPubsubTestNgModuleFactory.class)
@Test(singleThreaded = true)
public class GcpPubsubBlobNodeIntegrationTester{

	private static final Codec<BloqQueueStorageTestDto,byte[]> CODEC = new BloqQueueStorageTestDtoCodec();

	private final Datarouter datarouter;
	private final BaseBlobQueueStorageTestDao<BloqQueueStorageTestDto> dao;

	@Inject
	public GcpPubsubBlobNodeIntegrationTester(Datarouter datarouter, QueueNodeFactory queueNodeFactory){
		this.datarouter = datarouter;
		this.dao = new BaseBlobQueueStorageTestDao<>(
				datarouter,
				queueNodeFactory,
				GcpPubsubTestClientIds.GCP_PUBSUB,
				"GcpPubsubBlobTest",
				CODEC);
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	@BeforeMethod
	public void beforeMethod(){
		drainQueue();
	}

	private void drainQueue(){
		while(true){
			var retrieved = dao.poll();
			if(retrieved.isEmpty()){
				break;
			}
		}
	}

	@Test
	public void testSizeLimits(){
		int maxDataSize = dao.getMaxDataSize();
		Assert.assertTrue(maxDataSize < GcpPubsubBlobNode.MAX_TOPIC_PLUS_MESSAGE_SIZE);
		Assert.assertTrue(maxDataSize < GcpPubsubBlobNode.MAX_SERIALIZED_REQUEST_SIZE);

		testByteLimit(maxDataSize);
		//not enough room for topic
		Assert.assertThrows(GcpPubsubDataTooLargeException.class, () ->
				testByteLimit(GcpPubsubBlobNode.MAX_TOPIC_PLUS_MESSAGE_SIZE));
		Assert.assertThrows(GcpPubsubDataTooLargeException.class, () ->
				testByteLimit(GcpPubsubBlobNode.MAX_SERIALIZED_REQUEST_SIZE));
	}

	private void testByteLimit(int size){
		dao.putRaw(fillBytes('a', size));
	}

	@Test
	public void testPutAndPoll(){
		byte[] randomBytes = makeRandomBytes();
		dao.putRaw(randomBytes);
		var retrieved = dao.poll().get();
		Assert.assertEquals(retrieved.getRawData(), randomBytes);
		Assert.assertTrue(dao.poll().isEmpty());
	}

	@Test
	public void testPutAndPollWithType(){
		var dtos = List.of(
				new BloqQueueStorageTestDto('c', 3, true),
				new BloqQueueStorageTestDto('a', 0, true),
				new BloqQueueStorageTestDto('a', 0, false),
				new BloqQueueStorageTestDto('b', 0, false));
		var raw = Scanner.of(dtos)
				.map(CODEC::encode)
				.apply(VarIntByteArraysTool::encodeMulti);

		dao.combineAndPut(dtos);
		var result = dao.poll().get();
		Assert.assertEquals(result.getRawData(), raw);
		Assert.assertEquals(result.scanSplitDecodedData().list(), dtos);
		Assert.assertTrue(dao.poll().isEmpty());
	}

	@Test
	public void testInterruptPeek(){
		GcpPubsubTestHelper.testInterruptPeek(() -> {
			Assert.assertTrue(dao.peek().isEmpty());
			return null;
		});
	}

	private static byte[] fillBytes(char character, int length){
		byte[] bytes = new byte[length];
		Arrays.fill(bytes, StringCodec.UTF_8.encode("" + character)[0]);
		return bytes;
	}

	private static byte[] makeRandomBytes(){
		return StringCodec.UTF_8.encode(UUID.randomUUID().toString());
	}

}
