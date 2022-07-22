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
package io.datarouter.filesystem.raw.queue;

import io.datarouter.bytes.codec.stringcodec.StringCodec;

public class DirectoryQueueMessage{

	public final String id;
	public final byte[] content;

	public DirectoryQueueMessage(String id, byte[] content){
		this.id = id;
		this.content = content;
	}

	public byte[] getIdUtf8Bytes(){
		return StringCodec.UTF_8.encode(id);
	}

	public String getContentUtf8(){
		return StringCodec.UTF_8.decode(content);
	}

}
