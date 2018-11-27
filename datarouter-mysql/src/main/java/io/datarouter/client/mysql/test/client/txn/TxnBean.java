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

import java.util.Arrays;
import java.util.List;

import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;

public class TxnBean extends BaseDatabean<TxnBeanKey,TxnBean>{

	private TxnBeanKey key;

	public static class TxnBeanFielder extends BaseDatabeanFielder<TxnBeanKey,TxnBean>{
		public TxnBeanFielder(){
			super(TxnBeanKey.class);
		}
		@Override
		public List<Field<?>> getNonKeyFields(TxnBean bean){
			return Arrays.asList();
		}
	}

	public TxnBean(){
		this.key = new TxnBeanKey(null);
	}

	public TxnBean(String id){
		this.key = new TxnBeanKey(id);
	}

	@Override
	public Class<TxnBeanKey> getKeyClass(){
		return TxnBeanKey.class;
	}

	@Override
	public TxnBeanKey getKey(){
		return key;
	}

	public void setKey(TxnBeanKey key){
		this.key = key;
	}

	public String getId(){
		return key.getId();
	}


	public void setId(String id){
		key.setId(id);
	}

}
