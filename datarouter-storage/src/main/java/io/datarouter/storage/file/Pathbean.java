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
package io.datarouter.storage.file;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class Pathbean extends BaseDatabean<PathbeanKey,Pathbean>{

	private Long size;

	public static class FieldKeys{
		public static final LongFieldKey size = new LongFieldKey("size");
	}

	public static class PathbeanFielder extends BaseDatabeanFielder<PathbeanKey,Pathbean>{

		public PathbeanFielder(){
			super(PathbeanKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Pathbean databean){
			return List.of(
					new LongField(FieldKeys.size, databean.size));
		}

	}

	@Override
	public Supplier<PathbeanKey> getKeySupplier(){
		return PathbeanKey::new;
	}

	public Pathbean(){
		super(new PathbeanKey(null, null));
	}

	public Pathbean(PathbeanKey key){
		super(key);
	}

	public Pathbean(PathbeanKey key, Long size){
		super(key);
		this.size = size;
	}

	public Long getSize(){
		return size;
	}

}
