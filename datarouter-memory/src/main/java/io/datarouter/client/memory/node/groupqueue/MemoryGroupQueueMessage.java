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
package io.datarouter.client.memory.node.groupqueue;

import java.util.List;

public class MemoryGroupQueueMessage{

	private final String id;
	private final List<byte[]> values;
	private Long visibilityExpirationMs;

	public MemoryGroupQueueMessage(String id, List<byte[]> values){
		this.id = id;
		this.values = values;
	}

	public String getId(){
		return id;
	}

	public List<byte[]> getValues(){
		return values;
	}

	public void setVisibilityExpirationMs(long visiblityExpirationMs){
		this.visibilityExpirationMs = visiblityExpirationMs;
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
