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
package io.datarouter.bytes;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.scanner.BaseLinkedScanner;
import io.datarouter.scanner.Scanner;

/**
 * scanner that batches byte[]s into {@literal List<byte[]>}s containing as many unbroken byte[]s as possible without
 * exceeding maxOutputBytes total bytes. throws an exception if any input is larger than maxOutputBytes.
 */
public class BatchingByteArrayScanner extends BaseLinkedScanner<byte[],List<byte[]>>{

	private final int maxOutputBytes;

	private byte[] storage;

	public BatchingByteArrayScanner(Scanner<byte[]> input, int maxOutputBytes){
		super(input);
		this.maxOutputBytes = maxOutputBytes;
		storage = null;
	}

	@Override
	public boolean advanceInternal(){
		current = new ArrayList<>();
		int size = 0;
		if(storage != null){
			current.add(storage);
			size = storage.length;
			storage = null;
		}
		while(true){
			var bytesList = input.take(1);
			if(bytesList.isEmpty()){
				break;//no more inputs
			}
			var bytes = bytesList.get(0);
			if(bytes.length > maxOutputBytes){//this won't fit
				throw new IllegalArgumentException(
						"encountered oversized input with size=" + size + " and maxOutputBytes=" + maxOutputBytes);
			}
			int resultSize = bytes.length + size;
			if(resultSize > maxOutputBytes || resultSize < bytes.length){//second check is to avoid overflow
				storage = bytes;//save for next advance
				break;
			}
			current.add(bytes);
			size += bytes.length;
		}
		return size > 0;
	}

}
