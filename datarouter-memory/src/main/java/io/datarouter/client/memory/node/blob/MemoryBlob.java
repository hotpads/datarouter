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
package io.datarouter.client.memory.node.blob;

public class MemoryBlob{

	private final byte[] key;
	private final byte[] value;
	private final long createdMs;
	private final Long expirationMs;

	public MemoryBlob(byte[] key, byte[] value, Long ttlMs){
		this.key = key;
		this.value = value;
		createdMs = System.currentTimeMillis();
		expirationMs = ttlMs == null
				? null
				: createdMs + ttlMs;
	}

	public byte[] getKey(){
		return key;
	}

	public byte[] getValue(){
		return value;
	}

	public long getLength(){
		return value.length;
	}

	public boolean isExpired(){
		return expirationMs == null
				? false
				: System.currentTimeMillis() > expirationMs;
	}

	public boolean notExpired(){
		return !isExpired();
	}

}
