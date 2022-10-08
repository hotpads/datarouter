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
package io.datarouter.storage.queue;

import java.util.Map;

import io.datarouter.bytes.Codec;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.op.raw.BlobQueueStorage;

public class BlobQueueMessage<T> extends RawBlobQueueMessage{

	private final Codec<T,byte[]> codec;

	public BlobQueueMessage(
			byte[] handle,
			byte[] data,
			Map<String,String> messageAttributes,
			Codec<T,byte[]> codec){
		super(handle, data, messageAttributes);
		this.codec = codec;
	}

	public BlobQueueMessage(RawBlobQueueMessage rawMessage, Codec<T,byte[]> codec){
		super(rawMessage.getHandle(), rawMessage.getRawData(), rawMessage.getMessageAttributes());
		this.codec = codec;
	}

	/**
	 * scan and decode each T that was stored with {@link BlobQueueStorage#combineAndPut}
	 * @return scanner of each T
	 */
	public Scanner<T> scanSplitDecodedData(){
		return scanSplitData()
				.map(codec::decode);
	}

}