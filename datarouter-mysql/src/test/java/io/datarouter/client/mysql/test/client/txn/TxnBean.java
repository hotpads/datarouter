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
package io.datarouter.client.mysql.test.client.txn;

import java.util.List;
import java.util.function.Supplier;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class TxnBean extends BaseDatabean<TxnBeanKey,TxnBean>{

	public static class TxnBeanFielder extends BaseDatabeanFielder<TxnBeanKey,TxnBean>{

		public TxnBeanFielder(){
			super(TxnBeanKey::new);
		}

		@Override
		public List<Field<?>> getNonKeyFields(TxnBean bean){
			return List.of();
		}
	}

	public TxnBean(){
		super(new TxnBeanKey(null));
	}

	public TxnBean(String id){
		super(new TxnBeanKey(id));
	}

	@Override
	public Supplier<TxnBeanKey> getKeySupplier(){
		return TxnBeanKey::new;
	}

}
