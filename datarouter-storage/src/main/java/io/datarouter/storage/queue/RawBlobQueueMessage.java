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

import io.datarouter.bytes.VarIntByteArraysTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.op.raw.BlobQueueStorage;

public class RawBlobQueueMessage{

	private byte[] handle;
	private byte[] data;
	private Map<String,String> messageAttributes;

	public RawBlobQueueMessage(byte[] handle, byte[] data, Map<String,String> messageAttributes){
		this.handle = handle;
		this.data = data;
		this.messageAttributes = messageAttributes;
	}

	public byte[] getHandle(){
		return handle;
	}

	/**
	 * get the entire data that was stored with {@link BlobQueueStorage#combineAndPut} or
	 * {@link BlobQueueStorage#putRaw}
	 * @return data
	 */
	public byte[] getRawData(){
		return data;
	}

	/**
	 * scan each byte[] that was stored using {@link BlobQueueStorage#combineAndPut}
	 * @return scanner of each byte[]
	 */
	public Scanner<byte[]> scanSplitData(){
		return VarIntByteArraysTool.decodeMulti(data);
	}

	public Map<String,String> getMessageAttributes(){
		return messageAttributes;
	}

}