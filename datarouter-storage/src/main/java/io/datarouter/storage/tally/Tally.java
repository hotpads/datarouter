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
package io.datarouter.storage.tally;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class Tally extends BaseDatabean<TallyKey,Tally>{

	private Long tally;

	public static class FieldKeys{
		public static final LongFieldKey tally = new LongFieldKey("tally");
	}

	public static class TallyFielder extends BaseDatabeanFielder<TallyKey,Tally>{

		public TallyFielder(){
			super(TallyKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(Tally databean){
			return List.of(new LongField(FieldKeys.tally, databean.tally));
		}

	}

	public Tally(){
		this((String)null, null);
	}

	public Tally(String id, Long tally){
		this(new TallyKey(id), tally);
	}

	public Tally(TallyKey key, Long tally){
		super(key);
		this.tally = tally;
	}

	@Override
	public Supplier<TallyKey> getKeySupplier(){
		return TallyKey::new;
	}

	public Long getTally(){
		return tally;
	}

}
