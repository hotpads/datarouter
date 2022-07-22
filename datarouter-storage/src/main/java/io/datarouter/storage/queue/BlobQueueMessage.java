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

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

/**
 * This is a placeholder to satisfy generic parameters
 */
public class BlobQueueMessage extends BaseDatabean<BlobQueueMessageKey,BlobQueueMessage>{

	public static class BlobQueueMessageFielder
	extends BaseDatabeanFielder<BlobQueueMessageKey,BlobQueueMessage>{

		public BlobQueueMessageFielder(){
			super(BlobQueueMessageKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(BlobQueueMessage databean){
			return List.of();
		}

	}

	public BlobQueueMessage(){
		super(new BlobQueueMessageKey());
	}

	@Override
	public Supplier<BlobQueueMessageKey> getKeySupplier(){
		return BlobQueueMessageKey::new;
	}

}
