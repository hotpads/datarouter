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
package io.datarouter.storage.node.op.raw;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.datarouter.bytes.BatchingByteArrayScanner;
import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Codec;
import io.datarouter.bytes.PrependLengthByteArrayScanner;
import io.datarouter.bytes.VarIntTool;
import io.datarouter.model.databean.EmptyDatabean;
import io.datarouter.model.databean.EmptyDatabean.EmptyDatabeanFielder;
import io.datarouter.model.key.EmptyDatabeanKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.exception.DataTooLargeException;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.queue.BlobQueueMessage;

public interface BlobQueueStorage<T>{

	public static final String OP_getMaxDataSize = "getMaxDataSize";
	public static final String OP_put = "put";
	public static final String OP_peek = "peek";
	public static final String OP_ack = "ack";
	public static final String OP_poll = "poll";

	int getMaxRawDataSize();

	default boolean willFit(T data){
		return getCodec().encode(data).length < getMaxRawDataSize();
	}

	Codec<T,byte[]> getCodec();

	void putRaw(byte[] data, Config config);

	default void putRaw(byte[] data){
		putRaw(data, new Config());
	}

	default void put(T data, Config config){
		combineAndPut(Scanner.of(data), config);
	}

	default void put(T data){
		put(data, new Config());
	}

	default void putMulti(Collection<T> data, Config config){
		combineAndPut(Scanner.of(data), config);
	}

	default void putMulti(Collection<T> data){
		putMulti(data, new Config());
	}

	default void putMulti(Scanner<T> data, Config config){
		combineAndPut(data, config);
	}

	default void putMulti(Scanner<T> data){
		putMulti(data, new Config());
	}

	Optional<BlobQueueMessage<T>> peek(Config config);

	default Optional<BlobQueueMessage<T>> peek(){
		return peek(new Config());
	}

	void ack(byte[] handle, Config config);

	default void ack(byte[] handle){
		ack(handle, new Config());
	}

	default void ack(BlobQueueMessage<T> blobQueueMessage, Config config){
		ack(blobQueueMessage.getHandle(), config);
	}

	default void ack(BlobQueueMessage<T> blobQueueMessage){
		ack(blobQueueMessage, new Config());
	}

	default Optional<BlobQueueMessage<T>> poll(Config config){
		var optionalMessage = peek(config);
		optionalMessage.ifPresent(message -> ack(message, config));
		return optionalMessage;
	}

	default Optional<BlobQueueMessage<T>> poll(){
		return poll(new Config());
	}

	/**
	 * see {@link BlobQueueStorage#combineAndPut(Scanner, Config)}
	 */
	default void combineAndPut(Scanner<T> data){
		combineAndPut(data, new Config());
	}

	/**
	 * convenience method to automatically batch and put data with its length if each byte[] plus its length fits.
	 * @param data each byte[] must be smaller than {@link BlobQueueStorage#getMaxRawDataSize()} -
	 * {@link VarIntTool#encode(long)}d length
	 */
	default void combineAndPut(Scanner<T> data, Config config){
		data
				.map(getCodec()::encode)
				.each(bytes -> {
					if(bytes.length > getMaxRawDataSize() - VarIntTool.encode(bytes.length).length){
						throw new DataTooLargeException("BlobQueueStorage", List.of("a blob of size " + bytes.length));
					}
				})
				.apply(PrependLengthByteArrayScanner::of)
				.apply(scanner -> new BatchingByteArrayScanner(scanner, getMaxRawDataSize()))
				.map(ByteTool::concat)
				.forEach(bytes -> putRaw(bytes, config));
	}

	interface BlobQueueStorageNode<T>
	extends BlobQueueStorage<T>, Node<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder>{
	}

	interface PhysicalBlobQueueStorageNode<T>
	extends BlobQueueStorageNode<T>, PhysicalNode<EmptyDatabeanKey,EmptyDatabean,EmptyDatabeanFielder>{
	}

}
