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

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;

public class QueueMessage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> extends BaseQueueMessage{

	private D databean;
	private Map<String,String> messageAttributes;

	public QueueMessage(byte[] handle, D databean, Map<String,String> messageAttributes){
		super(handle);
		this.databean = databean;
		this.messageAttributes = messageAttributes;
	}

	public D getDatabean(){
		return databean;
	}

	public Map<String,String> getMessageAttributes(){
		return messageAttributes;
	}

}
