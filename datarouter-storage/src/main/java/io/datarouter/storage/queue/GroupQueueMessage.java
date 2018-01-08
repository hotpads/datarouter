/**
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.util.collection.CollectionTool;

public class GroupQueueMessage<PK extends PrimaryKey<PK>,D extends Databean<PK,D>> extends BaseQueueMessage<PK,D>{

	private List<D> databeans;

	public GroupQueueMessage(byte[] handle, List<D> databeans){
		super(handle);
		this.databeans = databeans;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>> List<D> getDatabeans(
			Collection<GroupQueueMessage<PK,D>> messages){
		List<D> databeans = new ArrayList<>();
		for(GroupQueueMessage<PK,D> message : messages){
			databeans.addAll(message.getDatabeans());
		}
		return databeans;
	}

	public List<D> getDatabeans(){
		return databeans;
	}

	public static boolean isEmpty(GroupQueueMessage<?,?> message){
		return message == null || message.isEmpty();
	}

	public boolean isEmpty(){
		return CollectionTool.isEmpty(databeans);
	}

	public boolean notEmpty(){
		return !isEmpty();
	}
}
