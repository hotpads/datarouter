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
package io.datarouter.storage.node;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;
import io.datarouter.util.ComparableTool;

public abstract class BaseNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
implements Node<PK,D,F>{

	private final DatabeanFieldInfo<PK,D,F> fieldInfo;

	public BaseNode(NodeParams<PK,D,F> params){
		try{
			this.fieldInfo = new DatabeanFieldInfo<>(params);
		}catch(Exception probablyNoPkInstantiated){
			throw new IllegalArgumentException("could not instantiate " + params.getClientName() + "." + params
					.getDatabeanName() + ". Check that the primary key is instantiated in the databean constructor.",
					probablyNoPkInstantiated);
		}
	}

	@Override
	public String toString(){
		return getName();
	}

	@Override
	public int compareTo(Node<PK,D,F> other){
		return ComparableTool.nullFirstCompareTo(getName(), other.getName());
	}

	@Override
	public DatabeanFieldInfo<PK,D,F> getFieldInfo(){
		return fieldInfo;
	}

}
