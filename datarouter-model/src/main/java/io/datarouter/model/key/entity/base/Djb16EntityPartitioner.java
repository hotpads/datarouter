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
package io.datarouter.model.key.entity.base;

import io.datarouter.model.field.FieldTool;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.util.HashMethods;

public class Djb16EntityPartitioner<EK extends EntityKey<EK>> extends BaseByteArrayEntityPartitioner<EK>{

	public Djb16EntityPartitioner(){
		super(HashMethods::longDjbHash, 16);
	}

	@Override
	public byte[] makeByteArrayHashInput(EK ek){
		return FieldTool.getPartitionerInput(ek.getFields());
	}

}
