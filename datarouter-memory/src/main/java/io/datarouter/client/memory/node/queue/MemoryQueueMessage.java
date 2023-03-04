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
package io.datarouter.client.memory.node.queue;

public class MemoryQueueMessage{

	private final String id;
	private final byte[] value;
	private Long visibilityExpirationMs;

	public MemoryQueueMessage(String id, byte[] value){
		this.id = id;
		this.value = value;
	}

	public String getId(){
		return id;
	}

	public byte[] getValue(){
		return value;
	}

	public void setVisibilityExpirationMs(long visibilityExpirationMs){
		this.visibilityExpirationMs = visibilityExpirationMs;
	}

	public void clearVisibilityExpiration(){
		visibilityExpirationMs = null;
	}

	public boolean isVisibilityExpired(){
		return visibilityExpirationMs == null
				? false
				: System.currentTimeMillis() > visibilityExpirationMs;
	}

}
